/*
 * ArgumentParser.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2007, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author  support@sql-workbench.net
 */
public class ArgumentParser
{
	private static final String ARG_PRESENT = "$__ARG_PRESENT__$";
	private Map<String, String> arguments = new HashMap<String, String>();
	private ArrayList unknownParameters = new ArrayList();
	private int argCount = 0;
	
	public ArgumentParser()
	{
	}

	public void addArgument(String key)
	{
		if (key == null) throw new NullPointerException("Key may not be null");
		this.arguments.put(key.toLowerCase(), null);
	}

	public void parse(String args[])
	{
		this.reset();
		StringBuilder line = new StringBuilder(200);
		for (int i=0; i<args.length; i++)
		{
			line.append(args[i]);
			line.append(' ');
		}
		this.parse(line.toString());
	}

	public void parse(String aCmdLine)
	{
		this.reset();

		WbStringTokenizer tok = new WbStringTokenizer('-', "\"'", false);
		tok.setDelimiterNeedsWhitspace(true);
		tok.setSourceString(aCmdLine.trim());

		while (tok.hasMoreTokens())
		{
			String word = tok.nextToken();// String)words.get(i);
			if (word == null || word.length() == 0) continue;
			String arg = null;
			String value = null;
			int pos = word.indexOf('=');
			if (pos > -1)
			{
				arg = word.substring(0, pos).trim().toLowerCase();
				value = word.substring(pos + 1).trim();
			}
			else
			{
				arg = word.trim().toLowerCase();
			}
			
			if (value == null)
			{
				value = ARG_PRESENT;
			}
			
			if (arguments.containsKey(arg))
			{
				arguments.put(arg, value);
				this.argCount ++;
			}
			else
			{
				this.unknownParameters.add(arg);
			}
		}
	}

	public boolean hasArguments()
	{
		return this.argCount > 0;
	}
	
	public int getArgumentCount()
	{
		return this.argCount;
	}

	public boolean hasUnknownArguments()
	{
		return this.unknownParameters.size() > 0;
	}

	public List getUnknownArguments()
	{
		return Collections.unmodifiableList(this.unknownParameters);
	}
	
	public boolean isArgPresent(String arg)
	{
		if (arg == null) return false;
		Object value = this.arguments.get(arg);
		return (value != null);
	}
	
	public void reset()
	{
		Iterator<String> keys = this.arguments.keySet().iterator();
		while (keys.hasNext())
		{
			String key = keys.next();
			this.arguments.put(key, null);
		}
		this.argCount = 0;
		this.unknownParameters.clear();
	}
	
	public boolean getBoolean(String key)
	{
		return getBoolean(key, false);
	}
	
	/**
	 * Return a parameter value as a boolean. If no 
	 * value was specified the given default value will be returned
	 * @see #getValue(String)
	 * @see StringUtil#stringToBool(String)
	 */
	public boolean getBoolean(String key, boolean def)
	{
		String value = this.getValue(key);
		if (value == null || value.trim().length() == 0) return def;
		return StringUtil.stringToBool(value);
	}

	/**
	 * Return the parameter for the give argument. 
	 * If no value was specified or the parameter was not 
	 * passed on the commandline null will be returned.
	 * Any leading or trailing quotes will be removed from the argument
	 * before it is returned
	 * @see StringUtil#trimQuotes(String)
	 */
	public String getValue(String key)
	{
		String value = this.arguments.get(key.toLowerCase());
		if (value == ARG_PRESENT) return null;
		value = StringUtil.trimQuotes(value);
		return value;
	}
	
	public int getIntValue(String key, int def)
	{
		return StringUtil.getIntValue(this.getValue(key),def);
	}
	
}
