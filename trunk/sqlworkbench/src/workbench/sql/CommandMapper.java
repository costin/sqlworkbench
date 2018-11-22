/*
 * This file is part of SQL Workbench/J, https://www.sql-workbench.eu
 *
 * Copyright 2002-2018, Thomas Kellerer
 *
 * Licensed under a modified Apache License, Version 2.0
 * that restricts the use for certain governments.
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at.
 *
 *     https://www.sql-workbench.eu/manual/license.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * To contact the author please send an email to: support@sql-workbench.eu
 *
 */
package workbench.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import workbench.log.CallerInfo;

import workbench.db.DbMetadata;
import workbench.db.WbConnection;

import workbench.log.LogMgr;
import workbench.resource.Settings;

import workbench.db.DBID;

import workbench.sql.commands.AlterSessionCommand;
import workbench.sql.commands.DdlCommand;
import workbench.sql.commands.IgnoredCommand;
import workbench.sql.commands.SelectCommand;
import workbench.sql.commands.SetCommand;
import workbench.sql.commands.TransactionEndCommand;
import workbench.sql.commands.TransactionStartCommand;
import workbench.sql.commands.UpdatingCommand;
import workbench.sql.commands.UseCommand;
import workbench.sql.wbcommands.MySQLShow;
import workbench.sql.wbcommands.PgCopyCommand;
import workbench.sql.wbcommands.WbCall;
import workbench.sql.wbcommands.WbConfirm;
import workbench.sql.wbcommands.WbConnInfo;
import workbench.sql.wbcommands.WbConnect;
import workbench.sql.wbcommands.WbCopy;
import workbench.sql.wbcommands.WbDataDiff;
import workbench.sql.wbcommands.WbDefinePk;
import workbench.sql.wbcommands.WbDefineVar;
import workbench.sql.wbcommands.WbDescribeObject;
import workbench.sql.wbcommands.WbDisableOraOutput;
import workbench.sql.wbcommands.WbEcho;
import workbench.sql.wbcommands.WbEnableOraOutput;
import workbench.sql.wbcommands.WbEndBatch;
import workbench.sql.wbcommands.WbExport;
import workbench.sql.wbcommands.WbFeedback;
import workbench.sql.wbcommands.WbFetchSize;
import workbench.sql.wbcommands.WbGenDelete;
import workbench.sql.wbcommands.WbGenDrop;
import workbench.sql.wbcommands.WbGenImpTable;
import workbench.sql.wbcommands.WbGenInsert;
import workbench.sql.wbcommands.WbGenerateFKScript;
import workbench.sql.wbcommands.WbGenerateScript;
import workbench.sql.wbcommands.WbGrepData;
import workbench.sql.wbcommands.WbGrepSource;
import workbench.sql.wbcommands.WbHelp;
import workbench.sql.wbcommands.WbHideWarnings;
import workbench.sql.wbcommands.WbHistory;
import workbench.sql.wbcommands.WbImport;
import workbench.sql.wbcommands.WbInclude;
import workbench.sql.wbcommands.WbIsolationLevel;
import workbench.sql.wbcommands.WbList;
import workbench.sql.wbcommands.WbListCatalogs;
import workbench.sql.wbcommands.WbListDependencies;
import workbench.sql.wbcommands.WbListIndexes;
import workbench.sql.wbcommands.WbListPkDef;
import workbench.sql.wbcommands.WbListProcedures;
import workbench.sql.wbcommands.WbListSchemas;
import workbench.sql.wbcommands.WbListTriggers;
import workbench.sql.wbcommands.WbListVars;
import workbench.sql.wbcommands.WbLoadPkMapping;
import workbench.sql.wbcommands.WbMessage;
import workbench.sql.wbcommands.WbMode;
import workbench.sql.wbcommands.WbObjectGrants;
import workbench.sql.wbcommands.WbOraShow;
import workbench.sql.wbcommands.WbProcSource;
import workbench.sql.wbcommands.WbRemoveVar;
import workbench.sql.wbcommands.WbRestoreConnection;
import workbench.sql.wbcommands.WbRowCount;
import workbench.sql.wbcommands.WbRunLB;
import workbench.sql.wbcommands.WbSavePkMapping;
import workbench.sql.wbcommands.WbSchemaDiff;
import workbench.sql.wbcommands.WbSchemaReport;
import workbench.sql.wbcommands.WbSelectBlob;
import workbench.sql.wbcommands.WbSetProp;
import workbench.sql.wbcommands.WbShowEncoding;
import workbench.sql.wbcommands.WbShowProps;
import workbench.sql.wbcommands.WbStartBatch;
import workbench.sql.wbcommands.WbSwitchDB;
import workbench.sql.wbcommands.WbSysExec;
import workbench.sql.wbcommands.WbSysOpen;
import workbench.sql.wbcommands.WbTableSource;
import workbench.sql.wbcommands.WbTriggerSource;
import workbench.sql.wbcommands.WbViewSource;
import workbench.sql.wbcommands.WbXslt;
import workbench.sql.wbcommands.console.WbAbout;
import workbench.sql.wbcommands.console.WbCreateProfile;
import workbench.sql.wbcommands.console.WbDefineDriver;
import workbench.sql.wbcommands.console.WbDefineMacro;
import workbench.sql.wbcommands.console.WbDeleteMacro;
import workbench.sql.wbcommands.console.WbDeleteProfile;
import workbench.sql.wbcommands.console.WbDisconnect;
import workbench.sql.wbcommands.console.WbDisplay;
import workbench.sql.wbcommands.console.WbListDrivers;
import workbench.sql.wbcommands.console.WbListMacros;
import workbench.sql.wbcommands.console.WbListProfiles;
import workbench.sql.wbcommands.console.WbRun;
import workbench.sql.wbcommands.console.WbSetDisplaySize;
import workbench.sql.wbcommands.console.WbStoreProfile;
import workbench.sql.wbcommands.console.WbToggleDisplay;

