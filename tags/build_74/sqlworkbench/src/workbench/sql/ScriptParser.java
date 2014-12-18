/*
 * ScriptParser.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2004, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: info@sql-workbench.net
 *
 */
package workbench.sql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import workbench.log.LogMgr;
import workbench.resource.Settings;
import workbench.util.SqlUtil;
import workbench.util.StrBuffer;
import workbench.util.StringUtil;

/**
 *
 * @author  info@sql-workbench.net
 */
public class ScriptParser
{

	private String originalScript = null;
	private ArrayList commands = null;
	private ArrayList commandBoundaries = null;
	private String delimiter = ";";
	private int delimiterLength = -1;
	private String alternateDelimiter;
	private	Matcher crlfMatcher;

	/** Create a ScriptParser
	 *
	 *	The actual script needs to be specified with setScript()
	 *  The delimiter will be evaluated dynamically
	 */
	public ScriptParser()
	{
	}

	/**
	 *	Initialize a ScriptParser from a file.
	 *	The delimiter will be evaluated dynamically
	 */
	public ScriptParser(File f)
		throws IOException
	{
		this.readScriptFromFile(f);
		this.findDelimiterToUse();
	}

	public void readScriptFromFile(String filename)
		throws IOException
	{
		File f = new File(filename);
		this.readScriptFromFile(f);
	}

	public void readScriptFromFile(File f)
		throws IOException
	{
		BufferedReader in = null;
		StrBuffer content = null;
		try
		{
			content = new StrBuffer((int)f.length());
			in = new BufferedReader(new FileReader(f));
			String line = in.readLine();
			while (line != null)
			{
				content.append(line);
				content.append('\n');
				line = in.readLine();
			}
		}
		catch (Exception e)
		{
			LogMgr.logError("ScriptParser.readFile()", "Error reading file " + f.getAbsolutePath(), e);
			content = new StrBuffer();
		}
		finally
		{
			try { in.close(); } catch (Throwable th) {}
		}
		this.setScript(content.toString());
	}

	/**
	 *	Create a ScriptParser for the given Script.
	 *	The delimiter to be used will be evaluated dynamically
	 */
	public ScriptParser(String aScript)
	{
		this.setScript(aScript);
	}

	/**
	 *	Define the script to be parsed and the delimiter to be used.
	 *	If delim == null, it will be evaluated dynamically.
	 *	First the it will check if the script ends with the alternate delimiter
	 *	if this is not the case, the script will be checked if it ends with GO
	 *	If so, GO will be used (MS SQL Server script style)
	 *	If none of the above is true, ; (semicolon) will be used
	 */
	public void setScript(String aScript)
	{
		if (aScript == null) throw new NullPointerException("SQL script may not be null");
		if (aScript.equals(this.originalScript)) return;
		this.originalScript = aScript;
		this.findDelimiterToUse();
		this.commands = null;
		this.commandBoundaries = null;
		
		this.crlfMatcher = StringUtil.PATTERN_CRLF.matcher(this.originalScript);
	}

	/**
	 *	Try to find out which delimiter should be used for the current script.
	 *	First the it will check if the script ends with the alternate delimiter
	 *	if this is not the case, the script will be checked if it ends with GO
	 *	If so, GO will be used (MS SQL Server script style)
	 *	If none of the above is true, ; (semicolon) will be used
	 */
	private void findDelimiterToUse()
	{
		this.delimiter = ";";

		String cleanSql = SqlUtil.makeCleanSql(this.originalScript, false).trim();
		if (this.alternateDelimiter == null)
		{
			this.alternateDelimiter = Settings.getInstance().getAlternateDelimiter();
		}
		if (cleanSql.endsWith(this.alternateDelimiter))
		{
			this.delimiter = this.alternateDelimiter;
		}
		else if (cleanSql.toUpperCase().endsWith("GO"))
		{
			this.delimiter = "GO";
		}
		this.delimiterLength = this.delimiter.length();
	}

	/**
	 *	Return the command index for the command which is located at
	 *	the given index of the current script.
	 */
	public int getCommandIndexAtCursorPos(int cursorPos)
	{
		if (this.commands == null) this.parseCommands();
		if (cursorPos < 0) return -1;
		int count = this.commandBoundaries.size();
		if (count == 1) return 0;
		for (int i=0; i < count - 1; i++)
		{
			CommandBoundary b = (CommandBoundary)this.commandBoundaries.get(i);
			CommandBoundary next = (CommandBoundary)this.commandBoundaries.get(i + 1);
			if (b.startPos <= cursorPos && b.endPos >= cursorPos) return i;
			if (b.endPos > cursorPos && next.startPos <= cursorPos) return i+1;
		}
		CommandBoundary b = (CommandBoundary)this.commandBoundaries.get(count - 1);
		if (b.startPos <= cursorPos && b.endPos > cursorPos) return count - 1;
		return -1;
	}

