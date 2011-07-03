/*
 * OracleUniqueConstraintReaderTest
 *
 *  This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 *  Copyright 2002-2011, Thomas Kellerer
 *  No part of this code may be reused without the permission of the author
 *
 *  To contact the author please send an email to: support@sql-workbench.net
 */
package workbench.db.oracle;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import workbench.TestUtil;
import workbench.WbTestCase;
import workbench.db.IndexDefinition;
import workbench.db.TableIdentifier;
import workbench.db.WbConnection;

/**
 *
 * @author Thomas Kellerer
 */
public class OracleUniqueConstraintReaderTest
	extends WbTestCase
{

	public OracleUniqueConstraintReaderTest()
	{
		super("OracleUniqueConstraintReaderTest");
	}

	@BeforeClass
	public static void setUpClass()
		throws Exception
	{
		OracleTestUtil.initTestCase();
		WbConnection conn = OracleTestUtil.getOracleConnection();
		if (conn == null) return;

		String sql =
			"CREATE TABLE parent \n" +
			"( \n" +
			"   id          integer    NOT NULL PRIMARY KEY, \n" +
			"   unique_id1  integer, \n" +
			"   unique_id2  integer \n" +
			"); \n" +
			"ALTER TABLE parent \n" +
			"   ADD CONSTRAINT uk_id1_id2 UNIQUE (unique_id1, unique_id2); \n" +
			" \n" +
			" \n" +
			"COMMIT;";
		TestUtil.executeScript(conn, sql);
	}

	@AfterClass
	public static void tearDownClass()
		throws Exception
	{
		OracleTestUtil.cleanUpTestCase();
	}

	@Test
	public void testProcessIndexList()
	{
		WbConnection con = OracleTestUtil.getOracleConnection();
		if (con == null) return;
		TableIdentifier parent = con.getMetadata().findObject(new TableIdentifier("PARENT"));
		List<IndexDefinition> indexList = con.getMetadata().getIndexReader().getTableIndexList(parent);
		boolean foundConstraint = false;
		for (IndexDefinition idx : indexList)
		{
			if (idx.getName().equals("UK_ID1_ID2"))
			{
				assertTrue(idx.isUniqueConstraint());
				assertEquals("UK_ID1_ID2", idx.getUniqueConstraintName());
				foundConstraint = true;
			}
		}
		assertTrue(foundConstraint);
	}
}
