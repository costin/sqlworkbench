/*
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2014 Thomas Kellerer.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * To contact the author please send an email to: support@sql-workbench.net
 */
package workbench.storage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import workbench.log.LogMgr;

import workbench.db.ColumnIdentifier;
import workbench.db.DbMetadata;
import workbench.db.IndexColumn;
import workbench.db.IndexDefinition;
import workbench.db.IndexReader;
import workbench.db.PkDefinition;
import workbench.db.ReaderFactory;
import workbench.db.TableDefinition;
import workbench.db.TableIdentifier;
import workbench.db.WbConnection;

import workbench.util.CollectionUtil;

/**
 *
 * @author Thomas Kellerer
 */
public class UpdateTableDetector
{
	private TableIdentifier updateTable;
	private List<ColumnIdentifier> missingPkcolumns;
	private WbConnection conn;

	public UpdateTableDetector(WbConnection db)
	{
		conn = db;
	}

	public TableIdentifier getUpdateTable()
	{
		return updateTable;
	}

	public List<ColumnIdentifier> getMissingPkColumns()
	{
		return missingPkcolumns;
	}

	public void checkUpdateTable(TableIdentifier tbl, ResultInfo resultInfo)
	{
		updateTable = null;
		resultInfo.setUpdateTable(null);
		missingPkcolumns = null;

		if (tbl == null)
		{
			return;
		}

		if (conn.getDbSettings().getOnlyRetrievePkForUpdateCheck())
		{
			LogMgr.logDebug("UpdateTableDetector.setUpdateTable()", "Only checking the PK definition for " + tbl.getTableExpression());
			checkPkOnlyForUpdateTable(tbl, resultInfo);
			if (updateTable != null) return;
		}

		List<ColumnIdentifier> columns = null;

		// check the columns which are in the new table so that we can refuse any changes to columns
		// which do not derive from that table.
		// Note that this does not work, if the columns were renamed via an alias in the select statement

		try
		{
			DbMetadata meta = conn.getMetadata();
			if (meta == null) return;

			if (conn.getDbSettings().getRetrieveTableDefinitionDirectly())
			{
				// try to use the table name as-is and retrieve the table definition directly
				// as this should cover the majority of all cases it should usually be a bit faster
				// than first searching for the table - especially for Oracle with its dead slow catalog views
				TableDefinition def = getDefinition(tbl);
				if (def != null && def.getColumnCount() > 0 && def.getTable() != null)
				{
					columns = def.getColumns();
					updateTable = def.getTable();
				}
			}

			if (updateTable == null)
			{
				// Look up the table in the database to make sure
				// we get the name correct (upper/lowercase etc)
				this.updateTable = findTable(tbl);
			}

			// No table found --> nothing to do.
			if (updateTable == null) return;

			if (columns == null)
			{
				columns = getColumns(tbl);
			}

			int realColumns = 0;

			if (columns != null)
			{
				this.missingPkcolumns = new ArrayList<>(columns.size());

				for (ColumnIdentifier column : columns)
				{
					int index = resultInfo.findColumn(column.getColumnName(), conn.getMetadata());
					if (index > -1)
					{
						syncResultColumn(index, column, resultInfo);
						realColumns++;
					}
					else if (column.isPkColumn())
					{
						this.missingPkcolumns.add(column);
					}
				}
			}

			if (realColumns == 0 && updateTable != null)
			{
				LogMgr.logWarning("UpdateTableDetector.setUpdateTable()", "No columns from the table " + this.updateTable.getTableExpression() + " could be found in the current result set!");
			}

			if (!resultInfo.hasPkColumns() && meta.getDbSettings().checkUniqueIndexesForPK())
			{
				checkUniqueIndexesFor(conn, updateTable, resultInfo);
			}
		}
		catch (Exception e)
		{
			this.updateTable = null;
			LogMgr.logError("UpdateTableDetector.setUpdateTable()", "Could not read table definition", e);
		}
		resultInfo.setUpdateTable(updateTable);
	}

	private List<ColumnIdentifier> getColumns(TableIdentifier table)
		throws SQLException
	{
		// If the object that was used in the original SELECT is
		// a synonym we have to get the definition of the underlying
		// table in order to find the primary key columns
		TableIdentifier synCheck = table.createCopy();

		if (synCheck.getSchema() == null)
		{
			// no need to check for catalogs, because the DBMS that support
			// synonyms (Oracle, DB2) don't support catalogs
			synCheck.setSchema(conn.getMetadata().getSchemaToUse());
		}

		// if the passed table is not a synonym resolveSynonym
		// will return the passed table
		TableIdentifier toCheck = getSynonymTable(synCheck);

		List<ColumnIdentifier> columns = null;

		if (conn.getDbSettings().useCompletionCacheForUpdateTableCheck())
		{
			columns = conn.getObjectCache().getColumns(toCheck);
		}
		else
		{
			columns = conn.getMetadata().getTableColumns(toCheck);
		}
		return columns;
	}

