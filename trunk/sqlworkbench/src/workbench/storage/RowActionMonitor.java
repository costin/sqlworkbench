/*
 * DataStoreUpdateMonitor.java
 *
 * Created on May 8, 2003, 12:11 PM
 */

package workbench.storage;

/**
 *
 * @author  workbench@kellerer.org
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