/*
 * OracleProcedureReader.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2009, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db.oracle;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import workbench.db.JdbcProcedureReader;
import workbench.db.NoConfigException;
import workbench.db.ProcedureDefinition;
import workbench.db.WbConnection;
import workbench.resource.Settings;
import workbench.sql.DelimiterDefinition;
import workbench.util.SqlUtil;

/**
 * A ProcedureReader to read the source of an Oracle procedure. 
 * Packages are handled properly. The Oracle JDBC driver
 * reports the package name in the catalog column 
 * of the getProcedures() ResultSet. 
 * The method {@link #readProcedureSource(ProcedureDefinition)} the 
 * catalog definition of the ProcedureDefinition is checked. If it's not 
 * null it is assumed that this the definition is actually a package.
 * 
 * @see workbench.db.JdbcProcedureReader
 * @author support@sql-workbench.net
 */
public class OracleProcedureReader
	extends JdbcProcedureReader
{
	public OracleProcedureReader(WbConnection conn)
	{
		super(conn);
	}

	private final StringBuilder PROC_HEADER = new StringBuilder("CREATE OR REPLACE ");
	
	public StringBuilder getProcedureHeader(String catalog, String schema, String procname, int procType)
	{
		return PROC_HEADER;
	}


	public CharSequence getPackageSource(String owner, String packageName)
	{
		final String sql = "SELECT text \n" +
			"FROM all_source \n" +
			"WHERE name = ? \n" +
			"AND   owner = ? \n" +
			"AND   type = ? \n" +
			"ORDER BY line";
		
		StringBuilder result = new StringBuilder(1000);
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		String nl = Settings.getInstance().getInternalEditorLineEnding();
		DelimiterDefinition delimiter = Settings.getInstance().getAlternateDelimiter(connection);
		
		try
		{
			int lineCount = 0;
			
			synchronized (connection)
			{
				stmt = this.connection.getSqlConnection().prepareStatement(sql);
				stmt.setString(1, packageName);
				stmt.setString(2, owner);
				stmt.setString(3, "PACKAGE");
				rs = stmt.executeQuery();
				while (rs.next())
				{
					String line = rs.getString(1);
					if (line != null)
					{
						lineCount ++;
						if (lineCount == 1)
						{
							result.append("CREATE OR REPLACE ");
						}
						result.append(line);
					}
				}
				result.append(nl);
				result.append(delimiter.getDelimiter());
				result.append(nl);
				result.append(nl);
				lineCount = 0;

				stmt.clearParameters();
				stmt.setString(1, packageName);
				stmt.setString(2, owner);
				stmt.setString(3, "PACKAGE BODY");
				rs = stmt.executeQuery();
				while (rs.next())
				{
					String line = rs.getString(1);
					if (line != null)
					{
						lineCount ++;
						if (lineCount == 1)
						{
							result.append("CREATE OR REPLACE ");
						}
						result.append(line);
					}
				}
			}
			result.append(nl);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			SqlUtil.closeAll(rs, stmt);
		}
		return result;
	}
	
	
	public void readProcedureSource(ProcedureDefinition def)
		throws NoConfigException
	{
		
		if (def.getCatalog() != null)
		{
			CharSequence source = getPackageSource(def.getSchema(), def.getCatalog());
			def.setSource(source);
			def.setOraclePackage(true);			
		}
		else if (def.isOraclePackage())
		{
			CharSequence source = getPackageSource(def.getSchema(), def.getProcedureName());
			def.setSource(source);
		}
		else
		{
			super.readProcedureSource(def);
		}
	}		
}