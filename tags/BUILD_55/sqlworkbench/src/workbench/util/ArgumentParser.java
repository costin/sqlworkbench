/*
 * Created on December 14, 2002, 2:38 PM
 */
package workbench.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author  workbench@kellerer.org
 */
public class ArgumentParser
{

	private Map arguments = new HashMap();
	
	public ArgumentParser()
	{
	}
	
	public void addArgument(String key)
	{
		if (key == null) throw new NullPointerException("Key may not be null");
		this.arguments.put(key.toLowerCase(), null);
	}
	
	public void parse(String args[])
	{
		StringBuffer line = new StringBuffer(200);
		for (int i=0; i<args.length; i++)
		{
			line.append(args[i]);
			line.append(' ');
		}
		this.parse(line.toString());
	}
	
	public void parse(String aCmdLine)
	{
		List words = StringUtil.split(aCmdLine, "-", false, "\"'", true);

		int count = words.size();
		for (int i=0; i < count; i++)
		{
			String word = (String)words.get(i);
			String arg = null;
			String value = null;
			int pos = word.indexOf('=');
			if (pos > -1)
			{
				arg = word.substring(0, pos).trim();
				value = word.substring(pos + 1).trim();
			}
			else
			{
				// ignore parameters without a value
				continue;
			}
			arg = arg.toLowerCase();
			if (arguments.containsKey(arg))
			{
				arguments.put(arg, value);
			}
		}
	}

	public boolean getBoolean(String key)
	{
		String value = this.getValue(key);
		return "true".equals(value);
	}
	
	public String getValue(String key)
	{
		String value = (String)this.arguments.get(key.toLowerCase());
		value = StringUtil.trimQuotes(value);
		return value;
	}
	
	public static void main(String[] args)
	{
		//String test = "spool /type=sql /file=\"d:/temp/test.sql\" /table=my_table;";
		//String test = "/profile=\"HSQLDB - Test Server\" /script=\"d:/temp/test.sql\"";
		String test = "-quotechar='\"' -file=\"d:/temp/export test.txt\" -delimiter=\" \" -dateformat=dd.MMM.yyyy";
		ArgumentParser parser = new ArgumentParser();
		parser.addArgument("type");
		parser.addArgument("file");
		parser.addArgument("table");
		parser.addArgument("delimiter");
		parser.addArgument("quotechar");
		parser.addArgument("dateformat");
		parser.addArgument("cleancr");
		parser.parse(test);
		System.out.println("delimiter=>" + parser.getValue("delimiter") + "<");
		System.out.println("file=" + parser.getValue("file"));
		System.out.println("quote=>" + parser.getValue("quotechar") + "<");
		System.out.println("done.");
	} 

}