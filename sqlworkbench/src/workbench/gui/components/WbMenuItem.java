/*
 * Created on 11. August 2002, 15:02
 */
package workbench.gui.components;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author  workbench@kellerer.org
 */
public class WbMenuItem 
	extends JMenuItem
{
	//private int additionalVerticalSpace = 0;
	Border originalBorder;
	
	public WbMenuItem()
	{
		super();
		this.checkSpacing();
	}
	
	public WbMenuItem(String aText)
	{
		super(aText);
		this.checkSpacing();
	}

	public WbMenuItem(Action anAction)
	{
		super(anAction);
		this.checkSpacing();
	}

	public WbMenuItem(String text, int mnemonic) 	
	{
		super(text, mnemonic);
		this.checkSpacing();
	}
	
	public WbMenuItem(Icon icon) 
	{
		super(icon);
		this.checkSpacing();
	}
	public WbMenuItem(String text, Icon icon) 
	{
		super(text, icon);
		this.checkSpacing();
	}

	public void updateUI()
	{
		super.updateUI();
		this.checkSpacing();
	}
  
	private void checkSpacing()
	{
		LookAndFeel lnf = UIManager.getLookAndFeel();	
		if (lnf.getClass() == com.sun.java.swing.plaf.windows.WindowsLookAndFeel.class)
		{
			if (this.originalBorder == null)
			{
				this.originalBorder = this.getBorder();
			}
			EmptyBorder e = new EmptyBorder(0,0,2,0);
			Border nb = new CompoundBorder(this.originalBorder, e);
			this.setBorder(nb);
		}
		else if (this.originalBorder != null)
		{
			this.setBorder(this.originalBorder);
		}
	}

	public void removeExtraSpacing()
	{
		if (this.originalBorder != null)
		{
			this.setBorder(this.originalBorder);
		}	
	}
	
	/*
	public Dimension getPreferredSize()
	{
		Dimension pref = super.getPreferredSize();
    Component parent = this.getParent();
    if (!(parent instanceof JPopupMenu))
    {
  		pref.height += this.additionalVerticalSpace;
    }
		return pref;
	}
	*/
	
	public void setText(String aText)
	{
		if (aText == null) return;
		int pos = aText.indexOf('&');
		if (pos > -1)
		{
			char mnemonic = aText.charAt(pos + 1);
			if (mnemonic != ' ')
			{
				aText = aText.substring(0, pos) + aText.substring(pos + 1);
			}
			super.setText(aText);
			if (mnemonic != ' ' && mnemonic != '&')
			{
				this.setMnemonic((int)mnemonic);
				try
				{
					this.setDisplayedMnemonicIndex(pos);
				}
				catch (Exception e)
				{
				}
			}
		}
		else
		{
			super.setText(aText);
		}
	}	

}
