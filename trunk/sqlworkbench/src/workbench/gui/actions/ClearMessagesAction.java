/*
 * ClearCompletionCacheAction.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2008, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.actions;

import java.awt.event.ActionEvent;
import workbench.interfaces.ResultLogger;
import workbench.resource.ResourceMgr;

/**
 * Action to clear the contents of the message display
 *
 * @author  support@sql-workbench.net
 */
public class ClearMessagesAction
	extends WbAction
{
	private ResultLogger logdisplay;
	
	public ClearMessagesAction(ResultLogger log)
	{
		super();
		logdisplay = log;
		this.initMenuDefinition("MnuTxtClearLog");
		this.setMenuItemName(ResourceMgr.MNU_TXT_EDIT);
		this.setEnabled(true);
	}

	public void executeAction(ActionEvent e)
	{
		if (logdisplay != null)
		{
			logdisplay.clearLog();
		}
	}
}
