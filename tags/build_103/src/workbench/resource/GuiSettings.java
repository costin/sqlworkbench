/*
 * GuiSettings.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2009, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.resource;

import java.awt.Color;
import workbench.util.StringUtil;


/**
 *
 * @author support@sql-workbench.net
 */
public class GuiSettings
{
	public static boolean getConfirmTabClose()
	{
		return Settings.getInstance().getBoolProperty("workbench.gui.closetab.confirm", false);
	}
	
	public static void setConfirmTabClose(boolean flag)
	{
		Settings.getInstance().setProperty("workbench.gui.closetab.confirm", flag);
	}

	public static boolean getShowTabIndex()
	{
		return Settings.getInstance().getBoolProperty(Settings.PROPERTY_SHOW_TAB_INDEX, true);
	}

	public static void setShowTabIndex(boolean flag)
	{
		Settings.getInstance().setProperty(Settings.PROPERTY_SHOW_TAB_INDEX, flag);
	}

	public static boolean getIncludeHeaderInOptimalWidth()
	{
		return Settings.getInstance().getBoolProperty("workbench.gui.optimalwidth.includeheader", true);
	}

	public static void setIncludeHeaderInOptimalWidth(boolean flag)
	{
		Settings.getInstance().setProperty("workbench.gui.optimalwidth.includeheader", flag);
	}

	public static boolean getAutomaticOptimalRowHeight()
	{
		return Settings.getInstance().getBoolProperty("workbench.gui.optimalrowheight.automatic", false);
	}

	public static void setAutomaticOptimalRowHeight(boolean flag)
	{
		Settings.getInstance().setProperty("workbench.gui.optimalrowheight.automatic", flag);
	}

	public static boolean getAutomaticOptimalWidth()
	{
		return Settings.getInstance().getBoolProperty("workbench.gui.optimalwidth.automatic", true);
	}

	public static void setAutomaticOptimalWidth(boolean flag)
	{
		Settings.getInstance().setProperty("workbench.gui.optimalwidth.automatic", flag);
	}

	public static boolean getUseAnimatedIcon()
	{
		return Settings.getInstance().getBoolProperty(Settings.PROPERTY_ANIMATED_ICONS, false);
	}

	public static void setUseAnimatedIcon(boolean flag)
	{
		Settings.getInstance().setProperty(Settings.PROPERTY_ANIMATED_ICONS, flag);
	}

	public static boolean getUseDynamicLayout()
	{
		return Settings.getInstance().getBoolProperty("workbench.gui.dynamiclayout", true);
	}

	public static void setUseDynamicLayout(boolean flag)
	{
		Settings.getInstance().setProperty("workbench.gui.dynamiclayout", flag);
	}

	public static boolean getShowMnemonics()
	{
		return Settings.getInstance().getBoolProperty("workbench.gui.showmnemonics", true);
	}

	public static boolean getShowSplash()
	{
		return Settings.getInstance().getBoolProperty("workbench.gui.showsplash", false);
	}

	public static int getProfileDividerLocation()
	{
		return Settings.getInstance().getIntProperty("workbench.gui.profiles.divider", -1);
	}

	public static void setProfileDividerLocation(int aValue)
	{
		Settings.getInstance().setProperty("workbench.gui.profiles.divider", Integer.toString(aValue));
	}

	public static void setMinColumnWidth(int width)
	{
		Settings.getInstance().setProperty("workbench.gui.optimalwidth.minsize", width);
	}

	public static int getMinColumnWidth()
	{
		return Settings.getInstance().getIntProperty("workbench.gui.optimalwidth.minsize", 50);
	}

	public static int getMaxColumnWidth()
	{
		return Settings.getInstance().getIntProperty("workbench.gui.optimalwidth.maxsize", 850);
	}

	public static void setMaxColumnWidth(int width)
	{
		Settings.getInstance().setProperty("workbench.gui.optimalwidth.maxsize", width);
	}

	public static int getAutRowHeightMaxLines()
	{
		return Settings.getInstance().getIntProperty("workbench.gui.optimalrowheight.maxlines", 10);
	}

	public static void setAutRowHeightMaxLines(int lines)
	{
		Settings.getInstance().setProperty("workbench.gui.optimalrowheight.maxlines", lines);
	}

	public static boolean getIgnoreWhitespaceForAutoRowHeight()
	{
		return Settings.getInstance().getBoolProperty("workbench.gui.optimalrowheight.ignore.emptylines", false);
	}

	public static void setIgnoreWhitespaceForAutoRowHeight(boolean flag)
	{
		Settings.getInstance().setProperty("workbench.gui.optimalrowheight.ignore.emptylines", flag);
	}

	public static void setLookAndFeelClass(String aClassname)
	{
		Settings.getInstance().setProperty("workbench.gui.lookandfeelclass", aClassname);
	}

	public static String getLookAndFeelClass()
	{
		return Settings.getInstance().getProperty("workbench.gui.lookandfeelclass", "");
	}

	public static int getMaxMacrosInMenu()
	{
		return Settings.getInstance().getIntProperty("workbench.gui.macro.maxmenuitems", 9);
	}

	public static final int SHOW_NO_FILENAME = 0;
	public static final int SHOW_FILENAME = 1;
	public static final int SHOW_FULL_PATH = 2;

	public static void setShowFilenameInWindowTitle(int type)
	{
		switch (type)
		{
			case SHOW_NO_FILENAME:
				Settings.getInstance().setProperty("workbench.gui.display.showfilename", "none");
				break;
			case SHOW_FILENAME:
				Settings.getInstance().setProperty("workbench.gui.display.showfilename", "name");
				break;
			case SHOW_FULL_PATH:
				Settings.getInstance().setProperty("workbench.gui.display.showfilename", "path");
				break;
		}
	}

