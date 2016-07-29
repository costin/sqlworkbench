/*
 * PrevTabAction.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2016, Thomas Kellerer
 *
 * Licensed under a modified Apache License, Version 2.0
 * that restricts the use for certain governments.
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at.
 *
 *     http://sql-workbench.net/manual/license.html
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
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;


/**
 *	Select the previous tab from a tabbed pane
 *	@author  Thomas Kellerer
 */
public class PrevTabAction
	extends WbAction
{
	private JTabbedPane client;

	public PrevTabAction(JTabbedPane aClient)
	{
		super();
		this.client = aClient;
		this.initMenuDefinition("MnuTxtPrevTab", KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK));
		this.removeIcon();
		this.setEnabled(true);
	}

	@Override
	public void executeAction(ActionEvent e)
	{
		if (client.getTabCount() == 0) return;
		int newIndex = client.getSelectedIndex() - 1;
		if (newIndex < 0) newIndex = client.getTabCount() - 1;
		client.setSelectedIndex(newIndex);
	}

}