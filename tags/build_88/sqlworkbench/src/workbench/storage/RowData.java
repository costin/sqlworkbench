/*
 * RowData.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2006, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.storage;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DecimalFormat;
import java.util.List;

import workbench.log.LogMgr;

/**
 *	A class to hold the data for a single row retrieved from the database.
 *	It will also save the originally retrieved information in case the
 *  data is changed.
 *	A row can be in three different status:
 *	NEW          - the row has not been retrieved from the database (i.e. was created on the client)
 *  MODIFIED     - the row has been retrieved but has been changed since then
 *  NOT_MODIFIED - The row has not been changed since it has been retrieved
 */
public class RowData
{
	public static final int NOT_MODIFIED = 0;
	public static final int MODIFIED = 1;
	public static final int NEW = 2;

	private int status = NOT_MODIFIED;

	/**
	 *	This flag will be used by the {@link DataStore}
	 *	to store the information for which rows the SQL statements
	 *  have been sent to the database during the update process
	 */
	private boolean dmlSent = false;
	
	private boolean useNullValueObject = true;
	
	private Object[] colData;
	private Object[] originalData;

	public RowData(ResultInfo info)
	{
		int colCount = info.getColumnCount();
		this.colData = new Object[colCount];
		for (int i = 0; i < colCount; i++)
		{
			this.setNull(i, info.getColumnType(i));
		}
		this.setNew();
	}
	
	public RowData(int aColCount)
	{
		this.colData = new Object[aColCount];
		this.setNew();
	}

	public void setUseNullValueObject(boolean flag) { this.useNullValueObject = flag; }
	public Object[] getData() { return this.colData; }
	
	/**
	 *	Read the row data from the supplied ResultSet
	 */
	public void read(ResultSet rs, ResultInfo info)
	{
		int colCount = this.colData.length;
		Object value = null;
		for (int i=0; i < colCount; i++)
		{
			int type = info.getColumnType(i);
			try
			{
				// This is a workaround for Oracle, because
				// it does not return the correct object class
				// when using getObject() on a TIMESTAMP column
				// I simply assume that this is working properly
				// for other JDBC drivers as well.

				// On the other hand we may not use getDate()
				// for Oracle DATE columns as this will remove
				// the time part of the column
				if (type == java.sql.Types.TIMESTAMP)
				{
					value = rs.getTimestamp(i+1);
				}
				else
				{
					value = rs.getObject(i + 1);
				}
				if (type == Types.CLOB)
				{
					// "Convert" a CLOB to a String object 
					// This makes handling the value much easier
					try
					{
						Clob clob = (Clob)value;
						int len = (int)clob.length();
						value = clob.getSubString(1, len);
					}
					catch (Exception e)
					{
						value = null;
					}
				}
			}
			catch (SQLException e)
			{
				value = null;
			}

			if (value == null && useNullValueObject)
			{
				this.colData[i] = NullValue.getInstance(type);
			}
			else
			{
				this.colData[i] = value;
			}
		}
		this.resetStatus();
	}

	/**
	 *	Create a deep copy of this object.
	 *	The status of the new row will be NOT_MODIFIED
	 */
	public RowData createCopy()
	{
		RowData result = new RowData(this.colData.length);
		for (int i=0; i < this.colData.length; i++)
		{
			result.colData[i] = this.colData[i];
		}
		return result;
	}

	public int getColumnCount()
	{
		if (this.colData == null) return 0;
		return this.colData.length;
	}

	/**
	 *	Sets the new data for the given column.
	 *	After a call isModified() will return true
	 *	if the row was not modified before. If the
	 *	row is new the status will not change.
	 *
	 *	@throws IndexOutOfBoundsException
	 */
	public void setValue(int aColIndex, Object aValue)
		throws IndexOutOfBoundsException
	{
		if (aValue == null) throw new NullPointerException("No null values allowed. Use setNull() instead");
		if (!this.isNew())
		{
			Object oldValue = this.colData[aColIndex];
			if (oldValue == null && aValue == null) return;
			if (oldValue != null && aValue != null)
			{
				if (oldValue.equals(aValue)) return;
			}
			if (this.originalData == null)
			{
				this.originalData = new Object[this.colData.length];
			}
			
			if (this.originalData[aColIndex] == null)
			{
				this.originalData[aColIndex] = this.colData[aColIndex];
			}
		}
		this.colData[aColIndex] = aValue;
		this.setModified();
	}

