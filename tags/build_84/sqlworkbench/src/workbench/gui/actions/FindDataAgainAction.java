/*
 * FindDataAgainAction.java
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

import javax.swing.KeyStroke;

import workbench.interfaces.Searchable;
import workbench.resource.ResourceMgr;


/**
 *	Action to clear the contents of a entry field
 *	@author  support@sql-workbench.net
 */
public class FindDataAgainAction extends WbAction
{
	private Searchable client;

	public FindDataAgainAction(Searchable aClient)
	{
		super();
		this.client = aClient;
		
		this.initMenuDefinition("MnuTxtFindInTableDataAgain", KeyStroke.getKeyStroke(KeyEvent.VK_F3, InputEvent.SHIFT_MASK));
		this.setIcon(ResourceMgr.getImage("FindAgain"));
		this.setMenuItemName(ResourceMgr.MNU_TXT_DATA);
	}

	public void executeAction(ActionEvent e)
	{
		this.client.findNext();
	}
}