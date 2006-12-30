/*
 * WbImportTest.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2007, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.sql.wbcommands;

import java.io.BufferedWriter;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import junit.framework.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import workbench.TestUtil;
import workbench.db.ConnectionMgr;
import workbench.db.WbConnection;
import workbench.db.exporter.RowDataConverter;
import workbench.sql.StatementRunnerResult;
import workbench.util.EncodingUtil;
import workbench.util.SqlUtil;
import workbench.util.ZipOutputFactory;

/**
 *
 * @author support@sql-workbench.net
 */
public class WbImportTest 
	extends TestCase
{
	private TestUtil util;
	private String basedir;
	private WbImport importCmd = new WbImport();
	private WbConnection connection;
	
	public WbImportTest(String testName)
	{
		super(testName);
		try
		{
			util = new TestUtil(testName);
			util.prepareEnvironment();
			this.basedir = util.getBaseDir();
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
		this.importCmd.setConnection(this.connection);
	}
	
	protected void tearDown() throws Exception
	{
		this.connection.disconnect();
		super.tearDown();
	}
	
	public void testPartialColumnXmlImport()
		throws Exception
	{
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n" + 
             "<wb-export> \n" + 
             "  <meta-data> \n" + 
             " \n" + 
             "    <generating-sql> \n" + 
             "    <![CDATA[ \n" + 
             "    select id, lastname, firstname from person \n" + 
             "    ]]> \n" + 
             "    </generating-sql> \n" + 
             " \n" + 
             "    <created>2006-07-29 23:31:40.366 CEST</created> \n" + 
             "    <jdbc-driver>HSQL Database Engine Driver</jdbc-driver> \n" + 
             "    <jdbc-driver-version>1.8.0</jdbc-driver-version> \n" + 
             "    <connection>User=SA, URL=jdbc:hsqldb:d:/daten/db/hsql18/test</connection> \n" + 
             "    <database-product-name>HSQL Database Engine</database-product-name> \n" + 
             "    <database-product-version>1.8.0</database-product-version> \n" + 
             "    <wb-tag-format>short</wb-tag-format> \n" + 
             "  </meta-data> \n" + 
             " \n" + 
             "  <table-def> \n" + 
             "    <!-- The following information was retrieved from the JDBC driver's ResultSetMetaData --> \n" + 
             "    <!-- column-name is retrieved from ResultSetMetaData.getColumnName() --> \n" + 
             "    <!-- java-class is retrieved from ResultSetMetaData.getColumnClassName() --> \n" + 
             "    <!-- java-sql-type-name is the constant's name from java.sql.Types --> \n" + 
             "    <!-- java-sql-type is the constant's numeric value from java.sql.Types as returned from ResultSetMetaData.getColumnType() --> \n" + 
             "    <!-- dbms-data-type is retrieved from ResultSetMetaData.getColumnTypeName() --> \n" + 
             " \n" + 
             "    <!-- For date and timestamp types, the internal long value obtained from java.util.Date.getTime() \n" + 
             "         is written as an attribute to the <column-data> tag. That value can be used \n" + 
             "         to create a java.util.Date() object directly, without the need to parse the actual tag content. \n" + 
             "         If Java is not used to parse this file, the date/time format used to write the data \n" + 
             "         is provided in the <data-format> tag of the column definition \n" + 
             "    --> \n" + 
             " \n" + 
             "    <table-name>junit_test</table-name> \n" + 
             "    <column-count>3</column-count> \n" + 
             " \n" + 
             "    <column-def index=\"0\"> \n" + 
             "      <column-name>NR</column-name> \n" + 
             "      <java-class>java.lang.Integer</java-class> \n" + 
             "      <java-sql-type-name>INTEGER</java-sql-type-name> \n" + 
             "      <java-sql-type>4</java-sql-type> \n" + 
             "      <dbms-data-type>INTEGER</dbms-data-type> \n" + 
             "    </column-def> \n" + 
             "    <column-def index=\"1\"> \n" + 
             "      <column-name>LASTNAME</column-name> \n" + 
             "      <java-class>java.lang.String</java-class> \n" + 
             "      <java-sql-type-name>VARCHAR</java-sql-type-name> \n" + 
             "      <java-sql-type>12</java-sql-type> \n" + 
             "      <dbms-data-type>VARCHAR(100)</dbms-data-type> \n" + 
             "    </column-def> \n" + 
             "    <column-def index=\"2\"> \n" + 
             "      <column-name>FIRSTNAME</column-name> \n" + 
             "      <java-class>java.lang.String</java-class> \n" + 
             "      <java-sql-type-name>VARCHAR</java-sql-type-name> \n" + 
             "      <java-sql-type>12</java-sql-type> \n" + 
             "      <dbms-data-type>VARCHAR(100)</dbms-data-type> \n" + 
             "    </column-def> \n" + 
             "  </table-def> \n" + 
             " \n" + 
             "<data> \n" + 
             "<rd><cd>1</cd><cd>Dent</cd><cd>Arthur</cd></rd> \n" + 
             "<rd><cd>2</cd><cd>Beeblebrox</cd><cd>Zaphod</cd></rd> \n" + 
             "<rd><cd>3</cd><cd>Prefect</cd><cd>Ford</cd></rd> \n" + 
             "</data> \n" + 
             "</wb-export>";
		try
		{
			File xmlFile = new File(this.basedir, "partial_xml_import.xml");
			BufferedWriter out = new BufferedWriter(EncodingUtil.createWriter(xmlFile, "UTF-8", false));
			out.write(xml);
			out.close();
			
			String cmd = "wbimport -importcolumns=nr,lastname -encoding='UTF-8' -file='" + xmlFile.getAbsolutePath() + "' -type=xml -table=junit_test";
			StatementRunnerResult result = importCmd.execute(this.connection, cmd);
			assertEquals("Import failed: " + result.getMessageBuffer().toString(), result.isSuccess(), true);
			
			Statement stmt = this.connection.createStatementForQuery();
			ResultSet rs = stmt.executeQuery("select nr, lastname, firstname from junit_test");
			int rowCount = 0;
			
			while (rs.next())
			{
				rowCount ++;
				int nr = rs.getInt(1);
				assertEquals("Wrong data imported", rowCount, nr);
				String lastname = rs.getString(2);
				switch (nr)
				{
					case 1: 
						assertEquals("Wrong data imported", "Dent", lastname);
						break;
					case 2: 
						assertEquals("Wrong data imported", "Beeblebrox", lastname);
						break;
					case 3: 
						assertEquals("Wrong data imported", "Prefect", lastname);
						break;
				}
				String firstname = rs.getString(3);
				assertNull("Omitted column imported", firstname);
				
			}
			assertEquals("Wrong number of rows", rowCount, 3);
			rs.close();
			stmt.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public void testMissingXmlColumn()
	{
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n" + 
             "<wb-export> \n" + 
             "  <meta-data> \n" + 
             " \n" + 
             "    <generating-sql> \n" + 
             "    <![CDATA[ \n" + 
             "    select id, lastname, firstname from person \n" + 
             "    ]]> \n" + 
             "    </generating-sql> \n" + 
             " \n" + 
             "    <created>2006-07-29 23:31:40.366 CEST</created> \n" + 
             "    <jdbc-driver>HSQL Database Engine Driver</jdbc-driver> \n" + 
             "    <jdbc-driver-version>1.8.0</jdbc-driver-version> \n" + 
             "    <connection>User=SA, URL=jdbc:hsqldb:d:/daten/db/hsql18/test</connection> \n" + 
             "    <database-product-name>HSQL Database Engine</database-product-name> \n" + 
             "    <database-product-version>1.8.0</database-product-version> \n" + 
             "    <wb-tag-format>short</wb-tag-format> \n" + 
             "  </meta-data> \n" + 
             " \n" + 
             "  <table-def> \n" + 
             "    <!-- The following information was retrieved from the JDBC driver's ResultSetMetaData --> \n" + 
             "    <!-- column-name is retrieved from ResultSetMetaData.getColumnName() --> \n" + 
             "    <!-- java-class is retrieved from ResultSetMetaData.getColumnClassName() --> \n" + 
             "    <!-- java-sql-type-name is the constant's name from java.sql.Types --> \n" + 
             "    <!-- java-sql-type is the constant's numeric value from java.sql.Types as returned from ResultSetMetaData.getColumnType() --> \n" + 
             "    <!-- dbms-data-type is retrieved from ResultSetMetaData.getColumnTypeName() --> \n" + 
             " \n" + 
             "    <!-- For date and timestamp types, the internal long value obtained from java.util.Date.getTime() \n" + 
             "         is written as an attribute to the <column-data> tag. That value can be used \n" + 
             "         to create a java.util.Date() object directly, without the need to parse the actual tag content. \n" + 
             "         If Java is not used to parse this file, the date/time format used to write the data \n" + 
             "         is provided in the <data-format> tag of the column definition \n" + 
             "    --> \n" + 
             " \n" + 
             "    <table-name>junit_test</table-name> \n" + 
             "    <column-count>4</column-count> \n" + 
             " \n" + 
             "    <column-def index=\"0\"> \n" + 
             "      <column-name>NR</column-name> \n" + 
             "      <java-class>java.lang.Integer</java-class> \n" + 
             "      <java-sql-type-name>INTEGER</java-sql-type-name> \n" + 
             "      <java-sql-type>4</java-sql-type> \n" + 
             "      <dbms-data-type>INTEGER</dbms-data-type> \n" + 
             "    </column-def> \n" + 
             "    <column-def index=\"1\"> \n" + 
             "      <column-name>LASTNAME</column-name> \n" + 
             "      <java-class>java.lang.String</java-class> \n" + 
             "      <java-sql-type-name>VARCHAR</java-sql-type-name> \n" + 
             "      <java-sql-type>12</java-sql-type> \n" + 
             "      <dbms-data-type>VARCHAR(100)</dbms-data-type> \n" + 
             "    </column-def> \n" + 
             "    <column-def index=\"2\"> \n" + 
             "      <column-name>FIRSTNAME</column-name> \n" + 
             "      <java-class>java.lang.String</java-class> \n" + 
             "      <java-sql-type-name>VARCHAR</java-sql-type-name> \n" + 
             "      <java-sql-type>12</java-sql-type> \n" + 
             "      <dbms-data-type>VARCHAR(100)</dbms-data-type> \n" + 
             "    </column-def> \n" + 
             "    <column-def index=\"3\"> \n" + 
             "      <column-name>EMAIL</column-name> \n" + 
             "      <java-class>java.lang.String</java-class> \n" + 
             "      <java-sql-type-name>VARCHAR</java-sql-type-name> \n" + 
             "      <java-sql-type>12</java-sql-type> \n" + 
             "      <dbms-data-type>VARCHAR(100)</dbms-data-type> \n" + 
             "    </column-def> \n" + 
             "  </table-def> \n" + 
             " \n" + 
             "<data> \n" + 
             "<rd><cd>1</cd><cd>Dent</cd><cd>Arthur</cd></rd> \n" + 
             "<rd><cd>2</cd><cd>Beeblebrox</cd><cd>Zaphod</cd></rd> \n" + 
             "<rd><cd>3</cd><cd>Prefect</cd><cd>Ford</cd></rd> \n" + 
             "</data> \n" + 
             "</wb-export>";
		try
		{
			File xmlFile = new File(this.basedir, "partial_xml_import.xml");
			BufferedWriter out = new BufferedWriter(EncodingUtil.createWriter(xmlFile, "UTF-8", false));
			out.write(xml);
			out.close();
			
			String cmd = "wbimport -continueOnError=false -encoding='UTF-8' -file='" + xmlFile.getAbsolutePath() + "' -type=xml -table=junit_test";
			StatementRunnerResult result = importCmd.execute(this.connection, cmd);
			assertEquals("Import succeeded", result.isSuccess(), false);

			cmd = "wbimport -encoding='UTF-8' -continueOnError=true -file='" + xmlFile.getAbsolutePath() + "' -type=xml -table=junit_test";
			result = importCmd.execute(this.connection, cmd);
			assertEquals("Import failed", result.isSuccess(), true);
			
			Statement stmt = this.connection.createStatementForQuery();
			ResultSet rs = stmt.executeQuery("select count(*) from junit_test");
			int rows = 0;
			if (rs.next()) rows = rs.getInt(1);
			assertEquals("Wrong number of rows imported", 3, rows);
			SqlUtil.closeAll(rs, stmt);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testTextClobImport()
	{
		try
		{
			File importFile  = new File(this.basedir, "import.txt");
			PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(importFile), "UTF-8"));
			out.println("nr\ttext_data");
			out.println("1\ttext_data_r1_c2.data");
			out.println("2\ttext_data_r2_c2.data");
			out.close();
			String data1 = "This is a CLOB string to be put into row 1";
			String data2 = "This is a CLOB string to be put into row 2";
			
			File datafile = new File(this.basedir, "text_data_r1_c2.data");
			out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(datafile), "UTF-8"));
			out.print(data1);
			out.close();
			
			datafile = new File(this.basedir, "text_data_r2_c2.data");
			out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(datafile), "UTF-8"));
			out.print(data2);
			out.close();
			
			StatementRunnerResult result = importCmd.execute(this.connection, "-- this is the import test\nwbimport -encoding=utf8 -file='" + importFile.getAbsolutePath() + "' -clobIsFilename=true -type=text -header=true -continueonerror=false -table=clob_test");
			assertEquals("Import failed: " + result.getMessageBuffer().toString(), result.isSuccess(), true);

			Statement stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery("select nr, text_data from clob_test order by nr");
			if (rs.next())
			{
				int nr = rs.getInt(1);
				String data = rs.getString(2);
				assertEquals(1, nr);
				assertEquals(data1, data);
			}
			else
			{
				fail("Not enough values imported");
			}
			if (rs.next())
			{
				int nr = rs.getInt(1);
				String data = rs.getString(2);
				assertEquals(2, nr);
				assertEquals(data2, data);
			}
			else
			{
				fail("Not enough values imported");
			}
			SqlUtil.closeAll(rs, stmt);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testRegularImport()
		throws Exception
	{
		int rowCount = 10;
		try
		{
			String name = "\u0627\u0644\u0633\u0639\u0631 \u0627\u0644\u0645\u0642\u062A\u0631\u062D \u0644\u0644\u0645\u0633\u0647\u0644\u0643";
			File importFile  = new File(this.basedir, "import.txt");
			PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(importFile), "UTF-8"));
			out.println("nr\tfirstname\tlastname");
			for (int i = 0; i < rowCount; i++)
			{
				out.print(Integer.toString(i));
				out.print('\t');
				out.println("First" + i + "\tLastname" + i);
			}
			// Make sure encoding is working
			out.println("999\tUnifirst\t"+name);
			rowCount ++;
			
			// test for empty values (should be stored as NULL)
			out.println("  \tempty nr\tempty");
			rowCount ++;
			
			// Check that quote characters are used if not specified
			out.println("42\tarthur\"dent\tempty");
			rowCount ++;
			out.close();
			
			StatementRunnerResult result = importCmd.execute(this.connection, "-- this is the import test\nwbimport -encoding=utf8 -file='" + importFile.getAbsolutePath() + "' -multiline=false -type=text -header=true -continueonerror=false -table=junit_test");
			assertEquals("Import failed: " + result.getMessageBuffer().toString(), result.isSuccess(), true);
			
			Statement stmt = this.connection.createStatementForQuery();
			ResultSet rs = stmt.executeQuery("select count(*) from junit_test");
			int count = -1;
			if (rs.next())
			{
				count = rs.getInt(1);
			}
			assertEquals("Not enough values imported", rowCount, count);
			
			rs.close();
			
			rs = stmt.executeQuery("select lastname from junit_test where nr = 999");
			if (rs.next())
			{
				String sname = rs.getString(1);
				assertEquals("Unicode incorrectly imported", name, sname);
			}
			else
			{
				fail("Unicode row not imported");
			}
			rs.close();
			
			rs = stmt.executeQuery("select firstname from junit_test where nr = 42");
			if (rs.next())
			{
				String sname = rs.getString(1);
				assertEquals("Embedded quote not imported", "arthur\"dent", sname);
			}
			else
			{
				fail("Row with embedded quote not imported");
			}
			rs.close();
			stmt.close();
			
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	public void testSkipImport()
		throws Exception
	{
		int rowCount = 10;
		try
		{
			File importFile  = new File(this.basedir, "partial.txt");
			PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(importFile), "UTF-8"));
			out.println("nr\tfirstname\tlastname");
			out.println("1\tArthur\tDent");
			out.println("2\tZaphod\tBeeblebrox");
			out.close();
			
			StatementRunnerResult result = importCmd.execute(this.connection, "-- this is the import test\nwbimport -encoding=utf8 -file='" + importFile.getAbsolutePath() + "' -filecolumns=nr,$wb_skip$,lastname -type=text -header=true -continueonerror=false -table=junit_test");
			assertEquals("Import failed: " + result.getMessageBuffer().toString(), result.isSuccess(), true);
			
			Statement stmt = this.connection.createStatementForQuery();
			ResultSet rs = stmt.executeQuery("select nr, firstname, lastname from junit_test");
			while (rs.next())
			{
				int nr = rs.getInt(1);
				String fname = rs.getString(2);
				String lname = rs.getString(3);
				assertNull("Firstname imported for nr=" + nr, fname);
				if (nr == 1)
				{
					assertEquals("Wrong lastname", "Dent", lname);
				}
				else if (nr == 2)
				{
					assertEquals("Wrong lastname", "Beeblebrox", lname);
				}
				else
				{
					fail("Wrong lines imported");
				}
			}

			rs.close();
			stmt.close();
			
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	public void testPartialTextImport()
		throws Exception
	{
		int rowCount = 100;
		try
		{
			File importFile  = new File(this.basedir, "partial.txt");
			PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(importFile), "UTF-8"));
			out.println("nr\tfirstname\tlastname");
			for (int i = 0; i < rowCount; i++)
			{
				int id = i+1;
				out.println(id + "\tFirstname" + id + "\tLastname" + id);
			}
			out.close();
			
			StatementRunnerResult result = importCmd.execute(this.connection, "-- this is the import test\nwbimport -encoding=utf8 -file='" + importFile.getAbsolutePath() + "' -filecolumns=nr,firstname,lastname -type=text -header=true -continueonerror=false -startrow=10 -endrow=20 -table=junit_test");
			assertEquals("Import failed: " + result.getMessageBuffer().toString(), result.isSuccess(), true);
			
			Statement stmt = this.connection.createStatementForQuery();
			ResultSet rs = stmt.executeQuery("select min(nr), max(nr), count(*) from junit_test");
			if (rs.next())
			{
				int min = rs.getInt(1);
				int max = rs.getInt(2);
				int count = rs.getInt(3);
				assertEquals("Import started at wrong id", 10, min);
				assertEquals("Import ended at wrong id", 20, max);
				assertEquals("Wrong number of rows imported", 11, count);
			}
			else
			{
				fail("No data imported");
			}
			rs.close();
			stmt.close();
			
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}
	
	public void testPartialColumnTextImport()
		throws Exception
	{
		int rowCount = 10;
		try
		{
			File importFile  = new File(this.basedir, "partial.txt");
			PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(importFile), "UTF-8"));
			out.println("nr\tfirstname\tlastname");
			out.println("1\tArthur\tDent");
			out.println("2\tZaphod\tBeeblebrox");
			out.close();
			
			StatementRunnerResult result = importCmd.execute(this.connection, "-- this is the import test\nwbimport -encoding=utf8 -file='" + importFile.getAbsolutePath() + "' -filecolumns=nr,firstname,lastname -importcolumns=nr,lastname -type=text -header=true -continueonerror=false -table=junit_test");
			assertEquals("Import failed: " + result.getMessageBuffer().toString(), result.isSuccess(), true);
			
			Statement stmt = this.connection.createStatementForQuery();
			ResultSet rs = stmt.executeQuery("select nr, firstname, lastname from junit_test");
			while (rs.next())
			{
				int nr = rs.getInt(1);
				String fname = rs.getString(2);
				String lname = rs.getString(3);
				assertNull("Firstname imported for nr=" + nr, fname);
				if (nr == 1)
				{
					assertEquals("Wrong lastname", "Dent", lname);
				}
				else if (nr == 2)
				{
					assertEquals("Wrong lastname", "Beeblebrox", lname);
				}
				else
				{
					fail("Wrong lines imported");
				}
			}

			rs.close();
			stmt.close();
			
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}
	
	public void testMultiLineImport()
	{
		int rowCount = 10;
		try
		{
			File importFile  = new File(this.basedir, "multi.txt");
			PrintWriter out = new PrintWriter(new FileWriter(importFile));
			out.println("firstname\tlastname\tnr");
			out.println("First\t\"Last\r\nname\"\t1");
			out.println("first2\tlast2\t2");
			out.println("first3\t\"last3\r\nlast3last3\"\t3");
			out.println("first4\t\"last4\tlast4\"\t4");
			out.close();
			
			StatementRunnerResult result = importCmd.execute(this.connection, "wbimport -file='" + importFile.getAbsolutePath() + "' -multiline=true -quotechar='\"' -type=text -header=true -continueonerror=false -table=junit_test");
			assertEquals("Import failed: " + result.getMessageBuffer().toString(), result.isSuccess(), true);
			
			Statement stmt = this.connection.createStatementForQuery();
			ResultSet rs = stmt.executeQuery("select nr, firstname, lastname from junit_test");
			int count = 0;
			while (rs.next())
			{
				count ++;
				int nr = rs.getInt(1);
				String first = rs.getString(2);
				String last = rs.getString(3);
				assertEquals("Wrong nr imported", count, nr);
				if (count == 1)
				{
					assertEquals("Wrong firstname imported", "First", first);
					assertEquals("Wrong firstname imported", "Last\r\nname", last);
				}
				else if (count == 2)
				{
					assertEquals("Wrong firstname imported", "first2", first);
					assertEquals("Wrong firstname imported", "last2", last);
				}
				else if (count == 3)
				{
					assertEquals("Wrong firstname imported", "first3", first);
					assertEquals("Wrong firstname imported", "last3\r\nlast3last3", last);
				}
				else if (count == 4)
				{
					assertEquals("Wrong firstname imported", "first4", first);
					assertEquals("Wrong firstname imported", "last4\tlast4", last);
				}
			}
			assertEquals("Wrong number of rows imported", 4, count);
			rs.close();
			stmt.close();
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}
	
	public void testZipMultiLineImport()
	{
		int rowCount = 10;
		try
		{
			
			File importFile  = new File(this.basedir, "zipmulti.txt");
			
			File archive = new File(this.basedir, "zipmulti.zip");
			ZipOutputFactory zout = new ZipOutputFactory(archive);
			PrintWriter out = new PrintWriter(zout.createWriter(importFile, "UTF-8"));
			
			out.println("nr\tfirstname\tlastname");
			out.print(Integer.toString(1));
			out.print('\t');
			out.println("First\t\"Last");
			out.println("name\"");
			out.close();
			zout.done();
			
			StatementRunnerResult result = importCmd.execute(this.connection, "wbimport -file='" + archive.getAbsolutePath() + "' -multiline=true -quotechar='\"' -type=text -header=true -continueonerror=false -table=junit_test");
			assertEquals("Import failed: " + result.getMessageBuffer().toString(), result.isSuccess(), true);
			
			Statement stmt = this.connection.createStatementForQuery();
			ResultSet rs = stmt.executeQuery("select nr, firstname, lastname from junit_test");
			int count = -1;
			if (rs.next())
			{
				int nr = rs.getInt(1);
				assertEquals("Wrong nr imported", 1, nr);
				
				String first = rs.getString(2);
				assertEquals("Wrong firstname imported", "First", first);
				
				String last = rs.getString(3);
				assertEquals("Wrong firstname imported", "Last\r\nname", last);
			}
			else
			{
				fail("No data imported");
			}
			rs.close();
			stmt.close();
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}
	
	public void testNoHeader()
		throws Exception
	{
		int rowCount = 10;
		try
		{
			File importFile  = new File(this.basedir, "import.txt");
			PrintWriter out = new PrintWriter(new FileWriter(importFile));
			for (int i = 0; i < rowCount; i++)
			{
				out.print(Integer.toString(i));
				out.print('\t');
				out.println("First" + i + "\tLastname" + i);
			}
			out.close();
			
			StatementRunnerResult result = importCmd.execute(this.connection, "wbimport -file='" + importFile.getAbsolutePath() + "' -multiline=true  -type=text -filecolumns=nr,firstname,lastname -header=false -table=junit_test");
			assertEquals("Export failed: " + result.getMessageBuffer().toString(), result.isSuccess(), true);
			
			Statement stmt = this.connection.createStatementForQuery();
			ResultSet rs = stmt.executeQuery("select count(*) from junit_test");
			int count = -1;
			if (rs.next())
			{
				count = rs.getInt(1);
			}
			assertEquals("Not enough values imported", rowCount, count);
			rs.close();
			rs = stmt.executeQuery("select nr,firstname,lastname from junit_test order by nr");
			if (rs.next())
			{
				int nr = rs.getInt(1);
				assertEquals("Wrong values imported", nr, 0);
			}
			rs.close();
			stmt.close();
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	public void testColumnsFromTable()
		throws Exception
	{
		int rowCount = 10;
		try
		{
			File importFile  = new File(this.basedir, "import.txt");
			PrintWriter out = new PrintWriter(new FileWriter(importFile));
			for (int i = 0; i < rowCount; i++)
			{
				out.print(Integer.toString(i));
				out.print('\t');
				out.println("First" + i + "\tLastname" + i);
			}
			out.close();
			
			StatementRunnerResult result = importCmd.execute(this.connection, "wbimport -file='" + importFile.getAbsolutePath() + "' -multiline=true -type=text -header=false -table=junit_test");
			assertEquals("Export failed: " + result.getMessageBuffer().toString(), result.isSuccess(), true);
			
			Statement stmt = this.connection.createStatementForQuery();
			ResultSet rs = stmt.executeQuery("select count(*) from junit_test");
			int count = -1;
			if (rs.next())
			{
				count = rs.getInt(1);
			}
			assertEquals("Not enough values imported", rowCount, count);
			rs.close();
			rs = stmt.executeQuery("select nr,firstname,lastname from junit_test order by nr");
			if (rs.next())
			{
				int nr = rs.getInt(1);
				assertEquals("Wrong values imported", nr, 0);
			}
			rs.close();
			stmt.close();
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}
	
	public void testDirImport()
		throws Exception
	{
		int rowCount = 10;
		try
		{
			File importFile  = new File(this.basedir, "junit_test.txt");
			PrintWriter out = new PrintWriter(new FileWriter(importFile));
			//out.println("nr\tfirstname\tlastname");
			for (int i = 0; i < rowCount; i++)
			{
				out.print(Integer.toString(i));
				out.print('\t');
				out.println("First" + i + "\tLastname" + i);
			}
			out.close();
			
			out = new PrintWriter(new FileWriter(new File(this.basedir, "datatype_test.txt")));
			//out.println("int_col\tdouble_col\tchar_col\tdate_col\ttime_col\tts_col");
			out.println("42\t42.1234\tfortytwo\t2006-02-01\t22:30\t2006-04-01 22:34:14\t");
			out.close();
			
			StatementRunnerResult result = importCmd.execute(this.connection, "wbimport -header=false -continueonerror=false -sourcedir='" + importFile.getParent() + "' -type=text");
			assertEquals("Export failed: " + result.getMessageBuffer().toString(), result.isSuccess(), true);
			
			Statement stmt = this.connection.createStatementForQuery();
			ResultSet rs = stmt.executeQuery("select count(*) from junit_test");
			int count = -1;
			if (rs.next())
			{
				count = rs.getInt(1);
			}
			assertEquals("Not enough values in table junit_test", rowCount, count);
			
			rs = stmt.executeQuery("select count(*) from datatype_test");
			count = -1;
			if (rs.next())
			{
				count = rs.getInt(1);
			}
			assertEquals("Not enough values in table datatype_test", 1, count);
			
			rs.close();
			stmt.close();
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}	
	
	public void testMappedImport()
		throws Exception
	{
		int rowCount = 10;
		try
		{
			File importFile  = new File(this.basedir, "import.txt");
			PrintWriter out = new PrintWriter(new FileWriter(importFile));
			out.println("nr\tpid\tfirstname\tlastname");
			for (int i = 0; i < rowCount; i++)
			{
				out.print(Integer.toString(i));
				out.print('\t');
				out.println("First" + i + "\tLastname" + i);
			}
			out.close();
			
			StatementRunnerResult result = importCmd.execute(this.connection, "wbimport -file='" + importFile.getAbsolutePath() + "' -type=text -continueonerror=true -header=true -table=junit_test");
			assertEquals("Export failed: " + result.getMessageBuffer().toString(), result.isSuccess(), true);
			
			Statement stmt = this.connection.createStatementForQuery();
			ResultSet rs = stmt.executeQuery("select count(*) from junit_test");
			int count = -1;
			if (rs.next())
			{
				count = rs.getInt(1);
			}
			assertEquals("Not enough values imported", rowCount, count);
			rs.close();
			stmt.close();
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}
	
	public void testDataTypes()
		throws Exception
	{
		int rowCount = 10;
		try
		{
			File importFile  = new File(this.basedir, "import_types.txt");
			PrintWriter out = new PrintWriter(new FileWriter(importFile));
			out.println("int_col\tdouble_col\tchar_col\tdate_col\ttime_col\tts_col");
			out.println("42\t42.1234\tfortytwo\t2006-02-01\t22:30\t2006-04-01 22:34\t");
			out.close();
			
			StatementRunnerResult result = importCmd.execute(this.connection, "wbimport -file='" + importFile.getAbsolutePath() + "' -decimal='.' -type=text -header=true -table=datatype_test -dateformat=yyyy-MM-dd -timestampformat=yyyy-MM-dd HH:mm");
			assertEquals("Import failed: " + result.getMessageBuffer().toString(), result.isSuccess(), true);
			
			Statement stmt = this.connection.createStatementForQuery();
			ResultSet rs = stmt.executeQuery("select int_col, double_col, char_col, date_col, time_col, ts_col from datatype_test");
			if (rs.next())
			{
				int i = rs.getInt(1);
				double d = rs.getDouble(2);
				String s = rs.getString(3);
				Date dt = rs.getDate(4);
				Time tt = rs.getTime(5);
				Timestamp ts = rs.getTimestamp(6);
				assertEquals("Wrong integer value", 42, i);
				assertEquals("Wrong varchar imported", "fortytwo", s);
				assertEquals("Wrong double value", 42.1234, d, 0.01);
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				java.util.Date d2 = df.parse("2006-02-01");
				assertEquals("Wrong date imported", d2, dt);
				
				df = new SimpleDateFormat("HH:mm");
				d2 = df.parse("22:30");
				java.sql.Time tm = new java.sql.Time(d2.getTime());
				assertEquals("Wrong time imported", tm, tt);
				
				df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				d2 = df.parse("2006-04-01 22:34");
				assertEquals("Wrong timestamp imported", d2, ts);
				
			}
			else
			{
				fail("No rows imported!");
			}
			rs.close();
			stmt.close();
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

	public void testZippedTextBlobImport()
	{
		int rowCount = 10;
		try
		{
			File importFile  = new File(this.basedir, "blob_test.txt");
			
			File archive = new File(this.basedir, "blob_test.zip");
			ZipOutputFactory zout = new ZipOutputFactory(archive);
			Writer w = zout.createWriter(importFile, "UTF-8");
			
			PrintWriter out = new PrintWriter(w);
			out.println("nr\tbinary_data");
			out.println("1\tblob_data_r1_c1.data");
			out.close();
			
			zout.done();
			
			File blobarchive = new File(this.basedir, "blob_test" + RowDataConverter.BLOB_ARCHIVE_SUFFIX + ".zip");
			zout = new ZipOutputFactory(blobarchive);
			OutputStream binaryOut = zout.createOutputStream(new File("blob_data_r1_c1.data"));
			
			byte[] testData = new byte[1024];
			for (int i = 0; i < testData.length; i++)
			{
				testData[i] = (byte)(i % 255);
			}
			binaryOut.write(testData);
			binaryOut.close();

			zout.done();

			StatementRunnerResult result = importCmd.execute(this.connection, "wbimport -file='" + archive.getAbsolutePath() + "' -decimal='.' -multiline=true -encoding='UTF-8' -type=text -header=true -table=blob_test");
			assertEquals("Import failed: " + result.getMessageBuffer().toString(), result.isSuccess(), true);
			
			Statement stmt = this.connection.createStatementForQuery();
			ResultSet rs = stmt.executeQuery("select nr, binary_data from blob_test");
			if (rs.next())
			{
				int nr = rs.getInt(1);
				assertEquals("Wrong data imported", 1, nr);
				
				Object blob = rs.getObject(2);
				assertNotNull("No blob data imported", blob);
				if (blob instanceof byte[])
				{
					byte[] retrievedData = (byte[])blob;
					assertEquals("Wrong blob size importee", 1024, retrievedData.length);
					assertEquals("Wrong content of blob data", retrievedData[0], 0);
					assertEquals("Wrong content of blob data", retrievedData[1], 1);
					assertEquals("Wrong content of blob data", retrievedData[2], 2);
					assertEquals("Wrong content of blob data", retrievedData[3], 3);
				}
				else
				{
					fail("Wrong blob data returned");
				}
			}
			else
			{
				fail("No rows imported");
			}
			rs.close();
			stmt.close();
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}
	
	public void testXmlImport()
	{
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n" + 
             "<wb-export> \n" + 
             "  <meta-data> \n" + 
             " \n" + 
             "    <generating-sql> \n" + 
             "    <![CDATA[ \n" + 
             "    select id, lastname, firstname from person \n" + 
             "    ]]> \n" + 
             "    </generating-sql> \n" + 
             " \n" + 
             "    <created>2006-07-29 23:31:40.366 CEST</created> \n" + 
             "    <jdbc-driver>HSQL Database Engine Driver</jdbc-driver> \n" + 
             "    <jdbc-driver-version>1.8.0</jdbc-driver-version> \n" + 
             "    <connection>User=SA, URL=jdbc:hsqldb:d:/daten/db/hsql18/test</connection> \n" + 
             "    <database-product-name>HSQL Database Engine</database-product-name> \n" + 
             "    <database-product-version>1.8.0</database-product-version> \n" + 
             "    <wb-tag-format>short</wb-tag-format> \n" + 
             "  </meta-data> \n" + 
             " \n" + 
             "  <table-def> \n" + 
             "    <!-- The following information was retrieved from the JDBC driver's ResultSetMetaData --> \n" + 
             "    <!-- column-name is retrieved from ResultSetMetaData.getColumnName() --> \n" + 
             "    <!-- java-class is retrieved from ResultSetMetaData.getColumnClassName() --> \n" + 
             "    <!-- java-sql-type-name is the constant's name from java.sql.Types --> \n" + 
             "    <!-- java-sql-type is the constant's numeric value from java.sql.Types as returned from ResultSetMetaData.getColumnType() --> \n" + 
             "    <!-- dbms-data-type is retrieved from ResultSetMetaData.getColumnTypeName() --> \n" + 
             " \n" + 
             "    <!-- For date and timestamp types, the internal long value obtained from java.util.Date.getTime() \n" + 
             "         is written as an attribute to the <column-data> tag. That value can be used \n" + 
             "         to create a java.util.Date() object directly, without the need to parse the actual tag content. \n" + 
             "         If Java is not used to parse this file, the date/time format used to write the data \n" + 
             "         is provided in the <data-format> tag of the column definition \n" + 
             "    --> \n" + 
             " \n" + 
             "    <table-name>junit_test</table-name> \n" + 
             "    <column-count>3</column-count> \n" + 
             " \n" + 
             "    <column-def index=\"0\"> \n" + 
             "      <column-name>NR</column-name> \n" + 
             "      <java-class>java.lang.Integer</java-class> \n" + 
             "      <java-sql-type-name>INTEGER</java-sql-type-name> \n" + 
             "      <java-sql-type>4</java-sql-type> \n" + 
             "      <dbms-data-type>INTEGER</dbms-data-type> \n" + 
             "    </column-def> \n" + 
             "    <column-def index=\"1\"> \n" + 
             "      <column-name>LASTNAME</column-name> \n" + 
             "      <java-class>java.lang.String</java-class> \n" + 
             "      <java-sql-type-name>VARCHAR</java-sql-type-name> \n" + 
             "      <java-sql-type>12</java-sql-type> \n" + 
             "      <dbms-data-type>VARCHAR(100)</dbms-data-type> \n" + 
             "    </column-def> \n" + 
             "    <column-def index=\"2\"> \n" + 
             "      <column-name>FIRSTNAME</column-name> \n" + 
             "      <java-class>java.lang.String</java-class> \n" + 
             "      <java-sql-type-name>VARCHAR</java-sql-type-name> \n" + 
             "      <java-sql-type>12</java-sql-type> \n" + 
             "      <dbms-data-type>VARCHAR(100)</dbms-data-type> \n" + 
             "    </column-def> \n" + 
             "  </table-def> \n" + 
             " \n" + 
             "<data> \n" + 
             "<rd><cd>1</cd><cd>Dent</cd><cd>Arthur</cd></rd> \n" + 
             "<rd><cd>2</cd><cd>Beeblebrox</cd><cd>Zaphod</cd></rd> \n" + 
             "</data> \n" + 
             "</wb-export>";
		try
		{
			File xmlFile = new File(this.basedir, "xml_import.xml");
			BufferedWriter out = new BufferedWriter(EncodingUtil.createWriter(xmlFile, "UTF-8", false));
			out.write(xml);
			out.close();
			
			String cmd = "wbimport -encoding='UTF-8' -file='" + xmlFile.getAbsolutePath() + "' -type=xml -table=junit_test";
			//System.out.println("cmd=" + cmd);
			StatementRunnerResult result = importCmd.execute(this.connection, cmd);
			assertEquals("Import failed: " + result.getMessageBuffer().toString(), result.isSuccess(), true);
			
			Statement stmt = this.connection.createStatementForQuery();
			ResultSet rs = stmt.executeQuery("select nr, firstname, lastname from junit_test");
			int rowCount = 0;
			
			while (rs.next())
			{
				rowCount ++;
				int nr = rs.getInt(1);
				assertEquals("Wrong data imported", rowCount, nr);
			}
			assertEquals("Wrong number of rows", rowCount, 2);
			rs.close();
			stmt.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
	}


	public void testPartialXmlImport()
	{
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n" + 
             "<wb-export> \n" + 
             "  <meta-data> \n" + 
             " \n" + 
             "    <generating-sql> \n" + 
             "    <![CDATA[ \n" + 
             "    select id, lastname, firstname from person \n" + 
             "    ]]> \n" + 
             "    </generating-sql> \n" + 
             " \n" + 
             "    <created>2006-07-29 23:31:40.366 CEST</created> \n" + 
             "    <jdbc-driver>HSQL Database Engine Driver</jdbc-driver> \n" + 
             "    <jdbc-driver-version>1.8.0</jdbc-driver-version> \n" + 
             "    <connection>User=SA, URL=jdbc:hsqldb:d:/daten/db/hsql18/test</connection> \n" + 
             "    <database-product-name>HSQL Database Engine</database-product-name> \n" + 
             "    <database-product-version>1.8.0</database-product-version> \n" + 
             "    <wb-tag-format>short</wb-tag-format> \n" + 
             "  </meta-data> \n" + 
             " \n" + 
             "  <table-def> \n" + 
             "    <!-- The following information was retrieved from the JDBC driver's ResultSetMetaData --> \n" + 
             "    <!-- column-name is retrieved from ResultSetMetaData.getColumnName() --> \n" + 
             "    <!-- java-class is retrieved from ResultSetMetaData.getColumnClassName() --> \n" + 
             "    <!-- java-sql-type-name is the constant's name from java.sql.Types --> \n" + 
             "    <!-- java-sql-type is the constant's numeric value from java.sql.Types as returned from ResultSetMetaData.getColumnType() --> \n" + 
             "    <!-- dbms-data-type is retrieved from ResultSetMetaData.getColumnTypeName() --> \n" + 
             " \n" + 
             "    <!-- For date and timestamp types, the internal long value obtained from java.util.Date.getTime() \n" + 
             "         is written as an attribute to the <column-data> tag. That value can be used \n" + 
             "         to create a java.util.Date() object directly, without the need to parse the actual tag content. \n" + 
             "         If Java is not used to parse this file, the date/time format used to write the data \n" + 
             "         is provided in the <data-format> tag of the column definition \n" + 
             "    --> \n" + 
             " \n" + 
             "    <table-name>junit_test</table-name> \n" + 
             "    <column-count>3</column-count> \n" + 
             " \n" + 
             "    <column-def index=\"0\"> \n" + 
             "      <column-name>NR</column-name> \n" + 
             "      <java-class>java.lang.Integer</java-class> \n" + 
             "      <java-sql-type-name>INTEGER</java-sql-type-name> \n" + 
             "      <java-sql-type>4</java-sql-type> \n" + 
             "      <dbms-data-type>INTEGER</dbms-data-type> \n" + 
             "    </column-def> \n" + 
             "    <column-def index=\"1\"> \n" + 
             "      <column-name>LASTNAME</column-name> \n" + 
             "      <java-class>java.lang.String</java-class> \n" + 
             "      <java-sql-type-name>VARCHAR</java-sql-type-name> \n" + 
             "      <java-sql-type>12</java-sql-type> \n" + 
             "      <dbms-data-type>VARCHAR(100)</dbms-data-type> \n" + 
             "    </column-def> \n" + 
             "    <column-def index=\"2\"> \n" + 
             "      <column-name>FIRSTNAME</column-name> \n" + 
             "      <java-class>java.lang.String</java-class> \n" + 
             "      <java-sql-type-name>VARCHAR</java-sql-type-name> \n" + 
             "      <java-sql-type>12</java-sql-type> \n" + 
             "      <dbms-data-type>VARCHAR(100)</dbms-data-type> \n" + 
             "    </column-def> \n" + 
             "  </table-def> \n" + 
             " \n" + 
             "<data> \n";
		String xmlEnd = "</data> \n" + 
             "</wb-export>";
		try
		{
			File xmlFile = new File(this.basedir, "xml_import2.xml");
			BufferedWriter out = new BufferedWriter(EncodingUtil.createWriter(xmlFile, "UTF-8", false));
			out.write(xml);
			for (int i=0; i < 100; i++)
			{
				int id = i + 1;
				out.write("<rd><cd>" + id + "</cd><cd>Lastname" + id + "</cd><cd>Firstname" + id + "</cd></rd>\n");
			}
			out.write(xmlEnd);
			out.close();
			
			String cmd = "wbimport -encoding='UTF-8' -file='" + xmlFile.getAbsolutePath() + "' -type=xml -startRow = 15 -endrow = 24 -table=junit_test";
			//System.out.println("cmd=" + cmd);
			StatementRunnerResult result = importCmd.execute(this.connection, cmd);
			assertEquals("Import failed: " + result.getMessageBuffer().toString(), result.isSuccess(), true);
			
			Statement stmt = this.connection.createStatementForQuery();
			ResultSet rs = stmt.executeQuery("select min(nr), max(nr), count(*) from junit_test");
			if (rs.next())
			{
				int min = rs.getInt(1);
				int max = rs.getInt(2);
				int count = rs.getInt(3);
				assertEquals("Import started at wrong id", 15, min);
				assertEquals("Import ended at wrong id", 24, max);
				assertEquals("Wrong number of rows imported", 10, count);
			}
			else
			{
				fail("No data imported");
			}
			rs.close();
			stmt.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testXmlBlobImport()
	{
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n" + 
             "<wb-export> \n" + 
             "  <meta-data> \n" + 
             " \n" + 
             "    <generating-sql> \n" + 
             "    <![CDATA[ \n" + 
             "    select * from blob_test \n" + 
             "    ]]> \n" + 
             "    </generating-sql> \n" + 
             " \n" + 
             "    <created>2006-07-30 00:05:59.316 CEST</created> \n" + 
             "    <jdbc-driver>HSQL Database Engine Driver</jdbc-driver> \n" + 
             "    <jdbc-driver-version>1.8.0</jdbc-driver-version> \n" + 
             "    <connection>User=SA, URL=jdbc:hsqldb:d:/daten/db/hsql18/test</connection> \n" + 
             "    <database-product-name>HSQL Database Engine</database-product-name> \n" + 
             "    <database-product-version>1.8.0</database-product-version> \n" + 
             "    <wb-tag-format>short</wb-tag-format> \n" + 
             "  </meta-data> \n" + 
             " \n" + 
             "  <table-def> \n" + 
             "    <table-name>blob_test</table-name> \n" + 
             "    <column-count>2</column-count> \n" + 
             "    <column-def index=\"0\"> \n" + 
             "      <column-name>NR</column-name> \n" + 
             "      <java-class>java.lang.Integer</java-class> \n" + 
             "      <java-sql-type-name>INTEGER</java-sql-type-name> \n" + 
             "      <java-sql-type>4</java-sql-type> \n" + 
             "      <dbms-data-type>INTEGER</dbms-data-type> \n" + 
             "    </column-def> \n" + 
             "    <column-def index=\"1\"> \n" + 
             "      <column-name>BINARY_DATA</column-name> \n" + 
             "      <java-class>byte[]</java-class> \n" + 
             "      <java-sql-type-name>BINARY</java-sql-type-name> \n" + 
             "      <java-sql-type>-2</java-sql-type> \n" + 
             "      <dbms-data-type>BINARY</dbms-data-type> \n" + 
             "    </column-def> \n" + 
             "  </table-def> \n" + 
             " \n" + 
             "<data> \n" + 
             "<rd><cd>1</cd><cd dataFile=\"test_r1_c2.data\"/></rd> \n" + 
             "</data> \n" + 
             "</wb-export>";
		try
		{
			File xmlFile = new File(this.basedir, "xml_import.xml");
			BufferedWriter out = new BufferedWriter(EncodingUtil.createWriter(xmlFile, "UTF-8", false));
			out.write(xml);
			out.close();

			File dataFile = new File(this.basedir, "test_r1_c2.data");
			FileOutputStream binaryOut = new FileOutputStream(dataFile);
			byte[] testData = new byte[1024];
			for (int i = 0; i < testData.length; i++)
			{
				testData[i] = (byte)(i % 255);
			}
			binaryOut.write(testData);
			binaryOut.close();
			
			StatementRunnerResult result = importCmd.execute(this.connection, "wbimport -encoding='UTF-8' -file='" + xmlFile.getAbsolutePath() + "' -type=xml -table=blob_test");
			assertEquals("Import failed: " + result.getMessageBuffer().toString(), result.isSuccess(), true);
			
			Statement stmt = this.connection.createStatementForQuery();
			ResultSet rs = stmt.executeQuery("select nr, binary_data from blob_test");
			int rowCount = 0;
			
			if (rs.next())
			{
				rowCount ++;
				int nr = rs.getInt(1);
				assertEquals("Wrong data imported", 1, nr);
				
				Object blob = rs.getObject(2);
				assertNotNull("No blob data imported", blob);
				if (blob instanceof byte[])
				{
					byte[] retrievedData = (byte[])blob;
					assertEquals("Wrong blob size imported", 1024, retrievedData.length);
					assertEquals("Wrong content of blob data", retrievedData[0], 0);
					assertEquals("Wrong content of blob data", retrievedData[1], 1);
					assertEquals("Wrong content of blob data", retrievedData[2], 2);
					assertEquals("Wrong content of blob data", retrievedData[3], 3);
				}
				else
				{
					fail("Wrong blob data returned");
				}
			}
			else
			{
				fail("Not enough data imported");
			}
			rs.close();
			stmt.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public void testXmlClobImport()
	{
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n" + 
             "<wb-export> \n" + 
             "  <meta-data> \n" + 
             " \n" + 
             "    <generating-sql> \n" + 
             "    <![CDATA[ \n" + 
             "    select * from blob_test \n" + 
             "    ]]> \n" + 
             "    </generating-sql> \n" + 
             " \n" + 
             "    <created>2006-07-30 00:05:59.316 CEST</created> \n" + 
             "    <jdbc-driver>HSQL Database Engine Driver</jdbc-driver> \n" + 
             "    <jdbc-driver-version>1.8.0</jdbc-driver-version> \n" + 
             "    <connection>User=SA, URL=jdbc:hsqldb:d:/daten/db/hsql18/test</connection> \n" + 
             "    <database-product-name>HSQL Database Engine</database-product-name> \n" + 
             "    <database-product-version>1.8.0</database-product-version> \n" + 
             "    <wb-tag-format>short</wb-tag-format> \n" + 
             "  </meta-data> \n" + 
             " \n" + 
             "  <table-def> \n" + 
             "    <table-name>clob_test</table-name> \n" + 
             "    <column-count>2</column-count> \n" + 
             "    <column-def index=\"0\"> \n" + 
             "      <column-name>NR</column-name> \n" + 
             "      <java-class>java.lang.Integer</java-class> \n" + 
             "      <java-sql-type-name>INTEGER</java-sql-type-name> \n" + 
             "      <java-sql-type>4</java-sql-type> \n" + 
             "      <dbms-data-type>INTEGER</dbms-data-type> \n" + 
             "    </column-def> \n" + 
             "    <column-def index=\"1\"> \n" + 
             "      <column-name>TEXT_DATA</column-name> \n" + 
             "      <java-class>java.lang.String</java-class> \n" + 
             "      <java-sql-type-name>LONGVARCHAR</java-sql-type-name> \n" + 
             "      <java-sql-type>-1</java-sql-type> \n" + 
             "      <dbms-data-type>LONGVARCHAR</dbms-data-type> \n" + 
             "    </column-def> \n" + 
             "  </table-def> \n" + 
             " \n" + 
             "<data> \n" + 
             "<rd><cd>1</cd><cd dataFile=\"test_r1_c2.data\"/></rd> \n" + 
             "</data> \n" + 
             "</wb-export>";
		try
		{
			File xmlFile = new File(this.basedir, "xml_import.xml");
			BufferedWriter out = new BufferedWriter(EncodingUtil.createWriter(xmlFile, "UTF-8", false));
			out.write(xml);
			out.close();

			File datafile = new File(this.basedir, "test_r1_c2.data");
			String data1 = "This is a CLOB string to be put into row 1";
			
			Writer pw = EncodingUtil.createWriter(datafile, "UTF-8", false);			
			pw.write(data1);
			pw.close();
			
			StatementRunnerResult result = importCmd.execute(this.connection, "wbimport -encoding='UTF-8' -file='" + xmlFile.getAbsolutePath() + "' -type=xml -table=clob_test");
			assertEquals("Import failed: " + result.getMessageBuffer().toString(), result.isSuccess(), true);
			
			Statement stmt = this.connection.createStatementForQuery();
			ResultSet rs = stmt.executeQuery("select nr, text_data from clob_test");
			int rowCount = 0;
			
			if (rs.next())
			{
				rowCount ++;
				int nr = rs.getInt(1);
				assertEquals("Wrong data imported", 1, nr);
				
				String data = rs.getString(2);
				assertEquals(data, data);
			}
			else
			{
				fail("Not enough data imported");
			}
			rs.close();
			stmt.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public void testZippedXmlBlobImport()
	{
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n" + 
             "<wb-export> \n" + 
             "  <meta-data> \n" + 
             " \n" + 
             "    <generating-sql> \n" + 
             "    <![CDATA[ \n" + 
             "    select * from blob_test \n" + 
             "    ]]> \n" + 
             "    </generating-sql> \n" + 
             " \n" + 
             "    <created>2006-07-30 00:05:59.316 CEST</created> \n" + 
             "    <jdbc-driver>HSQL Database Engine Driver</jdbc-driver> \n" + 
             "    <jdbc-driver-version>1.8.0</jdbc-driver-version> \n" + 
             "    <connection>User=SA, URL=jdbc:hsqldb:d:/daten/db/hsql18/test</connection> \n" + 
             "    <database-product-name>HSQL Database Engine</database-product-name> \n" + 
             "    <database-product-version>1.8.0</database-product-version> \n" + 
             "    <wb-tag-format>short</wb-tag-format> \n" + 
             "  </meta-data> \n" + 
             " \n" + 
             "  <table-def> \n" + 
             "    <!-- The following information was retrieved from the JDBC driver's ResultSetMetaData --> \n" + 
             "    <!-- column-name is retrieved from ResultSetMetaData.getColumnName() --> \n" + 
             "    <!-- java-class is retrieved from ResultSetMetaData.getColumnClassName() --> \n" + 
             "    <!-- java-sql-type-name is the constant's name from java.sql.Types --> \n" + 
             "    <!-- java-sql-type is the constant's numeric value from java.sql.Types as returned from ResultSetMetaData.getColumnType() --> \n" + 
             "    <!-- dbms-data-type is retrieved from ResultSetMetaData.getColumnTypeName() --> \n" + 
             " \n" + 
             "    <!-- For date and timestamp types, the internal long value obtained from java.util.Date.getTime() \n" + 
             "         is written as an attribute to the <column-data> tag. That value can be used \n" + 
             "         to create a java.util.Date() object directly, without the need to parse the actual tag content. \n" + 
             "         If Java is not used to parse this file, the date/time format used to write the data \n" + 
             "         is provided in the <data-format> tag of the column definition \n" + 
             "    --> \n" + 
             " \n" + 
             "    <table-name>blob_test</table-name> \n" + 
             "    <column-count>2</column-count> \n" + 
             " \n" + 
             "    <column-def index=\"0\"> \n" + 
             "      <column-name>NR</column-name> \n" + 
             "      <java-class>java.lang.Integer</java-class> \n" + 
             "      <java-sql-type-name>INTEGER</java-sql-type-name> \n" + 
             "      <java-sql-type>4</java-sql-type> \n" + 
             "      <dbms-data-type>INTEGER</dbms-data-type> \n" + 
             "    </column-def> \n" + 
             "    <column-def index=\"1\"> \n" + 
             "      <column-name>BINARY_DATA</column-name> \n" + 
             "      <java-class>byte[]</java-class> \n" + 
             "      <java-sql-type-name>BINARY</java-sql-type-name> \n" + 
             "      <java-sql-type>-2</java-sql-type> \n" + 
             "      <dbms-data-type>BINARY</dbms-data-type> \n" + 
             "    </column-def> \n" + 
             "  </table-def> \n" + 
             " \n" + 
             "<data> \n" + 
             "<rd><cd>1</cd><cd dataFile=\"test_r1_c2.data\"/></rd> \n" + 
             "</data> \n" + 
             "</wb-export>";
		try
		{
			File xmlFile = new File(this.basedir, "xml_import.xml");
			
			File archive = new File(this.basedir, "blob_test.zip");
			ZipOutputFactory zout = new ZipOutputFactory(archive);
			Writer w = zout.createWriter(xmlFile, "UTF-8");
			w.write(xml);
			w.close();
			zout.done();
			
			File blobarchive = new File(this.basedir, "blob_test" + RowDataConverter.BLOB_ARCHIVE_SUFFIX + ".zip");
			zout = new ZipOutputFactory(blobarchive);
			OutputStream binaryOut = zout.createOutputStream(new File("test_r1_c2.data"));
		
			byte[] testData = new byte[1024];
			for (int i = 0; i < testData.length; i++)
			{
				testData[i] = (byte)(i % 255);
			}
			binaryOut.write(testData);
			binaryOut.close();
			zout.done();
			
			StatementRunnerResult result = importCmd.execute(this.connection, "wbimport -encoding='UTF-8' -file='" + archive.getAbsolutePath() + "' -type=xml -table=blob_test");
			assertEquals("Import failed: " + result.getMessageBuffer().toString(), result.isSuccess(), true);
			
			Statement stmt = this.connection.createStatementForQuery();
			ResultSet rs = stmt.executeQuery("select nr, binary_data from blob_test");
			int rowCount = 0;
			
			if (rs.next())
			{
				rowCount ++;
				int nr = rs.getInt(1);
				assertEquals("Wrong data imported", 1, nr);
				
				Object data = rs.getObject(2);
				Object blob = rs.getObject(2);
				assertNotNull("No blob data imported", blob);
				if (blob instanceof byte[])
				{
					byte[] retrievedData = (byte[])blob;
					assertEquals("Wrong blob size imported", 1024, retrievedData.length);
					assertEquals("Wrong content of blob data", retrievedData[0], 0);
					assertEquals("Wrong content of blob data", retrievedData[1], 1);
					assertEquals("Wrong content of blob data", retrievedData[2], 2);
					assertEquals("Wrong content of blob data", retrievedData[3], 3);
				}
				else
				{
					fail("Wrong blob data returned");
				}
			}
			else
			{
				fail("Not enough data imported");
			}
			rs.close();
			stmt.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public void testTextBlobImport()
	{
		int rowCount = 10;
		try
		{
			File importFile  = new File(this.basedir, "blob_test.txt");
			PrintWriter out = new PrintWriter(new FileWriter(importFile));
			out.println("nr\tbinary_data");
			out.println("1\tblob_data_r1_c1.data");
			out.close();
			
			FileOutputStream binaryOut = new FileOutputStream(new File(this.basedir, "blob_data_r1_c1.data"));
			byte[] testData = new byte[1024];
			for (int i = 0; i < testData.length; i++)
			{
				testData[i] = (byte)(i % 255);
			}
			binaryOut.write(testData);
			binaryOut.close();
			
			StatementRunnerResult result = importCmd.execute(this.connection, "wbimport -file='" + importFile.getAbsolutePath() + "' -decimal='.' -type=text -header=true -table=blob_test");
			assertEquals("Import failed: " + result.getMessageBuffer().toString(), result.isSuccess(), true);
			
			Statement stmt = this.connection.createStatementForQuery();
			ResultSet rs = stmt.executeQuery("select nr, binary_data from blob_test");
			if (rs.next())
			{
				int nr = rs.getInt(1);
				assertEquals("Wrong data imported", 1, nr);
				
				Object blob = rs.getObject(2);
				assertNotNull("No blob data imported", blob);
				if (blob instanceof byte[])
				{
					byte[] retrievedData = (byte[])blob;
					assertEquals("Wrong blob size imported", 1024, retrievedData.length);
					assertEquals("Wrong content of blob data", retrievedData[0], 0);
					assertEquals("Wrong content of blob data", retrievedData[1], 1);
					assertEquals("Wrong content of blob data", retrievedData[2], 2);
					assertEquals("Wrong content of blob data", retrievedData[3], 3);
				}
				else
				{
					fail("Wrong blob data returned");
				}
			}
			else
			{
				fail("No rows imported");
			}
			rs.close();
			stmt.close();
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}
	
	private WbConnection prepareDatabase()
		throws SQLException, ClassNotFoundException
	{
		util.emptyBaseDirectory();
		WbConnection wb = util.getConnection();
		
		Statement stmt = wb.createStatement();
		stmt.executeUpdate("CREATE TABLE junit_test (nr integer, firstname varchar(100), lastname varchar(100))");
		stmt.executeUpdate("CREATE TABLE datatype_test (int_col integer, double_col double, char_col varchar(50), date_col date, time_col time, ts_col timestamp)");
		stmt.executeUpdate("CREATE TABLE blob_test (nr integer, binary_data BINARY)");
		stmt.executeUpdate("CREATE TABLE clob_test (nr integer, text_data LONGVARCHAR)");
		wb.commit();
		stmt.close();
		
		return wb;
	}
	
}
