/*
 * ConnectionProfile.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2004, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: info@sql-workbench.net
 *
 */
package workbench.db;

import java.beans.PropertyChangeListener;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Properties;

import workbench.WbManager;
import workbench.resource.Settings;
import workbench.util.WbCipher;
import workbench.util.WbPersistence;

public class ConnectionProfile
	implements PropertyChangeListener
{
	private static final String CRYPT_PREFIX = "@*@";
	private String name;
	private String url;
	private String driverclass;
	private String username;
	private String password;
	private String driverName;
	private boolean autocommit;
	private boolean disableUpdateTableCheck;
	private boolean rollbackBeforeDisconnect;
	private String description;
	private int id;
  private String identifier;
	private static int nextId = 1;
	private boolean changed;
	private boolean isNew;
	private boolean storePassword = true;
	private boolean seperateConnection;
	private Properties connectionProperties;
	private String workspaceFile;
	private boolean ignoreDropErrors;
	private boolean confirmUpdates;

	static
	{
		WbPersistence.makeTransient(ConnectionProfile.class, "inputPassword");
	}

	public ConnectionProfile()
	{
		this.id = getNextId();
    this.identifier = String.valueOf(this.id);
    this.isNew = true;
    this.changed = true;
		Settings.getInstance().addPropertyChangeListener(this);
	}

	private static synchronized int getNextId()
	{
		return nextId++;
	}

	public ConnectionProfile(String driverClass, String url, String userName, String pwd)
	{
		this();
		this.setUrl(url);
		this.setDriverclass(driverClass);
		this.setUsername(userName);
		this.setPassword(pwd);
		this.setName(url);
		this.changed = false;
	}

  public String getIdentifier()
  {
    return this.identifier;
  }

	public ConnectionProfile(String aName, String driverClass, String url, String userName, String pwd)
	{
		this();
		this.setUrl(url);
		this.setDriverclass(driverClass);
		this.setUsername(userName);
		this.setPassword(pwd);
		this.setName(aName);
		this.changed = false;
	}

	public boolean getUseSeperateConnectionPerTab()
	{
		return this.seperateConnection;
	}

	public void setUseSeperateConnectionPerTab(boolean aFlag)
	{
		if (this.seperateConnection != aFlag) this.changed = true;
		this.seperateConnection = aFlag;
	}

	public boolean getRollbackBeforeDisconnect()
	{
		return this.rollbackBeforeDisconnect;
	}

	public void setRollbackBeforeDisconnect(boolean flag)
	{
		if (flag != this.rollbackBeforeDisconnect) this.changed = true;
		this.rollbackBeforeDisconnect = flag;
	}

	/**
	 *	Sets the current password. If the password
	 *	is not already encrypted, it will be encrypted
	 *
	 *	@see #getPassword()
	 *	@see workbench.util.WbCipher#encryptString(String)
	 */
	public void setPassword(String aPwd)
	{
		if (aPwd == null)
		{
			this.password = null;
			this.changed = true;
			return;
		}

		aPwd = aPwd.trim();

		// check encryption settings when reading the profiles...
		if (Settings.getInstance().getUseEncryption())
		{
			if (!this.isEncrypted(aPwd))
			{
				aPwd = this.encryptPassword(aPwd);
			}
		}
		else
		{
			if (this.isEncrypted(aPwd))
			{
				aPwd = this.decryptPassword(aPwd);
			}
		}

		if (!aPwd.equals(this.password))
		{
			this.password = aPwd;
			if (this.storePassword) this.changed = true;
		}
	}


	/**
	 *	Returns the encrypted version of the password.
	 *	This getter/setter pair is used when saving the profile
	 *	@see #decryptPassword(String)
	 */
	public String getPassword()
	{
		if (this.storePassword)
			return this.password;
		else
			return null;
	}

	public String getInputPassword()
	{
		if (this.storePassword)
			return this.decryptPassword();
		else
			return "";
	}

	public void setInputPassword(String aPassword)
	{
		this.setPassword(aPassword);
	}

	/**
	 *	Returns the plain text version of the
	 *	current password.
	 *
	 *	@see #decryptPassword(String)
	 */
	public String decryptPassword()
	{
		return this.decryptPassword(this.password);
	}

	/**
	 *	Returns the plain text version of the given
	 *	password. This is not put into the getPassword()
	 *	method because the XMLEncode would write the
	 *	password in plain text into the XML file.
	 *	A method beginning with decrypt is not
	 *	regarded as a property and thus not written
	 *	to the XML file.
	 *
	 *	@parm the encrypted password
	 */
	public String decryptPassword(String aPwd)
	{
		if (aPwd == null) return null;
		if (!aPwd.startsWith(CRYPT_PREFIX))
		{
			return aPwd;
		}
		else
		{
			WbCipher des = WbManager.getInstance().getDesCipher();
			return des.decryptString(aPwd.substring(CRYPT_PREFIX.length()));
		}
	}

	public boolean isEncrypted(String aPwd)
	{
		return aPwd.startsWith(CRYPT_PREFIX);
	}

	public void setNew()
	{
		this.changed = true;
		this.isNew = true;
	}
	public boolean isNew() { return this.isNew; }
  public boolean isChanged()
	{
		return this.changed || this.isNew;
	}

  public void reset()
	{
		this.changed = false;
		this.isNew = false;
	}

	private String encryptPassword(String aPwd)
	{
		if (Settings.getInstance().getUseEncryption())
		{
			if (!this.isEncrypted(aPwd))
			{
				WbCipher des = WbManager.getInstance().getDesCipher();
				aPwd = CRYPT_PREFIX + des.encryptString(aPwd);
			}
		}
		return aPwd;
	}

	/**
	 *	Returns the name of the Profile
	 */
	public String toString() { return this.name; }

	/** Two connection profiles are equal if:
	 *  <ul>
	 * 	<li>the url are equal</li>
	 *  <li>the driver classes are equal</li>
	 *	<li>the usernames are equal</li>
	 *	<li>the (encrypted) passwords are equal</li>
	 *  </ul>
	 */
	public boolean equals(Object other)
	{
		try
		{
			ConnectionProfile prof = (ConnectionProfile)other;
			return this.id == prof.id;
			/*
			return this.url.equals(prof.url) &&
						 this.driverclass.equals(prof.driverclass) &&
						 this.username.equals(prof.username) &&
						 this.password.equals(prof.password);
			*/
		}
		catch (ClassCastException e)
		{
			return false;
		}
	}

	public String getUrl() { return this.url; }
	public void setUrl(String aUrl)
	{
		if (aUrl != null) aUrl = aUrl.trim();
		this.url = aUrl;
		this.changed = true;
	}

	public String getDriverclass() { return this.driverclass; }
	public void setDriverclass(String aDriverclass)
	{
		if (aDriverclass != null) aDriverclass = aDriverclass.trim();
		this.driverclass = aDriverclass;
		this.changed = true;
	}

	public String getUsername() { return this.username; }
	public void setUsername(java.lang.String aUsername)
	{
		if (aUsername != null) aUsername = aUsername.trim();
		this.username = aUsername;
		this.changed = true;
	}

	public boolean getAutocommit() { return this.autocommit; }
	public void setAutocommit(boolean aFlag)
	{
		if (aFlag != this.autocommit)
		{
			this.autocommit = aFlag;
			this.changed = true;
		}
	}

	public String getName() { return this.name; }
	public void setName(String aName)
	{
		this.name = aName;
		this.changed = true;
	}

	public String getDescription() { return this.description; }

	public void setDescription(String description)
	{
		this.changed = true;
		this.description = description;
	}

	public boolean getStorePassword() { return this.storePassword; }
	public void setStorePassword(boolean aFlag) { this.storePassword = aFlag; }

	public ConnectionProfile createCopy()
	{
		ConnectionProfile result = new ConnectionProfile();
		result.setAutocommit(this.autocommit);
		result.setDescription(this.description);
		result.setDriverclass(this.driverclass);
		result.setDriverName(this.driverName);
		result.setName(this.name);
		result.setPassword(this.getPassword());
		result.setUrl(this.url);
		result.setUsername(this.username);
		result.setWorkspaceFile(this.workspaceFile);
		result.setIgnoreDropErrors(this.ignoreDropErrors);
		result.setUseSeperateConnectionPerTab(this.seperateConnection);
		result.setRollbackBeforeDisconnect(this.rollbackBeforeDisconnect);
		result.setConfirmUpdates(this.confirmUpdates);
		if (this.connectionProperties != null)
		{
			Enumeration keys = this.connectionProperties.propertyNames();
			result.connectionProperties = new Properties();

			while (keys.hasMoreElements())
			{
				String key = (String)keys.nextElement();
				String value = this.connectionProperties.getProperty(key);
				result.connectionProperties.put(key, value);
			}
		}
		result.changed = false;
		return result;
	}

	public static Comparator getNameComparator()
	{
		return new Comparator()
		{
			public int compare(Object o1, Object o2)
			{
				if (o1 == null && o2 == null) return 0;
				if (o1 == null) return -1;
				if (o2 == null) return 1;
				if (o1 instanceof ConnectionProfile && o2 instanceof ConnectionProfile)
				{
					String name1 = ((ConnectionProfile)o1).name;
					String name2 = ((ConnectionProfile)o2).name;
					return name1.compareTo(name2);
				}
				return 0;
			}
		};
	}

	public boolean getIgnoreDropErrors()
	{
		return this.ignoreDropErrors;
	}

	public void setIgnoreDropErrors(boolean aFlag)
	{
		this.changed = (aFlag != this.ignoreDropErrors);
		this.ignoreDropErrors = aFlag;
	}

	public String getWorkspaceFile()
	{
		return this.workspaceFile;
	}

	public void setWorkspaceFile(String aWorkspaceFile)
	{
		this.workspaceFile = aWorkspaceFile;
    this.changed = true;
	}

	public void addConnectionProperty(String aKey, String aValue)
	{
		if (aKey == null) return;
		if (this.connectionProperties == null)
		{
			this.connectionProperties = new Properties();
		}
		this.connectionProperties.put(aKey, aValue);
		this.changed = true;
	}

	public Properties getConnectionProperties()
	{
		return this.connectionProperties;
	}

	public void setConnectionProperties(Properties props)
	{
		boolean wasDefined = (this.connectionProperties != null && this.connectionProperties.size() > 0);
		if (props != null)
		{
			if (props.size() == 0)
			{
				this.connectionProperties = null;
				if (wasDefined) this.changed = true;
			}
			else
			{
				this.connectionProperties = props;
				this.changed = true;
			}
		}
		else
		{
			this.connectionProperties = null;
			if (wasDefined) this.changed = true;
		}
	}

	public java.lang.String getDriverName()
	{
		return driverName;
	}

	public void setDriverName(java.lang.String driverName)
	{
		this.driverName = driverName;
	}

	/**
	 * Getter for property disableUpdateTableCheck.
	 * @return Value of property disableUpdateTableCheck.
	 */
	public boolean getDisableUpdateTableCheck()
	{
		return disableUpdateTableCheck;
	}

	/**
	 * Setter for property disableUpdateTableCheck.
	 * @param disableUpdateTableCheck New value of property disableUpdateTableCheck.
	 */
	public void setDisableUpdateTableCheck(boolean flag)
	{
		if (flag != this.disableUpdateTableCheck) this.changed = true;
		this.disableUpdateTableCheck = flag;
	}

	public boolean isConfirmUpdates()
	{
		return confirmUpdates;
	}

	public void setConfirmUpdates(boolean flag)
	{
		if (flag != this.confirmUpdates) this.changed = true;
		this.confirmUpdates = flag;
	}

	public void propertyChange(java.beans.PropertyChangeEvent evt)
	{
		if (Settings.ENCRYPT_PWD_KEY.equals(evt.getPropertyName()))
		{
			String old = this.password;
			// calling setPassword will encrypt/decrypt the password
			// according to the current setting
			this.setPassword(old);
		}
	}

}