/*
 * MacroGroupPanel.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2011, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.macros;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import workbench.gui.components.BooleanPropertyEditor;
import workbench.gui.components.StringPropertyEditor;
import workbench.gui.components.WbTraversalPolicy;
import workbench.resource.ResourceMgr;
import workbench.sql.macros.MacroGroup;

/**
 *
 * @author Thomas Kellerer
 */
public class MacroGroupPanel
	extends javax.swing.JPanel
{
	public MacroGroupPanel(PropertyChangeListener l)
	{
		initComponents();
		jTextField1.addPropertyChangeListener(l);
	}

	public void setMacroGroup(MacroGroup group)
	{
		BooleanPropertyEditor menu = (BooleanPropertyEditor)includeInMenu;
		menu.setSourceObject(group, "visibleInMenu");
		menu.setImmediateUpdate(true);

		StringPropertyEditor name = (StringPropertyEditor)jTextField1;
		name.setSourceObject(group, "name");
		name.setImmediateUpdate(true);

		WbTraversalPolicy policy = new WbTraversalPolicy();
		policy.addComponent(jTextField1);
		policy.addComponent(includeInMenu);
		policy.setDefaultComponent(jTextField1);
		setFocusTraversalPolicy(policy);
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
		GridBagConstraints gridBagConstraints;

    jLabel1 = new JLabel();
    jTextField1 = new StringPropertyEditor();
    includeInMenu = new BooleanPropertyEditor();

    setLayout(new GridBagLayout());

    jLabel1.setText(ResourceMgr.getString("LblMacroGrpName")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(5, 5, 0, 0);
    add(jLabel1, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new Insets(5, 5, 0, 5);
    add(jTextField1, gridBagConstraints);

    includeInMenu.setText(ResourceMgr.getString("LblMacroGrpMenu")); // NOI18N
    includeInMenu.setBorder(null);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new Insets(11, 5, 0, 0);
    add(includeInMenu, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private JCheckBox includeInMenu;
  private JLabel jLabel1;
  private JTextField jTextField1;
  // End of variables declaration//GEN-END:variables
}
