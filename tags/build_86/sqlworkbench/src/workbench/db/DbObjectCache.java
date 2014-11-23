/*
 * DbObjectCache.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2006, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import workbench.log.LogMgr;
import workbench.resource.Settings;

/**
 * A cache for DbObjects to support Auto-completion in the editor
 * @author  support@sql-workbench.net
 */
public class DbObjectCache
	implements PropertyChangeListener
{
	private WbConnection dbConnection;
	private Set schemasInCache = new HashSet();
	private final String NULL_SCHEMA = "$$wb-null-schema$$";
	private boolean retrieveOraclePublicSynonyms = false;
	
	// This will map a TableIdentifier to a list of ColumnIdentifier's
	private SortedMap objects = new TreeMap();
	
	DbObjectCache(WbConnection conn)
	{
		this.dbConnection = conn;
		retrieveOraclePublicSynonyms = conn.getMetadata().isOracle() && Settings.getInstance().getBoolProperty("workbench.editor.autocompletion.oracle.public_synonyms", false);
		conn.addChangeListener(this);
	}
	
	/**
	 * Add this list of tables to the current cache. 
	 */
	private void setTables(List tables)
	{
		if (this.objects == null)
		{
			this.objects = new TreeMap();
		}
		for (int i=0; i < tables.size(); i++)
		{
			TableIdentifier tbl = (TableIdentifier)tables.get(i);
			if (!this.objects.containsKey(tbl));
			{
				this.objects.put(tbl, Collections.EMPTY_LIST);
			}
		}
	}
	
	public Set getTables()
	{
		return getTables(null, null);
	}
	
	public Set getTables(String schema)
	{
		return getTables(schema, null);
	}
	
	/**
	 * Get the tables (and views) the are currently in the cache
	 */
	public Set getTables(String schema, String type)
	{
		if (this.objects.size() == 0 || (!schemasInCache.contains(schema == null ? NULL_SCHEMA : schema.toUpperCase()))) 
		{
			try
			{
				List tables = this.dbConnection.getMetadata().getSelectableObjectsList(schema);
				this.setTables(tables);
				this.schemasInCache.add(schema == null ? NULL_SCHEMA : schema.toUpperCase());
			}
			catch (Exception e)
			{
				LogMgr.logError("DbObjectCache.getTables()", "Could not retrieve table list", e);
			}
		}
		if (type != null)
			return filterTablesByType(schema, type);
		else
			return filterTablesBySchema(schema);
	}

	private Set filterTablesByType(String schema, String type)
	{
		this.getTables(schema);
		Iterator itr = this.objects.keySet().iterator();
		SortedSet result = new TreeSet();
		while (itr.hasNext())
		{
			TableIdentifier tbl = (TableIdentifier)itr.next();
			String ttype = tbl.getType();
			String tSchema = tbl.getSchema();
			if ( type.equalsIgnoreCase(ttype) &&
				   ((schema == null || schema.equalsIgnoreCase(tSchema) || tSchema == null || "public".equalsIgnoreCase(tSchema)))
				 )
			{
				TableIdentifier copy = tbl.createCopy();
				if (tSchema != null && tSchema.equals(tbl.getSchema())) copy.setSchema(null);
				result.add(copy);
			}
		}
		return result;
	}
	
	private Set filterTablesBySchema(String schema)
	{
		if (schema == null)	return Collections.unmodifiableSet(this.objects.keySet());
		Iterator itr = this.objects.keySet().iterator();
		SortedSet result = new TreeSet();
		while (itr.hasNext())
		{
			TableIdentifier tbl = (TableIdentifier)itr.next();
			String tSchema = tbl.getSchema();
			if (schema.equalsIgnoreCase(tSchema) || tSchema == null || "public".equalsIgnoreCase(tSchema))
			{
				TableIdentifier copy = tbl.createCopy();
				copy.setSchema(null);
				result.add(copy);
			}
		}
		return result;
	}
	
	/**
	 * Return the columns for the given table
	 * @return a List with {@link ColumnIdentifier} objects
	 */
	public List getColumns(TableIdentifier tbl)
	{
		String schema = tbl.getSchema();
		
		if (this.objects.size() == 0 || !schemasInCache.contains(schema == null ? NULL_SCHEMA : schema.toUpperCase()))
		{
			this.getTables(schema);
		}
		
		List cols = (List)this.objects.get(tbl);
		
		TableIdentifier tblToUse = null;
		TableIdentifier t2 = null;
		
		// if we didn't find an entry with the schema in the table, try
		// to find a table with that name but without the schema
		// (this is to support oracle public synonyms/objects)
		if (tbl.getSchema() != null && cols == null || cols == Collections.EMPTY_LIST)
		{
			if (!this.objects.containsKey(tbl))
			{
				t2 = tbl.createCopy();
				t2.setSchema(null);
				t2.setType(null);
				cols = (List)this.objects.get(t2);
				if (cols == null && retrieveOraclePublicSynonyms)
				{
					// retrieve Oracle PUBLIC synonyms
					this.getTables("PUBLIC");
					//t2.setType("SYNONYM");
					cols = (List)this.objects.get(t2);
				}
			}
		}
		
		if (cols == null || cols == Collections.EMPTY_LIST)
		{
			// use the stored key because that might carry the correct type attribute
			// TabelIdentifier.equals() doesn't compare the type, only the expression
			// so we'll get a containsKey() == true even if the type is different
			// (which is necessary because the TableIdentifier passed to this 
			// method will never contain a type!)
			if (objects.containsKey(tbl))
			{
				tblToUse = findKey(tbl);
			}
			else if (t2 != null && objects.containsKey(t2))
			{
				tblToUse = findKey(t2);
			}
			else
			{
				tblToUse = tbl;
			}
			
			try
			{
				cols = this.dbConnection.getMetadata().getTableColumns(tblToUse);
				Collections.sort(cols);
			}
			catch (Throwable e)
			{
				LogMgr.logError("DbObjectCache.getColumns", "Error retrieving columns for " + tbl, e);
				cols = Collections.EMPTY_LIST;
			}
			this.objects.put(tbl, cols);
				
		}
		return Collections.unmodifiableList(cols);
	}
	
	/**
	 * Return the stored key according to the passed
	 * TableIdentifier. The stored key might carry additional
	 * properties that the passed key does not have (even 
	 * though they are equal)
	 */
	private TableIdentifier findKey(TableIdentifier key)
	{
		if (key == null) return null;
		Iterator itr = this.objects.keySet().iterator();
		while (itr.hasNext())
		{
			TableIdentifier tbl = (TableIdentifier)itr.next();
			if (key.equals(tbl)) return tbl;
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
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (WbConnection.PROP_CONNECTION_STATE.equals(evt.getPropertyName()) &&
			  WbConnection.CONNECTION_CLOSED.equals(evt.getNewValue()))
		{
			this.clear();
		}
	}

}