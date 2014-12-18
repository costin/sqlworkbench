/*
 * TableDependency.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2007, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db;

import java.util.ArrayList;
import java.util.List;
import workbench.log.LogMgr;
import workbench.storage.DataStore;

/**
 *
 * @author  support@sql-workbench.net
 */
public class TableDependency
{
	private WbConnection connection;
	private TableIdentifier theTable;
	private DependencyNode tableRoot;
	private DbMetadata wbMetadata;
	private ArrayList<DependencyNode> leafs;
	private int currentLevel = 0;
	private int maxLevel = Integer.MAX_VALUE;
	private boolean readAborted = false;
	
	public TableDependency(WbConnection con, TableIdentifier tbl)
	{
		this.connection = con;
		this.wbMetadata = this.connection.getMetadata();
		this.theTable = tbl;
	}

	public void setMaxLevel(int max)
	{
		this.maxLevel = max;
	}

	public DependencyNode findLeafNodeForTable(TableIdentifier table)
	{
		String findExpr = table.getTableExpression(connection);
		for (DependencyNode node : leafs)
		{
			String expr = node.getTable().getTableExpression(connection);
			if (expr.equalsIgnoreCase(findExpr)) return node;
		}
		return null;
	}

	public void readTreeForChildren()
	{
		readDependencyTree(true);
	}
	
	public void readTreeForParents()
	{
		readDependencyTree(false);
	}
	
	public void readDependencyTree(boolean exportedKeys)
	{
		if (this.theTable == null) return;
		if (this.connection == null) return;
		this.readAborted = false;
		this.leafs = new ArrayList<DependencyNode>();
		
		// Make sure we are using the "correct" TableIdentifier
		// if the TableIdentifier passed in the constructor was 
		// created "on the commandline" e.g. by using a user-supplied
		// table name, we might not correctly find or compare all nodes
		// those identifiers will not have the flag "neverAdjustCase" set
		TableIdentifier tableToUse = this.theTable;
		if (!this.theTable.getNeverAdjustCase())
		{
			tableToUse = this.wbMetadata.findTable(theTable);
		}
		if (tableToUse == null) return;
		this.tableRoot = new DependencyNode(tableToUse);
		this.currentLevel = 0;
		this.readTree(this.tableRoot, exportedKeys);
	}

	/**
	 *	Create the dependency tree.
	 */
	private int readTree(DependencyNode parent, boolean exportedKeys)
	{
		try
		{
			DataStore ds = null;
			int catalogcol;
			int schemacol;
			int tablecol;
			int fknamecol;
			int tablecolumncol;
			int parentcolumncol;

			TableIdentifier ptbl = this.wbMetadata.resolveSynonym(parent.getTable());
			
			if (exportedKeys)
			{
				catalogcol = 4;
				schemacol = 5;
				tablecol = 6;
				fknamecol = 11;
				tablecolumncol = 7;
				parentcolumncol = 3;
				ds = this.wbMetadata.getExportedKeys(ptbl);
			}
			else
			{
				catalogcol = 0;
				schemacol = 1;
				tablecol = 2;
				fknamecol = 11;
				tablecolumncol = 3;
				parentcolumncol = 7;
				ds = this.wbMetadata.getImportedKeys(ptbl);
			}

			int count = ds.getRowCount();

			for (int i=0; i<count; i++)
			{
				String catalog = ds.getValueAsString(i, catalogcol);
				String schema = ds.getValueAsString(i, schemacol);
				String table = ds.getValueAsString(i, tablecol);
        String fkname = ds.getValueAsString(i, fknamecol);

				TableIdentifier tbl = new TableIdentifier(catalog, schema, table);

				tbl.setNeverAdjustCase(true);
				DependencyNode child = parent.addChild(tbl, fkname);
				String tablecolumn = ds.getValueAsString(i, tablecolumncol); // the column in "table" referencing the other table
				String parentcolumn = ds.getValueAsString(i, parentcolumncol); // the column in the parent table

				int update = ds.getValueAsInt(i, 9, -1);
				int delete = ds.getValueAsInt(i, 10, -1);
				child.setUpdateAction(this.wbMetadata.getDbSettings().getRuleDisplay(update));
				child.setDeleteAction(this.wbMetadata.getDbSettings().getRuleDisplay(delete));
				child.addColumnDefinition(tablecolumn, parentcolumn);
			}

			this.currentLevel ++;
			if (currentLevel > 10) 
			{
				// this is a bit paranoid, as I am testing for cycles before recursing
				// into the next child. This is a safetey net, just in case the cycle
				// is not detected. Better display the user incorrect data, than 
				// ending up in an endless loop.
				// A circular dependency with more than 10 levels is an ugly design anyway :)
				LogMgr.logWarning("TableDependency.readDependencyTree()", "Endless reference cycle detected for root=" + this.tableRoot, null);
				this.readAborted = true;
				return count;
			}
			
			List<DependencyNode> children = parent.getChildren();
			for (DependencyNode child : children)
			{
				if (!isCycle(child, parent))
				{
					this.readTree(child, exportedKeys);
				}
				this.leafs.add(child);
			}
      return count;
		}
		catch (Exception e)
		{
			LogMgr.logError("TableDependencyTree.readTree()", "Error when reading FK definition", e);
		}
    return 0;
	}

	private boolean isCycle(DependencyNode child, DependencyNode parent)
	{
		if (child.equals(parent)) return true;
		if (child.getTable().equals(parent.getTable())) return true;
		
		DependencyNode nextParent = parent.getParent();
		while (nextParent != null)
		{
			if (child.equals(nextParent)) return true;		
			nextParent = nextParent.getParent();
		}
		return false;
	}
	
	boolean wasAborted()
	{
		return this.readAborted;
	}
	
	public List<DependencyNode> getLeafs()
	{
		return this.leafs;
	}

	public DependencyNode getRootNode()
	{
		return this.tableRoot;
	}

}