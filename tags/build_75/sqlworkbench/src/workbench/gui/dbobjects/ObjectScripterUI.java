/*
 * ObjectScripterUI.java
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import workbench.gui.WbSwingUtilities;
import workbench.gui.actions.CreateSnippetAction;
import workbench.gui.sql.EditorPanel;
import workbench.interfaces.ScriptGenerationMonitor;
import workbench.interfaces.Scripter;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.util.WbThread;

/**
 *
 * @author  info@sql-workbench.net
 */
public class ObjectScripterUI
	extends JPanel
	implements Runnable, WindowListener, ScriptGenerationMonitor
{
	public static final int TPYE_CREATE = 1;
	public static final int TYPE_INSERT = 2;
	public static final int TYPE_GENERATE = 3;

	private Scripter scripter;
	private Thread worker;
	private JLabel statusMessage;
	private EditorPanel editor;
	private JFrame window;

	public ObjectScripterUI(Scripter scripter)
	{
		super();
		this.scripter = scripter;
		this.scripter.setProgressMonitor(this);

		this.statusMessage = new JLabel("");
		this.statusMessage.setBorder(new EtchedBorder());
		this.statusMessage.setMaximumSize(new Dimension(32768, 22));
		this.statusMessage.setMinimumSize(new Dimension(10, 22));
		this.statusMessage.setPreferredSize(new Dimension(60, 22));
		this.setLayout(new BorderLayout());
		this.add(this.statusMessage, BorderLayout.SOUTH);
		this.editor = EditorPanel.createSqlEditor();
		CreateSnippetAction create = new CreateSnippetAction(this.editor);
		this.editor.addPopupMenuItem(create, true);
		this.add(this.editor, BorderLayout.CENTER);
	}

	private void startScripting()
	{
		this.worker = new WbThread(this,"ObjectScripter Thread");
		this.worker.start();
	}

	public void run()
	{
		String script = this.scripter.getScript();
		this.editor.setText(script);
		this.editor.setCaretPosition(0);
		this.statusMessage.setText("");
	}

	public void setCurrentObject(String aTableName)
	{
		this.statusMessage.setText(aTableName);
		this.statusMessage.repaint();
	}

	public void show(Window aParent)
	{
		if (this.window == null)
		{
			this.window = new JFrame(ResourceMgr.getString("TxtWindowTitleGeneratedScript"));
			this.window.getContentPane().setLayout(new BorderLayout());
			this.window.getContentPane().add(this, BorderLayout.CENTER);
			this.window.setIconImage(ResourceMgr.getImage("script").getImage());
			if (!Settings.getInstance().restoreWindowSize(this.window, ObjectScripterUI.class.getName()))
			{
				this.window.setSize(500,400);
			}

			if (!Settings.getInstance().restoreWindowPosition(this.window, ObjectScripterUI.class.getName()))
			{
				WbSwingUtilities.center(this.window, aParent);
			}
			this.window.addWindowListener(this);
		}
		this.window.show();
		this.startScripting();
	}

	public void windowActivated(java.awt.event.WindowEvent e)
	{
	}

	public void windowClosed(java.awt.event.WindowEvent e)
	{
	}

	public void windowClosing(java.awt.event.WindowEvent e)
	{
		if (this.worker != null)
		{
			this.worker.interrupt();
			this.scripter = null;
			this.worker = null;
		}
		Settings.getInstance().storeWindowPosition(this.window, ObjectScripterUI.class.getName());
		Settings.getInstance().storeWindowSize(this.window, ObjectScripterUI.class.getName());
		this.window.hide();
		this.window.dispose();
	}

	public void windowDeactivated(java.awt.event.WindowEvent e)
	{
	}

	public void windowDeiconified(java.awt.event.WindowEvent e)
	{
	}

	public void windowIconified(java.awt.event.WindowEvent e)
	{
	}

	public void windowOpened(java.awt.event.WindowEvent e)
	{
	}

}
