/*
 * DbExplorerOptionsPanel.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2009, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.settings;

import javax.swing.JPanel;
import workbench.gui.components.WbCheckBoxLabel;
import workbench.interfaces.Restoreable;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;

/**
 *
 * @author  support@sql-workbench.net
 */
public class DbExplorerOptionsPanel
	extends JPanel
	implements Restoreable
{
	public DbExplorerOptionsPanel()
	{
		super();
		initComponents();
	}

	public void saveSettings()
	{
		Settings set = Settings.getInstance();
		set.setRetrieveDbExplorer(retrieveDbExplorer.isSelected());
		set.setShowDbExplorerInMainWindow(this.showDbExplorer.isSelected());
		set.setStoreExplorerObjectType(this.rememberObject.isSelected());
		set.setAutoGeneratePKName(autogeneratePK.isSelected());
		set.setShowTriggerPanel(showTriggerPanel.isSelected());
		set.setSelectDataPanelAfterRetrieve(autoselectDataPanel.isSelected());
		set.setRememberSortInDbExplorer(rememberSort.isSelected());
		set.setShowFocusInDbExplorer(showFocus.isSelected());
		set.setDefaultExplorerObjectType(this.defTableType.getText());
		((PlacementChooser)tabPlacement).saveSelection();
	}

	public void restoreSettings()
	{
		Settings set = Settings.getInstance();
		autogeneratePK.setSelected(set.getAutoGeneratePKName());
		defTableType.setText(set.getDefaultExplorerObjectType());
		((PlacementChooser)tabPlacement).setProperty("workbench.gui.dbobjects.tabletabs");
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    showDbExplorer = new javax.swing.JCheckBox();
    showTriggerPanel = new javax.swing.JCheckBox();
    retrieveDbExplorer = new javax.swing.JCheckBox();
    rememberObject = new javax.swing.JCheckBox();
    rememberSort = new javax.swing.JCheckBox();
    autoselectDataPanel = new javax.swing.JCheckBox();
    showFocus = new javax.swing.JCheckBox();
    autogeneratePK = new javax.swing.JCheckBox();
    defTableTypeLabel = new WbCheckBoxLabel();
    defTableType = new javax.swing.JTextField();
    jPanel1 = new javax.swing.JPanel();
    jLabel1 = new javax.swing.JLabel();
    tabPlacement = new PlacementChooser();

    setLayout(new java.awt.GridBagLayout());

    showDbExplorer.setSelected(Settings.getInstance().getShowDbExplorerInMainWindow());
    showDbExplorer.setText(ResourceMgr.getString("LblDbExplorerCheckBox")); // NOI18N
    showDbExplorer.setToolTipText(ResourceMgr.getString("d_LblDbExplorerCheckBox")); // NOI18N
    showDbExplorer.setBorder(null);
    showDbExplorer.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    showDbExplorer.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    showDbExplorer.setIconTextGap(5);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(10, 9, 0, 10);
    add(showDbExplorer, gridBagConstraints);

    showTriggerPanel.setSelected(Settings.getInstance().getShowTriggerPanel());
    showTriggerPanel.setText(ResourceMgr.getString("LblShowTriggerPanel")); // NOI18N
    showTriggerPanel.setToolTipText(ResourceMgr.getString("d_LblShowTriggerPanel")); // NOI18N
    showTriggerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    showTriggerPanel.setMargin(new java.awt.Insets(0, 0, 0, 0));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(7, 9, 0, 10);
    add(showTriggerPanel, gridBagConstraints);

    retrieveDbExplorer.setSelected(Settings.getInstance().getRetrieveDbExplorer());
    retrieveDbExplorer.setText(ResourceMgr.getString("LblRetrieveDbExplorer")); // NOI18N
    retrieveDbExplorer.setToolTipText(ResourceMgr.getString("d_LblRetrieveDbExplorer")); // NOI18N
    retrieveDbExplorer.setBorder(null);
    retrieveDbExplorer.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    retrieveDbExplorer.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
    retrieveDbExplorer.setIconTextGap(5);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(6, 9, 0, 10);
    add(retrieveDbExplorer, gridBagConstraints);

    rememberObject.setSelected(Settings.getInstance().getStoreExplorerObjectType());
    rememberObject.setText(ResourceMgr.getString("LblRememberObjectType")); // NOI18N
    rememberObject.setToolTipText(ResourceMgr.getString("d_LblRememberObjectType")); // NOI18N
    rememberObject.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    rememberObject.setMargin(new java.awt.Insets(0, 0, 0, 0));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(8, 9, 0, 10);
    add(rememberObject, gridBagConstraints);

    rememberSort.setSelected(Settings.getInstance().getRememberSortInDbExplorer());
    rememberSort.setText(ResourceMgr.getString("LblRememberDbExpSort")); // NOI18N
    rememberSort.setToolTipText(ResourceMgr.getString("d_LblRememberDbExpSort")); // NOI18N
    rememberSort.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    rememberSort.setMargin(new java.awt.Insets(0, 0, 0, 0));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(8, 9, 0, 10);
    add(rememberSort, gridBagConstraints);

    autoselectDataPanel.setSelected(Settings.getInstance().getSelectDataPanelAfterRetrieve());
    autoselectDataPanel.setText(ResourceMgr.getString("LblSelectDataPanel")); // NOI18N
    autoselectDataPanel.setToolTipText(ResourceMgr.getString("d_LblSelectDataPanel")); // NOI18N
    autoselectDataPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    autoselectDataPanel.setMargin(new java.awt.Insets(0, 0, 0, 0));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(8, 9, 0, 10);
    add(autoselectDataPanel, gridBagConstraints);

    showFocus.setSelected(Settings.getInstance().showFocusInDbExplorer());
    showFocus.setText(ResourceMgr.getString("LblShowFocus")); // NOI18N
    showFocus.setToolTipText(ResourceMgr.getString("d_LblShowFocus")); // NOI18N
    showFocus.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    showFocus.setMargin(new java.awt.Insets(0, 0, 0, 0));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(8, 9, 0, 10);
    add(showFocus, gridBagConstraints);

    autogeneratePK.setSelected(Settings.getInstance().getStoreExplorerObjectType());
    autogeneratePK.setText(ResourceMgr.getString("LblGeneratePkName")); // NOI18N
    autogeneratePK.setToolTipText(ResourceMgr.getString("d_LblGeneratePkName")); // NOI18N
    autogeneratePK.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    autogeneratePK.setMargin(new java.awt.Insets(0, 0, 0, 0));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(9, 9, 0, 10);
    add(autogeneratePK, gridBagConstraints);

    defTableTypeLabel.setLabelFor(autogeneratePK);
    defTableTypeLabel.setText(ResourceMgr.getString("LblDefTableType")); // NOI18N
    defTableTypeLabel.setToolTipText(ResourceMgr.getString("d_LblDefTableType")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(12, 9, 0, 0);
    add(defTableTypeLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(9, 9, 0, 20);
    add(defTableType, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 11;
    gridBagConstraints.weighty = 1.0;
    add(jPanel1, gridBagConstraints);

    jLabel1.setText(ResourceMgr.getString("LblObjTabPos")); // NOI18N
    jLabel1.setToolTipText(ResourceMgr.getString("d_LblObjTabPos")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 9;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(10, 9, 0, 0);
    add(jLabel1, gridBagConstraints);

    tabPlacement.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Top", "Bottom", "Left", "Right" }));
    tabPlacement.setToolTipText(ResourceMgr.getString("d_LblObjTabPos")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 9;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(7, 9, 0, 0);
    add(tabPlacement, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JCheckBox autogeneratePK;
  private javax.swing.JCheckBox autoselectDataPanel;
  private javax.swing.JTextField defTableType;
  private javax.swing.JLabel defTableTypeLabel;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JCheckBox rememberObject;
  private javax.swing.JCheckBox rememberSort;
  private javax.swing.JCheckBox retrieveDbExplorer;
  private javax.swing.JCheckBox showDbExplorer;
  private javax.swing.JCheckBox showFocus;
  private javax.swing.JCheckBox showTriggerPanel;
  private javax.swing.JComboBox tabPlacement;
  // End of variables declaration//GEN-END:variables

}
