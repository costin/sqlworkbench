/*
 * FixedLengthLineParserTest.java
 * JUnit based test
 *
 * Created on 10. August 2007, 20:48
 */

package workbench.util;

import junit.framework.TestCase;

/**
 *
 * @author thomas
 */
public class FixedLengthLineParserTest
	extends TestCase
{

	public FixedLengthLineParserTest(String testName)
	{
		super(testName);
	}

	public void testParser()
	{
		try
		{
			int[] cols = new int[]{5, 1, 10};
			FixedLengthLineParser parser = new FixedLengthLineParser(cols);
			String line = "12345H1234567890";
			parser.setLine(line);
			String first = parser.getNext();
			assertEquals("12345", first);
			String second = parser.getNext();
			assertEquals("H", second);
			String third = parser.getNext();
			assertEquals("1234567890", third);
			
			line = "    1H        10";
			parser.setLine(line);
			parser.setTrimValues(true);
			first = parser.getNext();
			assertEquals("1", first);
			second = parser.getNext();
			assertEquals("H", second);
			third = parser.getNext();
			assertEquals("10", third);
			
			parser.setLine(line);
			parser.setTrimValues(false);
			first = parser.getNext();
			assertEquals("    1", first);
			second = parser.getNext();
			assertEquals("H", second);
			third = parser.getNext();
			assertEquals("        10", third);
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}