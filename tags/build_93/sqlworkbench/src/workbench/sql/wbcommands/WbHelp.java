/*
 * WbHelp.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2007, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.sql.wbcommands;

import java.sql.SQLException;
import workbench.db.WbConnection;
import workbench.gui.actions.ShowHelpAction;
import workbench.sql.SqlCommand;
import workbench.sql.StatementRunnerResult;

/**
 *
 * @author  support@sql-workbench.net
 */
public class WbHelp extends SqlCommand
{
	public WbHelp()
	{
	}

	public String getVerb() { return "HELP"; }

	protected boolean isConnectionRequired() { return false; }
	
	public StatementRunnerResult execute(WbConnection aConnection, String aSql)
		throws SQLException
	{
		StatementRunnerResult result = new StatementRunnerResult();
		result.setSuccess();
		ShowHelpAction.getInstance().showHelp();
		return result;
	}

}
