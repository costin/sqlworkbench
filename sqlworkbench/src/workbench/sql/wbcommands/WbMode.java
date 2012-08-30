/*
 * WbMode.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2012, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.sql.wbcommands;

import java.sql.SQLException;
import workbench.db.ConnectionProfile;
import workbench.resource.ResourceMgr;
import workbench.sql.SqlCommand;
import workbench.sql.StatementRunnerResult;
import workbench.sql.formatter.SQLLexer;
import workbench.sql.formatter.SQLToken;
import workbench.util.ArgumentParser;

/**
 * A SQL Statement to change the read only mode of the current profile.
 *
 * @author Thomas Kellerer
 */
public class WbMode
	extends SqlCommand
{
	public static final String VERB = "WBMODE";

	public WbMode()
	{
		super();
		isUpdatingCommand = false;

		// Support auto-completion of parameters
		cmdLine = new ArgumentParser(false);
		cmdLine.addArgument("status");
		cmdLine.addArgument("readonly");
		cmdLine.addArgument("normal");
		cmdLine.addArgument("reset");
	}

	@Override
	public String getVerb()
	{
		return VERB;
	}

	@Override
	protected boolean isConnectionRequired()
	{
		return true;
	}

	@Override
	public StatementRunnerResult execute(String sql)
		throws SQLException
	{
		StatementRunnerResult result = new StatementRunnerResult();
		result.setSuccess();

		ConnectionProfile profile = (currentConnection != null ? currentConnection.getProfile() : null);
		if (profile == null)
		{
			result.setFailure();
			result.addMessageByKey("TxtNotConnected"); // should not happen
			return result;
		}

		String command = null;
		try
		{
			SQLLexer l = new SQLLexer(sql);
			SQLToken t = l.getNextToken(false, false); // ignore the verb
			t = l.getNextToken(false, false);
			if (t != null)
			{
				command = t.getContents();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		if (command == null)
		{
			result.addMessageByKey("ErrModeWrongArgs");
			result.setFailure();
			return result;
		}

		if (command.equalsIgnoreCase("reset"))
		{
			currentConnection.resetSessionFlags();
		}
		else if (command.equalsIgnoreCase("status"))
		{
			// only trigger display of current state
			result.setSuccess();
		}
		else if (command.equalsIgnoreCase("normal"))
		{
			currentConnection.setSessionConfirmUpdate(false);
			currentConnection.setSessionReadOnly(false);
		}
		else if (command.equalsIgnoreCase("readonly"))
		{
			currentConnection.setSessionReadOnly(true);
		}
		else if (command.equalsIgnoreCase("confirm"))
		{
			currentConnection.setSessionConfirmUpdate(true);
		}
		else
		{
			result.setFailure();
		}

		if (result.isSuccess())
		{
			result.addMessage(ResourceMgr.getFormattedString("MsgModeSession", currentConnection.isSessionReadOnly(), currentConnection.confirmUpdatesInSession()));
		}
		else
		{
			result.addMessageByKey("ErrModeWrongArgs");
		}
		return result;
	}
}
