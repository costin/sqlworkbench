/*
 * XmlOptions.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2005, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.dialogs.export;

/**
 *
 * @author support@sql-workbench.net
 */
public interface XmlOptions
{
	boolean getUseCDATA();
	void setUseCDATA(boolean flag);
	boolean getUseVerboseXml();
	void setUseVerboseXml(boolean flag);
}