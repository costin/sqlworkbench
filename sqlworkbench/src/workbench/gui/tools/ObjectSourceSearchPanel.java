/*
 * ObjectSourceSearchPanel
 * 
 *  This file is part of SQL Workbench/J, http://www.sql-workbench.net
 * 
 *  Copyright 2002-2009, Thomas Kellerer
 *  No part of this code maybe reused without the permission of the author
 * 
 *  To contact the author please send an email to: support@sql-workbench.net
 */
package workbench.gui.tools;

import java.awt.EventQueue;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import workbench.WbManager;
import workbench.db.ConnectionMgr;
import workbench.db.ConnectionProfile;
import workbench.db.DbObject;
import workbench.db.WbConnection;
import workbench.db.search.ObjectSourceSearcher;
import workbench.gui.MainWindow;
import workbench.gui.WbSwingUtilities;
import workbench.gui.components.DataStoreTableModel;
import workbench.gui.components.DividerBorder;
import workbench.gui.components.GenericRowMonitor;
import workbench.gui.components.RunningJobIndicator;
import workbench.gui.components.WbScrollPane;
import workbench.gui.components.WbSplitPane;
import workbench.gui.components.WbTable;
import workbench.gui.dbobjects.DbObjectSourcePanel;
import workbench.gui.profiles.ProfileSelectionDialog;
import workbench.interfaces.StatusBar;
import workbench.interfaces.ToolWindow;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.sql.wbcommands.ObjectResultListDataStore;
import workbench.storage.DataStore;
import workbench.util.StringUtil;
import workbench.util.WbThread;

/**
 *
 * @author Thomas Kellerer
 */
