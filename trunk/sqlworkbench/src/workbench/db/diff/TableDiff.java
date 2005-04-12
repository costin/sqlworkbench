/*
 * TableDiff.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2005, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: info@sql-workbench.net
 *
 */
package workbench.db.diff;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import workbench.db.ColumnIdentifier;
import workbench.db.IndexDefinition;
import workbench.db.TableIdentifier;
import workbench.db.report.ReportColumn;
import workbench.db.report.ReportTable;
import workbench.db.report.TagWriter;
import workbench.util.StrBuffer;

/**
 * Compares and evaluates the difference between a reference table
 * and a target table.
 * @author  info@sql-workbench.net
 */
public class TableDiff
{
	public static final String TAG_RENAME_TABLE = "rename";
	public static final String TAG_MODIFY_TABLE = "modify-table";
	public static final String TAG_ADD_COLUMN = "add-column";
	public static final String TAG_REMOVE_COLUMN = "remove-column";
	public static final String TAG_ADD_PK = "add-primary-key";
	public static final String TAG_MODIFY_PK = "modify-primary-key";
	public static final String TAG_REMOVE_PK = "remove-primary-key";
	
	private ReportTable referenceTable;
	private ReportTable targetTable;
	private StrBuffer indent;
	private TagWriter writer;
	
	public TableDiff(ReportTable reference, ReportTable target)
	{
		if (reference == null) throw new NullPointerException("Reference table may not be null");
		if (target == null) throw new NullPointerException("Target table may not be null");
		this.referenceTable = reference;
		this.targetTable = target;
	}

	public void writeXml(Writer out)
		throws IOException
	{
		StrBuffer result = this.getMigrateTargetXml();
		result.writeTo(out);
	}
	
	/**
	 * Return the XML that describes how the target table needs to 
	 * modified in order to get the same structure as the reference table.
	 * An empty string means that there are no differences
	 */
	public StrBuffer getMigrateTargetXml()
	{
		StrBuffer result = new StrBuffer(500);
		TableIdentifier ref = this.referenceTable.getTable();
		TableIdentifier target = this.targetTable.getTable();
		if (this.writer == null) this.writer = new TagWriter();
		StrBuffer colDiff = new StrBuffer(500);
		ArrayList colsToBeAdded = new ArrayList();
		ReportColumn[] refCols = this.referenceTable.getColumns();
		StrBuffer myindent = new StrBuffer(indent);
		myindent.append("  ");
		for (int i=0; i < refCols.length; i++)
		{
			ReportColumn tcol = targetTable.findColumn(refCols[i].getColumn().getColumnName());
			if (tcol == null)
			{
				colsToBeAdded.add(refCols[i]);
			}
			else
			{
				ColumnDiff d = new ColumnDiff(refCols[i], tcol);
				d.setTagWriter(this.writer);
				d.setIndent(myindent);
				StrBuffer diff = d.getMigrateTargetXml();
				if (diff.length() > 0)
				{
					colDiff.append(diff);
					colDiff.append('\n');
				}
			}
		}
		ArrayList colsToBeRemoved = new ArrayList();
		ReportColumn[] tcols = this.targetTable.getColumns();
		for (int i=0; i < tcols.length; i++)
		{
			if (this.referenceTable.findColumn(tcols[i].getColumn().getColumnName()) == null)
			{
				colsToBeRemoved.add(tcols[i]);
			}
		}
		boolean rename = !ref.getTable().equalsIgnoreCase(target.getTable());
		
		List refPk = this.referenceTable.getPrimaryKeyColumns();
		List tPk = this.targetTable.getPrimaryKeyColumns();
		
		if (colDiff.length() == 0 && !rename && colsToBeAdded.size() == 0 
			  && colsToBeRemoved.size() == 0 && refPk.equals(tPk)) 
		{
			return result;
		}
		

		writer.appendOpenTag(result, this.indent, TAG_MODIFY_TABLE, "name", target.getTable());
		result.append('\n');
		if (rename)
		{
			writer.appendOpenTag(result, myindent, TAG_RENAME_TABLE);
			result.append('\n');
			myindent.append("  ");
			writer.appendTag(result, myindent, ReportTable.TAG_TABLE_NAME, this.referenceTable.getTable().getTable());
			myindent.removeFromEnd(2);
			writer.appendCloseTag(result, myindent, TAG_RENAME_TABLE);
		}
		appendAddColumns(result, colsToBeAdded);
		appendRemoveColumns(result, colsToBeRemoved);
		
		String pkTagToUse = null;
		String attr[] = new String[] { "name" };
		String value[] = new String[1];
		List pkcols = null;

		if (refPk.size() == 0 && tPk.size() > 0)
		{
			value[0] = this.targetTable.getPrimaryKeyName();
			pkTagToUse = TAG_REMOVE_PK;
			pkcols = this.targetTable.getPrimaryKeyColumns();
		}
		else if (refPk.size() > 0 && tPk.size() == 0)
		{
			value[0] = this.referenceTable.getPrimaryKeyName();
			pkTagToUse = TAG_ADD_PK;
			pkcols = this.referenceTable.getPrimaryKeyColumns();
		}
		else if (!refPk.equals(tPk))
		{
			value[0] = this.targetTable.getPrimaryKeyName();
			pkTagToUse = TAG_MODIFY_PK;
			pkcols = this.referenceTable.getPrimaryKeyColumns();
		}
		
		writer.appendOpenTag(result, myindent, pkTagToUse, attr, value);
		result.append('\n');
		myindent.append("  ");
		Iterator itr = pkcols.iterator();
		while (itr.hasNext())
		{
			writer.appendTag(result, myindent, ReportColumn.TAG_COLUMN_NAME, (String)itr.next());
		}
		myindent.removeFromEnd(2);
		writer.appendCloseTag(result, myindent, pkTagToUse);
			
		result.append(colDiff);
		appendIndexDiff(result);
		writer.appendCloseTag(result, this.indent, TAG_MODIFY_TABLE);
		return result;
	}
	
