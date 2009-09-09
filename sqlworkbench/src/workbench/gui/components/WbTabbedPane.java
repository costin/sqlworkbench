/*
 * WbTabbedPane.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2009, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.components;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.dnd.DragSource;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.Icon;
import javax.swing.JTabbedPane;
import javax.swing.JToolTip;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.TabbedPaneUI;
import workbench.gui.WbSwingUtilities;
import workbench.gui.lnf.LnFHelper;
import workbench.interfaces.Moveable;
import workbench.log.LogMgr;

/**
 * A JTabbedPane that allows re-ordering of the tabs using drag & drop.
 * <br/>
 * Additionally it installs it's own UI to remove the unnecessary borders
 * that the standard Java Look & Feels create.
 * <br/>
 * A close button can be displayed inside each tab that will trigger
 * an event with the registered TabCloser.
 * 
 * @author  support@sql-workbench.net
 */
public class WbTabbedPane
	extends JTabbedPane
	implements MouseListener, MouseMotionListener, ChangeListener
{
	private Moveable tabMover;
	private int draggedTabIndex;
	private TabCloser tabCloser;
	private boolean hideDisabledButtons;
	private boolean jGoodies;
	
	public WbTabbedPane()
	{
		super();
		init();
	}

	public WbTabbedPane(int placement)
	{
		super(placement);
		init();
	}

	public void hideDisabledButtons(boolean flag)
	{
		hideDisabledButtons = flag;
	}
	
	public void setCloseButtonEnabled(Component panel, boolean flag)
	{
		if (tabCloser == null) return;

		int index = indexOfComponent(panel);
		if (index == -1) return;
		setCloseButtonEnabled(index, flag);
	}
	
	public void setCloseButtonEnabled(int index, boolean flag)
	{
		if (tabCloser == null) return;
		
		TabButtonComponent comp = getTabButton(index);
		if (comp != null)
		{
			comp.setEnabled(flag);
		}
	}

	protected TabButtonComponent getTabButton(int index)
	{
		TabButtonComponent comp = (TabButtonComponent)getTabComponentAt(index);
		return comp;
	}
	
	@Override
	public void setDisplayedMnemonicIndexAt(int tabIndex, int mnemonicIndex)
	{
		super.setDisplayedMnemonicIndexAt(tabIndex, mnemonicIndex);
		TabButtonComponent comp = getTabButton(tabIndex);
		if (comp != null)
		{
			comp.setDisplayedMnemonicIndex(mnemonicIndex);
		}
	}

	@Override
	public void setMnemonicAt(int tabIndex, int mnemonic)
	{
		super.setMnemonicAt(tabIndex, mnemonic);
		TabButtonComponent comp = getTabButton(tabIndex);
		if (comp != null)
		{
			comp.setDisplayedMnemonic(mnemonic);
		}
	}

	@Override
	public void setIconAt(int index, Icon icon)
	{
		super.setIconAt(index, icon);
		TabButtonComponent comp = getTabButton(index);
		if (comp != null)
		{
			comp.setIcon(icon);
		}
	}
	
	/**
	 * Enable/Disable the close button.
	 *
	 * @param closer the TabCloser to handle the close event. If null the close button will be hidden
	 */
	public void showCloseButton(TabCloser closer)
	{
		boolean wasAdded = (closer != null && tabCloser == null);
		boolean wasRemoved = (closer == null && tabCloser != null);

		tabCloser = closer;
		if (wasAdded)
		{
			addCloseButtons();
			updateButtons();
			addChangeListener(this);
		}
		else if (wasRemoved)
		{
			removeCloseButtons();
			removeChangeListener(this);
		}
	}

	protected void addCloseButtons()
	{
		if (tabCloser == null) return;

		for (int i=0; i < getTabCount(); i++)
		{
			String title = getTitleAt(i);
			Icon icon = getIconAt(i);
			TabButtonComponent comp = new TabButtonComponent(title, this, true);
			comp.setIcon(icon);
			setTabComponentAt(i, comp);
		}
	}

	protected void removeCloseButtons()
	{
		for (int i=0; i < getTabCount(); i++)
		{
			//setTabComponentAt(i, null);
			TabButtonComponent comp = getTabButton(i);
			if (comp != null)
			{
				comp.setButtonVisible(false);
			}
		}
	}
	
	public void closeButtonClicked(final int index)
	{
		if (tabCloser == null) return;
		if (!tabCloser.canCloseTab(index)) return;

		EventQueue.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				tabCloser.tabCloseButtonClicked(index);
			}
		});
		
	}

	public int getTabHeight()
	{
		Font font = getFont();
		if (font == null)
		{
			return 0;
		}
		FontMetrics metrics = getFontMetrics(font);
		if (metrics == null)
		{
			return 0;
		}
		int fontHeight = metrics.getHeight();
		Insets tabInsets = UIManager.getInsets("TabbedPane.tabInsets");
		if (tabInsets != null)
		{
			fontHeight += tabInsets.top + tabInsets.bottom + 2;
		}
		return fontHeight + 5;
	}

	public JToolTip createToolTip()
	{
		JToolTip tip = new MultiLineToolTip();
		tip.setComponent(this);
		return tip;
	}

	private void init()
	{
		// For use with the jGoodies Plastic look & feel
		putClientProperty("jgoodies.noContentBorder", Boolean.TRUE);
		putClientProperty("jgoodies.embeddedTabs", Boolean.valueOf(System.getProperty("jgoodies.embeddedTabs", "false")));
		jGoodies = LnFHelper.isJGoodies();
		try
		{
			TabbedPaneUI tui = TabbedPaneUIFactory.getBorderLessUI();
			if (tui != null)
			{
				this.setUI(tui);
			}
		}
		catch (Exception e)
		{
			LogMgr.logError("WbTabbedPane.init()", "Error during init", e);
		}
	}

	@Override
	public Insets getInsets()
	{
		return WbSwingUtilities.EMPTY_INSETS;
	}

	@Override
	public void setTitleAt(int index, String title)
	{
		super.setTitleAt(index, title);
		TabButtonComponent comp = getTabButton(index);
		if (comp != null)
		{
			comp.setTitle(title);
		}
	}

	@Override
	public void insertTab(String title, Icon icon, Component component, String tip, int index)
	{
		super.insertTab(title, icon, component, tip, index);
		if (!jGoodies)
		{
			// Always insert our own tab component to work around a bug with HTML rendering
			// in newer JDKs, see: http://bugs.sun.com/view_bug.do?bug_id=6670274
			
			// The JGoodies Look on the other hand has a problem when rendering a custom
			// component for the tab. If the buttons are disabled, and the jgoodies look
			// is active, it's better to not insert the custom component at all.
			setTabComponentAt(index, new TabButtonComponent(title, this, tabCloser != null));
		}
		if (tabCloser != null)
		{
			updateButtons();
		}
	}

	/**
	 * The empty override is intended, to give public access to the method
	 */
	@Override
	public void fireStateChanged()
	{
		super.fireStateChanged();
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean isManagingFocus()
	{
		return false;
	}

	@Override
	public boolean isRequestFocusEnabled()
	{
		return false;
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean isFocusTraversable()
	{
		return false;
	}

	@Override
	public boolean isFocusable()
	{
		return false;
	}

	public void disableDragDropReordering()
	{
		this.removeMouseListener(this);
		this.removeMouseMotionListener(this);
		this.tabMover = null;
		draggedTabIndex = -1;
	}

	public void enableDragDropReordering(Moveable mover)
	{
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.tabMover = mover;
		draggedTabIndex = -1;
	}

	public void mouseClicked(MouseEvent e)
	{
	}

	public void mousePressed(MouseEvent e)
	{
		int index = getUI().tabForCoordinate(this, e.getX(), e.getY());
		if (this.tabMover != null)
		{
			if (this.tabMover.startMove(index))
			{
				draggedTabIndex = index;
			}
		}
	}

	public void mouseReleased(MouseEvent e)
	{
		if (this.tabMover != null)
		{
			this.tabMover.endMove(draggedTabIndex);
		}
		draggedTabIndex = -1;
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	public void mouseEntered(MouseEvent e)
	{
	}

	public void mouseExited(MouseEvent e)
	{
	}

	public void mouseDragged(MouseEvent e)
	{
		if (tabMover == null)
		{
			return;
		}
		if (draggedTabIndex == -1)
		{
			return;
		}

		int newIndex = getUI().tabForCoordinate(this, e.getX(), e.getY());

		if (newIndex != -1 && newIndex != draggedTabIndex)
		{
			setCursor(DragSource.DefaultMoveDrop);
			if (tabMover.moveTab(draggedTabIndex, newIndex))
			{
				draggedTabIndex = newIndex;
			}
		}
	}

	public void mouseMoved(MouseEvent e)
	{
	}

	private void updateButtons()
	{
		if (tabCloser == null) return;
		
		int count = getTabCount();
		for (int i=0; i < count; i++)
		{
			boolean canClose = tabCloser.canCloseTab(i);
			setCloseButtonEnabled(i, canClose);
			if (hideDisabledButtons)
			{
				TabButtonComponent tab = getTabButton(i);
				if (tab != null) tab.setButtonVisible(canClose);
			}
		}
	}

	@Override
	public void stateChanged(ChangeEvent e)
	{
		updateButtons();
	}
}
