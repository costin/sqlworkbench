/*
 * EditorOptionsPanel.java
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
import workbench.gui.components.NumberField;
import workbench.gui.components.WbCheckBoxLabel;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.util.StringUtil;

/**
 *
 * @author  support@sql-workbench.net
 */
public class EditorOptionsPanel
	extends JPanel
	implements workbench.interfaces.Restoreable
{

	/** Creates new form EditorOptionsPanel */
	public EditorOptionsPanel()
	{
		initComponents();
		restoreSettings();
	}

	public void restoreSettings()
	{
		errorColor.setColor(Settings.getInstance().getEditorErrorColor());
		selectionColor.setColor(Settings.getInstance().getEditorSelectionColor());
		editorFont.setSelectedFont(Settings.getInstance().getEditorFont());

		// It is important to add these in the correct order
		// which is defined by the numeric values from Settings.SHOW_NO_FILENAME
		// SHOW_FILENAME and SHOW_FULL_PATH
		this.windowTitleComboBox.addItem(ResourceMgr.getString("TxtShowNone"));
		this.windowTitleComboBox.addItem(ResourceMgr.getString("TxtShowName"));
		this.windowTitleComboBox.addItem(ResourceMgr.getString("TxtShowPath"));
		int type = Settings.getInstance().getShowFilenameInWindowTitle();
		if (type >= Settings.SHOW_NO_FILENAME && type <= Settings.SHOW_FULL_PATH)
		{
			this.windowTitleComboBox.setSelectedIndex(type);
		}


		String paste = Settings.getInstance().getAutoCompletionPasteCase();
		if ("lower".equals(paste)) this.completionPasteCase.setSelectedIndex(0);
		else if ("lower".equals(paste)) this.completionPasteCase.setSelectedIndex(1);
		else this.completionPasteCase.setSelectedIndex(2);
	}

	public void saveSettings()
	{
		Settings set = Settings.getInstance();
		set.setMaxHistorySize(((NumberField)this.historySizeField).getValue());
		set.setAutoCompletionPasteCase((String)this.completionPasteCase.getSelectedItem());
		set.setCloseAutoCompletionWithSearch(closePopup.isSelected());
		set.setEditorErrorColor(errorColor.getSelectedColor());
		set.setEditorFont(editorFont.getSelectedFont());
		set.setAlternateDelimiter(this.altDelimitTextField.getText());
		set.setRightClickMovesCursor(rightClickMovesCursor.isSelected());
		set.setShowFilenameInWindowTitle(this.windowTitleComboBox.getSelectedIndex());
		set.setEditorSelectionColor(selectionColor.getSelectedColor());
		set.setAutoJumpNextStatement(this.autoAdvance.isSelected());
		set.setEditorTabWidth(StringUtil.getIntValue(this.tabSize.getText(), 2));
		set.setElectricScroll(StringUtil.getIntValue(electricScroll.getText(),-1));
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

    autoAdvanceLabel = new WbCheckBoxLabel();
    autoAdvance = new javax.swing.JCheckBox();
    editorTabSizeLabel = new javax.swing.JLabel();
    tabSize = new javax.swing.JTextField();
    altDelimLabel = new javax.swing.JLabel();
    altDelimitTextField = new javax.swing.JTextField();
    historySizeLabel = new javax.swing.JLabel();
    historySizeField = new NumberField();
    electricScrollLabel = new javax.swing.JLabel();
    electricScroll = new javax.swing.JTextField();
    rightClickLabel = new WbCheckBoxLabel();
    rightClickMovesCursor = new javax.swing.JCheckBox();
    windowTitleLabel = new javax.swing.JLabel();
    windowTitleComboBox = new javax.swing.JComboBox();
    selectionColorLabel = new javax.swing.JLabel();
    errorColor = new workbench.gui.components.WbColorPicker();
    errorColorLabel = new javax.swing.JLabel();
    selectionColor = new workbench.gui.components.WbColorPicker();
    editorFontLabel = new javax.swing.JLabel();
    editorFont = new workbench.gui.components.WbFontPicker();
    labelCloseSearch = new WbCheckBoxLabel();
    closePopup = new javax.swing.JCheckBox();
    completionPasteCase = new javax.swing.JComboBox();
    pasteLabel = new javax.swing.JLabel();

    setLayout(new java.awt.GridBagLayout());

    autoAdvanceLabel.setLabelFor(autoAdvance);
    autoAdvanceLabel.setText(ResourceMgr.getString("LblAutoAdvance"));
    autoAdvanceLabel.setToolTipText(ResourceMgr.getDescription("LblAutoAdvance"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(8, 12, 2, 0);
    add(autoAdvanceLabel, gridBagConstraints);

    autoAdvance.setSelected(Settings.getInstance().getAutoJumpNextStatement());
    autoAdvance.setText("");
    autoAdvance.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    autoAdvance.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    autoAdvance.setIconTextGap(5);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(8, 7, 2, 11);
    add(autoAdvance, gridBagConstraints);

    editorTabSizeLabel.setText(ResourceMgr.getString("LblTabWidth"));
    editorTabSizeLabel.setToolTipText(ResourceMgr.getDescription("LblTabWidth"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 11;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
    add(editorTabSizeLabel, gridBagConstraints);

    tabSize.setHorizontalAlignment(javax.swing.JTextField.LEFT);
    tabSize.setText(Settings.getInstance().getProperty("workbench.editor.tabwidth", "2"));
    tabSize.setMinimumSize(new java.awt.Dimension(72, 20));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 11;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(3, 11, 0, 15);
    add(tabSize, gridBagConstraints);

    altDelimLabel.setText(ResourceMgr.getString("LblAltDelimit"));
    altDelimLabel.setToolTipText(ResourceMgr.getDescription("LblAltDelimit"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(9, 12, 0, 0);
    add(altDelimLabel, gridBagConstraints);

    altDelimitTextField.setText(Settings.getInstance().getAlternateDelimiter());
    altDelimitTextField.setMinimumSize(new java.awt.Dimension(72, 20));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(6, 11, 0, 15);
    add(altDelimitTextField, gridBagConstraints);

    historySizeLabel.setText(ResourceMgr.getString("LblHistorySize"));
    historySizeLabel.setToolTipText(ResourceMgr.getDescription("LblHistorySize"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 12, 0, 0);
    add(historySizeLabel, gridBagConstraints);

    historySizeField.setText(Integer.toString(Settings.getInstance().getMaxHistorySize()));
    historySizeField.setMinimumSize(new java.awt.Dimension(72, 20));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 11, 0, 15);
    add(historySizeField, gridBagConstraints);

    electricScrollLabel.setText(ResourceMgr.getString("LblSettingElectricScroll"));
    electricScrollLabel.setToolTipText(ResourceMgr.getDescription("LblSettingElectricScroll"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 9;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
    add(electricScrollLabel, gridBagConstraints);

    electricScroll.setText(Integer.toString(Settings.getInstance().getElectricScroll()));
    electricScroll.setMinimumSize(new java.awt.Dimension(72, 20));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 9;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 11, 0, 15);
    add(electricScroll, gridBagConstraints);

    rightClickLabel.setLabelFor(rightClickMovesCursor);
    rightClickLabel.setText(ResourceMgr.getString("LblRightClickMove"));
    rightClickLabel.setToolTipText(ResourceMgr.getDescription("LblRightClickMove"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
    add(rightClickLabel, gridBagConstraints);

    rightClickMovesCursor.setSelected(Settings.getInstance().getRightClickMovesCursor());
    rightClickMovesCursor.setText("");
    rightClickMovesCursor.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    rightClickMovesCursor.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    rightClickMovesCursor.setIconTextGap(5);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(1, 7, 0, 11);
    add(rightClickMovesCursor, gridBagConstraints);

    windowTitleLabel.setText(ResourceMgr.getString("LblShowEditorInfo"));
    windowTitleLabel.setToolTipText(ResourceMgr.getDescription("LblShowEditorInfo"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 10;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 12, 0, 0);
    add(windowTitleLabel, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 10;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 11, 0, 15);
    add(windowTitleComboBox, gridBagConstraints);

    selectionColorLabel.setText(ResourceMgr.getString("LblSelectionColor"));
    selectionColorLabel.setToolTipText(ResourceMgr.getDescription("LblSelectionColor"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(8, 12, 0, 0);
    add(selectionColorLabel, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
    add(errorColor, gridBagConstraints);

    errorColorLabel.setText(ResourceMgr.getString("LblSelectErrorColor"));
    errorColorLabel.setToolTipText(ResourceMgr.getDescription("LblSelectErrorColor"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(9, 12, 0, 0);
    add(errorColorLabel, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 5, 0, 0);
    add(selectionColor, gridBagConstraints);

    editorFontLabel.setText(ResourceMgr.getString("LblEditorFont"));
    editorFontLabel.setToolTipText(ResourceMgr.getDescription("LblEditorFont"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(9, 12, 0, 0);
    add(editorFontLabel, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 15);
    add(editorFont, gridBagConstraints);

    labelCloseSearch.setLabelFor(closePopup);
    labelCloseSearch.setText(ResourceMgr.getString("TxtCloseCompletion"));
    labelCloseSearch.setToolTipText(ResourceMgr.getDescription("TxtCloseCompletion"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
    add(labelCloseSearch, gridBagConstraints);

    closePopup.setSelected(Settings.getInstance().getCloseAutoCompletionWithSearch());
    closePopup.setText("");
    closePopup.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    closePopup.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(1, 7, 0, 11);
    add(closePopup, gridBagConstraints);

    completionPasteCase.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Lowercase", "Uppercase", "As is" }));
    completionPasteCase.setToolTipText(ResourceMgr.getDescription("LblPasteCase"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 11, 0, 15);
    add(completionPasteCase, gridBagConstraints);

    pasteLabel.setText(ResourceMgr.getString("LblPasteCase"));
    pasteLabel.setToolTipText(ResourceMgr.getDescription("LblPasteCase"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
    add(pasteLabel, gridBagConstraints);

  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel altDelimLabel;
  private javax.swing.JTextField altDelimitTextField;
  private javax.swing.JCheckBox autoAdvance;
  private javax.swing.JLabel autoAdvanceLabel;
  private javax.swing.JCheckBox closePopup;
  private javax.swing.JComboBox completionPasteCase;
  private workbench.gui.components.WbFontPicker editorFont;
  private javax.swing.JLabel editorFontLabel;
  private javax.swing.JLabel editorTabSizeLabel;
  private javax.swing.JTextField electricScroll;
  private javax.swing.JLabel electricScrollLabel;
  private workbench.gui.components.WbColorPicker errorColor;
  private javax.swing.JLabel errorColorLabel;
  private javax.swing.JTextField historySizeField;
  private javax.swing.JLabel historySizeLabel;
  private javax.swing.JLabel labelCloseSearch;
  private javax.swing.JLabel pasteLabel;
  private javax.swing.JLabel rightClickLabel;
  private javax.swing.JCheckBox rightClickMovesCursor;
  private workbench.gui.components.WbColorPicker selectionColor;
  private javax.swing.JLabel selectionColorLabel;
  private javax.swing.JTextField tabSize;
  private javax.swing.JComboBox windowTitleComboBox;
  private javax.swing.JLabel windowTitleLabel;
  // End of variables declaration//GEN-END:variables

}
