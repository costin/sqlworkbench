/*
 * This file is part of SQL Workbench/J, https://www.sql-workbench.eu
 *
 * Copyright 2002-2019, Thomas Kellerer.
 *
 * Licensed under a modified Apache License, Version 2.0
 * that restricts the use for certain governments.
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.sql-workbench.eu/manual/license.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * To contact the author please send an email to: support@sql-workbench.eu
 */
package workbench.storage;

import java.util.ArrayList;
import java.util.List;

import workbench.log.LogMgr;

import workbench.db.ColumnIdentifier;
import workbench.db.TableIdentifier;
import workbench.db.WbConnection;

import workbench.util.Alias;
import workbench.util.SelectColumn;
import workbench.util.SqlUtil;

/**
 * A class to detect to which table a column from a result set belongs.
 *
 * @author Thomas Kellerer
 */
public class SourceTableDetector
{

  public void checkColumnTables(String sql, ResultInfo result, WbConnection connection)
  {
    resetResult(result);

    List<Alias> tables = SqlUtil.getTables(sql, true, connection);

    List<String> colNames = SqlUtil.getSelectColumns(sql, true, connection);
    List<SelectColumn> columns = new ArrayList<>(colNames.size());
    for (String name : colNames)
    {
      columns.add(new SelectColumn(name));
    }

    if (tables.size() == 1)
    {
      for (ColumnIdentifier col : result.getColumns())
      {
        col.setSourceTableName(tables.get(0).getObjectName());
      }
      result.setColumnTableDetected(true);
      return;
    }

    if (columns.size() != result.getColumnCount())
    {
      LogMgr.logWarning("SourceTableDetector.checkColumnTables()", "The SQL statement contains a different number of columns than the ResultInfo");
      return;
    }

    int matchedCols = 0;
    for (int i=0; i < columns.size(); i++)
    {
      SelectColumn col = columns.get(i);
      String colTable = col.getColumnTable();
      String table = findTableFromAlias(colTable, tables);
      if (table != null)
      {
        LogMgr.logDebug("SourceTableDetector.checkColumnTables()", "Column " + col.toString() + " seems to belong to table " + table);
        result.getColumn(i).setSourceTableName(table);
        matchedCols ++;
      }
    }
    result.setColumnTableDetected(matchedCols == columns.size());
  }

  private String findTableFromAlias(String alias, List<Alias> tables)
  {
    for (Alias tbl : tables)
    {
      if (tbl.getNameToUse().equalsIgnoreCase(alias)) return tbl.getObjectName();

      // take fully qualified names without an alias into account
      // e.g. select foo.id from public.foo join ...
      if (tbl.getAlias() == null)
      {
        TableIdentifier fqn = new TableIdentifier(tbl.getObjectName());
        TableIdentifier other = new TableIdentifier(alias);
        if (fqn.compareNames(other))
        {
          return tbl.getObjectName();
        }
      }
    }
    return null;
  }

  private void resetResult(ResultInfo result)
  {
    for (ColumnIdentifier col : result.getColumns())
    {
      col.setSourceTableName(null);
    }
    result.setColumnTableDetected(false);
  }

}
