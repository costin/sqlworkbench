/*
 * ConnectionMgr.java
 *
 * Created on November 25, 2001, 4:18 PM
 */

package workbench.db;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;

import workbench.resource.ResourceMgr;
import workbench.log.LogMgr;
import workbench.exception.NoConnectionException;
import java.sql.DriverManager;

/**
 *
 * @author  thomas
 * @version
 */
public class ConnectionMgr
{
	private HashMap connections;

	/** Creates new ConnectionMgr */
	public ConnectionMgr()
	{
		this.connections = new HashMap(10);
	}
	
	/**	
	 *	Return a connection for the given ID.
	 *	A NoConnectException is thrown if no connection
	 *	is found for that ID
	 */
	public WbConnection getConnection(String anId)
		throws NoConnectionException
	{
		return this.getConnection(anId, false);
	}
	
	/**
	 *	Return the connection identified by the given id.
	 *	Typically the ID is the ID of the MainWindow requesting
	 *	the connection. 
	 *	If no connection is found with that ID and the selectWindow 
	 *	parameter is set to true, the connection dialog
	 *	is displayed.
	 *	If still no connection is found a NoConnectionException is thrown
	 *	If a connection is created then it will be stored together
	 *	with the given ID.
	 *
	 *	@param ID the id for the connection 
	 *	@param showSelectWindow if true show the connection window
	 *	@throws NoConnectionException
	 *	@see workbench.gui.MainWindow#getWindowId()
	 *	@see #releaseConnection(String)
	 *	@see #disconnectAll()
	 */
	public WbConnection getConnection(String anId, boolean showSelectWindow)
		throws NoConnectionException
	{
		WbConnection conn = (WbConnection)this.connections.get(anId);
		if (conn == null && showSelectWindow)
		{
			conn = this.selectConnection();
			if (conn != null) this.connections.put(anId, conn);
		}
		if (conn == null)
		{
			throw new NoConnectionException(ResourceMgr.getString(ResourceMgr.ERROR_NO_CONNECTION_AVAIL));
		}
		return conn;
	}
	
	public WbConnection selectConnection()
	{
		WbConnection result = null;
		
		try
		{
			Class.forName("org.postgresql.Driver");
			String url = "jdbc:postgresql:wbtest";
			String user = "thomas";

			Connection conn  = DriverManager.getConnection(url, user, "");
			result = new WbConnection(conn);
		}
		catch (Exception e)
		{
			LogMgr.logError(this, "Error creating connection", e);
		}
		return result;
		
	}
	
	
	/**
	 *	Reads a connection from the applications settings.
	 *	The connection is is identified by the given name and is
	 *	assigned to the given id (=MainWindow)
	 */
	public WbConnection getNamedConnection(String anId, String aConnectionName)
	{
		return null;
	}
	
	/**
	 *	Disconnects all connections
	 */
	public void disconnectAll()
	{
		Iterator itr = this.connections.keySet().iterator();
		while (itr.hasNext())
		{
			String key = itr.next().toString();
			this.disconnect(key);
			this.connections.put(key, null);
		}
	}
	
	/**
	 *	Disconnect the connection with the given key
	 */
	public void disconnect(String anId)
	{
		try
		{
			Connection con = (Connection)this.connections.get(anId);
			if (con != null) con.close();
		}
		catch (Exception e)
		{
			LogMgr.logError(this, ResourceMgr.getString(ResourceMgr.ERROR_DISCONNECT) + " " + anId, e);
		}
	}
	public String toString()
	{
		return this.getClass().getName();
	}
}
