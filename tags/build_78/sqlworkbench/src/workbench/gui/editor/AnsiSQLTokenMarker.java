package workbench.gui.editor;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.StringTokenizer;

import workbench.log.LogMgr;

/**
 * ANSI-SQL token marker.
 *
 */
public class AnsiSQLTokenMarker extends SQLTokenMarker
{
	// public members
	public AnsiSQLTokenMarker()
	{
		super(getKeywordMap());
	}

	public void initDatabaseKeywords(Connection aConnection)
	{
		try
		{
			DatabaseMetaData meta = aConnection.getMetaData();
			String product = meta.getDatabaseProductName();
			this.isMySql = (product.toLowerCase().indexOf("mysql") > -1);
			String keys = null;

			keys = meta.getSQLKeywords();
			this.addKeywordList(keys, Token.KEYWORD1);
			//System.out.println("keys=" + keys);

			if (meta.getDatabaseProductName().toLowerCase().indexOf("oracle") > -1)
			{
				keywords.add("START", Token.KEYWORD1);
				keywords.add("SYNONYM", Token.KEYWORD1);
				keywords.add("WHILE", Token.KEYWORD1);
				keywords.add("LOOP", Token.KEYWORD1);
				keywords.add("ELSIF", Token.KEYWORD1);
				keywords.add("REFRESH", Token.KEYWORD1);
				keywords.add("NCHAR", Token.KEYWORD1);
				keywords.add("NVARCHAR", Token.KEYWORD1);
				keywords.add("CALL", Token.KEYWORD1);

				keywords.add("SNAPSHOT", Token.KEYWORD1);

				keywords.add("SYSDATE", Token.KEYWORD3);
				keywords.add("INSTR", Token.KEYWORD3);
				keywords.add("SUBSTR", Token.KEYWORD3);
				keywords.add("INSTRB", Token.KEYWORD3);
				keywords.add("SUBSTRB", Token.KEYWORD3);
				keywords.add("LPAD", Token.KEYWORD3);
				keywords.add("RPAD", Token.KEYWORD3);
				keywords.add("CONTINUE", Token.KEYWORD3);
				keywords.add("MODIFY", Token.KEYWORD3);
			}

			keys = meta.getStringFunctions();
			//System.out.println("string funcs=" + keys);
			this.addKeywordList(keys, Token.KEYWORD3);

			keys = meta.getNumericFunctions();
			//System.out.println("num funcs=" + keys);
			this.addKeywordList(keys, Token.KEYWORD3);

			keys = meta.getTimeDateFunctions();
			//System.out.println("date func=" + keys);
			this.addKeywordList(keys, Token.KEYWORD3);

			keys = meta.getSystemFunctions();
			//System.out.println("sys funcs=" + keys);
			this.addKeywordList(keys, Token.KEYWORD3);

		}
		catch (Exception e)
		{
			LogMgr.logWarning(this, "Could not read database keywords", e);
		}
	}
	private void addKeywordList(String aList, byte anId)
	{
		if (aList == null) return;
		StringTokenizer tok = new StringTokenizer(aList, ",");
		while (tok.hasMoreTokens())
		{
			String keyword = tok.nextToken();
			if (!keywords.containsKey(keyword))
			{
				//System.out.println("adding key=" + keyword);
				keywords.add(keyword.toUpperCase().trim(),anId);
			}
		}
	}

	public static KeywordMap getKeywordMap()
	{
		if (keywords == null)
		{
			keywords = new KeywordMap(true, 80);
			addKeywords();
			addDataTypes();
			addSystemFunctions();
			addOperators();
			addSystemStoredProcedures();
			addSystemTables();
		}
		return keywords;
	}

