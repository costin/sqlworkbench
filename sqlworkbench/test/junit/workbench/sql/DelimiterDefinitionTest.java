/*
 * DelimiterDefinitionTest.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2014, Thomas Kellerer
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
package workbench.sql;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author thomas
 */
public class DelimiterDefinitionTest
{

	@Test
	public void testRemove()
	{
		String sql =
			"delete from foo\n" +
			"/";
		String clean = DelimiterDefinition.DEFAULT_ORA_DELIMITER.removeDelimiter(sql);
		String expected = "delete from foo";
		System.out.println(clean);
		assertEquals(expected, clean);
	}

	@Test
	public void testTerminatesScript()
		throws Exception
	{
		String sql = "delete from thetable\nGO\n";
		assertTrue(DelimiterDefinition.DEFAULT_MS_DELIMITER.terminatesScript(sql, false));
		sql = "delete from thetable\nGO";
		assertTrue(DelimiterDefinition.DEFAULT_MS_DELIMITER.terminatesScript(sql, false));

		sql = "create or replace procedure my_test \n" +
				"as \n" +
				"begin \n" +
				"  null;" +
				"end; \n" +
				" / ";
		assertTrue(DelimiterDefinition.DEFAULT_ORA_DELIMITER.terminatesScript(sql, false));
		DelimiterDefinition del = new DelimiterDefinition("/", false);
		assertTrue(del.terminatesScript(sql, false));
	}

	@Test
	public void testDelimiter()
	{
		try
		{
			DelimiterDefinition d = new DelimiterDefinition();
			assertEquals(true, d.isEmpty());
			assertEquals(false, d.isSingleLine());

			d.setDelimiter(";");
			assertEquals(true, d.isStandard());

			d.setDelimiter(" ; ");
			assertEquals(true, d.isStandard());

			d = new DelimiterDefinition("/", true);
			assertEquals(false, d.isStandard());
			assertEquals(true, d.isSingleLine());

			d = new DelimiterDefinition("   / \n", true);
			assertEquals("/", d.getDelimiter());
			assertEquals(true, d.isSingleLine());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testParse()
	{
		try
		{
			DelimiterDefinition d = DelimiterDefinition.parseCmdLineArgument("/;nl");
			assertEquals(false, d.isEmpty());
			assertEquals(true, d.isSingleLine());
			assertEquals("/", d.getDelimiter());

			d = DelimiterDefinition.parseCmdLineArgument("/;bla");
			assertEquals(false, d.isEmpty());
			assertEquals(false, d.isSingleLine());
			assertEquals("/", d.getDelimiter());

			d = DelimiterDefinition.parseCmdLineArgument("/   ");
			assertEquals(false, d.isEmpty());
			assertEquals(false, d.isSingleLine());
			assertEquals("/", d.getDelimiter());

			d = DelimiterDefinition.parseCmdLineArgument("GO:nl");
			assertEquals(false, d.isEmpty());
			assertEquals(true, d.isSingleLine());
			assertEquals("GO", d.getDelimiter());

			d = DelimiterDefinition.parseCmdLineArgument("/:nl");
			assertEquals(false, d.isEmpty());
			assertEquals(true, d.isSingleLine());
			assertEquals("/", d.getDelimiter());

			d = DelimiterDefinition.parseCmdLineArgument("oracle");
			assertEquals(false, d.isEmpty());
			assertEquals(true, d.isSingleLine());
			assertEquals("/", d.getDelimiter());

			d = DelimiterDefinition.parseCmdLineArgument("mssql");
			assertEquals(false, d.isEmpty());
			assertEquals(true, d.isSingleLine());
			assertEquals("GO", d.getDelimiter());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
