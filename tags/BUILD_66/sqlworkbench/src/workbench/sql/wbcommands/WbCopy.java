package workbench.sql.wbcommands;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import workbench.WbManager;
import workbench.db.ColumnIdentifier;
import workbench.db.DataCopier;
import workbench.db.TableIdentifier;

import workbench.db.WbConnection;
import workbench.exception.ExceptionUtil;
import workbench.exception.WbException;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.sql.SqlCommand;
import workbench.sql.StatementRunnerResult;
import workbench.util.ArgumentParser;
import workbench.util.SqlUtil;
import workbench.util.StringUtil;

/**
 * A command to copy data from one DBMS to another. This is the commandline
 * version of the DataPumper.
 * @author  workbench@kellerer.org
 */
public class WbCopy
	extends SqlCommand
{
	public static final String VERB = "COPY";
	public static final String PARAM_SOURCETABLE = "sourcetable";
	public static final String PARAM_SOURCEQUERY = "sourcequery";
	public static final String PARAM_TARGETTABLE = "targettable";
	public static final String PARAM_SOURCEPROFILE = "sourceprofile";
	public static final String PARAM_TARGETPROFILE = "targetprofile";
	public static final String PARAM_COLUMNS = "columns";
	public static final String PARAM_SOURCEWHERE = "sourcewhere";
	public static final String PARAM_COMMITEVERY = "commitevery";
	public static final String PARAM_CONTINUE = "continue";
	public static final String PARAM_DELETETARGET = "deletetarget";
	//public static final String PARAM_DROPTARGET = "droptarget";
	//public static final String PARAM_CREATETARGET = "createtarget";
	
	private ArgumentParser cmdLine;
	private DataCopier copier;

	public WbCopy()
	{
		cmdLine = new ArgumentParser();
		cmdLine.addArgument(PARAM_SOURCETABLE);
		cmdLine.addArgument(PARAM_SOURCEQUERY);
		cmdLine.addArgument(PARAM_TARGETTABLE);
		cmdLine.addArgument(PARAM_SOURCEPROFILE);
		cmdLine.addArgument(PARAM_TARGETPROFILE);
		cmdLine.addArgument(PARAM_COLUMNS);
		cmdLine.addArgument(PARAM_SOURCEWHERE);
		cmdLine.addArgument(PARAM_COMMITEVERY);
		cmdLine.addArgument(PARAM_CONTINUE);
		cmdLine.addArgument(PARAM_DELETETARGET);
		//cmdLine.addArgument(PARAM_DROPTARGET);
		//cmdLine.addArgument(PARAM_CREATETARGET);
	}

	public String getVerb() { return VERB; }

	public StatementRunnerResult execute(WbConnection aConnection, String aSql)
		throws SQLException, WbException
	{
		StatementRunnerResult result = new StatementRunnerResult(aSql);
		aSql = SqlUtil.makeCleanSql(aSql, false, '"');
		int pos = aSql.indexOf(' ');
		if (pos > -1)
			aSql = aSql.substring(pos);
		else
			aSql = "";

		try
		{
			cmdLine.parse(aSql);
		}
		catch (Exception e)
		{
			result.addMessage(ResourceMgr.getString("ErrorImportWrongParameters"));
			result.setFailure();
			return result;
		}

		if (cmdLine.hasUnknownArguments())
		{
			List params = cmdLine.getUnknownArguments();
			StringBuffer msg = new StringBuffer(ResourceMgr.getString("ErrorUnknownParameter"));
			for (int i=0; i < params.size(); i++)
			{
				msg.append((String)params.get(i));
				if (i > 0) msg.append(',');
			}
			result.addMessage(msg.toString());
			result.addMessage("");
			result.addMessage(ResourceMgr.getString("ErrorCopyWrongParameters"));
			result.setFailure();
			return result;
		}


		String sourceProfile = cmdLine.getValue(PARAM_SOURCEPROFILE);
		String targetProfile = cmdLine.getValue(PARAM_TARGETPROFILE);

		if (targetProfile == null && targetProfile == null)
		{
			result.addMessage(ResourceMgr.getString("ErrorCopyWrongParameters"));
			result.setFailure();
			return result;
		}

		int commit = StringUtil.getIntValue(cmdLine.getValue(PARAM_COMMITEVERY),-1);

		String sourcetable = cmdLine.getValue(PARAM_SOURCETABLE);
		String sourcequery = cmdLine.getValue(PARAM_SOURCEQUERY);
		if (sourcetable == null && sourcequery == null)
		{
			result.addMessage(ResourceMgr.getString("ErrorCopyNoSourceSpecified"));
			result.addMessage("");
			result.addMessage(ResourceMgr.getString("ErrorCopyWrongParameters"));
			result.setFailure();
			return result;
		}

		String targettable = cmdLine.getValue(PARAM_TARGETTABLE);
		if (targettable == null)
		{
			result.addMessage(ResourceMgr.getString("ErrorCopyWrongParameters"));
			result.setFailure();
			return result;
		}

		WbConnection targetCon = null;
		WbConnection sourceCon = null;
		if (targetProfile == null)
		{
			targetCon = aConnection;
		}
		else
		{
			try
			{
				targetCon = WbManager.getInstance().getConnectionMgr().getConnection(targetProfile, "Wb-Copy-Target");
			}
			catch (Exception e)
			{
				result.addMessage(ResourceMgr.getString("ErrorCopyCouldNotConnectTarget"));
				result.setFailure();
				return result;
			}
		}

		if (sourceProfile == null)
		{
			sourceCon = aConnection;
		}
		else
		{
			try
			{
				sourceCon = WbManager.getInstance().getConnectionMgr().getConnection(sourceProfile, "Wb-Copy-Source");
			}
			catch (Exception e)
			{
				result.addMessage(ResourceMgr.getString("ErrorCopyCouldNotConnectSource"));
				result.setFailure();
				// disconnect the target connection only if it was created by this command
				if (targetCon.getId().startsWith("Wb-Copy"))
				{
					try { targetCon.disconnect(); } catch (Throwable th) {}
				}
				return result;
			}
		}
		boolean delete = "true".equalsIgnoreCase(cmdLine.getValue(PARAM_DELETETARGET));
		boolean cont = "true".equalsIgnoreCase(cmdLine.getValue(PARAM_CONTINUE));
		//boolean createTable = "true".equals(cmdLine.getValue(PARAM_CREATETARGET));
		//boolean dropTable = "true".equals(cmdLine.getValue(PARAM_DROPTARGET));

		this.copier = new DataCopier();
		copier.setRowActionMonitor(this.rowMonitor);
		copier.setContinueOnError(cont);
		copier.setCommitEvery(commit);

		copier.setDeleteTarget(delete);

		TableIdentifier targetId = new TableIdentifier(targettable);

		try
		{
			if (sourcetable != null)
			{
				TableIdentifier srcTable = new TableIdentifier(sourcetable);
				String where = cmdLine.getValue(PARAM_SOURCEWHERE);
				String columns = cmdLine.getValue(PARAM_COLUMNS);
				boolean containsMapping = (columns.indexOf('/') > -1);
				
				if (containsMapping)
				{
					Map mapping = this.parseMapping();
					copier.copyFromTable(sourceCon, targetCon, srcTable, targetId, mapping, where, false, false);
				}
				else
				{
					ColumnIdentifier[] cols = this.parseColumns();
					copier.copyToNewTable(sourceCon, targetCon, srcTable, targetId, cols, where);
				}
			}
			else
			{
				ColumnIdentifier[] cols = this.parseColumns();
				copier.copyFromQuery(sourceCon, targetCon, sourcequery, targetId, cols);
			}

			copier.start();
			result.setSuccess();
			
			String s = copier.getErrorMessage();
			if (s != null) result.addMessage(s);
			this.addErrorsFromImporter(result);
			result.addMessage(copier.getMessages());
			this.addWarningsFromImporter(result);
		}
		catch (SQLException e)
		{
			LogMgr.logError("WbCopy.execute()", "SQL Error when copying data", e);
			result.addMessage(ResourceMgr.getString("ErrorOnCopy"));
			result.addMessage(ExceptionUtil.getDisplay(e));
			result.setFailure();
		}
		catch (Exception e)
		{
			LogMgr.logError("WbCopy.execute()", "Error when copying data", e);
			result.setFailure();
			result.addMessage(ExceptionUtil.getDisplay(e));
		}
		finally
		{
			try
			{
				if (sourceCon.getId().startsWith("Wb-Copy"))
				{
					sourceCon.disconnect();
				}
			}
			catch (Exception e)
			{
				LogMgr.logError("WbCopy.execute()", "Errro when disconnecting source connection",e);
				result.addMessage(ExceptionUtil.getDisplay(e));
			}

			try
			{
				if (targetCon.getId().startsWith("Wb-Copy"))
				{
					targetCon.disconnect();
				}
			}
			catch (Exception e)
			{
				LogMgr.logError("WbCopy.execute()", "Errro when disconnecting target connection",e);
				result.addMessage(ExceptionUtil.getDisplay(e));
			}
		}

		return result;
	}

	private ColumnIdentifier[] parseColumns()
	{
		String cols = cmdLine.getValue(PARAM_COLUMNS);
		List l = StringUtil.stringToList(cols, ",");
		int count = l.size();
		ColumnIdentifier[] result = new ColumnIdentifier[count];
		for (int i=0; i < count; i++)
		{
			String c = (String)l.get(i);
			result[i] = new ColumnIdentifier(c);
		}
		return result;
	}

	private Map parseMapping()
	{
		String cols = cmdLine.getValue(PARAM_COLUMNS);
		List l = StringUtil.stringToList(cols, ",");
		int count = l.size();
		HashMap mapping = new HashMap(count);
		for (int i=0; i < count; i++)
		{
			String s = (String)l.get(i);
			int pos = s.indexOf("/");
			String scol = s.substring(0, pos).trim();
			String tcol = s.substring(pos + 1).trim();
			mapping.put(scol, tcol);
		}
		return mapping;
	}


	private void addWarningsFromImporter(StatementRunnerResult result)
	{
		String[] err = copier.getImportWarnings();
		for (int i=0; i < err.length; i++)
		{
			result.addMessage(err[i]);
		}
	}

	private void addErrorsFromImporter(StatementRunnerResult result)
	{
		String[] warn = copier.getImportErrors();
		for (int i=0; i < warn.length; i++)
		{
			result.addMessage(warn[i]);
		}
		if (warn.length > 0)
		{
			// force an empty line if we had warnings
			result.addMessage("");
		}
	}

	public void done()
	{
		super.done();
		this.copier = null;;
	}

	public void cancel()
		throws SQLException
	{
		super.cancel();
		if (this.copier != null)
		{
			this.copier.cancel();
		}
	}

}