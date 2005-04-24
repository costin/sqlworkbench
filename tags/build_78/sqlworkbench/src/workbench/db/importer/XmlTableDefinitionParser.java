/*
 * XmlDataTableParser.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2005, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: info@sql-workbench.net
 *
 */
package workbench.db.importer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.Reader;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import workbench.db.ColumnIdentifier;
import workbench.db.exporter.XmlRowDataConverter;
import workbench.log.LogMgr;
import workbench.util.EncodingUtil;
import workbench.util.StringUtil;

/**
 * A parser to read the table definition from a Workbench XML file.
 * @author  info@sql-workbench.net
 */
public class XmlTableDefinitionParser
	extends DefaultHandler
{
	private int currentColIndex;
	private ColumnIdentifier[] columnList;
	private String tableName;
	//private ColumnIdentifier currentColumn;
	private String filename;
	private String encoding = "UTF-8";
	private StringBuffer chars;
	private String tagFormat;
	
	public XmlTableDefinitionParser(String fname, String enc)
		throws FileNotFoundException
	{
		this.filename = fname;
		if (enc != null) this.encoding = enc;
		this.parseTableStructure();
	}
	
	public ColumnIdentifier[] getColumns()
	{
		return this.columnList;
	}
	
	public String getTagFormat()
	{
		return this.tagFormat;
	}
	
	public String getTableName()
	{
		return this.tableName;
	}

	private void parseTableStructure()
		throws FileNotFoundException
	{
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(false);
		Reader in = null;
		try
		{
			SAXParser saxParser = factory.newSAXParser();
			File f = new File(this.filename);
			in = EncodingUtil.createReader(f, this.encoding);
			InputSource source = new InputSource(in);
			saxParser.parse(source, this);
		}
		catch (ParsingEndedException e)
		{
			// expected exception to stop parsing
		}
		catch (FileNotFoundException e)
		{
			throw e;
		}
		catch (Throwable e)
		{
			LogMgr.logError("XmlDataTableParser.parseTableStructure()", "Error reading table structure", e);
			this.columnList = null;
			this.tableName = null;
		}
		finally
		{
			try { in.close(); } catch (Throwable th) {}
		}
	}
	
	public void startElement(String namespaceURI, String sName, String qName, Attributes attrs)
		throws SAXException
	{
		this.chars = new StringBuffer();
		if (qName.equals(XmlRowDataConverter.COLUMN_DEF_TAG))
		{
			this.columnList[this.currentColIndex] = new ColumnIdentifier();
		}
	}
	
	public void endElement(String namespaceURI, String sName, String qName)
		throws SAXException
	{
		if (qName.equals(XmlRowDataConverter.TAG_TAG_FORMAT))
		{
			this.tagFormat = this.chars.toString();
		}
		else if (qName.equals(XmlRowDataConverter.COLUMN_COUNT_TAG))
		{
			try
			{
				int count = StringUtil.getIntValue(this.chars.toString(), -1);
				this.columnList = new ColumnIdentifier[count];
				this.currentColIndex = 0;
			}
			catch (Exception e)
			{
				LogMgr.logError("XmlTableDefinitionParser.endElement", "Incorrec value for " + XmlRowDataConverter.COLUMN_COUNT_TAG + ": " + this.chars, e);
				throw new SAXException("Invalid column count");
			}
		}
		else if (qName.equals(XmlRowDataConverter.COLUMN_DEF_TAG))
		{
			currentColIndex ++;
		}
		else if (qName.equals(XmlRowDataConverter.TABLE_NAME_TAG))
		{
			this.tableName = this.chars.toString();
		}
		else if (qName.equals(XmlRowDataConverter.COLUMN_NAME_TAG))
		{
			this.columnList[this.currentColIndex].setColumnName(this.chars.toString()); 
		}
		else if (qName.equals(XmlRowDataConverter.JAVA_TYPE_TAG))
		{
			try
			{
				int type = Integer.parseInt(this.chars.toString());
				this.columnList[currentColIndex].setDataType(type);
			}
			catch (Exception e)
			{
				LogMgr.logError("XmlTableDefinitionParser.endElement()", "Could not read columnn type!", e);
				throw new SAXException("Could not read columnn type");
			}
		}
		else if (qName.equals("dbms-data-type"))
		{
			try
			{
				this.columnList[currentColIndex].setDbmsType(this.chars.toString());
			}
			catch (Exception e)
			{
				LogMgr.logError("XmlDataFileParser.endElement()", "Could not read dbms columnn type!", e);
				throw new SAXException("Could not read dbms columnn type");
			}
		}
		else if (qName.equals(XmlRowDataConverter.JAVA_CLASS_TAG))
		{
			try
			{
				this.columnList[currentColIndex].setColumnClass(this.chars.toString());
			}
			catch (Exception e)
			{
				LogMgr.logError("XmlTableDefinitionParser.endElement()", "Could not read columnn class name!", e);
				throw new SAXException("Could not read columnn name");
			}
		}
		else if (qName.equals(XmlRowDataConverter.TABLE_DEF_TAG))
		{
			throw new ParsingEndedException();
		}
	}	
	
	public void characters(char buf[], int offset, int len)
		throws SAXException
	{
		if (chars != null)
		{
			this.chars.append(buf, offset, len);
		}
	}		

}
