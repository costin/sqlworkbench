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

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;

import javax.swing.JTabbedPane;

import workbench.log.LogMgr;

import workbench.db.DbObject;
import workbench.db.TableIdentifier;
import workbench.db.WbConnection;

import workbench.gui.sql.SqlPanel;


/**
 *
 * @author Thomas Kellerer
 */
public class ResultTabDropHandler
	implements DropTargetListener

{
  private JTabbedPane resultTab;
  private SqlPanel sqlPanel;
  private final DropTarget target;

  public ResultTabDropHandler(SqlPanel panel, JTabbedPane tab)
  {
    sqlPanel = panel;
    resultTab = tab;
    target = new DropTarget(tab, DnDConstants.ACTION_COPY, this);
  }

  public void handleDrop(ObjectTreeTransferable selection)
  {
    if (sqlPanel.isBusy()) return;
    if (selection == null) return;

    ObjectTreeNode[] nodes = selection.getSelectedNodes();
    if (nodes == null || nodes.length != 1) return;

    DbObject dbo = nodes[0].getDbObject();
    if (dbo == null) return;

    WbConnection connection = sqlPanel.getConnection();
    if (connection == null) return;

    if (dbo instanceof TableIdentifier)
    {
      TableIdentifier tbl = (TableIdentifier)dbo;
      String sql = "select * from " + tbl.getTableExpression(connection);
      sqlPanel.executeMacroSql(sql, false, true);
    }
  }

	public void dispose()
	{
		if (target != null)
		{
			target.removeDropTargetListener(this);
		}
	}

	@Override
	public void dragEnter(java.awt.dnd.DropTargetDragEvent dropTargetDragEvent)
	{
    if (sqlPanel.isBusy())
    {
      dropTargetDragEvent.rejectDrag();
      return;
    }

    Transferable tr = dropTargetDragEvent.getTransferable();
    if (tr.isDataFlavorSupported(ObjectTreeTransferable.DATA_FLAVOR))
    {
      try
      {
        ObjectTreeTransferable selection = (ObjectTreeTransferable)tr.getTransferData(ObjectTreeTransferable.DATA_FLAVOR);
        ObjectTreeNode[] nodes = selection.getSelectedNodes();
        if (nodes != null && nodes.length == 1)
        {
          dropTargetDragEvent.acceptDrag(DnDConstants.ACTION_COPY);
        }
        return;
      }
      catch (Exception ex)
      {
        // ignore
      }
    }
    dropTargetDragEvent.rejectDrag();
	}

	@Override
	public void dragExit(java.awt.dnd.DropTargetEvent dropTargetEvent)
	{
	}

	@Override
	public void dragOver(java.awt.dnd.DropTargetDragEvent dropTargetDragEvent)
	{
	}

	@Override
	public void drop(java.awt.dnd.DropTargetDropEvent dropTargetDropEvent)
	{
		try
		{
			Transferable tr = dropTargetDropEvent.getTransferable();
      if (tr.isDataFlavorSupported(ObjectTreeTransferable.DATA_FLAVOR))
      {
        ObjectTreeTransferable selection = (ObjectTreeTransferable)tr.getTransferData(ObjectTreeTransferable.DATA_FLAVOR);
        handleDrop(selection);
      }
      else
			{
				dropTargetDropEvent.rejectDrop();
			}
		}
		catch (IOException | UnsupportedFlavorException io)
		{
			LogMgr.logError("ResultTabDropHandler.drop()", "Error processing drop event", io);
			dropTargetDropEvent.rejectDrop();
		}
	}

	@Override
	public void dropActionChanged(java.awt.dnd.DropTargetDragEvent dropTargetDragEvent)
	{
	}

}
