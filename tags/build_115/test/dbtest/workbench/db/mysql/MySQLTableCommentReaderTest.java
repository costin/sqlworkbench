/*
 * MySQLTableCommentReaderTest.java
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
package workbench.db.mysql;

import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import workbench.TestUtil;
import workbench.WbTestCase;
import workbench.db.TableIdentifier;
import workbench.db.WbConnection;
import workbench.resource.Settings;
import static org.junit.Assert.*;

/**
 *
 * @author Thomas Kellerer
 */
public class MySQLTableCommentReaderTest
	extends WbTestCase
{

	public MySQLTableCommentReaderTest()
	{
		super("MySQLTableCommentReaderTest");
	}

	@BeforeClass
	public static void setUpClass()
		throws Exception
	{
		MySQLTestUtil.initTestcase("MySQLTableCommentReaderTest");
		WbConnection con = MySQLTestUtil.getMySQLConnection();
		if (con == null)
		{
			return;
		}
		String sql =
			"CREATE TABLE aaa_one (id integer) COMMENT 'first table';\n" +
			"CREATE TABLE bbb_two (id2 integer) COMMENT 'other table';";
		TestUtil.executeScript(con, sql);
	}

	@AfterClass
	public static void tearDownClass()
		throws Exception
	{
		WbConnection con = MySQLTestUtil.getMySQLConnection();
		if (con == null)
		{
			return;
		}
		String sql =
			"DROP TABLE aaa_one;\n" +
			"DROP TABLE bbb_two;";
		TestUtil.executeScript(con, sql);
		MySQLTestUtil.cleanUpTestCase();
	}

	@Test
	public void testReadRemarks()
		throws Exception
	{
		WbConnection con = MySQLTestUtil.getMySQLConnection();
		if (con == null)
		{
			return;
		}

		Settings.getInstance().setProperty("workbench.db.mysql.tablecomments.retrieve", true);

		List<TableIdentifier> tables = con.getMetadata().getTableList();
		assertEquals(2, tables.size());
		assertEquals("first table", tables.get(0).getComment());
		assertEquals("other table", tables.get(1).getComment());
	}
}