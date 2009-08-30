/*
 * WbModeTest
 * 
 *  This file is part of SQL Workbench/J, http://www.sql-workbench.net
 * 
 *  Copyright 2002-2009, Thomas Kellerer
 *  No part of this code maybe reused without the permission of the author
 * 
 *  To contact the author please send an email to: support@sql-workbench.net
 */
package workbench.sql.wbcommands;

import java.sql.ResultSet;
import java.sql.Statement;
import junit.framework.TestCase;
import workbench.TestUtil;
import workbench.db.ConnectionMgr;
import workbench.db.WbConnection;
import workbench.interfaces.ExecutionController;
import workbench.sql.BatchRunner;
import workbench.util.SqlUtil;

/**
 *
 * @author Thomas Kellerer
 */
public class WbModeTest
	extends TestCase
{

	public WbModeTest(String testName)
	{
		super(testName);
	}

	public void testExecute()
		throws Exception
	{
		TestUtil util = new TestUtil("WbModeTest");

		try
		{
			WbConnection con = util.getConnection();
			TestUtil.executeScript(con,
				"create table mode_test (id integer primary key, some_value varchar(100));\n" +
				"insert into mode_test values (1, 'one');\n" +
				"insert into mode_test values (2, 'two');\n" +
				"commit;\n"
			);

			BatchRunner runner = new BatchRunner();
			runner.setConnection(con);
			runner.setVerboseLogging(false);
			runner.executeScript(
				"WbMode readonly;\n" +
				"delete from mode_test;\n" +
				"commit;\n"
			);

			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("select count(*) from mode_test");
			assertTrue(rs.next());
			int count = rs.getInt(1);
			assertEquals(count, 2);
			SqlUtil.closeResult(rs);
			
			Controller controll = new Controller();
			runner.setExecutionController(controll);

			runner.executeScript(
				"WbMode confirm;\n" +
				"delete from mode_test;\n" +
				"commit;\n"
			);

			assertEquals(2, controll.confirmStatementCalled);

			rs = stmt.executeQuery("select count(*) from mode_test");
			assertTrue(rs.next());
			count = rs.getInt(1);
			assertEquals(count, 0);
			SqlUtil.closeResult(rs);
			SqlUtil.closeStatement(stmt);
		}
		finally
		{
			ConnectionMgr.getInstance().disconnectAll();
		}
	}

	private class Controller
		implements ExecutionController
	{
		public int confirmStatementCalled;
		public int confirmCalled;

		public Controller()
		{
			confirmStatementCalled = 0;
			confirmCalled = 0;
		}

		public boolean confirmStatementExecution(String command)
		{
			confirmStatementCalled ++;
			return true;
		}

		public boolean confirmExecution(String prompt)
		{
			confirmCalled ++;
			return true;
		}

		public String getPassword(String prompt)
		{
			return "";
		}
	}
}
