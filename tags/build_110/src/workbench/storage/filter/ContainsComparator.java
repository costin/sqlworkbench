/*
 * ContainsComparator.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2011, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.storage.filter;

import workbench.resource.ResourceMgr;

/**
 * @author Thomas Kellerer
 */
public class ContainsComparator
	implements ColumnComparator
{
	public String getValueExpression(Object value) { return (value == null ? "" : value.toString()); }

	public String getDescription()
	{
		return ResourceMgr.getString("TxtOpContains");
	}

	public String getOperator() { return "contains"; }
	
	public boolean evaluate(Object reference, Object value, boolean ignoreCase)
	{
		if (reference == null && value == null) return true;
		if (reference == null || value == null) return false;
		try
		{
			String v = value.toString();
			String ref = reference.toString();
			if (ignoreCase)
				return (v.toLowerCase().indexOf(ref.toLowerCase()) > -1);
			else
				return (v.indexOf(ref) > -1);
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	public boolean supportsType(Class valueClass)
	{
		return (CharSequence.class.isAssignableFrom(valueClass));
	}

	public boolean supportsIgnoreCase()
	{
		return true;
	}

	public boolean needsValue() 
	{ 
		return true; 
	}
	
	public boolean validateInput(Object value) 
	{ 
		return true; 
	}

	public boolean comparesEquality() 
	{ 
		return false; 
	}
	
	public boolean equals(Object other)
	{
		return (other instanceof ContainsComparator);
	}
	
}