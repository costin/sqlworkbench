/*
 * WbCheckBox.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2005, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: info@sql-workbench.net
 *
 */
package workbench.gui.components;

import javax.swing.JCheckBox;

/**
 *
 * @author  info@sql-workbench.net
 */
public class WbCheckBox
	extends JCheckBox
{
	
	/** Creates a new instance of WbCheckBox */
	public WbCheckBox()
	{
	}
	
	public WbCheckBox(String text)
	{
		super(text);
	}

	public void setText(String newText)
	{
		int pos = newText.indexOf('&');
		if (pos > -1)
		{
			char mnemonic = newText.charAt(pos + 1);
			newText = newText.substring(0, pos) + newText.substring(pos + 1);
			this.setMnemonic((int)mnemonic);
			super.setText(newText);
		}
		else
		{
			super.setText(newText);
		}
	}
	
}