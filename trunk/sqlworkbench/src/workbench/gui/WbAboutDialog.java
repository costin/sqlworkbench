/*
 * WbAboutDialog.java
 *
 * Created on 17. Juli 2002, 21:37
 */

package workbench.gui;

import workbench.resource.ResourceMgr;

/**
 *
 * @author  sql.workbench@freenet.de
 */
public class WbAboutDialog extends javax.swing.JDialog
{
	
	/** Creates new form WbAboutDialog */
	public WbAboutDialog(java.awt.Frame parent, boolean modal)
	{
		super(parent, modal);
		initComponents();
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	private void initComponents()//GEN-BEGIN:initComponents
	{
		java.awt.GridBagConstraints gridBagConstraints;
		
		buttonPanel = new javax.swing.JPanel();
		closeButton = new javax.swing.JButton();
		contentPanel = new javax.swing.JPanel();
		logo = new javax.swing.JLabel();
		labelTitel = new javax.swing.JLabel();
		labelDesc = new javax.swing.JLabel();
		labelVersion = new javax.swing.JLabel();
		labelContact = new javax.swing.JLabel();
		
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle(ResourceMgr.getString("TxtAbout") + " " + ResourceMgr.TXT_PRODUCT_NAME);
		setName("AboutDialog");
		setResizable(false);
		addWindowListener(new java.awt.event.WindowAdapter()
		{
			public void windowClosing(java.awt.event.WindowEvent evt)
			{
				closeDialog(evt);
			}
		});
		
		buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
		
		closeButton.setFont(null);
		closeButton.setText(ResourceMgr.getString("LabelClose"));
		closeButton.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				closeButtonActionPerformed(evt);
			}
		});
		
		buttonPanel.add(closeButton);
		
		getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);
		
		contentPanel.setLayout(new java.awt.GridBagLayout());
		
		logo.setFont(null);
		logo.setIcon(ResourceMgr.getPicture("greenguy"));
		logo.setBorder(new javax.swing.border.EtchedBorder());
		logo.setIconTextGap(0);
		logo.setMaximumSize(new java.awt.Dimension(129, 138));
		logo.setMinimumSize(new java.awt.Dimension(129, 138));
		logo.setPreferredSize(new java.awt.Dimension(129, 138));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridheight = 4;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
		contentPanel.add(logo, gridBagConstraints);
		
		labelTitel.setFont(new java.awt.Font("Dialog", 1, 14));
		labelTitel.setText(ResourceMgr.TXT_PRODUCT_NAME);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new java.awt.Insets(6, 3, 0, 5);
		contentPanel.add(labelTitel, gridBagConstraints);
		
		labelDesc.setFont(null);
		labelDesc.setText(ResourceMgr.getString("TxtProductDescription"));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = new java.awt.Insets(2, 3, 0, 5);
		contentPanel.add(labelDesc, gridBagConstraints);
		
		labelVersion.setFont(null);
		labelVersion.setText(ResourceMgr.getString("TxtVersion"));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = new java.awt.Insets(2, 3, 0, 5);
		contentPanel.add(labelVersion, gridBagConstraints);
		
		labelContact.setFont(null);
		labelContact.setForeground(java.awt.Color.blue);
		labelContact.setText(ResourceMgr.getString("TxtContact"));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = new java.awt.Insets(12, 3, 0, 5);
		contentPanel.add(labelContact, gridBagConstraints);
		
		getContentPane().add(contentPanel, java.awt.BorderLayout.CENTER);
		
		pack();
	}//GEN-END:initComponents

	private void closeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_closeButtonActionPerformed
	{//GEN-HEADEREND:event_closeButtonActionPerformed
		this.closeDialog(null);
	}//GEN-LAST:event_closeButtonActionPerformed
	
	/** Closes the dialog */
	private void closeDialog(java.awt.event.WindowEvent evt)
	{//GEN-FIRST:event_closeDialog
		setVisible(false);
		dispose();
	}//GEN-LAST:event_closeDialog
	
	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[])
	{
		new WbAboutDialog(new javax.swing.JFrame(), true).show();
	}
	
	
	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JLabel labelTitel;
	private javax.swing.JPanel buttonPanel;
	private javax.swing.JLabel logo;
	private javax.swing.JLabel labelDesc;
	private javax.swing.JLabel labelContact;
	private javax.swing.JPanel contentPanel;
	private javax.swing.JButton closeButton;
	private javax.swing.JLabel labelVersion;
	// End of variables declaration//GEN-END:variables
	
}
