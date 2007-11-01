/*
 * HsqlShutdownHook.java
 * 
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 * 
 * Copyright 2002-2007, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author.
 * 
 * To contact the author please send an email to: support@sql-workbench.net
 */

package workbench.db.shutdown;

import java.sql.SQLException;
import java.sql.Statement;
import workbench.db.ConnectionMgr;
import workbench.db.WbConnection;
import workbench.log.LogMgr;
import workbench.util.SqlUtil;

/**
 *
 * @author support@sql-workbench.net
 */
public class HsqlShutdownHook
	implements DbShutdownHook
{

	private boolean canShutdown(WbConnection con)
	{
		String url = con.getUrl();
		if (url == null) return true;
		
		// this is a HSQL server connection. Do not shut down this!
		if (url.startsWith("jdbc:hsqldb:hsql:")) return false;
		
		return true;
	}
	
	public void shutdown(WbConnection con)
		throws SQLException
	{
		if (con == null) return;
		
		boolean otherActive = ConnectionMgr.getInstance().isActive(con);
		if (otherActive) return;

		if (canShutdown(con))
		{
			Statement stmt = null;
			try
			{
				stmt = con.createStatement();
				LogMgr.logInfo("HsqlShutdownHook.shutdown()", "Local HSQL connection detected. Sending SHUTDOWN to the engine before disconnecting");
				stmt.executeUpdate("SHUTDOWN");
			}
			catch (Exception e)
			{
				LogMgr.logWarning("HsqlShutdownHook.shutdown()", "Error when executing SHUTDOWN", e);
			}
			finally
			{
				SqlUtil.closeStatement(stmt);
			}
		}
		con.close();
	}

	/**
	 *	Disconnects a local HSQL connection. Beginning with 1.7.2 the local
	 *  (=in process) engine should be closed down with SHUTDOWN when
	 *  disconnecting. It shouldn't hurt for pre-1.7.2 either :-)
	 */
	private void shutdownHsql(WbConnection con)
	{
		
	}
	

	
}
