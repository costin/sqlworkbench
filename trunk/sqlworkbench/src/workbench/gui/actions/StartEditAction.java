/*
 * ClearAction.java
 *
 * Created on December 2, 2001, 1:32 AM
 */
package workbench.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import workbench.gui.components.WbToolbarButton;
import workbench.interfaces.DbData;
import workbench.resource.ResourceMgr;
import javax.swing.SwingUtilities;
import workbench.util.WbThread;

/**
 *	Action to copy the contents of a entry field into the clipboard
 *	@author  workbench@kellerer.org
 */
public class StartEditAction
	extends WbAction
{
	private DbData client;
	private Border enabledBorder = new CompoundBorder(new BevelBorder(BevelBorder.LOWERED), new EmptyBorder(2,2,2,2));
	private Border originalBorder;

	private boolean switchedOn = false;
	private JToggleButton toggleButton;
	private JCheckBoxMenuItem toggleMenu;

	public StartEditAction(DbData aClient)
	{
		super();
		this.client = aClient;
		this.initMenuDefinition("MnuTxtStartEdit");
		this.setMenuItemName(ResourceMgr.MNU_TXT_DATA);
		this.setIcon(ResourceMgr.getImage("editor"));
		this.setEnabled(false);
	}

	public void executeAction(ActionEvent e)
	{
		this.setSwitchedOn(!this.switchedOn);
		Thread t = new WbThread("StartEdit")
		{
			public void run()
			{
				if (switchedOn)
					client.startEdit();
				else
					client.endEdit();
			}
		};
		t.start();
		/*
		if (this.switchedOn)
			this.client.startEdit();
		else
			this.client.endEdit();
		*/
	}

	public boolean isSwitchedOn() { return this.switchedOn; }

	public void setSwitchedOn(boolean aFlag)
	{
		this.switchedOn = aFlag;
		if (this.toggleMenu != null) this.toggleMenu.setSelected(aFlag);
		if (this.toggleButton != null)
		{
			this.toggleButton.setSelected(aFlag);
		}
	}

	public JToggleButton createButton()
	{
		this.toggleButton = new JToggleButton(this);
		this.toggleButton.setText(null);
		this.toggleButton.setMargin(WbToolbarButton.MARGIN);
		return this.toggleButton;
	}

	public void addToToolbar(JToolBar aToolbar)
	{
		if (this.toggleButton == null) this.createButton();
		aToolbar.add(this.toggleButton);
	}

	public void addToMenu(JMenu aMenu)
	{
		if (this.toggleMenu == null)
		{
			this.toggleMenu= new JCheckBoxMenuItem();
			this.toggleMenu.setAction(this);
			String text = this.getValue(Action.NAME).toString();
			int pos = text.indexOf('&');
			if (pos > -1)
			{
				char mnemonic = text.charAt(pos + 1);
				text = text.substring(0, pos) + text.substring(pos + 1);
				this.toggleMenu.setMnemonic((int)mnemonic);
			}
			this.toggleMenu.setText(text);
			this.toggleMenu.setIconTextGap(0);
			String lnf = UIManager.getLookAndFeel().getClass().getName();
			if (lnf.startsWith("com.jgoodies"))
			{
				this.toggleMenu.setIcon(null);
			}
			else
			{
				this.toggleMenu.setIcon(ResourceMgr.getImage("blank"));
			}
		}
		aMenu.add(this.toggleMenu);
	}
	public void setEnabled(boolean aFlag)
	{
		boolean last = this.isEnabled();
		super.setEnabled(aFlag);
		if (!this.enabled || (last != this.enabled))
			this.setSwitchedOn(false);
	}
}