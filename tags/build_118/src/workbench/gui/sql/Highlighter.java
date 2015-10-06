/*
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2015 Thomas Kellerer.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * To contact the author please send an email to: support@sql-workbench.net
 */
package workbench.gui.sql;

import workbench.resource.GuiSettings;

import workbench.gui.WbSwingUtilities;

import workbench.sql.ErrorDescriptor;
import workbench.sql.parser.ScriptParser;

/**
 *
 * @author Thomas Kellerer
 */
public class Highlighter
{
  private EditorPanel editor;

  public Highlighter(EditorPanel editorPanel)
  {
    this.editor = editorPanel;
  }

	public void highlightStatement(ScriptParser scriptParser, int command, int startOffset)
	{
		if (this.editor == null) return;
		final int startPos = scriptParser.getStartPosForCommand(command) + startOffset;
		final int endPos = scriptParser.getEndPosForCommand(command) + startOffset;
		final int line = this.editor.getLineOfOffset(startPos);

		WbSwingUtilities.invoke(new Runnable()
		{
			@Override
			public void run()
			{
				editor.scrollTo(line, 0);
				editor.selectStatementTemporary(startPos, endPos);
			}
		});
	}

	public void markError(final boolean doHighlight, ScriptParser scriptParser, int commandWithError, int startOffset, ErrorDescriptor error)
	{
		if (editor == null) return;
		if (scriptParser == null) return;
		if (!doHighlight && !GuiSettings.jumpToError()) return;

		final int startPos;
		final int endPos;
		final int line;
		final int newCaret;

		if (error != null && error.getErrorPosition() > -1)
		{
			int startOfCommand = scriptParser.getStartPosForCommand(commandWithError) + startOffset;
			line = editor.getLineOfOffset(startOfCommand + error.getErrorPosition());
			startPos = editor.getLineStartOffset(line);
			endPos = editor.getLineEndOffset(line) - 1;
			if (error.getErrorColumn() > -1)
			{
				newCaret = startPos + error.getErrorColumn();
			}
			else
			{
				newCaret = startOfCommand + error.getErrorPosition();
			}
		}
		else
		{
			startPos = scriptParser.getStartPosForCommand(commandWithError) + startOffset;
			endPos = scriptParser.getEndPosForCommand(commandWithError) + startOffset;
			line = editor.getLineOfOffset(startPos);
      newCaret = editor.getLineStartOffset(line);
		}

		WbSwingUtilities.invoke(new Runnable()
		{
			@Override
			public void run()
			{
        editor.scrollTo(line, 0);
  			editor.setCaretPosition(newCaret);

				if (doHighlight)
				{
					editor.selectError(startPos, endPos);
				}
			}
		});
	}

}