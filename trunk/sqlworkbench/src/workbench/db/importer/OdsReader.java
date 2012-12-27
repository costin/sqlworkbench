/*
 * OdsReader.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2013, Thomas Kellerer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at.
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db.importer;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import workbench.log.LogMgr;

import workbench.util.CollectionUtil;

import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Row;
import org.odftoolkit.simple.table.Table;

/**
 *
 * @author Thomas Kellerer
 */
public class OdsReader
	implements SpreadsheetReader
{
	private String nullIndicator;
	private File inputFile;
	private SpreadsheetDocument dataFile;
	private Table worksheet;
	private int worksheetIndex = 0;
	private List<String> headerColumns;
	private final Set<String> tsFormats = CollectionUtil.treeSet("HH", "mm", "ss", "SSS", "KK", "kk");
	
	public OdsReader(File f, int sheetIndex)
	{
		inputFile = f;
		worksheetIndex = sheetIndex;
	}

	@Override
	public List<String> getHeaderColumns()
	{
		if (headerColumns == null)
		{
			Row rowData = worksheet.getRowByIndex(0);
			int colCount = rowData.getCellCount();
			headerColumns = new ArrayList<String>(colCount);
			for (int i=0; i < colCount; i++)
			{
				Cell cell = rowData.getCellByIndex(i);
				String title = cell.getDisplayText();

				if (title != null)
				{
					headerColumns.add(title.toString());
				}
				else
				{
					headerColumns.add("Col" + Integer.toString(i));
				}
			}
		}
		return headerColumns;
	}

	@Override
	public void setActiveWorksheet(int index)
	{
		worksheetIndex = index;
		if (dataFile != null)
		{
			worksheet = dataFile.getSheetByIndex(worksheetIndex);
		}
	}

	private boolean isTimestampFormat(String format)
	{
		for (String key : tsFormats)
		{
			if (format.contains(key)) return true;
		}
		return false;
	}

	@Override
	public List<Object> getRowValues(int row)
	{
		Row rowData = worksheet.getRowByIndex(row);
		int colCount = rowData.getCellCount();
		List<Object> result = new ArrayList<Object>(colCount);
		for (int col=0; col < colCount; col++)
		{
			Cell cell = rowData.getCellByIndex(col);
			String type = cell.getValueType();
			Object value = null;
			if ("boolean".equals(type))
			{
				value = cell.getBooleanValue();
			}
			else if ("date".equals(type) || "time".equals(type))
			{
				String fmt = cell.getFormatString();
				String text = cell.getDisplayText();
				try
				{
					SimpleDateFormat formatter = new SimpleDateFormat(fmt);
					java.util.Date udt = formatter.parse(text);
					if (isTimestampFormat(fmt))
					{
						value = new java.sql.Timestamp(udt.getTime());
					}
					else
					{
						value = new java.sql.Date(udt.getTime());
					}
				}
				catch (Exception ex)
				{
					LogMgr.logError("OdsReader.getRowValues()", "Could not parse date format: " + fmt, ex);
					Calendar cal = null;
					if ("time".equals(type))
					{
						cal = cell.getTimeValue();
					}
					else
					{
						cal = cell.getDateValue();
					}
					if (cal != null)
					{
						value = cal.getTime();
					}
				}
			}
			else if ("float".equals(type))
			{
				value = cell.getDoubleValue();
			}
			else if ("currency".equals(type))
			{
				value = cell.getCurrencyValue();
			}
			else
			{
				value = cell.getStringValue();
			}

			if (value != null && nullIndicator != null && value.equals(nullIndicator))
			{
				value = null;
			}
			result.add(value);
		}
		return result;
	}

	@Override
	public void setNullString(String nullString)
	{
		nullIndicator = nullString;
	}

	@Override
	public int getRowCount()
	{
		return worksheet.getRowCount();
	}

	@Override
	public void done()
	{
		if (dataFile != null)
		{
			dataFile.close();
		}
		dataFile = null;
		worksheet = null;
	}

	@Override
	public void load()
		throws IOException
	{
		try
		{
			dataFile = SpreadsheetDocument.loadDocument(inputFile);
			worksheet = dataFile.getSheetByIndex(worksheetIndex);
		}
		catch (Exception ex)
		{
			throw new IOException("Could not load file " + inputFile.getAbsolutePath(), ex);
		}
	}

	@Override
	public List<String> getSheets()
	{
		int sheetCount = dataFile.getSheetCount();
		List<String> result = new ArrayList<String>(sheetCount);
		for (int i=0; i < sheetCount; i ++)
		{
			String name = dataFile.getSheetByIndex(i).getTableName();
			result.add(name);
		}
		return result;
	}

}
