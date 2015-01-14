/*
 * CloseAllResultsAction.java
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

import workbench.resource.ResourceMgr;

import workbench.gui.sql.SqlPanel;

/**
 * An action to close all result tabs of a SqlPanel.
 *
 * @author  Thomas Kellerer
 */
public class CloseAllResultsAction
	extends WbAction
{
	private SqlPanel panel;

	public CloseAllResultsAction(SqlPanel sqlPanel)
	{
		super();
		panel = sqlPanel;
		this.initMenuDefinition("MnuTxtCloseAllResults");
		this.setMenuItemName(ResourceMgr.MNU_TXT_DATA);
		this.setIcon(null);
		this.setEnabled(panel.getResultTabCount() > 0);
	}

	@Override
	public void executeAction(ActionEvent e)
	{
		panel.closeAllResults();
	}

}