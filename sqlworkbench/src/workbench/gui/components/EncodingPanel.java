/*
 * EncodingPanel.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2013, Thomas Kellerer
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

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import workbench.interfaces.EncodingSelector;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;

import workbench.util.EncodingUtil;

/**
 *
 * @author Thomas Kellerer
 */
public class EncodingPanel
		extends JPanel
		implements EncodingSelector
{
	protected JComboBox encodings = new JComboBox();
	private JLabel label;

	public EncodingPanel()
	{
		this(Settings.getInstance().getDefaultEncoding(), true);
	}

	public EncodingPanel(String encoding)
	{
		this(encoding, true);
	}

	public EncodingPanel(String encoding, boolean showLabel)
	{
		super();
		String[] charsets = EncodingUtil.getEncodings();
		int count = charsets.length;
		for (int i=0; i < count; i++)
		{
			encodings.addItem(charsets[i]);
		}

		if (encoding != null)
		{
			encodings.setSelectedItem(encoding);
		}

		this.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		if (showLabel)
		{
			label =  new JLabel(ResourceMgr.getString("LblFileEncoding"));

			// align the label with the dropdown by applying the necessary insets
			int encHeight = encodings.getPreferredSize().height;
			int lblHeight = label.getPreferredSize().height;
			int inset = (encHeight - lblHeight) / 2;

			c.gridx = 0;
			c.gridy = 0;
			c.insets = new java.awt.Insets(inset, 0, 0, 0);
			c.fill = java.awt.GridBagConstraints.NONE;
			c.anchor = java.awt.GridBagConstraints.NORTHWEST;
			c.weighty = 1.0;

			this.add(label, c);
		}
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.insets = new java.awt.Insets(0, 4, 0, 0);
		c.fill = java.awt.GridBagConstraints.HORIZONTAL;
		c.anchor = java.awt.GridBagConstraints.NORTHWEST;
		c.weightx = 1.0;
		c.weighty = 1.0;

		this.add(encodings, c);
	}

	public void setLabelVisible(boolean flag)
	{
		if (this.label == null) return;
		this.label.setVisible(flag);
	}

	public boolean isLabelVisible()
	{
		if (this.label == null) return false;
		return this.label.isVisible();
	}

	@Override
	public void setEncoding(final String enc)
	{
		EventQueue.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				encodings.setSelectedItem(enc);
			}
		});
	}

	@Override
	public String getEncoding()
	{
		String enc = (String)this.encodings.getSelectedItem();
		return enc;
	}

}
