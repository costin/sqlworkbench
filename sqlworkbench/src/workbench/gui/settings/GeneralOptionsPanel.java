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

import java.awt.event.ActionListener;
import java.util.Collection;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import workbench.gui.components.WbCheckBoxLabel;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.util.WbLocale;

/**
 *
 * @author  support@sql-workbench.net
 */
public class GeneralOptionsPanel
	extends JPanel
	implements workbench.interfaces.Restoreable, ActionListener
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
		set.setShowTabIndex(showTabIndex.isSelected());
		set.setUseEncryption(this.useEncryption.isSelected());
		set.setMsgLogFont(msgLogFont.getSelectedFont());
		set.setUseAnimatedIcon(this.enableAnimatedIcon.isSelected());
		set.setQuoteChar(this.quoteCharField.getText().trim());
		set.setConsolidateLogMsg(this.consolidateLog.isSelected());
		set.setDefaultTextDelimiter(this.textDelimiterField.getText());
		set.setPDFReaderPath(pdfReaderPath.getFilename());
		set.setExitOnFirstConnectCancel(exitOnConnectCancel.isSelected());
		set.setShowConnectDialogOnStartup(autoConnect.isSelected());
		
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

    textDelimiterLabel = new javax.swing.JLabel();
    textDelimiterField = new javax.swing.JTextField();
    quoteCharLabel = new javax.swing.JLabel();
    quoteCharField = new javax.swing.JTextField();
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
    jPanel2 = new javax.swing.JPanel();
    useEncryption = new javax.swing.JCheckBox();
    consolidateLog = new javax.swing.JCheckBox();
    showTabIndex = new javax.swing.JCheckBox();
    enableAnimatedIcon = new javax.swing.JCheckBox();
    exitOnConnectCancel = new javax.swing.JCheckBox();
    autoConnect = new javax.swing.JCheckBox();

    setLayout(new java.awt.GridBagLayout());

    textDelimiterLabel.setText(ResourceMgr.getString("LblFieldDelimiter"));
    textDelimiterLabel.setToolTipText(ResourceMgr.getDescription("LblFieldDelimiter"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
    add(textDelimiterLabel, gridBagConstraints);

    textDelimiterField.setHorizontalAlignment(javax.swing.JTextField.LEFT);
    textDelimiterField.setText(Settings.getInstance().getDefaultTextDelimiter(true));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 15);
    add(textDelimiterField, gridBagConstraints);

    quoteCharLabel.setText(ResourceMgr.getString("LblQuoteChar"));
    quoteCharLabel.setToolTipText(ResourceMgr.getDescription("LblQuoteChar"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
    add(quoteCharLabel, gridBagConstraints);

    quoteCharField.setHorizontalAlignment(javax.swing.JTextField.LEFT);
    quoteCharField.setText(Settings.getInstance().getQuoteChar());
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 15);
    add(quoteCharField, gridBagConstraints);

    msgFontLabel.setText(ResourceMgr.getString("LblMsgLogFont"));
    msgFontLabel.setToolTipText(ResourceMgr.getDescription("LblMsgLogFont"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 12, 0, 0);
    add(msgFontLabel, gridBagConstraints);

    standardFontLabel.setText(ResourceMgr.getString("LblStandardFont"));
    standardFontLabel.setToolTipText(ResourceMgr.getDescription("LblStandardFont"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(8, 12, 0, 0);
    add(standardFontLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 15);
    add(msgLogFont, gridBagConstraints);

    standardFont.setFont(standardFont.getFont());
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 10, 0, 15);
    add(standardFont, gridBagConstraints);

    pdfReaderPathLabel.setText(ResourceMgr.getString("LblReaderPath"));
    pdfReaderPathLabel.setToolTipText(ResourceMgr.getDescription("LblReaderPath"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
    add(pdfReaderPathLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 15);
    add(pdfReaderPath, gridBagConstraints);

    logLevelLabel.setText(ResourceMgr.getString("LblLogLevel"));
    logLevelLabel.setToolTipText(ResourceMgr.getDescription("LblLogLevel"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
    add(logLevelLabel, gridBagConstraints);

    logLevel.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ERROR", "WARNING", "INFO", "DEBUG" }));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
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

    jPanel2.setLayout(new java.awt.GridBagLayout());

    useEncryption.setSelected(Settings.getInstance().getUseEncryption());
    useEncryption.setText(ResourceMgr.getString("LblUseEncryption"));
    useEncryption.setToolTipText(ResourceMgr.getDescription("LblUseEncryption"));
    useEncryption.setBorder(null);
    useEncryption.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    useEncryption.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    useEncryption.setIconTextGap(5);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    jPanel2.add(useEncryption, gridBagConstraints);

    consolidateLog.setSelected(Settings.getInstance().getConsolidateLogMsg());
    consolidateLog.setText(ResourceMgr.getString("LblConsolidateLog"));
    consolidateLog.setToolTipText(ResourceMgr.getDescription("LblConsolidateLog"));
    consolidateLog.setBorder(null);
    consolidateLog.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    consolidateLog.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    consolidateLog.setIconTextGap(5);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(6, 0, 1, 0);
    jPanel2.add(consolidateLog, gridBagConstraints);

    showTabIndex.setSelected(Settings.getInstance().getShowTabIndex());
    showTabIndex.setText(ResourceMgr.getString("LblShowTabIndex"));
    showTabIndex.setToolTipText(ResourceMgr.getDescription("LblShowTabIndex"));
    showTabIndex.setBorder(null);
    showTabIndex.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    showTabIndex.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    showTabIndex.setIconTextGap(5);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(6, 25, 1, 0);
    jPanel2.add(showTabIndex, gridBagConstraints);

    enableAnimatedIcon.setSelected(Settings.getInstance().getUseAnimatedIcon());
    enableAnimatedIcon.setText(ResourceMgr.getString("LblEnableAnimatedIcon"));
    enableAnimatedIcon.setToolTipText(ResourceMgr.getDescription("LblEnableAnimatedIcon"));
    enableAnimatedIcon.setBorder(null);
    enableAnimatedIcon.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    enableAnimatedIcon.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    enableAnimatedIcon.setIconTextGap(5);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 25, 1, 0);
    jPanel2.add(enableAnimatedIcon, gridBagConstraints);

    exitOnConnectCancel.setSelected(Settings.getInstance().getExitOnFirstConnectCancel());
    exitOnConnectCancel.setText(ResourceMgr.getString("LblExitOnConnectCancel"));
    exitOnConnectCancel.setToolTipText(ResourceMgr.getDescription("LblExitOnConnectCancel"));
    exitOnConnectCancel.setBorder(null);
    exitOnConnectCancel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    exitOnConnectCancel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    exitOnConnectCancel.setIconTextGap(5);
    exitOnConnectCancel.addActionListener(this);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(6, 25, 1, 0);
    jPanel2.add(exitOnConnectCancel, gridBagConstraints);

    autoConnect.setSelected(Settings.getInstance().getShowConnectDialogOnStartup());
    autoConnect.setText(ResourceMgr.getString("LblShowConnect"));
    autoConnect.setToolTipText(ResourceMgr.getDescription("LblShowConnect"));
    autoConnect.setBorder(null);
    autoConnect.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    autoConnect.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    autoConnect.setIconTextGap(5);
    autoConnect.addActionListener(this);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(6, 0, 1, 0);
    jPanel2.add(autoConnect, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 9;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(13, 12, 0, 10);
    add(jPanel2, gridBagConstraints);
  }

  // Code for dispatching events from components to event handlers.

  public void actionPerformed(java.awt.event.ActionEvent evt) {
    if (evt.getSource() == exitOnConnectCancel) {
      GeneralOptionsPanel.this.exitOnConnectCancelActionPerformed(evt);
    }
    else if (evt.getSource() == autoConnect) {
      GeneralOptionsPanel.this.autoConnectActionPerformed(evt);
    }
  }// </editor-fold>//GEN-END:initComponents

private void exitOnConnectCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitOnConnectCancelActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_exitOnConnectCancelActionPerformed

private void autoConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoConnectActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_autoConnectActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JCheckBox autoConnect;
  private javax.swing.JComboBox checkInterval;
  private javax.swing.JLabel checkUpdatesLabel;
  private javax.swing.JCheckBox consolidateLog;
  private javax.swing.JCheckBox enableAnimatedIcon;
  private javax.swing.JCheckBox exitOnConnectCancel;
  private javax.swing.JPanel jPanel2;
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
  private javax.swing.JCheckBox showTabIndex;
  private workbench.gui.components.WbFontPicker standardFont;
  private javax.swing.JLabel standardFontLabel;
  private javax.swing.JTextField textDelimiterField;
  private javax.swing.JLabel textDelimiterLabel;
  private javax.swing.JCheckBox useEncryption;
  // End of variables declaration//GEN-END:variables

}
