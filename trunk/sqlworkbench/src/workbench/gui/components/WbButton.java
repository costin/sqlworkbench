/*
 * WbButton.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2006, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.components;

import javax.swing.Action;
import javax.swing.JButton;

/**
 *
 * @author  thomas
 */
public class WbButton
	extends JButton
{
	
	public WbButton()
	{
		super();
		init();
	}
	
	public WbButton(Action a)
	{
		super(a);
		init();
	}
	
	public WbButton(String aText)
	{
		super(aText);
		init();
	}
	
	private void init()
	{
		putClientProperty("jgoodies.isNarrow", Boolean.FALSE);
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


