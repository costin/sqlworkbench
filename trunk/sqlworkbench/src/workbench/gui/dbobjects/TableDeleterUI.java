/*
 * TableDeleterUI.java
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

import java.awt.Frame;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import workbench.db.TableIdentifier;

import workbench.db.WbConnection;
import workbench.gui.WbSwingUtilities;
import workbench.gui.components.NoSelectionModel;
import workbench.gui.components.WbButton;
import workbench.interfaces.TableDeleteListener;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.util.WbThread;

/**
 *
 * @author  info@sql-workbench.net
 */
public class TableDeleterUI extends javax.swing.JPanel
{
	private JDialog dialog;
	private List objectNames;
	private List objectTypes;
	private boolean cancelled;
	private WbConnection connection;
	private Thread deleteThread;
	private List deleteListener;

	public TableDeleterUI()
	{
		initComponents();
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
    buttonPanel = new javax.swing.JPanel();
    deleteButton = new WbButton();
    cancelButton = new WbButton();
    mainPanel = new javax.swing.JPanel();
    jScrollPane1 = new javax.swing.JScrollPane();
    objectList = new javax.swing.JList();
    optionPanel = new javax.swing.JPanel();
    statusLabel = new javax.swing.JLabel();
    jPanel1 = new javax.swing.JPanel();
    useTruncateCheckBox = new javax.swing.JCheckBox();
    commitEach = new javax.swing.JRadioButton();
    commitAtEnd = new javax.swing.JRadioButton();

    setLayout(new java.awt.BorderLayout());

    buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

    deleteButton.setText(ResourceMgr.getString("LabelDeleteTableData"));
    deleteButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        deleteButtonActionPerformed(evt);
      }
    });

    buttonPanel.add(deleteButton);

    cancelButton.setText(ResourceMgr.getString("LabelCancel"));
    cancelButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        cancelButtonActionPerformed(evt);
      }
    });

    buttonPanel.add(cancelButton);

    add(buttonPanel, java.awt.BorderLayout.SOUTH);

    mainPanel.setLayout(new java.awt.BorderLayout());

    objectList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    objectList.setSelectionModel(new NoSelectionModel());
    jScrollPane1.setViewportView(objectList);

    mainPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

    optionPanel.setLayout(new java.awt.BorderLayout());

    statusLabel.setBorder(new javax.swing.border.EtchedBorder());
    statusLabel.setMaximumSize(new java.awt.Dimension(32768, 24));
    statusLabel.setMinimumSize(new java.awt.Dimension(150, 24));
    statusLabel.setPreferredSize(new java.awt.Dimension(150, 24));
    optionPanel.add(statusLabel, java.awt.BorderLayout.SOUTH);

    jPanel1.setLayout(new java.awt.GridBagLayout());

    useTruncateCheckBox.setText(ResourceMgr.getString("LabelUseTruncate"));
    useTruncateCheckBox.addItemListener(new java.awt.event.ItemListener()
    {
      public void itemStateChanged(java.awt.event.ItemEvent evt)
      {
        useTruncateCheckBoxItemStateChanged(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 11);
    jPanel1.add(useTruncateCheckBox, gridBagConstraints);

    commitEach.setSelected(true);
    commitEach.setText(ResourceMgr.getString("LabelCommitEachTableDelete")
    );
    buttonGroup1.add(commitEach);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 2, 0, 0);
    jPanel1.add(commitEach, gridBagConstraints);

    commitAtEnd.setText(ResourceMgr.getString("LabelCommitTableDeleteAtEnd"));
    buttonGroup1.add(commitAtEnd);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    jPanel1.add(commitAtEnd, gridBagConstraints);

    optionPanel.add(jPanel1, java.awt.BorderLayout.CENTER);

    mainPanel.add(optionPanel, java.awt.BorderLayout.SOUTH);

    add(mainPanel, java.awt.BorderLayout.CENTER);

  }//GEN-END:initComponents

	private void useTruncateCheckBoxItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_useTruncateCheckBoxItemStateChanged
	{//GEN-HEADEREND:event_useTruncateCheckBoxItemStateChanged
		if (this.useTruncateCheckBox.isSelected())
		{
			this.disableCommitSettings();
		}
		else if (!this.connection.getAutoCommit())
		{
			this.enableCommitSettings();
		}

	}//GEN-LAST:event_useTruncateCheckBoxItemStateChanged

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
	{//GEN-HEADEREND:event_cancelButtonActionPerformed
		this.cancelled = true;
		try
		{
			if (this.deleteThread != null)
			{
				this.deleteThread.interrupt();
				this.deleteThread.join(5000);
				this.deleteThread = null;
			}
		}
		catch (Exception e)
		{
			LogMgr.logWarning("TableDeleterUI.cancel()", "Error when trying to kill delete Thread", e);
		}
		this.dialog.setVisible(true);
		this.dialog.dispose();
		this.dialog = null;
	}//GEN-LAST:event_cancelButtonActionPerformed

	private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_deleteButtonActionPerformed
	{//GEN-HEADEREND:event_deleteButtonActionPerformed
		this.startDelete();
	}//GEN-LAST:event_deleteButtonActionPerformed

	public void setConnection(WbConnection aConn)
	{
		this.connection = aConn;
		if (this.connection != null)
		{

			this.useTruncateCheckBox.setEnabled(this.connection.getMetadata().supportsTruncate());
			boolean autoCommit = this.connection.getAutoCommit();
			if (autoCommit)
			{
				this.disableCommitSettings();
			}
			else
			{
				this.enableCommitSettings();
			}
		}

	}

	private void disableCommitSettings()
	{
		this.commitAtEnd.setEnabled(false);
		this.commitEach.setEnabled(false);
		this.commitAtEnd.setSelected(false);
		this.commitEach.setSelected(false);
	}

	private void enableCommitSettings()
	{
		this.commitAtEnd.setEnabled(true);
		this.commitEach.setEnabled(true);
		this.commitAtEnd.setSelected(false);
		this.commitEach.setSelected(true);
	}

	private void startDelete()
	{
		this.deleteThread = new WbThread("TableDeleteThread")
		{
			public void run()
			{
				doDelete();
			}
		};
		this.deleteThread.start();
	}

	private void doDelete()
	{
		this.cancelled = false;
		boolean ignoreAll = false;

		boolean commitEach = this.commitEach.isSelected();
		boolean useTruncate = this.useTruncateCheckBox.isSelected();
		if (useTruncate) commitEach = false;
		boolean hasError = false;
		List tables = new ArrayList();
		int count = this.objectNames.size();
		String table = null;
		for (int i=0; i < count; i++)
		{
			if (this.cancelled) break;
			table = (String)this.objectNames.get(i);
			this.statusLabel.setText(ResourceMgr.getString("TxtDeletingTable") + " " + table + " ...");
			try
			{
				this.deleteTable(table, useTruncate, commitEach);
				TableIdentifier tid = new TableIdentifier(table);
				tables.add(tid);
			}
			catch (Exception ex)
			{
				String error = ex.getMessage();
				LogMgr.logError("TableDeleterUI.doDelete()", "Error deleting table " + table, ex);
				if (!ignoreAll)
				{
					String question = ResourceMgr.getString("ErrorDeleteTableData");
					question = question.replaceAll("%table%", table);
					question = question.replaceAll("%error%", error);

					int choice = WbSwingUtilities.getYesNoIgnoreAll(this.dialog,  question);
					if (choice == JOptionPane.NO_OPTION)
					{
						// the hasError flag will cause a rollback at the end.
						hasError = true;
						break;
					}
					if (choice == WbSwingUtilities.IGNORE_ALL)
					{
						// if we ignore all errors we should do a commit at the
						// end in order to ensure that the delete's which were
						// successful are committed.
						hasError = false;
						ignoreAll = true;
					}
				}
			}
		}

		this.fireTableDeleted(tables);
		boolean commit = true;
		try
		{
			if (!commitEach)
			{
				if (hasError)
				{
					commit = false;
					this.connection.rollback();
				}
				else
				{
					this.connection.commit();
				}
			}
		}
		catch (SQLException e)
		{
			LogMgr.logError("TableDeleterUI.doDelete()", "Error on commit/rollback", e);
			String msg = null;

			if (commit) ResourceMgr.getString("ErrorCommitDeleteTableData");
			else msg = ResourceMgr.getString("ErrorRollbackTableData");
			msg = msg.replaceAll("%error%", e.getMessage());

			WbSwingUtilities.showErrorMessage(this.dialog, msg);
		}
		this.statusLabel.setText("");
		if (!hasError)
		{
			this.dialog.setVisible(true);
			this.dialog.dispose();
			this.dialog = null;
		}
		this.cancelled = false;
	}

	private void deleteTable(final String tableName, final boolean useTruncate, final boolean doCommit)
		throws SQLException
	{
		try
		{
			String deleteSql = null;
			if (useTruncate)
			{
				deleteSql = "TRUNCATE TABLE " + tableName;
			}
			else
			{
				deleteSql = "DELETE FROM " + tableName;
			}
			Statement stmt = this.connection.createStatement();
			LogMgr.logDebug("DataImporter.deleteTarget()", "Executing: [" + deleteSql + "] to delete target table...");
			int rows = stmt.executeUpdate(deleteSql);
			if (doCommit && !this.connection.getAutoCommit())
			{
				this.connection.commit();
			}
		}
		catch (SQLException e)
		{
			LogMgr.logError("TableDeleterUI.deleteTable()", "Error when deleting table!", e);
			throw e;
		}
	}
	public boolean dialogWasCancelled()
	{
		return this.cancelled;
	}
	public void setObjects(List objects)
	{
		this.objectNames = objects;
		int numNames = this.objectNames.size();

		String[] display = new String[numNames];
		for (int i=0; i < numNames; i ++)
		{
			display[i] = (String)this.objectNames.get(i);
		}
		this.objectList.setListData(display);
	}

	public void showDialog(Frame aParent)
	{
		this.dialog = new JDialog(aParent, ResourceMgr.getString("TxtDeleteTableData"), false);
		this.dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
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
		if (this.deleteListener == null) this.deleteListener = new ArrayList();
		this.deleteListener.add(listener);
	}
	
	public void removeDeleteListener(TableDeleteListener listener)
	{
		if (this.deleteListener == null) return;
		this.deleteListener.remove(listener);
	}
	
	public void fireTableDeleted(List tables)
	{
		if (this.deleteListener == null) return;
		for (int i=0; i<this.deleteListener.size(); i++)
		{
			TableDeleteListener l = (TableDeleteListener)this.deleteListener.get(i);
			l.tableDataDeleted(tables);
		}
	}
	
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.ButtonGroup buttonGroup1;
  private javax.swing.JPanel buttonPanel;
  private javax.swing.JButton cancelButton;
  private javax.swing.JRadioButton commitAtEnd;
  private javax.swing.JRadioButton commitEach;
  private javax.swing.JButton deleteButton;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JPanel mainPanel;
  private javax.swing.JList objectList;
  private javax.swing.JPanel optionPanel;
  private javax.swing.JLabel statusLabel;
  private javax.swing.JCheckBox useTruncateCheckBox;
  // End of variables declaration//GEN-END:variables

}
