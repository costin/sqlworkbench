/*
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2016, Thomas Kellerer.
 *
 * Licensed under a modified Apache License, Version 2.0
 * that restricts the use for certain governments.
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://sql-workbench.net/manual/license.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * To contact the author please send an email to: support@sql-workbench.net
 */
package workbench.db.teradata;

import workbench.db.DbMetadata;
import workbench.db.IndexDefinition;
import workbench.db.JdbcIndexReader;
import workbench.db.TableIdentifier;
import workbench.db.WbConnection;

/**
 *
 * @author Thomas Kellerer
 */
public class TeradataIndexReader
	extends JdbcIndexReader
{

	public TeradataIndexReader(DbMetadata meta)
	{
		super(meta);
	}

	@Override
	public CharSequence getIndexSource(TableIdentifier table, IndexDefinition idx)
	{
		if (table == null) return null;
		if (idx == null) return null;

		String sql = "CREATE";
		if (idx.isUnique())
		{
			sql += " UNIQUE";
		}

		WbConnection con = metaData == null ? null : metaData.getWbConnection();

		sql += " INDEX (" + idx.getColumnList() + ") ON " + table.getTableExpression(con) + ";";

		return sql;
	}

}
