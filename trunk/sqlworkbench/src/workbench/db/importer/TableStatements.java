/*
 * TableStatements.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2013, Thomas Kellerer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at.
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db.importer;

import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import workbench.db.TableIdentifier;
import workbench.db.WbConnection;
import workbench.log.LogMgr;
import workbench.sql.wbcommands.CommonArgs;
import workbench.util.ArgumentParser;
import workbench.util.SqlUtil;
import workbench.util.StringUtil;

/**
 * A class to manage and run table-level statements for the DataImporter.
 *
 * @see DataImporter#setPerTableStatements(TableStatements)
 *
 * @author Thomas Kellerer
 */
public class TableStatements
{
	private String preStatement;
	private String postStatement;
	private boolean ignoreErrors;
	private boolean success;

	public TableStatements(String pre, String post)
	{
		this.preStatement = pre;
		this.postStatement = post;
	}

	/**
	 * Initialize this TableStatement using the given commandline
	 * @param cmdLine
	 * @see workbench.sql.wbcommands.CommonArgs#ARG_PRE_TABLE_STMT
	 */
	public TableStatements(ArgumentParser cmdLine)
	{
		String sql = cmdLine.getValue(CommonArgs.ARG_PRE_TABLE_STMT);
		if (!StringUtil.isBlank(sql))
		{
			this.preStatement = sql;
		}

		sql = cmdLine.getValue(CommonArgs.ARG_POST_TABLE_STMT);
		if (!StringUtil.isBlank(sql))
		{
			this.postStatement = sql;
		}

		this.ignoreErrors = cmdLine.getBoolean(CommonArgs.ARG_IGNORE_TABLE_STMT_ERRORS, true);
	}

	public boolean hasStatements()
	{
		return (this.preStatement != null || this.postStatement != null);
	}

	/**
	 * Run the statement that is defined as the pre-processing statement
	 *
	 * @param con the connection on which to run the statement
	 * @param tbl the table for which to run the statement
	 * @throws java.sql.SQLException
	 * @see #getPreStatement(TableIdentifier)
	 */
	public void runPreTableStatement(WbConnection con, TableIdentifier tbl)
		throws SQLException
	{
		runStatement(con, tbl, getPreStatement(tbl));
	}

	/**
	 * Run the statement that is defined as the post-processing statement
	 *
	 * @param con the connection on which to run the statement
	 * @param tbl the table for which to run the statement
	 * @throws java.sql.SQLException
	 * @see #getPostStatement(TableIdentifier)
	 */
	public void runPostTableStatement(WbConnection con, TableIdentifier tbl)
		throws SQLException
	{
		runStatement(con, tbl, getPostStatement(tbl));
	}

	/**
	 * Runs the given SQL for the given Table.
	 *
	 * @param con the connection to be used when running the statement
	 * @param tbl the table for which to run the statement
	 * @param sql
	 * @throws java.sql.SQLException
	 * @see #getPreStatement(TableIdentifier)
	 * @see #getPostStatement(TableIdentifier)
	 */
	protected void runStatement(WbConnection con, TableIdentifier tbl, String sql)
		throws SQLException
	{
		if (StringUtil.isBlank(sql))
		{
			success = true;
			return;
		}

		Savepoint sp = null;
		Statement stmt = null;
		boolean useSavepoint = con.getDbSettings().useSavepointForImport();

		success = false;
		try
		{
			if (useSavepoint && !con.getAutoCommit()) sp = con.setSavepoint();
			stmt = con.createStatement();
			LogMgr.logDebug("TableStatements.runStatement", "Executing statement: " + sql);
			stmt.execute(sql);
			con.releaseSavepoint(sp);
			success = true;
		}
		catch (SQLException e)
		{
			con.rollback(sp);
			if (ignoreErrors)
			{
				LogMgr.logWarning("TableStatements.runStatement", "Error running statement: [" + sql + "]: " + e.getMessage());
			}
			else
			{
				LogMgr.logError("TableStatements.runStatement", "Error running statement: " + sql, e);
				throw e;
			}
		}
		catch (Throwable th)
		{
			LogMgr.logError("TableStatements.runStatement", "Error running statement: " + sql, th);
			con.rollback(sp);
		}
		finally
		{
			SqlUtil.closeStatement(stmt);
		}
	}

	public boolean wasSuccess()
	{
		return success;
	}

	/**
	 * Return the post-processing SQL for the passed table.
	 *
	 * Placeholders ${table.name} and ${table.expression} are replaced
	 * before running the statement.
	 *
	 * @param tbl the table for which the statement should be returned.
	 *
	 * @see workbench.db.TableIdentifier#getTableName()
	 * @see workbench.db.TableIdentifier#getTableExpression(workbench.db.WbConnection)
	 */
	public String getPostStatement(TableIdentifier tbl)
	{
		return getTableStatement(postStatement, tbl);
	}

	/**
	 * Return the post-processing SQL for the passed table.
	 *
	 * Placeholders ${table.name} and ${table.expression} are replaced
	 * before running the statement.
	 *
	 * @param tbl the table for which the statement should be returned.
	 *
	 * @see workbench.db.TableIdentifier#getTableName()
	 * @see workbench.db.TableIdentifier#getTableExpression(workbench.db.WbConnection)
	 */
	public String getPreStatement(TableIdentifier tbl)
	{
		return getTableStatement(preStatement, tbl);
	}

	private String getTableStatement(String source, TableIdentifier tbl)
	{
		if (source == null) return null;
		String sql = StringUtil.replace(source, "${table.name}", tbl.getTableName());
		sql = StringUtil.replace(sql, "${table.expression}", tbl.getTableExpression());
		return sql;
	}
}
