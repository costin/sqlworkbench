/*
 * DerbySynonymReader.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2008, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db.derby;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import workbench.db.DbMetadata;
import workbench.db.SynonymReader;
import workbench.db.TableIdentifier;
import workbench.resource.Settings;
import workbench.util.SqlUtil;

/**
 * @author support@sql-workbench.net
 */
public class DerbySynonymReader
	implements SynonymReader
{
	private DbMetadata meta;
	public DerbySynonymReader(DbMetadata dbMeta)
	{
		this.meta = dbMeta;
	}

	/**
	 * The DB2 JDBC driver returns Alias' automatically, so there 
	 * is no need to retrieve them here
	 */
	public List<String> getSynonymList(Connection con, String owner) 
		throws SQLException
	{
		List<String> result = new LinkedList<String>();
		String sql = "select a.alias " + 
             "from sys.sysaliases a, sys.sysschemas s \n" + 
             "where a.schemaid = s.schemaid \n" + 
			       " and a.aliastype = 'S' " +
			       " and s.schemaname = ?";		

		PreparedStatement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = con.prepareStatement(sql);
			stmt.setString(1, owner);
			rs = stmt.executeQuery();
			while (rs.next())
			{
				String alias = rs.getString(1);
				if (!rs.wasNull())
				{
					result.add(alias);
				}
			}
		}
		finally
		{
			SqlUtil.closeAll(rs, stmt);
		}

		return result;
	}

	public TableIdentifier getSynonymTable(Connection con, String anOwner, String aSynonym)
		throws SQLException
	{
		String sql = "select a.aliasinfo \n" + 
             "from sys.sysaliases a, sys.sysschemas s \n" + 
             "where a.schemaid = s.schemaid \n" + 
             " and a.alias = ?" +
			       " and s.schemaname = ?";		

		PreparedStatement stmt = con.prepareStatement(sql);
		stmt.setString(1, aSynonym);
		stmt.setString(2, anOwner);
		ResultSet rs = stmt.executeQuery();
		String table = null;
		TableIdentifier result = null;
		try
		{
			if (rs.next())
			{
				table = rs.getString(1);
				if (table != null)
				{
					result = new TableIdentifier(table);
				}
			}
		}
		finally
		{
			SqlUtil.closeAll(rs,stmt);
		}

		if (result != null)
		{
			String type = this.meta.getObjectType(result);
			result.setType(type);
		}

		return result;
	}

	public String getSynonymSource(Connection con, String anOwner, String aSynonym)
		throws SQLException
	{
		TableIdentifier id = getSynonymTable(con, anOwner, aSynonym);
		StringBuilder result = new StringBuilder(200);
		String nl = Settings.getInstance().getInternalEditorLineEnding();
		result.append("CREATE SYNONYM ");
		result.append(aSynonym);
		result.append(nl + "       FOR ");
		result.append(id.getTableExpression());
		result.append(';');
		result.append(nl);
		
		return result.toString();
	}

}
