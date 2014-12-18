/*
 * BatchRunner.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2007, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.sql;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.List;
import workbench.WbManager;
import workbench.db.ConnectionMgr;
import workbench.db.ConnectionProfile;
import workbench.db.WbConnection;
import workbench.gui.components.ConsoleStatusBar;
import workbench.gui.components.GenericRowMonitor;
import workbench.gui.profiles.ProfileKey;
import workbench.interfaces.ParameterPrompter;
import workbench.interfaces.ResultLogger;
import workbench.interfaces.StatementRunner;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.storage.DataPrinter;
import workbench.storage.DataStore;
import workbench.storage.RowActionMonitor;
import workbench.util.ArgumentParser;
import workbench.util.EncodingUtil;
import workbench.util.ExceptionUtil;
import workbench.util.FileDialogUtil;
import workbench.util.SqlUtil;
import workbench.util.StringUtil;
import workbench.util.WbFile;

/**
 * A class to run several statements from a script file. This is used
 * when running SQL Workbench in batch mode and for the {@link workbench.sql.wbcommands.WbInclude}
 * command.
 * 
 * @author  support@sql-workbench.net
 */
public class BatchRunner
{
	public static final String CMD_LINE_PROFILE_NAME = "WbCommandLineProfile";
	private List<String> filenames;
	private StatementRunner stmtRunner;
	private WbConnection connection;
	private boolean abortOnError = false;
	private String successScript;
	private String errorScript;
	private DelimiterDefinition delimiter = null;
	private boolean showResultSets = false;
	private boolean showTiming = true;
	private boolean success = true;
	private ConnectionProfile profile;
	private ResultLogger resultDisplay;
	private boolean cancelExecution = false;
	private RowActionMonitor rowMonitor;
	private boolean verboseLogging = true;
	private boolean checkEscapedQuotes = false;
	private String encoding = null;
	private boolean showProgress = false;
	private PrintStream console = System.out;
	private boolean quiet = false;
	
	public BatchRunner(String aFilelist)
	{
		this.filenames = StringUtil.stringToList(aFilelist, ",", true);
		this.stmtRunner = new DefaultStatementRunner();
		this.stmtRunner.setFullErrorReporting(true);
	}

	/**
	 * The baseDir is used when including other scripts using WbInclude
	 * If the filename of the included script is a relative filename
	 * then the scriptrunner will assume the script is located in the 
	 * baseDir
	 * @see StatementRunner#setBaseDir(String)
	 */
	public void setBaseDir(String dir) 
	{ 
		this.stmtRunner.setBaseDir(dir);
	}

	public WbConnection getConnection() 
	{
		return this.connection;
	}
	
	public void setParameterPrompter(ParameterPrompter p)
	{
		this.stmtRunner.setParameterPrompter(p);
	}
	
	/**
	 * For testing purposes to redirect the output to a file
	 */
	void setConsole(PrintStream output)
	{
		this.console = output;
	}
	
	public void showResultSets(boolean flag)
	{
		this.showResultSets = flag;
	}

	public void setVerboseLogging(boolean flag)
	{
		this.verboseLogging = flag;
		this.stmtRunner.setVerboseLogging(flag);
		this.showTiming = this.verboseLogging;
	}

	public void setIgnoreDropErrors(boolean flag)
	{
		this.stmtRunner.setIgnoreDropErrors(flag);
	}
	
	public void setProfile(ProfileKey def)
	{
		LogMgr.logInfo("BatchRunner", ResourceMgr.getString("MsgBatchConnecting") + " [" + def.getName() + "]");
		ConnectionMgr mgr = ConnectionMgr.getInstance();
		ConnectionProfile prof = mgr.getProfile(def);
		if (prof == null)
		{
			LogMgr.logError("BatchRunner", ResourceMgr.getString("ErrConnectionError"),null);
			throw new IllegalArgumentException("Could not find profile " + def.getName());
		}
		this.setProfile(prof);
	}

	public void setProfile(ConnectionProfile aProfile)
	{
		this.profile = aProfile;
	}

	public DelimiterDefinition getDelimiter() 
	{
		return this.delimiter;
	}
	
	public void setDelimiter(DelimiterDefinition delim) 
	{ 
		if (delim != null) this.delimiter = delim; 
	}

	public void setConnection(WbConnection conn)
	{
		this.connection = conn;
		this.stmtRunner.setConnection(this.connection);
	}

