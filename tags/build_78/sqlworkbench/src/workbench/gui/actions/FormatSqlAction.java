/*
 * FormatSqlAction.java
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

import workbench.interfaces.FormattableSql;
import workbench.resource.ResourceMgr;

/**
 *	Action to clear the contents of a entry field
 *	@author  info@sql-workbench.net
 */
public class FormatSqlAction extends WbAction
{
	private FormattableSql client;

	public FormatSqlAction(FormattableSql aClient)
	{
		super();
		this.client = aClient;
		this.initMenuDefinition("MnuTxtReformatSql",KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));
		this.setIcon(ResourceMgr.getImage("format"));
		this.setMenuItemName(ResourceMgr.MNU_TXT_SQL);
		this.setCreateToolbarSeparator(true);
	}

	public void executeAction(ActionEvent e)
	{
		this.client.reformatSql();
	}
}