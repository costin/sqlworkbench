/*
 * SqlFormatter.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2005, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.sql.formatter;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import workbench.sql.wbcommands.CommandTester;
import workbench.util.StringUtil;

/**
 *
 * @author  thomas
 */
public class SqlFormatter
{
	private final Set LINE_BREAK_BEFORE = new HashSet();
	{
		LINE_BREAK_BEFORE.add("SELECT");
		LINE_BREAK_BEFORE.add("SET");
		LINE_BREAK_BEFORE.add("FROM");
		LINE_BREAK_BEFORE.add("WHERE");
		LINE_BREAK_BEFORE.add("ORDER");
		LINE_BREAK_BEFORE.add("GROUP");
		LINE_BREAK_BEFORE.add("HAVING");
		LINE_BREAK_BEFORE.add("VALUES");
		LINE_BREAK_BEFORE.add("UNION");
		LINE_BREAK_BEFORE.add("MINUS");
		LINE_BREAK_BEFORE.add("INTERSECT");
		LINE_BREAK_BEFORE.add("REFRESH");
		LINE_BREAK_BEFORE.add("AS");
		LINE_BREAK_BEFORE.add("FOR");
	}

	private final Set LINE_BREAK_AFTER = new HashSet();
	{
		//LINE_BREAK_AFTER.add("UNION");
		//LINE_BREAK_AFTER.add("MINUS");
		//LINE_BREAK_AFTER.add("INTERSECT");
		LINE_BREAK_AFTER.add("AS");
		LINE_BREAK_AFTER.add("FOR");
		LINE_BREAK_AFTER.add("JOIN");
	}

	private final Set SUBSELECT_START = new HashSet();
	{
		SUBSELECT_START.add("IN");
		SUBSELECT_START.add("EXISTS");
	}

	// keywords terminating a WHERE clause
	private final Set WHERE_TERMINAL = new HashSet();
	{
		WHERE_TERMINAL.add("ORDER");
		WHERE_TERMINAL.add("GROUP");
		WHERE_TERMINAL.add("HAVING");
		WHERE_TERMINAL.add("UNION");
		WHERE_TERMINAL.add("INTERSECT");
		WHERE_TERMINAL.add("MINUS");
		WHERE_TERMINAL.add(";");
	}

	// keywords terminating the FROM part
	private final Set FROM_TERMINAL = new HashSet();
	{
		FROM_TERMINAL.addAll(WHERE_TERMINAL);
		FROM_TERMINAL.add("WHERE");
		FROM_TERMINAL.add("MINUS");
		FROM_TERMINAL.add("INTERSECT");
	}


	// keywords terminating an GROUP BY clause
	private final Set BY_TERMINAL = new HashSet();
	{
		BY_TERMINAL.addAll(WHERE_TERMINAL);
		BY_TERMINAL.add("SELECT");
		BY_TERMINAL.add("UPDATE");
		BY_TERMINAL.add("DELETE");
		BY_TERMINAL.add("INSERT");
		BY_TERMINAL.add("CREATE");
		BY_TERMINAL.add("GROUP");
		BY_TERMINAL.add(";");
	}

	private final Set SELECT_TERMINAL = new HashSet(1);
	{
		SELECT_TERMINAL.add("FROM");
	}

	private final Set SET_TERMINAL = new HashSet();
	{
		SET_TERMINAL.add("FROM");
		SET_TERMINAL.add("WHERE");
	}

	private String sql;
	private SQLLexer lexer;
	private StringBuffer result;
	private StringBuffer indent = null;
	private int realLength = 0;
	private int maxSubselectLength = 60;
	private Set dbFunctions = Collections.EMPTY_SET;
	
	public SqlFormatter(String aScript, int maxLength)
	{
		this(aScript, 0, maxLength);
	}

	private SqlFormatter(String aScript, int indentCount, int maxSubselectLength)
	{
		this.sql = aScript;
		Reader in = new StringReader(this.sql);
		this.lexer = new SQLLexer(in);
		this.result = new StringBuffer(this.sql.length() + 100);
		if (indentCount > 0)
		{
			this.indent = new StringBuffer(indentCount);
			for (int i=0; i < indentCount; i++) this.indent.append(' ');
		}
		this.maxSubselectLength = maxSubselectLength;
	}

	public void setDBFunctions(Set functionNames)
	{
		if (functionNames != null)
			this.dbFunctions = functionNames;
		else
			this.dbFunctions = Collections.EMPTY_SET;
	}
	
	public String getFormattedSql()
		throws Exception
	{
		this.formatSql();
		return this.result.toString();
	}

	private int getRealLength()
	{
		return this.realLength;
	}

