/*
 * TriggerReaderFactory.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2016, Thomas Kellerer
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
package workbench.db;

import workbench.db.mssql.SqlServerTriggerReader;
import workbench.db.oracle.OracleTriggerReader;
import workbench.db.postgres.PostgresTriggerReader;

/**
 * A factory to create instances of TriggerReader.
 *
 * Currently only one specialized TriggerReader is used for Postgres, for all
 * other DBMS, the DefaultTriggerReader is used.
 *
 * @author Thomas Kellerer
 */
public class TriggerReaderFactory
{
	public static TriggerReader createReader(WbConnection con)
	{
		if (con == null) return null;
		if (con.getMetadata() == null) return null;

		if (con.getMetadata().isPostgres())
		{
			return new PostgresTriggerReader(con);
		}
		if (con.getMetadata().isOracle())
		{
			return new OracleTriggerReader(con);
		}
		if (con.getMetadata().isSqlServer())
		{
			return new SqlServerTriggerReader(con);
		}
		return new DefaultTriggerReader(con);
	}
}
