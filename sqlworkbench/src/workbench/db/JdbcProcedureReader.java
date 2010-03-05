/*
 * JdbcProcedureReader.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2010, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import workbench.log.LogMgr;
import workbench.resource.Settings;
import workbench.sql.DelimiterDefinition;
import workbench.storage.DataStore;
import workbench.util.ExceptionUtil;
import workbench.util.NumberStringCache;
import workbench.util.SqlUtil;
import workbench.util.StringUtil;

/**
 * Retrieve information about stored procedures from the database.
 * To retrieve the source of the Stored procedure, SQL statements need
 * to be defined in the ProcSourceStatements.xml
 *
 * @see workbench.db.MetaDataSqlManager
 *
 * @author  Thomas Kellerer
 */
public class JdbcProcedureReader
	implements ProcedureReader
{
	final protected WbConnection connection;
	protected boolean useSavepoint = false;

	public JdbcProcedureReader(WbConnection conn)
	{
		this.connection = conn;
	}

	public StringBuilder getProcedureHeader(String catalog, String schema, String procName, int procType)
	{
		return StringUtil.emptyBuffer();
	}


	/**
	 * Checks if the given procedure exists in the database
	 */
	public boolean procedureExists(ProcedureDefinition def)
	{
		String catalog = def.getCatalog();
		String schema = def.getSchema();
		String procName = def.getProcedureName();
		int procType = def.getResultType();

		boolean exists = false;

		Savepoint sp = null;
		try
		{
			DataStore ds = getProcedures(catalog, schema, procName);
			
			if (ds.getRowCount() > 0)
			{
				int type = ds.getValueAsInt(0, ProcedureReader.COLUMN_IDX_PROC_LIST_TYPE, DatabaseMetaData.procedureResultUnknown);

				if (type == DatabaseMetaData.procedureResultUnknown ||
					  procType == DatabaseMetaData.procedureResultUnknown ||
						type == procType)
				{
					exists = true;
				}
			}
		}
		catch (Exception e)
		{
			this.connection.rollback(sp);
			LogMgr.logError("JdbcProcedureReader.procedureExists()", "Error checking procedure", e);
		}
		return exists;
	}

	public DataStore getProcedures(String catalog, String schema, String name)
		throws SQLException
	{
		if ("*".equals(catalog) || StringUtil.isBlank(catalog))
		{
			catalog = null;
		}

		if ("*".equals(schema) || "%".equals(schema) || StringUtil.isBlank(schema))
		{
			schema = null;
		}

		if (StringUtil.isBlank(name))
		{
			name = "%";
		}
		
		Savepoint sp = null;
		try
		{
			if (useSavepoint)
			{
				sp = this.connection.setSavepoint();
			}
			ResultSet rs = this.connection.getSqlConnection().getMetaData().getProcedures(catalog, schema, name);
			DataStore ds = fillProcedureListDataStore(rs);
			this.connection.releaseSavepoint(sp);
			ds.resetStatus();
			return ds;
		}
		catch (SQLException sql)
		{
			this.connection.rollback(sp);
			throw sql;
		}
	}

	public DataStore buildProcedureListDataStore(DbMetadata meta, boolean addSpecificName)
	{
		String[] cols  = null;
		int[] types = null;
		int[] sizes = null;

		if (addSpecificName)
		{
			cols = new String[] {"PROCEDURE_NAME", "TYPE", meta.getCatalogTerm().toUpperCase(), meta.getSchemaTerm().toUpperCase(), "REMARKS", "SPECIFIC_NAME"};
			types = new int[] {Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR};
			sizes = new int[] {30,12,10,10,20,50};
		}
		else
		{
			cols = new String[] {"PROCEDURE_NAME", "TYPE", meta.getCatalogTerm().toUpperCase(), meta.getSchemaTerm().toUpperCase(), "REMARKS"};
			types = new int[] {Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR};
			sizes = new int[] {30,12,10,10,20};
		}

		DataStore ds = new DataStore(cols, types, sizes);
		return ds;
	}

	public DataStore fillProcedureListDataStore(ResultSet rs)
		throws SQLException
	{

		int specIndex = JdbcUtils.getColumnIndex(rs, "SPECIFIC_NAME");
		boolean useSpecificName = specIndex > -1;

		DataStore ds = buildProcedureListDataStore(this.connection.getMetadata(), useSpecificName);

		try
		{
			while (rs.next())
			{
				String cat = rs.getString("PROCEDURE_CAT");
				String schema = rs.getString("PROCEDURE_SCHEM");
				String name = rs.getString("PROCEDURE_NAME");
				String remark = rs.getString("REMARKS");
				int type = rs.getInt("PROCEDURE_TYPE");
				Integer iType;
				if (rs.wasNull())
				{
					//sType = "N/A";
					iType = Integer.valueOf(DatabaseMetaData.procedureResultUnknown);
				}
				else
				{
					iType = Integer.valueOf(type);
				}
				int row = ds.addRow();
				if (useSpecificName)
				{
					String specname = rs.getString("SPECIFIC_NAME");
					ds.setValue(row, ProcedureReader.COLUMN_IDX_PROC_LIST_SPECIFIC_NAME, specname);
				}

				ds.setValue(row, ProcedureReader.COLUMN_IDX_PROC_LIST_CATALOG, cat);
				ds.setValue(row, ProcedureReader.COLUMN_IDX_PROC_LIST_SCHEMA, schema);
				ds.setValue(row, ProcedureReader.COLUMN_IDX_PROC_LIST_NAME, stripVersionInfo(name));
				ds.setValue(row, ProcedureReader.COLUMN_IDX_PROC_LIST_TYPE, iType);
				ds.setValue(row, ProcedureReader.COLUMN_IDX_PROC_LIST_REMARKS, remark);
			}
			ds.resetStatus();
		}
		catch (Exception e)
		{
			LogMgr.logError("JdbcProcedureReader.getProcedures()", "Error while retrieving procedures", e);
		}
		finally
		{
			SqlUtil.closeResult(rs);
		}
		return ds;
	}

	public static String convertProcType(int type)
	{
		if (type == DatabaseMetaData.procedureNoResult)
			return ProcedureReader.PROC_RESULT_NO;
		else if (type == DatabaseMetaData.procedureReturnsResult)
			return ProcedureReader.PROC_RESULT_YES;
		else
			return ProcedureReader.PROC_RESULT_UNKNOWN;
	}

	protected DataStore createProcColsDataStore()
	{
		final String[] cols = {"COLUMN_NAME", "TYPE", "TYPE_NAME", TableColumnsDatastore.JAVA_SQL_TYPE_COL_NAME, "REMARKS"};
		final int[] types =   {Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER, Types.VARCHAR};
		final int[] sizes =   {20, 10, 18, 5, 30};
		DataStore ds = new DataStore(cols, types, sizes);
		return ds;
	}

	protected String stripVersionInfo(String procname)
	{
		DbSettings dbS = this.connection.getMetadata().getDbSettings();
		String versionDelimiter = dbS.getProcVersionDelimiter();
		if (StringUtil.isEmptyString(versionDelimiter)) return procname;
		int pos = procname.lastIndexOf(versionDelimiter);
		if (pos < 0) return procname;
		return procname.substring(0,pos);
	}

	public DataStore getProcedureColumns(String aCatalog, String aSchema, String aProcname)
		throws SQLException
	{
		DataStore ds = createProcColsDataStore();
		ResultSet rs = null;
		Savepoint sp = null;
		try
		{
			if (useSavepoint)
			{
				sp = this.connection.setSavepoint();
			}
			rs = this.connection.getSqlConnection().getMetaData().getProcedureColumns(aCatalog, aSchema, aProcname, "%");
			while (rs.next())
			{
				int row = ds.addRow();
				String colName = rs.getString("COLUMN_NAME");
				ds.setValue(row, ProcedureReader.COLUMN_IDX_PROC_COLUMNS_COL_NAME, colName);
				int colType = rs.getInt("COLUMN_TYPE");
				String stype;

				switch (colType)
				{
					case DatabaseMetaData.procedureColumnUnknown:
						stype = "UNKNOWN";
						break;
					case DatabaseMetaData.procedureColumnInOut:
						stype = "INOUT";
						break;
					case DatabaseMetaData.procedureColumnIn:
						stype = "IN";
						break;
					case DatabaseMetaData.procedureColumnOut:
						stype = "OUT";
						break;
					case DatabaseMetaData.procedureColumnResult:
						stype = "RESULTSET";
						break;
					case DatabaseMetaData.procedureColumnReturn:
						stype = "RETURN";
						break;
					default:
						stype = NumberStringCache.getNumberString(colType);
				}
				ds.setValue(row, ProcedureReader.COLUMN_IDX_PROC_COLUMNS_RESULT_TYPE, stype);

				int sqlType = rs.getInt("DATA_TYPE");
				String typeName = rs.getString("TYPE_NAME");
				int digits = rs.getInt("PRECISION");
				int size = rs.getInt("LENGTH");
				String rem = rs.getString("REMARKS");

				String display = SqlUtil.getSqlTypeDisplay(typeName, sqlType, size, digits);
				ds.setValue(row, ProcedureReader.COLUMN_IDX_PROC_COLUMNS_DATA_TYPE, display);
				ds.setValue(row, ProcedureReader.COLUMN_IDX_PROC_COLUMNS_JDBC_DATA_TYPE, sqlType);
				ds.setValue(row, ProcedureReader.COLUMN_IDX_PROC_COLUMNS_REMARKS, rem);
			}
			this.connection.releaseSavepoint(sp);
		}
		catch (SQLException sql)
		{
			this.connection.rollback(sp);
			throw sql;
		}
		finally
		{
			SqlUtil.closeResult(rs);
		}

		return ds;
	}

	public void readProcedureSource(ProcedureDefinition def)
		throws NoConfigException
	{
		if (def == null) return;

		GetMetaDataSql sql = this.connection.getMetadata().metaSqlMgr.getProcedureSourceSql();
		if (sql == null)
		{
			throw new NoConfigException("No sql configured to retrieve procedure source");
		}

		String procName = stripVersionInfo(def.getProcedureName());

		StringBuilder source = new StringBuilder(500);

		StringBuilder header = getProcedureHeader(def.getCatalog(), def.getSchema(), procName, def.getResultType());
		source.append(header);

		Statement stmt = null;
		ResultSet rs = null;
		Savepoint sp = null;

		try
		{
			if (useSavepoint)
			{
				sp = this.connection.setSavepoint();
			}
			sql.setSchema(def.getSchema());
			sql.setObjectName(procName);
			sql.setCatalog(def.getCatalog());
			if (Settings.getInstance().getDebugMetadataSql())
			{
				LogMgr.logInfo("DbMetadata.getProcedureSource()", "Using query=\n" + sql.getSql());
			}

			stmt = this.connection.createStatementForQuery();
			rs = stmt.executeQuery(sql.getSql());
			while (rs.next())
			{
				String line = rs.getString(1);
				if (line != null)
				{
					source.append(line);
				}
			}
			this.connection.releaseSavepoint(sp);
		}
		catch (SQLException e)
		{
			if (sp != null) this.connection.rollback(sp);
			LogMgr.logError("JdbcProcedureReader.getProcedureSource()", "Error retrieving procedure source", e);
			source = new StringBuilder(ExceptionUtil.getDisplay(e));
			this.connection.rollback(sp);
		}
		finally
		{
			SqlUtil.closeAll(rs, stmt);
		}

		boolean needsTerminator = this.connection.getDbSettings().proceduresNeedTerminator();
		DelimiterDefinition delimiter = Settings.getInstance().getAlternateDelimiter(connection);
		if (!StringUtil.endsWith(source, delimiter.getDelimiter()) && needsTerminator)
		{
			if (delimiter.isSingleLine()) source.append('\n');
			source.append(delimiter.getDelimiter());
			if (delimiter.isSingleLine()) source.append('\n');
		}

		String comment = def.getComment();
		if (StringUtil.isNonBlank(comment))
		{
			CommentSqlManager mgr = new CommentSqlManager(connection.getDbSettings().getDbId());
			String template = mgr.getCommentSqlTemplate(def.getObjectType());
			if (StringUtil.isNonBlank(template))
			{
				template = template.replace(CommentSqlManager.COMMENT_OBJECT_NAME_PLACEHOLDER, def.getProcedureName());
				template = template.replace(CommentSqlManager.COMMENT_SCHEMA_PLACEHOLDER, def.getSchema());
				template = template.replace(CommentSqlManager.COMMENT_PLACEHOLDER, comment.replace("'", "''"));
				source.append('\n');
				source.append(template);
				source.append('\n');
				if (!template.endsWith(";"))
				{
					source.append(delimiter.getDelimiter());
					if (delimiter.isSingleLine()) source.append('\n');
				}
			}
		}

		String result = source.toString();

		String dbId = this.connection.getMetadata().getDbId();
		boolean replaceNL = Settings.getInstance().getBoolProperty("workbench.db." + dbId + ".replacenl.proceduresource", false);

		if (replaceNL)
		{
			String nl = Settings.getInstance().getInternalEditorLineEnding();
			result = StringUtil.replace(source.toString(), "\\n", nl);
		}

		def.setSource(result);
	}

	/**
	 * Return a List of {@link workbench.db.ProcedureDefinition} objects
	 * for Oracle packages only one ProcedureDefinition per package is returned (although
	 * the DbExplorer will list each function of the packages).
	 */
	public List<ProcedureDefinition> getProcedureList(String aCatalog, String aSchema, String name)
		throws SQLException
	{
		List<ProcedureDefinition> result = new LinkedList<ProcedureDefinition>();
		DataStore procs = getProcedures(aCatalog, aSchema, name);
		if (procs == null || procs.getRowCount() == 0) return result;
		procs.sortByColumn(ProcedureReader.COLUMN_IDX_PROC_LIST_NAME, true);
		int count = procs.getRowCount();
		Set<String> oraPackages = new HashSet<String>();

		for (int i = 0; i < count; i++)
		{
			String schema  = procs.getValueAsString(i, ProcedureReader.COLUMN_IDX_PROC_LIST_SCHEMA);
			String cat = procs.getValueAsString(i, ProcedureReader.COLUMN_IDX_PROC_LIST_CATALOG);
			String procName = procs.getValueAsString(i, ProcedureReader.COLUMN_IDX_PROC_LIST_NAME);
			int type = procs.getValueAsInt(i, ProcedureReader.COLUMN_IDX_PROC_LIST_TYPE, DatabaseMetaData.procedureNoResult);
			ProcedureDefinition def = null;
			if (this.connection.getMetadata().isOracle() && cat != null)
			{
				// The package name for Oracle is reported in the catalog column.
				// each function/procedure of the package is listed separately,
				// but we only want to create one ProcedureDefinition for the whole package
				if (!oraPackages.contains(cat))
				{
					def = ProcedureDefinition.createOraclePackage(schema, cat);
					oraPackages.add(cat);
				}
			}
			else
			{
				def = new ProcedureDefinition(cat, schema, procName, type);
			}
			if (def != null) result.add(def);
		}
		return result;
	}

}
