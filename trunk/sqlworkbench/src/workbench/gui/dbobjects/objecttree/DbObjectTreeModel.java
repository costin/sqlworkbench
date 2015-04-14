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
package workbench.gui.dbobjects.objecttree;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import workbench.resource.DbExplorerSettings;

import workbench.db.DbObject;

import workbench.gui.dbobjects.QuickFilterExpressionBuilder;

import workbench.storage.filter.ColumnExpression;

import workbench.util.StringUtil;

/**
 *
 * @author Thomas Kellerer
 */
public class DbObjectTreeModel
  extends DefaultTreeModel
{

  public DbObjectTreeModel()
  {
    super(new RootNode());
  }

  public DbObjectTreeModel(TreeNode root)
  {
    super(root);
  }

  public ObjectTreeNode findNodeByType(String name, String type)
  {
    if (StringUtil.isEmptyString(name) || StringUtil.isEmptyString(type)) return null;

    return findNodeByType((ObjectTreeNode)getRoot(), name, type);
  }

  public ObjectTreeNode findNodeByType(ObjectTreeNode parentNode, String name, String type)
  {
    if (parentNode.getName().equalsIgnoreCase(name) && parentNode.getType().equalsIgnoreCase(type)) return parentNode;
    int count = parentNode.getChildCount();
    for (int i=0; i < count; i ++)
    {
      ObjectTreeNode child = (ObjectTreeNode)parentNode.getChildAt(i);
      ObjectTreeNode nd1 = findNodeByType(child, name, type);
      if (nd1 != null) return nd1;
    }
    return null;
  }

  public void removeObjects(List<DbObject> toRemove)
  {
    removeObjects(toRemove, getRoot());
  }

  private void removeObjects(List<DbObject> toRemove, ObjectTreeNode node)
  {
    if (node == null) return;
    if (node.getChildCount() == 0) return;

    resetFilter();

    List<ObjectTreeNode> toDelete = new ArrayList<>();
    for (int i = 0; i < node.getChildCount(); i++)
    {
      ObjectTreeNode child = (ObjectTreeNode)node.getChildAt(i);
      DbObject dbo = child.getDbObject();
      if (dbo == null)
      {
        removeObjects(toRemove, child);
      }
      else if (toRemove.contains(dbo))
      {
        toDelete.add(child);
      }
    }

    for (ObjectTreeNode delete : toDelete)
    {
      node.remove(delete);
    }

    if (toDelete.size() > 0)
    {
      nodeStructureChanged(node);
    }
  }

  @Override
  public ObjectTreeNode getRoot()
  {
    return (ObjectTreeNode)super.getRoot();
  }

  public void resetFilter()
  {
    resetFilter(getRoot());
    nodeStructureChanged(getRoot());
  }

  public void resetFilter(ObjectTreeNode node)
  {
    int childCount = node.getChildCount();
    for (int i=0; i < childCount; i++)
    {
      ObjectTreeNode child = node.getChildAt(i);
      child.resetFilter();
      resetFilter(child);
    }
  }

  public void applyFilter(String text)
  {
    if (StringUtil.isEmptyString(text))
    {
      resetFilter();
    }
    else
    {
      QuickFilterExpressionBuilder builder = new QuickFilterExpressionBuilder();
      ColumnExpression expression = builder.buildExpression(text, DbExplorerSettings.getDbExpUsePartialMatch());
      applyFilter(getRoot(), expression);
    }
  }

  private boolean applyFilter(ObjectTreeNode node, ColumnExpression expression)
  {
    int childCount = node.getChildCount();
    boolean changed = false;

    for (int i=0; i < childCount; i++)
    {
      ObjectTreeNode child = node.getChildAt(i);
      if (child.applyFilter(expression))
      {
        changed = true;
      }
      else
      {
        if (applyFilter(child, expression))
        {
          changed = true;
        }
      }
    }
    if (changed)
    {
      nodeStructureChanged(node);
    }
    return changed;
  }
}