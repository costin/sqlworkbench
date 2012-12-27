/*
 * DataFormattingOptionsPanel.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2013, Thomas Kellerer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at.
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.settings;

import javax.swing.JPanel;
import workbench.gui.WbSwingUtilities;
import workbench.gui.components.NumberField;
import workbench.interfaces.Restoreable;
import workbench.interfaces.ValidatingComponent;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.util.StringUtil;

/**
 *
 * @author  Thomas Kellerer
 */
public class DataFormattingOptionsPanel
	extends JPanel
	implements Restoreable, ValidatingComponent
{
	public DataFormattingOptionsPanel()
	{
		super();
		initComponents();
	}

	@Override
	public void restoreSettings()
	{
		dateFormatTextField.setText(Settings.getInstance().getDefaultDateFormat());
		dateFormatTextField.setCaretPosition(0);
		timestampFormatTextField.setText(Settings.getInstance().getDefaultTimestampFormat());
		timestampFormatTextField.setCaretPosition(0);
		decimalField.setText(Settings.getInstance().getDecimalSymbol());
		timeFormat.setText(Settings.getInstance().getDefaultTimeFormat());
		maxDigitsField.setText(Integer.toString(Settings.getInstance().getMaxFractionDigits()));
	}

	@Override
	public void saveSettings()
	{
		Settings.getInstance().setDefaultDateFormat(this.dateFormatTextField.getText());
		Settings.getInstance().setDefaultTimeFormat(this.timeFormat.getText());
		Settings.getInstance().setDefaultTimestampFormat(this.timestampFormatTextField.getText());
		Settings.getInstance().setMaxFractionDigits(((NumberField)this.maxDigitsField).getValue());
		Settings.getInstance().setDecimalSymbol(this.decimalField.getText());
		Settings.getInstance().setProperty("workbench.db.oracle.fixdatetype", oraDateFix.isSelected());
	}


	@Override
	public boolean validateInput()
	{
		String format = dateFormatTextField.getText();

		if (StringUtil.isNonBlank(format))
		{
			String err = StringUtil.isDatePatternValid(format);
			if (err != null)
			{
				String msg = ResourceMgr.getFormattedString("ErrInvalidInput", dateFormatLabel.getText(), err);
				WbSwingUtilities.showErrorMessage(this, ResourceMgr.getString("TxtError"), msg);
				return false;
			}
		}

		format = timestampFormatTextField.getText();
		if (StringUtil.isNonBlank(format))
		{
			String err = StringUtil.isDatePatternValid(format);
			if (err != null)
			{
				String msg = ResourceMgr.getFormattedString("ErrInvalidInput", timestampFormatLabel.getText(), err);
				WbSwingUtilities.showErrorMessage(this, ResourceMgr.getString("TxtError"), msg);
				return false;
			}
		}

		format = timeFormat.getText();
		if (StringUtil.isNonBlank(format))
		{
			String err = StringUtil.isDatePatternValid(format);
			if (err != null)
			{
				String msg = ResourceMgr.getFormattedString("ErrInvalidInput", timeFormatLabel.getText(), err);
				WbSwingUtilities.showErrorMessage(this, ResourceMgr.getString("TxtError"), msg);
				return false;
			}
		}

		return true;
	}

	@Override
	public void componentDisplayed()
	{
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {
    java.awt.GridBagConstraints gridBagConstraints;

    dateFormatLabel = new javax.swing.JLabel();
    dateFormatTextField = new javax.swing.JTextField();
    decimalLabel = new javax.swing.JLabel();
    decimalField = new javax.swing.JTextField();
    maxDigitsLabel = new javax.swing.JLabel();
    maxDigitsField = new NumberField();
    timestampFormatLabel = new javax.swing.JLabel();
    timestampFormatTextField = new javax.swing.JTextField();
    timeFormatLabel = new javax.swing.JLabel();
    timeFormat = new javax.swing.JTextField();
    oraDateFix = new javax.swing.JCheckBox();

    setLayout(new java.awt.GridBagLayout());

    dateFormatLabel.setText(ResourceMgr.getString("LblDateFormat")); // NOI18N
    dateFormatLabel.setToolTipText(ResourceMgr.getString("d_LblDateFormat")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(8, 10, 0, 0);
    add(dateFormatLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(8, 7, 0, 9);
    add(dateFormatTextField, gridBagConstraints);

    decimalLabel.setText(ResourceMgr.getString("LblDecimalSymbol")); // NOI18N
    decimalLabel.setToolTipText(ResourceMgr.getDescription("LblDecimalSymbol"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(8, 10, 0, 0);
    add(decimalLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(8, 7, 0, 79);
    add(decimalField, gridBagConstraints);

    maxDigitsLabel.setText(ResourceMgr.getString("LblMaxDigits")); // NOI18N
    maxDigitsLabel.setToolTipText(ResourceMgr.getDescription("LblMaxDigits"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(8, 10, 0, 0);
    add(maxDigitsLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(8, 7, 0, 79);
    add(maxDigitsField, gridBagConstraints);

    timestampFormatLabel.setText(ResourceMgr.getString("LblTimestampFormat")); // NOI18N
    timestampFormatLabel.setToolTipText(ResourceMgr.getDescription("LblTimestampFormat"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(8, 10, 0, 0);
    add(timestampFormatLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(8, 7, 0, 9);
    add(timestampFormatTextField, gridBagConstraints);

    timeFormatLabel.setText(ResourceMgr.getString("LblTimeFormat")); // NOI18N
    timeFormatLabel.setToolTipText(ResourceMgr.getDescription("LblTimeFormat"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(8, 10, 0, 0);
    add(timeFormatLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(8, 7, 0, 79);
    add(timeFormat, gridBagConstraints);

    oraDateFix.setSelected(Settings.getInstance().getBoolProperty("workbench.db.oracle.fixdatetype", false));
    oraDateFix.setText(ResourceMgr.getString("LblOraDataTS")); // NOI18N
    oraDateFix.setToolTipText(ResourceMgr.getString("d_LblOraDataTS")); // NOI18N
    oraDateFix.setBorder(null);
    oraDateFix.setDoubleBuffered(true);
    oraDateFix.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    oraDateFix.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    oraDateFix.setIconTextGap(5);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 10);
    add(oraDateFix, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel dateFormatLabel;
  private javax.swing.JTextField dateFormatTextField;
  private javax.swing.JTextField decimalField;
  private javax.swing.JLabel decimalLabel;
  private javax.swing.JTextField maxDigitsField;
  private javax.swing.JLabel maxDigitsLabel;
  private javax.swing.JCheckBox oraDateFix;
  private javax.swing.JTextField timeFormat;
  private javax.swing.JLabel timeFormatLabel;
  private javax.swing.JLabel timestampFormatLabel;
  private javax.swing.JTextField timestampFormatTextField;
  // End of variables declaration//GEN-END:variables

}
