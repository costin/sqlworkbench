/*
 * FileDisconnectAction.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2011, Thomas Kellerer
 * No part of this code may be reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.actions;

import java.awt.event.ActionEvent;

import workbench.gui.MainWindow;

/**
 * Disconnect the current window
 * @author Thomas Kellerer
 */
public class FileDisconnectAction 
	extends WbAction
{
	private MainWindow window;

	public FileDisconnectAction(MainWindow aWindow)
	{
		super();
		this.window = aWindow;
		this.initMenuDefinition("MnuTxtDisconnect");
		setEnabled(false);
	}

	public void executeAction(ActionEvent e)
	{
		window.disconnect(true, true, true);
	}
}