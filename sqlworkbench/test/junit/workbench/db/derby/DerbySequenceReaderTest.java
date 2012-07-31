/*
 * DerbySequenceReaderTest.java
 *
 *  This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 *  Copyright 2002-2012, Thomas Kellerer
 *  No part of this code may be reused without the permission of the author
 *
 *  To contact the author please send an email to: support@sql-workbench.net
 */
package workbench.db.derby;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

import workbench.TestUtil;
import workbench.WbTestCase;
import workbench.db.ConnectionMgr;
import workbench.db.SequenceDefinition;
import workbench.db.WbConnection;


/**
 *
 * @author Thomas Kellerer
 */
public class DerbySequenceReaderTest
	extends WbTestCase
{

	public DerbySequenceReaderTest()
	{
		super("DerbySequenceReaderTest");
	}

	@After
	public void tearDown()
	{
		ConnectionMgr.getInstance().disconnectAll();
	}

	@AfterClass
	public static void afterClass()
	{
		DerbyTestUtil.clearProperties();
	}

	@Test
	public void testGetSequences()
		throws Exception
	{
		WbConnection con = DerbyTestUtil.getDerbyConnection(getTestUtil().getBaseDir());
		String sql =
			"create sequence big_seq as bigint;\n" +
			"create sequence seq_one;\n";

		TestUtil.executeScript(con, sql);
		con.commit();

		List<SequenceDefinition> sequences = con.getMetadata().getSequenceReader().getSequences(null, null, "%");
		assertNotNull(sequences);
		assertEquals(2, sequences.size());
		SequenceDefinition seq1 = sequences.get(0);
		assertNotNull(seq1);
		assertEquals("BIG_SEQ", seq1.getSequenceName());

		SequenceDefinition seq2 = sequences.get(1);
		assertNotNull(seq2);
		assertEquals("SEQ_ONE", seq2.getSequenceName());

		CharSequence cs = seq1.getSource(con);
		assertNotNull(cs);
		String expected =
				"CREATE SEQUENCE BIG_SEQ AS BIGINT\n" +
				"        INCREMENT BY 1\n" +
				"        NO MINVALUE\n" +
				"        NO MAXVALUE\n" +
				"        NO CYCLE;";
		assertEquals(expected, cs.toString().trim());
	}

}
