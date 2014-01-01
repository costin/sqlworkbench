/*
 * LogArea.java
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
package workbench.gui.sql;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JTextArea;

import workbench.resource.Settings;

import workbench.gui.WbSwingUtilities;
import workbench.gui.components.TextComponentMouseListener;

/**
 * @author Thomas Kellerer
 */
public class LogArea
	extends JTextArea
	implements PropertyChangeListener
{
	private TextComponentMouseListener contextMenu;

	public LogArea()
	{
		super();
		setBorder(WbSwingUtilities.EMPTY_BORDER);
		setFont(Settings.getInstance().getMsgLogFont());
		setEditable(false);
		setLineWrap(true);
		setWrapStyleWord(true);

		initColors();

		contextMenu = new TextComponentMouseListener();
		addMouseListener(contextMenu);

		Settings.getInstance().addPropertyChangeListener(this,
			Settings.PROPERTY_EDITOR_FG_COLOR,
			Settings.PROPERTY_EDITOR_BG_COLOR);
	}

	public void dispose()
	{
		setText("");
		Settings.getInstance().removePropertyChangeListener(this);
		if (contextMenu != null)
		{
			contextMenu.dispose();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		initColors();
	}

	private void initColors()
	{
		setBackground(Settings.getInstance().getEditorBackgroundColor());
		setForeground(Settings.getInstance().getEditorTextColor());
	}
}
