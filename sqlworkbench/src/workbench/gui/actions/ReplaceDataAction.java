/*
 * FindDataAction.java
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
import workbench.interfaces.Replaceable;
import workbench.interfaces.Replaceable;
import workbench.resource.ResourceMgr;

/**
 *	Search and replace inside the result set
 * 
 *	@author  support@sql-workbench.net
 */
public class ReplaceDataAction extends WbAction
{
	private Replaceable client;

	public ReplaceDataAction(Replaceable aClient)
	{
		super();
		this.client = aClient;
		this.initMenuDefinition("MnuTxtReplaceInTableData");
		this.setMenuItemName(ResourceMgr.MNU_TXT_DATA);
		this.setCreateToolbarSeparator(false);
	}

	public void executeAction(ActionEvent e)
	{
		this.client.replace();
	}
}
