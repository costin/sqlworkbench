/*
 * SqlCommand.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2014, Thomas Kellerer
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
package workbench.sql;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import workbench.RunMode;
import workbench.WbManager;
import workbench.interfaces.ParameterPrompter;
import workbench.interfaces.ResultLogger;
import workbench.interfaces.ResultSetConsumer;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;

import workbench.db.ConnectionProfile;
import workbench.db.DbSettings;
import workbench.db.ErrorPositionReader;
import workbench.db.WbConnection;

import workbench.storage.DataStore;
import workbench.storage.RowActionMonitor;

import workbench.sql.wbcommands.WbEnableOraOutput;

import workbench.util.ArgumentParser;
import workbench.util.DdlObjectInfo;
import workbench.util.ExceptionUtil;
import workbench.util.SqlUtil;
import workbench.util.StringUtil;
import workbench.util.WarningContent;
import workbench.util.WbFile;

/**
 * A single SQL command.
 *
 * This class is used if no special class was found for a given SQL verb.
 * The execute method checks if the command returned a result set or simply an update count.
 *
 * An instance of a SQL command should always be executed in a separate Thread
 * to allow cancelling of the running statement.
 *
 * @author  Thomas Kellerer
 */
public class SqlCommand
{
	protected Statement currentStatement;
	protected WbConnection currentConnection;
	protected boolean isCancelled;
	protected RowActionMonitor rowMonitor;
	protected boolean isUpdatingCommand;
	protected ErrorReportLevel errorLevel;
	protected ResultLogger resultLogger;
	protected StatementRunner runner;
	protected int queryTimeout;
	protected int maxRows;
	protected DataStore currentRetrievalData;
	protected ParameterPrompter prompter;
	protected ArgumentParser cmdLine;
	protected boolean includeStatementInError;
	protected boolean showDataLoading = true;

	public void setRowMonitor(RowActionMonitor monitor)
	{
		this.rowMonitor = monitor;
	}

	public void setResultLogger(ResultLogger logger)
	{
		this.resultLogger = logger;
	}

	public ArgumentParser getArgumentParser()
	{
		return this.cmdLine;
	}

	public void setErrorReportLevel(ErrorReportLevel level)
	{
		this.errorLevel = level;
	}

	public ErrorReportLevel getErrorReportLevel()
	{
		return this.errorLevel;
	}

	protected String getBaseDir()
	{
		String dir = runner == null ? null : runner.getBaseDir();
		if (dir == null)
		{
			dir = ".";
		}
		return dir;
	}

	protected String getDefaultSuccessMessage(StatementRunnerResult result)
	{
		String msg = result == null ? null : getSuccessMessage(result.getSourceCommand());
		if (msg != null) return msg;

		String verb = getVerb();
		if (StringUtil.isEmptyString(verb) && result != null)
		{
			verb = SqlUtil.getSqlVerb(result.getSourceCommand());
		}

		String dbid = null;
		if (currentConnection != null)
		{
			dbid = currentConnection.getDbId();
		}
		msg = ResourceMgr.getDynamicString("MsgStmtSuccess", verb, dbid);

		if (msg != null) return msg;

		if (StringUtil.isEmptyString(verb))
		{
			verb = ResourceMgr.getString("TxtStatement");
		}
		return ResourceMgr.getFormattedString("MsgKnownStatementOK", verb);
	}

	protected String getSuccessMessage(String sql)
	{
		String verb = getVerb();
		if (StringUtil.isEmptyString(verb))
		{
			verb = SqlUtil.getSqlVerb(sql);
		}
		DdlObjectInfo info = SqlUtil.getDDLObjectInfo(sql);
		if (info != null)
		{
			String msg = getSuccessMessage(info, verb);
			if (msg != null)
			{
				return msg;
			}
		}
		return null;
	}

