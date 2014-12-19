/*
 * ExecuteCurrentAction.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2007, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import workbench.gui.sql.SqlPanel;
import workbench.resource.ResourceMgr;

/**
 *	@author  support@sql-workbench.net
 */
public class ExecuteCurrentAction extends WbAction
{
	private SqlPanel target;
	
	public ExecuteCurrentAction(SqlPanel aPanel)
	{
		super();
		this.target = aPanel;
		this.initMenuDefinition("MnuTxtExecuteCurrent", KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_MASK));
		this.setIcon(ResourceMgr.getImage("ExecuteCurrent"));
		this.setMenuItemName(ResourceMgr.MNU_TXT_SQL);
	}

	public void executeAction(ActionEvent e)
	{
		this.target.runCurrentStatement();
	}
	
}