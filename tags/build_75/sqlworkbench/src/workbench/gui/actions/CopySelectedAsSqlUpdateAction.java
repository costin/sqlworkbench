/*
 * CopySelectedAsSqlUpdateAction.java
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

import workbench.gui.components.WbTable;
import workbench.resource.ResourceMgr;

/**
 *	Action to copy the contents of the data as SQL update statements into the clipboard
 *	@author  info@sql-workbench.net
 */
public class CopySelectedAsSqlUpdateAction extends WbAction
{
	private WbTable client;
	
	public CopySelectedAsSqlUpdateAction(WbTable aClient)
	{
		super();
		this.client = aClient;
		this.initMenuDefinition("MnuTxtCopySelectedAsSqlUpdate",null);
		this.setMenuItemName(ResourceMgr.MNU_TXT_COPY_SELECTED);
		this.setIcon(null);
		this.setEnabled(false);
	}

	public void executeAction(ActionEvent e)
	{
		client.copyAsSqlUpdate(true);
	}

}