	private static void addKeywords()
	{
		keywords.add("AVG",Token.KEYWORD1);
		keywords.add("ADD",Token.KEYWORD1);
		keywords.add("ALTER",Token.KEYWORD1);
		keywords.add("AS",Token.KEYWORD1);
		keywords.add("ASC",Token.KEYWORD1);
		keywords.add("BEGIN",Token.KEYWORD1);
		keywords.add("BREAK",Token.KEYWORD1);
		keywords.add("BY",Token.KEYWORD1);
		keywords.add("CASE",Token.KEYWORD1);
		keywords.add("CASCADE",Token.KEYWORD1);
		keywords.add("CHECK",Token.KEYWORD1);
		keywords.add("CHECKPOINT",Token.KEYWORD1);
		keywords.add("CLOSE",Token.KEYWORD1);
		keywords.add("CLUSTERED",Token.KEYWORD1);
		keywords.add("COLUMN",Token.KEYWORD1);
		keywords.add("COMMIT",Token.KEYWORD1);
		keywords.add("CONSTRAINT",Token.KEYWORD1);
		keywords.add("CREATE",Token.KEYWORD1);
		keywords.add("CURRENT",Token.KEYWORD1);
		keywords.add("CURRENT_DATE",Token.KEYWORD1);
		keywords.add("CURRENT_TIME",Token.KEYWORD1);
		keywords.add("CURSOR",Token.KEYWORD1);
		keywords.add("DATABASE",Token.KEYWORD1);
		keywords.add("DECLARE",Token.KEYWORD1);
		keywords.add("DEFAULT",Token.KEYWORD1);
		keywords.add("DELETE",Token.KEYWORD1);
		keywords.add("DENY",Token.KEYWORD1);
		keywords.add("DISTINCT",Token.KEYWORD1);
		keywords.add("DROP",Token.KEYWORD1);
		keywords.add("EXEC",Token.KEYWORD1);
		keywords.add("EXECUTE",Token.KEYWORD1);
		keywords.add("EXIT",Token.KEYWORD1);
		keywords.add("END",Token.KEYWORD1);
		keywords.add("ELSE",Token.KEYWORD1);
		keywords.add("FETCH",Token.KEYWORD1);
		keywords.add("FOR",Token.KEYWORD1);
		keywords.add("FOREIGN",Token.KEYWORD1);
		keywords.add("FROM",Token.KEYWORD1);
		keywords.add("GRANT",Token.KEYWORD1);
		keywords.add("GROUP",Token.KEYWORD1);
		keywords.add("HAVING",Token.KEYWORD1);
		keywords.add("IF",Token.KEYWORD1);
		keywords.add("INDEX",Token.KEYWORD1);
		keywords.add("INNER",Token.KEYWORD1);
		keywords.add("INSERT",Token.KEYWORD1);
		keywords.add("INTO",Token.KEYWORD1);
		keywords.add("IS",Token.KEYWORD1);
		keywords.add("ISOLATION",Token.KEYWORD1);
		keywords.add("KEY",Token.KEYWORD1);
		keywords.add("LEVEL",Token.KEYWORD1);
		keywords.add("MAX",Token.KEYWORD1);
		keywords.add("MIN",Token.KEYWORD1);
		keywords.add("MIRROREXIT",Token.KEYWORD1);
		keywords.add("NATIONAL",Token.KEYWORD1);
		keywords.add("NOCHECK",Token.KEYWORD1);
		keywords.add("OF",Token.KEYWORD1);
		keywords.add("ON",Token.KEYWORD1);
		keywords.add("ORDER",Token.KEYWORD1);
		keywords.add("PREPARE",Token.KEYWORD1);
		keywords.add("PRIMARY",Token.KEYWORD1);
		keywords.add("PRIVILEGES",Token.KEYWORD1);
		keywords.add("PROCEDURE",Token.KEYWORD1);
		keywords.add("FUNCTION",Token.KEYWORD1);
		keywords.add("PACKAGE",Token.KEYWORD1);
		keywords.add("BODY",Token.KEYWORD1);
		keywords.add("REFERENCES",Token.KEYWORD1);
		keywords.add("RESTORE",Token.KEYWORD1);
		keywords.add("RESTRICT",Token.KEYWORD1);
		keywords.add("REVOKE",Token.KEYWORD1);
		keywords.add("ROLLBACK",Token.KEYWORD1);
		keywords.add("SCHEMA",Token.KEYWORD1);
		keywords.add("SELECT",Token.KEYWORD1);
		keywords.add("SET",Token.KEYWORD1);
		keywords.add("TABLE",Token.KEYWORD1);
		keywords.add("TO",Token.KEYWORD1);
		keywords.add("TRANSACTION",Token.KEYWORD1);
		keywords.add("TRIGGER",Token.KEYWORD1);
		keywords.add("TRUNCATE",Token.KEYWORD1);
		keywords.add("UNION",Token.KEYWORD1);
		keywords.add("UNIQUE",Token.KEYWORD1);
		keywords.add("UPDATE",Token.KEYWORD1);
		keywords.add("VALUES",Token.KEYWORD1);
		keywords.add("VARYING",Token.KEYWORD1);
		keywords.add("VIEW",Token.KEYWORD1);
		keywords.add("WHERE",Token.KEYWORD1);
		keywords.add("WHEN",Token.KEYWORD1);
		keywords.add("WITH",Token.KEYWORD1);
		keywords.add("WORK",Token.KEYWORD1);

		// Workbench specific keywords
		keywords.add("DESC",Token.KEYWORD2);
		keywords.add("DESCRIBE",Token.KEYWORD2);
		keywords.add("XSLT",Token.KEYWORD2);
		keywords.add("WBEXPORT",Token.KEYWORD2);
		keywords.add("LIST",Token.KEYWORD2);
		keywords.add("LISTPROCS",Token.KEYWORD2);
		keywords.add("LISTDB",Token.KEYWORD2);
		keywords.add("ENABLEOUT",Token.KEYWORD2);
		keywords.add("DISABLEOUT",Token.KEYWORD2);
		keywords.add("WBIMPORT",Token.KEYWORD2);
		keywords.add("WBFEEDBACK",Token.KEYWORD2);
		keywords.add("WBINCLUDE",Token.KEYWORD2);
		keywords.add("WBCOPY",Token.KEYWORD2);
		keywords.add("WBVARDEF",Token.KEYWORD2);
		keywords.add("WBVARDEFINE",Token.KEYWORD2);
		keywords.add("WBVARLIST",Token.KEYWORD2);
		keywords.add("WBVARDELETE",Token.KEYWORD2);
		keywords.add("WBREPORT",Token.KEYWORD2);
		keywords.add("WBSTARTBATCH",Token.KEYWORD2);
		keywords.add("WBENDBATCH",Token.KEYWORD2);
		keywords.add("WBFEEDBACK",Token.KEYWORD2);
		keywords.add("WBDIFF",Token.KEYWORD2);
	}

