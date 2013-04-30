/*
 * JumpToStatement.java
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
package workbench.gui.actions;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;

import workbench.gui.WbSwingUtilities;
import workbench.gui.sql.EditorPanel;
import workbench.resource.ResourceMgr;
import workbench.util.StringUtil;

/**
 *
 * @author Thomas Kellerer
 */
public class JumpToLineAction
	extends WbAction
{
	private EditorPanel editor;

	public JumpToLineAction(EditorPanel panel)
	{
		super();
		initMenuDefinition("MnuTxtJumpToLineNr");
		setDefaultAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_MASK));
		setMenuItemName(ResourceMgr.MNU_TXT_EDIT);
		initializeShortcut();
		editor = panel;
	}

	@Override
	public void actionPerformed(ActionEvent evt)
	{
		String lineInput = WbSwingUtilities.getUserInput(editor, ResourceMgr.getString("TxtJumpToLine"), null);
		if (StringUtil.isBlank(lineInput)) return;

		int line = -1;
		try
		{
			line = Integer.valueOf(lineInput.trim()) - 1;
		}
		catch (NumberFormatException nfe)
		{
			return;
		}

		if (line > 0)
		{
			final int pos = editor.getLineStartOffset(line);
			EventQueue.invokeLater( new Runnable()
			{
				@Override
				public void run()
				{
					editor.setCaretPosition(pos);
				}
			});
		}
	}

}
