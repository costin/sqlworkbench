/*
 * WbListTables.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2014, Thomas Kellerer
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

import workbench.console.ConsoleSettings;
import workbench.console.RowDisplay;
import workbench.resource.ResourceMgr;

import workbench.db.TableIdentifier;

import workbench.storage.DataStore;

import workbench.sql.SqlCommand;
import workbench.sql.StatementRunnerResult;

import workbench.util.ArgumentParser;
import workbench.util.ArgumentType;
import workbench.util.StringUtil;

/**
 * List all tables available to the current user.
 * <br>
 * This is the same information as displayed in the DbExplorer's "Objects" tab.
 *
 * @see workbench.db.DbMetadata#getObjects(String, String, String, String[])
 * @author Thomas Kellerer
 */
public class WbListTables
	extends SqlCommand
{
	public static final String VERB = "WBLIST";

	public WbListTables()
	{
		cmdLine = new ArgumentParser();
		cmdLine.addArgument(CommonArgs.ARG_OBJECTS);
		cmdLine.addArgument(CommonArgs.ARG_TYPES, ArgumentType.ObjectTypeArgument);
		cmdLine.addArgument(CommonArgs.ARG_SCHEMA, ArgumentType.SchemaArgument);
		cmdLine.addArgument(CommonArgs.ARG_CATALOG, ArgumentType.CatalogArgument);
	}

	@Override
	public String getVerb()
	{
		return VERB;
	}

	@Override
	public StatementRunnerResult execute(String aSql)
		throws SQLException
	{
		String options = getCommandLine(aSql);

		String[] types = currentConnection.getMetadata().getTableTypesArray();

		StatementRunnerResult result = new StatementRunnerResult();
		ConsoleSettings.getInstance().setNextRowDisplay(RowDisplay.SingleLine);

		cmdLine.parse(options);

		String objects = options;
		String schema = null;
		String catalog = null;

		if (cmdLine.hasUnknownArguments())
		{
			result.addMessage(ResourceMgr.getString("ErrListWrongArgs"));
			result.setFailure();
			return result;
		}

		if (cmdLine.hasArguments())
		{

			objects = cmdLine.getValue("objects");

			List<String> typeList = cmdLine.getListValue(CommonArgs.ARG_TYPES);
			if (typeList.size() > 0)
			{
				types = StringUtil.toArray(typeList, true, true);
			}
			schema = cmdLine.getValue(CommonArgs.ARG_SCHEMA);
			catalog = cmdLine.getValue(CommonArgs.ARG_CATALOG);
		}

		if (StringUtil.isBlank(schema))
		{
			schema = currentConnection.getMetadata().getCurrentSchema();
		}

		if (StringUtil.isBlank(catalog))
		{
			catalog = currentConnection.getMetadata().getCurrentCatalog();
		}

		DataStore resultList = null;

		if (StringUtil.isBlank(objects))
		{
			objects = "%";
		}

		List<String> objectFilters = StringUtil.stringToList(objects, ",", true, true, false, true);

		for (String filter : objectFilters)
		{
			// Create a tableidentifier for parsing e.g. parameters
			// like -tables=public.*
			TableIdentifier tbl = new TableIdentifier(currentConnection.getMetadata().adjustObjectnameCase(filter), currentConnection);
			String tschema = tbl.getSchema();
			if (StringUtil.isBlank(tschema))
			{
				tschema = schema;
			}
			tschema = currentConnection.getMetadata().adjustSchemaNameCase(tschema);
			String tcatalog = tbl.getCatalog();
			if (StringUtil.isBlank(tcatalog))
			{
				tcatalog = catalog;
			}
			tcatalog = currentConnection.getMetadata().adjustObjectnameCase(tcatalog);

			String tname = tbl.getTableName();

			DataStore ds = currentConnection.getMetadata().getObjects(tcatalog, tschema, tname, types);
			if (resultList == null)
			{
				// first result retrieved
				resultList = ds;
			}
			else
			{
				// additional results retrieved, add them to the current result
				resultList.copyFrom(ds);
			}
		}

		if (resultList != null)
		{
			result.addDataStore(resultList);
		}
		return result;
	}

}
