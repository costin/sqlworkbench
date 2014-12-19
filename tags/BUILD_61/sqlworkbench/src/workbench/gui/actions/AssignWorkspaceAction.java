package workbench.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import workbench.gui.MainWindow;
import workbench.gui.WbSwingUtilities;
import workbench.gui.profiles.DriverEditorDialog;
import workbench.resource.ResourceMgr;

/**
 *	@author  workbench@kellerer.org
 */
public class AssignWorkspaceAction extends WbAction
{
	private MainWindow client;

	public AssignWorkspaceAction(MainWindow aClient)
	{
		super();
		this.client = aClient;
		this.putValue(Action.NAME, ResourceMgr.getString("MnuTxtAssignWorkspace"));
		this.putValue(Action.SHORT_DESCRIPTION, ResourceMgr.getDescription("MnuTxtAssignWorkspace"));
		this.putValue(WbAction.MAIN_MENU_ITEM, ResourceMgr.MNU_TXT_WORKSPACE);
		this.putValue(Action.SMALL_ICON, null);
	}

	public void actionPerformed(ActionEvent e)
	{
		this.client.assignWorkspace();
	}
	
}