	public void setMaxSubSelectLength(int max)
	{
		this.maxSubselectLength = max;
	}

	private int getCurrentLineLength()
	{
		int c = this.result.length() - 1;
		int pos = 0;
		while (this.result.charAt(c) != '\n' && c > 0)
		{
			pos ++;
			c --;
		}
		return pos;
	}

	private int indentNewline()
	{
		int pos = this.getCurrentLineLength();
		this.indentNewline(pos);
		return pos;
	}

	private void indentNewline(int pos)
	{
		this.appendNewline();
		for (int i = 0; i < pos; i ++) this.result.append(' ');
	}

	private void appendNewline()
	{
		if (this.result.length() == 0) return;
		this.result.append('\n');
		if (this.indent != null) this.result.append(indent);
	}


	private boolean lastCharIsWhitespace()
	{
		int len = this.result.length();
		if (len == 0) return false;
		char c = this.result.charAt(len -1);
		return Character.isWhitespace(c);
	}

	private void appendText(char c)
	{
		this.realLength++;
		this.result.append(c);
	}

	private void indent(char c)
	{
		this.result.append(c);
	}

	private void appendComment(String text)
	{
		if (text.startsWith("--"))
		{
			if (!this.isStartOfLine()) this.appendNewline();
		}
		else
		{
			if (!this.lastCharIsWhitespace()) this.appendText(' ');
		}
		this.appendText(text);
		if (text.startsWith("--"))
		{
			this.appendNewline();
		}
		else
		{
			this.appendText(' ');
		}
	}
	private void appendText(String text)
	{
		this.realLength += text.length();
		this.result.append(text);
	}

	private void appendText(StringBuffer text)
	{
		if (text.length() == 0) return;
		this.realLength += text.length();
		this.result.append(text);
	}

	private void indent(String text)
	{
		this.result.append(text);
	}

	private void indent(StringBuffer text)
	{
		this.result.append(text);
	}

	private void appendNonSeparator(String text)
	{
		this.realLength += text.length();
		if (!text.startsWith(" ") && !lastCharIsWhitespace()) this.result.append(' ');
		this.result.append(text);
	}

	private boolean needsWhitespace(SQLToken last, SQLToken current)
	{
		return this.needsWhitespace(last, current, false);
	}

	/**
	 * 	Return true if a whitespace should be added before the current token.
	 */
	private boolean needsWhitespace(SQLToken last, SQLToken current, boolean ignoreStartOfline)
	{
		char lastChar = last.getContents().charAt(0);
		char currChar = current.getContents().charAt(0);
		if (!ignoreStartOfline && this.isStartOfLine()) return false;
		if ( (lastChar == '<' || lastChar == '>') && (currChar == '=' || currChar == '<' || currChar == '>')) return false;
		if (currChar == '=') return true;
		if (lastChar == '=') return true;
		if (lastChar == '\"') return false;
		if (lastChar == '.' && current.isIdentifier()) return false;
		if (lastChar == '(' && current.isReservedWord()) return false;
		if (lastChar == ')' && !current.isSeparator() ) return true;
		if ((last.isIdentifier()|| last.isLiteral()) && current.isOperator()) return true;
		if ((current.isIdentifier() || current.isLiteral()) && last.isOperator()) return true;
		if (current.isSeparator() || current.isOperator()) return false;
		if (last.isSeparator() || last.isOperator()) return false;
		return true;
	}

	private SQLToken processFrom()
		throws Exception
	{
		StringBuffer b = new StringBuffer("     ");
		StringBuffer oldIndent = this.indent;
		//this.indent = null;
		SQLToken t = (SQLToken)this.lexer.getNextToken(true, false);
		SQLToken lastToken = t;
		while (t != null)
		{
			String text = t.getContents();

			if (t.isReservedWord() && FROM_TERMINAL.contains(text.toUpperCase()))
			{
				this.indent = oldIndent;
				return t;
			}
			else if (lastToken.isSeparator() && lastToken.getContents().equals("(") && text.equalsIgnoreCase("SELECT") )
			{
				t = this.processSubSelect(true);
				continue;
			}

			if (t.isComment())
			{
				this.appendComment(text);
			}
			else if (t.isSeparator() && text.equals("("))
			{
				if ((!lastToken.isSeparator() || lastToken == t) && !this.lastCharIsWhitespace()) this.appendText(' ');
				this.appendText(text);
			}
			else if (t.isSeparator() && text.equals(")"))
			{
				this.appendText(text);
			}
			else if (t.isSeparator() && text.equals(","))
			{
				this.appendText(",");
				this.appendNewline();
				this.indent(b);
			}
			else
			{
				if (this.needsWhitespace(lastToken, t)) this.appendText(' ');
				this.appendText(text);
				if (LINE_BREAK_AFTER.contains(text))
				{
					this.appendNewline();
					this.indent(b);
				}
			}
			lastToken = t;
			t = (SQLToken)this.lexer.getNextToken(true, false);
		}
		this.indent = oldIndent;
		return null;
	}

