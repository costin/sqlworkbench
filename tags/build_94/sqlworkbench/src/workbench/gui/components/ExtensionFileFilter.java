/*
 * ExtensionFileFilter.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2007, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.components;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.filechooser.FileFilter;

import workbench.resource.ResourceMgr;
import workbench.util.WbFile;

/**
 *
 * @author  support@sql-workbench.net
 */
public class ExtensionFileFilter
	extends FileFilter
{
	private static FileFilter textFileFilter;
	private static FileFilter sqlFileFilter;
	private static FileFilter sqlUpdateFileFilter;
	private static FileFilter jarFileFilter;
	private static FileFilter htmlFileFilter;
	private static FileFilter xmlFileFilter;

	private static FileFilter wkspFileFilter;

	private List extensions;
	private String desc;
	public static final String SQL_EXT = "sql";
	public static final String TXT_EXT = "txt";
	public static final String WORKSPACE_EXT = "wksp";
	public static final String XML_EXT = "xml";

	private boolean ignoreCase = true;

	public ExtensionFileFilter(String aDescription, List anExtensionList)
	{
		this(aDescription, anExtensionList, true);
	}

	public ExtensionFileFilter(String aDescription, List anExtensionList, boolean ignoreCase)
	{
		this.desc = aDescription;
		this.extensions = anExtensionList;
		this.ignoreCase = ignoreCase;
	}

	public String getDefaultExtension()
	{
		return (String)this.extensions.get(0);
	}

	public static String getExtension(File f)
	{
		return getExtension(f.getName());
	}
	
	public static String getExtension(String s)
	{
		WbFile f = new WbFile(s);
		return f.getExtension();
	}

	public static boolean hasSqlExtension(String aFilename)
	{
		String ext = getExtension(aFilename);
		return SQL_EXT.equalsIgnoreCase(ext);
	}

	public static boolean hasTxtExtension(String aFilename)
	{
		String ext = getExtension(aFilename);
		return TXT_EXT.equalsIgnoreCase(ext);
	}

	public static boolean hasHtmlExtension(String aFilename)
	{
		String ext = getExtension(aFilename);
		return "html".equalsIgnoreCase(ext);
	}

	public static boolean hasXmlExtension(String aFilename)
	{
		String ext = getExtension(aFilename);
		return XML_EXT.equalsIgnoreCase(ext);
	}

	public boolean accept(File f)
	{
		if (f.isDirectory())
		{
			return true;
		}
		if (this.extensions == null || this.extensions.size() == 0) return true;

		String extension = getExtension(f);
		if (extension == null) return false;

		if (this.extensions.contains(extension))
		{
			return true;
		}
		else if (this.ignoreCase)
		{
			for (int i=0; i < this.extensions.size(); i ++)
			{
				if (extension.equalsIgnoreCase(this.extensions.get(i).toString())) return true;
			}
		}

		return false;
	}

	public static FileFilter getJarFileFilter()
	{
		if (jarFileFilter == null)
		{
			ArrayList ext = new ArrayList();
			ext.add("jar");
			ext.add("zip");
			String desc = ResourceMgr.getString("TxtArchivesFilterName");
			jarFileFilter = new ExtensionFileFilter(desc, ext, true);
		}
		return jarFileFilter;
	}

	public static FileFilter getSqlFileFilter()
	{
		if (sqlFileFilter == null)
		{
			ArrayList ext = new ArrayList();
			ext.add(SQL_EXT);
			String desc = ResourceMgr.getString("TxtFileFilterSql");
			sqlFileFilter = new ExtensionFileFilter(desc, ext, true);
		}
		return sqlFileFilter;
	}

	public static FileFilter getSqlUpdateFileFilter()
	{
		if (sqlUpdateFileFilter == null)
		{
			ArrayList ext = new ArrayList();
			ext.add(SQL_EXT);
			String desc = ResourceMgr.getString("TxtFileFilterSqlUpdate");
			sqlUpdateFileFilter = new ExtensionFileFilter(desc, ext, true);
		}
		return sqlUpdateFileFilter;
	}

	public static FileFilter getTextFileFilter()
	{
		if (textFileFilter == null)
		{
			ArrayList ext = new ArrayList();
			ext.add(TXT_EXT);
			String desc = ResourceMgr.getString("TxtFileFilterText");
			textFileFilter = new ExtensionFileFilter(desc, ext, true);
		}
		return textFileFilter;
	}

	public static FileFilter getXmlFileFilter()
	{
		if (xmlFileFilter == null)
		{
			ArrayList ext = new ArrayList();
			ext.add(XML_EXT);
			String desc = ResourceMgr.getString("TxtFileFilterXml");
			xmlFileFilter = new ExtensionFileFilter(desc, ext, true);
		}
		return xmlFileFilter;
	}

	public static FileFilter getJavaFileFilter()
	{
		if (textFileFilter == null)
		{
			ArrayList ext = new ArrayList();
			ext.add("java");
			String desc = ResourceMgr.getString("TxtFileFilterJava");
			textFileFilter = new ExtensionFileFilter(desc, ext, true);
		}
		return textFileFilter;
	}

	public static FileFilter getWorkspaceFileFilter()
	{
		if (wkspFileFilter == null)
		{
			ArrayList ext = new ArrayList();
			ext.add(WORKSPACE_EXT);
			String desc = ResourceMgr.getString("TxtFileFilterWksp");
			wkspFileFilter = new ExtensionFileFilter(desc, ext, true);
		}
		return wkspFileFilter;
	}

	public static FileFilter getHtmlFileFilter()
	{
		if (htmlFileFilter == null)
		{
			ArrayList ext = new ArrayList();
			ext.add("html");
			String desc = ResourceMgr.getString("TxtFileFilterHtml");
			htmlFileFilter = new ExtensionFileFilter(desc, ext, true);
		}
		return htmlFileFilter;
	}

	// The description of this filter
	public String getDescription()
	{
		return this.desc;
	}
}