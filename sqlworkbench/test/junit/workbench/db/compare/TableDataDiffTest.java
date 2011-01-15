/*
 * TableDataDiffTest.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2011, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db.compare;

import java.io.StringWriter;
import java.sql.Statement;
import org.junit.Test;
import workbench.TestUtil;
import workbench.WbTestCase;
import workbench.db.TableIdentifier;
import workbench.db.WbConnection;
import workbench.util.SqlUtil;
import static org.junit.Assert.*;

/**
 *
 * @author Thomas Kellerer
 */
public class TableDataDiffTest
	extends WbTestCase
{

	private WbConnection source;
	private WbConnection target;
	private TestUtil util;
	
	public TableDataDiffTest()
	{
		super("TableDataDiffTest");
		util = getTestUtil();
	}

	protected void createDefaultDb()
		throws Exception
	{
		Statement sourceStmt = null;
		Statement targetStmt = null;
		try
		{
			source = util.getConnection("data_diff_reference");
			target = util.getConnection("data_diff_target");
			sourceStmt= source.getSqlConnection().createStatement();
			String create = "CREATE table person (" +
				"id1 integer not null, " +
				"id2 integer not null, " +
				"firstname varchar(20), " +
				"lastname varchar(20), primary key(id1, id2))";
			sourceStmt.executeUpdate(create);

			targetStmt = target.getSqlConnection().createStatement();
			targetStmt.executeUpdate(create.replace(" person ", " person_t "));

			int rowCount = 187;
			
			for (int i=0; i < rowCount; i++)
			{
				sourceStmt.executeUpdate("INSERT INTO person (id1, id2, firstname, lastname) " +
					"VALUES (" + i + ", " + (i + 1) + ", 'first" + i + "', 'last" + i + "')");
			}

			for (int i=0; i < rowCount; i++)
			{
				targetStmt.executeUpdate("INSERT INTO person_t (id1, id2, firstname, lastname) " +
					"VALUES (" + i + ", " + (i + 1) + ", 'first" + i + "', 'last" + i + "')");
			}

			targetStmt.executeUpdate("update person_t set firstname = 'Wrong' where id1 = 4");
			targetStmt.executeUpdate("update person_t set lastname = 'Wrong' where id1 = 8");
			targetStmt.executeUpdate("update person_t set lastname = 'Wrong' where id1 = 21");
			targetStmt.executeUpdate("update person_t set lastname = 'Wrong' where id1 = 69");
			targetStmt.executeUpdate("update person_t set firstname = 'Wrong', lastname = 'Also Wrong' where id1 = 83");
			targetStmt.executeUpdate("delete from person_t where id1 = 6");
			targetStmt.executeUpdate("delete from person_t where id1 = 12");
			targetStmt.executeUpdate("delete from person_t where id1 = 17");
			targetStmt.executeUpdate("delete from person_t where id1 = 53");
			targetStmt.executeUpdate("delete from person_t where id1 = 67");
			
			source.commit();
			target.commit();
		}
		finally
		{
			SqlUtil.closeStatement(sourceStmt);
			SqlUtil.closeStatement(targetStmt);
		}
	}

	/**
	 * Test of doSync method, of class TableDataDiff.
	 */
	@Test
	public void testDoSync()
		throws Exception
	{
		createDefaultDb();
		TableDataDiff diff = new TableDataDiff(source, target);
		StringWriter updates = new StringWriter(2500);
		StringWriter inserts = new StringWriter(2500);
		diff.setOutputWriters(updates, inserts, "\n", "UTF-8");
		diff.setTableName(new TableIdentifier("person"), new TableIdentifier("person_t"));
		diff.doSync();
//		System.out.println("----- sync script start \n" + output.toString() + "----- sync script end");
		TestUtil.executeScript(target, inserts.toString());
		TestUtil.executeScript(target, updates.toString());
		target.commit();
		updates = new StringWriter(2500);
		inserts = new StringWriter(2500);
		diff.setOutputWriters(updates, inserts, "\n", "UTF-8");
		diff.doSync();
//		System.out.println("----- sync script start \n" + output.toString() + "----- sync script end");
		assertTrue(updates.toString().length() == 0);
	}


	protected void createCaseDifferentDb()
		throws Exception
	{
		Statement sourceStmt = null;
		Statement targetStmt = null;
		try
		{
			source = util.getConnection("data_diff_reference");
			target = util.getConnection("data_diff_target");
			sourceStmt= source.getSqlConnection().createStatement();
			String create = "CREATE table person (" +
				"id1 integer not null, " +
				"id2 integer not null, " +
				"firstname varchar(20), " +
				"lastname varchar(20), primary key(id1, id2))";
			sourceStmt.executeUpdate(create);

			targetStmt = target.getSqlConnection().createStatement();
			create = "CREATE table person (" +
				"id1 integer not null, " +
				"id2 integer not null, " +
				"\"firstName\" varchar(20), " +
				"lastname varchar(20), primary key(id1, id2))";
			targetStmt.executeUpdate(create);

			int rowCount = 187;

			for (int i=0; i < rowCount; i++)
			{
				sourceStmt.executeUpdate("INSERT INTO person (id1, id2, firstname, lastname) " +
					"VALUES (" + i + ", " + (i + 1) + ", 'first" + i + "', 'last" + i + "')");
			}

			for (int i=0; i < rowCount; i++)
			{
				targetStmt.executeUpdate("INSERT INTO person (id1, id2, \"firstName\", lastname) " +
					"VALUES (" + i + ", " + (i + 1) + ", 'first" + i + "', 'last" + i + "')");
			}

			targetStmt.executeUpdate("update person set \"firstName\" = 'Wrong' where id1 = 4");
			targetStmt.executeUpdate("update person set lastname = 'Wrong' where id1 = 8");
			targetStmt.executeUpdate("update person set lastname = 'Wrong' where id1 = 21");
			targetStmt.executeUpdate("update person set lastname = 'Wrong' where id1 = 69");
			targetStmt.executeUpdate("update person set \"firstName\" = 'Wrong', lastname = 'Also Wrong' where id1 = 83");
			targetStmt.executeUpdate("delete from person where id1 = 6");
			targetStmt.executeUpdate("delete from person where id1 = 12");
			targetStmt.executeUpdate("delete from person where id1 = 17");
			targetStmt.executeUpdate("delete from person where id1 = 53");
			targetStmt.executeUpdate("delete from person where id1 = 67");

			source.commit();
			target.commit();
		}
		finally
		{
			SqlUtil.closeStatement(sourceStmt);
			SqlUtil.closeStatement(targetStmt);
		}
	}

	@Test
	public void testDifferentCase()
		throws Exception
	{
		createCaseDifferentDb();
		TableDataDiff diff = new TableDataDiff(source, target);
		StringWriter updates = new StringWriter(2500);
		StringWriter inserts = new StringWriter(2500);
		diff.setOutputWriters(updates, inserts, "\n", "UTF-8");
		diff.setTableName(new TableIdentifier("person"), new TableIdentifier("person"));
		diff.doSync();
		String output = updates.toString() + "\n" + inserts.toString();
//		System.out.println("----- sync script start \n" + output.toString() + "----- sync script end");
	}
}
