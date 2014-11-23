/*
 * FileActions.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2005, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.interfaces;

/**
 *
 * @author  support@sql-workbench.net
 */
public interface FileActions
{
	void saveItem() throws Exception;
	void deleteItem() throws Exception;
	void newItem(boolean copyCurrent) throws Exception;
}