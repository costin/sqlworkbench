/*
 * SqlGenerationOptionsPanel.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2008, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.settings;

import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import workbench.gui.components.WbCheckBoxLabel;
import workbench.interfaces.Restoreable;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.util.StringUtil;

/**
 *
 * @author  support@sql-workbench.net
 */
public class SqlGenerationOptionsPanel 
	extends JPanel
	implements Restoreable
{
	
	/** Creates new form FormatterOptionsPanel */
	public SqlGenerationOptionsPanel()
	{
		initComponents();
		
		List<String> types = Settings.getInstance().getLiteralTypeList();
		ComboBoxModel model1 = new DefaultComboBoxModel(types.toArray());
		literalTypes.setModel(model1);
		ComboBoxModel model2 = new DefaultComboBoxModel(types.toArray());
		exportLiteralTypes.setModel(model2);
		ComboBoxModel model3 = new DefaultComboBoxModel(types.toArray());
		diffLiteralsType.setModel(model3);
	}
	
	public void restoreSettings()
	{
		String genCase = Settings.getInstance().getGeneratedSqlTableCase();
		if ("lower".equals(genCase)) this.tableNameCase.setSelectedIndex(1);
		else if ("upper".equals(genCase)) this.tableNameCase.setSelectedIndex(2);
		else this.tableNameCase.setSelectedIndex(0);		
		this.literalTypes.setSelectedItem(Settings.getInstance().getDefaultCopyDateLiteralType());
		this.exportLiteralTypes.setSelectedItem(Settings.getInstance().getDefaultExportDateLiteralType());
		this.diffLiteralsType.setSelectedItem(Settings.getInstance().getDefaultDiffDateLiteralType());
		this.includeEmptyComments.setSelected(Settings.getInstance().getIncludeEmptyComments());
	}
	
	public void saveSettings()
	{
		Settings set = Settings.getInstance();
		set.setDoFormatUpdates(formatUpdates.isSelected());
		set.setDoFormatInserts(formatInserts.isSelected());
		set.setFormatInsertColsPerLine(StringUtil.getIntValue(insertColsPerLine.getText(),1));
		set.setFormatInsertColumnThreshold(StringUtil.getIntValue(insertThreshold.getText(),5));
		set.setFormatUpdateColumnThreshold(StringUtil.getIntValue(updateThreshold.getText(),5));
		set.setIncludeOwnerInSqlExport(includeOwner.isSelected());
		set.setGeneratedSqlTableCase((String)tableNameCase.getSelectedItem());
		set.setDefaultCopyDateLiteralType((String)literalTypes.getSelectedItem());
		set.setDefaultExportDateLiteralType((String)exportLiteralTypes.getSelectedItem());
		set.setDefaultDiffDateLiteralType((String)diffLiteralsType.getSelectedItem());
		set.setIncludeEmptyComments(includeEmptyComments.isSelected());
		set.setFormatInsertIgnoreIdentity(ignoreIdentity.isSelected());
	}
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    formatUpdates = new javax.swing.JCheckBox();
    formatUpdatesLabel = new WbCheckBoxLabel();
    formatInsertsLabel = new WbCheckBoxLabel();
    formatInserts = new javax.swing.JCheckBox();
    insertColThresholdLbl = new javax.swing.JLabel();
    insertThreshold = new javax.swing.JTextField();
    updateThreshold = new javax.swing.JTextField();
    updateColThresholdLbl = new javax.swing.JLabel();
    ignoreIdentityLabel = new WbCheckBoxLabel();
    ignoreIdentity = new javax.swing.JCheckBox();
    jSeparator1 = new javax.swing.JSeparator();
    insertColsPerLine = new javax.swing.JTextField();
    colsPerLineLabel = new javax.swing.JLabel();
    jSeparator2 = new javax.swing.JSeparator();
    includeOwner = new javax.swing.JCheckBox();
    includeOwnerLabel = new WbCheckBoxLabel();
    tableNameCaseLabel = new javax.swing.JLabel();
    tableNameCase = new javax.swing.JComboBox();
    jSeparator3 = new javax.swing.JSeparator();
    copyLiteralLabel = new javax.swing.JLabel();
    literalTypes = new javax.swing.JComboBox();
    exportLiteralTypes = new javax.swing.JComboBox();
    exportLiteralLabel = new javax.swing.JLabel();
    includeEmptyCommentsLabel = new WbCheckBoxLabel();
    includeEmptyComments = new javax.swing.JCheckBox();
    jSeparator4 = new javax.swing.JSeparator();
    diffLiteralsLabel = new javax.swing.JLabel();
    diffLiteralsType = new javax.swing.JComboBox();

    setLayout(new java.awt.GridBagLayout());

    formatUpdates.setFont(null);
    formatUpdates.setSelected(Settings.getInstance().getDoFormatUpdates());
    formatUpdates.setText("");
    formatUpdates.setBorder(null);
    formatUpdates.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    formatUpdates.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    formatUpdates.setIconTextGap(5);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 11);
    add(formatUpdates, gridBagConstraints);

    formatUpdatesLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    formatUpdatesLabel.setLabelFor(formatUpdates);
    formatUpdatesLabel.setText(ResourceMgr.getString("LblFmtUpd"));
    formatUpdatesLabel.setToolTipText(ResourceMgr.getDescription("LblFmtUpd"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(7, 12, 0, 0);
    add(formatUpdatesLabel, gridBagConstraints);

    formatInsertsLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    formatInsertsLabel.setLabelFor(formatInserts);
    formatInsertsLabel.setText(ResourceMgr.getString("LblFmtIns"));
    formatInsertsLabel.setToolTipText(ResourceMgr.getDescription("LblFmtIns"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
    add(formatInsertsLabel, gridBagConstraints);

    formatInserts.setFont(null);
    formatInserts.setSelected(Settings.getInstance().getDoFormatInserts());
    formatInserts.setText("");
    formatInserts.setBorder(null);
    formatInserts.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    formatInserts.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    formatInserts.setIconTextGap(5);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 11);
    add(formatInserts, gridBagConstraints);

    insertColThresholdLbl.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    insertColThresholdLbl.setText(ResourceMgr.getString("LblInsThres"));
    insertColThresholdLbl.setToolTipText(ResourceMgr.getDescription("LblInsThres"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(6, 18, 0, 0);
    add(insertColThresholdLbl, gridBagConstraints);

    insertThreshold.setText(Integer.toString(Settings.getInstance().getFormatInsertColumnThreshold()));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 10, 0, 15);
    add(insertThreshold, gridBagConstraints);

    updateThreshold.setText(Integer.toString(Settings.getInstance().getFormatUpdateColumnThreshold()));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 15);
    add(updateThreshold, gridBagConstraints);

    updateColThresholdLbl.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    updateColThresholdLbl.setText(ResourceMgr.getString("LblUpdThres"));
    updateColThresholdLbl.setToolTipText(ResourceMgr.getDescription("LblUpdThres"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(7, 18, 0, 0);
    add(updateColThresholdLbl, gridBagConstraints);

    ignoreIdentityLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    ignoreIdentityLabel.setLabelFor(ignoreIdentity);
    ignoreIdentityLabel.setText(ResourceMgr.getString("LblInsIgnoreId"));
    ignoreIdentityLabel.setToolTipText(ResourceMgr.getDescription("LblInsIgnoreId"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(6, 18, 0, 0);
    add(ignoreIdentityLabel, gridBagConstraints);

    ignoreIdentity.setFont(null);
    ignoreIdentity.setSelected(Settings.getInstance().getFormatInsertIgnoreIdentity());
    ignoreIdentity.setText("");
    ignoreIdentity.setBorder(null);
    ignoreIdentity.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    ignoreIdentity.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    ignoreIdentity.setIconTextGap(5);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 11);
    add(ignoreIdentity, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
    add(jSeparator1, gridBagConstraints);

    insertColsPerLine.setText(Integer.toString(Settings.getInstance().getFormatInsertColsPerLine()));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 15);
    add(insertColsPerLine, gridBagConstraints);

    colsPerLineLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    colsPerLineLabel.setText(ResourceMgr.getString("LblInsColsPerLine"));
    colsPerLineLabel.setToolTipText(ResourceMgr.getDescription("LblInsColsPerLine"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(7, 18, 0, 0);
    add(colsPerLineLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(6, 0, 2, 0);
    add(jSeparator2, gridBagConstraints);

    includeOwner.setFont(null);
    includeOwner.setSelected(Settings.getInstance().getIncludeOwnerInSqlExport());
    includeOwner.setText("");
    includeOwner.setBorder(null);
    includeOwner.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    includeOwner.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    includeOwner.setIconTextGap(5);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(9, 10, 0, 11);
    add(includeOwner, gridBagConstraints);

    includeOwnerLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    includeOwnerLabel.setLabelFor(includeOwner);
    includeOwnerLabel.setText(ResourceMgr.getString("LblGenInclOwn"));
    includeOwnerLabel.setToolTipText(ResourceMgr.getDescription("LblGenInclOwn"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(10, 12, 0, 0);
    add(includeOwnerLabel, gridBagConstraints);

    tableNameCaseLabel.setLabelFor(tableNameCase);
    tableNameCaseLabel.setText(ResourceMgr.getString("LblGenTableNameCase"));
    tableNameCaseLabel.setToolTipText(ResourceMgr.getDescription("LblGenTableNameCase"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 9;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 0);
    add(tableNameCaseLabel, gridBagConstraints);

    tableNameCase.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "As is", "Lowercase", "Uppercase" }));
    tableNameCase.setToolTipText(ResourceMgr.getDescription("LblGenTableNameCase"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 9;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 15);
    add(tableNameCase, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 10;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(7, 0, 2, 0);
    add(jSeparator3, gridBagConstraints);

    copyLiteralLabel.setText(ResourceMgr.getString("LblDefCopyLiteralType"));
    copyLiteralLabel.setToolTipText(ResourceMgr.getDescription("LblDefCopyLiteralType"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 11;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(9, 12, 0, 0);
    add(copyLiteralLabel, gridBagConstraints);

    literalTypes.setToolTipText(ResourceMgr.getDescription("LblDefCopyLiteralType"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 11;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(6, 8, 0, 0);
    add(literalTypes, gridBagConstraints);

    exportLiteralTypes.setToolTipText(ResourceMgr.getDescription("LblDefExportLiteralType"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 12;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(6, 8, 0, 0);
    add(exportLiteralTypes, gridBagConstraints);

    exportLiteralLabel.setLabelFor(exportLiteralTypes);
    exportLiteralLabel.setText(ResourceMgr.getString("LblDefExportLiteralType"));
    exportLiteralLabel.setToolTipText(ResourceMgr.getDescription("LblDefExportLiteralType"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 12;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(9, 12, 0, 0);
    add(exportLiteralLabel, gridBagConstraints);

    includeEmptyCommentsLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    includeEmptyCommentsLabel.setLabelFor(includeEmptyComments);
    includeEmptyCommentsLabel.setText(ResourceMgr.getString("LblGenInclEmptyComments"));
    includeEmptyCommentsLabel.setToolTipText(ResourceMgr.getDescription("LblGenInclEmptyComments"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 15;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(10, 12, 0, 0);
    add(includeEmptyCommentsLabel, gridBagConstraints);

    includeEmptyComments.setFont(null);
    includeEmptyComments.setSelected(Settings.getInstance().getIncludeOwnerInSqlExport());
    includeEmptyComments.setText("");
    includeEmptyComments.setBorder(null);
    includeEmptyComments.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    includeEmptyComments.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    includeEmptyComments.setIconTextGap(5);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 15;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(9, 8, 0, 11);
    add(includeEmptyComments, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 14;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(7, 0, 2, 0);
    add(jSeparator4, gridBagConstraints);

    diffLiteralsLabel.setLabelFor(diffLiteralsType);
    diffLiteralsLabel.setText(ResourceMgr.getString("LblDefDiffLiteralType"));
    diffLiteralsLabel.setToolTipText(ResourceMgr.getDescription("LblDefDiffLiteralType"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 13;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(9, 12, 0, 0);
    add(diffLiteralsLabel, gridBagConstraints);

    diffLiteralsType.setToolTipText(ResourceMgr.getDescription("LblDefExportLiteralType"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 13;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(6, 8, 0, 0);
    add(diffLiteralsType, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents
	
	
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel colsPerLineLabel;
  private javax.swing.JLabel copyLiteralLabel;
  private javax.swing.JLabel diffLiteralsLabel;
  private javax.swing.JComboBox diffLiteralsType;
  private javax.swing.JLabel exportLiteralLabel;
  private javax.swing.JComboBox exportLiteralTypes;
  private javax.swing.JCheckBox formatInserts;
  private javax.swing.JLabel formatInsertsLabel;
  private javax.swing.JCheckBox formatUpdates;
  private javax.swing.JLabel formatUpdatesLabel;
  private javax.swing.JCheckBox ignoreIdentity;
  private javax.swing.JLabel ignoreIdentityLabel;
  private javax.swing.JCheckBox includeEmptyComments;
  private javax.swing.JLabel includeEmptyCommentsLabel;
  private javax.swing.JCheckBox includeOwner;
  private javax.swing.JLabel includeOwnerLabel;
  private javax.swing.JLabel insertColThresholdLbl;
  private javax.swing.JTextField insertColsPerLine;
  private javax.swing.JTextField insertThreshold;
  private javax.swing.JSeparator jSeparator1;
  private javax.swing.JSeparator jSeparator2;
  private javax.swing.JSeparator jSeparator3;
  private javax.swing.JSeparator jSeparator4;
  private javax.swing.JComboBox literalTypes;
  private javax.swing.JComboBox tableNameCase;
  private javax.swing.JLabel tableNameCaseLabel;
  private javax.swing.JLabel updateColThresholdLbl;
  private javax.swing.JTextField updateThreshold;
  // End of variables declaration//GEN-END:variables
	
}
