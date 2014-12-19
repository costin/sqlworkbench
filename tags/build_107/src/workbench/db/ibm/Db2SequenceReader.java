/*
 * Db2SequenceReader.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2009, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db.ibm;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import workbench.db.SequenceDefinition;
import workbench.db.SequenceReader;
import workbench.db.WbConnection;

import workbench.log.LogMgr;
import workbench.resource.Settings;
import workbench.storage.DataStore;
import workbench.util.SqlUtil;
import workbench.util.StringUtil;

/**
 * A class to read sequence definitions from a DB2 database.
 * 
 * @author  support@sql-workbench.net
 */
public class Db2SequenceReader
	implements SequenceReader
{
	private WbConnection connection;
	private boolean isHost;
	private boolean quoteKeyword;
	
	public Db2SequenceReader(WbConnection conn)
	{
		this.connection = conn;
	}

	public List<SequenceDefinition> getSequences(String owner, String namePattern)
	{
		DataStore ds = getRawSequenceDefinition(owner, namePattern);
		if (ds == null) return Collections.emptyList();
		List<SequenceDefinition> result = new ArrayList<SequenceDefinition>(ds.getRowCount());
		for (int row = 0; row < ds.getRowCount(); row ++)
		{
			result.add(createSequenceDefinition(ds, row));
		}
		return result;
	}
	
	public SequenceDefinition getSequenceDefinition(String owner, String sequence)
	{
		DataStore ds = getRawSequenceDefinition(owner, sequence);
		if (ds == null || ds.getRowCount() != 1) return null;
		return createSequenceDefinition(ds, 0);
	}
	
	private SequenceDefinition createSequenceDefinition(DataStore ds, int row)
	{
		String name = ds.getValueAsString(row, "SEQNAME");
		SequenceDefinition result = new SequenceDefinition(null, name);
		result.setSequenceProperty("START", ds.getValue(row, "START"));
		result.setSequenceProperty("MINVALUE", ds.getValue(row, "MINVALUE"));
		result.setSequenceProperty("MAXVALUE", ds.getValue(row, "MAXVALUE"));
		result.setSequenceProperty("INCREMENT", ds.getValue(row, "INCREMENT"));
		result.setSequenceProperty("CYCLE", ds.getValue(row, "CYCLE"));
		result.setSequenceProperty("ORDER", ds.getValue(row, "ORDER"));
		result.setSequenceProperty("CACHE", ds.getValue(row, "CACHE"));
		result.setSequenceProperty("DATATYPEID", ds.getValue(row, "DATATYPEID"));
		result.setComment(ds.getValueAsString(row, "REMARKS"));
		readSequenceSource(result);
		return result;
	}
	
	public DataStore getRawSequenceDefinition(String schema, String namePattern)
	{
		String sql = null;

		int schemaIndex = -1;
		int nameIndex = -1;

		String nameCol;
		String schemaCol;
		if (this.connection.getMetadata().getDbId().equals("db2h") || isHost)
		{
			// Host system
			sql = "SELECT NAME, \n" +
			"       START, \n" +
			"       MINVALUE, \n" +
			"       MAXVALUE, \n" +
			"       INCREMENT, \n" +
			"       CYCLE, \n" +
			"       ORDER, \n" +
			"       CACHE, \n" +
			"       DATATYPEID, \n" +
			"       REMARKS \n" +
			"FROM   SYSIBM.SYSSEQUENCES \n";

			nameCol = "name";
			schemaCol = "schema";
		}
		else
		{
			sql = "SELECT SEQNAME, \n" +
			"       START, \n" +
			"       MINVALUE, \n" +
			"       MAXVALUE, \n" +
			"       INCREMENT, \n" +
			"       CYCLE, \n" +
			"       ORDER, \n" +
			"       CACHE, \n" +
			"       DATATYPEID, \n" +
		  "       REMARKS  \n" +
			"FROM   syscat.sequences \n";
			
			nameCol = "seqname";
			schemaCol = "seqschema";
		}

		boolean whereAdded = false;

		if (StringUtil.isNonBlank(schema))
		{
			sql += " WHERE " + schemaCol + " = ?";
			schemaIndex = 1;
			whereAdded = true;
		}

		if (StringUtil.isNonBlank(namePattern))
		{
			if (whereAdded)
			{
				sql += " AND ";
				nameIndex = 2;
			}
			else
			{
				sql += " WHERE ";
				nameIndex = 1;
			}
			sql += nameCol + " LIKE ? ";
		}

		// Needed for the unit test (because in H2 order is a reserved word)
		if (quoteKeyword)
		{
			sql = sql.replace(" ORDER,", " \"ORDER\",");
		}
		
		if (Settings.getInstance().getDebugMetadataSql())
		{
			LogMgr.logInfo("Db2SequenceReader.getRawSequenceDefinition()", "Using query=\n" + sql);
		}
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		DataStore result = null;
		try
		{
			stmt = this.connection.getSqlConnection().prepareStatement(sql);
			if (schemaIndex > -1)	stmt.setString(schemaIndex, schema);
			if (nameIndex > -1) stmt.setString(nameIndex, namePattern);
			rs = stmt.executeQuery();
			result = new DataStore(rs, this.connection, true);
		}
		catch (Exception e)
		{
			LogMgr.logError("OracleMetaData.getSequenceDefinition()", "Error when retrieving sequence definition", e);
		}
		finally
		{
			SqlUtil.closeAll(rs,stmt);
		}
		
		return result;
	}
	
	public CharSequence getSequenceSource(String schema, String sequence)
	{
		SequenceDefinition def = getSequenceDefinition(schema, sequence);
		if (def == null) return null;
		return def.getSource();
	}
	
	public void readSequenceSource(SequenceDefinition def)
	{
		StringBuilder result = new StringBuilder(100);

		String nl = Settings.getInstance().getInternalEditorLineEnding();

		result.append("CREATE SEQUENCE ");
		result.append(def.getSequenceName());

		Number start = (Number) def.getSequenceProperty("START");
		Number minvalue = (Number) def.getSequenceProperty("MINVALUE");
		Number maxvalue = (Number) def.getSequenceProperty("MAXVALUE");
		Number increment = (Number) def.getSequenceProperty("INCREMENT");
		String cycle = (String) def.getSequenceProperty("CYCLE");
		String order = (String) def.getSequenceProperty("ORDER");
		Number cache = (Number) def.getSequenceProperty("CACHE");
		Number typeid = (Number) def.getSequenceProperty("typeid");

		if (typeid != null)
		{
      result.append(" AS " + typeIdToName(typeid.intValue()));
		}
		result.append(nl + "      INCREMENT BY ");
		result.append(increment);

		if (start.longValue() > 0)
		{
			result.append(nl + "      START WITH ");
			result.append(start);
		}

		if (minvalue == null || minvalue.longValue() == 0)
		{
			result.append(nl + "      NO MINVALUE");
		}
		else
		{
			result.append(nl + "      MINVALUE ");
			result.append(minvalue);
		}

		if (maxvalue == null || maxvalue.longValue() == -1)
		{
			result.append(nl + "      MAXVALUE ");
			result.append(maxvalue);
		}
		else
		{
			result.append(nl + "      NO MAXVALUE");
		}
		if (cache != null && cache.longValue() > 0)
		{
			result.append(nl + "      CACHE ");
			result.append(cache);
		}
		else
		{
			result.append(nl + "      NO CACHE");
		}
		
		result.append(nl + "      ");
		if (cycle != null && cycle.equals("Y"))
		{
			result.append("CYCLE");
		}
		else
		{
			result.append("NO CYCLE");
		}

		result.append(nl + "      ");
		if (order != null && order.equals("Y"))
		{
			result.append("ORDER");
		}
		else
		{
			result.append("NO ORDER");
		}

		result.append(';');
		result.append(nl);

		if (StringUtil.isNonBlank(def.getComment()))
		{
			result.append("COMMENT ON SEQUENCE " + def.getSequenceName() + " IS '" + def.getComment().replace("'", "''") + "';");
			result.append(nl);
		}

		def.setSource(result);
	}

	private String typeIdToName(int id)
	{
		switch (id)
		{
			case 20:
				return "BIGINT";
			case 28:
				return "SMALLINT";
			case 16:
				return "DECIMAL";
		}
		return "INTEGER";
	}

	/**
	 * For testing purposes only!!!
	 * @param flag
	 */
	void setIsHost(boolean flag)
	{
		isHost = flag;
	}

	void setQuoteKeyword(boolean flag)
	{
		quoteKeyword = flag;
	}
}