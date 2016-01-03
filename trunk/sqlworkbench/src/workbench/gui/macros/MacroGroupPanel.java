/*
 * MacroGroupPanel.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2016, Thomas Kellerer
 *
 * Licensed under a modified Apache License, Version 2.0
 * that restricts the use for certain governments.
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at.
 *
 *     http://sql-workbench.net/manual/license.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.macros;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import workbench.resource.ResourceMgr;

import workbench.gui.components.BooleanPropertyEditor;
import workbench.gui.components.StringPropertyEditor;
import workbench.gui.components.WbTraversalPolicy;

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

		BooleanPropertyEditor popup = (BooleanPropertyEditor)includeInPopup;
		popup.setSourceObject(group, "visibleInPopup");
		popup.setImmediateUpdate(true);

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
  private void initComponents()
  {
    GridBagConstraints gridBagConstraints;

    jLabel1 = new JLabel();
    jTextField1 = new StringPropertyEditor();
    jPanel1 = new JPanel();
    includeInMenu = new BooleanPropertyEditor();
    includeInPopup = new BooleanPropertyEditor();

    setMinimumSize(new Dimension(120, 90));
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

    jPanel1.setLayout(new GridBagLayout());

    includeInMenu.setText(ResourceMgr.getString("LblMacroGrpMenu")); // NOI18N
    includeInMenu.setBorder(null);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.weighty = 1.0;
    jPanel1.add(includeInMenu, gridBagConstraints);

    includeInPopup.setText(ResourceMgr.getString("LblMacroGrpPop")); // NOI18N
    includeInPopup.setBorder(null);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new Insets(0, 8, 0, 0);
    jPanel1.add(includeInPopup, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new Insets(11, 5, 0, 0);
    add(jPanel1, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private JCheckBox includeInMenu;
  private JCheckBox includeInPopup;
  private JLabel jLabel1;
  private JPanel jPanel1;
  private JTextField jTextField1;
  // End of variables declaration//GEN-END:variables
}
