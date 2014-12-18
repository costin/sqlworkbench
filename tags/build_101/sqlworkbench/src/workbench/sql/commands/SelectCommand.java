/*
 * SelectCommand.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2008, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.sql.commands;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import workbench.util.ExceptionUtil;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.sql.SqlCommand;
import workbench.sql.StatementRunnerResult;
import workbench.util.StringUtil;

/**
 * Implementation of the SELECT statement. 
 * 
 * The result of the SELECT is passed back in the StatementRunnerResult object.
 * If a ResultSetConsumer is registered in the StatementRunner executing this
 * statement, the ResultSet will be returned directly, otherwise the 
 * ResultSet will be completely read into a DataStore.
 * 
 * @author  support@sql-workbench.net
 */
public class SelectCommand extends SqlCommand
{
	public static final String VERB = "SELECT";

	public SelectCommand()
	{
		
	}

	/**
	 * Runs the passed SQL statement using Statement.executeQuery()
	 */
	public StatementRunnerResult execute(String sql)
		throws SQLException
	{
		this.isCancelled = false;

		StatementRunnerResult result = new StatementRunnerResult(sql);
		result.setWarning(false);

		try
		{
			boolean isPrepared = false;

			this.runner.setSavepoint();
			
			if (Settings.getInstance().getCheckPreparedStatements()
				  && currentConnection.getPreparedStatementPool().isRegistered(sql))
			{
				this.currentStatement = currentConnection.getPreparedStatementPool().prepareStatement(sql);
				if (this.currentStatement != null)
				{
					isPrepared = true;
				}
			}
			
			if (this.currentStatement == null)
			{
				this.currentStatement = currentConnection.createStatementForQuery();
			}
			
			try 
			{ 
				if (this.queryTimeout >= 0 && currentConnection.supportsQueryTimeout())
				{
					this.currentStatement.setQueryTimeout(this.queryTimeout); 
				}
			} 
			catch (Exception th) 
			{
				LogMgr.logWarning("SelectCommand.execute()", "Error when setting query timeout", th);
			}

			try
			{
				this.currentStatement.setMaxRows(this.maxRows);
			}
			catch (Exception e)
			{
				LogMgr.logWarning("SelectCommand.execute()", "The JDBC driver does not support the setMaxRows() function! (" +e.getMessage() + ")");
			}

			ResultSet rs = null;
			boolean hasResult = true;

			if (isPrepared)
			{
				rs = ((PreparedStatement)this.currentStatement).executeQuery();
			}
			else
			{
				if (Settings.getInstance().getUseGenericExecuteForSelect())
				{
					hasResult = this.currentStatement.execute(sql);
				}
				else
				{
					rs = this.currentStatement.executeQuery(sql);
				}
			}

			if (this.isCancelled)
			{
				result.addMessage(ResourceMgr.getString("MsgStatementCancelled"));
				result.setFailure();
			}
			else
			{
				processResults(result, hasResult, rs);

				if (!isCancelled)
				{
					this.appendSuccessMessage(result);
				}
				else
				{
					result.addMessage(ResourceMgr.getString("MsgStatementCancelled"));
				}
				result.setSuccess();
			}

			this.runner.releaseSavepoint();
		}
		catch (Exception e)
		{
			result.clear();
			result.addMessage(ResourceMgr.getString("MsgExecuteError"));
			result.addMessage(StringUtil.getMaxSubstring(sql, 120));
			result.addMessage(ExceptionUtil.getAllExceptions(e));
			appendWarnings(result);
			LogMgr.logSqlError("SelectCommand.execute()", sql, e);
			result.setFailure();
			this.runner.rollbackSavepoint();
		}

		return result;
	}

	public String getVerb()
	{
		return VERB;
	}

	public void setMaxRows(int max)
	{
		if (max >= 0)
			this.maxRows = max;
		else
			this.maxRows = 0;
	}

}