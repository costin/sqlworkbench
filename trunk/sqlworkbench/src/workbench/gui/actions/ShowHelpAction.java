/*
 * ShowHelpAction.java
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
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import workbench.gui.help.HelpManager;
import workbench.resource.ResourceMgr;

/**
 * @author support@sql-workbench.net
 */
public class ShowHelpAction
	extends WbAction
{
	public ShowHelpAction()
	{
		super();
		initMenuDefinition("MnuTxtHelpContents",KeyStroke.getKeyStroke(KeyEvent.VK_F1,0));
		setIcon(ResourceMgr.getImage("help"));
	}
	
	public synchronized void executeAction(ActionEvent e)
	{
		HelpManager.showHelpIndex();
	}
	
}
