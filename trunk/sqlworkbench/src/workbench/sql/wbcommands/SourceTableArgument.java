/*
 * SourceTableArgument.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2012, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.sql.wbcommands;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import workbench.db.TableIdentifier;
import workbench.db.WbConnection;
import workbench.log.LogMgr;
import workbench.util.CollectionUtil;
import workbench.util.StringUtil;
import workbench.util.WbStringTokenizer;

/**
 * Evaluate table arguments that may contain wildcards.
 *
 * @author Thomas Kellerer
 */
public class SourceTableArgument
{
	private List<String> missingTables = new ArrayList<String>();
	private List<TableIdentifier> tables = new ArrayList<TableIdentifier>();
	private boolean wildcardsPresent;
	private boolean schemaAsCatalog;

	public SourceTableArgument(String includeTables, WbConnection dbConn)
		throws SQLException
	{
		if (dbConn == null) return;
		schemaAsCatalog = !dbConn.getDbSettings().supportsSchemas();
		initTableList(includeTables, null, null, dbConn.getMetadata().getTableTypesArray(), dbConn);
	}

	/**
	 *
	 * @param includeTables the parameter value to include tables
	 * @param excludeTables the parameter value to exclude tables
	 * @param types         the parameter value for the table types
	 * @param dbConn        the connection to use
	 * <p/>
	 * @throws SQLException
	 */
	public SourceTableArgument(String includeTables, String excludeTables, String schema, WbConnection dbConn)
		throws SQLException
	{
		if (dbConn == null) return;

		String[] types = dbConn.getMetadata().getTableTypesArray();
		schemaAsCatalog = !dbConn.getDbSettings().supportsSchemas();
		initTableList(includeTables, excludeTables, schema, types, dbConn);
	}

	/**
	 *
	 * @param includeTables  the tables to include may be null if a schema name is supplied
	 * @param excludeTables  tablename to exclude
	 * @param schema         the schema to use. May be null if table names are supplied
	 *                       if no table names are supplied, "%" or "*" can be used to return all tables from all schemas
	 * @param types          the object types to retrieve
	 * @param dbConn         the connection
	 *
	 * @throws SQLException
	 */
	public SourceTableArgument(String includeTables, String excludeTables, String schema, String[] types, WbConnection dbConn)
		throws SQLException
	{
		if (StringUtil.isEmptyString(includeTables)) return;
		if (dbConn == null) return;
		schemaAsCatalog = !dbConn.getDbSettings().supportsSchemas();

		initTableList(includeTables, excludeTables, schema, types, dbConn);
	}

	private void initTableList(String includeTables, String excludeTables, String schema, String[] types, WbConnection dbConn)
		throws SQLException
	{
		missingTables.clear();
		List<TableIdentifier> toAdd = parseArgument(includeTables, schema, true, types, dbConn);
		tables.addAll(toAdd);

		if (StringUtil.isNonBlank(excludeTables))
		{
			List<TableIdentifier> toRemove = parseArgument(excludeTables, schema, false, null, dbConn);
			tables.removeAll(toRemove);
		}
	}

	public static String[] parseTypes(String types, WbConnection conn)
	{
		if (StringUtil.isBlank(types)) return conn.getMetadata().getTableTypesArray();

		if ("%".equals(types) || "*".equals(types)) return null;

		List<String> typeList = StringUtil.stringToList(types.toUpperCase());

		if (typeList.isEmpty()) return conn.getMetadata().getTableTypesArray();

		String[] result = new String[typeList.size()];

		return typeList.toArray(result);
	}

	private List<TableIdentifier> parseArgument(String arg, String schema, boolean checkWildcard, String[] types, WbConnection dbConn)
		throws SQLException
	{
		List<String> args = getObjectNames(arg);

		List<TableIdentifier> result = CollectionUtil.arrayList();

		if (args.size() <= 0 && StringUtil.isBlank(schema)) return result;

		String schemaToUse;
		if (StringUtil.isBlank(schema))
		{
			schemaToUse = dbConn.getMetadata().getCurrentSchema();
		}
		else if (schema.equals("*") || schema.equals("%"))
		{
			// all tables from all schemas
			schemaToUse = null;
			args = CollectionUtil.arrayList("*");
		}
		else
		{
			schemaToUse =  dbConn.getMetadata().adjustSchemaNameCase(schema);
		}

		if (args.isEmpty() && schemaToUse != null)
		{
			List<TableIdentifier> l = null;

			if (schemaAsCatalog)
			{
				l = dbConn.getMetadata().getObjectList(null, schemaToUse, null, types);
			}
			else
			{
				l = dbConn.getMetadata().getObjectList(null, null, schemaToUse, types);
			}
			result.addAll(l);
		}
		else
		{
			for (String t : args)
			{
				if (t.indexOf('*') > -1 || t.indexOf('%') > -1)
				{
					if (checkWildcard) this.wildcardsPresent = true;
					TableIdentifier tbl = new TableIdentifier(t);
					if (tbl.getSchema() == null && !(t.equals("*") || t.equals("%")))
					{
						tbl.setSchema(schemaToUse);
					}
					tbl.adjustCase(dbConn);
					List<TableIdentifier> l = null;
					if (schemaAsCatalog)
					{
						l = dbConn.getMetadata().getObjectList(tbl.getTableName(), tbl.getSchema(), null, types);
					}
					else
					{
						l = dbConn.getMetadata().getObjectList(tbl.getTableName(), null, tbl.getSchema(), types);
					}
					result.addAll(l);
				}
				else
				{
					TableIdentifier toSearch = new TableIdentifier(t, dbConn);
					TableIdentifier tbl = null;
					
					if (types == null)
					{
						tbl = dbConn.getMetadata().findTable(toSearch);
					}
					else
					{
						tbl = dbConn.getMetadata().searchObjectOnPath(toSearch, types);
					}

					if (tbl != null)
					{
						result.add(tbl);
					}
					else
					{
						missingTables.add(t);
						LogMgr.logDebug("SourceTableArgument.parseArgument()", "Table " + t + " not found!");
					}
				}
			}
		}
		return result;
	}

	public List<String> getMissingTables()
	{
		return missingTables;
	}

	/**
	 * Returns all DB Object names from the comma separated list.
	 * This is different to {@link StringUtil#stringToList(String)} as it keeps any quotes that
	 * are present in the list.
	 *
	 * @param list a comma separated list of elements (optionally with quotes)
	 * @return a List of Strings as defined by the input string
	 * @see StringUtil#stringToList(java.lang.String)
	 */
	List<String> getObjectNames(String list)
	{
		if (StringUtil.isEmptyString(list)) return Collections.emptyList();
		WbStringTokenizer tok = new WbStringTokenizer(list, ",");
		tok.setDelimiterNeedsWhitspace(false);
		tok.setCheckBrackets(false);
		tok.setKeepQuotes(true);
		List<String> result = new LinkedList<String>();
		while (tok.hasMoreTokens())
		{
			String element = tok.nextToken();
			if (element == null) continue;
			element = element.trim();
			if (element.length() > 0)
			{
				result.add(element);
			}
		}
		return result;
	}

	public List<TableIdentifier> getTables()
	{
		return this.tables;
	}

	public boolean wasWildCardArgument()
	{
		return this.wildcardsPresent;
	}
}
