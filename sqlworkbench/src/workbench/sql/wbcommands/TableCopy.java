/*
 * TableCopy.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2009, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.sql.wbcommands;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import workbench.AppArguments;
import workbench.db.ColumnIdentifier;
import workbench.db.TableIdentifier;
import workbench.db.WbConnection;
import workbench.db.datacopy.DataCopier;
import workbench.resource.ResourceMgr;
import workbench.sql.StatementRunnerResult;
import workbench.storage.RowActionMonitor;
import workbench.util.ArgumentParser;
import workbench.util.SqlUtil;
import workbench.util.StringUtil;

/**
 * Handles a WbCopy call for a single table.
 *
 * @author support@sql-workbench.net
 */
public class TableCopy
	implements CopyTask
{
	private DataCopier copier;

	public void copyData()
		throws SQLException, Exception
	{
		this.copier.startCopy();
	}

	public boolean init(WbConnection sourceConnection, WbConnection targetConnection, StatementRunnerResult result, ArgumentParser cmdLine, RowActionMonitor monitor)
		throws SQLException
	{

		String sourcetable = cmdLine.getValue(WbCopy.PARAM_SOURCETABLE);
		String sourcequery = cmdLine.getValue(WbCopy.PARAM_SOURCEQUERY);
		String targettable = cmdLine.getValue(WbCopy.PARAM_TARGETTABLE);

		boolean cont = cmdLine.getBoolean(CommonArgs.ARG_CONTINUE);

		boolean createTable = cmdLine.getBoolean(WbCopy.PARAM_CREATETARGET);
		boolean dropTable = cmdLine.getBoolean(WbCopy.PARAM_DROPTARGET);
		boolean ignoreDropError = cmdLine.getBoolean(AppArguments.ARG_IGNORE_DROP, false);
		
		String keys = cmdLine.getValue(WbCopy.PARAM_KEYS);

		this.copier = new DataCopier();
		copier.setTransactionControl(cmdLine.getBoolean(CommonArgs.ARG_TRANS_CONTROL, true));
		copier.setKeyColumns(keys);

		String mode = cmdLine.getValue(CommonArgs.ARG_IMPORT_MODE);
		if (!this.copier.setMode(mode))
		{
			result.addMessage(ResourceMgr.getFormattedString("ErrImpInvalidMode", mode));
			result.setFailure();
			return false;
		}

		CommonArgs.setProgressInterval(copier, cmdLine);
		copier.setRowActionMonitor(monitor);
		copier.setContinueOnError(cont);

		CommonArgs.setCommitAndBatchParams(copier, cmdLine);

		copier.setDeleteTarget(CommonArgs.getDeleteType(cmdLine));

		TableIdentifier targetId = null;
		if (createTable)
		{
			targetId = new TableIdentifier(targettable);
			targetId.setNewTable(true);
		}
		else
		{
			targetId = targetConnection.getMetadata().findTable(new TableIdentifier(targettable));
		}

		if (sourcetable != null)
		{
			TableIdentifier srcTable = new TableIdentifier(sourcetable);
			String where = cmdLine.getValue(WbCopy.PARAM_SOURCEWHERE);
			Map<String, String> mapping = this.parseMapping(cmdLine);
			copier.copyFromTable(sourceConnection, targetConnection, srcTable, targetId, mapping, where, createTable, dropTable, ignoreDropError);
		}
		else
		{
			ColumnIdentifier[] cols = this.parseColumns(cmdLine);
			if (cols == null)
			{
				List<ColumnIdentifier> queryCols = SqlUtil.getResultSetColumns(sourcequery, sourceConnection);
				cols = new ColumnIdentifier[queryCols.size()];
				for (int i=0; i < queryCols.size(); i++)
				{
					cols[i] = queryCols.get(i);
				}
			}

			copier.copyFromQuery(sourceConnection, targetConnection, sourcequery, targetId, cols, createTable, dropTable, ignoreDropError);
		}

		boolean doSyncDelete = cmdLine.getBoolean(WbCopy.PARAM_DELETE_SYNC, false) && !createTable;
		copier.setDoDeleteSync(doSyncDelete);

		return true;
	}

	public boolean isSuccess()
	{
		if (this.copier == null) return true;
		return copier.isSuccess();
	}

	public CharSequence getMessages()
	{
		if (this.copier == null) return null;
		return copier.getAllMessages();
	}

	public void cancel()
	{
		if (this.copier != null)
		{
			this.copier.cancel();
		}
	}

	private ColumnIdentifier[] parseColumns(ArgumentParser cmdLine)
	{
		// First read the defined columns from the passed parameter
		String cols = cmdLine.getValue(WbCopy.PARAM_COLUMNS);
		if (StringUtil.isBlank(cols)) return null;

		List<String> l = StringUtil.stringToList(cols, ",", true, true, false, true);
		int count = l.size();
		ColumnIdentifier[] result = new ColumnIdentifier[count];
		for (int i=0; i < count; i++)
		{
			String c = l.get(i);
			if (c.indexOf("/") > -1)
			{
				copier.addError(ResourceMgr.getString("MsgCopyErrIllegalMapping"));
				return null;
			}
			result[i] = new ColumnIdentifier(c);
		}
		return result;
	}

	private Map<String, String> parseMapping(ArgumentParser cmdLine)
	{
		String cols = cmdLine.getValue(WbCopy.PARAM_COLUMNS);
		if (cols == null || cols.length() == 0) return null;

		List<String> l = StringUtil.stringToList(cols, ",", true, true, false, true);
		int count = l.size();

		// Use a LinkedHashMap to make sure the order of the columns
		// is preserved (in case -createTable) was also specified
		Map<String, String> mapping = new LinkedHashMap<String, String>();
		for (int i=0; i < count; i++)
		{
			String s = l.get(i);
			int pos = s.indexOf('/');
			if (pos == -1)
			{
				// No mapping just a list of columns
				mapping.put(s, null);
			}
			else
			{
				String scol = s.substring(0, pos).trim();
				String tcol = s.substring(pos + 1).trim();
				mapping.put(scol, tcol);
			}
		}
		return mapping;
	}

}
