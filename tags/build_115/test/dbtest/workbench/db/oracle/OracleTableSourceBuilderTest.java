/*
 * OracleTableSourceBuilderTest.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2013, Thomas Kellerer
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
package workbench.db.oracle;


import workbench.TestUtil;
import workbench.WbTestCase;

import workbench.db.TableIdentifier;
import workbench.db.WbConnection;

import workbench.sql.ScriptParser;

import org.junit.*;

import static org.junit.Assert.*;

import workbench.db.JdbcUtils;
import workbench.db.TableDefinition;
import workbench.db.TableSourceBuilder;
import workbench.db.TableSourceBuilderFactory;

/**
 *
 * @author Thomas Kellerer
 */
public class OracleTableSourceBuilderTest
	extends WbTestCase
{

	public OracleTableSourceBuilderTest()
	{
		super("OracleTableSourceBuilderTest");
	}

	@BeforeClass
	public static void setUpClass()
		throws Exception
	{
		String sql =
			"CREATE TABLE index_test (test_id integer not null, tenant_id integer);\n" +
			"ALTER TABLE index_test \n" +
			"   ADD CONSTRAINT pk_indexes PRIMARY KEY (test_id)  \n" +
			"   USING INDEX (CREATE INDEX idx_pk_index_test ON index_test (test_id, tenant_id) REVERSE);";

		OracleTestUtil.initTestCase();
		WbConnection con = OracleTestUtil.getOracleConnection();
		if (con == null) return;
		TestUtil.executeScript(con, sql, false);
	}

	@AfterClass
	public static void tearDownClass()
		throws Exception
	{
		OracleTestUtil.cleanUpTestCase();
	}

	@Test
	public void test12c()
		throws Exception
	{
//		WbConnection con = OracleTestUtil.getOracleConnection("thomas", "welcome", "10.0.26.20", "orcl");
		WbConnection con = OracleTestUtil.getOracleConnection();
		if (con == null) return;
		if (!JdbcUtils.hasMinimumServerVersion(con, "12.1"))
		{
			System.out.println("No Oracle 12c available, skipping test");
			return;
		}

		try
		{
			String sql =
				"create table default_null (id integer not null primary key, some_value varchar(100) default on null 'Arthur');\n" +
				"create sequence test_sequence;\n" +
				"create table sequence_default (id integer default test_sequence.nextval not null primary key);\n" +
				"create table ident_default (id integer generated always as identity);\n" +
				"create table ident_options (id integer generated always as identity start with 42 cache 50 maxvalue 42424242 cycle);\n";
			TestUtil.executeScript(con, sql);

			TableSourceBuilder builder = TableSourceBuilderFactory.getBuilder(con);

			{
				TableDefinition defNull = con.getMetadata().getTableDefinition(new TableIdentifier("DEFAULT_NULL"));
				String source = builder.getTableSource(defNull.getTable(), defNull.getColumns());
				assertTrue(source.contains("DEFAULT ON NULL 'Arthur' NOT NULL"));
			}

			{
				TableDefinition seqDef = con.getMetadata().getTableDefinition(new TableIdentifier("SEQUENCE_DEFAULT"));
				String seqDefSql = builder.getTableSource(seqDef.getTable(), seqDef.getColumns());
				String owner = seqDef.getTable().getSchema();
				String expression = "DEFAULT \"" + owner + "\".\"TEST_SEQUENCE\".\"NEXTVAL\" NOT NULL";
				assertTrue(seqDefSql.contains(expression));
			}

			{
				TableDefinition ident1 = con.getMetadata().getTableDefinition(new TableIdentifier("IDENT_DEFAULT"));
				String identSql = builder.getTableSource(ident1.getTable(), ident1.getColumns());
				assertTrue(identSql.contains("ID  NUMBER   GENERATED ALWAYS AS IDENTITY NOT NULL"));
			}

			{
				TableDefinition ident1 = con.getMetadata().getTableDefinition(new TableIdentifier("IDENT_OPTIONS"));
				String identSql = builder.getTableSource(ident1.getTable(), ident1.getColumns());
				assertTrue(identSql.contains("ID  NUMBER   GENERATED ALWAYS AS IDENTITY START WITH 42 MAXVALUE 42424242 CYCLE CACHE 50 NOT NULL"));
			}
		}
		finally
		{
			String cleanup =
				"drop table default_null cascade constraints purge; \n" +
				"drop sequence test_sequence; \n" +
				"drop table sequence_default cascade constraints purge; \n" +
				"drop table ident_default cascade constraints purge; \n" +
				"drop table ident_options cascade constraints purge;" +
				"purge recyclebin;";
			TestUtil.executeScript(con, cleanup);
		}
	}

	@Test
	public void testGetSource()
		throws Exception
	{
		WbConnection con = OracleTestUtil.getOracleConnection();
		if (con == null) return;
		TableIdentifier table = con.getMetadata().findTable(new TableIdentifier("INDEX_TEST"));
		assertNotNull(table);
		String sql = table.getSource(con).toString();

//		System.out.println(sql);
		//assertTrue(sql.indexOf("USING INDEX (") > 0);
		ScriptParser p = new ScriptParser(sql);
		assertEquals(2, p.getSize());
		String indexSql = p.getCommand(1);
		indexSql = indexSql.replaceAll("\\s+", " ");
//		System.out.println(indexSql);
		String expected = "ALTER TABLE INDEX_TEST ADD CONSTRAINT PK_INDEXES PRIMARY KEY (TEST_ID) USING INDEX ( CREATE INDEX IDX_PK_INDEX_TEST ON INDEX_TEST (TEST_ID ASC, TENANT_ID ASC) TABLESPACE USERS REVERSE )";
		assertEquals(expected, indexSql);

	}
}