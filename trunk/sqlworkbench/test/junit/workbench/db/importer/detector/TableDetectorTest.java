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
package workbench.db.importer.detector;

import java.io.File;
import java.sql.Types;
import java.util.List;

import workbench.TestUtil;
import workbench.WbTestCase;

import workbench.db.ColumnIdentifier;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Thomas Kellerer
 */
public class TableDetectorTest
  extends WbTestCase
{

  public TableDetectorTest()
  {
    super("TableDetectorTest");
  }

  @Before
  public void setUp()
  {
  }

  @After
  public void tearDown()
  {
  }

  @Test
  public void testAnalyzeFile()
    throws Exception
  {
    File data = new File(getTestUtil().getBaseDir(), "data.txt");
    TestUtil.writeFile(data,
      "id,firstname,lastname,dob,last_login,salary\n" +
      "4,Marvin,42,1000-01-01,2018-09-01 18:19:20,\n" +
      "2,Ford,Prefect,1975-01-01,2014-03-04 17:18:19,1234.567\n" +
      "3,Tricia,McMillan,1983-10-26,2015-09-01 18:19:20,4456.1\n" +
      "1,Arthur,Dent,1980-01-01,2015-07-06 14:15:16,42\n",
      "UTF-8");

    TableDetector detector = new TableDetector(data, ",", "\"", "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", true, 100, "UTF-8");
    detector.analyzeFile();
    List<ColumnIdentifier> columns = detector.getDBColumns();
    assertNotNull(columns);
    assertEquals(6,columns.size());
    for (int i=0; i < columns.size(); i++)
    {
      ColumnIdentifier col = columns.get(i);
      switch (i)
      {
        case 0:
          assertEquals("id", col.getColumnName());
          assertEquals(Types.INTEGER, col.getDataType());
          break;
        case 1:
          assertEquals("firstname", col.getColumnName());
          assertEquals(Types.VARCHAR, col.getDataType());
          assertEquals(6, col.getColumnSize());
          break;
        case 2:
          assertEquals("lastname", col.getColumnName());
          assertEquals(Types.VARCHAR, col.getDataType());
          assertEquals(8, col.getColumnSize());
          break;
        case 3:
          assertEquals("dob", col.getColumnName());
          assertEquals(Types.DATE, col.getDataType());
          break;
        case 4:
          assertEquals("last_login", col.getColumnName());
          assertEquals(Types.TIMESTAMP, col.getDataType());
          break;
        case 5:
          assertEquals("salary", col.getColumnName());
          assertEquals(Types.DECIMAL, col.getDataType());
          assertEquals(3, col.getDecimalDigits());
          break;

      }
    }
  }

}
