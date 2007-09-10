/*
 * ProcedureListPanel.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2007, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.dbobjects;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;

import workbench.db.DbMetadata;
import workbench.db.ProcedureReader;
import workbench.db.WbConnection;
import workbench.gui.WbSwingUtilities;
import workbench.gui.actions.ReloadAction;
import workbench.gui.components.WbMenuItem;
import workbench.gui.components.WbScrollPane;
import workbench.gui.components.WbSplitPane;
import workbench.gui.components.WbTable;
import workbench.gui.components.WbTraversalPolicy;
import workbench.gui.menu.GenerateScriptMenuItem;
import workbench.gui.renderer.ProcStatusRenderer;
import workbench.interfaces.PropertyStorage;
import workbench.interfaces.Reloadable;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.util.StringUtil;
import javax.swing.JLabel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import workbench.WbManager;
import workbench.db.ObjectScripter;
import workbench.db.ProcedureDefinition;
import workbench.gui.MainWindow;
import workbench.gui.components.DataStoreTableModel;
import workbench.gui.components.QuickFilterPanel;
import workbench.gui.components.WbTabbedPane;
import workbench.gui.renderer.RendererFactory;
import workbench.interfaces.CriteriaPanel;
import workbench.storage.DataStore;
import workbench.util.SqlUtil;
import workbench.util.WbWorkspace;


/**
 *
 * @author  support@sql-workbench.net
 *
 */
