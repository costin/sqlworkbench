/*
 * VariablesEditor.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2004, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: info@sql-workbench.net
 *
 */
package workbench.gui.sql;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.sql.Types;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.TableCellEditor;

import workbench.WbManager;
import workbench.gui.WbSwingUtilities;
import workbench.gui.actions.DeleteListEntryAction;
import workbench.gui.actions.NewListEntryAction;
import workbench.gui.components.DataStoreTableModel;
import workbench.gui.components.ValidatingDialog;
import workbench.gui.components.WbTable;
import workbench.gui.components.WbTextCellEditor;
import workbench.interfaces.FileActions;
import workbench.interfaces.ValidatingComponent;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.sql.SqlParameterPool;
import workbench.storage.DataStore;

/**
 *
 * @author  info@sql-workbench.net
 */
public class VariablesEditor 
	extends JPanel 
	implements FileActions, ValidatingComponent
{
	private DataStore varData;
	private WbTable variablesTable;

	private NewListEntryAction newItem;
	private DeleteListEntryAction deleteItem;

	public VariablesEditor(DataStore data)
	{
		String[] cols = new String[] { ResourceMgr.getString("TxtConnDataPropName"), ResourceMgr.getString("TxtConnDataPropValue") };
		int[] types = new int[] { Types.VARCHAR, Types.VARCHAR };
		int[] sizes = new int[] { 15, 5 };

		this.variablesTable = new WbTable();
		this.variablesTable.setRowSelectionAllowed(false);
		this.variablesTable.setColumnSelectionAllowed(false);
		this.varData = data;
		DataStoreTableModel model = new DataStoreTableModel(data);
		model.setLockedColumn(0);
		this.variablesTable.setModel(model);
		this.variablesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		JLabel l = new JLabel(ResourceMgr.getString("TxtVariableInputText"));
		Border b = BorderFactory.createEmptyBorder(5, 2, 5, 2);
		l.setBorder(b);
		l.setBackground(Color.WHITE);
		l.setOpaque(true);
		l.setHorizontalAlignment(SwingConstants.CENTER);
		
		this.setLayout(new BorderLayout());

		JScrollPane scroll = new JScrollPane(this.variablesTable);
		b = BorderFactory.createEmptyBorder(5, 0, 0, 0);
		Border b2 = BorderFactory.createCompoundBorder(b, scroll.getBorder());
		scroll.setBorder(b2);
		
		//WbToolbar toolbar = new WbToolbar();
		//toolbar.addDefaultBorder();
		this.newItem = new NewListEntryAction(this);
		this.deleteItem = new DeleteListEntryAction(this);

		//toolbar.add(this.newItem);
		//toolbar.add(this.deleteItem);
		//this.add(toolbar, BorderLayout.NORTH);
		this.add(l, BorderLayout.NORTH);
		this.add(scroll, BorderLayout.CENTER);
	}

	public Properties getProperties()
	{
		Properties props = new Properties();
		this.variablesTable.stopEditing();
		int count = this.varData.getRowCount();
		for (int row=0; row < count; row++)
		{
			String key = this.varData.getValueAsString(row, 0);
			String value = this.varData.getValueAsString(row, 1);
			props.setProperty(key, value);
		}
		return props;
	}

	public void deleteItem()
		throws Exception
	{
		this.variablesTable.deleteRow();
	}

	public void newItem(boolean copyCurrent)
		throws Exception
	{
		this.variablesTable.addRow();
		this.variablesTable.getSelectionModel().clearSelection();
	}

	public void saveItem()
		throws Exception
	{
		this.varData.updateDb(null);
	}

	public void componentDisplayed()
	{
		this.variablesTable.setColumnSelectionInterval(1,1);
		this.variablesTable.editCellAt(0, 1);
		TableCellEditor editor = this.variablesTable.getCellEditor();
		if (editor instanceof WbTextCellEditor)
		{
			WbTextCellEditor wbedit = (WbTextCellEditor)editor;
			wbedit.selectAll();
			wbedit.requestFocus();
		}
		
	}
	
	public boolean validateInput()
	{
		this.variablesTable.stopEditing();
		int rows = this.varData.getRowCount();
		for (int i=0; i < rows; i++)
		{
			String varName = this.varData.getValueAsString(i, 0);
			if (!SqlParameterPool.getInstance().isValidVariableName(varName))
			{
				String msg = ResourceMgr.getString("ErrorIllegalVariableName");
				msg = msg.replaceAll("%varname%", varName);
				msg = msg + "\n" + ResourceMgr.getString("ErrorVarDefWrongName");
				WbSwingUtilities.showErrorMessage(this, msg);
				return false;
			}
		}
		return true;
	}
	
	public static boolean showVariablesDialog(DataStore vardata)
	{
		VariablesEditor editor = new VariablesEditor(vardata);
		Dimension d = new Dimension(300,250);
		editor.setMinimumSize(d);
		editor.setPreferredSize(d);

		boolean result = false; 
		boolean ok = ValidatingDialog.showConfirmDialog(WbManager.getInstance().getCurrentWindow(), editor, ResourceMgr.getString("TxtEditVariablesWindowTitle"));
		if (ok)
		{
			try
			{
				vardata.updateDb(null);
				result = true;
			}
			catch (Exception e)
			{
				LogMgr.logError("VariablesEditor.showVariablesDialog()", "Error when saving values", e);
				result = false;
			}
		}
		return result;
	}
	
}
