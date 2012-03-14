/*
 * TableAlias.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2012, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.util;

import workbench.db.TableIdentifier;

/**
 * @author Thomas Kellerer
 */
public class TableAlias
	extends Alias
{
	private TableIdentifier table;

	public TableAlias(String value)
	{
		this(value, '.');
	}

	public TableAlias(String value, char separator)
	{
		super(value);

		if (getObjectName() != null)
		{
			this.table = new TableIdentifier(getObjectName(), separator);
		}
	}

	public final TableIdentifier getTable()
	{
		return this.table;
	}

	/**
	 * Compares the given name to this TableAlias checking
	 * if the name either references this table or its alias
	 */
	public boolean isTableOrAlias(String name, char separator)
	{
		if (StringUtil.isEmptyString(name))
		{
			return false;
		}

		TableIdentifier tbl = new TableIdentifier(name, separator);
		return (table.getTableName().equalsIgnoreCase(tbl.getTableName()) || name.equalsIgnoreCase(getAlias()));
	}
}
