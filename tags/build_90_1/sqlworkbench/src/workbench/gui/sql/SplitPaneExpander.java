/*
 * SplitPaneExpander.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2006, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.sql;

import javax.swing.JSplitPane;

/**
 * @author support@sql-workbench.net
 */
public class SplitPaneExpander
{
	private JSplitPane contentPanel;
	private int lastDivider = -1;
	private boolean upperPartExpanded = false;
	private boolean lowerPartExpanded = false;
	
	
	/** Creates a new instance of SplitPaneExpander */
	public SplitPaneExpander(JSplitPane client)
	{
		this.contentPanel = client;
	}
	
	public void undoExpand()
	{
		if (lastDivider != -1)
		{
			this.contentPanel.setDividerLocation(this.lastDivider);
		}
		else
		{
			int newLoc = (int)(this.contentPanel.getHeight() / 2);
			this.contentPanel.setDividerLocation(newLoc);
		}
		this.lastDivider = -1;
	}

	public void toggleUpperComponentExpand()
	{
		if (upperPartExpanded)
		{
			undoExpand();
			upperPartExpanded = false;
		}
		else
		{
			if (!lowerPartExpanded)
			{
				lastDivider = this.contentPanel.getDividerLocation();
			}
			this.contentPanel.setDividerLocation(this.contentPanel.getHeight());
			upperPartExpanded = true;
		}
		this.lowerPartExpanded = false;
	}

	public void toggleLowerComponentExpand()
	{
		if (this.lowerPartExpanded)
		{
			undoExpand();
			lowerPartExpanded = false;
		}
		else
		{
			if (!upperPartExpanded)
			{
				lastDivider = this.contentPanel.getDividerLocation();
			}
			this.contentPanel.setDividerLocation(0);
			this.lowerPartExpanded = true;
		}
		this.upperPartExpanded = false;
	}

}