	/**
	 *	Get the starting offset in the original script for the command indicated by index
	 */
	public int getStartPosForCommand(int index)
	{
		if (this.commands == null) this.parseCommands();
		if (index < 0 || index >= this.commands.size()) return -1;
		CommandBoundary b = (CommandBoundary)this.commandBoundaries.get(index);
		return b.startPos;
	}

	public int findNextLineStart(int pos)
	{
		if (pos < 0) return pos;
		int len = this.originalScript.length();
		if (pos >= len) return pos;
		char c = this.originalScript.charAt(pos);
		while (pos < len && (c == '\n' || c == '\r'))
		{
			pos ++;
			c = this.originalScript.charAt(pos);
		}
		return pos;
	}
	/**
	 *	Get the starting offset in the original script for the command indicated by index
	 */
	public int getEndPosForCommand(int index)
	{
		if (this.commands == null) this.parseCommands();
		if (index < 0 || index >= this.commands.size()) return -1;
		CommandBoundary b = (CommandBoundary)this.commandBoundaries.get(index);
		return b.endPos;
	}

	public String getCommand(int index)
	{
		if (this.commands == null) this.parseCommands();
		if (index < 0 || index >= this.commands.size()) return null;
		return (String)this.commands.get(index);
	}

	/**
	 *	Return the list of commands in the current script.
	 *	The list contains elements of <code>String</code>.
	 *	The commands will be returned without the used delimiter
	 */
	public List getCommands()
	{
		if (this.commands == null) this.parseCommands();
		return this.commands;
	}

	public void setAlternateDelimiter(String delim)
	{
		this.alternateDelimiter = delim;
	}

	public String getDelimiter()
	{
		return this.delimiter;
	}

	public String getScript()
	{
		return this.originalScript;
	}

	//private static final Pattern GO_PATTERN = Pattern.compile("(?mi)^\\s*go\\s*$");

	/**
	 *	Parse the given SQL Script into a List of single SQL statements.
	 *	Returns the index of the statement indicated by the currentCursorPos
	 */
	private void parseCommands()
	{
		this.commands = new ArrayList();
		this.commandBoundaries = new ArrayList();

		if (this.originalScript == null || this.originalScript.trim().length() == 0)
		{
			return;
		}

		if (this.delimiter == null) this.findDelimiterToUse();

		boolean quoteOn = false;
		boolean commentOn = false;
		boolean blockComment = false;
		boolean singleLineComment = false;
		boolean startOfLine = true;
		
		String value = null;
		int oldPos = -1;
		String currChar = null;
		String lastQuote = null;

		final int scriptLen = this.originalScript.length();
		int lastPos = 0;
		int currentIndex = -1;
		int pos = -1;

		if (this.delimiter.toUpperCase().equals(this.delimiter.toLowerCase()))
			pos = this.originalScript.indexOf(this.delimiter);
		else
			pos = this.originalScript.toUpperCase().indexOf(this.delimiter.toUpperCase());
		
		if (pos == -1 || pos == this.originalScript.length() - this.delimiterLength)
		{
			this.addCommand(0, -1);
			return;
		}

		for (pos = 0; pos < scriptLen; pos++)
		{
			currChar = this.originalScript.substring(pos, pos + 1).toUpperCase();
			
			// ignore quotes in comments
			if (!commentOn && currChar.charAt(0) == '\'' || currChar.charAt(0) == '"')
			{
				if (!quoteOn)
				{
					lastQuote = currChar;
					quoteOn = true;
				}
				else if (currChar.equals(lastQuote))
				{
					if (pos > 1)
					{
						// check if the current quote char was escaped
						if (this.originalScript.charAt(pos - 1) != '\\')
						{
							lastQuote = null;
							quoteOn = false;
						}
					}
					else
					{
						lastQuote = null;
						quoteOn = false;
					}
				}
			}

			// now check for comment start
			if (!quoteOn && pos < scriptLen - 1)
			{
				char toTest = currChar.charAt(0);

				if (!commentOn)
				{
					char next = this.originalScript.charAt(pos + 1);

					if (toTest == '/' && next == '*')
					{
						blockComment = true;
						singleLineComment = false;
						commentOn = true;
					}
					else if (startOfLine && (toTest == '#' || (toTest == '-' && next == '-')))
					{
						singleLineComment = true;
						blockComment = false;
						commentOn = true;
					}
				}
				else
				{
					if (singleLineComment)
					{
						if (toTest == '\r' || toTest == '\n')
						{
							singleLineComment = false;
							blockComment = false;
							commentOn = false;
							startOfLine = true;
							continue;
						}
					}
					else if (blockComment)
					{
						char last = this.originalScript.charAt(pos - 1);
						if (toTest == '/' && last == '*')
						{
							blockComment = false;
							singleLineComment = false;
							commentOn = false;
							continue;
						}
					}
				}
			}

			if (!quoteOn && !commentOn)
			{
				if (this.delimiterLength > 1 && pos + this.delimiterLength < scriptLen)
				{
					currChar = this.originalScript.substring(pos, pos + this.delimiterLength).toUpperCase();
				}

				if ((currChar.equals(this.delimiter) || (pos == scriptLen)))
				{
					this.addCommand(lastPos, pos);
					lastPos = pos + this.delimiterLength;
					startOfLine = true;
					
					// skip the startOfLine test.
					continue;
				}
			}
			
			if (currChar.charAt(0) == '\n' || currChar.charAt(0) == '\n' )
			{
				startOfLine = true;
			}
			else
			{
				startOfLine = Character.isWhitespace(currChar.charAt(0));
			}
			
		}

		if (lastPos < pos)
		{
			value = this.originalScript.substring(lastPos).trim();
			int endpos = this.originalScript.length();
			if (value.endsWith(this.delimiter))
			{
				endpos = endpos - this.delimiterLength;
			}
			this.addCommand(lastPos, endpos);
		}
	}

