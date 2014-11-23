/*
 * WbProperties.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2007, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import workbench.interfaces.PropertyStorage;
import workbench.util.EncodingUtil;

/**
 * An enhanced Properties class
 * 
 * @author support@sql-workbench.net
 */
public class WbProperties
	extends Properties
	implements PropertyStorage
{
	private int distinctSections = 2;

	private List changeListeners = new LinkedList();
	
	public WbProperties()
	{
	}
	
	public WbProperties(int num)
	{
		this.distinctSections = num;
	}

	public synchronized void saveToFile(String filename)
		throws IOException
	{
		FileOutputStream out = null;
		try
		{
			out = new FileOutputStream(filename);
			this.save(out);
		}
		finally
		{
			out.close();
		}
	}
	
	public synchronized void save(OutputStream out)
		throws IOException
	{
		Object[] keys = this.keySet().toArray();
		Arrays.sort(keys);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
		String value = null;
		String lastKey = null;
		String key = null;
		for (int i=0; i < keys.length; i++)
		{
			key = (String)keys[i];

			if (lastKey != null)
			{
				String k1 = null;
				String k2 = null;
				k1 = getSections(lastKey, this.distinctSections); //getFirstTwoSections(lastKey);
				k2 = getSections(key, this.distinctSections); //getFirstTwoSections(key);
				if (!k1.equals(k2))
				{
					bw.newLine();
				}
			}
			Object v = this.get(key);
			if (v != null)
			{
				value = v.toString();
				value = StringUtil.replace(value, "\\", "\\\\");
				if (value.indexOf('\n') > -1)
				{
					value = value.replaceAll("\n", "\\\\n");
				}
				if (value.length() > 0)
				{
					bw.write(key + "=" + value);
					bw.newLine();
				}
				else
				{
					bw.write(key + "=");
					bw.newLine();
				}
			}
			else
			{
				bw.write(key + "=");
				bw.newLine();
			}
			lastKey = key;
		}
		bw.flush();
	}

	public synchronized int getIntProperty(String property, int defaultValue)
	{
		String value = this.getProperty(property, null);
		if (value == null) return defaultValue;
		return StringUtil.getIntValue(value, defaultValue);
	}
	
	public synchronized boolean getBoolProperty(String property, boolean defaultValue)
	{
		String value = this.getProperty(property, null);
		if (value == null) return defaultValue;
		return StringUtil.stringToBool(value);
	}
	
	public synchronized void setProperty(String property, int value)
	{
		this.setProperty(property, Integer.toString(value));
	}
	
	public synchronized void setProperty(String property, boolean value)
	{
		this.setProperty(property, Boolean.toString(value));
	}
	
	private synchronized String getSections(String aString, int aNum)
	{
		int pos = aString.indexOf(".");
		String result = null;
		for (int i=1; i < aNum; i++)
		{
			int pos2 = aString.indexOf('.', pos + 1);
			if (pos2 > -1)
			{
				pos = pos2;
			}
			else
			{
				if (i == (aNum - 1))
				{
					pos = aString.length();
				}
			}
		}
		result = aString.substring(0, pos);
		return result;
	}

	public synchronized void addPropertyChangeListener(PropertyChangeListener aListener)
	{
		this.changeListeners.add(aListener);
	}
	
	public synchronized void removePropertyChangeListener(PropertyChangeListener aListener)
	{
		this.changeListeners.remove(aListener);
	}
	
	private synchronized void firePropertyChanged(String name, String oldValue, String newValue)
	{
		int count = this.changeListeners.size();
		if (count == 0) return;
		PropertyChangeEvent evt = new PropertyChangeEvent(this, name, oldValue, newValue);
		Iterator itr = this.changeListeners.iterator();
		while (itr.hasNext())
		{
			PropertyChangeListener l = (PropertyChangeListener)itr.next();
			if (l != null) l.propertyChange(evt);
		}
	}
	
	public synchronized Object setProperty(String name, String value)
	{
		if (name == null) return null;
		if (value == null) 
		{
			super.remove(name);
			return null;
		}
		
		String oldValue = (String)super.setProperty(name, value);
		
		if ( (oldValue == null && value != null) || 
			   (oldValue != null && value == null) ||
			   !oldValue.equals(value))
		{
			this.firePropertyChanged(name, oldValue, value);
		}
		return oldValue;
	}

	/**
	 *	Adds a property definition in the form key=value
	 *	Lines starting with # are ignored
	 *	Lines that do not contain a = character are ignored
	 *  Any text after a # sign in the value is ignored
	 */
	public synchronized void addPropertyDefinition(String line)
	{
		if (line == null) return;
		if (line.trim().length() == 0) return;
		if (line.startsWith("#")) return;
		int pos = line.indexOf("=");
		if (pos == -1) return;
		String key = line.substring(0, pos);
		String value = line.substring(pos + 1);
		pos = value.indexOf('#');
		if (pos > -1)
		{
			value = value.substring(0, pos);
		}
		this.setProperty(key, value.trim());
	}
	
	public synchronized void loadTextFile(String filename)
		throws IOException
	{
		loadTextFile(filename, null);
	}

	/**
	 *	Read the content of the file int this properties object.
	 *  This method does not support line continuation!
	 */
	public synchronized void loadTextFile(String filename, String encoding)
		throws IOException
	{
		BufferedReader in = null;
		File f = new File(filename);
		if(encoding == null) 
		{
			encoding = EncodingUtil.getDefaultEncoding();
		}
		try
		{
			in = EncodingUtil.createBufferedReader(f, encoding);
			String line = in.readLine();
			while (line != null)
			{
				this.addPropertyDefinition(StringUtil.decodeUnicode(line));
				line = in.readLine();
			}
		}
		finally
		{
			try { in.close(); } catch (Throwable th) {}
		}
	}
	
}