/*
 * ImportValueModifier.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2008, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db.importer.modifier;

import workbench.db.ColumnIdentifier;

/**
 *
 * @author support@sql-workbench.net
 */
public interface ImportValueModifier 
{
	int getSize();
	String modifyValue(ColumnIdentifier column, String value);
}