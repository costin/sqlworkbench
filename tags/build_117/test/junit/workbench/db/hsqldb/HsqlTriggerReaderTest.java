/*
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2015 Thomas Kellerer.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * To contact the author please send an email to: support@sql-workbench.net
 */
package workbench.db.hsqldb;



import java.util.List;

import workbench.TestUtil;
import workbench.WbTestCase;

import workbench.db.ConnectionMgr;
import workbench.db.TriggerDefinition;
import workbench.db.TriggerReader;
import workbench.db.TriggerReaderFactory;
import workbench.db.WbConnection;

import workbench.storage.DataStore;

import workbench.sql.DelimiterDefinition;

import org.junit.AfterClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Thomas Kellerer
 */
public class HsqlTriggerReaderTest
	extends WbTestCase
{

	public HsqlTriggerReaderTest()
	{
		super("HsqlTriggerReaderTest");
	}

	@AfterClass
	public static void tearDownClass()
		throws Exception
	{
		ConnectionMgr.getInstance().disconnectAll();
	}

	@Test
	public void testReader()
		throws Exception
	{
		TestUtil util = getTestUtil();
		WbConnection con = util.getHSQLConnection("trigger_test");
		TestUtil.executeScript(con,
			"create table person (id integer, name varchar(100), last_modified timestamp);");

		String trigger =
			"CREATE TRIGGER PERSON_TRG\n" +
			"BEFORE UPDATE ON PERSON\n" +
			"\n" +
			"REFERENCING NEW ROW AS NEWROW\n" +
			"FOR EACH ROW\n" +
			"SET NEWROW.LAST_MODIFIED=CURRENT_TIMESTAMP\n" +
			"/";

		TestUtil.executeScript(con, trigger, DelimiterDefinition.DEFAULT_ORA_DELIMITER);

		con.commit();

		TriggerReader reader = TriggerReaderFactory.createReader(con);
		assertNotNull(reader);
		DataStore triggers = reader.getTriggers(null, null);
		assertNotNull(triggers);
		assertEquals(1, triggers.getRowCount());
		assertEquals("PERSON_TRG", triggers.getValueAsString(0, TriggerReader.COLUMN_IDX_TABLE_TRIGGERLIST_TRG_NAME));

		List<TriggerDefinition> triggerList = reader.getTriggerList(null, null, "PERSON");
		assertEquals(1, triggerList.size());
		String source = reader.getTriggerSource(triggerList.get(0), false);
		assertNotNull(source);
		String expected =
			"CREATE TRIGGER PERSON_TRG\n" +
			"BEFORE UPDATE ON PERSON\n" +
			"\n" +
			"REFERENCING NEW ROW AS NEWROW\n" +
			"FOR EACH ROW\n" +
			"SET NEWROW.LAST_MODIFIED=CURRENT_TIMESTAMP";
		assertEquals(expected, source.trim());

	}

}
