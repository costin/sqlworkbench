/*
 * DbExplorerWindow.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2005, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: info@sql-workbench.net
 *
 */
package workbench.gui.dbobjects;

import java.awt.EventQueue;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import workbench.WbManager;
import workbench.db.ConnectionProfile;
import workbench.db.WbConnection;
import workbench.gui.WbSwingUtilities;
import workbench.gui.components.ConnectionSelector;
import workbench.interfaces.Connectable;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;

/**
 *
 * @author  info@sql-workbench.net
 */
public class DbExplorerWindow
	extends JFrame
	implements WindowListener, Connectable
{
	private DbExplorerPanel panel;
	private boolean connected;
	private static int instanceCount = 0;
	private boolean standalone;
	private ConnectionSelector connectionSelector;
	
	public DbExplorerWindow(DbExplorerPanel aPanel)
	{
		this(aPanel, null);
	}
	
	public DbExplorerWindow(DbExplorerPanel aPanel, String aProfileName)
	{
		super();
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.panel = aPanel;
		this.addWindowListener(this);
		this.getContentPane().add(this.panel);
		this.setIconImage(ResourceMgr.getImage("Database").getImage());
		this.setProfileName(aProfileName);
		this.restorePosition();
		instanceCount ++;
	}

	public void setProfileName(String aProfileName)
	{
		if (aProfileName != null)
		{
			this.setTitle(ResourceMgr.getString("TxtDbExplorerTitel") + " - [" + aProfileName + "]");
		}
		else
		{
			this.setTitle(ResourceMgr.getString("TxtDbExplorerTitel"));
		}
	}

	public void setStandalone(boolean flag)
	{
		this.standalone = flag;
		if (flag)
		{
			WbManager.getInstance().registerToolWindow(this);
			this.connectionSelector = new ConnectionSelector(this, this);
			this.connectionSelector.setPropertyKey("workbench.dbexplorer.connection.last");
			this.panel.showConnectButton(this.connectionSelector);
		}
	}
	public void selectConnection()
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				
				connectionSelector.selectConnection();
			}
		});
	}
	
  public void saveSettings()
  {
		Settings.getInstance().storeWindowPosition(this);
		Settings.getInstance().storeWindowSize(this);
		this.panel.saveSettings();
  }

	public void restorePosition()
	{
		Settings s = Settings.getInstance();

		if (!s.restoreWindowSize(this))
		{
			this.setSize(800,600);
		}

		if (!s.restoreWindowPosition(this))
		{
			WbSwingUtilities.center(this, null);
		}
	}

	public void windowActivated(WindowEvent e)
	{
	}

	public void windowClosed(WindowEvent e)
	{
		if (standalone)
		{
			WbManager.getInstance().unregisterToolWindow(this);
		}
		else
		{
			if (this.panel != null)
			{
				panel.explorerWindowClosed();
			}
		}
	}

	public void windowClosing(WindowEvent e)
	{
    this.saveSettings();
	}

	public void windowDeactivated(WindowEvent e)
	{
	}

	public void windowDeiconified(WindowEvent e)
	{
	}

	public void windowIconified(WindowEvent e)
	{
	}

	public void windowOpened(WindowEvent e)
	{
	}

	public static DbExplorerWindow showWindow()
	{
		DbExplorerPanel panel = new DbExplorerPanel(instanceCount + 1);
		panel.restoreSettings();
		DbExplorerWindow window = new DbExplorerWindow(panel);
		window.setStandalone(true);
		window.restorePosition();
		window.show();
		window.selectConnection();
		return window;
	}
	
	public void connectBegin(ConnectionProfile profile)
	{
	}
	
	public void connectCancelled()
	{
	}
	
	public void connectFailed(String error)
	{
		this.setProfileName(null);
		this.panel.setConnection(null);
	}
	
	public void connected(WbConnection conn)
	{
		this.setProfileName(conn.getProfile().getName());
		this.panel.setConnection(conn);
	}
	
	public String getConnectionId(ConnectionProfile profile)
	{
		if (this.panel == null) return "DbExplorerWindow";
		return this.panel.getId();
	}

	public void connectEnded()
	{
	}
}