/*
 * TextoptionsPanel.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2004, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: info@sql-workbench.net
 *
 */

package workbench.gui.dialogs.export;

import javax.swing.JPanel;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.util.CharacterRange;
import workbench.util.StringUtil;

/**
 *
 * @author  info@sql-workbench.net
 */
public class TextOptionsPanel 
	extends JPanel
	implements TextOptions
{
	
	/** Creates new form TextoptionsPanel */
	public TextOptionsPanel()
	{
		initComponents();
		CharacterRange[] ranges = CharacterRange.getRanges();
		for (int i=0; i < ranges.length; i++)
		{
			escapeRange.addItem(ranges[i]);
		}
	}

	public void saveSettings()
	{
		Settings s = Settings.getInstance();
		s.setBoolProperty("workbench.export.text.cleanup", this.getCleanupCarriageReturns());
		s.setBoolProperty("workbench.export.text.includeheader", this.getExportHeaders());
		s.setBoolProperty("workbench.export.text.quotealways", this.getQuoteAlways());
		s.setProperty("workbench.export.text.escaperange", this.getEscapeRange().getId());
		s.setProperty("workbench.export.text.lineending", (String)this.lineEnding.getSelectedItem());
		s.setDefaultTextDelimiter(this.getTextDelimiter());
		s.setQuoteChar(this.getTextQuoteChar());
	}
	
	public void restoreSettings()
	{
		Settings s = Settings.getInstance();
		this.setCleanupCarriageReturns(s.getBoolProperty("workbench.export.text.cleanup"));
		this.setExportHeaders(s.getBoolProperty("workbench.export.text.includeheader"));
		this.setQuoteAlways(s.getBoolProperty("workbench.export.text.quotealways"));
		int id = s.getIntProperty("workbench.export.text.escaperange",0);
		CharacterRange range = CharacterRange.getRangeById(id);
		this.setEscapeRange(range);
		this.setLineEnding(s.getProperty("workbench.export.text.lineending", "LF"));
		this.setTextQuoteChar(s.getQuoteChar());
		this.setTextDelimiter(s.getDefaultTextDelimiter(true));
	}
	
	public boolean getCleanupCarriageReturns()
	{
		return this.cleanupCRLF.isSelected();
	}

	public boolean getExportHeaders()
	{
		return this.exportHeaders.isSelected();
	}

	public String getTextDelimiter()
	{
		return this.delimiter.getText();
	}

	public String getTextQuoteChar()
	{
		return this.quoteChar.getText();
	}

	public void setCleanupCarriageReturns(boolean flag)
	{
		this.cleanupCRLF.setSelected(flag);
	}

	public void setExportHeaders(boolean flag)
	{
		this.exportHeaders.setSelected(flag);
	}

	public void setTextDelimiter(String delim)
	{
		this.delimiter.setText(delim);
	}

	public void setTextQuoteChar(String quote)
	{
		this.quoteChar.setText(quote);
	}

	public boolean getQuoteAlways()
	{
		return this.quoteAlways.isSelected();
	}
	
	public void setQuoteAlways(boolean flag)
	{
		this.quoteAlways.setSelected(flag);
	}

	public void setEscapeRange(CharacterRange range)
	{
		this.escapeRange.setSelectedItem(range);
	}
	
	public CharacterRange getEscapeRange()
	{
		return (CharacterRange)this.escapeRange.getSelectedItem();
	}
	
	public String getLineEnding()
	{
		String s = (String)lineEnding.getSelectedItem();;
		if ("LF".equals(s))
			return "\n";
		else if ("CRLF".equals(s))
			return "\r\n";
		else 
			return StringUtil.LINE_TERMINATOR;
	}
	
	public void setLineEnding(String ending)
	{
		if (ending == null) return;
		if ("\n".equals(ending))
		{
			lineEnding.setSelectedItem("LF");
		}
		else if ("\r\n".equals(ending))
		{
			lineEnding.setSelectedItem("CRLF");
		}
		else 
		{
			lineEnding.setSelectedItem(ending.toUpperCase());
		}
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
  private void initComponents()//GEN-BEGIN:initComponents
  {
    java.awt.GridBagConstraints gridBagConstraints;

    delimiterLabel = new javax.swing.JLabel();
    delimiter = new javax.swing.JTextField();
    exportHeaders = new javax.swing.JCheckBox();
    quoteCharLabel = new javax.swing.JLabel();
    quoteChar = new javax.swing.JTextField();
    cleanupCRLF = new javax.swing.JCheckBox();
    jPanel1 = new javax.swing.JPanel();
    quoteAlways = new javax.swing.JCheckBox();
    escapeRange = new javax.swing.JComboBox();
    escapeLabel = new javax.swing.JLabel();
    lineEndingLabel = new javax.swing.JLabel();
    lineEnding = new javax.swing.JComboBox();

    setLayout(new java.awt.GridBagLayout());

    delimiterLabel.setText(java.util.ResourceBundle.getBundle("language/wbstrings").getString("LabelFieldDelimiter"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
    add(delimiterLabel, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
    add(delimiter, gridBagConstraints);

    exportHeaders.setText(java.util.ResourceBundle.getBundle("language/wbstrings").getString("LabelExportIncludeHeaders"));
    exportHeaders.setToolTipText("");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    add(exportHeaders, gridBagConstraints);

    quoteCharLabel.setText(java.util.ResourceBundle.getBundle("language/wbstrings").getString("LabelQuoteChar"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 9;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
    add(quoteCharLabel, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 10;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
    add(quoteChar, gridBagConstraints);

    cleanupCRLF.setText(java.util.ResourceBundle.getBundle("language/wbstrings").getString("LabelExportCleanCR"));
    cleanupCRLF.setToolTipText("");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    add(cleanupCRLF, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 11;
    gridBagConstraints.weighty = 1.0;
    add(jPanel1, gridBagConstraints);

    quoteAlways.setText(java.util.ResourceBundle.getBundle("language/wbstrings").getString("LabelExportQuoteAlways"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    add(quoteAlways, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
    add(escapeRange, gridBagConstraints);

    escapeLabel.setText(java.util.ResourceBundle.getBundle("language/wbstrings").getString("LabelExportEscapeType"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
    add(escapeLabel, gridBagConstraints);

    lineEndingLabel.setText(java.util.ResourceBundle.getBundle("language/wbstrings").getString("LabelExportLineEnding"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
    add(lineEndingLabel, gridBagConstraints);

    lineEnding.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "LF", "CRLF" }));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
    add(lineEnding, gridBagConstraints);

  }//GEN-END:initComponents
	
	
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JCheckBox cleanupCRLF;
  private javax.swing.JTextField delimiter;
  private javax.swing.JLabel delimiterLabel;
  private javax.swing.JLabel escapeLabel;
  private javax.swing.JComboBox escapeRange;
  private javax.swing.JCheckBox exportHeaders;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JComboBox lineEnding;
  private javax.swing.JLabel lineEndingLabel;
  private javax.swing.JCheckBox quoteAlways;
  private javax.swing.JTextField quoteChar;
  private javax.swing.JLabel quoteCharLabel;
  // End of variables declaration//GEN-END:variables
	
}
