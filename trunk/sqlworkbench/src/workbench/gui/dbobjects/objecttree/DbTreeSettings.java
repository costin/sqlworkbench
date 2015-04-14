/*
 * DbTreeSettings.java
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
package workbench.gui.dbobjects.objecttree;

import workbench.resource.Settings;

/**
 *
 * @author Thomas Kellerer
 */
public class DbTreeSettings
{
  public static final String SETTINGS_PREFIX = "workbench.gui.dbtree.";

  public static boolean showOnlyCurrentSchema(String dbId)
  {
    boolean defaultValue = Settings.getInstance().getBoolProperty(SETTINGS_PREFIX + "only.currentschema", false);
    return Settings.getInstance().getBoolProperty(SETTINGS_PREFIX + dbId + ".only.currentschema", defaultValue);
  }

  public static TreePosition getDbTreePosition()
  {
    String pos = Settings.getInstance().getProperty(SETTINGS_PREFIX + "position", TreePosition.left.name());
    try
    {
      return TreePosition.valueOf(pos);
    }
    catch (Throwable th)
    {
      return TreePosition.left;
    }
  }

  public static void setDbTreePosition(TreePosition pos)
  {
    Settings.getInstance().setProperty(SETTINGS_PREFIX + "position", pos.name());
  }

  public static boolean getFilterWhileTyping()
  {
    return Settings.getInstance().getBoolProperty(SETTINGS_PREFIX + "quickfilter", true);
  }

  public static void setFilterWhileTyping(boolean flag)
  {
    Settings.getInstance().setProperty(SETTINGS_PREFIX + "quickfilter", flag);
  }

  public static boolean showCountRowsAction()
  {
    return Settings.getInstance().getBoolProperty(SETTINGS_PREFIX + "count.rows.visible", false);
  }

  public static void setShowCountRowsAction(boolean flag)
  {
    Settings.getInstance().setProperty(SETTINGS_PREFIX + "count.rows.visible", flag);
  }


  /**
   * Controls if the SELECT statement for displaying a table's data through drag and drop
   * should use the full column list.
   *
   * If this is true the columns for a dropped table will be retrieved and the generated
   * SQL will include all columns explicitely. This enables the use of the expression
   * replacement for certain data types.
   *
   * @return
   */
  public static boolean useColumnListForTableDataDisplay()
  {
    return Settings.getInstance().getBoolProperty(SETTINGS_PREFIX + "tabledata.select.use.columns", true);
  }
}