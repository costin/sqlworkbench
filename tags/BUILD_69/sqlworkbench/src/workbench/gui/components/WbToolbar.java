/*
 * WbToolbar.java
 *
 * Created on 16. Juli 2002, 12:33
 */

package workbench.gui.components;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import workbench.gui.actions.WbAction;

/**
 *
 * @author  workbench@kellerer.org
 */
public class WbToolbar extends JToolBar
{

	/** Creates a new instance of WbToolbar */
	public WbToolbar()
	{
		this.setFloatable(false);
		this.setRollover(true);
	}

	public JButton add(Action a)
	{
		JButton button;

		if (a instanceof WbAction)
		{
			button = ((WbAction)a).getToolbarButton();
		}
		else
		{
			button = new WbToolbarButton(a);
		}
		this.add(button);
		return button;
	}

	public JButton add(WbAction a)
	{
		JButton button = a.getToolbarButton();
		this.add(button);
		return button;
	}

	public void addSeparator()
	{
		this.addSeparator(this.getComponentCount());
	}
	public void addSeparator(int index)
	{
		if (isRollover())
			this.add(new WbToolbarSeparator(), index);
		else
			super.addSeparator();
	}
	
	public void addDefaultBorder()
	{
		Border b = new CompoundBorder(new EmptyBorder(1,0,1,0), new EtchedBorder());
		this.setBorder(b);
		this.setBorderPainted(true);
	}
}
