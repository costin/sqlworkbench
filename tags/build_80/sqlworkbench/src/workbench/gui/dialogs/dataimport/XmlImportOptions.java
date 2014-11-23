/*
 * XmlImportOptions.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2005, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.dialogs.dataimport;

/**
 *
 * @author support@sql-workbench.net
 */
public interface XmlImportOptions
{
	boolean getUseVerboseXml();
	void setUseVerboseXml(boolean flag);
}