	public boolean isConnected()
	{
		return this.connection != null;
	}
	
	public void connect()
		throws SQLException, ClassNotFoundException
	{
		this.connection = null;
		
		if (this.profile == null)
		{
			// Allow batch run without a profile for e.g. running a single WbCopy
			LogMgr.logWarning("BatchRunner.connect()", "Called without profile!");
			success = true;
			return;
		}
		
		try
		{
			ConnectionMgr mgr = ConnectionMgr.getInstance();
			WbConnection c = mgr.getConnection(this.profile, "BatchRunner");
			
			this.setConnection(c);
			String info = null;
			if (c.getProfile().getName().equals(CMD_LINE_PROFILE_NAME))
			{
				info = c.getDisplayString();
			}
			else
			{
				info = c.getProfile().getName();
			}
			LogMgr.logInfo("BatchRunner.connect()",  ResourceMgr.getFormattedString("MsgBatchConnectOk", c.getDisplayString()));
			if (!quiet) this.printMessage(ResourceMgr.getFormattedString("MsgBatchConnectOk", info));
		}
		catch (ClassNotFoundException e)
		{
			String error = ResourceMgr.getString("ErrDriverNotFound");
			error = StringUtil.replace(error, "%class%", profile.getDriverclass());
			LogMgr.logError("BatchRunner.connect()", error, null);
			printMessage(error);
			success = false;
			throw e;
		}
		catch (SQLException e)
		{
			success = false;
			String msg = ResourceMgr.getString("MsgBatchConnectError") + ": " + ExceptionUtil.getDisplay(e);
			LogMgr.logError("BatchRunner.connect()", msg, null);
			printMessage(msg);
			throw e;
		}
	}

	public boolean isSuccess()
	{
		return this.success;
	}

	public void setSuccessScript(String aFilename)
	{
		if (aFilename == null) return;
		File f = new File(aFilename);
		if (f.exists() && !f.isDirectory())
			this.successScript = aFilename;
		else
			this.successScript = null;
	}

	public void setErrorScript(String aFilename)
	{
		if (aFilename == null) return;
		File f = new File(aFilename);
		if (f.exists() && !f.isDirectory())
			this.errorScript = aFilename;
		else
			this.errorScript = null;
	}

	public void setRowMonitor(RowActionMonitor mon)
	{
		this.rowMonitor = mon;
		this.stmtRunner.setRowMonitor(this.rowMonitor);
	}

	public void execute()
		throws IOException
	{
		boolean error = false;
		int count = this.filenames.size();

		if (this.rowMonitor  != null)
		{
			this.rowMonitor.setMonitorType(RowActionMonitor.MONITOR_PROCESS);
		}

		int currentFileIndex = 0;

		for (String file : filenames)
		{
			currentFileIndex ++;

			WbFile fo = new WbFile(file);
			
			if (this.rowMonitor != null)
			{
				this.rowMonitor.setCurrentObject(file, currentFileIndex, count);
				this.rowMonitor.saveCurrentType("batchrunnerMain");
			}

			try
			{
				String msg = ResourceMgr.getString("MsgBatchProcessingFile") + " " + fo.getFullPath();
				LogMgr.logInfo("BatchRunner.execute()", msg);
				if (this.resultDisplay != null)
				{
					this.resultDisplay.appendToLog(msg);
					this.resultDisplay.appendToLog("\n");
				}
				String dir = fo.getCanonicalFile().getParent();
				this.setBaseDir(dir);
				
				error = this.executeScript(fo);
			}
			catch (Exception e)
			{
				error = true;
				LogMgr.logError("BatchRunner.execute()", ResourceMgr.getString("MsgBatchScriptFileError") + " " + file, e);
				String msg = null;
				
				if (e instanceof FileNotFoundException)
				{
					msg = ResourceMgr.getFormattedString("ErrFileNotFound", file);
				}
				else
				{
					msg = e.getMessage();
				}
				if (showProgress) printMessage("\n"); // force newline in case progress reporting was turned on
				printMessage(ResourceMgr.getString("TxtError") + ": " + msg);
			}
			if (error && abortOnError)
			{
				break;
			}
		}

		if (abortOnError && error)
		{
			this.success = false;
			try
			{
				if (this.errorScript != null)
				{
					LogMgr.logInfo("BatchRunner", ResourceMgr.getString("MsgBatchExecutingErrorScript") + " " + this.errorScript);
					this.executeScript(new File(this.errorScript));
				}
			}
			catch (Exception e)
			{
				LogMgr.logError("BatchRunner.execute()", ResourceMgr.getString("MsgBatchScriptFileError") + " " + this.errorScript, e);
			}
		}
		else
		{
			this.success = true;
			try
			{
				if (this.successScript != null)
				{
					LogMgr.logInfo("BatchRunner", ResourceMgr.getString("MsgBatchExecutingSuccessScript") + " " + this.successScript);
					this.executeScript(new File(this.successScript));
				}
			}
			catch (Exception e)
			{
				LogMgr.logError("BatchRunner.execute()", ResourceMgr.getString("MsgBatchScriptFileError") + " " + this.successScript, e);
			}
		}
	}

