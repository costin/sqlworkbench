/*
 * TextFileParser.java
 *
 * Created on November 22, 2003, 3:04 PM
 */

package workbench.db.importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import workbench.db.ColumnIdentifier;

import workbench.db.DbMetadata;
import workbench.db.WbConnection;
import workbench.log.LogMgr;
import workbench.storage.DataStore;
import workbench.util.ValueConverter;
import workbench.util.WbStringTokenizer;

/**
 *
 * @author  workbench@kellerer.org
 */
public class TextFileParser
	implements RowDataProducer
{
	private String filename;
	private String tableName;
	private String encoding = "8859_1";
	private String delimiter = "\t";
	private String quoteChar = "\"";

	private int colCount = -1;
	private ColumnIdentifier[] columns;
	private Object[] rowData;

	private boolean withHeader = true;
	private boolean cancelImport = false;
	private RowDataReceiver receiver;
	private String dateFormat;
	private String timestampFormat;
	private char decimalChar;

	private WbConnection connection;

	private ValueConverter converter;

	/** Creates a new instance of TextFileParser */
	public TextFileParser(String aFile)
	{
		this.filename = aFile;
	}

	public void setReceiver(RowDataReceiver rec)
	{
		this.receiver = rec;
	}
	public void setTableName(String aName)
	{
		this.tableName = aName;
	}

	public void setColumns(List columnList)
		throws Exception
	{
		if (columnList == null || columnList.size()  == 0) return;
		this.readColumnDefinitions(columnList);
	}

	public void setConnection(WbConnection aConn)
	{
		this.connection = aConn;
	}
	public void setEncoding(String enc)
	{
		if (enc == null) return;
		this.encoding = enc;
	}

	public void setDelimiter(String delimit)
	{
		this.delimiter = delimit;
		if ("\\t".equals(this.delimiter))
		{
			this.delimiter = "\t";
		}
	}

	private boolean doCancel()
	{
		Thread.yield();
		return this.cancelImport;
	}

	public void cancel()
	{
		this.cancelImport = true;
	}

	public void setDateFormat(String aFormat)
	{
		this.dateFormat = aFormat;
	}

	public void setTimeStampFormat(String aFormat)
	{
		this.timestampFormat = aFormat;
	}

	public void setContainsHeader(boolean aFlag)
	{
		this.withHeader = aFlag;
	}

	public void setQuoteChar(String aChar)
	{
		this.quoteChar = aChar;
	}

	public void setDecimalChar(String aChar)
	{
		if (aChar != null && aChar.trim().length() > 0)
		{
			this.decimalChar = aChar.trim().charAt(0);
		}
	}

	public void start()
		throws Exception
	{
		this.cancelImport = false;
		File f = new File(this.filename);
		long fileSize = f.length();

		InputStream inStream = new FileInputStream(f);
		BufferedReader in = new BufferedReader(new InputStreamReader(inStream, this.encoding),1024*256);

		this.converter = new ValueConverter(this.dateFormat, this.timestampFormat);
		this.converter.setDecimalCharacter(this.decimalChar);

		String line;
		List lineData;
		Object colData;
		int col;
		int row;

		try
		{
			line = in.readLine();
			if (this.withHeader && this.columns == null)
			{
				this.readColumns(line);
			}
			line = in.readLine();
		}
		catch (IOException e)
		{
			line = null;
		}

		if (this.colCount <= 0)
		{
			throw new Exception("Cannot import file without a column definition");
		}

		this.receiver.setTargetTable(this.tableName, this.columns);

		lineData = new ArrayList(this.colCount);
		Object value = null;
		this.rowData = new Object[this.colCount];
		WbStringTokenizer tok = new WbStringTokenizer(delimiter.charAt(0), "", false);
		int importRow = 0;

		while (line != null)
		{
			if (this.doCancel()) break;

			this.clearRowData();
			lineData.clear();

			tok.setSourceString(line);
			while (tok.hasMoreTokens())
			{
				lineData.add(tok.nextToken());
			}

			importRow ++;

			for (int i=0; i < this.colCount; i++)
			{
				try
				{
					value = lineData.get(i);
					if (value != null)
					{
						rowData[i] = converter.convertValue(value, this.columns[i].getDataType());
					}
				}
				catch (Exception e)
				{
					LogMgr.logWarning("TextFileParser.parse()","Error in line=" + importRow + "reading col=" + i + ",value=" + value, e);
					rowData[i] = null;
				}
			}

			if (this.doCancel()) break;

			this.receiver.processRow(rowData);

			try
			{
				line = in.readLine();
			}
			catch (IOException e)
			{
				line = null;
			}

			if (this.doCancel()) break;
		}

		try { in.close(); } catch (IOException e) {}

		if (!this.cancelImport)
		{
			this.receiver.importFinished();
		}
		else
		{
			this.receiver.importCancelled();
		}
	}

	private void clearRowData()
	{
		for (int i=0; i < this.colCount; i++)
		{
			this.rowData[i] = null;
		}
	}

	private void readColumns(String headerLine)
		throws Exception
	{
		List cols = new ArrayList();
		WbStringTokenizer tok = new WbStringTokenizer(delimiter.charAt(0), this.quoteChar, false);
		tok.setSourceString(headerLine);
		while (tok.hasMoreTokens())
		{
			String column = tok.nextToken();
			cols.add(column.toUpperCase());
		}
		this.readColumnDefinitions(cols);
	}

	private void readColumnDefinitions(List cols)
		throws Exception
	{
		try
		{
			this.colCount = cols.size();
			this.columns = new ColumnIdentifier[this.colCount];

			for (int i=0; i < this.colCount; i++)
			{
				this.columns[i] = new ColumnIdentifier((String)cols.get(i));
			}
			DbMetadata meta = this.connection.getMetadata();
			DataStore ds = meta.getTableDefinition(this.tableName);
			int tableCols = ds.getRowCount();

			for (int i=0; i < tableCols; i++)
			{
				String column = ds.getValueAsString(i, DbMetadata.COLUMN_IDX_TABLE_DEFINITION_COL_NAME).toUpperCase();
				int index = cols.indexOf(column);
				if (index >= 0)
				{
					this.columns[i].setDataType(ds.getValueAsInt(i, DbMetadata.COLUMN_IDX_TABLE_DEFINITION_DATA_TYPE, ColumnIdentifier.NO_TYPE));
				}
			}
		}
		catch (Exception e)
		{
			this.colCount = -1;
			this.columns = null;
		}
	}


}