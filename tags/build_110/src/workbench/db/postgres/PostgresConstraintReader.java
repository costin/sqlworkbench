/*
 * PostgresConstraintReader.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2011, Thomas Kellerer
 * No part of this code may be reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db.postgres;

import workbench.db.AbstractConstraintReader;


/**
 * Read table level constraints for Postgres
 * (column constraints are stored on table level...)
 * @author  Thomas Kellerer
 */
public class PostgresConstraintReader
	extends AbstractConstraintReader
{
	private static final String TABLE_SQL =
					 "select rel.conname, rel.consrc, obj_description(t.oid) as remarks \n" +
           "from pg_class t, pg_constraint rel \n" +
           "where t.relname = ? \n" +
           "and   t.oid = rel.conrelid " +
		       "and   rel.contype = 'c'";

	public String getColumnConstraintSql()
	{
		return null;
	}

	public String getTableConstraintSql()
	{
		return TABLE_SQL;
	}

	@Override
	public int getIndexForTableNameParameter()
	{
		return 1;
	}

}
