/*
 * MessageBufferTest.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2010, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.util;

import workbench.WbTestCase;

/**
 *
 * @author Thomas Kellerer
 */
public class MessageBufferTest 
	extends WbTestCase
{
	
	public MessageBufferTest(String testName)
	{
		super(testName);
	}

	public void testMaxSize()
	{
		try
		{
			int max = 5;
			MessageBuffer b = new MessageBuffer(5);
			for (int i = 0; i < max * 2; i++)
			{
				b.append("Line" + i + "\n");
			}
			String content = b.getBuffer().toString();
			String expected = "(...)\nLine5\nLine6\nLine7\nLine8\nLine9\n";
			assertEquals(expected, content);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void testAppendBuffer()
	{
		MessageBuffer b1 = new MessageBuffer();
		b1.append("Line one");
		int l = b1.getLength();
		MessageBuffer b2 = new MessageBuffer();
		b2.append(b1);
		assertEquals("Wrong length", b2.getLength(), l);
		
		b2 = new MessageBuffer();
		b2.append("Some stuff");
		int l2 = b2.getLength();
		b2.append(b1);
		assertEquals("Wrong length", b2.getLength(), l + l2);
	}
	
	public void testBuffer()
	{
		try
		{
			MessageBuffer b = new MessageBuffer();
			b.append("Hello, world");
			b.appendNewLine();
			b.append("how are you?");
			
			CharSequence s = b.getBuffer();
			assertEquals("Hello, world\nhow are you?", s.toString());
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	
}