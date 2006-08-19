/*
 * DataEditOptionsPanel.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2006, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.settings;

import javax.swing.JPanel;
import workbench.gui.components.DividerBorder;
import workbench.gui.components.FlatButton;
import workbench.gui.components.WbCheckBoxLabel;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.util.FileDialogUtil;

/**
 *
 * @author  support@sql-workbench.net
 */
public class DataEditOptionsPanel
	extends JPanel
	implements java.awt.event.ActionListener, workbench.interfaces.Restoreable
{

	/** Creates new form DataEditOptionsPanel */
	public DataEditOptionsPanel()
	{
		initComponents();
		setBorder(DividerBorder.BOTTOM_DIVIDER);
		restoreSettings();
	}

	public void restoreSettings()
	{
		pkMapFile.setCaretPosition(0);
		alternateColor.setSelectedColor(Settings.getInstance().getAlternateRowColor());
		requiredFieldColor.setSelectedColor(Settings.getInstance().getRequiredFieldColor());
		dataFont.setSelectedFont(Settings.getInstance().getDataFont());
	}

	public void saveSettings()
	{
		Settings set = Settings.getInstance();
		set.setUseAlternateRowColor(useAlternateRowColors.isSelected());
		set.setAlternateRowColor(alternateColor.getSelectedColor());
		set.setAllowRowHeightResizing(rowHeightResize.isSelected());
		set.setDataFont(dataFont.getSelectedFont());
		set.setRequiredFieldColor(requiredFieldColor.getSelectedColor());
		set.setHighlightRequiredFields(this.highlightRequired.isSelected());
		set.setPreviewDml(this.previewDml.isSelected());
		set.setPKMappingFilename(pkMapFile.getText());
	}
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
  // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
  private void initComponents()
  {
    java.awt.GridBagConstraints gridBagConstraints;

    pkMapFileLabel = new javax.swing.JLabel();
    jPanel3 = new javax.swing.JPanel();
    pkMapFile = new javax.swing.JTextField();
    selectMapFile = new FlatButton();
    previewDmlLabel = new WbCheckBoxLabel();
    previewDml = new javax.swing.JCheckBox();
    labelRowHeight = new WbCheckBoxLabel();
    rowHeightResize = new javax.swing.JCheckBox();
    requiredFieldLabel = new javax.swing.JLabel();
    requiredFieldColor = new workbench.gui.components.WbColorPicker();
    dataFontLabel = new javax.swing.JLabel();
    dataFont = new workbench.gui.components.WbFontPicker();
    highlightRequiredLabel = new WbCheckBoxLabel();
    highlightRequired = new javax.swing.JCheckBox();
    alternatingColorsLabel = new WbCheckBoxLabel();
    useAlternateRowColors = new javax.swing.JCheckBox();
    alternateColorLabel = new javax.swing.JLabel();
    alternateColor = new workbench.gui.components.WbColorPicker();

    setLayout(new java.awt.GridBagLayout());

    pkMapFileLabel.setText(ResourceMgr.getString("LblPKMapFile"));
    pkMapFileLabel.setToolTipText(ResourceMgr.getDescription("LblPKMapFile"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(11, 12, 0, 0);
    add(pkMapFileLabel, gridBagConstraints);

    jPanel3.setLayout(new java.awt.BorderLayout(5, 0));

    pkMapFile.setHorizontalAlignment(javax.swing.JTextField.LEFT);
    pkMapFile.setText(Settings.getInstance().getPKMappingFilename());
    pkMapFile.setMaximumSize(new java.awt.Dimension(2147483647, 22));
    pkMapFile.setMinimumSize(new java.awt.Dimension(6, 22));
    pkMapFile.setPreferredSize(new java.awt.Dimension(72, 22));
    jPanel3.add(pkMapFile, java.awt.BorderLayout.CENTER);

    selectMapFile.setText("...");
    selectMapFile.setMaximumSize(new java.awt.Dimension(22, 22));
    selectMapFile.setMinimumSize(new java.awt.Dimension(22, 22));
    selectMapFile.setPreferredSize(new java.awt.Dimension(22, 22));
    selectMapFile.addActionListener(this);

    jPanel3.add(selectMapFile, java.awt.BorderLayout.EAST);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(7, 8, 0, 15);
    add(jPanel3, gridBagConstraints);

    previewDmlLabel.setLabelFor(previewDml);
    previewDmlLabel.setText(ResourceMgr.getString("LblPreviewDml"));
    previewDmlLabel.setToolTipText(ResourceMgr.getDescription("LblPreviewDml"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(10, 12, 0, 0);
    add(previewDmlLabel, gridBagConstraints);

    previewDml.setFont(null);
    previewDml.setSelected(Settings.getInstance().getPreviewDml());
    previewDml.setText("");
    previewDml.setBorder(null);
    previewDml.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    previewDml.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    previewDml.setIconTextGap(5);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(10, 9, 0, 11);
    add(previewDml, gridBagConstraints);

    labelRowHeight.setLabelFor(rowHeightResize);
    labelRowHeight.setText(ResourceMgr.getString("LblRowResize"));
    labelRowHeight.setToolTipText(ResourceMgr.getDescription("LblRowResize"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(7, 12, 0, 0);
    add(labelRowHeight, gridBagConstraints);

    rowHeightResize.setSelected(Settings.getInstance().getAllowRowHeightResizing());
    rowHeightResize.setText("");
    rowHeightResize.setBorder(null);
    rowHeightResize.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    rowHeightResize.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    rowHeightResize.setIconTextGap(5);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(7, 9, 0, 11);
    add(rowHeightResize, gridBagConstraints);

    requiredFieldLabel.setText(ResourceMgr.getString("LblReqFldColor"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(9, 8, 0, 0);
    add(requiredFieldLabel, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(5, 4, 0, 25);
    add(requiredFieldColor, gridBagConstraints);

    dataFontLabel.setText(ResourceMgr.getString("LblDataFont"));
    dataFontLabel.setToolTipText(ResourceMgr.getDescription("LblDataFont"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
    add(dataFontLabel, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(5, 8, 0, 15);
    add(dataFont, gridBagConstraints);

    highlightRequiredLabel.setLabelFor(highlightRequired);
    highlightRequiredLabel.setText(ResourceMgr.getString("LblHiliteRqd"));
    highlightRequiredLabel.setToolTipText(ResourceMgr.getDescription("LblHiliteRqd"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(9, 12, 0, 0);
    add(highlightRequiredLabel, gridBagConstraints);

    highlightRequired.setFont(null);
    highlightRequired.setSelected(Settings.getInstance().getHighlightRequiredFields());
    highlightRequired.setText("");
    highlightRequired.setBorder(null);
    highlightRequired.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    highlightRequired.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    highlightRequired.setIconTextGap(5);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(9, 9, 0, 0);
    add(highlightRequired, gridBagConstraints);

    alternatingColorsLabel.setLabelFor(useAlternateRowColors);
    alternatingColorsLabel.setText(ResourceMgr.getString("LblUseAltRowColor"));
    alternatingColorsLabel.setToolTipText(ResourceMgr.getDescription("LblUseAltRowColor"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
    add(alternatingColorsLabel, gridBagConstraints);

    useAlternateRowColors.setFont(null);
    useAlternateRowColors.setSelected(Settings.getInstance().getUseAlternateRowColor());
    useAlternateRowColors.setText("");
    useAlternateRowColors.setBorder(null);
    useAlternateRowColors.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    useAlternateRowColors.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    useAlternateRowColors.setIconTextGap(5);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(5, 9, 0, 0);
    add(useAlternateRowColors, gridBagConstraints);

    alternateColorLabel.setText(ResourceMgr.getString("LblAlternateRowColor"));
    alternateColorLabel.setToolTipText(ResourceMgr.getDescription("LblAlternateRowColor"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(5, 8, 0, 0);
    add(alternateColorLabel, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 4, 0, 25);
    add(alternateColor, gridBagConstraints);

  }

  // Code for dispatching events from components to event handlers.

  public void actionPerformed(java.awt.event.ActionEvent evt)
  {
    if (evt.getSource() == selectMapFile)
    {
      DataEditOptionsPanel.this.selectMapFile(evt);
    }
  }// </editor-fold>//GEN-END:initComponents

	private void selectMapFile(java.awt.event.ActionEvent evt)//GEN-FIRST:event_selectMapFile
	{//GEN-HEADEREND:event_selectMapFile
		String fileName = FileDialogUtil.selectPkMapFile(this);
		if (fileName != null) pkMapFile.setText(fileName);
	}//GEN-LAST:event_selectMapFile


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private workbench.gui.components.WbColorPicker alternateColor;
  private javax.swing.JLabel alternateColorLabel;
  private javax.swing.JLabel alternatingColorsLabel;
  private workbench.gui.components.WbFontPicker dataFont;
  private javax.swing.JLabel dataFontLabel;
  private javax.swing.JCheckBox highlightRequired;
  private javax.swing.JLabel highlightRequiredLabel;
  private javax.swing.JPanel jPanel3;
  private javax.swing.JLabel labelRowHeight;
  private javax.swing.JTextField pkMapFile;
  private javax.swing.JLabel pkMapFileLabel;
  private javax.swing.JCheckBox previewDml;
  private javax.swing.JLabel previewDmlLabel;
  private workbench.gui.components.WbColorPicker requiredFieldColor;
  private javax.swing.JLabel requiredFieldLabel;
  private javax.swing.JCheckBox rowHeightResize;
  private javax.swing.JButton selectMapFile;
  private javax.swing.JCheckBox useAlternateRowColors;
  // End of variables declaration//GEN-END:variables

}
