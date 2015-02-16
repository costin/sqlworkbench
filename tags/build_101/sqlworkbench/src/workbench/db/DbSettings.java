/*
 * DbSettings.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2008, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db;

import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import workbench.log.LogMgr;
import workbench.resource.Settings;
import workbench.util.CaseInsensitiveComparator;
import workbench.util.StringUtil;

/**
 * Stores and manages db specific settings.
 * 
 * @author support@sql-workbench.net
 */
public class DbSettings
{
	private final String dbId;
	private boolean caseSensitive;
	private boolean useJdbcCommit;
	private boolean ddlNeedsCommit;
	private boolean trimDefaults = true;
	
	private boolean neverQuoteObjects;
	private boolean reportsRealSizeAsDisplaySize = false;
	private boolean allowExtendedCreateStatement = true;
	
	private boolean allowsMultipleGetUpdateCounts = true;
	private boolean supportsBatchedStatements = false;
	
	private Map<Integer, String> indexTypeMapping;
	public static final String IDX_TYPE_NORMAL = "NORMAL";
	private Set<String> updatingCommands;
	private final String prefix;

	public DbSettings(String id, String productName)
	{
		this.dbId = id;
		prefix = "workbench.db." + id + ".";
		Settings settings = Settings.getInstance();
		
		this.caseSensitive = settings.getBoolProperty(prefix + "casesensitive", false) || settings.getCaseSensitivServers().contains(productName);
		this.useJdbcCommit = settings.getBoolProperty(prefix + "usejdbccommit", false) || settings.getServersWhichNeedJdbcCommit().contains(productName);
		this.ddlNeedsCommit = settings.getBoolProperty(prefix + "ddlneedscommit", false) || settings.getServersWhereDDLNeedsCommit().contains(productName);
		
		// Migrate old list-based properties to new dbid based properties
		// If the flags were already set with the new format, re-applying the 
		// value won't change anything
		if (caseSensitive )
		{
			settings.removeCaseSensitivServer(productName);
			settings.setProperty(prefix + "casesensitive", true);
		}
		if (ddlNeedsCommit)
		{
			settings.removeDDLCommitServer(productName);
			settings.setProperty(prefix + "ddlneedscommit", true);
		}
		if (useJdbcCommit)
		{
			settings.removeJdbcCommitServer(productName);
			settings.setProperty(prefix + "usejdbccommit", true);
		}
		
		List<String> quote = StringUtil.stringToList(settings.getProperty("workbench.db.neverquote",""));
		this.neverQuoteObjects = quote.contains(this.getDbId());
		this.trimDefaults = settings.getBoolProperty(prefix + "trimdefaults", true);
		this.allowsMultipleGetUpdateCounts = settings.getBoolProperty(prefix + "multipleupdatecounts", true);
		this.reportsRealSizeAsDisplaySize = settings.getBoolProperty(prefix + "charsize.usedisplaysize", false);
		this.allowExtendedCreateStatement = settings.getBoolProperty(prefix + "extended.createstmt", true);
		this.supportsBatchedStatements = settings.getBoolProperty(prefix + "batchedstatements", false);
	}
	
	String getDbId() 
	{ 
		return this.dbId; 
	}

	public boolean isUpdatingCommand(String verb)
	{
		if (StringUtil.isEmptyString(verb)) return false;
		if (this.updatingCommands == null)
		{
			this.updatingCommands = new TreeSet<String>(new CaseInsensitiveComparator());
			
			String l = Settings.getInstance().getProperty("workbench.db.updatingcommands", null);
			List<String> commands = StringUtil.stringToList(l, ",", true, true);
			updatingCommands.addAll(commands);
			l = Settings.getInstance().getProperty(prefix + "updatingcommands", null);
			commands = StringUtil.stringToList(l, ",", true, true);
			updatingCommands.addAll(commands);
		}
		return updatingCommands.contains(verb);
	}
	
	public boolean longVarcharIsClob()
	{
		return Settings.getInstance().getBoolProperty(prefix + "clob.longvarchar", true);
	}

	public boolean supportsBatchedStatements()
	{
		return this.supportsBatchedStatements;
	}

	public boolean allowsExtendedCreateStatement()
	{
		return allowExtendedCreateStatement;
	}

	public boolean allowsMultipleGetUpdateCounts()
	{
		return this.allowsMultipleGetUpdateCounts;
	}

	public boolean reportsRealSizeAsDisplaySize()
	{
		return this.reportsRealSizeAsDisplaySize;
	}

	public boolean ddlNeedsCommit()
	{
		return ddlNeedsCommit;
	}

	public boolean neverQuoteObjects()
	{
		return neverQuoteObjects;
	}