	protected String getSuccessMessage(DdlObjectInfo info, String verb)
	{
		if ("DROP".equals(verb))
		{
			if (info == null || info.getObjectType() == null)
			{
				return ResourceMgr.getString("MsgGenDropSuccess");
			}
			if (StringUtil.isNonBlank(info.getObjectName()))
			{
				return ResourceMgr.getFormattedString("MsgDropSuccess", info.getDisplayType(), info.getObjectName());
			}
			else
			{
				return ResourceMgr.getFormattedString("MsgDropTypeSuccess", info.getObjectType());
			}
		}
		else if ("CREATE".equals(verb) || "RECREATE".equals(verb))
		{
			if (info == null || info.getObjectType() == null)
			{
				return ResourceMgr.getString("MsgGenCreateSuccess");
			}
			if (StringUtil.isNonBlank(info.getObjectName()))
			{
				return ResourceMgr.getFormattedString("MsgCreateSuccess", info.getDisplayType(), info.getObjectName());
			}
			else
			{
				String typeName = info.getObjectType();
				if ("INDEX".equals(typeName))
				{
					// this is essentially for Postgres which allows to create an index without a name (a name will be generated)
					typeName = info.getDisplayType();
				}
				return ResourceMgr.getFormattedString("MsgCreateTypeSuccess", typeName);
			}
		}
		else if ("ANALYZE".equals(verb))
		{
			String name = currentConnection.getMetadata().adjustObjectnameCase(info.getObjectName());
			return ResourceMgr.getFormattedString("MsgObjectAnalyzed", StringUtil.capitalize(info.getObjectType()), name);
		}
		return null;
	}

	protected void appendSuccessMessage(StatementRunnerResult result)
	{
		result.addMessage(getDefaultSuccessMessage(result));
	}

	public void setParameterPrompter(ParameterPrompter p)
	{
		this.prompter = p;
	}

	public void setShowDataLoading(boolean flag)
	{
		this.showDataLoading = flag;
	}

	protected void appendOutput(StatementRunnerResult result)
	{
		String s = this.currentConnection.getOutputMessages();
		boolean hideLabel = WbEnableOraOutput.HIDE_HINT.equals(runner.getSessionAttribute(StatementRunner.SERVER_MSG_PROP));
		if (!StringUtil.isBlank(s))
		{
			if (result.hasMessages())
			{
				result.addMessageNewLine();
			}
			if (!hideLabel)
			{
				result.addMessage(ResourceMgr.getString("TxtServerOutput"));
			}
			result.addMessage(s);
		}
	}

	/**
	 *	Append any warnings from the given Statement and Connection to the given
	 *	StringBuilder.
	 *
	 * If the connection is a connection to Oracle then any messages written with dbms_output are appended as well
	 * This behaviour is then similar to MS SQL Server where any messages displayed using the PRINT function are
	 * returned in the Warnings.
	 *
	 * @see workbench.util.SqlUtil#getWarnings(WbConnection, Statement)
	 */
	protected boolean appendWarnings(StatementRunnerResult result, boolean addLabel)
	{
		if (this.runner.getHideWarnings()) return false;

		WarningContent warnings = SqlUtil.getWarnings(this.currentConnection, this.currentStatement);
		if (warnings == null) return false;

		CharSequence warn = warnings.allWarnings;
		if (!warnings.isRealWarning)
		{
			addLabel = false;
		}
		boolean hasWarning = false;
		if (warn != null && warn.length() > 0)
		{
			hasWarning = true;
			if (addLabel && result.hasMessages()) result.addMessageNewLine();

			// Only add the "Warnings:" text if the message returned from the
			// server does not already start with "Warning"
			if (addLabel && warn.length() > 7 && !warn.subSequence(0, 7).toString().equalsIgnoreCase("Warning"))
			{
				result.addMessage(ResourceMgr.getString("TxtWarnings"));
			}
			result.addMessage(warn);
			result.setWarning(true);
		}
		if (hasWarning)
		{
			result.addMessageNewLine();
		}
		return hasWarning;
	}

	protected void setUnknownMessage(StatementRunnerResult result, ArgumentParser cmdline, String help)
	{
		if (!cmdLine.hasUnknownArguments()) return;
		StringBuilder msg = new StringBuilder(ResourceMgr.getString("ErrUnknownParameter"));
		msg.append(' ');
		msg.append(cmdLine.getUnknownArguments());
		result.addMessage(msg.toString());
		if (!WbManager.getInstance().isBatchMode() && help != null)
		{
			result.addMessageNewLine();
			result.addMessage(help);
		}
		result.setFailure();
	}

