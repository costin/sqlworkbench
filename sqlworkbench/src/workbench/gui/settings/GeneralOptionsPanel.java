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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collection;
import java.util.Locale;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import workbench.gui.components.WbCheckBoxLabel;
import workbench.gui.components.WbFilePicker;
import workbench.gui.components.WbFontPicker;
import workbench.interfaces.Restoreable;
import workbench.log.LogMgr;
import workbench.resource.GuiSettings;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.util.WbLocale;

/**
 *
 * @author  support@sql-workbench.net
 */
public class GeneralOptionsPanel
	extends JPanel
	implements Restoreable
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
		GuiSettings.setShowTabIndex(showTabIndex.isSelected());
		set.setUseEncryption(this.useEncryption.isSelected());
		set.setMsgLogFont(msgLogFont.getSelectedFont());
		GuiSettings.setUseAnimatedIcon(this.enableAnimatedIcon.isSelected());
		set.setQuoteChar(this.quoteCharField.getText().trim());
		set.setConsolidateLogMsg(this.consolidateLog.isSelected());
		set.setDefaultTextDelimiter(this.textDelimiterField.getText());
		set.setPDFReaderPath(pdfReaderPath.getFilename());
		set.setExitOnFirstConnectCancel(exitOnConnectCancel.isSelected());
		set.setShowConnectDialogOnStartup(autoConnect.isSelected());
		set.setAutoSaveWorkspace(autoSaveWorkspace.isSelected());

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
		set.setLanguage(getSelectedLanguage());
	}

	private Locale getSelectedLanguage()
	{
		WbLocale wl = (WbLocale)languageDropDown.getSelectedItem();
		return wl.getLocale();

	}
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
		GridBagConstraints gridBagConstraints;

    textDelimiterLabel = new JLabel();
    textDelimiterField = new JTextField();
    quoteCharLabel = new JLabel();
    quoteCharField = new JTextField();
    msgFontLabel = new JLabel();
    standardFontLabel = new JLabel();
    msgLogFont = new WbFontPicker();
    standardFont = new WbFontPicker();
    pdfReaderPathLabel = new JLabel();
    pdfReaderPath = new WbFilePicker();
    logLevelLabel = new JLabel();
    logLevel = new JComboBox();
    checkUpdatesLabel = new WbCheckBoxLabel();
    checkInterval = new JComboBox();
    langLabel = new JLabel();
    languageDropDown = new JComboBox();
    jPanel2 = new JPanel();
    useEncryption = new JCheckBox();
    consolidateLog = new JCheckBox();
    showTabIndex = new JCheckBox();
    enableAnimatedIcon = new JCheckBox();
    exitOnConnectCancel = new JCheckBox();
    autoConnect = new JCheckBox();
    autoSaveWorkspace = new JCheckBox();

    setLayout(new GridBagLayout());

    textDelimiterLabel.setText(ResourceMgr.getString("LblFieldDelimiter"));
    textDelimiterLabel.setToolTipText(ResourceMgr.getDescription("LblFieldDelimiter"));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(5, 12, 0, 0);
    add(textDelimiterLabel, gridBagConstraints);

    textDelimiterField.setHorizontalAlignment(JTextField.LEFT);
    textDelimiterField.setText(Settings.getInstance().getDefaultTextDelimiter(true));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(5, 10, 0, 15);
    add(textDelimiterField, gridBagConstraints);

    quoteCharLabel.setText(ResourceMgr.getString("LblQuoteChar"));
    quoteCharLabel.setToolTipText(ResourceMgr.getDescription("LblQuoteChar"));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(5, 12, 0, 0);
    add(quoteCharLabel, gridBagConstraints);

    quoteCharField.setHorizontalAlignment(JTextField.LEFT);
    quoteCharField.setText(Settings.getInstance().getQuoteChar());
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(5, 10, 0, 15);
    add(quoteCharField, gridBagConstraints);

    msgFontLabel.setText(ResourceMgr.getString("LblMsgLogFont"));
    msgFontLabel.setToolTipText(ResourceMgr.getDescription("LblMsgLogFont"));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(4, 12, 0, 0);
    add(msgFontLabel, gridBagConstraints);

    standardFontLabel.setText(ResourceMgr.getString("LblStandardFont"));
    standardFontLabel.setToolTipText(ResourceMgr.getDescription("LblStandardFont"));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(8, 12, 0, 0);
    add(standardFontLabel, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(5, 10, 0, 15);
    add(msgLogFont, gridBagConstraints);

    standardFont.setFont(standardFont.getFont());
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(4, 10, 0, 15);
    add(standardFont, gridBagConstraints);

    pdfReaderPathLabel.setText(ResourceMgr.getString("LblReaderPath"));
    pdfReaderPathLabel.setToolTipText(ResourceMgr.getDescription("LblReaderPath"));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(6, 12, 0, 0);
    add(pdfReaderPathLabel, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new Insets(5, 10, 0, 15);
    add(pdfReaderPath, gridBagConstraints);

    logLevelLabel.setText(ResourceMgr.getString("LblLogLevel"));
    logLevelLabel.setToolTipText(ResourceMgr.getDescription("LblLogLevel"));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(6, 12, 0, 0);
    add(logLevelLabel, gridBagConstraints);

    logLevel.setModel(new DefaultComboBoxModel(new String[] { "ERROR", "WARNING", "INFO", "DEBUG" }));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(5, 10, 0, 0);
    add(logLevel, gridBagConstraints);

    checkUpdatesLabel.setText(ResourceMgr.getString("LblCheckForUpdate"));
    checkUpdatesLabel.setToolTipText(ResourceMgr.getDescription("LblCheckForUpdate"));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(9, 12, 0, 0);
    add(checkUpdatesLabel, gridBagConstraints);

    checkInterval.setModel(new DefaultComboBoxModel(new String[] { "never", "daily", "7 days", "14 days", "30 days" }));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(7, 10, 0, 0);
    add(checkInterval, gridBagConstraints);

    langLabel.setText(ResourceMgr.getString("LblLanguage"));
    langLabel.setToolTipText(ResourceMgr.getDescription("LblLanguage"));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(3, 12, 0, 0);
    add(langLabel, gridBagConstraints);

    languageDropDown.setModel(new DefaultComboBoxModel(new String[] { "English", "German" }));
    languageDropDown.setToolTipText(ResourceMgr.getDescription("LblLanguage"));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(5, 10, 0, 15);
    add(languageDropDown, gridBagConstraints);

    jPanel2.setLayout(new GridBagLayout());

    useEncryption.setSelected(Settings.getInstance().getUseEncryption());
    useEncryption.setText(ResourceMgr.getString("LblUseEncryption"));
    useEncryption.setToolTipText(ResourceMgr.getDescription("LblUseEncryption"));
    useEncryption.setBorder(null);
    useEncryption.setHorizontalAlignment(SwingConstants.LEFT);
    useEncryption.setHorizontalTextPosition(SwingConstants.RIGHT);
    useEncryption.setIconTextGap(5);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    jPanel2.add(useEncryption, gridBagConstraints);

    consolidateLog.setSelected(Settings.getInstance().getConsolidateLogMsg());
    consolidateLog.setText(ResourceMgr.getString("LblConsolidateLog"));
    consolidateLog.setToolTipText(ResourceMgr.getDescription("LblConsolidateLog"));
    consolidateLog.setBorder(null);
    consolidateLog.setHorizontalAlignment(SwingConstants.LEFT);
    consolidateLog.setHorizontalTextPosition(SwingConstants.RIGHT);
    consolidateLog.setIconTextGap(5);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(6, 0, 1, 0);
    jPanel2.add(consolidateLog, gridBagConstraints);

    showTabIndex.setSelected(GuiSettings.getShowTabIndex());
    showTabIndex.setText(ResourceMgr.getString("LblShowTabIndex"));
    showTabIndex.setToolTipText(ResourceMgr.getDescription("LblShowTabIndex"));
    showTabIndex.setBorder(null);
    showTabIndex.setHorizontalAlignment(SwingConstants.LEFT);
    showTabIndex.setHorizontalTextPosition(SwingConstants.RIGHT);
    showTabIndex.setIconTextGap(5);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new Insets(6, 25, 1, 0);
    jPanel2.add(showTabIndex, gridBagConstraints);

    enableAnimatedIcon.setSelected(GuiSettings.getUseAnimatedIcon());
    enableAnimatedIcon.setText(ResourceMgr.getString("LblEnableAnimatedIcon"));
    enableAnimatedIcon.setToolTipText(ResourceMgr.getDescription("LblEnableAnimatedIcon"));
    enableAnimatedIcon.setBorder(null);
    enableAnimatedIcon.setHorizontalAlignment(SwingConstants.LEFT);
    enableAnimatedIcon.setHorizontalTextPosition(SwingConstants.RIGHT);
    enableAnimatedIcon.setIconTextGap(5);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new Insets(0, 25, 1, 0);
    jPanel2.add(enableAnimatedIcon, gridBagConstraints);

    exitOnConnectCancel.setSelected(Settings.getInstance().getExitOnFirstConnectCancel());
    exitOnConnectCancel.setText(ResourceMgr.getString("LblExitOnConnectCancel"));
    exitOnConnectCancel.setToolTipText(ResourceMgr.getDescription("LblExitOnConnectCancel"));
    exitOnConnectCancel.setBorder(null);
    exitOnConnectCancel.setHorizontalAlignment(SwingConstants.LEFT);
    exitOnConnectCancel.setHorizontalTextPosition(SwingConstants.RIGHT);
    exitOnConnectCancel.setIconTextGap(5);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(6, 25, 1, 0);
    jPanel2.add(exitOnConnectCancel, gridBagConstraints);

    autoConnect.setSelected(Settings.getInstance().getShowConnectDialogOnStartup());
    autoConnect.setText(ResourceMgr.getString("LblShowConnect"));
    autoConnect.setToolTipText(ResourceMgr.getDescription("LblShowConnect"));
    autoConnect.setBorder(null);
    autoConnect.setHorizontalAlignment(SwingConstants.LEFT);
    autoConnect.setHorizontalTextPosition(SwingConstants.RIGHT);
    autoConnect.setIconTextGap(5);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(6, 0, 1, 0);
    jPanel2.add(autoConnect, gridBagConstraints);

    autoSaveWorkspace.setSelected(Settings.getInstance().getAutoSaveWorkspace());
    autoSaveWorkspace.setText(ResourceMgr.getString("LblAutoSaveWksp"));
    autoSaveWorkspace.setToolTipText(ResourceMgr.getDescription("LblAutoSaveWksp"));
    autoSaveWorkspace.setBorder(null);
    autoSaveWorkspace.setHorizontalAlignment(SwingConstants.LEFT);
    autoSaveWorkspace.setHorizontalTextPosition(SwingConstants.RIGHT);
    autoSaveWorkspace.setIconTextGap(5);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(6, 0, 1, 0);
    jPanel2.add(autoSaveWorkspace, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 9;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new Insets(13, 12, 0, 10);
    add(jPanel2, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private JCheckBox autoConnect;
  private JCheckBox autoSaveWorkspace;
  private JComboBox checkInterval;
  private JLabel checkUpdatesLabel;
  private JCheckBox consolidateLog;
  private JCheckBox enableAnimatedIcon;
  private JCheckBox exitOnConnectCancel;
  private JPanel jPanel2;
  private JLabel langLabel;
  private JComboBox languageDropDown;
  private JComboBox logLevel;
  private JLabel logLevelLabel;
  private JLabel msgFontLabel;
  private WbFontPicker msgLogFont;
  private WbFilePicker pdfReaderPath;
  private JLabel pdfReaderPathLabel;
  private JTextField quoteCharField;
  private JLabel quoteCharLabel;
  private JCheckBox showTabIndex;
  private WbFontPicker standardFont;
  private JLabel standardFontLabel;
  private JTextField textDelimiterField;
  private JLabel textDelimiterLabel;
  private JCheckBox useEncryption;
  // End of variables declaration//GEN-END:variables

}
