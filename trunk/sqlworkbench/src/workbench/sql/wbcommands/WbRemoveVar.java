/*
 * WbRemoveVar.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2006, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.sql.wbcommands;

import java.sql.SQLException;

import workbench.db.WbConnection;
import workbench.resource.ResourceMgr;
import workbench.sql.SqlCommand;
import workbench.sql.VariablePool;
import workbench.sql.StatementRunnerResult;

/**
 *
 * @author  support@sql-workbench.net
 */
public class WbRemoveVar extends SqlCommand
{
	public static final String VERB = "WBVARDELETE";
	public WbRemoveVar()
	{
	}

	public String getVerb() { return VERB; }
	protected boolean isConnectionRequired() { return false; }

	public StatementRunnerResult execute(WbConnection aConnection, String aSql)
		throws SQLException
	{
		StatementRunnerResult result = new StatementRunnerResult();
		String var = stripVerb(aSql);

		String msg = null;

		if (var == null || var.length() == 0)
		{
			result.addMessage(ResourceMgr.getString("ErrVarRemoveWrongParameter"));
			result.setFailure();
			return result;
		}
		else
		{
			boolean removed = VariablePool.getInstance().removeValue(var);
			if (removed)
			{
				msg = ResourceMgr.getString("MsgVarDefVariableRemoved");
			}
			else
			{
				msg = ResourceMgr.getString("MsgVarDefVariableNotRemoved");
			}
			msg = msg.replaceAll("%var%", var);
		}

		result.addMessage(msg);
		result.setSuccess();

		return result;
	}

}
