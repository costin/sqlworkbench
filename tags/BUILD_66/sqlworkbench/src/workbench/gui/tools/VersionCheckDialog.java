/*
 * VersionCheckDialog.java
 *
 * Created on January 6, 2004, 5:13 PM
 */

package workbench.gui.tools;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import workbench.gui.components.DividerBorder;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.util.WbVersionReader;

/**
 *
 * @author  workbench@kellerer.org
 */
public class VersionCheckDialog extends javax.swing.JDialog
	implements ActionListener
{
	private Thread readThread;
	private Timer timeout;
	private boolean timedOut = false;
	private boolean isDevVersion =  false;
	private WbVersionReader versionReader;
	
	/** Creates new form VersionCheckDialog */
	public VersionCheckDialog(java.awt.Frame parent, boolean modal)
	{
		super(parent, modal);
		initComponents();
		this.closeButton.addActionListener(this);
		Font f = this.headingLabel.getFont().deriveFont(Font.BOLD);
		this.headingLabel.setFont(f);
		this.headingLabel.setText(ResourceMgr.getString("LabelReadingVersion"));
	}

	public void startRetrieveVersions()
	{
		this.readThread = new Thread()
		{
			public void run()
			{
				readVersion();
			}
		};
		readThread.setName("WbVersionCheck Thread");
		readThread.setDaemon(true);
		readThread.start();
		
		this.timeout = new Timer(60 * 1000, this);
		this.timedOut = false;
		this.timeout.start();
	}
	
	private void readVersion()
	{
		try
		{
			LogMgr.logDebug("VersionCheckDialog.readVersion()", "Retrieving versions from the website...");
			this.versionReader = new WbVersionReader();
			if (!this.timedOut)
			{
				this.stableVersion.setText(" " + ResourceMgr.getString("TxtBuild") + " " + this.versionReader.getStableBuildNumber() + " (" + this.versionReader.getStableBuildDate() + ")");
				this.devVersion.setText(" " + this.versionReader.getDevBuildDate());
			}
			else
			{
				this.stableVersion.setText(" " + ResourceMgr.getString("LabelVersionNotAvailable"));
				this.devVersion.setText(" " + ResourceMgr.getString("LabelVersionNotAvailable"));
			}
			checkDisplay();
		}
		catch (Exception e)
		{
			this.stableVersion.setText(" " + ResourceMgr.getString("LabelVersionNotAvailable"));
			this.devVersion.setText(" " + ResourceMgr.getString("LabelVersionNotAvailable"));
		}
		finally
		{
			if (this.timeout != null)
			{
				this.timeout.stop();
				this.timeout = null;
			}
			this.headingLabel.setText(ResourceMgr.getString("LabelVersionsAvailable"));
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
		
		String msg = ResourceMgr.getString("LabelVersionUpToDate");
		
		if (lastStable.getTime() > current.getTime())
		{
			this.stableVersion.setBackground(Color.YELLOW);
			msg = ResourceMgr.getString("LabelVersionNewStableAvailable");
		}
		else if (last.getTime() > current.getTime())
		{
			this.devVersion.setBackground(Color.YELLOW);
			msg = ResourceMgr.getString("LabelVersionNewDevAvailable");
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

		String msg = ResourceMgr.getString("LabelVersionUpToDate");
		if (releaseVersion > current)
		{
			this.stableVersion.setBackground(Color.YELLOW);
			msg = ResourceMgr.getString("LabelVersionNewStableAvailable");
		}
		this.statusLabel.setText(msg);
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
  private void initComponents()//GEN-BEGIN:initComponents
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

    yourVersionLabel.setText(ResourceMgr.getString("LabelYourVersion"));
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

    closeButton.setText(ResourceMgr.getString("LabelClose"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.insets = new java.awt.Insets(10, 0, 5, 5);
    gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
    gridBagConstraints.weighty = 1.0;
    getContentPane().add(closeButton, gridBagConstraints);

    stableVersionLabel.setText(ResourceMgr.getString("LabelStableVersion"));
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

    devVersionLabel.setText(ResourceMgr.getString("LabelDevVersion"));
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
    headingLabel.setMaximumSize(new java.awt.Dimension(32768, 25));
    headingLabel.setMinimumSize(new java.awt.Dimension(150, 25));
    headingLabel.setPreferredSize(new java.awt.Dimension(200, 25));
    headingLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    headingLabel.setOpaque(true);
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
  }//GEN-END:initComponents
	
	/** Closes the dialog */
	private void closeDialog(java.awt.event.WindowEvent evt)//GEN-FIRST:event_closeDialog
	{
		setVisible(false);
		dispose();
	}//GEN-LAST:event_closeDialog
	
	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[])
	{
		new VersionCheckDialog(new javax.swing.JFrame(), true).show();
	}
	
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
			this.stableVersion.setText(" " + ResourceMgr.getString("LabelVersionNotAvailable"));
			this.devVersion.setText(" " + ResourceMgr.getString("LabelVersionNotAvailable"));
		}
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
