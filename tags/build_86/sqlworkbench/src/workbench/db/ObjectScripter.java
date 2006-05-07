/*
 * ObjectScripter.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2006, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import workbench.util.ExceptionUtil;

import workbench.interfaces.ScriptGenerationMonitor;
import workbench.interfaces.Scripter;
import workbench.util.StrBuffer;

/**
 *
 * @author  support@sql-workbench.net
 */
public class ObjectScripter
	implements Scripter
{
	public static final String TYPE_SEQUENCE = "sequence";
	public static final String TYPE_TABLE = "table";
	public static final String TYPE_VIEW = "view";
	public static final String TYPE_SYNONYM = "synonym";
	public static final String TYPE_INSERT = "insert";
	public static final String TYPE_SELECT = "select";
	public static final String TYPE_PROC = "procedure";

	private Map objectList;
	private DbMetadata meta;
	private StrBuffer script;
	private ScriptGenerationMonitor progressMonitor;
	private WbConnection dbConnection;
	private boolean cancel;
	
	public ObjectScripter(Map objectList, WbConnection aConnection)
	{
		this.objectList = objectList;
		this.dbConnection = aConnection;
		this.meta = aConnection.getMetadata();
	}

	public void setProgressMonitor(ScriptGenerationMonitor aMonitor)
	{
		this.progressMonitor = aMonitor;
	}

	public String getScript()
	{
		if (this.script == null) this.generateScript();
		return this.script.toString();
	}
	
	public boolean isCancelled()
	{
		return this.cancel;
	}
	
	public void generateScript()
	{
		try
		{
			this.dbConnection.setBusy(true);
			this.cancel = false;
			this.script = new StrBuffer(this.objectList.size() * 500);
			if (!cancel) this.appendObjectType(TYPE_SEQUENCE);
			if (!cancel) this.appendObjectType(TYPE_TABLE);
			if (!cancel) this.appendObjectType(TYPE_VIEW);
			if (!cancel) this.appendObjectType(TYPE_SYNONYM);
			if (!cancel) this.appendObjectType(TYPE_INSERT);
			if (!cancel) this.appendObjectType(TYPE_SELECT);
			if (!cancel) this.appendObjectType(TYPE_PROC);
		}
		finally 
		{
			this.dbConnection.setBusy(false);
		}
	}

	public void cancel()
	{
		this.cancel = true;
	}
	
	private void appendObjectType(String typeFilter)
	{
		Iterator itr = this.objectList.entrySet().iterator();
		while (itr.hasNext())
		{
			if (cancel) break;
			Map.Entry entry = (Map.Entry)itr.next();
			Object key = entry.getKey();
			String type = (String)entry.getValue();
			String source = null;
			
			if (!type.equalsIgnoreCase(typeFilter)) continue;
			
			if (this.progressMonitor != null)
			{
				this.progressMonitor.setCurrentObject(key.toString());
			}
			try
			{
				if (key instanceof ProcedureDefinition)
				{
					ProcedureDefinition procDef = (ProcedureDefinition)key;
					type = procDef.getResultTypeDisplay();
					source = meta.getProcedureSource(procDef.getCatalog(), procDef.getSchema(), procDef.getProcedureName(), procDef.getResultType());
				}
				else
				{
					String object = (String)key;
					TableIdentifier tbl = new TableIdentifier(object);
					tbl.adjustCase(this.dbConnection);
					if (TYPE_TABLE.equalsIgnoreCase(type))
					{
						source = meta.getTableSource(tbl, true);
					}
					else if (TYPE_VIEW.equalsIgnoreCase(type))
					{
						source = meta.getExtendedViewSource(tbl, false);
					}
					else if (TYPE_SYNONYM.equalsIgnoreCase(type))
					{
						source = meta.getSynonymSource(tbl.getSchema(), tbl.getTableName());
					}
					else if (TYPE_SEQUENCE.equalsIgnoreCase(type))
					{
						source = this.meta.getSequenceSource(object);
					}
					else if (TYPE_INSERT.equalsIgnoreCase(type))
					{
						source = this.meta.getEmptyInsert(tbl);
					}
					else if (TYPE_SELECT.equalsIgnoreCase(type))
					{
						source = this.meta.getDefaultSelect(tbl);
					}
				}
			}
			catch (Exception e)
			{
				this.script.append("\nError creating script for " + key.toString() + " " + ExceptionUtil.getDisplay(e));
			}

			if (source != null && source.length() > 0)
			{
				boolean useSeparator = !type.equalsIgnoreCase("insert") && !type.equalsIgnoreCase("select");
				if (useSeparator) this.script.append("-- BEGIN " + type + " " + key.toString() + "\n");
				this.script.append(source);
				if (useSeparator) this.script.append("\n-- END " + type + " " + key.toString() + "\n");
				this.script.append("\n");
			}
		}
	}

}
