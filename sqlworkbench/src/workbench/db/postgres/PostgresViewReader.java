/*
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
package workbench.db.postgres;

import java.sql.SQLException;

import workbench.resource.Settings;

import workbench.db.DefaultViewReader;
import workbench.db.DropType;
import workbench.db.TableDefinition;
import workbench.db.WbConnection;

/**
 *
 * @author Thomas Kellerer
 */
public class PostgresViewReader
  extends DefaultViewReader
{

  public PostgresViewReader(WbConnection con)
  {
    super(con);
  }

  @Override
  public CharSequence getExtendedViewSource(TableDefinition view, DropType dropType, boolean includeCommit)
    throws SQLException
  {
    CharSequence source = super.getExtendedViewSource(view, dropType, false);
    PostgresRuleReader ruleReader = new PostgresRuleReader();

    CharSequence rules = ruleReader.getTableRuleSource(this.connection, view.getTable());
    StringBuilder result = new StringBuilder(source.length() + (rules == null ? 0 : rules.length()));
    result.append(source);
    if (rules != null)
    {
      result.append("\n\n");
      result.append(rules);
    }

    if (includeCommit)
    {
      result.append("COMMIT;");
      result.append(Settings.getInstance().getInternalEditorLineEnding());
    }
    return result;
  }
}
