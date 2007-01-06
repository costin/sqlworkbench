/*
 * SqlTabPopup.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2007, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.menu;

import javax.swing.JPopupMenu;
import workbench.gui.MainWindow;
import workbench.gui.actions.AddTabAction;
import workbench.gui.actions.FileDiscardAction;
import workbench.gui.actions.FileOpenAction;
import workbench.gui.actions.NewDbExplorerPanelAction;
import workbench.gui.actions.RemoveTabAction;
import workbench.gui.actions.RenameTabAction;
import workbench.gui.sql.EditorPanel;
import workbench.gui.sql.SqlPanel;
import workbench.interfaces.MainPanel;
import workbench.gui.actions.FileReloadAction;
import workbench.gui.actions.FileSaveAction;
import workbench.gui.actions.InsertTabAction;
import workbench.gui.actions.MoveSqlTabLeft;
import workbench.gui.actions.MoveSqlTabRight;

/**
 * @author  support@sql-workbench.net
 */
public class SqlTabPopup 
	extends JPopupMenu
{
	private AddTabAction add;
	private NewDbExplorerPanelAction newDbExp;
	private RemoveTabAction remove;
	private RenameTabAction rename;
	private MoveSqlTabLeft moveLeft;
	private MoveSqlTabRight moveRight;
	private InsertTabAction insert;
	
	public SqlTabPopup(MainWindow aClient)
	{
		this.add = new AddTabAction(aClient);
		this.add(add.getMenuItem());
		this.insert = new InsertTabAction(aClient);
		this.add(insert.getMenuItem());
		
		this.remove = new RemoveTabAction(aClient);
		this.add(remove.getMenuItem());

		if (aClient.canRenameTab())
		{
			this.rename = new RenameTabAction(aClient);
			this.add(rename.getMenuItem());
		}

		this.addSeparator();
		
		this.newDbExp = new NewDbExplorerPanelAction(aClient, "MnuTxtAddExplorerPanel");
		this.newDbExp.setIcon(null);
		this.add(newDbExp);
		
		MainPanel panel = aClient.getCurrentPanel();
		if (panel instanceof SqlPanel)
		{
			this.addSeparator();
			
			SqlPanel spanel = (SqlPanel)panel;
			int currentIndex = aClient.getCurrentPanelIndex();
			moveLeft = new MoveSqlTabLeft(aClient);
			moveLeft.setEnabled(currentIndex > 0);
			this.add(moveLeft.getMenuItem());
			int lastIndex = aClient.getLastSqlPanelIndex();
			moveRight = new MoveSqlTabRight(aClient);
			moveRight.setEnabled(currentIndex < lastIndex);
			this.add(moveRight);
			
			this.addSeparator();

			EditorPanel editor = spanel.getEditor();
			
			this.add(editor.getFileSaveAction().getMenuItem());
			this.add(editor.getFileOpenAction().getMenuItem());
			this.add(editor.getReloadAction().getMenuItem());
			this.addSeparator();
			FileDiscardAction discard = new FileDiscardAction(spanel);
			discard.removeIcon();
			this.add(discard.getMenuItem());
			this.remove.setEnabled(aClient.canCloseTab());
		}
	}

}
