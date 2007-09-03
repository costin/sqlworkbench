/*
 * GeneralOptionsPanel.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2007, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.settings;

import javax.swing.JPanel;
import workbench.gui.components.NumberField;
import workbench.gui.components.WbCheckBoxLabel;
import workbench.interfaces.Restoreable;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;

/**
 *
 * @author  support@sql-workbench.net
 */
public class DataFormattingOptionsPanel
	extends JPanel
	implements Restoreable
{

	/** Creates new form GeneralOptionsPanel */
	public DataFormattingOptionsPanel()
	{
		initComponents();
		restoreSettings();
	}
	
	public void restoreSettings()
	{
		alternateColor.setSelectedColor(Settings.getInstance().getAlternateRowColor());
	}

	public void saveSettings()
	{
		Settings set = Settings.getInstance();
		set.setUseAlternateRowColor(useAlternateRowColors.isSelected());
		set.setAlternateRowColor(alternateColor.getSelectedColor());
		set.setDefaultDateFormat(this.dateFormatTextField.getText());
		set.setDefaultTimeFormat(this.timeFormat.getText());
		set.setDefaultTimestampFormat(this.timestampFormatTextField.getText());
		set.setMaxFractionDigits(((NumberField)this.maxDigitsField).getValue());
		set.setDecimalSymbol(this.decimalField.getText());
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
  // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    dateFormatLabel = new javax.swing.JLabel();
    dateFormatTextField = new javax.swing.JTextField();
    decimalLabel = new javax.swing.JLabel();
    decimalField = new javax.swing.JTextField();
    maxDigitsLabel = new javax.swing.JLabel();
    maxDigitsField = new NumberField();
    jPanel1 = new javax.swing.JPanel();
    timestampFormatLabel = new javax.swing.JLabel();
    timestampFormatTextField = new javax.swing.JTextField();
    timeFormatLabel = new javax.swing.JLabel();
    timeFormat = new javax.swing.JTextField();
    alternatingColorsLabel = new WbCheckBoxLabel();
    useAlternateRowColors = new javax.swing.JCheckBox();
    alternateColorLabel = new javax.swing.JLabel();
    alternateColor = new workbench.gui.components.WbColorPicker();

    setLayout(new java.awt.GridBagLayout());

    dateFormatLabel.setText(ResourceMgr.getString("LblDateFormat"));
    dateFormatLabel.setToolTipText(ResourceMgr.getDescription("LblDateFormat"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
    add(dateFormatLabel, gridBagConstraints);

    dateFormatTextField.setText(Settings.getInstance().getDefaultDateFormat());
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 15);
    add(dateFormatTextField, gridBagConstraints);

    decimalLabel.setText(ResourceMgr.getString("LblDecimalSymbol"));
    decimalLabel.setToolTipText(ResourceMgr.getDescription("LblDecimalSymbol"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 12, 0, 0);
    add(decimalLabel, gridBagConstraints);

    decimalField.setText(Settings.getInstance().getDecimalSymbol());
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 10, 0, 15);
    add(decimalField, gridBagConstraints);

    maxDigitsLabel.setText(ResourceMgr.getString("LblMaxDigits"));
    maxDigitsLabel.setToolTipText(ResourceMgr.getDescription("LblMaxDigits"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 12, 0, 0);
    add(maxDigitsLabel, gridBagConstraints);

    maxDigitsField.setText(Integer.toString(Settings.getInstance().getMaxFractionDigits()));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 10, 0, 15);
    add(maxDigitsField, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.weighty = 1.0;
    add(jPanel1, gridBagConstraints);

    timestampFormatLabel.setText(ResourceMgr.getString("LblTimestampFormat"));
    timestampFormatLabel.setToolTipText(ResourceMgr.getDescription("LblTimestampFormat"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 12, 0, 0);
    add(timestampFormatLabel, gridBagConstraints);

    timestampFormatTextField.setText(Settings.getInstance().getDefaultTimestampFormat());
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 10, 0, 15);
    add(timestampFormatTextField, gridBagConstraints);

    timeFormatLabel.setText(ResourceMgr.getString("LblTimeFormat"));
    timeFormatLabel.setToolTipText(ResourceMgr.getDescription("LblTimeFormat"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 12, 0, 0);
    add(timeFormatLabel, gridBagConstraints);

    timeFormat.setText(Settings.getInstance().getDefaultTimeFormat());
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 10, 0, 15);
    add(timeFormat, gridBagConstraints);

    alternatingColorsLabel.setText(ResourceMgr.getString("LblUseAltRowColor"));
    alternatingColorsLabel.setToolTipText(ResourceMgr.getDescription("LblUseAltRowColor"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(10, 12, 0, 0);
    add(alternatingColorsLabel, gridBagConstraints);

    useAlternateRowColors.setFont(null);
    useAlternateRowColors.setSelected(Settings.getInstance().getUseAlternateRowColor());
    useAlternateRowColors.setBorder(null);
    useAlternateRowColors.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    useAlternateRowColors.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    useAlternateRowColors.setIconTextGap(5);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(11, 9, 0, 0);
    add(useAlternateRowColors, gridBagConstraints);

    alternateColorLabel.setText(ResourceMgr.getString("LblAlternateRowColor"));
    alternateColorLabel.setToolTipText(ResourceMgr.getDescription("LblAlternateRowColor"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(10, 8, 0, 0);
    add(alternateColorLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(6, 4, 0, 25);
    add(alternateColor, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private workbench.gui.components.WbColorPicker alternateColor;
  private javax.swing.JLabel alternateColorLabel;
  private javax.swing.JLabel alternatingColorsLabel;
  private javax.swing.JLabel dateFormatLabel;
  private javax.swing.JTextField dateFormatTextField;
  private javax.swing.JTextField decimalField;
  private javax.swing.JLabel decimalLabel;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JTextField maxDigitsField;
  private javax.swing.JLabel maxDigitsLabel;
  private javax.swing.JTextField timeFormat;
  private javax.swing.JLabel timeFormatLabel;
  private javax.swing.JLabel timestampFormatLabel;
  private javax.swing.JTextField timestampFormatTextField;
  private javax.swing.JCheckBox useAlternateRowColors;
  // End of variables declaration//GEN-END:variables

}