	private static void addDataTypes()
	{
		keywords.add("binary",Token.KEYWORD1);
		keywords.add("bit",Token.KEYWORD1);
		keywords.add("char",Token.KEYWORD1);
		keywords.add("character",Token.KEYWORD1);
		keywords.add("datetime",Token.KEYWORD1);
		keywords.add("date",Token.KEYWORD1);
		keywords.add("decimal",Token.KEYWORD1);
		keywords.add("float",Token.KEYWORD1);
		keywords.add("image",Token.KEYWORD1);
		keywords.add("int",Token.KEYWORD1);
		keywords.add("integer",Token.KEYWORD1);
		keywords.add("money",Token.KEYWORD1);
		//keywords.add("name",Token.KEYWORD1);
		keywords.add("number",Token.KEYWORD1);
		keywords.add("numeric",Token.KEYWORD1);
		keywords.add("nchar",Token.KEYWORD1);
		keywords.add("nvarchar",Token.KEYWORD1);
		keywords.add("ntext",Token.KEYWORD1);
		keywords.add("real",Token.KEYWORD1);
		keywords.add("smalldatetime",Token.KEYWORD1);
		keywords.add("smallint",Token.KEYWORD1);
		keywords.add("smallmoney",Token.KEYWORD1);
		keywords.add("text",Token.KEYWORD1);
		keywords.add("timestamp",Token.KEYWORD1);
		keywords.add("tinyint",Token.KEYWORD1);
		keywords.add("uniqueidentifier",Token.KEYWORD1);
		keywords.add("varbinary",Token.KEYWORD1);
		keywords.add("varchar",Token.KEYWORD1);
		keywords.add("nvarchar",Token.KEYWORD1);
		keywords.add("varchar2",Token.KEYWORD1);
		keywords.add("nvarchar2",Token.KEYWORD1);
		keywords.add("clob",Token.KEYWORD1);
		keywords.add("nclob",Token.KEYWORD1);
	}