public class ObjectSourceSearchPanel
	extends JPanel
	implements ListSelectionListener, WindowListener, StatusBar, ToolWindow
{
	private boolean standalone;
	private WbConnection connection;
	private ObjectSourceSearcher searcher;
	private GenericRowMonitor rowMonitor;
	private JFrame window;

	private WbTable results;
	private DbObjectSourcePanel objectSource;
	private DataStore emptyResult;
	private WbThread searchThread;
	
	public ObjectSourceSearchPanel()
	{
		initComponents();
		rowMonitor = new GenericRowMonitor(this);
		checkButtons();
		results = new WbTable(true, false, false);
		emptyResult = new ObjectResultListDataStore();
		WbScrollPane scroll = new WbScrollPane(results);
		results.setModel(new DataStoreTableModel(emptyResult), true);
		results.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		results.getSelectionModel().addListSelectionListener(this);
		
		((WbSplitPane)splitPane).setDividerBorder(WbSwingUtilities.EMPTY_BORDER);

		objectSource = new DbObjectSourcePanel(null, null);
		objectSource.setEditable(false);
		
		splitPane.setRightComponent(objectSource);
		splitPane.setLeftComponent(scroll);

		Border b = new CompoundBorder(new DividerBorder(DividerBorder.BOTTOM), new EmptyBorder(5,5,5,5));
		topPanel.setBorder(b);
	}

	protected void clearSearch()
	{
		DataStoreTableModel model = new DataStoreTableModel(emptyResult);
		results.setModel(model, true);
	}

	protected void startSearch()
	{
		clearSearch();
		searcher = new ObjectSourceSearcher(connection);
		searcher.setRowMonitor(rowMonitor);

		startButton.setText(ResourceMgr.getString("LblCancelSearch"));
		
		List<String> schemas = StringUtil.stringToList(schemaNames.getText(), ",", true, true, false);
		List<String> names = StringUtil.stringToList(objectNames.getText(), ",", true, true, false);
		List<String> types = StringUtil.stringToList(objectTypes.getText(), ",", true, true, false);

		searcher.setSchemasToSearch(schemas);
		searcher.setNamesToSearch(names);
		searcher.setTypesToSearch(types);

		final List<String> values = StringUtil.stringToList(searchValues.getText(), ",", true, true, false);
		
		searchThread = new WbThread("SourceSearch")
		{
			public void run()
			{
				try
				{
					List<DbObject> result = searcher.searchObjects(values,matchAll.isSelected(), ignoreCase.isSelected(),
						regex.isSelected());
					showResult(result);
				}
				catch (Exception e)
				{
					LogMgr.logError("ObjectSourceSearchPanel.startSearch()", "Error while searching", e);
				}
				finally
				{
					searchEnded();
				}
			}
		};
		selectConnection.setEnabled(false);
		searchThread.start();
	}

	protected void searchEnded()
	{
		searchThread = null;
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				clearStatusMessage();
				checkButtons();
			}
		});
	}
	
	protected void checkButtons()
	{
		if (searcher != null && searcher.isRunning())
		{
			startButton.setText(ResourceMgr.getString("LblCancelSearch"));
			selectConnection.setEnabled(false);
		}
		else
		{
			startButton.setText(ResourceMgr.getString("LblStartSearch"));
			startButton.setEnabled(this.connection != null);
			selectConnection.setEnabled(true);
		}
	}

	protected void showResult(List<DbObject> result)
		throws SQLException
	{
		try
		{
			ObjectResultListDataStore ds = new ObjectResultListDataStore();
			ds.setResultList(connection, result, searcher.getSearchSchemaCount() > 1);
			setModel(ds);
		}
		finally
		{
			checkButtons();
		}
	}

	protected void setModel(final DataStore data)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				DataStoreTableModel model = new DataStoreTableModel(data);
				results.setModel(model, true);
				results.adjustRowsAndColumns();
			}
		});
	}

	protected void cancelSearch()
	{
		if (searcher != null)
		{
			searcher.cancelSearch();
			searcher = null;
		}
	}

	protected void selectConnection()
	{
		String profilekey = "workbench.objectsearcher.lastprofile";
		
		ConnectionProfile prof = null;
		try
		{
			WbSwingUtilities.showWaitCursor(this.window);
			ProfileSelectionDialog dialog = new ProfileSelectionDialog(this.window, true, profilekey);
			WbSwingUtilities.center(dialog, this.window);
			WbSwingUtilities.showDefaultCursor(this.window);
			dialog.setVisible(true);
			boolean cancelled = dialog.isCancelled();
			if (!cancelled)
			{
				prof = dialog.getSelectedProfile();
				if (prof != null)
				{
					Settings.getInstance().setProperty(profilekey, prof.getName());
				}
				else
				{
					LogMgr.logError("ObjectSourceSearchPanel.selectConnection()", "NULL Profile selected!", null);
				}
			}
			dialog.setVisible(false);
			dialog.dispose();
		}
		catch (Throwable th)
		{
			LogMgr.logError("ObjectSourceSearchPanel.selectConnection()", "Error during connect", th);
			prof = null;
		}
		if (prof != null)
		{
			connect(prof);
		}
	}

	protected void connect(final ConnectionProfile profile)
	{
		clearSearch();
		setStatusMessage(ResourceMgr.getString("MsgConnecting"));
		//WbSwingUtilities.repaintNow(statusbar);
		WbSwingUtilities.showWaitCursor(window);
		
		WbThread t = new WbThread("Connection")
		{
			public void run()
			{
				try
				{
					connection = ConnectionMgr.getInstance().getConnection(profile, "ObjectSearcher");
					connectEnded(null);
				}
				catch (Exception e)
				{
					LogMgr.logError("ObjectSourceSearchPanel.connect()", "Error during connect", e);
					String msg = ResourceMgr.getString("ErrConnectionError") + "\n" + e.getMessage();
					connection = null;
					connectEnded(msg);
				}
			}
		};
		t.start();
	}

	protected void connectEnded(String error)
	{
		WbSwingUtilities.showDefaultCursor(window);
		if (error != null)
		{
			WbSwingUtilities.showErrorMessage(this, error);
		}
		objectSource.setDatabaseConnection(connection);
		EventQueue.invokeLater(new Runnable()		
		{
			public void run()
			{
				clearStatusMessage();
				checkButtons();
				updateWindowTitle();
			}
		});
	}

	protected void updateWindowTitle()
	{
		String title = ResourceMgr.getString("TxtWindowTitleObjectSearcher");
		if (this.connection != null && connection.getProfile() != null)
		{
			String profileName = connection.getProfile().getName();
			title = title + " [" + profileName + "]";
		}

		if (searcher != null && searcher.isRunning())
		{
			title = RunningJobIndicator.TITLE_PREFIX + title;
		}
		window.setTitle(title);
	}

	public void setStatusMessage(String message)
	{
		statusbar.setText(message);
	}

	public void clearStatusMessage()
	{
		statusbar.setText("");
	}

	public String getText()
	{
		return statusbar.getText();
	}

	public void saveSettings()
	{
		Settings s = Settings.getInstance();
		s.setProperty("workbench.objectsearcher.ignorecase", ignoreCase.isSelected());
		s.setProperty("workbench.objectsearcher.matchall", matchAll.isSelected());
		s.setProperty("workbench.objectsearcher.regex", regex.isSelected());
		s.storeWindowSize(window, "workbench.objectsearcher.window");
		s.setProperty("workbench.objectsearcher.searchvalues", searchValues.getText());
		s.setProperty("workbench.objectsearcher.schemas", schemaNames.getText());
		s.setProperty("workbench.objectsearcher.objectnames", objectNames.getText());
		s.setProperty("workbench.objectsearcher.objecttypes", objectTypes.getText());
		int location = splitPane.getDividerLocation();
		s.setProperty("workbench.objectsearcher.divider", location);
	}

	public void restoreSettings()
	{
		Settings s = Settings.getInstance();
		
		ignoreCase.setSelected(s.getBoolProperty("workbench.objectsearcher.ignorecase", true));
		matchAll.setSelected(s.getBoolProperty("workbench.objectsearcher.matchall", false));
		regex.setSelected(s.getBoolProperty("workbench.objectsearcher.regex", false));

		if (!s.restoreWindowSize(window, "workbench.objectsearcher.window"))
		{
			window.setSize(800,600);
		}

		searchValues.setText(s.getProperty("workbench.objectsearcher.searchvalues", ""));
		schemaNames.setText(s.getProperty("workbench.objectsearcher.schemas", ""));
		objectNames.setText(s.getProperty("workbench.objectsearcher.objectnames", ""));
		objectTypes.setText(s.getProperty("workbench.objectsearcher.objecttypes", ""));

		int location = s.getIntProperty("workbench.objectsearcher.divider", 200);
		splitPane.setDividerLocation(location);
	}

	protected void unregister()
	{
		WbManager.getInstance().unregisterToolWindow(this);
	}
	
	protected void done()
	{
		cancelSearch();
		saveSettings();

		if (standalone)
		{
			// Unregister will actually close the application
			// as this is the only (and thus last) window that is open#
			// WbManager will also take care of disconnecting everything
			unregister();
		}
		else
		{
			Thread t = new WbThread("DataPumper disconnect thread")
			{
				public void run()
				{
					disconnect();
					unregister();
				}
			};
			t.start();
		}
	}
	
	public void closeWindow()
	{
		this.done();
		if (this.window != null)
		{
			this.window.removeWindowListener(this);
			this.window.dispose();
		}
	}

	public void disconnect()
	{
		if (connection != null)
		{
			connection.disconnect();
		}
	}

	public void activate()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public WbConnection getConnection()
	{
		return connection;
	}

	public JFrame getWindow()
	{
		return window;
	}

	public void showWindow()
	{
		standalone = true;
		showWindow(null);
	}

	public void showWindow(final MainWindow parent)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				_showWindow(parent);
			}
		});
	}

	public void _showWindow(MainWindow parent)
	{
		this.window  = new JFrame(ResourceMgr.getString("TxtWindowTitleObjectSearcher"))
		{
			public void setVisible(boolean visible)
			{
				if (!visible) saveSettings();
				super.setVisible(visible);
			}
		};

		this.window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.window.setIconImage(ResourceMgr.getPng("searchsource16").getImage());
		this.window.getContentPane().add(this);
		this.restoreSettings();
		this.window.addWindowListener(this);
		WbManager.getInstance().registerToolWindow(this);

		if (parent == null)
		{
			if (!Settings.getInstance().restoreWindowPosition(this.window, "workbench.objectsearcher.window"))
			{
				WbSwingUtilities.center(this.window, null);
			}
		}
		else
		{
			WbSwingUtilities.center(this.window, parent);
		}

		this.window.setVisible(true);
		if (Settings.getInstance().getAutoConnectObjectSearcher() && parent != null)
		{
			final ConnectionProfile profile = parent.getCurrentProfile();
			if (profile != null)
			{
				EventQueue.invokeLater(new Runnable()
				{
					public void run()
					{
						connect(profile);
					}
				});
			}
		}
	}

	public void valueChanged(ListSelectionEvent e)
	{
		int row = results.getSelectedRow();
		String source = results.getValueAsString(row, ObjectResultListDataStore.COL_IDX_SOURCE);
		objectSource.setText(source);
	}

	public void windowOpened(WindowEvent e)
	{
	}

	public void windowClosing(WindowEvent e)
	{
		closeWindow();
	}

	public void windowClosed(WindowEvent e)
	{
	}

	public void windowIconified(WindowEvent e)
	{
	}

	public void windowDeiconified(WindowEvent e)
	{
	}

	public void windowActivated(WindowEvent e)
	{
	}

	public void windowDeactivated(WindowEvent e)
	{
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    topPanel = new javax.swing.JPanel();
    schemaLabel = new javax.swing.JLabel();
    schemaNames = new javax.swing.JTextField();
    nameLabel = new javax.swing.JLabel();
    objectNames = new javax.swing.JTextField();
    valueLabel = new javax.swing.JLabel();
    searchValues = new javax.swing.JTextField();
    jPanel1 = new javax.swing.JPanel();
    matchAll = new javax.swing.JCheckBox();
    ignoreCase = new javax.swing.JCheckBox();
    regex = new javax.swing.JCheckBox();
    typeLabel = new javax.swing.JLabel();
    objectTypes = new javax.swing.JTextField();
    resultContainer = new javax.swing.JPanel();
    splitPane = new WbSplitPane();
    footerPanel = new javax.swing.JPanel();
    statusbar = new javax.swing.JLabel();
    buttonPanel = new javax.swing.JPanel();
    startButton = new javax.swing.JButton();
    jButton1 = new javax.swing.JButton();
    closeButton = new javax.swing.JButton();
    selectConnection = new javax.swing.JButton();

    setLayout(new java.awt.BorderLayout());

    topPanel.setLayout(new java.awt.GridBagLayout());

    schemaLabel.setText(ResourceMgr.getString("LblSchemas")); // NOI18N
    schemaLabel.setToolTipText(ResourceMgr.getString("d_LblSchemas")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    topPanel.add(schemaLabel, gridBagConstraints);

    schemaNames.setToolTipText(ResourceMgr.getString("d_LblSchemas")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
    topPanel.add(schemaNames, gridBagConstraints);

    nameLabel.setText(ResourceMgr.getString("LblObjectNames")); // NOI18N
    nameLabel.setToolTipText(ResourceMgr.getString("d_LblObjectNames")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
    topPanel.add(nameLabel, gridBagConstraints);

    objectNames.setToolTipText(ResourceMgr.getString("d_LblObjectNames")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.3;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
    topPanel.add(objectNames, gridBagConstraints);

    valueLabel.setText(ResourceMgr.getString("LblSearchCriteria")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(9, 0, 0, 0);
    topPanel.add(valueLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.3;
    gridBagConstraints.insets = new java.awt.Insets(10, 4, 0, 0);
    topPanel.add(searchValues, gridBagConstraints);

    jPanel1.setLayout(new java.awt.GridBagLayout());

    matchAll.setText(ResourceMgr.getString("LblSearchMatchAll")); // NOI18N
    matchAll.setBorder(null);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
    jPanel1.add(matchAll, gridBagConstraints);

    ignoreCase.setText(ResourceMgr.getString("LblSearchIgnoreCase")); // NOI18N
    ignoreCase.setBorder(null);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    jPanel1.add(ignoreCase, gridBagConstraints);

    regex.setText(ResourceMgr.getString("LblSearchRegEx")); // NOI18N
    regex.setBorder(null);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
    jPanel1.add(regex, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(11, 10, 0, 5);
    topPanel.add(jPanel1, gridBagConstraints);

    typeLabel.setText(ResourceMgr.getString("LblTypes")); // NOI18N
    typeLabel.setToolTipText(ResourceMgr.getString("d_LblTypes")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 4;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
    topPanel.add(typeLabel, gridBagConstraints);

    objectTypes.setToolTipText(ResourceMgr.getString("d_LblTypes")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 5;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 0.3;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
    topPanel.add(objectTypes, gridBagConstraints);

    add(topPanel, java.awt.BorderLayout.NORTH);

    resultContainer.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 5, 5, 5));
    resultContainer.setLayout(new java.awt.BorderLayout(5, 5));

    splitPane.setDividerLocation(200);
    resultContainer.add(splitPane, java.awt.BorderLayout.CENTER);

    add(resultContainer, java.awt.BorderLayout.CENTER);

    footerPanel.setLayout(new java.awt.GridBagLayout());

    statusbar.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), javax.swing.BorderFactory.createEmptyBorder(2, 5, 2, 5)));
    statusbar.setMaximumSize(new java.awt.Dimension(73, 32));
    statusbar.setPreferredSize(new java.awt.Dimension(100, 22));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
    footerPanel.add(statusbar, gridBagConstraints);

    buttonPanel.setLayout(new java.awt.GridBagLayout());

    startButton.setText(ResourceMgr.getString("LblStartSearch")); // NOI18N
    startButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        startButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
    buttonPanel.add(startButton, gridBagConstraints);

    jButton1.setText(ResourceMgr.getString("LblShowScript")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
    buttonPanel.add(jButton1, gridBagConstraints);

    closeButton.setText(ResourceMgr.getString("LblClose")); // NOI18N
    closeButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        closeButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 4;
    gridBagConstraints.gridy = 0;
    buttonPanel.add(closeButton, gridBagConstraints);

    selectConnection.setText(ResourceMgr.getString("LblSelectConnection")); // NOI18N
    selectConnection.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        selectConnectionActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    buttonPanel.add(selectConnection, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(9, 5, 3, 5);
    footerPanel.add(buttonPanel, gridBagConstraints);

    add(footerPanel, java.awt.BorderLayout.SOUTH);
  }// </editor-fold>//GEN-END:initComponents

	private void selectConnectionActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_selectConnectionActionPerformed
	{//GEN-HEADEREND:event_selectConnectionActionPerformed
		selectConnection();
	}//GEN-LAST:event_selectConnectionActionPerformed

	private void closeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_closeButtonActionPerformed
	{//GEN-HEADEREND:event_closeButtonActionPerformed
		closeWindow();
	}//GEN-LAST:event_closeButtonActionPerformed

	private void startButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_startButtonActionPerformed
	{//GEN-HEADEREND:event_startButtonActionPerformed
		if (searcher == null || !searcher.isRunning())
		{
			startSearch();
		}
		else
		{
			cancelSearch();
		}
	}//GEN-LAST:event_startButtonActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel buttonPanel;
  private javax.swing.JButton closeButton;
  private javax.swing.JPanel footerPanel;
  private javax.swing.JCheckBox ignoreCase;
  private javax.swing.JButton jButton1;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JCheckBox matchAll;
  private javax.swing.JLabel nameLabel;
  private javax.swing.JTextField objectNames;
  private javax.swing.JTextField objectTypes;
  private javax.swing.JCheckBox regex;
  private javax.swing.JPanel resultContainer;
  private javax.swing.JLabel schemaLabel;
  private javax.swing.JTextField schemaNames;
  private javax.swing.JTextField searchValues;
  private javax.swing.JButton selectConnection;
  private javax.swing.JSplitPane splitPane;
  private javax.swing.JButton startButton;
  private javax.swing.JLabel statusbar;
  private javax.swing.JPanel topPanel;
  private javax.swing.JLabel typeLabel;
  private javax.swing.JLabel valueLabel;
  // End of variables declaration//GEN-END:variables

}
