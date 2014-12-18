/*
 * ColumnReference.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2004, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: info@sql-workbench.net
 *
 */
package workbench.db.report;

import workbench.util.StrBuffer;

/**
 *
 * @author  info@sql-workbench.net
 */
public class ColumnReference
{
	public static final String TAG_REFERENCE = "references";
	public static final String TAG_CONSTRAINT_NAME = "constraint-name";
	public static final String TAG_UPDATE_RULE = "update-rule";
	public static final String TAG_DELETE_RULE = "delete-rule";
	
	private String fkName;
	private String foreignColumn;
	private String foreignTable;
	private String updateRule;
	private String deleteRule;
	private TagWriter tagWriter = new TagWriter();
	
	public ColumnReference()
	{
	}
	
	public void setNamespace(String namespace)
	{
		this.tagWriter.setNamespace(namespace);
	}
	public void setConstraintName(String name) { this.fkName = name; }
	public void setForeignColumn(String col) { this.foreignColumn = col; }
	public void setForeignTable(String tbl) { this.foreignTable = tbl; }
	public void setUpdateRule(String rule) { this.updateRule = rule; }
	public void setDeleteRule(String rule) { this.deleteRule = rule; }
	
	public StrBuffer getXml(StrBuffer indent)
	{
		StrBuffer result = new StrBuffer(250);
		StrBuffer myindent = new StrBuffer(indent);
		myindent.append("  ");
		tagWriter.appendOpenTag(result, indent, TAG_REFERENCE);
		result.append('\n');
		
		tagWriter.appendTag(result, myindent, ReportTable.TAG_TABLE_NAME, this.foreignTable);
		tagWriter.appendTag(result, myindent, ReportColumn.TAG_COLUMN_NAME, this.foreignColumn);
		tagWriter.appendTag(result, myindent, TAG_CONSTRAINT_NAME, this.fkName);
		tagWriter.appendTag(result, myindent, TAG_DELETE_RULE, this.deleteRule);
		tagWriter.appendTag(result, myindent, TAG_UPDATE_RULE, this.updateRule);
		
		tagWriter.appendCloseTag(result, indent, TAG_REFERENCE);
		
		return result;
	}
	
}