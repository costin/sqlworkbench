/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package workbench.db.hsqldb;

import java.util.List;
import workbench.TestUtil;
import workbench.db.WbConnection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import workbench.WbTestCase;
import workbench.db.ColumnIdentifier;
import workbench.db.ConnectionMgr;
import workbench.db.TableDefinition;
import workbench.db.TableIdentifier;

/**
 *
 * @author Thomas Kellerer
 */
public class HsqlColumnEnhancerTest
	extends WbTestCase
{

	public HsqlColumnEnhancerTest()
	{
		super("HsqlColumnEnhancerTest");
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
		ConnectionMgr.getInstance().disconnectAll();
	}

	@Test
	public void testUpdateColumnDefinition()
		throws Exception
	{
		TestUtil util = getTestUtil();
		WbConnection con = util.getHSQLConnection("column_enhancer_test");
		TestUtil.executeScript(con,
			"CREATE TABLE gen_col_test (\n" +
			"  id  integer,  \n" +
			"  id2 integer generated always as (id*3), \n" +
			"  id3 integer generated always as identity (start with 24 increment by 42) \n" +
			");\n");
		TableDefinition tbl = con.getMetadata().getTableDefinition(new TableIdentifier(null, "PUBLIC", "GEN_COL_TEST"));
		assertNotNull(tbl);
		List<ColumnIdentifier> cols = tbl.getColumns();
		assertEquals(3, cols.size());
		for (ColumnIdentifier col : cols)
		{
			String name = col.getColumnName();
			if ("id2".equalsIgnoreCase(name))
			{
				assertEquals("GENERATED ALWAYS AS (ID*3)", col.getComputedColumnExpression());
			}
			if ("id3".equalsIgnoreCase(name))
			{
				assertEquals("GENERATED ALWAYS AS IDENTITY (START WITH 24 INCREMENT BY 42)", col.getComputedColumnExpression());
			}
		}
	}
}