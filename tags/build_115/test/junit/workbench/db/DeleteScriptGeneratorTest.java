/*
 * DeleteScriptGeneratorTest.java
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
package workbench.db;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import workbench.TestUtil;
import workbench.WbTestCase;

import workbench.storage.ColumnData;

import workbench.sql.ScriptParser;

import workbench.util.SqlUtil;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Thomas Kellerer
 */
public class DeleteScriptGeneratorTest
	extends WbTestCase
{
	private WbConnection dbConnection;

	public DeleteScriptGeneratorTest()
	{
		super("DeleteScriptGeneratorTest");
	}

	@Test
	public void testCleanup()
		throws Exception
	{
		String sql =
			"create table root  \n" +
			"( \n" +
			"  r_id integer primary key not null \n" +
			"); \n" +
			" \n" +
			"create table details \n" +
			"( \n" +
			"  d_id integer not null primary key \n" +
			"); \n" +
			" \n" +
			"create table level1 \n" +
			"( \n" +
			"  l1_id integer primary key not null, \n" +
			"  r_id integer not null references root (r_id), \n" +
			"  d_id integer not null references details(d_id) \n" +
			"); \n" +
			" \n" +
			"create table details_item \n" +
			"( \n" +
			"  di_id integer not null primary key, \n" +
			"  d_id integer not null references details(d_id), \n" +
			"  r_id integer not null references root (r_id), \n" +
			"  l1_id integer not null references level1 (l1_id) \n" +
			");\n" +
			"commit;";
		TestUtil util = getTestUtil();
		this.dbConnection = util.getConnection();
		TestUtil.executeScript(dbConnection, sql);
		TableIdentifier tbl = dbConnection.getMetadata().findTable(new TableIdentifier("ROOT"));

		DeleteScriptGenerator generator = new DeleteScriptGenerator(dbConnection);
		generator.setFormatSql(false);
		generator.setTable(tbl);
		generator.setRemoveRedundant(true);

		List<ColumnData> pk = new ArrayList<ColumnData>();
		ColumnData id = new ColumnData("42", new ColumnIdentifier("R_ID", ColumnIdentifier.NO_TYPE_INFO));
		pk.add(id);
		List<String> script = generator.getStatementsForValues(pk, true);
		assertEquals(3, script.size());
		assertEquals("DELETE FROM DETAILS_ITEM WHERE R_ID = 42", script.get(0));
		assertEquals("DELETE FROM LEVEL1 WHERE R_ID = 42", script.get(1));
		assertEquals("DELETE FROM ROOT WHERE R_ID = 42", script.get(2));
	}

	@Test
	public void testGenerateScript()
		throws Exception
	{
		createSimpleTables();
		DeleteScriptGenerator generator = new DeleteScriptGenerator(dbConnection);
		TableIdentifier table = new TableIdentifier("PERSON");
		generator.setTable(table);
		List<ColumnData> pk = new ArrayList<ColumnData>();
		ColumnData id = new ColumnData(new Integer(1), new ColumnIdentifier("ID"));
		pk.add(id);
		CharSequence sql = generator.getScriptForValues(pk);
		ScriptParser parser = new ScriptParser(sql.toString());
		assertEquals(2, parser.getSize());
		String addressDelete = parser.getCommand(0);
		String addressTable = SqlUtil.getDeleteTable(addressDelete);
		Pattern p = Pattern.compile("\\s*person_id\\s*=\\s*1", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(addressDelete);
		assertEquals("ADDRESS", addressTable);
		assertTrue(m.find());

		String personTable = SqlUtil.getDeleteTable(parser.getCommand(1));
		assertEquals("PERSON", personTable);
	}

	@Test
	public void testGenerateStatements()
		throws Exception
	{
		createMultiColumnPkTables();
		DeleteScriptGenerator generator = new DeleteScriptGenerator(dbConnection);
		TableIdentifier table = new TableIdentifier("BASE");
		generator.setTable(table);
		List<ColumnData> pk = new ArrayList<ColumnData>();
		pk.add(new ColumnData(new Integer(1), new ColumnIdentifier("BASE_ID1")));
		pk.add(new ColumnData(new Integer(1), new ColumnIdentifier("BASE_ID2")));

		List<String> statements = generator.getStatementsForValues(pk, true);

		assertEquals(4, statements.size());

		Statement stmt = dbConnection.createStatement();
		for (String sql : statements)
		{
			stmt.executeUpdate(sql);
		}
		dbConnection.commit();

		String[] tables = new String[]
		{
			"BASE", "CHILD1", "CHILD2", "CHILD22"
		};

		for (String st : tables)
		{
			ResultSet rs = stmt.executeQuery("select count(*) from " + st);
			int count = -1;
			if (rs.next())
			{
				count = rs.getInt(1);
			}
			assertEquals("Wrong count in table: " + st, 1, count);
		}

		stmt.close();

		String sql = statements.get(3);
		String t = SqlUtil.getDeleteTable(sql);
		assertEquals("BASE", t);

		sql = statements.get(2);
		t = SqlUtil.getDeleteTable(sql);
		assertEquals("CHILD1", t);

		// Test when root table should not be included
		statements = generator.getStatementsForValues(pk, false);
		assertEquals(3, statements.size());
		sql = statements.get(2);
		t = SqlUtil.getDeleteTable(sql);
		assertEquals("CHILD1", t);
	}

	@Test
	public void testExpression()
		throws Exception
	{
		createSimpleTables();

		DeleteScriptGenerator generator = new DeleteScriptGenerator(dbConnection);
		generator.setFormatSql(false);
		TableIdentifier table = new TableIdentifier("PERSON");
		generator.setTable(table);

		List<ColumnData> pk = new ArrayList<ColumnData>();
		ColumnData id = new ColumnData("IN (1,2,3)", new ColumnIdentifier("ID"));
		pk.add(id);

		List<String> statements = generator.getStatementsForValues(pk, true);
		assertEquals(2, statements.size());
		assertEquals("DELETE FROM ADDRESS WHERE PERSON_ID IN (1,2,3)", statements.get(0));

		pk.clear();
		id = new ColumnData("between -1000 and -100", new ColumnIdentifier("ID"));
		pk.add(id);
		statements = generator.getStatementsForValues(pk, true);
		assertEquals(2, statements.size());
		assertEquals("DELETE FROM ADDRESS WHERE PERSON_ID between -1000 and -100", statements.get(0));
		assertEquals("DELETE FROM PERSON WHERE ID between -1000 and -100", statements.get(1));

		pk.clear();
		id = new ColumnData("< 0", new ColumnIdentifier("ID"));
		pk.add(id);
		statements = generator.getStatementsForValues(pk, true);
		assertEquals(2, statements.size());
		assertEquals("DELETE FROM ADDRESS WHERE PERSON_ID < 0", statements.get(0));
		assertEquals("DELETE FROM PERSON WHERE ID < 0", statements.get(1));
	}


	private void createMultiColumnPkTables()
		throws Exception
	{
		String sql = "CREATE TABLE base \n" +
					 "( \n" +
					 "   base_id1  integer  NOT NULL, \n" +
					 "   base_id2  integer  NOT NULL \n" +
					 "); \n" +
					 "ALTER TABLE base \n" +
					 "   ADD CONSTRAINT base_pkey PRIMARY KEY (base_id1, base_id2); \n" +

					 "CREATE TABLE child1 \n" +
					 "( \n" +
					 "   child1_id1  integer  NOT NULL, \n" +
					 "   child1_id2  integer  NOT NULL, \n" +
					 "   c1base_id1  integer  NOT NULL, \n" +
					 "   c1base_id2  integer  NOT NULL \n" +
					 "); \n" +

					 "ALTER TABLE child1 \n" +
					 "   ADD CONSTRAINT child1_pkey PRIMARY KEY (child1_id1, child1_id2); \n" +
					 " \n" +
					 "ALTER TABLE child1 \n" +
					 "  ADD CONSTRAINT fk_child1 FOREIGN KEY (c1base_id1, c1base_id2) \n" +
					 "  REFERENCES base (base_id1, base_id2); \n" +

					 "CREATE TABLE child2 \n" +
					 "( \n" +
					 "   child2_id1  integer  NOT NULL, \n" +
					 "   child2_id2  integer  NOT NULL, \n" +
					 "   c2c1_id1  integer  NOT NULL, \n" +
					 "   c2c1_id2  integer  NOT NULL \n" +
					 "); \n" +

					 "ALTER TABLE child2 \n" +
					 "   ADD CONSTRAINT child2_pkey PRIMARY KEY (child2_id1, child2_id2); \n" +
					 " \n" +
					 "ALTER TABLE child2 \n" +
					 "  ADD CONSTRAINT fk_child2 FOREIGN KEY (c2c1_id1, c2c1_id2) \n" +
					 "  REFERENCES child1 (child1_id1, child1_id2); \n" +

					 "CREATE TABLE child22 \n" +
					 "( \n" +
					 "   child22_id1  integer  NOT NULL, \n" +
					 "   child22_id2  integer  NOT NULL, \n" +
					 "   c22c1_id1  integer  NOT NULL, \n" +
					 "   c22c1_id2  integer  NOT NULL \n" +
					 "); \n" +

					 "ALTER TABLE child22 \n" +
					 "   ADD CONSTRAINT child22_pkey PRIMARY KEY (child22_id1, child22_id2); \n" +
					 " \n" +
					 "ALTER TABLE child22 \n" +
					 "  ADD CONSTRAINT fk_child22 FOREIGN KEY (c22c1_id1, c22c1_id2) \n" +
					 "  REFERENCES child1 (child1_id1, child1_id2); \n"
					 ;

		TestUtil util = new TestUtil("DependencyDeleter");
		this.dbConnection = util.getConnection();
		TestUtil.executeScript(dbConnection, sql);
		Statement stmt = this.dbConnection.createStatement();
		stmt.executeUpdate("insert into base (base_id1, base_id2) values (1,1)");
		stmt.executeUpdate("insert into base (base_id1, base_id2) values (2,2)");

		stmt.executeUpdate("insert into child1 (child1_id1, child1_id2, c1base_id1, c1base_id2) values (11,11,1,1)");
		stmt.executeUpdate("insert into child1 (child1_id1, child1_id2, c1base_id1, c1base_id2) values (12,12,2,2)");

		stmt.executeUpdate("insert into child2 (child2_id1, child2_id2, c2c1_id1, c2c1_id2) values (101,101,11,11)");
		stmt.executeUpdate("insert into child2 (child2_id1, child2_id2, c2c1_id1, c2c1_id2) values (102,102,12,12)");

		stmt.executeUpdate("insert into child22 (child22_id1, child22_id2, c22c1_id1, c22c1_id2) values (201,201,11,11)");
		stmt.executeUpdate("insert into child22 (child22_id1, child22_id2, c22c1_id1, c22c1_id2) values (202,202,12,12)");
		dbConnection.commit();
		stmt.close();
	}

	private void createSimpleTables()
		throws Exception
	{
		String sql =
					 "CREATE TABLE address \n" +
					 "( \n" +
					 "   id           integer  NOT NULL, \n" +
					 "   address_data varchar(100) not null, \n" +
					 "   person_id    integer \n" +
					 "); \n" +

					 "ALTER TABLE address \n" +
					 "   ADD CONSTRAINT address_pkey PRIMARY KEY (id); \n" +

					 "CREATE TABLE person \n" +
					 "( \n" +
					 "   id        integer         NOT NULL, \n" +
					 "   firstname varchar(50), \n" +
					 "   lastname  varchar(50) \n" +
					 "); \n" +

					 "ALTER TABLE person \n" +
					 "   ADD CONSTRAINT person_pkey PRIMARY KEY (id); \n" +
					 " \n" +

					 "ALTER TABLE address \n" +
					 "  ADD CONSTRAINT fk_pers FOREIGN KEY (person_id) \n" +
					 "  REFERENCES person (id); \n";


		TestUtil util = new TestUtil("DeleteScriptGenerator");
		this.dbConnection = util.getConnection();
		TestUtil.executeScript(dbConnection, sql);
	}

}