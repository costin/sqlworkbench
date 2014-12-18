/*
 * ConstantColumnValuesTest.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2008, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db.importer;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import workbench.TestUtil;
import workbench.db.ColumnIdentifier;
import workbench.db.WbConnection;
import workbench.util.SqlUtil;
import workbench.util.ValueConverter;

/**
 *
 * @author support@sql-workbench.net
 */
public class ConstantColumnValuesTest extends TestCase
{
	
	public ConstantColumnValuesTest(String testName)
	{
		super(testName);
	}
	
	public void testGetStaticValues()
	{
		List<ColumnIdentifier> columns = new ArrayList<ColumnIdentifier>();
		columns.add(new ColumnIdentifier("test_run_id", java.sql.Types.INTEGER));
		columns.add(new ColumnIdentifier("title", java.sql.Types.VARCHAR));
		columns.add(new ColumnIdentifier("modified", java.sql.Types.TIMESTAMP));
		columns.add(new ColumnIdentifier("t2", java.sql.Types.VARCHAR));
		columns.add(new ColumnIdentifier("t3", java.sql.Types.VARCHAR));
		columns.add(new ColumnIdentifier("t4", java.sql.Types.VARCHAR));
		columns.add(new ColumnIdentifier("id", java.sql.Types.TIMESTAMP));
		try
		{
			ConstantColumnValues values = new ConstantColumnValues("test_run_id=42,title=\"hello, world\",modified=current_timestamp,t2='bla',t3=''bla'',id=${current_timestamp},t4='${ant.var}'", columns);
			assertEquals(7, values.getColumnCount());
			assertEquals(new Integer(42), values.getValue(0));
			assertEquals("hello, world", values.getValue(1));
			assertEquals(true, values.getValue(2) instanceof java.sql.Timestamp);
			assertEquals("bla", values.getValue(3));
			assertEquals("'bla'", values.getValue(4));
			assertEquals("current_timestamp", values.getFunctionLiteral(5));
			assertEquals("${ant.var}", values.getValue(6));
			
			assertEquals(true, values.removeColumn(new ColumnIdentifier("t2", java.sql.Types.VARCHAR)));
			assertEquals(false, values.removeColumn(new ColumnIdentifier("kkk", java.sql.Types.VARCHAR)));
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			fail(ex.getMessage());
		}
	}

	
	public void testInitFromDb()
	{
		TestUtil util = new TestUtil("testConstants");
		WbConnection con = null;
		String tablename = "constant_test";
		Statement stmt = null;
		try
		{
			con = util.getConnection("cons_test");
			stmt = con.createStatement();
			stmt.executeUpdate("create table constant_test (test_run_id integer, title varchar(20))");
			ValueConverter converter = new ValueConverter("yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss");
			ConstantColumnValues values = new ConstantColumnValues("test_run_id=42,title=\"hello, world\"", con, tablename, converter);
			assertEquals(2, values.getColumnCount());
			assertEquals(new Integer(42), values.getValue(0));
			assertEquals("hello, world", values.getValue(1));
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			fail(ex.getMessage());
		}
		finally
		{
			SqlUtil.closeStatement(stmt);
			try { con.disconnect(); } catch (Throwable th) {}
		}
	}
	
}