/*
 * WbGenDropTest.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2012, Thomas Kellerer
 * No part of this code may be reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 */
package workbench.sql.wbcommands;


import org.junit.*;
import workbench.sql.ScriptParser;
import workbench.util.WbFile;
import static org.junit.Assert.*;

import workbench.TestUtil;
import workbench.WbTestCase;
import workbench.db.ConnectionMgr;
import workbench.db.WbConnection;
import workbench.sql.StatementRunnerResult;
import workbench.util.SqlUtil;

/**
 *
 * @author Thomas Kellerer
 */
public class WbGenDropTest
	extends WbTestCase
{

	public WbGenDropTest()
	{
		super("WbGenDropTest");
	}

	@BeforeClass
	public static void setUpClass()
		throws Exception
	{
	}

	@AfterClass
	public static void tearDownClass()
		throws Exception
	{
	}

	@Before
	public void setUp()
	{
	}

	@After
	public void tearDown()
	{
		ConnectionMgr.getInstance().disconnectAll();
	}

	@Test
	public void testExecute()
		throws Exception
	{
		TestUtil util = getTestUtil();
		WbConnection conn = util.getConnection();

		String sql =
			"create table customer (cust_id integer not null primary key);\n" +
			"create table orders (order_id integer not null primary key, cust_id integer not null);\n" +
			"create table order_item (item_id integer not null primary key, order_id integer not null, currency_id integer not null);\n" +
			"create table currency (currency_id integer not null primary key);\n" +
			"create table delivery (deliv_id integer not null primary key, item_id integer not null);\n" +
			"create table invoice (invoice_id integer not null primary key, order_id integer not null);\n" +
			"alter table orders add constraint fk_orders_cust foreign key (cust_id) references customer (cust_id);\n" +
			"alter table order_item add constraint fk_oi_orders foreign key (order_id) references orders(order_id);\n" +
			"alter table order_item add constraint fk_oi_currency foreign key (currency_id) references currency(currency_id);\n" +
			"alter table delivery add constraint fk_del_oi foreign key (item_id) references order_item (item_id);\n" +
			"alter table invoice add constraint fk_inv_order foreign key (order_id) references orders (order_id);\n" +
			"commit;\n";
		TestUtil.executeScript(conn, sql);

		WbGenDrop cmd = new WbGenDrop();
		cmd.setConnection(conn);
		StatementRunnerResult result = cmd.execute("WbGenerateDrop -tables=customer, orders");
		assertTrue(result.isSuccess());
		String script = result.getMessageBuffer().toString();
		ScriptParser p = new ScriptParser(script);

		assertEquals("ALTER TABLE ORDERS DROP CONSTRAINT FK_ORDERS_CUST", SqlUtil.makeCleanSql(p.getCommand(0), false, false));
		assertEquals("DROP TABLE CUSTOMER", SqlUtil.makeCleanSql(p.getCommand(1), false, false));

		WbFile dir = new WbFile(util.getBaseDir());

		result = cmd.execute("WbGenerateDrop -tables=customer, orders -outputDir='"  + dir.getFullPath() + "'");
		assertTrue(result.isSuccess());

		WbFile cust = new WbFile(dir, "drop_customer.sql");
		assertTrue(cust.exists());

		WbFile ord = new WbFile(dir, "drop_orders.sql");
		assertTrue(ord.exists());

		util.emptyBaseDirectory();

		WbFile scriptFile = new WbFile(util.getBaseDir(), "drop_tables.sql");

		result = cmd.execute("WbGenerateDrop -tables=customer, orders -outputFile='"  + scriptFile.getFullPath() + "'");
		assertTrue(result.isSuccess());

		assertTrue(scriptFile.exists());
		util.emptyBaseDirectory();
	}

}
