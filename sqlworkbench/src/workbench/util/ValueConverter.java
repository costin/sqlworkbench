/*
 * ValueConverter.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2004, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: info@sql-workbench.net
 *
 */
package workbench.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;

/**
 * Utility class to parse String into approriate Java classes according
 * to a type from java.sql.Types.
 *
 * @author  info@sql-workbench.net
 */
public class ValueConverter
{
	/**
	 *	Often used date formats which are tried when parsing a Date
	 *  or a TimeStamp column
	 */
	private static final String[] dateFormats = new String[] {
														"yyyy-MM-dd HH:mm:ss",
														"dd.MM.yyyy HH:mm:ss",
														"MM/dd/yy HH:mm:ss",
														"MM/dd/yyyy HH:mm:ss",
														"yyyy-MM-dd",
														"dd.MM.yyyy",
														"MM/dd/yy",
														"MM/dd/yyyy",
														"dd-MMM-yyyy"
													};

	private String defaultDateFormat;
	private String defaultTimestampFormat;
	private char decimalCharacter = '.';
	private SimpleDateFormat formatter = new SimpleDateFormat();

	public ValueConverter()
	{
	}

	public ValueConverter(String aDateFormat, String aTimeStampFormat)
	{
		this.setDefaultDateFormat(aDateFormat);
		this.setDefaultTimestampFormat(aTimeStampFormat);
	}

	public void setDefaultDateFormat(String aFormat)
	{
		this.defaultDateFormat = aFormat;
	}

	public void setDefaultTimestampFormat(String aFormat)
	{
		this.defaultTimestampFormat = aFormat;
	}

	public void setDecimalCharacter(char aChar)
	{
		this.decimalCharacter = aChar;
	}

	public Object convertValue(Object aValue, int type)
		throws Exception
	{
		if (aValue == null) return null;

		switch (type)
		{
			case Types.BIGINT:
				if (aValue.toString().length() == 0) return null;
				return new BigInteger(aValue.toString());
			case Types.INTEGER:
			case Types.SMALLINT:
			case Types.TINYINT:
				if (aValue.toString().length() == 0) return null;
				return new Integer(aValue.toString());
			case Types.NUMERIC:
			case Types.DECIMAL:
			case Types.DOUBLE:
			case Types.REAL:
			case Types.FLOAT:
				if (aValue.toString().length() == 0) return null;
				return new BigDecimal(this.adjustDecimalString(aValue.toString()));
			case Types.CHAR:
			case Types.VARCHAR:
				if (aValue instanceof String)
					return aValue;
				else
					return aValue.toString();
			case Types.DATE:
				if (aValue.toString().length() == 0) return null;
				return this.parseDate((String)aValue);
			case Types.TIMESTAMP:
				if (aValue.toString().length() == 0) return null;
				java.sql.Date d = this.parseDate((String)aValue);
				if (d == null) throw new Exception("Could not parse date " + aValue);
				Timestamp t = new Timestamp(d.getTime());
				return t;
			default:
				return aValue;
		}
	}

	public String getDatePattern()
	{
		return this.defaultDateFormat;
	}

	public String getTimestampPattern()
	{
		return this.defaultTimestampFormat;
	}

  public java.sql.Date parseDate(String aDate)
  {
		java.util.Date result = null;
		if (this.defaultDateFormat != null)
		{
			this.formatter.applyPattern(this.defaultDateFormat);
			try
			{
				result = this.formatter.parse(aDate);
			}
			catch (Exception e)
			{
				result = null;
			}
		}

		if (result == null && this.defaultTimestampFormat != null)
		{
			this.formatter.applyPattern(this.defaultTimestampFormat);
			try
			{
				result = this.formatter.parse(aDate);
			}
			catch (Exception e)
			{
				result = null;
			}
		}

		if (result == null)
		{
			for (int i=0; i < dateFormats.length; i++)
			{
				try
				{
					this.formatter.applyPattern(dateFormats[i]);
					result = this.formatter.parse(aDate);
					break;
				}
				catch (Exception e)
				{
					result = null;
				}
			}
		}

		if (result != null)
		{
			return new java.sql.Date(result.getTime());
		}
		return null;
  }

	//private static Pattern DECIMAL = Pattern.compile("^[-+]?\\d+\\.?\\d*e?\\d*$");

	private String adjustDecimalString(String input)
	{
		if (input == null)  return input;
		String value = input.trim();
		int len = value.length();
		if (len == 0) return value;
		StringBuffer result = new StringBuffer(len);
		int pos = value.lastIndexOf(this.decimalCharacter);
		for (int i=0; i < len; i++)
		{
			char c = value.charAt(i);
			if (i == pos)
			{
				// replace the decimal char with a . as that is required by BigDecimal(String)
				result.append('.');
			}
			// filter out everything but valid number characters
			else if ("+-0123456789eE".indexOf(c) > -1)
			{
				result.append(c);
			}
		}

		return result.toString();
	}

	public static void main(String[] args)
	{
		String input = "$1.234,5";
		ValueConverter convert = new ValueConverter();
		convert.setDecimalCharacter(',');
		input = convert.adjustDecimalString(input);
		System.out.println("input=" + input);
	}

}