	/**
	 * Cancels this statements execution.
	 *
	 * Cancelling is done by calling <tt>cancel()</tt> on the current JDBC Statement object.
	 * This requires that the JDBC driver actually supports cancelling of statements <b>and</b>
	 * that this method is called from a different thread.
	 *
	 * It also sets the internal cancelled flag so that <tt>SqlCommand</tt>s
	 * that process data in a loop can check this flag and exit the loop
	 * (e.g. {@link workbench.sql.wbcommands.WbExport})
	 *
	 */
	public void cancel()
		throws SQLException
	{
		synchronized (this)
		{
			isCancelled = true;
			if (currentRetrievalData != null)
			{
				currentRetrievalData.cancelRetrieve();
			}

			if (currentStatement != null)
			{
				try
				{
					LogMgr.logTrace("SqlCommand.cancel()", "Cancelling statement execution (" + StringUtil.getMaxSubstring(currentStatement.toString(), 80) + ")");
					currentStatement.cancel();
					LogMgr.logTrace("SqlCommand.cancel()", "Cancelled.");
				}
				catch (Throwable th)
				{
					LogMgr.logWarning("SqlCommand.cancel()", "Error when cancelling statement", th);
				}
			}
		}
	}

	/**
	 * Cleanup all resource after running the statement.
	 *
	 * This method should be called, once the caller is finished with running
	 * the SQL command.
	 *
	 * This releases any database resources that were obtained during the execution of the statement (especially it
	 * closes the JDBC statement object that was used to run this command).
	 * <br/>
	 * Warnings are cleared for the current statement and the connection.
	 */
	public void done()
	{
		synchronized (this)
		{
			if (currentStatement != null)
			{
				String info = getVerb() + " (" + getClass().getSimpleName() + ")";
				LogMgr.logTrace("SqlCommand.done()", "Cleaning up SqlCommand " + info + ")");

				try { currentStatement.clearBatch(); } catch (Throwable th) {}
				try { currentStatement.clearWarnings(); } catch (Throwable th) {}
				try { currentConnection.clearWarnings(); } catch (Throwable e) {}

				try
				{
					currentStatement.close();
				}
				catch (Throwable th)
				{
					LogMgr.logError("SqlCommand.done()", "Error when closing the current statement for: " + info + ")", th);
				}
			}
			currentStatement = null;
			isCancelled = false;
			currentRetrievalData = null;
		}
	}

	/**
	 * Define the StatementRunner that runs this statement.
	 * This is needed e.g. to determine the base directory for
	 * relative file paths (WbInclude, WbImport, etc)
	 *
	 * @param r The runner running this statement.
	 */
	public void setStatementRunner(StatementRunner r)
	{
		this.runner = r;
	}

	public boolean isModeSupported(RunMode mode)
	{
		return true;
	}

	/**
	 * Checks if this statement needs a connection to a database to run.
	 *
	 * This should be overridden by an ancestor not requiring a connection.
	 *
	 * @return true
	 */
	protected boolean isConnectionRequired()
	{
		return true;
	}

	/**
	 * Should be overridden by a specialised SqlCommand.
	 * setConnection must be called before calling execute()
	 *
	 * @see #setConnection(workbench.db.WbConnection)
	 */
	public StatementRunnerResult execute(String sql)
		throws SQLException, Exception
	{
		StatementRunnerResult result = new StatementRunnerResult(sql);

		this.currentStatement = this.currentConnection.createStatement();
		this.isCancelled = false;

		sql = getSqlToExecute(sql);
		String verb = SqlUtil.getSqlVerb(sql);

		result.ignoreUpdateCounts(currentConnection.getDbSettings().verbsWithoutUpdateCount().contains(verb));

		runner.setSavepoint();
		try
		{
			boolean hasResult = this.currentStatement.execute(sql);
			result.setSuccess();
			processResults(result, hasResult);
			appendSuccessMessage(result);
			runner.releaseSavepoint();
		}
		catch (Exception e)
		{
			runner.rollbackSavepoint();
			appendOutput(result);
			appendWarnings(result, true);
			addErrorInfo(result, sql, e);
			LogMgr.logUserSqlError("SqlCommand.execute()", sql, e);
		}
		finally
		{
			this.done();
		}
		return result;
	}

	/**
	 * Tries to process any "pending" results from the last statement that was
	 * executed, but only if the current DBMS supports multiple SQL statements
	 * in a single execute() call.
	 *
	 * In all other cases the current SqlCommand will process results properly.
	 *
	 * If the DBMS does not support "batched" statements, then only possible
	 * warnings are stored in the result object
	 *
	 * @see #processResults(StatementRunnerResult, boolean, ResultSet)
	 * @see #appendWarnings(workbench.sql.StatementRunnerResult, boolean)
	 */
	protected void processMoreResults(String sql, StatementRunnerResult result, boolean hasResult)
		throws SQLException
	{
		if (this.isMultiple(sql))
		{
			processResults(result, hasResult, null);
		}
		else
		{
			appendWarnings(result, true);
		}
	}

