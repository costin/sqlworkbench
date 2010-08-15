/*
 * TextOptionsPanel.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2010, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.dialogs.dataimport;

import javax.swing.JPanel;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.util.StringUtil;

/**
 *
 * @author  Thomas Kellerer
 */
public class TextOptionsPanel
	extends JPanel
	implements TextImportOptions
{

	/** Creates new form TextoptionsPanel */
	public TextOptionsPanel()
	{
		super();
		initComponents();
	}

	public void saveSettings()
	{
		saveSettings("text");
	}

	public void saveSettings(String key)
	{
		Settings s = Settings.getInstance();
		s.setProperty("workbench.import."  + key + ".containsheader", this.getContainsHeader());
		s.setProperty("workbench.import." + key + ".decode", this.getDecode());
		s.setDelimiter("workbench.import." + key + ".fielddelimiter", getTextDelimiter());
		s.setProperty("workbench.import." + key + ".quotechar", this.getTextQuoteChar());
		s.setProperty("workbench.import." + key + ".decimalchar", this.getDecimalChar());
	}

	public void restoreSettings()
	{
		restoreSettings("text");
	}

	public void restoreSettings(String key)
	{
		Settings s = Settings.getInstance();
		this.setContainsHeader(s.getBoolProperty("workbench.import." + key + ".containsheader", true));
		this.setDecode(s.getBoolProperty("workbench.import." + key + ".decode", false));
		this.setTextQuoteChar(s.getProperty("workbench.import." + key + ".quotechar", s.getQuoteChar()));
		this.setTextDelimiter(s.getDelimiter("workbench.import." + key + ".fielddelimiter", "\\t", true));
		this.setDecimalChar(s.getProperty("workbench.import." + key + ".decimalchar", "."));
	}

	public boolean getDecode()
	{
		return this.decode.isSelected();
	}

	public void setDecode(boolean flag)
	{
		this.decode.setSelected(flag);
	}

	public boolean getContainsHeader()
	{
		return this.headerIncluded.isSelected();
	}

	public String getTextDelimiter()
	{
		return this.delimiter.getText();
	}

	public String getTextQuoteChar()
	{
		return this.quoteChar.getText();
	}

	public void disableHeaderSelection()
	{
		this.headerIncluded.setEnabled(false);
	}
	public void setContainsHeader(boolean flag)
	{
		this.headerIncluded.setSelected(flag);
	}

	public void setTextDelimiter(String delim)
	{
		this.delimiter.setText(delim);
	}

	public void setTextQuoteChar(String quote)
	{
		this.quoteChar.setText(quote);
	}

	public String getDecimalChar()
	{
		String s = this.decimalCharTextField.getText();
		if (StringUtil.isBlank(s)) return ".";
		return s.trim();
	}
	public void setDecimalChar(String s)
	{
		this.decimalCharTextField.setText(s);
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

    delimiterLabel = new javax.swing.JLabel();
    delimiter = new javax.swing.JTextField();
    headerIncluded = new javax.swing.JCheckBox();
    quoteCharLabel = new javax.swing.JLabel();
    quoteChar = new javax.swing.JTextField();
    jPanel1 = new javax.swing.JPanel();
    decode = new javax.swing.JCheckBox();
    decimalCharLabel = new javax.swing.JLabel();
    decimalCharTextField = new javax.swing.JTextField();

    setLayout(new java.awt.GridBagLayout());

    delimiterLabel.setText(ResourceMgr.getString("LblFieldDelimiter"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
    add(delimiterLabel, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
    add(delimiter, gridBagConstraints);

    headerIncluded.setText(ResourceMgr.getString("LblImportIncludeHeaders"));
    headerIncluded.setToolTipText(ResourceMgr.getDescription("LblImportIncludeHeaders"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    add(headerIncluded, gridBagConstraints);

    quoteCharLabel.setText(ResourceMgr.getString("LblQuoteChar"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
    add(quoteCharLabel, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
    add(quoteChar, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 11;
    gridBagConstraints.weighty = 1.0;
    add(jPanel1, gridBagConstraints);

    decode.setText(ResourceMgr.getString("LblImportDecode"));
    decode.setToolTipText(ResourceMgr.getDescription("LblImportDecode"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    add(decode, gridBagConstraints);

    decimalCharLabel.setText(ResourceMgr.getString("LblImportDecimalChar"));
    decimalCharLabel.setToolTipText(ResourceMgr.getDescription("LblImportDecimalChar"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
    add(decimalCharLabel, gridBagConstraints);

    decimalCharTextField.setToolTipText(ResourceMgr.getDescription("LblImportDecimalChar"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
    add(decimalCharTextField, gridBagConstraints);

  }
  // </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel decimalCharLabel;
  private javax.swing.JTextField decimalCharTextField;
  private javax.swing.JCheckBox decode;
  private javax.swing.JTextField delimiter;
  private javax.swing.JLabel delimiterLabel;
  private javax.swing.JCheckBox headerIncluded;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JTextField quoteChar;
  private javax.swing.JLabel quoteCharLabel;
  // End of variables declaration//GEN-END:variables

}