	private SQLToken processList(SQLToken last, int indentCount, Set terminalKeys)
		throws Exception
	{
		StringBuffer b = new StringBuffer(indentCount);
		for (int i=0; i < indentCount; i++) b.append(' ');

		boolean isSelect = last.getContents().equals("SELECT");
		SQLToken t = (SQLToken)this.lexer.getNextToken(true, false);
		SQLToken lastToken = last;
		
		while (t != null)
		{
			String text = t.getContents();
			if (t.isComment())
			{
				this.appendComment(text);
			}
			else if (isSelect && "DECODE".equalsIgnoreCase(text))
			{
				if (this.needsWhitespace(lastToken, t)) this.appendText(' ');
				this.appendText(text);
				t = processDecode(indentCount);
				continue;
			}
			else if (isSelect && "CASE".equals(text))
			{
				if (this.needsWhitespace(lastToken, t)) this.appendText(' ');
				this.appendText(text);
				t = processCase(indentCount);
				continue;
			}
			else if (t.isReservedWord() && terminalKeys.contains(text.toUpperCase()))
			{
				return t;
			}
			else if (t.isSeparator() && text.equals("("))
			{
				if (this.needsWhitespace(lastToken, t)) this.appendText(' ');
				this.appendText("(");
				// an equal sign immediately followed by an opening 
				// bracket cannot be a function call (the function name 
				// is missing) so it has to be a sub-select
				if ("=".equals(lastToken.getContents()))
				{
					t = this.processSubSelect(false);
					this.appendText(t.getContents());
				}
				else
				{
					this.processFunctionCall(t);
				}
			}
			else if (t.isSeparator() && text.equals(","))
			{
				this.appendText(",");
				this.appendNewline();
				this.indent(b);
			}
			else if (text.equals("*") && !lastToken.isSeparator())
			{
				this.appendText(" *");
			}
			else
			{
				if (this.needsWhitespace(lastToken, t)) this.appendText(' ');
				this.appendText(text);
			}
			lastToken = t;
			t = (SQLToken)this.lexer.getNextToken(true, false);
		}
		return null;
	}

	private SQLToken processSubSelect(boolean addSelectKeyword)
		throws Exception
	{
		SQLToken t = (SQLToken)this.lexer.getNextToken(false, false);
		int bracketCount = 1;
		StringBuffer subSql = new StringBuffer(250);

		// this method gets called when then "parser" hits an
		// IN ( situation. If no SELECT is coming, we assume
		// its a list like IN ('x','Y')
		if (!"SELECT".equalsIgnoreCase(t.getContents()) && !addSelectKeyword)
		{
			return this.processInList(t);
		}

		if (addSelectKeyword)
		{
			subSql.append("SELECT ");
		}

		int lastIndent = this.getCurrentLineLength();

		boolean realSubSelect = false;

		while (t != null)
		{
			String text = t.getContents();
			if (t.isSeparator() && text.equals(")"))
			{
				bracketCount --;

				if (bracketCount == 0)
				{
					SqlFormatter f = new SqlFormatter(subSql.toString(), lastIndent, this.maxSubselectLength);
					String s = f.getFormattedSql();
					if (f.getRealLength() < this.maxSubselectLength)
					{
						s = s.replaceAll(" *\n *", " ");
						this.appendText(s.trim());
					}
					else
					{
						this.appendText(s);
						//this.appendNewline();
						//for (int i=0; i < lastIndent; i++) this.indent(' ');
					}

					return t;
				}
			}
			else if (t.isSeparator() && text.equals("("))
			{
				bracketCount ++;
			}
			subSql.append(' ');
			subSql.append(text);
			t = (SQLToken)this.lexer.getNextToken();
		}
		return null;
	}
	
