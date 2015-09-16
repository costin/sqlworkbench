/*
 * CreateDummySqlAction.java
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
package workbench.gui.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import workbench.interfaces.WbSelectionListener;
import workbench.interfaces.WbSelectionModel;

import workbench.db.ColumnIdentifier;
import workbench.db.DbObject;
import workbench.db.DummyInsert;
import workbench.db.DummySelect;
import workbench.db.DummyUpdate;
import workbench.db.ObjectScripter;
import workbench.db.TableIdentifier;

import workbench.gui.dbobjects.DbObjectList;
import workbench.gui.dbobjects.ObjectScripterUI;

/**
 * @author Thomas Kellerer
 */
public class CreateDummySqlAction
	extends WbAction
	implements WbSelectionListener
{
	private DbObjectList source;
	private String scriptType;
  private WbSelectionModel selection;

	public static CreateDummySqlAction createDummyUpdateAction(DbObjectList client, ListSelectionModel list)
	{
		return new CreateDummySqlAction("MnuTxtCreateDummyUpdate", client, list, ObjectScripter.TYPE_UPDATE);
	}

	public static CreateDummySqlAction createDummyInsertAction(DbObjectList client, ListSelectionModel list)
	{
		return new CreateDummySqlAction("MnuTxtCreateDummyInsert", client, list, ObjectScripter.TYPE_INSERT);
	}

	public static CreateDummySqlAction createDummySelectAction(DbObjectList client, ListSelectionModel list)
	{
		return new CreateDummySqlAction("MnuTxtCreateDefaultSelect", client, list, ObjectScripter.TYPE_SELECT);
	}

	public static CreateDummySqlAction createDummyUpdateAction(DbObjectList client, WbSelectionModel list)
	{
		return new CreateDummySqlAction("MnuTxtCreateDummyUpdate", client, list, ObjectScripter.TYPE_UPDATE);
	}

	public static CreateDummySqlAction createDummyInsertAction(DbObjectList client, WbSelectionModel list)
	{
		return new CreateDummySqlAction("MnuTxtCreateDummyInsert", client, list, ObjectScripter.TYPE_INSERT);
	}

	public static CreateDummySqlAction createDummySelectAction(DbObjectList client, WbSelectionModel list)
	{
		return new CreateDummySqlAction("MnuTxtCreateDefaultSelect", client, list, ObjectScripter.TYPE_SELECT);
	}

	private CreateDummySqlAction(String key, DbObjectList client, ListSelectionModel list, String type)
  {
    this(key, client, WbSelectionModel.Factory.createFacade(list), type);
  }

	private CreateDummySqlAction(String key, DbObjectList client, WbSelectionModel list, String type)
	{
		super();
		isConfigurable = false;
		this.initMenuDefinition(key);
		this.source = client;
		this.scriptType = type;
    selection = list;
		setEnabled(client.getSelectionCount() > 0);
		selection.addSelectionListener(this);
	}

  @Override
  public void dispose()
  {
    super.dispose();
    if (selection != null)
    {
      selection.removeSelectionListener(this);
    }
  }

  @Override
  public void executeAction(ActionEvent e)
  {
    List<? extends DbObject> objects = source.getSelectedObjects();
    List<DbObject> scriptObjects = new ArrayList<>(objects.size());
    List<ColumnIdentifier> cols = new ArrayList<>();
    for (DbObject dbo : objects)
    {
      if (dbo instanceof TableIdentifier)
      {
        TableIdentifier tbl = (TableIdentifier)dbo;
        if (scriptType.equalsIgnoreCase(ObjectScripter.TYPE_SELECT))
        {
          scriptObjects.add(new DummySelect(tbl));
        }
        else if (scriptType.equalsIgnoreCase(ObjectScripter.TYPE_UPDATE))
        {
          scriptObjects.add(new DummyUpdate(tbl));
        }
        else if (scriptType.equalsIgnoreCase(ObjectScripter.TYPE_INSERT))
        {
          scriptObjects.add(new DummyInsert(tbl));
        }
      }
      else if (dbo instanceof ColumnIdentifier)
      {
        cols.add((ColumnIdentifier)dbo);
      }
    }
    if (cols.size() > 0)
    {
      TableIdentifier tbl = source.getObjectTable();
      if (tbl != null)
      {
        if (scriptType.equalsIgnoreCase(ObjectScripter.TYPE_SELECT))
        {
          scriptObjects.add(new DummySelect(tbl, cols));
        }
        else
        {
          scriptObjects.add(new DummyInsert(tbl, cols));
        }
      }
    }
    ObjectScripter s = new ObjectScripter(scriptObjects, source.getConnection());
    ObjectScripterUI scripterUI = new ObjectScripterUI(s);
    scripterUI.show(SwingUtilities.getWindowAncestor(source.getComponent()));
  }

  @Override
  public void selectionChanged(WbSelectionModel source)
  {
		setEnabled(source.getSelectionCount() > 0);
  }

}