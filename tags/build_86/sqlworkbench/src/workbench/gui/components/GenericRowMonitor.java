/*
 * GenericRowMonitor.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2006, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.components;

import java.util.HashMap;
import java.util.Map;
import workbench.interfaces.StatusBar;
import workbench.resource.ResourceMgr;
import workbench.storage.RowActionMonitor;

/**
 *
 * @author support@sql-workbench.net
 */
public class GenericRowMonitor
	implements RowActionMonitor 
{
	private StatusBar statusBar;
	private String updateMsg;
	private String currentMonitorObject;
	private int monitorType;
	private String objectMsg = ResourceMgr.getString("MsgProcessObject") + " ";
	private Map typeStack = new HashMap();

	public GenericRowMonitor(StatusBar status)
	{
		this.statusBar = status;
	}

	public int getMonitorType() { return this.monitorType; }
	
	public void setMonitorType(int type)
	{
		statusBar.clearStatusMessage();
		this.monitorType = type;
		try
		{
			switch (type)
			{
				case RowActionMonitor.MONITOR_INSERT:
					this.updateMsg = ResourceMgr.getString("MsgImportingRow") + " ";
					break;
				case RowActionMonitor.MONITOR_UPDATE:
					this.updateMsg = ResourceMgr.getString("MsgUpdatingRow") + " ";
					break;
				case RowActionMonitor.MONITOR_LOAD:
					this.updateMsg = ResourceMgr.getString("MsgLoadingRow") + " ";
					break;
				case RowActionMonitor.MONITOR_EXPORT:
					this.updateMsg = ResourceMgr.getString("MsgWritingRow") + " ";
					break;
				case RowActionMonitor.MONITOR_COPY:
					this.updateMsg = ResourceMgr.getString("MsgCopyingRow") + " ";
					break;
				case RowActionMonitor.MONITOR_PROCESS_TABLE:
					this.updateMsg = ResourceMgr.getString("MsgProcessTable") + " ";
					break;
				case RowActionMonitor.MONITOR_PLAIN:
					this.updateMsg = null;
					break;
				default:
					clearRowMonitorSettings();
			}
		}
		catch (Exception e)
		{
			clearRowMonitorSettings();
		}
	}

	public void setCurrentObject(String name, long number, long total)
	{
		if (this.monitorType == RowActionMonitor.MONITOR_PLAIN)
		{
			statusBar.setStatusMessage(name);
		}
		else
		{
			this.currentMonitorObject = name;
			StringBuffer msg = new StringBuffer(40);
			msg.append(objectMsg);
			msg.append(name);
			if (number > 0)
			{
				msg.append(" (");
				msg.append(number);
				if (total > 0)
				{
					msg.append('/');
					msg.append(total);
				}
				msg.append(')');
			}
			statusBar.setStatusMessage(msg.toString());
		}
	}

	public void setCurrentRow(long currentRow, long totalRows)
	{
		StringBuffer msg = new StringBuffer(40);
		if (this.updateMsg == null)
		{
			msg.append(objectMsg);
			msg.append(this.currentMonitorObject);
			msg.append(" (");
		}
		else
		{
			msg.append(this.updateMsg);
		}
		msg.append(currentRow);
		if (totalRows > 0)
		{
			msg.append('/');
			msg.append(totalRows);
		}
		if (this.updateMsg == null) msg.append(')');
		statusBar.setStatusMessage(msg.toString());
	}

	public void jobFinished()
	{
		statusBar.clearStatusMessage();
	}

	private void clearRowMonitorSettings()
	{
		this.updateMsg = null;
		this.monitorType = -1;
		statusBar.clearStatusMessage();
	}
	
	public void saveCurrentType(String type) 
	{
		TypeEntry entry = new TypeEntry();
		entry.msg = this.updateMsg;
		entry.type = this.monitorType;
		entry.obj = this.currentMonitorObject;
		this.typeStack.put(type, entry);
		//statusBar.clearStatusMessage();
	}
	
	public void restoreType(String type) 
	{
		TypeEntry entry = (TypeEntry)typeStack.get(type);
		if (entry == null) return;
		this.updateMsg = entry.msg;
		this.currentMonitorObject = entry.obj;
		this.monitorType = entry.type;
		//statusBar.clearStatusMessage();
	}
	
}

class TypeEntry
{
	int type;
	String msg;
	String obj;
	
	public TypeEntry()
	{
	}
}
