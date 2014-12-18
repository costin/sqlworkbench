/*
 * SortDescendingAction.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2005, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: info@sql-workbench.net
 *
 */
package workbench.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;

import workbench.gui.components.SortArrowIcon;

/**
 *	Action to copy the contents of a entry field into the clipboard
 *	@author  info@sql-workbench.net
 */
public class SortDescendingAction extends WbAction
{
	private ActionListener client;

	public SortDescendingAction(ActionListener aClient)
	{
		super();
		this.client = aClient;
		this.setMenuTextByKey("MnuTxtSortDescending");
		this.putValue(Action.SMALL_ICON, SortArrowIcon.ARROW_UP);
	}

	public void executeAction(ActionEvent e)
	{
		e.setSource(this);
		this.client.actionPerformed(e);
	}
}