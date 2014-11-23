/*
 * MoveSqlTabRight.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2009, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.actions;

import java.awt.event.ActionEvent;
import workbench.gui.MainWindow;
import workbench.resource.ResourceMgr;

/**
 *	@author  support@sql-workbench.net
 */
public class MoveSqlTabRight extends WbAction
{
	private MainWindow client;
	
	public MoveSqlTabRight(MainWindow aClient)
	{
		super();
		this.client = aClient;
		this.initMenuDefinition("MnuTxtMoveTabRight");
		this.setIcon(null);
	}

	public void executeAction(ActionEvent e)
	{
		this.client.moveTabRight();
	}
}