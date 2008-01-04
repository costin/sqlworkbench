/*
 * GeneralOptionsPanel.java
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

import java.util.Collection;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import workbench.gui.components.WbCheckBoxLabel;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.util.StringUtil;
import workbench.util.WbLocale;

/**
 *
 * @author  support@sql-workbench.net
 */
public class GeneralOptionsPanel
	extends JPanel
	implements workbench.interfaces.Restoreable
{

	/** Creates new form GeneralOptionsPanel */
	public GeneralOptionsPanel()
	{
		initComponents();
		pdfReaderPath.setAllowMultiple(false);
		standardFont.setAllowFontReset(true);
		String[] updTypes = new String[] {
			ResourceMgr.getString("LblUpdCheckNever"),
			ResourceMgr.getString("LblUpdCheckDaily"),
			ResourceMgr.getString("LblUpdCheck7"),
			ResourceMgr.getString("LblUpdCheck14"),
			ResourceMgr.getString("LblUpdCheck30")
		};
		checkInterval.setModel(new DefaultComboBoxModel(updTypes));
	}

	public void restoreSettings()
	{
		msgLogFont.setSelectedFont(Settings.getInstance().getMsgLogFont());
		standardFont.setSelectedFont(Settings.getInstance().getStandardFont());
		pdfReaderPath.setFilename(Settings.getInstance().getPDFReaderPath());
		logLevel.setSelectedItem(LogMgr.getLevel());
		int days = Settings.getInstance().getUpdateCheckInterval();
		if (days == 1)
		{
			checkInterval.setSelectedIndex(1);
		}
		else if (days == 7)
		{
			checkInterval.setSelectedIndex(2);
		}
		else if (days == 14)
		{
			checkInterval.setSelectedIndex(3);
		}
		else if (days == 30)
		{
			checkInterval.setSelectedIndex(4);
		}
		else
		{
			checkInterval.setSelectedIndex(0);
		}
		languageDropDown.removeAllItems();
		String currentLang = Settings.getInstance().getLanguage().getLanguage();
		
		Collection<WbLocale> locales = Settings.getInstance().getLanguages();
		int index = 0;
		int currentIndex = -1;
		for (WbLocale l : locales)
		{
			languageDropDown.addItem(l);
			if (l.getLocale().getLanguage().equals(currentLang))
			{
				currentIndex = index;
			}
			index++;
		}
		if (currentIndex != -1)
		{
			languageDropDown.setSelectedIndex(currentIndex);
		}
	}

	public void saveSettings()
	{
		Settings set = Settings.getInstance();

		// General settings
		set.setStandardFont(standardFont.getSelectedFont());
		set.setUseEncryption(this.useEncryption.isSelected());
		set.setMsgLogFont(msgLogFont.getSelectedFont());
		set.setUseAnimatedIcon(this.enableAnimatedIcon.isSelected());
		set.setQuoteChar(this.quoteCharField.getText().trim());
		set.setConsolidateLogMsg(this.consolidateLog.isSelected());
		set.setDefaultTextDelimiter(this.textDelimiterField.getText());
		set.setPDFReaderPath(pdfReaderPath.getFilename());
		int index = checkInterval.getSelectedIndex();
		switch (index)
		{
			case 0:
				set.setUpdateCheckInterval(-1);
				break;
			case 1:
				set.setUpdateCheckInterval(1);
				break;
			case 2: 
				set.setUpdateCheckInterval(7);
				break;
			case 3: 
				set.setUpdateCheckInterval(14);
				break;
			case 4: 
				set.setUpdateCheckInterval(30);
				break;
			default:
				set.setUpdateCheckInterval(-1);
				break;
		}
		String level = (String)logLevel.getSelectedItem();
		LogMgr.setLevel(level);
		set.setProperty("workbench.log.level", level);
		WbLocale wl = (WbLocale)languageDropDown.getSelectedItem();
		set.setLanguage(wl.getLocale());
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    useEncryptionLabel = new WbCheckBoxLabel();
    useEncryption = new javax.swing.JCheckBox();
    textDelimiterLabel = new javax.swing.JLabel();
    textDelimiterField = new javax.swing.JTextField();
    quoteCharLabel = new javax.swing.JLabel();
    quoteCharField = new javax.swing.JTextField();
    jPanel1 = new javax.swing.JPanel();
    enableAnimatedIconLabel = new WbCheckBoxLabel();
    enableAnimatedIcon = new javax.swing.JCheckBox();
    consolidateLogLabel = new WbCheckBoxLabel();
    consolidateLog = new javax.swing.JCheckBox();
    msgFontLabel = new javax.swing.JLabel();
    standardFontLabel = new javax.swing.JLabel();
    msgLogFont = new workbench.gui.components.WbFontPicker();
    standardFont = new workbench.gui.components.WbFontPicker();
    pdfReaderPathLabel = new javax.swing.JLabel();
    pdfReaderPath = new workbench.gui.components.WbFilePicker();
    logLevelLabel = new javax.swing.JLabel();
    logLevel = new javax.swing.JComboBox();
    checkUpdatesLabel = new WbCheckBoxLabel();
    checkInterval = new javax.swing.JComboBox();
    langLabel = new javax.swing.JLabel();
    languageDropDown = new javax.swing.JComboBox();

    setLayout(new java.awt.GridBagLayout());

    useEncryptionLabel.setLabelFor(useEncryption);
    useEncryptionLabel.setText(ResourceMgr.getString("LblUseEncryption"));
    useEncryptionLabel.setToolTipText(ResourceMgr.getDescription("LblUseEncryption"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(7, 12, 0, 0);
    add(useEncryptionLabel, gridBagConstraints);

    useEncryption.setFont(null);
    useEncryption.setSelected(Settings.getInstance().getUseEncryption());
    useEncryption.setText("");
    useEncryption.setBorder(null);
    useEncryption.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    useEncryption.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    useEncryption.setIconTextGap(5);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(7, 10, 0, 11);
    add(useEncryption, gridBagConstraints);

    textDelimiterLabel.setText(ResourceMgr.getString("LblFieldDelimiter"));
    textDelimiterLabel.setToolTipText(ResourceMgr.getDescription("LblFieldDelimiter"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 13;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 12, 0, 0);
    add(textDelimiterLabel, gridBagConstraints);

    textDelimiterField.setHorizontalAlignment(javax.swing.JTextField.LEFT);
    textDelimiterField.setText(Settings.getInstance().getDefaultTextDelimiter(true));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 13;
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 10, 0, 15);
    add(textDelimiterField, gridBagConstraints);

    quoteCharLabel.setText(ResourceMgr.getString("LblQuoteChar"));
    quoteCharLabel.setToolTipText(ResourceMgr.getDescription("LblQuoteChar"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 14;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 12, 0, 0);
    add(quoteCharLabel, gridBagConstraints);

    quoteCharField.setHorizontalAlignment(javax.swing.JTextField.LEFT);
    quoteCharField.setText(Settings.getInstance().getQuoteChar());
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 14;
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 10, 0, 15);
    add(quoteCharField, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 17;
    gridBagConstraints.weighty = 1.0;
    add(jPanel1, gridBagConstraints);

    enableAnimatedIconLabel.setLabelFor(enableAnimatedIcon);
    enableAnimatedIconLabel.setText(ResourceMgr.getString("LblEnableAnimatedIcon"));
    enableAnimatedIconLabel.setToolTipText(ResourceMgr.getDescription("LblEnableAnimatedIcon"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(6, 12, 1, 0);
    add(enableAnimatedIconLabel, gridBagConstraints);

    enableAnimatedIcon.setFont(null);
    enableAnimatedIcon.setSelected(Settings.getInstance().getUseAnimatedIcon());
    enableAnimatedIcon.setText("");
    enableAnimatedIcon.setBorder(null);
    enableAnimatedIcon.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    enableAnimatedIcon.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    enableAnimatedIcon.setIconTextGap(5);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(4, 10, 1, 25);
    add(enableAnimatedIcon, gridBagConstraints);

    consolidateLogLabel.setLabelFor(consolidateLog);
    consolidateLogLabel.setText(ResourceMgr.getString("LblConsolidateLog"));
    consolidateLogLabel.setToolTipText(ResourceMgr.getDescription("LblConsolidateLog"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(8, 12, 1, 0);
    add(consolidateLogLabel, gridBagConstraints);

    consolidateLog.setFont(null);
    consolidateLog.setSelected(Settings.getInstance().getConsolidateLogMsg());
    consolidateLog.setText("");
    consolidateLog.setBorder(null);
    consolidateLog.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    consolidateLog.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    consolidateLog.setIconTextGap(5);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(8, 10, 1, 11);
    add(consolidateLog, gridBagConstraints);

    msgFontLabel.setText(ResourceMgr.getString("LblMsgLogFont"));
    msgFontLabel.setToolTipText(ResourceMgr.getDescription("LblMsgLogFont"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 12, 0, 0);
    add(msgFontLabel, gridBagConstraints);

    standardFontLabel.setText(ResourceMgr.getString("LblStandardFont"));
    standardFontLabel.setToolTipText(ResourceMgr.getDescription("LblStandardFont"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(8, 12, 0, 0);
    add(standardFontLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 15);
    add(msgLogFont, gridBagConstraints);

    standardFont.setFont(standardFont.getFont());
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 10, 0, 15);
    add(standardFont, gridBagConstraints);

    pdfReaderPathLabel.setText(ResourceMgr.getString("LblReaderPath"));
    pdfReaderPathLabel.setToolTipText(ResourceMgr.getDescription("LblReaderPath"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 15;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 12, 0, 0);
    add(pdfReaderPathLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 15;
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(2, 10, 0, 15);
    add(pdfReaderPath, gridBagConstraints);

    logLevelLabel.setText(ResourceMgr.getString("LblLogLevel"));
    logLevelLabel.setToolTipText(ResourceMgr.getDescription("LblLogLevel"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 16;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
    add(logLevelLabel, gridBagConstraints);

    logLevel.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ERROR", "WARNING", "INFO", "DEBUG" }));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 16;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 10, 0, 0);
    add(logLevel, gridBagConstraints);

    checkUpdatesLabel.setText(ResourceMgr.getString("LblCheckForUpdate"));
    checkUpdatesLabel.setToolTipText(ResourceMgr.getDescription("LblCheckForUpdate"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(9, 12, 0, 0);
    add(checkUpdatesLabel, gridBagConstraints);

    checkInterval.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "never", "daily", "7 days", "14 days", "30 days" }));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(7, 10, 0, 0);
    add(checkInterval, gridBagConstraints);

    langLabel.setText(ResourceMgr.getString("LblLanguage"));
    langLabel.setToolTipText(ResourceMgr.getDescription("LblLanguage"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 12, 0, 0);
    add(langLabel, gridBagConstraints);

    languageDropDown.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "English", "German" }));
    languageDropDown.setToolTipText(ResourceMgr.getDescription("LblLanguage"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 15);
    add(languageDropDown, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JComboBox checkInterval;
  private javax.swing.JLabel checkUpdatesLabel;
  private javax.swing.JCheckBox consolidateLog;
  private javax.swing.JLabel consolidateLogLabel;
  private javax.swing.JCheckBox enableAnimatedIcon;
  private javax.swing.JLabel enableAnimatedIconLabel;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JLabel langLabel;
  private javax.swing.JComboBox languageDropDown;
  private javax.swing.JComboBox logLevel;
  private javax.swing.JLabel logLevelLabel;
  private javax.swing.JLabel msgFontLabel;
  private workbench.gui.components.WbFontPicker msgLogFont;
  private workbench.gui.components.WbFilePicker pdfReaderPath;
  private javax.swing.JLabel pdfReaderPathLabel;
  private javax.swing.JTextField quoteCharField;
  private javax.swing.JLabel quoteCharLabel;
  private workbench.gui.components.WbFontPicker standardFont;
  private javax.swing.JLabel standardFontLabel;
  private javax.swing.JTextField textDelimiterField;
  private javax.swing.JLabel textDelimiterLabel;
  private javax.swing.JCheckBox useEncryption;
  private javax.swing.JLabel useEncryptionLabel;
  // End of variables declaration//GEN-END:variables

}
