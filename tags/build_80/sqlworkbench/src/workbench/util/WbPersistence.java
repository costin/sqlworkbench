/*
 * WbPersistence.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2005, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.util;

import java.beans.BeanInfo;
import java.beans.ExceptionListener;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import workbench.log.LogMgr;

public class WbPersistence
	implements ExceptionListener
{
	private String filename;

	public WbPersistence(String file)
	{
		filename = file;
	}

	/**
	 * Makes a property of the given class transient, so that it won't be written
	 * into the XML file when saved using WbPersistence
	 * @param clazz
	 * @param property
	 */
	public static void makeTransient(Class clazz, String property)
	{
		try
		{
			BeanInfo info = Introspector.getBeanInfo( clazz );
			PropertyDescriptor propertyDescriptors[] = info.getPropertyDescriptors();
			for (int i = 0; i < propertyDescriptors.length; i++)
			{
				PropertyDescriptor pd = propertyDescriptors[i];
				if ( pd.getName().equals(property) )
				{
					pd.setValue( "transient", Boolean.TRUE );
				}
			}
		}
		catch ( IntrospectionException e )
		{
		}
	}

	public Object readObject()
		throws Exception
	{
		InputStream in = new BufferedInputStream(new FileInputStream(filename), 32*1024);
		return readObject(in);
	}

	public Object readObject(InputStream in)
		throws Exception
	{
		XMLDecoder e = new XMLDecoder(in, null, this);
		Object result = e.readObject();
		e.close();
		return result;
	}

	public void writeObject(Object aValue)
	{
		if (aValue == null) return;

		try
		{
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename), 32*1024);
			XMLEncoder e = new XMLEncoder(out);
			e.writeObject(aValue);
			e.close();
		}
		catch (Throwable e)
		{
			LogMgr.logError("WbPersistence.writeObject()", "Error writing " + filename, e);
		}
	}

	public void exceptionThrown(Exception e)
	{
		LogMgr.logError("WbPersistence", "Error reading file " + filename, e);
	}
	
}