	public void cancel()
	{
		this.cancelExecution = true;
		if (this.stmtRunner != null)
		{
			this.stmtRunner.cancel();
		}
	}

	private boolean executeScript(File scriptFile)
		throws IOException
	{
		boolean error = false;
		StatementRunnerResult result = null;
		ScriptParser parser = new ScriptParser();
		DelimiterDefinition altDelim = null;
		
		// If no delimiter has been defined, than use the default fallback 
		if (this.delimiter == null)
		{
			altDelim = Settings.getInstance().getAlternateDelimiter(this.connection);
			parser.setDelimiters(DelimiterDefinition.STANDARD_DELIMITER, altDelim);
		}
		else
		{
			// if a delimiter has been defined, then use only this
			parser.setDelimiters(this.delimiter, null);
		}
		
		if (this.connection != null)
		{
			parser.setSupportOracleInclude(this.connection.getDbSettings().supportSingleLineCommands());
			parser.setCheckForSingleLineCommands(this.connection.getDbSettings().supportShortInclude());
		}
		parser.setCheckEscapedQuotes(this.checkEscapedQuotes);
		parser.setFile(scriptFile, this.encoding);
		String sql = null;
		this.cancelExecution = false;

		int executedCount = 0;
		long start, end;

		int interval;
		if (scriptFile.length() < 50000)
		{
			interval = 1;
		}
		else if (scriptFile.length() < 100000)
		{
			interval = 10;
		}
		else
		{
			interval = 100;
		}

		start = System.currentTimeMillis();

		parser.startIterator();

		while (parser.hasNext())
		{
			sql = parser.getNextCommand();
			if (sql == null) continue;

			try
			{
				if (this.resultDisplay == null && LogMgr.isDebugEnabled()) LogMgr.logDebug("BatchRunner", ResourceMgr.getString("MsgBatchExecutingStatement") + " "  + sql);
				long verbstart = System.currentTimeMillis();
				this.stmtRunner.runStatement(sql, 0, -1);
				long verbend = System.currentTimeMillis();
				
				error = false;
				
				result = this.stmtRunner.getResult();
				
				if (result != null)
				{
					error = !result.isSuccess();
					
					// We have to store the result of hasMessages() 
					// as the getMessageBuffer() will clear the buffer
					// and a subsequent call to hasMessages() will return false;
					boolean hasMessage = result.hasMessages();
					String feedback = result.getMessageBuffer().toString();
					
					if (error) LogMgr.logError("BatchRunner.execute()", feedback, null);
					else if (result.hasWarning()) LogMgr.logWarning("BatchRunner.execute()", feedback);
					
					if (hasMessage && (this.stmtRunner.getVerboseLogging() || error))
					{
						this.printMessage("\n" + feedback);
					}
					else if (result.hasWarning())
					{
						String verb = SqlUtil.getSqlVerb(sql);
						String msg = StringUtil.replace(ResourceMgr.getString("MsgStmtCompletedWarn"), "%verb%", verb);	
						this.printMessage("\n" + msg);
					}
					executedCount ++;
				}

				if (this.showTiming)
				{
					this.printMessage(ResourceMgr.getString("MsgSqlVerbTime") + " " + (((double)(verbend - verbstart)) / 1000.0) + "s");
				}

				if (this.rowMonitor != null && (executedCount % interval == 0))
				{
					this.rowMonitor.restoreType("batchrunnerMain");
					this.rowMonitor.setCurrentRow(executedCount, -1);
				}

				if (this.cancelExecution)
				{
					if (!quiet) this.printMessage(ResourceMgr.getString("MsgStatementCancelled"));
					break;
				}

				if (this.showResultSets 
					&& result != null 
					&& result.isSuccess() 
					&& result.hasDataStores() 
					&& this.console != null)
				{
					console.println();
					console.println(sql);
					console.println("---------------- " + ResourceMgr.getString("MsgResultLogStart") + " ----------------------------");
					List<DataStore> data = result.getDataStores();
					for (DataStore ds : data)
					{
						DataPrinter printer = new DataPrinter(ds);
						printer.printTo(console);
					}
					console.println("---------------- " + ResourceMgr.getString("MsgResultLogEnd") + "   ----------------------------");
				}
			}
			catch (Exception e)
			{
				LogMgr.logError("BatchRunner", ResourceMgr.getString("MsgBatchStatementError") + " "  + sql, e);
				printMessage(ExceptionUtil.getDisplay(e));
				error = true;
				break;
			}
			if (error && abortOnError) break;
		}
		
		end = System.currentTimeMillis();
		
		StringBuilder msg = new StringBuilder();
		msg.append(scriptFile.getCanonicalPath());
		msg.append(": ");
		msg.append(executedCount);
		msg.append(' ');
		msg.append(ResourceMgr.getString("MsgTotalStatementsExecuted"));
		if (resultDisplay == null) msg.insert(0, '\n'); // force newline on console
		this.printMessage(msg.toString());
		
		parser.done();

		if (this.showTiming)
		{
			long execTime = (end - start);
			String m = ResourceMgr.getString("MsgExecTime") + " " + (((double)execTime) / 1000.0) + "s";
			this.printMessage(m);
		}

		return error;
	}

