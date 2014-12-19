/*
 * ConnectionInfoBuilder.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2013, Thomas Kellerer
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
package workbench.db;

import java.sql.SQLException;
import workbench.resource.ResourceMgr;
import workbench.util.StringUtil;

/**
 * A class to generate a summary display of a WbConnection.
 *
 * The information can be presented as HTML or plain text.
 *
 * @author Thomas Kellerer
 */
public class ConnectionInfoBuilder
{

	public ConnectionInfoBuilder()
	{
	}

	public String getHtmlDisplay(WbConnection conn)
	{
		if (conn == null) return "";
		if (conn.isClosed()) return "";
		return getDisplay(conn, true, 0);
	}

	public String getPlainTextDisplay(WbConnection conn, int indent)
	{
		if (conn == null) return "";
		if (conn.isClosed()) return "";
		return getDisplay(conn, false, indent);
	}

	private String getDisplay(WbConnection conn, boolean useHtml, int indent)
	{
		try
		{
			StringBuilder content = new StringBuilder(500);
			if (useHtml) content.append("<html>");

			String space = StringUtil.padRight("", indent);

			String lineStart = useHtml ? "<div style=\"white-space:nowrap;\">" : space;
			String lineEnd = useHtml ? "</div>\n" : "\n";
			String boldStart = useHtml ? "<b>" : "";
			String boldEnd = useHtml ? "</b> " : " ";
			String newLine = useHtml ? "<br>\n" : "\n";

			boolean busy = conn.isBusy();
			DbMetadata wbmeta = conn.getMetadata();

			String username = null;
			String isolationlevel = null;
			String productVersion = null;
			String driverName = null;
			if (busy)
			{
				username = conn.getDisplayUser();
				isolationlevel = "n/a";
				productVersion = "n/a";
				driverName = "n/a";
			}
			else
			{
				username = conn.getCurrentUser();
				isolationlevel = conn.getIsolationLevelName();
				productVersion = conn.getDatabaseProductVersion();
				driverName = conn.getSqlConnection().getMetaData().getDriverName();
			}
			String dbVersion = conn.getDatabaseVersion().toString();

			content.append(lineStart + boldStart + ResourceMgr.getString("LblDbProductName") + ":" + boldEnd + wbmeta.getProductName() + lineEnd);
			content.append(lineStart + boldStart + ResourceMgr.getString("LblDbProductVersion") + ":" + boldEnd + dbVersion + lineEnd);
			content.append(lineStart + boldStart + ResourceMgr.getString("LblDbProductInfo") + ":" + boldEnd + productVersion + lineEnd);
			content.append(lineStart + boldStart + ResourceMgr.getString("LblDriverInfoName") + ":" + boldEnd + driverName + lineEnd);
			content.append(lineStart + boldStart + ResourceMgr.getString("LblDriverInfoClass") + ":" + boldEnd + conn.getProfile().getDriverclass() + lineEnd);
			content.append(lineStart + boldStart + ResourceMgr.getString("LblDriverInfoVersion") + ":" + boldEnd + conn.getDriverVersion() + lineEnd);
			content.append(lineStart + boldStart + ResourceMgr.getString("LblDbURL") + ":" + boldEnd + conn.getUrl() + lineEnd);
			content.append(space + boldStart + "Isolation Level:" + boldEnd + " " + isolationlevel + newLine);
			content.append(space + boldStart + ResourceMgr.getString("LblUsername") + ":" + boldEnd + username + newLine);

			String term = wbmeta.getSchemaTerm();
			String s = StringUtil.capitalize(term);
			if (!"schema".equalsIgnoreCase(term))
			{
				s += " (" + ResourceMgr.getString("LblSchema") + ")";
			}
			content.append(space + boldStart + s + ":" + boldEnd + nvl(busy ? "n/a" : conn.getCurrentSchema()) + newLine);

			term = wbmeta.getCatalogTerm();
			s = StringUtil.capitalize(term);
			if (!"catalog".equalsIgnoreCase(term))
			{
				s += " (" +  ResourceMgr.getString("LblCatalog") + ")";
			}
			content.append(space + boldStart + s + ":" + boldEnd + nvl(busy ? "n/a" : conn.getCurrentCatalog()) + newLine);
			content.append(space + boldStart + "Workbench DBID:" + boldEnd + wbmeta.getDbId() + newLine);
			content.append(space + boldStart + "Connection ID:" + boldEnd + conn.getId());
			if (useHtml) content.append("</html>");
			return content.toString();
		}
		catch (SQLException e)
		{
			return e.getMessage();
		}
	}

	private String nvl(String value)
	{
		if (value == null) return "";
		return value;
	}
}