	private SQLToken processDecode(int indent)
		throws Exception
	{
		StringBuffer current = new StringBuffer(indent);

		for (int i=0; i < indent; i++) current.append(' ');
		
		StringBuffer b = new StringBuffer(indent + 2);
		for (int i=0; i < indent; i++) b.append(' ');
		b.append("      ");
		
		SQLToken t = (SQLToken)this.lexer.getNextToken(true,true);
		String text = null;
		int commaCount = 0;
		boolean inQuotes = false;
		while (t != null)
		{
			text = t.getContents();
			if ("'".equals(text))
			{
				inQuotes = !inQuotes;
			}
			if (",".equals(text) && !inQuotes) commaCount ++;
			
			if (",".equals(text) && !inQuotes)
			{
				this.appendText(text);
				if (commaCount % 2 == 1)
				{
					this.appendNewline();
					this.indent(b);
				}
			}
			else if (")".equalsIgnoreCase(text) && !inQuotes)
			{
				this.appendNewline();
				this.indent(current);
				this.appendText(text);
				t = (SQLToken)this.lexer.getNextToken(true, false);
				return t;
			}
			else if (text.indexOf("\n") == -1 &&  text.indexOf("\r") == -1)
			{
				this.appendText(text);
			}
			t = (SQLToken)this.lexer.getNextToken(true,true);
		}
		return null;
	}

	private SQLToken processCase(int indent)
		throws Exception
	{
		StringBuffer current = new StringBuffer(indent);

		for (int i=0; i < indent; i++) current.append(' ');
		
		StringBuffer b = new StringBuffer(indent + 2);
		for (int i=0; i < indent; i++) b.append(' ');
		b.append("  ");
		
		SQLToken t = (SQLToken)this.lexer.getNextToken(true,true);
		String text = null;
		while (t != null)
		{
			text = t.getContents();
			if ("WHEN".equalsIgnoreCase(text) || "ELSE".equalsIgnoreCase(text))
			{
				this.appendNewline();
				this.indent(b);
				this.appendText(text);
			}
			else if ("THEN".equalsIgnoreCase(text))
			{
				this.appendText(text);
			}
			else if ("END".equalsIgnoreCase(text))
			{
				this.appendNewline();
				this.indent(current);
				this.appendText(text);
				//this.appendNewline();
				t = (SQLToken)this.lexer.getNextToken(true, false);
				return t;
			}
			else if (text.indexOf("\n") == -1 &&  text.indexOf("\r") == -1)
			{
				this.appendText(text);
			}
			t = (SQLToken)this.lexer.getNextToken(true,true);
		}
		return null;
	}
	
	private String fixWbCommandCase(String verb)
	{
		if (!verb.toLowerCase().startsWith("wb")) return verb;
		String s = "Wb" + Character.toUpperCase(verb.charAt(2)) + verb.substring(3).toLowerCase();
		return s;
	}
	
	private SQLToken processWbCommand(int indent)
		throws Exception
	{
		StringBuffer b = new StringBuffer(indent);

		for (int i=0; i < indent; i++) b.append(' ');

		SQLToken t = (SQLToken)this.lexer.getNextToken(true,false);
		boolean first = true;
		boolean isParm = false;
		while (t != null)
		{
			String text = t.getContents();
			if (isParm) text = text.toLowerCase();
			if (text.equals("-"))
			{
				if (!first) 
				{
					this.appendNewline();
					this.indent(b);
				}
				else 
				{
					this.appendText(' ');
				}
				isParm = true;
			}
			else
			{
				isParm = false;
			}
			this.appendText(text);
			t = (SQLToken)this.lexer.getNextToken(true,false);
			first = false;
		}
		return null;
	}
	
	private SQLToken processBracketList(int indentCount)
		throws Exception
	{
		StringBuffer b = new StringBuffer(indentCount);

		for (int i=0; i < indentCount; i++) b.append(' ');

		this.appendText(b);
		SQLToken t = (SQLToken)this.lexer.getNextToken(true,false);

		while (t != null)
		{
			String text = t.getContents();
			if (t.isSeparator() && text.equals(")"))
			{
				this.appendNewline();
				//this.indent(b);
				this.appendText(")");
				return (SQLToken)this.lexer.getNextToken();
			}
			else if (t.isSeparator() && text.equals("("))
			{
				this.appendText(" (");
				this.processFunctionCall(t);
			}
			else if (t.isSeparator() && text.equals(","))
			{
				this.appendText(",");
				this.appendNewline();
				this.indent(b);
			}
			else if (!t.isWhiteSpace())
			{
				this.appendText(text);
				if (t.isComment()) this.appendText(' ');
			}
			t = (SQLToken)this.lexer.getNextToken(true, false);
		}
		return null;
	}

