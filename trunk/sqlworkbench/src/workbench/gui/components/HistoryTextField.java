/*
 * HistoryTextField.java
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
package workbench.gui.components;

import java.awt.Component;
import java.util.List;

import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import workbench.interfaces.PropertyStorage;
import workbench.resource.Settings;

import workbench.util.FixedSizeList;
import workbench.util.StringUtil;

/**
 * @author Thomas Kellerer
 */
public class HistoryTextField
	extends JComboBox
{
	private String propName;
	private FixedSizeList<String> historyValues;
	private TextComponentMouseListener contextMenu;

	public HistoryTextField()
	{
		this(null);
	}

	public HistoryTextField(String prop)
	{
		super();
		setEditable(true);
		setSettingsProperty(prop);
		contextMenu = new TextComponentMouseListener();
		getEditor().getEditorComponent().addMouseListener(contextMenu);
		getEditor().getEditorComponent().setFocusTraversalKeysEnabled(true);
		setFocusTraversalKeysEnabled(true);
	}

	public void dispose()
	{
		historyValues.clear();
		ComboBoxEditor ed = getEditor();
		if (ed != null)
		{
			Component comp = ed.getEditorComponent();
			if (comp != null)
			{
				comp.removeMouseListener(contextMenu);
			}
		}
		contextMenu.dispose();
	}

	public void setSettingsProperty(String prop)
	{
		propName = prop;
		int maxHistorySize = Settings.getInstance().getIntProperty("workbench.history." + propName + ".size", 25);
		historyValues = new FixedSizeList<>(maxHistorySize);
	}

	public void selectAll()
	{
		getEditor().selectAll();
	}

	public void setColumns(int cols)
	{
		StringBuilder b = new StringBuilder(cols);
		for (int i=0; i < cols; i++) b.append('w');
		this.setPrototypeDisplayValue(b);
	}

	public String getText()
	{
		Object item = getSelectedItem();
		if (item == null) item = getEditor().getItem();
		if (item == null) return null;
		return (String)item;
	}

	public void setText(String s)
	{
		this.setSelectedItem(s);
		Component edit = this.getEditor().getEditorComponent();
		if (edit instanceof JTextField)
		{
			JTextField field = (JTextField)edit;
			field.setCaretPosition(0);
		}
	}

	public void saveSettings(PropertyStorage props, String prefix)
	{
		props.setProperty(prefix + propName + ".history", StringUtil.listToString(historyValues, ';', true));
		props.setProperty(prefix + propName + ".lastvalue", this.getText());
	}

	public void restoreSettings(PropertyStorage props, String prefix)
	{
		String s = props.getProperty(prefix + propName + ".history", "");
		List<String> l = StringUtil.stringToList(s, ";", true, true);
		this.setText("");
		this.historyValues.clear();
		for (String value : l)
		{
			historyValues.append(value);
		}
		this.updateModel();
		String lastValue = props.getProperty(prefix + propName + ".lastvalue", null);

		if (lastValue != null) this.setText(lastValue);
	}

	public void restoreSettings()
	{
		restoreSettings(Settings.getInstance(), "workbench.quickfilter.");
	}

	public void saveSettings()
	{
		saveSettings(Settings.getInstance(), "workbench.quickfilter." );
	}

	public void storeCurrent()
	{
		addToHistory(getText());
	}

	public void addToHistory(String s)
	{
		if (StringUtil.isEmptyString(s)) return;
		s = s.trim();
		Object item = getSelectedItem();
		historyValues.addEntry(s);
		updateModel();
		setSelectedItem(item);
	}

	private void updateModel()
	{
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		for (String entry : this.historyValues.getEntries())
		{
			model.addElement(entry);
		}
		setModel(model);
	}
}
