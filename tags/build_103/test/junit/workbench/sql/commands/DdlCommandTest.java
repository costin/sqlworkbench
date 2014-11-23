/*
 * DdlCommandTest.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2009, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.sql.commands;

import junit.framework.TestCase;
import workbench.TestUtil;
import workbench.sql.StatementRunner;
import workbench.sql.StatementRunnerResult;

/**
 *
 * @author support@sql-workbench.net
 */
public class DdlCommandTest 
	extends TestCase
{
	
	public DdlCommandTest(String testName)
	{
		super(testName);
	}

	public void testIgnoreDropErrors()
	{
		try
		{
			TestUtil util = new TestUtil("ignoreDrop");
			StatementRunner runner = util.createConnectedStatementRunner();
			String sql = "drop table does_not_exist";
			runner.setIgnoreDropErrors(true);
			runner.runStatement(sql);
			StatementRunnerResult result = runner.getResult();
			assertTrue(result.isSuccess());
			
			runner.setIgnoreDropErrors(false);
			runner.setUseSavepoint(true);
			runner.runStatement(sql);
			result = runner.getResult();
			assertFalse(result.isSuccess());
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	
}