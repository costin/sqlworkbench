/*
 * ProfileListModel.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2005, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.profiles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.swing.AbstractListModel;

import workbench.db.ConnectionMgr;
import workbench.db.ConnectionProfile;

/**
 *
 * @author  support@sql-workbench.net
 */
class ProfileListModel 
	extends AbstractListModel
{

	private ArrayList profiles;
	private boolean changed = false;
	
	/** Creates a new instance of ProfileListModel */
	public ProfileListModel(Map aProfileList)
	{
		this.profiles = new ArrayList();
		
		if (aProfileList != null)
		{
			this.profiles.addAll(aProfileList.values());
			Collections.sort(this.profiles, ConnectionProfile.getNameComparator());
		}
	}

	/** Returns the value at the specified index.
	 * @param index the requested index
	 * @return the value at <code>index</code>
	 *
	 */
	public Object getElementAt(int index)
	{
		return this.profiles.get(index);
	}

	/**
	 * Returns the length of the list.
	 * @return the length of the list
	 *
	 */
	public int getSize()
	{
		return this.profiles.size();
	}
	
	public void profileChanged(ConnectionProfile aProfile)
	{
		int index = this.profiles.indexOf(aProfile);
		if (index >= 0)
		{
			this.fireContentsChanged(this, index, index);
		}
	}

	public void addProfile(ConnectionProfile aProfile)
	{
		ConnectionMgr conn = ConnectionMgr.getInstance();
		conn.addProfile(aProfile);
		this.profiles.add(this.profiles.size(), aProfile);
		this.fireIntervalAdded(this, this.profiles.size() - 1,  this.profiles.size() - 1);
		this.changed = true;
	}

	public void deleteProfile(int index)
	{
		ConnectionMgr conn = ConnectionMgr.getInstance();
		ConnectionProfile profile = (ConnectionProfile)this.profiles.get(index);
		conn.removeProfile(profile);
		this.profiles.remove(index);
		this.fireIntervalRemoved(this, index, index);
		this.changed = true;
	}

	private void putProfile(int index, ConnectionProfile aProfile)
	{
		this.profiles.set(index, aProfile);
	}

	public boolean isChanged()
	{
		if (changed) return true;
		for (int i=0; i < this.profiles.size(); i++)
		{
			ConnectionProfile profile = (ConnectionProfile)this.profiles.get(i);
			if (profile.isChanged()) return true;
		}
		return false;
	}
	public Collection getValues()
	{
		return this.profiles;
	}
	
	
}
