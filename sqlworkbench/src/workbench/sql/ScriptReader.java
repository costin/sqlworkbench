/*
 * Created on December 8, 2002, 12:18 PM
 */
package workbench.sql;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 *
 * @author  workbench@kellerer.org
 */
public class ScriptReader
{
	private Reader content;
	//private String delimiter;
	private char[] delimiter;
	//private char[] delimit;
	private int delimitLen;
	
	public ScriptReader(String aDelimiter) throws IOException
	{
		this.delimiter = aDelimiter.trim().toUpperCase().toCharArray();
		this.delimitLen = delimiter.length;
	}
	
	public void setScript(String aScript)
	{
		this.content = new StringReader(aScript);
	}
	
	public void setScriptFilename(String aFilename)
		throws IOException, FileNotFoundException
	{
		this.content = new BufferedReader(new FileReader(aFilename));
	}
	
	public String getNextStatement()
		throws IOException
	{
		boolean quoteOn = false;
		
		char[] currChar = new char[this.delimitLen];

		int numChars = this.content.read(currChar);
		
		if (numChars == -1) return null;
		
		StringBuffer result = new StringBuffer(200);
		
		while (numChars != -1)
		{
			
			for (int i=0; i < numChars; i++)
			{
				if (currChar[i] == '\'' || currChar[i] == '\"')
				{
					quoteOn = !quoteOn;
				}
			}

			if (!quoteOn)
			{
				boolean isDelimiter = true;
				for (int i=0; i < this.delimitLen; i++)
				{
					if (currChar[i] != this.delimiter[i]) 
					{
						isDelimiter = false;
						break;
					}
				}
				if (isDelimiter) 
					break;
				else
					result.append(currChar);
			}
			else
			{
				result.append(currChar);
			}
			numChars = this.content.read(currChar);
		}
		return result.toString().trim();
	}
	
	public void close()
		throws IOException
	{
		this.content.close();
	}
	
	public static void main(String[] args)
	{
		try
		{
			ScriptReader reader = new ScriptReader(";");
			reader.setScriptFilename("d:/projects/java/jworkbench/sql/test.sql");
			String cmd = reader.getNextStatement();
			while (cmd != null)
			{
				System.out.println(cmd);
				System.out.println("------------");
				cmd = reader.getNextStatement();
			}
			reader.close();	
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
