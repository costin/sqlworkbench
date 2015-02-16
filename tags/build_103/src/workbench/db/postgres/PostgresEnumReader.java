/*
 * PostgresEnumReader.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2009, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db.postgres;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import workbench.db.WbConnection;
import workbench.log.LogMgr;
import workbench.util.SqlUtil;
import workbench.util.StringUtil;

/**
 *
 * @author support@sql-workbench.net
 */
public class PostgresEnumReader
{
	public List<String> getEnumValues(WbConnection con, String typeName)
	{
		if (StringUtil.isBlank(typeName)) return Collections.emptyList();
		Statement stmt = null;
		ResultSet rs = null;
		List<String> result = new ArrayList<String>();
		try
		{
			stmt = con.createStatementForQuery();
			rs = stmt.executeQuery("SELECT enumlabel FROM pg_enum WHERE enumtypid = '" + typeName + "'::regtype ORDER BY oid");
			while (rs.next())
			{
				result.add(rs.getString(1));
			}
		}
		catch (SQLException e)
		{
			LogMgr.logError("PostgresEnumReader.getEnumValues()", "Could not read enum values", e);
		}
		finally
		{
			SqlUtil.closeAll(rs, stmt);
		}
		return result;
	}
}