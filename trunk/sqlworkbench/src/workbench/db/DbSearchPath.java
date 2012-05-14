/*
 * DbSearchPath.java
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
import workbench.db.ibm.Db2SearchPath;
import workbench.db.postgres.PostgresUtil;

/**
 *
 * @author Thomas Kellerer
 */
public interface DbSearchPath
{
	List<String> getSearchPath(WbConnection dbConn, String defaultSchema);

	public static DbSearchPath DEFAULT_HANDLER = new DbSearchPath()
	{
		@Override
		public List<String> getSearchPath(WbConnection dbConn, String defaultSchema)
		{
			if (defaultSchema == null)
			{
				defaultSchema = dbConn.getCurrentSchema();
			}
			if (defaultSchema == null)
			{
				return Collections.emptyList();
			}
			return Collections.singletonList(dbConn.getMetadata().adjustSchemaNameCase(defaultSchema));
		}
	};

	public static DbSearchPath PG_HANDLER = new DbSearchPath()
	{
		@Override
		public List<String> getSearchPath(WbConnection dbConn, String defaultSchema)
		{
			return PostgresUtil.getSearchPath(dbConn);
		}
	};

	public static class Factory
	{
		public static DbSearchPath getSearchPathHandler(WbConnection con)
		{
			if (con != null && con.getMetadata().isPostgres())
			{
				return PG_HANDLER;
			}
			else if (con.getDbId().equals("db2i"))
			{
				return new Db2SearchPath();
			}
			return DEFAULT_HANDLER;
		}
	}
}