import workbench.util.CaseInsensitiveComparator;
import workbench.util.CollectionUtil;
import workbench.util.SqlParsingUtil;
import workbench.util.SqlUtil;
import workbench.util.StringUtil;

/**
 * @author Thomas Kellerer
 */
public class CommandMapper
{
	private final Map<String, SqlCommand> cmdDispatch;
	private final List<String> dbSpecificCommands;
	private final Set<String> passThrough = CollectionUtil.caseInsensitiveSet();
	private boolean supportsSelectInto;
	private DbMetadata metaData;
	private final boolean allowAbbreviated;

	public CommandMapper()
	{
		cmdDispatch = new TreeMap<>(CaseInsensitiveComparator.INSTANCE);
		cmdDispatch.put("*", new SqlCommand());

		// Workbench specific commands
		addCommand(new WbList());
		addCommand(new WbListProcedures());
		addCommand(new WbDefineVar());
		addCommand(new WbEnableOraOutput());
		addCommand(new WbDisableOraOutput());
		addCommand(new WbStartBatch());
		addCommand(new WbEndBatch());
		addCommand(new WbXslt());
		addCommand(new WbRemoveVar());
		addCommand(new WbListVars());
		addCommand(new WbExport());
		addCommand(new WbImport());
		addCommand(new WbCopy());
		addCommand(new WbSchemaReport());
		addCommand(new WbSchemaDiff());
		addCommand(new WbDataDiff());
		addCommand(new WbFeedback());
		addCommand(new WbDefinePk());
		addCommand(new WbListPkDef());
		addCommand(new WbLoadPkMapping());
		addCommand(new WbSavePkMapping());
		addCommand(new WbConfirm());
		addCommand(new WbMessage());
		addCommand(new WbCall());
		addCommand(new WbConnect());
    addCommand(new WbRestoreConnection());
		addCommand(new WbInclude());
		addCommand(new WbListCatalogs());
		addCommand(new WbListSchemas());
		addCommand(new WbHelp());
		addCommand(new WbSelectBlob());
		addCommand(new WbHideWarnings());
		addCommand(new WbProcSource());
		addCommand(new WbListTriggers());
		addCommand(new WbListIndexes());
		addCommand(new WbTriggerSource());
		addCommand(new WbViewSource());
		addCommand(new WbTableSource());
		addCommand(new WbDescribeObject());
		addCommand(new WbGrepSource());
		addCommand(new WbGrepData());
		addCommand(new WbMode());
		addCommand(new WbFetchSize());
		addCommand(new WbAbout());
		addCommand(new WbRunLB());
		addCommand(new WbIsolationLevel());
		addCommand(new WbConnInfo());
		addCommand(new WbSysExec());
		addCommand(new WbSysOpen());
		addCommand(new WbShowProps());
    WbSetProp set = new WbSetProp();
		addCommand(set);
    cmdDispatch.put(WbSetProp.SET_DB_CONFIG_VERB, set);
		addCommand(new WbGenDrop());
		addCommand(new WbGenerateScript());
		addCommand(new WbGenerateFKScript());
		addCommand(new WbGenDelete());
		addCommand(new WbGenInsert());
    addCommand(new WbGenImpTable());
    addCommand(new WbObjectGrants());
		addCommand(new WbEcho());
		addCommand(new WbShowEncoding());
		addCommand(new WbRowCount());

		addCommand(new WbDisconnect());
		addCommand(new WbDisplay());
		addCommand(new WbToggleDisplay());
		addCommand(new WbSetDisplaySize());
		addCommand(new WbRun());
		addCommand(new WbHistory());
		addCommand(new WbListMacros());
		addCommand(new WbDefineMacro());
		addCommand(new WbDeleteMacro());

		addCommand(new WbStoreProfile());
		addCommand(new WbDeleteProfile());
		addCommand(new WbCreateProfile());
		addCommand(new WbDefineDriver());
		addCommand(new WbListProfiles());
		addCommand(new WbListDrivers());
    addCommand(new WbListDependencies());

		// Wrappers for standard SQL statements
		addCommand(TransactionEndCommand.getCommit());
		addCommand(TransactionEndCommand.getRollback());

		addCommand(UpdatingCommand.getDeleteCommand());
		addCommand(UpdatingCommand.getInsertCommand());
		addCommand(UpdatingCommand.getUpdateCommand());
		addCommand(UpdatingCommand.getTruncateCommand());

    addCommand(new WbSwitchDB());
		addCommand(new SetCommand());
		addCommand(new SelectCommand());

		for (DdlCommand cmd : DdlCommand.getDdlCommands())
		{
			addCommand(cmd);
		}
		this.cmdDispatch.put("CREATE OR REPLACE", DdlCommand.getCreateCommand());

		this.dbSpecificCommands = new ArrayList<>();
		this.allowAbbreviated = Settings.getInstance().getBoolProperty("workbench.sql.allow.abbreviation", false);
		registerExtensions();
	}

