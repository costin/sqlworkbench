/*
 * ObjectNameFilterTest
 * 
 *  This file is part of SQL Workbench/J, http://www.sql-workbench.net
 * 
 *  Copyright 2002-2009, Thomas Kellerer
 *  No part of this code maybe reused without the permission of the author
 * 
 *  To contact the author please send an email to: support@sql-workbench.net
 */
package workbench.db;

import java.util.Set;
import junit.framework.TestCase;
import workbench.util.CollectionUtil;

/**
 *
 * @author Thomas Kellerer
 */
public class ObjectNameFilterTest
	extends TestCase
{

	public ObjectNameFilterTest(String testName)
	{
		super(testName);
	}

	public void testIsExcluded()
	{
		ObjectNameFilter filter = new ObjectNameFilter();
		Set<String> names = CollectionUtil.hashSet("ONE", "^DEV[0-9]+");
		filter.setFilterExpressions(names);
		assertTrue(filter.isExcluded("one"));
		assertTrue(filter.isExcluded("dev1"));
		assertFalse(filter.isExcluded("public"));
		assertFalse(filter.isModified());
		filter.addExpression("something");
		assertTrue(filter.isModified());
		assertTrue(filter.isExcluded("something"));
		filter.resetModified();
		assertFalse(filter.isModified());
	}

	public void testCopy()
	{
		ObjectNameFilter filter = new ObjectNameFilter();
		Set<String> names = CollectionUtil.hashSet("ONE", "two");
		filter.setFilterExpressions(names);
		ObjectNameFilter copy = filter.createCopy();
		assertTrue(filter.equals(copy));
		filter.addExpression("three");
		assertEquals(2, copy.getSize());
		assertTrue(copy.isExcluded("one"));
		assertFalse(copy.isExcluded("three"));
	}

}
