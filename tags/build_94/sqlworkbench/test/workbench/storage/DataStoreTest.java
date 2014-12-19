/*
 * DataStoreTest.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2007, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.storage;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import junit.framework.*;
import java.sql.Types;
import java.util.List;
import workbench.TestUtil;
import workbench.db.ColumnIdentifier;
import workbench.db.ConnectionMgr;
import workbench.db.TableIdentifier;
import workbench.db.WbConnection;
import workbench.resource.Settings;
import workbench.storage.filter.AndExpression;
import workbench.storage.filter.ComplexExpression;
import workbench.storage.filter.LessThanComparator;
import workbench.storage.filter.NumberEqualsComparator;
import workbench.storage.filter.OrExpression;
import workbench.storage.filter.StartsWithComparator;
import workbench.storage.filter.StringEqualsComparator;
import workbench.util.ExceptionUtil;
import workbench.util.SqlUtil;

/**
 *
 * @author support@sql-workbench.net
 */
public class DataStoreTest 
	extends TestCase
{
	private final int rowcount = 10;
	private TestUtil util;
	
	public DataStoreTest(String testName)
		throws Exception
	{
		super(testName);
		this.util = new TestUtil(testName);
		util.prepareEnvironment();
	}

	private WbConnection preparePkTestDb()
		throws Exception
	{
		util.emptyBaseDirectory();
		WbConnection wb = util.getConnection("pkTestDb");
		Connection con = wb.getSqlConnection();
		Statement stmt = con.createStatement();
		try { stmt.executeUpdate("DROP TABLE junit_test"); } catch (Throwable th) {}
		stmt.executeUpdate("CREATE TABLE junit_test (key integer primary key, firstname varchar(100), lastname varchar(100))");
		stmt.close();
		PreparedStatement pstmt = con.prepareStatement("insert into junit_test (key, firstname, lastname) values (?,?,?)");
		for (int i=0; i < rowcount; i ++)
		{
			pstmt.setInt(1, i);
			pstmt.setString(2, "FirstName" + i);
			pstmt.setString(3, "LastName" + i);
			pstmt.executeUpdate();
		}
		con.commit();
		return wb;
	}

	public void testMissingPkColumns()
	{
		try
		{
			util.emptyBaseDirectory();
			WbConnection con = util.getConnection("pkTestDb");
			Statement stmt = con.createStatement();
			stmt.executeUpdate("CREATE TABLE junit_test (id1 integer, id2 integer, id3 integer, some_data varchar(100), primary key (id1, id2, id3))");
			stmt.executeUpdate("insert into junit_test (id1,id2,id3, some_data) values (1,2,3,'bla')");

			String sql = "select id1, id2, some_data from JUnit_Test";
			
			ResultSet rs = stmt.executeQuery(sql);
			DataStore ds = new DataStore(rs, con);
			rs.close();
			stmt.close();
			ds.setGeneratingSql(sql);
			ds.checkUpdateTable(con);
			assertEquals("Missing PK columns not detected", false, ds.pkColumnsComplete());
			List<ColumnIdentifier> cols = ds.getMissingPkColumns();
			assertEquals("Not all missing columns detected", 1, cols.size());
			String col1 = cols.get(0).getColumnName();
			assertEquals("Wrong column detected", true, col1.equals("ID3"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	
	public void testQuotedKeyColumns()
	{
		
		try
		{
			WbConnection con = preparePkTestDb();
			
			String[] cols = new String[] {"\"KEY\"","LASTNAME", "FIRSTNAME", "LASTNAME" };
			int[] types = new int[] {java.sql.Types.VARCHAR, java.sql.Types.VARCHAR,  java.sql.Types.VARCHAR, java.sql.Types.VARCHAR};
			
			int[] sizes = new int[] { 20,20,20,20 };
			ResultInfo info = new ResultInfo(cols, types, sizes);
			DataStore ds = new DataStore(info);
			ds.setGeneratingSql("SELECT \"KEY\", LASTNAME, FIRSTNAME FROM JUNIT_TEST");
			TableIdentifier tbl = new TableIdentifier("JUNIT_TEST");
			ds.setUpdateTableToBeUsed(tbl);
			ds.checkUpdateTable(con);
			
			ResultInfo newInfo = ds.getResultInfo();
			assertEquals(newInfo.getColumnCount(), 4);
			assertEquals(newInfo.isPkColumn(0), true);
			assertEquals(ds.pkColumnsComplete(), true);
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
	
	
	public void testCasePreserving()
	{
		WbConnection con = null;
		Statement stmt = null;
		try
		{
			con = prepareRetrieveDatabase();

			stmt = con.createStatement();
			stmt.executeUpdate("delete from junit_test");
			con.commit();
			
			String sql = "select key, lastname, firstname from JUnit_Test";
			ResultSet rs = stmt.executeQuery(sql);
			DataStore ds = new DataStore(rs, con);
			ds.setGeneratingSql(sql);

			int row = ds.addRow();
			ds.setValue(row, 0, new Integer(1));
			ds.setValue(row, 1, "Dent");
			ds.setValue(row, 2, "Arthur");
			
			ds.checkUpdateTable(con);
			
			assertEquals("Not all PK columns detected", ds.pkColumnsComplete(), true);
			
			// Set to case preserving
			Settings.getInstance().setGeneratedSqlTableCase("original");
			List<DmlStatement> l = ds.getUpdateStatements(con);
			assertEquals("Wrong number of update statements", 1, l.size());
			
			DmlStatement dml = (DmlStatement)l.get(0);
			SqlLiteralFormatter f = new SqlLiteralFormatter(con);
			String insert = dml.getExecutableStatement(f, false);
			String verb = SqlUtil.getSqlVerb(insert);
			assertEquals("Wrong statement generated", "INSERT", verb.toUpperCase());
			String table = SqlUtil.getInsertTable(insert);
			assertEquals("JUnit_Test", table);
			
			// Set to upper case
			Settings.getInstance().setGeneratedSqlTableCase("upper");
			l = ds.getUpdateStatements(con);
			assertEquals("Wrong number of update statements", 1, l.size());
			
			dml = (DmlStatement)l.get(0);
			insert = dml.getExecutableStatement(f, false);
			table = SqlUtil.getInsertTable(insert);
			assertEquals("JUNIT_TEST", table);

			// Test lower case
			Settings.getInstance().setGeneratedSqlTableCase("lower");
			l = ds.getUpdateStatements(con);
			assertEquals("Wrong number of update statements", 1, l.size());
			
			dml = (DmlStatement)l.get(0);
			insert = dml.getExecutableStatement(f, false);
			table = SqlUtil.getInsertTable(insert);
			assertEquals("junit_test", table);
			
			ds.resetStatus();
			ds.setValue(0, 1, "Dent2");

			// Test uppercase 
			Settings.getInstance().setGeneratedSqlTableCase("upper");
			l = ds.getUpdateStatements(con);
			assertEquals("Wrong number of update statements", 1, l.size());
			
			dml = (DmlStatement)l.get(0);
			String update = dml.getExecutableStatement(f, false);
			verb = SqlUtil.getSqlVerb(update);
			table = SqlUtil.getUpdateTable(update);
			assertEquals("UPDATE", verb);
			assertEquals("JUNIT_TEST", table);
			
			// Test lower case
			Settings.getInstance().setGeneratedSqlTableCase("lower");
			l = ds.getUpdateStatements(con);
			assertEquals("Wrong number of update statements", 1, l.size());
			
			dml = (DmlStatement)l.get(0);
			update = dml.getExecutableStatement(f, false);
			table = SqlUtil.getUpdateTable(update);
			assertEquals("junit_test", table);

			// Test lower case
			Settings.getInstance().setGeneratedSqlTableCase("original");
			l = ds.getUpdateStatements(con);
			assertEquals("Wrong number of update statements", 1, l.size());
			
			dml = (DmlStatement)l.get(0);
			update = dml.getExecutableStatement(f, false);
			table = SqlUtil.getUpdateTable(update);
			assertEquals("JUnit_Test", table);
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail(ExceptionUtil.getDisplay(e));
		}
		finally
		{
			ConnectionMgr.getInstance().disconnectAll();
		}
	}
	
	public void testPkDetection()
	{
		try
		{
			WbConnection con = preparePkTestDb();
			Statement stmt = con.createStatement();
			final String sql = "select key, firstname, lastname from junit_test";
			ResultSet rs = stmt.executeQuery(sql);
			DataStore ds = new DataStore(rs, con);
			rs.close();
			ds.setGeneratingSql(sql);
			
			assertEquals("Non-existing primary key found", false, ds.hasPkColumns());
			ds.checkUpdateTable();
			assertEquals("Primary key not found", true, ds.hasPkColumns());
			assertEquals("Not all PK columns detected", ds.pkColumnsComplete(), true);
			
			stmt.executeUpdate("DROP TABLE junit_test");
			stmt.executeUpdate("CREATE TABLE junit_test (key integer, firstname varchar(100), lastname varchar(100))");
			stmt.executeUpdate("insert into junit_test (key, firstname, lastname) values (42, 'Zaphod', 'Beeblebrox')");
			stmt.executeUpdate("insert into junit_test (key, firstname, lastname) values (1, 'Mary', 'Moviestar')");
			con.commit();
			
			rs = stmt.executeQuery(sql);
			ds = new DataStore(rs, con);
			ds.setGeneratingSql(sql);
			rs.close();
			
			ds.updatePkInformation();
			assertEquals("Non-existing primary key found", false, ds.hasPkColumns());
			
			ResultInfo info = ds.getResultInfo();
			info.setIsPkColumn(0, true);
			assertEquals("Primary key not recognized", true, ds.hasPkColumns());
			
			ds.setValue(0, 1, "Arthur");
			ds.setValue(0, 2, "Dent");
			int rows = ds.updateDb(con, null);
			assertEquals("Incorrect number of rows updated", 1, rows);
			
			rs = stmt.executeQuery("select firstname, lastname from junit_test where key = 42");
			boolean hasRows = rs.next();
			assertEquals("No rows fetched", true, hasRows);
			
			String fname = rs.getString(1);
			String lname = rs.getString(2);
			assertEquals("Firstname incorrectly updated", "Arthur", fname);
			assertEquals("Lastname incorrectly updated", "Dent", lname);
			rs.close();
			
			rs = stmt.executeQuery("select firstname, lastname from junit_test where key = 1");
			hasRows = rs.next();
			assertEquals("No rows fetched", true, hasRows);
			
			fname = rs.getString(1);
			lname = rs.getString(2);
			assertEquals("Incorrect firstname affected", "Mary", fname);
			assertEquals("Incorrect lastname affected", "Moviestar", lname);
			rs.close();
			
			String mapping = "junit_test=key";
			File f = new File(util.getBaseDir(), "pk_mapping.properties");
			FileWriter w = new FileWriter(f);
			w.write(mapping);
			w.close();
			PkMapping.getInstance().loadMapping(f.getAbsolutePath());

			rs = stmt.executeQuery(sql);
			ds = new DataStore(rs, con);
			ds.setGeneratingSql(sql);
			rs.close();
			
			ds.updatePkInformation();
			assertEquals("Primary key from mapping not found", true, ds.hasPkColumns());
			
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
	

	private WbConnection prepareRetrieveDatabase()
		throws Exception
	{
		util.emptyBaseDirectory();
		WbConnection wb = util.getConnection();
		Connection con = wb.getSqlConnection();
		Statement stmt = con.createStatement();
		try { stmt.executeUpdate("DROP TABLE junit_test"); } catch (Throwable th) {}
		stmt.executeUpdate("CREATE TABLE junit_test (key integer primary key, firstname varchar(100), lastname varchar(100))");
		stmt.close();
		PreparedStatement pstmt = con.prepareStatement("insert into junit_test (key, firstname, lastname) values (?,?,?)");
		for (int i=0; i < rowcount; i ++)
		{
			pstmt.setInt(1, i);
			pstmt.setString(2, "FirstName" + i);
			pstmt.setString(3, "LastName" + i);
			pstmt.executeUpdate();
		}
		con.commit();
		return wb;
	}

	public void testUpdate()
	{
		WbConnection con = null;
		Statement stmt = null;
		try
		{
			con = prepareRetrieveDatabase();
			
			stmt = con.createStatement();
			String sql = "select key, lastname, firstname from junit_test";
			ResultSet rs = stmt.executeQuery(sql);
			DataStore ds = new DataStore(rs, con);
			SqlUtil.closeResult(rs);
			
			List tbl = SqlUtil.getTables(sql);
			assertEquals("Wrong number of tables retrieved from SQL", 1, tbl.size());
			
			String table = (String)tbl.get(0);
			assertEquals("Wrong update table returned", "junit_test", table);
			
			TableIdentifier id = new TableIdentifier(table);
			ds.setUpdateTable(id);
			assertEquals(rowcount, ds.getRowCount());
			
			ds.setValue(0, 1, "Dent");
			ds.setValue(0, 2, "Arthur");
			ds.updateDb(con, null);
			
			rs = stmt.executeQuery("select lastname, firstname from junit_test where key = 0");
			boolean hasNext = rs.next();
			assertEquals("Updated row not found", true, hasNext);
			String lastname = rs.getString(1);
			String firstname = rs.getString(2);
			assertEquals("Firstname not updated", "Arthur", firstname);
			assertEquals("Lastname not updated", "Dent", lastname);
			SqlUtil.closeResult(rs);
			
			rs = stmt.executeQuery("select lastname, firstname from junit_test where key = 1");
			hasNext = rs.next();
			assertEquals("Updated row not found", true, hasNext);
			lastname = rs.getString(1);
			firstname = rs.getString(2);
			assertEquals("Firstname updated", "FirstName1", firstname);
			assertEquals("Lastname updated", "LastName1", lastname);
			SqlUtil.closeResult(rs);
			
			int row = ds.addRow();
			ds.setValue(row, 0, new Integer(42));
			ds.setValue(row, 1, "Beeblebrox");
			assertEquals("Row not inserted", rowcount + 1, ds.getRowCount());
			ds.setValue(row, 2, "Zaphod");
			ds.updateDb(con, null);
			
			rs = stmt.executeQuery("select lastname, firstname from junit_test where key = 42");
			hasNext = rs.next();
			assertEquals("Updated row not found", true, hasNext);
			lastname = rs.getString(1);
			firstname = rs.getString(2);
			assertEquals("Firstname not updated", "Zaphod", firstname);
			assertEquals("Lastname not updated", "Beeblebrox", lastname);
			SqlUtil.closeAll(rs, stmt);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail(ExceptionUtil.getDisplay(e));
		}
		finally
		{
			ConnectionMgr.getInstance().disconnectAll();
		}
	}
	
	public void testFilter()
	{
		WbConnection con = null;
		Statement stmt = null;
		try
		{
			con = prepareRetrieveDatabase();
			
			stmt = con.createStatement();
			stmt.executeUpdate("insert into junit_test (key, firstname, lastname) values (42, 'Zaphod', 'Beeblebrox')");
			con.commit();
			
			String sql = "select key, lastname, firstname from junit_test";
			ResultSet rs = stmt.executeQuery(sql);
			DataStore ds = new DataStore(rs, con);
			
			ComplexExpression expr = new AndExpression();
			expr.addColumnExpression("FIRSTNAME", new StringEqualsComparator(), "Zaphod");
			expr.addColumnExpression("LASTNAME", new StartsWithComparator(), "Bee");
			
			ds.applyFilter(expr);
			assertEquals("AND Filter not correct", 1, ds.getRowCount());
			
			expr = new AndExpression();
			expr.addColumnExpression("KEY", new NumberEqualsComparator(), new Integer(100));
			ds.applyFilter(expr);
			assertEquals("Number Filter not correct", 0, ds.getRowCount());
			
			expr = new OrExpression();
			expr.addColumnExpression("KEY", new LessThanComparator(), new Integer(1));
			expr.addColumnExpression("FIRSTNAME", new StringEqualsComparator(), "Zaphod");
			
			ds.applyFilter(expr);
			assertEquals("OR Filter not correct", 2, ds.getRowCount());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail(ExceptionUtil.getDisplay(e));
		}
		finally
		{
			ConnectionMgr.getInstance().disconnectAll();
		}
		
	}

	public void testUpdateCaseSensitive()
	{
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			util.emptyBaseDirectory();
			WbConnection con = util.getConnection();
			stmt = con.createStatement();
			stmt.executeUpdate("create table \"case_test\" (nr integer primary key, text_data varchar(100))");
			String sql = "select * from \"case_test\"";
			rs = stmt.executeQuery(sql);
			DataStore ds = new DataStore(rs, con, true);;
			rs.close();
			
			ds.checkUpdateTable(sql, con);
			int row = ds.addRow();
			ds.setValue(row, 0, new Integer(42));
			ds.setValue(row, 1, "TestData");
			ds.updateDb(con, null);
			
			rs = stmt.executeQuery(sql);
			if (rs.next())
			{
				int nr = rs.getInt(1);
				assertEquals(42, nr);
				
				String s = rs.getString(2);
				assertEquals("TestData", s);
			}
			else
			{
				fail("New rows not updated");
			}
			rs.close();
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
	
	public void testList()
	{
		try
		{
			String[] cols = new String[] { "CHAR", "INT", "DOUBLE"};
			int[] types = new int[] { Types.VARCHAR, Types.INTEGER, Types.DOUBLE };
			DataStore ds = new DataStore(cols, types);
			ds.addRow();
			assertEquals(1, ds.getRowCount());
			String s = "Testvalue";
			Integer i = new Integer(42);
			Double d = new Double(4.2);
			ds.setValue(0, 0, s);
			ds.setValue(0, 1, i);
			ds.setValue(0, 2, d);
			assertEquals(s, ds.getValue(0, 0));
			assertEquals(i, ds.getValue(0, 1));
			assertEquals(i.intValue(), ds.getValueAsInt(0, 1, -1));
			assertEquals(d, ds.getValue(0, 2));
			ds.resetStatus();
			
			String newValue = "Newvalue";
			ds.setValue(0, 0, newValue);
			assertEquals(newValue, ds.getValue(0, 0));
			assertEquals(s, ds.getOriginalValue(0, 0));
			assertEquals(true, ds.isModified());
			ds.resetStatus();
			assertEquals(false, ds.isModified());
			assertEquals(newValue, ds.getOriginalValue(0, 0));
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
		finally
		{
			ConnectionMgr.getInstance().disconnectAll();
		}
	}
	
}