/*
 * HistoryTextField.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2007, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.components;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import workbench.interfaces.PropertyStorage;
import workbench.resource.Settings;
import workbench.util.StringUtil;

/**
 * @author support@sql-workbench.net
 */
public class HistoryTextField
	extends JComboBox
{
	private String propName;
	private List<String> historyValues = new ArrayList<String>();
	private int maxHistorySize = 25;
		
	public HistoryTextField(String prop)
	{
		super();
		setEditable(true);
		this.propName = prop;
		this.maxHistorySize = Settings.getInstance().getIntProperty("workbench.history." + propName + ".size", 25);
	}
	
	public String getText()
	{
		Object item = getSelectedItem();
		if (item == null) item = getEditor().getItem();
		if (item == null) return "";
		return (String)item;
	}
	
	public void setText(String s)
	{
//		this.getEditor().setItem(s);
		this.setSelectedItem(s);
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
		this.historyValues.addAll(l);
		this.updateModel();
		String lastValue = props.getProperty(prefix + propName + ".lastvalue", null);
		
		if (lastValue != null) this.setText(lastValue);
	}
	
	public void restoreSettings()
	{
		restoreSettings(Settings.getInstance(), "workbench.quickfilter." + propName + ".");
	}

	public void saveSettings()
	{
		saveSettings(Settings.getInstance(), "workbench.quickfilter." + propName + ".");
	}
	
	public void addToHistory(String s)
	{
		if (StringUtil.isEmptyString(s)) return;
		s = s.trim();	
		Object item = getSelectedItem();
		int index = historyValues.indexOf(s);
		if (index > -1)
		{
			this.historyValues.remove(index);
		}
		else
		{
			while (this.historyValues.size() >= this.maxHistorySize)
			{
				this.historyValues.remove(historyValues.size() - 1);
			}
		}
		this.historyValues.add(0,s);
		this.updateModel();
		setSelectedItem(item);
	}
	
	private void updateModel()
	{
		DefaultComboBoxModel model = new DefaultComboBoxModel(this.historyValues.toArray());
		setModel(model);
	}
}
