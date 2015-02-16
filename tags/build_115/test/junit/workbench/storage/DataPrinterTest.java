/*
 * DataPrinterTest.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2013, Thomas Kellerer
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
package workbench.storage;

import java.io.StringWriter;
import java.sql.Types;
import java.io.Writer;
import org.junit.Test;
import workbench.WbTestCase;
import static org.junit.Assert.*;

/**
 *
 * @author Thomas Kellerer
 */
public class DataPrinterTest
	extends WbTestCase
{

	public DataPrinterTest()
	{
		super("DataPrinterTest");
	}

	@Test
	public void testWriteDataString()
		throws Exception
	{
		int[] types = new int[] {Types.INTEGER, Types.VARCHAR, Types.VARCHAR };
		String[] names = new String[] {"ID", "FIRSTNAME", "LASTNAME" };
		DataStore ds = new DataStore(names, types);
		int row = ds.addRow();
		ds.setValue(row, 0, Integer.valueOf(1));
		ds.setValue(row, 1, "Arthur");
		ds.setValue(row, 2, "Dent");

		row = ds.addRow();
		ds.setValue(row, 0, Integer.valueOf(2));
		ds.setValue(row, 1, "Zaphod");
		ds.setValue(row, 2, "Beeblebrox");
		
		Writer out = new StringWriter(50);

		DataPrinter printer = new DataPrinter(ds, ";", "\n", null, true);
		printer.writeDataString(out, null);
		assertEquals("ID;FIRSTNAME;LASTNAME\n1;Arthur;Dent\n2;Zaphod;Beeblebrox\n", out.toString());

		out = new StringWriter(50);
		printer.writeDataString(out, new int[] {1} );
		assertEquals("ID;FIRSTNAME;LASTNAME\n2;Zaphod;Beeblebrox\n", out.toString());

		int[] colMap = new int[] { 2, 1, 0 };
		printer.setColumnMapping(colMap);

		out = new StringWriter(50);
		printer.writeDataString(out, new int[] {0} );
		assertEquals("LASTNAME;FIRSTNAME;ID\nDent;Arthur;1\n", out.toString());
	}
}