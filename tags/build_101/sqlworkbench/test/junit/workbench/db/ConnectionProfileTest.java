/*
 * ConnectionProfileTest.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2007, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db;

import java.util.Properties;
import junit.framework.TestCase;
import workbench.sql.DelimiterDefinition;

/**
 * @author support@sql-workbench.net
 */
public class ConnectionProfileTest
	extends TestCase
{
	public ConnectionProfileTest(String testName)
	{
		super(testName);
	}

	public void testCreateCopy()
		throws Exception
	{
		ConnectionProfile old = new ConnectionProfile();
		old.setAlternateDelimiter(new DelimiterDefinition("/", true));
		old.setAutocommit(false);
		old.setConfirmUpdates(true);
		old.setDriverName("Postgres");
		old.setEmptyStringIsNull(true);
		old.setUseSeparateConnectionPerTab(true);
		old.setIgnoreDropErrors(true);
		old.setStoreExplorerSchema(true);
		old.setName("First");
		old.setStorePassword(true);
		old.setCopyExtendedPropsToSystem(true);
		old.setIncludeNullInInsert(true);
		old.setIdleTime(42);
		old.setTrimCharData(true);
		old.setIdleScript("select 12 from dual");
		old.setPostConnectScript("drop database");
		old.setPreDisconnectScript("shutdown abort");
		old.setUrl("jdbc:some:database");
		old.setHideWarnings(true);

		ConnectionProfile copy = old.createCopy();
		assertFalse(copy.getAutocommit());
		assertTrue(copy.getConfirmUpdates());
		assertEquals("Postgres", copy.getDriverName());
		assertEquals("First", copy.getName());
		assertTrue(copy.getStorePassword());
		assertTrue(copy.getUseSeparateConnectionPerTab());
		assertTrue(copy.getStoreExplorerSchema());
		assertTrue(copy.getIgnoreDropErrors());
		assertTrue(copy.getTrimCharData());
		assertTrue(copy.getIncludeNullInInsert());
		assertEquals(42, copy.getIdleTime());
		assertEquals("select 12 from dual", old.getIdleScript());
		assertEquals("jdbc:some:database", copy.getUrl());
		assertTrue(copy.isHideWarnings());
		
		assertEquals("drop database", old.getPostConnectScript());
		assertEquals("shutdown abort", old.getPreDisconnectScript());
		
		DelimiterDefinition delim = copy.getAlternateDelimiter();
		assertNotNull(delim);
		assertEquals("/", delim.getDelimiter());
		assertTrue(delim.isSingleLine());
		assertTrue(copy.getCopyExtendedPropsToSystem());

		old.setAlternateDelimiter(null);
		copy = old.createCopy();
		assertNull(copy.getAlternateDelimiter());
	}
	
	public void testProps()
		throws Exception
	{
		ConnectionProfile profile = new ConnectionProfile();
		profile.setAlternateDelimiter(new DelimiterDefinition("/", true));
		profile.setAutocommit(false);
		profile.setConfirmUpdates(true);
		profile.setDriverName("Postgres");
		profile.reset();
		
		Properties props = new Properties();
		props.setProperty("remarksReporting", "true");
		profile.setConnectionProperties(props);
		assertTrue(profile.isChanged());
		profile.setCopyExtendedPropsToSystem(true);
		assertTrue(profile.isChanged());
		
		profile.setAutocommit(true);
		profile.setConfirmUpdates(false);
		assertTrue(profile.isChanged());

		profile.setAutocommit(true);
		profile.setConfirmUpdates(false);
		assertTrue(profile.isChanged());
		
		profile.setUrl("jdbc:postgres:local");
		assertTrue(profile.isChanged());

		profile.setUrl("jdbc:postgres:local");
		assertTrue(profile.isChanged());

		profile.setHideWarnings(false);
		profile.reset();
		profile.setHideWarnings(true);
		assertTrue(profile.isChanged());
		
		profile.reset();
		// Changing to a new URL has to be reflected
		profile.setUrl("jdbc:postgres:local;someProp=myValue");
		assertTrue(profile.isChanged());

		profile.setInputPassword("welcome");
		profile.setStorePassword(true);
		profile.reset();

		// check if changing the password sets the changed flag
		profile.setInputPassword("secret");
		assertTrue(profile.isChanged());

		profile.setStorePassword(false);
		profile.reset();
		profile.setInputPassword("welcome");
		// password are not saved, changing the password should not mark the profile
		// as changed
		assertFalse(profile.isChanged());

		profile.setEmptyStringIsNull(false);
		profile.reset();
		profile.setEmptyStringIsNull(true);
		assertTrue(profile.isChanged());
		profile.setEmptyStringIsNull(true);
		assertTrue(profile.isChanged());

		profile.setUseSeparateConnectionPerTab(false);
		profile.reset();
		profile.setUseSeparateConnectionPerTab(true);
		assertTrue(profile.isChanged());
		profile.setUseSeparateConnectionPerTab(true);
		assertTrue(profile.isChanged());
		
		profile.setStoreExplorerSchema(false);
		profile.reset();
		profile.setStoreExplorerSchema(true);
		assertTrue(profile.isChanged());
		profile.setStoreExplorerSchema(true);
		assertTrue(profile.isChanged());

		profile.reset();
		profile.setDriverName("Postgres 8.3");
		assertTrue(profile.isChanged());
		
	}
}
