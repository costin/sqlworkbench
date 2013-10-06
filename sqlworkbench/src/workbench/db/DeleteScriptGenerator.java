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

import java.io.File;
import java.io.FileWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

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

import workbench.util.AggregatingMap;
import workbench.util.CollectionUtil;
import workbench.util.FileUtil;
import workbench.util.SqlUtil;
import workbench.util.StringUtil;

/**
 * Generates a SQL script to delete a record from the given table and
 * any dependent tables.
 *
 * @author  Thomas Kellerer
 */
public class DeleteScriptGenerator
	implements Scripter
{
	private final WbConnection connection;
	private List<ColumnData> columnValues;
	private TableDependency dependency;
	private final DbMetadata meta;
	private TableIdentifier rootTable;
	private WbTable sourceTable;
	private ScriptGenerationMonitor monitor;
	private final List<String> statements = new LinkedList<String>();
	private final SqlLiteralFormatter formatter;
	private boolean formatSql = true;
	private boolean showFkNames;

	private final Comparator<Integer> descComparator = new Comparator<Integer>()
		{
			@Override
			public int compare(Integer i1, Integer i2)
			{
				int val1 = i1.intValue();
				int val2 = i2.intValue();
				return (val1 < val2 ? 1 : (val1==val2 ? 0 : -1));
			}
		};

	private final Comparator<DependencyNode> levelSorter =new Comparator<DependencyNode>()
	{

		@Override
		public int compare(DependencyNode o1, DependencyNode o2)
		{
			return o1.getLevel() - o2.getLevel();
		}
	};

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

	public void setShowConstraintNames(boolean flag)
	{
		this.showFkNames = flag;
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
		this.dependency.setScriptMonitor(monitor);
		this.dependency.readDependencyTree(true);

		long calcStart = System.currentTimeMillis();

		Map<Integer, Set<DependencyNode>> levels = buildLevelsTopDown(dependency.getRootNode(), 1);

		if (this.monitor != null)
		{
			this.monitor.setCurrentObject(ResourceMgr.getFormattedString("MsgCalcDelDeps"), -1, -1);
		}
		long adjustStart = System.currentTimeMillis();
		int moved =	adjustLevels(levels);
		int loops = 1;
		while (moved > 0 && loops <= levels.size())
		{
			// additional iterations are necessary if a node was moved from one level to the next
			// in that case the new level hierarchy could mean that a node from the target level now needs
			// to be moved up in tur. But this can't be done in a single loop because otherwise adjustLevels
			// would generate a ConcurrentModificationException
			loops ++;
			moved = adjustLevels(levels);
		}
		long duration = System.currentTimeMillis() - adjustStart;
		LogMgr.logDebug("DeleteScriptGenerator.createStatements()", "Adjusting level hierarchy in " + loops + " iterations took: " + duration + "ms");

		long tableCount = 0;

		for (Map.Entry<Integer, Set<DependencyNode>> entry : levels.entrySet())
		{
			if (entry.getValue().size() > 0)
			{
				// collect all nodes for one table (on the current level) so that we can generate a single delete statement
				// that covers all foreign keys at once
				AggregatingMap<TableIdentifier, DependencyNode> tableNodes = new AggregatingMap<TableIdentifier, DependencyNode>(false);

				for (DependencyNode node : entry.getValue())
				{
					tableNodes.addValue(node.getTable(), node);
				}

				// The tables that are deleted on the same level also need to be sorted to avoid deleting from a table
				// that is used in the sub-select of another table later on.
				// the sum of the node levels gives an indication on which one has less dependencies
				// theoretically it could still mean that two tables that have the same number of dependencies
				// need a specific order, but this is too hard to detect...
				List<TableIdentifier> sorted = sortTables(tableNodes.getMap());
				tableCount += sorted.size();
				for (TableIdentifier tbl : sorted)
				{
					statements.add(createDeleteStatement(tbl, tableNodes.get(tbl)));
				}
			}
		}

		if (includeRoot)
		{
			DependencyNode root = this.dependency.getRootNode();
			StringBuilder rootSql = new StringBuilder(100);
			rootSql.append("DELETE FROM ");
			rootSql.append(root.getTable().getTableExpression(this.connection));
			rootSql.append("\nWHERE ");
			this.addRootTableWhere(rootSql);
			statements.add(formatSql(rootSql));
		}
		duration = System.currentTimeMillis() - calcStart;
		LogMgr.logDebug("DeleteScriptGenerator.createStatements()", "Generated " + statements.size() + " statements for " + tableCount + " tables in " + duration + "ms");
	}

	private List<TableIdentifier> sortTables(final Map<TableIdentifier, Set<DependencyNode>>  tables)
	{
		List<TableIdentifier> sorted = new ArrayList<TableIdentifier>(tables.keySet());

		final Comparator<TableIdentifier> levelComp = new Comparator<TableIdentifier>()
		{
			@Override
			public int compare(TableIdentifier o1, TableIdentifier o2)
			{
				return -1 * (getLevelTotal(o1) - getLevelTotal(o2));
			}

			private int getLevelTotal(TableIdentifier tbl)
			{
				Set<DependencyNode> nodes = tables.get(tbl);
				if (nodes == null) return 0;
				int sum = 0;
				for (DependencyNode node : nodes)
				{
					sum += node.getLevel();
				}
				return sum;
			}
		};
		Collections.sort(sorted, levelComp);
		return sorted;
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

	private String createDeleteStatement(TableIdentifier table, Set<DependencyNode> nodes)
	{
		if (table == null) return StringUtil.EMPTY_STRING;
		if (CollectionUtil.isEmpty(nodes)) return StringUtil.EMPTY_STRING;

		Set<DependencyNode> processed = new HashSet<DependencyNode>(nodes.size());
		StringBuilder sql = new StringBuilder(nodes.size() * 200);

		if (showFkNames)
		{
			for (DependencyNode node : nodes)
			{
				sql.append("-- ").append(node.getFkName()).append('\n');
			}
		}
		sql.append("DELETE FROM ");
		sql.append(table.getTableExpression(this.connection));
		sql.append(" \nWHERE");

		boolean first = true;
		for (DependencyNode node : nodes)
		{
			if (processed.contains(node)) continue;
			if (first)
			{
				first = false;
			}
			else
			{
				sql.append("\n   OR");
			}
			addParentWhere(sql, node);
			processed.add(node);
		}
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
					addParentWhere(sql, parent);
					sql.append("))");
				}
				else
				{
					sql.append(' ');
					addRootTableWhere(sql, parentColumn, column);
				}
				count ++;
			}
		}
		catch (Throwable th)
		{
			LogMgr.logError("DeleteScriptGenerator.addParentWhere()", "Error during script generation", th);
		}
	}

	private void addRootTableWhere(StringBuilder sql)
	{
		boolean first = true;
		for (ColumnData data : this.columnValues)
		{
			if (!first)
			{
				sql.append(" AND ");
			}
			else
			{
				first = false;
			}
			ColumnIdentifier col = data.getIdentifier();
			String colname;
			if (col.getDataType() == ColumnIdentifier.NO_TYPE_INFO)
			{
				colname = SqlUtil.quoteObjectname(col.getColumnName(), false);
			}
			else
			{
				colname = connection.getMetadata().quoteObjectname(col.getColumnName());
			}
			appendColumnData(sql, colname, data);
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
		childColumn = connection.getMetadata().quoteObjectname(childColumn);
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
		sql.append(column);
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

	private int adjustLevels(Map<Integer, Set<DependencyNode>> levels)
	{
		Map<DependencyNode, Integer> newLevels = new HashMap<DependencyNode, Integer>();

		for (Map.Entry<Integer, Set<DependencyNode>> entry : levels.entrySet())
		{
			Iterator<DependencyNode> itr = entry.getValue().iterator();
			while (itr.hasNext())
			{
				DependencyNode node = itr.next();
				int otherLevel = findTableDependentLevel(levels, node.getTable(), entry.getKey());
				if (otherLevel > 0)
				{
					if (otherLevel == entry.getKey() && otherLevel > 1)
					{
						otherLevel --;
						LogMgr.logTrace("DeleteScriptGenerator.adjustLevels()" , "Entry for table: " + node.getTable() + " (" + node.getFkName() + ") should be moved to the same level (" + entry.getKey() + "). Moving to " + otherLevel);
					}
					if (otherLevel != entry.getKey())
					{
						LogMgr.logTrace("DeleteScriptGenerator.adjustLevels()" , "Moving entry for table: " + node.getTable() + " (" + node.getFkName() + ") from level " + entry.getKey() + " to level " + otherLevel);
						newLevels.put(node, otherLevel);
						itr.remove();
					}
				}
			}
		}

		for (Map.Entry<DependencyNode, Integer> entry : newLevels.entrySet())
		{
			Set<DependencyNode> nodes = levels.get(entry.getValue());
			if (nodes != null)
			{
				nodes.add(entry.getKey());
			}
		}

		return newLevels.size();
	}

	private List<DependencyNode> sortLevelNodes(Set<DependencyNode> nodes)
	{
		List<DependencyNode> sorted = new ArrayList<DependencyNode>(nodes.size());
		Collections.sort(sorted, levelSorter);
		return sorted;
	}

	private int findTableDependentLevel(Map<Integer, Set<DependencyNode>> levels, TableIdentifier table, int startLevel)
	{
		for (Map.Entry<Integer, Set<DependencyNode>> entry : levels.entrySet())
		{
			if (startLevel < entry.getKey()) continue;
			for (DependencyNode node : entry.getValue())
			{
				DependencyNode parent = node.getParent();
				while (parent != null)
				{
					if (parent.getTable().equals(table))
					{
						return entry.getKey();
					}
					parent = parent.getParent();
				}
			}
		}
		return -1;
	}

	private Map<Integer, Set<DependencyNode>> buildLevelsTopDown(DependencyNode root, int level)
	{
		AggregatingMap<Integer, DependencyNode> map = new AggregatingMap<Integer, DependencyNode>(new TreeMap<Integer, Set<DependencyNode>>(descComparator));

		List<DependencyNode> children = root.getChildren();

		if (children.isEmpty())
		{
			return map.getMap();
		}

		Integer lvl = Integer.valueOf(level);
		for (DependencyNode child : children)
		{
			map.addValue(lvl, child);
		}

		for (DependencyNode child : children)
		{
			if (child.getChildren().size() > 0)
			{
				map.addAllValues(buildLevelsTopDown(child, level + 1));
			}
		}
		return map.getMap();
	}

	private void dumpTree(Map<Integer, Set<DependencyNode>> levels, String fname)
	{
		FileWriter writer = null;
		try
		{
			writer = new FileWriter(new File("c:/temp", fname));
			writer.append(this.rootTable.getTableExpression() + "\n");

			for (Map.Entry<Integer, Set<DependencyNode>> entry : levels.entrySet())
			{
//				Set<DependencyNode> sorted = new TreeSet<DependencyNode>(new Comparator<DependencyNode>()
//				{
//					@Override
//					public int compare(DependencyNode o1, DependencyNode o2)
//					{
//						return o1.getTable().getTableName().compareTo(o2.getTable().getTableName());
//					}
//				});
//				sorted.addAll(entry.getValue());

				writer.append(entry.getKey() + ":\n");
				for (DependencyNode node : entry.getValue())
				{
					writer.append("  " + node.getTable() + " (" + node.getFkName() + ")\n");
				}
			}
		}
		catch (Exception ex)
		{
			LogMgr.logDebug("dumpTree()", "error writing tree", ex);
		}
		finally
		{
			FileUtil.closeQuietely(writer);
		}
	}

}
