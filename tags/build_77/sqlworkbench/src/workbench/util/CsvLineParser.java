/*
 * CsvLineParser.java
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

/**
 * A class to efficiently parse a delimited line of data. 
 * A quoted delimiter is recognized, line data spanning multiple lines (i.e.
 * data with embedded \n) is not recognized
 * @author  info@sql-workbench.net
 */
public class CsvLineParser
{
	private String lineData = null;
	private int len = 0;
	private int current = 0;
	private char delimiter;
	private char quoteChar = 0;
	
	public CsvLineParser(char delimit)
	{
		this.delimiter = delimit;
	}
	
	public CsvLineParser(char delimit, char quote)
	{
		this.delimiter = delimit;
		this.quoteChar = quote;
	}
	
	public CsvLineParser(String line, char delimit, char quote)
	{
		this.setLine(line);
		this.delimiter = delimit;
		this.quoteChar = quote;
	}
	
	public void setLine(String line)
	{
		this.lineData = line;
		this.len = this.lineData.length();
		this.current = 0;
	}
	
	public boolean hasNext()
	{
		return current < len;
	}
	
	public String getNext()
	{
		int beginField, endField;
		
		beginField = current;
		boolean inQuotes = false;
		int endOffset = 0;
		while (current < len)
		{
			char c = this.lineData.charAt(current);
			if (!inQuotes && (c == delimiter))
			{
				break;
			}
			if (c == this.quoteChar) 
			{
				// don't return the quote at the end
				if (inQuotes) endOffset = 1;
				
				// don't return the quote at the beginning
				if (current == beginField) beginField ++;
				inQuotes = !inQuotes;
			}
			current ++;
		}
		
		String next = this.lineData.substring(beginField, current - endOffset);
		this.current ++; // skip the delimiter
		
		return next;
	}
	
	public static void main(String args[])
	{
		try
		{
			CsvLineParser p = new CsvLineParser('\t');
			p.setLine("field1\tDazu sagt A. immer: \"sch�n\"\tfield3");
			while (p.hasNext())
				System.out.println(p.getNext());
		}
		catch (Throwable th)
		{
			th.printStackTrace();
		}
		System.out.println("Done.");
	}
	
}
