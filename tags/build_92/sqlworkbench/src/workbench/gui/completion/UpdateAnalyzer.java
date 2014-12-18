/*
 * UpdateAnalyzer.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2007, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.completion;

import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import workbench.db.TableIdentifier;
import workbench.db.WbConnection;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.sql.formatter.SQLLexer;
import workbench.sql.formatter.SQLToken;
import workbench.util.SqlUtil;
import workbench.util.StringUtil;
import workbench.util.TableAlias;

/**
 * Analyze an UPDATE statement regarding the context for the auto-completion
 * @author support@sql-workbench.net
 */
public class UpdateAnalyzer
	extends BaseAnalyzer
{
	public UpdateAnalyzer(WbConnection conn, String statement, int cursorPos)
	{
		super(conn, statement, cursorPos);
	}

	protected void checkContext()
	{
		checkOverwrite();
		
		final int IN_SET = 1;
		final int IN_UPDATE = 2;
		final int IN_WHERE = 3;
		
		int state = -1;
		boolean nextIsTable = false;
		String table = null;
		
		SQLLexer lexer = new SQLLexer(sql);
		SQLToken t = lexer.getNextToken(false, false);
		
		while (t != null)
		{
			if (nextIsTable)
			{
				table = t.getContents();
				nextIsTable = false;
			}
			if (t.getContents().equals("UPDATE"))
			{
				nextIsTable = true;
				if (cursorPos > t.getCharEnd())
				{
					state = IN_UPDATE;
				}
			}
			else if (t.getContents().equals("SET"))
			{
				if (cursorPos > t.getCharEnd())
				{
					state = IN_SET;
				}
			}
			else if (t.getContents().equals("WHERE"))
			{
				if (cursorPos > t.getCharEnd())
				{
					state = IN_WHERE;
				}
			}
			t = lexer.getNextToken(false, false);
		}
		
		if (state == IN_UPDATE)
		{
			context = CONTEXT_TABLE_LIST;
			String q = this.getQualifierLeftOfCursor();
			if (q != null)
			{
				this.schemaForTableList = q;
			}
			else
			{
				this.schemaForTableList = this.dbConnection.getMetadata().getCurrentSchema();
			}
		}
		else
		{
			// "inside" the SET and after the WHERE we always need the column list
			if (table != null)
			{
				context = CONTEXT_COLUMN_LIST;
				tableForColumnList = new TableIdentifier(table);
			}
		}
	}


}