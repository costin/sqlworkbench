/*
 * HsqlSequenceReader.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2006, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db.hsqldb;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import workbench.db.SequenceReader;
import workbench.log.LogMgr;
import workbench.resource.Settings;
import workbench.storage.DataStore;
import workbench.util.SqlUtil;

/**
 * @author  support@sql-workbench.net
 */
public class HsqlSequenceReader
	implements SequenceReader
{
	private Connection dbConn;
	private boolean useInformationSchema;
	
	public HsqlSequenceReader(Connection conn)
	{
		this.dbConn = conn;
		this.useInformationSchema = HsqlMetadata.supportsInformationSchema(conn);
	}

	public DataStore getSequenceDefinition(String owner, String sequence)
	{
		
		StringBuffer query = new StringBuffer(100);
		query.append("SELECT sequence_name, dtd_identifier, maximum_value, minimum_value, increment, start_with FROM ");
		if (useInformationSchema) query.append("information_schema.");
		query.append("system_sequences WHERE sequence_name = ?");
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		DataStore result = null;
		try
		{
			stmt = this.dbConn.prepareStatement(query.toString());
			stmt.setString(1, sequence.trim());
			rs = stmt.executeQuery();
			result = new DataStore(rs, true);
		}
		catch (Throwable e)
		{
			LogMgr.logError("HsqlSequenceReader.getSequenceDefinition()", "Error when retrieving sequence definition", e);
		}
		finally
		{
			SqlUtil.closeAll(rs, stmt);
		}

		return result;	
	}

	public List getSequenceList(String owner)
	{
		ResultSet rs = null;
		PreparedStatement stmt = null;
		ArrayList result = new ArrayList(100);

		StringBuffer query = new StringBuffer(100);
		query.append("SELECT sequence_name FROM ");
		if (useInformationSchema) query.append("information_schema.");
		query.append("system_sequences");

		try
		{
			stmt = this.dbConn.prepareStatement(query.toString());
			rs = stmt.executeQuery();
			while (rs.next())
			{
				String seq = rs.getString(1);
				if (seq != null) result.add(seq.trim());
			}
		}
		catch (Throwable e)
		{
			LogMgr.logError("HsqlSequenceReader.getSequenceList()", "Error when retrieving sequences",e);
		}
		finally
		{
			SqlUtil.closeAll(rs, stmt);
		}
		return result;
	}

	public String getSequenceSource(String owner, String sequence)
	{
		ResultSet rs = null;
		PreparedStatement stmt = null;
		StringBuffer query = new StringBuffer(100);
		query.append("SELECT sequence_name, dtd_identifier, start_with, maximum_value, increment FROM ");
		if (useInformationSchema)
		{
			query.append("information_schema.");
		}
		query.append("system_sequences WHERE sequence_name = ?");
		StringBuffer result = new StringBuffer(100);
		result.append("CREATE SEQUENCE ");
		String nl = Settings.getInstance().getInternalEditorLineEnding();
		try
		{
			stmt = this.dbConn.prepareStatement(query.toString());
			stmt.setString(1, sequence);
			rs = stmt.executeQuery();
			while (rs.next())
			{
				String seq = rs.getString(1);
				result.append(seq);
				String type = rs.getString(2);
				if (!"INTEGER".equals(type))
				{
					result.append(" AS " + type);
				}
				long start = rs.getLong(3);
				if (start > 0)
				{
					result.append(nl + "       START WITH ");
					result.append(start);
				}
				String max = rs.getString(4);
				final BigInteger bigMax = new BigInteger("9223372036854775807");
				final BigInteger intMax = new BigInteger(Integer.toString(Integer.MAX_VALUE));
				boolean isMax = false;
				try
				{
					BigInteger maxValue = new BigInteger(max);
					isMax = (maxValue.equals(intMax) || maxValue.equals(bigMax));
				}
				catch (Exception e)
				{
					isMax = false;
				}
				
				if (!isMax)
				{
					result.append(nl + "       START WITH ");
					result.append(start);
				}
				long inc = rs.getLong(5);
				if (inc != 1)
				{
					result.append(nl + "       INCREMENT BY ");
					result.append(inc);
				}
				result.append(";");
			}
		}
		catch (Throwable e)
		{
			LogMgr.logError("HsqlSequenceReader.getSequenceSource()", "Error when retrieving sequence source",e);
		}
		finally
		{
			SqlUtil.closeAll(rs, stmt);
		}
		return result.toString();
	}
}