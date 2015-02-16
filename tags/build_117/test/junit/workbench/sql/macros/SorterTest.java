/*
 * SorterTest.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2015, Thomas Kellerer
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
package workbench.sql.macros;

import java.util.Set;
import java.util.TreeSet;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Thomas Kellerer
 */
public class SorterTest
{
	@Test
	public void testCompare()
	{
		Set<SortOrderElement> list = new TreeSet<SortOrderElement>(new Sorter());
		list.add(new SortOrderElement(4));
		list.add(new SortOrderElement(5));
		list.add(new SortOrderElement(1));
		list.add(new SortOrderElement(3));
		list.add(new SortOrderElement(2));

		int index = 1;
		for (SortOrderElement e : list)
		{
			assertEquals(index, e.getSortOrder());
			index ++;
		}
	}

	static class SortOrderElement
		implements Sortable
	{
		private int sortOrder;

		public SortOrderElement(int i)
		{
			sortOrder = i;
		}

		public void setSortOrder(int index)
		{
			sortOrder = index;
		}
		public int getSortOrder()
		{
			return sortOrder;
		}


	}
}