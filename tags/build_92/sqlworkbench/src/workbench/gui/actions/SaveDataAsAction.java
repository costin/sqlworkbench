/*
 * SaveDataAsAction.java
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
import workbench.gui.components.WbTable;
import workbench.gui.dialogs.export.DataStoreExporter;

import workbench.resource.ResourceMgr;

/**
 *	Action to copy the contents of a entry field into the clipboard
 *	@author  support@sql-workbench.net
 */
public class SaveDataAsAction 
	extends WbAction
{
	private WbTable client;

	public SaveDataAsAction(WbTable aClient)
	{
		super();
		this.client = aClient;
		this.initMenuDefinition("MnuTxtSaveDataAs");
		this.setIcon(ResourceMgr.getImage("SaveAs"));
		this.setMenuItemName(ResourceMgr.MNU_TXT_DATA);
		this.setEnabled(false);
	}

	public void executeAction(ActionEvent e)
	{
		if (client.getDataStore().getUpdateTable() == null)
		{
			client.detectDefinedPkColumns();
		}
		DataStoreExporter exporter = new DataStoreExporter(this.client.getDataStore(), this.client);
		exporter.saveAs();
	}
}