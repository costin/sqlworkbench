/*
 * ConnectionEditorPanel.java
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.swing.ComboBoxModel;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import workbench.WbManager;

import workbench.db.ConnectionMgr;
import workbench.db.ConnectionProfile;
import workbench.db.DbDriver;
import workbench.gui.WbSwingUtilities;
import workbench.gui.components.BooleanPropertyEditor;
import workbench.gui.components.ComboStringPropertyEditor;
import workbench.gui.components.FlatButton;
import workbench.gui.components.IntegerPropertyEditor;
import workbench.gui.components.PasswordPropertyEditor;
import workbench.gui.components.StringPropertyEditor;
import workbench.gui.components.TextComponentMouseListener;
import workbench.gui.components.WbButton;
import workbench.gui.components.WbCheckBoxLabel;
import workbench.gui.components.WbTraversalPolicy;
import workbench.interfaces.SimplePropertyEditor;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.util.FileDialogUtil;

/**
 *
 * @author  support@sql-workbench.net
 */
public class ConnectionEditorPanel
	extends JPanel
	implements PropertyChangeListener, ActionListener
{
	private ConnectionProfile currentProfile;
	private ProfileListModel sourceModel;
	private boolean init;
	private List editors;

	public ConnectionEditorPanel()
	{
		this.initComponents();

		WbTraversalPolicy policy = new WbTraversalPolicy();
		policy.addComponent(tfProfileName);
		policy.addComponent(cbDrivers);
		policy.addComponent(tfURL);
		policy.addComponent(tfUserName);
		policy.addComponent(tfPwd);
		policy.addComponent(tfFetchSize);
		policy.addComponent(cbAutocommit);
		policy.addComponent(cbStorePassword);
		policy.addComponent(cbSeparateConnections);
		policy.addComponent(cbIgnoreDropErrors);
		policy.addComponent(disableTableCheck);
		policy.addComponent(rollbackBeforeDisconnect);
		policy.addComponent(tfWorkspaceFile);
		policy.addComponent(confirmUpdates);
		policy.addComponent(emptyStringIsNull);
		policy.addComponent(removeComments);
		policy.setDefaultComponent(tfProfileName);

		this.setFocusCycleRoot(true);
		this.setFocusTraversalPolicy(policy);

		this.initEditorList();

		this.selectWkspButton.addActionListener(this);
		this.showPassword.addActionListener(this);
	}

	public void setFocusToTitle()
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				tfProfileName.requestFocusInWindow();
				tfProfileName.selectAll();
			}
		});
	}
	
	private void initEditorList()
	{
		this.editors = new ArrayList(10);
		initEditorList(this);
	}

	private void initEditorList(Container parent)
	{
		for (int i=0; i < parent.getComponentCount(); i++)
		{
			Component c = parent.getComponent(i);
			if (c instanceof SimplePropertyEditor)
			{
				this.editors.add(c);
				String name = c.getName();
				c.addPropertyChangeListener(name, this);
        ((SimplePropertyEditor)c).setImmediateUpdate(true);
			}
			else if (c instanceof JPanel)
			{
				initEditorList((JPanel)c);
			}
		}
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

    tfProfileName = new StringPropertyEditor();
    cbDrivers = new javax.swing.JComboBox();
    tfURL = new StringPropertyEditor();
    tfUserName = new StringPropertyEditor();
    tfPwd = new PasswordPropertyEditor();
    cbAutocommit = new BooleanPropertyEditor();
    lblUsername = new javax.swing.JLabel();
    lblPwd = new javax.swing.JLabel();
    lblDriver = new javax.swing.JLabel();
    lblUrl = new javax.swing.JLabel();
    jSeparator2 = new javax.swing.JSeparator();
    jSeparator1 = new javax.swing.JSeparator();
    manageDriversButton = new WbButton();
    extendedProps = new FlatButton();
    helpButton = new WbButton();
    tfFetchSize = new IntegerPropertyEditor();
    fetchSizeLabel = new javax.swing.JLabel();
    showPassword = new FlatButton();
    wbOptionsPanel = new javax.swing.JPanel();
    cbStorePassword = new BooleanPropertyEditor();
    disableTableCheck = new BooleanPropertyEditor();
    rollbackBeforeDisconnect = new BooleanPropertyEditor();
    confirmUpdates = new BooleanPropertyEditor();
    cbIgnoreDropErrors = new BooleanPropertyEditor();
    cbSeparateConnections = new BooleanPropertyEditor();
    emptyStringIsNull = new BooleanPropertyEditor();
    includeNull = new BooleanPropertyEditor();
    removeComments = new BooleanPropertyEditor();
    rememberExplorerSchema = new BooleanPropertyEditor();
    jPanel1 = new javax.swing.JPanel();
    tfWorkspaceFile = new StringPropertyEditor();
    selectWkspButton = new javax.swing.JButton();
    workspaceFileLabel = new javax.swing.JLabel();
    autoCommitLabel = new WbCheckBoxLabel();

    setLayout(new java.awt.GridBagLayout());

    setMinimumSize(new java.awt.Dimension(220, 200));
    tfProfileName.setHorizontalAlignment(javax.swing.JTextField.LEFT);
    tfProfileName.setName("name");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
    add(tfProfileName, gridBagConstraints);

    cbDrivers.setFocusCycleRoot(true);
    cbDrivers.setMaximumSize(new java.awt.Dimension(32767, 20));
    cbDrivers.setMinimumSize(new java.awt.Dimension(40, 20));
    cbDrivers.setName("driverclass");
    cbDrivers.setPreferredSize(new java.awt.Dimension(120, 20));
    cbDrivers.setVerifyInputWhenFocusTarget(false);
    cbDrivers.addItemListener(new java.awt.event.ItemListener()
    {
      public void itemStateChanged(java.awt.event.ItemEvent evt)
      {
        cbDriversItemStateChanged(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 2, 6);
    add(cbDrivers, gridBagConstraints);

    tfURL.setHorizontalAlignment(javax.swing.JTextField.LEFT);
    tfURL.setMaximumSize(new java.awt.Dimension(2147483647, 20));
    tfURL.setMinimumSize(new java.awt.Dimension(40, 20));
    tfURL.setName("url");
    tfURL.setPreferredSize(new java.awt.Dimension(100, 20));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 2, 6);
    add(tfURL, gridBagConstraints);

    tfUserName.setHorizontalAlignment(javax.swing.JTextField.LEFT);
    tfUserName.setToolTipText("");
    tfUserName.setMaximumSize(new java.awt.Dimension(2147483647, 20));
    tfUserName.setMinimumSize(new java.awt.Dimension(40, 20));
    tfUserName.setName("username");
    tfUserName.setPreferredSize(new java.awt.Dimension(100, 20));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 2, 6);
    add(tfUserName, gridBagConstraints);

    tfPwd.setName("password");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 2, 2);
    add(tfPwd, gridBagConstraints);

    cbAutocommit.setName("autocommit");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 1, 6, 6);
    add(cbAutocommit, gridBagConstraints);

    lblUsername.setLabelFor(tfUserName);
    lblUsername.setText(ResourceMgr.getString("LblUsername"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 2, 0);
    add(lblUsername, gridBagConstraints);

    lblPwd.setLabelFor(tfPwd);
    lblPwd.setText(ResourceMgr.getString("LblPassword"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
    add(lblPwd, gridBagConstraints);

    lblDriver.setLabelFor(cbDrivers);
    lblDriver.setText(ResourceMgr.getString("LblDriver"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 2, 0);
    add(lblDriver, gridBagConstraints);

    lblUrl.setLabelFor(tfURL);
    lblUrl.setText(ResourceMgr.getString("LblDbURL"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 2, 0);
    add(lblUrl, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
    add(jSeparator2, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 15;
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
    gridBagConstraints.weighty = 1.0;
    add(jSeparator1, gridBagConstraints);

    manageDriversButton.setText(ResourceMgr.getString("LblEditDrivers"));
    manageDriversButton.setToolTipText(ResourceMgr.getDescription("LblEditDrivers"));
    manageDriversButton.setMaximumSize(new java.awt.Dimension(220, 25));
    manageDriversButton.setMinimumSize(new java.awt.Dimension(200, 25));
    manageDriversButton.setPreferredSize(new java.awt.Dimension(180, 25));
    manageDriversButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        showDriverEditorDialog(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 17;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 6, 0);
    add(manageDriversButton, gridBagConstraints);

    extendedProps.setText(ResourceMgr.getString("LblConnExtendedProps"));
    extendedProps.setToolTipText(ResourceMgr.getDescription("LblConnExtendedProps"));
    extendedProps.addMouseListener(new java.awt.event.MouseAdapter()
    {
      public void mouseClicked(java.awt.event.MouseEvent evt)
      {
        extendedPropsMouseClicked(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 2, 5, 6);
    add(extendedProps, gridBagConstraints);

    helpButton.setText(ResourceMgr.getString("LblHelp"));
    helpButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        helpButtonActionPerformed(evt);
      }
    });

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 17;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
    add(helpButton, gridBagConstraints);

    tfFetchSize.setName("defaultFetchSize");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 2);
    add(tfFetchSize, gridBagConstraints);

    fetchSizeLabel.setText(ResourceMgr.getString("LblFetchSize"));
    fetchSizeLabel.setToolTipText(ResourceMgr.getDescription("LblFetchSize"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
    add(fetchSizeLabel, gridBagConstraints);

    showPassword.setText(ResourceMgr.getString("LblShowPassword"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 2, 2, 6);
    add(showPassword, gridBagConstraints);

    wbOptionsPanel.setLayout(new java.awt.GridBagLayout());

    cbStorePassword.setSelected(true);
    cbStorePassword.setText(ResourceMgr.getString("LblSavePassword"));
    cbStorePassword.setToolTipText(ResourceMgr.getDescription("LblSavePassword"));
    cbStorePassword.setName("storePassword");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
    wbOptionsPanel.add(cbStorePassword, gridBagConstraints);

    disableTableCheck.setText(ResourceMgr.getString("LblDisableAutoTableCheck"));
    disableTableCheck.setToolTipText(ResourceMgr.getDescription("LblDisableAutoTableCheck"));
    disableTableCheck.setName("disableUpdateTableCheck");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
    wbOptionsPanel.add(disableTableCheck, gridBagConstraints);

    rollbackBeforeDisconnect.setText(ResourceMgr.getString("LblRollbackBeforeDisconnect"));
    rollbackBeforeDisconnect.setToolTipText(ResourceMgr.getDescription("LblRollbackBeforeDisconnect"));
    rollbackBeforeDisconnect.setName("rollbackBeforeDisconnect");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
    wbOptionsPanel.add(rollbackBeforeDisconnect, gridBagConstraints);

    confirmUpdates.setText(ResourceMgr.getString("LblConfirmDbUpdates"));
    confirmUpdates.setToolTipText(ResourceMgr.getDescription("LblConfirmDbUpdates"));
    confirmUpdates.setName("confirmUpdates");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
    wbOptionsPanel.add(confirmUpdates, gridBagConstraints);

    cbIgnoreDropErrors.setSelected(true);
    cbIgnoreDropErrors.setText(ResourceMgr.getString("LblIgnoreDropErrors"));
    cbIgnoreDropErrors.setToolTipText(ResourceMgr.getDescription("LblIgnoreDropErrors"));
    cbIgnoreDropErrors.setName("ignoreDropErrors");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
    wbOptionsPanel.add(cbIgnoreDropErrors, gridBagConstraints);

    cbSeparateConnections.setText(ResourceMgr.getString("LblSeperateConnections"));
    cbSeparateConnections.setToolTipText(ResourceMgr.getDescription("LblSeperateConnections"));
    cbSeparateConnections.setName("useSeparateConnectionPerTab");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
    wbOptionsPanel.add(cbSeparateConnections, gridBagConstraints);

    emptyStringIsNull.setText(ResourceMgr.getString("LblEmptyStringIsNull"));
    emptyStringIsNull.setToolTipText(ResourceMgr.getDescription("LblEmptyStringIsNull"));
    emptyStringIsNull.setName("emptyStringIsNull");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
    wbOptionsPanel.add(emptyStringIsNull, gridBagConstraints);

    includeNull.setText(ResourceMgr.getString("LblIncludeNullInInsert"));
    includeNull.setToolTipText(ResourceMgr.getDescription("LblIncludeNullInInsert"));
    includeNull.setName("includeNullInInsert");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
    wbOptionsPanel.add(includeNull, gridBagConstraints);

    removeComments.setText(ResourceMgr.getString("LblRemoveComments"));
    removeComments.setToolTipText(ResourceMgr.getDescription("LblRemoveComments"));
    removeComments.setName("removeComments");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
    wbOptionsPanel.add(removeComments, gridBagConstraints);

    rememberExplorerSchema.setText(ResourceMgr.getString("LblRememberSchema"));
    rememberExplorerSchema.setToolTipText(ResourceMgr.getDescription("LblRememberSchema"));
    rememberExplorerSchema.setName("storeExplorerSchema");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
    wbOptionsPanel.add(rememberExplorerSchema, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 9;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.gridheight = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(3, 0, 5, 6);
    add(wbOptionsPanel, gridBagConstraints);

    jPanel1.setLayout(new java.awt.GridBagLayout());

    tfWorkspaceFile.setHorizontalAlignment(javax.swing.JTextField.LEFT);
    tfWorkspaceFile.setToolTipText("");
    tfWorkspaceFile.setMaximumSize(new java.awt.Dimension(2147483647, 20));
    tfWorkspaceFile.setMinimumSize(new java.awt.Dimension(40, 20));
    tfWorkspaceFile.setName("workspaceFile");
    tfWorkspaceFile.setPreferredSize(new java.awt.Dimension(100, 20));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    jPanel1.add(tfWorkspaceFile, gridBagConstraints);

    selectWkspButton.setText("...");
    selectWkspButton.setMaximumSize(new java.awt.Dimension(26, 22));
    selectWkspButton.setMinimumSize(new java.awt.Dimension(26, 22));
    selectWkspButton.setPreferredSize(new java.awt.Dimension(26, 22));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
    jPanel1.add(selectWkspButton, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 13;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 6);
    add(jPanel1, gridBagConstraints);

    workspaceFileLabel.setLabelFor(tfWorkspaceFile);
    workspaceFileLabel.setText(ResourceMgr.getString("LblOpenWksp"));
    workspaceFileLabel.setToolTipText(ResourceMgr.getDescription("LblOpenWksp"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 13;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
    add(workspaceFileLabel, gridBagConstraints);

    autoCommitLabel.setLabelFor(cbAutocommit);
    autoCommitLabel.setText("Autocommit");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 3, 0);
    add(autoCommitLabel, gridBagConstraints);

  }// </editor-fold>//GEN-END:initComponents

	private void helpButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_helpButtonActionPerformed
	{//GEN-HEADEREND:event_helpButtonActionPerformed
		//HtmlViewer viewer = new HtmlViewer((JDialog)SwingUtilities.getWindowAncestor(this));
		//viewer.showProfileHelp();
		WbManager.getInstance().showHelpForProfiles((JDialog)SwingUtilities.getWindowAncestor(this));
	}//GEN-LAST:event_helpButtonActionPerformed

	private void extendedPropsMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_extendedPropsMouseClicked
	{//GEN-HEADEREND:event_extendedPropsMouseClicked
		this.editExtendedProperties();
	}//GEN-LAST:event_extendedPropsMouseClicked

	private void cbDriversItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_cbDriversItemStateChanged
	{//GEN-HEADEREND:event_cbDriversItemStateChanged
		if (this.init) return;
		if (evt.getStateChange() == ItemEvent.SELECTED)
		{
			String oldDriver = null;
			DbDriver newDriver = null;
			try
			{
				oldDriver = this.currentProfile.getDriverclass();
				newDriver = (DbDriver)this.cbDrivers.getSelectedItem();
				if(this.currentProfile != null)
				{
					this.currentProfile.setDriverclass(newDriver.getDriverClass());
					this.currentProfile.setDriverName(newDriver.getName());
				}
				if (oldDriver == null || !oldDriver.equals(newDriver.getDriverClass()))
				{
					this.tfURL.setText(newDriver.getSampleUrl());
				}
			}
			catch (Exception e)
			{
				LogMgr.logError("ConnectionProfilePanel.cbDriversItemStateChanged()", "Error changing driver", e);
			}

			if (!newDriver.canReadLibrary())
			{
				EventQueue.invokeLater(
					new Runnable()
					{
						public void run()
						{
							if (WbSwingUtilities.getYesNo(ConnectionEditorPanel.this, ResourceMgr.getString("MsgDriverLibraryNotReadable")))
							{
								showDriverEditorDialog(null);
							}
						}
					});
			}
		}
	}//GEN-LAST:event_cbDriversItemStateChanged

	private void showDriverEditorDialog(java.awt.event.ActionEvent evt)//GEN-FIRST:event_showDriverEditorDialog
	{//GEN-HEADEREND:event_showDriverEditorDialog
		final Frame parent = (Frame)(SwingUtilities.getWindowAncestor(this)).getParent();
		DbDriver drv = (DbDriver)cbDrivers.getSelectedItem();
		final String drvName;
		if (drv != null)
		{
			drvName = drv.getName();
		}
		else
		{
			drvName = null;
		}
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				DriverEditorDialog d = new DriverEditorDialog(parent);
				d.setDriverName(drvName);
				WbSwingUtilities.center(d,parent);
				d.setVisible(true);
				if (!d.isCancelled())
				{
					List drivers = ConnectionMgr.getInstance().getDrivers();
					setDrivers(drivers);
				}
				d.dispose();
			}
		});

	}//GEN-LAST:event_showDriverEditorDialog

  // Variables declaration - do not modify//GEN-BEGIN:variables
  protected javax.swing.JLabel autoCommitLabel;
  protected javax.swing.JCheckBox cbAutocommit;
  protected javax.swing.JComboBox cbDrivers;
  protected javax.swing.JCheckBox cbIgnoreDropErrors;
  protected javax.swing.JCheckBox cbSeparateConnections;
  protected javax.swing.JCheckBox cbStorePassword;
  protected javax.swing.JCheckBox confirmUpdates;
  protected javax.swing.JCheckBox disableTableCheck;
  protected javax.swing.JCheckBox emptyStringIsNull;
  protected javax.swing.JButton extendedProps;
  protected javax.swing.JLabel fetchSizeLabel;
  protected javax.swing.JButton helpButton;
  protected javax.swing.JCheckBox includeNull;
  protected javax.swing.JPanel jPanel1;
  protected javax.swing.JSeparator jSeparator1;
  protected javax.swing.JSeparator jSeparator2;
  protected javax.swing.JLabel lblDriver;
  protected javax.swing.JLabel lblPwd;
  protected javax.swing.JLabel lblUrl;
  protected javax.swing.JLabel lblUsername;
  protected javax.swing.JButton manageDriversButton;
  protected javax.swing.JCheckBox rememberExplorerSchema;
  protected javax.swing.JCheckBox removeComments;
  protected javax.swing.JCheckBox rollbackBeforeDisconnect;
  protected javax.swing.JButton selectWkspButton;
  protected javax.swing.JButton showPassword;
  protected javax.swing.JTextField tfFetchSize;
  protected javax.swing.JTextField tfProfileName;
  protected javax.swing.JPasswordField tfPwd;
  protected javax.swing.JTextField tfURL;
  protected javax.swing.JTextField tfUserName;
  protected javax.swing.JTextField tfWorkspaceFile;
  protected javax.swing.JPanel wbOptionsPanel;
  protected javax.swing.JLabel workspaceFileLabel;
  // End of variables declaration//GEN-END:variables

	public void setDrivers(List aDriverList)
	{
		if (aDriverList != null)
		{
			this.init = true;
			Object currentDriver = this.cbDrivers.getSelectedItem();
			try
			{
				Collections.sort(aDriverList, DbDriver.getDriverClassComparator());
				this.cbDrivers.setModel(new DefaultComboBoxModel(aDriverList.toArray()));
				if (currentDriver != null)
				{
					this.cbDrivers.setSelectedItem(currentDriver);
				}
			}
			catch (Exception e)
			{
				LogMgr.logError("ConnectionEditorPanel.setDrivers()", "Error when setting new driver list", e);
			}
			finally
			{
				this.init = false;
			}

		}
	}

	public void editExtendedProperties()
	{
		if (this.currentProfile == null) return;
		Properties p = this.currentProfile.getConnectionProperties();
		ConnectionPropertiesEditor editor = new ConnectionPropertiesEditor(p);
		Dimension d = new Dimension(300,250);
		editor.setMinimumSize(d);
		editor.setPreferredSize(d);

		int choice = JOptionPane.showConfirmDialog(this, editor, ResourceMgr.getString("TxtEditConnPropsWindowTitle"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (choice == JOptionPane.OK_OPTION)
		{
			this.currentProfile.setConnectionProperties(editor.getProperties());
		}
	}

	public void selectWorkspace()
	{
		FileDialogUtil util = new FileDialogUtil();
		String filename = util.getWorkspaceFilename(SwingUtilities.getWindowAncestor(this), false, true);
		if (filename == null) return;
		this.tfWorkspaceFile.setText(filename);
	}

	public void setSourceList(ProfileListModel aSource)
	{
		this.sourceModel = aSource;
	}

	public void updateProfile()
	{
		if (this.init) return;
		if (this.currentProfile == null) return;
		if (this.editors == null) return;
		boolean changed = false;

		for (int i=0; i < this.editors.size(); i++)
		{
			SimplePropertyEditor editor = (SimplePropertyEditor)this.editors.get(i);
			changed = changed || editor.isChanged();
			editor.applyChanges();
		}

		if (changed)
		{
			this.sourceModel.profileChanged(this.currentProfile);
		}
	}

	public ConnectionProfile getProfile()
	{
		this.updateProfile();
		return this.currentProfile;
	}

	private void initPropertyEditors()
	{
		if (this.editors == null) return;
		if (this.currentProfile == null) return;

		for (int i=0; i < this.editors.size(); i++)
		{
			SimplePropertyEditor editor = (SimplePropertyEditor)this.editors.get(i);
			Component c = (Component)editor;
			String property = c.getName();
			if (property != null)
			{
				editor.setSourceObject(this.currentProfile, property);
			}
		}
	}

	public void setProfile(ConnectionProfile aProfile)
	{
		if (aProfile == null) return;

		this.currentProfile = aProfile;
		//System.out.println("switching to profile: " + aProfile.getName() + "(" + aProfile.getIdentifier() + ")");
		this.initPropertyEditors();

		String drvClass = aProfile.getDriverclass();
		DbDriver drv = null;
		if (drvClass != null)
		{
			String name = aProfile.getDriverName();
			drv = ConnectionMgr.getInstance().findDriverByName(drvClass, name);
		}

		try
		{
			this.init = true;
			cbDrivers.setSelectedItem(drv);
		}
		catch (Exception e)
		{
			LogMgr.logError("ConnectionEditorPanel.setProfile()", "Error setting profile", e);
		}
		finally
		{
			this.init = false;
		}
	}

	/** This method gets called when a bound property is changed.
	 * @param evt A PropertyChangeEvent object describing the event source
	 * and the property that has changed.
	 *
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (evt.getSource() instanceof SimplePropertyEditor)
		{
			if (!this.init)	this.sourceModel.profileChanged(this.currentProfile);
		}
	}
	
	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		if (e.getSource() == this.selectWkspButton)
		{
			this.selectWorkspace();
		}
		else if (e.getSource() == this.showPassword)
		{
			String pwd = this.getProfile().getInputPassword();
			String title = ResourceMgr.getString("LblCurrentPassword");
			title += " " + this.getProfile().getUsername();
			JTextField f = new JTextField();
			f.setDisabledTextColor(Color.BLACK);
			f.setEditable(false);
			f.setText(pwd);
			Border b = new CompoundBorder(new LineBorder(Color.LIGHT_GRAY), new EmptyBorder(2,2,2,2));
			f.setBorder(b);
			TextComponentMouseListener l = new TextComponentMouseListener();
			f.addMouseListener(l);
			//WbSwingUtilities.showMessage(this, f);
			JOptionPane.showMessageDialog(this.getParent(), f, title, JOptionPane.PLAIN_MESSAGE);
		}
	}

}