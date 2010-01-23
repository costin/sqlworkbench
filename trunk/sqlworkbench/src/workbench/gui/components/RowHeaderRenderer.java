/*
 * RowHeaderRenderer
 *
 *  This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 *  Copyright 2002-2009, Thomas Kellerer
 *  No part of this code maybe reused without the permission of the author
 *
 *  To contact the author please send an email to: support@sql-workbench.net
 */
package workbench.gui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import workbench.resource.GuiSettings;
import workbench.util.NumberStringCache;

/**
 *
 * @author Thomas Kellerer
 */
public class RowHeaderRenderer
	implements ListCellRenderer, TableModelListener
{
	private JLabel label;
	private JTable table;
	private TableRowHeader rowHeader;
	private boolean useAlternateColor;
	private Color baseColor;
	private Color alternateColor;
	private int colWidth = -1;

	public RowHeaderRenderer(TableRowHeader rowHead, JTable client)
	{
		table = client;
		table.getModel().addTableModelListener(this);
		label = new JLabel();
		rowHeader = rowHead;
		JTableHeader header = client.getTableHeader();
		label.setFont(header.getFont());
		label.setOpaque(true);
		label.setHorizontalAlignment(SwingConstants.RIGHT);

		if (table.getRowCount() == 0)
		{
			colWidth = 16;
		}

		if (GuiSettings.getUseButtonStyleRowNumbers())
		{
			Border b = new CompoundBorder(UIManager.getBorder("TableHeader.cellBorder"), new EmptyBorder(0, 1, 0, 2));
			label.setBorder(b);
			label.setForeground(header.getForeground());
			label.setBackground(header.getBackground());
		}
		else
		{
			label.setBorder(new SingleLineBorder(SingleLineBorder.RIGHT, Color.LIGHT_GRAY));
			useAlternateColor = true;
			baseColor = table.getBackground();
			alternateColor = GuiSettings.getAlternateRowColor();
		}
	}

	private synchronized void calculateWidth()
	{
		FontMetrics fm = label.getFontMetrics(label.getFont());
		int width = 8;
		if (fm != null)
		{
			Rectangle2D r = fm.getStringBounds("9", label.getGraphics());
			width = r.getBounds().width;
		}
		String max = NumberStringCache.getNumberString(table.getRowCount());
		colWidth = max.length() * width + (width * 2);
	}

	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
	{
		if (colWidth == -1)
		{
			calculateWidth();
		}

		if (useAlternateColor)
		{
			if ((index % 2) == 1)
			{
				label.setBackground(alternateColor);
			}
			else
			{
				label.setBackground(baseColor);
			}
		}
		label.setText(NumberStringCache.getNumberString(index + 1));
		int height = table.getRowHeight(index);
		Dimension size = new Dimension(colWidth, height);
		label.setPreferredSize(size);
		return label;
	}

	@Override
	public void tableChanged(TableModelEvent e)
	{
		rowHeader.modelChanged(e.getFirstRow());
		calculateWidth();
	}

	public void dispose()
	{
		if (table == null) return;
		TableModel m = table.getModel();
		if (m == null) return;
		m.removeTableModelListener(this);
	}
}