	private int addCommand(int startPos, int endPos)
	{
		String value = null;
		startPos = this.getRealStartPos(startPos, endPos);
		//endPos = this.getRealEndPos(startPos, endPos);
		
		if (endPos == -1)
		{
			value = this.originalScript.substring(startPos).trim();
		}
		else
		{
			value = this.originalScript.substring(startPos, endPos).trim();
		}

		if (value.endsWith(this.delimiter))
		{
			value = value.substring(0, value.length() - this.delimiterLength);
			endPos -= this.delimiterLength;
		}

		String clean = SqlUtil.makeCleanSql(value, false);
		if (clean.equalsIgnoreCase(this.delimiter)) return -1;
		
		if (clean.length() > 0)
		{
			this.commands.add(value);
			CommandBoundary b = new CommandBoundary(startPos, endPos);
			this.commandBoundaries.add(b);
		}
		return this.commands.size() - 1;
	}

	/**
	 *	Check for the real beginning of the statement identified by 
	 *	startPos/endPos. This method will return the actual start of the
	 *	command with leading comments trimmed
	 */
	private int getRealStartPos(int startPos, int endPos)
	{
		if (startPos + 2 > this.originalScript.length()) return startPos;
		
		if (endPos == -1) endPos = this.originalScript.length();
		
		int start = startPos;
		
		while (start + 2 < endPos)
		{
			String s = this.originalScript.substring(start, start + 2).trim();
			if ("/*".equals(s))
			{
				int pos = this.originalScript.indexOf("*/", start);
				if (pos > -1) start = pos + 2;
				startPos = start;
			}
			else if ("--".equals(s))
			{
				if (crlfMatcher.find(start))
				{
					start = crlfMatcher.start();
					startPos = start;
				}
			}
			else if (s.length() > 0 && s.matches("\\w*"))
			{
				break;
			}
			start ++;
		}
		return startPos;
	}

	/**
	 *	Find the real end for the given command, trimming 
	 *	any possible comments after the real statement
	 */
	private int getRealEndPos(int startPos, int endPos)
	{
		if (endPos == -1) return endPos;
		
		String value = this.originalScript.substring(startPos, endPos);
		Pattern p = Pattern.compile("[^\\*]" + this.delimiter);
		Matcher m = p.matcher(value);
		int last = -1;
		while (m.find())
		{
			last = m.start();
		}
		if (last == -1)  return endPos;
		
		return startPos + last + this.delimiterLength;
	}
	
	public static void main(String args[])
	{
		try
		{
			String sql = "--bbbb\ncreate procedure bla/* create the procedure */\nas\nset x = 5;\nend/--'\ncommit/\n--";
			//String sql = "  --'\ncommit/\n--";
			ScriptParser p = new ScriptParser(sql);
			List l = p.getCommands();
			for (int i=0; i < l.size(); i++)
			{
				System.out.println("==============");
				System.out.println(l.get(i));
			}
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}

		System.out.println("*** Done.");
	}

}

class CommandBoundary
{
	public int startPos;
	public int endPos;

	public CommandBoundary(int start, int end)
	{
		this.startPos = start;
		this.endPos = end;
	}

}