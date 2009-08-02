/*
 * WbSchemaReport.java
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

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import workbench.db.ConnectionProfile;
import workbench.db.TableIdentifier;
import workbench.db.report.SchemaReporter;
import workbench.interfaces.ScriptGenerationMonitor;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.sql.SqlCommand;
import workbench.sql.StatementRunnerResult;
import workbench.storage.RowActionMonitor;
import workbench.util.ArgumentParser;
import workbench.util.ArgumentType;
import workbench.util.StringUtil;
import workbench.util.WbFile;
import workbench.util.XsltTransformer;

/**
 *
 * @author  support@sql-workbench.net
 */
public class WbSchemaReport
	extends SqlCommand
	implements RowActionMonitor
{
	public static final String PARAM_INCLUDE_TABLES = "includeTables";
	public static final String PARAM_INCLUDE_PROCS = "includeProcedures";
	public static final String PARAM_INCLUDE_GRANTS = "includeTableGrants";
	public static final String PARAM_INCLUDE_SEQUENCES = "includeSequences";
	public static final String PARAM_INCLUDE_VIEWS = "includeViews";
	public static final String PARAM_TYPES = "types";

	public static final String VERB = "WBREPORT";
	private SchemaReporter reporter;
	private int currentTable = 0;

	public WbSchemaReport()
	{
		super();
		cmdLine = new ArgumentParser();
		cmdLine.addArgument("types");
		cmdLine.addArgument("file");
		cmdLine.addArgument("tables", ArgumentType.TableArgument);
		cmdLine.addArgument("schemas");
		cmdLine.addArgument("reportTitle");
		cmdLine.addArgument("useSchemaName", ArgumentType.BoolArgument);
		cmdLine.addArgument(PARAM_TYPES);
		cmdLine.addArgument(PARAM_INCLUDE_VIEWS, ArgumentType.BoolArgument);
		cmdLine.addArgument(PARAM_INCLUDE_PROCS, ArgumentType.BoolArgument);
		cmdLine.addArgument(PARAM_INCLUDE_TABLES, ArgumentType.BoolArgument);
		cmdLine.addArgument(PARAM_INCLUDE_GRANTS, ArgumentType.BoolArgument);
		cmdLine.addArgument(PARAM_INCLUDE_SEQUENCES, ArgumentType.BoolArgument);
		cmdLine.addArgument(WbXslt.ARG_STYLESHEET);
		cmdLine.addArgument(WbXslt.ARG_OUTPUT);
	}

	public String getVerb() { return VERB; }

	public StatementRunnerResult execute(final String sql)
		throws SQLException
	{
		StatementRunnerResult result = new StatementRunnerResult();

		cmdLine.parse(getCommandLine(sql));

		if (cmdLine.hasUnknownArguments())
		{
			setUnknownMessage(result, cmdLine, ResourceMgr.getString("ErrSchemaReportWrongParameters"));
			return result;
		}

		WbFile output = evaluateFileArgument(cmdLine.getValue("file"));

		if (output == null)
		{
			result.addMessage(ResourceMgr.getString("ErrSchemaReportWrongParameters"));
			result.setFailure();
			return result;
		}


		this.reporter = new SchemaReporter(currentConnection);
		String title = cmdLine.getValue("reportTitle");
		this.reporter.setReportTitle(title);

		List<String> types = cmdLine.getListValue(PARAM_TYPES);
		reporter.setObjectTypes(types);

		this.reporter.setIncludeViews(cmdLine.getBoolean(PARAM_INCLUDE_VIEWS, true));
		SourceTableArgument tableArg = new SourceTableArgument(this.cmdLine.getValue("tables"), this.currentConnection);

		List<TableIdentifier> tables = tableArg.getTables();
		if (tables != null && tables.size() > 0)
		{
			// The SchemaReporter needs fully initialized TableIdentifiers
			List<TableIdentifier> dbTables = new ArrayList<TableIdentifier>(tables.size());
			for (TableIdentifier tbl : tables)
			{
				TableIdentifier table = currentConnection.getMetadata().findSelectableObject(tbl);
				if (table != null)
				{
					dbTables.add(table);
				}
			}
			this.reporter.setTableList(dbTables);
		}
		else
		{
			String arg = cmdLine.getValue("schemas");
			List<String> schemas = StringUtil.stringToList(arg, ",");
			this.reporter.setSchemas(schemas);
		}

		String alternateSchema = cmdLine.getValue("useschemaname");
		this.reporter.setSchemaNameToUse(alternateSchema);

		this.reporter.setProgressMonitor(this);

		if (this.rowMonitor != null)
		{
			this.rowMonitor.setMonitorType(RowActionMonitor.MONITOR_PROCESS);
		}

		this.reporter.setIncludeTables(cmdLine.getBoolean(PARAM_INCLUDE_TABLES, true));
		this.reporter.setIncludeProcedures(cmdLine.getBoolean(PARAM_INCLUDE_PROCS, false));
		this.reporter.setIncludeGrants(cmdLine.getBoolean(PARAM_INCLUDE_GRANTS, false));
		this.reporter.setIncludeSequences(cmdLine.getBoolean(PARAM_INCLUDE_SEQUENCES, false));

		if (currentConnection != null && currentConnection.getMetadata().isOracle())
		{
			// check if remarksReporting is turned on for Oracle, if not issue a warning.
			ConnectionProfile prof = currentConnection.getProfile();
			Properties props = prof.getConnectionProperties();
			String value = "false";
			if (props != null)
			{
				value = props.getProperty("remarksReporting", "false");
			}
			if (!"true".equals(value))
			{
				result.addMessage(ResourceMgr.getString("MsgSchemaReporterOracleRemarksWarning"));
				result.addMessage("");
			}
		}

		// currentTable will be incremented as we have registered
		// this object as the RowActionMonitor of the SchemaReporter
		// see setCurrentObject()
		this.currentTable = 0;
		this.reporter.setOutputFilename(output.getFullPath());

		try
		{
			this.reporter.writeXml();
		}
		catch (IOException e)
		{
			result.setFailure();
			result.addMessage(e.getMessage());
		}

		String xslt = cmdLine.getValue(WbXslt.ARG_STYLESHEET);
		String xsltOutput = cmdLine.getValue(WbXslt.ARG_OUTPUT);

		if (result.isSuccess())
		{
			String msg = ResourceMgr.getFormattedString("MsgSchemaReportTablesWritten", currentTable, output.getFullPath());
			result.addMessage(msg);
			result.setSuccess();
		}

		if (!StringUtil.isEmptyString(xslt) && !StringUtil.isEmptyString(xsltOutput))
		{
			try
			{
				XsltTransformer transformer = new XsltTransformer();
				transformer.setXsltBaseDir(new File(runner.getBaseDir()));
				transformer.transform(output.getFullPath(), xsltOutput, xslt);
				result.addMessage(ResourceMgr.getFormattedString("MsgXsltSuccessful", xsltOutput));
				result.setSuccess();
			}
			catch (Exception e)
			{
				LogMgr.logError("WbSchemaReport.execute()", "Error when transforming '" + output.getFullPath() + "' to '" + xsltOutput + "' using " + xslt, e);
				result.addMessage(e.getMessage());
			}
		}


		return result;
	}

	public void cancel()
		throws SQLException
	{
		if (this.reporter != null)
		{
			this.reporter.cancelExecution();
		}
	}

	public void setCurrentObject(String anObject, long number, long total)
	{
		if (anObject == null)
		{
			this.currentTable = 0;
			return;
		}
		else
		{
			this.currentTable ++;
			if (this.rowMonitor != null)
			{
				if (number > 0)
				{
					this.rowMonitor.setCurrentObject(anObject, number, total);
				}
				else if (rowMonitor instanceof ScriptGenerationMonitor)
				{
					((ScriptGenerationMonitor)this.rowMonitor).setCurrentObject(anObject);
				}

			}
		}
	}

	public void setCurrentRow(long number, long total) {}
	public int getMonitorType() { return RowActionMonitor.MONITOR_PLAIN; }
	public void setMonitorType(int aType) {}
	public void jobFinished() {}
	public void saveCurrentType(String type) {}
	public void restoreType(String type) {}

}
