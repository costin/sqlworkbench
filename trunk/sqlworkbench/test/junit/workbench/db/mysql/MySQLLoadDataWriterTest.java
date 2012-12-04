/*
 * MySQLLoadDataWriterTest.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2012, Thomas Kellerer
 * No part of this code may be reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 */
package workbench.db.mysql;

import java.sql.Types;
import java.util.List;

import workbench.TestUtil;
import workbench.WbTestCase;

import workbench.db.ColumnIdentifier;
import workbench.db.TableIdentifier;
import workbench.db.exporter.DataExporter;
import workbench.db.exporter.RowDataConverter;

import workbench.storage.ResultInfo;
import workbench.storage.RowData;

import workbench.util.StrBuffer;
import workbench.util.StringUtil;
import workbench.util.WbFile;

import org.junit.Test;

import static org.junit.Assert.*;
/**
 *
 * @author Thomas Kellerer
 */
public class MySQLLoadDataWriterTest
	extends WbTestCase
{
	public MySQLLoadDataWriterTest()
	{
		super("MySQLLoadDataWriterTest");
	}

	@Test
	public void testWriteFormatFile()
		throws Exception
	{
		TestUtil util = getTestUtil();
		final WbFile export = new WbFile(util.getBaseDir(), "export.txt");
		DataExporter exporter = new DataExporter(null)
		{

			@Override
			public boolean getQuoteAlways()
			{
				return false;
			}

			@Override
			public String getFullOutputFilename()
			{
				return export.getFullPath();
			}

			@Override
			public boolean getExportHeaders()
			{
				return true;
			}

			@Override
			public String getTextDelimiter()
			{
				return "\t";
			}

			@Override
			public String getTextQuoteChar()
			{
				return "\"";
			}
			@Override
			public String getEncoding()
			{
				return "UTF-8";
			}

			@Override
			public String getTableNameToUse()
			{
				return "person";
			}
		};

		ColumnIdentifier id = new ColumnIdentifier("id" ,Types.INTEGER, true);
		ColumnIdentifier firstname = new ColumnIdentifier("firstname", Types.VARCHAR);
		ColumnIdentifier lastname = new ColumnIdentifier("lastname", Types.VARCHAR);
		final TableIdentifier table = new TableIdentifier("person");

		final ResultInfo info = new ResultInfo(new ColumnIdentifier[] { id, firstname, lastname } );
		info.setUpdateTable(table);

		RowDataConverter converter = new RowDataConverter()
		{

			@Override
			public ResultInfo getResultInfo()
			{
				return info;
			}

			@Override
			public StrBuffer convertRowData(RowData row, long rowIndex)
			{
				return new StrBuffer();
			}

			@Override
			public StrBuffer getStart()
			{
				return null;
			}

			@Override
			public StrBuffer getEnd(long totalRows)
			{
				return null;
			}
		};

		try
		{
			MySQLLoadDataWriter writer = new MySQLLoadDataWriter();
			writer.writeFormatFile(exporter, converter);
			WbFile formatFile = new WbFile(util.getBaseDir(), "load_person.sql");
			assertTrue(formatFile.exists());

			List<String> lines = StringUtil.readLines(formatFile);
			assertNotNull(lines);
			assertEquals(7, lines.size());
			assertTrue(lines.get(0).endsWith("export.txt'"));
			assertEquals("  into table person", lines.get(1));
			assertEquals("  character set UTF-8", lines.get(2));
			assertEquals("    terminated by '\\t'", lines.get(4));
			assertEquals("  ignore 1 lines", lines.get(6));
		}
		finally
		{
			util.emptyBaseDirectory();
		}
	}
}
