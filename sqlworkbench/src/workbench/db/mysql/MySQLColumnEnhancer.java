/*
 * MySQLColumnEnhancer.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2015, Thomas Kellerer
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
package workbench.db.mysql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import workbench.log.LogMgr;
import workbench.resource.Settings;

import workbench.db.ColumnDefinitionEnhancer;
import workbench.db.ColumnIdentifier;
import workbench.db.JdbcUtils;
import workbench.db.TableDefinition;
import workbench.db.WbConnection;

import workbench.util.SqlUtil;

/**
 * A class to retrieve enum and collation definitions for the columns of a MySQL table.
 *
 * @author  Thomas Kellerer
 * @see workbench.db.DbMetadata#getTableDefinition(workbench.db.TableIdentifier)
 * @see MySQLEnumReader
 * @see MySQLColumnCollationReader
 */
public class MySQLColumnEnhancer
	implements ColumnDefinitionEnhancer
{

	@Override
	public void updateColumnDefinition(TableDefinition tbl, WbConnection connection)
	{
		MySQLColumnCollationReader collationReader = new MySQLColumnCollationReader();
		collationReader.readCollations(tbl, connection);

		MySQLEnumReader enumReader = new MySQLEnumReader();
		enumReader.readEnums(tbl, connection);

    if (JdbcUtils.hasMinimumServerVersion(connection, "5.7"))
    {
      updateComputedColumns(tbl, connection);
    }
	}

  private void updateComputedColumns(TableDefinition tbl, WbConnection connection)
  {
		PreparedStatement stmt = null;
		ResultSet rs = null;

    String sql =
      "select column_name, extra, generation_expression \n" +
      "from information_schema.columns \n" +
      "where table_schema = ? \n" +
      "and table_name = ? \n " +
      "and extra is not null and extra <> '' \n" +
      "and generation_expression is not null and generation_expression <> ''";

		if (Settings.getInstance().getDebugMetadataSql())
		{
      LogMgr.logDebug("MySQLColumnEnhancer.updateComputedColumns()", "Retrieving computed column definitions using:\n" + SqlUtil.replaceParameters(sql, tbl.getTable().getRawCatalog(), tbl.getTable().getRawTableName()));
		}

    try
    {
      stmt = connection.getSqlConnection().prepareStatement(sql);
      stmt.setString(1, tbl.getTable().getRawCatalog());
      stmt.setString(2, tbl.getTable().getRawTableName());
      rs = stmt.executeQuery();
      List<ColumnIdentifier> columns = tbl.getColumns();
      while (rs.next())
      {
        String colname = rs.getString(1);
        String genType = rs.getString(2);
        String expression = rs.getString(3);
        ColumnIdentifier col = ColumnIdentifier.findColumnInList(columns, colname);
        if (col != null)
        {
          String genSql = " GENERATED ALWAYS AS (" + expression + ") " + genType.replace("GENERATED", "").trim();
          col.setComputedColumnExpression(genSql);
        }
      }
    }
    catch (Exception ex)
    {

    }
    finally
    {
      SqlUtil.closeAll(rs, stmt);
    }
  }
}
