/*
 * TemplateHandler.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2012, Thomas Kellerer
 * No part of this code may be reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 */
package workbench.db.sqltemplates;

import java.io.IOException;
import workbench.log.LogMgr;
import workbench.resource.Settings;
import workbench.util.FileUtil;
import workbench.util.StringUtil;
import workbench.util.WbFile;

/**
 *
 * @author Thomas Kellerer
 */
public abstract class TemplateHandler
{

	protected String getStringProperty(String property, String defaultValue)
	{
		String value= Settings.getInstance().getProperty(property, defaultValue);
		if (value != null && value.startsWith("@file:"))
		{
			value = readFile(value);
		}
		return value;
	}

	private String readFile(String propValue)
	{
		String fname = propValue.replace("@file:", "");
		WbFile f = new WbFile(fname);
		if (!f.isAbsolute())
		{
			f = new WbFile(Settings.getInstance().getConfigDir(), fname);
		}

		String sql = null;
		try
		{
			LogMgr.logDebug("TemplateHandler.readFile()", "Reading SQL template from: " + f.getAbsolutePath());
			sql = FileUtil.readFile(f, "UTF-8");
		}
		catch (IOException io)
		{
			LogMgr.logError("TemplateHandler.readFile", "Could not read file: " + fname, io);
			sql = null;
		}
		return sql;
	}

	/**
	 * Remove the placeholder completely from the template SQL.
	 *
	 * @param sql          the sql template
	 * @param placeholder  the placeholder
	 * @param withNL       controls replacing of newlines after the placeholder.
	 *                     if true, newlines after and whitespace before the placeholder are removed as well.
	 *
	 * @return the template with the placeholder removed
	 */
	public static String removePlaceholder(String sql, String placeholder, boolean withNL)
	{
		String s;
		if (withNL)
		{
			StringBuilder b = new StringBuilder(placeholder.length() + 10);
			b.append("[ \\t]*");
			b.append(StringUtil.quoteRegexMeta(placeholder));
			b.append("[\n|\r\n]?");
			s = b.toString();
		}
		else
		{
			s = StringUtil.quoteRegexMeta(placeholder);
		}
		return sql.replaceAll(s, StringUtil.EMPTY_STRING);
	}

	/**
	 * Replace the placeholder in the given SQL template.
	 *
	 * If the template does not have whitespace before or after the placeholder a whitespace will be inserted.
	 *
	 * @param sql           the SQL template
	 * @param placeholder   the placeholder
	 * @param replacement   the replacement
	 * @return the template with the placeholder replaced
	 */
	public static String replacePlaceHolder(String sql, String placeholder, String replacement)
	{
		if (StringUtil.isEmptyString(replacement)) return removePlaceholder(sql, placeholder, false);
		int pos = sql.indexOf(placeholder);
		if (pos < 0) return sql;

		String realReplacement = replacement;

		if (pos > 1)
		{
			char prev = sql.charAt(pos - 1);
			if (!Character.isWhitespace(prev))
			{
				realReplacement = " " + realReplacement;
			}
		}

		if (pos + placeholder.length() < sql.length())
		{
			char next = sql.charAt(pos + placeholder.length());
			if (!Character.isWhitespace(next))
			{
				realReplacement = realReplacement + " ";
			}
		}
		return StringUtil.replace(sql, placeholder, realReplacement);
	}

}
