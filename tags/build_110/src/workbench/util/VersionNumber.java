/*
 * VersionNumber.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2011, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.util;

/**
 * @author Thomas Kellerer
 */
public class VersionNumber
{
	private int major = -1;
	private int minor = -1;
	
	public VersionNumber(int majorVersion, int minorVersion)
	{
		this.major = majorVersion;
		this.minor = minorVersion;
	}
	
	public VersionNumber(String number)
	{
		if (StringUtil.isEmptyString(number)) 
		{
			return;
		}
		
		if ("@BUILD_NUMBER@".equals(number))
		{
			major = 999;
			minor = 999;
		}
		else
		{
			try
			{
				String[] numbers = number.split("\\.");

				major = Integer.parseInt(numbers[0]);

				if (numbers.length > 1)
				{
					if (numbers[1].indexOf('-') > -1)
					{
						String plain = numbers[1].substring(0, numbers[1].indexOf('-'));
						minor = Integer.parseInt(plain);
					}
					else
					{
						minor = Integer.parseInt(numbers[1]);
					}
				}
				else
				{
					minor = -1;
				}
			}
			catch (Exception e)
			{
				minor = -1;
				major = -1;
			}
		}
	}

	public boolean isValid()
	{
		return this.major != -1;
	}

	public int getMajorVersion() { return this.major; }
	public int getMinorVersion() { return this.minor; }
	
	public boolean isNewerThan(VersionNumber other)
	{
		if (!this.isValid()) return false;
		if (this.major > other.major) return true;
		if (this.major == other.major) 
		{
			if (this.minor > other.minor) return true;
		}
		return false;
	}

	public boolean isNewerOrEqual(VersionNumber other)
	{
		if (isNewerThan(other)) return true;
		if (this.major == other.major && this.minor == other.minor) return true;
		return false;
	}
	
	public String toString()
	{
		if (minor == -1 && major == -1) return "n/a";
		
		if (minor == -1) return Integer.toString(major);
		if (major == 999) return "999";
		return Integer.toString(major) + "." + Integer.toString(minor);
	}
}