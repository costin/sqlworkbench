/*
 * PostgresSearchPathTest.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2012, Thomas Kellerer
 * No part of this code may be reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 */
package workbench.db.postgres;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Set;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import workbench.TestUtil;
import workbench.WbTestCase;
import workbench.db.TableIdentifier;
import workbench.db.WbConnection;
import workbench.storage.DataStore;
import static org.junit.Assert.*;
import workbench.db.ColumnIdentifier;
import workbench.sql.StatementRunnerResult;
import workbench.sql.wbcommands.ObjectInfo;
import workbench.util.SqlUtil;
/**
 *
 * @author Thomas Kellerer
 */
public class PostgresSearchPathTest
	extends WbTestCase
{

	public PostgresSearchPathTest()
	{
		super("PostgresSearchPathTest");
	}

	@BeforeClass
	public static void setUp()
		throws Exception
	{
		PostgresTestUtil.initTestCase("path_1");
		WbConnection con = PostgresTestUtil.getPostgresConnection();
		if (con == null) return;

		TestUtil.executeScript(con,
			"CREATE SCHEMA path_2;\n" +
			"CREATE table path_1.t1 (id1 integer);\n" +
			"CREATE table path_2.t2 (id2 integer, c2 text);\n" +
			"insert into path_1.t1 values (1);\n" +
			"insert into path_2.t2 values (2);\n" +
			"COMMIT; \n");
	}

	@AfterClass
	public static void tearDown()
		throws Exception
	{
		PostgresTestUtil.cleanUpTestCase();
	}

	@Test
	public void testDetectUpdateTable()
		throws Exception
	{
		WbConnection con = PostgresTestUtil.getPostgresConnection();
		if (con == null) return;

		TestUtil.executeScript(con, "set search_path=path_2,path_1");
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = con.createStatement();

			String sql = "select * from t1";
			rs = stmt.executeQuery(sql);
			DataStore ds1 = new DataStore(rs, con);
			SqlUtil.closeResult(rs);

			ds1.setGeneratingSql(sql);
			ds1.checkUpdateTable(con);
			TableIdentifier tbl1 = ds1.getUpdateTable();
			assertNotNull(tbl1);
			assertEquals("path_1", tbl1.getSchema());

			sql = "select * from t2";
			rs = stmt.executeQuery(sql);
			DataStore ds2 = new DataStore(rs, con);
			SqlUtil.closeResult(rs);

			ds2.setGeneratingSql(sql);
			ds2.checkUpdateTable(con);
			TableIdentifier tbl2 = ds2.getUpdateTable();
			assertNotNull(tbl2);
			assertEquals("path_2", tbl2.getSchema());
		}
		finally
		{
			SqlUtil.closeStatement(stmt);
		}
	}

	@Test
	public void testCompletion()
		throws Exception
	{
		WbConnection con = PostgresTestUtil.getPostgresConnection();
		if (con == null) return;

		TestUtil.executeScript(con, "set search_path=path_2,path_1");
		List<ColumnIdentifier> columns = con.getObjectCache().getColumns(new TableIdentifier("t1"));
		assertNotNull(columns);
		assertEquals(1, columns.size());
		assertEquals("id1", columns.get(0).getColumnName());

		TestUtil.executeScript(con, "set search_path=path_1,path_2");
		columns = con.getObjectCache().getColumns(new TableIdentifier("t2"));
		assertNotNull(columns);
		assertEquals(2, columns.size());
		assertEquals("id2", columns.get(0).getColumnName());
		assertEquals("c2", columns.get(1).getColumnName());

		Set<TableIdentifier> tables = con.getObjectCache().getTables("PATH_1");
		assertNotNull(tables);
		assertEquals(1, tables.size());
		assertTrue(tables.contains(new TableIdentifier("t1")));
	}

	@Test
	public void testObjectInfo()
		throws Exception
	{
		WbConnection con = PostgresTestUtil.getPostgresConnection();
		if (con == null) return;

		TestUtil.executeScript(con, "set search_path=path_2,path_1");

		ObjectInfo info = new ObjectInfo();
		StatementRunnerResult result = info.getObjectInfo(con, "t1", false);

		assertNotNull(result);
		assertTrue(result.hasDataStores());
		assertEquals(1, result.getDataStores().size());
		assertEquals("path_1.t1", result.getDataStores().get(0).getResultName());

		result = info.getObjectInfo(con, "t2", false);

		assertNotNull(result);
		assertTrue(result.hasDataStores());
		assertEquals(1, result.getDataStores().size());
		assertEquals("path_2.t2", result.getDataStores().get(0).getResultName());
	}

}
