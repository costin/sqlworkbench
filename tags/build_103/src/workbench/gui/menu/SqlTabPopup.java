/*
 * SqlTabPopup.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2009, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.menu;

import javax.swing.JPopupMenu;
import workbench.gui.MainWindow;
import workbench.gui.actions.AddTabAction;
import workbench.gui.actions.CloseOtherTabsAction;
import workbench.gui.actions.FileDiscardAction;
import workbench.gui.actions.NewDbExplorerPanelAction;
import workbench.gui.actions.RemoveTabAction;
import workbench.gui.actions.RenameTabAction;
import workbench.gui.sql.EditorPanel;
import workbench.gui.sql.SqlPanel;
import workbench.interfaces.MainPanel;
import workbench.gui.actions.InsertTabAction;
import workbench.gui.actions.LockPanelAction;
import workbench.gui.actions.MoveSqlTabLeft;
import workbench.gui.actions.MoveSqlTabRight;
import workbench.gui.actions.ToggleExtraConnection;

/**
 * @author  support@sql-workbench.net
 */
public class SqlTabPopup
	extends JPopupMenu
{
	public SqlTabPopup(MainWindow aClient)
	{
		super();
		AddTabAction add = new AddTabAction(aClient);
		this.add(add);
		InsertTabAction insert = new InsertTabAction(aClient);
		this.add(insert);

		addSeparator();
		
		RemoveTabAction remove = new RemoveTabAction(aClient);
		remove.setEnabled(aClient.canCloseTab());
		this.add(remove);

		MainPanel panel = aClient.getCurrentPanel();

		int tabCount = aClient.getLastSqlPanelIndex();
		int lockedCount = 0;
		for (int i=0; i < tabCount; i++)
		{
			MainPanel p = aClient.getSqlPanel(i);
			if (p.isLocked()) lockedCount ++;
		}

		if (panel instanceof SqlPanel || lockedCount > 0)
		{
			CloseOtherTabsAction closeOthers = new CloseOtherTabsAction(aClient);
			this.add(closeOthers);
		}
		
		if (aClient.canRenameTab())
		{
			RenameTabAction rename = new RenameTabAction(aClient);
			this.add(rename);
		}

		LockPanelAction lock = new LockPanelAction(panel);

		this.add(lock.getMenuItem());
		lock.setSwitchedOn(panel.isLocked());

		if (panel instanceof SqlPanel)
		{
			this.addSeparator();

			SqlPanel spanel = (SqlPanel)panel;
			int currentIndex = aClient.getCurrentPanelIndex();
			MoveSqlTabLeft moveLeft = new MoveSqlTabLeft(aClient);
			moveLeft.setEnabled(currentIndex > 0);
			this.add(moveLeft);
			int lastIndex = aClient.getLastSqlPanelIndex();
			MoveSqlTabRight moveRight = new MoveSqlTabRight(aClient);
			moveRight.setEnabled(currentIndex < lastIndex);
			this.add(moveRight);
		}

		this.addSeparator();

		NewDbExplorerPanelAction newDbExp = new NewDbExplorerPanelAction(aClient, "MnuTxtAddExplorerPanel");
		newDbExp.removeIcon();
		add(newDbExp);

		if (aClient.canUseSeparateConnection())
		{
			this.addSeparator();
			ToggleExtraConnection toggle = new ToggleExtraConnection(aClient);
			this.add(toggle.getMenuItem());
		}

		if (panel instanceof SqlPanel)
		{
			this.addSeparator();
			SqlPanel spanel = (SqlPanel)panel;

			EditorPanel editor = spanel.getEditor();

			this.add(editor.getFileSaveAction());
			this.add(editor.getFileOpenAction());
			this.add(editor.getReloadAction());
			this.addSeparator();
			FileDiscardAction discard = new FileDiscardAction(spanel);
			discard.removeIcon();
			this.add(discard);
			remove.setEnabled(aClient.canCloseTab());
		}
	}

}
