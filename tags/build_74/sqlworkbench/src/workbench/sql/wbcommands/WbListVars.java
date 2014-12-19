/*
 * WbListVars.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2004, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: info@sql-workbench.net
 *
 */
package workbench.sql.wbcommands;

import java.sql.SQLException;

import workbench.db.WbConnection;
import workbench.sql.SqlCommand;
import workbench.sql.SqlParameterPool;
import workbench.sql.StatementRunnerResult;

/**
 *
 * @author  info@sql-workbench.net
 */
public class WbListVars extends SqlCommand
{
	public WbListVars()
	{
	}

	public String getVerb() { return "WBVARLIST"; }

	public StatementRunnerResult execute(WbConnection aConnection, String aSql)
		throws SQLException
	{
		StatementRunnerResult result = new StatementRunnerResult(getVerb());
		result.addDataStore(SqlParameterPool.getInstance().getVariablesDataStore());
		result.setSuccess();
		return result;
	}

}