	private SQLToken processInList(SQLToken current)
		throws Exception
	{
		ArrayList list = new ArrayList(25);
		list.add(new StringBuffer(""));
		SQLToken t = current;

		int bracketcount = 0;
		int elementcounter = 0;

		while (t != null)
		{
			String text = t.getContents();
			if (t.isSeparator() && text.equals(")"))
			{
				if (bracketcount == 0)
				{
					this.appendCommaList(list);
					return (SQLToken)this.lexer.getNextToken();
				}
				else
				{
					StringBuffer b = (StringBuffer)list.get(elementcounter);
					if (b == null)
					{
						b = new StringBuffer(text);
						if (elementcounter < list.size()) list.set(elementcounter, b);
					}
					else
					{
						b.append(text);
					}
				}
			}
			else if (t.isSeparator() && text.equals("("))
			{
				bracketcount ++;
			}
			else if (t.isSeparator() && text.equals(","))
			{
				if (bracketcount == 0)
				{
					list.add(new StringBuffer(""));
					elementcounter = list.size() - 1;
				}
			}
			else if (!t.isWhiteSpace())
			{
				StringBuffer b = (StringBuffer)list.get(elementcounter);
				if (b == null)
				{
					b = new StringBuffer(text);
					if (t.isComment()) b.append(" ");
					list.set(elementcounter, b);
				}
				else
				{
					b.append(text);
					if (t.isComment()) b.append(' ');
				}
			}
			t = (SQLToken)this.lexer.getNextToken(true, false);
		}
		return null;
	}

	private void appendCommaList(ArrayList aList)
	{
		int indent = this.getCurrentLineLength();
		StringBuffer ind = new StringBuffer(indent);
		for (int i=0; i < indent; i++) ind.append(' ');
		boolean newline = (aList.size() > 10);
		int count = aList.size();
		for (int i=0; i < count; i++)
		{
			this.appendText((StringBuffer)aList.get(i));
			if (i < count - 1) this.appendText(", ");
			if (newline)
			{
				this.appendNewline();
				this.indent(ind);
			}
		}
		this.appendText(")");
	}

	private void advanceToOpeningBracket()
		throws Exception
	{
		SQLToken t = (SQLToken)this.lexer.getNextToken(false, false);
		while (t != null)
		{
			if (t.isSeparator() && t.getContents().equals("("))
			{
				this.appendNewline();
				this.appendText(t.getContents());
				return;
			}
			this.appendText(' ');
			this.appendText(t.getContents());
			t = (SQLToken)this.lexer.getNextToken(false, false);
		}
	}

	private boolean isStartOfLine()
	{
		int len = this.result.length();
		if (len == 0) return true;
		return (this.result.charAt(len - 1) == '\n');
	}

	private boolean isLastCharWhitespace()
	{
		int len = this.result.length();
		if (len == 0) return true;
		return (this.result.charAt(len - 1) == ' ');
	}

	private void formatSql()
		throws Exception
	{
		SQLToken t = (SQLToken)this.lexer.getNextToken(true, false);
		SQLToken lastToken = t;
		CommandTester wbTester = new CommandTester();
		//if (this.indent != null) this.appendText(this.indent);
		while (t != null)
		{
			if (t.isComment())
			{
				String text = t.getContents();
				this.appendComment(text);
			}
			else if (t.isReservedWord())
			{
				if (lastToken.isComment()) this.appendNewline();

				String word = t.getContents().toUpperCase();
				
				if (LINE_BREAK_BEFORE.contains(word))
				{
					if (!isStartOfLine()) this.appendNewline();
					if ("SET".equals(word)) this.indent("   ");
					this.appendText(word);
				}
				else
				{
					if (!lastToken.isSeparator() && lastToken != t && !isStartOfLine()) this.appendText(' ');
					if (wbTester.isWbCommand(word))
					{
						this.appendText(fixWbCommandCase(word));
					}
					else
					{
						this.appendText(word);
					}
				}

				if (LINE_BREAK_AFTER.contains(word))
				{
					this.appendNewline();
				}

				if (word.equals("ALL") && lastToken.isReservedWord() && lastToken.getContents().equals("UNION"))
				{
					this.appendNewline();
				}

				if (word.equals("SELECT"))
				{
					t = this.processList(t,"SELECT".length() + 1, SELECT_TERMINAL);
					if (t == null) return;
					continue;
				}

				if (word.equals("SET"))
				{
					t = this.processList(t,"SET".length() + 4, SET_TERMINAL);
					if (t == null) return;
					continue;
				}
				if (word.equals("FROM"))
				{
					t = this.processFrom();
					if (t == null) return;
					continue;
				}

				if (word.equals("CREATE"))
				{
					t = this.processCreate(t);
					if (t == null) return;
					continue;
				}
				if (word.equals("BY") && lastToken.isReservedWord()	&& lastToken.getContents().equals("GROUP"))
				{
					t = this.processList(lastToken, "GROUP BY ".length(), BY_TERMINAL);
					if (t == null) return;
					continue;
				}

				if (word.equalsIgnoreCase("WHERE"))
				{
					t = this.processWhere(t);
					if (t == null) return;
					continue;
				}

				if (word.equalsIgnoreCase("INTO"))
				{
					t = this.processIntoKeyword();
					continue;
				}

				if (word.equalsIgnoreCase("VALUES"))
				{
					// the next (non-whitespace token has to be a (
					t = (SQLToken)this.lexer.getNextToken(false, false);
					if (t.isSeparator() && t.getContents().equals("("))
					{
						this.appendNewline();
						this.appendText("(");
						this.appendNewline();

						t = this.processBracketList(2);
					}
					if (t == null) return;
					continue;
				}
				if (wbTester.isWbCommand(word))
				{
					t = this.processWbCommand(word.length() + 1);
				}
				
			}
			else
			{
				String word = t.getContents().toUpperCase();
				boolean newLine = false;
				if (LINE_BREAK_BEFORE.contains(word))
				{
					if (!isStartOfLine()) this.appendNewline();
				}

				if (t.isSeparator() && word.equals("("))
				{
					this.appendText(" (");
					this.processFunctionCall(t);
				}
				else
				{
					if (t.isSeparator() && word.equals(";"))
					{
						this.appendText(word);
						this.appendNewline();
						this.appendNewline();
					}
					else
					{
						if (this.needsWhitespace(lastToken, t)) this.appendText(' ');
						this.appendText(t.getContents());
					}
				}
			}
			lastToken = t;
			t = (SQLToken)this.lexer.getNextToken(false, false);
		}
		this.appendNewline();
		this.appendNewline();
	}

