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

import workbench.log.LogMgr;

/**
 *
 * @author support@sql-workbench.net
 */
public class WbThread
	extends Thread
	implements Thread.UncaughtExceptionHandler 	
{

	/** Creates a new instance of WbThread */
	public WbThread(String name)
	{
		super();
		this.setName(name);
		this.setDaemon(true);
		this.setUncaughtExceptionHandler(this);
	}

	public WbThread(Runnable run, String name)
	{
		super(run);
		this.setName(name);
		this.setDaemon(true);
		this.setUncaughtExceptionHandler(this);
	}

  public void uncaughtException(Thread thread, Throwable error)
  {
    LogMgr.logError("WbThread.uncaughtException", "Thread + " + thread.getName() + " caused an exception", error);
  }

}