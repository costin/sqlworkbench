/*
 * ExceptionUtil.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2004, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: info@sql-workbench.net
 *
 */
package workbench.exception;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;

import workbench.log.LogMgr;

/**
 *
 *	@author  info@sql-workbench.net
 */
public class ExceptionUtil
{

	private ExceptionUtil()
	{
	}

	public static StringBuffer getSqlStateString(SQLException se)
	{
		StringBuffer result = new StringBuffer("SQL State=");
		try
		{
			String state = se.getSQLState();
			if (state != null && state.length() > 0)
			{
				result.append(state);
				result.append(", ");
			}
			int error = se.getErrorCode();
			result.append("DB Errorcode=");
			result.append(Integer.toString(error));
		}
		catch (Throwable th)
		{
			result.append("(unknown)");
		}
		return result;
	}

	public static String getDisplay(Throwable th)
	{
		return getDisplay(th, false);
	}

	public static String getDisplay(Throwable th, boolean includeStackTrace)
	{
		StringBuffer result;
		try
		{
			if (th.getMessage() == null)
			{
				result = new StringBuffer(th.getClass().getName());
			}
			else
			{
				result = new StringBuffer(th.getMessage().trim());
			}
			
			if (th instanceof SQLException)
			{
				SQLException se = (SQLException)th;
				result.append(" [");
				result.append(getSqlStateString(se));
				result.append("] ");
			}

			if (includeStackTrace)
			{
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				th.printStackTrace(pw);
				result.append("\r\n");
				result.append(sw.getBuffer());
			}
		}
		catch (Throwable th1)
		{
			LogMgr.logError("ExceptionUtil.getDisplay()", "Error while creating display string", th1);
			result = new StringBuffer("Exception: " + th.getClass().getName());
		}
		return result.toString();
	}


	public static void main(String args[])
	{
		Exception e = new NullPointerException("Testing");
		System.out.println("e=" + getDisplay(e, true));
		System.out.println("*****");
	}

}
