/*
 * MainWindowTest.java
 * 
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 * 
 * Copyright 2002-2007, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author.
 * 
 * To contact the author please send an email to: support@sql-workbench.net
 */
package workbench.gui;

import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import junit.framework.TestCase;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JMenuOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.jemmy.operators.Operator.StringComparator;
import workbench.gui.sql.SqlPanel;
import workbench.util.StringUtil;
import workbench.util.WbFile;

/**
 * @author support@sql-workbench.net
 */
public class MainWindowTest
	extends TestCase
{
	private GuiTestUtil testUtil;

	public MainWindowTest(String testName)
	{
		super(testName);
		this.testUtil = new GuiTestUtil("MainWindowTest");
	}

	private void startApplication()
	{
		try
		{
			testUtil.startApplication();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	private void settingsTest()
	{
		JFrameOperator mainWindow = new JFrameOperator("SQL Workbench");
		new JMenuBarOperator(mainWindow).pushMenuNoBlock("Tools|Options", "|");
		JDialogOperator dialog = new JDialogOperator(mainWindow, "Settings");
		final JListOperator pages = new JListOperator(dialog);

		int count = pages.getModel().getSize();
		assertEquals(10, count);

		NamedComponentChooser chooser = new NamedComponentChooser();
		chooser.setName("pagetitle");

		QueueTool tool = new QueueTool();
		for (int i = 1; i < count; i++)
		{
			final int index = i;
			pages.selectItem(index);
			tool.waitEmpty();

			String pg = pages.getSelectedValue().toString();
			JLabelOperator title = new JLabelOperator(dialog, chooser);
			assertEquals(pg, title.getText());
		}

		new JButtonOperator(dialog, "Cancel").push();
	}

	private void createDriver()
	{
		JFrameOperator mainWindow = new JFrameOperator("SQL Workbench");
		new JMenuBarOperator(mainWindow).pushMenuNoBlock("File|Manage Drivers", "|");
		JDialogOperator dialog = new JDialogOperator(mainWindow, "Manage drivers");
		JListOperator list = new JListOperator(dialog);
		list.selectItem("H2 Database Engine");
		new JButtonOperator(dialog, "Cancel").push();
	}

	private void connect()
	{
		JFrameOperator mainWindow = new JFrameOperator("SQL Workbench");
		JMenuBar bar = mainWindow.getJMenuBar();
		//		JMenuBarOperator mainMenu = new JMenuBarOperator(mainWindow);
		JMenuBarOperator menu = new JMenuBarOperator(bar);
		menu.pushMenuNoBlock("File|Connect", "|");

		JDialogOperator dialog = new JDialogOperator(mainWindow, "Select Connection Profile");
		JTextFieldOperator profileName = new JTextFieldOperator(dialog, "New Profile");
		profileName.setText("Test Connection");

		NamedComponentChooser chooser = new NamedComponentChooser();
		chooser.setName("driverclass");

		JComboBoxOperator driver = new JComboBoxOperator(dialog, chooser);
		driver.setToolTipText("test");
		StringComparator comp = new DefaultStringComparator(false, false);

		int index = driver.findItemIndex("H2 Database Engine", comp);
		if (index <= 0) fail("H2 Driver not found");
		driver.selectItem(index);

		chooser.setName("url");
		JTextFieldOperator url = new JTextFieldOperator(dialog, chooser);
		WbFile db = new WbFile(testUtil.getBaseDir(), "testdb");
		url.setText("jdbc:h2:" + db.getFullPath());

		chooser.setName("username");
		JTextFieldOperator username = new JTextFieldOperator(dialog, chooser);
		username.setText("sa");
		new JButtonOperator(dialog, "OK").push();
	}

	public void runSql()
	{
		NamedComponentChooser chooser = new NamedComponentChooser();
		chooser.setName("sqleditor1");
		JFrameOperator mainWindow = new JFrameOperator("SQL Workbench");
		JComponentOperator editorComp = new JComponentOperator(mainWindow, chooser);
		JMenuBarOperator mainMenu = new JMenuBarOperator(mainWindow);

		chooser.setName("sqlpanel1");
		JComponentOperator panel = new JComponentOperator(mainWindow, chooser);
		final SqlPanel sqlPanel = (SqlPanel)panel.getSource();

		String msg = runSql(sqlPanel, "create table person (nr integer primary key, firstname varchar(20), lastname varchar(20));");
		System.out.println("Create message: " + msg);
		assertTrue(msg.indexOf("Object created") > -1);

		msg = runSql(sqlPanel, "insert into person (nr, firstname, lastname) values (42, 'Ford', 'Prefect');\ncommit;");
		assertTrue(msg.indexOf("1 row(s) affected") > -1);

		runSql(sqlPanel, "select nr, firstname, lastname from person");

		JTableOperator result = new JTableOperator(mainWindow);
		int rows = result.getRowCount();
		assertEquals(1, rows);
		assertEquals(3, result.getColumnCount());

		Object nr = result.getValueAt(0, 0);
		assertEquals(nr, new Integer(42));

		JMenuOperator dataMenu = new JMenuOperator(mainMenu.getMenu(3));
		JMenuItem saveItem = (JMenuItem)dataMenu.getMenuComponent(1);
		JMenuItemOperator save = new JMenuItemOperator(saveItem);
		assertFalse(save.isEnabled());

		result.setValueAt("Arthur", 0, 1);

		// The first call to setValueAt() will make the result table display
		// the status column
		assertEquals(4, result.getColumnCount());

		// because of the status column the lastname column 
		// is the column with index 3 (not 2)
		result.setValueAt("Dent", 0, 3);

		assertTrue(save.isEnabled());

		saveChanges(sqlPanel);

		// Make sure the status column is turned off after saving
		assertEquals(3, result.getColumnCount());

		runSql(sqlPanel, "select nr, firstname, lastname from person where lastname = 'Dent';");

		// Obtain a new referenct to the result table as the 
		// SQLPanel has created a new instance when running the select
		result = new JTableOperator(mainWindow);
		assertEquals(1, result.getRowCount());
		assertEquals(3, result.getColumnCount());

		String firstname = (String)result.getValueAt(0, 1);
		assertEquals("Arthur", firstname);

		msg = runSql(sqlPanel, "update person set firstname = null where nr = 42;\ncommit;");
		System.out.println("update message: " + msg);
		assertTrue(msg.indexOf("1 row(s) affected") > -1);

		msg = runSql(sqlPanel, "select nr, firstname, lastname from person where lastname = 'Dent';");
		System.out.println("Message: " + msg);

		result = new JTableOperator(mainWindow);
		firstname = (String)result.getValueAt(0, 1);
		assertTrue(StringUtil.isWhitespaceOrEmpty(firstname));

		result.setValueAt("Arthur", 0, 1);
		new QueueTool().waitEmpty();

		msg = saveChanges(sqlPanel);
		System.out.println("Message: " + msg);
	}

	private void execute(Runnable r)
	{
		QueueTool tool = new QueueTool();
		tool.invokeAndWait(r);
		tool.waitEmpty();
	}

	private String saveChanges(final SqlPanel panel)
	{
		Runnable r = new Runnable()
			{

				public void run()
				{
					panel.updateDb();
				}
			};
		execute(r);
		waitFor(panel);
		return panel.getLogMessage();
	}

	private String runSql(final SqlPanel panel, final String sql)
	{
		Runnable r = new Runnable()
			{

				public void run()
				{
					panel.getEditor().setText(sql);
					panel.runAll();
				}
			};
		execute(r);
		waitFor(panel);
		return panel.getLogMessage();
	}

	private void waitFor(SqlPanel panel)
	{
		while (panel.isBusy())
		{
			Thread.yield();
		}
	}

	public void testWindow()
	{
		try
		{
			startApplication();
			connect();
			settingsTest();
			runSql();
			testUtil.stopApplication();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}