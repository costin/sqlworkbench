/*
 * GenericObjectDropper.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2008, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import workbench.interfaces.ObjectDropper;
import workbench.log.LogMgr;
import workbench.storage.RowActionMonitor;
import workbench.util.SqlUtil;
import workbench.util.StringUtil;

/**
 * A helper class to drop different types of objects
 * 
 * @author  support@sql-workbench.net
 */
public class GenericObjectDropper
	implements ObjectDropper
{
	private List<? extends DbObject> objects;
	private WbConnection connection;
	private Statement currentStatement;
	private boolean cascadeConstraints;
	private TableIdentifier objectTable;
	private RowActionMonitor monitor;
	
	public GenericObjectDropper()
	{
	}

	public List<? extends DbObject> getObjects()
	{
		return objects;
	}

	public void setRowActionMonitor(RowActionMonitor mon)
	{
		this.monitor = mon;
	}
	
	public boolean supportsFKSorting()
	{
		if (objects == null) return false;
		
		int numTypes = this.objects.size();
		for (int i=0; i < numTypes; i++)
		{
			DbObject obj = this.objects.get(i);
			if (!(obj instanceof TableIdentifier))
			{
				return false;
			}
		}
		return true;
	}
	
	public boolean supportsCascade()
	{
		boolean canCascade = false;

		if (objects != null && this.connection != null)
		{
			int numTypes = this.objects.size();
			for (int i=0; i < numTypes; i++)
			{
				String type = this.objects.get(i).getObjectType();
				String verb = this.connection.getDbSettings().getCascadeConstraintsVerb(type);

				// if at least one type can be dropped with CASCADE, enable the checkbox
				if (!StringUtil.isEmptyString(verb))
				{
					canCascade = true;
					break;
				}
			}
		}
		return canCascade;
	}

	public void setObjects(List<? extends DbObject> toDrop)
	{
		this.objects = toDrop;
	}
	
	public void setObjectTable(TableIdentifier tbl)
	{
		this.objectTable = tbl;
	}

	public WbConnection getConnection()
	{
		return this.connection;
	}
	
	public void setConnection(WbConnection aConn)
	{
		this.connection = aConn;
	}

	public CharSequence getScript()
	{
		if (this.connection == null) throw new NullPointerException("No connection!");
		if (this.objects == null || this.objects.size() == 0) return null;
		
		boolean needCommit = this.connection.shouldCommitDDL();
		int count = this.objects.size();
		StringBuffer result = new StringBuffer(count * 40);
		for (int i=0; i < count; i++)
		{
			CharSequence sql = getDropStatement(i);
			result.append(sql);
			result.append(";\n");
			if (needCommit) result.append("COMMIT;\n");
			result.append("\n");
		}
		return result;
	}

	private CharSequence getDropStatement(int index)
	{
		String name = this.objects.get(index).getObjectName();
		String type = this.objects.get(index).getObjectType();

		StringBuilder sql = new StringBuilder(120);
		sql.append("DROP ");
		sql.append(type);
		sql.append(' ');
		sql.append(name);

		boolean needTableForIndexDrop = this.connection.getDbSettings().needsTableForDropIndex();
		
		if (needTableForIndexDrop && "INDEX".equals(type) && objectTable != null)
		{
			sql.append(" ON ");
			sql.append(objectTable.getTableExpression(this.connection));
		}

		String cascade = null;
		if (this.cascadeConstraints)
		{
			cascade = this.connection.getDbSettings().getCascadeConstraintsVerb(type);
			if (cascade != null)
			{
				sql.append(' ');
				sql.append(cascade);
			}
		}
		return sql;
	}
	
	public void dropObjects()
		throws SQLException
	{
		boolean needCommit = this.connection.shouldCommitDDL();
		
		try
		{
			if (this.connection == null) throw new NullPointerException("No connection!");
			if (this.connection.isBusy()) return;
			if (this.objects == null || this.objects.size() == 0) return;
			int count = this.objects.size();
			this.connection.setBusy(true);
			
    	currentStatement = this.connection.createStatement();
			for (int i=0; i < count; i++)
			{
				String sql = getDropStatement(i).toString();
				LogMgr.logDebug("ObjectDropper.execute()", "Using SQL: " + sql);
				if (monitor != null)
				{
					String name = objects.get(i).getObjectName();
					monitor.setCurrentObject(name, i + 1, count);
				}
				currentStatement.execute(sql);
			}

			if (needCommit)
			{
				this.connection.commit(); 
			}
		}
		catch (SQLException e)
		{
			if (needCommit)
			{
				try { this.connection.rollback(); } catch (Throwable th) {}
			}
			throw e;
		}
		finally
		{
			SqlUtil.closeStatement(currentStatement);
			this.currentStatement = null;
			this.connection.setBusy(false);
		}
	}

	public void cancel()
		throws SQLException
	{
		if (this.currentStatement == null) return;
		this.currentStatement.cancel();
		if (this.connection.shouldCommitDDL())
		{
			try { this.connection.rollback(); } catch (Throwable th) {}
		}
	}
	
	public void setCascade(boolean flag)
	{
		if (this.supportsCascade())
		{
			this.cascadeConstraints = flag;
		}
	}

}
