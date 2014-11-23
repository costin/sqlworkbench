/*
 * SelectionDisplay.java
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

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumnModel;
import workbench.gui.WbSwingUtilities;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.util.StringUtil;
import workbench.util.WbNumberFormatter;

/**
 *
 * @author Thomas Kellerer
 */
public class SelectionDisplay
	extends JLabel
{
	private JTable table;
	private Border activeBorder = new CompoundBorder(new DividerBorder(DividerBorder.LEFT), new EmptyBorder(0, 3, 0, 3));
	private ListSelectionListener rowListener;
	private ListSelectionListener columnListener;
	private WbNumberFormatter formatter;

	public SelectionDisplay()
	{
		rowListener = new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				rowSelectionChanged(e);
			}
		};
		columnListener = new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				columnSelectionChanged(e);
			}
		};
		setBorder(WbSwingUtilities.EMPTY_BORDER);
		formatter = Settings.getInstance().createDefaultDecimalFormatter(2);
	}

	public void removeClient(JTable client)
	{
		if (client != null)
		{
			ListSelectionModel rowModel = client.getSelectionModel();
			if (rowModel != null)
			{
				rowModel.removeListSelectionListener(rowListener);
			}
			TableColumnModel col = client.getColumnModel();
			ListSelectionModel colModel = (col != null ? col.getSelectionModel() : null);
			if (colModel != null)
			{
				colModel.removeListSelectionListener(columnListener);
			}
			setText("");
		}
	}

	public void setClient(JTable client)
	{
		removeClient(table);

		table = client;
		if (client != null)
		{
			ListSelectionModel rowModel = client.getSelectionModel();
			if (rowModel != null)
			{
				rowModel.addListSelectionListener(rowListener);
			}
			TableColumnModel col = client.getColumnModel();
			ListSelectionModel colModel = (col != null ? col.getSelectionModel() : null);
			if (colModel != null)
			{
				colModel.addListSelectionListener(columnListener);
			}
		}
	}

	protected void columnSelectionChanged(ListSelectionEvent e)
	{
		showSelection();
	}

	protected void rowSelectionChanged(ListSelectionEvent e)
	{
		showSelection();
	}

	@Override
	public void setText(String text)
	{
		super.setText(text);
		if (StringUtil.isEmptyString(text))
		{
			setBorder(WbSwingUtilities.EMPTY_BORDER);
		}
		else
		{
			setBorder(activeBorder);
		}
	}


	protected void showSelection()
	{
		if (table == null)
		{
			setText("");
			return;
		}

		int cols[] = table.getSelectedColumns();

		StringBuilder display = new StringBuilder(30);

		double sum = 0;
		boolean numbers = false;

		if (cols.length == 1 && table.getColumnSelectionAllowed())
		{
			int rows[] = table.getSelectedRows();
			for (int i=0; i < rows.length; i++)
			{
				Object o = table.getValueAt(rows[i], cols[0]);
				if (o instanceof Number)
				{
					sum += ((Number)o).doubleValue();
					numbers = true;
				}
			}
		}

		if (numbers)
		{
			String v = formatter.format(sum);
			display.append(ResourceMgr.getFormattedString("MsgSelectSum", v));
		}
		else
		{
			int rows = table.getSelectedRowCount();
			if (rows > 0)
			{
				display.append(ResourceMgr.getFormattedString("MsgRowsSelected", rows));
			}
		}
		if (display.length() == 0)
		{
			setText("");
		}
		else
		{
			setText(display.toString());
		}
	}
}