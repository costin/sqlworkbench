/*
 * TextRowDataConverter.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2004, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: info@sql-workbench.net
 *
 */
package workbench.db.exporter;

import java.text.SimpleDateFormat;

import workbench.storage.ResultInfo;
import workbench.storage.RowData;
import workbench.util.StrBuffer;
import workbench.util.StringUtil;

/**
 *
 * @author  info@sql-workbench.net
 */
public class TextRowDataConverter
	extends RowDataConverter
{
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat timestampFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SS");
	private String delimiter = "\t";
	private String quoteCharacter = "\"";
	private boolean writeHeader = true;
	private boolean cleanCR = false;

	public TextRowDataConverter(ResultInfo info)
	{
		super(info);
	}

	public void setCleanNonPrintable(boolean flag)
	{
		this.cleanCR = flag;
	}

	public StrBuffer convertData()
	{
		return null;
	}

	public StrBuffer getEnd(long totalRows)
	{
		return null;
	}

	public String getFormatName()
	{
		return "Text";
	}

	public StrBuffer convertRowData(RowData row, long rowIndex)
	{
		int count = this.metaData.getColumnCount();
		StrBuffer result = new StrBuffer(count * 30);
		for (int c=0; c < count; c ++)
		{
			String value = this.getValueAsFormattedString(row, c);
			if (this.cleanCR)
			{
				value = StringUtil.cleanNonPrintable(value);
			}
			if (value == null) value = "";
			boolean needQuote = (value.indexOf(this.delimiter) > -1);
			if (needQuote) result.append(this.quoteCharacter);

			result.append(value);

			if (needQuote) result.append(this.quoteCharacter);

			if (c < count - 1) result.append(this.getDelimiter());
		}
		result.append("\n");
		return result;
	}

	public StrBuffer getStart()
	{
		if (!this.isWriteHeader()) return null;

		StrBuffer result = new StrBuffer();
		int colCount = this.metaData.getColumnCount();
		for (int c=0; c < colCount; c ++)
		{
			String name = this.metaData.getColumnName(c);
			result.append(name);
			if (c < colCount - 1) result.append(this.getDelimiter());
		}
		result.append("\n");
		return result;
	}

	public String getDelimiter()
	{
		return delimiter;
	}

	public void setDelimiter(String delimit)
	{
		if (delimit.equals("\\t"))
		{
			this.delimiter = "\t";
		}
		else
		{
			this.delimiter = delimit;
		}
	}

	public String getQuoteCharacter()
	{
		return quoteCharacter;
	}

	public void setQuoteCharacter(String quoteCharacter)
	{
		this.quoteCharacter = quoteCharacter;
	}

	public boolean isWriteHeader()
	{
		return writeHeader;
	}

	public void setWriteHeader(boolean writeHeader)
	{
		this.writeHeader = writeHeader;
	}

}