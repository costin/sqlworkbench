/*
 * Created on 5. August 2002, 21:06
 */
package workbench.gui.dbobjects;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import workbench.WbManager;
import workbench.db.DataSpooler;
import workbench.db.DbMetadata;
import workbench.db.ObjectScripter;
import workbench.db.WbConnection;
import workbench.gui.MainWindow;
import workbench.gui.WbSwingUtilities;
import workbench.gui.actions.ReloadAction;
import workbench.gui.actions.SpoolDataAction;
import workbench.gui.actions.WbAction;

import workbench.gui.components.*;
import workbench.gui.dbobjects.ObjectScripterUI;
import workbench.gui.renderer.SqlTypeRenderer;
import workbench.gui.sql.EditorPanel;
import workbench.gui.sql.SqlPanel;
import workbench.interfaces.FilenameChangeListener;
import workbench.interfaces.ShareableDisplay;
import workbench.interfaces.Spooler;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.storage.DataStore;
import workbench.util.SqlUtil;
import workbench.db.TableIdentifier;
import workbench.gui.sql.ExecuteSqlDialog;



/**
 *
 * @author  workbench@kellerer.org
 *
 */
public class TableListPanel
	extends JPanel
	implements ActionListener, ChangeListener, ListSelectionListener, MouseListener
						, ShareableDisplay, Spooler, FilenameChangeListener
{
	private WbConnection dbConnection;
	private JPanel listPanel;
	private FindPanel findPanel;
	private WbTable tableList;
	private WbTable tableDefinition;
	private WbTable indexes;
	private WbTable importedKeys;
	private WbTable exportedKeys;

	private TableDataPanel tableData;

	private TableDependencyTreeDisplay importedTableTree;
	private WbSplitPane importedPanel;

	private TableDependencyTreeDisplay exportedTableTree;
	private WbSplitPane exportedPanel;

	private WbScrollPane indexPanel;
	private TriggerDisplayPanel triggers;
	private EditorPanel tableSource;
	private JTabbedPane displayTab;
	private WbSplitPane splitPane;

	private Object retrieveLock = new Object();
	private JComboBox tableTypes = new JComboBox();
	//private JComboBox catalogs = new JComboBox();
	private String currentSchema;
	private String currentCatalog;
	private SpoolDataAction spoolData;


	private WbMenuItem dropTableItem;
	private WbMenuItem scriptTablesItem;
	private WbMenuItem deleteTableItem;

	private MainWindow parentWindow;

	private String selectedCatalog;
	private String selectedSchema;
	private String selectedTableName;
	private String selectedObjectType;

	private boolean shiftDown = false;
	private boolean shouldRetrieve;

	private boolean shouldRetrieveTable;
	private boolean shouldRetrieveTableSource;
	private boolean shouldRetrieveTriggers;
	private boolean shouldRetrieveIndexes;
	private boolean shouldRetrieveExportedKeys;
	private boolean shouldRetrieveImportedKeys;
	private boolean shouldRetrieveExportedTree;
	private boolean shouldRetrieveImportedTree;
	private boolean shouldRetrieveTableDataCount;

	private boolean busy;
	private boolean ignoreStateChanged = false;

	private static final String DROP_CMD = "drop-table";
	private static final String SCRIPT_CMD = "create-scripts";
	private static final String DELETE_TABLE_CMD = "delete-table-data";

	private JMenu showDataMenu;
	private String[] availableTableTypes;
	private WbAction dropIndexAction;
	private WbAction createIndexAction;
	private WbAction createDummyInsertAction;

	// holds a reference to other WbTables which
	// need to display the same table list
	// e.g. the table search panel
	private List tableListClients;

	public TableListPanel(MainWindow aParent)
		throws Exception
	{
		this.parentWindow = aParent;
		this.setBorder(WbSwingUtilities.EMPTY_BORDER);
		this.displayTab = new JTabbedPane();
		this.displayTab.setTabPlacement(JTabbedPane.BOTTOM);
		this.displayTab.setUI(TabbedPaneUIFactory.getBorderLessUI());
		this.displayTab.setBorder(WbSwingUtilities.EMPTY_BORDER);

		this.tableDefinition = new WbTable();
		this.tableDefinition.setAdjustToColumnLabel(false);
		this.tableDefinition.setSelectOnRightButtonClick(true);
		WbScrollPane scroll = new WbScrollPane(this.tableDefinition);
		this.displayTab.add(ResourceMgr.getString("TxtDbExplorerTableDefinition"), scroll);

		this.indexes = new WbTable();
		this.indexes.setAdjustToColumnLabel(false);
		this.indexPanel = new WbScrollPane(this.indexes);
		this.indexes.getSelectionModel().addListSelectionListener(this);

		this.tableSource = EditorPanel.createSqlEditor();
		this.tableSource.setEditable(false);
		this.tableSource.showFindOnPopupMenu();

		//this.tableSource.addPopupMenuItem(new FileSaveAsAction(this.tableSource), true);

		this.displayTab.add(ResourceMgr.getString("TxtDbExplorerSource"), this.tableSource);
		this.tableData = new TableDataPanel();

		this.importedKeys = new WbTable();
		this.importedKeys.setAdjustToColumnLabel(false);
		scroll = new WbScrollPane(this.importedKeys);
		this.importedPanel = new WbSplitPane(JSplitPane.VERTICAL_SPLIT);
		this.importedPanel.setDividerBorder(WbSwingUtilities.EMPTY_BORDER);
		this.importedPanel.setDividerLocation(100);
		this.importedPanel.setDividerSize(4);
		this.importedPanel.setTopComponent(scroll);
		this.importedTableTree = new TableDependencyTreeDisplay();
		this.importedPanel.setBottomComponent(this.importedTableTree);

		this.exportedKeys = new WbTable();
		this.exportedKeys.setAdjustToColumnLabel(false);
		scroll = new WbScrollPane(this.exportedKeys);
		this.exportedPanel = new WbSplitPane(JSplitPane.VERTICAL_SPLIT);
		this.exportedPanel.setDividerBorder(WbSwingUtilities.EMPTY_BORDER);
		this.exportedPanel.setDividerLocation(100);
		this.exportedPanel.setDividerSize(4);
		this.exportedPanel.setTopComponent(scroll);
		this.exportedTableTree = new TableDependencyTreeDisplay();
		this.exportedPanel.setBottomComponent(this.exportedTableTree);

		//this.exportedPanel = new WbScrollPane(this.exportedKeys);

		this.triggers = new TriggerDisplayPanel();

		this.listPanel = new JPanel();
		this.tableList = new WbTable();
		this.tableList.setSelectOnRightButtonClick(true);
		this.tableList.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		this.tableList.setCellSelectionEnabled(false);
		this.tableList.setColumnSelectionAllowed(false);
		this.tableList.setRowSelectionAllowed(true);
		this.tableList.getSelectionModel().addListSelectionListener(this);
		this.tableList.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		this.tableList.setAdjustToColumnLabel(false);

		this.spoolData = new SpoolDataAction(this);
		this.tableList.addPopupAction(spoolData, true);

		this.createDummyInsertAction = new WbAction(this, "create-dummy-insert");
		this.createDummyInsertAction.setEnabled(true);
		this.createDummyInsertAction.initMenuDefinition("MnuTxtCreateDummyInsert");

		this.tableList.addPopupAction(this.createDummyInsertAction, false);

		this.extendPopupMenu();
		this.findPanel = new FindPanel(this.tableList);

		ReloadAction a = new ReloadAction(this);
		a.getToolbarButton().setToolTipText(ResourceMgr.getString("TxtRefreshTableList"));
		this.findPanel.addToToolbar(a, true, false);

		JPanel selectPanel = new JPanel();
		selectPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		//selectPanel.setBorder(new LineBorder(Color.RED));
		//selectPanel.setMinimumSize(new Dimension(100, 18));

		this.tableTypes.setMaximumSize(new Dimension(32768, 18));
		this.tableTypes.setMinimumSize(new Dimension(80, 18));
		//selectPanel.add(this.tableTypes);

		//this.catalogs.setMaximumSize(new Dimension(32768, 18));
		//this.catalogs.setMinimumSize(new Dimension(80, 18));
		//selectPanel.add(this.catalogs);

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridBagLayout());
		GridBagConstraints constr = new GridBagConstraints();
		constr.anchor = GridBagConstraints.WEST;
		constr.gridx = 0;

		topPanel.add(this.tableTypes, constr);

		constr = new GridBagConstraints();
		constr.anchor = GridBagConstraints.WEST;
		constr.gridwidth = GridBagConstraints.REMAINDER;
		constr.fill = GridBagConstraints.HORIZONTAL;
		constr.weightx = 1.0;
		topPanel.add(this.findPanel, constr);

		this.listPanel.setLayout(new BorderLayout());
		this.listPanel.add(topPanel, BorderLayout.NORTH);

		this.splitPane = new WbSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		scroll = new WbScrollPane(this.tableList);

		this.listPanel.add(scroll, BorderLayout.CENTER);
		this.listPanel.setBorder(WbSwingUtilities.EMPTY_BORDER);

		this.splitPane.setLeftComponent(this.listPanel);
		this.splitPane.setRightComponent(displayTab);
		this.splitPane.setDividerSize(8);
		this.splitPane.setDividerBorder(WbSwingUtilities.EMPTY_BORDER);
		this.setLayout(new BorderLayout());
		this.add(splitPane, BorderLayout.CENTER);

		WbTraversalPolicy pol = new WbTraversalPolicy();
		pol.setDefaultComponent(findPanel);
		pol.addComponent(findPanel);
		pol.addComponent(tableList);
		pol.addComponent(tableDefinition);
		this.setFocusTraversalPolicy(pol);
		this.reset();
		this.parentWindow.addFilenameChangeListener(this);
    this.parentWindow.addIndexChangeListener(this);
		this.displayTab.addMouseListener(this);
		this.tableList.addMouseListener(this);

		this.dropIndexAction = new WbAction(this,"drop-index");
		this.dropIndexAction.setEnabled(false);
		this.indexes.setSelectOnRightButtonClick(true);
		this.dropIndexAction.initMenuDefinition("MnuTxtDropIndex");
		this.indexes.addPopupAction(this.dropIndexAction, true);

		this.createIndexAction = new WbAction(this, "create-index");
		this.createIndexAction.setEnabled(false);
		this.createIndexAction.initMenuDefinition("MnuTxtCreateIndex");
		this.tableDefinition.getSelectionModel().addListSelectionListener(this);
		this.tableDefinition.addPopupAction(this.createIndexAction, true);

	}

	private void extendPopupMenu()
	{
		JPopupMenu popup = this.tableList.getPopupMenu();
		popup.addSeparator();
		this.dropTableItem = new WbMenuItem(ResourceMgr.getString("MnuTxtDropDbObject"));
		this.dropTableItem.setActionCommand(DROP_CMD);
		this.dropTableItem.setIcon(ResourceMgr.getImage("blank"));
		this.dropTableItem.addActionListener(this);
		this.dropTableItem.setEnabled(false);
		popup.add(this.dropTableItem);

		this.deleteTableItem = new WbMenuItem(ResourceMgr.getString("MnuTxtDeleteTableData"));
		this.deleteTableItem.setActionCommand(DELETE_TABLE_CMD);
		this.deleteTableItem.setBlankIcon();
		this.deleteTableItem.addActionListener(this);
		this.deleteTableItem.setEnabled(true);
		popup.add(this.deleteTableItem);

		this.scriptTablesItem = new WbMenuItem(ResourceMgr.getString("MnuTxtCreateScript"));
		this.scriptTablesItem.setIcon(ResourceMgr.getImage("script"));
		this.scriptTablesItem.setActionCommand(SCRIPT_CMD);
		this.scriptTablesItem.addActionListener(this);
		this.scriptTablesItem.setEnabled(true);
		popup.addSeparator();
		popup.add(this.scriptTablesItem);

		this.showDataMenu = new WbMenu(ResourceMgr.getString("MnuTxtShowTableData"));
		this.showDataMenu.setEnabled(false);
		this.updateShowDataMenu();
		popup.addSeparator();
		popup.add(this.showDataMenu);
	}

	private Font boldFont = null;
	private Font standardFont = null;

	private void initFonts()
	{
		this.standardFont = WbManager.getSettings().getStandardFont();
		this.boldFont = new Font(this.standardFont.getName(), Font.BOLD, this.standardFont.getSize());
	}

	private void updateShowDataMenu()
	{
		if (this.showDataMenu == null)
		{
			this.showDataMenu = new WbMenu(ResourceMgr.getString("MnuTxtShowTableData"));
		}

		String[] panels = this.parentWindow.getPanelLabels();
		if (panels == null) return;

		int current = this.parentWindow.getCurrentPanelIndex();
		int newCount = panels.length;
		if (current > newCount - 1) return;

		int currentCount = this.showDataMenu.getItemCount();

		if (this.boldFont == null) this.initFonts();
		JMenuItem item = null;

		for (int i=0; i < newCount; i++)
		{
			if (i >= currentCount)
			{
				item = new WbMenuItem();
				item.setActionCommand("panel-" + i);
				item.addActionListener(this);
				this.showDataMenu.add(item);
			}
			else
			{
				item = this.showDataMenu.getItem(i);
			}

			item.setText(panels[i]);
			if (i == current)
			{
				item.setFont(this.boldFont);
			}
			else
			{
				item.setFont(this.standardFont);
			}
		}
		int i = newCount;
		while (i < this.showDataMenu.getItemCount())
		{
			this.showDataMenu.remove(i);
			i++;
		}
	}

	private void addTablePanels()
	{
		try
		{
			if (this.displayTab.getComponentCount() > 3) return;
			this.ignoreStateChanged = true;
			if (this.displayTab.getComponentCount() == 2) this.addDataPanel();
			this.displayTab.add(ResourceMgr.getString("TxtDbExplorerIndexes"), this.indexPanel);
			this.displayTab.add(ResourceMgr.getString("TxtDbExplorerFkColumns"), this.importedPanel);
			this.displayTab.add(ResourceMgr.getString("TxtDbExplorerReferencedColumns"), this.exportedPanel);
			this.displayTab.add(ResourceMgr.getString("TxtDbExplorerTriggers"), this.triggers);
		}
		finally
		{
			this.ignoreStateChanged = false;
		}
	}

	private void removeTablePanels(boolean includeDataPanel)
	{
		try
		{
			int index = this.displayTab.getSelectedIndex();
			this.ignoreStateChanged = true;

			//if (this.displayTab.getTabCount() == 2) return;

			this.displayTab.setSelectedIndex(0);

			int count = this.displayTab.getTabCount();
			if (count < 3 && includeDataPanel) return;
			if (count < 2 && !includeDataPanel) return;

			if (count == 3 && includeDataPanel) this.removeDataPanel();

			this.displayTab.remove(this.indexPanel);
			this.indexes.reset();
			this.displayTab.remove(this.importedPanel);
			this.importedKeys.reset();
			this.displayTab.remove(this.exportedPanel);
			this.exportedKeys.reset();
			this.displayTab.remove(this.triggers);
			this.triggers.reset();
			if (index < this.displayTab.getTabCount())
			{
				this.displayTab.setSelectedIndex(index);
			}
		}
		finally
		{
			this.ignoreStateChanged = false;
		}
	}

	private void addDataPanel()
	{
		this.displayTab.add(ResourceMgr.getString("TxtDbExplorerData"), this.tableData);
	}

	private void removeDataPanel()
	{
		this.displayTab.remove(this.tableData);
		this.tableData.reset();
	}

	public void setInitialFocus()
	{
		this.findPanel.setFocusToEntryField();
	}

	public void disconnect()
	{
		this.dbConnection = null;
		this.tableTypes.removeActionListener(this);
		this.displayTab.removeChangeListener(this);
		this.availableTableTypes = null;
		this.tableTypes.removeAllItems();
		this.reset();
	}

	public void reset()
	{
		synchronized (retrieveLock)
		{
			this.tableList.reset();
			this.resetDetails();
			this.invalidateData();
		}
	}

	public void clearTableData()
	{
		this.tableData.reset();
		this.shouldRetrieveTableDataCount = true;
	}

	public void resetDetails()
	{
		this.tableDefinition.reset();
		this.importedKeys.reset();
		this.exportedKeys.reset();
		this.indexes.reset();
		this.triggers.reset();
		this.tableSource.setText("");
		this.invalidateData();
		//this.updateDisplayClients();
		this.importedTableTree.reset();
		this.exportedTableTree.reset();
		this.tableData.reset();
	}

	private void invalidateData()
	{
		this.shouldRetrieveTable = true;
		this.shouldRetrieveTableSource = true;
		this.shouldRetrieveTriggers = true;
		this.shouldRetrieveIndexes = true;
		this.shouldRetrieveExportedKeys = true;
		this.shouldRetrieveImportedKeys = true;
		this.shouldRetrieveExportedTree = true;
		this.shouldRetrieveImportedTree = true;
		this.shouldRetrieveTableDataCount = true;
	}

	public void setConnection(WbConnection aConnection)
	{
		this.dbConnection = aConnection;
		this.importedTableTree.setConnection(aConnection);
		this.exportedTableTree.setConnection(aConnection);
		this.tableData.setConnection(aConnection);

		this.tableTypes.removeActionListener(this);
		//this.catalogs.removeActionListener(this);

		this.triggers.setConnection(aConnection);
		this.tableSource.getSqlTokenMarker().initDatabaseKeywords(aConnection.getSqlConnection());
		this.reset();
		try
		{
			String preferredType = WbManager.getSettings().getProperty("workbench.dbexplorer", "defTableType", null);
			if (preferredType != null && preferredType.length() == 0) preferredType = null;
			List types = this.dbConnection.getMetadata().getTableTypes();
			this.availableTableTypes = new String[types.size()];
			this.tableTypes.removeAllItems();
			this.tableTypes.addItem("*");
			int preferredIndex = -1;

			for (int i=0; i < types.size(); i++)
			{
				String type = types.get(i).toString();
				if (type.equalsIgnoreCase(preferredType))
				{
					preferredIndex = i + 1;
				}
				this.tableTypes.addItem(type);
				this.availableTableTypes[i] = type;
			}

			if (preferredIndex > -1)
			{
				try
				{
					this.tableTypes.setSelectedIndex(preferredIndex);
				}
				catch (Exception e)
				{
					// ignore it!
				}
			}
		}
		catch (Exception e)
		{
		}

		this.tableTypes.addActionListener(this);
		this.displayTab.addChangeListener(this);
	}

	public void setCatalogAndSchema(String aCatalog, String aSchema)
		throws Exception
	{
		this.setCatalogAndSchema(aCatalog, aSchema, true);
	}

	public void setCatalogAndSchema(String aCatalog, String aSchema, boolean retrieve)
		throws Exception
	{
		this.reset();
		this.currentSchema = aSchema;
		this.currentCatalog = aCatalog;

		if (!retrieve) return;
		if (this.dbConnection == null) return;

		if (this.isVisible() || this.isClientVisible())
		{
			this.retrieve();
		}
		else
		{
			this.shouldRetrieve = true;
		}
	}

	public void retrieve()
	{
		if (busy) return;

		try
		{
			if (dbConnection == null || dbConnection.isClosed())
			{
				WbManager.getInstance().showErrorMessage(this, ResourceMgr.getString("ErrorConnectionGone"));
				return;
			}
		}
		catch (SQLException e)
		{
			LogMgr.logError("TableListPanel.retrieve()", "Error checking connection", e);
			return;
		}

		try
		{
			WbSwingUtilities.showWaitCursor(this);
			setBusy(true);
			synchronized (retrieveLock)
			{
				reset();

				// Some JDBC drivers (e.g. PostgreSQL) return a selection
				// of table types to the user when passing null to getTables()
				// But we really might want to see all tables!
				// So if the app settings tells us to do, we'll use the list
				// of types provided by the driver instead of using null as the type
				String[] types = null;
				String type = (String)tableTypes.getSelectedItem();
				if ("*".equals(type))
				{
					if (WbManager.getSettings().getUseTableTypeList())
					{
						types = availableTableTypes;
					}
				}
				else
				{
					types = new String[1];
					types[0] = type;
				}
				DataStore ds = dbConnection.getMetadata().getTables(currentCatalog, currentSchema, types);
				DataStoreTableModel rs = new DataStoreTableModel(ds);
				tableList.setModel(rs, true);
				tableList.adjustColumns();
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						updateDisplayClients();
					}
				});
				shouldRetrieve = false;
			}
		}
		catch (OutOfMemoryError mem)
		{
			WbManager.getInstance().showErrorMessage(TableListPanel.this, ResourceMgr.getString("MsgOutOfMemoryError"));
		}
		catch (Throwable e)
		{
			LogMgr.logError("TableListPanel.retrieve()", "Error retrieving table list", e);
		}
		finally
		{
			WbSwingUtilities.showDefaultCursor(this);
			setBusy(false);
		}
	}

	/**
	 *	Starts the retrieval of the tables in a background thread
	 */
	public void startRetrieve()
	{
		Thread t = new Thread()
		{
			public void run()
			{
				retrieve();
			}
		};
		t.setDaemon(true);
		t.setName("TableListPanel retrieve() thread");
		t.start();
	}

	public void setVisible(boolean aFlag)
	{
		super.setVisible(aFlag);
		if (this.shouldRetrieve)
			this.retrieve();
	}

	public void saveSettings()
	{
		this.triggers.saveSettings();
		this.tableData.saveSettings();
		Settings s = WbManager.getSettings();
		s.setProperty(this.getClass().getName(), "divider", this.splitPane.getDividerLocation());
		s.setProperty(this.getClass().getName(), "exportedtreedivider", this.exportedPanel.getDividerLocation());
		s.setProperty(this.getClass().getName(), "importedtreedivider", this.exportedPanel.getDividerLocation());
		s.setProperty(this.getClass().getName(), "lastsearch", this.findPanel.getSearchString());
	}

	public void restoreSettings()
	{
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int maxWidth = (int)(d.getWidth() - 50);
		int loc = WbManager.getSettings().getIntProperty(this.getClass().getName(), "divider");
		if (loc == 0 || loc > maxWidth) loc = 200;
		this.splitPane.setDividerLocation(loc);

		loc = WbManager.getSettings().getIntProperty(this.getClass().getName(), "exportedtreedivider");
		if (loc == 0 || loc > maxWidth) loc = 200;
		this.exportedPanel.setDividerLocation(loc);

		loc = WbManager.getSettings().getIntProperty(this.getClass().getName(), "importedtreedivider");
		if (loc == 0 || loc > maxWidth) loc = 200;
		this.importedPanel.setDividerLocation(loc);

		String s = WbManager.getSettings().getProperty(this.getClass().getName(), "lastsearch", "");
		this.findPanel.setSearchString(s);
		this.triggers.restoreSettings();
		this.tableData.restoreSettings();
	}

	public void valueChanged(ListSelectionEvent e)
	{
		if (e.getValueIsAdjusting()) return;
		if (e.getSource() == this.tableList.getSelectionModel())
		{
			this.updateDisplay();
		}
		else if (e.getSource() == this.indexes.getSelectionModel())
		{
			this.dropIndexAction.setEnabled(this.indexes.getSelectedRowCount() > 0);
		}
		else if (e.getSource() == this.tableDefinition.getSelectionModel())
		{
			this.createIndexAction.setEnabled(this.tableDefinition.getSelectedRowCount() > 0);
		}
	}

	public void updateDisplay()
	{
		int count = this.tableList.getSelectedRowCount();

		this.showDataMenu.setEnabled(count == 1);
		this.dropTableItem.setEnabled(count > 0);

		if (count > 1) return;

		int row = this.tableList.getSelectedRow();
		if (row < 0) return;

		this.selectedCatalog = tableList.getValueAsString(row, DbMetadata.COLUMN_IDX_TABLE_LIST_CATALOG);
		this.selectedSchema = tableList.getValueAsString(row, DbMetadata.COLUMN_IDX_TABLE_LIST_SCHEMA);
		this.selectedTableName = tableList.getValueAsString(row, DbMetadata.COLUMN_IDX_TABLE_LIST_NAME);
		this.selectedObjectType = tableList.getValueAsString(row, DbMetadata.COLUMN_IDX_TABLE_LIST_TYPE).toLowerCase();
		this.resetDetails();
		boolean dataVisible = false;

		if (this.selectedObjectType.indexOf("table") > -1)
		{
			addTablePanels();
			dataVisible = true;
		}
		else
		{
			if (isTableType(this.selectedObjectType))
			{
				dataVisible = true;
				if (this.displayTab.getTabCount() == 2)
				{
					this.addDataPanel();
				}
				else
				{
					this.removeTablePanels(false);
				}
			}
			else
			{
				removeTablePanels(true);
			}
		}
		if (dataVisible)
		{
			this.tableData.setReadOnly(!maybeUpdateable(this.selectedObjectType));
			this.tableData.setTable(this.selectedCatalog, this.selectedSchema, this.selectedTableName);
		}
		else
		{
			this.tableData.reset();
		}
		this.showDataMenu.setEnabled(this.isTableType(selectedObjectType));
		this.startRetrieveCurrentPanel();
	}

	private boolean maybeUpdateable(String aType)
	{
		if (aType == null) return false;
		return (aType.indexOf("table") > -1 || aType.indexOf("view") > -1);
	}

	private boolean isTableType(String aType)
	{
		if (aType == null) return false;
		return (aType.indexOf("table") > -1 ||
		        aType.indexOf("view") > -1 ||
						aType.indexOf("synonym") > -1 ||
						(aType.indexOf("sequence") > -1 && this.dbConnection.getMetadata().isPostgres())
					);
	}

	private void retrieveTableSource()
		throws SQLException
	{
		WbSwingUtilities.showWaitCursorOnWindow(this);

		try
		{
			DbMetadata meta = this.dbConnection.getMetadata();
			if (tableDefinition.getRowCount() == 0)
			{
				this.retrieveTableDefinition();
			}
			if (this.selectedObjectType.indexOf("view") > -1)
			{
				String viewSource = meta.getExtendedViewSource(this.selectedCatalog, this.selectedSchema, this.selectedTableName, tableDefinition.getDataStore(), true);
				tableSource.setText(viewSource);
				tableSource.setCaretPosition(0);
			}
			else if ("synonym".equals(this.selectedObjectType))
			{
				String synSource = meta.getSynonymSource(this.selectedSchema, this.selectedTableName);
				tableSource.setText(synSource);
				tableSource.setCaretPosition(0);
			}
			else if ("sequence".equals(this.selectedObjectType))
			{
				String seqSource = meta.getSequenceSource(this.selectedCatalog, this.selectedSchema,this.selectedTableName);
				tableSource.setText(seqSource);
				tableSource.setCaretPosition(0);
			}
			else if (this.selectedObjectType.indexOf("table") > -1)
			{
				// the table information has to be retrieved before
				// the table source, because otherwise the DataStores
				// passed to getTableSource() would be empty
				this.retrieveIndexes();
				this.retrieveImportedTables();
				String sql = meta.getTableSource(this.selectedCatalog, this.selectedSchema, this.selectedTableName, tableDefinition.getDataStore(), indexes.getDataStore(), importedKeys.getDataStore(), true);
				tableSource.setText(sql);
				tableSource.setCaretPosition(0);
			}
		}
		finally
		{
			WbSwingUtilities.showDefaultCursorOnWindow(this);
		}
		shouldRetrieveTableSource = false;
	}

	private void retrieveTableDefinition()
		throws SQLException
	{
		WbSwingUtilities.showWaitCursorOnWindow(this);

		try
		{
			DbMetadata meta = this.dbConnection.getMetadata();
			DataStore def = meta.getTableDefinition(this.selectedCatalog, this.selectedSchema, this.selectedTableName, this.selectedObjectType, false);
			DataStoreTableModel model = new DataStoreTableModel(def);
			tableDefinition.setPrintHeader(this.selectedTableName);
			tableDefinition.setModel(model, true);
			tableDefinition.adjustColumns();

			// remove the last two columns if we are not displaying a SEQUENCE for Oracle
			if (!(meta.isOracle() && "SEQUENCE".equalsIgnoreCase(this.selectedObjectType)))
			{
				TableColumnModel colmod = tableDefinition.getColumnModel();
				TableColumn col = colmod.getColumn(DbMetadata.COLUMN_IDX_TABLE_DEFINITION_JAVA_SQL_TYPE);
				col.setCellRenderer(new SqlTypeRenderer());

				col = colmod.getColumn(colmod.getColumnCount() - 1);
				colmod.removeColumn(col);

				col = colmod.getColumn(colmod.getColumnCount() - 1);
				colmod.removeColumn(col);
			}
		}
		finally
		{
			WbSwingUtilities.showDefaultCursorOnWindow(this);
		}
		shouldRetrieveTable = false;
	}

	private void startRetrieveCurrentPanel()
	{
		final Component caller = this;
		Thread t = new Thread()
		{
			public void run()
			{
				retrieveCurrentPanel();
			}
		};
		t.setDaemon(true);
		t.setName("TableListPanel RetrievePanel Thread");
		t.start();
	}

	private void retrieveCurrentPanel()
	{
		if (this.busy) return;

		if (this.tableList.getSelectedRowCount() <= 0) return;
		int index = this.displayTab.getSelectedIndex();

		this.setBusy(true);
		try
		{
			switch (index)
			{
				case 0:
					if (this.shouldRetrieveTable) this.retrieveTableDefinition();
					break;
				case 1:
					if (this.shouldRetrieveTableSource) this.retrieveTableSource();
					break;
				case 2:
					if (this.shouldRetrieveTableDataCount)
					{
						this.tableData.showData(!this.shiftDown);
						this.shouldRetrieveTableDataCount = false;
					}
					break;
				case 3:
					if (this.shouldRetrieveIndexes) this.retrieveIndexes();
					break;
				case 4:
					if (this.shouldRetrieveImportedKeys) this.retrieveImportedTables();
					if (this.shouldRetrieveImportedTree) this.retrieveImportedTree();
					break;
				case 5:
					if (this.shouldRetrieveExportedKeys) this.retrieveExportedTables();
					if (this.shouldRetrieveExportedTree) this.retrieveExportedTree();
					break;
				case 6:
					if (this.shouldRetrieveTriggers) this.retrieveTriggers();
			}
		}
		catch (Throwable ex)
		{
			LogMgr.logError("TableListPanel.retrieveCurrentPanel()", "Error retrieving panel " + index, ex);
		}
		finally
		{
			this.setBusy(false);
			WbSwingUtilities.showDefaultCursor(this);
			WbSwingUtilities.showDefaultCursor(this.tableData);
		}
	}

	private synchronized void setBusy(boolean aFlag)
	{
		this.busy = aFlag;
	}

	private void retrieveTriggers()
		throws SQLException
	{
		synchronized (retrieveLock)
		{
			triggers.readTriggers(this.selectedCatalog, this.selectedSchema, this.selectedTableName);
			this.shouldRetrieveTriggers = false;
		}
	}

	private void retrieveIndexes()
		throws SQLException
	{
		synchronized (retrieveLock)
		{
			DbMetadata meta = this.dbConnection.getMetadata();
			indexes.setModel(meta.getTableIndexes(this.selectedCatalog, this.selectedSchema, this.selectedTableName), true);
			indexes.adjustColumns();
			this.shouldRetrieveIndexes = false;
		}
	}

	private void retrieveExportedTables()
		throws SQLException
	{
		WbSwingUtilities.showWaitCursorOnWindow(this);

		try
		{
			synchronized (retrieveLock)
			{
				DbMetadata meta = this.dbConnection.getMetadata();
				DataStoreTableModel model = new DataStoreTableModel(meta.getReferencedBy(this.selectedCatalog, this.selectedSchema, this.selectedTableName));
				exportedKeys.setModel(model, true);
				exportedKeys.adjustColumns();
				this.shouldRetrieveExportedKeys = false;
			}
		}
		catch (Throwable th)
		{
			LogMgr.logError("TableListPanel.retrieveExportedTables()", "Error retrieving table references", th);
		}
		finally
		{
			WbSwingUtilities.showDefaultCursorOnWindow(this);
		}
	}

	private void retrieveImportedTables()
		throws SQLException
	{
		WbSwingUtilities.showWaitCursorOnWindow(this);

		try
		{
			synchronized (retrieveLock)
			{
				DbMetadata meta = this.dbConnection.getMetadata();
				DataStoreTableModel model = new DataStoreTableModel(meta.getForeignKeys(this.selectedCatalog, this.selectedSchema, this.selectedTableName));
				importedKeys.setModel(model, true);
				importedKeys.adjustColumns();
				this.shouldRetrieveImportedKeys = false;
			}
		}
		catch (Throwable th)
		{
			LogMgr.logError("TableListPanel.retrieveImportedTables()", "Error retrieving table references", th);
		}
		finally
		{
			WbSwingUtilities.showDefaultCursorOnWindow(this);
		}
	}

	private void retrieveImportedTree()
	{
		WbSwingUtilities.showWaitCursorOnWindow(this);

		try
		{
			synchronized (retrieveLock)
			{
				importedTableTree.readTree(this.selectedCatalog, this.selectedSchema, this.selectedTableName, false);
				this.shouldRetrieveImportedTree = false;
			}
		}
		catch (Throwable th)
		{
			LogMgr.logError("TableListPanel.retrieveImportedTree()", "Error retrieving table references", th);
		}
		finally
		{
			WbSwingUtilities.showDefaultCursorOnWindow(this);
		}
	}

	private void retrieveExportedTree()
	{
		WbSwingUtilities.showWaitCursorOnWindow(this);

		try
		{
			synchronized (retrieveLock)
			{
				exportedTableTree.readTree(this.selectedCatalog, this.selectedSchema, this.selectedTableName, true);
				this.shouldRetrieveExportedTree = false;
			}
		}
		catch (Throwable th)
		{
			LogMgr.logError("TableListPanel.retrieveImportedTree()", "Error retrieving table references", th);
		}
		finally
		{
			WbSwingUtilities.showDefaultCursorOnWindow(this);
		}
	}

	public void reload()
	{
		this.reset();
		this.retrieve();
	}

	private String buildSqlForTable()
	{
		if (this.selectedTableName == null || this.selectedTableName.length() == 0) return null;
		String table = SqlUtil.quoteObjectname(this.selectedTableName);

		if (this.tableDefinition.getRowCount() == 0)
		{
			try
			{
				this.retrieveTableDefinition();
			}
			catch (Exception e)
			{
				return null;
			}
		}

		int colCount = this.tableDefinition.getRowCount();
		if (colCount == 0) return null;

		StringBuffer sql = new StringBuffer(colCount * 80);

		sql.append("SELECT ");
		boolean quote = false;
		for (int i=0; i < colCount; i++)
		{
			String column = this.tableDefinition.getValueAsString(i, DbMetadata.COLUMN_IDX_TABLE_DEFINITION_COL_NAME);
			column = SqlUtil.quoteObjectname(column);
			if (i > 0 && i < colCount) sql.append(",\n");
			if (i > 0) sql.append("       ");
			sql.append(column);
		}
		sql.append("\nFROM ");
		if (this.selectedSchema != null && this.selectedSchema.trim().length() > 0)
		{
			sql.append(this.selectedSchema);
			sql.append(".");
		}

		sql.append(table);

		return sql.toString();
	}
	/**
	 *	Invoked when the type dropdown changes or the "Show data" item is selected
	 */
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == this.tableTypes)
		{
			try { this.retrieve(); } catch (Exception ex) {}
		}
		else
		{
			String command = e.getActionCommand();
			if (command.startsWith("panel-"))
			{
				int panelIndex = 0;
				try
				{
					panelIndex = Integer.parseInt(command.substring(6));
					final SqlPanel panel = (SqlPanel)this.parentWindow.getSqlPanel(panelIndex);
					String sql = this.buildSqlForTable();
					if (sql != null)
					{
						panel.setStatementText(sql);
						this.parentWindow.show();
						this.parentWindow.selectTab(panelIndex);
						EventQueue.invokeLater(new Runnable()
						{
							public void run()
							{
								panel.selectEditor();
							}
						});
					}
				}
				catch (Exception ex)
				{
					LogMgr.logError("TableListPanel().actionPerformed()", "Error when accessing editor tab", ex);
				}
			}
			else if (command.equals(DELETE_TABLE_CMD))
			{
				this.deleteTables();
			}
			else if (command.equals(DROP_CMD))
			{
				this.dropTables();
			}
			else if (command.equals(SCRIPT_CMD))
			{
				this.createScript();
			}
			else if (e.getSource() == this.dropIndexAction)
			{
				this.dropIndexes();
			}
			else if (e.getSource() == this.createIndexAction)
			{
				this.createIndex();
			}
			else if (e.getSource() == this.createDummyInsertAction)
			{
				this.createDummyInserts();
			}
		}
	}

	private void dropIndexes()
	{
		if (this.indexes.getSelectedRowCount() == 0) return;
		int rows[] = this.indexes.getSelectedRows();
		int count = rows.length;
		if (count == 0) return;

		ArrayList names = new ArrayList(count);
		ArrayList types = new ArrayList(count);
		for (int i=0; i < count; i ++)
		{
			String name = this.indexes.getValueAsString(rows[i], DbMetadata.COLUMN_IDX_TABLE_INDEXLIST_INDEX_NAME);
			names.add(this.selectedSchema + "." + name);
			types.add("INDEX");
		}
		ObjectDropperUI ui = new ObjectDropperUI();
		ui.setObjects(names, types);
		ui.setConnection(this.dbConnection);
		JFrame f = (JFrame)SwingUtilities.getWindowAncestor(this);
		ui.showDialog(f);
		if (!ui.dialogWasCancelled())
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					try
					{
						retrieveIndexes();
					}
					catch (Exception e)
					{
						LogMgr.logError("TableListPanel.dropIndex()", "Error re-retrieving indexes", e);
					}
				}
			});
		}
	}

	private void createScript()
	{
		int[] rows = this.tableList.getSelectedRows();
		int count = rows.length;
		HashMap tables = new HashMap(count);
		for (int i=0; i < count; i++)
		{
			int row = rows[i];
			String owner = this.tableList.getValueAsString(row, DbMetadata.COLUMN_IDX_TABLE_LIST_SCHEMA);
			String table = this.tableList.getValueAsString(row, DbMetadata.COLUMN_IDX_TABLE_LIST_NAME);
			String type = this.tableList.getValueAsString(row, DbMetadata.COLUMN_IDX_TABLE_LIST_TYPE);

			if (owner != null)
			{
				tables.put(owner + "." + table, type);
			}
			else
			{
				tables.put(table, type);
			}
		}
		ObjectScripter s = new ObjectScripter(tables, this.dbConnection);
		ObjectScripterUI ui = new ObjectScripterUI(s);
		ui.show(SwingUtilities.getWindowAncestor(this));
	}

	private boolean isClientVisible()
	{
		if (this.tableListClients == null) return false;
		for (int i=0; i < this.tableListClients.size(); i++)
		{
			JTable table = (JTable)this.tableListClients.get(i);
			if (table.isVisible()) return true;
		}
		return false;
	}

	private void updateDisplayClients()
	{
		if (this.tableListClients == null) return;
		TableModel model = this.tableList.getModel();
		for (int i=0; i < this.tableListClients.size(); i++)
		{
			JTable table = (JTable)this.tableListClients.get(i);
			if (table != null && model != null)
			{
				table.setModel(model);
				if (table instanceof WbTable)
				{
					WbTable t = (WbTable)table;
					t.adjustColumns();
				}
				table.repaint();
			}
		}
	}

	public void addTableListDisplayClient(JTable aClient)
	{
		if (this.tableListClients == null) this.tableListClients = new ArrayList();
		if (!this.tableListClients.contains(aClient)) this.tableListClients.add(aClient);
	}
	public void removeTableListDisplayClient(JTable aClient)
	{
		if (this.tableListClients == null) return;
		this.tableListClients.remove(aClient);
	}

	private void deleteTables()
	{
		if (this.tableList.getSelectedRowCount() == 0) return;
		int rows[] = this.tableList.getSelectedRows();
		int count = rows.length;
		if (count == 0) return;

		ArrayList names = new ArrayList(count);

		for (int i=0; i < count; i ++)
		{
			String type = this.tableList.getValueAsString(rows[i], DbMetadata.COLUMN_IDX_TABLE_LIST_TYPE);
			if (!"table".equalsIgnoreCase(type) && !"view".equalsIgnoreCase(type)) continue;

			String name = this.tableList.getValueAsString(rows[i], DbMetadata.COLUMN_IDX_TABLE_LIST_NAME);
			String schema = this.tableList.getValueAsString(rows[i], DbMetadata.COLUMN_IDX_TABLE_LIST_SCHEMA);

			if (schema!= null && schema.length() > 0)
			{
				name = SqlUtil.quoteObjectname(schema) + "." + SqlUtil.quoteObjectname(name);
			}
			else
			{
				name = SqlUtil.quoteObjectname(name);
			}

			names.add(name);
		}
		TableDeleterUI deleter = new TableDeleterUI();
		deleter.setObjects(names);
		deleter.setConnection(this.dbConnection);
		JFrame f = (JFrame)SwingUtilities.getWindowAncestor(this);
		deleter.showDialog(f);
	}

	private void createDummyInserts()
	{
		int[] rows = this.tableList.getSelectedRows();
		int count = rows.length;
		HashMap tables = new HashMap(count);
		for (int i=0; i < count; i++)
		{
			int row = rows[i];
			String table = this.tableList.getValueAsString(row, DbMetadata.COLUMN_IDX_TABLE_LIST_NAME);
			tables.put(table, "INSERT");
		}
		ObjectScripter s = new ObjectScripter(tables, this.dbConnection);
		ObjectScripterUI ui = new ObjectScripterUI(s);
		ui.show(SwingUtilities.getWindowAncestor(this));
	}


	private void createIndex()
	{
		if (this.tableDefinition.getSelectedRowCount() <= 0) return;
		int rows[] = this.tableDefinition.getSelectedRows();
		int count = rows.length;
		String[] columns = new String[count];

		String msg = ResourceMgr.getString("LabelInputIndexName");
		String indexName = ResourceMgr.getString("TxtNewIndexName");
		//String indexName = WbSwingUtilities.getUserInput(this, msg, defaultName);
		if (indexName == null || indexName.trim().length() == 0) return;

		for (int i=0; i < count; i++)
		{
			columns[i] = this.tableDefinition.getValueAsString(rows[i], DbMetadata.COLUMN_IDX_TABLE_DEFINITION_COL_NAME).toLowerCase();
		}
		TableIdentifier table = new TableIdentifier(this.selectedCatalog, this.selectedSchema, this.selectedTableName);
		String sql = this.dbConnection.getMetadata().buildIndexSource(table, indexName, false, columns);
		String title = ResourceMgr.getString("TxtWindowTitleCreateIndex");
		Window parent = SwingUtilities.getWindowAncestor(this);
		Frame owner = null;
		if (parent != null && parent instanceof Frame)
		{
			owner = (Frame)parent;
		}
		ExecuteSqlDialog dialog = new ExecuteSqlDialog(owner, title, sql, indexName, this.dbConnection);
		dialog.setStartButtonText(ResourceMgr.getString("TxtCreateIndex"));
		dialog.show();
		this.shouldRetrieveIndexes = true;
	}

	private void dropTables()
	{
		if (this.tableList.getSelectedRowCount() == 0) return;
		int rows[] = this.tableList.getSelectedRows();
		int count = rows.length;
		if (count == 0) return;

		ArrayList names = new ArrayList(count);
		ArrayList types = new ArrayList(count);
		for (int i=0; i < count; i ++)
		{
			String name = this.tableList.getValueAsString(rows[i], DbMetadata.COLUMN_IDX_TABLE_LIST_NAME);
			String schema = this.tableList.getValueAsString(rows[i], DbMetadata.COLUMN_IDX_TABLE_LIST_SCHEMA);
      if (schema!= null && schema.length() > 0)
      {
        name = SqlUtil.quoteObjectname(schema) + "." + SqlUtil.quoteObjectname(name);
      }
      else
      {
        name = SqlUtil.quoteObjectname(name);
      }
			String type = this.tableList.getValueAsString(rows[i], DbMetadata.COLUMN_IDX_TABLE_LIST_TYPE);
			names.add(name);
			types.add(type);
		}
		ObjectDropperUI ui = new ObjectDropperUI();
		ui.setObjects(names, types);
		ui.setConnection(this.dbConnection);
		JFrame f = (JFrame)SwingUtilities.getWindowAncestor(this);
		ui.showDialog(f);
		if (!ui.dialogWasCancelled())
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					reload();
				}
			});
		}
	}

	public void spoolData()
	{
		int rowCount = this.tableList.getSelectedRowCount();
		if (rowCount <= 0) return;

		if (rowCount > 1)
		{
			this.spoolTables();
			return;
		}

		int row = this.tableList.getSelectedRow();
		if (row < 0) return;
		String table = this.tableList.getValueAsString(row, DbMetadata.COLUMN_IDX_TABLE_LIST_NAME);
		String sql = "SELECT * FROM " + SqlUtil.quoteObjectname(table);
		DataSpooler spooler = new DataSpooler();
		spooler.executeStatement(this.parentWindow, this.dbConnection, sql);
	}

	public void spoolTables()
	{
		String lastDir = WbManager.getSettings().getLastExportDir();
		JFileChooser fc = new JFileChooser(lastDir);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		fc.setDialogTitle(ResourceMgr.getString("LabelSelectDirTitle"));
		ExportOptionsPanel options = new ExportOptionsPanel();
		fc.setAccessory(options);
		int commitEvery = WbManager.getSettings().getIntProperty("workbench.dataspooler", "commitevery", -1);
		if (commitEvery > 0)
		{
			options.setCommitEvery(commitEvery);
		}

		int answer = fc.showDialog(SwingUtilities.getWindowAncestor(this), null);
		if (answer == JFileChooser.APPROVE_OPTION)
		{
			File fdir = fc.getSelectedFile();
			WbManager.getSettings().setLastExportDir(fdir.getAbsolutePath());
			commitEvery = options.getCommitEvery();
			WbManager.getSettings().setProperty("workbench.dataspooler.commitevery", Integer.toString(commitEvery));
			DataSpooler spooler = new DataSpooler();
			spooler.setConnection(this.dbConnection);
			spooler.setShowProgress(true);
			String ext = null;
			if (options.isTypeSql())
			{
				spooler.setOutputTypeSqlInsert();
				spooler.setIncludeCreateTable(options.getCreateTable());
				spooler.setCommitEvery(commitEvery);
				ext = ".sql";
			}
			else
			{
				spooler.setOutputTypeText();
				spooler.setExportHeaders(options.getIncludeTextHeader());
				ext = ".txt";
			}

			fc = null;

			int[] rows = this.tableList.getSelectedRows();
			for (int i = 0; i < rows.length; i ++)
			{
				if (rows[i] < 0) continue;
				String table = this.tableList.getValueAsString(rows[i], DbMetadata.COLUMN_IDX_TABLE_LIST_NAME);
				if (table == null) continue;

				String ttype = this.tableList.getValueAsString(rows[i], DbMetadata.COLUMN_IDX_TABLE_LIST_TYPE);
				if (ttype == null) continue;
				if (!this.isTableType(ttype.toLowerCase())) continue;
				String stmt = "SELECT * FROM " + SqlUtil.quoteObjectname(table);
				String fname = table.replaceAll("[\t\\:\\\\/\\?\\*\\|<>]", "").toLowerCase();
				File f = new File(fdir, fname + ext);
				spooler.addJob(f.getAbsolutePath(), stmt);
			}
			spooler.startExportJobs();
		}
	}

	public Window getParentWindow()
	{
		return SwingUtilities.getWindowAncestor(this);
	}

	/** Invoked when the displayed tab has changed.
	 *	Retrieve table detail information here.
	 */
	public void stateChanged(ChangeEvent e)
	{
		if (this.ignoreStateChanged) return;
    if (e.getSource() == this.displayTab)
    {
      this.startRetrieveCurrentPanel();
    }
    else
    {
			// Updating the showDataMenu needs to be posted because
			// the ChangeEvent is also triggered when a tab has been
			// removed (thus implicitely changing the index)
			// but the changeEvent occurs <b>before</b> the actual
			// pane is removed from the control.
      EventQueue.invokeLater(new Runnable()
			{
				public void run()
				{
					updateShowDataMenu();
				}
			});
    }
	}

	public void fileNameChanged(Object sender, String newFilename)
	{
		try
		{
			this.updateShowDataMenu();
		}
		catch (Exception e)
		{
			LogMgr.logError("TableListPanel.fileNameChanged()", "Error when updating the popup menu", e);

			// re-initialize the menu from scratch

			try { this.updateShowDataMenu(); } catch (Throwable th) {}
		}
	}

	public void mouseClicked(MouseEvent e)
	{
		this.shiftDown = ((e.getModifiers() & ActionEvent.SHIFT_MASK) == ActionEvent.SHIFT_MASK);
	}

	public void mouseEntered(MouseEvent e)
	{
	}

	public void mouseExited(MouseEvent e)
	{
	}

	public void mousePressed(MouseEvent e)
	{
	}

	public void mouseReleased(MouseEvent e)
	{
	}

}