/*
 * TableExporter.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2013, Thomas Kellerer
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
package workbench.gui.dbobjects;

import java.awt.Frame;
import java.sql.SQLException;
import java.util.List;
import workbench.db.DbMetadata;
import workbench.db.DbObject;
import workbench.db.TableIdentifier;
import workbench.db.WbConnection;
import workbench.db.exporter.DataExporter;
import workbench.db.exporter.ExportType;
import workbench.gui.WbSwingUtilities;
import workbench.gui.dialogs.export.ExportFileDialog;
import workbench.interfaces.DbExecutionListener;
import workbench.interfaces.ProgressReporter;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.storage.RowActionMonitor;
import workbench.util.CollectionUtil;
import workbench.util.StringUtil;
import workbench.util.WbFile;

/**
 *
 * @author Thomas Kellerer
 */
public class TableExporter
	implements DbExecutionListener
{
	private DataExporter exporter;
	private ProgressDialog progress;

	public TableExporter(WbConnection conn)
	{
		exporter = new DataExporter(conn);
		exporter.setReportInterval(ProgressReporter.DEFAULT_PROGRESS_INTERVAL);
	}

	public DataExporter getExporter()
	{
		return exporter;
	}

	public void exportTables(List<DbObject> tables, Frame caller)
	{
		if (CollectionUtil.isEmpty(tables)) return;

		ExportFileDialog dialog = new ExportFileDialog(caller);
		dialog.setIncludeSqlUpdate(false);
		dialog.setSelectDirectoryOnly(true);
		dialog.restoreSettings();

		String title = ResourceMgr.getString("LblSelectDirTitle");
		WbConnection dbConnection = exporter.getConnection();
		DbMetadata meta = dbConnection.getMetadata();
		boolean answer = dialog.selectOutput(title);
		if (answer)
		{
			String fdir = dialog.getSelectedFilename();

			dialog.setExporterOptions(exporter);

			ExportType type = dialog.getExportType();
			String ext = type.getDefaultFileExtension();

			for (DbObject dbo : tables)
			{
				String ttype = dbo.getObjectType();
				if (ttype == null) continue;
				if (!meta.objectTypeCanContainData(ttype)) continue;
				if (!(dbo instanceof TableIdentifier)) continue;
				TableIdentifier tbl = (TableIdentifier)dbo;
				String fname = StringUtil.makeFilename(dbo.getObjectName());
				WbFile f = new WbFile(fdir, fname + ext);
				try
				{
					exporter.addTableExportJob(f, tbl);
				}
				catch (SQLException e)
				{
					LogMgr.logError("TableListPanel.exportTables()", "Error adding ExportJob", e);
					WbSwingUtilities.showMessage(caller, e.getMessage());
				}
			}
		}
	}

	public void startExport(Frame parent)
	{
		progress = new ProgressDialog(ResourceMgr.getString("MsgSpoolWindowTitle"), parent, exporter);
		progress.getInfoPanel().setMonitorType(RowActionMonitor.MONITOR_EXPORT);
		exporter.setRowMonitor(progress.getMonitor());
		progress.showProgress();
		exporter.addExecutionListener(this);
		exporter.startBackgroundExport();
	}

	@Override
	public void executionStart(WbConnection conn, Object source)
	{
	}

	@Override
	public void executionEnd(WbConnection conn, Object source)
	{
		if (progress != null)
		{
			progress.finished();
			progress.dispose();
		}
	}

}