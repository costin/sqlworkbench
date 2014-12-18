/*
 * LobFileStatementTest.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2006, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.util;

import java.io.File;
import junit.framework.*;

/**
 *
 * @author support@sql-workbench.net
 */
public class LobFileStatementTest extends TestCase
{
	
	public LobFileStatementTest(String testName)
	{
		super(testName);
	}

	public void testGetParameterCount()
	{
		File f = new File("c:/temp/test.data");
		try
		{
			// LobFileStatement checks for the presence of the file!
			f.createNewFile();
			String sql = "update bla set col = {$blobfile=c:/temp/test.data} where x = 1";
			LobFileStatement stmt = new LobFileStatement(sql);
			assertEquals("Wrong parameter count", 1, stmt.getParameterCount());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("could not parse statement");
		}
		finally
		{
			f.delete();
		}
	}

	
	public void testGetPreparedSql()
	{
		File f = new File("c:/temp/test.data");
		try
		{
			// LobFileStatement checks for the presence of the file!
			f.createNewFile();
			
			
			String sql = "update bla set col = {$blobfile=c:/temp/test.data} where x = 1";
			LobFileStatement stmt = new LobFileStatement(sql);
			String newSql = stmt.getPreparedSql();
			assertEquals("Wrong SQL generated", "update bla set col =  ?  where x = 1", newSql);
			
			sql = "update bla set col = {$clobfile=c:\\temp\\test.data} where x = 1";
			stmt = new LobFileStatement(sql);
			assertEquals("Wrong SQL generated", "update bla set col =  ?  where x = 1", stmt.getPreparedSql());
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("could not parse statement");
		}
		finally
		{
			f.delete();
		}
	}
	
}