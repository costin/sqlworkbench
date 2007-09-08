/*
 * PostgresIndexReader.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2007, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Savepoint;
import workbench.db.DbMetadata;
import workbench.db.JdbcIndexReader;
import workbench.db.TableIdentifier;
import workbench.log.LogMgr;
import workbench.resource.Settings;
import workbench.storage.DataStore;
import workbench.util.ExceptionUtil;
import workbench.util.SqlUtil;
import workbench.util.StringUtil;

/**
 * @author  support@sql-workbench.net
 */
public class PostgresIndexReader
	extends JdbcIndexReader
{
	public PostgresIndexReader(DbMetadata meta)
	{
		super(meta);
	}
	
	public StringBuilder getIndexSource(TableIdentifier table, DataStore indexDefinition, String tableNameToUse)
	{
		Connection con = this.metaData.getSqlConnection();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql = "SELECT indexdef FROM pg_indexes WHERE indexname = ? ";
		String nl = Settings.getInstance().getInternalEditorLineEnding();
		int count = indexDefinition.getRowCount();
		if (count == 0) return StringUtil.emptyBuffer();
		StringBuilder source = new StringBuilder(count * 50);
		Savepoint sp = null;
		try
		{
			sp = con.setSavepoint();
			stmt = con.prepareStatement(sql);
			for (int i = 0; i < count; i++)
			{
				String idxName = indexDefinition.getValueAsString(i, DbMetadata.COLUMN_IDX_TABLE_INDEXLIST_INDEX_NAME);
				String pk = indexDefinition.getValueAsString(i, DbMetadata.COLUMN_IDX_TABLE_INDEXLIST_PK_FLAG);
				if ("YES".equalsIgnoreCase(pk)) continue;
				stmt.setString(1, idxName);
				rs = stmt.executeQuery();
				if (rs.next())
				{
					source.append(rs.getString(1));
					source.append(';');
					source.append(nl);
				}
			}
			source.append(nl);
			con.releaseSavepoint(sp);
		}
		catch (Exception e)
		{
			try { con.rollback(sp); } catch (Throwable th) {}
			LogMgr.logError("PostgresIndexReader.getIndexSource()", "Error retrieving source", e);
			source = new StringBuilder(ExceptionUtil.getDisplay(e));
		}
		finally
		{
			SqlUtil.closeAll(rs, stmt);
		}
		return source;
	}

}
