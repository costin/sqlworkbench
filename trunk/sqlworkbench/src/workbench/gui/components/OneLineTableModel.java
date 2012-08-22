/*
 * OneLineTableModel.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2012, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.components;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;


/**
 *
 * @author Thomas Kellerer
 */
public class OneLineTableModel
	implements TableModel
{
	private String columnTitle;
	private String message;

	public OneLineTableModel(String colTitle, String msg)
	{
		this.columnTitle = colTitle;
		this.message = msg;
	}

	public void setMessage(String aMessage)
	{
		this.message = aMessage;
	}

	@Override
	public Object getValueAt(int row, int col)
	{
		return message;
	}

	@Override
	public void setValueAt(Object aValue, int row, int column)
	{
	}

	@Override
	public int getColumnCount()
	{
		return 1;
	}

	@Override
	public int getRowCount()
	{
		return 1;
	}

	@Override
	public boolean isCellEditable(int row, int column)
	{
		return false;
	}

	@Override
	public void addTableModelListener(TableModelListener l)
	{
	}

	@Override
	public Class getColumnClass(int columnIndex)
	{
		return String.class;
	}

	@Override
	public String getColumnName(int columnIndex)
	{
		return this.columnTitle;
	}

	@Override
	public void removeTableModelListener(TableModelListener l)
	{
	}

}
