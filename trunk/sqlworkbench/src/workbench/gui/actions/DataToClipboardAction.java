/*
 * DataToClipboardAction.java
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
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;

import workbench.interfaces.Exporter;
import workbench.resource.ResourceMgr;
import workbench.util.StringUtil;

/**
 *	Action to copy the contents of a entry field into the clipboard
 *	@author  info@sql-workbench.net
 */
public class DataToClipboardAction extends WbAction
{
	private Exporter client;
	
	public DataToClipboardAction(Exporter aClient)
	{
		super();
		this.client = aClient;
			
		this.setMenuItemName(ResourceMgr.MNU_TXT_DATA);
		String desc = ResourceMgr.getDescription("MnuTxtDataToClipboard");
		
		String shift = KeyEvent.getKeyModifiersText(KeyEvent.SHIFT_MASK);
		desc = StringUtil.replace(desc, "%shift%", shift);
		
		this.initMenuDefinition(ResourceMgr.getString("MnuTxtDataToClipboard"), desc, KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
	}

	public void executeAction(ActionEvent e)
	{
		boolean shiftPressed = ((e.getModifiers() & ActionEvent.SHIFT_MASK) == ActionEvent.SHIFT_MASK);
		client.copyDataToClipboard(!shiftPressed, false); 
	}
	
	public void addToInputMap(InputMap im, ActionMap am)
	{
		super.addToInputMap(im, am);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK), this.getActionName());
	}
}
