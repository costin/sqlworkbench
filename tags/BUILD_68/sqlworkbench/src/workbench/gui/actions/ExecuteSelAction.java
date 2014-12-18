package workbench.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import workbench.resource.ResourceMgr;

/**
 *	@author  workbench@kellerer.org
 */
public class ExecuteSelAction extends WbAction
{
	private ActionListener target;
	
	public ExecuteSelAction(ActionListener aListener)
	{
		super();
		this.target = aListener;
		this.initMenuDefinition(ResourceMgr.TXT_EXECUTE_SEL, KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));
		this.setIcon(ResourceMgr.getImage(ResourceMgr.IMG_EXEC_SEL));
		this.setMenuItemName(ResourceMgr.MNU_TXT_SQL);
		this.putValue(WbAction.ADD_TO_TOOLBAR, "true");
		this.putValue(ALTERNATE_ACCELERATOR, KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0));
	}

	public void executeAction(ActionEvent e)
	{
		this.target.actionPerformed(e);
	}
	
}