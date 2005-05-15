/*
 * SelectAnalyzer.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2005, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.completion;
import java.util.regex.Pattern;
import workbench.db.DbMetadata;
import workbench.db.WbConnection;
import workbench.util.SqlUtil;
import workbench.util.StringUtil;


/**
 * Analyze a DDL statement regarding the context for the auto-completion
 * @author support@sql-workbench.net
 */
public class DdlAnalyzer
	extends BaseAnalyzer
{
	private final Pattern TABLE_PATTERN = Pattern.compile("\\sTABLE\\s|\\sTABLE$", Pattern.CASE_INSENSITIVE);
	private final Pattern VIEW_PATTERN = Pattern.compile("\\sVIEW\\s|\\sVIEW$", Pattern.CASE_INSENSITIVE);

	public DdlAnalyzer(WbConnection conn, String statement, int cursorPos)
	{	
		super(conn, statement, cursorPos);
	}
	
	protected void checkContext()
	{

		String verb = SqlUtil.getSqlVerb(this.sql);
		String q = this.getQualifierLeftOfCursor(sql, pos);
		if (q != null)
		{
			this.schemaForTableList = q;
		}
		else
		{
			this.schemaForTableList = this.dbConnection.getMetadata().getCurrentSchema();
		}
		
		if ("TRUNCATE".equalsIgnoreCase(verb))
		{
			context = CONTEXT_TABLE_LIST;
			return;
		}
		
		int tablePos = StringUtil.findPattern(TABLE_PATTERN, sql, 0);
		// for DROP etc, we'll need to be after the table keyword
		// otherwise it could be a DROP PROCEDURE as well.
		if (tablePos > -1 && pos > tablePos + 5)
		{
			context = CONTEXT_TABLE_LIST;
			setTableTypeFilter(DbMetadata.TABLE_TYPE_TABLE);
		}
		else 
		{
			int viewPos = StringUtil.findPattern(VIEW_PATTERN, sql, 0);
			if (viewPos > -1 && pos > tablePos + 4)
			{
				context = CONTEXT_TABLE_LIST;
				setTableTypeFilter(DbMetadata.TABLE_TYPE_VIEW);
			}
		}
	}


}