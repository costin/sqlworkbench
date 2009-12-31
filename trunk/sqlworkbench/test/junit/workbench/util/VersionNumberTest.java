/*
 * VersionNumberTest.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2010, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.util;

import junit.framework.TestCase;

/**
 *
 * @author Thomas Kellerer
 */
public class VersionNumberTest extends TestCase
{
	
	public VersionNumberTest(String testName)
	{
		super(testName);
	}
	
	public void testVersion()
	{
		VersionNumber one = new VersionNumber("94");
		assertEquals(one.getMajorVersion(), 94);
		assertEquals(one.getMinorVersion(), -1);
		
		VersionNumber two = new VersionNumber("94.2");
		assertEquals(two.getMajorVersion(), 94);
		assertEquals(two.getMinorVersion(), 2);
		
		assertTrue(two.isNewerThan(one));
		assertFalse(one.isNewerThan(two));
		
		VersionNumber na = new VersionNumber(null);
		assertFalse(na.isNewerThan(two));
		assertTrue(two.isNewerThan(na));
		
		VersionNumber dev = new VersionNumber("@BUILD_NUMBER@");
		assertFalse(one.isNewerThan(dev));
		assertTrue(dev.isNewerThan(one));
		
		assertTrue(dev.isNewerThan(two));
		assertFalse(two.isNewerThan(dev));
		
		VersionNumber current = new VersionNumber("96.8");
		VersionNumber stable = new VersionNumber("97");
		assertTrue(stable.isNewerThan(current));
		assertFalse(current.isNewerThan(stable));
		
		VersionNumber v2 = new VersionNumber("96.9");
		assertTrue(v2.isNewerOrEqual(current));
		VersionNumber v3 = new VersionNumber("96.8");
		assertTrue(v3.isNewerOrEqual(current));
	}
	
}
