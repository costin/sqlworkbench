/*
 * WbAboutDialog.java
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

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;

import workbench.gui.actions.EscAction;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.util.BrowserLauncher;

/**
 *
 * @author  support@sql-workbench.net
 */
public class WbAboutDialog extends javax.swing.JDialog
	implements ActionListener
{
	private EscAction escAction;

	/** Creates new form WbAboutDialog */
	public WbAboutDialog(java.awt.Frame parent, boolean modal)
	{
		super(parent, modal);
		initComponents();
		homepageLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		mailToLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		jLabel1.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		getRootPane().setDefaultButton(closeButton);
		InputMap im = this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap am = this.getRootPane().getActionMap();
		escAction = new EscAction(this);
		im.put(escAction.getAccelerator(), escAction.getActionName());
		am.put(escAction.getActionName(), escAction);
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

    buttonPanel = new javax.swing.JPanel();
    closeButton = new javax.swing.JButton();
    contentPanel = new javax.swing.JPanel();
    logo = new javax.swing.JLabel();
    labelTitel = new javax.swing.JLabel();
    labelDesc = new javax.swing.JLabel();
    labelVersion = new javax.swing.JLabel();
    labelCopyright = new javax.swing.JLabel();
    jLabel1 = new javax.swing.JLabel();
    jLabel2 = new javax.swing.JLabel();
    jdkVersion = new javax.swing.JLabel();
    homepageLabel = new javax.swing.JLabel();
    mailToLabel = new javax.swing.JLabel();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    setTitle(ResourceMgr.getString("TxtAbout") + " " + ResourceMgr.TXT_PRODUCT_NAME);
    setName("AboutDialog");
    addWindowListener(new java.awt.event.WindowAdapter()
    {
      public void windowClosing(java.awt.event.WindowEvent evt)
      {
        closeDialog(evt);
      }
    });

    buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

    closeButton.setText(ResourceMgr.getString("LabelClose"));
    closeButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        closeButtonActionPerformed(evt);
      }
    });

    buttonPanel.add(closeButton);

    getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);

    contentPanel.setLayout(new java.awt.GridBagLayout());

    logo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/workbench/resource/images/hitchguide.gif")));
    logo.setBorder(new javax.swing.border.EtchedBorder());
    logo.setIconTextGap(0);
    logo.setMaximumSize(new java.awt.Dimension(172, 128));
    logo.setMinimumSize(new java.awt.Dimension(172, 128));
    logo.setPreferredSize(new java.awt.Dimension(172, 128));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridheight = 7;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
    contentPanel.add(logo, gridBagConstraints);

    labelTitel.setFont(new java.awt.Font("Dialog", 1, 14));
    labelTitel.setText(ResourceMgr.TXT_PRODUCT_NAME);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(6, 8, 0, 4);
    contentPanel.add(labelTitel, gridBagConstraints);

    labelDesc.setText(ResourceMgr.getString("TxtProductDescription"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 4);
    contentPanel.add(labelDesc, gridBagConstraints);

    labelVersion.setText(ResourceMgr.getBuildInfo());
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 4);
    contentPanel.add(labelVersion, gridBagConstraints);

    labelCopyright.setText(ResourceMgr.getString("TxtCopyright"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(12, 8, 0, 4);
    contentPanel.add(labelCopyright, gridBagConstraints);

    jLabel1.setText("<html>Built with NetBeans (<u>www.netbeans.org</u>)</html>");
    jLabel1.addMouseListener(new java.awt.event.MouseAdapter()
    {
      public void mouseClicked(java.awt.event.MouseEvent evt)
      {
        jLabel1MouseClicked(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(4, 5, 0, 4);
    contentPanel.add(jLabel1, gridBagConstraints);

    jLabel2.setText("The editor is based on jEdit's 2.2.1 syntax highlighting package");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 4);
    contentPanel.add(jLabel2, gridBagConstraints);

    jdkVersion.setText(ResourceMgr.getString("TxtJavaVersion") + " " + System.getProperty("java.version"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 4);
    contentPanel.add(jdkVersion, gridBagConstraints);

    homepageLabel.setText("<html><u>www.sql-workbench.net</u></html>");
    homepageLabel.addMouseListener(new java.awt.event.MouseAdapter()
    {
      public void mouseClicked(java.awt.event.MouseEvent evt)
      {
        homepageLabelMouseClicked(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 4);
    contentPanel.add(homepageLabel, gridBagConstraints);

    mailToLabel.setText("support@sql-workbench.net");
    mailToLabel.addMouseListener(new java.awt.event.MouseAdapter()
    {
      public void mouseClicked(java.awt.event.MouseEvent evt)
      {
        mailToLabelMouseClicked(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 4);
    contentPanel.add(mailToLabel, gridBagConstraints);

    getContentPane().add(contentPanel, java.awt.BorderLayout.CENTER);

    pack();
  }
  // </editor-fold>//GEN-END:initComponents

	private void mailToLabelMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_mailToLabelMouseClicked
	{//GEN-HEADEREND:event_mailToLabelMouseClicked
		try
		{
			if (evt.getClickCount() == 1) BrowserLauncher.openURL("mailto:support@sql-workbench.net");
		}
		catch (Exception e)
		{
		}

	}//GEN-LAST:event_mailToLabelMouseClicked

	private void homepageLabelMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_homepageLabelMouseClicked
	{//GEN-HEADEREND:event_homepageLabelMouseClicked
		try
		{
			if (evt.getClickCount() == 1) BrowserLauncher.openURL("http://www.sql-workbench.net");
		}
		catch (Exception e)
		{
		}
	}//GEN-LAST:event_homepageLabelMouseClicked

	private void jLabel1MouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_jLabel1MouseClicked
	{//GEN-HEADEREND:event_jLabel1MouseClicked
		try
		{
			if (evt.getClickCount() == 1) BrowserLauncher.openURL("http://www.netbeans.org");
		}
		catch (Exception e)
		{
		}
	}//GEN-LAST:event_jLabel1MouseClicked

	private void closeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_closeButtonActionPerformed
	{//GEN-HEADEREND:event_closeButtonActionPerformed
		this.closeDialog(null);
	}//GEN-LAST:event_closeButtonActionPerformed

	/** Closes the dialog */
	private void closeDialog(java.awt.event.WindowEvent evt)
	{//GEN-FIRST:event_closeDialog
		setVisible(false);
		dispose();
	}//GEN-LAST:event_closeDialog

	public void actionPerformed(ActionEvent e)
	{
		if (e.getActionCommand().equals(escAction.getActionName()))
		{
			closeDialog(null);
		}
	}

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel buttonPanel;
  private javax.swing.JButton closeButton;
  private javax.swing.JPanel contentPanel;
  private javax.swing.JLabel homepageLabel;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jdkVersion;
  private javax.swing.JLabel labelCopyright;
  private javax.swing.JLabel labelDesc;
  private javax.swing.JLabel labelTitel;
  private javax.swing.JLabel labelVersion;
  private javax.swing.JLabel logo;
  private javax.swing.JLabel mailToLabel;
  // End of variables declaration//GEN-END:variables

}
