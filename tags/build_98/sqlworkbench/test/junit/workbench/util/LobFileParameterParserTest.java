/*
 * LobFileParameterParserTest.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2008, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.util;

import junit.framework.*;

/**
 *
 * @author support@sql-workbench.net
 */
public class LobFileParameterParserTest extends TestCase
{
	
	public LobFileParameterParserTest(String testName)
	{
		super(testName);
	}

	public void testGetParameters()
	{
		try
		{
			String sql = "update bla set col = {$blobfile=c:/temp/test.data} where x=1";
			
			LobFileParameterParser p = new LobFileParameterParser(sql);
			LobFileParameter[] parms = p.getParameters();
			assertNotNull(parms);
			assertEquals("File not recognized", 1, parms.length);
			assertEquals("Wrong filename", "c:/temp/test.data", parms[0].getFilename());
			
			sql = "update bla set col = {$clobfile=c:/temp/test.data encoding=UTF8} where x=1";
			p = new LobFileParameterParser(sql);
			parms = p.getParameters();
			assertNotNull(parms);
			assertEquals("File not recognized", 1, parms.length);
			assertEquals("Wrong filename", "c:/temp/test.data", parms[0].getFilename());
			assertEquals("Wrong encoding", "UTF8", parms[0].getEncoding());

			sql = "update bla set col = {$clobfile='c:/my data/test.data' encoding='UTF-8'} where x=1";
			p = new LobFileParameterParser(sql);
			parms = p.getParameters();
			assertNotNull(parms);
			assertEquals("File not recognized", 1, parms.length);
			assertEquals("Wrong filename", "c:/my data/test.data", parms[0].getFilename());
			assertEquals("Wrong encoding", "UTF-8", parms[0].getEncoding());
			
			sql = "{$blobfile=c:/temp/test.data}";
			p = new LobFileParameterParser(sql);
			parms = p.getParameters();
			assertNotNull(parms);
			assertEquals("File not recognized", 1, parms.length);
			assertEquals("Wrong filename returned", "c:/temp/test.data", parms[0].getFilename());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Could not retrieve blobfile parameter");
		}
	}
	
}