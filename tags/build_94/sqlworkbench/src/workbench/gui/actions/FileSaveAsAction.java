/*
 * FileSaveAsAction.java
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

import workbench.interfaces.TextFileContainer;
import workbench.resource.ResourceMgr;

/**
 * Save the current file in the SQL Editor with a new name.
 *	@author  support@sql-workbench.net
 */
public class FileSaveAsAction extends WbAction
{
	private TextFileContainer client;

	public FileSaveAsAction(TextFileContainer aClient)
	{
		super();
		this.client = aClient;
		this.initMenuDefinition("MnuTxtFileSaveAs");
		this.setMenuItemName(ResourceMgr.MNU_TXT_FILE);
	}

	public void executeAction(ActionEvent e)
	{
		this.client.saveFile();
	}
}