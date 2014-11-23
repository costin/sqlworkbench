/*
 * ProfileSelectionDialog.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2006, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.profiles;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import workbench.db.ConnectionProfile;
import workbench.gui.WbSwingUtilities;
import workbench.gui.actions.EscAction;
import workbench.gui.components.DividerBorder;
import workbench.gui.components.WbButton;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.util.StringUtil;


/**
 *
 * @author  support@sql-workbench.net
 */
public class ProfileSelectionDialog
	extends JDialog 
	implements ActionListener, WindowListener, TreeSelectionListener
{
  private JPanel buttonPanel;
  private JButton okButton;
  private JButton cancelButton;
	private ProfileEditorPanel profiles;
	private ConnectionProfile selectedProfile;
	private boolean cancelled = false;
	private String escActionCommand;

	/** Creates new form ProfileSelectionDialog */
	public ProfileSelectionDialog(Frame parent, boolean modal)
	{
		this(parent, modal, null);
	}
	
	public ProfileSelectionDialog(Frame parent, boolean modal, String lastProfileKey)
	{
		super(parent, modal);
		initComponents(lastProfileKey);

		JRootPane root = this.getRootPane();
		root.setDefaultButton(okButton);
		InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap am = root.getActionMap();
		EscAction esc = new EscAction(this);
		escActionCommand = esc.getActionName();
		im.put(esc.getAccelerator(), esc.getActionName());
		am.put(esc.getActionName(), esc);

	}

  private void initComponents(String lastProfileKey)
  {
		profiles = new ProfileEditorPanel(lastProfileKey);
//		DividerBorder b = new DividerBorder(DividerBorder.BOTTOM + DividerBorder.RIGHT + DividerBorder.TOP, 1);
//		profiles.setBorder(b);
    buttonPanel = new JPanel();
    okButton = new WbButton(ResourceMgr.getString(ResourceMgr.TXT_OK));
		okButton.setEnabled(profiles.getSelectedProfile() != null);
		
    cancelButton = new WbButton(ResourceMgr.getString(ResourceMgr.TXT_CANCEL));

		addWindowListener(this);
    buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

    buttonPanel.add(okButton);
		okButton.addActionListener(this);

    buttonPanel.add(cancelButton);
		cancelButton.addActionListener(this);

		profiles.addListMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent evt)
			{
				profileListClicked(evt);
			}
		});
		profiles.addSelectionListener(this);

		BorderLayout bl = new BorderLayout();
		this.getContentPane().setLayout(bl);
		getContentPane().add(profiles, BorderLayout.CENTER);
    getContentPane().add(buttonPanel, BorderLayout.SOUTH);


		setTitle(ResourceMgr.getString("LblSelectProfile"));
		//this.setFocusTraversalPolicy(null);
		this.restoreSize();
  }

	/** Closes the dialog */
	private void closeDialog()
	{
		this.saveSize();
		this.profiles.saveSettings();
		this.setVisible(false);
	}

	public ConnectionProfile getSelectedProfile()
	{
		return this.selectedProfile;
	}

	public void restoreSize()
	{
		if (!Settings.getInstance().restoreWindowSize(this))
		{
			this.setSize(700,550);
		}
	}

	public void saveSize()
	{
		Settings s = Settings.getInstance();
		s.storeWindowSize(this);
	}

	public void selectProfile()
	{
		this.selectedProfile = this.profiles.getSelectedProfile();
		if (this.checkPassword())
		{
			this.cancelled = false;
			this.closeDialog();
		}
	}

	private boolean checkPassword()
	{
		if (this.selectedProfile == null) return false;
		if (this.selectedProfile.getStorePassword()) return true;
		
		String pwd = WbSwingUtilities.getUserInput(this, ResourceMgr.getString("MsgInputPwdWindowTitle"), "", true);
		if (StringUtil.isEmptyString(pwd)) return false;
		this.selectedProfile.setPassword(pwd);
		return true;
	}
	
	public void profileListClicked(MouseEvent evt)
	{
		if (evt.getButton() == MouseEvent.BUTTON1 && evt.getClickCount() == 2)
		{
			EventQueue.invokeLater(new Runnable()
			{
				public void run()
				{
					selectProfile();
				}
			});
		}
	}
	
	public void setInitialFocus()
	{
		profiles.setInitialFocus();
	}

	/** Invoked when an action occurs.
	 */
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == this.okButton)
		{
			EventQueue.invokeLater(new Runnable()
			{
				public void run()
				{
					selectProfile();
				}
			});
		}
		else if (e.getSource() == this.cancelButton ||
						e.getActionCommand().equals(escActionCommand))
		{
			this.selectedProfile = null;
			this.closeDialog();
		}
	}

	public boolean isCancelled() { return this.cancelled;	}

	public void windowActivated(WindowEvent e)
	{
	}

	public void setVisible(boolean aFlag)
	{
		super.setVisible(aFlag);
		if (aFlag)
		{
			this.setInitialFocus();
		}
	}
	
	public void windowClosed(WindowEvent e)
	{
		this.profiles.done();
	}

	public void windowClosing(WindowEvent e)
	{
		this.cancelled = true;
		this.selectedProfile = null;
    this.closeDialog();
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
		this.cancelled = true;
		this.selectedProfile = null;
		this.setInitialFocus();
	}

	public void valueChanged(TreeSelectionEvent e)
	{
		this.okButton.setEnabled(profiles.getSelectedProfile() != null);
	}

}