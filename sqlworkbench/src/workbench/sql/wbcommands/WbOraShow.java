/*
 * WbOraShow.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2012, Thomas Kellerer
 * No part of this code may be reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 */
package workbench.sql.wbcommands;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import workbench.db.oracle.OracleErrorInformationReader;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.sql.SqlCommand;
import workbench.sql.StatementRunnerResult;
import workbench.sql.formatter.SQLLexer;
import workbench.sql.formatter.SQLToken;
import workbench.storage.DataStore;
import workbench.util.CaseInsensitiveComparator;
import workbench.util.CollectionUtil;
import workbench.util.SqlUtil;

/**
 * An implementation of various SQL*Plus "show" commands.
 *
 * Currently supported commands:
 * <ul>
 *    <li>parameters</li>
 *    <li>user</li>
 *    <li>errors</li>
 *    <li>sga</li>
 *    <li>recyclebin</li>
 *    <li>autocommit</li>
 * </ul>
 * @author Thomas Kellerer
 */
public class WbOraShow
	extends SqlCommand
{
	public static final String VERB = "SHOW";

	private final long ONE_KB = 1024;
	private final long ONE_MB = ONE_KB * 1024;

	private final Set<String> types = CollectionUtil.caseInsensitiveSet(
		"FUNCTION", "PROCEDURE", "PACKAGE", "PACKAGE BODY", "TRIGGER", "VIEW", "TYPE", "TYPE BODY", "DIMENSION",
		"JAVA SOURCE", "JAVA CLASS");

	private Map<String, String> propertyUnits = new TreeMap<String, String>(CaseInsensitiveComparator.INSTANCE);

	public WbOraShow()
	{
		propertyUnits.put("result_cache_max_size", "kb");
		propertyUnits.put("sga_max_size", "mb");
		propertyUnits.put("sga_target", "mb");
		propertyUnits.put("memory_max_target", "mb");
		propertyUnits.put("memory_target", "mb");
		propertyUnits.put("db_recovery_file_dest_size", "mb");
		propertyUnits.put("db_recycle_cache_size", "mb");
	}


	@Override
	public StatementRunnerResult execute(String sql)
		throws SQLException, Exception
	{
		StatementRunnerResult result = new StatementRunnerResult(sql);

		String clean = getCommandLine(sql);
		SQLLexer lexer = new SQLLexer(clean);
		SQLToken token = lexer.getNextToken(false, false);
		if (token == null)
		{
			result.addMessage(ResourceMgr.getString("ErrOraShow"));
			result.setFailure();
			return result;
		}
		String verb = token.getText().toLowerCase();
		if (verb.startsWith("parameter"))
		{
			SQLToken name = lexer.getNextToken(false, false);
			String parm = null;
			if (name != null)
			{
				parm = name.getContents();
			}
			return getParameterValues(parm);
		}
		else if (verb.equals("sga"))
		{
			return getSGAInfo(true);
		}
		else if (verb.equals("sgainfo"))
		{
			return getSGAInfo(false);
		}
		else if (verb.equals("logsource"))
		{
			return getLogSource();
		}
		else if (verb.equals("recyclebin"))
		{
			return showRecycleBin();
		}
		else if (verb.equals("user"))
		{
			result.addMessage("USER is " + currentConnection.getCurrentUser());
		}
		else if (verb.equals("appinfo"))
		{
			return getAppInfo(sql);
		}
		else if (verb.equals("autocommit"))
		{
			if (currentConnection.getAutoCommit())
			{
				result.addMessage("autocommit ON");
			}
			else
			{
				result.addMessage("autocommit OFF");
			}
		}
		else if (verb.startsWith("error"))
		{
			return getErrors(lexer, sql);
		}
		else
		{
			result.addMessage(ResourceMgr.getString("ErrOraShow"));
			result.setFailure();
		}
		return result;
	}

	private StatementRunnerResult showRecycleBin()
	{
		StatementRunnerResult result = new StatementRunnerResult("SHOW RECYCLEBIN");
		String sql =
				"SELECT original_name as \"ORIGINAL NAME\", \n" +
				"       object_name as \"RECYCLEBIN NAME\", \n" +
				"       type as \"OBJECT TYPE\", \n" +
				"       droptime as \"DROP TIME\" \n" +
				"FROM user_recyclebin \n" +
				"WHERE can_undrop = 'YES' \n" +
				"ORDER BY original_name, \n" +
				"         droptime desc, \n" +
				"         object_name";

		ResultSet rs = null;

		Statement stmt = null;
		try
		{
			stmt = this.currentConnection.createStatementForQuery();
			rs = stmt.executeQuery(sql);
			processResults(result, true, rs);
			if (result.hasDataStores() && result.getDataStores().get(0).getRowCount() == 0)
			{
				result.clear();
			}
			result.setSuccess();
		}
		catch (SQLException ex)
		{
			result.setFailure();
			result.addMessage(ex.getMessage());
		}
		finally
		{
			SqlUtil.closeAll(rs, stmt);
		}
		return result;
	}

	private StatementRunnerResult getErrors(SQLLexer lexer, String sql)
	{
		StatementRunnerResult result = new StatementRunnerResult(sql);

		SQLToken token = lexer.getNextToken(false, false);

		String schema = null;
		String object = null;
		String type = null;

		if (token != null && types.contains(token.getText()))
		{
			type = token.getContents();
			token = lexer.getNextToken(false, false);
		}

		if (token != null)
		{
			String v = token.getText();
			int pos = v.indexOf('.');

			if (pos > 0)
			{
				schema = v.substring(0, pos - 1);
				object = v.substring(pos);
			}
			else
			{
				object = v;
			}
		}

		OracleErrorInformationReader reader = new OracleErrorInformationReader(currentConnection);
		String errors = reader.getErrorInfo(schema, object, type, true);
		if (errors.length() > 0)
		{
			result.addMessage(errors);
		}
		else
		{
			result.addMessage(ResourceMgr.getString("TxtOraNoErr"));
		}
		return result;
	}

	private StatementRunnerResult getAppInfo(String sql)
	{
		String query = "SELECT module FROM v$session WHERE audsid = USERENV('SESSIONID')";
		Statement stmt = null;
		ResultSet rs = null;
		StatementRunnerResult result = new StatementRunnerResult(sql);

		try
		{
			stmt = this.currentConnection.createStatementForQuery();
			rs = stmt.executeQuery(query);
			if (rs.next())
			{
				String appInfo = rs.getString(1);
				if (appInfo == null)
				{
					result.addMessage("appinfo is OFF");
				}
				else
				{
					result.addMessage("appinfo is \"" + appInfo + "\"");
				}
			}
		}
		catch (SQLException ex)
		{
			result.setFailure();
			result.addMessage(ex.getMessage());
		}
		finally
		{
			SqlUtil.closeAll(rs, stmt);
		}
		return result;
	}

	private StatementRunnerResult getParameterValues(String parameter)
	{
		String query =
			"select name,  \n" +
			"       case type \n" +
			"         when 1 then 'boolean'  \n" +
			"         when 2 then 'string' \n" +
			"         when 3 then 'integer' \n" +
			"         when 4 then 'parameter file' \n" +
			"         when 5 then 'reserved' \n" +
			"         when 6 then 'big integer' \n" +
			"         else to_char(type) \n" +
			"       end as type,  \n" +
			"       value, \n" +
			"       description, \n"  +
			"       update_comment \n" +
			"from v$parameter\n ";
		ResultSet rs = null;

		if (parameter != null)
		{
			query += "where name like lower('%" + parameter + "%')\n ";
		}
		query += "order by name";
		StatementRunnerResult result = new StatementRunnerResult(query);

		if (Settings.getInstance().getDebugMetadataSql())
		{
			LogMgr.logDebug("WbOraShow.getParameterValues()", "Using SQL: " + query);
		}

		Statement stmt = null;
		try
		{
			stmt = this.currentConnection.createStatementForQuery();
			rs = stmt.executeQuery(query);
			processResults(result, true, rs);
			if (result.hasDataStores())
			{
				DataStore ds = result.getDataStores().get(0);
				for (int row=0; row < ds.getRowCount(); row++)
				{
					String property = ds.getValueAsString(row, 0);
					String value = ds.getValueAsString(row, 2);
					String formatted = formatMemorySize(property, value);
					if (formatted != null)
					{
						ds.setValue(row, 2, formatted);
					}
				}
				ds.resetStatus();
			}
		}
		catch (SQLException ex)
		{
			result.setFailure();
			result.addMessage(ex.getMessage());
		}
		finally
		{
			SqlUtil.closeAll(rs, stmt);
		}
		return result;
	}

	@Override
	public String getVerb()
	{
		return VERB;
	}

	protected String formatMemorySize(String property, String value)
	{
		String unit = propertyUnits.get(property);
		if (unit == null) return null;
		try
		{
			long lvalue = Long.valueOf(value);
			if (lvalue == 0) return null;

			if ("kb".equals(unit))
			{
				return Long.toString(roundToKb(lvalue)) + "K";
			}
			if ("mb".equals(unit))
			{
				return Long.toString(roundToMb(lvalue)) + "M";
			}
		}
		catch (NumberFormatException nfe)
		{
		}
		return null;
	}

	protected StatementRunnerResult getLogSource()
	{
		StatementRunnerResult result = new StatementRunnerResult();

		String sql =
			"select destination \n" +
			"from V$ARCHIVE_DEST \n "+
			"where status = 'VALID'";

		Statement stmt = null;
		ResultSet rs = null;

		if (Settings.getInstance().getDebugMetadataSql())
		{
			LogMgr.logDebug("WbOraShow.getLogSource()", "Using SQL: " + sql);
		}

		try
		{
			stmt = this.currentConnection.createStatementForQuery();
			rs = stmt.executeQuery(sql);
			DataStore ds = new DataStore(new String[] {"LOGSOURCE", "VALUE"}, new int[] {Types.VARCHAR, Types.VARCHAR});
			while (rs.next())
			{
				String dest = rs.getString(1);
				if ("USE_DB_RECOVERY_FILE_DEST".equals(dest))
				{
					dest = "";
				}
				int row = ds.addRow();
				ds.setValue(row, 0, "LOGSOURCE");
				ds.setValue(row, 1, dest);
			}
			ds.setGeneratingSql("show logsource");
			ds.setResultName("LOGSOURCE");
			ds.resetStatus();
			result.addDataStore(ds);
			result.setSuccess();
		}
		catch (SQLException ex)
		{
			LogMgr.logError("WbOraShow.getSGAInfo()", "Could not retrieve SGA info", ex);
			result.setFailure();
			result.addMessage(ex.getMessage());
		}
		finally
		{
			SqlUtil.closeAll(rs, stmt);
		}
		return result;
	}

	protected StatementRunnerResult getSGAInfo(boolean sqlPlusMode)
	{
		StatementRunnerResult result = new StatementRunnerResult();

		String sql = null;
		if (sqlPlusMode)
		{
			sql =
				"select 'Total System Global Area' as \"Memory\", \n" +
				"       sum(VALUE) as \"Value\", \n" +
				"       'bytes' as unit \n" +
				"from V$SGA \n" +
				"union all \n" +
				"select NAME, \n" +
				"       VALUE, \n" +
				"       'bytes' \n" +
				"from V$SGA";
		}
		else
		{
			sql = "select * from v$sgainfo";
		}

		Statement stmt = null;
		ResultSet rs = null;

		if (Settings.getInstance().getDebugMetadataSql())
		{
			LogMgr.logDebug("WbOraShow.getSGAInfo()", "Using SQL: " + sql);
		}

		try
		{
			stmt = this.currentConnection.createStatementForQuery();
			rs = stmt.executeQuery(sql);
			DataStore ds = new DataStore(rs, true);
			ds.setGeneratingSql(sqlPlusMode ? "show sga" : "show sgainfo");
			ds.setResultName("SGA Size");
			ds.resetStatus();
			result.addDataStore(ds);
			result.setSuccess();
		}
		catch (SQLException ex)
		{
			LogMgr.logError("WbOraShow.getSGAInfo()", "Could not retrieve SGA info", ex);
			result.setFailure();
			result.addMessage(ex.getMessage());
		}
		finally
		{
			SqlUtil.closeAll(rs, stmt);
		}
		return result;
	}

	private long roundToKb(long input)
	{
		if (input < ONE_KB) return input;
		return input / ONE_KB;
	}

	private long roundToMb(long input)
	{
		if (input < ONE_MB) return input;
		return input / ONE_MB;
	}

}
