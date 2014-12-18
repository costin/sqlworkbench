/*
 * PostgresBlobFormatter.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2007, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.storage;

import java.sql.SQLException;

/**
 * A class to format a byte[] array to be used as a literal in a SQL 
 * Statement for Postgres. 
 * 
 * PG accepts binary contents as octal Strings, e.g. 
 * \001\002\004
 * 
 * See also: http://www.postgresql.org/docs/8.2/static/datatype-binary.html
 * 
 * @author support@sql-workbench.net
 */
public class PostgresBlobFormatter
	implements BlobLiteralFormatter
{
	
	public PostgresBlobFormatter()
	{
	}

	public CharSequence getBlobLiteral(Object value)
		throws SQLException
	{
		if (value == null) return null;
		if (value instanceof byte[])
		{
			byte[] buffer = (byte[])value;
			StringBuilder result = new StringBuilder(buffer.length * 5 + 2);
			result.append('\'');
			for (int i = 0; i < buffer.length; i++)
			{
				result.append('\\');
				result.append('\\');
				int c = (buffer[i] < 0 ? 256 + buffer[i] : buffer[i]);
				String s = Integer.toOctalString(c);
				if (s.length() == 1) 
				{
					result.append('0');
					result.append('0');
				}
				else if (s.length() == 2)
				{
					result.append('0');
				}
				result.append(s);
			}
			result.append('\'');
			return result;
		}
		return value.toString();
	}
}
