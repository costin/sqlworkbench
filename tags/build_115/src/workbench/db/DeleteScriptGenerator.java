/*
 * DeleteScriptGenerator.java
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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import workbench.WbManager;
import workbench.interfaces.ScriptGenerationMonitor;
import workbench.interfaces.Scripter;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;

import workbench.gui.components.WbTable;
import workbench.gui.dbobjects.ObjectScripterUI;

import workbench.storage.ColumnData;
import workbench.storage.DataStore;
import workbench.storage.SqlLiteralFormatter;

import workbench.sql.formatter.SQLLexer;
import workbench.sql.formatter.SQLToken;
import workbench.sql.formatter.SqlFormatter;

/**
 * Generates a SQL script to delete a record from the given table and
 * any dependent tables.
 *
 * @author  Thomas Kellerer
 */
public class DeleteScriptGenerator
	implements Scripter
{
	private WbConnection connection;
	private List<ColumnData> columnValues;
	private TableDependency dependency;
	private DbMetadata meta;
	private TableIdentifier rootTable;
	private WbTable sourceTable;
	private ScriptGenerationMonitor monitor;
	private List<String> statements = new LinkedList<String>();
	private SqlLiteralFormatter formatter;
	private boolean formatSql = true;
	private boolean removeReduntant;

	public DeleteScriptGenerator(WbConnection aConnection)
		throws SQLException
	{
		this.connection = aConnection;
		this.meta = this.connection.getMetadata();
		this.formatter = new SqlLiteralFormatter(this.connection);
	}

	@Override
	public WbConnection getCurrentConnection()
	{
		return connection;
	}

	public void setRemoveRedundant(boolean flag)
	{
		this.removeReduntant = flag;
	}

	public void setFormatSql(boolean flag)
	{
		this.formatSql = flag;
	}

	public void setSource(WbTable aTable)
	{
		this.sourceTable = aTable;
	}

	public void setTable(TableIdentifier table)
		throws SQLException
	{
		if (table == null) throw new IllegalArgumentException("The table name may not be empty");

		// Make sure we are using a completely filled TableIdentifier
		// otherwise comparisons won't work correctly
		this.rootTable = this.meta.findTable(table, false);
		this.dependency = new TableDependency(this.connection, this.rootTable);
	}

	public void setValues(List<ColumnData> colValues)
	{
		this.columnValues = colValues;
	}

	@Override
	public boolean isCancelled()
	{
		// not implemented yet
		return false;
	}

	@Override
	public void cancel()
	{
		if (dependency != null)
		{
			dependency.cancel();
		}
	}

	private void createStatements(boolean includeRoot)
	{
		ArrayList<DependencyNode> parents = new ArrayList<DependencyNode>();
		List<DependencyNode> visitedTables = new ArrayList<DependencyNode>();
		this.dependency.readDependencyTree(true);
		List<DependencyNode> leafs = this.dependency.getLeafs();

		List<DependencyNode> toIgnore;
		if (removeReduntant)
		{
			DependencyDuplicateFinder finder = new DependencyDuplicateFinder(dependency.getRootNode());
			toIgnore = finder.getDuplicates();
		}
		else
		{
			toIgnore = Collections.emptyList();
		}

		for (DependencyNode node : leafs)
		{
			if (toIgnore.contains(node)) continue;
			if (visitedTables.contains(node)) continue;

			statements.add(createDeleteStatement(node));
			visitedTables.add(node);
			DependencyNode p = node.getParent();
			while (p != null)
			{
				if (!isMasterTable(p) && !parents.contains(p) && !leafs.contains(p))
				{
					parents.add(p);
				}
				p = p.getParent();
			}
		}

		for (DependencyNode pnode : parents)
		{
			if (toIgnore.contains(pnode)) continue;
			if (visitedTables.contains(pnode)) continue;

			statements.add(createDeleteStatement(pnode));
			visitedTables.add(pnode);
		}

		if (includeRoot)
		{
			DependencyNode root = this.dependency.getRootNode();
			StringBuilder rootSql = new StringBuilder(100);
			rootSql.append("DELETE FROM ");
			rootSql.append(root.getTable().getTableExpression(this.connection));
			rootSql.append(" WHERE ");
			this.addRootTableWhere(rootSql);
			statements.add(formatSql(rootSql));
		}
	}

	private String formatSql(StringBuilder sql)
	{
		if (!formatSql)
		{
			return sql.toString();
		}

		try
		{
			SqlFormatter f = new SqlFormatter(sql, Settings.getInstance().getFormatterMaxSubselectLength(), connection.getDbId());
			String formatted = f.getFormattedSql() + "\n";
			return formatted;
		}
		catch (Exception e)
		{
			return sql.toString();
		}
	}

	private String createDeleteStatement(DependencyNode node)
	{
		if (node == null) return null;
		StringBuilder sql = new StringBuilder(200);
		sql.append("DELETE FROM ");
		sql.append(node.getTable().getTableExpression(this.connection));
		sql.append(" WHERE ");

		this.addParentWhere(sql, node);
		return formatSql(sql);
	}

	private void addParentWhere(StringBuilder sql, DependencyNode node)
	{
		try
		{
			DependencyNode parent = node.getParent();

			Map<String, String> columns = node.getColumns();
			int count = 0;
			for (Entry<String, String> entry : columns.entrySet())
			{
				String column = entry.getKey();
				String parentColumn = entry.getValue();

				boolean addRootWhere = this.rootTable.equals(parent.getTable());

				if (count > 0) sql.append(" AND ");

				if (!addRootWhere)
				{
					sql.append(" (");
					sql.append(column);
					sql.append(" IN ( SELECT ");
					sql.append(parentColumn);
					sql.append(" FROM ");
					sql.append(parent.getTable().getTableExpression(this.connection));
					sql.append(" WHERE ");
					this.addParentWhere(sql, parent);
					sql.append("))");
				}
				else
				{
					this.addRootTableWhere(sql, parentColumn, column);
				}
				count ++;
			}
		}
		catch (Throwable th)
		{
			LogMgr.logError("DeleteScriptGenerator.addParentWhere()", "Error during script generation", th);
		}
	}

	private boolean isMasterTable(DependencyNode node)
	{
		TableIdentifier table = node.getTable();
		return this.rootTable.equals(table);
	}

	private void addRootTableWhere(StringBuilder sql)
	{
		boolean first = true;
		for (ColumnData col : this.columnValues)
		{
			if (!first)
			{
				sql.append(" AND ");
			}
			else
			{
				first = false;
			}
			appendColumnData(sql, col.getIdentifier().getColumnName(), col);
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
		appendColumnData(sql, childColumn, data);
	}

	private boolean isExpression(ColumnData data)
	{
		if (data == null) return false;
		if (data.getIdentifier() == null) return false;

		Object value = data.getValue();
		if (value == null)
		{
			return false;
		}

		if (value instanceof String)
		{
			String s = (String)value;
			SQLLexer lexer = new SQLLexer(s);
			SQLToken first = lexer.getNextToken(false, false);
			if (first.isNumberLiteral() || first.isLiteral())
			{
				return false;
			}
			return true;
		}
		return false;
	}

	private void appendColumnData(StringBuilder sql, String column, ColumnData data)
	{
		sql.append(connection.getMetadata().quoteObjectname(column));
		if (data == null || data.isNull())
		{
			sql.append(" IS NULL");
		}
		else if (isExpression(data))
		{
			sql.append(' ');
			sql.append(data.getValue());
		}
		else
		{
			sql.append(" = ");
			sql.append(formatter.getDefaultLiteral(data));
		}
	}

	public void startGenerate()
	{
		ObjectScripterUI ui = new ObjectScripterUI(this);
		ui.show(WbManager.getInstance().getCurrentWindow());
	}

	@Override
	public void setProgressMonitor(ScriptGenerationMonitor aMonitor)
	{
		this.monitor = aMonitor;
	}

	@Override
	public String getScript()
	{
		if (this.statements.isEmpty())
		{
			this.generateScript();
		}
		StringBuilder script = new StringBuilder();

		for (String dml : statements)
		{
			script.append(dml);
			script.append(";\n\n");
		}

		return script.toString();
	}

	public CharSequence getScriptForValues(List<ColumnData> values)
		throws SQLException
	{
		this.statements.clear();
		this.setValues(values);
		this.createStatements(true);
		return getScript();
	}

	public List<String> getStatementsForValues(List<ColumnData> values, boolean includeRoot)
	{
		this.statements.clear();
		this.setValues(values);
		this.createStatements(includeRoot);
		return Collections.unmodifiableList(statements);
	}

	@Override
	public void generateScript()
	{
		if (this.sourceTable == null) return;

		DataStore ds = this.sourceTable.getDataStore();
		if (ds == null) return;

		int[] rows = this.sourceTable.getSelectedRows();
		if (rows.length == 0)
		{
			return;
		}

		if (this.connection.isBusy())
		{
			Exception e = new Exception("Connection is busy");
			LogMgr.logError("DeleteScriptGenerator.generateScript()", "Connection is busy!", e);
		}

		ds.checkUpdateTable();
		TableIdentifier tbl = ds.getUpdateTable();

		int numRows = rows.length;

		try
		{
			connection.setBusy(true);
			this.setTable(tbl);

			for (int i=0; i < numRows; i++)
			{
				List<ColumnData> pkvalues = ds.getPkValues(rows[i]);
				this.setValues(pkvalues);
				if (monitor != null) this.monitor.setCurrentObject(ResourceMgr.getString("MsgGeneratingScriptForRow"), i+1, numRows);
				this.createStatements(true);
			}
		}
		catch (Exception e)
		{
			LogMgr.logError("SqlPanel.generateDeleteScript", "Error generating delete script", e);
		}
		finally
		{
			connection.setBusy(false);
		}
	}
}