	protected void processResults(StatementRunnerResult result, boolean hasResult)
		throws SQLException
	{
		processResults(result, hasResult, null);
	}

	/**
	 * Process any ResultSets or updatecounts generated by the last statement execution.
	 *
	 * @param result the result object to which any DataStore or update count should be added
	 * @param hasResult the return value of the execute() method that was called before
	 * @param firstResult the ResultSet that was obtained by calling executeQuery() before
	 */
	protected void processResults(StatementRunnerResult result, boolean hasResult, ResultSet firstResult)
		throws SQLException
	{
		if (result == null) return;

		if (currentConnection == null || currentConnection.isClosed())
		{
			LogMgr.logError("SqlCommand.processResults()", "Current connection has been closed. Aborting...", null);
			return;
		}

		appendOutput(result);

		// Postgres obviously clears the warnings if the getMoreResults() is called,
		// so we add the warnings before calling getMoreResults(). This doesn't seem
		// to do any harm for other DBMS as well.
		appendWarnings(result, true);

		if (!runner.getStatementHook().fetchResults())
		{
			return;
		}

		int updateCount = -1;
		boolean moreResults = false;

		if (hasResult)
		{
			moreResults = true;
		}
		else
		{
			// the initial execute() did not return a result set, so the first "result" is an updateCount
			try
			{
				updateCount = this.currentStatement.getUpdateCount();
			}
			catch (Exception e)
			{
				LogMgr.logError("SqlCommand.processResults()", "Error when calling getUpdateCount()", e);
				updateCount = -1;
			}

			try
			{
				moreResults = this.currentStatement.getMoreResults();
			}
			catch (Exception e)
			{
				// Some drivers throw errors if no result is available. In this case
				// simply assume there are no more results.
				LogMgr.logError("SqlCommand.processResults()", "Error when calling getMoreResults()", e);
				moreResults = false;
			}
		}

		if (currentConnection == null || currentConnection.isClosed() || currentConnection.getMetadata() == null)
		{
			LogMgr.logError("SqlCommand.processResults()", "Current connection has been closed. Aborting...", null);
			return;
		}

		// if no result was passed, we need to call getResultSet() immediately
		boolean firstResultProcessed = (firstResult == null);

		ResultSet rs = null;
		boolean multipleUpdateCounts = this.currentConnection.getDbSettings().allowsMultipleGetUpdateCounts();

		int counter = 0;
		int maxLoops = currentConnection.getDbSettings().getMaxResults();

		while (moreResults || updateCount > -1)
		{

			if (currentConnection == null || currentConnection.isClosed())
			{
				LogMgr.logError("SqlCommand.processResults()", "Current connection has been closed. Aborting...", null);
				return;
			}

			if (updateCount > -1)
			{
				result.addUpdateCountMsg(updateCount);
			}

			if (moreResults)
			{
				if (!firstResultProcessed)
				{
					rs = firstResult;
					firstResultProcessed = true;
				}
				else
				{
					ResultSet newRs = this.currentStatement.getResultSet();
					// workaround for a MySQL Driver bug that returns the same ResultSet over and over again
					if (newRs == firstResult || newRs == rs)
					{
						LogMgr.logWarning("SqlCommand.processResults()", "Received the same ResultSet for the second time. Aborting!");
						break;
					}
					rs = newRs;
				}

				if (rs == null) break;

				// this is for SQL Server messages sent with "PRINT"
				// they need to be retrieved for each result set.
				appendWarnings(result, false);

				ResultSetConsumer consumer = runner.getConsumer();
				if (consumer != null)
				{
					result.addResultSet(rs);
					consumer.consumeResult(result);
				}
				else
				{
					RowActionMonitor monitorToUse = null;
					if (showDataLoading && runner.getStatementHook().displayResults())
					{
						monitorToUse = this.rowMonitor;
					}

					// we have to use an instance variable for the retrieval, otherwise the retrieval
					// cannot be cancelled!
					this.currentRetrievalData = new DataStore(rs, false, monitorToUse, maxRows, this.currentConnection);
					try
					{
						// Not reading the data in the constructor enables us
						// to cancel the retrieval of the data from the ResultSet
						// without using statement.cancel()
						// The DataStore checks for the cancel flag during processing
						// of the ResulSet
						this.currentRetrievalData.setGeneratingSql(result.getSourceCommand());
						this.currentRetrievalData.initData(rs, maxRows);
					}
					catch (SQLException e)
					{
						// Some JDBC driver throw an exception when a statement is
						// cancelled. But in this case, we do not want to throw away the
						// data that was retrieved until now. We only add a warning
						if (this.currentRetrievalData != null && this.currentRetrievalData.isCancelled())
						{
							result.addMessage(ResourceMgr.getString("MsgErrorDuringRetrieve"));
							result.addMessage(ExceptionUtil.getAllExceptions(e));
							result.setWarning(true);
						}
						else
						{
							// if the statement was not cancelled, make sure
							// the error is displayed to the user.
							throw e;
						}
					}
					finally
					{
						SqlUtil.closeResult(rs);
					}

					if (runner.getStatementHook().displayResults())
					{
						result.addDataStore(this.currentRetrievalData);
					}
					result.addRowsProcessed(currentRetrievalData.getRowCount());
				}
			}

			if (currentConnection == null || currentConnection.isClosed())
			{
				LogMgr.logError("SqlCommand.processResults()", "Current connection has been closed. Aborting...", null);
				return;
			}

			try
			{
				moreResults = this.currentStatement.getMoreResults();
			}
			catch (SQLException sql)
			{
				// SQL Exceptions should be shown to the user
				LogMgr.logError("SqlCommand.processResults()", "Error when calling getMoreResults()", sql);
				result.setWarning(true);
				result.addMessage("\n" + sql.getMessage().trim() + "\n");
				break;
			}
			catch (Throwable th)
			{
				// Some drivers do not support getMoreResults() properly.
				// So, this exception is simply ignored, so that processing can proceed normally
				LogMgr.logError("SqlCommand.processResults()", "Error when calling getMoreResults()", th);
				break;
			}

			if (multipleUpdateCounts)
			{
				try
				{
					updateCount = this.currentStatement.getUpdateCount();
				}
				catch (Exception e)
				{
					LogMgr.logWarning("SqlCommand.processResult()", "Error when calling getUpdateCount(): " + ExceptionUtil.getDisplay(e));
					updateCount = -1;
					multipleUpdateCounts = false;
				}
			}
			else
			{
				updateCount = -1;
			}

			counter ++;

			// some JDBC drivers do not implement getMoreResults() and getUpdateCount()
			// correctly, so this is a safety to prevent an endless loop
			if (maxLoops > 0 && counter >= maxLoops)
			{
				LogMgr.logWarning("SqlCommand.processResults()", "Breaking out of loop because " + maxLoops + " iterations reached");
				break;
			}
		}

		this.currentRetrievalData = null;
	}