	public void setEncoding(String enc)
		throws UnsupportedEncodingException
	{
		if (enc == null)
		{
			this.encoding = null;
		}
		else
		{
			if (!EncodingUtil.isEncodingSupported(enc))
				throw new UnsupportedEncodingException(enc + " encoding not supported!");
			this.encoding = EncodingUtil.cleanupEncoding(enc);
		}
	}

	public void setShowTiming(boolean flag)
	{
		this.showTiming = flag;
	}

	public void setAbortOnError(boolean aFlag)
	{
		this.abortOnError = aFlag;
	}

	public void setCheckEscapedQuotes(boolean flag)
	{
		this.checkEscapedQuotes = flag;
	}

	public void setResultLogger(ResultLogger logger)
	{
		this.resultDisplay = logger;
		if (this.stmtRunner != null)
		{
			this.stmtRunner.setResultLogger(logger);
		}
	}

	private void printMessage(String msg)
	{
		if (this.resultDisplay == null)
		{
			if (msg != null && msg.length() > 0) System.out.println(msg);
		}
		else
		{
			this.resultDisplay.appendToLog(msg);
			this.resultDisplay.appendToLog("\n");
		}
	}

	public static ConnectionProfile createCmdLineProfile(ArgumentParser cmdLine)
	{
		ConnectionProfile result = null;
		if (!cmdLine.isArgPresent(WbManager.ARG_CONN_URL)) return null;
		try
		{
			String url = cmdLine.getValue(WbManager.ARG_CONN_URL);
			if (url == null) 
			{
				LogMgr.logError("BatchRunner.createCmdLineProfile()", "Cannot connect with command line settings without a connection URL!", null);
				return null;
			}
			String driverclass = cmdLine.getValue(WbManager.ARG_CONN_DRIVER);
			if (driverclass == null) 
			{
				LogMgr.logError("BatchRunner.createCmdLineProfile()", "Cannot connect with command line settings without a driver class!", null);
				return null;
			}
			String user = cmdLine.getValue(WbManager.ARG_CONN_USER);
			String pwd = cmdLine.getValue(WbManager.ARG_CONN_PWD);
			String jar = cmdLine.getValue(WbManager.ARG_CONN_JAR);
			String commit =  cmdLine.getValue(WbManager.ARG_CONN_AUTOCOMMIT);
			String wksp = cmdLine.getValue(WbManager.ARG_WORKSPACE);
			String delimDef = cmdLine.getValue(WbManager.ARG_ALT_DELIMITER);
			DelimiterDefinition delim = DelimiterDefinition.parseCmdLineArgument(delimDef);
			
			boolean rollback = cmdLine.getBoolean(WbManager.ARG_CONN_ROLLBACK, false);
			
			if (jar != null)
			{
				ConnectionMgr.getInstance().registerDriver(driverclass, jar);
			}
			
			result = new ConnectionProfile(CMD_LINE_PROFILE_NAME, driverclass, url, user, pwd);
			result.setRollbackBeforeDisconnect(rollback);
			result.setAlternateDelimiter(delim);
			if (!StringUtil.isEmptyString(wksp))
			{
				wksp = FileDialogUtil.replaceConfigDir(wksp);
				File f = new File(wksp);
				if (!f.exists() && !f.isAbsolute())
				{
					f = new File(Settings.getInstance().getConfigDir(), wksp);
				}
				if (f.exists())
				{
					result.setWorkspaceFile(f.getAbsolutePath());
				}
			}
			if (!StringUtil.isEmptyString(commit))
			{
				result.setAutocommit(StringUtil.stringToBool(commit));
			}
		}
		catch (Exception e)
		{
			LogMgr.logError("BatchRunner.initFromCommandLine()", "Error creating temporary profile", e);
			result = null;
		}
		return result;
	}
	
