/*
 * ColumnExpression.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2008, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.storage.filter;

import java.util.Map;

/**
 * A class to define the filter criteria for a single column 
 * 
 * @author support@sql-workbench.net
 */
public class ColumnExpression
	implements FilterExpression, ExpressionValue
{
	private String columnName;
	private Object filterValue;
	private ColumnComparator comparator;
	private boolean ignoreCase;
	
	/**
	 * Default constructor needed for XML serialisation.
	 */
	public ColumnExpression()
	{
	}

	/**
	 * Define the filter for a column
	 * 
	 * @param column the column name
	 * @param comparator the comparator to be used to compare the reference value against the actual values
	 * @param filterValue the filter value to compare against the actual values
	 * 
	 * @see #setFilterValue(Object)
	 * @see #setComparator(ColumnComparator)
	 */
	public ColumnExpression(String column, ColumnComparator comparator, Object filterValue)
	{
		setComparator(comparator);
		setFilterValue(filterValue);
		setColumnName(column);
	}

	/**
	 * Define a "generic" column filter
	 * 
	 * @param comparator the comparator to be used to compare the reference value against the actual values
	 * @param filterValue the filter value to compare against the actual values
	 * 
	 * @see #setFilterValue(Object)
	 * @see #setComparator(ColumnComparator)
	 */
	public ColumnExpression(ColumnComparator comparator, Object filterValue)
	{
		setComparator(comparator);
		setFilterValue(filterValue);
		setColumnName("*");
	}
	
	public Object getFilterValue()
	{
		return filterValue;
	}

	public void setFilterValue(Object value)
	{
		this.filterValue = value;
	}

	public ColumnComparator getComparator()
	{
		return comparator;
	}

	public void setComparator(ColumnComparator comp)
	{
		this.comparator = comp;
	}
	
	public boolean equals(Object other)
	{
		try
		{
			ColumnExpression def = (ColumnExpression)other;
			if (this.filterValue == null || def.filterValue == null) return false;
			boolean result = this.filterValue.equals(def.filterValue);
			if (result)
			{
				result = this.comparator.equals(def.comparator);
			}
			return result;
		}
		catch (Throwable th)
		{
			return false;
		}
	}

	public boolean evaluate(Object value)
	{
		if (value != null && !comparator.supportsType(value.getClass())) return false;
		return comparator.evaluate(filterValue, value, this.ignoreCase);
	}
	
	public boolean evaluate(Map columnValues)
	{
		Object value = columnValues.get(this.columnName);
		return evaluate(value);
	}
	
	public String getColumnName()
	{

		return this.columnName;
	}

	public void setColumnName(String column)
	{
		this.columnName = (column == null ? null : column.toLowerCase());
	}
	
	public String toString()
	{
		return "[" + columnName + "] " + this.comparator.getOperator() + " " + comparator.getValueExpression(this.filterValue);
	}

	public boolean isIgnoreCase()
	{
		return ignoreCase;
	}

	public void setIgnoreCase(boolean ignoreCase)
	{
		this.ignoreCase = ignoreCase;
	}
	
	public boolean isColumnSpecific() 
	{
		return true;
	}
}