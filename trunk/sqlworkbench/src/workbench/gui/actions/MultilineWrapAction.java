/*
 * SetNullAction.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2014, Thomas Kellerer
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

import workbench.gui.components.WbCellEditor;
import workbench.resource.GuiSettings;

/**
 *
 * @author Thomas Kellerer
 */
public class MultilineWrapAction
	extends CheckBoxAction
{
	private WbCellEditor client;

	public MultilineWrapAction(WbCellEditor editor)
	{
		super("LblWordWrap", GuiSettings.PROP_WRAP_MULTILINE_EDITOR);
		client = editor;
		if (client != null && client.getEditor() != null)
		{
			addToInputMap(client.getEditor());
		}
	}

	@Override
	public void executeAction(ActionEvent e)
	{
		super.executeAction(e);
		if (client != null)
		{
			client.setWordwrap(GuiSettings.getWrapMultilineEditor());
		}
	}

	@Override
	public boolean allowDuplicate()
	{
		return true;
	}

}
