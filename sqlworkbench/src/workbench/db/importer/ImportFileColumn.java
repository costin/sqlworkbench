/*
 * ImportFileColumn.java
 *
 * This file is part of SQL Workbench/J, https://www.sql-workbench.eu
 *
 * Copyright 2002-2019, Thomas Kellerer
 *
 * Licensed under a modified Apache License, Version 2.0
 * that restricts the use for certain governments.
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at.
 *
 *     https://www.sql-workbench.eu/manual/license.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * To contact the author please send an email to: support@sql-workbench.eu
 *
 */
package workbench.db.importer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import workbench.db.ColumnIdentifier;

import workbench.util.SqlUtil;

/**
 *
 * @author Thomas Kellerer
 */
public class ImportFileColumn
{
  private ColumnIdentifier tableColumn;

  private int targetIndex = -1;
  private int dataWidth = -1;
  private Pattern columnFilter;

  public static final ImportFileColumn SKIP_COLUMN = new ImportFileColumn(new ColumnIdentifier(RowDataProducer.SKIP_INDICATOR));

  public ImportFileColumn(ColumnIdentifier col)
  {
    if (col == null) throw new NullPointerException("Column may not be null");
    this.tableColumn = col;
  }

  public ColumnIdentifier getColumn()
  {
    return tableColumn;
  }

  public Pattern getColumnFilter()
  {
    return columnFilter;
  }

  public void setColumnFilter(Pattern pattern)
  {
    this.columnFilter = pattern;
  }

  public int getTargetIndex()
  {
    return targetIndex;
  }

  public void setTargetIndex(int index)
  {
    this.targetIndex = index;
  }

  public int getDataWidth()
  {
    return dataWidth;
  }

  public void setDataWidth(int width)
  {
    this.dataWidth = width;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj instanceof ImportFileColumn)
    {
      final ImportFileColumn other = (ImportFileColumn) obj;
      return this.tableColumn.equals(other.tableColumn);
    }
    else if (obj instanceof ColumnIdentifier)
    {
      return this.tableColumn.equals(obj);
    }
    else if (obj instanceof String)
    {
      String thisName = SqlUtil.removeObjectQuotes(tableColumn.getColumnName());
      return thisName.equalsIgnoreCase(SqlUtil.removeObjectQuotes((String)obj));
    }
    return false;
  }

  @Override
  public int hashCode()
  {
    return tableColumn.hashCode();
  }

  /**
   * Creates a List that implements a different indexOf() method.
   * The standard ArrayList implementation calls obj.equals(elementData[i]) to
   * test if the passed object is in the list. If we want to compare different
   * objects (String, ColumnIdentifier, ImportFileColumn) to the elements in
   * the list this does not work (even though ImportFileColumn.equals() handles
   * all three cases).
   * <br>
   * So the list generated by createList() calls elementData[i].equals(parameter)
   * to test if the passed parameter is contained in the list.
   * <br/>
   * This makes it easier to search for a column name by String or ColumnIdentifier
   * in the list.
   */
  public static List<ImportFileColumn> createList()
  {
    return new ArrayList<ImportFileColumn>()
    {
      @Override
      public int indexOf(Object elem)
      {
        if (elem == null) return -1;
        int size = size();
        for (int i = 0; i < size; i++)
        {
          if (get(i).equals(elem))
          {
            return i;
          }
        }
        return -1;
      }
    };
  }

  @Override
  public String toString()
  {
    return tableColumn.getColumnName() + "@" + targetIndex;
  }
}
