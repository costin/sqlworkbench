/*
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2014, Thomas Kellerer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at.
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.sql;

import java.util.ArrayList;
import java.util.List;

import workbench.sql.formatter.SQLLexer;
import workbench.sql.formatter.SQLToken;

import workbench.util.StringUtil;

/**
 *
 * @author Thomas Kellerer
 */
public class BookmarkAnnotation
	extends AnnotationReader
{
	public static final String ANNOTATION = "wbmark";

	public BookmarkAnnotation()
	{
		super(ANNOTATION);
	}

	public String getBookmarkName(String sql)
	{
		return StringUtil.trim(getAnnotationValue(sql));
	}

	/**
	 * Parses the given SQL script for bookmark annotations.
	 *
	 * @param script  the script to parse
	 * @return the list of bookmarks found
	 */
	public List<NamedScriptLocation> getBookmarks(String script)
	{
		List<NamedScriptLocation> bookmarks = new ArrayList<NamedScriptLocation>();
		SQLLexer lexer = new SQLLexer(script);
		SQLToken token = lexer.getNextToken(true, false);

		while (token != null)
		{
			if (token.isComment())
			{
				String locationName = StringUtil.trim(extractAnnotationValue(token));
				if (locationName != null)
				{
					NamedScriptLocation bookmark = new NamedScriptLocation(locationName, token.getCharBegin());
					bookmarks.add(bookmark);
				}
			}
			token = lexer.getNextToken(true, false);
		}
		return bookmarks;
	}

}
