/*
 * ConnectionEditorPanel.java
 *
 * Created on January 25, 2002, 11:27 PM
 */

package workbench.gui.profiles;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
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
import workbench.gui.components.WbButton;
import workbench.gui.components.WbTraversalPolicy;
import workbench.interfaces.SimplePropertyEditor;
import workbench.resource.ResourceMgr;

/**
 *
 * @author  workbench@kellerer.org
 */
public class ConnectionEditorPanel 
	extends JPanel
	implements PropertyChangeListener, ActionListener
{
	private ConnectionProfile currentProfile;
	private List drivers;
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
		policy.addComponent(cbAutocommit);
		policy.addComponent(cbStorePassword);
		policy.addComponent(cbSeperateConnections);
		policy.addComponent(cbIgnoreDropErrors);
		policy.addComponent(tfWorkspaceFile);
		policy.setDefaultComponent(tfProfileName);
		
		this.setFocusCycleRoot(true);
		this.setFocusTraversalPolicy(policy);
		
		this.initEditorList();
		
		this.selectWkspButton.addActionListener(this);
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
		cbStorePassword = new BooleanPropertyEditor();
		cbSeperateConnections = new BooleanPropertyEditor();
		cbIgnoreDropErrors = new BooleanPropertyEditor();
		jSeparator1 = new javax.swing.JSeparator();
		jLabel3 = new javax.swing.JLabel();
		tfWorkspaceFile = new StringPropertyEditor();
		selectWkspButton = new javax.swing.JButton();
		manageDriversButton = new WbButton();
		extendedProps = new javax.swing.JButton();
		
		setLayout(new java.awt.GridBagLayout());
		
		setMinimumSize(new java.awt.Dimension(200, 200));
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
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(0, 4, 2, 6);
		add(tfPwd, gridBagConstraints);
		
		cbAutocommit.setText("Autocommit");
		cbAutocommit.setName("autocommit");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 5;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(2, 1, 2, 6);
		add(cbAutocommit, gridBagConstraints);
		
		lblUsername.setLabelFor(tfUserName);
		lblUsername.setText(ResourceMgr.getString(ResourceMgr.TXT_DB_USERNAME));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(0, 5, 2, 0);
		add(lblUsername, gridBagConstraints);
		
		lblPwd.setLabelFor(tfPwd);
		lblPwd.setText(ResourceMgr.getString(ResourceMgr.TXT_DB_PASSWORD));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
		add(lblPwd, gridBagConstraints);
		
		lblDriver.setLabelFor(cbDrivers);
		lblDriver.setText(ResourceMgr.getString(ResourceMgr.TXT_DB_DRIVER));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(0, 5, 2, 0);
		add(lblDriver, gridBagConstraints);
		
		lblUrl.setLabelFor(tfURL);
		lblUrl.setText(ResourceMgr.getString(ResourceMgr.TXT_DB_URL));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(0, 5, 2, 0);
		add(lblUrl, gridBagConstraints);
		
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 7;
		gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		add(jSeparator2, gridBagConstraints);
		
		cbStorePassword.setSelected(true);
		cbStorePassword.setText(ResourceMgr.getString("LabelSavePassword"));
		cbStorePassword.setToolTipText(ResourceMgr.getDescription("LabelSavePassword"));
		cbStorePassword.setName("storePassword");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 8;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 0);
		add(cbStorePassword, gridBagConstraints);
		
		cbSeperateConnections.setText(ResourceMgr.getString("LabelSeperateConnections"));
		cbSeperateConnections.setToolTipText(ResourceMgr.getDescription("LabelSeperateConnections"));
		cbSeperateConnections.setName("useSeperateConnectionPerTab");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 9;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 0);
		add(cbSeperateConnections, gridBagConstraints);
		
		cbIgnoreDropErrors.setSelected(true);
		cbIgnoreDropErrors.setText(ResourceMgr.getString("LabelIgnoreDropErrors"));
		cbIgnoreDropErrors.setToolTipText(ResourceMgr.getDescription("LabelIgnoreDropErrors"));
		cbIgnoreDropErrors.setName("ignoreDropErrors");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 10;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 0);
		add(cbIgnoreDropErrors, gridBagConstraints);
		
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 14;
		gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
		gridBagConstraints.weighty = 1.0;
		add(jSeparator1, gridBagConstraints);
		
		jLabel3.setLabelFor(tfWorkspaceFile);
		jLabel3.setText(ResourceMgr.getString("LabelOpenWksp"));
		jLabel3.setToolTipText(ResourceMgr.getDescription("LabelOpenWksp"));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 11;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(0, 5, 2, 0);
		add(jLabel3, gridBagConstraints);
		
		tfWorkspaceFile.setHorizontalAlignment(javax.swing.JTextField.LEFT);
		tfWorkspaceFile.setMaximumSize(new java.awt.Dimension(2147483647, 20));
		tfWorkspaceFile.setMinimumSize(new java.awt.Dimension(40, 20));
		tfWorkspaceFile.setName("workspaceFile");
		tfWorkspaceFile.setPreferredSize(new java.awt.Dimension(100, 20));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 11;
		gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(0, 6, 2, 29);
		add(tfWorkspaceFile, gridBagConstraints);
		
		selectWkspButton.setText("...");
		selectWkspButton.setMaximumSize(new java.awt.Dimension(26, 22));
		selectWkspButton.setMinimumSize(new java.awt.Dimension(26, 22));
		selectWkspButton.setPreferredSize(new java.awt.Dimension(26, 22));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 11;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		add(selectWkspButton, gridBagConstraints);
		
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
		gridBagConstraints.gridy = 15;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 6, 0);
		add(manageDriversButton, gridBagConstraints);
		
		extendedProps.setText(ResourceMgr.getString("LabelConnExtendedProps"));
		extendedProps.setToolTipText(ResourceMgr.getDescription("LabelConnExtendedProps"));
		extendedProps.setBorder(new javax.swing.border.CompoundBorder(new javax.swing.border.EtchedBorder(), new javax.swing.border.EmptyBorder(new java.awt.Insets(1, 6, 1, 6))));
		extendedProps.addMouseListener(new java.awt.event.MouseAdapter()
		{
			public void mouseClicked(java.awt.event.MouseEvent evt)
			{
				extendedPropsMouseClicked(evt);
			}
		});
		
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 5;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints.insets = new java.awt.Insets(1, 4, 2, 6);
		add(extendedProps, gridBagConstraints);
		
	}//GEN-END:initComponents

	private void extendedPropsMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_extendedPropsMouseClicked
	{//GEN-HEADEREND:event_extendedPropsMouseClicked
		this.editExtendedProperties();
	}//GEN-LAST:event_extendedPropsMouseClicked

	private void cbDriversItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_cbDriversItemStateChanged
	{//GEN-HEADEREND:event_cbDriversItemStateChanged
		if (this.init) return;
		if (evt.getStateChange() == ItemEvent.SELECTED)
		{
			/*
			String selected = (String)this.cbDrivers.getSelectedItem();
			if (selected != null)
			{
				DbDriver newDriver = WbManager.getInstance().getConnectionMgr().findDriver(selected);
				if (newDriver != null) this.tfURL.setText(newDriver.getSampleUrl());
			}
			*/
			DbDriver newDriver = (DbDriver)this.cbDrivers.getSelectedItem();
			if(this.currentProfile != null)
			{
				this.currentProfile.setDriverclass(newDriver.getDriverClass());
				this.currentProfile.setDriverName(newDriver.getName());
			}
			this.tfURL.setText(newDriver.getSampleUrl());
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
  		List drivers = WbManager.getInstance().getConnectionMgr().getDrivers();
    	this.setDrivers(drivers);
    }
    d.dispose();
	}//GEN-LAST:event_showDriverEditorDialog

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JCheckBox cbAutocommit;
	private javax.swing.JComboBox cbDrivers;
	private javax.swing.JCheckBox cbIgnoreDropErrors;
	private javax.swing.JCheckBox cbSeperateConnections;
	private javax.swing.JCheckBox cbStorePassword;
	private javax.swing.JButton extendedProps;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JSeparator jSeparator1;
	private javax.swing.JSeparator jSeparator2;
	private javax.swing.JLabel lblDriver;
	private javax.swing.JLabel lblPwd;
	private javax.swing.JLabel lblUrl;
	private javax.swing.JLabel lblUsername;
	private javax.swing.JButton manageDriversButton;
	private javax.swing.JButton selectWkspButton;
	private javax.swing.JTextField tfProfileName;
	private javax.swing.JPasswordField tfPwd;
	private javax.swing.JTextField tfURL;
	private javax.swing.JTextField tfUserName;
	private javax.swing.JTextField tfWorkspaceFile;
	// End of variables declaration//GEN-END:variables

	public void setDrivers(List aDriverList)
	{
		if (aDriverList != null)
		{
			this.init = true;
			Collections.sort(aDriverList, DbDriver.getDriverClassComparator());
			this.cbDrivers.setModel(new DefaultComboBoxModel(aDriverList.toArray()));
			this.init = false;
		}
	}

	public void editExtendedProperties()
	{
		if (this.currentProfile == null) return;
		Properties p = this.currentProfile.getConnectionProperties();
		ConnectionPropertiesEditor editor = new ConnectionPropertiesEditor(p);
		Dimension d = new Dimension(200,200);
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
		String filename = WbManager.getInstance().getWorkspaceFilename(SwingUtilities.getWindowAncestor(this), false, true);
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
		this.init = true;
		this.currentProfile = aProfile;
		this.initPropertyEditors();
		String drvClass = aProfile.getDriverclass();
		String name = aProfile.getDriverName();
		DbDriver drv = WbManager.getInstance().getConnectionMgr().findDriver(drvClass, name);
		cbDrivers.setSelectedItem(drv);
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

	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		if (e.getSource() == this.selectWkspButton)
		{
			this.selectWorkspace();
		}
	}
	
}
