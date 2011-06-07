/*
 * DbObjectCache.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2011, Thomas Kellerer
 * No part of this code may be reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import workbench.log.LogMgr;
import workbench.resource.Settings;
import workbench.storage.DataStore;
import workbench.util.CollectionUtil;

/**
 * A cache for database objects to support Auto-completion in the editor
 * @author  Thomas Kellerer
 */
public class DbObjectCache
	implements PropertyChangeListener
{
	private WbConnection dbConnection;
	private static final String NULL_SCHEMA = "$$wb-null-schema$$";
	private boolean retrieveOraclePublicSynonyms;

	private Set<String> schemasInCache;
	private SortedMap<TableIdentifier, List<ColumnIdentifier>> objects;
	private Map<String, List<ProcedureDefinition>> procedureCache = new HashMap<String, List<ProcedureDefinition>>();

	DbObjectCache(WbConnection conn)
	{
		this.dbConnection = conn;
		this.createCache();
		retrieveOraclePublicSynonyms = conn.getMetadata().isOracle() && Settings.getInstance().getBoolProperty("workbench.editor.autocompletion.oracle.public_synonyms", false);
		conn.addChangeListener(this);
	}

	private void createCache()
	{
		schemasInCache = new TreeSet<String>();
		objects = new TreeMap<TableIdentifier, List<ColumnIdentifier>>(new TableNameSorter(true));
	}
	/**
	 * Add this list of tables to the current cache.
	 */
	private void setTables(List<TableIdentifier> tables)
	{
		for (TableIdentifier tbl : tables)
		{
			if (!this.objects.containsKey(tbl))
			{
				this.objects.put(tbl, null);
			}
		}
	}

	public Set<TableIdentifier> getTables(String schema)
	{
		return getTables(schema, null);
	}

	private String getSchemaToUse(String schema)
	{
		DbMetadata meta = this.dbConnection.getMetadata();
		return meta.adjustSchemaNameCase(schema);
	}

	/**
	 * Get the tables (and views) the are currently in the cache
	 */
	public Set<TableIdentifier> getTables(String schema, List<String> type)
	{
		String schemaToUse = getSchemaToUse(schema);
		if (this.objects.size() == 0 || (!schemasInCache.contains(schemaToUse == null ? NULL_SCHEMA : schemaToUse)))
		{
			try
			{
				DbMetadata meta = this.dbConnection.getMetadata();
				List<TableIdentifier> tables = meta.getSelectableObjectsList(null, schemaToUse);
				for (TableIdentifier tbl : tables)
				{
					tbl.checkQuotesNeeded(dbConnection);
				}
				this.setTables(tables);
				this.schemasInCache.add(schema == null ? NULL_SCHEMA : schemaToUse);
			}
			catch (Exception e)
			{
				LogMgr.logError("DbObjectCache.getTables()", "Could not retrieve table list", e);
			}
		}
		if (type != null)
			return filterTablesByType(schemaToUse, type);
		else
			return filterTablesBySchema(schemaToUse);
	}

	/**
	 * Get the procedures the are currently in the cache
	 */
	public List<ProcedureDefinition> getProcedures(String schema)
	{
		String schemaToUse = getSchemaToUse(schema);
		List<ProcedureDefinition> procs = procedureCache.get(schemaToUse);
		if (procs == null)
		{
			try
			{
				procs = this.dbConnection.getMetadata().getProcedureReader().getProcedureList(null, schemaToUse, "%");
				if (dbConnection.getDbSettings().getRetrieveProcParmsForAutoCompletion())
				{
					for (ProcedureDefinition proc : procs)
					{
						proc.getParameterTypes(this.dbConnection);
					}
				}
				procedureCache.put(schemaToUse, procs);
			}
			catch (SQLException e)
			{
				LogMgr.logError("ExecAnalyzer.checkContext()", "Error retrieving procedures", e);
			}
		}
		return procs;
	}

	private Set<TableIdentifier> filterTablesByType(String schema, List<String> type)
	{
		this.getTables(schema);
		String schemaToUse = getSchemaToUse(schema);
		SortedSet<TableIdentifier> result = new TreeSet<TableIdentifier>(new TableNameSorter());
		for (TableIdentifier tbl : objects.keySet())
		{
			String ttype = tbl.getType();
			String tSchema = tbl.getSchema();
			if ( type.contains(ttype) &&
				   ((schemaToUse == null || schemaToUse.equalsIgnoreCase(tSchema) || tSchema == null || "public".equalsIgnoreCase(tSchema)))
				 )
			{
				TableIdentifier copy = tbl.createCopy();
				if (tSchema != null && tSchema.equals(tbl.getSchema())) copy.setSchema(null);
				result.add(copy);
			}
		}
		return result;
	}

	private Set<TableIdentifier> filterTablesBySchema(String schema)
	{
		SortedSet<TableIdentifier> result = new TreeSet<TableIdentifier>(new TableNameSorter(true));
		DbMetadata meta = this.dbConnection.getMetadata();
		String schemaToUse = getSchemaToUse(schema);

		boolean alwaysUseSchema = dbConnection.getDbSettings().alwaysUseSchemaForCompletion();
		boolean alwaysUseCatalog = dbConnection.getDbSettings().alwaysUseCatalogForCompletion();

		String currentSchema = meta.getCurrentSchema();

		for (TableIdentifier tbl : objects.keySet())
		{
			String tSchema = tbl.getSchema();

			// meta.ignoreSchema() needs to be tested, because if that is true
			// the returned Tables will not contain the schema...
			boolean ignoreSchema = meta.ignoreSchema(schemaToUse, currentSchema);

			if (schemaToUse == null || schemaToUse.equalsIgnoreCase(tSchema) || ignoreSchema)
			{
				TableIdentifier copy = tbl.createCopy();
				if (ignoreSchema && !alwaysUseSchema)
				{
					copy.setSchema(null);
				}

				if (meta.ignoreCatalog(copy.getCatalog()) && !alwaysUseCatalog)
				{
					copy.setCatalog(null);
				}
				result.add(copy);
			}
		}
		return result;
	}

	/**
	 * Return the columns for the given table.
	 *
	 * If the table columns are not in the cache they are retrieved from the database.
	 * 
	 * @return the columns of the table.
	 * @see DbMetadata#getTableDefinition(workbench.db.TableIdentifier)
	 */
	public synchronized List<ColumnIdentifier> getColumns(TableIdentifier tbl)
	{
		String schema = getSchemaToUse(tbl.getSchema());

		TableIdentifier toSearch = tbl.createCopy();
		toSearch.adjustCase(dbConnection);
		if (toSearch.getSchema() == null)
		{
			toSearch.setSchema(schema);
		}

		List<ColumnIdentifier> cols = this.objects.get(toSearch);
		if (cols == null)
		{
			try
			{
				TableDefinition def = dbConnection.getMetadata().getTableDefinition(toSearch);
				addTable(def);
			}
			catch (SQLException sql)
			{
				LogMgr.logWarning("DbObjectCache.getColumns()", "Error retrieving table definition", sql);
				return null;
			}
		}

		// To support Oracle public synonyms, try to find a table with that name but without a schema
		if (retrieveOraclePublicSynonyms && toSearch.getSchema() != null && cols == null)
		{
			toSearch.setSchema(null);
			toSearch.setType(null);
			cols = this.objects.get(toSearch);
			if (cols == null)
			{
				// retrieve Oracle PUBLIC synonyms
				this.getTables("PUBLIC");
				cols = this.objects.get(toSearch);
			}
		}

		if (CollectionUtil.isEmpty(cols))
		{
			TableIdentifier tblToUse = null;

			// use the stored key because that might carry the correct type attribute
			// TabelIdentifier.equals() doesn't compare the type, only the expression
			// so we'll get a containsKey() == true even if the type is different
			// (which is necessary because the TableIdentifier passed to this
			// method will never contain a type!)
			// only using objects.get() would not return anything!
			if (objects.containsKey(toSearch))
			{
				// we have already retrieved the list of tables, but not the columns for this table
				// the table identifier in the object map contains correct type and schema information, so we need
				// to use that
				tblToUse = findEntry(toSearch);
			}
			else
			{
				// retrieve the real table identifier based on the table name
				tblToUse = this.dbConnection.getMetadata().findObject(toSearch);
			}

			try
			{
				cols = this.dbConnection.getMetadata().getTableColumns(tblToUse);
			}
			catch (Throwable e)
			{
				LogMgr.logError("DbObjectCache.getColumns", "Error retrieving columns for " + tblToUse, e);
				cols = null;
			}

			if (tblToUse != null && CollectionUtil.isNonEmpty(cols))
			{
				this.objects.put(tblToUse, cols);
			}

		}
		return Collections.unmodifiableList(cols);
	}

	public synchronized void removeTable(TableIdentifier tbl)
	{
		if (tbl == null) return;

		boolean removed = this.objects.remove(tbl) != null;
		if (removed)
		{
			LogMgr.logDebug("DbObjectCach.addTableList()", "Removed " + tbl.getTableName() + " from the cache");
		}
	}

	public synchronized void addTableList(DataStore tables, String schema)
	{
		if (schema == null || "*".equals(schema) || "%".equals(schema)) return;
		Set<String> selectable = dbConnection.getMetadata().getObjectsWithData();

		int count = 0;

		// remove all tables for this schema otherwise we cannot get rid of
		// tables that might have been dropped.
		Iterator<TableIdentifier> itr = objects.keySet().iterator();
		while (itr.hasNext())
		{
			TableIdentifier tbl = itr.next();
			if (schema.equalsIgnoreCase(tbl.getSchema()))
			{
				itr.remove();
			}
		}

		for (int row = 0; row < tables.getRowCount(); row++)
		{
			String type = tables.getValueAsString(row, DbMetadata.COLUMN_IDX_TABLE_LIST_TYPE);
			if (selectable.contains(type))
			{
				TableIdentifier tbl = createIdentifier(tables, row);
				if (objects.get(tbl) == null)
				{
					// The table is either not there, or no columns have been retrieved so it's safe to add
					objects.put(tbl, null);
					count ++;
				}
			}
		}
		this.schemasInCache.add(schema);
		LogMgr.logDebug("DbObjectCach.addTableList()", "Added " + count + " objects");
	}

	private TableIdentifier createIdentifier(DataStore tableList, int row)
	{
		String name = tableList.getValueAsString(row, DbMetadata.COLUMN_IDX_TABLE_LIST_NAME);
		String schema = tableList.getValueAsString(row, DbMetadata.COLUMN_IDX_TABLE_LIST_SCHEMA);
		String catalog = tableList.getValueAsString(row, DbMetadata.COLUMN_IDX_TABLE_LIST_CATALOG);
		String type = tableList.getValueAsString(row, DbMetadata.COLUMN_IDX_TABLE_LIST_TYPE);
		String comment = tableList.getValueAsString(row, DbMetadata.COLUMN_IDX_TABLE_LIST_REMARKS);
		TableIdentifier tbl = new TableIdentifier(catalog, schema, name);
		tbl.setType(type);
		tbl.setNeverAdjustCase(true);
		tbl.setComment(comment);
		return tbl;
	}

	public synchronized void addTable(TableDefinition table)
	{
		if (table != null)
		{
			this.objects.put(table.getTable(), table.getColumns());
		}
	}

	/**
	 * Return the stored key according to the passed
	 * TableIdentifier. The stored key might carry additional
	 * properties that the passed key does not have (even
	 * though they are equal)
	 */
	private TableIdentifier findEntry(TableIdentifier key)
	{
		if (key == null) return null;

		// as contains() is using the comparator as well, we have to use it here also!
		Comparator<? super TableIdentifier> comparator = objects.comparator();

		for (TableIdentifier tbl : objects.keySet())
		{
			if (comparator.compare(key, tbl) == 0) return tbl;
		}
		return null;
	}

	/**
	 * Disposes any db objects held in the cache
	 */
	public void clear()
	{
		if (this.objects != null) this.objects.clear();
		this.schemasInCache.clear();
	}

	/**
	 * Notification about the state of the connection. If the connection
	 * is closed, we can dispose the object cache
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (WbConnection.PROP_CONNECTION_STATE.equals(evt.getPropertyName()) &&
			  WbConnection.CONNECTION_CLOSED.equals(evt.getNewValue()))
		{
			this.clear();
		}
	}

}