	public static BatchRunner createBatchRunner(ArgumentParser cmdLine)
	{
		String scripts = cmdLine.getValue(WbManager.ARG_SCRIPT);
		if (scripts == null || scripts.trim().length() == 0) return null;

		String profilename = cmdLine.getValue(WbManager.ARG_PROFILE);
		
		boolean abort = cmdLine.getBoolean(WbManager.ARG_ABORT, true);
		boolean showResult = cmdLine.getBoolean(WbManager.ARG_DISPLAY_RESULT);
		boolean showProgress = cmdLine.getBoolean(WbManager.ARG_SHOWPROGRESS, false);
		
		String encoding = cmdLine.getValue(WbManager.ARG_SCRIPT_ENCODING);
		
		ConnectionProfile profile = null;
		if (profilename == null)
		{
			profile = createCmdLineProfile(cmdLine);
		}
		else
		{
			String group = cmdLine.getValue(WbManager.ARG_PROFILE_GROUP);
			ProfileKey def = new ProfileKey(StringUtil.trimQuotes(profilename), StringUtil.trimQuotes(group));
			
			profile = ConnectionMgr.getInstance().getProfile(def);
			if (profile == null)
			{
				String msg = "Profile [" + def + "] not found!";
				System.err.println(msg);
				LogMgr.logError("BatchRunner.initFromCommandLine", msg, null);
				return null;
			}
		}


		if (profile != null)
		{
			boolean ignoreDrop = cmdLine.getBoolean(WbManager.ARG_IGNORE_DROP, true);
			profile.setIgnoreDropErrors(ignoreDrop);
		}
		
		String success = cmdLine.getValue(WbManager.ARG_SUCCESS_SCRIPT);
		String error = cmdLine.getValue(WbManager.ARG_ERROR_SCRIPT);
		String feed = cmdLine.getValue(WbManager.ARG_FEEDBACK);
		boolean feedback = cmdLine.getBoolean(WbManager.ARG_FEEDBACK, true);

		BatchRunner runner = new BatchRunner(scripts);
		runner.showResultSets(showResult);
		try
		{
			runner.setEncoding(encoding);
		}
		catch (Exception e)
		{
			LogMgr.logError("BatchRunner.createBatchRunner()", "Invalid encoding '" + encoding + "' specified. Using platform default'", null);
		}
		
		runner.setAbortOnError(abort);
		runner.setErrorScript(error);
		runner.setSuccessScript(success);
		runner.setProfile(profile);
		runner.setVerboseLogging(feedback);
		runner.quiet = cmdLine.isArgPresent(WbManager.ARG_QUIET);
		
		// if no showTiming argument was provided but feedback was disabled
		// disable the display of the timing information as well.
		String tim = cmdLine.getValue(WbManager.ARG_SHOW_TIMING);
		if (tim == null && feed != null && !feedback)  
		{
			runner.showTiming = false;
		}
		else
		{
			runner.showTiming = cmdLine.getBoolean(WbManager.ARG_SHOW_TIMING, true);
		}
		runner.showProgress = showProgress;
		if (showProgress)
		{
			runner.setRowMonitor(new GenericRowMonitor(new ConsoleStatusBar()));
		}
		
		DelimiterDefinition delim = DelimiterDefinition.parseCmdLineArgument(cmdLine.getValue(WbManager.ARG_DELIMITER));

		if (delim != null)
		{
			runner.setDelimiter(delim);
		}
		
		return runner;
	}
}