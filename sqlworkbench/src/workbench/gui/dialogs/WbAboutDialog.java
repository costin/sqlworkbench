/*
 * WbAboutDialog.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2008, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import workbench.gui.actions.EscAction;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.util.BrowserLauncher;
import workbench.util.WbFile;

/**
 *
 * @author  support@sql-workbench.net
 */
public class WbAboutDialog 
	extends JDialog
	implements ActionListener
{
	private EscAction escAction;

	public WbAboutDialog(java.awt.Frame parent)
	{
		super(parent, true);
		initComponents();
		homepageLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		mailToLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		builtWithNbLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		jeditLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		getRootPane().setDefaultButton(closeButton);
		escAction = new EscAction(this, this);
		WbFile f = Settings.getInstance().getConfigFile();
		String s = ResourceMgr.getFormattedString("LblSettingsLocation", f.getFullPath());
		settingsLabel.setText(s);
		f = LogMgr.getLogfile();
		logfileLabel.setText("Logfile: " + f.getFullPath());
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
		GridBagConstraints gridBagConstraints;

    buttonPanel = new JPanel();
    closeButton = new JButton();
    contentPanel = new JPanel();
    logo = new JLabel();
    labelTitel = new JLabel();
    labelDesc = new JLabel();
    labelVersion = new JLabel();
    labelCopyright = new JLabel();
    builtWithNbLabel = new JLabel();
    jeditLabel = new JLabel();
    jdkVersion = new JLabel();
    homepageLabel = new JLabel();
    mailToLabel = new JLabel();
    settingsLabel = new JLabel();
    logfileLabel = new JLabel();

    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setTitle(ResourceMgr.getString("TxtAbout") + " " + ResourceMgr.TXT_PRODUCT_NAME);
    setName("AboutDialog"); // NOI18N
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent evt) {
        closeDialog(evt);
      }
    });

    buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

    closeButton.setText(ResourceMgr.getString("LblClose"));
    closeButton.setName("close"); // NOI18N
    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        closeButtonActionPerformed(evt);
      }
    });
    buttonPanel.add(closeButton);

    getContentPane().add(buttonPanel, BorderLayout.SOUTH);

    contentPanel.setLayout(new GridBagLayout());

    logo.setIcon(new ImageIcon(getClass().getResource("/workbench/resource/images/hitchguide.gif"))); // NOI18N
    logo.setBorder(BorderFactory.createEtchedBorder());
    logo.setIconTextGap(0);
    logo.setMaximumSize(new Dimension(172, 128));
    logo.setMinimumSize(new Dimension(172, 128));
    logo.setPreferredSize(new Dimension(172, 128));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridheight = 7;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(5, 5, 0, 0);
    contentPanel.add(logo, gridBagConstraints);

    labelTitel.setFont(new Font("Dialog", 1, 14));
    labelTitel.setText(ResourceMgr.TXT_PRODUCT_NAME);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new Insets(6, 8, 0, 4);
    contentPanel.add(labelTitel, gridBagConstraints);

    labelDesc.setText(ResourceMgr.getString("TxtProductDescription"));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(0, 8, 0, 4);
    contentPanel.add(labelDesc, gridBagConstraints);

    labelVersion.setText(ResourceMgr.getBuildInfo());
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(0, 8, 0, 4);
    contentPanel.add(labelVersion, gridBagConstraints);

    labelCopyright.setText(ResourceMgr.getString("TxtCopyright"));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(12, 8, 0, 4);
    contentPanel.add(labelCopyright, gridBagConstraints);

    builtWithNbLabel.setText("<html>Built with NetBeans (<u>www.netbeans.org</u>)</html>");
    builtWithNbLabel.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent evt) {
        builtWithNbLabelMouseClicked(evt);
      }
    });
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(4, 5, 0, 4);
    contentPanel.add(builtWithNbLabel, gridBagConstraints);

    jeditLabel.setText("<html>The editor is based on jEdit's 2.2.2 <u>syntax highlighting package</u></html>");
    jeditLabel.setToolTipText("http://syntax.jedit.org/");
    jeditLabel.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent evt) {
        jeditLabelMouseClicked(evt);
      }
    });
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(0, 5, 5, 4);
    contentPanel.add(jeditLabel, gridBagConstraints);

    jdkVersion.setText(ResourceMgr.getString("TxtJavaVersion") + " " + System.getProperty("java.runtime.version"));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(0, 8, 0, 4);
    contentPanel.add(jdkVersion, gridBagConstraints);

    homepageLabel.setText("<html><u>www.sql-workbench.net</u></html>");
    homepageLabel.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent evt) {
        homepageLabelMouseClicked(evt);
      }
    });
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(0, 8, 0, 4);
    contentPanel.add(homepageLabel, gridBagConstraints);

    mailToLabel.setText("support@sql-workbench.net");
    mailToLabel.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent evt) {
        mailToLabelMouseClicked(evt);
      }
    });
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(0, 8, 0, 4);
    contentPanel.add(mailToLabel, gridBagConstraints);

    settingsLabel.setText("Settings:");
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.SOUTHWEST;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new Insets(7, 5, 0, 0);
    contentPanel.add(settingsLabel, gridBagConstraints);

    logfileLabel.setText("Log file:");
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 10;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(1, 5, 0, 0);
    contentPanel.add(logfileLabel, gridBagConstraints);

    getContentPane().add(contentPanel, BorderLayout.CENTER);

    pack();
  }// </editor-fold>//GEN-END:initComponents

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

	private void builtWithNbLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_builtWithNbLabelMouseClicked
		try
		{
			if (evt.getClickCount() == 1) BrowserLauncher.openURL("http://www.netbeans.org");
		}
		catch (Exception e)
		{
		}
}//GEN-LAST:event_builtWithNbLabelMouseClicked

	private void closeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_closeButtonActionPerformed
	{//GEN-HEADEREND:event_closeButtonActionPerformed
		this.closeDialog(null);
	}//GEN-LAST:event_closeButtonActionPerformed

	private void closeDialog(java.awt.event.WindowEvent evt)
	{//GEN-FIRST:event_closeDialog
		setVisible(false);
		dispose();
	}//GEN-LAST:event_closeDialog

	private void jeditLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jeditLabelMouseClicked
		try
		{
			if (evt.getClickCount() == 1) BrowserLauncher.openURL("http://syntax.jedit.org/");
		}
		catch (Exception e)
		{
		}
}//GEN-LAST:event_jeditLabelMouseClicked

	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == escAction)
		{
			closeDialog(null);
		}
	}

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private JLabel builtWithNbLabel;
  private JPanel buttonPanel;
  private JButton closeButton;
  private JPanel contentPanel;
  private JLabel homepageLabel;
  private JLabel jdkVersion;
  private JLabel jeditLabel;
  private JLabel labelCopyright;
  private JLabel labelDesc;
  private JLabel labelTitel;
  private JLabel labelVersion;
  private JLabel logfileLabel;
  private JLabel logo;
  private JLabel mailToLabel;
  private JLabel settingsLabel;
  // End of variables declaration//GEN-END:variables

}