	private SQLToken processWhere(SQLToken previousToken)
		throws Exception
	{
		SQLToken t = (SQLToken)this.lexer.getNextToken(true, false);
		SQLToken lastToken = previousToken;
		int bracketCount = 0;
		boolean bracketChange = false;
		while (t != null)
		{
			String verb = t.getContents();

			if (t.isReservedWord() && WHERE_TERMINAL.contains(verb))
			{
				return t;
			}

			if (t.isSeparator() && verb.equals(";"))
			{
				return t;
			}

			if (t.isSeparator() && verb.equals(")"))
			{
				bracketCount --;
				this.appendText(")");
			}
			else if (t.isComment())
			{
				this.appendComment(verb);
			}
			else if (bracketCount == 0 && t.isReservedWord() && (verb.equals("AND") || verb.equals("OR")) )
			{
				if (!this.isStartOfLine()) this.appendNewline();
				this.appendText(verb);
				this.appendText("  ");
				if (verb.equals("OR")) this.appendText(' ');
			}
			else if (t.isSeparator() && t.getContents().equals("("))
			{
				bracketCount ++;
				String lastWord = lastToken.getContents();
				if (lastToken.isReservedWord() && SUBSELECT_START.contains(lastWord))
				{
					this.appendText(" (");
					t = this.processSubSelect(false);
					if (t == null) return null;
					continue;
				}
				if (lastWord != null) lastWord = lastWord.toUpperCase();
				if (!lastToken.isSeparator() && !this.dbFunctions.contains(lastWord)) this.appendText(' ');
				this.appendText(t.getContents());
			}
			else
			{
				if (this.needsWhitespace(lastToken, t)) this.appendText(' ');
				this.appendText(t.getContents());
			}

			lastToken = t;
			t = (SQLToken)this.lexer.getNextToken(true, false);
		}
		return null;
	}

	private SQLToken processIntoKeyword()
		throws Exception
	{
		SQLToken t = (SQLToken)this.lexer.getNextToken(false, false);
		// we expect an identifier now (the table name)
		// but to be able to handle "wrong statements" we'll
		// make sure everything's fine

		if (t.isIdentifier())
		{
			this.appendText(' ');
			this.appendText(t.getContents());
			t = (SQLToken)this.lexer.getNextToken(false, false);
			if (t.getContents().equalsIgnoreCase("VALUES"))
			{
				// no column list to format here...
				return t;
			}
			else if (t.isSeparator() && t.getContents().equals("("))
			{
				this.appendNewline();
				this.appendText(t.getContents());
				this.appendNewline();
				return this.processBracketList(2);
			}
			return t;
		}
		else
		{
			return t;
		}
	}

	private void processFunctionCall(SQLToken last)
		throws Exception
	{
		int bracketCount = 1;
		SQLToken t = (SQLToken)this.lexer.getNextToken(true, false);
		SQLToken lastToken = last;
		while (t != null)
		{
			String text = t.getContents();
			if (t.isSeparator() && text.equals(")"))
			{
				bracketCount --;
			}
			if (t.isSeparator() && text.equals("("))
			{
				bracketCount ++;
			}
			if (this.needsWhitespace(lastToken, t)) this.appendText(' ');
			this.appendText(t.getContents());

			if (bracketCount == 0)
			{
				this.appendText(' ');
				break;
			}
			lastToken = t;
			t = (SQLToken)this.lexer.getNextToken(true, false);
		}
	}

