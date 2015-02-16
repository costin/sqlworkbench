/*
 * DbObjectChanger
 * 
 *  This file is part of SQL Workbench/J, http://www.sql-workbench.net
 * 
 *  Copyright 2002-2009, Thomas Kellerer
 *  No part of this code maybe reused without the permission of the author
 * 
 *  To contact the author please send an email to: support@sql-workbench.net
 */
package workbench.db;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import workbench.log.LogMgr;
import workbench.util.StringUtil;

/**
 *
 * @author Thomas Kellerer
 */
public class DbObjectChanger
{
	public static final String PARAM_OLD_OBJECT_NAME = "%object_name%";
	public static final String PARAM_NEW_OBJECT_NAME = "%new_object_name%";
	public static final String PARAM_CONSTRAINT_NAME = "%constraint_name%";
	public static final String PARAM_TABLE_NAME = MetaDataSqlManager.TABLE_NAME_PLACEHOLDER;

	private WbConnection dbConnection;
	private DbSettings settings;
	private CommentSqlManager commentMgr;

	public DbObjectChanger(WbConnection con)
	{
		if (con != null)
		{
			settings = con.getDbSettings();
			commentMgr = new CommentSqlManager(settings.getDbId());
		}
		dbConnection = con;
	}

	public String getAlterScript(Map<DbObject, DbObject> changedObjects)
	{
		StringBuilder result = new StringBuilder(changedObjects.size() * 50);
		for (Map.Entry<DbObject, DbObject> entry : changedObjects.entrySet())
		{
			String sql = getRename(entry.getKey(), entry.getValue());
			if (sql != null)
			{
				result.append(sql);
				result.append(";\n");
			}
			String commentSql = getCommentSql(entry.getKey(), entry.getValue());
			if (commentSql != null)
			{
				result.append(commentSql);
				result.append(";\n");
			}
		}
		if (settings.ddlNeedsCommit())
		{
			result.append("\nCOMMIT;\n");
		}
		return result.toString();
	}
	
	public String getRename(DbObject oldTable, DbObject newTable)
	{
		if (newTable == null || oldTable == null) return null;

		String type = oldTable.getObjectType();
		String sql = getRenameObjectSql(type);
		if (sql == null) return null;
		
		String oldName = oldTable.getObjectName();
		String newName = newTable.getObjectName();

		if (StringUtil.equalStringOrEmpty(oldName.trim(), newName.trim(), true)) return null; // no change
		sql = sql.replace(PARAM_OLD_OBJECT_NAME, oldName.trim());
		sql = sql.replace(PARAM_NEW_OBJECT_NAME, newName.trim());
		return sql;
	}

	public String getCommentSql(DbObject oldTable, DbObject newTable)
	{
		if (commentMgr == null || newTable == null || oldTable == null) return null;

		String type = oldTable.getObjectType();

		String sql = getCommentSql(type);
		if (sql == null) return null;

		String oldComment = oldTable.getComment();
		String newComment = newTable.getComment();

		String schema = oldTable.getSchema();
		if (schema == null) schema = "";
		
		if (StringUtil.equalStringOrEmpty(oldComment, newComment, true)) return null; // no change
		String oldname = oldTable.getObjectName(dbConnection);
		if (oldname == null) oldname = "";
		
		sql = sql.replace(CommentSqlManager.COMMENT_OBJECT_NAME_PLACEHOLDER, oldname);
		sql = sql.replace(CommentSqlManager.COMMENT_SCHEMA_PLACEHOLDER, schema);
		sql = sql.replace(CommentSqlManager.COMMENT_PLACEHOLDER, newComment.replace("'", "''"));
		return sql;
	}

	public String getRenameObjectSql(String type)
	{
		if (settings == null || type == null) return null;
		return settings.getRenameObjectSql(type);
	}

	public String getCommentSql(String type)
	{
		if (commentMgr == null || type == null) return null;
		return commentMgr.getCommentSqlTemplate(type);
	}

	public String getDropPKScript(TableIdentifier table)
	{
		String sql = getDropPK(table);
		if (StringUtil.isBlank(sql)) return null;
		StringBuilder script = new StringBuilder(sql);
		script.append(";\n");
		if (settings.ddlNeedsCommit())
		{
			script.append("\nCOMMIT;\n");
		}
		return script.toString();
	}

	public String getDropPK(TableIdentifier table)
	{
		String type = table.getObjectType();
		if (StringUtil.isBlank(type)) return null;
		String sql = settings.getDropPrimaryKeySql(type);

		String pkConstraint = table.getPrimaryKeyName();
		if (StringUtil.isBlank(sql))
		{
			// The database doesn't support "DROP PRIMARY KEY", so we need to
			// drop the corresponding constraint
			if (StringUtil.isBlank(pkConstraint))
			{
				try
				{
					TableDefinition def = dbConnection.getMetadata().getTableDefinition(table);
					pkConstraint = def.getTable().getPrimaryKeyName();
				}
				catch (SQLException e)
				{
					LogMgr.logError("DbObjectChanger.generateDropPK()", "Error retrieving table definition", e);
					return null;
				}
			}
			sql = settings.getDropConstraint(type);
		}

		if (sql == null) return null;
		
		sql = sql.replace(PARAM_TABLE_NAME, table.getTableExpression(dbConnection));
		if (pkConstraint != null)
		{
			sql = sql.replace(PARAM_CONSTRAINT_NAME, pkConstraint);
		}
		return sql;
	}

	public String getAddPKScript(TableIdentifier table, List<ColumnIdentifier> pkCols)
	{
		String sql = getAddPK(table, pkCols);
		if (StringUtil.isBlank(sql)) return null;
		StringBuilder script = new StringBuilder(sql);
		script.append(";\n");
		if (settings.ddlNeedsCommit())
		{
			script.append("\nCOMMIT;\n");
		}
		return script.toString();
	}

	public String getAddPK(TableIdentifier table, List<ColumnIdentifier> pkCols)
	{
		String type = table.getObjectType();
		if (StringUtil.isBlank(type)) return null;
		String sql = settings.getAddPK(type);
		if (StringUtil.isBlank(sql)) return null;

		String pkName = "PK_" + table.getTableName().toUpperCase();
		if (dbConnection.getMetadata().storesLowerCaseIdentifiers())
		{
			pkName = pkName.toLowerCase();
		}
		
		sql = sql.replace(PARAM_TABLE_NAME, table.getTableExpression(dbConnection));
		if (pkName != null)
		{
			sql = sql.replace(PARAM_CONSTRAINT_NAME, pkName);
		}

		StringBuilder cols = new StringBuilder(pkCols.size() * 5);
		for (int i=0; i < pkCols.size(); i++)
		{
			if (i > 0) cols.append(", ");
			cols.append(pkCols.get(0).getColumnName(dbConnection));
		}
		sql = sql.replace(MetaDataSqlManager.COLUMN_LIST_PLACEHOLDER, cols);
		return sql;
	}

}