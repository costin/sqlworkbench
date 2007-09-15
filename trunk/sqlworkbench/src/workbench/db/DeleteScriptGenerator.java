/*
 * DeleteScriptGenerator.java
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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import workbench.WbManager;
import workbench.gui.components.WbTable;
import workbench.gui.dbobjects.ObjectScripterUI;
import workbench.interfaces.ScriptGenerationMonitor;
import workbench.interfaces.Scripter;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.sql.formatter.SqlFormatter;
import workbench.storage.ColumnData;
import workbench.storage.DataStore;
import workbench.storage.SqlLiteralFormatter;
import workbench.util.SqlUtil;
import workbench.util.StringUtil;

/**
 *	Generates a SQL script to delete a record from the given table and
 *	any dependent tables.
 * @author  support@sql-workbench.net
 */
public class DeleteScriptGenerator
	implements Scripter
{
	private WbConnection connection;
	private List<ColumnData> columnValues;
	private TableDependency dependency;
	private DbMetadata meta;
	private DataStore tableDefinition;
	private TableIdentifier rootTable = null;
	private WbTable sourceTable = null;
	private ScriptGenerationMonitor monitor;
	private List<DependencyNode> visitedTables = new ArrayList<DependencyNode>();
	private String script;
	private SqlLiteralFormatter formatter;
	
	public DeleteScriptGenerator(WbConnection aConnection)
		throws SQLException
	{
		this.connection = aConnection;
		this.meta = this.connection.getMetadata();
		this.formatter = new SqlLiteralFormatter(this.connection);
	}

	public void setSource(WbTable aTable)
	{
		this.sourceTable = aTable;
	}

	public void setTable(TableIdentifier table)
		throws SQLException
	{
		if (table == null) throw new IllegalArgumentException("The table name may not be empty");

		this.rootTable = table.createCopy();
		if (rootTable.getSchema() == null)
		{
			rootTable.setSchema(this.meta.getCurrentSchema());
		}
		this.dependency = new TableDependency(this.connection, this.rootTable);
		this.tableDefinition = this.meta.getTableDefinition(this.rootTable);
	}

	public void setValues(List<ColumnData> colValues)
	{
		this.columnValues = colValues;
	}

	public boolean isCancelled()
	{
		// not implemented yet
		return false;
	}
	
	public void cancel()
	{
		// not implemented yet
	}
	
	private String createScriptForCurrentObject()
	{
		ArrayList<DependencyNode> parents = new ArrayList<DependencyNode>();
		this.dependency.readDependencyTree(true);
		List<DependencyNode> leafs = this.dependency.getLeafs();
		StringBuilder sql = new StringBuilder(2000);
		
		for (DependencyNode node : leafs)
		{
			if (this.visitedTables.contains(node)) continue;
			this.addDeleteStatement(sql, node);
			this.visitedTables.add(node);
			DependencyNode p = node.getParent();
			while (p != null)
			{
				if (!isMasterTable(p) && !parents.contains(p) && !leafs.contains(p))
				{
					parents.add(p);
				}
				p = p.getParent();
			}
			sql.append("\n\n");
		}

		for (DependencyNode pnode : parents)
		{
			if (this.visitedTables.contains(pnode)) continue;
			this.addDeleteStatement(sql, pnode);
			this.visitedTables.add(pnode);
			sql.append("\n");
		}

		DependencyNode root = this.dependency.getRootNode();
		sql.append("DELETE FROM ");
		sql.append(root.getTable().getTableExpression(this.connection));
		sql.append("\n WHERE ");
		this.addRootTableWhere(sql);
		sql.append(';');
		try
		{
			int max = Settings.getInstance().getFormatterMaxSubselectLength();
			SqlFormatter format = new SqlFormatter(sql.toString(), max);
			return format.getFormattedSql().trim();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return sql.toString();
		}
	}

	private void addDeleteStatement(StringBuilder sql, DependencyNode node)
	{
		if (node == null) return;

		sql.append("DELETE FROM ");
		sql.append(node.getTable().getTableExpression(this.connection));
		sql.append("\n WHERE ");

		this.addParentWhere(sql, node);
		sql.append(';');
	}

	private void addParentWhere(StringBuilder sql, DependencyNode node)
	{
		try
		{
			DependencyNode parent = node.getParent();
			sql.append(" (");

			Map columns = node.getColumns();
			Iterator itr = columns.entrySet().iterator();
			int count = 0;
			while (itr.hasNext())
			{
				Map.Entry entry = (Map.Entry)itr.next();
				String column = (String)entry.getKey();
				column = this.meta.adjustObjectnameCase(column);
				String parentColumn = (String)entry.getValue();
				//if (nodeColumn != null && !nodeColumn.equals(column)) continue;
				if (count > 0) sql.append("\n          AND ");
				if (!this.rootTable.equals(parent.getTable()))
				{
					sql.append('(');
					sql.append(column);
					sql.append(" IN ( SELECT ");
					sql.append(parentColumn);
					sql.append(" FROM ");
					sql.append(parent.getTable().getTableExpression(this.connection));
					sql.append("\n WHERE ");
					this.addParentWhere(sql, parent);
					sql.append(")) ");
					count ++;
				}
				else
				{
					this.addRootTableWhere(sql, parentColumn, column);
				}

			}
			sql.append(')');
		}
		catch (Throwable th)
		{
			LogMgr.logError("DeleteScriptGenerator.addParentWhere()", "Error during script generation", th);
		}
	}

	private boolean isMasterTable(DependencyNode node)
	{
		TableIdentifier table = node.getTable();
		return (this.rootTable.equals(table));
	}

	private void addRootTableWhere(StringBuilder sql)
	{
		boolean first = true;
		for (ColumnData col : this.columnValues)
		{
			if (!first)
			{
				sql.append("\n   AND ");
			}
			else
			{
				first = false;
			}

			sql.append(col.getIdentifier().getColumnName());
			Object data = col.getValue();
			if (data == null)
			{
				sql.append(" IS NULL");
			}
			else
			{
				sql.append(" = ");
				sql.append(formatter.getDefaultLiteral(col));
//	      String value = data.toString();
//				sql.append(" = ");
//				boolean charType = (type == Types.VARCHAR || type == Types.CHAR);
//				if (charType)	sql.append('\'');
//				sql.append(value);
//				if (charType)	sql.append('\'');
			}
		}
	}

	private ColumnData findColData(String column)
	{
		for (ColumnData col : this.columnValues)
		{
			if (col.getIdentifier().getColumnName().equalsIgnoreCase(column)) return col;
		}		
		return null;
	}
	
	private void addRootTableWhere(StringBuilder sql, String parentColumn, String childColumn)
	{
		ColumnData data = findColData(parentColumn);
		
		parentColumn = this.meta.adjustObjectnameCase(parentColumn);

		int type = this.getColumnType(tableDefinition, parentColumn);
		sql.append(SqlUtil.quoteObjectname(childColumn));
		if (data.isNull())
		{
			sql.append(" IS NULL");
		}
		else
		{
			sql.append(" = ");
			sql.append(formatter.getDefaultLiteral(data));
//	    String value = data.toString();
//			sql.append(" = ");
//			boolean charType = (type == Types.VARCHAR || type == Types.CHAR);
//			if (charType)	sql.append('\'');
//			sql.append(value);
//			if (charType)	sql.append('\'');
		}
	}

	private int getColumnType(DataStore tableDef, String aColname)
	{
		for (int i=0; i < tableDef.getRowCount(); i ++)
		{
			String col = tableDef.getValueAsString(i, DbMetadata.COLUMN_IDX_TABLE_DEFINITION_COL_NAME);
			if (aColname.equals(col))
			{
				String t = tableDef.getValueAsString(i, DbMetadata.COLUMN_IDX_TABLE_DEFINITION_JAVA_SQL_TYPE);
				int type = StringUtil.getIntValue(t, 0);
				return type;
			}
		}
		return -1;
	}

	public void startGenerate()
	{
		ObjectScripterUI ui = new ObjectScripterUI(this);
		ui.show(WbManager.getInstance().getCurrentWindow());
	}

	public void setProgressMonitor(ScriptGenerationMonitor aMonitor)
	{
		this.monitor = aMonitor;
	}

	public String getScript()
	{
		if (this.script == null) this.generateScript();
		return this.script;
	}
	
	public void generateScript()
	{
		this.script = "";
		if (this.sourceTable == null) return;

		DataStore ds = this.sourceTable.getDataStore();
		if (ds == null) return;

		int[] rows = this.sourceTable.getSelectedRows();
		if (rows.length == 0)
		{
			return;
		}

		ds.checkUpdateTable();
		TableIdentifier tbl = ds.getUpdateTable();
		int numRows = rows.length;
		StringBuilder result = new StringBuilder(numRows * 150);
		int max = Settings.getInstance().getFormatterMaxSubselectLength();
		StringBuilder sep = new StringBuilder(max + 2);
		sep.append('\n');
		for (int i=0; i < max; i++) sep.append('=');
		sep.append('\n');

		try
		{
			for (int i=0; i < numRows; i++)
			{
				List<ColumnData> pkvalues = ds.getPkValues(rows[i]);
				this.setTable(tbl);
				this.setValues(pkvalues);
				this.monitor.setCurrentObject(ResourceMgr.getString("MsgGeneratingScriptForRow") + " " + i);
				
				String rowScript = this.createScriptForCurrentObject();
				if (i > 0) result.append(sep);
				result.append(rowScript);
			}
		}
		catch (Exception e)
		{
			LogMgr.logError("SqlPanel.generateDeleteScript", "Error generating delete script", e);
		}
		this.script = result.toString();
	}
}
