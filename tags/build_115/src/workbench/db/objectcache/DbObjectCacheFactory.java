/*
 * DbObjectCacheFactory.java
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
package workbench.db.objectcache;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import workbench.db.WbConnection;
import workbench.log.LogMgr;

/**
 * A factory for DbObjectCache instances.
 *
 * For each unique JDBC URL (including the username) one instance of the cache will be maintained.
 *
 * @author Thomas Kellerer
 */
public class DbObjectCacheFactory
	implements PropertyChangeListener
{
	private final Object lock = new Object();
	private Map<String, ObjectCache> caches;

	/**
	 * Thread safe singleton-instance
	 */
	protected static class LazyInstanceHolder
	{
		protected static final DbObjectCacheFactory instance = new DbObjectCacheFactory();
	}

	public static DbObjectCacheFactory getInstance()
	{
		return LazyInstanceHolder.instance;
	}

	private DbObjectCacheFactory()
	{
		this.caches = new HashMap<String, ObjectCache>();
	}

	public DbObjectCache getCache(WbConnection connection)
	{
		if (connection == null) return null;

		synchronized (lock)
		{
			String key = makeKey(connection);
			ObjectCache cache = caches.get(key);
			if (cache == null)
			{
				LogMgr.logDebug("DbObjectCacheFactory.getCache()", "Creating new cache for: " + key);
				cache = new ObjectCache(connection);
				caches.put(key, cache);
			}
			return new DbObjectCache(cache, connection);
		}
	}

	private String makeKey(WbConnection connection)
	{
		return connection.getProfile().getUsername() + "@" + connection.getProfile().getUrl();
	}

	/**
	 * Notification about the state of the connection. If the connection
	 * is closed, we can dispose the object cache
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (WbConnection.PROP_CONNECTION_STATE.equals(evt.getPropertyName()) &&
			  WbConnection.CONNECTION_CLOSED.equals(evt.getNewValue()))
		{
			WbConnection conn = (WbConnection)evt.getSource();
			synchronized (lock)
			{
				String key = makeKey(conn);
				LogMgr.logDebug("DbObjectCacheFactory.propertyChange()", "Remove cache for key=" + key);
				ObjectCache cache = caches.get(key);
				if (cache != null)
				{
					cache.clear();
					caches.remove(key);
				}
				conn.removeChangeListener(this);
			}
		}
	}

	public void clear()
	{
		synchronized(lock)
		{
			for (ObjectCache cache : caches.values())
			{
				cache.clear();
			}
			caches.clear();
		}
	}
}