	/**
	 *	Returns the value for the given column
	 *
	 *	@throws IndexOutOfBoundsException
	 */
	public Object getValue(int aColumn)
		throws IndexOutOfBoundsException
	{
		return this.colData[aColumn];
	}

	public Object getOriginalValue(int aColumn)
		throws IndexOutOfBoundsException
	{
		if (this.originalData == null) return this.getValue(aColumn);
		if (this.originalData[aColumn] == null) return this.getValue(aColumn);
		return this.originalData[aColumn];
	}

	public void restoreOriginalValues()
	{
		if (this.originalData == null) return;
		for (int i=0; i < this.originalData.length; i++)
		{
			if (this.originalData[i] != null)
			{
				this.colData[i] = this.originalData[i];
			}
		}
		this.originalData = null;
		this.resetStatus();
	}

	public boolean isColumnModified(int aColumn)
	{
		if (this.isOriginal()) return false;
		if (this.isNew())
		{
			return this.colData[aColumn] != null;
		}
		else
		{
			if (this.originalData == null) return false;
			//if (this.originalData[aColumn] == null && this.colData[aColumn] != null && !(this.colData[aColumn] instanceof NullValue)) return true; 
			return (this.originalData[aColumn] != null);
		}
	}
	public void setNull(int aColumn, int aType)
	{
		NullValue nul = NullValue.getInstance(aType);
		this.setValue(aColumn, nul);
	}

	/**
	 *	Resets the internal status. After a call to resetStatus()
	 *	isModified() will return false, and isOriginal() will return true.
	 */
	public void resetStatus()
	{
		this.status = NOT_MODIFIED;
		this.dmlSent = false;
		this.originalData = null;
	}

	/**
	 * Resets data and status
	 */
	public void reset()
	{
		this.colData = null;
		this.resetStatus();
	}
	/**
	 *	Sets the status of this row to new.
	 */
	public void setNew()
	{
		this.status = NEW;
	}

	/**
	 *	Returns true if the row is neither modified nor is a new row.
	 *
	 *	@return true if the row has not been altered since retrieval
	 */
	public boolean isOriginal()
	{
		return this.status == NOT_MODIFIED;
	}

	/**
	 *	Check if the row has been modified.
	 *
	 *	@return true if the row has been modified since retrieval
	 *
	 */
	public boolean isModified()
	{
		return (this.status & MODIFIED) ==  MODIFIED;
	}

	/**
	 *	Check if the row has been added to the DataStore
	 *	after the initial retrieve.
	 *
	 *	@return true if it's a new row
	 */
	public boolean isNew()
	{
		return (this.status & NEW) == NEW;
	}

	/**
	 *	Set the status to modified.
	 */
	public void setModified()
	{
		this.status = this.status | MODIFIED;
	}

	public void setDmlSent(boolean aFlag)
	{
		this.dmlSent = aFlag;
	}

	public boolean isDmlSent() { return this.dmlSent; }

	public StringBuffer getDataAsString(String aDelimiter, DecimalFormat formatter)
	{
		return this.getDataAsString(aDelimiter, formatter, null);
	}

	public StringBuffer getDataAsString(String aDelimiter, DecimalFormat formatter, boolean[] columns)
	{
		int count = this.colData.length;
		StringBuffer result = new StringBuffer(count * 20);
		//int start = 0;
		int numCols = 0;
		if (columns != null && count != columns.length) columns = null;

		for (int c=0; c < count; c++)
		{
			if (columns != null)
			{
				if (!columns[c]) continue;
			}
			Object value = this.getValue(c);
			if (numCols > 0) result.append(aDelimiter);

			if (value != null)
			{
				if ((value instanceof Double ||
				    value instanceof Float ||
						value instanceof BigDecimal) && formatter != null)
				{
					Number num = (Number)value;
					result.append(formatter.format(num.doubleValue()));
				}
				else
				{
					String v = value.toString();
					if (v.indexOf((char)0) > 0)
					{
						LogMgr.logWarning("RowData.getDataAsString()", "Found a zero byte in the data! Replacing with space char.");
						byte[] d = v.getBytes();
						int len = d.length;
						for (int i=0; i < len; i++)
						{
							if (d[i] == 0) d[i] = 20;
						}
						v = new String(d);
					}
					result.append(v);
				}
			}
			numCols ++;
		}
		return result;
	}

	public String toString()
	{
		int count = this.colData.length;
		StringBuffer result = new StringBuffer(count * 20);

		result.append('{');
		for (int c=0; c < count; c++)
		{
			result.append('[');
			result.append(this.getValue(c));
			result.append(']');
		}
		result.append('}');
		return result.toString();
	}
}