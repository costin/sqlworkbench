/*
 * ConstraintReader.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2012, Thomas Kellerer
 * No part of this code may be reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import workbench.util.StringUtil;

/**
 * An interface to read column and table constraints from the database.
 *
 * @author Thomas Kellerer
 */
public interface ConstraintReader
{
	/**
	 *	Returns the column constraints for the given table.
	 *
	 * The key to the returned Map is	the column name, the value is the full expression which can be appended
	 * to the column definition inside a CREATE TABLE statement.
	 *
	 * @param dbConnection the connection to use
	 * @param table        the table to check
	 */
	Map<String, String> getColumnConstraints(WbConnection dbConnection, TableIdentifier table);


	/**
	 * Returns the table level constraints for the table (usually these are check constraints).
	 *
	 * @param dbConnection  the connection to use
	 * @param table        the table to check
	 *
	 * @return a list of table constraints or an empty list if nothing was found
	 */
	List<TableConstraint> getTableConstraints(WbConnection dbConnection, TableIdentifier table);

	/**
	 * Rebuild the source of the given constraints.
	 *
	 * @param constraints  the constraints for which to build the source
	 * @param indent       a line indent to be used
	 */
	String getConstraintSource(List<TableConstraint> constraints, String indent);

	/**
	 * A ConstraintReader which does nothing.
	 */
	public static final ConstraintReader NULL_READER = new ConstraintReader()
	{
		@Override
		public Map<String, String> getColumnConstraints(WbConnection dbConnection, TableIdentifier table)
		{
			return Collections.emptyMap();
		}

		@Override
		public List<TableConstraint> getTableConstraints(WbConnection dbConnection, TableIdentifier table)
		{
			return Collections.emptyList();
		}

		@Override
		public String getConstraintSource(List<TableConstraint> constraints, String indent)
		{
			return StringUtil.EMPTY_STRING;
		}
	};
}
