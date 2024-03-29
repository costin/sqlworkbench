/*
 * This file is part of SQL Workbench/J, https://www.sql-workbench.eu
 *
 * Copyright 2002-2019, Thomas Kellerer.
 *
 * Licensed under a modified Apache License, Version 2.0
 * that restricts the use for certain governments.
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.sql-workbench.eu/manual/license.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * To contact the author please send an email to: support@sql-workbench.eu
 */
package workbench.gui.dbobjects.objecttree;

import java.awt.Point;

import workbench.db.ColumnIdentifier;
import workbench.db.ConnectionMgr;
import workbench.db.DbObject;
import workbench.db.ProcedureDefinition;
import workbench.db.TableIdentifier;
import workbench.db.WbConnection;

import workbench.gui.sql.EditorPanel;

import workbench.sql.formatter.FormatterUtil;
import workbench.sql.formatter.WbSqlFormatter;
import workbench.sql.parser.ParserType;
import workbench.sql.parser.ScriptParser;

import workbench.util.StringUtil;

/**
 *
 * @author Thomas Kellerer
 */
public class EditorDropHandler
{
  private EditorPanel editor;

  public EditorDropHandler(EditorPanel editor)
  {
    this.editor = editor;
  }

  public void handleDrop(ObjectTreeTransferable selection, Point location)
  {
    if (selection == null) return;
    ObjectTreeNode[] nodes = selection.getSelectedNodes();
    if (nodes == null || nodes.length == 0) return;

    String id = selection.getConnectionId();
    WbConnection conn = ConnectionMgr.getInstance().findConnection(id);

    ScriptParser parser = new ScriptParser(ParserType.getTypeFromConnection(conn));
    parser.setScript(editor.getSelectedStatement());

    int editorPos = editor.xyToOffset((int)location.getX() - editor.getPainter().getGutterWidth(), (int)location.getY());
    int index = parser.getCommandIndexAtCursorPos(editorPos);

    String sql = parser.getCommand(index, false);

    // handle the case where a single table is dragged
    // into an "empty" area of the editor. In that case
    // generate a select statement for the table instead
    // of just inserting the table name
    if (nodes.length == 1 && StringUtil.isEmptyString(sql))
    {
      DbObject dbo = nodes[0].getDbObject();
      if (dbo instanceof TableIdentifier)
      {
        String text = "select * from " + dbo.getObjectExpression(conn);
        WbSqlFormatter formatter = new WbSqlFormatter(text, conn.getDbId());
        text = formatter.getFormattedSql();
        insertString(text, editorPos);
        return;
      }
    }

    int line = editor.getLineOfOffset(editorPos);
    int column = editor.getColumnOfOffset(editorPos);
    boolean isStringLiteral = editor.getTokenMarker().isStringLiteralAt(line, column);

    StringBuilder text = new StringBuilder(nodes.length * 20);
    for (int i=0; i < nodes.length; i++)
    {
      if (i > 0) text.append(", ");
      text.append(getDisplayString(conn, nodes[i], !isStringLiteral));
    }

    insertString(text.toString(), editorPos);
  }

  private void insertString(String text, int location)
  {
    int start = editor.getSelectionStart();
    int end = editor.getSelectionEnd();

    if (start < end && start <= location && location <= end)
    {
      editor.setSelectedText(text);
    }
    else
    {
      editor.insertText(location, text);
    }
  }

  private String getDisplayString(WbConnection conn, ObjectTreeNode node, boolean applyFormat)
  {
    if (node == null) return "";

    DbObject dbo = node.getDbObject();
    if (dbo == null)
    {
      if (TreeLoader.TYPE_COLUMN_LIST.equals(node.getType()))
      {
        return getColumnList(node, applyFormat);
      }
      return node.getName();
    }

    String name;
    if (dbo instanceof ProcedureDefinition)
    {
      name = ((ProcedureDefinition)dbo).getDisplayName();
    }
    else
    {
      name = dbo.getObjectExpression(conn);
      if (applyFormat)
      {
        return FormatterUtil.getIdentifier(name);
      }
    }
    return name;
  }

  private String getColumnList(ObjectTreeNode columns, boolean applyFormat)
  {
    int count = columns.getChildCount();
    StringBuilder result = new StringBuilder(count * 10);
    int colCount = 0;
    for (int i=0; i < count; i++)
    {
      ObjectTreeNode col = (ObjectTreeNode)columns.getChildAt(i);
      if (col != null && col.getDbObject() != null)
      {
        DbObject dbo = col.getDbObject();
        if (dbo instanceof ColumnIdentifier)
        {
          if (colCount > 0) result.append(", ");
          result.append(dbo.getObjectName());
          colCount++;
        }
      }
    }
    String name = result.toString();
    if (applyFormat)
    {
      return FormatterUtil.getIdentifier(name);
    }
    return name;
  }
}
