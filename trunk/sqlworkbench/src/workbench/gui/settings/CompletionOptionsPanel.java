/*
 * CompletionOptionsPanel.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2014, Thomas Kellerer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at.
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
package workbench.gui.settings;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import workbench.interfaces.Restoreable;
import workbench.interfaces.ValidatingComponent;
import workbench.resource.ColumnSortType;
import workbench.resource.GuiSettings;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;

import workbench.db.objectcache.ObjectCacheStorage;

import workbench.gui.WbSwingUtilities;

import workbench.util.DurationNumber;

/**
 *
 * @author tkellerer
 */
public class CompletionOptionsPanel
	extends JPanel
	implements Restoreable, ValidatingComponent
{
	public CompletionOptionsPanel()
	{
		initComponents();

		DefaultComboBoxModel model = new DefaultComboBoxModel(ObjectCacheStorage.values());
		localStorageType.setModel(model);

		WbSwingUtilities.setMinimumSize(maxAgeField, 5);
	}

	@Override
	public void restoreSettings()
	{
		String[] pasteCase = new String[] {
			ResourceMgr.getString("LblLowercase"),
			ResourceMgr.getString("LblUppercase"),
			ResourceMgr.getString("LblAsIs")
		};
		completionPasteCase.setModel(new DefaultComboBoxModel(pasteCase));
		String paste = Settings.getInstance().getAutoCompletionPasteCase();
		if ("lower".equals(paste)) this.completionPasteCase.setSelectedIndex(0);
		else if ("upper".equals(paste)) this.completionPasteCase.setSelectedIndex(1);
		else this.completionPasteCase.setSelectedIndex(2);

		String[] sortItems = new String[] {
			ResourceMgr.getString("LblSortPastColName"),
			ResourceMgr.getString("LblSortPastColPos")
		};
		completionColumnSort.setModel(new DefaultComboBoxModel(sortItems));

		ColumnSortType sort = Settings.getInstance().getAutoCompletionColumnSortType();
		if (sort == ColumnSortType.position)
		{
			this.completionColumnSort.setSelectedIndex(1);
		}
		else
		{
			this.completionColumnSort.setSelectedIndex(0);
		}
		closePopup.setSelected(Settings.getInstance().getCloseAutoCompletionWithSearch());
		filterSearch.setSelected(GuiSettings.getFilterCompletionSearch());
		partialMatch.setSelected(GuiSettings.getPartialCompletionSearch());
		sortColumns.setSelected(GuiSettings.getSortCompletionColumns());
		cyleEntries.setSelected(GuiSettings.getCycleCompletionPopup());

		ObjectCacheStorage storage = GuiSettings.getLocalStorageForObjectCache();
		localStorageType.setSelectedItem(storage);
		localStorageType.doLayout();

		maxAgeField.setText(GuiSettings.getLocalStorageMaxAge());
		WbSwingUtilities.makeEqualSize(completionColumnSort, completionPasteCase, localStorageType);
	}

	@Override
	public void saveSettings()
	{
		Settings set = Settings.getInstance();
		int index = this.completionPasteCase.getSelectedIndex();
		if (index == 0)
		{
			set.setAutoCompletionPasteCase("lower");
		}
		else if (index == 1)
		{
			set.setAutoCompletionPasteCase("upper");
		}
		else
		{
			set.setAutoCompletionPasteCase(null);
		}

		set.setCloseAutoCompletionWithSearch(closePopup.isSelected());
		index = completionColumnSort.getSelectedIndex();
		if (index == 1)
		{
			set.setAutoCompletionColumnSort(ColumnSortType.position);
		}
		else
		{
			set.setAutoCompletionColumnSort(ColumnSortType.name);
		}
		GuiSettings.setFilterCompletionSearch(filterSearch.isSelected());
		GuiSettings.setPartialCompletionSearch(partialMatch.isSelected());
		GuiSettings.setSortCompletionColumns(sortColumns.isSelected());
		GuiSettings.setCycleCompletionPopup(cyleEntries.isSelected());
		ObjectCacheStorage storage = (ObjectCacheStorage)localStorageType.getSelectedItem();
		GuiSettings.setLocalStorageForObjectCache(storage);
		GuiSettings.setLocalStorageMaxAge(maxAgeField.getText().trim().toLowerCase());
	}

	@Override
	public boolean validateInput()
	{
		String duration = maxAgeField.getText();
		DurationNumber n = new DurationNumber();

		if (n.isValid(duration)) return true;

		WbSwingUtilities.showErrorMessageKey(this, "ErrInvalidAge");
		maxAgeField.selectAll();
		WbSwingUtilities.requestFocus(maxAgeField);
		return false;
	}

	@Override
	public void componentDisplayed()
	{
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

    pasteLabel = new JLabel();
    completionPasteCase = new JComboBox();
    closePopup = new JCheckBox();
    completionColumnSort = new JComboBox();
    pasterOrderLabel = new JLabel();
    sortColumns = new JCheckBox();
    partialMatch = new JCheckBox();
    filterSearch = new JCheckBox();
    cyleEntries = new JCheckBox();
    localStorageLabel = new JLabel();
    localStorageType = new JComboBox();
    maxAgeLabel = new JLabel();
    maxAgeField = new JTextField();

    setLayout(new GridBagLayout());

    pasteLabel.setText(ResourceMgr.getString("LblPasteCase")); // NOI18N
    pasteLabel.setToolTipText(ResourceMgr.getString("d_LblPasteCase")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(11, 12, 0, 0);
    add(pasteLabel, gridBagConstraints);

    completionPasteCase.setModel(new DefaultComboBoxModel(new String[] { "Lowercase", "Uppercase", "As is" }));
    completionPasteCase.setToolTipText(ResourceMgr.getDescription("LblPasteCase"));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(8, 11, 0, 15);
    add(completionPasteCase, gridBagConstraints);

    closePopup.setText(ResourceMgr.getString("TxtCloseCompletion")); // NOI18N
    closePopup.setToolTipText(ResourceMgr.getString("d_TxtCloseCompletion")); // NOI18N
    closePopup.setBorder(null);
    closePopup.setHorizontalAlignment(SwingConstants.LEFT);
    closePopup.setHorizontalTextPosition(SwingConstants.RIGHT);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new Insets(11, 12, 0, 0);
    add(closePopup, gridBagConstraints);

    completionColumnSort.setToolTipText(ResourceMgr.getDescription("LblPasteSort"));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(6, 11, 0, 15);
    add(completionColumnSort, gridBagConstraints);

    pasterOrderLabel.setText(ResourceMgr.getString("LblPasteSort")); // NOI18N
    pasterOrderLabel.setToolTipText(ResourceMgr.getString("d_LblPasteSort")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(9, 12, 0, 0);
    add(pasterOrderLabel, gridBagConstraints);

    sortColumns.setText(ResourceMgr.getString("LblCompletionSortCols")); // NOI18N
    sortColumns.setToolTipText(ResourceMgr.getString("d_LblCompletionSortCols")); // NOI18N
    sortColumns.setBorder(null);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(11, 12, 0, 0);
    add(sortColumns, gridBagConstraints);

    partialMatch.setText(ResourceMgr.getString("LblCompletionPartialMatch")); // NOI18N
    partialMatch.setToolTipText(ResourceMgr.getString("d_LblCompletionPartialMatch")); // NOI18N
    partialMatch.setBorder(null);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(9, 12, 0, 0);
    add(partialMatch, gridBagConstraints);

    filterSearch.setText(ResourceMgr.getString("LblCompletionFilterSearch")); // NOI18N
    filterSearch.setToolTipText(ResourceMgr.getString("d_LblCompletionFilterSearch")); // NOI18N
    filterSearch.setBorder(null);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(9, 12, 0, 0);
    add(filterSearch, gridBagConstraints);

    cyleEntries.setText(ResourceMgr.getString("LblCompletionCycle")); // NOI18N
    cyleEntries.setToolTipText(ResourceMgr.getString("d_LblCompletionCycle")); // NOI18N
    cyleEntries.setBorder(null);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 9;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new Insets(9, 12, 0, 0);
    add(cyleEntries, gridBagConstraints);

    localStorageLabel.setText(ResourceMgr.getString("LblLocalStorageType")); // NOI18N
    localStorageLabel.setToolTipText(ResourceMgr.getString("d_LblLocalStorageType")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(9, 12, 0, 0);
    add(localStorageLabel, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(6, 11, 0, 15);
    add(localStorageType, gridBagConstraints);

    maxAgeLabel.setText(ResourceMgr.getString("LblLocalMaxAge")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(9, 12, 0, 0);
    add(maxAgeLabel, gridBagConstraints);

    maxAgeField.setText("jTextField1");
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(6, 11, 0, 15);
    add(maxAgeField, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private JCheckBox closePopup;
  private JComboBox completionColumnSort;
  private JComboBox completionPasteCase;
  private JCheckBox cyleEntries;
  private JCheckBox filterSearch;
  private JLabel localStorageLabel;
  private JComboBox localStorageType;
  private JTextField maxAgeField;
  private JLabel maxAgeLabel;
  private JCheckBox partialMatch;
  private JLabel pasteLabel;
  private JLabel pasterOrderLabel;
  private JCheckBox sortColumns;
  // End of variables declaration//GEN-END:variables
}