	public static int getShowFilenameInWindowTitle()
	{
		String type = Settings.getInstance().getProperty("workbench.gui.display.showfilename", "none");
		if ("name".equalsIgnoreCase(type)) return SHOW_FILENAME;
		if ("path".equalsIgnoreCase(type)) return SHOW_FULL_PATH;
		return SHOW_NO_FILENAME;
	}

	public static String getTitleGroupSeparator()
	{
		String sep = Settings.getInstance().getProperty("workbench.gui.display.titlegroupsep", "/");
		if ("XXX".equals(sep)) return "";
		return sep;
	}

	public static void setTitleGroupSeparator(String sep)
	{
		if (StringUtil.isBlank(sep)) sep = "XXX";
		Settings.getInstance().setProperty("workbench.gui.display.titlegroupsep", sep);
	}

	public static String getTitleGroupBracket()
	{
		return Settings.getInstance().getProperty("workbench.gui.display.titlegroupbracket", null);
	}

	public static void setTitleGroupBracket(String bracket)
	{
		Settings.getInstance().setProperty("workbench.gui.display.titlegroupbracket", bracket);
	}

	public static void setShowWorkspaceInWindowTitle(boolean flag)
	{
		Settings.getInstance().setProperty("workbench.gui.display.showpworkspace", flag);
	}

	public static boolean getShowWorkspaceInWindowTitle()
	{
		return Settings.getInstance().getBoolProperty("workbench.gui.display.showpworkspace", true);
	}

	public static void setShowProfileGroupInWindowTitle(boolean flag)
	{
		Settings.getInstance().setProperty("workbench.gui.display.showprofilegroup", flag);
	}

	public static boolean getShowProfileGroupInWindowTitle()
	{
		return Settings.getInstance().getBoolProperty("workbench.gui.display.showprofilegroup", false);
	}

	public static void setShowProductNameAtEnd(boolean flag)
	{
		Settings.getInstance().setProperty("workbench.gui.display.name_at_end", flag);
	}

	public static boolean getShowProductNameAtEnd()
	{
		return Settings.getInstance().getBoolProperty("workbench.gui.display.name_at_end", false);
	}

	public static boolean getShowToolbar()
	{
		return Settings.getInstance().getBoolProperty(Settings.PROPERTY_SHOW_TOOLBAR, true);
	}

	public static void setShowToolbar(final boolean show)
	{
		Settings.getInstance().setProperty(Settings.PROPERTY_SHOW_TOOLBAR, show);
	}

	public static boolean getAllowRowHeightResizing()
	{
		return Settings.getInstance().getBoolProperty("workbench.gui.display.rowheightresize", false);
	}

	public static void setAllowRowHeightResizing(boolean flag)
	{
		Settings.getInstance().setProperty("workbench.gui.display.rowheightresize", flag);
	}

	public static boolean getUseAlternateRowColor()
	{
		return Settings.getInstance().getBoolProperty("workbench.gui.table.alternate.use", false);
	}

	public static void setUseAlternateRowColor(boolean flag)
	{
		Settings.getInstance().setProperty("workbench.gui.table.alternate.use", flag);
	}

	public static void setNullColor(Color c)
	{
		Settings.getInstance().setColor("workbench.gui.table.null.color", c);
	}

	public static Color getNullColor()
	{
		return Settings.getInstance().getColor("workbench.gui.table.null.color", null);
	}

	public static Color getExpressionHighlightColor()
	{
		return Settings.getInstance().getColor("workbench.gui.table.searchhighlite.color", Color.YELLOW);
	}

	public static void setExpressionHighlightColor(Color c)
	{
		Settings.getInstance().setColor("workbench.gui.table.searchhighlite.color", c);
	}

	public static Color getAlternateRowColor()
	{
		Color defColor = (getUseAlternateRowColor() ? new Color(252,252,252) : null);
		return Settings.getInstance().getColor("workbench.gui.table.alternate.color", defColor);
	}

	public static void setAlternateRowColor(Color c)
	{
		Settings.getInstance().setColor("workbench.gui.table.alternate.color", c);
	}

	public static void setRequiredFieldColor(Color c)
	{
		Settings.getInstance().setColor("workbench.gui.edit.requiredfield.color", c);
	}

	public static Color getRequiredFieldColor()
	{
		return Settings.getInstance().getColor("workbench.gui.edit.requiredfield.color", new Color(255,100,100));
	}

	public static void setHighlightRequiredFields(boolean flag)
	{
		Settings.getInstance().setProperty("workbench.gui.edit.requiredfield.dohighlight", flag);
	}

	public static boolean getHighlightRequiredFields()
	{
		return Settings.getInstance().getBoolProperty("workbench.gui.edit.requiredfield.dohighlight", true);
	}

	public static void setConfirmDiscardResultSetChanges(boolean flag)
	{
		Settings.getInstance().setProperty("workbench.gui.edit.warn.discard.changes", flag);
	}

	public static boolean getConfirmDiscardResultSetChanges()
	{
		return Settings.getInstance().getBoolProperty("workbench.gui.edit.warn.discard.changes", false);
	}

	public static boolean getShowSelectionSummary()
	{
		return Settings.getInstance().getBoolProperty("workbench.gui.selection.summar", true);
	}

	public static void setShowSelectionSummary(boolean flag)
	{
		Settings.getInstance().setProperty("workbench.gui.selection.summar", flag);
	}
}