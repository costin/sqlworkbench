/*
 * DataStoreExporter.java
 *
 * Created on 12. August 2004, 22:07
 */

package workbench.db.exporter;

import java.sql.Clob;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import workbench.db.TableIdentifier;
import workbench.db.WbConnection;
import workbench.log.LogMgr;
import workbench.util.StrBuffer;
import workbench.storage.*;

/**
 * Interface for classes that can take objects of type {@link RowData}
 * and convert them to e.g. text, XML, HTML
 *
 * @author  workbench@kellerer.org
 */
public abstract class RowDataConverter
{
	protected String encoding = "UTF-8";
	protected WbConnection originalConnection;
	protected String generatingSql;
	protected ResultInfo metaData;
	
	private DateFormat defaultDateFormatter;
	private DecimalFormat defaultNumberFormatter;
	private DateFormat defaultTimestampFormatter;

	/**
	 *	The metadata for the result set that should be exported
	 */
	public RowDataConverter(ResultInfo meta)
	{
		this.metaData = meta;
	}


	/**
	 *	The connection that was used to generate the source data.
	 */
	public void setOriginalConnection(WbConnection conn)
	{
		this.originalConnection = conn;
	}
	
	/**
	 *	The SQL statement that was used to generate the data.
	 */
	public void setGeneratingSql(String sql)
	{
		this.generatingSql = sql;
	}
	
	/**
	 *	Set the encoding for the output string.
	 *	This might not be used by all implemented Converters
	 *	The default encoding is UTF-8
	 */
	public void setEncoding(String enc)
	{
		this.encoding = enc;
	}
	
	/**
	 *	Returns a display name for this exporter
	 */
	public abstract String getFormatName();
	
	/**
	 *	Returns the data from the source as one String
	 *  in the format of this exporter.
	 *	This is equivalent to concatenating the output from:
	 *	#getStart();
	 *  #converRowData(RowData) for all rows
	 *  #getEnd();
	 */
	public abstract StrBuffer convertData();
	
	
	/**
	 *	Returns the data for one specific row as a String in the 
	 *  correct format
	 */
	public abstract StrBuffer convertRowData(RowData row, int rowIndex);
	
	/**
	 *	Returns the String sequence needed in before the actual data part.
	 *  (might be an empty string)
	 */
	public abstract StrBuffer getStart();
	
	/**
	 *	Returns the String sequence needed in before the actual data part.
	 *  (might be an empty string)
	 */
	public abstract StrBuffer getEnd();
	
	public void setDefaultTimestampFormatter(DateFormat formatter)
	{
		this.defaultTimestampFormatter = formatter;
	}
	
	public void setDefaultDateFormatter(DateFormat formatter)
	{
		this.defaultDateFormatter = formatter;
	}

	public void setDefaultNumberFormatter(DecimalFormat formatter)
	{
		this.defaultNumberFormatter = formatter;
	}

	public void setDefaultDateFormat(String format)
	{
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		this.setDefaultDateFormatter(formatter);
	}
	
	public void setDefaultTimestampFormat(String format)
	{
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		this.setDefaultTimestampFormatter(formatter);
	}

	public void setDefaultNumberFormat(String aFormat)
	{
		if (aFormat == null) return;
		try
		{
			this.defaultNumberFormatter = new DecimalFormat(aFormat);
		}
		catch (Exception e)
		{
			this.defaultNumberFormatter = null;
			LogMgr.logWarning("RowDataConverter.setDefaultDateFormat()", "Could not create decimal formatter for format " + aFormat);
		}
	}
	
	/**
	 * Return the column's value as a formatted String.
	 * Especially for Date objects this is different then getValueAsString()
	 * as a default formatter can be defined.
	 * @param aRow The requested row
	 * @param aColumn The column in aRow for which the value should be formatted
	 * @return The formatted value as a String
	 * @see #setDefaultDateFormatter(SimpleDateFormat)
	 * @see #setDefaultTimestampFormatter(SimpleDateFormat)
	 * @see #setDefaultNumberFormatter(SimpleDateFormat)
	 * @see #setDefaultDateFormat(String)
	 * @see #setDefaultTimestampFormat(String)
	 * @see #setDefaultNumberFormat(String)
	 */
	public String getValueAsFormattedString(RowData row, int col)
		throws IndexOutOfBoundsException
	{
		Object value = row.getValue(col);
    if (value == null || value instanceof NullValue)
		{
      return null;
		}
    else
		{
			String result = null;
			if (value instanceof java.sql.Timestamp && this.defaultTimestampFormatter != null)
			{
				result = this.defaultTimestampFormatter.format(value);
			}
			else if (value instanceof java.util.Date && this.defaultDateFormatter != null)
			{
				result = this.defaultDateFormatter.format(value);
			}
			else if (value instanceof Number && this.defaultNumberFormatter != null)
			{
				result = this.defaultNumberFormatter.format(value);
			}
			else if (value instanceof Clob)
			{
				try
				{
					Clob lob = (Clob)value;
					long len = lob.length();
					return lob.getSubString(1, (int)len);
				}
				catch (SQLException e)
				{
					return "";
				}
			}
			else
			{
				result = value.toString();
			}
      return result;
		}
	}
	
}
