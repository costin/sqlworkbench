/*
 * DataEditOptionsPanel.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2009, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.settings;

import javax.swing.JPanel;
import workbench.gui.components.FlatButton;
import workbench.interfaces.Restoreable;
import workbench.resource.GuiSettings;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.util.FileDialogUtil;

/**
 * A panel to edit the options for data editing, such as the font to be
 * used in the JTable, the PK Mapping file, colors used for required
 * fields and alternating row coloring.
 *
 * @author  support@sql-workbench.net
 */
public class DataEditOptionsPanel
	extends JPanel
	implements java.awt.event.ActionListener, Restoreable
{

	public DataEditOptionsPanel()
	{
		super();
		initComponents();
	}

	public void restoreSettings()
	{
		pkMapFile.setCaretPosition(0);
		previewDml.setSelected(Settings.getInstance().getPreviewDml());
		requiredFieldColor.setSelectedColor(GuiSettings.getRequiredFieldColor());
		highlightRequired.setSelected(GuiSettings.getHighlightRequiredFields());
		pkMapFile.setText(Settings.getInstance().getPKMappingFilename());
		warnDiscard.setSelected(GuiSettings.getConfirmDiscardResultSetChanges());
	}

	public void saveSettings()
	{
		GuiSettings.setRequiredFieldColor(requiredFieldColor.getSelectedColor());
		GuiSettings.setHighlightRequiredFields(this.highlightRequired.isSelected());
		GuiSettings.setConfirmDiscardResultSetChanges(warnDiscard.isSelected());
		Settings.getInstance().setPreviewDml(this.previewDml.isSelected());
		Settings.getInstance().setPKMappingFilename(pkMapFile.getText());
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    previewDml = new javax.swing.JCheckBox();
    requiredFieldColor = new workbench.gui.components.WbColorPicker();
    highlightRequired = new javax.swing.JCheckBox();
    dummyPanel = new javax.swing.JPanel();
    warnDiscard = new javax.swing.JCheckBox();
    jPanel1 = new javax.swing.JPanel();
    pkMapFileLabel = new javax.swing.JLabel();
    pkMapFile = new javax.swing.JTextField();
    selectMapFile = new FlatButton();
    jPanel2 = new javax.swing.JPanel();

    setLayout(new java.awt.GridBagLayout());

    previewDml.setText(ResourceMgr.getString("LblPreviewDml")); // NOI18N
    previewDml.setToolTipText(ResourceMgr.getString("d_LblPreviewDml")); // NOI18N
    previewDml.setBorder(null);
    previewDml.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    previewDml.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    previewDml.setIconTextGap(5);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(10, 12, 0, 11);
    add(previewDml, gridBagConstraints);

    requiredFieldColor.setToolTipText(ResourceMgr.getString("LblReqFldColor"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(10, 8, 0, 25);
    add(requiredFieldColor, gridBagConstraints);

    highlightRequired.setText(ResourceMgr.getString("LblHiliteRqd")); // NOI18N
    highlightRequired.setToolTipText(ResourceMgr.getString("d_LblHiliteRqd")); // NOI18N
    highlightRequired.setBorder(null);
    highlightRequired.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    highlightRequired.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    highlightRequired.setIconTextGap(5);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(13, 12, 0, 0);
    add(highlightRequired, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    add(dummyPanel, gridBagConstraints);

    warnDiscard.setText(ResourceMgr.getString("LblWarnChgResultSet")); // NOI18N
    warnDiscard.setToolTipText(ResourceMgr.getString("d_LblWarnChgResultSet")); // NOI18N
    warnDiscard.setBorder(null);
    warnDiscard.setMargin(new java.awt.Insets(0, 0, 0, 0));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
    add(warnDiscard, gridBagConstraints);

    jPanel1.setLayout(new java.awt.GridBagLayout());

    pkMapFileLabel.setText(ResourceMgr.getString("LblPKMapFile")); // NOI18N
    pkMapFileLabel.setToolTipText(ResourceMgr.getString("d_LblPKMapFile")); // NOI18N
    pkMapFileLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 10));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    jPanel1.add(pkMapFileLabel, gridBagConstraints);

    pkMapFile.setHorizontalAlignment(javax.swing.JTextField.LEFT);
    pkMapFile.setMaximumSize(new java.awt.Dimension(2147483647, 22));
    pkMapFile.setMinimumSize(new java.awt.Dimension(25, 22));
    pkMapFile.setPreferredSize(new java.awt.Dimension(72, 22));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
    jPanel1.add(pkMapFile, gridBagConstraints);

    selectMapFile.setText("...");
    selectMapFile.setMaximumSize(new java.awt.Dimension(22, 22));
    selectMapFile.setMinimumSize(new java.awt.Dimension(22, 22));
    selectMapFile.setPreferredSize(new java.awt.Dimension(22, 22));
    selectMapFile.addActionListener(this);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
    jPanel1.add(selectMapFile, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.weightx = 1.0;
    jPanel1.add(jPanel2, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(15, 12, 0, 15);
    add(jPanel1, gridBagConstraints);
  }

  // Code for dispatching events from components to event handlers.

  public void actionPerformed(java.awt.event.ActionEvent evt) {
    if (evt.getSource() == selectMapFile) {
      DataEditOptionsPanel.this.selectMapFile(evt);
    }
  }// </editor-fold>//GEN-END:initComponents

	private void selectMapFile(java.awt.event.ActionEvent evt)//GEN-FIRST:event_selectMapFile
	{//GEN-HEADEREND:event_selectMapFile
		String fileName = FileDialogUtil.selectPkMapFile(this);
		if (fileName != null) pkMapFile.setText(fileName);
	}//GEN-LAST:event_selectMapFile


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel dummyPanel;
  private javax.swing.JCheckBox highlightRequired;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JTextField pkMapFile;
  private javax.swing.JLabel pkMapFileLabel;
  private javax.swing.JCheckBox previewDml;
  private workbench.gui.components.WbColorPicker requiredFieldColor;
  private javax.swing.JButton selectMapFile;
  private javax.swing.JCheckBox warnDiscard;
  // End of variables declaration//GEN-END:variables

}
