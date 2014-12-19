/*
 * ParameterDefinition.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2005, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: info@sql-workbench.net
 *
 */
package workbench.sql.preparedstatement;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import workbench.log.LogMgr;
import workbench.util.SqlUtil;
import workbench.util.ValueConverter;

/**
 * @author  info@sql-workbench.net
 *	The defintion for a single parameter in StatementParameters
 *	@see StatementParameters
 */
public class ParameterDefinition
{
	private static final ValueConverter CONVERTER = new ValueConverter();
	private int type;
	private int index;
	
	private boolean valueValid = false;
	private Object value = null;
	
	public ParameterDefinition(int index, int type)
	{
		this.index = index;
		this.type = type;
	}
	
	public int getIndex() { return this.index; }
	public int getType() { return this.type; }
	
	public boolean isValueValid(String v)
	{
		boolean valid = false;
		try
		{
			Object value = CONVERTER.convertValue(v, this.type);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	public boolean setValue(String v)
	{
		try
		{
			this.value = CONVERTER.convertValue(v, this.type);
			this.valueValid = true;
		}
		catch (Exception e)
		{
			this.valueValid = false;
			LogMgr.logError("ParameterDefinition.setValue()", "Error applying value " + v + " for type " + SqlUtil.getTypeName(this.type), e);
		}
		return this.valueValid;
	}
	
	public Object getValue() { return this.value; }
	
	public void setStatementValue(PreparedStatement stmt)
		throws IllegalStateException, SQLException
	{
		if (!this.valueValid) throw new IllegalStateException("No valid value defined for parameter " + this.index);
		if (this.value == null)
		{
			stmt.setNull(this.index, this.type);
		}
		else
		{
			stmt.setObject(this.index, this.value);
		}
	}
		
}