	private void registerExtensions()
	{
		List<SqlCommand> commands = CommandRegistry.getInstance().getCommands();
		for (SqlCommand cmd : commands)
		{
			addCommand(cmd);
		}
	}

	public Collection<String> getAllWbCommands()
	{
		Collection<SqlCommand> commands = cmdDispatch.values();
		TreeSet<String> result = new TreeSet<>();
		for (SqlCommand cmd : commands)
		{
			if (cmd.isWbCommand())
			{
				result.addAll(cmd.getAllVerbs());
			}
		}
		return result;
	}

	/**
	 * Add a new command definition during runtime.
	 */
	public final void addCommand(SqlCommand command)
	{
    for (String verb : command.getAllVerbs())
    {
      cmdDispatch.put(verb, command);
    }
	}

	/**
	 * Initialize the CommandMapper with a database connection.
	 * This will add DBMS specific commands to the internal dispatch.
	 *
	 * This method can be called multiple times.
	 */
	public void setConnection(WbConnection aConn)
	{
		this.cmdDispatch.keySet().removeAll(dbSpecificCommands);
		this.dbSpecificCommands.clear();
		this.supportsSelectInto = false;

		if (aConn == null) return;

		this.metaData = aConn.getMetadata();

		if (metaData == null)
		{
			LogMgr.logError("CommandMapper.setConnection()","Received connection without metaData!", null);
			return;
		}

		if (metaData.isOracle())
		{
			SqlCommand wbcall = this.cmdDispatch.get(WbCall.VERB);

			this.cmdDispatch.put(WbCall.EXEC_VERB_LONG, wbcall);
			this.cmdDispatch.put(WbCall.EXEC_VERB_SHORT, wbcall);

			AlterSessionCommand alter = new AlterSessionCommand();
			this.cmdDispatch.put(alter.getVerb(), alter);
			this.cmdDispatch.put(WbOraShow.VERB, new WbOraShow());

			WbFeedback echo = new WbFeedback("ECHO");
			this.cmdDispatch.put(echo.getVerb(), echo);

			SqlCommand wbEcho = this.cmdDispatch.get(WbEcho.VERB);
			this.cmdDispatch.put("prompt", wbEcho);

			SqlCommand confirm = this.cmdDispatch.get(WbConfirm.VERB);
			this.cmdDispatch.put("pause", confirm);

			this.dbSpecificCommands.add("pause");
			this.dbSpecificCommands.add("prompt");
			this.dbSpecificCommands.add(alter.getVerb());
			this.dbSpecificCommands.add(WbCall.EXEC_VERB_LONG);
			this.dbSpecificCommands.add(WbCall.EXEC_VERB_SHORT);
			this.dbSpecificCommands.add(echo.getVerb());
			this.dbSpecificCommands.add(WbOraShow.VERB);
		}

		if (metaData.isSqlServer() || metaData.isMySql())
		{
			UseCommand cmd = new UseCommand();
			this.cmdDispatch.put(cmd.getVerb(), cmd);
			this.dbSpecificCommands.add(cmd.getVerb());
		}

    if (metaData.isFirebird())
		{
			DdlCommand recreate = DdlCommand.getRecreateCommand();
			this.cmdDispatch.put(recreate.getVerb(), recreate);
			this.dbSpecificCommands.add(recreate.getVerb());
		}

    if (metaData.isPostgres())
		{
      PgCopyCommand copy = new PgCopyCommand();

      this.cmdDispatch.put(copy.getVerb(), copy);
			this.dbSpecificCommands.add(copy.getVerb());
    }

    if (metaData.isPostgres() || DBID.Greenplum.isDB(metaData.getDbId()) || DBID.Redshift.isDB(metaData.getDbId()))
    {
      // support manual transactions in auto commit mode
      this.cmdDispatch.put(TransactionStartCommand.BEGIN.getVerb(), TransactionStartCommand.BEGIN);
      this.cmdDispatch.put(TransactionStartCommand.START_TRANSACTION.getVerb(), TransactionStartCommand.START_TRANSACTION);
      this.cmdDispatch.put(TransactionStartCommand.BEGIN_TRANSACTION.getVerb(), TransactionStartCommand.BEGIN_TRANSACTION);
      this.cmdDispatch.put(TransactionStartCommand.BEGIN_WORK.getVerb(), TransactionStartCommand.BEGIN_WORK);
      this.dbSpecificCommands.add(TransactionStartCommand.START_TRANSACTION.getVerb());
      this.dbSpecificCommands.add(TransactionStartCommand.BEGIN_TRANSACTION.getVerb());
      this.dbSpecificCommands.add(TransactionStartCommand.BEGIN_WORK.getVerb());
      this.dbSpecificCommands.add(TransactionStartCommand.BEGIN.getVerb());
		}

    if (metaData.isSqlServer())
    {
      this.cmdDispatch.put(TransactionStartCommand.BEGIN_TRANSACTION.getVerb(), TransactionStartCommand.BEGIN_TRANSACTION);
      this.cmdDispatch.put(TransactionStartCommand.BEGIN_TRAN.getVerb(), TransactionStartCommand.BEGIN_TRAN);
      this.dbSpecificCommands.add(TransactionStartCommand.BEGIN_TRANSACTION.getVerb());
      this.dbSpecificCommands.add(TransactionStartCommand.BEGIN_TRAN.getVerb());
    }

    if (metaData.isVertica())
    {
      this.cmdDispatch.put(TransactionStartCommand.BEGIN_TRANSACTION.getVerb(), TransactionStartCommand.BEGIN_TRANSACTION);
      this.cmdDispatch.put(TransactionStartCommand.BEGIN_WORK.getVerb(), TransactionStartCommand.BEGIN_WORK);
      this.cmdDispatch.put(TransactionStartCommand.START_TRANSACTION.getVerb(), TransactionStartCommand.START_TRANSACTION);
      this.dbSpecificCommands.add(TransactionStartCommand.BEGIN_TRANSACTION.getVerb());
      this.dbSpecificCommands.add(TransactionStartCommand.BEGIN_WORK.getVerb());
      this.dbSpecificCommands.add(TransactionStartCommand.START_TRANSACTION.getVerb());
    }

    if (metaData.isMySql())
		{
			MySQLShow show = new MySQLShow();
			this.cmdDispatch.put(show.getVerb(), show);
			this.dbSpecificCommands.add(show.getVerb());
		}

    List<String> startTrans = Settings.getInstance().getListProperty("workbench.db." + metaData.getDbId() + ".start_transaction", false, "");
    for (String sql : startTrans)
    {
      sql = SqlUtil.makeCleanSql(sql, false, false);

      if (this.cmdDispatch.containsKey(sql))
      {
        LogMgr.logInfo(new CallerInfo(){}, "Configured command " + sql.toUpperCase() + " is already registered as a transaction start command");
      }
      else
      {
        LogMgr.logInfo(new CallerInfo(){}, "Adding " + sql.toUpperCase() + " as a transaction start command");
        TransactionStartCommand startCmd = TransactionStartCommand.fromVerb(sql);
        this.cmdDispatch.put(startCmd.getVerb(), startCmd);
        this.dbSpecificCommands.add(startCmd.getVerb());
      }
    }

		if (metaData.getDbSettings().useWbProcedureCall())
		{
			SqlCommand wbcall = this.cmdDispatch.get(WbCall.VERB);
			this.cmdDispatch.put("CALL", wbcall);
			this.dbSpecificCommands.add("CALL");
		}

		List<String> verbs = Settings.getInstance().getListProperty("workbench.db.ignore." + metaData.getDbId(), false, "");
		for (String verb : verbs)
		{
			if (verb == null) continue;
			IgnoredCommand cmd = new IgnoredCommand(verb);
			this.cmdDispatch.put(verb, cmd);
			this.dbSpecificCommands.add(verb);
		}

		List<String> passVerbs = Settings.getInstance().getListProperty("workbench.db." + metaData.getDbId() + ".passthrough", false, "");
		passThrough.clear();
		if (passVerbs != null)
		{
			for (String v : passVerbs)
			{
				passThrough.add(v);
			}
		}

		// this is stored in an instance variable for performance
		// reasons, so we can skip the call to isSelectIntoNewTable() in
		// getCommandToUse()
		// For a single call this doesn't matter, but when executing
		// huge scripts the repeated call to getCommandToUse should
		// be as quick as possible
		this.supportsSelectInto = metaData.supportsSelectIntoNewTable();
	}

