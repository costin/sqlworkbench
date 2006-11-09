/*
 * RegExComparator.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2006, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.storage.filter;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author support@sql-workbench.net
 */
public class RegExComparator
	implements ColumnComparator
{
	public RegExComparator()
	{
	}
	
	public boolean supportsIgnoreCase() { return true; }
	
	public String getValueExpression(Object value) { return "'" + value + "'";}
	public String getOperator() { return "matches"; }
	public boolean needsValue() { return true; }
	public boolean comparesEquality() { return false; }
	
	public boolean supportsType(Class valueClass)
	{
		return (String.class.isAssignableFrom(valueClass));
	}


	public boolean evaluate(Object reference, Object value, boolean ignoreCase)
	{
		if (reference == null && value == null) return true;
		if (reference == null && value != null) return false;
		if (reference != null && value == null) return false;
		Pattern p = null;
		if (ignoreCase)
		{
			p = Pattern.compile((String)reference, Pattern.CASE_INSENSITIVE);
		}
		else
		{
			p = Pattern.compile((String)reference);
		}
		Matcher m = p.matcher((String)value);
		return m.matches();
	}

	public boolean equals(Object other)
	{
		return (other instanceof RegExComparator);
	}
	
	public boolean validateInput(String value)
	{
		try
		{
			Pattern.compile(value);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
}
