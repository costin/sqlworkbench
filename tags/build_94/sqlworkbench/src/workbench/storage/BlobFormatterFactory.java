/*
 * BlobFormatterFactory.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2007, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.storage;

import workbench.db.DbMetadata;

/**
 * @author support@sql-workbench.net
 */
public class BlobFormatterFactory
{

	public static BlobLiteralFormatter createAnsiFormatter()
	{
		// SQL Server, MySQL support the ANSI Syntax
		// using 0xABCDEF...
		// we use that for all others as well.
		HexBlobFormatter f = new HexBlobFormatter();
		f.setPrefix("0x");
		
		return f;
	}
	
	public static BlobLiteralFormatter createInstance(DbMetadata meta)
	{
		if (meta.isPostgres())
		{
			return new PostgresBlobFormatter();
		}
		else if (meta.isOracle())
		{
			// this might only work with Oracle 10g...
			// and will probably fail on BLOBs > 4KB
			HexBlobFormatter f = new HexBlobFormatter();
			f.setUseUpperCase(true);
			f.setPrefix("to_blob(utl_raw.cast_to_raw('0x");
			f.setSuffix("'))");
			return f;
		}
		else if ("db2_nt".equalsIgnoreCase(meta.getDbId()) || "h2".equals(meta.getDbId()))
		{
			// Although the DB2 Manuals says it supports
			// binary string constants, it is very likely
			// that this will be rejected by DB2 due to the 
			// max.length of 32K for binary strings.
			HexBlobFormatter f = new HexBlobFormatter();
			f.setUseUpperCase(true);
			f.setPrefix("X'");
			f.setSuffix("'");
			return f;
		}
		else if (meta.isHsql())
		{
			HexBlobFormatter f = new HexBlobFormatter();
			f.setUseUpperCase(false);
			f.setPrefix("'");
			f.setSuffix("'");
			return f;
		}
		return createAnsiFormatter();
	}
	
}