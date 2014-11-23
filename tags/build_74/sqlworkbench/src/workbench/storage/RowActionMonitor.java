/*
 * RowActionMonitor.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2004, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: info@sql-workbench.net
 *
 */
package workbench.storage;

/**
 *
 * @author  info@sql-workbench.net
 */
public interface RowActionMonitor
{
	final int MONITOR_INSERT = 0;
	final int MONITOR_UPDATE = 1;
	final int MONITOR_LOAD = 2;
	final int MONITOR_EXPORT = 3;
	final int MONITOR_COPY = 4;
	final int MONITOR_PROCESS_TABLE = 5;
	final int MONITOR_PROCESS = 6;

	void setMonitorType(int aType);
	void setCurrentObject(String object, int number, int totalObjects);
	void setCurrentRow(int currentRow, int totalRows);
	void jobFinished();
}