	/**
	 * Remove comments from the SQL if the current connection profile has the
	 * corresponding option defined.
	 *
	 * Additionally the DB specific settings is checked whether the DBMS supports
	 * comments inside SQL statements.
	 *
	 * @param originalSql
	 * @return the sql with comments removed if necessary.
	 *
	 * @see workbench.db.ConnectionProfile#getRemoveComments()
	 * @see workbench.db.DbSettings#supportsCommentInSql()
	 * @see workbench.db.DbSettings#removeNewLinesInSQL()
	 */
	protected String getSqlToExecute(String originalSql)
	{
		try
		{
			boolean removeComments = currentConnection.getRemoveComments();
			boolean removeNewLines = currentConnection.getRemoveNewLines();

			if (!removeComments && !removeNewLines ) return originalSql;

			return SqlUtil.makeCleanSql(originalSql, !removeNewLines, !removeComments);
		}
		catch (NullPointerException npe)
		{
			// Just in case currentConnection is not initialized for some reason!
			return originalSql;
		}
	}

	/**
	 * Define the connection for this command.
	 * <br/>
	 * The StatementRunner keeps only one instance of each command, so
	 * this must be called every time before execute() is called to make
	 * sure the command is acting on the correct connection.
	 * <br/>
	 *
	 * @param conn the new current connection
	 * @see #execute(java.lang.String)
	 */
	public void setConnection(WbConnection conn)
	{
		this.currentConnection = conn;
	}

