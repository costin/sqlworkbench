/*
 * WbToolbarButton.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2007, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.components;

import java.awt.Insets;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import workbench.gui.WbSwingUtilities;

/**
 *
 * @author  support@sql-workbench.net
 */
public class WbToolbarButton 
	extends WbButton
{
	public static final Insets MARGIN = new Insets(1,1,1,1);
	
	public WbToolbarButton()
	{
		super();
		if (UIManager.getLookAndFeel() instanceof MetalLookAndFeel)
		{
			enableToolbarRollover();
		}
	}

	public WbToolbarButton(String aText)
	{
		super(aText);
		initMargin();
	}
	
	public WbToolbarButton(Action a)
	{
		super(a);
		this.setText(null);
		iconButton = true;
		initMargin();
	}

	public WbToolbarButton(Icon icon)
	{
		super(icon);
		this.setText(null);
	}
	
	public void setAction(Action a)
	{
		super.setAction(a);
		this.setText(null);
		initMargin();
	}
	
	private void initMargin()
	{
		this.setMargin(MARGIN);
	}
	
	public void setFlatLook()
	{
//		if (WbManager.getInstance().isWindowsClassic())
//		{
			this.setBorder(WbSwingUtilities.FLAT_BUTTON_BORDER);
//		}
	}
	
}