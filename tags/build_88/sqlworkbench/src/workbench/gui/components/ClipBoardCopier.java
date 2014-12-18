/*
 * ClipBoardCopier.java
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
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.StringWriter;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import workbench.WbManager;
import workbench.db.ColumnIdentifier;
import workbench.gui.WbSwingUtilities;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.storage.DataStore;

/**
 * A class to copy the data of a {@link workbench.components.WbTable} to 
 * the clipboard. Either as tab-separated text of SQL Statements.
 *
 * @author support@sql-workbench.net
 */
public class ClipBoardCopier
{
	private WbTable client;
	
	public ClipBoardCopier(WbTable t)
	{
		this.client = t;
	}
	
	/**
	 *	Copy data from the table as tab-delimited into the clipboard
	 *	@param includeHeaders if true, then a header line with the column names is copied as well
	 *  @param selectedOnly if true, then only selected rows are copied, else all rows
	 *
	 */
	public void copyDataToClipboard(boolean includeHeaders, boolean selectedOnly, final boolean showSelectColumns)
	{
		if (this.client.getRowCount() <= 0) return;
		
		List columnsToCopy = null;
		if (showSelectColumns)
		{
			// Display column selection dialog
      ColumnSelectionResult result = this.selectColumns(includeHeaders, selectedOnly, true, client.getSelectedRowCount() > 0);
			if (result == null) return;
			columnsToCopy = result.columns;
      includeHeaders = result.includeHeaders;
      selectedOnly = result.selectedOnly;
		}

		try
		{
			DataStore ds = this.client.getDataStore();
			StringWriter out = null;
			int count = this.client.getRowCount();
			int[] rows = null;
			if (selectedOnly)
			{
				rows = this.client.getSelectedRows();
				count = rows.length;
			}
			
			out = new StringWriter(count * 250);
			// Do not use StringUtil.LINE_TERMINATOR for the line terminator
			// because for some reason this creates additional empty lines
			// under Windows
			ds.writeDataString(out, "\t", "\n", includeHeaders, rows, columnsToCopy);
			Clipboard clp = Toolkit.getDefaultToolkit().getSystemClipboard();
			WbSwingUtilities.showWaitCursorOnWindow(this.client);
			StringSelection sel = new StringSelection(out.toString());
			clp.setContents(sel, sel);
		}
		catch (Throwable ex)
		{
			if (ex instanceof OutOfMemoryError)
			{
				WbManager.getInstance().showOutOfMemoryError();
			}
			LogMgr.logError(this, "Could not copy text data to clipboard", ex);
		}
		WbSwingUtilities.showDefaultCursorOnWindow(this.client);
	}
	
	public void copyAsSqlInsert(boolean selectedOnly, boolean showSelectColumns)
	{
		this.copyAsSql(false, selectedOnly, showSelectColumns, false);
	}

	public void copyAsSqlDeleteInsert(boolean selectedOnly, boolean showSelectColumns)
	{
		this.copyAsSql(false, selectedOnly, showSelectColumns, true);
	}

	/**
	 * Copy the data of the client table as SQL UPDATE statements to the clipboard.
	 * Before copying, the primary key columns of the underlying {@link workbench.storage.DataStore}
	 * are checked. If none are present, the user is prompted to select the key columns
	 *
	 * @see workbench.storage.DataStore#hasPkColumns()
	 * @see workbench.components.WbTable#detectDefinedPkColumns()
	 * @see #copyAsSql(boolean, boolean, boolean, boolean)
	 */
	public void copyAsSqlUpdate(boolean selectedOnly, boolean showSelectColumns)
	{
		DataStore ds = this.client.getDataStore();
		if (ds == null) return;

		boolean result = true;
		// we need decent PK columns in order to create update statements
		if (!ds.hasPkColumns()) client.detectDefinedPkColumns();
		if (!ds.hasPkColumns())
		{
			result = this.client.selectKeyColumns();
		}
		if (result)
		{
			copyAsSql(true, selectedOnly, showSelectColumns, false);
		}
	}
	

	/**
	 * 	Copy the data of the client table into the clipboard using SQL statements
	 */
	public void copyAsSql(boolean useUpdate, boolean selectedOnly, boolean showSelectColumns, boolean includeDelete)
	{
		if (this.client.getRowCount() <= 0) return;
		
		DataStore ds = this.client.getDataStore();
		if (ds == null) return;

		List columnsToInclude = null;
		if (showSelectColumns)
		{
      ColumnSelectionResult result = this.selectColumns(false, selectedOnly, false, client.getSelectedRowCount() > 0);
			if (result == null) return;
			columnsToInclude = result.columns;
      selectedOnly = result.selectedOnly;
		}
			
		try
		{
			WbSwingUtilities.showWaitCursorOnWindow(this.client);
			int rows[] = null;
			if (selectedOnly) rows = this.client.getSelectedRows();

			String data;
			if (useUpdate)
			{
				data = ds.getDataAsSqlUpdate(rows, columnsToInclude);
			}
			else if (includeDelete)
			{
				data = ds.getDataAsSqlDeleteInsert(rows, columnsToInclude);
			}
			else
			{
				data = ds.getDataAsSqlInsert(rows, columnsToInclude);
			}
			Clipboard clp = Toolkit.getDefaultToolkit().getSystemClipboard();
			StringSelection sel = new StringSelection(data);
			clp.setContents(sel, sel);
		}
		catch (Throwable e)
		{
			if (e instanceof OutOfMemoryError)
			{
				WbManager.getInstance().showOutOfMemoryError();
			}
			LogMgr.logError(this, "Error when copying SQL inserts", e);
		}
		WbSwingUtilities.showDefaultCursorOnWindow(this.client);
	}
	
	/**
	 *	A general purpose method to select specific columns from the result set
	 *  this is e.g. used for copying data to the clipboard
	 *
	 */
	public ColumnSelectionResult selectColumns(boolean includeHeader, boolean selectedOnly, boolean showHeaderSelection, boolean showSelectedRowsSelection)
	{
		DataStore ds = this.client.getDataStore();
		if (ds == null) return null;

    ColumnSelectionResult result = new ColumnSelectionResult();
    result.includeHeaders = includeHeader;
    result.selectedOnly = selectedOnly;

		ColumnIdentifier[] originalCols = ds.getColumns();
		ColumnSelectorPanel panel = new ColumnSelectorPanel(originalCols, includeHeader, selectedOnly, showHeaderSelection, showSelectedRowsSelection);
		panel.selectAll();
		int choice = JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(this.client), panel, ResourceMgr.getString("MsgSelectColumnsWindowTitle"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (choice == JOptionPane.OK_OPTION)
		{
			result.columns = panel.getSelectedColumns();
      result.includeHeaders = panel.includeHeader();
      result.selectedOnly = panel.selectedOnly();
		}
    else
    {
        result = null;
    }
		return result;
	}

}

class ColumnSelectionResult
{
    public boolean includeHeaders;
    public boolean selectedOnly;
    public List columns;
}