	public boolean trimDefaults()
	{
		return trimDefaults;
	}

	public boolean useSetNull()
	{
		return Settings.getInstance().getBoolProperty(prefix + "import.use.setnull", false);
	}
	
	public boolean useJdbcCommit()
	{
		return useJdbcCommit;
	}

	public boolean isStringComparisonCaseSensitive()
	{
		return this.caseSensitive;
	}

	public boolean getDefaultBeforeNull()
	{
		return Settings.getInstance().getBoolProperty("workbench.db.defaultbeforenull." + this.getDbId(), false);
	}
	
	/**
	 *	Return the verb which does a DROP ... CASCADE for the given
	 *  object type. If the current DBMS does not support cascaded dropping
	 *  of objects, then null will be returned.
	 *
	 *	@param aType the database object type to drop (TABLE, VIEW etc)
	 *  @return a String which can be appended to a DROP type name command in order to drop dependent objects as well
	 *          or null if the current DBMS does not support this.
	 */
	public String getCascadeConstraintsVerb(String aType)
	{
		if (aType == null) return null;
		String verb = Settings.getInstance().getProperty("workbench.db.drop." + aType.toLowerCase() + ".cascade." + getDbId(), null);
		return verb;
	}
	
	public boolean needsCatalogIfNoCurrent()
	{
		return Settings.getInstance().getBoolProperty("workbench.db." + this.getDbId() + ".catalog.neededwhenempty", false);
	}
	
	public String getInsertForImport()
	{
		return Settings.getInstance().getProperty("workbench.db." + this.getDbId() + ".import.insert", null);
	}
	
	public String getRefCursorTypeName()
	{
		return Settings.getInstance().getProperty(prefix + "refcursor.typename", null);
	}

	public int getRefCursorDataType()
	{
		return Settings.getInstance().getIntProperty(prefix + "refcursor.typevalue", Integer.MIN_VALUE);
	}
	
	public boolean useWbProcedureCall()
	{
		return Settings.getInstance().getBoolProperty(prefix + "procs.use.wbcall", false);
	}
	
	public boolean needsTableForDropIndex()
	{
		boolean needsTable = Settings.getInstance().getBoolProperty(prefix + "dropindex.needstable", false);
		return needsTable;
	}	

	public boolean useSavepointForImport()
	{
		return Settings.getInstance().getBoolProperty("workbench.db." + this.getDbId() + ".import.usesavepoint", false);
	}

	public boolean useSavePointForDML()
	{
		return Settings.getInstance().getBoolProperty(prefix + "sql.usesavepoint", false);
	}
	
	public boolean useSavePointForDDL()
	{
		return Settings.getInstance().getBoolProperty(prefix + "ddl.usesavepoint", false);
	}

	/**
	 * Returns the type for the formatter 
	 * @return hex, octal, char
	 */
	public String getBlobLiteralType()
	{
		return Settings.getInstance().getProperty(prefix + "blob.literal.type", "hex");
	}
	
	public String getBlobLiteralPrefix()
	{
		return Settings.getInstance().getProperty(prefix + "blob.literal.prefix", null);
	}
	
	public String getBlobLiteralSuffix()
	{
		return Settings.getInstance().getProperty(prefix + "blob.literal.suffix", null);
	}

	public boolean getBlobLiteralUpperCase()
	{
		return Settings.getInstance().getBoolProperty(prefix + "blob.literal.upcase", false);
	}
	
	public boolean supportSingleLineCommands()
	{
		String ids = Settings.getInstance().getProperty("workbench.db.checksinglelinecmd", "");
		if ("*".equals(ids)) return true;
		List dbs = StringUtil.stringToList(ids, ",", true, true);
		return dbs.contains(this.getDbId());
	}

	public String getLineComment()
	{
		return Settings.getInstance().getProperty(prefix + "linecomment", null);
	}

	public boolean supportsQueryTimeout()
	{
		boolean result = Settings.getInstance().getBoolProperty(prefix + "supportquerytimeout", true);
		return result;
	}
	
	public boolean supportsGetPrimaryKeys()
	{
		boolean result = Settings.getInstance().getBoolProperty(prefix + "supportgetpk", true);
		return result;
	}
	
	public boolean supportShortInclude()
	{
		String ids = Settings.getInstance().getProperty("workbench.db.supportshortinclude", "");
		if ("*".equals(ids)) return true;
		List dbs = StringUtil.stringToList(ids, ",", true, true);
		return dbs.contains(this.getDbId());
	}

	public String getProcVersionDelimiter()
	{
		return Settings.getInstance().getProperty("workbench.db.procversiondelimiter." + this.getDbId(), null);
	}

