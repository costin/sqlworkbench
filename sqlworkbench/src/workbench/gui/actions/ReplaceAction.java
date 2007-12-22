/*
 * ReplaceAction.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2008, Thomas Kellerer
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

import workbench.interfaces.Replaceable;
import workbench.resource.ResourceMgr;

/**
 *	Start search & replace in the editor
 *	@author  support@sql-workbench.net
 */
public class ReplaceAction extends WbAction
{
	private Replaceable client;

	public ReplaceAction(Replaceable aClient)
	{
		super();
		this.client = aClient;
		this.initMenuDefinition("MnuTxtReplace",KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_MASK));
		this.setMenuItemName(ResourceMgr.MNU_TXT_EDIT);
	}

	public void executeAction(ActionEvent e)
	{
		this.client.replace();
	}
}
