/*
 * ProgressPanel.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2004, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: info@sql-workbench.net
 *
 */
package workbench.gui.dbobjects;

import java.io.File;

import workbench.gui.components.WbButton;
import workbench.interfaces.Interruptable;
import workbench.resource.ResourceMgr;
import workbench.storage.RowActionMonitor;

/**
 *
 * @author  info@sql-workbench.net
 */
public class ProgressPanel 
	extends javax.swing.JPanel
	implements RowActionMonitor
{

	private Interruptable spooler;
	
	/** Creates new form SpoolerProgress */
	public ProgressPanel(Interruptable aWorker)
	{
		this.spooler = aWorker;
		initComponents();
	}
	
	public void setRowInfo(long aRow)
	{
		this.rowInfo.setText(Long.toString(aRow));
	}
	
	public void setRowInfo(String info)
	{
		this.rowInfo.setText(info);
	}
	
	public void setInfoText(String aText)
	{
		this.progressInfoText.setText(aText);
	}

	public void setFilename(String aFilename)
	{
		File f = new File(aFilename);
		String fullName = f.getAbsolutePath();
		this.fileNameField.setToolTipText(fullName);
		this.fileNameField.setText(fullName);
	}
	
	public void setRowSize(int cols)
	{
		this.rowInfo.setColumns(cols);
	}
	
	public void setInfoSize(int cols)
	{
		this.progressInfoText.setColumns(cols);
	}

	public void jobFinished()
	{
	}

	public void setCurrentObject(String object, int number, int totalObjects)
	{
	}

	public void setCurrentRow(int currentRow, int totalRows)
	{
		if (currentRow > -1) this.rowInfo.setText(Long.toString(currentRow));
	}

	public void setMonitorType(int aType)
	{
	}
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
  private void initComponents()//GEN-BEGIN:initComponents
  {
    java.awt.GridBagConstraints gridBagConstraints;

    fileNameField = new javax.swing.JTextField();
    infoPanel = new javax.swing.JPanel();
    progressInfoText = new javax.swing.JTextField();
    rowInfo = new javax.swing.JTextField();
    cancelButton = new WbButton();

    setLayout(new java.awt.GridBagLayout());

    fileNameField.setEditable(false);
    fileNameField.setBorder(new javax.swing.border.CompoundBorder(new javax.swing.border.EtchedBorder(), new javax.swing.border.EmptyBorder(new java.awt.Insets(2, 2, 2, 2))));
    fileNameField.setDisabledTextColor(java.awt.Color.black);
    fileNameField.setMargin(new java.awt.Insets(0, 2, 0, 0));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 6);
    add(fileNameField, gridBagConstraints);

    infoPanel.setLayout(new java.awt.BorderLayout(0, 5));

    infoPanel.setBorder(new javax.swing.border.CompoundBorder(new javax.swing.border.EtchedBorder(), new javax.swing.border.EmptyBorder(new java.awt.Insets(2, 2, 2, 2))));
    progressInfoText.setColumns(10);
    progressInfoText.setEditable(false);
    progressInfoText.setBorder(null);
    progressInfoText.setDisabledTextColor(java.awt.Color.black);
    infoPanel.add(progressInfoText, java.awt.BorderLayout.CENTER);

    rowInfo.setColumns(8);
    rowInfo.setEditable(false);
    rowInfo.setBorder(null);
    rowInfo.setDisabledTextColor(java.awt.Color.black);
    infoPanel.add(rowInfo, java.awt.BorderLayout.EAST);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(5, 6, 0, 6);
    add(infoPanel, gridBagConstraints);

    cancelButton.setText(ResourceMgr.getString("LabelCancel"));
    cancelButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        cancelButtonActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(12, 0, 4, 0);
    add(cancelButton, gridBagConstraints);

  }//GEN-END:initComponents

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
	{//GEN-HEADEREND:event_cancelButtonActionPerformed
		if (this.spooler != null) 
		{
			if (this.spooler.confirmCancel()) this.spooler.cancelExecution();
		}
	}//GEN-LAST:event_cancelButtonActionPerformed
	
	
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton cancelButton;
  private javax.swing.JTextField fileNameField;
  private javax.swing.JPanel infoPanel;
  private javax.swing.JTextField progressInfoText;
  private javax.swing.JTextField rowInfo;
  // End of variables declaration//GEN-END:variables
	
}