/*
 * VersionCheckDialog.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2006, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.tools;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.Timer;
import workbench.resource.ResourceMgr;
import workbench.util.BrowserLauncher;
import workbench.util.WbThread;
import workbench.util.WbVersionReader;

/**
 *
 * @author  support@sql-workbench.net
 */
public class VersionCheckDialog extends javax.swing.JDialog
	implements ActionListener, MouseListener
{
	private Thread readThread;
	private Timer timeout;
	private boolean timedOut = false;
	private WbVersionReader versionReader;

	/** Creates new form VersionCheckDialog */
	public VersionCheckDialog(java.awt.Frame parent)
	{
		super(parent, true);
		initComponents();
		this.closeButton.addActionListener(this);
		Font f = this.headingLabel.getFont().deriveFont(Font.BOLD);
		this.headingLabel.setFont(f);
		this.headingLabel.setText(ResourceMgr.getString("LblReadingVersion"));
		this.headingLabel.addMouseListener(this);
		this.headingLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}

	private void startRetrieveVersions()
	{
		this.readThread = new WbThread("WbVersionCheck Thread")
		{
			public void run()
			{
				readVersion();
			}
		};
		readThread.start();

		this.timeout = new Timer(60 * 1000, this);
		this.timedOut = false;
		this.timeout.start();
	}

	public void show()
	{
		startRetrieveVersions();
		super.show();
	}
	
	private void readVersion()
	{
		try
		{
			this.versionReader = new WbVersionReader();

			this.stableVersion.setText(" " + ResourceMgr.getString("LblNotAvailable"));
			this.devVersion.setText(" " + ResourceMgr.getString("LblNotAvailable"));

			if (!this.timedOut)
			{
				this.stableVersion.setText(" " + ResourceMgr.getString("TxtBuild") + " " + this.versionReader.getStableBuildNumber() + " (" + this.versionReader.getStableBuildDate() + ")");
				String date = this.versionReader.getDevBuildDate();
				String nr = this.versionReader.getDevBuildNumber();
				if (date != null && nr != null)
				{
					this.devVersion.setText(" " + ResourceMgr.getString("TxtBuild") + " " + nr + " (" + date + ")");
				}
			}
			checkDisplay();
		}
		catch (Exception e)
		{
			this.stableVersion.setText(" " + ResourceMgr.getString("LblNotAvailable"));
			this.devVersion.setText(" " + ResourceMgr.getString("LblNotAvailable"));
		}
		finally
		{
			if (this.timeout != null)
			{
				this.timeout.stop();
				this.timeout = null;
			}
			this.headingLabel.setText(ResourceMgr.getString("LblVersionsAvailable"));
			this.headingLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
	}

	private void checkDisplay()
	{
		if (ResourceMgr.isDevBuild())
			checkDevVersion();
		else
			checkStableVersion();
	}

	private void checkDevVersion()
	{
		String builddate = this.versionReader.getDevBuildDate();
		String stableDate = this.versionReader.getStableBuildDate();

		Date current = ResourceMgr.getBuildDate();
		Date last = null;
		Date lastStable = null;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		try
		{
			last = format.parse(builddate);
			lastStable = format.parse(stableDate);
		}
		catch (Exception e)
		{
			last = new Date(0);
			lastStable = new Date(0);
		}

		String msg = ResourceMgr.getString("LblVersionUpToDate");

		if (lastStable.getTime() > current.getTime())
		{
			this.stableVersion.setBackground(Color.YELLOW);
			msg = ResourceMgr.getString("LblVersionNewStableAvailable");
		}
		else if (last.getTime() > current.getTime())
		{
			this.devVersion.setBackground(Color.YELLOW);
			msg = ResourceMgr.getString("LblVersionNewDevAvailable");
		}
		this.statusLabel.setText(msg);
	}

	private void checkStableVersion()
	{
		int current = ResourceMgr.getBuildNumber();
		String last = this.versionReader.getStableBuildNumber();
		int releaseVersion = -1;
		try
		{
			releaseVersion = Integer.parseInt(last);
		}
		catch (Exception e)
		{
			releaseVersion = -1;
		}

		String msg = ResourceMgr.getString("LblVersionUpToDate");
		if (releaseVersion > current)
		{
			this.stableVersion.setBackground(Color.YELLOW);
			msg = ResourceMgr.getString("LblVersionNewStableAvailable");
		}
		this.statusLabel.setText(msg);
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

    yourVersionLabel = new javax.swing.JLabel();
    yourVersion = new javax.swing.JLabel();
    closeButton = new javax.swing.JButton();
    stableVersionLabel = new javax.swing.JLabel();
    stableVersion = new javax.swing.JLabel();
    devVersionLabel = new javax.swing.JLabel();
    devVersion = new javax.swing.JLabel();
    statusLabel = new javax.swing.JLabel();
    headingLabel = new javax.swing.JLabel();
    jSeparator2 = new javax.swing.JSeparator();

    getContentPane().setLayout(new java.awt.GridBagLayout());

    setTitle(ResourceMgr.getString("TxtWindowTitleVersionCheck"));
    setResizable(false);
    addWindowListener(new java.awt.event.WindowAdapter()
    {
      public void windowClosing(java.awt.event.WindowEvent evt)
      {
        closeDialog(evt);
      }
    });

    yourVersionLabel.setText(ResourceMgr.getString("LblYourVersion"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
    getContentPane().add(yourVersionLabel, gridBagConstraints);

    yourVersion.setBackground(java.awt.Color.white);
    yourVersion.setText(" " + ResourceMgr.getBuildInfo());
    yourVersion.setOpaque(true);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 5);
    getContentPane().add(yourVersion, gridBagConstraints);

    closeButton.setText(ResourceMgr.getString("LblClose"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 5);
    getContentPane().add(closeButton, gridBagConstraints);

    stableVersionLabel.setText(ResourceMgr.getString("LblStableVersion"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
    getContentPane().add(stableVersionLabel, gridBagConstraints);

    stableVersion.setBackground(java.awt.Color.white);
    stableVersion.setOpaque(true);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 5);
    getContentPane().add(stableVersion, gridBagConstraints);

    devVersionLabel.setText(ResourceMgr.getString("LblDevVersion"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
    getContentPane().add(devVersionLabel, gridBagConstraints);

    devVersion.setBackground(java.awt.Color.white);
    devVersion.setOpaque(true);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 5);
    getContentPane().add(devVersion, gridBagConstraints);

    statusLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    statusLabel.setBorder(new javax.swing.border.EtchedBorder());
    statusLabel.setMaximumSize(new java.awt.Dimension(32768, 28));
    statusLabel.setMinimumSize(new java.awt.Dimension(100, 28));
    statusLabel.setPreferredSize(new java.awt.Dimension(200, 28));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(9, 3, 0, 3);
    getContentPane().add(statusLabel, gridBagConstraints);

    headingLabel.setBackground(new java.awt.Color(255, 255, 255));
    headingLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    headingLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    headingLabel.setMaximumSize(new java.awt.Dimension(32768, 25));
    headingLabel.setMinimumSize(new java.awt.Dimension(150, 25));
    headingLabel.setOpaque(true);
    headingLabel.setPreferredSize(new java.awt.Dimension(200, 25));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(8, 2, 5, 2);
    getContentPane().add(headingLabel, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
    getContentPane().add(jSeparator2, gridBagConstraints);

    setSize(new java.awt.Dimension(386, 212));
  }
  // </editor-fold>//GEN-END:initComponents

	/** Closes the dialog */
	private void closeDialog(java.awt.event.WindowEvent evt)//GEN-FIRST:event_closeDialog
	{
		setVisible(false);
		dispose();
	}//GEN-LAST:event_closeDialog

	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		if (e.getSource() == this.closeButton)
		{
			setVisible(false);
			dispose();
		}
		else if (e.getSource() == this.timeout)
		{
			this.readThread.interrupt();
			this.readThread = null;
			this.timedOut = true;
			this.stableVersion.setText(" " + ResourceMgr.getString("LblNotAvailable"));
			this.devVersion.setText(" " + ResourceMgr.getString("LblNotAvailable"));
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
  private javax.swing.JButton closeButton;
  private javax.swing.JLabel devVersion;
  private javax.swing.JLabel devVersionLabel;
  private javax.swing.JLabel headingLabel;
  private javax.swing.JSeparator jSeparator2;
  private javax.swing.JLabel stableVersion;
  private javax.swing.JLabel stableVersionLabel;
  private javax.swing.JLabel statusLabel;
  private javax.swing.JLabel yourVersion;
  private javax.swing.JLabel yourVersionLabel;
  // End of variables declaration//GEN-END:variables

}