/*
 * DwUpdatePreview.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2009, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.sql;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import workbench.WbManager;
import workbench.db.WbConnection;
import workbench.gui.WbSwingUtilities;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.storage.DataStore;
import workbench.storage.DmlStatement;
import workbench.storage.SqlLiteralFormatter;
import workbench.util.ExceptionUtil;
import workbench.util.MemoryWatcher;
import workbench.util.MessageBuffer;

/**
 * @author support@sql-workbench.net
 */
public class DwUpdatePreview
{
	public boolean confirmUpdate(Component caller, DataStore ds, WbConnection dbConn)
	{
		boolean doSave = true;
		
		Window win = SwingUtilities.getWindowAncestor(caller);
		MessageBuffer buffer = null;
		try
		{
			List<DmlStatement> stmts = ds.getUpdateStatements(dbConn);
			if (stmts.isEmpty()) return true;
			
			Dimension max = new Dimension(800,600);
			Dimension pref = new Dimension(400, 300);
			final EditorPanel preview = EditorPanel.createSqlEditor();
			preview.setEditable(false);
			preview.showFindOnPopupMenu();
			preview.setBorder(WbSwingUtilities.EMPTY_BORDER);
			preview.setPreferredSize(pref);
			preview.setMaximumSize(max);
			JScrollPane scroll = new JScrollPane(preview);
			scroll.setMaximumSize(max);
			int maxStatements = Settings.getInstance().getIntProperty("workbench.db.previewsql.maxstatements", 5000);
			if (maxStatements < stmts.size())
			{
				LogMgr.logWarning("DwUpdatePreview.confirmUpdate()", "Only " + maxStatements + " of " + stmts.size() + " statments displayed. To view all statements increase the value of the property 'workbench.db.previewsql.maxstatements'");
			}
			buffer = new MessageBuffer(maxStatements);

			SqlLiteralFormatter f = new SqlLiteralFormatter(dbConn);

			boolean lowMemory = false;
			for (DmlStatement dml : stmts)
			{
				buffer.append(dml.getExecutableStatement(f));
				buffer.append(";");
				buffer.appendNewLine();
				if (MemoryWatcher.isMemoryLow())
				{
					lowMemory = true;
					buffer.clear();
				}
			}

			if (lowMemory)
			{
				WbManager.getInstance().showLowMemoryError();
				return false;
			}

			final String text = buffer.getBuffer().toString();
			
			WbSwingUtilities.invoke(new Runnable()
			{
				public void run()
				{
					preview.setText(text);
					preview.setCaretPosition(0);
					preview.repaint();
				}
			});
			
			WbSwingUtilities.showDefaultCursor(caller);
			Runnable painter = new Runnable()
			{
				public void run()
				{
					preview.repaint();
				}
			};
			doSave = WbSwingUtilities.getOKCancel(ResourceMgr.getString("MsgConfirmUpdates"), win, scroll, painter);
		}
		catch (SQLException e)
		{
			LogMgr.logError("DwUpdatePreview.confirmUpdate()", "Error when previewing SQL", e);
			String msg = ExceptionUtil.getDisplay(e);
			WbSwingUtilities.showErrorMessage(win, msg);
			return false;
		}
		catch (OutOfMemoryError mem)
		{
			if (buffer != null)
			{
				buffer.clear();
				System.gc();
			}
			WbManager.getInstance().showOutOfMemoryError();
			return false;
		}
		catch (Throwable th)
		{
			LogMgr.logError("DwUpdatePreview.confirmUpdate()", "Error when previewing SQL", th);
			WbSwingUtilities.showErrorMessage(caller, ExceptionUtil.getDisplay(th));
			return false;
		}
		return doSave;
		
	}
}