	private SQLToken processCreate(SQLToken previous)
		throws Exception
	{
		SQLToken t = (SQLToken)this.lexer.getNextToken(true, false);
		String verb = t.getContents().toUpperCase();
		if (verb.equals("TABLE"))
		{
			this.appendText(' ');
			this.appendText(t.getContents());
			this.appendText(' ');
			t = this.processCreateTable(t);
			return t;
		}
		else if (verb.equals("VIEW") || verb.equals("SNAPSHOT"))
		{
			this.appendText(' ');
			this.appendText(t.getContents());
			this.appendText(' ');
			return this.processCreateView(t);
		}
		else if (verb.equals("INDEX"))
		{
			this.appendText(' ');
			this.appendText(t.getContents());
			//this.appendText(' ');
			return this.processCreateIndex(t);
		}

		return t;
	}

	private SQLToken processCreateTable(SQLToken previous)
		throws Exception
	{
		SQLToken t = (SQLToken)this.lexer.getNextToken(false, false);
		SQLToken last = previous;
		int bracketCount = 0;
		StringBuffer definition = new StringBuffer(200);

		while (t != null)
		{
			if (t.getContents().equals("(") )
			{
				if (bracketCount == 0)
				{
					// start of table definition
					this.appendNewline();
					this.appendText('(');
					this.appendNewline();
				}
				else
				{
					definition.append('(');
				}
				bracketCount ++;
			}
			else if (t.getContents().equals(")"))
			{
				if (bracketCount == 1)
				{
					// end of table definition
					this.outputFormattedColumnDefs(definition);
					this.appendNewline();
					//this.appendText(')');
					//this.appendNewline();
					return t;
				}
				else
				{
					definition.append(')');
				}
				bracketCount--;
			}
			else if (bracketCount > 0)
			{
				// collect the table definition so that it's easier to format it
				if (this.needsWhitespace(last, t, true))
				{
					definition.append(' ');
				}
				definition.append(t.getContents());
			}
			else
			{
				this.appendText(t.getContents());
				if (this.needsWhitespace(last, t, true))
				{
					this.appendText(' ');
				}
			}
			last = t;
			t = (SQLToken)this.lexer.getNextToken(false, false);
		}
		return t;
	}

	private void outputFormattedColumnDefs(StringBuffer source)
	{
		ArrayList cols = new ArrayList();
		int size = source.length();
		int bracketCount = 0;
		int lastPos = 0;
		for (int i=0; i < size; i++)
		{
			char c = source.charAt(i);
			if (c == ',' && bracketCount == 0)
			{
				cols.add(source.substring(lastPos, i).trim());
				lastPos = i + 1;
			}
			else if (c == '(')
			{
				bracketCount ++;
			}
			else if (c == ')')
			{
				bracketCount --;
			}
		}
		cols.add(source.substring(lastPos, size).trim());

		// second pass, find the longest column name
		int count = cols.size();
		int width = 0;

		for (int i=0; i < count; i++)
		{
			String def = (String)cols.get(i);
			int pos = def.indexOf(' ');
			if (pos > width) width = pos;
		}
		width += 2;

		// third pass, output the definitions aligned to the longest column name
		for (int i=0; i < count; i++)
		{
			String def = (String)cols.get(i);
			int pos = def.indexOf(' ');

			this.indent("   ");
			if (pos == -1)
			{
				// no type present, simply append the whole column
				this.appendText(def);
			}
			else
			{
				String col = def.substring(0, pos);
				String type = def.substring(pos + 1);
				this.appendText(col);
				while (pos < width)
				{
					this.appendText(' ');
					pos ++;
				}
				this.appendText(type);
			}
			if (i < count - 1)
			{
				this.appendText(',');
				this.appendNewline();
			}
		}
	}

