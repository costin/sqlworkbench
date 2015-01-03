/*
 * FirebirdColumnEnhancer.java
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
package workbench.db.firebird;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import workbench.log.LogMgr;
import workbench.resource.Settings;

import workbench.db.ColumnDefinitionEnhancer;
import workbench.db.ColumnIdentifier;
import workbench.db.JdbcUtils;
import workbench.db.TableDefinition;
import workbench.db.WbConnection;

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
		boolean is30 = JdbcUtils.hasMinimumServerVersion(conn, "3.0");

		String sql;
		if (is30)
		{
			sql =
				"select rf.rdb$field_name, \n" +
				"       f.rdb$computed_source, \n" +
				"       rf.rdb$identity_type as identity_type, \n" +
				"       g.rdb$initial_value as initial_value \n" +
				"from rdb$fields f   \n" +
				"   join rdb$relation_fields rf on f.rdb$field_name = rf.rdb$field_source  \n" +
				"   join rdb$relations r on r.rdb$relation_name = rf.rdb$relation_name  \n" +
				"   left join rdb$generators g on g.rdb$generator_name = rf.rdb$generator_name \n" +
        "where (f.rdb$computed_source IS NOT NULL or rf.rdb$identity_type is not null) \n" +
				"  and r.rdb$relation_name = ? ";
		}
		else
		{
			sql = "select rf.rdb$field_name, f.rdb$computed_source, null as identity_type, null as initial_value \n" +
             "from rdb$fields f  \n" +
             "   join rdb$relation_fields rf on f.rdb$field_name = rf.rdb$field_source \n" +
             "   join rdb$relations r on r.rdb$relation_name = rf.rdb$relation_name \n" +
             "where f.rdb$computed_source IS NOT NULL \n" +
             "  and r.rdb$relation_name = ? ";
		}

		if (Settings.getInstance().getDebugMetadataSql())
		{
			LogMgr.logInfo("FirebirdColumnEnhancer.updateColumnDefinition()", "Query to retrieve column information:\n " + sql);
		}
		Map<String, String> expressions = new HashMap<String, String>();
		Map<String, String> identityCols = new HashMap<String, String>();
		try
		{
			stmt = conn.getSqlConnection().prepareStatement(sql);
			stmt.setString(1, table.getTable().getTableName());
			rs = stmt.executeQuery();
			while (rs.next())
			{
				String colName = rs.getString(1).trim();
				String expr = rs.getString(2);
				int identity = rs.getInt(3);
				if (rs.wasNull())
				{
					identity = -1;
				}
				int seqStart = rs.getInt(4);
				if (rs.wasNull())
				{
					seqStart = -1;
				}

				if (identity > 0)
				{
					String gen = "GENERATED BY DEFAULT AS IDENTITY";
					if (seqStart > 0)
					{
						gen += " (START WITH " + seqStart + ")";
					}
					identityCols.put(colName, gen);
				}
				expressions.put(colName, expr);
			}
		}
		catch (Exception e)
		{
			LogMgr.logWarning("FirebirdColumnEnhancer.updateColumnDefinition()", "Error retrieving column information using sql:\n" + sql, e);
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
				col.setComputedColumnExpression("COMPUTED BY " + expr.trim());
			}
			String generator = identityCols.get(col.getColumnName());
			if (generator != null)
			{
				col.setIsAutoincrement(true);
				col.setGeneratorExpression(generator);
			}
		}
	}

}
