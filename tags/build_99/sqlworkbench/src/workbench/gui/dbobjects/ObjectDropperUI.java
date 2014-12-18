/*
 * ObjectDropperUI.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2008, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.dbobjects;

import java.awt.EventQueue;
import java.awt.Frame;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.WindowConstants;

import workbench.db.DbObject;
import workbench.gui.WbSwingUtilities;
import workbench.gui.components.NoSelectionModel;
import workbench.gui.components.WbButton;
import workbench.interfaces.ObjectDropper;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.util.WbThread;

/**
 *
 * @author  support@sql-workbench.net
 */
public class ObjectDropperUI
	extends javax.swing.JPanel
{
	protected JDialog dialog;
	protected boolean cancelled;
	protected boolean running;
	protected ObjectDropper dropper;
	private Thread dropThread;
	
	public ObjectDropperUI(ObjectDropper drop)
	{
		dropper = drop;
		initComponents();
	}
	
	protected void doDrop()
	{
		if (this.running) return;
		try
		{
			this.running = true;
			this.cancelled = false;
			this.dropper.dropObjects();
		}
		catch (Throwable ex)
		{
			String msg = ex.getMessage();
			WbSwingUtilities.showErrorMessage(this.dialog, msg);
		}
		finally
		{
			this.running = false;
		}

		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				if (cancelled)
				{
					dropButton.setEnabled(true);
				}
				else
				{
					dialog.setVisible(false);
					dialog.dispose();
					dialog = null;
				}
			}
		});

	}

	public boolean dialogWasCancelled()
	{
		return this.cancelled;
	}

	protected void initDisplay()
	{
		List<? extends DbObject> objects = this.dropper.getObjects();
		int numNames = objects.size();

		String[] display = new String[numNames];
		for (int i=0; i < numNames; i ++)
		{
			display[i] = objects.get(i).getObjectType() + " " + objects.get(i).getObjectName();
		}
		this.objectList.setListData(display);
		
		if (!dropper.supportsCascade())
		{
			this.mainPanel.remove(this.optionPanel);
			this.checkBoxCascadeConstraints.setSelected(false);
		}
	}

	public void showDialog(Frame aParent)
	{
		initDisplay();
		
		this.dialog = new JDialog(aParent, ResourceMgr.getString("TxtDropObjectsTitle"), true);
		try
		{
			this.dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			this.dialog.getContentPane().add(this);
			this.dialog.pack();
			if (this.dialog.getWidth() < 200)
			{
				this.dialog.setSize(200, this.dialog.getHeight());
			}
			WbSwingUtilities.center(this.dialog, aParent);
			this.cancelled = true;
			this.dialog.setVisible(true);
		}
		finally
		{
			if (this.dialog != null)
			{
				this.dialog.dispose();
				this.dialog = null;
			}
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
    buttonPanel = new javax.swing.JPanel();
    dropButton = new WbButton();
    cancelButton = new WbButton();
    mainPanel = new javax.swing.JPanel();
    jScrollPane1 = new javax.swing.JScrollPane();
    objectList = new javax.swing.JList();
    optionPanel = new javax.swing.JPanel();
    checkBoxCascadeConstraints = new javax.swing.JCheckBox();

    setLayout(new java.awt.BorderLayout());

    buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

    dropButton.setText(ResourceMgr.getString("LblDrop"));
    dropButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        dropButtonActionPerformed(evt);
      }
    });

    buttonPanel.add(dropButton);

    cancelButton.setText(ResourceMgr.getString("LblCancel"));
    cancelButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        cancelButtonActionPerformed(evt);
      }
    });

    buttonPanel.add(cancelButton);

    add(buttonPanel, java.awt.BorderLayout.SOUTH);

    mainPanel.setLayout(new java.awt.BorderLayout());

    objectList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    objectList.setSelectionModel(new NoSelectionModel());
    jScrollPane1.setViewportView(objectList);

    mainPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

    optionPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

    checkBoxCascadeConstraints.setText(ResourceMgr.getString("LblCascadeConstraints"));
    checkBoxCascadeConstraints.setToolTipText(ResourceMgr.getDescription("LblCascadeConstraints"));
    optionPanel.add(checkBoxCascadeConstraints);

    mainPanel.add(optionPanel, java.awt.BorderLayout.SOUTH);

    add(mainPanel, java.awt.BorderLayout.CENTER);

  }// </editor-fold>//GEN-END:initComponents

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
	{//GEN-HEADEREND:event_cancelButtonActionPerformed
		this.cancelled = true;
		if (this.running)
		{
			try
			{
				dropper.cancel();
			}
			catch (Exception e)
			{
				LogMgr.logError("ObjectDropperUI", "Error when cancelling drop", e);
			}
			dropButton.setEnabled(true);
		}
		else
		{
			this.dialog.setVisible(false);
			this.dialog.dispose();
			this.dialog = null;
		}
	}//GEN-LAST:event_cancelButtonActionPerformed

	private void dropButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_dropButtonActionPerformed
	{//GEN-HEADEREND:event_dropButtonActionPerformed
		if (this.running) return;
		
		this.dropButton.setEnabled(false);
		this.dropper.setCascade(checkBoxCascadeConstraints.isSelected());
		
		dropThread = new WbThread("DropThread")
		{
			public void run()
			{
				doDrop();
			}
		};
		dropThread.start();
	}//GEN-LAST:event_dropButtonActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  protected javax.swing.JPanel buttonPanel;
  protected javax.swing.JButton cancelButton;
  protected javax.swing.JCheckBox checkBoxCascadeConstraints;
  protected javax.swing.JButton dropButton;
  protected javax.swing.JScrollPane jScrollPane1;
  protected javax.swing.JPanel mainPanel;
  protected javax.swing.JList objectList;
  protected javax.swing.JPanel optionPanel;
  // End of variables declaration//GEN-END:variables

}