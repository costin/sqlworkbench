/*
 * H2MergeGeneratorTest.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2014, Thomas Kellerer
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
package workbench.db.h2database;

import java.sql.Types;
import org.junit.Test;
import static org.junit.Assert.*;
import workbench.WbTestCase;
import workbench.db.ColumnIdentifier;
import workbench.db.TableIdentifier;
import workbench.storage.DataStore;
import workbench.storage.ResultInfo;
import workbench.storage.RowDataContainer;

/**
 *
 * @author Thomas Kellerer
 */
public class H2MergeGeneratorTest
	extends WbTestCase
{
	public H2MergeGeneratorTest()
	{
		super("H2MergeGeneratorTest");
	}

	@Test
	public void testGenerateMerge()
	{
		ColumnIdentifier id = new ColumnIdentifier("id", Types.INTEGER);
		id.setIsPkColumn(true);
		ColumnIdentifier fname = new ColumnIdentifier("fname", Types.VARCHAR);
		ColumnIdentifier lname = new ColumnIdentifier("lname", Types.VARCHAR);
		ResultInfo info = new ResultInfo(new ColumnIdentifier[] { id, fname, lname });

		TableIdentifier tbl = new TableIdentifier("person");
		info.setUpdateTable(tbl);
		DataStore ds = new DataStore(info);
		ds.forceUpdateTable(tbl);
		int row = ds.addRow();
		ds.setValue(row, 0, Integer.valueOf(42));
		ds.setValue(row, 1, "Arthur");
		ds.setValue(row, 2, "Dent");

		row = ds.addRow();
		ds.setValue(row, 0, Integer.valueOf(24));
		ds.setValue(row, 1, "Ford");
		ds.setValue(row, 2, "Prefect");

		H2MergeGenerator generator = new H2MergeGenerator();
		String sql = generator.generateMerge(ds);
		assertNotNull(sql);
		String expected =
			"MERGE INTO person (id, fname, lname)\n" +
			"  KEY (id)\n" +
			"VALUES\n" +
			"  (42, 'Arthur', 'Dent'),\n" +
			"  (24, 'Ford', 'Prefect');";
//		System.out.println("----- expected: \n" + expected + "\n****** result: \n" + sql + "\n-------");
		assertEquals(expected, sql.trim());

		RowDataContainer selected = RowDataContainer.Factory.createContainer(ds, new int[] {0});
		sql = generator.generateMerge(selected);
		assertNotNull(sql);

		expected =
			"MERGE INTO person (id, fname, lname)\n" +
			"  KEY (id)\n" +
			"VALUES\n" +
			"  (42, 'Arthur', 'Dent');";
//		System.out.println("----- expected: \n" + expected + "\n****** result: \n" + sql + "\n-------");
		assertEquals(expected, sql.trim());
	}

	@Test
	public void testIncremental()
	{
		ColumnIdentifier id = new ColumnIdentifier("id", Types.INTEGER);
		id.setIsPkColumn(true);
		ColumnIdentifier fname = new ColumnIdentifier("fname", Types.VARCHAR);
		ColumnIdentifier lname = new ColumnIdentifier("lname", Types.VARCHAR);
		ResultInfo info = new ResultInfo(new ColumnIdentifier[] { id, fname, lname });

		TableIdentifier tbl = new TableIdentifier("person");
		info.setUpdateTable(tbl);
		DataStore ds = new DataStore(info);
		ds.forceUpdateTable(tbl);
		int row = ds.addRow();
		ds.setValue(row, 0, Integer.valueOf(42));
		ds.setValue(row, 1, "Arthur");
		ds.setValue(row, 2, "Dent");

		row = ds.addRow();
		ds.setValue(row, 0, Integer.valueOf(24));
		ds.setValue(row, 1, "Ford");
		ds.setValue(row, 2, "Prefect");

		H2MergeGenerator generator = new H2MergeGenerator();
		StringBuilder result = new StringBuilder(100);
		String part = generator.generateMergeStart(ds);
		result.append(part);
		part = generator.addRow(info, ds.getRow(0), 0);
		result.append(part);

		part = generator.addRow(info, ds.getRow(1), 1);
		result.append(part);

		result.append(generator.generateMergeEnd(ds));

		String expected =
			"MERGE INTO person (id, fname, lname)\n" +
			"  KEY (id)\n" +
			"VALUES\n" +
			"  (42, 'Arthur', 'Dent'),\n" +
			"  (24, 'Ford', 'Prefect');";
//		System.out.println("----- expected: \n" + expected + "\n****** result: \n" + result.toString() + "\n-------");
		assertEquals(expected, result.toString().trim());
	}
}