	/**
   *
   * Returns the SqlCommand to be used for the given SQL string.
   *
   * This also checks for "SELECT ... INTO ... " style statments that
   * don't actually select something but create a new table.
   * As those aren't "real" queries they need to be run and handled
   * differently - for those statements SelectCommand will not be used.
   *
	 * @param sql the statement to be executed
	 * @return the instance of SqlCommand to be used to run the sql, or null if the
	 * given sql is empty or contains comments only
	 */
	public SqlCommand getCommandToUse(String sql)
	{
		SqlCommand cmd = null;

		WbConnection conn = metaData == null ? null : metaData.getWbConnection();
		String verb = SqlParsingUtil.getInstance(conn).getSqlVerb(sql);

		if (StringUtil.isEmptyString(verb)) return null;

		if (this.supportsSelectInto && "SELECT".equals(verb) && this.metaData != null && this.metaData.isSelectIntoNewTable(sql))
		{
			LogMgr.logDebug("CommandMapper.getCommandToUse()", "Found 'SELECT ... INTO new_table'");
			// use the generic SqlCommand implementation for this and not the SelectCommand
			cmd = this.cmdDispatch.get("*");
		}

		// checking for the collection size before checking for the presence
		// is a bit faster because of the hashing that is necessary to look up
		// the entry. Again this doesn't matter for a single command, but when
		// running a large script this does make a difference
		else if (passThrough.size() > 0 && passThrough.contains(verb))
		{
			cmd = this.cmdDispatch.get("*");
		}
		else
		{
			cmd = this.cmdDispatch.get(verb);
		}

		if (cmd == null && allowAbbreviated)
		{
			Set<String> verbs = cmdDispatch.keySet();
			int found = 0;
			String lastVerb = null;
			String lverb = verb.toLowerCase();
			for (String toTest : verbs)
			{
				if (cmdDispatch.get(toTest).isWbCommand())
				{
					if (toTest.toLowerCase().startsWith(lverb))
					{
						lastVerb = toTest;
						found ++;
					}
				}
			}
			if (found == 1)
			{
				LogMgr.logDebug("CommandMapper.getCommandToUse()", "Found workbench command " + lastVerb + " for abbreviation " + verb);
				cmd = cmdDispatch.get(lastVerb);
			}
		}

		if (cmd == null)
		{
			cmd = this.cmdDispatch.get("*");
		}
		return cmd;
	}

}
