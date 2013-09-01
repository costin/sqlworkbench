/*
 * WbStringTokenizerTest.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2013, Thomas Kellerer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at.
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

import java.util.List;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Thomas Kellerer
 */
public class WbStringTokenizerTest
{

	@Test
	public void testParameterTokens()
	{
		WbStringTokenizer tok = new WbStringTokenizer('-', "\"'", false);
		tok.setDelimiterNeedsWhitspace(true);
		tok.setSourceString(" -other='stuff' -empty= -list='a','b' -one=' ' -nested='\"test\"'");
		List<String> tokens = tok.getAllTokens();
		int i = 0;
		for (String t : tokens)
		{
			switch (i)
			{
				case 1:
					assertEquals("other=stuff", t.trim());
					break;
				case 2:
					assertEquals("empty=", t.trim());
					break;
				case 3:
					assertEquals("list=a,b", t.trim());
					break;
				case 5:
					assertEquals("nested=\"test\"", t.trim());
			}
			i ++;
		}
	}

	@Test
	public void testTokenizer()
	{
		String data = "value1\t\"quoted value\"\t  \tlast";
		WbStringTokenizer tok = new WbStringTokenizer(data, "\t", true, "\"", false);

		int count = 0;
		while (tok.hasMoreTokens())
		{
			String value = tok.nextToken();
			switch (count)
			{
				case 0:
					assertEquals("Wrong first value", "value1", value);
					break;
				case 1:
					assertEquals("Wrong first value", "quoted value", value);
					break;
				case 2:
					assertEquals("Wrong first value", "  ", value);
					break;
				case 3:
					assertEquals("Wrong first value", "last", value);
					break;
				default:
					fail("Wrong number of parameters retrieved");
			}
			count ++;
		}
	}

}
