/*
 * OracleObjectCompiler.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2006, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db.oracle;

import java.sql.SQLException;
import java.sql.Statement;

import workbench.db.WbConnection;
import workbench.util.SqlUtil;

/**
 *
 * @author  support@sql-workbench.net
 */
public class OracleObjectCompiler
{
	private WbConnection dbConnection;
	private Statement stmt;
	private String lastError = null;
	
	public OracleObjectCompiler(WbConnection conn)
		throws SQLException
	{
		this.dbConnection = conn;
		this.stmt = this.dbConnection.createStatement();
	}

	public void close()
	{
		if (this.stmt != null)
		{
			SqlUtil.closeStatement(stmt);
		}
	}
	
	public String getLastError()
	{
		return this.lastError;
	}
	
	public boolean compileObject(String name, String type)
	{
		String sql = "ALTER " + type + " " + name + " COMPILE";
		this.lastError = null;
		try
		{
			this.stmt.executeUpdate(sql);
			return true;
		}
		catch (SQLException e)
		{
			this.lastError = e.getMessage();
			return false;
		}
		finally
		{
			this.dbConnection.setBusy(false);
		}
	}
	
}