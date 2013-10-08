/*
 * WbGenInsert.java
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
package workbench.sql.wbcommands;

import java.sql.SQLException;
import java.util.List;

import workbench.interfaces.ScriptGenerationMonitor;
import workbench.resource.ResourceMgr;

import workbench.db.DummyInsert;
import workbench.db.TableIdentifier;
import workbench.db.WbConnection;
import workbench.db.importer.TableDependencySorter;

import workbench.storage.RowActionMonitor;

import workbench.sql.SqlCommand;
import workbench.sql.StatementRunnerResult;

import workbench.util.ArgumentParser;
import workbench.util.ArgumentType;
import workbench.util.CollectionUtil;
import workbench.util.SqlUtil;

/**
 * A SqlCommand to create a script of INSERT statements for a list of tables respecting FK constraints.
 *
 * @author Thomas Kellerer
 */
public class WbGenInsert
	extends SqlCommand
	implements ScriptGenerationMonitor
{
	String test = "wbgenerateinsert -tables=T995_COUNTRY_PARAMETERS,T994_WAREHOUSE_PARAMETERS,T982_COUNTRY_DIVISIONS,T012_WH_EXT,T013_ORDERAREAS,T015_STORES,T016_STORE_OPEN_HOURS,T040_ITEMGRP_INT,T041_ITEMGRP_INT_DESCR,T042_MAINITEMGRP,T043_MAINITEMGRP_DESCR,T044_SUBITEMGRP,T045_SUBITEMGRP_DESCR,T046_ITEMFAMILIES,T047_ITEMFAMILY_DESCR,T050_VAT_RATES,T051_VAT_DESCR,T060_ITEMS_EXT,T061_ITEM_DETAILS_WH,T065_ITEM_DESCR,T087_ITEMSALE_HOURS,T102_ADVREGIONS,T104_ADVREGION_STORE_REL,T105_ADVTYPES,T110_ASSORTMENTAREAS,T112_ASSORTMENTGROUPS,T113_ASSORTMENT_ITEM_REL,T114_ASSORTMENT_STORE_REL,T301_ADVDATES_FOOD,T303_ADVITEMS_FOOD,T311_ADVITEMSALE_HOURS,T500_BAKINGAREAS,T501_BAKING_MACHINES,T502_BAKING_PROGRAMS,T503_BAKING_CLUSTERS,T506_TIME_WINDOWS,T510_STORE_PARAMETERS,T511_STORE_CLUSTER,T512_STORE_ETLS,T520_ITEM_PARAMETERS,T530_STORE_ITEM_PARAMETERS,T531_STORE_ITEM_ETLS,T560_ITEM_SELLING_UNIT";
	public static final String VERB = "WBGENERATEINSERT";

	public static final String PARAM_TABLES = "tables";
	public static final String PARAM_FULL_INSERT = "fullInsert";

	private TableDependencySorter tableSorter;

	public WbGenInsert()
	{
		super();
		this.isUpdatingCommand = true;
		cmdLine = new ArgumentParser();
		cmdLine.addArgument(PARAM_TABLES, ArgumentType.TableArgument);
		cmdLine.addArgument(PARAM_FULL_INSERT, ArgumentType.BoolArgument);
	}

	@Override
	public StatementRunnerResult execute(String sql)
		throws SQLException, Exception
	{
		StatementRunnerResult result = new StatementRunnerResult();
		String args = getCommandLine(sql);
		cmdLine.parse(args);

		if (cmdLine.hasUnknownArguments())
		{
			setUnknownMessage(result, cmdLine, ResourceMgr.getString("ErrGenDeleteWrongParam"));
			result.setFailure();
			return result;
		}

		if (!cmdLine.hasArguments())
		{
			result.addMessage(ResourceMgr.getString("ErrGenDropWrongParam"));
			result.setFailure();
			return result;
		}

		String names = cmdLine.getValue(PARAM_TABLES);
		boolean fullInsert = cmdLine.getBoolean(PARAM_FULL_INSERT, false);
		SourceTableArgument tableArgs = new SourceTableArgument(names, currentConnection);

		List<TableIdentifier> tables = tableArgs.getTables();

		if (CollectionUtil.isEmpty(tables))
		{
			result.addMessage(ResourceMgr.getFormattedString("ErrTableNotFound", names));
			result.setFailure();
			return result;
		}

		tableSorter = new TableDependencySorter(this.currentConnection);

		if (this.rowMonitor != null)
		{
			rowMonitor.setMonitorType(RowActionMonitor.MONITOR_PROCESS_TABLE);
			tableSorter.setProgressMonitor(this);
		}

		List<TableIdentifier> sorted = tableSorter.sortForInsert(tables);

		if (this.rowMonitor != null)
		{
			rowMonitor.jobFinished();
		}

		if (this.isCancelled)
		{
			result.addMessageByKey("MsgStatementCancelled");
		}
		else
		{
			if (!fullInsert)
			{
				result.addMessageByKey("MsgInsertSeq");
				result.addMessageNewLine();
			}

			for (TableIdentifier table : sorted)
			{
				if (fullInsert)
				{
					DummyInsert insert = new DummyInsert(table);
					insert.setFormatSql(false);
					String source = insert.getSource(currentConnection).toString();
					result.addMessage(SqlUtil.makeCleanSql(source,false) + ";");
				}
				else
				{
					result.addMessage("    " + table.getTableExpression());
				}
				result.setSuccess();
			}
		}
		return result;
	}


	@Override
	public void cancel()
		throws SQLException
	{
		super.cancel();
		if (tableSorter != null)
		{
			tableSorter.cancel();
		}
	}

	@Override
	public void done()
	{
		super.done();
		tableSorter = null;
	}

	@Override
	public boolean isUpdatingCommand(WbConnection con, String sql)
	{
		return false;
	}

	@Override
	public String getVerb()
	{
		return VERB;
	}

	@Override
	public void setCurrentObject(String anObject, int current, int count)
	{
		if (this.rowMonitor != null)
		{
			if (anObject.indexOf(' ') > -1)
			{
				try
				{
					rowMonitor.saveCurrentType("genDel");
					rowMonitor.setMonitorType(RowActionMonitor.MONITOR_PLAIN);
					rowMonitor.setCurrentObject(anObject, current, count);
				}
				finally
				{
					rowMonitor.restoreType("genDel");
				}
			}
			else
			{
				rowMonitor.setCurrentObject(anObject, current, count);
			}
		}
	}

}
