/*
 * DataExporter.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2005, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: info@sql-workbench.net
 *
 */
package workbench.db.exporter;

import java.awt.Frame;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JDialog;

import javax.swing.JFrame;

import workbench.WbManager;
import workbench.db.TableIdentifier;
import workbench.db.WbConnection;
import workbench.exception.ExceptionUtil;
import workbench.gui.WbSwingUtilities;
import workbench.gui.dbobjects.ProgressPanel;
import workbench.gui.dialogs.export.ExportFileDialog;
import workbench.gui.dialogs.export.ExportOptions;
import workbench.gui.dialogs.export.HtmlOptions;
import workbench.gui.dialogs.export.SqlOptions;
import workbench.gui.dialogs.export.TextOptions;
import workbench.gui.dialogs.export.XmlOptions;
import workbench.interfaces.Interruptable;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.storage.DataStore;
import workbench.storage.ResultInfo;
import workbench.storage.RowActionMonitor;
import workbench.util.CharacterRange;
import workbench.util.FileDialogUtil;
import workbench.util.SqlUtil;
import workbench.util.StringUtil;
import workbench.util.WbThread;


/**
 *
 * @author  info@sql-workbench.net
 */
public class DataExporter
	implements Interruptable
{
	public static final int EXPORT_SQL = 1;
	public static final int EXPORT_TXT = 2;
	public static final int EXPORT_XML = 3;
	public static final int EXPORT_HTML = 4;

	private WbConnection dbConn;
	private String sql;
	private String htmlTitle = null;
	private String outputfile;
	private String fullOutputFileName;
	private String xsltFile = null;
	private String transformOutputFile = null;
	private int exportType;
	private boolean exportHeaders;
	private boolean includeCreateTable = false;
	private boolean headerOnly = false;
	
	private int sqlType = SqlRowDataConverter.SQL_INSERT;
	private boolean useCDATA = false;
	private CharacterRange escapeRange = null;
	private String lineEnding = StringUtil.LINE_TERMINATOR;
	private String tableName;
	private String sqlTable;
	private String encoding;
	private List columnsToExport;

	private String delimiter = "\t";
	private String quoteChar = null;
	private boolean quoteAlways = false;
	private String dateFormat = null;
	private String dateTimeFormat = null;
	private char decimalSymbol = '.';
	private String chrFunc = null;
	private String concatString = "||";
	private String concatFunction = null;
	private int commitEvery=0;

	private SimpleDateFormat dateFormatter = null;
	private	SimpleDateFormat dateTimeFormatter = null;
	private DecimalFormat numberFormatter = null;

	/** If true, then cr/lf characters will be removed from
	 *  character columns
	 */
	private boolean cleancr = false;
	private boolean append = false;
	private boolean escapeHtml = true;
	private boolean createFullHtmlPage = true;
	private boolean verboseFormat = true;

	private boolean showProgressWindow = false;
	public static final int DEFAULT_PROGRESS_INTERVAL = 10;
	private int progressInterval = DEFAULT_PROGRESS_INTERVAL;
	
	private ProgressPanel progressPanel;
	private JDialog progressWindow;
	private boolean keepRunning = true;
	private boolean cancelJobs = false;
	private int pendingJobs = 0;
	private boolean jobsRunning = false;
	private RowActionMonitor rowMonitor;

	private List keyColumnsToUse;

	private ArrayList warnings = new ArrayList();
	private ArrayList errors = new ArrayList();
	private ArrayList jobQueue;
	private ExportWriter exportWriter;
	private Window parentWindow;
	
	public DataExporter()
	{
	}

	private void openProgressMonitor(Window parent, boolean modal)
	{
		Frame f = null;
		if (parent instanceof Frame)
		{
			f = (Frame)parent;
		}
		openProgressMonitor(parent, modal);
	}
	
	private void createProgressPanel()
	{
		progressPanel = new ProgressPanel(this);
		this.progressPanel.setFilename(this.outputfile);
		this.progressPanel.setInfoText(ResourceMgr.getString("MsgSpoolStart"));
	}
	/**
	 *	Open the progress monitor window.
	 */
	private void openProgressMonitor(Frame parent, boolean modal)
	{
		File f = new File(this.outputfile);
		String fname = f.getName();

		if (this.progressPanel == null) createProgressPanel();
		
		this.progressWindow = new JDialog(parent, modal);
		this.progressWindow.getContentPane().add(progressPanel);
		this.progressWindow.pack();
		this.progressWindow.setTitle(ResourceMgr.getString("MsgSpoolWindowTitle"));
		//this.progressWindow.setIconImage(ResourceMgr.getPicture("SpoolData16").getImage());
		this.progressWindow.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				keepRunning = false;
			}
		});

		WbSwingUtilities.center(this.progressWindow, null);
		this.progressWindow.show();
	}

	public void addTableExportJob(String anOutputfile, String table)
	{
		if (this.jobQueue == null)
		{
			this.jobQueue = new ArrayList();
		}
		JobEntry job = new DataExporter.JobEntry();
		job.tableName = table;
		job.outputFile = anOutputfile;
		job.sqlStatement = "SELECT * FROM " + SqlUtil.quoteObjectname(table);
		this.jobQueue.add(job);
	}

	public void addJob(String anOutputfile, String aStatement)
	{
		if (this.jobQueue == null)
		{
			this.jobQueue = new ArrayList();
		}
		JobEntry job = new DataExporter.JobEntry();
		job.outputFile = anOutputfile;
		job.sqlStatement = aStatement;
		this.jobQueue.add(job);
	}

	public WbConnection getConnection()
	{
		return this.dbConn;
	}

	public void setConnection(WbConnection aConn)
	{
		this.dbConn = aConn;
	}

	public boolean confirmCancel()
	{
		if (!this.jobsRunning) return true;
		String msg = ResourceMgr.getString("MsgCancelAllCurrent");
		String current = ResourceMgr.getString("LabelCancelCurrentExport");
		String all = ResourceMgr.getString("LabelCancelAllExports");
		int answer = WbSwingUtilities.getYesNo(this.progressWindow, msg, new String[] { current, all });
		if (answer == 1)
		{
			this.cancelJobs = true;
		}
		return true;
	}

	public void cancelExecution()
	{
		this.keepRunning = false;
		if (this.exportWriter != null)
		{
			this.exportWriter.cancel();
		}
	}

	public void setTableName(String aTablename) { this.tableName = aTablename; }
	public String getTableName() { return this.tableName; }

	public void setEncoding(String enc) { this.encoding = enc; }
	public String getEncoding() { return this.encoding; }

	public void setRowMonitor(RowActionMonitor monitor)
	{
		this.rowMonitor = monitor;
		if (this.rowMonitor != null)
		{
			this.rowMonitor.setMonitorType(RowActionMonitor.MONITOR_EXPORT);
		}
	}

	/**
	 *	Define the columns that should be exported
	 *  This is only respected for the export of a DataStore, not
	 *  for exporting a ResultSet
	 *
	 *	@see #startExport(workbench.storage.DataStore)
	 */
	public void setColumnsToExport(List columns)
	{
		this.columnsToExport = columns;
	}

	public List getColumnsToExport()
	{
		return this.columnsToExport;
	}
	
	public void setExportAllColumns()
	{
		this.columnsToExport = null;
	}
	
	public void setUseCDATA(boolean flag) { this.useCDATA = flag; }
	public boolean getUseCDATA() { return this.useCDATA; }

	public void setAppendToFile(boolean aFlag) { this.append = aFlag; }
	public boolean getAppendToFile() { return this.append; }

	public void setExportHeaderOnly(boolean aFlag) { this.headerOnly = aFlag; }
	public boolean getExportHeaderOnly() { return this.headerOnly; }

	public void setCommitEvery(int aCount) { this.commitEvery = aCount; }
	public int getCommitEvery() { return this.commitEvery; }

	public String getTypeDisplay()
	{
		switch (this.exportType)
		{
			case EXPORT_HTML:
				return "HTML";
				
			case EXPORT_SQL:
				if (this.getSqlType() == SqlRowDataConverter.SQL_DELETE_INSERT)
					return "SQL DELETE/INSERT";
				else if (this.getSqlType() == SqlRowDataConverter.SQL_INSERT)
					return "SQL INSERT";
				else if (this.getSqlType() == SqlRowDataConverter.SQL_UPDATE)
					return "SQL UPDATE";
				
			case EXPORT_TXT:
				return "Text";
				
			case EXPORT_XML:
				return "XML";
		}
		return "";
	}
	
	/**
	 * Control the progress display in the RowActionMonitor
	 * This is used by the WBEXPORT command to turn off the row
	 * progress display. Turning off the display will speed up 
	 * the export because the GUI does not need to be updated
	 */
	public void setProgressInterval(int interval) 
	{ 
		if (interval <= 0)
			this.progressInterval = 0;
		else 
			this.progressInterval = interval;
	}
	public int getProgressInterval() { return this.progressInterval; }
	
	
	/** Control the display of a progress window. This is used
	 *  from within the DbExplorer
	 */
	public void setShowProgressWindow(boolean aFlag)
	{
	  if (!WbManager.getInstance().isBatchMode())
	  {
	    this.showProgressWindow = aFlag;
	  }
	}
	public boolean getShowProgressWindow() { return this.showProgressWindow; }

	public void setXsltTransformation(String xsltFileName)
	{
		this.xsltFile = xsltFileName;
	}
	public String getXsltTransformation() { return this.xsltFile; }

	public void setXsltTransformationOutput(String aFilename)
	{
		this.transformOutputFile = aFilename;
	}
	public String getXsltTransformationOutput() { return this.transformOutputFile; }

	public void setExportHeaders(boolean aFlag) { this.exportHeaders = aFlag; }
	public boolean getExportHeaders() { return this.exportHeaders; }

	public void setCreateFullHtmlPage(boolean aFlag) { this.createFullHtmlPage = aFlag; }
	public boolean getCreateFullHtmlPage() { return this.createFullHtmlPage; }

	public void setEscapeHtml(boolean aFlag) { this.escapeHtml = aFlag; }
	public boolean getEscapeHtml() { return this.escapeHtml; }

	public void setTextDelimiter(String aDelimiter)
	{
		if (aDelimiter != null && aDelimiter.trim().length() > 0) this.delimiter = aDelimiter;
	}
	public String getTextDelimiter() { return this.delimiter; }

	public void setTextQuoteChar(String aQuote) { this.quoteChar = aQuote; }
	public String getTextQuoteChar() { return this.quoteChar; }

	public void setDateFormat(String aFormat)
	{
		this.dateFormat = aFormat;
		if (this.dateFormat != null)
		{
			try
			{
				dateFormatter = new SimpleDateFormat(this.dateFormat);
			}
			catch (IllegalArgumentException i)
			{
				this.addWarning(ResourceMgr.getString("ErrorWrongDateFormat") + " " + this.dateFormat);
				dateFormatter = null;
			}
		}
	}

	public SimpleDateFormat getDateFormatter()
	{
		return this.dateFormatter;
	}

	public String getDateFormat()
	{
		return this.dateFormat;
	}

	public void setTimestampFormat(String aFormat)
	{
		this.dateTimeFormat = aFormat;
		if (this.dateTimeFormat != null)
		{
			try
			{
				dateTimeFormatter = new SimpleDateFormat(this.dateTimeFormat);
			}
			catch (Exception e)
			{
				this.warnings.add(ResourceMgr.getString("ErrorWrongDateFormat") + " " + this.dateTimeFormat);
				dateTimeFormatter = null;
			}
		}
	}

	public String getTimestampFormat() { return this.dateTimeFormat; }
	public SimpleDateFormat getTimestampFormatter()
	{
		return this.dateTimeFormatter;
	}

	public void setHtmlTitle(String aTitle) { this.htmlTitle = aTitle; }
	public String getHtmlTitle() { return this.htmlTitle; }

	public void setOutputTypeHtml() { this.exportType = EXPORT_HTML; }
	public void setOutputTypeXml() { this.exportType = EXPORT_XML; }
	public void setOutputTypeText() { this.exportType = EXPORT_TXT; }

	public void setOutputTypeSqlInsert()
	{
		this.exportType = EXPORT_SQL;
		this.sqlType = SqlRowDataConverter.SQL_INSERT;
	}

	public void setOutputTypeSqlUpdate()
	{
		this.exportType = EXPORT_SQL;
		this.sqlType = SqlRowDataConverter.SQL_UPDATE;
	}
	
	public void setOutputTypeSqlDeleteInsert()
	{
		this.exportType = EXPORT_SQL;
		this.sqlType = SqlRowDataConverter.SQL_DELETE_INSERT;
	}

	public int getSqlType()
	{
		if (this.exportType == EXPORT_SQL)
			return this.sqlType;
		else
			return -1;
	}
		
	public void setOutputFilename(String aFilename) { this.outputfile = aFilename; }

	public String getOutputFilename() { return this.outputfile; }
	public String getFullOutputFilename() { return this.fullOutputFileName; }

	public void setCleanupCarriageReturns(boolean aFlag) { this.cleancr = aFlag; }
	public boolean getCleanupCarriageReturns() { return this.cleancr; }

	public void setConcatString(String aConcatString)
	{
		if (aConcatString == null) return;
		this.concatString = aConcatString;
		this.concatFunction = null;
	}
	public String getConcatString() { return this.concatString; }

	public void setChrFunction(String aFunc) { this.chrFunc = aFunc; }
	public String getChrFunction() { return this.chrFunc; }

	public void setDecimalSymbol(char aSymbol)
	{
		this.decimalSymbol = aSymbol;
		if (this.decimalSymbol != 0)
		{
			DecimalFormatSymbols symbols = new DecimalFormatSymbols();
			symbols.setDecimalSeparator(this.decimalSymbol);
			numberFormatter = new DecimalFormat("0.#", symbols);
			numberFormatter.setGroupingUsed(false);
			numberFormatter.setMaximumFractionDigits(999);
		}
	}
	public DecimalFormat getDecimalFormatter() { return this.numberFormatter; }


	public void setDecimalSymbol(String aSymbol)
	{
		if (aSymbol == null || aSymbol.length() == 0) return;
		this.setDecimalSymbol(aSymbol.charAt(0));
	}

	public char getDecimalSymbol() { return this.decimalSymbol; }


	public void setSql(String aSql)
	{
		this.sql = aSql;
		String cleanSql = SqlUtil.makeCleanSql(aSql, false);
		List tables = SqlUtil.getTables(cleanSql);
		if (tables.size() == 1);
		{
			this.sqlTable = (String)tables.get(0);
		}
	}

	public String getSql() { return this.sql; }

	private void startBackgroundThread()
	{
		Thread t = new WbThread("Export")
		{
			public void run()
			{
				try { startExport(); } catch (Throwable th) {}
			}
		};
		t.start();
	}

	public void startExportJobs()
	{
		Thread t = new WbThread("Export Jobs")
		{
			public void run()
			{
				try { runJobs(); } catch (Throwable th) {}
			}
		};
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	public void runJobs()
	{
		if (this.jobQueue == null) return;
		int count = this.jobQueue.size();
		this.pendingJobs = count;
		this.jobsRunning = true;
		this.cancelJobs = false;
		for (int i=0; i < count; i++)
		{
			JobEntry job = (JobEntry)this.jobQueue.get(i);
			this.setSql(job.sqlStatement);
			this.setOutputFilename(job.outputFile);
			if (this.progressPanel != null)
			{
				this.progressPanel.setFilename(this.outputfile);
				this.progressPanel.setRowInfo(0);
			}
			if (this.rowMonitor != null && job.tableName != null)
			{
				this.rowMonitor.setCurrentObject(job.tableName, i + 1, count);
			}

			try
			{
				this.startExport();
			}
			catch (Throwable th)
			{
				LogMgr.logError("DataExporter.runJobs()", "Error exporting data for [" + this.sql + "] to file: " + this.outputfile, th);
				this.addError(th.getMessage());
			}
			this.pendingJobs --;
			if (this.cancelJobs) break;
		}
		this.pendingJobs = 0;
		this.jobsRunning = false;
		this.closeProgress();
	}

	public void setCurrentRow(int currentRow)
	{
		if (this.rowMonitor != null)
		{
			this.rowMonitor.setCurrentRow(currentRow, -1);
		}
	}

	public long startExport()
		throws IOException, SQLException
	{
		Statement stmt = this.dbConn.createStatement();
		ResultSet rs = null;
		long rows = 0;
		try
		{
			stmt.setFetchSize(1500);
		}
		catch (Exception e)
		{
		}

		try
		{
			stmt.execute(this.sql);
			rs = stmt.getResultSet();
			rows = this.startExport(rs);
		}
		catch (Exception e)
		{
			this.addError(ExceptionUtil.getDisplay(e));
			LogMgr.logError("DataExporter.startExport()", "Could not execute SQL statement: " + e.getMessage(), e);
			if (this.showProgressWindow)
			{
				if (!jobsRunning) this.closeProgress();
				WbSwingUtilities.showErrorMessage(this.parentWindow, ResourceMgr.getString("MsgExecuteError") + ": " + e.getMessage());
			}
		}
		finally
		{
			try { rs.close(); } catch (Throwable th) {}
			try { stmt.close(); } catch (Throwable th) {}
			if (!jobsRunning) this.closeProgress();
		}
		return rows;
	}

	public boolean isSuccess() { return this.errors.size() == 0; }
	public boolean hasWarning() { return this.warnings.size() > 0; }
	public boolean hasError() { return this.errors.size() > 0; }

	public String[] getErrors()
	{
		int count = this.errors.size();
		String[] result = new String[count];
		for (int i=0; i < count; i++)
		{
			result[i] = (String)this.errors.get(i);
		}
		return result;
	}

	public String[] getWarnings()
	{
		int count = this.warnings.size();
		String[] result = new String[count];
		for (int i=0; i < count; i++)
		{
			result[i] = (String)this.warnings.get(i);
		}
		return result;
	}

	public void addWarning(String msg)
	{
		if (this.warnings == null) this.warnings = new ArrayList();
		this.warnings.add(msg);
	}

	public void addError(String msg)
	{
		if (this.errors == null) this.errors = new ArrayList();
		this.errors.add(msg);
	}

	private String getDefaultEncoding()
	{
		String enc = System.getProperty("file.encoding");
		// We prefer a standard encoding instead of the windows encoding
		if ("Cp1252".equals(enc)) return "ISO-8859-15";
		return enc;
	}

	public long startExport(ResultSet rs)
		throws IOException, SQLException, Exception
	{
		try
		{
			ResultSetMetaData meta = rs.getMetaData();
			ResultInfo info = new ResultInfo(meta, this.dbConn);
			this.exportWriter = createExportWriter(info);
			if (this.progressPanel != null) this.progressPanel.setInfoText(ResourceMgr.getString("MsgSpoolingRow"));
			this.exportWriter.writeExport(rs, info);
		}
		catch (SQLException e)
		{
			this.errors.add(e.getMessage());
			LogMgr.logError("DataExporter", "SQL Error", e);
			throw e;
		}
		finally
		{
			if (this.exportWriter != null) this.exportWriter.exportFinished();
			if (!jobsRunning) this.closeProgress();
			try { rs.close(); } catch (Throwable th) {}
		}
		long numRows = this.exportWriter.getNumberOfRecords();
		return numRows;
	}

	public long startExport(DataStore ds)
		throws IOException, SQLException, Exception
	{
		try
		{
			ResultInfo info = ds.getResultInfo();
			this.exportWriter = createExportWriter(info);
			this.exportWriter.writeExport(ds);
		}
		catch (SQLException e)
		{
			this.errors.add(e.getMessage());
			LogMgr.logError("DataExporter", "Error when exporting DataStore", e);
			throw e;
		}
		finally
		{
			this.exportWriter.exportFinished();
			if (!jobsRunning) this.closeProgress();
		}
		long numRows = this.exportWriter.getNumberOfRecords();
		return numRows;
	}

	/**
	 *	Export a table to an external file.
	 */
	public ExportWriter createExportWriter(ResultInfo info)
		throws IOException, SQLException, Exception
	{
		if (!jobsRunning)
		{
			this.warnings.clear();
			this.errors.clear();
		}
		if (this.sqlTable != null)
		{
			info.setUpdateTable(new TableIdentifier(this.sqlTable));
		}

		if (this.encoding == null) this.encoding = getDefaultEncoding();
		ExportWriter exporter = null;

		switch (this.exportType)
		{
			case EXPORT_HTML:
				exporter = new HtmlExportWriter(this);
				break;
			case EXPORT_SQL:
				exporter = new SqlExportWriter(this);
				break;
			case EXPORT_TXT:
				exporter = new TextExportWriter(this);
				break;
			case EXPORT_XML:
				exporter = new XmlExportWriter(this);
		}

		if (this.tableName != null)
		{
			exporter.setTableToUse(this.tableName);
		}
		
		BufferedWriter pw = null;
		final int buffSize = 64*1024;
		
		try
		{
			File f = new File(this.outputfile);
			this.fullOutputFileName = f.getAbsolutePath();

			// first try to open the file using the current encoding
			if (this.encoding != null)
			{
				try
				{
					OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(f, this.append), this.encoding);
					pw = new BufferedWriter(out, buffSize);
				}
				catch (UnsupportedEncodingException e)
				{
					pw = null;
					String msg = ResourceMgr.getString("ErrorExportWronEncoding");
					msg = StringUtil.replace(msg, "%encoding%", this.encoding);
					this.encoding = null;
					this.addWarning(msg);
				}
			}

			// if opening the file with an encoding failed, open the file
			// without encoding (thus using the
			if (pw == null)
			{
				pw = new BufferedWriter(new FileWriter(f,this.append), buffSize);
			}
			exporter.setOutput(pw);
		}
		catch (IOException e)
		{
			this.errors.add(e.getMessage());
			LogMgr.logError("DataExporter", "Error writing data file", e);
			throw e;
		}
		finally
		{
		}
		
		if (this.progressInterval > 0)
		{
			exporter.setRowMonitor(this.rowMonitor);
			exporter.setProgressInterval(this.progressInterval);
		}
		else if (this.rowMonitor != null)
		{
			this.rowMonitor.setMonitorType(RowActionMonitor.MONITOR_PLAIN);
			String msg = ResourceMgr.getString("MsgExportingData") + " " + this.fullOutputFileName;
			this.rowMonitor.setCurrentObject(msg, -1, -1);
			Thread.yield();
		}

		if (this.showProgressWindow)
		{
			if (this.progressPanel == null) createProgressPanel();//this.openProgressMonitor(this.parentWindow, false);
			exporter.setRowMonitor(this.progressPanel);
		}
		
		return exporter;
	}

	public void closeProgress()
	{
		if (this.progressWindow != null)
		{
			this.progressWindow.hide();
			this.progressWindow.dispose();
			this.progressPanel = null;
		}
	}

	public void executeStatement(WbConnection aConnection, String aSql)
	{
		this.executeStatement(null, aConnection, aSql, false);
	}

	public boolean selectOutput(Window parent)
	{
		ExportFileDialog dialog = new ExportFileDialog(parent);
		dialog.setIncludeSqlInsert(true);
		dialog.setIncludeSqlUpdate(true);

		boolean result = dialog.selectOutput();
		if (result)
		{
			dialog.setExporterOptions(this);
		}
		return result;
	}
	
	public void executeStatement(Window aParent, WbConnection aConnection, String aSql, boolean modal)
	{
		this.setSql(aSql);
		boolean includeSqlExport = this.sqlTable != null;
		this.parentWindow = aParent;
		ExportFileDialog dialog = new ExportFileDialog(aParent);
		dialog.setIncludeSqlInsert(true);
		dialog.setIncludeSqlUpdate(true);

		boolean result = dialog.selectOutput();
		if (result)
		{
			try
			{
				this.setConnection(aConnection);
				dialog.setExporterOptions(this);
				this.setShowProgressWindow(true);
				Frame parent = null;
				if (aParent instanceof Frame)
				{
					parent = (Frame)aParent;
				}
				this.startBackgroundThread();
				this.openProgressMonitor(parent, modal);
			}
			catch (Exception e)
			{
				LogMgr.logError("DataExporter.executeStatement()", "Could not export data", e);
				if (aParent != null)
				{
					WbSwingUtilities.showErrorMessage(aParent, ExceptionUtil.getDisplay(e));
				}
			}
		}
	}

	public void setOptions(ExportOptions options)
	{
		this.setEncoding(options.getEncoding());
		this.setDateFormat(options.getDateFormat());
		this.setTimestampFormat(options.getTimestampFormat());
		this.setEncoding(options.getEncoding());
	}

	public void setSqlOptions(SqlOptions sqlOptions)
	{
		if (sqlOptions.getCreateInsert())
		{
			this.setOutputTypeSqlInsert();
		}
		else if (sqlOptions.getCreateUpdate())
		{
			this.setOutputTypeSqlUpdate();
		}
		else if (sqlOptions.getCreateDeleteInsert())
		{
			this.setOutputTypeSqlDeleteInsert();
		}
		this.setIncludeCreateTable(sqlOptions.getCreateTable());
		this.setCommitEvery(sqlOptions.getCommitEvery());
		this.setTableName(sqlOptions.getAlternateUpdateTable());
		this.setKeyColumnsToUse(sqlOptions.getKeyColumns());
	}

	public void setXmlOptions(XmlOptions xmlOptions)
	{
		this.setOutputTypeXml();
		this.setUseCDATA(xmlOptions.getUseCDATA());
		this.setUseVerboseFormat(xmlOptions.getUseVerboseXml());
	}

	public void setHtmlOptions(HtmlOptions html)
	{
		this.setOutputTypeHtml();
		this.setCreateFullHtmlPage(html.getCreateFullPage());
		this.setHtmlTitle(html.getPageTitle());
		this.setEscapeHtml(html.getEscapeHtml());
	}

	public void setTextOptions(TextOptions text)
	{
		this.setOutputTypeText();
		this.setExportHeaders(text.getExportHeaders());
		this.setCleanupCarriageReturns(text.getCleanupCarriageReturns());
		this.setTextDelimiter(text.getTextDelimiter());
		this.setTextQuoteChar(text.getTextQuoteChar());
		this.setQuoteAlways(text.getQuoteAlways());
		this.setEscapeRange(text.getEscapeRange());
		this.setLineEnding(text.getLineEnding());
	}

	public boolean isIncludeCreateTable()
	{
		return includeCreateTable;
	}

	public void setIncludeCreateTable(boolean includeCreateTable)
	{
		this.includeCreateTable = includeCreateTable;
	}

	/**
	 * Getter for property keyColumnsToUse.
	 * @return Value of property keyColumnsToUse.
	 */
	public java.util.List getKeyColumnsToUse()
	{
		return keyColumnsToUse;
	}

	/**
	 * Setter for property keyColumnsToUse.
	 * @param keyColumnsToUse New value of property keyColumnsToUse.
	 */
	public void setKeyColumnsToUse(java.util.List keyColumnsToUse)
	{
		this.keyColumnsToUse = keyColumnsToUse;
	}

	/**
	 * Getter for property concatFunction.
	 * @return Value of property concatFunction.
	 */
	public java.lang.String getConcatFunction()
	{
		return concatFunction;
	}

	/**
	 * Setter for property concatFunction.
	 * @param func New value of property concatFunction.
	 */
	public void setConcatFunction(String func)
	{
		this.concatFunction = func;
		this.concatString = null;
	}

	private class JobEntry
	{
		private String outputFile;
		private String sqlStatement;
		private String tableName;
	}

	public boolean getQuoteAlways()
	{
		return quoteAlways;
	}

	public void setQuoteAlways(boolean flag)
	{
		this.quoteAlways = flag;
	}

	public void setEscapeRange(CharacterRange range)
	{
		this.escapeRange = range;
	}

	public CharacterRange getEscapeRange()
	{
		return this.escapeRange;
	}

	public void setLineEnding(String ending)
	{
		if (ending != null) this.lineEnding = ending;
	}

	public String getLineEnding()
	{
		return this.lineEnding;
	}

	public boolean getUseVerboseFormat()
	{
		return verboseFormat;
	}

	public void setUseVerboseFormat(boolean flag)
	{
		this.verboseFormat = flag;
	}

}
