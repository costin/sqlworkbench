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
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.util.StringUtil;

/**
 *
 * @author  support@sql-workbench.net
 */
public class GeneralOptionsPanel
	extends JPanel
	implements java.awt.event.MouseListener, java.awt.event.ActionListener, workbench.interfaces.Restoreable
{

	/** Creates new form GeneralOptionsPanel */
	public GeneralOptionsPanel()
	{
		initComponents();
		pdfReaderPath.setAllowMultiple(false);
		restoreSettings();
	}

	public void restoreSettings()
	{
		this.enableDbmsOutput.addActionListener(this);
		this.defaultBufferSize.setEnabled(this.enableDbmsOutput.isSelected());
		setBufferSizeLabelColor();
		msgLogFont.setSelectedFont(Settings.getInstance().getMsgLogFont());
		standardFont.setSelectedFont(Settings.getInstance().getStandardFont());
		pdfReaderPath.setFilename(Settings.getInstance().getPDFReaderPath());
		logLevel.setSelectedItem(LogMgr.getLevel());
		int days = Settings.getInstance().getUpdateCheckInterval();
		if (days == 7)
		{
			checkInterval.setSelectedIndex(1);
		}
		else if (days == 14)
		{
			checkInterval.setSelectedIndex(2);
		}
		else if (days == 30)
		{
			checkInterval.setSelectedIndex(3);
		}
		else
		{
			checkInterval.setSelectedIndex(0);
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
		set.setDefaultDateFormat(this.dateFormatTextField.getText());
		set.setDefaultTimeFormat(this.timeFormat.getText());
		set.setDefaultTimestampFormat(this.timestampFormatTextField.getText());
		set.setMaxFractionDigits(((NumberField)this.maxDigitsField).getValue());
		set.setQuoteChar(this.quoteCharField.getText().trim());
		set.setEnableDbmsOutput(this.enableDbmsOutput.isSelected());
		set.setConsolidateLogMsg(this.consolidateLog.isSelected());
    set.setDbmsOutputDefaultBuffer(StringUtil.getIntValue(this.defaultBufferSize.getText(), -1));
		set.setDecimalSymbol(this.decimalField.getText());
		set.setDefaultTextDelimiter(this.textDelimiterField.getText());
		set.setPDFReaderPath(pdfReaderPath.getFilename());
		set.setUpdateCheckInterval((String)checkInterval.getSelectedItem());
		String level = (String)logLevel.getSelectedItem();
		LogMgr.setLevel(level);
		set.setProperty("workbench.log.level", level);
	}

	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		if (e.getSource() == this.enableDbmsOutput)
		{
			this.defaultBufferSize.setEnabled(this.enableDbmsOutput.isSelected());
		}
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

    useEncryptionLabel = new WbCheckBoxLabel();
    useEncryption = new javax.swing.JCheckBox();
    dateFormatLabel = new javax.swing.JLabel();
    dateFormatTextField = new javax.swing.JTextField();
    decimalLabel = new javax.swing.JLabel();
    decimalField = new javax.swing.JTextField();
    maxDigitsLabel = new javax.swing.JLabel();
    maxDigitsField = new NumberField();
    textDelimiterLabel = new javax.swing.JLabel();
    textDelimiterField = new javax.swing.JTextField();
    quoteCharLabel = new javax.swing.JLabel();
    quoteCharField = new javax.swing.JTextField();
    jPanel1 = new javax.swing.JPanel();
    enableDbmsOutputLabel = new WbCheckBoxLabel();
    enableDbmsOutput = new javax.swing.JCheckBox();
    enableAnimatedIconLabel = new WbCheckBoxLabel();
    enableAnimatedIcon = new javax.swing.JCheckBox();
    consolidateLogLabel = new WbCheckBoxLabel();
    consolidateLog = new javax.swing.JCheckBox();
    defaultBufferSize = new javax.swing.JTextField();
    bufferSizeLabel = new javax.swing.JLabel();
    timestampFormatLabel = new javax.swing.JLabel();
    timestampFormatTextField = new javax.swing.JTextField();
    msgFontLabel = new javax.swing.JLabel();
    standardFontLabel = new javax.swing.JLabel();
    msgLogFont = new workbench.gui.components.WbFontPicker();
    standardFont = new workbench.gui.components.WbFontPicker();
    timeFormatLabel = new javax.swing.JLabel();
    timeFormat = new javax.swing.JTextField();
    pdfReaderPathLabel = new javax.swing.JLabel();
    pdfReaderPath = new workbench.gui.components.WbFilePicker();
    logLevelLabel = new javax.swing.JLabel();
    logLevel = new javax.swing.JComboBox();
    checkUpdatesLabel = new WbCheckBoxLabel();
    checkInterval = new javax.swing.JComboBox();

    setLayout(new java.awt.GridBagLayout());

    useEncryptionLabel.setLabelFor(useEncryption);
    useEncryptionLabel.setText(ResourceMgr.getString("LblUseEncryption"));
    useEncryptionLabel.setToolTipText(ResourceMgr.getDescription("LblUseEncryption"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
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
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 11);
    add(useEncryption, gridBagConstraints);

    dateFormatLabel.setFont(null);
    dateFormatLabel.setText(ResourceMgr.getString("LblDateFormat"));
    dateFormatLabel.setToolTipText(ResourceMgr.getDescription("LblDateFormat"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
    add(dateFormatLabel, gridBagConstraints);

    dateFormatTextField.setText(Settings.getInstance().getDefaultDateFormat());
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 15);
    add(dateFormatTextField, gridBagConstraints);

    decimalLabel.setText(ResourceMgr.getString("LblDecimalSymbol"));
    decimalLabel.setToolTipText(ResourceMgr.getDescription("LblDecimalSymbol"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 10;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 12, 0, 0);
    add(decimalLabel, gridBagConstraints);

    decimalField.setText(Settings.getInstance().getDecimalSymbol());
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 10;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 10, 0, 15);
    add(decimalField, gridBagConstraints);

    maxDigitsLabel.setText(ResourceMgr.getString("LblMaxDigits"));
    maxDigitsLabel.setToolTipText(ResourceMgr.getDescription("LblMaxDigits"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 12;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 12, 0, 0);
    add(maxDigitsLabel, gridBagConstraints);

    maxDigitsField.setText(Integer.toString(Settings.getInstance().getMaxFractionDigits()));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 12;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 10, 0, 15);
    add(maxDigitsField, gridBagConstraints);

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
    gridBagConstraints.gridwidth = 3;
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
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 10, 0, 15);
    add(quoteCharField, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 18;
    gridBagConstraints.weighty = 1.0;
    add(jPanel1, gridBagConstraints);

    enableDbmsOutputLabel.setLabelFor(enableDbmsOutput);
    enableDbmsOutputLabel.setText(ResourceMgr.getString("LblEnableDbmsOutput"));
    enableDbmsOutputLabel.setToolTipText(ResourceMgr.getDescription("LblEnableDbmsOutput"));
    enableDbmsOutputLabel.addMouseListener(this);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 12, 0, 0);
    add(enableDbmsOutputLabel, gridBagConstraints);

    enableDbmsOutput.setFont(null);
    enableDbmsOutput.setSelected(Settings.getInstance().getEnableDbmsOutput());
    enableDbmsOutput.setText("");
    enableDbmsOutput.setBorder(null);
    enableDbmsOutput.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    enableDbmsOutput.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    enableDbmsOutput.setIconTextGap(5);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 10, 0, 3);
    add(enableDbmsOutput, gridBagConstraints);

    enableAnimatedIconLabel.setLabelFor(enableAnimatedIcon);
    enableAnimatedIconLabel.setText(ResourceMgr.getString("LblEnableAnimatedIcon"));
    enableAnimatedIconLabel.setToolTipText(ResourceMgr.getDescription("LblEnableAnimatedIcon"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
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
    gridBagConstraints.gridy = 4;
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(4, 10, 1, 25);
    add(enableAnimatedIcon, gridBagConstraints);

    consolidateLogLabel.setLabelFor(consolidateLog);
    consolidateLogLabel.setText(ResourceMgr.getString("LblConsolidateLog"));
    consolidateLogLabel.setToolTipText(ResourceMgr.getDescription("LblConsolidateLog"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(6, 12, 1, 0);
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
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(6, 10, 1, 11);
    add(consolidateLog, gridBagConstraints);

    defaultBufferSize.setColumns(8);
    defaultBufferSize.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
    defaultBufferSize.setText(Integer.toString(Settings.getInstance().getDbmsOutputDefaultBuffer()));
    defaultBufferSize.setMaximumSize(new java.awt.Dimension(2147483647, 21));
    defaultBufferSize.setMinimumSize(new java.awt.Dimension(100, 21));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 3, 0, 15);
    add(defaultBufferSize, gridBagConstraints);

    bufferSizeLabel.setText(ResourceMgr.getString("LblDefaultBufferSize"));
    bufferSizeLabel.setToolTipText(ResourceMgr.getDescription("LblDefaultBufferSize"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 3, 0, 0);
    add(bufferSizeLabel, gridBagConstraints);

    timestampFormatLabel.setFont(null);
    timestampFormatLabel.setText(ResourceMgr.getString("LblTimestampFormat"));
    timestampFormatLabel.setToolTipText(ResourceMgr.getDescription("LblTimestampFormat"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 12, 0, 0);
    add(timestampFormatLabel, gridBagConstraints);

    timestampFormatTextField.setText(Settings.getInstance().getDefaultTimestampFormat());
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 10, 0, 15);
    add(timestampFormatTextField, gridBagConstraints);

    msgFontLabel.setText(ResourceMgr.getString("LblMsgLogFont"));
    msgFontLabel.setToolTipText(ResourceMgr.getDescription("LblMsgLogFont"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(7, 12, 0, 0);
    add(msgFontLabel, gridBagConstraints);

    standardFontLabel.setText(ResourceMgr.getString("LblStandardFont"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(7, 12, 0, 0);
    add(standardFontLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 15);
    add(msgLogFont, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 10, 0, 15);
    add(standardFont, gridBagConstraints);

    timeFormatLabel.setFont(null);
    timeFormatLabel.setText(ResourceMgr.getString("LblTimeFormat"));
    timeFormatLabel.setToolTipText(ResourceMgr.getDescription("LblTimeFormat"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 9;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 12, 0, 0);
    add(timeFormatLabel, gridBagConstraints);

    timeFormat.setText(Settings.getInstance().getDefaultTimeFormat());
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 9;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 10, 0, 15);
    add(timeFormat, gridBagConstraints);

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
    gridBagConstraints.gridwidth = 3;
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
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 10, 0, 0);
    add(logLevel, gridBagConstraints);

    checkUpdatesLabel.setText(ResourceMgr.getString("LblCheckForUpdate"));
    checkUpdatesLabel.setToolTipText(ResourceMgr.getDescription("LblCheckForUpdate"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(8, 12, 0, 0);
    add(checkUpdatesLabel, gridBagConstraints);

    checkInterval.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "never", "7 days", "14 days", "30 days" }));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(7, 10, 0, 0);
    add(checkInterval, gridBagConstraints);
  }

  // Code for dispatching events from components to event handlers.

  public void mouseClicked(java.awt.event.MouseEvent evt)
  {
    if (evt.getSource() == enableDbmsOutputLabel)
    {
      GeneralOptionsPanel.this.enableDbmsOutputLabelMouseClicked(evt);
    }
  }

  public void mouseEntered(java.awt.event.MouseEvent evt)
  {
  }

  public void mouseExited(java.awt.event.MouseEvent evt)
  {
  }

  public void mousePressed(java.awt.event.MouseEvent evt)
  {
  }

  public void mouseReleased(java.awt.event.MouseEvent evt)
  {
  }// </editor-fold>//GEN-END:initComponents

	private void enableDbmsOutputLabelMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_enableDbmsOutputLabelMouseClicked
	{//GEN-HEADEREND:event_enableDbmsOutputLabelMouseClicked
		this.defaultBufferSize.setEnabled(this.enableDbmsOutput.isSelected());
		setBufferSizeLabelColor();
	}//GEN-LAST:event_enableDbmsOutputLabelMouseClicked


	private void setBufferSizeLabelColor()
	{
		if (defaultBufferSize.isEnabled())
		{
			this.bufferSizeLabel.setForeground(this.defaultBufferSize.getForeground());
		}
		else
		{
			this.bufferSizeLabel.setForeground(this.defaultBufferSize.getDisabledTextColor());
		}		
	}
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel bufferSizeLabel;
  private javax.swing.JComboBox checkInterval;
  private javax.swing.JLabel checkUpdatesLabel;
  private javax.swing.JCheckBox consolidateLog;
  private javax.swing.JLabel consolidateLogLabel;
  private javax.swing.JLabel dateFormatLabel;
  private javax.swing.JTextField dateFormatTextField;
  private javax.swing.JTextField decimalField;
  private javax.swing.JLabel decimalLabel;
  private javax.swing.JTextField defaultBufferSize;
  private javax.swing.JCheckBox enableAnimatedIcon;
  private javax.swing.JLabel enableAnimatedIconLabel;
  private javax.swing.JCheckBox enableDbmsOutput;
  private javax.swing.JLabel enableDbmsOutputLabel;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JComboBox logLevel;
  private javax.swing.JLabel logLevelLabel;
  private javax.swing.JTextField maxDigitsField;
  private javax.swing.JLabel maxDigitsLabel;
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
  private javax.swing.JTextField timeFormat;
  private javax.swing.JLabel timeFormatLabel;
  private javax.swing.JLabel timestampFormatLabel;
  private javax.swing.JTextField timestampFormatTextField;
  private javax.swing.JCheckBox useEncryption;
  private javax.swing.JLabel useEncryptionLabel;
  // End of variables declaration//GEN-END:variables

}
