/*
 * FileSaveAsAction.java
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

import workbench.interfaces.TextFileContainer;
import workbench.resource.ResourceMgr;

/**
 *	@author  info@sql-workbench.net
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