public class ProcedureListPanel
	extends JPanel
	implements ListSelectionListener, Reloadable, ActionListener
{
	private static final String DROP_CMD = "drop-object";
	private static final String COMPILE_CMD = "compile-procedure";
	
	private WbConnection dbConnection;
	private JPanel listPanel;
	private CriteriaPanel findPanel;
	private WbTable procList;
	private WbTable procColumns;
	protected DbObjectSourcePanel source;
	private JTabbedPane displayTab;
	private WbSplitPane splitPane;
	private String currentSchema;
	private String currentCatalog;
	private boolean shouldRetrieve;
	private WbMenuItem dropTableItem;
	private WbMenuItem recompileItem;
	private WbMenuItem createScriptItem;
	private JLabel infoLabel;
	private boolean isRetrieving;
	protected ProcStatusRenderer statusRenderer;

	public ProcedureListPanel(MainWindow parent) 
		throws Exception
	{
		this.displayTab = new WbTabbedPane();
		this.displayTab.setTabPlacement(JTabbedPane.BOTTOM);

		this.procColumns = new WbTable();
		this.procColumns.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		this.procColumns.setCellSelectionEnabled(false);
		this.procColumns.setColumnSelectionAllowed(false);
		this.procColumns.setRowSelectionAllowed(true);
		this.procColumns.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		JScrollPane scroll = new WbScrollPane(this.procColumns);

		Reloadable sourceReload = new Reloadable()
		{
			public void reload()
			{
				if (dbConnection.isBusy()) return;
				try
				{
					dbConnection.setBusy(true);
					retrieveCurrentProcedure();
				}
				finally
				{
					dbConnection.setBusy(false);
				}
			}
		};
		
		source = new DbObjectSourcePanel(parent, sourceReload);
		this.displayTab.add(ResourceMgr.getString("TxtDbExplorerSource"), source);
		this.displayTab.add(ResourceMgr.getString("TxtDbExplorerTableDefinition"), scroll);

		this.listPanel = new JPanel();
		this.statusRenderer = new ProcStatusRenderer();
		this.procList = new WbTable(true, false, false)
		{
			public TableCellRenderer getCellRenderer(int row, int column) 
			{
				if (column == ProcedureReader.COLUMN_IDX_PROC_LIST_TYPE) return statusRenderer;
				return super.getCellRenderer(row, column);
			}
		};
		
		this.procList.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		this.procList.setCellSelectionEnabled(false);
		this.procList.setColumnSelectionAllowed(false);
		this.procList.setRowSelectionAllowed(true);
		this.procList.getSelectionModel().addListSelectionListener(this);
		this.procList.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		String[] cols = new String[] {"PROCEDURE_NAME", "TYPE", "CATALOG", "SCHEMA", "REMARKS"};
		this.findPanel = new QuickFilterPanel(this.procList, cols, false, "procedurelist");
		
		ReloadAction a = new ReloadAction(this);
		
		this.findPanel.addToToolbar(a, true, false);
		a.getToolbarButton().setToolTipText(ResourceMgr.getString("TxtRefreshProcedureList"));
		this.listPanel.setLayout(new BorderLayout());
		this.listPanel.add((JPanel)findPanel, BorderLayout.NORTH);

		this.splitPane = new WbSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		this.splitPane.setOneTouchExpandable(true);
		this.splitPane.setDividerSize(6);
		scroll = new WbScrollPane(this.procList);

		this.listPanel.add(scroll, BorderLayout.CENTER);

		this.infoLabel = new JLabel("");
		EmptyBorder b = new EmptyBorder(1, 3, 0, 0);
		this.infoLabel.setBorder(b);
		this.listPanel.add(this.infoLabel, BorderLayout.SOUTH);

		this.splitPane.setLeftComponent(this.listPanel);
		this.splitPane.setRightComponent(displayTab);
		this.splitPane.setDividerBorder(WbSwingUtilities.EMPTY_BORDER);
		this.setLayout(new BorderLayout());
		this.add(splitPane, BorderLayout.CENTER);

		WbTraversalPolicy pol = new WbTraversalPolicy();
		pol.setDefaultComponent((JPanel)findPanel);
		pol.addComponent((JPanel)findPanel);
		pol.addComponent(this.procList);
		pol.addComponent(this.procColumns);
		this.setFocusTraversalPolicy(pol);
		this.reset();
		this.extendPopupMenu();
	}

	private void extendPopupMenu()
	{
		JPopupMenu popup = this.procList.getPopupMenu();
		popup.addSeparator();
		this.createScriptItem = new GenerateScriptMenuItem();
		this.createScriptItem.addActionListener(this);
		popup.add(this.createScriptItem);
		popup.addSeparator();
		this.dropTableItem = new WbMenuItem(ResourceMgr.getString("MnuTxtDropDbObject"));
		this.dropTableItem.setActionCommand(DROP_CMD);
		this.dropTableItem.addActionListener(this);
		this.dropTableItem.setEnabled(false);
		popup.add(this.dropTableItem);
	}

	public void disconnect()
	{
		this.dbConnection = null;
		this.reset();
	}

	public void reset()
	{
		this.procList.reset();
		this.procColumns.reset();
		this.source.setText("");
	}

	public void setConnection(WbConnection aConnection)
	{
		this.dbConnection = aConnection;
		this.source.setDatabaseConnection(aConnection);
		this.reset();

		if (this.recompileItem != null)
		{
			this.recompileItem.removeActionListener(this);
		}
		
		JPopupMenu popup = this.procList.getPopupMenu();
		if (this.dbConnection.getMetadata().isOracle())
		{
			this.recompileItem = new WbMenuItem(ResourceMgr.getString("MnuTxtRecompile"));
			this.recompileItem.setActionCommand(COMPILE_CMD);
			this.recompileItem.addActionListener(this);
			this.recompileItem.setEnabled(false);
			popup.add(this.recompileItem);
		}
		else
		{
			if (this.recompileItem != null)
			{
				popup.remove(this.recompileItem);
			}
			this.recompileItem = null;
		}
	}

	public void setCatalogAndSchema(String aCatalog, String aSchema, boolean retrieve)
		throws Exception
	{
		this.currentSchema = aSchema;
		this.currentCatalog = aCatalog;
		if (this.isVisible() && retrieve)
		{
			this.retrieve();
		}
		else
		{
			this.reset();
			this.shouldRetrieve = true;
		}
	}

	public void panelSelected()
	{
		retrieveIfNeeded();
	}
	
	public void retrieveIfNeeded()
	{
		if (this.shouldRetrieve) this.retrieve();
	}

	public void retrieve()
	{
		if (this.isRetrieving) return;
		if (!WbSwingUtilities.checkConnection(this, this.dbConnection)) return;
		
		try
		{
			this.reset();
			this.dbConnection.setBusy(true);
			this.isRetrieving = true;
			DbMetadata meta = dbConnection.getMetadata();
			WbSwingUtilities.showWaitCursorOnWindow(this);
			DataStore ds = meta.getProcedures(currentCatalog, currentSchema);
			DataStoreTableModel model = new DataStoreTableModel(ds);
			
			int rows = model.getRowCount();
			String info = rows + " " + ResourceMgr.getString("TxtTableListObjects");
			this.infoLabel.setText(info);
			procList.setModel(model, true);
			procList.adjustOrOptimizeColumns();
			shouldRetrieve = false;
		}
		catch (OutOfMemoryError mem)
		{
			WbManager.getInstance().showOutOfMemoryError();
		}
		catch (Throwable e)
		{
			LogMgr.logError("ProcedureListPanel.retrieve() thread", "Could not retrieve procedure list", e);
		}
		finally
		{
			this.isRetrieving = false;
			this.dbConnection.setBusy(false);
			WbSwingUtilities.showDefaultCursorOnWindow(this);
		}

	}

	private void dropObjects()
	{
		if (!WbSwingUtilities.checkConnection(this, this.dbConnection)) return;
		if (this.procList.getSelectedRowCount() == 0) return;
		int rows[] = this.procList.getSelectedRows();
		int count = rows.length;
		if (count == 0) return;

		ArrayList<String> names = new ArrayList<String>(count);
		ArrayList<String> types = new ArrayList<String>(count);

		this.readSelectedItems(names, types);

		ObjectDropperUI dropperUI = new ObjectDropperUI();
		dropperUI.setObjects(names, types);
		dropperUI.setConnection(this.dbConnection);
		JFrame f = (JFrame)SwingUtilities.getWindowAncestor(this);
		dropperUI.showDialog(f);
		if (!dropperUI.dialogWasCancelled())
		{
			EventQueue.invokeLater(new Runnable()
			{
				public void run()
				{
					reload();
				}
			});
		}
	}

	public void setVisible(boolean aFlag)
	{
		super.setVisible(aFlag);
		if (aFlag && this.shouldRetrieve)
			this.retrieve();
	}

	private String getWorkspacePrefix(int index)
	{
		return "dbexplorer" + index + ".procedurelist.";
	}
	
	public void saveSettings()
	{
		storeSettings(Settings.getInstance(), this.getClass().getName() + ".");
		findPanel.saveSettings();
	}
	
	public void saveToWorkspace(WbWorkspace w, int index)
	{
		String prefix = getWorkspacePrefix(index);
		storeSettings(w.getSettings(), prefix);
		findPanel.saveSettings(w.getSettings(), prefix);
	}
	
	private void storeSettings(PropertyStorage props, String prefix)
	{
		props.setProperty(prefix + "divider", this.splitPane.getDividerLocation());
	}
	
	public void restoreSettings()
	{
		readSettings(Settings.getInstance(), this.getClass().getName() + ".");
		findPanel.restoreSettings();
	}
	
	public void readFromWorkspace(WbWorkspace w, int index)
	{
		String prefix = getWorkspacePrefix(index);
		readSettings(w.getSettings(), prefix);
		this.findPanel.restoreSettings(w.getSettings(), prefix);
	}
	
	private void readSettings(PropertyStorage props, String prefix)
	{
		int loc = props.getIntProperty(prefix + "divider", 200);
		this.splitPane.setDividerLocation(loc);
	}

	public void valueChanged(ListSelectionEvent e)
	{
		if (e.getSource() != this.procList.getSelectionModel()) return;
		if (e.getValueIsAdjusting()) return;
		retrieveCurrentProcedure();
	}
	
	protected void retrieveCurrentProcedure()
	{
		int row = this.procList.getSelectedRow();

		if (row < 0) return;
		this.dropTableItem.setEnabled(this.procList.getSelectedRowCount() > 0);
		if (this.recompileItem != null)
		{
			this.recompileItem.setEnabled(this.procList.getSelectedRowCount() > 0);
		}

		final String proc = this.procList.getValueAsString(row, ProcedureReader.COLUMN_IDX_PROC_LIST_NAME);
		final String schema = this.procList.getValueAsString(row, ProcedureReader.COLUMN_IDX_PROC_LIST_SCHEMA);
		final String catalog = this.procList.getValueAsString(row, ProcedureReader.COLUMN_IDX_PROC_LIST_CATALOG);
		final int type = this.procList.getDataStore().getValueAsInt(row, ProcedureReader.COLUMN_IDX_PROC_LIST_TYPE, DatabaseMetaData.procedureResultUnknown);
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				retrieveProcDefinition(catalog, schema, proc, type);
			}
		});
	}

	private void retrieveProcDefinition(String catalog, String schema, String proc, int type)
	{
		if (this.dbConnection == null) return;
		if (!WbSwingUtilities.checkConnection(this, this.dbConnection)) return;
		
		DbMetadata meta = dbConnection.getMetadata();
		Container parent = this.getParent();
		parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		CharSequence sql = null;
		try
		{
			dbConnection.setBusy(true);
			try
			{
				procColumns.setVisible(false);
				DataStoreTableModel model = new DataStoreTableModel(meta.getProcedureColumns(catalog, schema, proc));
				procColumns.setModel(model, true);
				
				TableColumnModel colmod = procColumns.getColumnModel();
				// Assign the correct renderer to display java.sql.Types values
				TableColumn col = colmod.getColumn(ProcedureReader.COLUMN_IDX_PROC_COLUMNS_JDBC_DATA_TYPE);
				if (col != null)
				{
					col.setCellRenderer(RendererFactory.getSqlTypeRenderer());
				}
				procColumns.adjustOrOptimizeColumns();
			}
			catch (Exception ex)
			{
				LogMgr.logError("ProcedureListPanel.valueChanged() thread", "Could not read procedure definition", ex);
				procColumns.reset();
			}
			finally
			{
				procColumns.setVisible(true);
			}

			try
			{
				sql = meta.getProcedureSource(catalog, schema, proc, type);
				source.setText(sql == null ? "" : sql.toString());
			}
			catch (Throwable ex)
			{
				sql = null;
				LogMgr.logError("ProcedureListPanel.valueChanged() thread", "Could not read procedure source", ex);
				source.setText(ex.getMessage());
			}
		}
		finally
		{
			parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			dbConnection.setBusy(false);
		}
		final int pos = checkOraclePackage(sql, catalog, proc, type);
		
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				source.setCaretPosition(pos,(pos > 0));
				source.requestFocusInWindow();
			}
		});
	}
	
	private int checkOraclePackage(CharSequence sql, String catalog, String object, int type)
	{
		if (sql == null) return 0;
		if (this.dbConnection == null) return 0;
		if (!this.dbConnection.getMetadata().isOracle()) return 0;
		
		// The package name is stored in the catalog field
		// if that is empty, it's not a packaged function
		
		if (StringUtil.isEmptyString(catalog)) return 0;
		
		int bodyPos = SqlUtil.getKeywordPosition("PACKAGE BODY", sql);
		
		// If no package body was found, start at the beginning
		// so the function declaration is selected
		if (bodyPos == -1) bodyPos = 0;
		
		String regex = "(PROCEDURE|FUNCTION)\\s+" + object;

		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		
		Matcher m = p.matcher(sql);
		if (m.find(bodyPos))
		{
			return m.start();
		}
		return 0;
	}
	
	protected void createScript()
	{
		if (this.procList.getSelectedRowCount() == 0) return;
		int rows[] = this.procList.getSelectedRows();
		int count = rows.length;
		HashMap<ProcedureDefinition, String> procs = new HashMap<ProcedureDefinition, String>(count);
		for (int i = 0; i < count; i++)
		{
			String proc = this.procList.getValueAsString(rows[i], ProcedureReader.COLUMN_IDX_PROC_LIST_NAME);
			String schema = this.procList.getValueAsString(rows[i], ProcedureReader.COLUMN_IDX_PROC_LIST_SCHEMA);
			String catalog = this.procList.getValueAsString(rows[i], ProcedureReader.COLUMN_IDX_PROC_LIST_CATALOG);
			int type = this.procList.getDataStore().getValueAsInt(rows[i], ProcedureReader.COLUMN_IDX_PROC_LIST_TYPE, DatabaseMetaData.procedureResultUnknown);
			ProcedureDefinition def = new ProcedureDefinition(catalog, schema, proc, type);
			procs.put(def, "PROCEDURE");
		}
		ObjectScripter s = new ObjectScripter(procs, this.dbConnection);
		ObjectScripterUI scripterUI = new ObjectScripterUI(s);
		scripterUI.show(SwingUtilities.getWindowAncestor(this));
		
	}
	
	private void readSelectedItems(List<String> names, List<String> types)
	{
		if (this.procList.getSelectedRowCount() == 0) return;
		int rows[] = this.procList.getSelectedRows();
		int count = rows.length;
		if (count == 0) return;

		for (int i=0; i < count; i ++)
		{
			String name = this.procList.getValueAsString(rows[i], ProcedureReader.COLUMN_IDX_PROC_LIST_NAME);

			// MS SQL Server appends a semicolon at the end of the name...
			if (name.indexOf(';') > 0)
			{
				name = name.substring(0, name.indexOf(';'));
			}

			String schema = this.procList.getValueAsString(rows[i], ProcedureReader.COLUMN_IDX_PROC_LIST_SCHEMA);
      if (schema != null && schema.length() > 0)
      {
  			name = dbConnection.getMetadata().quoteObjectname(schema) + "." + dbConnection.getMetadata().quoteObjectname(name);
      }
      else
      {
        name = dbConnection.getMetadata().quoteObjectname(name);
      }

			int procType = this.procList.getDataStore().getValueAsInt(rows[i], ProcedureReader.COLUMN_IDX_PROC_LIST_TYPE, DatabaseMetaData.procedureResultUnknown);
			String type = StringUtil.EMPTY_STRING;
			if (procType == DatabaseMetaData.procedureReturnsResult)
			{
				type = "FUNCTION";
			}
			else if (procType == DatabaseMetaData.procedureNoResult)
			{
				type = "PROCEDURE";
			}
			
			if (this.dbConnection.getMetadata().isOracle())
			{
				// Oracle reports the type of the procedure in a rather strange way.
				// the only way to tell if it's a package, is to look at the CATALOG column
				// if that contains an entry, it's a packaged procedure
				String catalog = this.procList.getValueAsString(rows[i], ProcedureReader.COLUMN_IDX_PROC_LIST_CATALOG);
				if (catalog != null && catalog.length() > 0)
				{
					type = "PACKAGE";

					// the procedure itself can neither be dropped
					// nor recompiled. So we use the name of the package
					// as the object name
					name = catalog;
				}
			}
			names.add(name);
			types.add(type);
		}
	}

	private void compileObjects()
	{
		if (this.procList.getSelectedRowCount() == 0) return;
		int rows[] = this.procList.getSelectedRows();
		int count = rows.length;
		if (count == 0) return;

		List<String> names = new ArrayList<String>(count);
		List<String> types = new ArrayList<String>(count);

		this.readSelectedItems(names, types);

		try
		{
			ObjectCompilerUI compilerUI = new ObjectCompilerUI(names, types, this.dbConnection);
			compilerUI.show(SwingUtilities.getWindowAncestor(this));
		}
		catch (SQLException e)
		{
			LogMgr.logError("ProcedureListPanel.compileObjects()", "Error initializing ObjectCompilerUI", e);
		}
	}
	
	public void reload()
	{
		this.reset();
		this.retrieve();
	}
	
	public void actionPerformed(ActionEvent e)
	{
		String command = e.getActionCommand();
		if (DROP_CMD.equals(command))
		{
			this.dropObjects();
		}
		else if (COMPILE_CMD.equals(command))
		{
			this.compileObjects();
		}
		else if (e.getSource() == this.createScriptItem)
		{
			EventQueue.invokeLater(new Runnable()
			{
				public void run()
				{
					createScript();
				}
			});
		}
		
	}
}
