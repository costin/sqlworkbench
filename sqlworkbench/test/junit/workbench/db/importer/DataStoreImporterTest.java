/*
 * DataStoreImporterTest.java
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
package workbench.db.importer;

import java.io.File;
import java.io.FileWriter;
import java.sql.Types;
import org.junit.Test;
import workbench.TestUtil;
import workbench.WbTestCase;
import workbench.storage.DataStore;
import static org.junit.Assert.*;

/**
 *
 * @author Thomas Kellerer
 */
public class DataStoreImporterTest
	extends WbTestCase
{
	private TestUtil util;

	public DataStoreImporterTest()
		throws Exception
	{
		super();
		util = getTestUtil();
	}

	private DataStore prepareDataStore()
	{
		String[] cols = new String[] { "ID", "FIRSTNAME", "LASTNAME" };
		int[] types = new int[] { Types.INTEGER, Types.VARCHAR, Types.VARCHAR };
		int[] sizes = new int[] { 5, 25, 25 };

		DataStore ds = new DataStore(cols, types, sizes);
		return ds;
	}

	@Test
	public void testNoColumnNames()
	{
		String content = "1\tHarry\tHandsome\n2\tMary\tMoviestart\n3\tArthur\tDent";
		DataStore ds = prepareDataStore();
		DataStoreImporter importer = new DataStoreImporter(ds, null, null);

		TextImportOptions to = new DefaultTextImportOptions("\t", "\"");
		to.setContainsHeader(false);
		ImportOptions o = new DefaultImportOptions();

		importer.importString(content, o, to);
		importer.startImport();
		assertEquals("Wrong number of rows imported", 3, ds.getRowCount());

		String name = ds.getValueAsString(0, 1);
		assertEquals("Wrong firstname", "Harry", name);
	}

	@Test
	public void testImportFile()
		throws Exception
	{
		String content = "id\tfirstname\tlastname\n1\tHarry\tHandsome\n2\tMary\tMoviestart\n3\tArthur\tDent";
		File f = new File(util.getBaseDir(), "ds_import.txt");

		FileWriter w = new FileWriter(f);
		w.write(content);
		w.close();

		DataStore ds = prepareDataStore();
		DataStoreImporter importer = new DataStoreImporter(ds, null, null);

		TextImportOptions to = new DefaultTextImportOptions("\t", "\"");
		ImportOptions o = new DefaultImportOptions();

		importer.setImportOptions(f,ProducerFactory.ImportType.Text, o, to, null);
		importer.startImport();
		assertEquals("Wrong number of rows imported", 3, ds.getRowCount());

		String name = ds.getValueAsString(0, 1);
		assertEquals("Wrong firstname", "Harry", name);
	}

	@Test
	public void testImportString()
		throws Exception
	{
		String content = "id\tfirstname\tlastname\n1\tHarry\tHandsome\n2\tMary\tMoviestart\n3\tArthur\tDent";
		DataStore ds = prepareDataStore();
		DataStoreImporter importer = new DataStoreImporter(ds, null, null);
		importer.importString(content);
		importer.startImport();
		assertEquals("Wrong number of rows imported", 3, ds.getRowCount());

		String name = ds.getValueAsString(0, 1);
		assertEquals("Wrong firstname", "Harry", name);
	}

	@Test
	public void testImportWrongColOrder()
		throws Exception
	{
		String content = "id\tlastname\tfirstname\n1\tHandsome\tHarry\n2\tMoviestar\tMary\n3\tDent\tArthur";
		DataStore ds = prepareDataStore();
		DataStoreImporter importer = new DataStoreImporter(ds, null, null);
		importer.importString(content);
		importer.startImport();
		assertEquals("Wrong number of rows imported", 3, ds.getRowCount());

		String name = ds.getValueAsString(0, 1);
		assertEquals("Wrong firstname", "Harry", name);
	}


}
