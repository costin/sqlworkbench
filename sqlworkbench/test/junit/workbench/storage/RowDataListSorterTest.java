/*
 * RowDataListSorterTest.java
 *
 * This file is part of SQL Workbench/J, https://www.sql-workbench.eu
 *
 * Copyright 2002-2019, Thomas Kellerer
 *
 * Licensed under a modified Apache License, Version 2.0
 * that restricts the use for certain governments.
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at.
 *
 *     https://www.sql-workbench.eu/manual/license.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * To contact the author please send an email to: support@sql-workbench.eu
 *
 */
package workbench.storage;

import workbench.WbTestCase;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Thomas Kellerer
 */
public class RowDataListSorterTest
	extends WbTestCase
{

	public RowDataListSorterTest()
	{
		super("RowDataListSorterTest");
	}

	@Test
	public void testSort()
		throws Exception
	{
		RowDataList data = new RowDataList(20);
		RowData row = null;

		row = new RowData(2);
		row.setValue(0, new Integer(2));
		row.setValue(1, new Integer(2));
		data.add(row);

		row = new RowData(2);
		row.setValue(0, new Integer(2));
		row.setValue(1, new Integer(3));
		data.add(row);

		row = new RowData(2);
		row.setValue(0, new Integer(2));
		row.setValue(1, new Integer(1));
		data.add(row);

		row = new RowData(2);
		row.setValue(0, new Integer(1));
		row.setValue(1, new Integer(3));
		data.add(row);

		row = new RowData(2);
		row.setValue(0, new Integer(1));
		row.setValue(1, new Integer(1));
		data.add(row);

		row = new RowData(2);
		row.setValue(0, new Integer(1));
		row.setValue(1, new Integer(2));
		data.add(row);

		assertEquals(data.size(), 6);

		Integer i1 = (Integer) data.get(0).getValue(0);
		assertEquals(i1.intValue(), 2);

		RowDataListSorter sorter = new RowDataListSorter(0, true);
		sorter.sort(data);

		i1 = (Integer) data.get(0).getValue(0);
		assertEquals(i1.intValue(), 1);

		i1 = (Integer) data.get(3).getValue(0);
		assertEquals(i1.intValue(), 2);

		sorter = new RowDataListSorter(0, false);
		sorter.sort(data);

		i1 = (Integer) data.get(0).getValue(0);
		assertEquals(i1.intValue(), 2);

		i1 = (Integer) data.get(3).getValue(0);
		assertEquals(i1.intValue(), 1);

		sorter = new RowDataListSorter(new int[]{0, 1}, new boolean[]{true, true});
		sorter.sort(data);

		i1 = (Integer) data.get(0).getValue(0);
		assertEquals(i1.intValue(), 1);

		i1 = (Integer) data.get(0).getValue(1);
		assertEquals(i1.intValue(), 1);

		i1 = (Integer) data.get(1).getValue(0);
		assertEquals(i1.intValue(), 1);

		i1 = (Integer) data.get(1).getValue(1);
		assertEquals(i1.intValue(), 2);

		sorter = new RowDataListSorter(new int[]{0, 1}, new boolean[]{true, false});
		sorter.sort(data);

		i1 = (Integer) data.get(0).getValue(0);
		assertEquals(i1.intValue(), 1);

		i1 = (Integer) data.get(0).getValue(1);
		assertEquals(i1.intValue(), 3);

		i1 = (Integer) data.get(1).getValue(0);
		assertEquals(i1.intValue(), 1);

		i1 = (Integer) data.get(1).getValue(1);
		assertEquals(i1.intValue(), 2);
	}

	@Test
	public void testSortLastColumn()
		throws Exception
	{
		int count = 10;
		RowDataList data = new RowDataList(count);
		for (int i=0; i < count; i++)
		{
			RowData row = new RowData(3);
			row.setValue(0, "foo");
			row.setValue(1, "bar");
			row.setValue(2, Integer.valueOf(count - i));
			data.add(row);
		}
		RowDataListSorter sorter = new RowDataListSorter(2, true);
		sorter.sort(data);
		for (int i=0; i < count; i++)
		{
			int value = (Integer) data.get(i).getValue(2);
			assertEquals(i + 1, value);
		}
	}
}
