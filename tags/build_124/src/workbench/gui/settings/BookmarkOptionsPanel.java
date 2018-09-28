/*
 * EditorOptionsPanel.java
 *
 * This file is part of SQL Workbench/J, https://www.sql-workbench.eu
 *
 * Copyright 2002-2018, Thomas Kellerer
 *
 * Licensed under a modified Apache License, Version 2.0
 * that restricts the use for certain governments.
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at.
 *
 *     https://www.sql-workbench.eu/manual/license.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * To contact the author please send an email to: support@sql-workbench.eu
 *
 */
package workbench.gui.settings;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import workbench.interfaces.Restoreable;
import workbench.resource.GuiSettings;
import workbench.resource.ResourceMgr;

/**
 *
 * @author  Thomas Kellerer
 */
public class BookmarkOptionsPanel
	extends JPanel
	implements Restoreable, ActionListener
{

	public BookmarkOptionsPanel()
	{
		super();
		initComponents();
	}

	@Override
	public void restoreSettings()
	{
		useResultForBookmark.setSelected(GuiSettings.getUseResultTagForBookmarks());
		useProcs.setSelected(GuiSettings.getParseProceduresForBookmarks());
		rememberColWidths.setSelected(GuiSettings.getSaveBookmarkColWidths());
		rememberSort.setSelected(GuiSettings.getSaveBookmarkSort());
		useParamNames.setSelected(GuiSettings.getProcBookmarksIncludeParmName());
	}

	@Override
	public void saveSettings()
	{
		GuiSettings.setUseResultTagForBookmarks(useResultForBookmark.isSelected());
		GuiSettings.setSaveBookmarksColWidths(rememberColWidths.isSelected());
		GuiSettings.setSaveBookmarksSort(rememberSort.isSelected());
		GuiSettings.setParseProceduresForBookmarks(useProcs.isSelected());
		GuiSettings.setProcBookmarksIncludeParmName(useParamNames.isSelected());
	}


	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {
    GridBagConstraints gridBagConstraints;

    useResultForBookmark = new JCheckBox();
    rememberColWidths = new JCheckBox();
    rememberSort = new JCheckBox();
    useProcs = new JCheckBox();
    useParamNames = new JCheckBox();

    setLayout(new GridBagLayout());

    useResultForBookmark.setText(ResourceMgr.getString("LblBookmarkResultName")); // NOI18N
    useResultForBookmark.setToolTipText(ResourceMgr.getString("d_LblBookmarkResultName")); // NOI18N
    useResultForBookmark.setBorder(null);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
    gridBagConstraints.weightx = 1.0;
    add(useResultForBookmark, gridBagConstraints);

    rememberColWidths.setText(ResourceMgr.getString("MnuTxtBookmarksSaveWidths")); // NOI18N
    rememberColWidths.setToolTipText(ResourceMgr.getString("d_MnuTxtBookmarksSaveWidths")); // NOI18N
    rememberColWidths.setBorder(null);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new Insets(10, 0, 0, 0);
    add(rememberColWidths, gridBagConstraints);

    rememberSort.setText(ResourceMgr.getString("MnuTxtRememberSort")); // NOI18N
    rememberSort.setToolTipText(ResourceMgr.getString("d_MnuTxtRememberSort")); // NOI18N
    rememberSort.setBorder(null);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new Insets(10, 0, 0, 0);
    add(rememberSort, gridBagConstraints);

    useProcs.setText(ResourceMgr.getString("LblBookmarkProcs")); // NOI18N
    useProcs.setToolTipText(ResourceMgr.getString("d_MnuTxtRememberSort")); // NOI18N
    useProcs.setBorder(null);
    useProcs.addActionListener(this);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
    gridBagConstraints.insets = new Insets(10, 0, 0, 0);
    add(useProcs, gridBagConstraints);

    useParamNames.setText(ResourceMgr.getString("LblBookmarkProcsParams")); // NOI18N
    useParamNames.setBorder(null);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
    gridBagConstraints.insets = new Insets(10, 18, 0, 0);
    add(useParamNames, gridBagConstraints);
  }

  // Code for dispatching events from components to event handlers.

  public void actionPerformed(ActionEvent evt)
  {
    if (evt.getSource() == useProcs)
    {
      BookmarkOptionsPanel.this.useProcsActionPerformed(evt);
    }
  }// </editor-fold>//GEN-END:initComponents

  private void useProcsActionPerformed(ActionEvent evt)//GEN-FIRST:event_useProcsActionPerformed
  {//GEN-HEADEREND:event_useProcsActionPerformed
    useParamNames.setEnabled(useProcs.isSelected());
  }//GEN-LAST:event_useProcsActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private JCheckBox rememberColWidths;
  private JCheckBox rememberSort;
  private JCheckBox useParamNames;
  private JCheckBox useProcs;
  private JCheckBox useResultForBookmark;
  // End of variables declaration//GEN-END:variables

}