/*
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 * Copyright 2002-2008, Thomas Kellerer
 *
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */

package workbench.db.importer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import workbench.db.ColumnIdentifier;
import workbench.util.StringUtil;

/**
 *
 * @author Thomas Kellerer
 */
public class ImportFileColumn
{
	private ColumnIdentifier tableColumn;

	private int targetIndex = -1;
	private int dataWidth = -1;
	private Pattern columnFilter;

	public static final ImportFileColumn SKIP_COLUMN =
		new ImportFileColumn(new ColumnIdentifier(RowDataProducer.SKIP_INDICATOR));

	public ImportFileColumn(ColumnIdentifier col)
	{
		if (col == null) throw new NullPointerException("Column may not be null");
		this.tableColumn = col;
	}

	public ColumnIdentifier getColumn()
	{
		return tableColumn;
	}

	public Pattern getColumnFilter()
	{
		return columnFilter;
	}

	public void setColumnFilter(Pattern pattern)
	{
		this.columnFilter = pattern;
	}

	public int getTargetIndex()
	{
		return targetIndex;
	}

	public void setTargetIndex(int index)
	{
		this.targetIndex = index;
	}

	public int getDataWidth()
	{
		return dataWidth;
	}

	public void setDataWidth(int width)
	{
		this.dataWidth = width;
	}

	@Override
	public boolean equals(Object obj)
	{
//		System.out.println(tableColumn.getColumnName() + " equals " + obj.toString());
		if (obj instanceof ImportFileColumn)
		{
			final ImportFileColumn other = (ImportFileColumn) obj;
			return this.tableColumn.equals(other.tableColumn);
		}
		else if (obj instanceof ColumnIdentifier)
		{
			return this.tableColumn.equals(obj);
		}
		else if (obj instanceof String)
		{
			String thisName = StringUtil.trimQuotes(tableColumn.getColumnName());
			return thisName.equalsIgnoreCase(StringUtil.trimQuotes((String)obj));
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return tableColumn.hashCode();
	}

	/**
	 * Creates a List that implements a different indexOf() method.
	 * The standard ArrayList implementation calls obj.equals(elementData[i]) to
	 * test if the passed object is in the list. If we want to compare different
	 * objects (String, ColumnIdentifier, ImportFileColumn) to the elements in
	 * the list this does not work (even though ImportFileColumn.equals() handles
	 * all three cases).
	 * <br>
	 * So the list generated by createLis() calls elementData[i].equals(parameter)
	 * to test if the passed parameter is contained in the list.
	 * <br/>
	 * This makes it easier to search for a column name by String or ColumnIdentifier
	 * in the list.
	 */
	public static List<ImportFileColumn> createList()
	{
		return new ArrayList<ImportFileColumn>()
		{
			public int indexOf(Object elem)
			{
				if (elem == null) return -1;
				int size = size();
				for (int i = 0; i < size; i++)
				{
					if (get(i).equals(elem))
					{
						return i;
					}
				}
				return -1;
			}
		};
	}

	public String toString()
	{
		return tableColumn.getColumnName() + "@" + targetIndex;
	}
}