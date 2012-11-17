/*
 * VersionCheckDialog.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2012, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.tools;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import workbench.resource.ResourceMgr;

import workbench.util.BrowserLauncher;
import workbench.util.UpdateVersion;
import workbench.util.VersionNumber;
import workbench.util.WbVersionReader;

/**
 * A Dialog to display available versions from the SQL Workbench/J homepage.
 *
 * @author  Thomas Kellerer
 */
public class VersionCheckDialog
	extends JDialog
	implements ActionListener, MouseListener
{
	private WbVersionReader versionReader;

	public VersionCheckDialog(Frame parent)
	{
		super(parent, true);
		initComponents();
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.closeButton.addActionListener(this);
		Font f = this.headingLabel.getFont().deriveFont(Font.BOLD);
		this.headingLabel.setFont(f);
		this.headingLabel.setText(ResourceMgr.getString("LblReadingVersion"));
		this.headingLabel.addMouseListener(this);
		this.headingLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		this.stableVersion.setText(" " + ResourceMgr.getString("LblNotAvailable"));
		this.devVersion.setText(" " + ResourceMgr.getString("LblNotAvailable"));
	}

	private void startRetrieveVersions()
	{
		readVersion();
	}

	@Override
	public void setVisible(boolean flag)
	{
		if (flag) startRetrieveVersions();
		super.setVisible(flag);
	}

	protected void readVersion()
	{
		this.versionReader = new WbVersionReader(this);
		this.versionReader.startCheckThread();
	}

	private void checkDisplay()
	{
		UpdateVersion version = this.versionReader.getAvailableUpdate();

		String msg = ResourceMgr.getString("LblVersionUpToDate");
		if (!this.versionReader.success())
		{
			msg = ResourceMgr.getString("LblVersionReadError");
		}
		else if (version == UpdateVersion.stable)
		{
			this.stableVersion.setBackground(Color.YELLOW);
			msg = ResourceMgr.getString("LblVersionNewStableAvailable");
		}
		else if (version == UpdateVersion.devBuild)
		{
			this.devVersion.setBackground(Color.YELLOW);
			msg = ResourceMgr.getString("LblVersionNewDevAvailable");
		}
		this.statusLabel.setText(msg);
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
		GridBagConstraints gridBagConstraints;

    yourVersionLabel = new JLabel();
    yourVersion = new JLabel();
    closeButton = new JButton();
    stableVersionLabel = new JLabel();
    stableVersion = new JLabel();
    devVersionLabel = new JLabel();
    devVersion = new JLabel();
    statusLabel = new JLabel();
    headingLabel = new JLabel();
    jSeparator2 = new JSeparator();

    setTitle(ResourceMgr.getString("TxtWindowTitleVersionCheck"));
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent evt) {
        closeDialog(evt);
      }
    });
    getContentPane().setLayout(new GridBagLayout());

    yourVersionLabel.setText(ResourceMgr.getString("LblYourVersion"));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new Insets(5, 5, 5, 0);
    getContentPane().add(yourVersionLabel, gridBagConstraints);

    yourVersion.setBackground(Color.white);
    yourVersion.setText(" " + ResourceMgr.getBuildInfo());
    yourVersion.setOpaque(true);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new Insets(5, 10, 5, 5);
    getContentPane().add(yourVersion, gridBagConstraints);

    closeButton.setText(ResourceMgr.getString("LblClose"));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.anchor = GridBagConstraints.SOUTHEAST;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new Insets(10, 0, 5, 5);
    getContentPane().add(closeButton, gridBagConstraints);

    stableVersionLabel.setText(ResourceMgr.getString("LblStableVersion"));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new Insets(0, 5, 5, 0);
    getContentPane().add(stableVersionLabel, gridBagConstraints);

    stableVersion.setBackground(Color.white);
    stableVersion.setOpaque(true);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(0, 10, 5, 5);
    getContentPane().add(stableVersion, gridBagConstraints);

    devVersionLabel.setText(ResourceMgr.getString("LblDevVersion"));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new Insets(0, 5, 0, 0);
    getContentPane().add(devVersionLabel, gridBagConstraints);

    devVersion.setBackground(Color.white);
    devVersion.setOpaque(true);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(0, 10, 0, 5);
    getContentPane().add(devVersion, gridBagConstraints);

    statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
    statusLabel.setBorder(BorderFactory.createEtchedBorder());
    statusLabel.setMaximumSize(new Dimension(32768, 28));
    statusLabel.setMinimumSize(new Dimension(100, 28));
    statusLabel.setPreferredSize(new Dimension(200, 28));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new Insets(9, 3, 0, 3);
    getContentPane().add(statusLabel, gridBagConstraints);

    headingLabel.setBackground(new Color(255, 255, 255));
    headingLabel.setHorizontalAlignment(SwingConstants.CENTER);
    headingLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    headingLabel.setMaximumSize(new Dimension(32768, 25));
    headingLabel.setMinimumSize(new Dimension(150, 25));
    headingLabel.setOpaque(true);
    headingLabel.setPreferredSize(new Dimension(200, 25));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new Insets(8, 2, 5, 2);
    getContentPane().add(headingLabel, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.insets = new Insets(0, 0, 4, 0);
    getContentPane().add(jSeparator2, gridBagConstraints);

    setSize(new Dimension(386, 212));
  }// </editor-fold>//GEN-END:initComponents

	/** Closes the dialog */
	private void closeDialog(java.awt.event.WindowEvent evt)//GEN-FIRST:event_closeDialog
	{
		setVisible(false);
		dispose();
	}//GEN-LAST:event_closeDialog

	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == this.versionReader)
		{
			if (this.versionReader.success())
			{
				this.stableVersion.setText(" " + ResourceMgr.getString("TxtBuild") + " " + this.versionReader.getStableBuildNumber() + " (" + this.versionReader.getStableBuildDate() + ")");
				String date = this.versionReader.getDevBuildDate();
				VersionNumber nr = this.versionReader.getDevBuildNumber();
				if (date != null && nr.isValid())
				{
					this.devVersion.setText(" " + ResourceMgr.getString("TxtBuild") + " " + nr + " (" + date + ")");
				}
			}
			else
			{
				this.stableVersion.setText(" " + ResourceMgr.getString("LblNotAvailable"));
				this.devVersion.setText(" " + ResourceMgr.getString("LblNotAvailable"));
			}
			this.headingLabel.setText(ResourceMgr.getString("LblVersionsAvailable"));
			this.headingLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

			checkDisplay();
		}
		else if (e.getSource() == this.closeButton)
		{
			setVisible(false);
			dispose();
		}
	}

	public void mouseClicked(java.awt.event.MouseEvent e)
	{
		if (e.getSource() == this.headingLabel &&
				e.getButton() == MouseEvent.BUTTON1 &&
				e.getClickCount() == 1)
		{
			try
			{
				BrowserLauncher.openURL("http://www.sql-workbench.net");
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

	public void mouseEntered(java.awt.event.MouseEvent e)
	{
	}

	public void mouseExited(java.awt.event.MouseEvent e)
	{
	}

	public void mousePressed(java.awt.event.MouseEvent e)
	{
	}

	public void mouseReleased(java.awt.event.MouseEvent e)
	{
	}

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private JButton closeButton;
  private JLabel devVersion;
  private JLabel devVersionLabel;
  private JLabel headingLabel;
  private JSeparator jSeparator2;
  private JLabel stableVersion;
  private JLabel stableVersionLabel;
  private JLabel statusLabel;
  private JLabel yourVersion;
  private JLabel yourVersionLabel;
  // End of variables declaration//GEN-END:variables

}
