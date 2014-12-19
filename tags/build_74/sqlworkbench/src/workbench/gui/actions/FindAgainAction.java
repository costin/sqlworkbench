/*
 * FindAgainAction.java
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

import workbench.interfaces.Searchable;
import workbench.resource.ResourceMgr;


/**
 *	Action to clear the contents of a entry field
 *	@author  info@sql-workbench.net
 */
public class FindAgainAction extends WbAction
{
	private Searchable client;

	public FindAgainAction(Searchable aClient)
	{
		super();
		this.client = aClient;
		this.initMenuDefinition("MnuTxtFindAgain", KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
		this.setMenuItemName(ResourceMgr.MNU_TXT_EDIT);
	}

	public void executeAction(ActionEvent e)
	{
		this.client.findNext();
	}
}