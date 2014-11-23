/*
 * RunMacroAction.java
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
import javax.swing.Action;
import javax.swing.KeyStroke;

import workbench.gui.MainWindow;
import workbench.gui.editor.MacroExpander;
import workbench.gui.macros.MacroRunner;
import workbench.gui.sql.SqlPanel;
import workbench.resource.ResourceMgr;
import workbench.resource.StoreableKeyStroke;
import workbench.sql.macros.MacroDefinition;
import workbench.util.NumberStringCache;
import workbench.util.StringUtil;

/**
 *	@author  Thomas Kellerer
 */
public class RunMacroAction
	extends WbAction
{
	private MainWindow client;
	private MacroDefinition macro;

	public RunMacroAction(MainWindow aClient, MacroDefinition def, int index)
	{
		super();
		this.macro = def;
		this.client = aClient;
		if (def == null)
		{
			String title = ResourceMgr.getPlainString("LblRunMacro");
			setMenuText(title);
			String desc = ResourceMgr.getDescription("MnuTxtRunMacro", true);
			desc = desc.replaceAll("[ ]*(%macro%)[ ]*", " ");
			this.putValue(Action.SHORT_DESCRIPTION, desc);
		}
		else
		{
			String menuTitle = def.getName();
			if (index < 10)
			{
				menuTitle = "&" + NumberStringCache.getNumberString(index) + " - " + def.getName();
			}
			this.setMenuText(menuTitle);
			String desc = ResourceMgr.getDescription("MnuTxtRunMacro", true);
			desc = StringUtil.replace(desc, "%macro%", "'" + macro.getName() + "'");
			this.putValue(Action.SHORT_DESCRIPTION, desc);
			StoreableKeyStroke key = macro.getShortcut();
			if (key != null)
			{
				KeyStroke stroke = key.getKeyStroke();
				setAccelerator(stroke);
			}
		}
		this.setMenuItemName(ResourceMgr.MNU_TXT_MACRO);
		this.setIcon(null);
		setEnabled(macro != null && client != null);
	}

	public void setMacro(MacroDefinition def)
	{
		this.macro = def;
	}

	@Override
	public void executeAction(ActionEvent e)
	{
		if (this.client == null || this.macro == null) return;

		SqlPanel sql = this.client.getCurrentSqlPanel();
		if (sql == null) return;

		if (macro.getExpandWhileTyping())
		{
			MacroExpander expander = sql.getEditor().getMacroExpander();
			if (expander != null)
			{
				expander.insertMacroText(macro.getText());
				sql.selectEditorLater();
			}
		}
		else
		{
			boolean shiftPressed = isShiftPressed(e) && invokedByMouse(e);
			MacroRunner runner = new MacroRunner();
			runner.runMacro(macro, sql, shiftPressed);
		}
	}
}