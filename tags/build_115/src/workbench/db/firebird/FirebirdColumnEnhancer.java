/*
 * FirebirdColumnEnhancer.java
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
package workbench.db.firebird;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import workbench.db.ColumnDefinitionEnhancer;
import workbench.db.ColumnIdentifier;
import workbench.db.TableDefinition;
import workbench.db.WbConnection;
import workbench.log.LogMgr;
import workbench.util.SqlUtil;
import workbench.util.StringUtil;

/**
 *
 * @author Thomas Kellerer
 */
public class FirebirdColumnEnhancer
	implements ColumnDefinitionEnhancer
{

	@Override
	public void updateColumnDefinition(TableDefinition table, WbConnection conn)
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;

		String sql = "select rf.rdb$field_name, f.rdb$computed_source  \n" +
             "from rdb$fields f  \n" +
             "   join rdb$relation_fields rf on f.rdb$field_name = rf.rdb$field_source \n" +
             "   join rdb$relations r on r.rdb$relation_name = rf.rdb$relation_name \n" +
             "where f.rdb$computed_source IS NOT NULL \n" +
             "and r.rdb$relation_name = ? ";

		Map<String, String> expressions = new HashMap<String, String>();
		try
		{
			stmt = conn.getSqlConnection().prepareStatement(sql);
			stmt.setString(1, table.getTable().getTableName());
			rs = stmt.executeQuery();
			while (rs.next())
			{
				expressions.put(rs.getString(1).trim(), rs.getString(2).trim());
			}
		}
		catch (Exception e)
		{
			LogMgr.logWarning("FirebirdColumnEnhancer.updateColumnDefinition()", "Error retrieving computed columns", e);
		}
		finally
		{
			SqlUtil.closeAll(rs, stmt);
		}

		for (ColumnIdentifier col : table.getColumns())
		{
			String expr = expressions.get(col.getColumnName());
			if (StringUtil.isNonBlank(expr))
			{
				col.setComputedColumnExpression("COMPUTED BY " + expr);
			}
		}
	}

}