/*
 * WbDescribeTable.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2008, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.sql.wbcommands;

import java.sql.SQLException;
import workbench.db.TableIdentifier;

import workbench.resource.ResourceMgr;
import workbench.sql.SqlCommand;
import workbench.sql.StatementRunnerResult;
import workbench.storage.ColumnRemover;
import workbench.storage.DataStore;
import workbench.util.SqlUtil;

/**
 *
 * @author  support@sql-workbench.net
 */
public class WbDescribeTable
	extends SqlCommand
{
	public static final String VERB = "DESC";
	public static final String VERB_LONG = "DESCRIBE";

	@Override
	public String getVerb()
	{
		return VERB;
	}

	@Override
	public String getAlternateVerb()
	{
		return VERB_LONG;
	}

	@Override
	public StatementRunnerResult execute(String sql)
		throws SQLException
	{
		StatementRunnerResult result = new StatementRunnerResult();
		String table = SqlUtil.stripVerb(SqlUtil.makeCleanSql(sql, false, false, '\''));

		TableIdentifier tbl = new TableIdentifier(table);

		DataStore ds = currentConnection.getMetadata().getTableDefinition(tbl);
		if (ds == null || ds.getRowCount() == 0)
		{
			result.setFailure();
			String msg = ResourceMgr.getString("ErrTableOrViewNotFound");
			msg = msg.replace("%name%", table);
			result.addMessage(msg);
		}
		else
		{
			ColumnRemover remover = new ColumnRemover(ds);
			DataStore cols = remover.removeColumnsByName("java.sql.Types", "SCALE/SIZE", "PRECISION", "POSITION");
			result.setSuccess();
			result.addDataStore(cols);
		}
		return result;
	}

}
