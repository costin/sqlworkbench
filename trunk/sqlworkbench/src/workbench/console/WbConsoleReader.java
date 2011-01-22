/*
 * WbConsoleReader
 * 
 *  This file is part of SQL Workbench/J, http://www.sql-workbench.net
 * 
 *  Copyright 2002-2011, Thomas Kellerer
 *  No part of this code may be reused without the permission of the author
 * 
 *  To contact the author please send an email to: support@sql-workbench.net
 */
package workbench.console;

/**
 *
 * @author Thomas Kellerer
 */
public interface WbConsoleReader
{
	String readPassword(String prompt);
	String readLine(String prompt);
	void shutdown();
}
