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
package workbench.db.postgres;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import workbench.db.DbMetadata;
import workbench.log.LogMgr;
import workbench.resource.Settings;

import workbench.db.DbObject;
import workbench.db.SequenceDefinition;
import workbench.db.TableIdentifier;
import workbench.db.WbConnection;
import workbench.db.dependency.DependencyReader;
import workbench.db.dependency.DependencyReaderFactory;

import workbench.gui.dbobjects.objecttree.DbObjectSorter;

import workbench.util.CollectionUtil;
import workbench.util.SqlUtil;

/**
 *
 * @author Thomas Kellerer
 */
public class PostgresDependencyReader
  implements DependencyReader
{
  private final Set<String> supportedTypes = CollectionUtil.caseInsensitiveSet("table", "view");
  private final Set<String> searchBoth = CollectionUtil.caseInsensitiveSet(DependencyReaderFactory.getSearchBothDirections(DbMetadata.DBID_PG, "view"));

  public PostgresDependencyReader()
  {
  }

  @Override
  public List<DbObject> getObjectDependencies(WbConnection connection, DbObject base)
  {
    if (base == null || connection == null) return Collections.emptyList();


    String typeCase =
      "       CASE cl.relkind \n" +
      "          WHEN 'r' THEN 'TABLE'\n" +
      "          WHEN 'i' THEN 'INDEX'\n" +
      "          WHEN 'S' THEN 'SEQUENCE'\n" +
      "          WHEN 'v' THEN 'VIEW'\n" +
      "          WHEN 'm' THEN 'MATERIALIZED VIEW'\n" +
      "          WHEN 'c' THEN 'TYPE'\n" +
      "          WHEN 't' THEN 'TOAST'\n" +
      "          WHEN 'f' THEN 'FOREIGN TABLE'\n" +
      "       END AS object_type, \n";

    String searchViewNameSql =
      "select vtu.table_schema, \n" +
      "       vtu.table_name, \n" + typeCase +
      "       obj_description(cl.oid) as remarks\n" +
      "from information_schema.view_table_usage vtu \n" +
      "  join pg_class cl on cl.oid = concat_ws('.', quote_ident(vtu.table_schema), quote_ident(vtu.table_name))::regclass \n" +
      "where (view_schema, view_name) = (?, ?)" +
      "order by view_schema, view_name";

    String searchTableNameSql =
        "select vtu.view_schema, \n" +
        "       vtu.view_name, \n" + typeCase +
        "       obj_description(cl.oid) as remarks\n" +
        "from information_schema.view_table_usage vtu \n" +
        "  join pg_class cl on cl.oid = concat_ws('.', quote_ident(vtu.view_schema), quote_ident(vtu.view_name))::regclass \n" +
        "where (table_schema, table_name) = (?, ?)" +
        "order by view_schema, view_name";

    String sql = searchTableNameSql;
    if (connection.getMetadata().isViewType(base.getObjectType()))
    {
      sql = searchViewNameSql;
    }

    List<DbObject> objects = retrieveObjects(connection, base, sql);

    if (searchBoth.contains(base.getObjectType()))
    {
      List<DbObject> tbl = retrieveObjects(connection, base, searchTableNameSql);
      objects.addAll(tbl);
    }

    return objects;
  }

  private List<DbObject> retrieveObjects(WbConnection connection, DbObject base, String sql)
  {
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    List<DbObject> result = new ArrayList<>();

		if (Settings.getInstance().getDebugMetadataSql())
		{
			String s = SqlUtil.replaceParameters(sql, base.getSchema(), base.getObjectName(), base.getObjectType());
			LogMgr.logDebug("PostgresDependencyReader.retrieveObjects()", "Retrieving dependent objects using query:\n" + s);
		}

    Savepoint sp = null;
    try
    {
      connection.setSavepoint();
      pstmt = connection.getSqlConnection().prepareStatement(sql);
      pstmt.setString(1, base.getSchema());
      pstmt.setString(2, base.getObjectName());

      rs = pstmt.executeQuery();
      while (rs.next())
      {
        String schema = rs.getString(1);
        String name = rs.getString(2);
        String type = rs.getString(3);
        String remarks = rs.getString(4);
        if (type.equals("SEQUENCE"))
        {
          SequenceDefinition seq = new SequenceDefinition(null, schema, name);
          seq.setComment(remarks);
          result.add(seq);
        }
        else
        {
          TableIdentifier tbl = new TableIdentifier(null, schema, name);
          tbl.setComment(remarks);
          tbl.setType(type);
          result.add(tbl);
        }
      }
    }
    catch (Exception ex)
    {
			String s = SqlUtil.replaceParameters(sql, base.getSchema(), base.getObjectName(), base.getObjectType());
      LogMgr.logError("PostgresDependencyReader.retrieveObjects()", "Could not read object dependency using:\n" + s, ex);
    }
    finally
    {
      connection.rollback(sp);
      SqlUtil.closeAll(rs, pstmt);
    }

    DbObjectSorter sorter = new DbObjectSorter();
    Collections.sort(result, sorter);
    return result;
  }

  @Override
  public boolean supportsDependencies(String objectType)
  {
    return supportedTypes.contains(objectType);
  }

}