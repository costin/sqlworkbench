/*
 * AboutAction.java
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
import workbench.WbManager;

/**
 * Action to display program version information
 * @see workbench.gui.dialogs.WbAboutDialog
 * @author Thomas Kellerer
 */
public class AboutAction
	extends WbAction
{
	public AboutAction()
	{
		super();
		initMenuDefinition("MnuTxtAbout");
		removeIcon();
	}
	
	public void executeAction(ActionEvent e)
	{
		WbManager.getInstance().showDialog("workbench.gui.dialogs.WbAboutDialog");
	}
}
