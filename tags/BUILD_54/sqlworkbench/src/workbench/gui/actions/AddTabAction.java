package workbench.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import workbench.gui.MainWindow;
import workbench.resource.ResourceMgr;

/**
 *	@author  workbench@kellerer.org
 */
public class AddTabAction extends WbAction
{
	private MainWindow client;

	public AddTabAction(MainWindow aClient)
	{
		super();
		this.client = aClient;
		this.putValue(Action.NAME, ResourceMgr.getString("MnuTxtAddTab"));
		this.putValue(Action.SHORT_DESCRIPTION, ResourceMgr.getDescription("MnuTxtAddTab"));
		this.putValue(WbAction.MAIN_MENU_ITEM, ResourceMgr.MNU_TXT_VIEW);
		this.putValue(Action.SMALL_ICON, null);
	}

	public void actionPerformed(ActionEvent e)
	{
		this.client.addTab();
	}
}