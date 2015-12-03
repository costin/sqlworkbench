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
package workbench.db.mssql;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import workbench.log.LogMgr;
import workbench.resource.Settings;

import workbench.db.DbObject;
import workbench.db.TableIdentifier;
import workbench.db.WbConnection;
import workbench.db.dependency.DependencyReader;

import workbench.gui.dbobjects.objecttree.DbObjectSorter;

import workbench.util.CollectionUtil;
import workbench.util.SqlUtil;

/**
 *
 * @author Thomas Kellerer
 */
public class SqlServerDependencyReader
  implements DependencyReader
{

  private final Set<String> supportedTypes = CollectionUtil.caseInsensitiveSet("table", "view");

  @Override
  public List<DbObject> getObjectDependencies(WbConnection connection, DbObject base)
  {
    if (base == null || connection == null) return Collections.emptyList();

    String typeDesc =
      "       case ao.type_desc \n" +
      "          when 'USER_TABLE' then 'TABLE'\n" +
      "          when 'SYSTEM_TABLE' then 'SYSTEM TABLE'\n" +
      "          when 'INTERNAL_TABLE' then 'SYSTEM TABLE'\n" +
      "          else type_desc \n" +
      "        end as type \n";

    String viewRefSql =
      "SELECT vtu.TABLE_CATALOG, vtu.TABLE_SCHEMA, vtu.TABLE_NAME,\n" + typeDesc +
      "FROM INFORMATION_SCHEMA.VIEW_TABLE_USAGE vtu \n" +
      "  JOIN sys.all_objects ao ON ao.name = vtu.TABLE_NAME and schema_name(ao.schema_id) = vtu.TABLE_SCHEMA\n" +
        "WHERE VIEW_CATALOG = ? \n" +
        "  AND VIEW_SCHEMA = ? \n" +
        "  AND VIEW_NAME = ? \n" +
        "ORDER BY vtu.VIEW_CATALOG, vtu.VIEW_SCHEMA, vtu.VIEW_NAME";

    String tableRefSql =
      "SELECT vtu.VIEW_CATALOG, vtu.VIEW_SCHEMA, vtu.VIEW_NAME,\n" + typeDesc +
      "FROM INFORMATION_SCHEMA.VIEW_TABLE_USAGE vtu \n" +
      "  JOIN sys.all_objects ao ON ao.name = vtu.VIEW_NAME and schema_name(ao.schema_id) = vtu.VIEW_SCHEMA \n" +
        "WHERE TABLE_CATALOG = ? \n" +
        "  AND TABLE_SCHEMA = ? \n" +
        "  AND TABLE_NAME = ? \n" +
        "ORDER BY vtu.VIEW_CATALOG_SCHEMA, vtu.VIEW_SCHEMA, vtu.VIEW__NAME";

    if (connection.getMetadata().isViewType(base.getObjectType()))
    {
      return retrieveObjects(connection, base, viewRefSql);
    }

    if (base.getObjectType().equalsIgnoreCase("TABLE"))
    {
      return retrieveObjects(connection, base, tableRefSql);
    }

    return Collections.emptyList();
  }

  private List<DbObject> retrieveObjects(WbConnection connection, DbObject base, String sql)
  {
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    List<DbObject> result = new ArrayList<>();

		if (Settings.getInstance().getDebugMetadataSql())
		{
			String s = SqlUtil.replaceParameters(sql, base.getCatalog(), base.getSchema(), base.getObjectName(), base.getObjectType());
			LogMgr.logDebug("SqlServerDependencyReader.retrieveObjects()", "Retrieving dependent objects using query:\n" + s);
		}

    try
    {
      pstmt = connection.getSqlConnection().prepareStatement(sql);
      pstmt.setString(1, base.getCatalog());
      pstmt.setString(2, base.getSchema());
      pstmt.setString(3, base.getObjectName());

      rs = pstmt.executeQuery();
      while (rs.next())
      {
        String catalog = rs.getString(1);
        String schema = rs.getString(2);
        String name = rs.getString(3);
        String type = rs.getString(4);
        TableIdentifier tbl = new TableIdentifier(catalog, schema, name);
        tbl.setType(type);
        result.add(tbl);
      }
    }
    catch (Exception ex)
    {
			String s = SqlUtil.replaceParameters(sql, base.getCatalog(), base.getSchema(), base.getObjectName(), base.getObjectType());
      LogMgr.logError("SqlServerDependencyReader.retrieveObjects()", "Could not read object dependency using:\n" + s, ex);
    }
    finally
    {
      SqlUtil.closeAll(rs, pstmt);
    }

    DbObjectSorter sorter = new DbObjectSorter(true);
    Collections.sort(result, sorter);
    return result;
  }

  @Override
  public boolean supportsDependencies(String objectType)
  {
    return supportedTypes.contains(objectType);
  }

}
