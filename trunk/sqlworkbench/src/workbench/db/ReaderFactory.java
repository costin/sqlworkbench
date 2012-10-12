/*
 * ReaderFactory.java
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

import workbench.db.cubrid.CubridSequenceReader;
import workbench.db.derby.DerbyConstraintReader;
import workbench.db.derby.DerbySequenceReader;
import workbench.db.firebird.FirebirdConstraintReader;
import workbench.db.firebird.FirebirdIndexReader;
import workbench.db.firebird.FirebirdProcedureReader;
import workbench.db.firebird.FirebirdSequenceReader;
import workbench.db.firstsql.FirstSqlConstraintReader;
import workbench.db.h2database.H2ConstraintReader;
import workbench.db.h2database.H2IndexReader;
import workbench.db.h2database.H2SequenceReader;
import workbench.db.hsqldb.HsqlConstraintReader;
import workbench.db.hsqldb.HsqlSequenceReader;
import workbench.db.ibm.Db2ConstraintReader;
import workbench.db.ibm.Db2SequenceReader;
import workbench.db.ibm.InformixSequenceReader;
import workbench.db.ingres.IngresSequenceReader;
import workbench.db.mssql.SqlServerConstraintReader;
import workbench.db.mssql.SqlServerProcedureReader;
import workbench.db.mssql.SqlServerSequenceReader;
import workbench.db.mssql.SqlServerUtil;
import workbench.db.mysql.MySQLIndexReader;
import workbench.db.mysql.MySqlProcedureReader;
import workbench.db.nuodb.NuoDBSequenceReader;
import workbench.db.oracle.OracleConstraintReader;
import workbench.db.oracle.OracleIndexReader;
import workbench.db.oracle.OracleProcedureReader;
import workbench.db.oracle.OracleSequenceReader;
import workbench.db.postgres.PostgresConstraintReader;
import workbench.db.postgres.PostgresIndexReader;
import workbench.db.postgres.PostgresProcedureReader;
import workbench.db.postgres.PostgresSequenceReader;
import workbench.db.vertica.VerticaSequenceReader;
import workbench.resource.Settings;

/**
 * A factory to create instances of the various readers specific for a DBMS.
 *
 * @author Thomas Kellerer
 */
public class ReaderFactory
{
	public static ProcedureReader getProcedureReader(DbMetadata meta)
	{
		if (meta.isOracle())
		{
			return new OracleProcedureReader(meta.getWbConnection());
		}
		if (meta.isPostgres())
		{
			return new PostgresProcedureReader(meta.getWbConnection());
		}
		if (meta.isFirebird())
		{
			return new FirebirdProcedureReader(meta.getWbConnection());
		}
		if (meta.isSqlServer())
		{
			boolean useJdbc = Settings.getInstance().getBoolProperty("workbench.db.mssql.usejdbcprocreader", false);
			if (!useJdbc)
			{
				return new SqlServerProcedureReader(meta.getWbConnection());
			}
		}
		if (meta.isMySql())
		{
			return new MySqlProcedureReader(meta.getWbConnection());
		}
		return new JdbcProcedureReader(meta.getWbConnection());
	}

	public static SequenceReader getSequenceReader(WbConnection con)
	{
		DbMetadata meta = con.getMetadata();
		if (meta.isPostgres())
		{
			return new PostgresSequenceReader(con);
		}
		if (meta.isOracle())
		{
			return new OracleSequenceReader(con);
		}
		if (meta.isHsql())
		{
			return new HsqlSequenceReader(con);
		}
		if (meta.isApacheDerby() && JdbcUtils.hasMinimumServerVersion(con, "10.6"))
		{
			return new DerbySequenceReader(con);
		}
		if (meta.isH2())
		{
			return new H2SequenceReader(con);
		}
		if (meta.isFirebird())
		{
			return new FirebirdSequenceReader(con);
		}
		if (meta.getDbId().startsWith("db2"))
		{
			return new Db2SequenceReader(con, meta.getDbId());
		}
		if (meta.getDbId().equals("cubrid"))
		{
			return new CubridSequenceReader(con);
		}
		if (meta.getDbId().equals("vertica_database"))
		{
			return new VerticaSequenceReader(con);
		}
		if (meta.isSqlServer() && SqlServerUtil.isSqlServer2012(con))
		{
			return new SqlServerSequenceReader(con);
		}
		if (con.getDbId().equals("informix_dynamic_server"))
		{
			return new InformixSequenceReader(con);
		}
		if (con.getDbId().equals("ingres"))
		{
			return new IngresSequenceReader(con);
		}
		if (con.getDbId().equals("nuodb"))
		{
			return new NuoDBSequenceReader(con);
		}
		return null;
	}

	public static IndexReader getIndexReader(DbMetadata meta)
	{
		if (meta.isOracle())
		{
			return new OracleIndexReader(meta);
		}
		if (meta.isPostgres())
		{
			return new PostgresIndexReader(meta);
		}
		if (meta.isH2())
		{
			return new H2IndexReader(meta);
		}
		if (meta.isFirebird() && JdbcUtils.hasMinimumServerVersion(meta.getWbConnection(), "2.5"))
		{
			return new FirebirdIndexReader(meta);
		}
		if (meta.isMySql())
		{
			return new MySQLIndexReader(meta);
		}
		return new JdbcIndexReader(meta);
	}

	public static ConstraintReader getConstraintReader(DbMetadata meta)
	{
		String dbid = meta.getDbId();
		if (meta.isPostgres())
		{
			return new PostgresConstraintReader(meta.getDbId());
		}
		if (meta.isOracle())
		{
			return new OracleConstraintReader(meta.getDbId());
		}
		if (meta.isHsql())
		{
			return new HsqlConstraintReader(meta.getWbConnection());
		}
		if (meta.isSqlServer())
		{
			return new SqlServerConstraintReader(meta.getWbConnection());
		}
		if (dbid.startsWith("db2"))
		{
			return new Db2ConstraintReader(meta.getWbConnection());
		}
		if (meta.isFirebird())
		{
			return new FirebirdConstraintReader();
		}
		if ("h2".equals(dbid))
		{
			return new H2ConstraintReader();
		}
		if (dbid.startsWith("adaptive_server"))
		{
			return new SybaseConstraintReader(meta.getWbConnection());
		}
		if (meta.isApacheDerby())
		{
			return new DerbyConstraintReader();
		}
		if (dbid.startsWith("firstsql"))
		{
			return new FirstSqlConstraintReader();
		}
		return null;
	}
}
