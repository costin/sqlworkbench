/*
 * HsqlColumnEnhancerTest.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2016, Thomas Kellerer
 *
 * Licensed under a modified Apache License, Version 2.0
 * that restricts the use for certain governments.
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at.
 *
 *     http://sql-workbench.net/manual/license.html
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
package workbench.db.hsqldb;

import java.util.List;
import workbench.TestUtil;
import workbench.db.WbConnection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import workbench.WbTestCase;
import workbench.db.ColumnIdentifier;
import workbench.db.ConnectionMgr;
import workbench.db.JdbcUtils;
import workbench.db.TableDefinition;
import workbench.db.TableIdentifier;

/**
 *
 * @author Thomas Kellerer
 */
public class HsqlColumnEnhancerTest
	extends WbTestCase
{

	public HsqlColumnEnhancerTest()
	{
		super("HsqlColumnEnhancerTest");
	}

	@BeforeClass
	public static void setUpClass()
		throws Exception
	{
	}

	@AfterClass
	public static void tearDownClass()
		throws Exception
	{
		ConnectionMgr.getInstance().disconnectAll();
	}

	@Test
	public void testUpdateColumnDefinition()
		throws Exception
	{
		TestUtil util = getTestUtil();
		WbConnection con = util.getHSQLConnection("column_enhancer_test");
		TestUtil.executeScript(con,
			"CREATE TABLE gen_col_test (\n" +
			"  id  integer,  \n" +
			"  id2 integer generated always as (id*3), \n" +
			"  id3 integer generated always as identity (start with 24 increment by 42) \n" +
			");\n");
		TableDefinition tbl = con.getMetadata().getTableDefinition(new TableIdentifier(null, "PUBLIC", "GEN_COL_TEST"));
		assertNotNull(tbl);
		List<ColumnIdentifier> cols = tbl.getColumns();
		assertEquals(3, cols.size());
		for (ColumnIdentifier col : cols)
		{
			String name = col.getColumnName();
			if ("id2".equalsIgnoreCase(name))
			{
				assertEquals("GENERATED ALWAYS AS (ID*3)", col.getComputedColumnExpression());
			}
			if ("id3".equalsIgnoreCase(name))
			{
				assertEquals("GENERATED ALWAYS AS IDENTITY (START WITH 24 INCREMENT BY 42)", col.getComputedColumnExpression());
			}
		}

	}

	@Test
	public void testColumnSequence()
		throws Exception
	{
		String sql = "CREATE SEQUENCE seq_company_id;\n" +
			"CREATE TABLE company\n" +
			"(\n" +
			"   id bigint GENERATED BY DEFAULT AS SEQUENCE seq_company_id PRIMARY KEY, \n" +
			"   company_name varchar(100) NOT NULL\n" +
			");";
		TestUtil util = getTestUtil();
		WbConnection con = util.getHSQLConnection("column_sequence_test");
		if (JdbcUtils.hasMinimumServerVersion(con, "2.2"))
		{
			TestUtil.executeScript(con, sql);
			TableDefinition tbl = con.getMetadata().getTableDefinition(new TableIdentifier(null, "PUBLIC", "COMPANY"));
			assertNotNull(tbl);
			List<ColumnIdentifier> cols = tbl.getColumns();
			assertEquals(2, cols.size());
			ColumnIdentifier column = cols.get(0);
			assertEquals("GENERATED BY DEFAULT AS SEQUENCE SEQ_COMPANY_ID", column.getComputedColumnExpression());
		}
		else
		{
			System.out.println("Wrong HSQL version. Skipping sequence test");
		}
	}
}
