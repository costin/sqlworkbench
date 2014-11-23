/*
 * StatementRunner.java
 *
 * Created on 16. November 2002, 13:43
 */

package workbench.sql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import workbench.db.DbMetadata;

import workbench.db.WbConnection;
import workbench.log.LogMgr;
import workbench.sql.MacroManager;
import workbench.sql.commands.DdlCommand;
import workbench.sql.commands.SelectCommand;
import workbench.sql.commands.SetCommand;
import workbench.sql.commands.SingleVerbCommand;
import workbench.sql.commands.UpdatingCommand;
import workbench.sql.commands.UseCommand;
import workbench.sql.wbcommands.WbCopy;
import workbench.sql.wbcommands.WbDescribeTable;
import workbench.sql.wbcommands.WbDisableOraOutput;
import workbench.sql.wbcommands.WbEnableOraOutput;
import workbench.sql.wbcommands.WbHelp;
import workbench.sql.wbcommands.WbImport;
import workbench.sql.wbcommands.WbListCatalogs;
import workbench.sql.wbcommands.WbListProcedures;
import workbench.sql.wbcommands.WbListTables;
import workbench.sql.wbcommands.WbOraExecute;
import workbench.sql.wbcommands.WbExport;
import workbench.storage.RowActionMonitor;
import workbench.util.SqlUtil;
import workbench.sql.commands.IgnoredCommand;
import workbench.util.StringUtil;

/**
 *
 * @author  workbench@kellerer.org
 */
public class StatementRunner
{
	private WbConnection dbConnection;
	private StatementRunnerResult result;

	private HashMap cmdDispatch;
	private ArrayList dbSpecificCommands;

	private SqlCommand currentCommand;
	private SqlCommand currentConsumer;

	private int maxRows;
	private boolean isCancelled;

	private RowActionMonitor rowMonitor;

	public StatementRunner()
	{
		cmdDispatch = new HashMap();
		cmdDispatch.put("*", new SqlCommand());

		SqlCommand sql = new WbListTables();
		cmdDispatch.put(sql.getVerb(), sql);

		sql = new WbHelp();
		cmdDispatch.put(sql.getVerb(), sql);

		sql = new WbListProcedures();
		cmdDispatch.put(sql.getVerb(), sql);

		sql = new WbDescribeTable();
		cmdDispatch.put(sql.getVerb(), sql);
    cmdDispatch.put("DESCRIBE", sql);

		sql = new WbEnableOraOutput();
		cmdDispatch.put(sql.getVerb(), sql);

		sql = new WbDisableOraOutput();
		cmdDispatch.put(sql.getVerb(), sql);

		sql = new SelectCommand();
		cmdDispatch.put(sql.getVerb(), sql);

		sql = new WbExport();
		cmdDispatch.put(sql.getVerb(), sql);
		cmdDispatch.put("EXP", sql);
		cmdDispatch.put("EXPORT", sql);
		cmdDispatch.put("SPOOL", sql);

		sql = new WbImport();
		cmdDispatch.put(sql.getVerb(), sql);
		cmdDispatch.put("IMP", sql);

		sql = new WbCopy();
		cmdDispatch.put(sql.getVerb(), sql);

		cmdDispatch.put(WbListCatalogs.LISTCAT.getVerb(), WbListCatalogs.LISTCAT);
		cmdDispatch.put(WbListCatalogs.LISTDB.getVerb(), WbListCatalogs.LISTDB);

		cmdDispatch.put(SingleVerbCommand.COMMIT.getVerb(), SingleVerbCommand.COMMIT);
		cmdDispatch.put(SingleVerbCommand.ROLLBACK.getVerb(), SingleVerbCommand.ROLLBACK);

		cmdDispatch.put(UpdatingCommand.DELETE.getVerb(), UpdatingCommand.DELETE);
		cmdDispatch.put(UpdatingCommand.INSERT.getVerb(), UpdatingCommand.INSERT);
		cmdDispatch.put(UpdatingCommand.UPDATE.getVerb(), UpdatingCommand.UPDATE);

		for (int i=0; i < DdlCommand.DDL_COMMANDS.size(); i ++)
		{
			sql = (SqlCommand)DdlCommand.DDL_COMMANDS.get(i);
			cmdDispatch.put(sql.getVerb(), sql);
		}

		this.dbSpecificCommands = new ArrayList();
	}

