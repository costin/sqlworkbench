/*
 * SqlServerColumnEnhancerTest.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2015, Thomas Kellerer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at.
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db.mssql;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import workbench.TestUtil;
import workbench.WbTestCase;
import workbench.resource.Settings;

import workbench.db.ColumnIdentifier;
import workbench.db.TableDefinition;
import workbench.db.TableIdentifier;
import workbench.db.WbConnection;
import workbench.db.sqltemplates.ColumnChanger;

import workbench.util.SqlUtil;

import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Thomas Kellerer
 */
public class SqlServerColumnEnhancerTest
	extends WbTestCase
{

	public SqlServerColumnEnhancerTest()
	{
		super("SqlServerColumnEnhancerTest");
	}

	@BeforeClass
	public static void setUpClass()
		throws Exception
	{
		SQLServerTestUtil.initTestcase("SqlServerProcedureReaderTest");
		WbConnection conn = SQLServerTestUtil.getSQLServerConnection();
		Assume.assumeNotNull("No connection available", conn);
		SQLServerTestUtil.dropAllObjects(conn);
		String sql =
				"create table sales \n" +
				"( \n" +
				"   pieces integer, \n" +
				"   single_price numeric(19,2), \n" +
				"   total_price as (pieces * single_price), \n" +
				"   avg_price as (single_price / pieces) persisted, \n" +
        "   some_text varchar(20) collate Latin1_General_BIN, \n" +
        "   some_geo geometry \n" +
				")";
		TestUtil.executeScript(conn, sql);
	}

	@AfterClass
	public static void tearDownClass()
		throws Exception
	{
		WbConnection conn = SQLServerTestUtil.getSQLServerConnection();
		Assume.assumeNotNull("No connection available", conn);
		SQLServerTestUtil.dropAllObjects(conn);
	}

	@Test
	public void testEnhancer()
		throws SQLException
	{
		WbConnection conn = SQLServerTestUtil.getSQLServerConnection();
		assertNotNull("No connection available", conn);

		TableDefinition def = conn.getMetadata().getTableDefinition(new TableIdentifier("sales"));
		assertNotNull(def);
		List<ColumnIdentifier> cols = def.getColumns();
		assertEquals(6, cols.size());
		ColumnIdentifier total = cols.get(2);
		assertEquals("total_price", total.getColumnName());
		assertEquals("AS ([pieces]*[single_price])", total.getComputedColumnExpression());

		ColumnIdentifier avg = cols.get(3);
		assertEquals("avg_price", avg.getColumnName());
		assertEquals("AS ([single_price]/[pieces]) PERSISTED", avg.getComputedColumnExpression());

    ColumnIdentifier text = cols.get(4);
    assertEquals("some_text", text.getColumnName());
    assertEquals("Latin1_General_BIN", text.getCollation());

    ColumnIdentifier geo = cols.get(5);
    assertEquals("some_geo", geo.getColumnName());
    assertEquals("geometry", geo.getDbmsType());
	}

	@Test
	public void testRemarks()
		throws SQLException
	{
		WbConnection conn = SQLServerTestUtil.getSQLServerConnection();
		assertNotNull("No connection available", conn);
		Settings.getInstance().setProperty("workbench.db.microsoft_sql_server.remarks.column.retrieve", true);

		ColumnChanger changer = new ColumnChanger(conn);
		TableIdentifier sales = conn.getMetadata().findTable(new TableIdentifier("sales"));
		TableDefinition def = conn.getMetadata().getTableDefinition(sales);

		// Update the column remark using the SQL generated by the ColumnChanger
		ColumnIdentifier pieces = def.getColumns().get(0);
		ColumnIdentifier newCol = pieces.createCopy();
		newCol.setComment("Total number ordered");
		String sql = changer.getColumnCommentSql(def.getTable(), newCol);

		Statement stmt = null;
		try
		{
			stmt = conn.createStatement();
			stmt.execute(sql);
			conn.commit();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw e;
		}
		finally
		{
			SqlUtil.closeStatement(stmt);
		}

    // Now check if the column remark was defined correctly
		def = conn.getMetadata().getTableDefinition(sales);
		pieces = def.getColumns().get(0);
		assertEquals("Total number ordered", pieces.getComment());
	}


}
