/*
 * DataStoreTableModel.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2006, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.components;
import java.awt.Toolkit;
import java.sql.SQLException;
import java.sql.Types;

import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import workbench.gui.WbSwingUtilities;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.storage.DataStore;
import workbench.storage.ResultInfo;
import workbench.storage.filter.FilterExpression;
import workbench.util.SqlUtil;
import workbench.util.WbThread;

/**
 * TableModel for displaying the contents of a {@link workbench.storage.DataStore }
 * @author support@sql-workbench.net
 *
 */
public class DataStoreTableModel
	extends AbstractTableModel
{
	private DataStore dataCache;
	private WbTable parentTable;
	private boolean showStatusColumn = false;
	
	private int columnStartIndex = 0;
	public static final String NOT_AVAILABLE = "(n/a)";
	private int lockColumn = -1;

	// used for sorting the model
	private boolean sortAscending = true;
	private int sortColumn = -1;
	private boolean allowEditing = true;
	private final Object model_change_lock = new Object();

	public DataStoreTableModel(DataStore aDataStore) 
		throws IllegalArgumentException
	{
		if (aDataStore == null) throw new IllegalArgumentException("DataStore cannot be null");
		this.setDataStore(aDataStore);
	}

	public DataStore getDataStore()
	{
		return this.dataCache;
	}

	public void setDataStore(DataStore newData)
	{
		this.dispose();
		this.dataCache = newData;
		this.showStatusColumn = false;
		this.columnStartIndex = 0;
		this.sortColumn = -1;
		this.fireTableStructureChanged();
	}

	/**
	 *	Return the contents of the field at the given position
	 *	in the result set.
	 *	@param row - The row to get. Counting starts at zero.
	 *	@param col - The column to get. Counting starts at zero.
	 */
	public Object getValueAt(int row, int col)
	{
		if (this.showStatusColumn && col == 0)
		{
			return this.dataCache.getRowStatus(row);
		}

		try
		{
			Object result;
			result = this.dataCache.getValue(row, col - this.columnStartIndex);
			return result;
		}
		catch (Exception e)
		{
			return "Error";
		}
	}

	public int findColumn(String aColname)
	{
		int index = -1;
		try
		{
			index = this.dataCache.getColumnIndex(aColname) + this.columnStartIndex;
		}
		catch (SQLException e)
		{
			index = -1;
		}
		return index;
	}

	public void setUpdateTable(String aTable)
	{
		this.dataCache.setUpdateTable(aTable);
	}

	/**
	 *	Shows or hides the status column.
	 *	The status column will display an indicator if the row has
	 *  been modified or was inserted
	 */
	public void setShowStatusColumn(boolean aFlag)
	{
		if (aFlag == this.showStatusColumn) return;
		synchronized(model_change_lock)
		{
			if (aFlag)
			{
				this.columnStartIndex = 1;
				if (this.sortColumn != -1) this.sortColumn++;
			}
			else
			{
				this.columnStartIndex = 0;
				if (this.sortColumn != -1) this.sortColumn--;
			}
			this.showStatusColumn = aFlag;
		}
		this.fireTableStructureChanged();
	}

	public boolean getShowStatusColumn() { return this.showStatusColumn; }

	public boolean isUpdateable()
	{
		if (this.dataCache == null) return false;
		return this.dataCache.isUpdateable();
	}

	public void setValueAt(Object aValue, int row, int column)
	{
		if (this.showStatusColumn && column == 0) return;

		if (this.isUpdateable())
		{
			if (aValue == null || aValue.toString().length() == 0)
			{
				this.dataCache.setNull(row, column - this.columnStartIndex);
			}
			else
			{
				try
				{
					this.dataCache.setInputValue(row, column - this.columnStartIndex, aValue);
				}
				catch (Exception ce)
				{
					LogMgr.logError(this, "Error converting input >" + aValue + "< to column type (" + this.getColumnType(column) + ") ", ce);
					Toolkit.getDefaultToolkit().beep();
					String msg = ResourceMgr.getString("MsgConvertError");
					msg = msg + "\r\n" + ce.getLocalizedMessage();
					WbSwingUtilities.showErrorMessage(parentTable, msg);
					return;
				}
			}
			fireTableDataChanged();
		}
	}

	/**
	 *	Return the number of columns in the model.
	 *	This will return the number of columns of the underlying DataStore (plus one
	 *  if the status column is enabled)
	 */
	public int getColumnCount()
	{
		return this.dataCache.getColumnCount() + this.columnStartIndex;
	}

	/**
	 *	Returns the current width of the given column.
	 *	It returns the value of {@link workbench.storage.DataStore#getColumnDisplaySize(int)}
	 *  for every column which is not the status column.
	 */
	public int getColumnWidth(int aColumn)
	{
		if (this.showStatusColumn && aColumn == 0) return 5;
		if (this.dataCache == null) return 0;
		try
		{
			return this.dataCache.getColumnDisplaySize(aColumn);
		}
		catch (Exception e)
		{
			LogMgr.logWarning("DataStoreTableModel.getColumnWidth()", "Error retrieving display size for column " + aColumn, e);
			return 100;
		}
	}

	/**
	 *	Returns the name of the datatype (according to java.sql.Types) of the
	 *  given column.
	 */
	public String getColumnTypeName(int aColumn)
	{
		if (aColumn == 0) return "";
		return SqlUtil.getTypeName(this.getColumnType(aColumn));
	}

	public String getDbmsType(int col)
	{
		if (this.dataCache == null) return null;
		if (this.showStatusColumn && col == 0) return null;
		try
		{
			ResultInfo info = this.dataCache.getResultInfo();
			return info.getDbmsTypeName(col - this.columnStartIndex);
		}
		catch (Exception e)
		{
			return null;
		}
		
	}
	/**
	 *	Returns the type (java.sql.Types) of the given column.
	 */
	public int getColumnType(int aColumn)
	{
		if (this.dataCache == null) return Types.NULL;
		if (this.showStatusColumn && aColumn == 0) return 0;

		try
		{
			return this.dataCache.getColumnType(aColumn - this.columnStartIndex);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return Types.VARCHAR;
		}
	}

	/**
	 *	Number of rows in the result set
	 */
	public int getRowCount()
	{
		if (this.dataCache == null) return 0;
		return this.dataCache.getRowCount();
	}

	public Class getColumnClass(int aColumn)
	{
		if (this.dataCache == null) return null;
		if (aColumn == 0 && this.showStatusColumn) return Integer.class;
		return this.dataCache.getColumnClass(aColumn - columnStartIndex);
	}

	public int insertRow(int afterRow)
	{
		int row = this.dataCache.insertRowAfter(afterRow);
		this.fireTableRowsInserted(row, row);
		return row;
	}

	public int addRow()
	{
		int row = this.dataCache.addRow();
		this.fireTableRowsInserted(row, row);
		return row;
	}
	
	public void deleteRow(int aRow)
	{
		this.dataCache.deleteRow(aRow);
		this.fireTableRowsDeleted(aRow, aRow);
	}

	public int duplicateRow(int aRow)
	{
		int row = this.dataCache.duplicateRow(aRow);
		this.fireTableRowsInserted(row, row);
		return row;
	}

	public void fileImported()
	{
		int row = this.getRowCount();
		this.fireTableRowsInserted(0, row - 1);
	}

	/**
	 *	Clears the EventListenerList and empties the DataStore
	 */
	public void dispose()
	{
		this.listenerList = new EventListenerList();
		if (this.dataCache != null)
		{
			this.dataCache.reset();
			this.dataCache = null;
		}
	}

	/** Return the name of the column as defined by the ResultSetData.
	 */
	public String getColumnName(int aColumn)
	{
		if (this.showStatusColumn && aColumn == 0) return " ";

		try
		{
			String name = this.dataCache.getColumnName(aColumn - this.columnStartIndex);
			return name;
		}
		catch (Exception e)
		{
			return NOT_AVAILABLE;
		}
	}

	public boolean isCellEditable(int row, int column)
	{
		if (this.lockColumn > -1)
		{
			return (column != lockColumn && this.allowEditing);
		}
		else if (this.columnStartIndex > 0 && column < this.columnStartIndex)
		{
			return false;
		}
		else
		{
			return this.allowEditing;// && !SqlUtil.isBlobType(this.dataCache.getColumnType(column));
		}
	}

	
	/**
	 * Clear the locked column. After a call to clearLockedColumn()
	 * all columns (except the status column) are editable 
	 * when the table is in edit mode.
	 * @see #setLockedColumn(int)
	 */
	public void clearLockedColumn()
	{
		this.lockColumn = -1;
	}

	/**
	 * Define a column that may not be edited even if the 
	 * table is in "Edit mode"
	 * @param column the column to be set as non-editable
	 * @see #clearLockedColumn()
	 */
	public void setLockedColumn(int column)
	{
		this.lockColumn = column;
	}

	public void setAllowEditing(boolean aFlag)
	{
		this.allowEditing = aFlag;
	}

	/** 
	 * Return true if the data is sorted in ascending order.
	 * @return True if sorted in ascending order
	 */
	public boolean isSortAscending()
	{
		return sortAscending;
	}

	/**
	 * Return the current sort column
	 * @return the index of the current sort column or -1 if not sorted
	 */
	public int getSortColumn()
	{
		return this.sortColumn;
	}

	/**
	 * Sort the data by the given column. If the data is already
	 * sorted by this column, then the sort order will be reversed
	 */
	public void sortByColumn(int column)
	{
		boolean ascending = true;
		if (this.sortColumn == column) ascending = !this.sortAscending;
		sortByColumn(column, ascending);
	}

	/**
	 * Clears the filter that is currently defined on the underlying 
	 * DataStore. A tableDataChanged Event will be fired after this
	 */
	public void resetFilter()
	{
		if (isSortInProgress()) return;
		
		dataCache.clearFilter();
		// sort() will already fire a tableDataChanged() 
		// if a sort column was defined
		if (!sort()) 
		{
			fireTableDataChanged();
		}
	}
	
	/**
	 * Applys the given filter to the underlying 
	 * DataStore. A tableDataChanged Event will be fired after this
	 */
	public void applyFilter(FilterExpression filter)
	{
		if (isSortInProgress()) return;
		
		dataCache.applyFilter(filter);
		// sort() will already fire a tableDataChanged() 
		// if a sort column was defined
		if (!sort()) 	
		{
			this.fireTableDataChanged();
		}
	}
	
	private synchronized void setSortInProgress(final boolean flag)
	{
		this.sortingInProgress = flag;
	}

	private synchronized boolean isSortInProgress()
	{
		return this.sortingInProgress;
	}
	
	/**
	 *	Re-apply the last sort order defined.
	 *  If no sort order was defined this method does nothing
	 */
	public boolean sort()
	{
		if (this.sortColumn == -1) return false;
		this.sortByColumn(this.sortColumn, this.sortAscending);
		return true;
	}

	/**
	 * Sort the data by the given column in the defined order
	 */
	public void sortByColumn(int aColumn, boolean ascending)
	{
		this.sortAscending = ascending;
		this.sortColumn = aColumn;
		try
		{
			setSortInProgress(true);
			this.dataCache.sortByColumn(aColumn - columnStartIndex, ascending);
		}
		catch (Throwable th)
		{
			LogMgr.logError("DataStoreTableModel.sortByColumn()", "Error when sorting data", th);
		}
		finally
		{
			setSortInProgress(false);
		}
		fireTableChanged(new TableModelEvent(this));
	}

	private boolean sortingInProgress = false;

	public void sortInBackground(WbTable table, int aColumn)
	{
		if (sortingInProgress) return;

		if (aColumn < 0 && aColumn >= this.getColumnCount())
		{
			LogMgr.logWarning("DataStoreTableModel", "Wrong column index for sorting specified!");
			return;
		}

		boolean ascending = true;
		if (this.sortColumn == aColumn) ascending = !this.sortAscending;
		sortInBackground(table, aColumn, ascending);
	}

	/**
	 *	Start a new thread to sort the data.
	 *	Any call to this method while the thread is running, will be ignored
	 */
	public void sortInBackground(final WbTable table, final int aColumn, final boolean ascending)
	{
		if (isSortInProgress()) return;

		Thread t = new WbThread("Data Sort")
		{
			public void run()
			{
				try
				{
					table.sortingStarted();
					sortByColumn(aColumn, ascending);
				}
				finally
				{
					table.sortingFinished();
				}
			}
		};
		t.start();
	}

}