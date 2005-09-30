/*
 * SelectMaxRowsAction.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2005, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.actions;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;
import workbench.gui.sql.DwPanel;

import workbench.gui.sql.SqlPanel;
import workbench.resource.ResourceMgr;

/**
 *	Action to copy the contents of a entry field into the clipboard
 *	@author  support@sql-workbench.net
 */
public class SelectMaxRowsAction extends WbAction
{
	private DwPanel client;

	public SelectMaxRowsAction(DwPanel aClient)
	{
		super();
		this.client = aClient;
		this.initMenuDefinition("MnuTxtSelectMaxRows", KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
		this.setMenuItemName(ResourceMgr.MNU_TXT_EDIT);
	}

	public void executeAction(ActionEvent e)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				client.selectMaxRowsField();
			}
		});
	}
}
