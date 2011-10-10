/*
 * SettingsListener
 * 
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 * 
 * Copyright 2002-2012, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 * 
 * To contact the author please send an email to: support@sql-workbench.net
 */
package workbench.resource;

/**
 *
 * @author Thomas Kellerer
 */
public interface SettingsListener
{
	/**
	 * This is called before the configuration settings are persisted to the
	 * external file. 
	 */
	void beforeSettingsSave();
}
