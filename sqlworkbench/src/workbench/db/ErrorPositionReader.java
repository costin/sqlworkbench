/*
 * ErrorPositionReader.java
 *
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
package workbench.db;

import java.util.regex.PatternSyntaxException;

import workbench.log.LogMgr;

import workbench.db.oracle.OracleErrorPositionReader;

import workbench.sql.ErrorDescriptor;

/**
 *
 * @author Thomas Kellerer
 */
public interface ErrorPositionReader
{
	/**
	 * Return the position of the error as reported by the DBMS.
	 *
	 * @param con   the connection on which the error happened
	 *              (may be used by the reader to retrieve additional information)
	 * @param sql   the SQL statement that caused the error
	 * @param ex    the error exception
	 * @return an ErrorDescriptor describing the error position inside the SQL or null if this could not be determined
	 */
	ErrorDescriptor getErrorPosition(WbConnection con, String sql, Exception ex);

	/**
	 * Enhances the error messages generated by the driver.
	 *
	 * This is DBMS dependent. A concrete implementation could add the error position to the message
	 * or add an indicator where exactly the error occurred.
	 *
	 * @return an enhanced error message that can be displayed to the user.
	 */
	String enhanceErrorMessage(String sql, String errorMessage, ErrorDescriptor errorPosition);

	// <editor-fold defaultstate="collapsed" desc="Factory">

	public class Factory
	{
		private static final ErrorPositionReader dummyReader = new ErrorPositionReader()
		{
			@Override
			public ErrorDescriptor getErrorPosition(WbConnection con, String sql, Exception ex)
			{
				return null;
			}

			@Override
			public String enhanceErrorMessage(String sql, String originalMessage, ErrorDescriptor errorPosition)
			{
				return originalMessage;
			}
		};

		/**
		 * Create an instance of an ErrorPositionReader for the DBMS identified by the connection.
		 *
		 * @param conn  the connection
		 * @return an ErrorPositionReader, never null
		 */
		public static ErrorPositionReader createPositionReader(WbConnection conn)
		{
			if (conn == null) return dummyReader;
			if (conn.getMetadata().isOracle())
			{
				return new OracleErrorPositionReader();
			}

			String colRegex = conn.getDbSettings().getErrorColumnInfoRegex();
			String lineRegex = conn.getDbSettings().getErrorLineInfoRegex();
			String posRegex = conn.getDbSettings().getErrorPosInfoRegex();
			boolean zeroBased = conn.getDbSettings().getErrorPosIsZeroBased();

			if (posRegex != null)
			{
				try
				{
					RegexErrorPositionReader reader = new RegexErrorPositionReader(posRegex);
					reader.setNumbersAreOneBased(zeroBased);
					LogMgr.logDebug("ErrorPositionReader.Factory.createPositionReader()", "Initialized reader for dbid=" + conn.getDbId() + " using: positionRegex: " + posRegex);
					return reader;
				}
				catch (PatternSyntaxException pse)
				{
					LogMgr.logError("ErrorPositionReader.Factory.createPositionReader()", "Could not initialize regex based reader using positionRegex: " + posRegex, pse);
				}
			}
			else if (colRegex != null || lineRegex != null)
			{
				try
				{
					RegexErrorPositionReader reader = new RegexErrorPositionReader(lineRegex, colRegex);
					reader.setNumbersAreOneBased(zeroBased);
					LogMgr.logDebug("ErrorPositionReader.Factory.createPositionReader()", "Initialized reader for dbid=" + conn.getDbId() + " using: lineRegex: " + lineRegex + ", columnRegex:" + colRegex);
					return reader;
				}
				catch (PatternSyntaxException pse)
				{
					LogMgr.logError("ErrorPositionReader.Factory.createPositionReader()", "Could not initialize regex based reader using: lineRegex: " + lineRegex + ", columnRegex:" + colRegex, pse);
				}
			}
			return dummyReader;
		}
	}
	// </editor-fold>

}
