/*
 * HtmlOptionsPanel.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2011, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.dialogs.export;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;

/**
 *
 * @author  Thomas Kellerer
 */
public class HtmlOptionsPanel 
	extends javax.swing.JPanel
	implements HtmlOptions
{
	public HtmlOptionsPanel()
	{
		super();
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
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
		GridBagConstraints gridBagConstraints;

    pageTitleLabel = new JLabel();
    pageTitle = new JTextField();
    fullPage = new JCheckBox();
    escapeHtml = new JCheckBox();

    setLayout(new GridBagLayout());

    pageTitleLabel.setText(ResourceMgr.getString("LblExportHtmlPageTitle")); // NOI18N
    pageTitleLabel.setHorizontalTextPosition(SwingConstants.LEADING);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(0, 4, 0, 0);
    add(pageTitleLabel, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new Insets(3, 3, 0, 4);
    add(pageTitle, gridBagConstraints);

    fullPage.setText(ResourceMgr.getString("LblExportFullHtml")); // NOI18N
    fullPage.setBorder(null);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(6, 3, 0, 0);
    add(fullPage, gridBagConstraints);

    escapeHtml.setText(ResourceMgr.getString("LblExportEscapeHtml")); // NOI18N
    escapeHtml.setBorder(null);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new Insets(4, 3, 0, 0);
    add(escapeHtml, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents
	
	
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private JCheckBox escapeHtml;
  private JCheckBox fullPage;
  private JTextField pageTitle;
  private JLabel pageTitleLabel;
  // End of variables declaration//GEN-END:variables
	
}