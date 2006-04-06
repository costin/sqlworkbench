/*
 * HtmlOptionsPanel.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2006, Thomas Kellerer
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
public class HtmlOptionsPanel 
	extends javax.swing.JPanel
	implements HtmlOptions
{
	
	/** Creates new form HtmlOptionsPanel */
	public HtmlOptionsPanel()
	{
		initComponents();
	}

	public void saveSettings()
	{
		Settings s = Settings.getInstance();
		s.setProperty("workbench.export.html.createfullpage", this.getCreateFullPage());
		s.setProperty("workbench.export.html.escape", this.getEscapeHtml());
		s.setProperty("workbench.export.html.pagetitle", this.getPageTitle());
	}
	
	public void restoreSettings()
	{
		Settings s = Settings.getInstance();
		this.setCreateFullPage(s.getBoolProperty("workbench.export.html.createfullpage"));
		this.setEscapeHtml(s.getBoolProperty("workbench.export.html.escape"));
		this.setPageTitle(s.getProperty("workbench.export.html.pagetitle", ""));
	}
	
	public boolean getCreateFullPage()
	{
		return this.fullPage.isSelected();
	}

	public boolean getEscapeHtml()
	{
		return escapeHtml.isSelected();
	}

	public String getPageTitle()
	{
		return pageTitle.getText();
	}

	public void setCreateFullPage(boolean flag)
	{
		fullPage.setSelected(flag);
	}

	public void setEscapeHtml(boolean flag)
	{
		escapeHtml.setSelected(flag);
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
  private void initComponents()//GEN-BEGIN:initComponents
  {
    java.awt.GridBagConstraints gridBagConstraints;

    pageTitleLabel = new javax.swing.JLabel();
    pageTitle = new javax.swing.JTextField();
    fullPage = new javax.swing.JCheckBox();
    escapeHtml = new javax.swing.JCheckBox();

    setLayout(new java.awt.GridBagLayout());

    pageTitleLabel.setText(ResourceMgr.getString("LblExportHtmlPageTitle"));
    pageTitleLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
    add(pageTitleLabel, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
    add(pageTitle, gridBagConstraints);

    fullPage.setText(ResourceMgr.getString("LblExportFullHtml"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    add(fullPage, gridBagConstraints);

    escapeHtml.setText(ResourceMgr.getString("LblExportEscapeHtml"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weighty = 1.0;
    add(escapeHtml, gridBagConstraints);

  }//GEN-END:initComponents
	
	
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JCheckBox escapeHtml;
  private javax.swing.JCheckBox fullPage;
  private javax.swing.JTextField pageTitle;
  private javax.swing.JLabel pageTitleLabel;
  // End of variables declaration//GEN-END:variables
	
}
