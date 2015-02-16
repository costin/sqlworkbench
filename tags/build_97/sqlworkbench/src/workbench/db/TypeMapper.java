/*
 * TypeMapper.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2007, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;

import workbench.log.LogMgr;
import workbench.util.SqlUtil;

/**
 * A class to map datatypes from one DBMS to another.
 * @author support@sql-workbench.net
 */
public class TypeMapper
{
	private WbConnection dbConn;
	private HashMap<Integer, String> typeInfo;
	private List<String> ignoreTypes;

	public TypeMapper(WbConnection aConnection)
	{
		this.dbConn = aConnection;
		this.ignoreTypes = aConnection.getDbSettings().getDataTypesToIgnore();
		this.createTypeMap();
	}

	public String getTypeName(int type, int size, int digits)
	{
		Integer key = new Integer(type);
		if (!this.typeInfo.containsKey(key)) return SqlUtil.getTypeName(type);
		String name = this.typeInfo.get(key);

		StringBuilder result = new StringBuilder(30);
		result.append(name);

		// Now we need to check if the data type needs an argument.
		// I could use the "parameter" column from the driver's type info
		// result set, but it seems that not every DBMS returns the correct
		// information there, so I'm using my "own" logic to determine
		// if the data type needs an argument.
		if (type == Types.VARCHAR || type == Types.CHAR)
		{
			if (size == 0) return null;
			result.append('(');
			result.append(size);
			result.append(')');
		}
		// INTEGER's normally don't need a size argument
		else if (SqlUtil.isNumberType(type) && !SqlUtil.isIntegerType(type))
		{
			if (SqlUtil.isDecimalType(type, size, digits))
			{
				result.append('(');
				result.append(size);
				result.append(',');
				result.append(digits);
				result.append(')');
			}
			else if (type != Types.INTEGER)
			{
				result.append('(');
				result.append(size);
				result.append(')');
			}
		}

		return result.toString();
	}


	private void createTypeMap()
	{
		ResultSet rs = null;
		this.typeInfo = new HashMap<Integer, String>(27);
		try
		{
			rs = this.dbConn.getSqlConnection().getMetaData().getTypeInfo();
			while (rs.next())
			{
				String name = rs.getString(1);
				int type = rs.getInt(2);

				// we can't handle arrays anyway
				if (type == java.sql.Types.ARRAY || type == java.sql.Types.OTHER) continue;
				if (this.ignoreTypes.contains(name)) continue;

				Integer key = new Integer(type);
				if (this.typeInfo.containsKey(key))
				{
					LogMgr.logWarning("TypeMapper.createTypeMap()", "The mapping from JDBC type "  + SqlUtil.getTypeName(type) + " to  DB type " + name + " will be ignored. A mapping is already present.");
				}
				else
				{
					LogMgr.logInfo("TypeMapper.createTypeMap()", "Mapping JDBC type "  + SqlUtil.getTypeName(type) + " to DB type " + name);
					this.typeInfo.put(key, name);
				}
			}
		}
		catch (SQLException e)
		{
			LogMgr.logError("TypeMapper.createTypeMap()", "Error reading type info for target connection", e);
			this.typeInfo = new HashMap<Integer, String>();
		}
		finally
		{
			SqlUtil.closeResult(rs);
		}
	}
}
