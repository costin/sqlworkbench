/*
 * OracleMetadata.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2008, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db.oracle;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import workbench.db.ConnectionProfile;
import workbench.db.DataTypeResolver;
import workbench.db.DbMetadata;
import workbench.db.ErrorInformationReader;
import workbench.db.TableIdentifier;
import workbench.db.WbConnection;
import workbench.resource.Settings;
import workbench.util.ExceptionUtil;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.util.SqlUtil;
import workbench.util.StringUtil;

/**
 *
 * @author support@sql-workbench.net
 */
public class OracleMetadata
	implements ErrorInformationReader, DataTypeResolver
{
	private WbConnection connection;
	private PreparedStatement columnStatement;
	private int version;
	private boolean retrieveSnapshots = true;
	static final int BYTE_SEMANTICS = 1;
	static final int CHAR_SEMANTICS = 2;
	private int defaultLengthSemantics = -1;
	private boolean alwaysShowCharSemantics = false;
	private boolean useOwnSql = true;
	private boolean globalMapDateToTimestamp = false;
	/**
	 * Only for testing purposes
	 */
	OracleMetadata(int defaultSemantics, boolean alwaysShowSemantics)
	{
		defaultLengthSemantics = defaultSemantics;
		alwaysShowCharSemantics = alwaysShowSemantics;
	}
	
	public OracleMetadata(WbConnection conn)
	{
		this.connection = conn;
		try
		{
			this.version = this.connection.getSqlConnection().getMetaData().getDatabaseMajorVersion();
		}
		catch (Throwable th)
		{
			// The old Oracle 8 driver (classes12.jar) does not implement getDatabaseMajorVersion()
      // and throws an AbstractMethodError
			this.version = 8;
		}

		alwaysShowCharSemantics = Settings.getInstance().getBoolProperty("workbench.db.oracle.charsemantics.displayalways", true);
		
		if (!alwaysShowCharSemantics)
		{
			Statement stmt = null;
			ResultSet rs = null;
			try
			{
				stmt = this.connection.createStatement();
				String sql = "select value from v$nls_parameters where parameter = 'NLS_LENGTH_SEMANTICS'";
				rs = stmt.executeQuery(sql);
				if (rs.next())
				{
					String v = rs.getString(1);
					if ("BYTE".equalsIgnoreCase(v))
					{
						defaultLengthSemantics = BYTE_SEMANTICS;
					}
					else if ("CHAR".equalsIgnoreCase(v))
					{
						defaultLengthSemantics = CHAR_SEMANTICS;
					}
				}
			}
			catch (Exception e)
			{
				defaultLengthSemantics = BYTE_SEMANTICS;
				LogMgr.logWarning("OracleMetadata.<init>", "Could not retrieve NLS_LENGTH_SEMANTICS from v$nls_parameters. Assuming byte semantics", e);
			}
			finally
			{
				SqlUtil.closeAll(rs, stmt);
			}
		}
		
		boolean fixNVARCHAR = Settings.getInstance().useOracleNVarcharFix();
		boolean checkCharSemantics = Settings.getInstance().useOracleCharSemanticsFix();		
		
		useOwnSql = (version > 8 && (checkCharSemantics || fixNVARCHAR));
		globalMapDateToTimestamp = Settings.getInstance().getBoolProperty("workbench.db.oracle.fixdatetype", false);
	}

	public boolean isOracle8()
	{
		return this.version == 8;
	}

	private boolean getRemarksReporting()
	{
		// The old "remarksReporting" property should not be taken from the 
		// System properties as a fall-back
		String value = getDriverProperty("remarksReporting", false);
		if (value == null)
		{
			value = getDriverProperty("oracle.jdbc.remarksReporting", true);
		}
		return "true".equalsIgnoreCase(value == null ? "false" : value.trim());
	}

	public boolean getMapDateToTimestamp()
	{
		if (globalMapDateToTimestamp) return true;
		// if the mapping hasn't been enabled globally, then check the driver property
		
		// Newer Oracle drivers support a connection property to automatically 
		// return DATE columns as Types.TIMESTAMP. We have to mimic that 
		// when using our own statement to retrieve column definitions
		String value = getDriverProperty("oracle.jdbc.mapDateToTimestamp", true);
		return "true".equalsIgnoreCase(value);
	}

	private String getDriverProperty(String property, boolean includeSystemProperty)
	{
		String value = null;
		ConnectionProfile profile = this.connection.getProfile();
		if (profile != null)
		{
			Properties props = profile.getConnectionProperties();
			value = (props != null ? props.getProperty(property, null) : null);
			if (value == null && includeSystemProperty)
			{
				value = System.getProperty(property, null);
			}
		}
		return value;
	}

	public ResultSet getColumns(String catalog, String schema, String table, String cols)
		throws SQLException
	{
		// make sure the statement object is closed properly 
		columnsProcessed();
		
		if (!useOwnSql)
		{
			this.columnStatement = null;
			return this.connection.getSqlConnection().getMetaData().getColumns(catalog, schema, table, cols);
		}
		
		boolean fixNVARCHAR = Settings.getInstance().useOracleNVarcharFix();

		// Oracle 9 and above reports a wrong length if NLS_LENGTH_SEMANTICS is set to char
    // this statement fixes this problem and also removes the usage of LIKE
    // to speed up the retrieval.
		final String sql1 =
			"SELECT NULL AS table_cat,  \n" +
			"     t.owner AS table_schem,  \n" +
			"     t.table_name AS table_name,  \n" +
			"     t.column_name AS column_name,  \n   " +
			"     DECODE(t.data_type, 'CHAR', " + Types.CHAR + ", " +
			"                    'VARCHAR2', " + Types.VARCHAR + ", " +
			"                    'NVARCHAR2', " + (fixNVARCHAR ? Types.VARCHAR : Types.OTHER) + ", " +
			"                    'NCHAR', " + (fixNVARCHAR ? Types.VARCHAR : Types.OTHER) + ", " +
			"                    'NUMBER', " + Types.DECIMAL + ", " +
			"                    'LONG', " + Types.LONGVARCHAR + ", " +
			"                    'DATE', " + (getMapDateToTimestamp() ? Types.TIMESTAMP : Types.DATE) + ", " +
			"                    'RAW', " + Types.VARBINARY + ", " +
			"                    'LONG RAW', " + Types.LONGVARBINARY + ", " +
			"                    'BLOB', " + Types.BLOB + ", " +
			"                    'CLOB', " + Types.CLOB + ", " +
			"                    'NCLOB', " + (fixNVARCHAR ? Types.CLOB : Types.OTHER) + ", " +
			"                    'BFILE', -13, " +
			"                    'FLOAT', " + Types.FLOAT + ", " +
			"                    'TIMESTAMP(6)', " + Types.TIMESTAMP + ", " +
			"                    'TIMESTAMP(6) WITH TIME ZONE', -101, " +
			"                    'TIMESTAMP(6) WITH LOCAL TIME ZONE', -102, " +
			"                    'INTERVAL YEAR(2) TO MONTH', -103, " +
			"                    'INTERVAL DAY(2) TO SECOND(6)', -104, " +
			"                    'BINARY_FLOAT', 100, " +
			"                    'BINARY_DOUBLE', 101, " +
			"                    " + Types.OTHER + ") AS data_type,  \n" +
			"     t.data_type AS type_name,  \n" +
			"     DECODE(t.data_precision, null, " +
			"        decode(t.data_type, 'VARCHAR', t.char_length, " +
			"                            'VARCHAR2', t.char_length, " +
			"                            'NVARCHAR', t.char_length, " +
			"                            'NVARCHAR2', t.char_length, " +
			"                            'CHAR', t.char_length, " +
			"                            'NCHAR', t.char_length, t.data_length), " +
			"               t.data_precision) AS column_size,  \n" +
			"     0 AS buffer_length,  \n" +
			"     t.data_scale AS decimal_digits,  \n" +
			"     10 AS num_prec_radix,  \n" +
			"     DECODE (t.nullable, 'N', 0, 1) AS nullable,  \n";

		final String sql2 = 
			"     t.data_default AS column_def,  \n" +
			"     decode(t.data_type, 'VARCHAR2', " +
			"            decode(t.char_used, 'B', " + BYTE_SEMANTICS + ", 'C', " + CHAR_SEMANTICS + ", 0), " +
			"            0) AS sql_data_type,  \n " +
			"       0 AS sql_datetime_sub,  \n" +
			"       t.data_length AS char_octet_length,  \n" +
			"       t.column_id AS ordinal_position,   \n" +
			"       DECODE (t.nullable, 'N', 'NO', 'YES') AS is_nullable  \n" +
			" FROM all_tab_columns t";

		// I'm not using LIKE for the condition to select owner/table
    // because internally we never call this with wildcards and leaving out the 
		// like (which is used in the original statement from Oracle's driver)
    // speeds up the statement
		final String where = " WHERE t.owner = ? AND t.table_name = ? AND t.column_name LIKE ? ESCAPE '/'  \n";
		final String comment_join = "   AND t.owner = c.owner (+)  AND t.table_name = c.table_name (+)  AND t.column_name = c.column_name (+)  \n";
		final String order = "ORDER BY table_schem, table_name, ordinal_position";
		
		final String sql_comment = sql1 + "       c.comments AS remarks, \n" + sql2 + ", all_col_comments c  \n" + where + comment_join + order;
		final String sql_no_comment = sql1 + "       null AS remarks, \n" + sql2 + where + order;
		
		String sql = null;

		if (getRemarksReporting())
		{
			sql = sql_comment;
		}
		else
		{
			sql = sql_no_comment;
		}

		ResultSet rs = null;

		int pos = table != null ? table.indexOf('@') : -1;

		if (pos > 0)
		{
			String dblink = table.substring(pos);
			table = table.substring(0, pos);
			sql = StringUtil.replace(sql, "all_tab_columns", "all_tab_columns" + dblink);
			sql = StringUtil.replace(sql, "all_col_comments", "all_col_comments" + dblink);
			String dblinkOwner = this.getDbLinkTargetSchema(dblink.substring(1), schema);
			if (StringUtil.isEmptyString(schema) && !StringUtil.isEmptyString(dblinkOwner))
			{
				schema = dblinkOwner;
			}
		}

		this.columnStatement = this.connection.getSqlConnection().prepareStatement(sql);
		this.columnStatement.setString(1, schema);
		this.columnStatement.setString(2, table);
		this.columnStatement.setString(3, cols != null ? cols : "%");
//		if (Settings.getInstance().getDebugMetadataSql())
//		{
//			LogMgr.logDebug("OracleMetadata.getColumns()", "Using: " + sql);
//		}
		rs = this.columnStatement.executeQuery();
		return rs;
	}

	private String getDbLinkTargetSchema(String dblink, String owner)
	{
		String sql = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String linkOwner = null;

		// check if DB Link name contains a domain
		// If yes, use the link name directly
		if (dblink.indexOf('.') > 0)
		{
			sql = "SELECT username FROM all_db_links WHERE db_link = ? AND (owner = ? or owner = 'PUBLIC')";
		}
		else
		{
			// apparently Oracle stores all DB Links with the default domain
			// appended. I did not find a reliable way to retrieve the domain
			// name, so I'm using a like to retrieve the definition
			// hoping that there won't be two dblinks with the same name
			// but different domains
			sql = "SELECT username FROM all_db_links WHERE db_link like ? AND (owner = ? or owner = 'PUBLIC')";
			dblink = dblink + ".%";
		}

		try
		{
			synchronized (connection)
			{
				stmt = this.connection.getSqlConnection().prepareStatement(sql);
				stmt.setString(1, dblink);
				stmt.setString(2, owner);
				rs = stmt.executeQuery();
				if (rs.next())
				{
					linkOwner = rs.getString(1);
				}
			}
		}
		catch (Exception e)
		{
			LogMgr.logError("OracleMetadata.getDblinkSchema()", "Error retrieving target schema for DBLINK " + dblink, e);
		}
		finally
		{
			SqlUtil.closeAll(rs, stmt);
		}

		return linkOwner;
	}
	
	/**
	 *	Return the errors reported in the all_errors table for Oracle.
	 *	This method can be used to obtain error information after a CREATE PROCEDURE
	 *	or CREATE TRIGGER statement has been executed.
	 *
	 *	@return extended error information if available
	 */
	public String getErrorInfo(String schema, String objectName, String objectType)
	{
		final String ERROR_QUERY = 
			"SELECT line, position, text " + 
			" FROM all_errors   " + 
			"WHERE owner = ? " + 
			"  AND type = ? " + 
			"  AND name = ? " + 
			" ORDER BY LINE ";

		if (objectType == null || objectName == null)
		{
			return "";
		}
		PreparedStatement stmt = null;
		ResultSet rs = null;

		StringBuilder result = new StringBuilder(250);
		try
		{
			if (objectName.indexOf('.') > -1)
			{
				schema = objectName.substring(0, objectName.indexOf('.'));
			}
			else if (schema == null)
			{
				schema = this.connection.getCurrentSchema();
			}
			DbMetadata meta = this.connection.getMetadata();
			
			stmt = this.connection.getSqlConnection().prepareStatement(ERROR_QUERY);
			stmt.setString(1, meta.adjustSchemaNameCase(StringUtil.trimQuotes(schema)));
			stmt.setString(2, objectType.toUpperCase().trim());
			stmt.setString(3, meta.adjustObjectnameCase(StringUtil.trimQuotes(objectName)));

			rs = stmt.executeQuery();
			int count = 0;
			while (rs.next())
			{
				if (count > 0)
				{
					result.append("\n");
				}
				int line = rs.getInt(1);
				int pos = rs.getInt(2);
				String msg = rs.getString(3);
				result.append(ResourceMgr.getFormattedString("ErrAtLinePos", line, pos));
				result.append("\n");
				result.append(msg);
				count++;
			}
		}
		catch (SQLException e)
		{
			LogMgr.logError("OracleMetadata.getExtendedErrorInfo()", "Error retrieving error information", e);
		}
		finally
		{
			SqlUtil.closeAll(rs, stmt);
		}
		return result.toString();
	}

	/**
	 * Returns a Set with Strings identifying available Snapshots (materialized views)
	 * The names will be returned as owner.tablename
	 * In case the retrieve throws an error, this method will return
	 * an empty set in subsequent calls.
	 */
	public Set<String> getSnapshots(String schema)
	{
		if (!retrieveSnapshots)
		{
			return Collections.emptySet();
		}
		Set<String> result = new HashSet<String>();
		String sql = "SELECT owner||'.'||mview_name FROM all_mviews";
		if (schema != null)
		{
			sql += " WHERE owner = ?";
		}

		PreparedStatement stmt = null;
		ResultSet rs = null;

		try
		{
			stmt = this.connection.getSqlConnection().prepareStatement(sql);
			if (schema != null)
			{
				stmt.setString(1, schema);
			}
			rs = stmt.executeQuery();
			while (rs.next())
			{
				String name = rs.getString(1);
				result.add(name);
			}
		}
		catch (SQLException e)
		{
			LogMgr.logWarning("OracleMetadata.getSnapshots()", "Error accessing all_mviews", e);
			// When we get an exception, most probably we cannot access the ALL_MVIEWS view.
			// To avoid further (unnecessary) calls, we are disabling the support
			// for snapshots
			this.retrieveSnapshots = false;
			result = Collections.emptySet();
		}
		finally
		{
			SqlUtil.closeAll(rs, stmt);
		}
		return result;
	}

	public String getSnapshotSource(TableIdentifier tbl)
	{
		if (!retrieveSnapshots)
		{
			return StringUtil.EMPTY_STRING;
		}
		String result = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql = "SELECT query FROM all_mviews WHERE owner = ? and mview_name = ?";

		try
		{
			stmt = this.connection.getSqlConnection().prepareStatement(sql);
			stmt.setString(1, tbl.getSchema());
			stmt.setString(2, tbl.getTableName());
			rs = stmt.executeQuery();
			if (rs.next())
			{
				result = rs.getString(1);
				if (rs.wasNull())
				{
					result = "";
				}
				else
				{
					result = result.trim();
				}

				if (!result.endsWith(";"))
				{
					result = result + ";";
				}
			}
		}
		catch (SQLException e)
		{
			LogMgr.logWarning("OracleMetadata.getSnapshotSource()", "Error accessing all_mviews", e);
			this.retrieveSnapshots = false;
			result = ExceptionUtil.getDisplay(e);
		}
		finally
		{
			SqlUtil.closeAll(rs, stmt);
		}
		return result;
	}

	/**
	 * Close the statement object that was used in {@link #getColumns(String, String, String, String)}.
	 * This method should be called after closing the ResultSet obtained from that method.
	 */
	public void columnsProcessed()
	{
		SqlUtil.closeStatement(columnStatement);
		columnStatement = null;
	}

	private String getVarcharType(String type, int size, int semantics)
	{
		StringBuilder result = new StringBuilder(25);
		result.append(type);
		result.append('(');
		result.append(size);

		// Only apply this logic vor VARCHAR columns
		// NVARCHAR (which might have been reported as type == VARCHAR) does not 
		// allow Byte/Char semantics
		if (type.startsWith("VARCHAR"))
		{
			if (alwaysShowCharSemantics || semantics != defaultLengthSemantics)
			{
				if (semantics < 0) semantics = defaultLengthSemantics;
				if (semantics == BYTE_SEMANTICS)
				{
					result.append(" Byte");
				}
				else if (semantics == CHAR_SEMANTICS)
				{
					result.append(" Char");
				}
			}
		}
		result.append(')');
		return result.toString();
	}
	
	public String getSqlTypeDisplay(String dbmsName, int sqlType, int size, int digits, int byteOrChar)
	{
		String display = null;
		
		if (sqlType == Types.VARCHAR) 
		{
			// Hack to get Oracle's VARCHAR2(xx Byte) or VARCHAR2(xxx Char) display correct
			// Our own statement to retrieve column information in OracleMetaData
			// will return the byte/char semantics in the field WB_SQL_DATA_TYPE
			// Oracle's JDBC driver does not supply this information (because
			// the JDBC standard does not define a column for this)
			display = getVarcharType(dbmsName, size, byteOrChar);
		}
		else if ("NUMBER".equalsIgnoreCase(dbmsName))
		{
			if (digits < 0)
			{
				return "NUMBER";
			}
			else
			{
				return "NUMBER(" + size + "," + digits + ")";
			}
		}
		else 
		{
			display = SqlUtil.getSqlTypeDisplay(dbmsName, sqlType, size, digits);
		}
		return display;
	}
	
}
