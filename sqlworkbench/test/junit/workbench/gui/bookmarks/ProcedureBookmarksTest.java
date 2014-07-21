/*
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2014 Thomas Kellerer.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * To contact the author please send an email to: support@sql-workbench.net
 */

package workbench.gui.bookmarks;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Thomas Kellerer
 */
public class ProcedureBookmarksTest
{

	public ProcedureBookmarksTest()
	{
	}

	@Before
	public void setUp()
	{
	}

	@After
	public void tearDown()
	{
	}

	@Test
	public void testParseScript()
	{
		String script =
			"-- this is a test proc\n" +
			"create or replace function foo(p_foo integer) return boolean as begin return 42; end;\n" +
			"\n" +
			"-- another proc\n" +
			"drop procedure bar;\n" +
			"create procedure bar as begin null; end;\n";
		ProcedureBookmarks parser = new ProcedureBookmarks();
		parser.parseScript(script);
		List<NamedScriptLocation> bookmarks = parser.getBookmarks();
		System.out.println(bookmarks);
		assertEquals(2, bookmarks.size());
	}

}
