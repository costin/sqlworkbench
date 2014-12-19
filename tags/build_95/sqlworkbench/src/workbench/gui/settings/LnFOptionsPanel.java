/*
 * LnFOptionsPanel.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2007, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.settings;

import java.awt.BorderLayout;
import java.awt.FontMetrics;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import workbench.gui.actions.DeleteListEntryAction;
import workbench.gui.actions.NewListEntryAction;
import workbench.gui.components.DividerBorder;
import workbench.gui.components.WbSplitPane;
import workbench.gui.components.WbToolbar;
import workbench.gui.lnf.LnFDefinition;
import workbench.gui.lnf.LnFManager;
import workbench.interfaces.FileActions;
import workbench.interfaces.Restoreable;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;

/**
 *
 * @author support@sql-workbench.net
 */
public class LnFOptionsPanel 
	extends JPanel
	implements Restoreable, ListSelectionListener, FileActions, 
	           PropertyChangeListener
{
	private JList lnfList;
	private LnFDefinitionPanel definitionPanel;
	protected LnFManager manager = new LnFManager();
	private WbToolbar toolbar;
	
	public LnFOptionsPanel()
	{
		setLayout(new BorderLayout());
		
		lnfList = new JList();
		lnfList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lnfList.setBorder(new EmptyBorder(2,1,2,1));
		FontMetrics fm = getFontMetrics(getFont());
		int width= fm.stringWidth("MMMMMMMMMMMMMMM");
		lnfList.setFixedCellWidth(width);
		
		JScrollPane scroll = new JScrollPane(lnfList);

		this.toolbar = new WbToolbar();
		this.toolbar.add(new NewListEntryAction(this));
		this.toolbar.add(new DeleteListEntryAction(this));
		toolbar.setBorder(DividerBorder.BOTTOM_DIVIDER);
		
		definitionPanel = new LnFDefinitionPanel();
		definitionPanel.setPropertyListener(this);

		add(scroll, BorderLayout.WEST);
		add(toolbar, BorderLayout.NORTH);
		add(definitionPanel, java.awt.BorderLayout.CENTER);

		ListModel model = new LnfList();
		lnfList.setModel(model);
		lnfList.addListSelectionListener(this);
		LnFDefinition clnf = manager.getCurrentLnF();
		lnfList.setSelectedValue(clnf, true);
		definitionPanel.setCurrentLookAndFeeld(clnf);
		restoreSettings();
	}
	
	public void saveSettings()
	{
//		int divider = splitPane.getDividerLocation();
//		Settings.getInstance().setProperty(this.getClass().getName() + ".divider", divider);
		manager.saveLookAndFeelDefinitions();
	}
	
	public void restoreSettings()
	{
//		int divider = Settings.getInstance().getIntProperty(this.getClass().getName() + ".divider", 120);
//		splitPane.setDividerLocation(divider);
	}
	
	public void valueChanged(ListSelectionEvent evt)
	{
		int index = lnfList.getSelectedIndex();
		LnFDefinition def = (LnFDefinition)manager.getAvailableLookAndFeels().get(index);
		definitionPanel.setDefinition(def);
	}

	public void saveItem() throws Exception
	{
	}

	public void deleteItem() throws Exception
	{
		int index = lnfList.getSelectedIndex();
		if (index > -1)
		{
			manager.removeDefinition(index);
		}
		if (lnfList.getModel().getSize() == 0)
		{
			definitionPanel.setDefinition(null);
		}
		if (index >= lnfList.getModel().getSize())
		{
			index = lnfList.getModel().getSize() - 1;
		}
		lnfList.setSelectedIndex(index);
		valueChanged(null);
		lnfList.repaint();
	}

	public void newItem(boolean copyCurrent) throws Exception
	{
		try
		{
			LnFDefinition def = null;
			if (copyCurrent)
			{
				int index = lnfList.getSelectedIndex();
				LnFDefinition current = (LnFDefinition)manager.getAvailableLookAndFeels().get(index);
				def = current.createCopy();
			}
			else
			{
				String d = ResourceMgr.getString("TxtLnFSample");
				def = new LnFDefinition(d);
			}
			int index = manager.addDefinition(def);
			lnfList.setSelectedIndex(index);
			definitionPanel.setDefinition(def);
			lnfList.updateUI();
		}
		catch (Exception e)
		{
			LogMgr.logError("LnFOptionsPanel.newItem()", "Error creating new item", e);
		}
	}

	public void propertyChange(PropertyChangeEvent evt)
	{
		if (evt.getPropertyName().equals("name"))
		{
			lnfList.repaint();
		}
	}

	class LnfList 
		extends AbstractListModel
	{
		public Object getElementAt(int index)
		{
			return manager.getAvailableLookAndFeels().get(index);
		}

		public int getSize()
		{
			return manager.getAvailableLookAndFeels().size();
		}
	}
	
}