/*
 * EditConnectScriptsPanel.java
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
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JPanel;
import workbench.db.ConnectionProfile;
import workbench.gui.WbSwingUtilities;
import workbench.gui.components.ValidatingDialog;
import workbench.gui.components.WbTraversalPolicy;
import workbench.gui.sql.EditorPanel;
import workbench.interfaces.ValidatingComponent;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;

/**
 * @author support@sql-workbench.net
 */
public class EditConnectScriptsPanel
	extends JPanel
	implements ValidatingComponent 
{
	private EditorPanel postConnectEditor;
	private EditorPanel preDisconnectEditor;
	
	public EditConnectScriptsPanel(ConnectionProfile profile)
	{
		if (profile == null) throw new NullPointerException("Null profile specified!");
		
		this.setLayout(new GridBagLayout());
		
		JPanel p1 = new JPanel(new BorderLayout(0,5));
		JLabel l1 = new JLabel(ResourceMgr.getString("LblPostConnectScript"));
		postConnectEditor = EditorPanel.createSqlEditor();
		postConnectEditor.setText(profile.getPostConnectScript());
		Dimension d = new Dimension(400,200);
		postConnectEditor.setPreferredSize(d);
		p1.add(l1, BorderLayout.NORTH);
		p1.add(postConnectEditor, BorderLayout.CENTER);
		
		JPanel p2 = new JPanel(new BorderLayout(0,5));
		JLabel l2 = new JLabel(ResourceMgr.getString("LblPreDisconnectScript"));
		preDisconnectEditor = EditorPanel.createSqlEditor();
		preDisconnectEditor.setText(profile.getPreDisconnectScript());
		preDisconnectEditor.setPreferredSize(d);
		preDisconnectEditor.setCaretVisible(false);
		p2.add(preDisconnectEditor, BorderLayout.CENTER);
		p2.add(l2, BorderLayout.NORTH);

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1.0;
		c.weighty = 0.5;
		c.fill = GridBagConstraints.BOTH;
		this.add(p1, c);
		c.gridy = 1;
		c.insets = new Insets(10,0,0,0);
		this.add(p2, c);
		WbTraversalPolicy pol = new WbTraversalPolicy();
		pol.addComponent(this.postConnectEditor);
		pol.addComponent(this.preDisconnectEditor);
		pol.setDefaultComponent(this.postConnectEditor);
		this.setFocusTraversalPolicy(pol);
	}
	
	public String getPostConnectScript() 
	{
		return postConnectEditor.getText();
	}
	
	public String getPreDisconnectScript()
	{
		return preDisconnectEditor.getText();
	}
	
	public static boolean editScripts(Dialog owner, ConnectionProfile profile)
	{
		EditConnectScriptsPanel p = new EditConnectScriptsPanel(profile);
		ValidatingDialog d = new ValidatingDialog(owner, "LblEditConnScripts", p);
		Settings.getInstance().restoreWindowSize(d,"workbench.gui.connectscripts.window");
		WbSwingUtilities.center(d, owner);
		d.setVisible(true);
		Settings.getInstance().storeWindowSize(d, "workbench.gui.connectscripts.window");
		if (d.isCancelled()) return false;
		profile.setPreDisconnectScript(p.getPreDisconnectScript());
		profile.setPostConnectScript(p.getPostConnectScript());
		return true;
	}

	public boolean validateInput()
	{
		return true;
	}

	public void componentDisplayed()
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				postConnectEditor.requestFocusInWindow();
			}
		});
	}
}
