/*
 * CopySelectedAsSqlDeleteInsertAction.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2005, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: info@sql-workbench.net
 *
 */
package workbench.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import workbench.gui.components.WbTable;
import workbench.resource.ResourceMgr;

/**
 *	Action to copy the contents of a entry field into the clipboard
 *	@author  info@sql-workbench.net
 */
public class CopySelectedAsSqlDeleteInsertAction extends WbAction
{
	private WbTable client;

	public CopySelectedAsSqlDeleteInsertAction(WbTable aClient)
	{
		super();
		this.client = aClient;
		this.initMenuDefinition("MnuTxtCopySelectedAsSqlDeleteInsert");
		this.setMenuItemName(ResourceMgr.MNU_TXT_COPY_SELECTED);
		this.setIcon(null);
		this.setEnabled(false);
	}

	public void executeAction(ActionEvent e)
	{
		boolean ctrlPressed = ((e.getModifiers() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK);
		ctrlPressed = ctrlPressed && ((e.getModifiers() & ActionEvent.MOUSE_EVENT_MASK) == ActionEvent.MOUSE_EVENT_MASK);
		client.copyAsSqlDeleteInsert(true, ctrlPressed);
	}

}