	private void appendAddColumns(StrBuffer result, List colsToAdd)
	{
		Iterator itr = colsToAdd.iterator();
		if (!itr.hasNext()) return;
		StrBuffer myindent = new StrBuffer(this.indent);
		myindent.append("  ");
		writer.appendOpenTag(result, myindent, TAG_ADD_COLUMN);
		result.append('\n');
		myindent.append("  ");
		while (itr.hasNext())
		{
			ReportColumn col = (ReportColumn)itr.next();
			col.setNamespace(this.writer.getNamespace());
			col.appendXml(result, myindent, false);
		}
		myindent.removeFromEnd(2);
		writer.appendCloseTag(result, myindent, TAG_ADD_COLUMN);
		result.append('\n');
	}
	
	private void appendRemoveColumns(StrBuffer result, List colsToRemove)
	{
		Iterator itr = colsToRemove.iterator();
		if (!itr.hasNext()) return;
		StrBuffer myindent = new StrBuffer(this.indent);
		myindent.append(indent);
		while (itr.hasNext())
		{
			ReportColumn col = (ReportColumn)itr.next();
			writer.appendEmptyTag(result, myindent, TAG_REMOVE_COLUMN, "name", col.getColumn().getColumnName());
			result.append('\n');
		}
		result.append('\n');
	}
	
	private void appendIndexDiff(StrBuffer result)
	{
		IndexDefinition[] ref = this.referenceTable.getIndexList();
		IndexDefinition[] targ = this.targetTable.getIndexList();
		if (ref == null && targ == null) return;
		IndexDiff id = new IndexDiff(ref, targ);
		id.setTagWriter(this.writer);
		//StrBuffer myindent = new StrBuffer(indent);
		//myindent.append("  ");
		id.setIndent(indent);
		StrBuffer diff = id.getMigrateTargetXml();
		if (diff.length() > 0)
		{
			result.append(diff);
		}
	}
	
	/**
	 *	Set the {@link workbench.db.report.TagWriter} to 
	 *  be used for writing the XML tags
	 */
	public void setTagWriter(TagWriter tagWriter)
	{
		this.writer = tagWriter;
	}
	
	/**
	 *	Set an indent for generating the XML
	 */
	public void setIndent(String ind)
	{
		if (ind == null) this.indent = null;
		this.indent = new StrBuffer(ind);
	}
	
	/**
	 *	Set an indent for generating the XML
	 */
	public void setIndent(StrBuffer ind)
	{
		this.indent = ind;
	}
	
	public static void main(String args[])
	{
		try
		{
			TagWriter w = new TagWriter("wb");
			TableIdentifier t = new TableIdentifier("PERSON");
			List cols = new ArrayList();
			ColumnIdentifier col = new ColumnIdentifier("LASTNAME");
			col.setDbmsType("VARCHAR2(20)");
			col.setIsPkColumn(true);
			col.setIsNullable(false);
			col.setComment("Nachname der Person");
			cols.add(col);
			
			col = new ColumnIdentifier("FIRSTNAME");
			col.setDbmsType("VARCHAR2(20)");
			col.setDefaultValue("Hans");
			cols.add(col);
			
			col = new ColumnIdentifier("CITY");
			col.setDbmsType("VARCHAR2(20)");
			col.setIsPkColumn(false);
			col.setIsNullable(true);
			cols.add(col);
//
//			col = new ColumnIdentifier("ZIP");
//			col.setDbmsType("VARCHAR2(20)");
//			col.setIsPkColumn(false);
//			col.setIsNullable(true);
//			cols.add(col);
			
			ReportTable ref = new ReportTable(t);
			ref.setColumns(cols);

			TableIdentifier s = new TableIdentifier("PERSON2");
			cols = new ArrayList();
			col = new ColumnIdentifier("LASTNAME");
			col.setDbmsType("VARCHAR2(15)");
			col.setIsPkColumn(false);
			col.setIsNullable(true);
			cols.add(col);
			
			col = new ColumnIdentifier("FIRSTNAME");
			col.setDbmsType("VARCHAR2(20)");
			col.setIsNullable(false);
			cols.add(col);

//			col = new ColumnIdentifier("CITY");
//			col.setDbmsType("VARCHAR2(20)");
//			col.setIsNullable(false);
//			cols.add(col);
//			
//			col = new ColumnIdentifier("ZIP");
//			col.setDbmsType("VARCHAR2(20)");
//			col.setIsNullable(false);
//			cols.add(col);
			
			ReportTable target = new ReportTable(s);
			target.setColumns(cols);
			TableDiff d = new TableDiff(ref, target);
			d.setIndent("  ");
			//d.setTagWriter(w);
			System.out.println(d.getMigrateTargetXml());
		}
		catch (Throwable th)
		{
			th.printStackTrace();
		}
		System.out.println("Done.");
	}
}
