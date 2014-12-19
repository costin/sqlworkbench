/*
 * TableDependencySorterTest.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2008, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db.importer;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import workbench.TestUtil;
import workbench.db.ConnectionMgr;
import workbench.db.TableIdentifier;
import workbench.db.WbConnection;

/**
 *
 * @author support@sql-workbench.net
 */
public class TableDependencySorterTest
	extends TestCase
{
	private WbConnection dbConn;
	
	public TableDependencySorterTest(String testName)
	{
		super(testName);
	}

	@Override
	protected void setUp()
		throws Exception
	{
		super.setUp();
		TestUtil util = new TestUtil("dependencyTest");
		dbConn = util.getConnection();
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

			sql = "CREATE TABLE tbl1 \n" + 
             "( \n" + 
             "   id          INTEGER NOT NULL PRIMARY KEY" + 
             ")";			
			stmt.executeUpdate(sql);
			
			sql = "CREATE TABLE tbl2 \n" + 
             "( \n" + 
             "   id          INTEGER NOT NULL PRIMARY KEY" + 
             ")";			
			stmt.executeUpdate(sql);
			
			sql = "CREATE TABLE tbl3 \n" + 
             "( \n" + 
             "   id          INTEGER NOT NULL PRIMARY KEY" + 
             ")";			
			stmt.executeUpdate(sql);
	}

	@Override
	protected void tearDown()
		throws Exception
	{
		ConnectionMgr.getInstance().disconnectAll();
		super.tearDown();
	}

	public void testPlainTables()
	{
		ArrayList<TableIdentifier> tables = new ArrayList<TableIdentifier>();
		for (int i=0; i < 3; i++)
		{
			tables.add(new TableIdentifier("tbl" + (i + 1)));
		}
		
		TableDependencySorter sorter = new TableDependencySorter(this.dbConn);
		List<TableIdentifier> result = sorter.sortForInsert(tables);
		assertEquals("Not enough entries", tables.size(), result.size());
	}

	public void testCheckDependencies()
	{
		TableIdentifier base = new TableIdentifier("base");
		TableIdentifier child1 = new TableIdentifier("child1");
		TableIdentifier child2 = new TableIdentifier("child2");
		TableIdentifier child1_detail = new TableIdentifier("child1_detail");
		TableIdentifier child1_detail2 = new TableIdentifier("child1_detail2");
		TableIdentifier child2_detail = new TableIdentifier("child2_detail");
		ArrayList<TableIdentifier> tbl = new ArrayList<TableIdentifier>();
		tbl.add(child1);
		tbl.add(base);
		tbl.add(child2);
		tbl.add(child1_detail);
		tbl.add(child2_detail);
		tbl.add(child1_detail2);
		TableDependencySorter sorter = new TableDependencySorter(this.dbConn);
		List<TableIdentifier> result = sorter.sortForDelete(tbl, false);
//		for (TableIdentifier t : result)
//		{
//			System.out.println(t.toString());
//		}
//		System.out.println("--------------------");
		assertEquals("Not enough entries", tbl.size(), result.size());
		assertEquals("Wrong first table", result.get(0), child1_detail2);
		
		// the second entry is either child1_detail or child2_detail
		TableIdentifier second = result.get(1);
		assertEquals(true, second.equals(child1_detail) || second.equals(child2_detail));

		TableIdentifier last = result.get(result.size() - 1);
		assertEquals("Wrong last table", true, last.equals(base));
		
		TableIdentifier lastButOne = result.get(result.size() - 2);
		assertEquals(true, lastButOne.equals(child1) || lastButOne.equals(child2));
		
		List<TableIdentifier> insertList = sorter.sortForInsert(tbl);
//		for (TableIdentifier t : insertList)
//		{
//			System.out.println(t.toString());
//		}
		assertEquals("Not enough entries", tbl.size(), insertList.size());
		assertEquals("Wrong first table for insert", base, insertList.get(0));
	}

	public void testCheckAddMissing()
	{
		TableIdentifier child1 = new TableIdentifier("child1");
		ArrayList<TableIdentifier> tbl = new ArrayList<TableIdentifier>();
		tbl.add(child1);
		
		TableDependencySorter sorter = new TableDependencySorter(this.dbConn);
		List<TableIdentifier> result = sorter.sortForDelete(tbl, true);
//		for (TableIdentifier t : result)
//		{
//			System.out.println(t.toString());
//		}
//		System.out.println("--------------------");
		
		// Should have added child1_detail and child1_detail2
		assertEquals("Not enough entries", 3, result.size());
		String first = result.get(0).getTableName().toLowerCase();
		String second = result.get(1).getTableName().toLowerCase();
		assertEquals("Wrong first table", true, first.equals("child1_detail") || first.equals("child1_detail2"));
		assertEquals("Wrong second table", true, second.equals("child1_detail") || second.equals("child1_detail2"));
		assertEquals("Wrong third table", "child1", result.get(2).getTableName().toLowerCase());
	}
	
}