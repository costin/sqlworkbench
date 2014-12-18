/*
 * ToolbarSeperator.java
 *
 * Created on 12. Juli 2002, 00:37
 */

package workbench.gui.components;

import java.awt.Dimension;
import javax.swing.JPanel;

/**
 *
 * @author  workbench@kellerer.org
 */
public class WbToolbarSeparator 
	extends JPanel
{

	/** Creates a new instance of ToolbarSeperator */
	public WbToolbarSeparator()
	{
		Dimension d = new Dimension(9, 18);
		this.setPreferredSize(d);
		this.setMinimumSize(d);
		this.setMaximumSize(new Dimension(9, 24));
		this.setBorder(new DividerBorder(DividerBorder.VERTICAL_MIDDLE));
	}

}