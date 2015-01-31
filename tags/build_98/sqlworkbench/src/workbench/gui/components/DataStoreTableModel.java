/*
 * DataStoreTableModel.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2008, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.components;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.sql.SQLException;
import java.sql.Types;
import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import workbench.db.ConnectionProfile;
import workbench.db.WbConnection;
import workbench.gui.WbSwingUtilities;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.storage.DataStore;
import workbench.storage.NamedSortDefinition;
import workbench.storage.ResultInfo;
import workbench.storage.SortDefinition;
import workbench.storage.filter.FilterExpression;
import workbench.util.ConverterException;
import workbench.util.SqlUtil;
import workbench.util.StringUtil;
import workbench.util.WbThread;

/**
 * TableModel for displaying the contents of a {@link workbench.storage.DataStore }
 * 
 * @author support@sql-workbench.net
 */
public class DataStoreTableModel
	extends AbstractTableModel
{
	private DataStore dataCache;
	private boolean showStatusColumn = false;
	private int columnStartIndex = 0;
	
	private int lockColumn = -1;

	private SortDefinition sortColumns = new SortDefinition();
	
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
			LogMgr.logError("DataStoreTableModel.getValue()", "Error retrieving value at: " + row + "/" + col, e);
			return "Error";
		}
	}

	public int findColumn(String aColname)
	{
		int index = this.dataCache.getColumnIndex(aColname);
		if (index == -1) return -1;
		return index + this.columnStartIndex;
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
			}
			else
			{
				this.columnStartIndex = 0;
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

	private boolean isNull(Object value, int column)
	{
		if (value == null) return true;
		String s = value.toString(); 
		int type = this.dataCache.getColumnType(column);
		if (SqlUtil.isCharacterType(type))
		{
			WbConnection con = this.dataCache.getOriginalConnection();
			ConnectionProfile profile = (con != null ? con.getProfile() : null);
			if (profile == null || profile.getEmptyStringIsNull())
			{
				return (s.length() == 0);
			}
			return false;
		}
		return StringUtil.isEmptyString(s);
	}
	
	public void setValueAt(Object aValue, int row, int column)
	{
		// Updates to the status column shouldn't happen anyway ....
		if (this.showStatusColumn && column == 0) return;

		if (isNull(aValue, column - this.columnStartIndex))
		{
			this.dataCache.setValue(row, column - this.columnStartIndex, null);
		}
		else
		{
			try
			{
				this.dataCache.setInputValue(row, column - this.columnStartIndex, aValue);
			}
			catch (ConverterException ce)
			{
				int type = this.getColumnType(column);
				LogMgr.logError(this, "Error converting input >" + aValue + "< to column type " + SqlUtil.getTypeName(type) + " (" + type + ")", ce);
				Toolkit.getDefaultToolkit().beep();
				String msg = ResourceMgr.getString("MsgConvertError");
				msg = msg + "\r\n" + ce.getLocalizedMessage();
				WbSwingUtilities.showErrorMessage(msg);
				return;
			}
		}
		WbSwingUtilities.invoke(new Runnable()
		{
			public void run()
			{
				fireTableDataChanged();
			}
		});
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
	 * 
	 * @param aColumn the column index
	 * 
	 * @return the width of the column as defined by the DataStore or 0
	 * @see workbench.storage.DataStore#getColumnDisplaySize(int)
	 * @see #findColumn(String)
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
	
	public void deleteRow(int aRow, boolean withDependencies)
		throws SQLException
	{
		if (withDependencies)
		{
			this.dataCache.deleteRowWithDependencies(aRow);
		}
		else
		{
			this.dataCache.deleteRow(aRow);
		}
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
			return "(n/a)";
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
	
	private void setSortInProgress(final boolean flag)
	{
		this.sortingInProgress = flag;
	}

	private boolean isSortInProgress()
	{
		return this.sortingInProgress;
	}


	/** 
	 * Return true if the data is sorted in ascending order.
	 * @return True if sorted in ascending order
	 */
	public boolean isSortAscending(int col)
	{
		return this.sortColumns.isSortAscending(col - columnStartIndex);
	}

	
	public boolean isPrimarySortColumn(int col)
	{
		return this.sortColumns.isPrimarySortColumn(col - columnStartIndex);
	}
	
	/**
	 * Check if the table is sorted by a column
	 * @return true if the given column is a sort column
	 * @see #isSortAscending(int)
	 */
	public boolean isSortColumn(int col)
	{
		return this.sortColumns.isSortColumn(col - columnStartIndex);
	}

	/**
	 * Returns a snapshot of the current sort columns identified
	 * by their names instead of their column index (as done by SortDefinition)
	 * 
	 * @return the current sort definition with named columns
	 */
	public NamedSortDefinition getSortDefinition()
	{
		return new NamedSortDefinition(this.dataCache, this.sortColumns);
	}
	
	public void setSortDefinition(NamedSortDefinition definition)
	{
		if (definition == null) return;
		SortDefinition newSort = definition.getSortDefinition(dataCache);
		if (!newSort.equals(this.sortColumns))
		{
			this.sortColumns = newSort;
			applySortColumns();
		}
	}
	
	/**
	 * Sort the data by the given column. If the data is already
	 * sorted by this column, then the sort order will be reversed
	 */
	public void sortByColumn(int column)
	{
		// if the column was not sorted at all isSortAscending will return false
		// thus negating ascending will sort ascending for non-sorted
		// columns and will toggle the sort direction for an existing sort column
		boolean ascending = !isSortAscending(column);
		sortByColumn(column, ascending, false);
	}
	
	/**
	 *	Re-apply the last sort order defined.
	 *  If no sort order was defined this method does nothing
	 */
	public boolean sort()
	{
		if (this.sortColumns == null) return false;
		applySortColumns();
		return true;
	}

	public void removeSortColumn(int column)
	{
		boolean isPrimaryColumn = this.sortColumns.isPrimarySortColumn(column);
		this.sortColumns.removeSortColumn(column);
		
		// if the primary (== first) column was removed
		// we have to re-apply the sort definition
		if (isPrimaryColumn)
		{
			applySortColumns();
		}
	}
	
	/**
	 * Sort the data by the given column in the defined order
	 */
	public void sortByColumn(int column, boolean ascending, boolean addSortColumn)
	{
		if (addSortColumn)
		{
			sortColumns.addSortColumn(column - columnStartIndex, ascending);
		}
		else
		{
			sortColumns.setSortColumn(column - columnStartIndex, ascending);
		}
		applySortColumns();
	}
	
	private void applySortColumns()
	{
		if (this.sortColumns == null) return;
		if (this.dataCache == null) return;
		
		synchronized (this.dataCache)
		{
			try
			{
				setSortInProgress(true);
				this.dataCache.sort(this.sortColumns);
			}
			catch (Throwable th)
			{
				LogMgr.logError("DataStoreTableModel.sortByColumn()", "Error when sorting data", th);
			}
			finally
			{
				setSortInProgress(false);
			}
		}
		final TableModelEvent event = new TableModelEvent(this);
		WbSwingUtilities.invoke(new Runnable()
		{
			public void run()
			{
				fireTableChanged(event);
			}
		});
	}

	private boolean sortingInProgress = false;

	public void sortInBackground(WbTable table, int aColumn, boolean addSortColumn)
	{
		if (sortingInProgress) return;

		if (aColumn < 0 && aColumn >= this.getColumnCount())
		{
			LogMgr.logWarning("DataStoreTableModel", "Wrong column index for sorting specified!");
			return;
		}
		boolean ascending = !this.isSortAscending(aColumn);
		sortInBackground(table, aColumn, ascending, addSortColumn);
	}

	/**
	 *	Start a new thread to sort the data.
	 *	Any call to this method while the thread is running, will be ignored
	 */
	public void sortInBackground(final WbTable table, final int aColumn, final boolean ascending, final boolean addSortColumn)
	{
		if (isSortInProgress()) return;

		Thread t = new WbThread("Data Sort")
		{
			public void run()
			{
				try
				{
					table.sortingStarted();
					sortByColumn(aColumn, ascending, addSortColumn);
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