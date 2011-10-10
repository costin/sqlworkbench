/*
 * WorkspaceOptions.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2012, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.settings;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import workbench.gui.components.WbFilePicker;
import workbench.interfaces.Restoreable;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.util.StringUtil;

/**
 *
 * @author  Thomas Kellerer
 */
public class WorkspaceOptions
	extends JPanel
	implements Restoreable, ActionListener
{
	public WorkspaceOptions()
	{
		super();
		initComponents();
		String[] types = new String[] {
			ResourceMgr.getString("LblFileWksplink"),
			ResourceMgr.getString("LblFileWkspcontent"),
			ResourceMgr.getString("LblFileWkspnone")
		};
		fileHandling.setModel(new DefaultComboBoxModel(types));
	}

	public void restoreSettings()
	{
		createBackup.setSelected(Settings.getInstance().getCreateWorkspaceBackup());
		backupCount.setEnabled(createBackup.isSelected());
		backupCount.setText(Integer.toString(Settings.getInstance().getMaxWorkspaceBackup()));
		backupDirPicker.setFilename(Settings.getInstance().getWorkspaceBackupDir());

		ExternalFileHandling handling = Settings.getInstance().getFilesInWorkspaceHandling();
		switch (handling)
		{
			case link:
				fileHandling.setSelectedIndex(0);
				break;
			case content:
				fileHandling.setSelectedIndex(1);
				break;
			case none:
				fileHandling.setSelectedIndex(2);
				break;
			default:
				fileHandling.setSelectedIndex(0);
		}
	}

	public void saveSettings()
	{
		Settings set = Settings.getInstance();

		// General settings
		set.setAutoSaveWorkspace(autoSaveWorkspace.isSelected());
		set.setCreateWorkspaceBackup(createBackup.isSelected());
		int index = fileHandling.getSelectedIndex();
		switch (index)
		{
			case 0:
				set.setFilesInWorkspaceHandling(ExternalFileHandling.link);
				break;
			case 1:
				set.setFilesInWorkspaceHandling(ExternalFileHandling.content);
				break;
			case 2:
				set.setFilesInWorkspaceHandling(ExternalFileHandling.none);
				break;
			default:
				set.setFilesInWorkspaceHandling(ExternalFileHandling.link);
		}
		int value = StringUtil.getIntValue(backupCount.getText(), -1);
		if (value > -1)
		{
			set.setMaxWorkspaceBackup(value);
		}
		set.setWorkspaceBackupDir(backupDirPicker.getFilename());
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
		GridBagConstraints gridBagConstraints;

    autoSaveWorkspace = new JCheckBox();
    createBackup = new JCheckBox();
    jLabel1 = new JLabel();
    backupCount = new JTextField();
    jLabel2 = new JLabel();
    backupDirPicker = new WbFilePicker();
    jLabel3 = new JLabel();
    fileHandling = new JComboBox();

    setLayout(new GridBagLayout());

    autoSaveWorkspace.setSelected(Settings.getInstance().getAutoSaveWorkspace());
    autoSaveWorkspace.setText(ResourceMgr.getString("LblAutoSaveWksp")); // NOI18N
    autoSaveWorkspace.setToolTipText(ResourceMgr.getString("d_LblAutoSaveWksp")); // NOI18N
    autoSaveWorkspace.setBorder(null);
    autoSaveWorkspace.setHorizontalAlignment(SwingConstants.LEFT);
    autoSaveWorkspace.setHorizontalTextPosition(SwingConstants.RIGHT);
    autoSaveWorkspace.setIconTextGap(5);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(10, 12, 3, 0);
    add(autoSaveWorkspace, gridBagConstraints);

    createBackup.setText(ResourceMgr.getString("LblBckWksp")); // NOI18N
    createBackup.setToolTipText(ResourceMgr.getString("d_LblBckWksp")); // NOI18N
    createBackup.setBorder(null);
    createBackup.addActionListener(this);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(6, 12, 1, 0);
    add(createBackup, gridBagConstraints);

    jLabel1.setLabelFor(backupCount);
    jLabel1.setText(ResourceMgr.getString("LblMaxWkspBck")); // NOI18N
    jLabel1.setToolTipText(ResourceMgr.getString("d_LblMaxWkspBck")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(7, 8, 0, 2);
    add(jLabel1, gridBagConstraints);

    backupCount.setColumns(3);
    backupCount.setHorizontalAlignment(JTextField.TRAILING);
    backupCount.setToolTipText(ResourceMgr.getString("d_LblMaxWkspBck")); // NOI18N
    backupCount.setMinimumSize(new Dimension(30, 20));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new Insets(6, 3, 0, 3);
    add(backupCount, gridBagConstraints);

    jLabel2.setText(ResourceMgr.getString("LblBckDir")); // NOI18N
    jLabel2.setToolTipText(ResourceMgr.getString("d_LblBckDir")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(6, 12, 4, 0);
    add(jLabel2, gridBagConstraints);

    backupDirPicker.setToolTipText(ResourceMgr.getString("d_LblBckDir")); // NOI18N
    backupDirPicker.setSelectDirectoryOnly(true);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new Insets(6, 5, 5, 3);
    add(backupDirPicker, gridBagConstraints);

    jLabel3.setText(ResourceMgr.getString("LblRememberFileWksp")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new Insets(11, 12, 0, 0);
    add(jLabel3, gridBagConstraints);

    fileHandling.setModel(new DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new Insets(7, 5, 0, 0);
    add(fileHandling, gridBagConstraints);
  }

  // Code for dispatching events from components to event handlers.

  public void actionPerformed(java.awt.event.ActionEvent evt) {
    if (evt.getSource() == createBackup) {
      WorkspaceOptions.this.createBackupActionPerformed(evt);
    }
  }// </editor-fold>//GEN-END:initComponents

	private void createBackupActionPerformed(ActionEvent evt)//GEN-FIRST:event_createBackupActionPerformed
	{//GEN-HEADEREND:event_createBackupActionPerformed
		backupCount.setEnabled(createBackup.isSelected());
	}//GEN-LAST:event_createBackupActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private JCheckBox autoSaveWorkspace;
  private JTextField backupCount;
  private WbFilePicker backupDirPicker;
  private JCheckBox createBackup;
  private JComboBox fileHandling;
  private JLabel jLabel1;
  private JLabel jLabel2;
  private JLabel jLabel3;
  // End of variables declaration//GEN-END:variables

}
