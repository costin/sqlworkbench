/*
 * BlobInfoDialog.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2006, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.dialogs;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import workbench.gui.WbSwingUtilities;
import workbench.gui.actions.EscAction;
import workbench.gui.components.BlobHandler;
import workbench.gui.components.EncodingPanel;
import workbench.gui.components.WbButton;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.util.FileDialogUtil;

/**
 *
 * @author  support@sql-workbench.net
 */
public class BlobInfoDialog
	extends JDialog
	implements java.awt.event.MouseListener, java.awt.event.ActionListener, java.awt.event.WindowListener
{
	private Object blobValue;
	private BlobHandler handler;
	private EscAction escAction;
	private File uploadFile;
	
	public BlobInfoDialog(java.awt.Frame parent, boolean modal)
	{
		super(parent, modal);
		initComponents();
		handler = new BlobHandler();

		getRootPane().setDefaultButton(closeButton);
		InputMap im = this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap am = this.getRootPane().getActionMap();
		escAction = new EscAction(this);
		im.put(escAction.getAccelerator(), escAction.getActionName());
		am.put(escAction.getActionName(), escAction);

		String encoding = Settings.getInstance().getDefaultBlobTextEncoding();
		encodingPanel.setEncoding(encoding);
		WbSwingUtilities.center(this, parent);
	}

	public File getUploadedFile() { return uploadFile; }
	
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == escAction)
		{
			setVisible(false);
			dispose();
		}
	}

	private void closeWindow()
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				setVisible(false);
			}
		});
	}

	public void setBlobValue(Object value)
	{
		this.blobValue = value;
		String lbl = null;
		if (value instanceof File)
		{
			lbl = ResourceMgr.getString("LblFileSize");
		}
		else
		{
			lbl = ResourceMgr.getString("LblBlobSize");
		}
		long len = handler.getBlobSize(blobValue);
		lbl = lbl + ": " + Long.toString(len) + " Byte";
		infoLabel.setText(lbl);
		if (value instanceof File)
		{
			infoLabel.setToolTipText(value.toString());
		}
		else
		{
			infoLabel.setToolTipText(handler.getByteDisplay(len).toString());
		}
		saveAsButton.setEnabled(len > 0);
		showAsTextButton.setEnabled(len > 0);
		showImageButton.setEnabled(len > 0);
		showHexButton.setEnabled(len > 0);
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

    closeButton = new javax.swing.JButton();
    jPanel1 = new javax.swing.JPanel();
    infoLabel = new javax.swing.JLabel();
    showAsTextButton = new WbButton();
    saveAsButton = new WbButton();
    encodingPanel = new EncodingPanel(null, false);
    showImageButton = new javax.swing.JButton();
    uploadButton = new WbButton();
    showHexButton = new javax.swing.JButton();

    getContentPane().setLayout(new java.awt.GridBagLayout());

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    setTitle(ResourceMgr.getString("TxtBlobInfo"));
    setResizable(false);
    addWindowListener(this);

    closeButton.setText(ResourceMgr.getString("LblClose"));
    closeButton.addMouseListener(this);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    gridBagConstraints.insets = new java.awt.Insets(12, 0, 10, 0);
    getContentPane().add(closeButton, gridBagConstraints);

    jPanel1.setLayout(new java.awt.GridBagLayout());

    jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.lightGray));
    infoLabel.setText("jLabel1");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(13, 10, 0, 8);
    jPanel1.add(infoLabel, gridBagConstraints);

    showAsTextButton.setText(ResourceMgr.getString("LblShowAsTxt"));
    showAsTextButton.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3)));
    showAsTextButton.addMouseListener(this);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(0, 8, 14, 5);
    jPanel1.add(showAsTextButton, gridBagConstraints);

    saveAsButton.setText(ResourceMgr.getString("MnuTxtFileSaveAs"));
    saveAsButton.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3)));
    saveAsButton.addMouseListener(this);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(9, 8, 2, 5);
    jPanel1.add(saveAsButton, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 14, 8);
    jPanel1.add(encodingPanel, gridBagConstraints);

    showImageButton.setText(ResourceMgr.getString("LblShowAsImg"));
    showImageButton.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3)));
    showImageButton.addMouseListener(this);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(16, 8, 2, 5);
    jPanel1.add(showImageButton, gridBagConstraints);

    uploadButton.setText(ResourceMgr.getString("LblUploadFile"));
    uploadButton.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3)));
    uploadButton.addMouseListener(this);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(9, 8, 2, 5);
    jPanel1.add(uploadButton, gridBagConstraints);

    showHexButton.setText(ResourceMgr.getString("LblShowAsHex"));
    showHexButton.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3)));
    showHexButton.addMouseListener(this);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(16, 6, 2, 5);
    jPanel1.add(showHexButton, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    getContentPane().add(jPanel1, gridBagConstraints);

    pack();
  }

  // Code for dispatching events from components to event handlers.

  public void mouseClicked(java.awt.event.MouseEvent evt)
  {
    if (evt.getSource() == closeButton)
    {
      BlobInfoDialog.this.closeButtonMouseClicked(evt);
    }
    else if (evt.getSource() == showAsTextButton)
    {
      BlobInfoDialog.this.showAsTextButtonMouseClicked(evt);
    }
    else if (evt.getSource() == saveAsButton)
    {
      BlobInfoDialog.this.saveAsButtonMouseClicked(evt);
    }
    else if (evt.getSource() == showImageButton)
    {
      BlobInfoDialog.this.showImageButtonMouseClicked(evt);
    }
    else if (evt.getSource() == uploadButton)
    {
      BlobInfoDialog.this.uploadButtonMouseClicked(evt);
    }
    else if (evt.getSource() == showHexButton)
    {
      BlobInfoDialog.this.showHexButtonMouseClicked(evt);
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
  }

  public void windowActivated(java.awt.event.WindowEvent evt)
  {
  }

  public void windowClosed(java.awt.event.WindowEvent evt)
  {
    if (evt.getSource() == BlobInfoDialog.this)
    {
      BlobInfoDialog.this.formWindowClosed(evt);
    }
  }

  public void windowClosing(java.awt.event.WindowEvent evt)
  {
  }

  public void windowDeactivated(java.awt.event.WindowEvent evt)
  {
  }

  public void windowDeiconified(java.awt.event.WindowEvent evt)
  {
  }

  public void windowIconified(java.awt.event.WindowEvent evt)
  {
  }

  public void windowOpened(java.awt.event.WindowEvent evt)
  {
  }// </editor-fold>//GEN-END:initComponents

	private void showHexButtonMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_showHexButtonMouseClicked
	{//GEN-HEADEREND:event_showHexButtonMouseClicked
		HexViewer v = new HexViewer(this, ResourceMgr.getString("TxtBlobData"));
		v.setData(handler.getBlobAsArray(this.blobValue));
		v.setVisible(true);
		closeWindow();		
		
	}//GEN-LAST:event_showHexButtonMouseClicked

	private void uploadButtonMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_uploadButtonMouseClicked
	{//GEN-HEADEREND:event_uploadButtonMouseClicked
		String file = FileDialogUtil.getBlobFile(this, false);
		if (file != null)
		{
			this.uploadFile = new File(file);
		}
		closeWindow();
	}//GEN-LAST:event_uploadButtonMouseClicked

	private void showImageButtonMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_showImageButtonMouseClicked
	{//GEN-HEADEREND:event_showImageButtonMouseClicked
		ImageViewer v = new ImageViewer(this, ResourceMgr.getString("TxtBlobData"));
		v.setData(this.blobValue);
		v.setVisible(true);
		closeWindow();
	}//GEN-LAST:event_showImageButtonMouseClicked

	private void formWindowClosed(java.awt.event.WindowEvent evt)//GEN-FIRST:event_formWindowClosed
	{//GEN-HEADEREND:event_formWindowClosed
		String encoding = encodingPanel.getEncoding();
		Settings.getInstance().setDefaultBlobTextEncoding(encoding);
	}//GEN-LAST:event_formWindowClosed

	private void saveAsButtonMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_saveAsButtonMouseClicked
	{//GEN-HEADEREND:event_saveAsButtonMouseClicked
		handler.saveBlobToFile(this, blobValue);
	}//GEN-LAST:event_saveAsButtonMouseClicked

	private void showAsTextButtonMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_showAsTextButtonMouseClicked
	{//GEN-HEADEREND:event_showAsTextButtonMouseClicked
		handler.showBlobAsText(this, this.blobValue, encodingPanel.getEncoding());
		closeWindow();
	}//GEN-LAST:event_showAsTextButtonMouseClicked

	private void closeButtonMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_closeButtonMouseClicked
	{//GEN-HEADEREND:event_closeButtonMouseClicked
		this.setVisible(false);
	}//GEN-LAST:event_closeButtonMouseClicked

  // Variables declaration - do not modify//GEN-BEGIN:variables
  public javax.swing.JButton closeButton;
  public workbench.gui.components.EncodingPanel encodingPanel;
  public javax.swing.JLabel infoLabel;
  public javax.swing.JPanel jPanel1;
  public javax.swing.JButton saveAsButton;
  public javax.swing.JButton showAsTextButton;
  public javax.swing.JButton showHexButton;
  public javax.swing.JButton showImageButton;
  public javax.swing.JButton uploadButton;
  // End of variables declaration//GEN-END:variables

}
