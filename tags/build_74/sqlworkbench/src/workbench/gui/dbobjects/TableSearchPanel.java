/*
 * TableSearchPanel.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2004, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: info@sql-workbench.net
 *
 */
package workbench.gui.dbobjects;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import workbench.db.DbMetadata;
import workbench.db.TableIdentifier;
import workbench.db.TableSearcher;
import workbench.db.WbConnection;
import workbench.gui.WbSwingUtilities;
import workbench.gui.actions.ReloadAction;
import workbench.gui.components.DataStoreTableModel;
import workbench.gui.components.EmptyTableModel;
import workbench.gui.components.TabbedPaneUIFactory;
import workbench.gui.components.WbScrollPane;
import workbench.gui.components.WbSplitPane;
import workbench.gui.components.WbTable;
import workbench.gui.components.WbToolbarButton;
import workbench.gui.sql.EditorPanel;
import workbench.interfaces.ShareableDisplay;
import workbench.interfaces.TableSearchDisplay;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.storage.DataStore;
import workbench.util.Like;


/**
 *
 * @author  info@sql-workbench.net
 */
public class TableSearchPanel
	extends JPanel
	implements TableSearchDisplay, ListSelectionListener, KeyListener
{
	private DataStore tableList;
	private TableModel tableListModel;
	//private String currentTable;
	private String currentSql;
	private TableSearcher searcher;
	private WbConnection connection;
	private boolean tableLogged;
	private String fixedStatusText;
	private ShareableDisplay tableListSource;
	private DataStore currentResult;
	private Dimension maxTableSize = new Dimension(32768, 150);
	private WbTable currentDisplayTable;
	private JScrollPane currentScrollPane;
	private TitledBorder currentBorder;
	private Like searchPattern;
	private WbTable firstTable;
	private EditorPanel sqlDisplay;

	public TableSearchPanel(ShareableDisplay aTableListSource)
	{
		this.tableListModel = new EmptyTableModel();
		this.tableListSource = aTableListSource;
		initComponents();
		this.resultTabPane.setUI(TabbedPaneUIFactory.getBorderLessUI());
		this.resultTabPane.setBorder(WbSwingUtilities.EMPTY_BORDER);

		sqlDisplay = EditorPanel.createSqlEditor();
		this.resultTabPane.addTab(ResourceMgr.getString("LabelTableSearchSqlLog"), sqlDisplay);

		WbTable tables = (WbTable)this.tableNames;
		tables.setAdjustToColumnLabel(false);

		WbToolbarButton reload = (WbToolbarButton)this.reloadButton;
		Border b = reload.getBorder();
		reload.setBorder(new CompoundBorder(b, new EmptyBorder(1,1,1,1)));
		reload.setAction(new ReloadAction(this.tableListSource));
		reload.setToolTipText(ResourceMgr.getString("TxtRefreshTableList"));

		this.searcher = new TableSearcher();
		this.searcher.setDisplay(this);
		//this.searchResult.setFont(Settings.getInstance().getMsgLogFont());
		this.tableNames.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		this.fixedStatusText = ResourceMgr.getString("TxtSearchingTable") + " ";
		((WbSplitPane)this.jSplitPane1).setDividerBorder(WbSwingUtilities.EMPTY_BORDER);
		tables.getSelectionModel().addListSelectionListener(this);
		this.startButton.setEnabled(false);
		this.searchText.addKeyListener(this);
		Border eb = new EmptyBorder(0,2,0,0);
		CompoundBorder b2 = new CompoundBorder(this.statusInfo.getBorder(), eb);
		this.statusInfo.setBorder(b2);
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
  private void initComponents()//GEN-BEGIN:initComponents
  {
    java.awt.GridBagConstraints gridBagConstraints;

    buttonGroup1 = new javax.swing.ButtonGroup();
    jSplitPane1 = new WbSplitPane();
    resultTabPane = new javax.swing.JTabbedPane();
    resultScrollPane = new WbScrollPane();
    resultPanel = new javax.swing.JPanel();
    tablePane = new javax.swing.JPanel();
    tableListScrollPane = new WbScrollPane();
    tableNames = new WbTable();
    selectButtonPanel = new javax.swing.JPanel();
    selectAllButton = new javax.swing.JButton();
    jPanel2 = new javax.swing.JPanel();
    selectNoneButton = new javax.swing.JButton();
    statusInfo = new javax.swing.JLabel();
    entryPanel = new javax.swing.JPanel();
    startButton = new javax.swing.JButton();
    searchText = new javax.swing.JTextField();
    jLabel1 = new javax.swing.JLabel();
    reloadButton = new WbToolbarButton();
    columnFunction = new javax.swing.JTextField();
    labelRowCount = new javax.swing.JLabel();
    rowCount = new javax.swing.JTextField();

    setLayout(new java.awt.BorderLayout());

    jSplitPane1.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(1, 1, 1, 1)));
    jSplitPane1.setDividerLocation(150);
    resultScrollPane.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    resultPanel.setLayout(new java.awt.GridBagLayout());

    resultPanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
    resultScrollPane.setViewportView(resultPanel);

    resultTabPane.addTab(ResourceMgr.getString("LabelTableSearchResultTab"), resultScrollPane);

    jSplitPane1.setRightComponent(resultTabPane);

    tablePane.setLayout(new java.awt.BorderLayout());

    tableNames.setModel(this.tableListModel);
    tableListScrollPane.setViewportView(tableNames);

    tablePane.add(tableListScrollPane, java.awt.BorderLayout.CENTER);

    selectButtonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 3));

    selectAllButton.setText(ResourceMgr.getString("LabelSelectAll"));
    selectAllButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        selectAllButtonActionPerformed(evt);
      }
    });

    selectButtonPanel.add(selectAllButton);

    jPanel2.setMaximumSize(new java.awt.Dimension(5, 0));
    jPanel2.setMinimumSize(new java.awt.Dimension(4, 0));
    jPanel2.setPreferredSize(new java.awt.Dimension(4, 0));
    selectButtonPanel.add(jPanel2);

    selectNoneButton.setText(ResourceMgr.getString("LabelSelectNone"));
    selectNoneButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        selectNoneButtonActionPerformed(evt);
      }
    });

    selectButtonPanel.add(selectNoneButton);

    tablePane.add(selectButtonPanel, java.awt.BorderLayout.SOUTH);

    jSplitPane1.setLeftComponent(tablePane);

    add(jSplitPane1, java.awt.BorderLayout.CENTER);

    statusInfo.setBorder(new javax.swing.border.EtchedBorder());
    statusInfo.setMinimumSize(new java.awt.Dimension(4, 22));
    statusInfo.setPreferredSize(new java.awt.Dimension(4, 22));
    add(statusInfo, java.awt.BorderLayout.SOUTH);

    entryPanel.setLayout(new java.awt.GridBagLayout());

    startButton.setText(ResourceMgr.getString("LabelStartSearch"));
    startButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        startButtonActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 1);
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    entryPanel.add(startButton, gridBagConstraints);

    searchText.setColumns(20);
    searchText.setText("% ... %");
    searchText.setToolTipText(ResourceMgr.getDescription("LabelSearchTableCriteria"));
    searchText.setMinimumSize(new java.awt.Dimension(100, 20));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 4;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    entryPanel.add(searchText, gridBagConstraints);

    jLabel1.setText("LIKE");
    jLabel1.setToolTipText(ResourceMgr.getDescription("LabelSearchTableCriteria"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
    entryPanel.add(jLabel1, gridBagConstraints);

    reloadButton.setText("jButton1");
    reloadButton.setBorder(new javax.swing.border.EtchedBorder());
    reloadButton.setMaximumSize(new java.awt.Dimension(24, 24));
    reloadButton.setMinimumSize(new java.awt.Dimension(24, 24));
    reloadButton.setPreferredSize(new java.awt.Dimension(24, 24));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 0);
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    entryPanel.add(reloadButton, gridBagConstraints);

    columnFunction.setColumns(8);
    columnFunction.setText("$col$");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    entryPanel.add(columnFunction, gridBagConstraints);

    labelRowCount.setLabelFor(rowCount);
    labelRowCount.setText(ResourceMgr.getString("LabelMaxRows"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 5;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    entryPanel.add(labelRowCount, gridBagConstraints);

    rowCount.setColumns(4);
    rowCount.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    rowCount.setText("0");
    rowCount.setMinimumSize(new java.awt.Dimension(30, 20));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 6;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 5);
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    entryPanel.add(rowCount, gridBagConstraints);

    add(entryPanel, java.awt.BorderLayout.NORTH);

  }//GEN-END:initComponents

	private void selectNoneButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_selectNoneButtonActionPerformed
	{//GEN-HEADEREND:event_selectNoneButtonActionPerformed
		this.tableNames.getSelectionModel().removeSelectionInterval(0, this.tableNames.getRowCount() - 1);
	}//GEN-LAST:event_selectNoneButtonActionPerformed

	private void selectAllButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_selectAllButtonActionPerformed
	{//GEN-HEADEREND:event_selectAllButtonActionPerformed
		this.tableNames.getSelectionModel().setSelectionInterval(0, this.tableNames.getRowCount() - 1);
	}//GEN-LAST:event_selectAllButtonActionPerformed

	private void startButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_startButtonActionPerformed
	{//GEN-HEADEREND:event_startButtonActionPerformed
		if (this.searcher.isRunning())
		{
			this.searcher.cancelSearch();
		}
		else
		{
			this.searchData();
		}
	}//GEN-LAST:event_startButtonActionPerformed

	private void adjustDataTable()
	{
		if (this.currentDisplayTable != null)
		{
			int rows = this.currentDisplayTable.getRowCount();
			int cols = this.currentDisplayTable.getColumnCount();
			int height = this.currentDisplayTable.getRowHeight();
			int width = this.resultScrollPane.getWidth();
			// Recycle the Dimension object from the ScrollPane
			String label = this.currentBorder.getTitle();
			label = label + " (" + rows + " " + (rows == 1 ? ResourceMgr.getString("TxtFoundRow") : ResourceMgr.getString("TxtFoundRows")) + ")";
			this.currentBorder.setTitle(label);
			Dimension size = this.currentScrollPane.getPreferredSize();
			if (rows > 25) rows = 25;
			size.setSize(width - 20, (rows + 4) * height );
			this.currentScrollPane.setPreferredSize(size);
		}
	}

	public synchronized void addResultRow(String aTablename, ResultSet aResult)
	{
		try
		{
			if (!this.tableLogged)
			{
				// Adjust the last table (which is now completed), before creating the new one
				// Note: this will not adjust the table if only one database table was searched
				// (or only results from one database table where returned)
				// therefor it's important to call this in searchEnded() as well
				this.adjustDataTable();
				this.currentDisplayTable = new WbTable();
				this.currentDisplayTable.setUseDefaultStringRenderer(false);
				if (this.firstTable == null)
				{
					this.firstTable = this.currentDisplayTable;
				}
				this.currentDisplayTable.setDefaultRenderer(String.class, new ResultHighlightingRenderer(this.searchPattern));
				this.currentResult = new DataStore(aResult);
				DataStoreTableModel model = new DataStoreTableModel(this.currentResult);
				this.currentDisplayTable.setModel(model, true);
				this.currentScrollPane  = new ParentWidthScrollPane(this.currentDisplayTable);
				TitledBorder b = new TitledBorder(aTablename);
				this.currentBorder = b;
				Font f = b.getTitleFont();
				f = f.deriveFont(Font.BOLD);
				b.setTitleFont(f);
				b.setBorder(new EtchedBorder());
				this.currentScrollPane.setBorder(b);
				GridBagConstraints constraints = new GridBagConstraints();
				constraints.gridx = 0;
				constraints.fill = GridBagConstraints.HORIZONTAL;
				constraints.weightx = 1.0;
				constraints.anchor = GridBagConstraints.WEST;
				this.resultPanel.add(this.currentScrollPane, constraints);
				this.tableLogged = true;
			}
			this.currentResult.addRow(aResult);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 *	Call back function from the table searcher...
	 */
	public synchronized void setCurrentTable(String aTablename, String aSql)
	{
		//this.currentTable = aTablename;
		this.currentSql = aSql;
		this.tableLogged = false;
		this.currentResult = null;
		this.statusInfo.setText(this.fixedStatusText + aTablename);
		this.sqlDisplay.appendLine(aSql);
		this.sqlDisplay.appendLine(";\n\n");
	}

	public void setStatusText(String aStatustext)
	{
		this.statusInfo.setText(aStatustext);
	}
	/** Getter for property connection.
	 * @return Value of property connection.
	 *
	 */
	public WbConnection getConnection()
	{
		return connection;
	}

	/** Setter for property connection.
	 * @param connection New value of property connection.
	 *
	 */
	public void setConnection(WbConnection connection)
	{
		this.connection = connection;
		this.searcher.setConnection(connection);
		this.tableListSource.addTableListDisplayClient(this.tableNames);
	}

	public void disconnect()
	{
		this.resetResult();
		this.tableListSource.removeTableListDisplayClient(this.tableNames);
	}

	private void resetResult()
	{
    // resultPanel.removeAll() does not work
    // the old tables just stay in there
    // so I re-create the actual result panel
		this.resultPanel = new JPanel();
    this.resultPanel.setLayout(new GridBagLayout());
    this.resultScrollPane.setViewportView(resultPanel);
		this.sqlDisplay.setText("");
		this.firstTable = null;
	}

	public void searchData()
	{
		if (!searcher.setColumnFunction(this.columnFunction.getText()))
		{
			WbSwingUtilities.showErrorMessage(this, ResourceMgr.getString("MsgErrorColFunction"));
			return;
		}

		if (this.tableNames.getSelectedRowCount() == 0) return;
		this.resetResult();

		int[] selectedTables = this.tableNames.getSelectedRows();
		ArrayList searchTables = new ArrayList(this.tableNames.getSelectedRowCount());
		DataStore tables = ((WbTable)(this.tableNames)).getDataStore();
		for (int i=0; i < selectedTables.length; i++)
		{
			StringBuffer table = new StringBuffer(100);
			String type = tables.getValueAsString(selectedTables[i], DbMetadata.COLUMN_IDX_TABLE_LIST_TYPE);

			String schema = tables.getValueAsString(selectedTables[i], DbMetadata.COLUMN_IDX_TABLE_LIST_SCHEMA);
			String tablename = tables.getValueAsString(selectedTables[i], DbMetadata.COLUMN_IDX_TABLE_LIST_NAME);

			if ("synonym".equalsIgnoreCase(type))
			{
				TableIdentifier id = this.connection.getMetadata().getSynonymTable(schema, tablename);
				if (id != null)
				{
					schema = id.getSchema();
					tablename = id.getTable();
				}
			}


			if (schema != null && schema.length() > 0)
			{
				table.append(schema);
				table.append('.');
			}
			table.append(tablename);
			searchTables.add(table.toString());
		}
		int maxRows = 0;
		try
		{
			maxRows = Integer.parseInt(this.rowCount.getText());
		}
		catch (Exception e)
		{
			maxRows = 0;
		}
		String text = this.searchText.getText();
		searcher.setMaxRows(maxRows);
		searcher.setCriteria(text);
		boolean sensitive= this.connection.getMetadata().isStringComparisonCaseSensitive();
		boolean ignoreCase = !sensitive;
		if (sensitive)
		{
			ignoreCase = searcher.getCriteriaMightBeCaseInsensitive();
		}
		this.searchPattern = new Like(searcher.getCriteria(), ignoreCase);
		searcher.setTableNames(searchTables);
		searcher.search(); // starts the background thread
	}

	public void saveSettings()
	{
		Settings s = Settings.getInstance();
		String cl = this.getClass().getName();
		s.setProperty(cl, "divider", this.jSplitPane1.getDividerLocation());
		s.setProperty(cl, "criteria", this.searchText.getText());
		s.setProperty(cl, "maxrows", this.rowCount.getText());
		s.setProperty(cl, "column-function", this.columnFunction.getText());
	}

	public void restoreSettings()
	{
		Settings s = Settings.getInstance();
		String cl = this.getClass().getName();
		int loc = s.getIntProperty(cl, "divider");
		if (loc == 0) loc = 200;
		this.jSplitPane1.setDividerLocation(loc);
		this.searchText.setText(s.getProperty(cl, "criteria", ""));
		this.rowCount.setText(s.getProperty(cl, "maxrows", "0"));
		this.columnFunction.setText(s.getProperty(cl, "column-function", "$col$"));
	}

	public void searchEnded()
	{
		this.adjustDataTable();
		if (this.firstTable != null)
		{
			int height = this.firstTable.getRowHeight();
			JScrollBar sb = this.resultScrollPane.getVerticalScrollBar();
			sb.setUnitIncrement(height);
			sb.setBlockIncrement(height * 5);
		}
		// insert a dummy panel at the end which will move
		// all tables in the pane to the upper border
		// e.g. when there is only one table
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.weighty = 1.0;
		constraints.anchor = GridBagConstraints.WEST;
		this.resultPanel.add(new JPanel(), constraints);

		this.resultPanel.doLayout();
		this.searchText.setEnabled(true);
		this.columnFunction.setEnabled(true);
		startButton.setText(ResourceMgr.getString("LabelStartSearch"));
		this.statusInfo.setText("");
	}

	public void searchStarted()
	{
		this.searchText.setEnabled(false);
		this.columnFunction.setEnabled(false);
		startButton.setText(ResourceMgr.getString("LabelCancelSearch"));
	}

	public void valueChanged(javax.swing.event.ListSelectionEvent e)
	{
		this.startButton.setEnabled(this.tableNames.getSelectedRowCount() > 0);
	}

	public void keyPressed(java.awt.event.KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_ENTER)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					searchData();
				}
			}
			);
		}
	}

	public void keyReleased(java.awt.event.KeyEvent e)
	{
	}

	public void keyTyped(java.awt.event.KeyEvent e)
	{
	}

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.ButtonGroup buttonGroup1;
  private javax.swing.JTextField columnFunction;
  private javax.swing.JPanel entryPanel;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JSplitPane jSplitPane1;
  private javax.swing.JLabel labelRowCount;
  private javax.swing.JButton reloadButton;
  private javax.swing.JPanel resultPanel;
  private javax.swing.JScrollPane resultScrollPane;
  private javax.swing.JTabbedPane resultTabPane;
  private javax.swing.JTextField rowCount;
  private javax.swing.JTextField searchText;
  private javax.swing.JButton selectAllButton;
  private javax.swing.JPanel selectButtonPanel;
  private javax.swing.JButton selectNoneButton;
  private javax.swing.JButton startButton;
  private javax.swing.JLabel statusInfo;
  private javax.swing.JScrollPane tableListScrollPane;
  private javax.swing.JTable tableNames;
  private javax.swing.JPanel tablePane;
  // End of variables declaration//GEN-END:variables

	class ParentWidthScrollPane
		extends JScrollPane
	{
		private Dimension preferredSize = new Dimension(0,0);

		public ParentWidthScrollPane(Component view)
		{
			super(view);
		}
		public Dimension getPreferredSize()
		{
			Dimension d = super.getPreferredSize();
			Container parent = this.getParent();
			this.preferredSize.setSize( (double)parent.getWidth() - 5, d.getHeight());
			return this.preferredSize;
		}
	}

	class ResultHighlightingRenderer
		extends DefaultTableCellRenderer
	{
		private Like pattern;
		private Color background = UIManager.getColor("Table.background");
		private Color foreground = UIManager.getColor("Table.foreground");
		private Color selectBack = UIManager.getColor("Table.selectionBackground");
		private Color selectText = UIManager.getColor("Table.selectionForeground");

		public ResultHighlightingRenderer(Like aPattern)
		{
			this.pattern = aPattern;
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			JLabel result = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			try
			{
				if (!isSelected && value != null && value instanceof String && this.pattern.like((String)value))
				{
					result.setBackground(Color.YELLOW);
					result.setForeground(Color.BLACK);
				}
				else if (isSelected)
				{
					result.setBackground(selectBack);
					result.setForeground(selectText);
				}
				else
				{
					result.setBackground(background);
					result.setForeground(foreground);
				}
			}
			catch (Exception e)
			{
				result.setBackground(background);
				result.setForeground(foreground);
			}
			return result;
		}


	}

}
