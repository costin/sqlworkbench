/*
 * 
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 * Copyright 2002-2008, Thomas Kellerer
 * 
 * No part of this code maybe reused without the permission of the author
 * 
 * To contact the author please send an email to: support@sql-workbench.net
 * 
 */
package workbench.db.importer;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import workbench.TestUtil;
import workbench.db.ColumnIdentifier;
import workbench.db.WbConnection;

/**
 *
 * @author support@sql-workbench.net
 */
public class TextFileParserTest
	extends TestCase
{
	private TestUtil util;
	private WbConnection connection;

	public TextFileParserTest(String testName)
	{
		super(testName);
		try
		{
			util = new TestUtil(testName);
			util.prepareEnvironment();
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	protected void setUp() throws Exception
	{
		super.setUp();
		this.connection = prepareDatabase();
	}

	protected void tearDown() throws Exception
	{
		this.connection.disconnect();
		super.tearDown();
	}

	public void testSetColumns()
		throws Exception
	{
		TextFileParser parser = new TextFileParser();
		parser.setConnection(connection);
		List<ColumnIdentifier> cols = new ArrayList<ColumnIdentifier>();
		cols.add(new ColumnIdentifier("lastname"));
		cols.add(new ColumnIdentifier("firstname"));
		cols.add(new ColumnIdentifier("nr"));
		parser.setTableName("person");
		parser.setColumns(cols);

		List<ColumnIdentifier> toImport = parser.getColumnsToImport();
		assertNotNull(toImport);
		assertEquals(3, toImport.size());
		assertEquals("NR", toImport.get(2).getColumnName());
		assertEquals("FIRSTNAME", toImport.get(1).getColumnName());
		assertEquals("LASTNAME", toImport.get(0).getColumnName());

		parser = new TextFileParser();
		parser.setConnection(connection);
		cols =new ArrayList<ColumnIdentifier>();
		cols.add(new ColumnIdentifier("lastname"));
		cols.add(new ColumnIdentifier(RowDataProducer.SKIP_INDICATOR));
		cols.add(new ColumnIdentifier("firstname"));
		cols.add(new ColumnIdentifier("nr"));
		parser.setTableName("person");
		parser.setColumns(cols);

		toImport = parser.getColumnsToImport();
		assertNotNull(toImport);
		assertEquals(3, toImport.size());
		assertEquals("NR", toImport.get(2).getColumnName());
		assertEquals("FIRSTNAME", toImport.get(1).getColumnName());
		assertEquals("LASTNAME", toImport.get(0).getColumnName());
	}

	public void testRetainColumns()
		throws Exception
	{
		TextFileParser parser = new TextFileParser();
		parser.setConnection(connection);
		List<ColumnIdentifier> fileCols =new ArrayList<ColumnIdentifier>();
		fileCols.add(new ColumnIdentifier("nr"));
		fileCols.add(new ColumnIdentifier("address_id"));
		fileCols.add(new ColumnIdentifier("age"));
		fileCols.add(new ColumnIdentifier("lastname"));
		fileCols.add(new ColumnIdentifier("firstname"));
		fileCols.add(new ColumnIdentifier("nickname"));
		parser.setTableName("person");
		parser.setColumns(fileCols);

		List<ColumnIdentifier> importCols = new ArrayList<ColumnIdentifier>();
		importCols.add(new ColumnIdentifier("firstname"));
		importCols.add(new ColumnIdentifier("lastname"));
		importCols.add(new ColumnIdentifier("nr"));
		parser.retainColumns(importCols);
		
		List<ColumnIdentifier> toImport = parser.getColumnsToImport();
		assertNotNull(toImport);
		assertEquals(3, toImport.size());
		assertEquals("NR", toImport.get(0).getColumnName());
		assertEquals("LASTNAME", toImport.get(1).getColumnName());
		assertEquals("FIRSTNAME", toImport.get(2).getColumnName());
	}

	public void setColumnWidths()
		throws Exception
	{
		TextFileParser parser = new TextFileParser();
		parser.setConnection(connection);
		List<ColumnIdentifier> cols = new ArrayList<ColumnIdentifier>();
		cols.add(new ColumnIdentifier("lastname"));
		cols.add(new ColumnIdentifier("firstname"));
		cols.add(new ColumnIdentifier("nr"));
		parser.setTableName("person");
		parser.setColumns(cols);
		Map<ColumnIdentifier, Integer> widths = new HashMap<ColumnIdentifier, Integer>();
		widths.put(new ColumnIdentifier("lastname"), Integer.valueOf(15));
		widths.put(new ColumnIdentifier("nr"), Integer.valueOf(3));
		widths.put(new ColumnIdentifier("firstname"), Integer.valueOf(10));
		parser.setColumnWidths(widths);

		List<ImportFileColumn> importCols = parser.getImportColumns();
		assertEquals(3, importCols.size());
		assertEquals("NR", importCols.get(0).getColumn().getColumnName());
		assertEquals(3, importCols.get(0).getDataWidth());

		assertEquals("LASTNAME", importCols.get(1).getColumn().getColumnName());
		assertEquals(15, importCols.get(1).getDataWidth());

		assertEquals("FIRSTNAME", importCols.get(2).getColumn().getColumnName());
		assertEquals(10, importCols.get(2).getDataWidth());
	}
	
	private WbConnection prepareDatabase()
		throws SQLException, ClassNotFoundException
	{
		util.emptyBaseDirectory();
		WbConnection wb = util.getConnection();

		Statement stmt = wb.createStatement();
		stmt.executeUpdate("CREATE TABLE person (nr integer, firstname varchar(100), lastname varchar(100))");
		wb.commit();
		stmt.close();

		return wb;
	}
}