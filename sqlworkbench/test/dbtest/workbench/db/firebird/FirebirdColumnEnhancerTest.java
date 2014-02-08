/*
 * FirebirdColumnEnhancerTest.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2014, Thomas Kellerer
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
package workbench.db.firebird;

import java.util.List;

import workbench.TestUtil;
import workbench.WbTestCase;

import workbench.db.ColumnIdentifier;
import workbench.db.JdbcUtils;
import workbench.db.TableDefinition;
import workbench.db.TableIdentifier;
import workbench.db.TableSourceBuilder;
import workbench.db.TableSourceBuilderFactory;
import workbench.db.WbConnection;

import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Thomas Kellerer
 */
public class FirebirdColumnEnhancerTest
	extends WbTestCase
{

	public FirebirdColumnEnhancerTest()
	{
		super("FirebirdColumnEnhancerTest");
	}

	@BeforeClass
	public static void setUp()
		throws Exception
	{
		FirebirdTestUtil.initTestCase();
		WbConnection con = FirebirdTestUtil.getFirebirdConnection();
		if (con == null) return;

		TestUtil.executeScript(con, "create table foo (id integer, double_id computed by (id * 2));");
	}

	@AfterClass
	public static void tearDown()
		throws Exception
	{
		WbConnection con = FirebirdTestUtil.getFirebirdConnection();
		if (con == null) return;
		TestUtil.executeScript(con, "drop table foo;");
	}

	@Test
	public void testUpdateColumnDefinition()
		throws Exception
	{
		WbConnection con = FirebirdTestUtil.getFirebirdConnection();
		Assume.assumeNotNull(con);

		List<TableIdentifier> tables = con.getMetadata().getTableList();
		assertNotNull(tables);
		assertEquals(1, tables.size());

		TableDefinition def = con.getMetadata().getTableDefinition(tables.get(0));
		assertNotNull(def);
		List<ColumnIdentifier> cols = def.getColumns();
		assertNotNull(cols);
		assertEquals(2, cols.size());
		ColumnIdentifier computed = cols.get(1);
		assertEquals("DOUBLE_ID", computed.getColumnName());
		assertEquals("COMPUTED BY (id * 2)", computed.getComputedColumnExpression());
	}

	@Test
	public void testIdentityCols()
		throws Exception
	{
		WbConnection con = FirebirdTestUtil.getFirebirdConnection();
		Assume.assumeNotNull(con);
		if (!JdbcUtils.hasMinimumServerVersion(con, "3.0")) return;

		try
		{
			TestUtil.executeScript(con,
				"create table foo_gen (id integer generated by default as identity, double_id computed by (id * 2));");

			TableDefinition def = con.getMetadata().getTableDefinition(new TableIdentifier("FOO_GEN"));
			assertNotNull(def);
			List<ColumnIdentifier> cols = def.getColumns();
			assertNotNull(cols);
			assertEquals(2, cols.size());
			ColumnIdentifier id = cols.get(0);
			assertTrue(id.isAutoincrement());

			ColumnIdentifier computed = cols.get(1);
			assertEquals("DOUBLE_ID", computed.getColumnName());
			assertEquals("COMPUTED BY (id * 2)", computed.getComputedColumnExpression());

			TableSourceBuilder builder = TableSourceBuilderFactory.getBuilder(con);
			String sql = builder.getTableSource(def.getTable(), def.getColumns());
			assertTrue(sql.contains("GENERATED BY DEFAULT AS IDENTITY"));
		}
		finally
		{
			TestUtil.executeScript(con, "drop table foo_gen;");
		}

	}

}
