/*
 * SaveListFileAction.java
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

import workbench.interfaces.FileActions;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;

/**
 *	Action to copy the contents of a entry field into the clipboard
 *	@author  support@sql-workbench.net
 */
public class SaveListFileAction extends WbAction
{
	private FileActions client;

	public SaveListFileAction(FileActions aClient)
	{
		this.client = aClient;
		this.setMenuTextByKey("LblSaveProfiles");
		this.setIcon(	ResourceMgr.getImage(ResourceMgr.IMG_SAVE));
	}

	public void executeAction(ActionEvent e)
	{
		try
		{
			this.client.saveItem();
		}
		catch (Exception ex)
		{
			LogMgr.logError(this, "Error saving profiles", ex);
		}
	}
}