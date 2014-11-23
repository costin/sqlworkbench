package workbench.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import workbench.interfaces.Commitable;
import workbench.resource.ResourceMgr;

/**
 *	Action to clear the contents of a entry field
 *	@author  workbench@kellerer.org
 */
public class RollbackAction extends WbAction
{
	private Commitable client;
	public RollbackAction(Commitable aClient)
	{
		super();
		this.client = aClient;
		this.initMenuDefinition("MnuTxtRollback", KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.ALT_MASK));
		this.setMenuItemName(ResourceMgr.MNU_TXT_SQL);
		this.setIcon(ResourceMgr.getImage("Rollback"));
	}

	public void executeAction(ActionEvent e)
	{
		if (this.client != null) this.client.rollback();
	}
	
}