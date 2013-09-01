/*
 * ConnectionMgr.java
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
package workbench.db;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import workbench.WbManager;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;

import workbench.db.objectcache.DbObjectCacheFactory;
import workbench.db.shutdown.DbShutdownFactory;
import workbench.db.shutdown.DbShutdownHook;

import workbench.gui.profiles.ProfileKey;

import workbench.util.CaseInsensitiveComparator;
import workbench.util.ExceptionUtil;
import workbench.util.FileUtil;
import workbench.util.FileVersioner;
import workbench.util.PropertiesCopier;
import workbench.util.WbFile;
import workbench.util.WbPersistence;
import workbench.util.WbThread;

/**
 * A connection factory for the application.
 *
 * @author  Thomas Kellerer
 */
public class ConnectionMgr
	implements PropertyChangeListener
{
	private Map<String, WbConnection> activeConnections = Collections.synchronizedMap(new HashMap<String, WbConnection>());

	private List<ConnectionProfile> profiles;
	private List<DbDriver> drivers;
	private boolean profilesChanged;
	private boolean readTemplates = true;
	private boolean templatesImported;
	private List<PropertyChangeListener> driverChangeListener;
	private static ConnectionMgr instance = new ConnectionMgr();

	private final Object driverLock = new Object();
	private final Object profileLock = new Object();

	private String currentFilename;

	private ConnectionMgr()
	{
		Settings.getInstance().addPropertyChangeListener(this, Settings.PROPERTY_PROFILE_STORAGE);
	}

	public static ConnectionMgr getInstance()
	{
		return instance;
	}

	/**
	 * Create a new connection.
	 *
	 * The profile to be used is searched by the given profile key
	 *
	 * @param def the profile to be used
	 * @param anId the id to be assigned to the connection
	 * @return a new Connection
	 *
	 * @throws java.lang.ClassNotFoundException
	 * @throws java.sql.SQLException
	 * @throws java.lang.Exception
	 */
	public WbConnection getConnection(ProfileKey def, String anId)
		throws ClassNotFoundException, SQLException, Exception
	{
		ConnectionProfile prof = this.getProfile(def);
		if (prof == null) return null;

		return this.getConnection(prof, anId);
	}

	/**
	 * Finds a connection based on the ID.
	 *
	 * For testing purposes only.
	 * @param id the id to find
	 * @return the connection, null if not found
	 */
	public WbConnection findConnection(String id)
	{
		return this.activeConnections.get(id);
	}

	public int getOpenCount()
	{
		return this.activeConnections.size();
	}

	public WbConnection getConnection(ConnectionProfile aProfile, String connId)
		throws ClassNotFoundException, SQLException, UnsupportedClassVersionError
	{
		if (this.activeConnections.containsKey(connId))
		{
			int count = getPrefixCount(connId) + 1;
			String newId = connId + "/" + Integer.toString(count);
			LogMgr.logWarning("ConnectionMgr.getConnection()", "A new connection for ID " + connId + " was requested, but there is already an active one with that ID! Using ID=" + newId + " instead.");
			connId = newId;
		}

		LogMgr.logInfo("ConnectionMgr.getConnection()", "Creating new connection for [" + aProfile.getKey() + "] for driver=" + aProfile.getDriverclass());
		WbConnection conn = this.connect(aProfile, connId);
		conn.runPostConnectScript();
		String driverVersion = conn.getDriverVersion();
		String jdbcVersion = conn.getJDBCVersion();
		String dbVersion = conn.getSqlConnection().getMetaData().getDatabaseProductVersion();

		LogMgr.logInfo("ConnectionMgr.getConnection()", "Connected to: [" +
			conn.getMetadata().getProductName() + "], Database version: [" + dbVersion + "], Driver version: [" +
			driverVersion + "], JDBC Version: [" + jdbcVersion + "], ID: ["  + connId + "]"
		);

		this.activeConnections.put(connId, conn);

		return conn;
	}

	private int getPrefixCount(String idPrefix)
	{
		int count = 0;
		for (String key : activeConnections.keySet())
		{
			if (key.startsWith(idPrefix))
			{
				count++;
			}
		}
		return count;
	}

	public Class loadClassFromDriverLib(ConnectionProfile profile, String className)
		throws ClassNotFoundException, UnsupportedClassVersionError
	{
		String drvClass = profile.getDriverclass();
		String drvName = profile.getDriverName();
		DbDriver drv = this.findDriverByName(drvClass, drvName);
		if (drv == null) return null;
		return drv.loadClassFromDriverLib(className);
	}

	private Properties getConnectionProperties(ConnectionProfile profile)
	{
		Properties props = new Properties(profile.getConnectionProperties());
		if (profile.getOracleSysDBA())
		{
			props.put("internal_logon", "sysdba");
		}
		return props;
	}

	WbConnection connect(ConnectionProfile profile, String anId)
		throws ClassNotFoundException, SQLException, UnsupportedClassVersionError
	{
		String drvClass = profile.getDriverclass();
		String drvName = profile.getDriverName();
		DbDriver drv = this.findDriverByName(drvClass, drvName);
		if (drv == null)
		{
			throw new SQLException("Driver class not registered");
		}

		copyPropsToSystem(profile);
		int oldTimeout = DriverManager.getLoginTimeout();
		Connection sql = null;
		try
		{
			int timeout = profile.getConnectionTimeoutValue();
			if (timeout > 0)
			{
				DriverManager.setLoginTimeout(timeout);
			}
			sql = drv.connect(profile.getUrl(), profile.getUsername(), profile.decryptPassword(), anId, getConnectionProperties(profile));
		}
		finally
		{
			DriverManager.setLoginTimeout(oldTimeout);
		}

		try
		{
			sql.setAutoCommit(profile.getAutocommit());
		}
		catch (Throwable th)
		{
			// some drivers do not support this, so
			// we just ignore the error :-)
			LogMgr.logInfo("ConnectionMgr.connect()", "Driver (" + drv.getDriverClass() + ") does not support the autocommit property: " + ExceptionUtil.getDisplay(th));
		}
		WbConnection conn = new WbConnection(anId, sql, profile);
		return conn;
	}

	private void copyPropsToSystem(ConnectionProfile profile)
	{
		if (profile != null && profile.getCopyExtendedPropsToSystem())
		{
			PropertiesCopier copier = new PropertiesCopier();
			copier.copyToSystem(profile.getConnectionProperties());
		}
	}

	private void removePropsFromSystem(ConnectionProfile profile)
	{
		if (profile != null && profile.getCopyExtendedPropsToSystem())
		{
			// Check if there is another connection open which uses
			// the same profile. If that is the case the
			// properties should not be removed from the system properties
			for (WbConnection con : activeConnections.values())
			{
				if (con.getProfile().equals(profile))
				{
					return;
				}
			}
			PropertiesCopier copier = new PropertiesCopier();
			copier.removeFromSystem(profile.getConnectionProperties());
		}
	}

	public boolean isNameUsed(String aName)
	{
		for (DbDriver db : drivers)
		{
			if (db.getName().equals(aName))
			{
				return true;
			}
		}
		return false;
	}

	public DbDriver findDriverByName(String drvClassName, String driverName)
	{
		DbDriver firstMatch = null;

		if (this.drivers == null) this.readDrivers();

		if (driverName == null || driverName.length() == 0) return this.findDriver(drvClassName);

		for (DbDriver db : drivers)
		{
			if (db.getDriverClass().equals(drvClassName))
			{
				// if the classname and the driver name are the same return the driver immediately
				// If we don't find a match for the name, we'll use
				// the first match for the classname
				if (db.getName().equals(driverName)) return db;
				if (firstMatch == null)
				{
					firstMatch = db;
				}
			}
		}

		// In batch mode the default drivers (DriverTemplates.xml) are not loaded.
		if (firstMatch == null && WbManager.getInstance().isBatchMode())
		{
			// We simple pretend there is one available, this will e.g. make
			// the ODBC Bridge work without a WbDrivers.xml
			return new DbDriver(driverName, drvClassName, null);
		}

		LogMgr.logDebug("ConnectionMgr.findDriverByName()", "Did not find driver with name="+ driverName + ", using " + (firstMatch == null ? "(n/a)" : firstMatch.getName()));

		return firstMatch;
	}

	public DbDriver findRegisteredDriver(String drvClassName)
	{
		if (this.drivers == null)	this.readDrivers();

		DbDriver db = null;

		for (int i=0; i < this.drivers.size(); i ++)
		{
			db = this.drivers.get(i);
			if (db.getDriverClass().equals(drvClassName)) return db;
		}
		return null;
	}

	public DbDriver findDriver(String drvClassName)
	{
		if (drvClassName == null)
		{
			LogMgr.logError("ConnectionMgr.findDriver()", "Called with a null classname!", new NullPointerException());
			return null;
		}

		DbDriver db = this.findRegisteredDriver(drvClassName);

		if (db == null)
		{
			LogMgr.logWarning("ConnectionMgr.findDriver()", "Did not find a registered driver with classname = ["+drvClassName+"]");
			try
			{
				// not found --> maybe it's present in the normal classpath...
				// eg the ODBC bridge
				Class drvcls = Class.forName(drvClassName);
				Driver drv = (Driver)drvcls.newInstance();
				db = new DbDriver(drv);
			}
			catch (Exception cnf)
			{
				LogMgr.logError("ConnectionMgr.findDriver()", "Error creating instance for driver class [" + drvClassName + "] ", cnf);
				db = null;
			}
		}
		return db;
	}

	/**
	 * Add a new, dynamically defined driver to the list of available
	 * drivers.
	 * This is used if a driver definition is passed on the commandline
	 *
	 * @see workbench.sql.BatchRunner#createCmdLineProfile(workbench.util.ArgumentParser)
	 */
	public DbDriver registerDriver(String drvClassName, String jarFile)
	{
		if (this.drivers == null) this.readDrivers();

		DbDriver drv = new DbDriver("JdbcDriver", drvClassName, jarFile);
		drv.setInternal(true);

		// this method is called from BatchRunner.createCmdLineProfile() when
		// the user passed all driver information on the command line.
		// as most likely this is the correct driver it has to be put
		// at the beginning of the list, to prevent a different driver
		// with the same driver class in WbDrivers.xml to be used instead
		this.drivers.add(0,drv);

		return drv;
	}

	/**
	 *	Returns a List of registered drivers.
	 *	This list is read from WbDrivers.xml
	 */
	public List<DbDriver> getDrivers()
	{
		if (this.drivers == null)
		{
			this.readDrivers();
		}
		return this.drivers;
	}

	public void setDrivers(List<DbDriver> aDriverList)
	{
		this.drivers = aDriverList;
		if (this.driverChangeListener != null)
		{
			PropertyChangeEvent evt = new PropertyChangeEvent(this, "drivers", null, null);
			for (PropertyChangeListener l : this.driverChangeListener)
			{
				l.propertyChange(evt);
			}
		}
	}

	public static ConnectionProfile findProfile(List<ConnectionProfile> list, ProfileKey key)
	{
		if (key == null) return null;
		if (list == null) return null;

		String name = key.getName();
		String group = key.getGroup();

		ConnectionProfile firstMatch = null;
		for (ConnectionProfile prof : list)
		{
			if (name.equalsIgnoreCase(prof.getName().trim()))
			{
				if (firstMatch == null) firstMatch = prof;
				if (group == null)
				{
					return prof;
				}
				else if (group.equalsIgnoreCase(prof.getGroup().trim()))
				{
					return prof;
				}
			}
		}
		return firstMatch;
	}

	/**
	 * Find a connection profile identified by the given key.
	 *
	 * @param key the key of the profile
	 * @return a connection profile with that name or null if none was found.
	 */
	public ConnectionProfile getProfile(ProfileKey key)
	{
		synchronized (profileLock)
		{
			if (key == null) return null;
			if (this.profiles == null) this.readProfiles();
			return findProfile(profiles, key);
		}
	}

	public void addDriverChangeListener(PropertyChangeListener l)
	{
		if (this.driverChangeListener == null) this.driverChangeListener = new ArrayList<PropertyChangeListener>();
		this.driverChangeListener.add(l);
	}

	public void removeDriverChangeListener(PropertyChangeListener l)
	{
		if (this.driverChangeListener == null) return;
		this.driverChangeListener.remove(l);
	}

	/**
	 * Return a list with profile keys that can be displayed to the user.
	 * The returned list is already sorted.
	 */
	public List<String> getProfileKeys()
	{
		synchronized (profileLock)
		{
			if (profiles == null) readProfiles();
			List<String> result = new ArrayList(profiles.size());
			for (ConnectionProfile profile : profiles)
			{
				result.add(profile.getKey().toString());
			}
			Collections.sort(result, CaseInsensitiveComparator.INSTANCE);
			return result;
		}
	}

	/**
	 *	Returns a List with the current profiles.
	 */
	public List<ConnectionProfile> getProfiles()
	{
		LogMgr.logTrace("ConnectionMgr.getProfiles()", "getProfiles() called at " + System.currentTimeMillis() + " from " + Thread.currentThread().getName());

		synchronized (profileLock)
		{
			if (this.profiles == null)
			{
				this.readProfiles();
			}
			return Collections.unmodifiableList(this.profiles);
		}
	}

	/**
	 * Re-load the profiles, regardless whether they have been loaded or not.
	 * This should only be used in unit-testing
	 */
	public void reloadProfiles()
	{
		synchronized (profileLock)
		{
			this.readProfiles();
		}
	}


	/**
	 * Disconnects all connections.
	 *
	 * @see #closeConnection(workbench.db.WbConnection)
	 */
	public void disconnectAll()
	{
		for (WbConnection con : this.activeConnections.values())
		{
			this.closeConnection(con);
		}
		this.activeConnections.clear();
		DbObjectCacheFactory.getInstance().clear();
	}

	/**
	 * Close all connections in a background thread.
	 *
	 * The list of active connections is cleared immediately, so any getConnection() after
	 * calling this method, will create a new physical connection, even if the current ones
	 * have not all been disconnected.
	 */
	public void abortAll(List<WbConnection> toAbort)
	{
		LogMgr.logWarning("ConnectionMgr.abortAll()", "Trying to abort all connections");

		for (WbConnection con : toAbort)
		{
			activeConnections.remove(con.getId());
		}

		if (LogMgr.isDebugEnabled())
		{
			dumpConnections();
		}

		for (final WbConnection con : toAbort)
		{
			WbThread disc = new WbThread("Disconnect for " + con.getId())
			{
				@Override
				public void run()
				{
					con.shutdown(false);
				}
			};
			long wait = Settings.getInstance().getIntProperty("workbench.db.connectionmgr.abortwait", 10);
			WbThread.runWithTimeout(disc, wait * 1000);
		}
		LogMgr.logWarning("ConnectionMgr.abortAll()", "Aborting all connections finished");
	}

	public void dumpConnections()
	{
		if (LogMgr.isDebugEnabled())
		{
			StringBuilder msg = new StringBuilder(activeConnections.size() * 20);
			for (WbConnection conn : activeConnections.values())
			{
				msg.append("Active connection: ");
				msg.append((conn == null ? "(null)" : conn.toString()));
				msg.append('\n');
			}

			if (msg.length() > 0)
			{
				LogMgr.logDebug("ConnectionMgr.dumpConnections()", msg.toString().trim());
			}
			else
			{
				LogMgr.logDebug("ConnectionMgr.dumpConnections()", "No more active connections.");
			}
		}
	}

	public void disconnect(WbConnection con)
	{
		if (con == null) return;
		this.activeConnections.remove(con.getId());
		LogMgr.logDebug("ConnectionMgr.disconnect()", "Trying to physically close the connection with id=" + con.getId());
		this.closeConnection(con);
	}

	/**
	 * Disconnect the given connection.
	 * @param conn the connection to disconnect.
	 */
	private void closeConnection(WbConnection conn)
	{
		if (conn == null) return;
		if (conn.isClosed()) return;

		try
		{
			if (conn.getProfile() != null)
			{
				LogMgr.logInfo("ConnectionMgr.disconnect()", "Disconnecting: [" + conn.getProfile().getName() + "], ID=" + conn.getId());
			}
			else
			{
				LogMgr.logInfo("ConnectionMgr.disconnect()", "Disconnecting connection with ID=" + conn.toString());
			}

			conn.runPreDisconnectScript();

			removePropsFromSystem(conn.getProfile());

			DbShutdownHook hook = DbShutdownFactory.getShutdownHook(conn);
			if (hook != null)
			{
				hook.shutdown(conn);
			}
			else
			{
				conn.shutdown();
			}
		}
		catch (Exception e)
		{
			LogMgr.logError(this, ResourceMgr.getString("ErrOnDisconnect"), e);
		}
	}

	/**
	 * Check if there is another connection active with the same URL.
	 *
	 * This is used when the connection to an embedded database that
	 * needs a {@link workbench.db.shutdown.DbShutdownHook} is called.
	 *
	 * @return true if there is another active connection.
	 */
	public boolean isActive(WbConnection aConn)
	{
		String url = aConn.getUrl();
		String id = aConn.getId();

		for (WbConnection conn : this.activeConnections.values())
		{
			if (conn == null) continue;

			if (conn.getId().equals(id)) continue;

			String u = conn.getUrl();
			if (u == null) continue;
			// we found one connection with the same URL
			if (u.equals(url)) return true;
		}

		return false;
	}

	private void createBackup(WbFile f)
	{
		int maxVersions = Settings.getInstance().getMaxWorkspaceBackup();
		String dir = Settings.getInstance().getWorkspaceBackupDir();
		String sep = Settings.getInstance().getFileVersionDelimiter();
		FileVersioner version = new FileVersioner(maxVersions, dir, sep);
		try
		{
			version.createBackup(f);
		}
		catch (IOException e)
		{
			LogMgr.logWarning("ConnectionMgr.createBackup()", "Error when creating backup for: " + f.getAbsolutePath(), e);
		}
	}

	/**
	 * Saves the driver definitions to an external file.
	 *
	 * The name of the file defaults to <tt>WbDrivers.xml</tt>. The exact location
	 * can be set in the configuration file.
	 * @see workbench.resource.Settings#getDriverConfigFilename()
	 * @see WbPersistence#writeObject(Object)
	 */
	public void saveDrivers()
	{
		if (WbManager.getInstance().isRestrictedMode())
		{
			LogMgr.logInfo("ConnectionMgr.saveProfiles()", "Application is running in restricted mode. Connection profiles are not saved");
			return;
		}

		if (Settings.getInstance().getCreateDriverBackup())
		{
			WbFile f = new WbFile(Settings.getInstance().getDriverConfigFilename());
			createBackup(f);
		}

		WbPersistence writer = new WbPersistence(Settings.getInstance().getDriverConfigFilename());

		// As drivers and profiles can be saved in console mode, we need to make
		// sure, that the "internal" drivers that are created "on-the-fly" when connecting
		// from the commandline are not stored in the configuration file.
		List<DbDriver> allDrivers = new ArrayList<DbDriver>(this.drivers);
		Iterator<DbDriver> itr = allDrivers.iterator();
		while (itr.hasNext())
		{
			if (itr.next().isInternal())
			{
				itr.remove();
			}
		}

		try
		{
			writer.writeObject(allDrivers);
		}
		catch (IOException e)
		{
			LogMgr.logError("ConnectionMgr.saveDrivers()", "Could not save drivers", e);
		}
	}

	@SuppressWarnings("unchecked")
	private void readDrivers()
	{
		synchronized (driverLock)
		{
			try
			{
				WbPersistence reader = new WbPersistence(Settings.getInstance().getDriverConfigFilename());
				Object result = reader.readObject();
				if (result == null)
				{
					this.drivers = Collections.synchronizedList(new ArrayList<DbDriver>());
				}
				else if (result instanceof ArrayList)
				{
					this.drivers = Collections.synchronizedList((List<DbDriver>) result);
				}
			}
			catch (FileNotFoundException fne)
			{
				LogMgr.logDebug("ConnectionMgr.readDrivers()", "WbDrivers.xml not found. Using defaults.");
				this.drivers = null;
			}
			catch (Exception e)
			{
				LogMgr.logDebug(this, "Could not load driver definitions. Creating new one...", e);
				this.drivers = null;
			}

			if (this.drivers == null)
			{
				this.drivers = Collections.synchronizedList(new ArrayList<DbDriver>());
			}
		}
		if (this.readTemplates)
		{
			this.importTemplateDrivers();
		}
	}

	public void setReadTemplates(boolean aFlag)
	{
		this.readTemplates = aFlag;
	}

	@SuppressWarnings("unchecked")
	private void importTemplateDrivers()
	{
		if (this.templatesImported) return;

		synchronized (driverLock)
		{
			if (this.drivers == null) this.readDrivers();

			// now read the templates and append them to the driver list
			InputStream in = null;
			try
			{
				in = getDriverTemplates();

				WbPersistence reader = new WbPersistence();
				ArrayList<DbDriver> templates = (ArrayList<DbDriver>)reader.readObject(in);

				for (int i=0; i < templates.size(); i++)
				{
					DbDriver drv = templates.get(i);
					if (!this.isNameUsed(drv.getName()))
					{
						this.drivers.add(drv);
					}
				}
			}
			catch (Throwable io)
			{
				LogMgr.logWarning("ConectionMgr.readDrivers()", "Could not read driver templates!", io);
			}
			finally
			{
				FileUtil.closeQuietely(in);
			}
		}
		this.templatesImported = true;
	}

	private InputStream getDriverTemplates()
		throws IOException
	{
		WbFile f = new WbFile(WbManager.getInstance().getJarPath(), "DriverTemplates.xml");
		if (f.exists())
		{
			LogMgr.logInfo("ConnectionMgr.getDriverTemplates()", "Reading external DriverTemplates from " + f.getFullPath());
			return new FileInputStream(f);
		}
		return this.getClass().getResourceAsStream("DriverTemplates.xml");
	}

	/**
	 * Remove all defined connection profiles.
	 * This does not make sure that all connections are closed!
	 * This method is used in Unit tests to setup a new set of profiles.
	 */
	public void clearProfiles()
	{
		synchronized (profileLock)
		{
			if (this.profiles != null)
			{
				this.profiles.clear();
			}
		}
	}

	/**
	 * Retrieves the connection profiles from an XML file.
	 *
	 * @see WbPersistence#readObject()
	 * @see workbench.resource.Settings#getProfileStorage()
	 */
	private void readProfiles()
	{
		long start = System.currentTimeMillis();
		LogMgr.logTrace("ConnectionMgr.readProfiles()", "readProfiles() called at " + start + " from " + Thread.currentThread().getName());

		String filename = getFileName();
		Object result = null;
		try
		{
			if (this.currentFilename != null)
			{
				LogMgr.logInfo("ConnectionMgr.readProfiles()", "Loading connection profiles from " + filename);
			}
			WbPersistence reader = new WbPersistence(filename);
			result = reader.readObject();
		}
		catch (FileNotFoundException fne)
		{
			LogMgr.logDebug("ConnectionMgr.readProfiles()", filename + " not found. Creating new one.");
			result = null;
		}
		catch (Exception e)
		{
			LogMgr.logError("ConnectionMgr.readProfiles()", "Error when reading connection profiles from " + filename, e);
			result = null;
		}

		this.profiles = new ArrayList<ConnectionProfile>();

		if (result instanceof Collection)
		{
			Collection c = (Collection)result;
			this.profiles.addAll(c);
		}
		else if (result instanceof Object[])
		{
			// This is to support the very first version of the profile storage
			// probably obsolete by know, but you never know...
			Object[] l = (Object[])result;
			for (Object prof : l)
			{
				this.profiles.add((ConnectionProfile)prof);
			}
		}
		this.resetProfiles();
		long end = System.currentTimeMillis();
		long duration = end - start;
		LogMgr.logTrace("ConnectionMgr.readProfiles()", "readProfiles() finished at " + end + " (" + duration + "ms) in " + Thread.currentThread().getName());
	}


	/**
	 * Reset the changed status on the profiles.
	 *
	 * Called after saving the profiles.
	 */
	private void resetProfiles()
	{
		if (this.profiles != null)
		{
			for (ConnectionProfile profile : this.profiles)
			{
				profile.reset();
			}
			this.profilesChanged = false;
		}
	}

	private String getFileName()
	{
		if (currentFilename == null)
		{
			return Settings.getInstance().getProfileStorage();
		}
		return currentFilename;
	}

	/**
	 * Save the connectioin profiles to an external file.
	 *
	 * This will also reset the changed flag for any modified or new
	 * profiles. The name of the file defaults to <tt>WbProfiles.xml</tt>, but
	 * can be defined in the configuration properties.
	 *
	 * @see workbench.resource.Settings#getProfileStorage()
	 * @see WbPersistence#writeObject(Object)
	 */
	public void saveProfiles()
	{
		if (WbManager.getInstance().isRestrictedMode())
		{
			LogMgr.logInfo("ConnectionMgr.saveProfiles()", "Application is running in restricted mode. Connection profiles are not saved");
			return;
		}

		synchronized (profileLock)
		{
			if (this.profiles != null)
			{
				if (Settings.getInstance().getCreateProfileBackup())
				{
					WbFile f = new WbFile(getFileName());
					createBackup(f);
				}

				WbPersistence writer = new WbPersistence(getFileName());
				try
				{
					writer.writeObject(this.profiles);
					this.resetProfiles();
				}
				catch (IOException e)
				{
					LogMgr.logError("ConnectionMgr.saveProfiles()", "Error saving profiles", e);
				}
			}
		}
	}

	/**
	 *	Returns true if any of the profile definitions has changed.
	 *	(Or if a profile has been deleted or added)
	 */
	public boolean profilesAreModified()
	{
		if (WbManager.getInstance().isRestrictedMode())
		{
			return false;
		}

		if (this.profilesChanged) return true;
		synchronized (profileLock)
		{
			if (this.profiles == null) return false;
			for (ConnectionProfile profile : this.profiles)
			{
				if (profile.isChanged())
				{
					return true;
				}
			}
			return false;
		}
	}

	public void applyProfiles(List<ConnectionProfile> newProfiles)
	{
		if (newProfiles == null) return;

		synchronized (profileLock)
		{
			this.profilesChanged = (profiles.size() != newProfiles.size());

			this.profiles.clear();
			for (ConnectionProfile profile : newProfiles)
			{
				this.profiles.add(profile.createStatefulCopy());
			}
		}
	}

	public void addProfile(ConnectionProfile aProfile)
	{
		synchronized (profileLock)
		{

			if (this.profiles == null)
			{
				this.readProfiles();
			}

			this.profiles.remove(aProfile);
			this.profiles.add(aProfile);
			this.profilesChanged = true;
		}
	}

	public void removeProfile(ConnectionProfile aProfile)
	{
		synchronized (profileLock)
		{
			if (this.profiles == null) return;

			this.profiles.remove(aProfile);
			// deleting a new profile should not change the status to changed
			if (!aProfile.isNew())
			{
				this.profilesChanged = true;
			}
		}
	}

	/**
	 * When the property {@link Settings#PROPERTY_PROFILE_STORAGE} is changed
	 * the current list of profiles is cleared.
	 *
	 * @param evt
	 * @see #clearProfiles()
	 */
	@Override
	public void propertyChange(java.beans.PropertyChangeEvent evt)
	{
		if (evt.getPropertyName().equals(Settings.PROPERTY_PROFILE_STORAGE))
		{
			this.clearProfiles();
		}
	}

}
