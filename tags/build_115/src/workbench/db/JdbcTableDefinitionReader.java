/*
 * JdbcTableDefinitionReader.java
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
package workbench.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import workbench.log.LogMgr;
import workbench.resource.Settings;

import workbench.db.derby.DerbyColumnEnhancer;
import workbench.db.firebird.FirebirdColumnEnhancer;
import workbench.db.h2database.H2ColumnEnhancer;
import workbench.db.hsqldb.HsqlColumnEnhancer;
import workbench.db.ibm.Db2ColumnEnhancer;
import workbench.db.mssql.SqlServerColumnEnhancer;
import workbench.db.mssql.SqlServerUtil;
import workbench.db.mysql.MySQLColumnEnhancer;
import workbench.db.nuodb.NuoDbColumnEnhancer;
import workbench.db.postgres.PostgresColumnEnhancer;

import workbench.util.CollectionUtil;
import workbench.util.SqlUtil;
import workbench.util.StringUtil;

/**
 *
 * @author Thomas Kellerer
 */
public class JdbcTableDefinitionReader
	implements TableDefinitionReader
{
	protected final WbConnection dbConnection;

	public JdbcTableDefinitionReader(WbConnection conn)
	{
		dbConnection = conn;
	}

	/**
	 * Return the definition of the given table.
	 * <br/>
	 * To display the columns for a table in a DataStore create an
	 * instance of {@link TableColumnsDatastore}.
	 *
	 * @param table The table for which the definition should be retrieved
	 * @param primaryKeyColumns the primary keys of <tt>table</tt>, may not be null
	 * @param dbConnection the connection to be used
	 * @param typeResolver the DataTypeResolver to be used. If null, it will be taken from the connection
	 *
	 * @throws SQLException
	 * @return the definition of the table.
	 * @see TableColumnsDatastore
	 */
	@Override
	public List<ColumnIdentifier> getTableColumns(TableIdentifier table, DataTypeResolver typeResolver)
		throws SQLException
	{
		DbSettings dbSettings = dbConnection.getDbSettings();
		DbMetadata dbmeta = dbConnection.getMetadata();

		String tablename = SqlUtil.removeObjectQuotes(table.getTableName());
		String schema = SqlUtil.removeObjectQuotes(table.getSchema());
		String catalog = SqlUtil.removeObjectQuotes(table.getCatalog());

		if (dbConnection.getDbSettings().supportsMetaDataWildcards())
		{
			tablename = SqlUtil.escapeUnderscore(tablename, dbConnection);
			schema = SqlUtil.escapeUnderscore(schema, dbConnection);
			catalog = SqlUtil.escapeUnderscore(catalog, dbConnection);
		}

		ResultSet rs = null;
		List<ColumnIdentifier> columns = new ArrayList<ColumnIdentifier>();

		PkDefinition primaryKey = table.getPrimaryKey();
		Set<String> primaryKeyColumns = CollectionUtil.caseInsensitiveSet();

		if (primaryKey != null)
		{
			primaryKeyColumns.addAll(primaryKey.getColumns());
		}

		try
		{
			rs = dbmeta.getJdbcMetaData().getColumns(catalog, schema, tablename, "%");

			ResultSetMetaData rsmeta = rs.getMetaData();

			if (Settings.getInstance().getDebugMetadataSql())
			{
				String fqn = SqlUtil.fullyQualifiedName(dbConnection, table);
				SqlUtil.dumpResultSetInfo("DatabaseMetaData.getColumns() for " + fqn, rsmeta);
			}

			boolean jdbc4 = false;

			if (rsmeta.getColumnCount() > 22)
			{
				String name = rsmeta.getColumnName(23);

				// HSQLDB 1.8 returns 23 columns, but is not JDBC4, so I need to check for the name as well.
				jdbc4 = name.equals("IS_AUTOINCREMENT");
			}

			// apparently some drivers (e.g. for DB2) do not return column names
			// so I can only access the information by column index, not by name!
			while (rs != null && rs.next())
			{
				String colName = StringUtil.trim(rs.getString(4));
				int sqlType = rs.getInt(5);  // "COLUMN_NAME"
				String typeName = rs.getString(6); // "TYPE_NAME"

				sqlType = typeResolver.fixColumnType(sqlType, typeName);
				ColumnIdentifier col = new ColumnIdentifier(dbmeta.quoteObjectname(colName), sqlType);

				int size = rs.getInt(7); // "COLUMN_SIZE"
				int digits = -1;
				try
				{
					digits = rs.getInt(9); // "DECIMAL_DIGITS"
				}
				catch (Exception e)
				{
					digits = -1;
				}
				if (rs.wasNull()) digits = -1;

				String remarks = rs.getString(12); // "REMARKS"
				String defaultValue = rs.getString(13); // "COLUMN_DEF"
				if (defaultValue != null && dbSettings.trimDefaults())
				{
					defaultValue = defaultValue.trim();
				}

				int position = -1;
				try
				{
					position = rs.getInt(17); // "ORDINAL_POSITION"
				}
				catch (SQLException e)
				{
					LogMgr.logWarning("DbMetadata", "JDBC driver does not suport ORDINAL_POSITION column for getColumns()", e);
					position = -1;
				}

				String nullable = rs.getString(18); // "IS_NULLABLE"
				String increment = jdbc4 ? rs.getString(23) : "NO"; // "IS_AUTOINCREMENT"
				boolean autoincrement = StringUtil.stringToBool(increment);

				String display = typeResolver.getSqlTypeDisplay(typeName, sqlType, size, digits);

				if (dbConnection.getMetadata().isSqlServer() && dbSettings.fixSqlServerAutoincrement())
				{
					// The Microsoft JDBC Driver does not return the autoincrement attribute correctly for identity columns.
					// (And they refuse to fix this: http://social.msdn.microsoft.com/Forums/en/sqldataaccess/thread/20df12f3-d1bf-4526-9daa-239a83a8e435)
					// This hack works around Microsoft's ignorance regarding Java and JDBC
					autoincrement = display.contains("identity");
				}

				col.setDbmsType(display);
				col.setIsAutoincrement(autoincrement);
				col.setIsPkColumn(primaryKeyColumns.contains(colName));
				col.setIsNullable("YES".equalsIgnoreCase(nullable));
				col.setDefaultValue(defaultValue);
				col.setComment(remarks);
				col.setColumnSize(size);
				col.setDecimalDigits(digits);
				col.setPosition(position);
				columns.add(col);
			}
		}
		finally
		{
			SqlUtil.closeResult(rs);
		}

		// Some JDBC drivers (e.g. Ingres) do not return the columns in the correct order, so we need to make sure they are sorted correctly
		// for any DBMS returning them in the correct order, this shouldn't make a difference.
		ColumnIdentifier.sortByPosition(columns);

		return columns;
	}

	/**
	 * Return the definition of the given table.
	 * <br/>
	 * To display the columns for a table in a DataStore create an
	 * instance of {@link TableColumnsDatastore}.
	 *
	 * @param toRead The table for which the definition should be retrieved
	 *
	 * @throws SQLException
	 * @return the definition of the table.
	 * @see TableColumnsDatastore
	 */
	@Override
	public TableDefinition getTableDefinition(TableIdentifier toRead)
		throws SQLException
	{
		if (toRead == null) return null;

		TableIdentifier table = toRead.createCopy();
		table.adjustCase(dbConnection);

		String catalog = SqlUtil.removeObjectQuotes(table.getCatalog());
		String schema = SqlUtil.removeObjectQuotes(table.getSchema());
		String tablename = SqlUtil.removeObjectQuotes(table.getTableName());

		DbMetadata meta = dbConnection.getMetadata();
		if (schema == null)
		{
			schema = meta.getCurrentSchema();
			table.setSchema(schema);
		}

		if (catalog == null)
		{
			catalog = meta.getCurrentCatalog();
			table.setCatalog(catalog);
		}

		TableIdentifier retrieve = table;

		if (dbConnection.getDbSettings().isSynonymType(table.getType()))
		{
			TableIdentifier id = meta.getSynonymTable(catalog, schema, tablename);
			if (id != null)
			{
				schema = id.getSchema();
				tablename = id.getTableName();
				catalog = null;
				retrieve = table.createCopy();
				retrieve.setSchema(schema);
				retrieve.parseTableIdentifier(tablename);
				retrieve.setCatalog(null);
			}
		}

		PkDefinition pk = meta.getIndexReader().getPrimaryKey(retrieve);
		retrieve.setPrimaryKey(pk);

		List<ColumnIdentifier> columns = getTableColumns(retrieve, meta.getDataTypeResolver());

		retrieve.setNewTable(false);
		TableDefinition result = new TableDefinition(retrieve, columns);

		ColumnDefinitionEnhancer columnEnhancer = getColumnEnhancer(dbConnection);
		if (columnEnhancer != null)
		{
			columnEnhancer.updateColumnDefinition(result, dbConnection);
		}

		return result;
	}

	private ColumnDefinitionEnhancer getColumnEnhancer(WbConnection con)
	{
		if (con == null) return null;
		DbMetadata meta = con.getMetadata();
		if (meta == null) return null;

		if (meta.isPostgres())
		{
			 return new PostgresColumnEnhancer();
		}
		if (meta.isH2())
		{
			return new H2ColumnEnhancer();
		}
		if (meta.isApacheDerby())
		{
			return new DerbyColumnEnhancer();
		}
		if (meta.isMySql())
		{
			return new MySQLColumnEnhancer();
		}
		if (con.getDbId().equals("db2"))
		{
			return new Db2ColumnEnhancer();
		}
		if (meta.isSqlServer() && SqlServerUtil.isSqlServer2005(con))
		{
			return new SqlServerColumnEnhancer();
		}
		if (meta.isFirebird())
		{
			return new FirebirdColumnEnhancer();
		}
		if (con.getDbId().equals("nuodb"))
		{
			return new NuoDbColumnEnhancer();
		}
		if (meta.isHsql() && JdbcUtils.hasMinimumServerVersion(con, "2.0"))
		{
			return new HsqlColumnEnhancer();
		}
		return null;
	}
}