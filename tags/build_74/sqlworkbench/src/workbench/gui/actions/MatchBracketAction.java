/*
 * MatchBracketAction.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2004, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: info@sql-workbench.net
 *
 */
package workbench.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import workbench.gui.sql.EditorPanel;
import workbench.resource.ResourceMgr;

/**
 *	Action to copy the contents of a entry field into the clipboard
 *	@author  info@sql-workbench.net
 */
public class MatchBracketAction extends WbAction
{
	private EditorPanel client;

	public MatchBracketAction(EditorPanel aClient)
	{
		super();
		this.client = aClient;
		this.initMenuDefinition("MnuTxtMatchBracket", KeyStroke.getKeyStroke(KeyEvent.VK_B,KeyEvent.CTRL_MASK));
		this.setMenuItemName(ResourceMgr.MNU_TXT_EDIT);
		this.setEnabled(true);
	}

	public void executeAction(ActionEvent e)
	{
		this.client.matchBracket();
	}

}