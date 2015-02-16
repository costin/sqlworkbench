/*
 * WbSysExec.java
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

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import workbench.log.LogMgr;

import workbench.sql.SqlCommand;
import workbench.sql.StatementRunnerResult;

import workbench.util.*;

/**
 * A workbench command to call an operating system program (or command)
 *
 * @author Thomas Kellerer
 */
public class WbSysExec
	extends SqlCommand
{
	public static final String VERB = "WBSYSEXEC";
	public static final String ARG_PROGRAM = "program";
	public static final String ARG_PRG_ARG = "argument";
	public static final String ARG_WORKING_DIR = "dir";
	public static final String ARG_DOCUMENT = "document";

	private Process task;

	public WbSysExec()
	{
		super();
		cmdLine = new ArgumentParser();
		cmdLine.addArgument(ARG_PROGRAM);
		cmdLine.addArgument(ARG_WORKING_DIR);
		cmdLine.addArgument(ARG_DOCUMENT);
		cmdLine.addArgument(ARG_PRG_ARG, ArgumentType.Repeatable);
		ConditionCheck.addParameters(cmdLine);
	}

	@Override
	public void cancel()
		throws SQLException
	{
		this.isCancelled = true;
		if (this.task != null)
		{
			task.destroy();
		}
	}

	@Override
	public StatementRunnerResult execute(String sql)
		throws SQLException, Exception
	{
		StatementRunnerResult result = new StatementRunnerResult(sql);
		String command = getCommandLine(sql);
		
		if (StringUtil.isBlank(command))
		{
			result.setFailure();
			result.addMessageByKey("ErrExecNoParm");
			return result;
		}

		cmdLine.parse(command);
		if (!ConditionCheck.isCommandLineOK(result, cmdLine))
		{
			return result;
		}

		BufferedReader stdIn = null;
		BufferedReader stdError = null;
		try
		{
			String prg = cmdLine.getValue(ARG_PROGRAM);
			String doc = cmdLine.getValue(ARG_DOCUMENT);

			ConditionCheck.Result check = ConditionCheck.checkConditions(cmdLine);
			if (!check.isOK())
			{
				result.addMessage(ConditionCheck.getMessage("ErrExec", check));
				result.setSuccess();
				return result;
			}

			if (StringUtil.isNonBlank(prg))
			{
				List<String> args = new ArrayList<String>();
				if (prg.startsWith("."))
				{
					WbFile f = evaluateFileArgument(prg);
					prg = f.getFullPath();
				}
				args.add(prg);
				List<String> params = cmdLine.getList(ARG_PRG_ARG);
				args.addAll(params);
				ProcessBuilder pb = new ProcessBuilder(args);
				String dir = cmdLine.getValue(ARG_WORKING_DIR);
				if (StringUtil.isNonBlank(dir))
				{
					File f = evaluateFileArgument(dir);
					pb.directory(f);
				}
				this.task = pb.start();
			}
			else if (StringUtil.isNonBlank(doc) && Desktop.isDesktopSupported())
			{
				try
				{
					Desktop.getDesktop().open(new File(doc));
					result.setSuccess();
				}
				catch (IOException io)
				{
					result.setFailure();
					result.addMessage(io.getLocalizedMessage());
				}
				return result;
			}
			else
			{
				File cwd = new File(getBaseDir());
				this.task = Runtime.getRuntime().exec(command, null, cwd);
			}

			stdIn = new BufferedReader(new InputStreamReader(task.getInputStream()));
			stdError = new BufferedReader(new InputStreamReader(task.getErrorStream()));

			String out = stdIn.readLine();
			while (out != null)
			{
				result.addMessage(out);
				out = stdIn.readLine();
			}

			out = stdError.readLine();
			if (out != null)
			{
				result.setFailure();
			}
			while (out != null)
			{
				result.addMessage(out);
				out = stdError.readLine();
			}
			task.waitFor();
			int exitValue = task.exitValue();
			if (exitValue != 0)
			{
				result.addMessage("Exit code: " + exitValue);
			}
		}
		catch (Exception e)
		{
			LogMgr.logError("WbExec.execute()", "Error calling external program", e);
			result.addMessage(ExceptionUtil.getDisplay(e));
			result.setFailure();
		}
		finally
		{
			FileUtil.closeQuietely(stdIn);
			FileUtil.closeQuietely(stdError);
			this.task = null;
		}
		return result;
	}


	@Override
	protected boolean isConnectionRequired()
	{
		return false;
	}


	@Override
	public String getVerb()
	{
		return VERB;
	}

}