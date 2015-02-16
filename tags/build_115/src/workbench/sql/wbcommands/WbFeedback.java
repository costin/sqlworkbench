/*
 * WbFeedback.java
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

import java.sql.SQLException;
import workbench.resource.ResourceMgr;
import workbench.sql.SqlCommand;
import workbench.sql.StatementRunnerResult;
import workbench.sql.formatter.SQLLexer;
import workbench.sql.formatter.SQLToken;
import workbench.util.ArgumentParser;

/**
 * Control the level of feedback during script execution.
 *
 * @author Thomas Kellerer
 */
public class WbFeedback
	extends SqlCommand
{
	public static final String VERB = "WBFEEDBACK";
	private final String command;

	public WbFeedback()
	{
		this(VERB);
	}

	public WbFeedback(String verb)
	{
		super();
		this.command = verb;
		this.cmdLine = new ArgumentParser(false);
		this.cmdLine.addArgument("on");
		this.cmdLine.addArgument("off");
	}

	@Override
	public String getVerb()
	{
		return command;
	}

	@Override
	protected boolean isConnectionRequired()
	{
		return false;
	}

	@Override
	public StatementRunnerResult execute(String sql)
		throws SQLException
	{
		StatementRunnerResult result = new StatementRunnerResult();
		result.setSuccess();

		SQLLexer lexer = new SQLLexer(sql);
		// Skip the SQL Verb
		SQLToken token = lexer.getNextToken(false, false);

		// get the parameter
		token = lexer.getNextToken(false, false);
		String parm = (token != null ? token.getContents() : null);

		if (parm == null)
		{
			if (runner.getVerboseLogging())
			{
				result.addMessage(ResourceMgr.getString("MsgFeedbackEnabled"));
			}
			else
			{
				result.addMessage(ResourceMgr.getString("MsgFeedbackDisabled"));
			}
			result.setSuccess();
		}
		else if ("off".equalsIgnoreCase(parm) || "false".equalsIgnoreCase(parm))
		{
			this.runner.setVerboseLogging(false);
			result.addMessage(ResourceMgr.getString("MsgFeedbackDisabled"));
		}
		else if ("on".equalsIgnoreCase(parm) || "true".equalsIgnoreCase(parm))
		{
			this.runner.setVerboseLogging(true);
			result.addMessage(ResourceMgr.getString("MsgFeedbackEnabled"));
		}
		else
		{
			result.setFailure();
			result.addMessage(ResourceMgr.getString("ErrFeedbackWrongParameter"));
		}
		return result;
	}

}