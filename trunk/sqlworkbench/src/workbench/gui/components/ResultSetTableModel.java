package workbench.gui.components;

import java.awt.Toolkit;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import workbench.db.WbConnection;
import workbench.exception.WbException;
import workbench.log.LogMgr;
import workbench.storage.DataStore;
import workbench.util.SqlUtil;


/** 
 * TableModel for displaying the contents of a ResultSet.
 * The data is cached in a {@link workbench.storage.DataStore }
 */
public class ResultSetTableModel 
	extends AbstractTableModel
{
	private DataStore dataCache;
	private boolean showStatusColumn = false;
	private int statusOffset = 0;
	public static final String NOT_AVAILABLE = "(n/a)";
	
	public ResultSetTableModel(ResultSet aResultSet) 
		throws SQLException, WbException
	{
		this(aResultSet, null);
	}
	
	public ResultSetTableModel(DataStore aDataStore)
		throws NullPointerException
	{
		if (aDataStore == null) throw new NullPointerException("DataStore cannot be null");
		this.dataCache = aDataStore;
	}
	
	public ResultSetTableModel(ResultSet aResultSet, List aColumnList) 
		throws SQLException, WbException
	{
		this.dataCache = new DataStore(aResultSet, aColumnList);
	}
	
	public DataStore getDataStore()
	{
		return this.dataCache;
	}
	/**
	 *	Return the contents of the field at the given position
	 *	in the result set.
	 *	@parm row - The row to get. Counting starts at zero.
	 *	@parm col - The column to get. Counting starts at zero.
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
			result = this.dataCache.getValue(row, col - this.statusOffset);
			return result;
		}
		catch (Exception e)
		{
			StringBuffer msg = new StringBuffer("Error @");
			msg.append(row);
			msg.append('/');
			msg.append(col);
			msg.append(" - ");
			if (e.getMessage() == null)
			{
				msg.append(e.getClass().getName());
			}
			else
			{
				msg.append(e.getMessage());
			}
			return msg.toString();
		}
	}	

	public int findColumn(String aColname)
	{
		int index = -1;
		try
		{
			index = this.dataCache.getColumnIndex(aColname);
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
	
	public void setShowStatusColumn(boolean aFlag)
	{
		if (aFlag == this.showStatusColumn) return;
		this.showStatusColumn = aFlag;
		if (this.showStatusColumn)
			this.statusOffset = 1;
		else 
			this.statusOffset = 0;
		this.fireTableStructureChanged();
	}
	
	public boolean getShowStatusColumn() { return this.showStatusColumn; }
	
	public boolean isUpdateable() { return this.dataCache.isUpdateable(); }
	
	public void setValueAt(Object aValue, int row, int column)
	{
		if (this.showStatusColumn && column == 0) return;
		
		try
		{
			if (this.isUpdateable())
			{
				if (aValue == null || aValue.toString().length() == 0) 
				{
					this.dataCache.setNull(row, column - this.statusOffset);
				}
				else 
				{
					try
					{
						Object realValue = this.convertCellValue(aValue, row, column);
						this.dataCache.setValue(row, column - this.statusOffset, realValue);
					}
					catch (Exception ce)
					{
						LogMgr.logError(this, "Error converting input >" + aValue + "< to column type (" + this.getColumnType(column) + ") ", ce);
						Toolkit.getDefaultToolkit().beep();
						return;
					}
				}
				fireTableDataChanged();
			}
		}
		catch (Exception e)
		{
			System.err.println("Error in setValueAt");
			e.printStackTrace(System.err);
		}
	}
	
	/**
	 *	Convert a String to the internal storage class for this cell.
	 *	An empty string will be converted to DataStore.NULL_VALUE.
	 */
	private Object convertCellValue(Object aValue, int aRow, int aColumn)
		throws Exception
	{
		Object lastValue = this.getValueAt(aRow, aColumn);
		String current = (String)aValue;
		int type = this.getColumnType(aColumn);
		switch (type)
		{
			case Types.BIGINT:
				return new BigInteger(((String)aValue).trim());
			case Types.INTEGER:
			case Types.SMALLINT:
				return Integer.valueOf(((String)aValue).trim());
			case Types.NUMERIC:
			case Types.DECIMAL:
				return new BigDecimal(((String)aValue).trim());
			case Types.DOUBLE:
				return new Double(((String)aValue).trim());
			case Types.REAL:
			case Types.FLOAT:
				return new Float(((String)aValue).trim());
			case Types.CHAR:
			case Types.VARCHAR:
				return (String)aValue;
			case Types.DATE:
				DateFormat df = new SimpleDateFormat();
				return df.parse(((String)aValue).trim());
			case Types.TIMESTAMP:
				return java.sql.Timestamp.valueOf(((String)aValue).trim());
			default:
				return aValue;
		}
	}
		
	/**
	 *	Return the number of columns in the model.
	 */
	public int getColumnCount()
	{
		return this.dataCache.getColumnCount() + this.statusOffset;
	}

	public int getColumnWidth(int aColumn)
	{
		if (this.showStatusColumn && aColumn == 0) return 5;
		try
		{
			return this.dataCache.getColumnDisplaySize(aColumn - this.statusOffset);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return 100;
		}
	}

	public String getColumnTypeName(int aColumn)
	{
		if (aColumn == 0) return "";
		return SqlUtil.getTypeName(this.getColumnType(aColumn));
	}
	
	public int getColumnType(int aColumn)
	{
		if (this.showStatusColumn && aColumn == 0) return 0;
		
		try
		{
			return this.dataCache.getColumnType(aColumn - this.statusOffset);
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
		return this.dataCache.getRowCount();
	}

	public Class getColumnClass(int aColumn)
	{
		if (this.showStatusColumn && aColumn == 0) return Integer.class;
		
		int type = this.getColumnType(aColumn);
		switch (type)
		{
			case Types.BIGINT:
			case Types.INTEGER:
				return BigInteger.class;
			case Types.SMALLINT:
				return Integer.class;
			case Types.NUMERIC:
			case Types.DECIMAL:
				return BigDecimal.class;
			case Types.DOUBLE:
				return Double.class;
			case Types.REAL:
			case Types.FLOAT:
				return Float.class;
			case Types.CHAR:
			case Types.VARCHAR:
				return String.class;
			case Types.DATE:
				return java.util.Date.class;
			case Types.TIMESTAMP:
				return java.sql.Timestamp.class;
			default:
				return Object.class;
		}
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
	
	public void dispose()
	{
		this.dataCache = null;
	}
	
	public void finalize()
	{
		this.dispose();
	}
	
	/** Return the name of the column as defined by the ResultSetData.
	 */	
	public String getColumnName(int aColumn)
	{
		if (this.showStatusColumn && aColumn == 0) return " ";
		
		try
		{
			String name = this.dataCache.getColumnName(aColumn - this.statusOffset);
			if (name == null || name.length() == 0)
			{
				name = "Col" + aColumn;
			}
			return name;
		}
		catch (Exception e)
		{
			return NOT_AVAILABLE;
		}
	}
	
	public StringBuffer getRowData(int aRow)
	{
		int count = this.getColumnCount();
		StringBuffer result = new StringBuffer(count * 20);
		int start = 0;
		if (this.showStatusColumn) start = 1;
		for (int c=start; c < count; c++)
		{
			Object value = this.getValueAt(aRow, c);
			if (value != null) result.append(value.toString());
			if (c < count) result.append('\t');
		}
		return result;
	}
	
	public boolean isCellEditable(int row, int column)
	{
		if (this.showStatusColumn)
			return (column != 0);
		else
			return true;
	}
	
}
