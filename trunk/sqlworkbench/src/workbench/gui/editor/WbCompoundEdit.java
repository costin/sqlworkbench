/*
 * WbCompoundEdit.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2014, Thomas Kellerer
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
package workbench.gui.editor;

import java.util.ArrayList;
import java.util.List;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

/**
 *
 * @author Thomas Kellerer
 */
public class WbCompoundEdit
	implements UndoableEdit
{
	private List<UndoableEdit> edits = new ArrayList<UndoableEdit>();
	private boolean acceptNew = true;

	public int getSize()
	{
		return edits.size();
	}

	public void clear()
	{
		this.edits.clear();
	}

	public void finished()
	{
		acceptNew = false;
	}

	public UndoableEdit getLast()
	{
		if (edits.size() == 0) return null;
		return edits.get(edits.size() - 1);
	}

	@Override
	public void undo()
		throws CannotUndoException
	{
		if (edits.size() == 0) return;
		for (int i=edits.size() - 1; i > -1; i--)
		{
			UndoableEdit edit = edits.get(i);
			if (edit.canUndo() && edit.isSignificant()) edit.undo();
		}
	}

	@Override
	public boolean canUndo()
	{
		if (edits.size() == 0) return false;
		for (int i=0; i < edits.size(); i++)
		{
			UndoableEdit edit = edits.get(i);
			if (!edit.canUndo()) return false;
		}
		return true;
	}

	@Override
	public void redo()
		throws CannotRedoException
	{
		if (edits.size() == 0) return;

		for (int i=0; i < edits.size(); i++)
		{
			UndoableEdit edit = edits.get(i);
			edit.redo();
		}
	}

	@Override
	public boolean canRedo()
	{
		if (edits.size() == 0) return false;
		for (UndoableEdit edit : edits)
		{
			if (!edit.canRedo()) return false;
		}
		return true;
	}

	@Override
	public void die()
	{
		for (UndoableEdit edit : edits)
		{
			edit.die();
		}
	}

	@Override
	public boolean addEdit(UndoableEdit anEdit)
	{
		if (!acceptNew) return false;
		return edits.add(anEdit);
	}

	@Override
	public boolean replaceEdit(UndoableEdit anEdit)
	{
		return false;
	}

	@Override
	public boolean isSignificant()
	{
		for (UndoableEdit edit : edits)
		{
			if (edit.isSignificant()) return true;
		}
		return false;
	}

	@Override
	public String getPresentationName()
	{
		UndoableEdit edit = getLast();
		if (edit == null) return "";
		return edit.getPresentationName();
	}

	@Override
	public String getUndoPresentationName()
	{
		UndoableEdit edit = getLast();
		if (edit == null) return "";
		return edit.getUndoPresentationName();
	}

	@Override
	public String getRedoPresentationName()
	{
		UndoableEdit edit = getLast();
		if (edit == null) return "";
		return edit.getRedoPresentationName();
	}

}
