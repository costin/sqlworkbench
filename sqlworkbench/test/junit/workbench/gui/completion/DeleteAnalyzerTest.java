/*
 * DeleteAnalyzerTest.java
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
package workbench.gui.completion;


import org.junit.Test;
import static org.junit.Assert.*;

import workbench.WbTestCase;
import workbench.db.TableIdentifier;

/**
 *
 * @author Thomas Kellerer
 */
public class DeleteAnalyzerTest
	extends WbTestCase
{
	public DeleteAnalyzerTest()
	{
		super("DeleteAnalyzer");
	}

	@Test
	public void testGetTable()
	{
		String sql = "DELETE FROM public.sometable WHERE foo = 1";
		DeleteAnalyzer analyzer = new DeleteAnalyzer(null, sql, sql.indexOf("foo") - 1);
		analyzer.checkContext();
		TableIdentifier table = analyzer.getTableForColumnList();
		assertNotNull(table);
		assertEquals("public", table.getSchema());
		assertEquals("sometable", table.getTableName());
	}

	@Test
	public void testAlternateSeparator()
	{
		String sql = "DELETE FROM mylib/sometable WHERE foo = 1";
		DeleteAnalyzer analyzer = new DeleteAnalyzer(null, sql, sql.indexOf("foo") - 1);
		analyzer.setCatalogSeparator('/');
		analyzer.checkContext();
		TableIdentifier table = analyzer.getTableForColumnList();
		assertNotNull(table);
		assertEquals("mylib", table.getCatalog());
		assertEquals("sometable", table.getTableName());
	}
}