	public void setConnection(WbConnection aConn)
	{

		if (this.dbConnection != null)
		{
			int count = this.dbSpecificCommands.size();
			for (int i=0; i < count; i++)
			{
				this.cmdDispatch.remove(this.dbSpecificCommands.get(i));
			}
			this.dbSpecificCommands.clear();
		}

		this.dbConnection = aConn;

		if (this.dbConnection.getMetadata().isOracle())
		{
			this.cmdDispatch.put(WbOraExecute.EXEC.getVerb(), WbOraExecute.EXEC);
			this.cmdDispatch.put(WbOraExecute.EXECUTE.getVerb(), WbOraExecute.EXECUTE);

			SetCommand set = new SetCommand();
			this.cmdDispatch.put(set.getVerb(), set);
			this.dbSpecificCommands.add(set.getVerb());
			this.dbSpecificCommands.add(WbOraExecute.EXEC.getVerb());
			this.dbSpecificCommands.add(WbOraExecute.EXECUTE.getVerb());
		}
		else if (this.dbConnection.getMetadata().isSqlServer())
		{
			UseCommand cmd = new UseCommand();
			this.cmdDispatch.put(cmd.getVerb(), cmd);
			this.dbSpecificCommands.add(cmd.getVerb());
		}
		
		String verbs = this.dbConnection.getMetadata().getVerbsToIgnore();
		List l = StringUtil.stringToList(verbs, ",");
		for (int i=0; i < l.size(); i++)
		{
			String verb = (String)l.get(i);
			if (verb == null) continue;
			verb = verb.toUpperCase();
			IgnoredCommand cmd = new IgnoredCommand(verb);
			this.cmdDispatch.put(verb, cmd);
			this.dbSpecificCommands.add(verb);
		}
		
	}

	public StatementRunnerResult getResult()
	{
		return this.result;
	}

	public void setRowMonitor(RowActionMonitor monitor)
	{
		this.rowMonitor = monitor;
	}

	public void runStatement(String aSql, int maxRows)
		throws SQLException, Exception
	{
		String cleanSql = SqlUtil.makeCleanSql(aSql, false);
		if (cleanSql == null || cleanSql.length() == 0)
		{
			if (this.result == null)
			{
				this.result = new StatementRunnerResult("");
			}
			this.result.clear();
			this.result.setSuccess();
			return;
		}

		String verb = this.getRealVerb(cleanSql);

		this.currentCommand = (SqlCommand)this.cmdDispatch.get(verb);

		// if no mapping is found use the default implementation
		if (this.currentCommand == null)
		{
			this.currentCommand = (SqlCommand)this.cmdDispatch.get("*");
		}

		if (this.dbConnection.getMetadata().isInformix())
		{
			LogMgr.logDebug("StatementRunner.runStatement()", "Using command instance " + this.currentCommand.getClass().getName() + " to execute the statement: " + aSql);
		}

		this.currentCommand.setConsumerWaiting(this.currentConsumer != null);
		this.currentCommand.setRowMonitor(this.rowMonitor);
		this.currentCommand.setMaxRows(maxRows);
		this.result = this.currentCommand.execute(this.dbConnection, aSql);

		if (this.currentCommand.isResultSetConsumer())
		{
			this.currentConsumer = this.currentCommand;
		}
		else
		{
			if (this.currentConsumer != null)
			{
				this.currentCommand.setConsumerWaiting(false);
				this.currentConsumer.consumeResult(this.result);
				this.currentConsumer = null;
			}
		}

	}

	/**
	 *	Check for a SELECT ... INTO syntax for Informix which actually
	 *  creates a table. In that case we will simply pretend it's a
	 *  CREATE statement
	 */
	private String getRealVerb(String cleanSql)
	{
		String verb = SqlUtil.getSqlVerb(cleanSql).toUpperCase();
		DbMetadata meta = this.dbConnection.getMetadata();
		if (!meta.supportsSelectIntoNewTable()) return verb;

		if (meta.isSelectIntoNewTable(cleanSql))
		{
			LogMgr.logDebug("StatementRunner.getRealVerb()", "Found 'SELECT ... INTO new_table'");
			return "*";
		}
		else
		{
			return verb;
		}
	}

	public void statementDone()
	{
		if (this.currentCommand != null && this.currentConsumer != this.currentCommand)
		{
			this.currentCommand.done();
			this.currentCommand = null;
		}
	}

	public void cancel()
	{
		try
		{
			if (this.currentCommand != null)
			{
				this.isCancelled = true;
				this.currentCommand.cancel();
			}
		}
		catch (Throwable th)
		{
			LogMgr.logDebug("StatementRunner.cancel()", "Error when cancelling statement", th);
		}
	}

	public void done()
	{
		if (this.result != null) this.result.clear();
		this.result = null;
		this.statementDone();
		this.currentConsumer = null;
	}

}