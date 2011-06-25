/*
 * TableDeleterUI.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2011, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.dbobjects;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import workbench.db.TableDeleter;
import workbench.db.TableIdentifier;
import workbench.db.WbConnection;
import workbench.db.importer.TableDependencySorter;
import workbench.gui.WbSwingUtilities;
import workbench.gui.components.EditWindow;
import workbench.gui.components.NoSelectionModel;
import workbench.gui.components.SimpleStatusBar;
import workbench.gui.components.WbButton;
import workbench.interfaces.JobErrorHandler;
import workbench.interfaces.StatusBar;
import workbench.interfaces.TableDeleteListener;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.util.ExceptionUtil;
import workbench.util.WbThread;

/**
 *
 * @author  Thomas Kellerer
 */
public class TableDeleterUI
	extends javax.swing.JPanel
	implements WindowListener, JobErrorHandler
{
	private JDialog dialog;
	private List<TableIdentifier> objectNames;
	private boolean cancelled;
	private WbConnection connection;
	private Thread deleteThread;
	private Thread checkThread;
	private List<TableDeleteListener> deleteListener;
	private TableDeleter deleter;

	public TableDeleterUI()
	{
		super();
		initComponents();
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
		GridBagConstraints gridBagConstraints;

    buttonGroup1 = new ButtonGroup();
    buttonPanel = new JPanel();
    deleteButton = new WbButton();
    cancelButton = new WbButton();
    mainPanel = new JPanel();
    jScrollPane1 = new JScrollPane();
    objectList = new JList();
    optionPanel = new JPanel();
    statusLabel = new SimpleStatusBar();
    jPanel1 = new JPanel();
    checkFKButton = new JButton();
    jPanel2 = new JPanel();
    commitEach = new JRadioButton();
    commitAtEnd = new JRadioButton();
    useTruncateCheckBox = new JCheckBox();
    jPanel3 = new JPanel();
    cascadeTruncate = new JCheckBox();
    showScript = new JButton();
    addMissingTables = new JCheckBox();

    setLayout(new BorderLayout());

    buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

    deleteButton.setText(ResourceMgr.getString("LblDeleteTableData"));
    deleteButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        deleteButtonActionPerformed(evt);
      }
    });
    buttonPanel.add(deleteButton);

    cancelButton.setText(ResourceMgr.getString("LblCancel"));
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        cancelButtonActionPerformed(evt);
      }
    });
    buttonPanel.add(cancelButton);

    add(buttonPanel, BorderLayout.SOUTH);

    mainPanel.setLayout(new BorderLayout(0, 5));

    objectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    objectList.setSelectionModel(new NoSelectionModel());
    jScrollPane1.setViewportView(objectList);

    mainPanel.add(jScrollPane1, BorderLayout.CENTER);

    optionPanel.setLayout(new BorderLayout(0, 5));

    statusLabel.setBorder(BorderFactory.createEtchedBorder());
    statusLabel.setMaximumSize(new Dimension(32768, 24));
    statusLabel.setMinimumSize(new Dimension(150, 24));
    statusLabel.setPreferredSize(new Dimension(150, 24));
    optionPanel.add(statusLabel, BorderLayout.SOUTH);

    jPanel1.setLayout(new GridBagLayout());

    checkFKButton.setText(ResourceMgr.getString("LblCheckFKDeps"));
    checkFKButton.setToolTipText(ResourceMgr.getDescription("LblCheckFKDeps"));
    checkFKButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        checkFKButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new Insets(3, 3, 0, 5);
    jPanel1.add(checkFKButton, gridBagConstraints);

    jPanel2.setLayout(new GridBagLayout());

    buttonGroup1.add(commitEach);
    commitEach.setSelected(true);
    commitEach.setText(ResourceMgr.getString("LblCommitEachTableDelete")
    );
    commitEach.setBorder(null);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new Insets(0, 6, 0, 0);
    jPanel2.add(commitEach, gridBagConstraints);

    buttonGroup1.add(commitAtEnd);
    commitAtEnd.setText(ResourceMgr.getString("LblCommitTableDeleteAtEnd"));
    commitAtEnd.setBorder(null);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new Insets(4, 6, 0, 0);
    jPanel2.add(commitAtEnd, gridBagConstraints);

    useTruncateCheckBox.setText(ResourceMgr.getString("LblUseTruncate")); // NOI18N
    useTruncateCheckBox.setBorder(null);
    useTruncateCheckBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent evt) {
        useTruncateCheckBoxItemStateChanged(evt);
      }
    });
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new Insets(7, 6, 0, 0);
    jPanel2.add(useTruncateCheckBox, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    jPanel2.add(jPanel3, gridBagConstraints);

    cascadeTruncate.setText(ResourceMgr.getString("LblCascadeConstraints")); // NOI18N
    cascadeTruncate.setBorder(null);
    cascadeTruncate.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent evt) {
        cascadeTruncateItemStateChanged(evt);
      }
    });
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new Insets(3, 22, 0, 0);
    jPanel2.add(cascadeTruncate, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridheight = 3;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new Insets(3, 4, 0, 9);
    jPanel1.add(jPanel2, gridBagConstraints);

    showScript.setText(ResourceMgr.getString("LblShowScript"));
    showScript.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        showScriptActionPerformed(evt);
      }
    });
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new Insets(9, 3, 0, 5);
    jPanel1.add(showScript, gridBagConstraints);

    addMissingTables.setSelected(true);
    addMissingTables.setText(ResourceMgr.getString("LblIncFkTables"));
    addMissingTables.setToolTipText(ResourceMgr.getDescription("LblIncFkTables"));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new Insets(0, 2, 0, 0);
    jPanel1.add(addMissingTables, gridBagConstraints);

    optionPanel.add(jPanel1, BorderLayout.CENTER);

    mainPanel.add(optionPanel, BorderLayout.SOUTH);

    add(mainPanel, BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents

	private void useTruncateCheckBoxItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_useTruncateCheckBoxItemStateChanged
	{//GEN-HEADEREND:event_useTruncateCheckBoxItemStateChanged
		checkState();
	}//GEN-LAST:event_useTruncateCheckBoxItemStateChanged

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
	{//GEN-HEADEREND:event_cancelButtonActionPerformed
		if (this.deleter != null)
		{
			this.deleter.cancel();
		}
		this.cancelled = true;
		closeWindow();
	}//GEN-LAST:event_cancelButtonActionPerformed

	private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_deleteButtonActionPerformed
	{//GEN-HEADEREND:event_deleteButtonActionPerformed
		this.startDelete();
	}//GEN-LAST:event_deleteButtonActionPerformed

	private void checkFKButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkFKButtonActionPerformed

		if (this.connection.isBusy())
		{
			return;
		}

		this.deleteButton.setEnabled(false);
		this.showScript.setEnabled(false);
		this.statusLabel.setText(ResourceMgr.getString("MsgFkDeps"));

		WbSwingUtilities.showWaitCursor(dialog);

		this.checkThread = new WbThread("FKCheck")
		{

			@Override
			public void run()
			{
				List<TableIdentifier> sorted = null;
				try
				{
					connection.setBusy(true);
					TableDependencySorter sorter = new TableDependencySorter(connection);
					sorted = sorter.sortForDelete(objectNames, addMissingTables.isSelected());
				}
				catch (Exception e)
				{
					LogMgr.logError("TableDeleterUI.checkFK()", "Error checking FK dependencies", e);
					WbSwingUtilities.showErrorMessage(ExceptionUtil.getDisplay(e));
					sorted = null;
				}
				finally
				{
					connection.setBusy(false);
					fkCheckFinished(sorted);
				}
			}
		};

		checkThread.start();
	}//GEN-LAST:event_checkFKButtonActionPerformed

	private void showScriptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showScriptActionPerformed
		showScript();
	}//GEN-LAST:event_showScriptActionPerformed

	private void cascadeTruncateItemStateChanged(ItemEvent evt)//GEN-FIRST:event_cascadeTruncateItemStateChanged
	{//GEN-HEADEREND:event_cascadeTruncateItemStateChanged
		// TODO add your handling code here:
	}//GEN-LAST:event_cascadeTruncateItemStateChanged

	protected void fkCheckFinished(final List<TableIdentifier> newlist)
	{
		this.checkThread = null;
		EventQueue.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				statusLabel.setText("");
				if (newlist != null)
				{
					setObjects(newlist);
				}
				deleteButton.setEnabled(true);
				showScript.setEnabled(true);
				WbSwingUtilities.showDefaultCursor(dialog);
			}
		});
	}

	@Override
	public void fatalError(String msg)
	{
		WbSwingUtilities.showErrorMessage(this, msg);
	}

	@Override
	public int getActionOnError(int errorRow, String errorColumn, String data, String errorMessage)
	{
		int choice = WbSwingUtilities.getYesNoIgnoreAll(this.dialog, errorMessage);
		if (choice == WbSwingUtilities.IGNORE_ALL)
		{
			return JobErrorHandler.JOB_IGNORE_ALL;
		}
		if (choice == JOptionPane.YES_OPTION)
		{
			return JobErrorHandler.JOB_CONTINUE;
		}
		return JobErrorHandler.JOB_ABORT;
	}

	protected void closeWindow()
	{
		try
		{
			if (this.deleter != null)
			{
				this.deleter.cancel();
			}
			if (this.deleteThread != null)
			{
				this.deleteThread.interrupt();
				this.deleteThread = null;
			}
		}
		catch (Exception e)
		{
			LogMgr.logWarning("TableDeleterUI.cancel()", "Error when trying to kill delete Thread", e);
		}

		try
		{
			if (this.checkThread != null)
			{
				this.checkThread.interrupt();
				this.checkThread = null;
			}
		}
		catch (Exception e)
		{
			LogMgr.logWarning("TableDeleterUI.cancel()", "Error when trying to kill check thread", e);
		}

		this.dialog.setVisible(false);
		this.dialog = null;
	}

	public void setConnection(WbConnection aConn)
	{
		this.connection = aConn;
		if (connection != null)
		{
			useTruncateCheckBox.setEnabled(this.connection.getDbSettings().supportsTruncate());
			checkState();
		}
	}

	protected void checkState()
	{
		boolean autoCommit = connection == null ? true : connection.getAutoCommit();
		boolean useTruncate = useTruncateCheckBox.isSelected();
		if (autoCommit)
		{
			commitAtEnd.setEnabled(false);
			commitEach.setEnabled(false);
		}
		else if (useTruncate)
		{
			commitAtEnd.setEnabled(connection.getDbSettings().truncateNeedsCommit());
			commitEach.setEnabled(connection.getDbSettings().truncateNeedsCommit());
		}
		else
		{
			commitAtEnd.setEnabled(true);
			commitEach.setEnabled(true);
		}

		boolean canCascade = connection == null ? false : connection.getDbSettings().supportsCascadedTruncate();
		cascadeTruncate.setEnabled(canCascade && useTruncate);
		if (!canCascade)
		{
			cascadeTruncate.setSelected(false);
		}
	}

	protected void startDelete()
	{
		deleteButton.setEnabled(false);

		this.deleteThread = new WbThread("TableDeleteThread")
		{
			@Override
			public void run()
			{
				doDelete();
			}
		};
		this.deleteThread.start();
	}

	protected void doDelete()
	{
		this.cancelled = false;

		boolean doCommitEach = commitEach.isEnabled() && this.commitEach.isSelected();
		boolean useTruncate = useTruncateCheckBox.isSelected();
		boolean cascadedTruncate = useTruncate ? cascadeTruncate.isSelected() : false;

		deleter = new TableDeleter(this.connection);
		deleter.setStatusBar((StatusBar)statusLabel);

		boolean hasError = false;
		List<TableIdentifier> deletedTables = null;

		try
		{
			deletedTables = deleter.deleteTableData(this.objectNames, doCommitEach, useTruncate, cascadedTruncate);
		}
		catch (SQLException e)
		{
			// Basically any error should have been handled by the TableDeleter
			// or through the JobErrorHandler callbacks
			WbSwingUtilities.showErrorMessage(this, ExceptionUtil.getDisplay(e));
		}
		finally
		{
			EventQueue.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					deleteButton.setEnabled(true);
				}
			});
		}

		this.fireTableDeleted(deletedTables);

		this.statusLabel.setText("");
		this.deleter = null;

		if (!hasError)
		{
			this.closeWindow();
		}
	}

	protected void showScript()
	{
		boolean doCommitEach = this.commitEach.isSelected();
		boolean useTruncate = this.useTruncateCheckBox.isSelected();
		boolean cascade = useTruncate && cascadeTruncate.isSelected();
		TableDeleter tblDeleter = new TableDeleter(this.connection);
		CharSequence script = tblDeleter.generateScript(objectNames, doCommitEach, useTruncate, cascade);
		final EditWindow w = new EditWindow(this.dialog, ResourceMgr.getString("TxtWindowTitleGeneratedScript"), script.toString(), "workbench.tabledeleter.scriptwindow", true);
		w.setVisible(true);
		w.dispose();
	}

	public boolean dialogWasCancelled()
	{
		return this.cancelled;
	}

	public void setObjects(List<TableIdentifier> objects)
	{
		this.objectNames = objects;
		int numNames = this.objectNames.size();

		String[] display = new String[numNames];
		for (int i = 0; i < numNames; i++)
		{
			display[i] = this.objectNames.get(i).toString();
		}
		this.objectList.setListData(display);
	}

	public void showDialog(Frame aParent)
	{
		this.dialog = new JDialog(aParent, ResourceMgr.getString("TxtDeleteTableData"), false);
		this.dialog.getContentPane().add(this);
		this.dialog.pack();
		if (this.dialog.getWidth() < 200)
		{
			this.dialog.setSize(200, this.dialog.getHeight());
		}
		WbSwingUtilities.center(this.dialog, aParent);
		this.cancelled = true;
		this.dialog.setVisible(true);
	}

	public void addDeleteListener(TableDeleteListener listener)
	{
		if (this.deleteListener == null)
		{
			this.deleteListener = new ArrayList<TableDeleteListener>();
		}
		this.deleteListener.add(listener);
	}

	public void removeDeleteListener(TableDeleteListener listener)
	{
		if (this.deleteListener == null)
		{
			return;
		}
		this.deleteListener.remove(listener);
	}

	protected void fireTableDeleted(List tables)
	{
		if (this.deleteListener == null || tables == null)
		{
			return;
		}
		for (TableDeleteListener l : this.deleteListener)
		{
			l.tableDataDeleted(tables);
		}
	}

	@Override
	public void windowActivated(WindowEvent e)
	{
	}

	@Override
	public void windowClosed(WindowEvent e)
	{
	}

	@Override
	public void windowClosing(WindowEvent e)
	{
		this.cancelled = true;
		closeWindow();
	}

	@Override
	public void windowDeactivated(WindowEvent e)
	{
	}

	@Override
	public void windowDeiconified(WindowEvent e)
	{
	}

	@Override
	public void windowIconified(WindowEvent e)
	{
	}

	@Override
	public void windowOpened(WindowEvent e)
	{
	}
	
  // Variables declaration - do not modify//GEN-BEGIN:variables
  public JCheckBox addMissingTables;
  public ButtonGroup buttonGroup1;
  public JPanel buttonPanel;
  public JButton cancelButton;
  public JCheckBox cascadeTruncate;
  public JButton checkFKButton;
  public JRadioButton commitAtEnd;
  public JRadioButton commitEach;
  public JButton deleteButton;
  public JPanel jPanel1;
  public JPanel jPanel2;
  public JPanel jPanel3;
  public JScrollPane jScrollPane1;
  public JPanel mainPanel;
  public JList objectList;
  public JPanel optionPanel;
  public JButton showScript;
  public JLabel statusLabel;
  public JCheckBox useTruncateCheckBox;
  // End of variables declaration//GEN-END:variables
}
