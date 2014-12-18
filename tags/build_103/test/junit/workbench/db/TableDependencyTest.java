/*
 * TableDependencyTest.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2009, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db;

import java.sql.Statement;
import java.util.List;
import junit.framework.TestCase;
import workbench.TestUtil;

/**
 *
 * @author support@sql-workbench.net
 */
public class TableDependencyTest
	extends TestCase
{
	public TableDependencyTest(String testName)
	{
		super(testName);
	}

	protected WbConnection createRegularDB()
		throws Exception
	{
		super.setUp();
		TestUtil util = new TestUtil("dependencyTest");
		WbConnection dbConn = util.getConnection();
		Statement stmt = dbConn.createStatement();
		String baseSql = "CREATE TABLE base  \n" + 
					 "( \n" + 
					 "   id1  INTEGER NOT NULL, \n" + 
					 "   id2  INTEGER NOT NULL, \n" + 
					 "   primary key (id1, id2) \n" + 
					 ")";			
		stmt.execute(baseSql);

		String child1Sql = "CREATE TABLE child1 \n" + 
					 "( \n" + 
					 "   id          INTEGER NOT NULL PRIMARY KEY, \n" + 
					 "   base_id1    INTEGER NOT NULL, \n" + 
					 "   base_id2    INTEGER NOT NULL, \n" + 
					 "   FOREIGN KEY (base_id1, base_id2) REFERENCES base (id1,id2) \n" + 
					 ")";			
		stmt.executeUpdate(child1Sql);

		String child2Sql = "CREATE TABLE child2 \n" + 
					 "( \n" + 
					 "   id          INTEGER NOT NULL PRIMARY KEY, \n" + 
					 "   base_id1    INTEGER NOT NULL, \n" + 
					 "   base_id2    INTEGER NOT NULL, \n" + 
					 "   FOREIGN KEY (base_id1, base_id2) REFERENCES base (id1,id2) \n" + 
					 ")";			
		stmt.executeUpdate(child2Sql);

		String child3Sql = "CREATE TABLE child2_detail \n" + 
					 "( \n" + 
					 "   id          INTEGER NOT NULL PRIMARY KEY, \n" + 
					 "   child_id    INTEGER NOT NULL, \n" + 
					 "   FOREIGN KEY (child_id) REFERENCES child2 (id) \n" + 
					 ")";			
		stmt.executeUpdate(child3Sql);

		String sql = "CREATE TABLE child1_detail \n" + 
					 "( \n" + 
					 "   id          INTEGER NOT NULL PRIMARY KEY, \n" + 
					 "   child1_id    INTEGER NOT NULL, \n" + 
					 "   FOREIGN KEY (child1_id) REFERENCES child1 (id) \n" + 
					 ")";			
		stmt.executeUpdate(sql);

		sql = "CREATE TABLE child1_detail2 \n" + 
					 "( \n" + 
					 "   id          INTEGER NOT NULL PRIMARY KEY, \n" + 
					 "   detail_id    INTEGER NOT NULL, \n" + 
					 "   FOREIGN KEY (detail_id) REFERENCES child1_detail (id) \n" + 
					 ")";			
		stmt.executeUpdate(sql);
		return dbConn;
	}

	@Override
	protected void tearDown()
		throws Exception
	{
		ConnectionMgr.getInstance().disconnectAll();
		super.tearDown();
	}

	public void testDependency()
		throws Exception
	{
		try
		{
			TableIdentifier base = new TableIdentifier("BASE");
			TableDependency dep = new TableDependency(createRegularDB(), base);
			dep.readTreeForChildren();
			DependencyNode root = dep.getRootNode();
			assertNotNull("No root returned", root);
			List<DependencyNode> leafs = dep.getLeafs();
			for (DependencyNode node : leafs)
			{
				int level = node.getLevel();
				String tbl = node.getTable().getTableName();
				if (tbl.equalsIgnoreCase("base"))
				{
					assertEquals("Wrong level for base table", 0, level);
				}
				if (tbl.equalsIgnoreCase("child1") || tbl.equalsIgnoreCase("child2"))
				{
					assertEquals("Wrong level for childX tables", 1, level);
				}
				if (tbl.equalsIgnoreCase("child1_detail") || tbl.equalsIgnoreCase("child2_detail"))
				{
					assertEquals("Wrong level for detail tables", 2, level);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
		finally
		{
			ConnectionMgr.getInstance().disconnectAll();
		}
	}
	
	protected WbConnection createTwoLevelCycleDB()
		throws Exception
	{
		super.setUp();
		TestUtil util = new TestUtil("dependencyCycleTest");
		WbConnection dbConn = util.getConnection();
		Statement stmt = dbConn.createStatement();

		String sql = null;
		
		sql = "CREATE TABLE base \n" +
					 "( \n" + 
					 "   id          INTEGER NOT NULL PRIMARY KEY " +
					 ")";			
		stmt.executeUpdate(sql);
		
		sql = "CREATE TABLE tbl1 \n" + 
					 "( \n" + 
					 "   id          INTEGER NOT NULL PRIMARY KEY, " +
					 "   base_id     INTEGER, " +
					 "   tbl2_id     integer," +
					 "   foreign key (base_id) references base (id) " +
					 ")";			
		stmt.executeUpdate(sql);

		sql = "CREATE TABLE tbl2 \n" + 
					 "( \n" + 
					 "   id          INTEGER NOT NULL PRIMARY KEY," +
					 "   tbl1_id     integer " +
					 ")";			
		stmt.executeUpdate(sql);

		sql = "alter table tbl1 add foreign key (tbl2_id) references tbl2 (id)";
		stmt.executeUpdate(sql);
		
		sql = "alter table tbl2 add foreign key (tbl1_id) references tbl1 (id)";
		stmt.executeUpdate(sql);
		
		return dbConn;
	}

	public void testCycle()
	{
		try
		{
			WbConnection con = createTwoLevelCycleDB();
			TableIdentifier tbl = new TableIdentifier("base");
			TableDependency dep = new TableDependency(con, tbl);
			dep.readTreeForChildren();
			assertEquals(false, dep.wasAborted());
			assertNotNull("No root returned", dep.getRootNode());
//			dep.getRootNode().printAll();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
		finally
		{
			ConnectionMgr.getInstance().disconnectAll();
		}
	}
	
	protected WbConnection createSingleLevelCycleDB()
		throws Exception
	{
		super.setUp();
		TestUtil util = new TestUtil("dependencyCycleTest");
		WbConnection dbConn = util.getConnection();
		Statement stmt = dbConn.createStatement();

		String sql = null;
		
		sql = "CREATE TABLE tbl1 \n" + 
					 "( \n" + 
					 "   id          INTEGER NOT NULL PRIMARY KEY, " +
					 "   tbl2_id     integer" +
					 ")";			
		stmt.executeUpdate(sql);

		sql = "CREATE TABLE tbl2 \n" + 
					 "( \n" + 
					 "   id          INTEGER NOT NULL PRIMARY KEY," +
					 "   tbl1_id     integer " +
					 ")";			
		stmt.executeUpdate(sql);

		sql = "alter table tbl1 add foreign key (tbl2_id) references tbl2 (id)";
		stmt.executeUpdate(sql);
		
		sql = "alter table tbl2 add foreign key (tbl1_id) references tbl1 (id)";
		stmt.executeUpdate(sql);
		
		return dbConn;
	}
	
	public void testDirectCycle()
	{
		try
		{
			WbConnection con = createSingleLevelCycleDB();
			TableIdentifier tbl = new TableIdentifier("tbl1");
			TableDependency dep = new TableDependency(con, tbl);
			dep.readTreeForChildren();
			assertEquals(false, dep.wasAborted());
			assertNotNull("No root returned", dep.getRootNode());
			dep.getRootNode().printAll();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
		finally
		{
			ConnectionMgr.getInstance().disconnectAll();
		}
	}	
}