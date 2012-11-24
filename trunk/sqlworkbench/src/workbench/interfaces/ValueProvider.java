/*
 * ValueProvider.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2012, Thomas Kellerer
 * No part of this code may be reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 */
package workbench.interfaces;


import java.util.Collection;
import workbench.storage.ResultInfo;

/**
 *
 * @author Thomas Kellerer
 */
public interface ValueProvider
{
	ResultInfo getResultInfo();
	Collection<String> getColumnValues(String columnName);
}
