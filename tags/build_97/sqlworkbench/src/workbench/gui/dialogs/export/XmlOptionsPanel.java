/*
 * XmlOptionsPanel.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2007, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.dialogs.export;

import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.gui.dialogs.export.XmlOptions;

/**
 *
 * @author  support@sql-workbench.net
 */
public class XmlOptionsPanel 
	extends javax.swing.JPanel
	implements XmlOptions
{
	
	/** Creates new form XmlOptionsPanel */
	public XmlOptionsPanel()
	{
		initComponents();
	}

	public void saveSettings()
	{
		Settings s = Settings.getInstance();
		s.setProperty("workbench.export.xml.usecdata", this.getUseCDATA());
		s.setProperty("workbench.export.xml.verbosexml", this.getUseVerboseXml());
	}
	
	public void restoreSettings()
	{
		Settings s = Settings.getInstance();
		this.setUseCDATA(s.getBoolProperty("workbench.export.xml.usecdata"));
		this.setUseVerboseXml(s.getBoolProperty("workbench.export.xml.verbosexml", true));
	}
	
	public boolean getUseVerboseXml()
	{
		return this.verboseXmlCheckBox.isSelected();
	}
	
	public void setUseVerboseXml(boolean flag)
	{
		this.verboseXmlCheckBox.setSelected(flag);
	}
	
	public boolean getUseCDATA()
	{
		return useCdata.isSelected();
	}
	
	public void setUseCDATA(boolean flag)
	{
		useCdata.setSelected(flag);
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

    useCdata = new javax.swing.JCheckBox();
    verboseXmlCheckBox = new javax.swing.JCheckBox();

    setLayout(new java.awt.GridBagLayout());

    useCdata.setText(ResourceMgr.getString("LblExportUseCDATA"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    add(useCdata, gridBagConstraints);

    verboseXmlCheckBox.setText(ResourceMgr.getString("LblExportVerboseXml"));
    verboseXmlCheckBox.setToolTipText(ResourceMgr.getDescription("LblExportVerboseXml"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    add(verboseXmlCheckBox, gridBagConstraints);

  }
  // </editor-fold>//GEN-END:initComponents
	
	
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JCheckBox useCdata;
  private javax.swing.JCheckBox verboseXmlCheckBox;
  // End of variables declaration//GEN-END:variables
	
}