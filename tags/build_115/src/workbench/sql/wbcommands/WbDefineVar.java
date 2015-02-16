/*
 * WbDefineVar.java
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
package workbench.sql.wbcommands;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;

import workbench.db.WbConnection;

import workbench.sql.SqlCommand;
import workbench.sql.StatementRunnerResult;
import workbench.sql.VariablePool;

import workbench.util.*;

/**
 * SQL Command to define a variable that gets stored in the system
 * wide parameter pool.
 *
 * @see workbench.sql.VariablePool
 *
 * @author  Thomas Kellerer
 */
public class WbDefineVar
	extends SqlCommand
{
	public static final String VERB = "WBVARDEF";

	public WbDefineVar()
	{
		super();
		this.cmdLine = new ArgumentParser();
		this.cmdLine.addArgument("file", ArgumentType.StringArgument);
		this.cmdLine.addArgument("contentFile", ArgumentType.StringArgument);
		this.cmdLine.addArgument("variable", ArgumentType.StringArgument);
		this.cmdLine.addArgument("replaceVars", ArgumentType.BoolArgument);
		this.cmdLine.addArgument("removeUndefined", ArgumentType.BoolSwitch);

		CommonArgs.addEncodingParameter(cmdLine);
	}

	@Override
	public String getVerb()
	{
		return VERB;
	}

	@Override
	protected boolean isConnectionRequired()
	{
		return false;
	}

	@Override
	public StatementRunnerResult execute(String aSql)
		throws SQLException
	{
		StatementRunnerResult result = new StatementRunnerResult();
		String sql = getCommandLine(aSql);

		cmdLine.parse(sql);
		WbFile file = this.evaluateFileArgument(cmdLine.getValue("file"));
		WbFile contentFile = this.evaluateFileArgument(cmdLine.getValue("contentFile"));

		boolean removeUndefined = cmdLine.getBoolean("removeUndefined");
		String varDef = cmdLine.getNonArguments();

		if (file != null && contentFile != null)
		{
			result.addMessageByKey("ErrVarFileWrong");
			result.setFailure();
			return result;
		}

		if (file != null)
		{
			// if the file argument has been supplied, no variable definition
			// can be present, but the encoding parameter might have been passed
			String encoding = cmdLine.getValue("encoding");
			try
			{
				if (file.exists())
				{
					VariablePool.getInstance().readFromFile(file.getFullPath(), encoding);
					String msg = ResourceMgr.getString("MsgVarDefFileLoaded");
					msg = StringUtil.replace(msg, "%file%", file.getFullPath());
					result.addMessage(msg);
					result.setSuccess();
				}
				else
				{
					String msg = ResourceMgr.getFormattedString("ErrFileNotFound", file.getFullPath());
					result.addMessage(msg);
					result.setFailure();
				}
			}
			catch (Exception e)
			{
				LogMgr.logError("WbDefineVar.execute()", "Error reading definition file: " + file.getFullPath(), e);
				String msg = ResourceMgr.getString("ErrReadingVarDefFile");
				msg = StringUtil.replace(msg, "%file%", file.getAbsolutePath());
				msg = msg + " " + ExceptionUtil.getDisplay(e);
				result.addMessage(msg);
				result.setFailure();
			}
		}
		else if (contentFile != null)
		{
			readFileContents(result, contentFile);
		}
		else
		{
			WbStringTokenizer tok = new WbStringTokenizer("=", true, "\"'", false);
			tok.setSourceString(varDef);
			tok.setKeepQuotes(true);
			String value = null;
			String var = null;

			if (tok.hasMoreTokens()) var = tok.nextToken();

			if (var == null)
			{
				result.addMessageByKey("ErrVarDefWrongParameter");
				result.setFailure();
				return result;
			}

			var = var.trim();

			if (tok.hasMoreTokens()) value = tok.nextToken();

			result.setSuccess();

			if (value != null)
			{
				if (value.trim().startsWith("@") || StringUtil.trimQuotes(value).startsWith("@"))
				{
					String valueSql = null;
					try
					{
						// In case the @ sign was placed inside the quotes, make sure
						// there are no quotes before removing the @ sign
						value = StringUtil.trimQuotes(value);
						valueSql = StringUtil.trimQuotes(value.trim().substring(1));
						value = this.evaluateSql(currentConnection, valueSql, result);
					}
					catch (Exception e)
					{
						LogMgr.logError("WbDefineVar.execute()", "Error retrieving variable value using SQL: " + valueSql, e);
						String err = ResourceMgr.getString("ErrReadingVarSql");
						err = StringUtil.replace(err, "%sql%", valueSql);
						err = err + "\n\n" + ExceptionUtil.getDisplay(e);
						result.addMessage(err);
						result.setFailure();
						return result;
					}
				}
				else
				{
					// WbStringTokenizer returned any quotes that were used, so
					// we have to remove them again as they should not be part of the variable value
					value = StringUtil.trimQuotes(value.trim());
					if (removeUndefined)
					{
						// as the SQL that was passed to this command already has all variables replaced,
						// we can simply remove anything that looks like a variable in the value.
						value = VariablePool.getInstance().removeVariables(value);
					}
				}
				setVariable(result, var, value);
				if (result.isSuccess())
				{
					String msg = ResourceMgr.getString("MsgVarDefVariableDefined");
					msg = StringUtil.replace(msg, "%var%", var);
					msg = StringUtil.replace(msg, "%value%", value);
					msg = StringUtil.replace(msg, "%varname%", VariablePool.getInstance().buildVarName(var, false));
					result.addMessage(msg);
				}
			}
			else
			{
				VariablePool.getInstance().removeValue(var);
				String removed = ResourceMgr.getString("MsgVarDefVariableRemoved");
				removed = removed.replace("%var%", var);
				result.addMessage(removed);
			}
		}

		return result;
	}

	private void setVariable(StatementRunnerResult result, String var, String value)
	{
		try
		{
			VariablePool.getInstance().setParameterValue(var, value);
		}
		catch (IllegalArgumentException e)
		{
			result.addMessageByKey("ErrVarDefWrongName");
			result.setFailure();
		}
	}

	/**
	 *	Return the result of the given SQL string and return
	 *	the value of the first column of the first row
	 *	as a string value.
	 *
	 *	If the SQL gives an error, an empty string will be returned
	 */
	private String evaluateSql(WbConnection conn, String sql, StatementRunnerResult stmtResult)
		throws SQLException
	{
		ResultSet rs = null;
		String result = StringUtil.EMPTY_STRING;
		if (conn == null)
		{
			throw new SQLException("Cannot evaluate SQL based variable without a connection");
		}

		try
		{
			this.currentStatement = conn.createStatement();

			if (sql.endsWith(";"))
			{
				sql = sql.substring(0, sql.length() - 1);
			}
			rs = this.currentStatement.executeQuery(sql);
			ResultSetMetaData meta = rs.getMetaData();
			int colCount = meta.getColumnCount();
			if (rs.next())
			{
				Object value = rs.getObject(1);
				if (value != null)
				{
					result = value.toString();
				}
			}

			if (rs.next())
			{
				stmtResult.setWarning(true);
				stmtResult.addMessageByKey("ErrVarDefRows");
			}

			if (colCount > 1)
			{
				stmtResult.setWarning(true);
				stmtResult.addMessageByKey("ErrVarDefCols");
			}

			if (stmtResult.hasWarning())
			{
				stmtResult.addMessageNewLine();
			}
		}
		finally
		{
			SqlUtil.closeResult(rs);
		}

		return result;
	}

	private void readFileContents(StatementRunnerResult result, WbFile contentFile)
	{
		String varname = cmdLine.getValue("variable");
		if (StringUtil.isBlank(varname))
		{
			result.addMessageByKey("ErrVarNoName");
			result.setFailure();
			return;
		}

		boolean replace = cmdLine.getBoolean("replaceVars", true);
		String encoding = cmdLine.getValue("encoding");
		if (encoding == null)
		{
			encoding = EncodingUtil.getDefaultEncoding();
		}

		try
		{
			String value = FileUtil.readFile(contentFile, encoding);
			if (replace)
			{
				value = VariablePool.getInstance().replaceAllParameters(value);
			}

			setVariable(result, varname, value);
			String msg = ResourceMgr.getFormattedString("MsgVarReadFile", varname, contentFile.getFullPath());
			result.addMessage(msg);
		}
		catch (FileNotFoundException fnf)
		{
			LogMgr.logError("WbDefineVar.execute()", "Content file " + contentFile.getFullPath() + " not found!", fnf);
			result.addMessage(ResourceMgr.getFormattedString("ErrFileNotFound", contentFile.getFullPath()));
			result.setFailure();
		}
		catch (IOException io)
		{
			result.addMessage(ExceptionUtil.getDisplay(io));
			result.setFailure();
		}
	}

}