	/**
	 *	Should be overridden by a specialised SqlCommand
	 */
	public String getVerb()
	{
		return StringUtil.EMPTY_STRING;
	}

	public String getAlternateVerb()
	{
		return null;
	}

	/**
	 * Check if this verb for this statement is considered an updating command in
	 * all circumstances.
	 */
	public boolean isUpdatingCommand()
	{
		return this.isUpdatingCommand;
	}

	/**
	 * Check if this statement needs a confirmation by the user
	 * to be run.
	 *
	 * @param con the current connection
	 * @param sql the statement to be executed
	 * @return true if the user should confirm to run this statement
	 * @see #getModificationTarget(workbench.db.WbConnection, java.lang.String)
	 * @see workbench.db.ConnectionProfile#getConfirmUpdates()
	 */
	public boolean needConfirmation(WbConnection con, String sql)
	{
		if (con == null || con.isClosed()) return false;
		ConnectionProfile prof = getModificationTarget(con, sql);
		if (prof == null) return false;
		if (isUpdatingCommand(con, sql))
		{
			if (con.confirmUpdatesInSession())
			{
				return true;
			}
			if (prof.getPreventDMLWithoutWhere())
			{
				return SqlUtil.isUnRestrictedDML(sql);
			}
		}
		return false;
	}

	/**
	 * Check if the given SQL statement may update the database.
	 * This method will also consider additional keywords
	 * added by the user (globally or DBMS specific) and check if
	 * the SELECT statement is a disguised CREATE TABLE.
	 *
	 * @param con the database connection to check against
	 * @param sql the SQL statement to check
	 * @return true if the SQL might update the database
	 *
	 * @see workbench.db.DbSettings#isUpdatingCommand(java.lang.String)
	 * @see workbench.db.DbMetadata#isSelectIntoNewTable(java.lang.String)
	 */
	public boolean isUpdatingCommand(WbConnection con, String sql)
	{
		if (this.isUpdatingCommand) return true;
		if (con == null) return isUpdatingCommand;
		if (con.isClosed()) return isUpdatingCommand;
		String verb = SqlUtil.getSqlVerb(sql);
		boolean updating = con.getDbSettings().isUpdatingCommand(verb);
		if (updating) return true;
		return con.getMetadata().isSelectIntoNewTable(sql);
	}

	/**
	 * Returns the profile that is attached to the connection where the
	 * data would be modified.
	 * The default implementation returns the profile attached to the
	 * passed connection. WbCopy will check the commandline and will
	 * return the profile specified in the -targetProfile parameter
	 *
	 * @param con the connection to check
	 * @param sql the SQL statement to check (ignored)
	 * @return the profile attached to the connection
	 *
	 * @see workbench.sql.wbcommands.WbCopy#getModificationTarget(workbench.db.WbConnection, java.lang.String)
	 */
	public ConnectionProfile getModificationTarget(WbConnection con, String sql)
	{
		if (con == null) return null;
		return con.getProfile();
	}

	/**
	 * Check if the current profile allows running of the given SQL. This
	 * will check for the read-only option and will disallowe statements
	 * where {@link #isUpdatingCommand(workbench.db.WbConnection, java.lang.String)}
	 * returns true.
	 *
	 * @param con
	 * @param sql
	 * @return true if the profile is not read-only, or if the statement does not update anything
	 * @see #isUpdatingCommand(workbench.db.WbConnection, java.lang.String)
	 */
	public boolean isModificationAllowed(WbConnection con, String sql)
	{
		if (con == null || con.isClosed()) return true;
		ConnectionProfile prof = getModificationTarget(con, sql);
		if (prof == null) return true;

		// if the modification target is not the profile of the current connection
		// only the readOnly property of the profile is relevant
		if (!con.getProfile().equals(prof) && prof.isReadOnly())
		{
			return false;
		}

		// the command modifies the profile of the current connection,
		// the state of the current connection is relevant.
		if (con.getProfile().equals(prof) && isUpdatingCommand(con, sql))
		{
			if (con.isSessionReadOnly()) return false;
		}
		return true;
	}

	public void setQueryTimeout(int timeout)
	{
		this.queryTimeout = timeout;
	}

	public void setMaxRows(int max)
	{
		maxRows = max;
	}


