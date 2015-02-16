/*
 * WbThread.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2007, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.util;

/**
 *
 * @author support@sql-workbench.net
 */
public class WbThread
	extends Thread
{

	/** Creates a new instance of WbThread */
	public WbThread(String name)
	{
		super();
		this.setName(name);
		this.setDaemon(true);
	}

	public WbThread(Runnable run, String name)
	{
		super(run);
		this.setName(name);
		this.setDaemon(true);
	}

}