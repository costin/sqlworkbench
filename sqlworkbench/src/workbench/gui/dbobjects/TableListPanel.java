/*
 * Created on 5. August 2002, 21:06
 */
package workbench.gui.dbobjects;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import workbench.WbManager;
import workbench.db.DbMetadata;
import workbench.db.WbConnection;
import workbench.gui.actions.ReloadAction;
import workbench.gui.components.*;
import workbench.gui.components.DataStoreTableModel;
import workbench.gui.sql.EditorPanel;
import workbench.interfaces.Reloadable;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.storage.DataStore;


/**
 *
 * @author  workbench@kellerer.org
 *
 */
public class TableListPanel
	extends JPanel
	implements ActionListener, ListSelectionListener, Reloadable
{
	private WbConnection dbConnection;
	private JPanel listPanel;
	private FindPanel findPanel;
	private WbTable tableList;
	private WbTable tableDefinition;
	private WbTable indexes;
	private WbTable importedKeys;
	private WbTable exportedKeys;
	private WbScrollPane importedPanel;
	private WbScrollPane exportedPanel;
	private WbScrollPane indexPanel;
	private TriggerDisplayPanel triggers;
	private EditorPanel tableSource;
	private JTabbedPane displayTab;
	private JSplitPane splitPane;
	private DbMetadata meta;
	private Object retrieveLock = new Object();
	private JComboBox tableTypes = new JComboBox();
	private String currentSchema;
	private String currentCatalog;
	private boolean shouldRetrieve;

	public TableListPanel()
		throws Exception
	{
		this.displayTab = new JTabbedPane();
		this.displayTab.setTabPlacement(JTabbedPane.BOTTOM);
		this.tableDefinition = new WbTable();
		this.tableDefinition.setAdjustToColumnLabel(false);

		JScrollPane scroll = new WbScrollPane(this.tableDefinition);
		this.displayTab.add(ResourceMgr.getString("TxtDbExplorerTableDefinition"), scroll);

		this.indexes = new WbTable();
		this.indexes.setAdjustToColumnLabel(false);
		this.indexPanel = new WbScrollPane(this.indexes);

		this.tableSource = new EditorPanel();
		this.tableSource.setEditable(false);
		this.displayTab.add(ResourceMgr.getString("TxtDbExplorerSource"), this.tableSource);

		this.importedKeys = new WbTable();
		this.importedKeys.setAdjustToColumnLabel(false);
		this.importedPanel = new WbScrollPane(this.importedKeys);

		this.exportedKeys = new WbTable();
		this.exportedKeys.setAdjustToColumnLabel(false);
		this.exportedPanel = new WbScrollPane(this.exportedKeys);

		this.triggers = new TriggerDisplayPanel();

		this.listPanel = new JPanel();
		this.tableList = new WbTable();
		this.tableList.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		this.tableList.setCellSelectionEnabled(false);
		this.tableList.setColumnSelectionAllowed(false);
		this.tableList.setRowSelectionAllowed(true);
		this.tableList.getSelectionModel().addListSelectionListener(this);
		this.tableList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.tableList.setAdjustToColumnLabel(false);

		JPanel topPanel = new JPanel();
		this.findPanel = new FindPanel(this.tableList);
		this.findPanel.addToToolbar(new ReloadAction(this), true, false);

		topPanel.setLayout(new GridBagLayout());
		this.tableTypes.setMaximumSize(new Dimension(32768, 18));
		this.tableTypes.setMaximumSize(new Dimension(80, 18));
		GridBagConstraints constr = new GridBagConstraints();
		constr.anchor = GridBagConstraints.WEST;
		topPanel.add(this.tableTypes, constr);
		//this.findPanel.toolbar.setBorder(new DividerBorder(DividerBorder.LEFT_RIGHT));
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

		this.splitPane.setLeftComponent(this.listPanel);
		this.splitPane.setRightComponent(displayTab);
		this.setLayout(new BorderLayout());
		this.add(splitPane, BorderLayout.CENTER);

		WbTraversalPolicy pol = new WbTraversalPolicy();
		pol.setDefaultComponent(findPanel);
		pol.addComponent(findPanel);
		pol.addComponent(tableList);
		pol.addComponent(tableDefinition);
		this.setFocusTraversalPolicy(pol);
		this.reset();
	}

	private void addTablePanels()
	{
		if (this.displayTab.getComponentCount() > 2) return;
		this.displayTab.add(ResourceMgr.getString("TxtDbExplorerIndexes"), this.indexPanel);
		this.displayTab.add(ResourceMgr.getString("TxtDbExplorerFkColumns"), this.importedPanel);
		this.displayTab.add(ResourceMgr.getString("TxtDbExplorerReferencedColumns"), this.exportedPanel);
		this.displayTab.add(ResourceMgr.getString("TxtDbExplorerTriggers"), this.triggers);
	}

	private void removeTablePanels()
	{
		if (this.displayTab.getComponentCount() == 2) return;
		this.displayTab.setSelectedIndex(0);
		this.displayTab.remove(this.indexPanel);
		this.displayTab.remove(this.importedPanel);
		this.displayTab.remove(this.exportedPanel);
		this.displayTab.remove(this.triggers);
	}

	public void setInitialFocus()
	{
		this.findPanel.setFocusToEntryField();
	}

	public void disconnect()
	{
		this.dbConnection = null;
		this.meta = null;
		this.reset();
	}

	private void reset()
	{
		this.tableList.reset();
		this.tableDefinition.reset();
		this.importedKeys.reset();
		this.exportedKeys.reset();
		this.indexes.reset();
		this.triggers.reset();
		this.tableSource.setText("");
	}

	public void setConnection(WbConnection aConnection)
	{
		this.dbConnection = aConnection;
		this.meta = aConnection.getMetadata();
		this.tableTypes.removeActionListener(this);
		this.triggers.setConnection(aConnection);
		this.tableSource.getSqlTokenMarker().initDatabaseKeywords(aConnection.getSqlConnection());
		this.reset();
		try
		{
			List types = this.meta.getTableTypes();
			this.tableTypes.removeAllItems();
			this.tableTypes.addItem("*");
			for (int i=0; i < types.size(); i++)
			{
				this.tableTypes.addItem(types.get(i));
			}
			this.tableTypes.setSelectedItem(null);
		}
		catch (Exception e)
		{
		}
		this.tableTypes.addActionListener(this);
	}

	public void setCatalogAndSchema(String aCatalog, String aSchema)
		throws Exception
	{
		this.reset();
		this.currentSchema = aSchema;
		this.currentCatalog = aCatalog;
		if (this.isVisible())
			this.retrieve();
		else
			this.shouldRetrieve = true;
	}

	public void retrieve()
	{
		final Container parent = this.getParent();
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					String table = (String)tableTypes.getSelectedItem();
					DataStoreTableModel rs = meta.getListOfTables(currentCatalog, currentSchema, table);
					tableList.setModel(rs, true);
					tableList.adjustColumns();
					parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					shouldRetrieve = false;
				}
				catch (Exception e)
				{
					LogMgr.logError("TableListPanel.retrieve()", "Error retrieving table list", e);
				}
			}
		});
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
		WbManager.getSettings().setProperty(this.getClass().getName(), "divider", this.splitPane.getDividerLocation());
	}

	public void restoreSettings()
	{
		int loc = WbManager.getSettings().getIntProperty(this.getClass().getName(), "divider");
		if (loc == 0) loc = 200;
		this.splitPane.setDividerLocation(loc);
		this.triggers.restoreSettings();
	}

	public void valueChanged(ListSelectionEvent e)
	{
		if (e.getValueIsAdjusting()) return;
		final int row = this.tableList.getSelectedRow();
		if (row < 0) return;
		final Container parent = this.getParent();

		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				String catalog = tableList.getValueAsString(row, DbMetadata.COLUMN_IDX_TABLE_LIST_CATALOG);
				String schema = tableList.getValueAsString(row, DbMetadata.COLUMN_IDX_TABLE_LIST_SCHEMA);
				String table = tableList.getValueAsString(row, DbMetadata.COLUMN_IDX_TABLE_LIST_NAME);
				String type = tableList.getValueAsString(row, DbMetadata.COLUMN_IDX_TABLE_LIST_TYPE);
				boolean isTable = false;

				synchronized (retrieveLock)
				{
					parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					try
					{
						tableDefinition.setModel(meta.getTableDefinitionModel(catalog, schema, table), true);
						tableDefinition.adjustColumns();
					}
					catch (Exception ex)
					{
						LogMgr.logError(this, "Could not read table definition", ex);
					}

					if (type.toLowerCase().indexOf("view") > -1)
					{
						String viewSource = meta.getViewSource(catalog, schema, table);
						tableSource.setText(viewSource);
						tableSource.setCaretPosition(0);
					}
					else if (type.toLowerCase().indexOf("table") > -1)
					{
						// the table information has to be retrieved before
						// the table source, because otherwise the DataStores
						// passed to getTableSource() would be empty
						retrieveTableInformation(catalog, schema, table);
						String sql = meta.getTableSource(table, tableDefinition.getDataStore(), indexes.getDataStore(), importedKeys.getDataStore());
						tableSource.setText(sql);
						tableSource.setCaretPosition(0);
						isTable = true;
					}
					else
					{
						tableSource.setText("");
					}
					if (!isTable)
					{
						removeTablePanels();
					}

					parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}
		});
	}

	private void retrieveTableInformation(String catalog, String schema, String table)
	{
		addTablePanels();
		triggers.readTriggers(catalog, schema, table);

		DataStoreTableModel model = new DataStoreTableModel(meta.getForeignKeys(catalog, schema, table));
		importedKeys.setModel(model, true);
		importedKeys.adjustColumns();
		model = new DataStoreTableModel(meta.getReferencedBy(catalog, schema, table));
		exportedKeys.setModel(model, true);
		exportedKeys.adjustColumns();

		indexes.setModel(meta.getTableIndexes(catalog, schema, table), true);
		indexes.adjustColumns();
	}

	public void reload()
	{
		this.reset();
		this.retrieve();
	}
	/** Invoked when an action occurs.
	 *
	 */
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == this.tableTypes)
		{
			try { this.retrieve(); } catch (Exception ex) {}
		}
	}
	public static void main(String args[])
	{
		Connection con = null;
		try
		{
			//JFrame f = new JFrame("Test");
			//Class.forName("com.inet.tds.TdsDriver");
			//Class.forName("oracle.jdbc.OracleDriver");
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			//final Connection con = DriverManager.getConnection("jdbc:inetdae:demsqlvisa02:1433?database=visa_cpl_test", "visa", "savivisa");
			//final Connection con = DriverManager.getConnection("jdbc:oracle:thin:@DEMRDB34:1521:SBL1", "sadmin", "sadmin");
			//con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:oradb", "auto", "auto");
			con = DriverManager.getConnection("jdbc:odbc:Auto", "auto", "auto");

			DatabaseMetaData meta = con.getMetaData();
			System.out.println(meta.getDatabaseProductName());
			System.out.println(meta.getCatalogTerm());
			/*
			ResultSet rs;
			rs = meta.getImportedKeys(null, "AUTO", "EXPENSE");
			while (rs.next())
			{
				System.out.println("column=" + rs.getString(4) + ", fk_table=" + rs.getString(7) + ", fk_col=" + rs.getString(8));
			}
			rs.close();
			*/
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try { con.close(); } catch (Throwable th) {}
		}
	}


}
