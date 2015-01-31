/*
 * FileDiscardAction.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2006, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;

import workbench.gui.sql.SqlPanel;
import workbench.resource.ResourceMgr;

public class FileDiscardAction extends WbAction
{
	private SqlPanel client;

	public FileDiscardAction(SqlPanel aClient)
	{
		super();
		this.client = aClient;
		String desc = ResourceMgr.getDescription("MnuTxtFileDiscard", true);
		this.putValue(Action.SHORT_DESCRIPTION, desc);
		this.initMenuDefinition(ResourceMgr.getString("MnuTxtFileDiscard"), desc, KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.CTRL_MASK));
		this.setMenuItemName(ResourceMgr.MNU_TXT_FILE);
		this.setEnabled(aClient.hasFileLoaded());
	}

	public void addToInputMap(InputMap im, ActionMap am)
	{
		super.addToInputMap(im, am);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK), this.getActionName());
	}
	
	public void executeAction(ActionEvent e)
	{
		this.client.closeFile(!isShiftPressed(e));
	}
}