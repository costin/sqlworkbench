/*
 * ClearAction.java
 *
 * Created on December 2, 2001, 1:32 AM
 */
package workbench.gui.actions;

import workbench.interfaces.ClipboardSupport;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import workbench.resource.ResourceMgr;
import javax.swing.Action;

/**
 *	Action to clear the contents of a entry field
 *	@author  thomas.kellerer@web.de
 */
public class ClearAction
	extends AbstractAction
{
	private ClipboardSupport client;
	
	public ClearAction(ClipboardSupport aClient)
	{
		this.client = aClient;
		this.putValue(Action.NAME, ResourceMgr.getString(ResourceMgr.TXT_CLEAR));
    this.putValue(ACTION_COMMAND_KEY, "ClearAction");
		this.putValue(WbActionConstants.MAIN_MENU_ITEM, ResourceMgr.MNU_TXT_EDIT);
	}
	
	public void actionPerformed(ActionEvent e)
	{
		this.client.clear();
	}
}
