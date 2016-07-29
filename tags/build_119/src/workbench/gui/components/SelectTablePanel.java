/*
 * SelectTablePanel.java
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
package workbench.gui.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;

import workbench.interfaces.ValidatingComponent;
import workbench.resource.ResourceMgr;

/**
 *
 * @author Thomas Kellerer
 */
public class SelectTablePanel
	extends JPanel
	implements ValidatingComponent, ActionListener
{

	public SelectTablePanel(List<String> tablenames)
	{
		initComponents();
		tables.setModel(new DefaultComboBoxModel(tablenames.toArray(new String[tablenames.size()])));
	}

	@Override
	public boolean validateInput()
	{
		return tables.getSelectedItem() != null;
	}

	@Override
	public void componentDisplayed()
	{
	}

  @Override
  public void componentWillBeClosed()
  {
  }

	public String getSelectedTable()
	{
		return (String)tables.getSelectedItem();
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jLabel1 = new JLabel();
    tables = new JComboBox();

    jLabel1.setText(ResourceMgr.getString("MsgEnterUpdateTable")); // NOI18N
    jLabel1.setVerticalAlignment(SwingConstants.TOP);

    tables.addActionListener(this);

    GroupLayout layout = new GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addComponent(jLabel1, GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)
        .addGap(20, 20, 20))
      .addComponent(tables, 0, 222, Short.MAX_VALUE)
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(Alignment.LEADING)
      .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
        .addComponent(jLabel1, GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
        .addPreferredGap(ComponentPlacement.RELATED)
        .addComponent(tables, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
        .addContainerGap())
    );
  }

  // Code for dispatching events from components to event handlers.

  public void actionPerformed(java.awt.event.ActionEvent evt) {
    if (evt.getSource() == tables) {
      SelectTablePanel.this.tablesActionPerformed(evt);
    }
  }// </editor-fold>//GEN-END:initComponents

	private void tablesActionPerformed(ActionEvent evt)//GEN-FIRST:event_tablesActionPerformed
	{//GEN-HEADEREND:event_tablesActionPerformed

	}//GEN-LAST:event_tablesActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private JLabel jLabel1;
  private JComboBox tables;
  // End of variables declaration//GEN-END:variables
}