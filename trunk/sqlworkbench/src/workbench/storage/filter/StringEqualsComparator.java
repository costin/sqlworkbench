/*
 * EqualsComparator.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2005, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.storage.filter;

/**
 * @author support@sql-workbench.net
 */
public class StringEqualsComparator
	implements ColumnComparator
{
	public StringEqualsComparator()
	{
	}
	
	public boolean supportsIgnoreCase() { return true; }

	public String getValueExpression(Object value) { return "'" + value + "'";}
	public String getName() { return "Equals"; }
	public String getOperator() { return "="; }
	
	public boolean evaluate(Object reference, Object value, boolean ignoreCase)
	{
		if (reference == null && value == null) return true;
		if (reference == null && value != null) return false;
		if (reference != null && value == null) return false;
		try
		{
			if (ignoreCase)
				return ((String)reference).equalsIgnoreCase((String)value);
			else
				return reference.equals(value);
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	public boolean supportsType(Class valueClass)
	{
		return (String.class.isAssignableFrom(valueClass));
	}

	public boolean equals(Object other)
	{
		return (other instanceof StringEqualsComparator);
	}
	
}