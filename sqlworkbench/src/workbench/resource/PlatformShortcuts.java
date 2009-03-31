/*
 * PlatformShortcuts.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2009, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.resource;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import workbench.util.PlatformHelper;

/**
 * Centralize the definition of default keyboard shortcuts for MacOS and the rest of the world.
 *
 * KeyEvent.META_MASK is the "Command" (or "Apple") Key<br/>
 * KeyEvent.CTRL_MASK is the "Option" key<br/>
 * 
 * @author support@sql-workbench.net
 */
public class PlatformShortcuts
{
	public static KeyStroke getDefaultCopyShortcut()
	{
		return KeyStroke.getKeyStroke(KeyEvent.VK_C, getDefaultModifier());
	}

	public static KeyStroke getDefaultCutShortcut()
	{
		return KeyStroke.getKeyStroke(KeyEvent.VK_X, getDefaultModifier());
	}

	public static KeyStroke getDefaultPasteShortcut()
	{
		return KeyStroke.getKeyStroke(KeyEvent.VK_V, getDefaultModifier());
	}

	public static int getDefaultModifier()
	{
		return Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
	}

	public static KeyStroke getDefaultEndOfLine(boolean select)
	{
		if (PlatformHelper.isMacOS())
		{
			return KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.META_MASK | (select ? KeyEvent.SHIFT_MASK : 0) );
		}
		return KeyStroke.getKeyStroke(KeyEvent.VK_END, 0);
	}

	public static KeyStroke getDefaultStartOfLine(boolean select)
	{
		if (PlatformHelper.isMacOS())
		{
			return KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.META_MASK | (select ? KeyEvent.SHIFT_MASK : 0) );
		}
		return KeyStroke.getKeyStroke(KeyEvent.VK_HOME, (select ? KeyEvent.SHIFT_MASK : 0));
	}

	public static KeyStroke getDefaultStartOfDoc(boolean select)
	{
		if (PlatformHelper.isMacOS())
		{
			return KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.META_MASK | (select ? KeyEvent.SHIFT_MASK : 0) );
		}
		return KeyStroke.getKeyStroke(KeyEvent.VK_HOME, KeyEvent.CTRL_MASK | (select ? KeyEvent.SHIFT_MASK : 0));
	}

	public static KeyStroke getDefaultEndOfDoc(boolean select)
	{
		if (PlatformHelper.isMacOS())
		{
			return KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.META_MASK | (select ? KeyEvent.SHIFT_MASK : 0) );
		}
		return KeyStroke.getKeyStroke(KeyEvent.VK_END, KeyEvent.CTRL_MASK | (select ? KeyEvent.SHIFT_MASK : 0));
	}

}
