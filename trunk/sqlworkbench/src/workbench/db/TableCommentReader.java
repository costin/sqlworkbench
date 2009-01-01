/*
 * TableCommentReader.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2009, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db;

import java.sql.ResultSet;
import java.sql.Savepoint;
import java.util.List;
import workbench.log.LogMgr;
import workbench.resource.Settings;
import workbench.util.SqlUtil;
import workbench.util.StringUtil;

/**
 * @author support@sql-workbench.net
 */
public class TableCommentReader
{
	/**
	 * Return the SQL that is needed to re-create the comment on the given table.
	 * The syntax to be used, can be configured in the TableCommentStatements.xml file.
	 */
	public String getTableCommentSql(WbConnection dbConnection, TableIdentifier table)
	{
		String commentStatement = dbConnection.getMetadata().metaSqlMgr.getTableCommentSql();
		if (StringUtil.isBlank(commentStatement))
		{
			return null;
		}
		String comment = getTableComment(dbConnection, table);
		String result = null;
		if (Settings.getInstance().getIncludeEmptyComments() || StringUtil.isNonBlank(comment))
		{
			result = StringUtil.replace(commentStatement, MetaDataSqlManager.COMMENT_TABLE_PLACEHOLDER, table.getTableName());
			result = StringUtil.replace(result, MetaDataSqlManager.COMMENT_SCHEMA_PLACEHOLDER, table.getSchema());
			result = StringUtil.replace(result, MetaDataSqlManager.COMMENT_PLACEHOLDER, comment == null ? "" : comment.replace("'", "''"));
		}
		return result;
	}

	public String getTableComment(WbConnection dbConnection, TableIdentifier tbl)
	{
		TableIdentifier table = tbl.createCopy();
		table.adjustCase(dbConnection);
		ResultSet rs = null;
		String result = null;
		Savepoint sp = null;
		try
		{
			if (dbConnection.getDbSettings().useSavePointForDML())
			{
				sp = dbConnection.setSavepoint();
			}
			rs = dbConnection.getSqlConnection().getMetaData().getTables(table.getRawCatalog(), table.getRawSchema(), table.getRawTableName(), null);
			if (rs.next())
			{
				result = rs.getString("REMARKS");
			}
			dbConnection.releaseSavepoint(sp);
		}
		catch (Exception e)
		{
			dbConnection.rollback(sp);
			LogMgr.logError("DbMetadata.getTableComment()", "Error retrieving comment for table " + table.getTableExpression(), e);
			result = null;
		}
		finally
		{
			SqlUtil.closeResult(rs);
		}

		return result;
	}

	/**
	 * Return the SQL that is needed to re-create the comment on the given columns.
	 * The syntax to be used, can be configured in the ColumnCommentStatements.xml file.
	 */
	public StringBuilder getTableColumnCommentsSql(WbConnection con, TableIdentifier table, List<ColumnIdentifier> columns)
	{
		String columnStatement = con.getMetadata().metaSqlMgr.getColumnCommentSql();
		if (StringUtil.isBlank(columnStatement)) return null;
		StringBuilder result = new StringBuilder(columns.size() * 25);
		for (ColumnIdentifier col : columns)
		{
			String column = col.getColumnName();
			String comment = col.getComment();
			if (Settings.getInstance().getIncludeEmptyComments() || StringUtil.isNonBlank(comment))
			{
				try
				{
					String commentSql = StringUtil.replace(columnStatement, MetaDataSqlManager.COMMENT_TABLE_PLACEHOLDER, table.getTableName());
					commentSql = StringUtil.replace(commentSql, MetaDataSqlManager.COMMENT_SCHEMA_PLACEHOLDER, table.getSchema());
					commentSql = StringUtil.replace(commentSql, MetaDataSqlManager.COMMENT_COLUMN_PLACEHOLDER, column);
					commentSql = StringUtil.replace(commentSql, MetaDataSqlManager.COMMENT_PLACEHOLDER, comment == null ? "" : comment.replace("'" ,"''"));
					result.append(commentSql);
					result.append("\n");
				}
				catch (Exception e)
				{
					LogMgr.logError("DbMetadata.getTableColumnCommentsSql()", "Error creating comments SQL for remark=" + comment, e);
				}
			}
		}
		return result;
	}

}
