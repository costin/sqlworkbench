/*
 * SelectAnalyzerTest.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2018, Thomas Kellerer
 *
 * Licensed under a modified Apache License, Version 2.0
 * that restricts the use for certain governments.
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at.
 *
 *     http://sql-workbench.net/manual/license.html
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

import java.util.List;

import workbench.WbTestCase;

import workbench.db.TableIdentifier;

import workbench.util.TableAlias;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Thomas Kellerer
 */
public class SelectAnalyzerTest
	extends WbTestCase
{

	public SelectAnalyzerTest()
	{
		super("SelectAnalyzerTest");
	}


  @Test
  public void testDerivedTable()
  {
    String sql =
      "select *\n" +
      "from (\n" +
      "  select ct.name, ct.population, row_count() over (order by ct.population) as rn\n" +
      "  from city ct \n" +
      "    join country cy ON ct. \n" +
      "  where cy.continent = 'Europe'\n" +
      ") t;";

    int pos = sql.indexOf("ON ct.") + 6;
		StatementContext context = new StatementContext(null, sql, pos, false);
		BaseAnalyzer analyzer = context.getAnalyzer();
		analyzer.checkContext();
    assertEquals(BaseAnalyzer.CONTEXT_COLUMN_LIST, analyzer.getContext());
    TableIdentifier tbl = analyzer.getTableForColumnList();
    assertEquals("city", tbl.getTableName());
  }

  @Test
  public void testPosition()
  {
    String sql =
      "select id, firstname, lastname, comment\n" +
      "from address as x\n" +
      "order by id";

    int pos = sql.indexOf('x');
		StatementContext context = new StatementContext(null, sql, pos, false);
		BaseAnalyzer analyzer = context.getAnalyzer();
		analyzer.checkContext();
    assertEquals(BaseAnalyzer.CONTEXT_TABLE_LIST, analyzer.getContext());
  }

  @Test
  public void testAlias()
  {
    String sql =
      "select \n" +
      "from some_table t1\n" +
      "  join join other_table t2 on t1.id = t2.id";
    int pos = 7;
		StatementContext context = new StatementContext(null, sql, pos, false);
		BaseAnalyzer analyzer = context.getAnalyzer();
		analyzer.checkContext();
    List data = analyzer.getData();
    assertNotNull(data);
    assertEquals(2, data.size());
    assertEquals("t1 (some_table)", data.get(0).toString());
    assertEquals("t2 (other_table)", data.get(1).toString());
  }

	@Test
	public void testUnion()
	{
		String sql = "select f. from foo f union select b. from bar b";
		int pos = sql.indexOf("f.") + 2;
		StatementContext context = new StatementContext(null, sql, pos, false);
		BaseAnalyzer analyzer = context.getAnalyzer();
		analyzer.checkContext();
		TableIdentifier tbl = analyzer.getTableForColumnList();
		assertNotNull(tbl);
		assertEquals("foo", tbl.getTableName().toLowerCase());

		pos = sql.indexOf("b.") + 2;
		context = new StatementContext(null, sql, pos, false);
		analyzer = context.getAnalyzer();
		analyzer.checkContext();
		tbl = analyzer.getTableForColumnList();
		assertNotNull(tbl);
		assertEquals("bar", tbl.getTableName().toLowerCase());

		sql = "select f. from foo f except select b. from bar b";
		pos = sql.indexOf("b.") + 2;
		context = new StatementContext(null, sql, pos, false);
		analyzer = context.getAnalyzer();
		analyzer.checkContext();
		tbl = analyzer.getTableForColumnList();
		assertNotNull(tbl);
		assertEquals("bar", tbl.getTableName().toLowerCase());

		sql = "select * from  minus select b. from bar b";
		context = new StatementContext(null, sql, 13, false);
		analyzer = context.getAnalyzer();
		analyzer.checkContext();
		int ctx = analyzer.getContext();
		assertEquals(BaseAnalyzer.CONTEXT_TABLE_LIST, ctx);

		sql = "select f. from foo f intersect select b. from bar b";
		pos = sql.indexOf("b.") + 2;
		context = new StatementContext(null, sql, pos, false);
		analyzer = context.getAnalyzer();
		analyzer.checkContext();
		tbl = analyzer.getTableForColumnList();
		assertNotNull(tbl);
		assertEquals("bar", tbl.getTableName().toLowerCase());
	}

	@Test
	public void testUnion2()
	{
		String sql =
			"select * \n" +
			"from (\n" +
			"  select t1. from t1\n" +
			"  union \n" +
			"  select t2. from t2\n" +
			") u1";

		int pos = sql.indexOf("t1.") + 3;
		StatementContext context = new StatementContext(null, sql, pos, false);
		BaseAnalyzer analyzer = context.getAnalyzer();
		analyzer.checkContext();
		TableIdentifier tbl = analyzer.getTableForColumnList();
		assertNotNull(tbl);
		assertEquals("t1", tbl.getTableName().toLowerCase());

		pos = sql.indexOf("t2.") + 3;
		context = new StatementContext(null, sql, pos, false);
		analyzer = context.getAnalyzer();
		analyzer.checkContext();
		tbl = analyzer.getTableForColumnList();
		assertNotNull(tbl);
		assertEquals("t2", tbl.getTableName().toLowerCase());
	}

	@Test
	public void testWhere()
	{
		String sql = "select id2 from two where two. ";
		int pos = sql.indexOf("one.") + 4;
		StatementContext context = new StatementContext(null, sql, pos, false);
		BaseAnalyzer analyzer = context.getAnalyzer();
		analyzer.checkContext();
		TableIdentifier tbl = analyzer.getTableForColumnList();
		assertEquals("two", tbl.getTableName().toLowerCase());
	}


	@Test
	public void testJoin()
	{
		String sql =
			"select b. \n" +
			" from public.t1 a join public.t2 as b using (id)";
		int pos = sql.indexOf('.') + 1;
		StatementContext ctx = new StatementContext(null, sql, pos, false);
		BaseAnalyzer analyzer = ctx.getAnalyzer();
		analyzer.checkContext();
		int context = analyzer.getContext();
		assertEquals(BaseAnalyzer.CONTEXT_COLUMN_LIST, context);
		TableIdentifier tbl = analyzer.getTableForColumnList();
		assertEquals("t2", tbl.getTableName());
	}

	@Test
	public void testJoin2()
	{
		String sql =
              "select * \n" +
              "from  \n" +
              "  join y on x.id = y.xid \n" +
              "  join z on x.id = z.yid";
		int pos = sql.indexOf("from") + 5;
		StatementContext ctx = new StatementContext(null, sql, pos, false);
		BaseAnalyzer analyzer = ctx.getAnalyzer();
		analyzer.checkContext();
		int context = analyzer.getContext();
		assertEquals(BaseAnalyzer.CONTEXT_TABLE_LIST, context);
	}

	@Test
	public void testJoin3()
	{
    String sql =
      "\n" +
      "  select ct.name, ct.population, dense_rank() over (order by ct.population desc) as rnk\n" +
      "  from city ct \n" +
      "    join country cy ON ct. \n" +
      "  where cy.continent = 'Europe'\n";

		int pos = sql.indexOf("ON ct.") + 6;
		StatementContext ctx = new StatementContext(null, sql, pos, false);
		BaseAnalyzer analyzer = ctx.getAnalyzer();
		analyzer.checkContext();
		int context = analyzer.getContext();
		assertEquals(BaseAnalyzer.CONTEXT_COLUMN_LIST, context);
		TableIdentifier tbl = analyzer.getTableForColumnList();
		assertEquals("city", tbl.getTableName());
	}

	@Test
	public void testCTE()
	{
		String sql =
			"with foobar as (\n" +
			"  select t1.c1, t2.  \n" +
			"  from table1 t1\n" +
			"    join table2 t2 on t1.id = t2.id1\n" +
			")\n" +
			"select *\n" +
			"from foobar;";
		int pos = sql.indexOf("t2.") + 3;
		StatementContext ctx = new StatementContext(null, sql, pos, false);
		BaseAnalyzer analyzer = ctx.getAnalyzer();
		analyzer.checkContext();
		int context = analyzer.getContext();
		assertEquals(BaseAnalyzer.CONTEXT_COLUMN_LIST, context);
		TableIdentifier tbl = analyzer.getTableForColumnList();
		assertEquals("table2", tbl.getTableName());
	}

	@Test
	public void testSpaces()
	{
		String sql = "SELECT x. FROM \"Dumb Named Schema\".\"Problematically Named Table\" x";
		SelectAnalyzer analyzer = new SelectAnalyzer(null, sql, sql.indexOf(" FROM"));
		List<TableAlias> tables = analyzer.getTables();
		assertEquals(1, tables.size());
		TableAlias alias = tables.get(0);
		TableIdentifier tbl = alias.getTable();
		assertEquals("\"Dumb Named Schema\".\"Problematically Named Table\"", tbl.getTableExpression());
		assertEquals("Dumb Named Schema", tbl.getSchema());
		assertEquals("Problematically Named Table", tbl.getTableName());
		assertEquals("x", alias.getAlias());
		analyzer.checkContext();
		tbl = analyzer.getTableForColumnList();
		assertEquals("\"Dumb Named Schema\".\"Problematically Named Table\"", tbl.getTableExpression());
	}

	@Test
	public void testAnalyzer()
	{
		String sql = "SELECT a.att1\n      ,a.\nFROM   adam   a";
		SelectAnalyzer analyzer = new SelectAnalyzer(null, sql, 23);
		String quali = analyzer.getQualifierLeftOfCursor();
		assertEquals("Wrong qualifier detected", "a", quali);

		sql = "SELECT a.att1 FROM t1 a JOIN t2 b ON a.id = b.id";
		int pos = sql.indexOf("a.id") + 2;

		analyzer = new SelectAnalyzer(null, sql, pos);
		analyzer.checkContext();
		TableIdentifier tbl = analyzer.getTableForColumnList();
		assertNotNull(tbl);
		assertEquals("t1", tbl.getTableName());

		sql = "SELECT a.att1 FROM t1 a JOIN t2 b ON (a.id = b.id)";
		pos = sql.indexOf("a.id") + 2;
		analyzer = new SelectAnalyzer(null, sql, pos);
		analyzer.checkContext();
		tbl = analyzer.getTableForColumnList();
		assertNotNull(tbl);
		assertEquals("t1", tbl.getTableName());

		sql = "SELECT a.att1 FROM t1 a JOIN t2 b ON (a.id = b.id)";
		pos = sql.indexOf("FROM") + "FROM".length() + 1;
		analyzer = new SelectAnalyzer(null, sql, pos);
		analyzer.checkContext();
		tbl = analyzer.getTableForColumnList();
		assertNull(tbl);
		assertEquals(BaseAnalyzer.CONTEXT_TABLE_LIST, analyzer.context);

		pos = sql.indexOf("JOIN") + "JOIN".length() + 1;
		analyzer = new SelectAnalyzer(null, sql, pos);
		analyzer.checkContext();
		tbl = analyzer.getTableForColumnList();
		assertNull(tbl);
		assertEquals(BaseAnalyzer.CONTEXT_TABLE_LIST, analyzer.context);

		sql = "SELECT * \n" +
				 "  FROM person p \n" +
				 "   JOIN address a on a.person_id = p.id \n" +
				 "   JOIN \n" +
				 "   JOIN author at on at.author_id = p.id";

		pos = sql.indexOf("JOIN \n") + 5;
		analyzer = new SelectAnalyzer(null, sql, pos);
		analyzer.checkContext();
		assertEquals(BaseAnalyzer.CONTEXT_TABLE_LIST, analyzer.context);

		sql = "SELECT * \n" +
				 "  FROM person p \n" +
				 "   JOIN \n" +
				 "  WHERE p.id = 42";

		pos = sql.indexOf("JOIN \n") + 5;
		analyzer = new SelectAnalyzer(null, sql, pos);
		analyzer.checkContext();
		assertEquals(BaseAnalyzer.CONTEXT_TABLE_LIST, analyzer.context);
	}

	@Test
	public void testNonStandardNames()
	{
		String select = "select  from #some_table";
		SelectAnalyzer analyzer = new SelectAnalyzer(null, select, 7);
		analyzer.checkContext();
		assertEquals(BaseAnalyzer.CONTEXT_COLUMN_LIST, analyzer.getContext());
		assertEquals("#some_table", analyzer.getTableForColumnList().getTableName());

		select = "select * from #some_schema.";
		int pos = select.indexOf('.') + 1;
		analyzer = new SelectAnalyzer(null, select, pos);
		analyzer.checkContext();
		assertEquals(BaseAnalyzer.CONTEXT_TABLE_LIST, analyzer.getContext());
		assertEquals("#some_schema", analyzer.getSchemaForTableList());
	}

  @Test
	public void testOrderBy()
	{
		String sql =
				"select sum(f.c1) as c1_total, \n" +
				"       sum(f.c2) as c2_total, \n" +
				"       f.id, \n" +
				"       b.foo \n" +
				"from foo f \n" +
				"  join bar b on f.id = b.fid \n" +
				"where b.foo NOT IN (1,2,3) \n" +
				"group by f.id, b.foo \n" +
				"order by  ";

		int pos = sql.indexOf("order by") + "order by".length() + 1;
		SelectAnalyzer analyzer = new SelectAnalyzer(null, sql, pos);
		analyzer.checkContext();
		assertEquals(BaseAnalyzer.CONTEXT_COLUMN_LIST, analyzer.getContext());
	}

  @Test
  public void testJoinColumns()
  {
    String sql =
      "select * \n" +
      "from orders as o\n" +
      "  join orderentries as oe on oe. = o.pk";
    int pos = sql.indexOf("on oe.") + 6;
    SelectAnalyzer analyzer = new SelectAnalyzer(null, sql, pos);
    analyzer.checkContext();
    assertEquals(BaseAnalyzer.CONTEXT_COLUMN_LIST, analyzer.getContext());
    assertNotNull(analyzer.getTableForColumnList());
    assertEquals("orderentries", analyzer.getTableForColumnList().getTableName().toLowerCase());
  }

  @Test
  public void testJoinTables()
  {
    String sql =
      "select * \n" +
      "from orders as o\n" +
      "  join orderentries as oe on  = o.pk";
    int pos = sql.indexOf("on ") + 3;
    SelectAnalyzer analyzer = new SelectAnalyzer(null, sql, pos);
    analyzer.checkContext();
    assertEquals(BaseAnalyzer.CONTEXT_FROM_LIST, analyzer.getContext());
    List data = analyzer.getData();
    assertNotNull(data);
    assertEquals(2, data.size());
    TableAlias t = (TableAlias)data.get(0);
    assertEquals("orders", t.getObjectName());
    t = (TableAlias)data.get(1);
    assertEquals("orderentries", t.getObjectName());
  }
}
