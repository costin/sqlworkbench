/*
 * ConnectionMgr.java
 *
 * Created on November 25, 2001, 4:18 PM
 */

package workbench.db;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import workbench.exception.NoConnectionException;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.WbManager;
import workbench.exception.WbException;
import workbench.gui.dbobjects.DbExplorerWindow;
import workbench.util.WbPersistence;

/**
 *
 * @author  thomas
 * @version
 */
public class ConnectionMgr
{
	private WbConnection currentConnection;
	private Map profiles;
	private List drivers;
	
	/** Creates new ConnectionMgr */
	public ConnectionMgr()
	{
	}
	
	/**
	 *	Return a new connection specified by the profile, for the
	 *	given window id
	 */
	public WbConnection getConnection(ConnectionProfile aProfile)
		throws ClassNotFoundException, SQLException, NoConnectionException
	{
		this.disconnect();
		
		String drvName = aProfile.getDriverclass();
		DbDriver drv = this.findDriver(drvName);
		if (drv == null)
		{
			throw new NoConnectionException("Driver class not registered");
		}
		
		WbConnection conn = new WbConnection();
		Connection sql;
		// The DriverManager refuses to use a driver which was not loaded
		// from the system classloader, so the connection has to be 
		// established directly from the driver.
		try
		{
			sql = drv.connect(aProfile.getUrl(), aProfile.getUsername(), aProfile.decryptPassword());
		
			try
			{
				sql.setAutoCommit(aProfile.getAutocommit());
			}
			catch (Throwable th)
			{
				// some drivers do not support this, so
				// we just ignore the error :-)
			}
			conn.setSqlConnection(sql);
			this.currentConnection = conn;
		}
		catch (WbException e)
		{
			throw new NoConnectionException(e.getMessage());
		}
		
		return conn;
	}

	private DbDriver findDriver(String drvName)
	{
		if (this.drivers == null)
		{
			this.readDrivers();
		}
		DbDriver db = null;
		for (int i=0; i < this.drivers.size(); i ++)
		{
			db = (DbDriver)this.drivers.get(i);
			if (db.getDriverClass().equals(drvName)) return db;
		}
		if (db == null)
		{
			// maybe it's present in the normal classpath...
			try
			{
				Class drvcls = Class.forName(drvName);
				Driver drv = (Driver)drvcls.newInstance();
				db = new DbDriver(drv);
			}
			catch (Exception cnf)
			{
				db = null;
			}
		}
		return db;
	}
	
	/**
	 *	Returns a List of registered drivers.
	 *	This list is read from the workbench.settings file
	 */
	public List getDrivers()
	{
		if (this.drivers == null)
		{
			this.readDrivers();
		}
		return this.drivers;
	}

	public void setDrivers(List aDriverList)
	{
		this.drivers = aDriverList;
	}
	
	public Map getProfiles()
	{
		if (this.profiles == null) this.readProfiles();
		return this.profiles;
	}

	public String getDisplayString()
	{
		return getDisplayString(this.currentConnection);
	}
	
	public static String getDisplayString(WbConnection con)
	{
		try
		{
			return getDisplayString(con.getSqlConnection());
		}
		catch (Exception e)
		{
			LogMgr.logError("ConnectionMgr", "getDisplayString() - No java.sql.Connection!", e);
			return "n/a";
		}
	}
	/**
	 *	Return a readable display of a connection
	 */
	public static String getDisplayString(Connection con)
	{
		String displayString = null;
		
		try
		{
			DatabaseMetaData data = con.getMetaData();
			StringBuffer buff = new StringBuffer();
			buff.append("User=");
			buff.append(data.getUserName());
			
			String catName = data.getCatalogTerm();
			String catalog = con.getCatalog();
			if (catName == null) catName = "Catalog";
			if (catName != null && catName.length() > 0 &&
			    catalog != null && catalog.length() > 0)
			{
				buff.append(", ");
				buff.append(catName);
				buff.append('=');
				buff.append(catalog);
			}
			
			buff.append(", URL=");
			buff.append(data.getURL());
			displayString = buff.toString();
		}
		catch (Exception e)
		{
			LogMgr.logError("ConnectionMgr", "Could not retrieve connection information", e);
			displayString = "n/a";
		}
		return displayString;
	}

	/**
	 *	Disconnects all connections
	 */
	public void disconnectAll()
	{
		this.disconnect();
		/*
		Iterator itr = this.activeConnections.keySet().iterator();
		while (itr.hasNext())
		{
			String key = itr.next().toString();
			this.disconnect(key);
			this.activeConnections.put(key, null);
		}
		*/
	}
	
	/**
	 *	Disconnect the connection with the given key
	 */
	public void disconnect()
	{
		try
		{
			//WbConnection con = (WbConnection)this.activeConnections.get(anId);
			if (this.currentConnection != null) 
			{
				this.currentConnection.close();
				this.currentConnection = null;
			}
			//this.activeConnections.put(anId, null);
		}
		catch (Exception e)
		{
			LogMgr.logError(this, ResourceMgr.getString(ResourceMgr.ERROR_DISCONNECT), e);
		}
	}
	
	public String toString()
	{
		return this.getClass().getName();
	}
	
	public void writeSettings()
	{
		this.saveXmlProfiles();
		this.saveDrivers();
	}
	
	public void saveDrivers()
	{
		WbPersistence.writeObject(this.drivers, "WbDrivers.xml");
	}
	
	private void readDrivers()
	{
		try
		{
			Object result = WbPersistence.readObject("WbDrivers.xml");
			if (result == null)
			{
				InputStream in = this.getClass().getResourceAsStream("DriverTemplates.xml");
				result = WbPersistence.readObject(in);
			}
			
			if (result == null)
			{
				this.drivers = Collections.EMPTY_LIST;
			}
			else if (result instanceof Collection)
			{
				Iterator itr = ((Collection)result).iterator();
				this.drivers = new ArrayList();
				while (itr.hasNext())
				{
					DbDriver driv = (DbDriver)itr.next();
					this.drivers.add(driv);
				}
			}
		}
		catch (Exception e)
		{
			LogMgr.logWarning(this, "Could not load driver definitions!");
			this.drivers = Collections.EMPTY_LIST;
		}
	}

	public ResultSet getTableDefinition(String aTable)
	{
		if (this.currentConnection == null) return null;
		try
		{
			return this.currentConnection.getMetadata().getTableDefinition(aTable);
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	public void readProfiles()
	{
		this.readXmlProfiles();
	}

	public void saveXmlProfiles()
	{
		if (this.profiles != null)
		{
			WbPersistence.writeObject(new ArrayList(this.profiles.values()), "WbProfiles.xml");
		}
	}
	
	public void setProfiles(Collection c)
	{
		Iterator itr = ((Collection)c).iterator();
		if (this.profiles == null)
		{
			this.profiles = new HashMap();
		}
		else
		{
			this.profiles.clear();
		}
		while (itr.hasNext())
		{
			ConnectionProfile prof = (ConnectionProfile)itr.next();
			this.profiles.put(prof.getName(), prof);
		}
	}
	
	
	public void readXmlProfiles()
	{
		Object result = WbPersistence.readObject("WbProfiles.xml");
		if (result instanceof Collection)
		{
			this.setProfiles((Collection)result);
		}
		else if (result instanceof Object[])
		{
			Object[] l = (Object[])result;
			this.profiles = new HashMap(20);
			for (int i=0; i < l.length; i++)
			{
				ConnectionProfile prof = (ConnectionProfile)l[i];
				this.profiles.put(prof.getName(), prof);
			}
		}
	}

}
