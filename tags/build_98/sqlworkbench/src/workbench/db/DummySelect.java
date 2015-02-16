/*
 * DummySelect.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2007, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db;
import java.sql.SQLException;
import workbench.resource.Settings;
import workbench.storage.DataStore;
import workbench.util.StringUtil;
/**
 * @author support@sql-workbench.net
 */
public class DummySelect 
	implements DbObject
{
	private TableIdentifier table;
	
	public DummySelect(TableIdentifier tbl)
	{
		this.table = tbl;
	}

	public String getCatalog()
	{
		return null;
	}

	public String getObjectExpression(WbConnection conn)
	{
		return null;
	}

	public String getObjectName()
	{
		return null;
	}

	public String getObjectName(WbConnection conn)
	{
		return null;
	}

	public String getObjectType()
	{
		return "SELECT";
	}

	public String getSchema()
	{
		return null;
	}

	public CharSequence getSource(WbConnection con)
		throws SQLException
	{
		DbMetadata meta = con.getMetadata();
		String nl = Settings.getInstance().getInternalEditorLineEnding();
		DataStore tableDef = meta.getTableDefinition(table);

		if (tableDef.getRowCount() == 0) return StringUtil.EMPTY_STRING;
		int colCount = tableDef.getRowCount();
		if (colCount == 0) return StringUtil.EMPTY_STRING;

		StringBuilder sql = new StringBuilder(colCount * 80);

		sql.append("SELECT ");
		for (int i=0; i < colCount; i++)
		{
			String column = tableDef.getValueAsString(i, DbMetadata.COLUMN_IDX_TABLE_DEFINITION_COL_NAME);
			if (i > 0)
			{
				sql.append(',');
				sql.append(nl);
				sql.append("       ");
			}

			sql.append(column);
		}
		sql.append(nl);
		sql.append("FROM ");
		sql.append(table.getTableExpression(con));
		sql.append(';');
		sql.append(nl);

		return sql.toString();	}
	
}