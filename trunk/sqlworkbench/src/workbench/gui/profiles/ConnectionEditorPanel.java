/*
 * ConnectionEditorPanel.java
 *
 * Created on January 25, 2002, 11:27 PM
 */

package workbench.gui.profiles;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import workbench.WbManager;
import workbench.db.ConnectionProfile;
import workbench.db.DbDriver;
import workbench.gui.WbSwingUtilities;
import workbench.gui.components.BooleanPropertyEditor;
import workbench.gui.components.ComboStringPropertyEditor;
import workbench.gui.components.PasswordPropertyEditor;
import workbench.gui.components.StringPropertyEditor;
import workbench.gui.components.TextComponentMouseListener;
import workbench.gui.components.WbTraversalPolicy;
import workbench.interfaces.SimplePropertyEditor;
import workbench.resource.ResourceMgr;

/**
 *
 * @author  workbench@kellerer.org
 */
public class ConnectionEditorPanel 
	extends JPanel
	implements PropertyChangeListener, FocusListener
{
	private ConnectionProfile currentProfile;
	private List drivers;
	private ProfileListModel sourceModel;
	private boolean init;
	private List editors;
	
	public ConnectionEditorPanel()
	{
		this.initComponents();
		this.initEditorList();
		
		// we only monitor changes to the name, because that needs
		// to be updated immediately on the list. The other
		// property are only updated when the parent requests this
		//this.tfProfileName.addPropertyChangeListener(tfProfileName.getName(), this);
		WbTraversalPolicy policy = new WbTraversalPolicy();
		policy.addComponent(tfProfileName);
		policy.addComponent(cbDrivers);
		policy.addComponent(tfURL);
		policy.addComponent(tfUserName);
		policy.addComponent(tfPwd);
		policy.addComponent(cbAutocommit);
		policy.addComponent(cbStorePassword);
		policy.addComponent(cbSeperateConnections);
		policy.setDefaultComponent(tfProfileName);
		this.setFocusTraversalPolicy(policy);
		this.setFocusCycleRoot(true);
		//this.addFocusListener(this);
	}
	
	private void initEditorList()
	{
		this.editors = new ArrayList(10);
		for (int i=0; i < this.getComponentCount(); i++)
		{
			Component c = this.getComponent(i);
			if (c instanceof SimplePropertyEditor)
			{
				this.editors.add(c);
				c.addPropertyChangeListener(c.getName(), this);
        ((SimplePropertyEditor)c).setImmediateUpdate(true);
			}
		}
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	private void initComponents()//GEN-BEGIN:initComponents
	{
		java.awt.GridBagConstraints gridBagConstraints;
		
		lblUsername = new javax.swing.JLabel();
		tfUserName = new StringPropertyEditor();
		lblPwd = new javax.swing.JLabel();
		jLabel1 = new javax.swing.JLabel();
		cbDrivers = new ComboStringPropertyEditor();
		jLabel2 = new javax.swing.JLabel();
		tfURL = new StringPropertyEditor();
		tfPwd = new PasswordPropertyEditor();
		cbAutocommit = new BooleanPropertyEditor();
		tfProfileName = new StringPropertyEditor();
		cbStorePassword = new BooleanPropertyEditor();
		cbSeperateConnections = new BooleanPropertyEditor();
		jSeparator1 = new javax.swing.JSeparator();
		manageDriversButton = new javax.swing.JButton();
		
		setLayout(new java.awt.GridBagLayout());
		
		setMinimumSize(new java.awt.Dimension(200, 200));
		lblUsername.setText(ResourceMgr.getString(ResourceMgr.TXT_DB_USERNAME));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new java.awt.Insets(0, 5, 2, 0);
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		add(lblUsername, gridBagConstraints);
		
		tfUserName.setHorizontalAlignment(javax.swing.JTextField.LEFT);
		tfUserName.setToolTipText("");
		tfUserName.setMaximumSize(new java.awt.Dimension(2147483647, 20));
		tfUserName.setMinimumSize(new java.awt.Dimension(40, 20));
		tfUserName.setName("username");
		tfUserName.setPreferredSize(new java.awt.Dimension(100, 20));
		tfUserName.addMouseListener(new TextComponentMouseListener());
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new java.awt.Insets(0, 4, 2, 6);
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		add(tfUserName, gridBagConstraints);
		
		lblPwd.setText(ResourceMgr.getString(ResourceMgr.TXT_DB_PASSWORD));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		add(lblPwd, gridBagConstraints);
		
		jLabel1.setText(ResourceMgr.getString(ResourceMgr.TXT_DB_DRIVER));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new java.awt.Insets(0, 5, 2, 0);
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		add(jLabel1, gridBagConstraints);
		
		cbDrivers.setEditable(true);
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
		gridBagConstraints.insets = new java.awt.Insets(0, 4, 2, 6);
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.weightx = 0.5;
		add(cbDrivers, gridBagConstraints);
		
		jLabel2.setText(ResourceMgr.getString(ResourceMgr.TXT_DB_URL));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new java.awt.Insets(0, 5, 2, 0);
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		add(jLabel2, gridBagConstraints);
		
		tfURL.setHorizontalAlignment(javax.swing.JTextField.LEFT);
		tfURL.setMaximumSize(new java.awt.Dimension(2147483647, 20));
		tfURL.setMinimumSize(new java.awt.Dimension(40, 20));
		tfURL.setName("url");
		tfURL.setPreferredSize(new java.awt.Dimension(100, 20));
		tfURL.addMouseListener(new TextComponentMouseListener());
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new java.awt.Insets(0, 4, 2, 6);
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		add(tfURL, gridBagConstraints);
		
		tfPwd.setName("password");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new java.awt.Insets(0, 4, 2, 6);
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		add(tfPwd, gridBagConstraints);
		
		cbAutocommit.setText("Autocommit");
		cbAutocommit.setName("autocommit");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 5;
		gridBagConstraints.insets = new java.awt.Insets(0, 4, 2, 6);
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		add(cbAutocommit, gridBagConstraints);
		
		tfProfileName.setHorizontalAlignment(javax.swing.JTextField.LEFT);
		tfProfileName.setName("name");
		tfProfileName.addMouseListener(new TextComponentMouseListener());
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new java.awt.Insets(10, 5, 5, 5);
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.weightx = 1.0;
		add(tfProfileName, gridBagConstraints);
		
		cbStorePassword.setSelected(true);
		cbStorePassword.setText(ResourceMgr.getString("LabelSavePassword"));
		cbStorePassword.setToolTipText(ResourceMgr.getDescription("LabelSavePassword"));
		cbStorePassword.setName("storePassword");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 6;
		gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		add(cbStorePassword, gridBagConstraints);
		
		cbSeperateConnections.setText(ResourceMgr.getString("LabelSeperateConnections"));
		cbSeperateConnections.setToolTipText(ResourceMgr.getDescription("LabelSeperateConnections"));
		cbSeperateConnections.setName("useSeperateConnectionPerTab");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 7;
		gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		add(cbSeperateConnections, gridBagConstraints);
		
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 8;
		gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
		gridBagConstraints.weighty = 1.0;
		add(jSeparator1, gridBagConstraints);
		
		manageDriversButton.setText(ResourceMgr.getString("LabelEditDrivers"));
		manageDriversButton.setToolTipText(ResourceMgr.getDescription("EditDrivers"));
		manageDriversButton.setMaximumSize(new java.awt.Dimension(100, 25));
		manageDriversButton.setMinimumSize(new java.awt.Dimension(70, 25));
		manageDriversButton.setPreferredSize(new java.awt.Dimension(100, 25));
		manageDriversButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				showDriverEditorDialog(evt);
			}
		});
		
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 9;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 6, 0);
		gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
		add(manageDriversButton, gridBagConstraints);
		
	}//GEN-END:initComponents

	private void cbDriversItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_cbDriversItemStateChanged
	{//GEN-HEADEREND:event_cbDriversItemStateChanged
		if (this.init) return;
		if (evt.getStateChange() == ItemEvent.SELECTED)
		{
			String selected = (String)this.cbDrivers.getSelectedItem();
			if (selected != null)
			{
				DbDriver newDriver = WbManager.getInstance().getConnectionMgr().findDriver(selected);
				if (newDriver != null) this.tfURL.setText(newDriver.getSampleUrl());
			}
		}
	}//GEN-LAST:event_cbDriversItemStateChanged

	private void showDriverEditorDialog(java.awt.event.ActionEvent evt)//GEN-FIRST:event_showDriverEditorDialog
	{//GEN-HEADEREND:event_showDriverEditorDialog
		// not really nice, but works until the driver editor can be
		// called from a different location...
		Frame parent = (Frame)(SwingUtilities.getWindowAncestor(this)).getParent();
		DriverEditorDialog d = new DriverEditorDialog(parent, true);
		WbSwingUtilities.center(d,parent);
		d.show();
    if (!d.isCancelled())
    {
  		List drivers = WbManager.getInstance().getConnectionMgr().getDriverClasses();
    	this.setDrivers(drivers);
    }
    d.dispose();
	}//GEN-LAST:event_showDriverEditorDialog

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JLabel jLabel1;
	private javax.swing.JTextField tfURL;
	private javax.swing.JLabel lblUsername;
	private javax.swing.JTextField tfProfileName;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JPasswordField tfPwd;
	private javax.swing.JCheckBox cbAutocommit;
	private javax.swing.JButton manageDriversButton;
	private javax.swing.JCheckBox cbStorePassword;
	private javax.swing.JSeparator jSeparator1;
	private javax.swing.JLabel lblPwd;
	private javax.swing.JTextField tfUserName;
	private javax.swing.JCheckBox cbSeperateConnections;
	private javax.swing.JComboBox cbDrivers;
	// End of variables declaration//GEN-END:variables

	public void setDrivers(List aDriverList)
	{
		if (aDriverList != null)
		{
			this.init = true;
			this.cbDrivers.setModel(new DefaultComboBoxModel(aDriverList.toArray()));
			this.init = false;
		}
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
		this.init = true;
		this.currentProfile = aProfile;
		this.initPropertyEditors();
		this.init = false;
	}

	/** This method gets called when a bound property is changed.
	 * @param evt A PropertyChangeEvent object describing the event source
	 *   	and the property that has changed.
	 *
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		//this.updateProfile();
    if (!this.init)	this.sourceModel.profileChanged(this.currentProfile);
	}	

	/** Invoked when a component gains the keyboard focus.
	 *
	 */
	public void focusGained(FocusEvent e)
	{
	}
	
	/** Invoked when a component loses the keyboard focus.
	 *
	 */
	public void focusLost(FocusEvent e)
	{
		this.updateProfile();
	}
	
}
