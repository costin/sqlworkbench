/*
 * StatementContextTest.java
 * JUnit based test
 *
 * Created on 2. August 2007, 22:28
 */

package workbench.gui.completion;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import junit.framework.TestCase;
import workbench.TestUtil;
import workbench.db.ColumnIdentifier;
import workbench.db.ConnectionMgr;
import workbench.db.TableIdentifier;
import workbench.db.WbConnection;
import workbench.util.SqlUtil;

/**
 *
 * @author thomas
 */
public class StatementContextTest extends TestCase
{
	private TestUtil util;
	private WbConnection con;
	
	public StatementContextTest(String testName)
	{
		super(testName);
	}

  protected void setUp()
    throws Exception
  {
    super.setUp();
		util = new TestUtil("InsertAnalyzerTest");
		con = util.getConnection("completion_test");
		prepareDatabase(con);
  }

  protected void tearDown()
    throws Exception
  {
		util.emptyBaseDirectory();
		ConnectionMgr.getInstance().disconnectAll();
    super.tearDown();
  }
	
  public void testSelectColumnList()
  {
		
		try
		{
			StatementContext context = new StatementContext(con, "select  from one", 7);
			BaseAnalyzer analyzer = context.getAnalyzer();
			assertTrue(analyzer instanceof SelectAnalyzer);
			List objects = analyzer.getData();
			assertNotNull(objects);
			assertEquals(4, objects.size());
			Object o = objects.get(1);
			assertTrue(o instanceof ColumnIdentifier);
			ColumnIdentifier c = (ColumnIdentifier)o;
			assertEquals("firstname", c.getColumnName().toLowerCase());
			
			context = new StatementContext(con, "select * from one where  ", 24);
			analyzer = context.getAnalyzer();
			assertTrue(analyzer instanceof SelectAnalyzer);
			objects = analyzer.getData();
			assertNotNull(objects);
			assertEquals(3, objects.size());
			o = objects.get(0);
			assertTrue(o instanceof ColumnIdentifier);
			c = (ColumnIdentifier)o;
			assertEquals("firstname", c.getColumnName().toLowerCase());
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
  }

  public void testSelectTableList()
  {
		
		try
		{
			StatementContext context = new StatementContext(con, "select * from ", 14);
			BaseAnalyzer analyzer = context.getAnalyzer();
			assertTrue(analyzer instanceof SelectAnalyzer);
			List objects = analyzer.getData();
			assertNotNull(objects);
			assertEquals(3, objects.size());
			Object o = objects.get(0);
			assertTrue(o instanceof TableIdentifier);
			TableIdentifier t = (TableIdentifier)o;
		assertEquals("one", t.getTableName().toLowerCase());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
  }
	
  public void testDeleteTableList()
  {
		try
		{
			StatementContext context = new StatementContext(con, "delete from  where ", 12);
			BaseAnalyzer analyzer = context.getAnalyzer();
			assertTrue(analyzer instanceof DeleteAnalyzer);
			List objects = analyzer.getData();
			assertNotNull(objects);
			assertEquals(3, objects.size());
			Object o = objects.get(0);
			assertTrue(o instanceof TableIdentifier);
			TableIdentifier t = (TableIdentifier)o;
			assertEquals("one", t.getTableName().toLowerCase());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
  }

  public void testDeleteColumnList()
  {
		try
		{
			StatementContext context = new StatementContext(con, "delete from two where ", 22);
			BaseAnalyzer analyzer = context.getAnalyzer();
			assertTrue(analyzer instanceof DeleteAnalyzer);
			List objects = analyzer.getData();
			assertNotNull(objects);
			assertEquals(2, objects.size());
			Object o = objects.get(0);
			assertTrue(o instanceof ColumnIdentifier);
			ColumnIdentifier t = (ColumnIdentifier)o;
			assertEquals("id2", t.getColumnName().toLowerCase());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
  }
	
	private void prepareDatabase(WbConnection con)
		throws SQLException
	{
		Statement stmt = null;
		try
		{
			stmt = con.createStatement();
			stmt.executeUpdate("create table one (id1 integer, firstname varchar(100), lastname varchar(100))");
			stmt.executeUpdate("create table two (id2 integer, some_data varchar(100))");
			stmt.executeUpdate("create table three (id3 integer, more_data varchar(100))");
		}
		finally
		{
			SqlUtil.closeStatement(stmt);
		}
	}	

}
