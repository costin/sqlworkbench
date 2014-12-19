package workbench.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import workbench.interfaces.Searchable;
import workbench.resource.ResourceMgr;

/**
 *	Action to clear the contents of a entry field
 *	@author  workbench@kellerer.org
 */
public class FindDataAction extends WbAction
{
	private Searchable client;

	public FindDataAction(Searchable aClient)
	{
		super();
		this.client = aClient;
		this.initMenuDefinition("MnuTxtFindData", KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
		this.setIcon(ResourceMgr.getImage(ResourceMgr.IMG_FIND));
		this.setMenuItemName(ResourceMgr.MNU_TXT_DATA);
		this.setCreateToolbarSeparator(true);
	}

	public void executeAction(ActionEvent e)
	{
		this.client.find();
	}
}