	public boolean supportsTruncate()
	{
		String s = Settings.getInstance().getProperty("workbench.db.truncatesupported", StringUtil.EMPTY_STRING);
		List l = StringUtil.stringToList(s, ",");
		return l.contains(this.getDbId());
	}

	public boolean isViewType(String type)
	{
		if (type == null) return false;
		type = type.toLowerCase();
		if (type.toUpperCase().indexOf("VIEW") > -1) return true;
		String viewTypes = Settings.getInstance().getProperty(prefix + "additional.viewtypes", "view").toLowerCase();
		List types = StringUtil.stringToList(viewTypes, ",", true, true, false);
		return (types.contains(type.toLowerCase()));
	}
	
	public boolean isSynonymType(String type)
	{
		if (type == null) return false;
		String synTypes = Settings.getInstance().getProperty(prefix + "synonymtypes", "synonym").toLowerCase();
		List types = StringUtil.stringToList(synTypes, ",", true, true, false);
		return (types.contains(type.toLowerCase()));
	}
	
	String mapIndexType(Object type)
	{
		if (type == null) return null;
		if (type instanceof String) 
		{
			int t = StringUtil.getIntValue((String)type, Integer.MIN_VALUE);
			if (t == Integer.MIN_VALUE) return (String)type;
			return mapIndexType(t);
		}
		if (type instanceof Number)
		{
			return mapIndexType(((Number)type).intValue());
		}
		return null;
	}
	
	String mapIndexType(int type)
	{
		if (indexTypeMapping == null)
		{
			this.indexTypeMapping = new HashMap<Integer, String>();
			String map = Settings.getInstance().getProperty(prefix + "indextypes", null);
			if (map != null)
			{
				List<String> entries = StringUtil.stringToList(map, ";", true, true);
				for (String entry : entries)
				{
					String[] mapping = entry.split(",");
					if (mapping.length != 2) continue;
					int value = StringUtil.getIntValue(mapping[0], Integer.MIN_VALUE);
					if (value != Integer.MIN_VALUE)
					{
						indexTypeMapping.put(new Integer(value), mapping[1]);
					}
				}
			}
		}
		String dbmsType = this.indexTypeMapping.get(new Integer(type));
		if (dbmsType == null) 
		{
			if (Settings.getInstance().getDebugMetadataSql())
			{
				LogMgr.logDebug("DbSettings.mapIndexType()", "No mapping for type = " + type);
			}
			return IDX_TYPE_NORMAL;
		}
		return dbmsType;
	}	
	
	public boolean proceduresNeedTerminator()
	{
		String value = Settings.getInstance().getProperty("workbench.db.noprocterminator", null);
		if (value == null) return true;
		List l = StringUtil.stringToList(value, ",");
		return !l.contains(this.dbId);
	}	

	public IdentifierCase getSchemaNameCase()
	{
		// This allows overriding the default value returned by the JDBC driver
		String nameCase = Settings.getInstance().getProperty("workbench.db."  + this.getDbId() + ".schemaname.case", null);
		if (nameCase != null)
		{
			if ("lower".equals(nameCase))
			{
				return IdentifierCase.lower;
			}
			else if ("upper".equals(nameCase))
			{
				return IdentifierCase.upper;
			}
			else if ("mixed".equals(nameCase))
			{
				return IdentifierCase.mixed;
			}
		}
		return IdentifierCase.unknown;
	}
	
	public IdentifierCase getObjectNameCase()
	{
		// This allows overriding the default value returned by the JDBC driver
		String nameCase = Settings.getInstance().getProperty("workbench.db."  + this.getDbId() + ".objectname.case", null);
		if (nameCase != null)
		{
			if ("lower".equals(nameCase))
			{
				return IdentifierCase.lower;
			}
			else if ("upper".equals(nameCase))
			{
				return IdentifierCase.upper;
			}
			else if ("mixed".equals(nameCase))
			{
				return IdentifierCase.mixed;
			}
		}
		return IdentifierCase.unknown;
	}

