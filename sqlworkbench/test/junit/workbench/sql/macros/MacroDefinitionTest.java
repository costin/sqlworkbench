/*
 * MacroDefinitionTest.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2012, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.sql.macros;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import workbench.resource.StoreableKeyStroke;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Thomas Kellerer
 */
public class MacroDefinitionTest
{

	@Test
	public void testCreateCopy()
	{
		MacroDefinition macro = new MacroDefinition("test", "select 42 from dual");
		assertTrue(macro.isVisibleInMenu());
		macro.setVisibleInMenu(false);
		macro.setExpandWhileTyping(true);
		macro.setSortOrder(5);
		macro.setAppendResult(true);
		StoreableKeyStroke key = new StoreableKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));
		macro.setShortcut(key);

		MacroDefinition copy = macro.createCopy();
		assertTrue(copy.getExpandWhileTyping());
		StoreableKeyStroke key2 = copy.getShortcut();
		assertEquals(key, key2);
		assertTrue(copy.isAppendResult());
		assertFalse(copy.isVisibleInMenu());
		assertFalse(copy.isModified());
		assertEquals(5, copy.getSortOrder());
		assertEquals(macro.getName(), copy.getName());
		assertEquals(macro.getText(), copy.getText());
	}

	@Test
	public void testModified()
	{
		MacroDefinition macro = new MacroDefinition("test", "select 42 from dual");
		assertFalse(macro.isModified());
		macro.setExpandWhileTyping(true);
		assertTrue(macro.isModified());

		macro.resetModified();
		macro.setAppendResult(true);
		assertTrue(macro.isModified());

		macro.resetModified();
		macro.setVisibleInMenu(false);
		assertTrue(macro.isModified());
	}
}
