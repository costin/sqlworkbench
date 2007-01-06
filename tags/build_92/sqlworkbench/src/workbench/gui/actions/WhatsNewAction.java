/*
 * WhatsNewAction.java
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

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import workbench.WbManager;
import workbench.gui.MainWindow;
import workbench.gui.help.WhatsNewViewer;

/**
 * @author support@sql-workbench.net
 */
public class WhatsNewAction
	extends WbAction
{
	private static WhatsNewAction instance = new WhatsNewAction();
	public static WhatsNewAction getInstance()
	{
		return instance;
	}
	
	private WhatsNewAction()
	{
		super();
		this.initMenuDefinition("MnuTxtWhatsNew");
		this.removeIcon();
	}

	public void executeAction(ActionEvent e)
	{
		WbManager.getInstance().showDialog("workbench.gui.help.WhatsNewViewer");
	}
	
}
