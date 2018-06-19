/*
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2017 Thomas Kellerer.
 *
 * Licensed under a modified Apache License, Version 2.0 (the "License")
 * that restricts the use for certain governments.
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.sql-workbench.net/manual/license.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * To contact the author please send an email to: support@sql-workbench.net
 */
package workbench.sql.lexer;

import java.io.IOException;

import workbench.WbTestCase;

import org.junit.Test;

import static org.junit.Assert.*;


/**
 *
 * @author Thomas Kellerer
 */
public class PostgresLexerTest
  extends WbTestCase
{

  public PostgresLexerTest()
  {
  }

  @Test
  public void testIdentifiers()
  {
    PostgresLexer lexer = new PostgresLexer("select foo#>>'{one}' from table");
    SQLToken token = lexer.getNextToken(false, false);
    token = lexer.getNextToken(false, false);
    assertEquals("foo", token.getText());
    token = lexer.getNextToken(false, false);
    assertEquals("#>>", token.getText());
  }

  @Test
  public void testLiterals()
    throws IOException
  {
    PostgresLexer lexer = new PostgresLexer("select E'foobar'");
    SQLToken token = lexer.getNextToken(false, false);
    token = lexer.getNextToken(false, false);
    assertNotNull(token);
    assertEquals(SQLToken.LITERAL_STRING, token.getID());
    assertEquals("E'foobar'", token.getText());
    token = lexer.getNextToken(false, false);
    assertNull(token);

    lexer = new PostgresLexer("select E'\\\\xCAFEBABE'");
    token = lexer.getNextToken(false, false);
    token = lexer.getNextToken(false, false);
    assertNotNull(token);
    assertEquals(SQLToken.LITERAL_STRING, token.getID());
    assertEquals("E'\\\\xCAFEBABE'", token.getText());
    token = lexer.getNextToken(false, false);
    assertNull(token);

    lexer = new PostgresLexer("select E'\\foobar'");
    token = lexer.getNextToken(false, false);
    token = lexer.getNextToken(false, false);
    assertNotNull(token);
    assertEquals(SQLToken.LITERAL_STRING, token.getID());
    assertEquals("E'\\foobar'", token.getText());
    token = lexer.getNextToken(false, false);
    assertNull(token);
  }

}