/*
 * WbGrep
 *
 *  This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 *  Copyright 2002-2009, Thomas Kellerer
 *  No part of this code maybe reused without the permission of the author
 *
 *  To contact the author please send an email to: support@sql-workbench.net
 */
package workbench.sql.wbcommands;

import java.sql.SQLException;
import java.util.List;
import workbench.db.TableIdentifier;
import workbench.db.search.ClientSideTableSearcher;
import workbench.interfaces.TableSearchConsumer;
import workbench.resource.ResourceMgr;
import workbench.sql.SqlCommand;
import workbench.sql.StatementRunnerResult;
import workbench.storage.DataStore;
import workbench.storage.RowActionMonitor;
import workbench.storage.filter.ColumnComparator;
import workbench.storage.filter.ContainsComparator;
import workbench.storage.filter.RegExComparator;
import workbench.storage.filter.StartsWithComparator;
import workbench.storage.filter.StringEqualsComparator;
import workbench.util.ArgumentParser;
import workbench.util.CollectionUtil;
import workbench.util.StringUtil;

/**
 * A class to search for text in all columns of all tables.
 * 
 * @author Thomas Kellerer
 */
public class WbSearchData
	extends SqlCommand
	implements TableSearchConsumer
{
	public static final String VERB = "WBSEARCHDATA";
	public static final String PARAM_TABLES = "tables";
	public static final String PARAM_EXCLUDE_TABLES = "excludeTables";
	public static final String PARAM_EXPRESSION = "searchValue";

	public static final String PARAM_COMPARATOR = "compareType";
	
	private ClientSideTableSearcher searcher;
	private StatementRunnerResult searchResult;
	private int foundTables;
	private List<String> searchedTables;
	
	public WbSearchData()
	{
		super();
		this.isUpdatingCommand = false;

		cmdLine = new ArgumentParser();
		cmdLine.addArgument(PARAM_TABLES);
		cmdLine.addArgument(PARAM_EXCLUDE_TABLES);
		cmdLine.addArgument(PARAM_EXPRESSION);
		cmdLine.addArgument(PARAM_COMPARATOR, CollectionUtil.arrayList("equals", "startsWith", "contains", "matches"));
	}

	@Override
	public String getVerb()
	{
		return VERB;
	}

	@Override
	public StatementRunnerResult execute(String sql)
		throws SQLException
	{
		searchResult = new StatementRunnerResult();
		String args = getCommandLine(sql);
		cmdLine.parse(args);

		if (cmdLine.hasUnknownArguments())
		{
			setUnknownMessage(searchResult, cmdLine, ResourceMgr.getString("ErrDataSearchWrongParms"));
			searchResult.setFailure();
			return searchResult;
		}
		
		String searchValue = cmdLine.getValue(PARAM_EXPRESSION);
		if (StringUtil.isBlank(searchValue))
		{
			searchResult.addMessage(ResourceMgr.getString("ErrDataSearchValueReq"));
			searchResult.addMessage(ResourceMgr.getString("ErrDataSearchWrongParms"));
			searchResult.setFailure();
			return searchResult;
		}
		
		String tableNames = cmdLine.getValue(PARAM_TABLES);
		String excludeTables = cmdLine.getValue(PARAM_EXCLUDE_TABLES);
		List<TableIdentifier> tables = null;

		if (StringUtil.isNonBlank(tableNames))
		{
			SourceTableArgument parser = new SourceTableArgument(tableNames, excludeTables, currentConnection);
			tables = parser.getTables();
		}
		else
		{
			String schema = currentConnection.getCurrentSchema();
			tables = currentConnection.getMetadata().getSelectableObjectsList(schema);
			if (StringUtil.isNonBlank(excludeTables))
			{
				SourceTableArgument parser = new SourceTableArgument(excludeTables, currentConnection);
				tables.removeAll(parser.getTables());
			}
		}

		searcher = new ClientSideTableSearcher();
		searcher.setConnection(currentConnection);
		searcher.setTableNames(tables);
		String comparatorType = cmdLine.getValue(PARAM_COMPARATOR);
		if (StringUtil.isBlank(comparatorType))
		{
			comparatorType = "contains";
		}
		ColumnComparator comp = null;
		if ("equals".equalsIgnoreCase(comparatorType))
		{
			comp = new StringEqualsComparator();
		}
		else if ("startsWith".equalsIgnoreCase(comparatorType))
		{
			comp = new StartsWithComparator();
		}
		else if ("matches".equalsIgnoreCase(comparatorType))
		{
			comp = new RegExComparator();
		}
		else
		{
			comp = new ContainsComparator();
		}
		searcher.setComparator(comp);
		searcher.setConsumer(this);
		searcher.setCriteria(searchValue);

		if (rowMonitor != null)
		{
			rowMonitor.setMonitorType(RowActionMonitor.MONITOR_PROCESS);
		}

		searchResult.setSuccess();
		searcher.search();

		StringBuilder summary = new StringBuilder(searchedTables.size() * 20);
		summary.append(ResourceMgr.getString("MsgSearchedTables"));
		for (String table : searchedTables)
		{
			summary.append("\n  ");
			summary.append(table);
		}
		summary.append('\n');
		searchResult.addMessage(summary.toString());
		String msg = ResourceMgr.getFormattedString("MsgSearchDataFinished", searchedTables.size(), foundTables);
		searchResult.addMessage(msg);

		return searchResult;
	}

	@Override
	public void cancel()
		throws SQLException
	{
		super.cancel();
		if (searcher != null)
		{
			searcher.cancelSearch();
		}
	}

	@Override
	public void done()
	{
		super.done();
		searcher = null;
	}

	public void setCurrentTable(String tableName, String query)
	{
		if (rowMonitor != null)
		{
			rowMonitor.setCurrentObject(tableName, -1, -1);
		}
	}

	public void error(String msg)
	{
		searchResult.addMessage(msg);
		searchResult.setFailure();
	}

	public void tableSearched(TableIdentifier table, DataStore result)
	{
		searchedTables.add(table.getTableName());
		if (result != null && result.getRowCount() > 0)
		{
			result.resetStatus();
			result.setGeneratingFilter(searcher.getSearchExpression());
			searchResult.addDataStore(result);
			foundTables ++;
		}
	}

	public void setStatusText(String message)
	{
		if (rowMonitor != null)
		{
			rowMonitor.setCurrentObject(message, -1, -1);
		}
	}

	public void searchStarted()
	{
		if (rowMonitor != null)
		{
			rowMonitor.jobFinished();
		}
		searchedTables = CollectionUtil.sizedArrayList(50);
		foundTables = 0;
	}

	public void searchEnded()
	{
		if (rowMonitor != null)
		{
			rowMonitor.jobFinished();
		}
	}

}
