package workbench.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import workbench.resource.ResourceMgr;

/**
 *	@author  workbench@kellerer.org
 */
public class ExecuteCurrentAction extends WbAction
{
	private ActionListener target;
	
	public ExecuteCurrentAction(ActionListener aListener)
	{
		super();
		this.target = aListener;
		this.putValue(Action.NAME, ResourceMgr.getString("MnuTxtExecuteCurrent"));
		this.putValue(Action.SHORT_DESCRIPTION, ResourceMgr.getDescription("MnuTxtExecuteCurrent"));
		//this.putValue(Action.SMALL_ICON, ResourceMgr.getImage(ResourceMgr.IMG_EXEC_SEL));
		this.putValue(WbAction.MAIN_MENU_ITEM, ResourceMgr.MNU_TXT_SQL);
		this.putValue(WbAction.ADD_TO_TOOLBAR, "false");
		this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_MASK));
		//this.putValue(ALTERNATE_ACCELERATOR, KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0));
	}

	public void actionPerformed(ActionEvent e)
	{
		this.target.actionPerformed(e);
	}
	
}