	/**
	 *	Process the elements in a () combination
	 *	Any bracket inside the brackets are assumed to be "function calls"
	 *  and just treated as further elements.
	 *	It is assumed that the passed SQLToken is the opening bracket
	 *  @return the token after the closing bracket
	 */
	private SQLToken processCommaList(SQLToken previous, int maxElements, int indentCount)
		throws Exception
	{
		StringBuffer definition = new StringBuffer(200);
		SQLToken t = previous;
		SQLToken last = previous;
		int bracketCount = 0;

		while (t != null)
		{
			if (t.getContents().equals("(") )
			{
				if (bracketCount > 0)
				{
					definition.append('(');
				}
				bracketCount ++;
			}
			else if (t.getContents().equals(")"))
			{
				if (bracketCount == 1)
				{
					List elements = StringUtil.stringToList(definition.toString(), ",");
					this.outputElements(elements, maxElements, indentCount);
					return (SQLToken)this.lexer.getNextToken(true, false);
				}
				else
				{
					definition.append(')');
				}
				bracketCount--;
			}
			else if (bracketCount > 0)
			{
				if (this.needsWhitespace(last, t, true))
				{
					definition.append(' ');
				}
				definition.append(t.getContents());
			}
			last = t;
			t = (SQLToken)this.lexer.getNextToken(true, false);
		}
		return t;
	}

	/*
	 *	Output the elements of the given List comma separated
	 *  If the list contains more elements, then maxElements
	 *  each element will be put on a single line
	 *	If more then one line is "printed" they will be indented by
	 *  indentCount spaces
	 */
	private void outputElements(List elements, int maxElements, int indentCount)
	{
		StringBuffer indent = new StringBuffer(indentCount);
		for (int i=0; i<indentCount; i++) indent.append(' ');

		int count = elements.size();

		if (count > maxElements)
		{
			this.appendNewline();
			this.indent(indent);
			this.appendText("(");
		}
		else
		{
			this.appendText(" (");
		}

		if (count > maxElements)
		{
			this.appendNewline();
			this.indent(indent);
			this.indent("  ");
		}

		for (int i=0; i < count; i++)
		{
			String text = (String)elements.get(i);
			this.appendText(text);
			if (i < count - 1)
			{
				if (count > maxElements)
				{
					this.appendText(',');
					this.appendNewline();
					this.indent(indent);
					this.indent("  ");
				}
				else
				{
					this.appendText(", ");
				}
			}
		}
		if (count > maxElements)
		{
			this.appendNewline();
			this.indent(indent);
		}
		this.appendText(")");
	}

	/**
	 * Format a CREATE VIEW statement
	 */
	private SQLToken processCreateView(SQLToken previous)
		throws Exception
	{
		SQLToken t = (SQLToken)this.lexer.getNextToken(false, false);
		SQLToken last = previous;
		int bracketCount = 0;
		StringBuffer definition = new StringBuffer(200);

		while (t != null)
		{
			if (t.getContents().equals("(") )
			{
				if (bracketCount == 0)
				{
					// start of column definition
					this.appendNewline();
					this.appendText('(');
					this.appendNewline();
				}
				else
				{
					definition.append('(');
				}
				bracketCount ++;
			}
			else if (t.getContents().equals(")"))
			{
				if (bracketCount == 1)
				{
					// end of table definition
					this.outputFormattedColumnDefs(definition);
					this.appendNewline();
					//this.appendText(')');
					//this.appendNewline();
					return t;
				}
				else
				{
					definition.append(')');
				}
				bracketCount--;
			}
			else if (bracketCount > 0)
			{
				// collect the table definition so that it's easier to format it
				if (this.needsWhitespace(last, t, true))
				{
					definition.append(' ');
				}
				definition.append(t.getContents());
			}
			else if ("SELECT".equals(t.getContents()))
			{
				return t;
			}
			else if ("AS".equals(t.getContents()))
			{
				this.appendNewline();
				this.appendText(t.getContents());
				this.appendNewline();
			}
			else
			{
				this.appendText(t.getContents());
				if (this.needsWhitespace(last, t, true))
				{
					this.appendText(' ');
				}
			}
			last = t;
			t = (SQLToken)this.lexer.getNextToken(false, false);
		}
		return t;
	}

	private SQLToken processCreateIndex(SQLToken previous)
		throws Exception
	{
		SQLToken t = (SQLToken)this.lexer.getNextToken(true, false);
		SQLToken last = previous;
		StringBuffer definition = new StringBuffer(200);
		int bracketCount = 0;

		while (t != null)
		{
			String text = t.getContents();
			if (t.getContents().equals("(") )
			{
				return this.processCommaList(t, 5, 7);
			}
			else if (t.isReservedWord() && "ON".equals(text))
			{
				this.appendNewline();
				this.indent("       ");
				this.appendText(text);
			}
			else
			{
				if (this.needsWhitespace(last, t)) this.appendText(' ');
				this.appendText(text);
			}
			t = (SQLToken)this.lexer.getNextToken(true, false);
		}
		return t;
	}

	private SQLToken processCreateOther(SQLToken last)
		throws Exception
	{
		return (SQLToken)this.lexer.getNextToken(false, false);
	}

}