	/**
	 *	Translates the numberic constants of DatabaseMetaData for trigger rules
	 *	into text (e.g DatabaseMetaData.importedKeyNoAction --> NO ACTION)
	 *
	 *	@param code the numeric value for a rule as defined by DatabaseMetaData.importedKeyXXXX constants
	 *	@return String
	 */
	public String getRuleDisplay(int code)
	{
		StringBuilder key = new StringBuilder(40);
		switch (code)
		{
			case DatabaseMetaData.importedKeyNoAction:
				key.append("workbench.sql.fkrule.noaction");
				break;
			case DatabaseMetaData.importedKeyRestrict:
				key.append("workbench.sql.fkrule.restrict");
				break;
			case DatabaseMetaData.importedKeySetNull:
				key.append("workbench.sql.fkrule.setnull");
				break;
			case DatabaseMetaData.importedKeyCascade:
				key.append("workbench.sql.fkrule.cascade");
				break;
			case DatabaseMetaData.importedKeySetDefault:
				key.append("workbench.sql.fkrule.setdefault");
				break;
			case DatabaseMetaData.importedKeyInitiallyDeferred:
				key.append("workbench.sql.fkrule.initiallydeferred");
				break;
			case DatabaseMetaData.importedKeyInitiallyImmediate:
				key.append("workbench.sql.fkrule.initiallyimmediate");
				break;
			case DatabaseMetaData.importedKeyNotDeferrable:
				key.append("workbench.sql.fkrule.notdeferrable");
				break;
			default:
				key = null;
		}
		if (key != null)
		{
			key.append('.');
			key.append(this.getDbId());
			String display = Settings.getInstance().getProperty(key.toString(), null);
			if (display != null) return display;
		}
		switch (code)
		{
			case DatabaseMetaData.importedKeyNoAction:
				return "NO ACTION";
			case DatabaseMetaData.importedKeyRestrict:
				return "RESTRICT";
			case DatabaseMetaData.importedKeySetNull:
				return "SET NULL";
			case DatabaseMetaData.importedKeyCascade:
				return "CASCADE";
			case DatabaseMetaData.importedKeySetDefault:
				return "SET DEFAULT";
			case DatabaseMetaData.importedKeyInitiallyDeferred:
				return "INITIALLY DEFERRED";
			case DatabaseMetaData.importedKeyInitiallyImmediate:
				return "INITIALLY IMMEDIATE";
			case DatabaseMetaData.importedKeyNotDeferrable:
				return "NOT DEFERRABLE";
			default:
				return StringUtil.EMPTY_STRING;
		}
	}
	
	public boolean useSetCatalog()
	{
		return Settings.getInstance().getBoolProperty("workbench.db." + this.getDbId() + ".usesetcatalog", true);
	}
	
	public boolean isNotDeferrable(String deferrable)
	{
		if (StringUtil.isEmptyString(deferrable)) return true;
		return (deferrable.equals(getRuleDisplay(DatabaseMetaData.importedKeyNotDeferrable)));
	}

	/**
	 * Retrieve the list of datatypes that should be ignored for the current 
	 * dbms. The names in that list must match the names returned   
	 * by DatabaseMetaData.getTypeInfo() 
	 */
	public List<String> getDataTypesToIgnore()
	{
		String types = Settings.getInstance().getProperty("workbench.ignoretypes." + getDbId(), null);
		List<String> ignored = StringUtil.stringToList(types, ",", true, true);
		return ignored;
	}
	
	public String getQueryForCurrentCatalog()
	{
		String query = Settings.getInstance().getProperty("workbench.db." + this.getDbId() + ".currentcatalog.query", null);
		return query;
	}

	public boolean getConvertDateInExport()
	{
		return Settings.getInstance().getBoolProperty("workbench.db." + this.getDbId() + ".export.convert.date2ts", false);
	}
	
	public boolean needsExactClobLength()
	{
		return Settings.getInstance().getBoolProperty("workbench.db." + this.getDbId() + ".exactcloblength", false);
	}
	
	public boolean getFormatViewSource()
	{
		return Settings.getInstance().getBoolProperty("workbench.db." + this.getDbId() + ".source.view.doformat", false);
	}
	
	public String getDropSingleColumnSql()
	{
		return Settings.getInstance().getProperty("workbench.db." + this.getDbId() + ".drop.column", null);
	}

	public String getDropMultipleColumnSql()
	{
		return Settings.getInstance().getProperty("workbench.db." + this.getDbId() + ".drop.column.multi", null);
	}

	public boolean supportsSortedIndex()
	{
		return Settings.getInstance().getBoolProperty("workbench.db." + this.getDbId() + ".index.sorted", true);
	}

	public boolean includeSystemTablesInSelectable()
	{
		return Settings.getInstance().getBoolProperty("workbench.db." + this.getDbId() + ".systemtables.selectable", false);
	}
	
//	public boolean preferParametersFromStatement()
//	{
//		return Settings.getInstance().getBoolProperty("workbench.db." + this.getDbId() + ".call.parameters.fromstatement", true);
//	}
//
//	public boolean callStatementsNeedInput()
//	{
//		return Settings.getInstance().getBoolProperty("workbench.db." + this.getDbId() + ".call.parameters.prompt", true);
//	}
	
	public boolean canDropType(String type)
	{
		if (StringUtil.isEmptyString(type)) return false;
		if (type.equalsIgnoreCase("column"))
		{
			return getDropSingleColumnSql() != null;
		}
		return true;
	}
}