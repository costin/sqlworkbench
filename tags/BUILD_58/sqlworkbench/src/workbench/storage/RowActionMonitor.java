/*
 * DataStoreUpdateMonitor.java
 *
 * Created on May 8, 2003, 12:11 PM
 */

package workbench.storage;

/**
 *
 * @author  tkellerer
 */
public interface RowActionMonitor
{
	final int MONITOR_INSERT = 0;
	final int MONITOR_UPDATE = 1;
	final int MONITOR_LOAD = 2;
	void setMonitorType(int aType);
	void setCurrentRow(int currentRow, int totalRows);
}
