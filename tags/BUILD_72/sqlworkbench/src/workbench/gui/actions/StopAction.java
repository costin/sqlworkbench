/*
 * ClearAction.java
 *
 * Created on December 2, 2001, 1:32 AM
 */
package workbench.gui.actions;

import java.awt.event.ActionEvent;

import workbench.interfaces.Interruptable;
import workbench.resource.ResourceMgr;

/**
 *	Action to copy the contents of a entry field into the clipboard
 *	@author  workbench@kellerer.org
 */
public class StopAction extends WbAction
{
	private Interruptable panel;

	public StopAction(Interruptable aPanel)
	{
		super();
		this.panel = aPanel;
		this.initMenuDefinition(ResourceMgr.TXT_STOP_STMT);
		this.setIcon(ResourceMgr.getImage("Stop"));
		this.setMenuItemName(ResourceMgr.MNU_TXT_SQL);
		this.putValue(WbAction.ADD_TO_TOOLBAR, "true");
		this.setCreateMenuSeparator(true);
	}

	public void executeAction(ActionEvent e)
	{
		this.panel.cancelExecution();
	}
}
