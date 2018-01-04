/*
 * DeleteWord.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2018, Thomas Kellerer
 *
 * Licensed under a modified Apache License, Version 2.0
 * that restricts the use for certain governments.
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at.
 *
 *     http://sql-workbench.net/manual/license.html
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
package workbench.gui.editor.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.text.BadLocationException;

import workbench.gui.editor.JEditTextArea;
import workbench.gui.editor.TextUtilities;

/**
 *
 * @author Thomas Kellerer
 */
public class DeleteWord
	extends EditorAction
{
	public DeleteWord()
	{
		super("TxtEdDelWord", KeyEvent.VK_DELETE, KeyEvent.CTRL_MASK);
	}

	@Override
	public void actionPerformed(ActionEvent evt)
	{
    JEditTextArea textArea = getTextArea(evt);
		int start = textArea.getSelectionStart();
		if (start != textArea.getSelectionEnd())
		{
			textArea.setSelectedText("");
		}

		int line = textArea.getCaretLine();
		int lineStart = textArea.getLineStartOffset(line);
		int caret = start - lineStart;

		String lineText = textArea.getLineText(textArea.getCaretLine());

		if (caret == lineText.length())
		{
			if (lineStart + caret == textArea.getDocumentLength())
			{
				textArea.getToolkit().beep();
				return;
			}
			caret++;
		}
		else
		{
			caret = TextUtilities.findWordEnd(lineText, caret);
		}

		try
		{
			textArea.getDocument().remove(start, (caret + lineStart) - start);
		}
		catch (BadLocationException bl)
		{
			bl.printStackTrace();
		}
	}
}
