/*
 * ProfileEditor.java
 *
 * Created on 1. Juli 2002, 18:34
 */

package workbench.gui.db;

import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JToolBar;
import javax.swing.ListModel;
import javax.swing.border.Border;
import workbench.WbManager;
import workbench.db.ConnectionMgr;
import workbench.db.ConnectionProfile;
import workbench.exception.WbException;
import workbench.interfaces.FileActions;
import workbench.resource.ResourceMgr;

/**
 *
 * @author  thomas.kellerer@inline-skate.com
 */
public class ProfileEditorPanel
	extends javax.swing.JPanel
	implements FileActions
{
	//private ConnectionEditorPanel connectionEditor;
	private ProfileListModel model;
	private JToolBar toolbar;
	private int lastIndex = -1;

	/** Creates new form ProfileEditor */
	public ProfileEditorPanel()
	{
		initComponents();
		this.fillDrivers();
		String last = WbManager.getSettings().getLastConnection();
		System.out.println("selecting last profile");
		this.selectProfile(last);
		jList1.setNextFocusableComponent(connectionEditor);
		this.connectionEditor.setNextFocusableComponent(jList1);
		this.toolbar = new JToolBar();
		this.toolbar.setFloatable(false);
		this.toolbar.add(new NewProfileAction(this));
		this.toolbar.add(new SaveProfileAction(this));
		this.toolbar.addSeparator();
		this.toolbar.add(new DeleteProfileAction(this));
		this.listPanel.add(this.toolbar, BorderLayout.NORTH);
	}

	private void fillDrivers()
	{
		List drivers = WbManager.getInstance().getConnectionMgr().getDrivers();
		this.connectionEditor.setDrivers(drivers);
	}

	private void fillProfiles()
	{
		this.model = new ProfileListModel(WbManager.getInstance().getConnectionMgr().getProfiles());
		this.jList1.setModel(this.model);
	}
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	private void initComponents()//GEN-BEGIN:initComponents
	{
		jSplitPane1 = new javax.swing.JSplitPane();
		
		
		listPanel = new javax.swing.JPanel();
		jList1 = new javax.swing.JList();
		connectionEditor = new workbench.gui.db.ConnectionEditorPanel();
		
		setLayout(new java.awt.BorderLayout());
		
		jSplitPane1.setBorder(new javax.swing.border.EtchedBorder());
		jSplitPane1.setDividerLocation(100);
		jSplitPane1.setDividerSize(4);
		listPanel.setLayout(new java.awt.BorderLayout());
		
		jList1.setFont(null);
		this.fillProfiles();
		jList1.addListSelectionListener(new javax.swing.event.ListSelectionListener()
		{
			public void valueChanged(javax.swing.event.ListSelectionEvent evt)
			{
				jList1ValueChanged(evt);
			}
		});
		
		listPanel.add(jList1, java.awt.BorderLayout.CENTER);
		
		jSplitPane1.setLeftComponent(listPanel);
		
		
		connectionEditor.setFont(null);
		jSplitPane1.setRightComponent(connectionEditor);
		
		add(jSplitPane1, java.awt.BorderLayout.CENTER);
		
	}//GEN-END:initComponents

	private void jList1ValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_jList1ValueChanged
	{//GEN-HEADEREND:event_jList1ValueChanged
		if (evt.getSource() == this.jList1)
		{
			if (lastIndex > -1)
			{
				ConnectionProfile current = this.connectionEditor.getProfile();
				this.model.putProfile(lastIndex, current);
			}
			ConnectionProfile newProfile = (ConnectionProfile)this.jList1.getSelectedValue();
			this.connectionEditor.setProfile(newProfile);
			lastIndex = this.jList1.getSelectedIndex();
		}
	}//GEN-LAST:event_jList1ValueChanged

	public ConnectionProfile getSelectedProfile()
	{
		this.updateUI();
		this.connectionEditor.updateProfile();
		ConnectionProfile prof = (ConnectionProfile)jList1.getSelectedValue();
		return prof;
	}


	// Variables declaration - do not modify//GEN-BEGIN:variables
	private workbench.gui.db.ConnectionEditorPanel connectionEditor;
	private javax.swing.JSplitPane jSplitPane1;
	private javax.swing.JList jList1;
	private javax.swing.JPanel listPanel;
	// End of variables declaration//GEN-END:variables


	private void selectProfile(String aProfileName)
	{
		if (aProfileName == null) return;

		try
		{
			ListModel m = jList1.getModel();
			int count = m.getSize();

			for (int i=0; i < count; i++)
			{
				ConnectionProfile prof = (ConnectionProfile)m.getElementAt(i);
				if (prof.getName().equals(aProfileName))
				{
					this.jList1.setSelectedIndex(i);
					break;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			jList1.setSelectedIndex(0);
		}
	}

	/**
	 *	Remove an item from the listmodel
	 */
	public void deleteItem() throws WbException
	{
		int index = this.jList1.getSelectedIndex();
		if (index > 0) this.jList1.setSelectedIndex(index - 1);
		this.model.deleteProfile(index);
		this.jList1.updateUI();
	}

	/**
	 *	Create a new profile. This will only be
	 *	created in the ListModel.
	 */
	public void newItem() throws WbException
	{
		ConnectionProfile cp = new ConnectionProfile();
		cp.setName(ResourceMgr.getString("EmptyProfileName"));
		this.model.addProfile(cp);
		this.selectProfile(cp.getName());
		this.jList1.updateUI();
	}

	public void saveItem() throws WbException
	{
		ConnectionMgr conn = WbManager.getInstance().getConnectionMgr();
		conn.putProfiles(this.model.getValues());
		conn.saveXmlProfiles();
	}

}
