/*
 * WbToolbarButton.java
 *
 * Created on 10. Juli 2002, 10:37
 */

package workbench.gui.components;

import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.Action;

/**
 *
 * @author  sql.workbench@freenet.de
 */
public class WbToolbarButton extends javax.swing.JButton
{

	/** Creates a new instance of WbToolbarButton */
	public WbToolbarButton(Action a)
	{
		super(a);
		this.setText(null);
		//Dimension d = new Dimension(24,24);
		//this.setMaximumSize(d);
		//this.setPreferredSize(d);
		//this.setMinimumSize(d);
		Insets m = new Insets(1,1,1,1);
		this.setMargin(m);
		//this.setRolloverEnabled(true);
	}

}
