/*
 * CopySelectedAsSqlInsertAction.java
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
package workbench.gui.actions;

import java.awt.event.ActionEvent;
import workbench.gui.components.ClipBoardCopier;

import workbench.gui.components.WbTable;
import workbench.resource.ResourceMgr;

/**
 * Action to copy the selected content of a table to the clipboard as SQL INSERT statements
 * @see workbench.gui.components.ClipBoardCopier
 * @author  Thomas Kellerer
 */
public class CopySelectedAsSqlInsertAction
	extends WbAction
{
	private WbTable client;

	public CopySelectedAsSqlInsertAction(WbTable aClient)
	{
		super();
		this.client = aClient;
		this.initMenuDefinition("MnuTxtCopySelectedAsSqlInsert", null);
		this.setMenuItemName(ResourceMgr.MNU_TXT_COPY_SELECTED);
		this.setEnabled(false);
	}

	@Override
	public boolean hasCtrlModifier()
	{
		return true;
	}

	@Override
	public boolean hasShiftModifier()
	{
		return true;
	}

	@Override
	public void executeAction(ActionEvent e)
	{
		ClipBoardCopier copier = new ClipBoardCopier(this.client);
		boolean selectColumns = false;
		if (invokedByMouse(e))
		{
			selectColumns = isCtrlPressed(e);
		}
		copier.copyAsSqlInsert(true, selectColumns);
	}

}
