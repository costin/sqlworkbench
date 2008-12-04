/*
 * MacroManagerGui.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2008, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.macros;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import workbench.gui.WbSwingUtilities;
import workbench.gui.actions.DeleteListEntryAction;
import workbench.gui.actions.NewListEntryAction;
import workbench.gui.actions.SaveListFileAction;
import workbench.gui.components.WbSplitPane;
import workbench.gui.components.WbToolbar;
import workbench.gui.profiles.NewGroupAction;
import workbench.interfaces.FileActions;
import workbench.resource.Settings;
import workbench.sql.macros.MacroDefinition;
import workbench.sql.macros.MacroGroup;

/**
 * Displays all defined macros and lets the user add, edit and delete macros.
 * It uses {@link workbench.sql.macros.MacroManager} to retrieve and store
 * the macros.
 *
 * @author support@sql-workbench.net
 */
public class MacroManagerGui
	extends JPanel
	implements FileActions, TreeSelectionListener, PropertyChangeListener, TreeModelListener
{
	private JToolBar toolbar;
	private JSplitPane splitPane;
	private MacroDefinitionPanel macroPanel;
	private MacroGroupPanel groupPanel;
	private MacroTree macroTree;

	public MacroManagerGui()
	{
		super();
		this.macroTree = new MacroTree();
		this.setLayout(new BorderLayout());

		this.toolbar = new WbToolbar();
		this.toolbar.add(new NewListEntryAction(this));
		this.toolbar.add(new NewGroupAction(macroTree, "LblNewMacroGroup"));
		this.toolbar.addSeparator();

		DeleteListEntryAction deleteAction = new DeleteListEntryAction(this);
		this.toolbar.add(deleteAction);
		this.toolbar.addSeparator();
		this.toolbar.add(new SaveListFileAction(this));

		macroTree.setDeleteAction(deleteAction);

		JPanel treePanel = new JPanel();
		treePanel.setLayout(new BorderLayout());
		treePanel.add(this.toolbar, BorderLayout.NORTH);

		splitPane = new WbSplitPane();
		splitPane.setDividerLocation(140);

		JScrollPane scroll = new JScrollPane(this.macroTree);
		scroll.setBorder(WbSwingUtilities.EMPTY_BORDER);
		treePanel.add(scroll, java.awt.BorderLayout.CENTER);

		splitPane.setLeftComponent(treePanel);

		macroPanel = new MacroDefinitionPanel(this);
		groupPanel = new MacroGroupPanel(this);
		
		splitPane.setRightComponent(macroPanel);

		add(splitPane, BorderLayout.CENTER);
		macroTree.addTreeSelectionListener(this);
	}

	public MacroDefinition getSelectedMacro()
	{
		return macroTree.getSelectedMacro();
	}

	public void deleteItem()
		throws Exception
	{
		macroTree.deleteSelection();
		macroTree.repaint();
	}

	public void newItem(boolean copyCurrent) throws Exception
	{
		boolean ok = macroTree.addMacro(copyCurrent);
		if (ok)
		{
			macroPanel.selectMacroName();
		}
	}

	private void selectListLater()
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				macroTree.requestFocusInWindow();
			}
		});
	}

	public void saveItem() throws Exception
	{
		macroPanel.applyChanges();
		macroTree.saveChanges();
	}
	
	public void saveSettings()
	{
		int location = this.splitPane.getDividerLocation();
		Settings.getInstance().setProperty(this.getClass().getName() + ".divider", location);
		MacroDefinition macro = getSelectedMacro();
		Settings.getInstance().setProperty(this.getClass().getName() + ".lastmacro", macro == null ? "" : macro.getName());
		MacroGroup group = macroTree.getGroupForSelectedMacro();
		Settings.getInstance().setProperty(this.getClass().getName() + ".lastmacrogroup", group == null ? "" : group.getName());
	}

	public void restoreSettings()
	{
		int location = Settings.getInstance().getIntProperty(this.getClass().getName() + ".divider", 140);
		this.splitPane.setDividerLocation(location);
		String macro = Settings.getInstance().getProperty(this.getClass().getName() + ".lastmacro", null);
		String group = Settings.getInstance().getProperty(this.getClass().getName() + ".lastmacrogroup", null);
		this.selectMacro(group, macro);
		this.selectListLater();
	}

	private void selectMacro(String group, String macro)
	{
		macroTree.selectMacro(group, macro);
	}

	private void showGroup(final MacroGroup group)
	{
		macroPanel.setMacro(null);
		groupPanel.setMacroGroup(group);
		changePanel(groupPanel);
	}
	
	private void showMacro(final MacroDefinition entry)
	{
		groupPanel.setMacroGroup(null);
		macroPanel.setMacro(entry);
		changePanel(macroPanel);
	}

	private void changePanel(JPanel newPanel)
	{
		int location = splitPane.getDividerLocation();
		splitPane.setRightComponent(newPanel);
		splitPane.setDividerLocation(location);
	}
	
	public void treeNodesChanged(TreeModelEvent e)
	{
		
	}

	public void treeNodesInserted(TreeModelEvent e)
	{

	}

	public void treeNodesRemoved(TreeModelEvent e)
	{

	}

	public void treeStructureChanged(TreeModelEvent e)
	{

	}


	public void propertyChange(java.beans.PropertyChangeEvent evt)
	{
		this.macroTree.repaint();
	}

	public void valueChanged(TreeSelectionEvent e)
	{
		TreePath path = e.getPath();
		if (path == null) return;
		MacroTreeNode node = (MacroTreeNode)path.getLastPathComponent();
		if (node.getAllowsChildren())
		{
			MacroGroup group = (MacroGroup)node.getDataObject();
			showGroup(group);
		}
		else
		{
			MacroDefinition macro = (MacroDefinition)node.getDataObject();
			showMacro(macro);
		}
	}
}
