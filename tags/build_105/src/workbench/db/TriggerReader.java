/*
 * TriggerReader.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2009, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import workbench.log.LogMgr;
import workbench.resource.Settings;
import workbench.sql.DelimiterDefinition;
import workbench.storage.DataStore;
import workbench.util.ExceptionUtil;
import workbench.util.SqlUtil;
import workbench.util.StringUtil;

/**
 * A class to read triggers from the database.
 * The reading is done by Statements configured in XML files.
 *
 * @author Thomas Kellerer
 * @see MetaDataSqlManager
 */
public class TriggerReader
{
	private WbConnection dbConnection;
	private DbMetadata dbMeta;

	public TriggerReader(WbConnection conn)
	{
		this.dbMeta = conn.getMetadata();
		this.dbConnection = conn;
	}

	/**
	 *	The column index in the DataStore returned by getTableTriggers which identifies
	 *  the name of the trigger.
	 */
	public static final int COLUMN_IDX_TABLE_TRIGGERLIST_TRG_NAME = 0;
	/**
	 *	The column index in the DataStore returned by getTableTriggers which identifies
	 *  the type (INSERT, UPDATE etc) of the trigger.
	 */
	public static final int COLUMN_IDX_TABLE_TRIGGERLIST_TRG_TYPE = 1;
	/**
	 *	The column index in the DataStore returned by getTableTriggers which identifies
	 *  the event (before, after) of the trigger.
	 */
	public static final int COLUMN_IDX_TABLE_TRIGGERLIST_TRG_EVENT = 2;

	/**
	 * Return a list of triggers available in the given schema.
	 */
	public DataStore getTriggers(String catalog, String schema)
		throws SQLException
	{
		return getTriggers(catalog, schema, null);
	}

	public List<TriggerDefinition> getTriggerList(String catalog, String schema)
		throws SQLException
	{
		DataStore triggers = getTriggers(catalog, schema);
		List<TriggerDefinition> result = new ArrayList<TriggerDefinition>(triggers.getRowCount());
		for (int row = 0; row < triggers.getRowCount(); row ++)
		{
			String trgName = triggers.getValueAsString(row, COLUMN_IDX_TABLE_TRIGGERLIST_TRG_NAME);
			String trgType = triggers.getValueAsString(row, COLUMN_IDX_TABLE_TRIGGERLIST_TRG_TYPE);
			String trgEvent = triggers.getValueAsString(row, COLUMN_IDX_TABLE_TRIGGERLIST_TRG_EVENT);

			TriggerDefinition trg = new TriggerDefinition(catalog, schema, trgName);
			trg.setTriggerType(trgType);
			trg.setTriggerEvent(trgEvent);
			result.add(trg);
		}
		return result;
	}
	/**
	 *	Return the list of defined triggers for the given table.
	 */
	public DataStore getTableTriggers(TableIdentifier table)
		throws SQLException
	{
		TableIdentifier tbl = table.createCopy();
		tbl.adjustCase(this.dbConnection);
		return getTriggers(tbl.getCatalog(), tbl.getSchema(), tbl.getTableName());
	}

	public static final String TRIGGER_NAME_COLUMN = "TRIGGER";
	public static final String TRIGGER_TYPE_COLUMN = "TYPE";
	public static final String TRIGGER_EVENT_COLUMN = "EVENT";

	public static final String[] LIST_COLUMNS = {TRIGGER_NAME_COLUMN, TRIGGER_TYPE_COLUMN, TRIGGER_EVENT_COLUMN};

