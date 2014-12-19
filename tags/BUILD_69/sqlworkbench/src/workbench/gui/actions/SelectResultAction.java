/*
 * ClearAction.java
 *
 * Created on December 2, 2001, 1:32 AM
 */
package workbench.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import workbench.gui.sql.SqlPanel;
import workbench.resource.ResourceMgr;

/**
 *	Action to copy the contents of a entry field into the clipboard
 *	@author  workbench@kellerer.org
 */
public class SelectResultAction extends WbAction
{
	private SqlPanel client;

	public SelectResultAction(SqlPanel aClient)
	{
		super();
		this.client = aClient;
		this.initMenuDefinition("MnuTxtSelectResult", KeyStroke.getKeyStroke(KeyEvent.VK_F6,0));
		this.setMenuItemName(ResourceMgr.MNU_TXT_EDIT);
	}

	public void executeAction(ActionEvent e)
	{
		this.client.selectResult();
	}
}