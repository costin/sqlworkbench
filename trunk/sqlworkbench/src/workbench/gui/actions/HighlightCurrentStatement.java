/*
 * HighlightCurrentStatement.java
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import workbench.resource.ResourceMgr;
import workbench.resource.Settings;

/**
 *	Toggle highlighting of the currently executed statement.
 *	@author  Thomas Kellerer
 */
public class HighlightCurrentStatement
	extends CheckBoxAction
	implements PropertyChangeListener
{

	public HighlightCurrentStatement()
	{
		super("MnuTxtHighlightCurrent", Settings.PROPERTY_HIGHLIGHT_CURRENT_STATEMENT);
		this.setMenuItemName(ResourceMgr.MNU_TXT_SQL);
		Settings.getInstance().addPropertyChangeListener(this, Settings.PROPERTY_HIGHLIGHT_CURRENT_STATEMENT);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		this.setSwitchedOn(Settings.getInstance().getBoolProperty(Settings.PROPERTY_HIGHLIGHT_CURRENT_STATEMENT, false));
	}

}
