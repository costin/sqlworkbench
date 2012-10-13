/*
 * TableDefinitionReader
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2012, Thomas Kellerer
 * No part of this code may be reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 */
package workbench.db;

import java.sql.SQLException;
import java.util.List;
import workbench.db.oracle.OracleTableDefinitionReader;

/**
 *
 * @author Thomas Kellerer
 */
public interface TableDefinitionReader
{
	/**
	 * Return the definition of the given table.
	 * <br/>
	 * To display the columns for a table in a DataStore create an
	 * instance of {@link TableColumnsDatastore}.
	 *
	 * @param toRead The table for which the definition should be retrieved (it should have a PK assigned)
	 * @param dbConnection the connection to use
	 * @param typeResolver the data type resolver that should be used to "clean up" data types returned from the driver
	 *
	 * @throws SQLException
	 * @return the definition of the table. If toRead was null, null is returned
	 *
	 * @see TableColumnsDatastore
	 * @see TableIdentifier#getPrimaryKey()
	 */
	List<ColumnIdentifier> getTableColumns(TableIdentifier toRead, DataTypeResolver typeResolver)
		throws SQLException;

	TableDefinition getTableDefinition(TableIdentifier toRead)
		throws SQLException;

}