	protected DataStore getTriggers(String catalog, String schema, String tableName)
		throws SQLException
	{
		final int[] types =   {Types.VARCHAR, Types.VARCHAR, Types.VARCHAR};
		final int[] sizes =   {30, 30, 20};

		DataStore result = new DataStore(LIST_COLUMNS, types, sizes);

		GetMetaDataSql sql = dbMeta.metaSqlMgr.getListTriggerSql();
		if (sql == null)
		{
			LogMgr.logWarning("TriggerReader.getTriggers()", "getTriggers() called but no SQL configured");
			return result;
		}

		sql.setSchema(schema);
		sql.setCatalog(catalog);
		sql.setObjectName(tableName);

		Statement stmt = this.dbConnection.createStatementForQuery();
		String query = sql.getSql();

		if (Settings.getInstance().getDebugMetadataSql())
		{
			LogMgr.logInfo("DbMetadata.getTableTriggers()", "Using query=\n" + query);
		}
		ResultSet rs = stmt.executeQuery(query);
		try
		{
			while (rs.next())
			{
				int row = result.addRow();
				String value = rs.getString(1);
				if (!rs.wasNull() && value != null) value = value.trim();
				result.setValue(row, COLUMN_IDX_TABLE_TRIGGERLIST_TRG_NAME, value);

				value = rs.getString(2);
				result.setValue(row, COLUMN_IDX_TABLE_TRIGGERLIST_TRG_TYPE, value);

				value = rs.getString(3);
				result.setValue(row, COLUMN_IDX_TABLE_TRIGGERLIST_TRG_EVENT, value);
			}
			result.resetStatus();
		}
		finally
		{
			SqlUtil.closeAll(rs, stmt);
		}
		return result;
	}

	/**
	 * Retrieve the SQL Source of the given trigger.
	 *
	 * @param aCatalog The catalog in which the trigger is defined. This should be null if the DBMS does not support catalogs
	 * @param aSchema The schema in which the trigger is defined. This should be null if the DBMS does not support schemas
	 * @param aTriggername
	 * @throws SQLException
	 * @return the trigger source
	 */
	public String getTriggerSource(String aCatalog, String aSchema, String aTriggername)
		throws SQLException
	{
		StringBuilder result = new StringBuilder(500);

		if ("*".equals(aCatalog)) aCatalog = null;
		if ("*".equals(aSchema)) aSchema = null;

		GetMetaDataSql sql = dbMeta.metaSqlMgr.getTriggerSourceSql();
		if (sql == null) return StringUtil.EMPTY_STRING;

		sql.setSchema(aSchema);
		sql.setCatalog(aCatalog);
		sql.setObjectName(aTriggername);
		Statement stmt = this.dbConnection.createStatementForQuery();
		String query = sql.getSql();

		if (Settings.getInstance().getDebugMetadataSql())
		{
			LogMgr.logInfo("DbMetadata.getTriggerSource()", "Using query=\n" + query);
		}

		String nl = Settings.getInstance().getInternalEditorLineEnding();

		ResultSet rs = null;
		try
		{
			// for some DBMS (e.g. SQL Server)
			// we need to run a exec which might not work
			// when using executeQuery() (depending on the JDBC driver)
			stmt.execute(query);
			rs = stmt.getResultSet();

			if (rs != null)
			{
				int colCount = rs.getMetaData().getColumnCount();
				while (rs.next())
				{
					for (int i=1; i <= colCount; i++)
					{
						result.append(rs.getString(i));
					}
				}
			}
			CharSequence warn = SqlUtil.getWarnings(this.dbConnection, stmt);
			if (warn != null)
			{
				if (result.length() > 0) result.append(nl + nl);
				result.append(warn);
			}

			DelimiterDefinition delim = Settings.getInstance().getAlternateDelimiter(dbConnection);
			if (result.length() > 0 && delim != null && !delim.isStandard())
			{
				result.append(nl);
				result.append(delim.getDelimiter());
			}
		}
		catch (SQLException e)
		{
			LogMgr.logError("DbMetadata.getTriggerSource()", "Error reading trigger source", e);
			if (this.dbMeta.isPostgres()) try { this.dbConnection.rollback(); } catch (Throwable th) {}
			result.append(ExceptionUtil.getDisplay(e));
			SqlUtil.closeAll(rs, stmt);
			return result.toString();
		}
		finally
		{
			SqlUtil.closeAll(rs, stmt);
		}

		boolean replaceNL = Settings.getInstance().getBoolProperty("workbench.db." + dbMeta.getDbId() + ".replacenl.triggersource", false);

		String source = result.toString();
		if (replaceNL && source.length() > 0)
		{
			source = StringUtil.replace(source, "\\n", nl);
		}
		return source;
	}

}