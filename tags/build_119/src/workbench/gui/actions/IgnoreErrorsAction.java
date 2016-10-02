/*
 * IgnoreErrorsAction.java
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

import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import workbench.resource.IconMgr;
import workbench.resource.Settings;

import workbench.gui.components.WbToolbarButton;

/**
 *	Toggle the "ignore errors" settings
 *	@author  Thomas Kellerer
 */
public class IgnoreErrorsAction
	extends CheckBoxAction
{
	private JToggleButton toggleButton;

	public IgnoreErrorsAction()
	{
		super("MnuTxtIgnoreErrors", null);
		super.setSwitchedOn(Settings.getInstance().getIgnoreErrors());
	}

	public JToggleButton createButton()
	{
		this.toggleButton = new JToggleButton(this);
		this.toggleButton.setText(null);
		this.toggleButton.setMargin(WbToolbarButton.MARGIN);
		this.toggleButton.setIcon(IconMgr.getInstance().getToolbarIcon("ignore_error"));
		this.toggleButton.setSelected(isSwitchedOn());
		return this.toggleButton;
	}

	@Override
	public void addToToolbar(JToolBar aToolbar)
	{
		if (this.toggleButton == null) this.createButton();
		aToolbar.add(this.toggleButton);
	}

	@Override
	public void setSwitchedOn(boolean aFlag)
	{
		super.setSwitchedOn(aFlag);
		if (this.toggleButton != null)
		{
			this.toggleButton.setSelected(isSwitchedOn());
		}
		Settings.getInstance().setIgnoreErrors(isSwitchedOn());
	}

}