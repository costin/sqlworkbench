/*
 * ReloadAction.java
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

import workbench.interfaces.Reloadable;
import workbench.resource.ResourceMgr;

/**
 *	@author  support@sql-workbench.net
 */
public class ReloadAction extends WbAction
{
	private Reloadable client;
	private boolean ctrlPressed;
	
	public ReloadAction(Reloadable aClient)
	{
		super();
		this.client = aClient;
		this.setMenuTextByKey("TxtReload");
		this.setIcon(ResourceMgr.getImage("Refresh"));
		//this.setMenuItemName(ResourceMgr.MNU_TXT_DATA);
	}

	public boolean ctrlPressed()
	{
		return ctrlPressed;
	}
	
	public void executeAction(ActionEvent e)
	{
		this.ctrlPressed = ((e.getModifiers() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK);
		this.client.reload();
	}
	
}