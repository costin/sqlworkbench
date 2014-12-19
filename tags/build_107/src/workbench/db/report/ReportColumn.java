/*
 * ReportColumn.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2009, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db.report;

import workbench.db.ColumnIdentifier;
import workbench.util.SqlUtil;
import workbench.util.StrBuffer;
import workbench.util.StringUtil;

/**
 *
 * @author  support@sql-workbench.net
 */
public class ReportColumn
{
	public static final String TAG_COLUMN_DEFINITION = "column-def";
	public static final String TAG_COLUMN_NAME = "column-name";
	public static final String TAG_COLUMN_DBMS_TYPE = "dbms-data-type";
	public static final String TAG_COLUMN_JAVA_TYPE_NAME = "java-sql-type-name";
	public static final String TAG_COLUMN_JAVA_TYPE = "java-sql-type";
	public static final String TAG_COLUMN_JAVA_CLASS = "java-class";

	public static final String TAG_COLUMN_SIZE = "dbms-data-size";
	public static final String TAG_COLUMN_DIGITS = "dbms-data-digits";
	public static final String TAG_COLUMN_POSITION = "dbms-position";
	public static final String TAG_COLUMN_DEFAULT = "default-value";
	public static final String TAG_COLUMN_NULLABLE = "nullable";
	public static final String TAG_COLUMN_PK = "primary-key";
	public static final String TAG_COLUMN_COMMENT = "comment";

	private ColumnReference fk;
	private ColumnIdentifier column;
	private TagWriter tagWriter = new TagWriter();
	private boolean isRealColumn = true;
	private boolean isReferenced = false;

	public ReportColumn(ColumnIdentifier col)
	{
		this.column = col;
	}

	public ColumnIdentifier getColumn()
	{
		return this.column;
	}

	public ColumnReference getForeignKey()
	{
		return this.fk;
	}

	public void setIsReferenced(boolean flag)
	{
		isReferenced = flag;
	}
	public boolean isReferenced()
	{
		return isReferenced;
	}

	public void setForeignKeyReference(ColumnReference ref)
	{
		this.fk = ref;
	}

	public void appendXml(StrBuffer result, StrBuffer indent)
	{
		appendXml(result, indent, true);
	}

	public void appendXml(StrBuffer result, StrBuffer indent, boolean includePosition)
	{
		appendXml(result, indent, includePosition, TAG_COLUMN_DEFINITION, false);
	}

	public void appendXml(StrBuffer result, StrBuffer indent, boolean includePosition, String mainTagToUse, boolean shortInfo)
	{
		StrBuffer myindent = new StrBuffer(indent);

		myindent.append("  ");
		if (shortInfo)
		{
			tagWriter.appendOpenTag(result, indent, mainTagToUse);
		}
		else
		{
			tagWriter.appendOpenTag(result, indent, mainTagToUse, "name", StringUtil.trimQuotes(this.column.getColumnName()));
		}

		result.append('\n');

		if (includePosition) tagWriter.appendTag(result, myindent, TAG_COLUMN_POSITION, this.column.getPosition());
		if (!shortInfo) tagWriter.appendTag(result, myindent, TAG_COLUMN_NAME, this.column.getColumnName());
		tagWriter.appendTag(result, myindent, TAG_COLUMN_DBMS_TYPE, this.column.getDbmsType());
		if (isRealColumn && !shortInfo) tagWriter.appendTag(result, myindent, TAG_COLUMN_PK, this.column.isPkColumn());
		if (isRealColumn) tagWriter.appendTag(result, myindent, TAG_COLUMN_NULLABLE, this.column.isNullable());
		if (isRealColumn) tagWriter.appendTag(result, myindent, TAG_COLUMN_DEFAULT, this.column.getDefaultValue(), true);
		tagWriter.appendTag(result, myindent, TAG_COLUMN_SIZE, this.column.getColumnSize());
		tagWriter.appendTag(result, myindent, TAG_COLUMN_DIGITS, this.column.getDigitsDisplay());
		if (!shortInfo) tagWriter.appendTag(result, myindent, TAG_COLUMN_JAVA_TYPE, this.column.getDataType());
		tagWriter.appendTag(result, myindent, TAG_COLUMN_JAVA_TYPE_NAME, SqlUtil.getTypeName(this.column.getDataType()));
		if (!shortInfo) tagWriter.appendTag(result, myindent, TAG_COLUMN_COMMENT, this.column.getComment(), true);

		if (this.fk != null)
		{
			result.append(fk.getXml(myindent));
		}
		tagWriter.appendCloseTag(result, indent, mainTagToUse);
	}

	public void setRealColumn(boolean flag)
	{
		this.isRealColumn = flag;
	}

	public String toString()
	{
		return column.toString();
	}
}