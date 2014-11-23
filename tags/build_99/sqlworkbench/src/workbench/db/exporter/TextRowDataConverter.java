/*
 * TextRowDataConverter.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2008, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db.exporter;

import java.io.File;
import workbench.log.LogMgr;
import workbench.storage.RowData;
import workbench.util.CharacterRange;
import workbench.util.QuoteEscapeType;
import workbench.util.SqlUtil;
import workbench.util.StrBuffer;
import workbench.util.StringUtil;

/**
 *
 * @author  support@sql-workbench.net
 */
public class TextRowDataConverter
	extends RowDataConverter
{
	private String delimiter = "\t";
	private String quoteCharacter = null;
	private boolean quoteAlways = false;
	private CharacterRange escapeRange = null;
	private String additionalEncodeCharacters = null;
	private String delimiterAndQuote = null;
	private String lineEnding = StringUtil.LINE_TERMINATOR;
	private boolean writeBlobFiles = true;
	private boolean writeClobFiles = false;
	private QuoteEscapeType quoteEscape = QuoteEscapeType.none;
	
	public TextRowDataConverter()
	{
		super();
	}

	public void setWriteClobToFile(boolean flag)
	{
		this.writeClobFiles = flag;
	}
	
	public void setWriteBlobToFile(boolean flag)
	{
		writeBlobFiles = flag;
	}
	
	public StrBuffer getEnd(long totalRows)
	{
		return null;
	}

	public void setQuoteEscaping(QuoteEscapeType type)
	{
		this.quoteEscape = type;
	}
	
	public QuoteEscapeType getQuoteEscaping()
	{
		return this.quoteEscape;
	}
	
	public StrBuffer convertRowData(RowData row, long rowIndex)
	{
		int count = this.metaData.getColumnCount();
		StrBuffer result = new StrBuffer(count * 30);
		boolean canQuote = this.quoteCharacter != null;
		int currentColIndex = 0;

		for (int c=0; c < count; c ++)
		{
			if (!this.includeColumnInExport(c)) continue;
			if (currentColIndex > 0)
			{
				result.append(this.delimiter);
			}
			int colType = this.metaData.getColumnType(c);
			String value = null;
			
			boolean addQuote = quoteAlways;
			
			if (writeBlobFiles && SqlUtil.isBlobType(colType))
			{
				File blobFile = createBlobFile(row, c, rowIndex);
				
				value = blobFile.getName();
				try
				{
					writeBlobFile(row.getValue(c), blobFile);
				}
				catch (Exception e)
				{
					LogMgr.logError("TextRowDataConverter.convertRowData", "Error writing BLOB file", e);
					throw new RuntimeException("Error writing BLOB file", e);
				}
				
			}
			else if (writeClobFiles && SqlUtil.isClobType(colType, this.originalConnection.getDbSettings()))
			{
				Object clobData = row.getValue(c);
				if (clobData != null)
				{
					File clobFile = createBlobFile(row, c, rowIndex);
					value = clobFile.getName();
					try
					{
						String s = clobData.toString();
						writeClobFile(s, clobFile, this.encoding);
					}
					catch (Exception e)
					{
						throw new RuntimeException("Error writing CLOB file", e);
					}
				}
			}
			else 
			{
				value = this.getValueAsFormattedString(row, c);
			}
			
			if (value == null) value = "";

			if (SqlUtil.isCharacterType(colType))
			{
				boolean containsDelimiter = value.indexOf(this.delimiter) > -1;
				addQuote = (this.quoteAlways || (canQuote && containsDelimiter));
				
				if (this.escapeRange != null && this.escapeRange != CharacterRange.RANGE_NONE)
				{
					if (addQuote)
					{
						value = StringUtil.escapeUnicode(value, this.quoteCharacter, this.escapeRange);
					}
					else
					{
						value = StringUtil.escapeUnicode(value, this.delimiterAndQuote, this.escapeRange);
					}
				}
				if (this.quoteCharacter != null && this.quoteEscape != QuoteEscapeType.none && value.indexOf(this.quoteCharacter) > -1)
				{
					if (this.quoteEscape == QuoteEscapeType.escape)
					{
						value = StringUtil.replace(value, this.quoteCharacter, "\\" + this.quoteCharacter);
					}
					else
					{
						value = StringUtil.replace(value, this.quoteCharacter, this.quoteCharacter + this.quoteCharacter);
					}
				}
			}

			if (addQuote) result.append(this.quoteCharacter);
			
			result.append(value);

			if (addQuote) result.append(this.quoteCharacter);

			currentColIndex ++;
		}
		result.append(lineEnding);
		return result;
	}

	public void setLineEnding(String ending)
	{
		if (ending != null) this.lineEnding = ending;
	}

	public StrBuffer getStart()
	{
		this.setAdditionalEncodeCharacters();

		if (!this.writeHeader) return null;

		StrBuffer result = new StrBuffer();
		int colCount = this.metaData.getColumnCount();
		boolean first = true;
		for (int c=0; c < colCount; c ++)
		{
			if (!this.includeColumnInExport(c)) continue;
			String name = this.metaData.getColumnName(c);
			if (first) 
			{
				first = false;
			}
			else
			{
				result.append(this.delimiter);
			}
			result.append(name);
		}
		result.append(lineEnding);
		return result;
	}

	public void setDelimiter(String delimit)
	{
		if (delimit == null || delimit.trim().length() == 0) return;

		if (delimit.equals("\\t"))
		{
			this.delimiter = "\t";
		}
		else
		{
			this.delimiter = delimit;
		}
		setAdditionalEncodeCharacters();
	}

	private void setAdditionalEncodeCharacters()
	{
		if (this.escapeRange == null) return;
		if (this.quoteCharacter == null && this.delimiter == null) return;

		this.delimiterAndQuote = this.delimiter;
		// If values should always be quoted, then we need to
		// escape the quote character in values
		if (this.quoteCharacter != null)
		{
			this.additionalEncodeCharacters = this.quoteCharacter;
			this.delimiterAndQuote += this.quoteCharacter;
		}
		
	}

	public void setQuoteCharacter(String quote)
	{
		if (quote != null && quote.trim().length() > 0)
		{
			this.quoteCharacter = quote;
			setAdditionalEncodeCharacters();
		}
	}

	public void setQuoteAlways(boolean flag)
	{
		this.quoteAlways = flag;
	}

	/**
	 *	Define the range of characters to be escaped 
	 *  @see workbench.util.StringUtil
	 */
	public void setEscapeRange(CharacterRange range)
	{
		this.escapeRange = range;
	}

}