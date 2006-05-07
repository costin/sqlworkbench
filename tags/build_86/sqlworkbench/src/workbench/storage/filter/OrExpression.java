/*
 * OrExpression.java
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

import java.util.Iterator;
import java.util.Map;

/**
 * @author support@sql-workbench.net
 */
public class OrExpression
	extends ComplexExpression
{
	public OrExpression()
	{
	}
	
	public boolean evaluate(Map columnValues)
	{
		int count = filter.size();
		for (int i=0; i < count; i++)
		{
			FilterExpression expr = (FilterExpression)this.filter.get(i);
			if (expr.evaluate(columnValues)) return true;
		}
		return false;
	}
	
	public boolean equals(Object other)
	{
		if (other instanceof OrExpression)
		{
			return super.equals(other);
		}
		else
		{
			return false;
		}
	}
	
	public String toString()
	{
		StringBuffer value = new StringBuffer();
		int count = filter.size();
		for (int i=0; i < count; i++)
		{
			FilterExpression expr = (FilterExpression)this.filter.get(i);
			if (i > 0) value.append(" OR ");
			value.append(expr.toString());
		}
		return value.toString();
	}
	
}
