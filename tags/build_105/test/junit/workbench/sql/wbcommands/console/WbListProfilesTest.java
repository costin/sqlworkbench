/*
 * WbListProfilesTest.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2009, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.sql.wbcommands.console;

import workbench.WbTestCase;
import workbench.db.ConnectionMgr;
import workbench.db.ConnectionProfile;
import workbench.sql.StatementRunnerResult;
import workbench.util.StringUtil;

/**
 *
 * @author support@sql-workbench.net
 */
public class WbListProfilesTest
	extends WbTestCase
{

	public WbListProfilesTest(String testName)
	{
		super(testName);
	}

	public void testNeedsConnection()
	{
		WbListProfiles list = new WbListProfiles();
		assertFalse(list.isConnectionRequired());
	}

	public void testExecute()
		throws Exception
	{
		String sql = "WbListProfiles";
		WbListProfiles list = new WbListProfiles();
		StatementRunnerResult result = list.execute(sql);
		assertTrue(result.isSuccess());

		ConnectionProfile prof = new ConnectionProfile("TestProfile", "dummy.class", "jdbc:test", "user", "pwd");
		ConnectionMgr.getInstance().clearProfiles();
		ConnectionMgr.getInstance().addProfile(prof);
		result = list.execute(sql);
		assertTrue(result.isSuccess());

		String msg = result.getMessageBuffer().toString();
//		System.out.println("****\n" + msg);
		String[] lines = msg.split(StringUtil.REGEX_CRLF);
		assertEquals(2, lines.length);
		assertEquals("Default group", lines[0]);
		assertEquals("  TestProfile, User=user, URL=jdbc:test", lines[1]);
	}
}