/*
 * TableAlias.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2006, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.util;

import workbench.db.TableIdentifier;

/**
 * @author  support@sql-workbench.net
 */
public class TableAlias
{
	private final TableIdentifier table;
	private final String alias;
	private String display;
	
	public TableAlias(String value)
	{
		String tablename = null;
		int apos = StringUtil.findFirstWhiteSpace(value);
		if (apos > -1)
		{
			tablename = value.substring(0, apos).trim();
			this.alias = value.substring(apos + 1).trim();
		}
		else
		{
			tablename = value.trim();
			this.alias = null;
		}
		this.table = new TableIdentifier(tablename);
		
	}
	
	public final String getAlias() { return this.alias; }
	public final TableIdentifier getTable() { return this.table; }
	public final String getNameToUse() 
	{
		if (alias == null) return table.getTableName();
		return alias;
	}
	public String toString() 
	{
		if (display == null)
		{
			if (alias == null) display = table.getTableName();
			else display = alias + " (" + table + ")";
		}
		return display;
	} 
		
	public boolean isTableOrAlias(String name)
	{
		TableIdentifier tbl = new TableIdentifier(name);
		if (this.alias == null)
		{
			return table.getTableName().equalsIgnoreCase(tbl.getTableName());
		}
		else
		{
			return (table.getTableName().equalsIgnoreCase(tbl.getTableName()) || name.equalsIgnoreCase(alias));
		}
	}
}