	private TableIdentifier getSynonymTable(TableIdentifier toCheck)
	{
		if (conn.getDbSettings().useCompletionCacheForUpdateTableCheck())
		{
			return conn.getObjectCache().getSynonymTable(toCheck);
		}
		return conn.getMetadata().resolveSynonym(toCheck);
	}

	private void checkPkOnlyForUpdateTable(TableIdentifier tbl, ResultInfo resultInfo)
	{
		DbMetadata meta = conn.getMetadata();
		if (meta == null) return;
		if (resultInfo == null) return;

		PkDefinition pk = meta.getIndexReader().getPrimaryKey(tbl);

		TableIdentifier table = tbl.createCopy();
		table.adjustCase(conn);

		if (pk == null || CollectionUtil.isEmpty(pk.getColumns()))
		{
			checkUniqueIndexesFor(conn, tbl, resultInfo);
		}
		else
		{
			for (String colName : pk.getColumns())
			{
				int index = resultInfo.findColumn(colName, conn.getMetadata());
				if (index > -1)
				{
					resultInfo.setIsPkColumn(index, true);
					resultInfo.setIsNullable(index, false);
				}
			}
		}
		if (resultInfo.hasPkColumns())
		{
			this.updateTable = table;
			resultInfo.setUpdateTable(updateTable);
		}
	}

	private void checkUniqueIndexesFor(WbConnection con, TableIdentifier tableToUse, ResultInfo result)
	{
		if (tableToUse == null || result == null) return;
		LogMgr.logInfo("UpdateTableDetector.checkUniqueIndexesForPK()", "No PK found for table " + tableToUse.getTableName()+ " Trying to find an unique index.");
		List<IndexDefinition> indexes = null;
		if (con.getDbSettings().useCompletionCacheForUpdateTableCheck())
		{
			indexes = con.getObjectCache().getUniqueIndexes(tableToUse);
		}
		else
		{
			IndexReader reader = ReaderFactory.getIndexReader(con.getMetadata());
			indexes = reader.getUniqueIndexes(tableToUse);
		}
		if (CollectionUtil.isEmpty(indexes)) return;

		IndexDefinition idx = indexes.get(0);
		List<IndexColumn> columns = idx.getColumns();
		LogMgr.logInfo("UpdateTableDetector.checkUniqueIndexesForPK()", "Using unique index " + idx.getObjectName() + " as a surrogate PK");
		for (IndexColumn col : columns)
		{
			int index = result.findColumn(col.getColumn(), conn.getMetadata());

			if (index > -1)
			{
				result.setIsPkColumn(index, true);
				result.setIsNullable(index, false);
			}
		}
	}

	public void syncResultColumn(int index, ColumnIdentifier column, ResultInfo info)
	{
		boolean canUpdate = true;
		if (!column.isAutoGenerated() && column.getComputedColumnExpression() != null)
		{
			canUpdate = false;
			LogMgr.logDebug("UpdateTableDetector.syncResultColumn()", "Column " + column.getColumnName() + " can not be updated because it is a computed column");
		}

		if (info.getColumn(index).isReadonly())
		{
			LogMgr.logDebug("UpdateTableDetector.syncResultColumn()", "Column " + column.getColumnName() + " was marked as read-only by the driver!");
		}

		info.setUpdateable(index, canUpdate);
		info.setIsPkColumn(index, column.isPkColumn());
		info.setIsNullable(index, column.isNullable());
		ColumnIdentifier resultCol = info.getColumn(index);
		resultCol.setIsAutoincrement(column.isAutoincrement());
		resultCol.setComputedColumnExpression(column.getComputedColumnExpression());
	}

	private TableDefinition getDefinition(TableIdentifier toFind)
		throws SQLException
	{
		TableDefinition def = null;
		if (conn.getDbSettings().useCompletionCacheForUpdateTableCheck())
		{
			List<ColumnIdentifier> columns = conn.getObjectCache().getColumns(toFind);
			if (columns != null)
			{
				TableIdentifier tbl = conn.getObjectCache().getTable(toFind);
				def = new TableDefinition(tbl, columns);
			}
		}
		if (def == null)
		{
			def = conn.getMetadata().getTableDefinition(toFind);
		}
		return def;
	}

	private TableIdentifier findTable(TableIdentifier table)
	{
		TableIdentifier tbl = null;
		if (conn.getDbSettings().useCompletionCacheForUpdateTableCheck())
		{
			tbl = conn.getObjectCache().getTable(table);
			if (tbl == null)
			{
				tbl = conn.getObjectCache().getSynonymTable(table);
			}
		}

		if (tbl == null)
		{
			DbMetadata meta = conn.getMetadata();
			tbl = meta.searchSelectableObjectOnPath(table);
			if (tbl == null && table.getSchema() == null)
			{
				tbl = meta.getSynonymTable(table);
				if (tbl != null)
				{
					LogMgr.logDebug("UpdateTableDetector.findTable()", "Using synonym table: " + tbl.getObjectExpression(null));
				}
			}
		}
		return tbl;
	}
}
