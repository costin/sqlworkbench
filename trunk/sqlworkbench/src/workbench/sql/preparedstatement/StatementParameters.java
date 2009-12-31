/*
 * StatementParameters.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2010, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.sql.preparedstatement;

import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import workbench.db.WbConnection;
import workbench.log.LogMgr;
import workbench.util.SqlUtil;

/**
 * A class to store the parameters for a PreparedStatement
 * @author  Thomas Kellerer
 */
public class StatementParameters
{
	private int parameterCount;
	private ParameterDefinition[] parameter;
	
	public StatementParameters(String sql, WbConnection conn)
		throws SQLException
	{
		PreparedStatement pstmt = null;
		try
		{
			pstmt = conn.getSqlConnection().prepareStatement(sql);
			ParameterMetaData meta = pstmt.getParameterMetaData();
			this.parameterCount = meta.getParameterCount();
			if (this.parameterCount > 0)
			{
				this.parameter = new ParameterDefinition[this.parameterCount];
				
				for (int i=1; i <= this.parameterCount; i++)
				{
					int type = meta.getParameterType(i);
					ParameterDefinition def = new ParameterDefinition(i, type);
					this.parameter[i-1] = def;
				}
			}
		}
		catch (Exception e)
		{
			LogMgr.logError("StatementParameter.<init>", "Error when checking parameters", e);
			if (e instanceof SQLException) throw (SQLException)e;
			else throw new SQLException("Error retrieving statement parameters: " + e.getClass().getName());
		}
		finally
		{
			SqlUtil.closeStatement(pstmt);
		}
	}

	public StatementParameters(List<ParameterDefinition> params)
	{
		this.parameterCount = params.size();
		this.parameter = new ParameterDefinition[parameterCount];
		for (int i=0; i < params.size(); i++)
		{
			parameter[i] = params.get(i);
		}
	}
	
	public int getParameterType(int index)
	{
		return this.parameter[index].getType();
	}
	
	public Object getParameterValue(int index)
	{
		return this.parameter[index].getValue();
	}
	
	public void applyParameter(PreparedStatement pstmt)
		throws SQLException
	{
		for (int i=0; i < this.parameterCount; i++)
		{
			this.parameter[i].setStatementValue(pstmt);
		}
	}
	
	public boolean isValueValid(int index, String value)
	{
		return this.parameter[index].isValueValid(value);
	}
	
	public void setParameterValue(int index, String value)
	{
		this.parameter[index].setValue(value);
	}
	
	public int getParameterCount() { return this.parameterCount; }
	
	public boolean hasParameter()
	{
		return this.parameterCount > 0;
	}
}