	/**
	 * Get a "clean" version of the sql with the verb stripped off
	 * and all comments and newlines removed for processing the
	 * parameters to a Workbench command
	 *
	 * @param sql the sql to "clean"
	 * @return the sql with the verb, comments and newlines removed
	 * @see workbench.util.SqlUtil#makeCleanSql(java.lang.String, boolean)
	 * @see workbench.util.SqlUtil#getSqlVerb(String)
	 */
	protected String getCommandLine(String sql)
	{
		return SqlUtil.stripVerb(SqlUtil.makeCleanSql(sql, false, false));
	}

	/**
	 * Assumes the given parameter is a filename supplied by the end user.
	 * <br/>
	 * If the filename is absolute, a file object with that path is returned.
	 * If the filename does not contain a full path, the current baseDir of the
	 * StatementRunner is added to the returned file.
	 *
	 * @param fileName
	 * @return a File object pointing to the file indicated by the user.
	 * @see workbench.sql.StatementRunner#getBaseDir()
	 */
	public WbFile evaluateFileArgument(String fileName)
	{
		if (StringUtil.isEmptyString(fileName)) return null;

		String fname = StringUtil.trimQuotes(fileName);
		WbFile f  = new WbFile(fname);
		if (f.isAbsolute()) return f;

		// Use the "current" directory of the StatementRunner
		// for the path of the file, if no path is specified.
		if (this.runner != null)
		{
			String dir = this.runner.getBaseDir();
			if (StringUtil.isNonEmpty(dir))
			{
				f = new WbFile(dir, fname);
			}
		}
		return f;
	}

	protected void addErrorStatementInfo(StatementRunnerResult result, String sql)
	{
		if (errorLevel != ErrorReportLevel.none)
		{
			StringBuilder msg = new StringBuilder(150);
			msg.append(ResourceMgr.getString("MsgExecuteError"));
			msg.append('\n');
			if (errorLevel == ErrorReportLevel.full)
			{
				msg.append(sql);
			}
			else
			{
				msg.append(StringUtil.getMaxSubstring(sql.trim(), Settings.getInstance().getMaxErrorStatementLength()));
			}
			result.addMessage(msg);
			result.addMessageNewLine();
		}
	}

	protected void addErrorInfo(StatementRunnerResult result, String executedSql, Exception e)
	{
		addErrorStatementInfo(result, executedSql);
		addErrorPosition(result, executedSql, e);
	}

	protected void addErrorPosition(StatementRunnerResult result, String executedSql, Exception e)
	{
		try
		{
			ErrorPositionReader reader = ErrorPositionReader.Factory.createPositionReader(currentConnection);
			String sql = result.getSourceCommand() == null ? executedSql : result.getSourceCommand();
			ErrorDescriptor error = reader.getErrorPosition(currentConnection, sql, e);
			if (error != null)
			{
				String fullMsg = reader.enhanceErrorMessage(sql, e.getMessage(), error);
				result.setFailure(error);
				result.addMessage(fullMsg);
			}
			else
			{
				result.addMessage(ExceptionUtil.getDisplay(e));
				result.setFailure();
			}
		}
		catch (Throwable th)
		{
			LogMgr.logError("SqlCommand.addErrorPosition()", "Could not retrieve error position!", th);
			result.addMessage(ExceptionUtil.getAllExceptions(e));
			result.setFailure();
		}
	}

	/**
	 * Check if the passed SQL is a "batched" statement.
	 * <br/>
	 * Returns true if the passed SQL string could be a "batched"
	 * statement that actually contains more than one statement.
	 * SQL Server supports these kind of "batches". If this is the case
	 * affected rows will always be shown, because we cannot know
	 * if the statement did not update anything or if it actually
	 * updated only 0 rows (for some reason SQL Server seems to
	 * return 0 as the updatecount even if no update was involved).
	 * <br/>
	 * Currently this is only checking for newlines in the passed string.
	 *
	 * @param sql the statement/script to check
	 * @return true if the passed sql could contain more than one (independent) statements
	 */
	protected boolean isMultiple(String sql)
	{
		if (this.currentConnection == null) return false;
		if (this.currentConnection.getMetadata() == null) return false;
		DbSettings settings = currentConnection.getDbSettings();
		if (settings.supportsBatchedStatements())
		{
			return (sql.indexOf('\n') > -1);
		}
		return false;
	}

	@Override
	public String toString()
	{
		return getVerb();
	}

}
