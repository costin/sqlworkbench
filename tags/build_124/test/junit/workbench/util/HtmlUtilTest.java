/*
 * HtmlUtilTest.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2018, Thomas Kellerer
 *
 * Licensed under a modified Apache License, Version 2.0
 * that restricts the use for certain governments.
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at.
 *
 *     http://sql-workbench.net/manual/license.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Thomas Kellerer
 */
public class HtmlUtilTest
{

	@Test
  public void testCleanHTML()
  {
    assertEquals("foo", HtmlUtil.cleanHTML("<html>foo</html>"));
    assertEquals("foo", HtmlUtil.cleanHTML("<html><i>foo</i></html>"));
  }
  
  @Test
  public void testEscapeHTML()
	{
		String input = "<sometag> sometext";
		String escaped = HtmlUtil.escapeHTML(input);
		assertEquals("&lt;sometag&gt; sometext", escaped);

		input = "a &lt; b";
		escaped = HtmlUtil.escapeHTML(input);
		assertEquals("a &amp;lt; b", escaped);
	}

	@Test
  public void testEscapeXML()
	{
		String input = "<sometag> sometext";
		String escaped = HtmlUtil.escapeXML(input);
		assertEquals("&lt;sometag&gt; sometext", escaped);

		input = "a &lt; b";
		escaped = HtmlUtil.escapeXML(input);
		assertEquals("a &amp;lt; b", escaped);

		input = "a > b";
		escaped = HtmlUtil.escapeXML(input);
		assertEquals("a &gt; b", escaped);

		input = "a '>' b";
		escaped = HtmlUtil.escapeXML(input, false);
		assertEquals("a '&gt;' b", escaped);

		input = "& > < \"";
		escaped = HtmlUtil.escapeXML(input, false);
		assertEquals("&amp; &gt; &lt; &quot;", escaped);
	}
}