	private static void addSystemFunctions()
	{
		keywords.add("ABS",Token.KEYWORD3);
		keywords.add("ACOS",Token.KEYWORD3);
		keywords.add("ASIN",Token.KEYWORD3);
		keywords.add("ATAN",Token.KEYWORD3);
		keywords.add("ATN2",Token.KEYWORD3);
		keywords.add("CAST",Token.KEYWORD3);
		keywords.add("CEILING",Token.KEYWORD3);
		keywords.add("COS",Token.KEYWORD3);
		keywords.add("COT",Token.KEYWORD3);
		keywords.add("COUNT", Token.KEYWORD3);
		keywords.add("CURRENT_TIME",Token.KEYWORD3);
		keywords.add("CURRENT_DATE",Token.KEYWORD3);
		keywords.add("CURRENT_TIMESTAMP",Token.KEYWORD3);
		keywords.add("CURRENT_USER",Token.KEYWORD3);
		keywords.add("DATALENGTH",Token.KEYWORD3);
		keywords.add("DATEADD",Token.KEYWORD3);
		keywords.add("DATEDIFF",Token.KEYWORD3);
		keywords.add("DATENAME",Token.KEYWORD3);
		keywords.add("DATEPART",Token.KEYWORD3);
		keywords.add("DAY",Token.KEYWORD3);
		keywords.add("EXP",Token.KEYWORD3);
		keywords.add("FLOOR",Token.KEYWORD3);
		keywords.add("LOG",Token.KEYWORD3);
		keywords.add("MONTH",Token.KEYWORD3);
		keywords.add("RIGHT",Token.KEYWORD3);
		keywords.add("ROUND",Token.KEYWORD3);
		keywords.add("SIN",Token.KEYWORD3);
		keywords.add("SOUNDEX",Token.KEYWORD3);
		keywords.add("SPACE",Token.KEYWORD3);
		keywords.add("SQRT",Token.KEYWORD3);
		keywords.add("SQUARE",Token.KEYWORD3);
		keywords.add("TAN",Token.KEYWORD3);
		keywords.add("UPPER",Token.KEYWORD3);
		keywords.add("USER",Token.KEYWORD3);
		keywords.add("YEAR",Token.KEYWORD3);
	}

	private static void addOperators()
	{
		keywords.add("ALL",Token.KEYWORD1);
		keywords.add("AND",Token.KEYWORD1);
		keywords.add("ANY",Token.KEYWORD1);
		keywords.add("BETWEEN",Token.KEYWORD1);
		keywords.add("CROSS",Token.KEYWORD1);
		keywords.add("EXISTS",Token.KEYWORD1);
		keywords.add("IN",Token.KEYWORD1);
		keywords.add("INTERSECT",Token.KEYWORD1);
		keywords.add("JOIN",Token.KEYWORD1);
		keywords.add("LIKE",Token.KEYWORD1);
		keywords.add("NOT",Token.KEYWORD1);
		keywords.add("NULL",Token.KEYWORD1);
		keywords.add("OR",Token.KEYWORD1);
		keywords.add("OUTER",Token.KEYWORD1);
		keywords.add("SOME",Token.KEYWORD1);
	}

	private static void addSystemStoredProcedures()
	{
	}

	private static void addSystemTables()
	{
	}

	private static KeywordMap keywords;
}