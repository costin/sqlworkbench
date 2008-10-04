/*
 * HtmlOptionsPanel.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2008, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.dialogs.export;

import workbench.resource.ResourceMgr;
import workbench.resource.Settings;

/**
 *
 * @author  support@sql-workbench.net
 */
public class SpreadSheetOptionsPanel
	extends javax.swing.JPanel
	implements SpreadSheetOptions
{
	private String exportType;

	public SpreadSheetOptionsPanel(String type)
	{
		super();
		exportType = type;
		initComponents();
	}

	public void saveSettings()
	{
		Settings s = Settings.getInstance();
		s.setProperty("workbench.export." + exportType + ".pagetitle", this.getPageTitle());
		s.setProperty("workbench.export." + exportType + ".header", getExportHeaders());
	}

	public void restoreSettings()
	{
		Settings s = Settings.getInstance();
		this.setPageTitle(s.getProperty("workbench.export." + exportType + ".pagetitle", ""));
		boolean headerDefault = s.getBoolProperty("workbench.export." + exportType + ".default.header", false);
		boolean header = s.getBoolProperty("workbench.export." + exportType + ".header", headerDefault);
		this.setExportHeaders(header);
	}

	public boolean getExportHeaders()
	{
		return exportHeaders.isSelected();
	}

	public void setExportHeaders(boolean flag)
	{
		exportHeaders.setSelected(flag);
	}

	public String getPageTitle()
	{
		return pageTitle.getText();
	}

	public void setPageTitle(String title)
	{
		pageTitle.setText(title);
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    pageTitleLabel = new javax.swing.JLabel();
    pageTitle = new javax.swing.JTextField();
    jPanel1 = new javax.swing.JPanel();
    exportHeaders = new javax.swing.JCheckBox();

    setLayout(new java.awt.GridBagLayout());

    pageTitleLabel.setText(ResourceMgr.getString("LblSheetName"));
    pageTitleLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(7, 6, 3, 6);
    add(pageTitleLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 6);
    add(pageTitle, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    add(jPanel1, gridBagConstraints);

    exportHeaders.setText(ResourceMgr.getString("LblExportIncludeHeaders"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(7, 6, 3, 6);
    add(exportHeaders, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JCheckBox exportHeaders;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JTextField pageTitle;
  private javax.swing.JLabel pageTitleLabel;
  // End of variables declaration//GEN-END:variables

}
