/*
 * ProducerFactory.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2007, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db.importer;
import java.io.File;
import java.util.List;
import workbench.db.ColumnIdentifier;
import workbench.db.TableIdentifier;
import workbench.db.WbConnection;
import workbench.gui.dialogs.dataimport.ImportOptions;
import workbench.gui.dialogs.dataimport.TextImportOptions;
import workbench.gui.dialogs.dataimport.XmlImportOptions;
import workbench.interfaces.ImportFileParser;
import workbench.sql.wbcommands.CommandTester;
import workbench.sql.wbcommands.CommonArgs;
import workbench.sql.wbcommands.CommonArgs;
import workbench.sql.wbcommands.WbExport;
import workbench.sql.wbcommands.WbImport;
import workbench.util.StringUtil;
import workbench.util.ValueConverter;
import workbench.util.ZipUtil;

/**
 *	A factory for RowDataProducer to import text or XML files. 
 * @author  support@sql-workbench.net
 */
public class ProducerFactory
{
	public static final int IMPORT_TEXT = 0;
	public static final int IMPORT_XML = 1;
	private int importType = -1;
	private TextImportOptions textOptions;
	private XmlImportOptions xmlOptions;
	private ImportOptions generalOptions;
	private File inputFile;
	private List<ColumnIdentifier> inputColumns;
	private TableIdentifier table;
	private RowDataProducer producer;
	private ImportFileParser fileParser;
	private WbConnection connection;
	private int batchSize = -1;
	
	public ProducerFactory(File inputFile)
	{
		this.setInputFile(inputFile);
	}
	
	public void setBatchSize(int size) 
	{
		this.batchSize = (size > 0 ? size : -1);
	}
	
	public void setConnection(WbConnection conn)
	{
		if (this.connection != conn)
		{
			this.producer = null;
		}
		this.connection = conn;
	}
	
	public void setGeneralOptions(ImportOptions options)
	{
		this.generalOptions = options;
	}
	
	public void setTextOptions(TextImportOptions options)
	{
		this.textOptions = options;
	}

	public TextImportOptions getTextOptions()
	{
		return this.textOptions;
	}
	
	public XmlImportOptions getXmlOptions()
	{
		return this.xmlOptions;
	}
	
	public void setXmlOptions(XmlImportOptions options)
	{
		this.xmlOptions = options;
	}
	
	public void setImporterOptions(DataImporter importer)
	{
		importer.setMode(generalOptions.getMode());
	}
	
	public void setType(int type)
	{
		if (type == IMPORT_TEXT)
			this.setImportTypeText();
		else if (type == IMPORT_XML)
			this.setImportTypeXml();
		else
			throw new IllegalArgumentException("Not a valid import type!");
	}

	public boolean isTextImport()
	{
		return this.importType == IMPORT_TEXT;
	}
	public boolean isXmlImport()
	{
		return this.importType == IMPORT_XML;
	}
	
	public void setImportTypeText()
	{
		this.importType = IMPORT_TEXT;
		this.producer = null;
	}
	
	public void setImportTypeXml()
	{
		this.importType = IMPORT_XML;
		this.producer = null;
	}
	
	private void setInputFile(File inputFilename)
	{
		this.inputFile = inputFilename;
		this.inputColumns = null;
		this.producer = null;
		this.fileParser = null;
	}
	
	public void setTargetTable(TableIdentifier tableId)
	{
		this.table = tableId;
		if (this.table == null) return;
		if (this.producer == null) getProducer();
		fileParser.setTableName(tableId.getTableExpression());
	}
	
	public RowDataProducer getProducer()
	{
		if (this.producer == null)
		{
			if (this.importType == IMPORT_TEXT)
				createTextFileParser();
			else if (this.importType == IMPORT_XML)
				createXmlFileParser();
		}
		return this.producer;
	}

	/**
	 *	Return the list of columns defined in the file
	 */
	public List<ColumnIdentifier> getFileColumns()
	{
		if (this.inputColumns == null)
		{
			getProducer();
			this.inputColumns = fileParser.getColumnsFromFile();
		}
		return this.inputColumns;
	}

	public void setImportColumns(List<ColumnIdentifier> cols)
		throws Exception
	{
		if (this.producer == null) getProducer();
		this.fileParser.setColumns(cols);
	}
	
	private void createTextFileParser()
	{
		TextFileParser parser = new TextFileParser(inputFile);
		parser.setEncoding(this.generalOptions.getEncoding());
		parser.setContainsHeader(this.textOptions.getContainsHeader());
		parser.setQuoteChar(this.textOptions.getTextQuoteChar());
		parser.setDecodeUnicode(this.textOptions.getDecode());
		parser.setDelimiter(this.textOptions.getTextDelimiter());
		parser.setConnection(this.connection);
		
		ValueConverter converter = new ValueConverter();
		converter.setDefaultDateFormat(this.generalOptions.getDateFormat());
		converter.setDefaultTimestampFormat(this.generalOptions.getTimestampFormat());
		String dec = this.textOptions.getDecimalChar();
		if (dec != null) converter.setDecimalCharacter(dec.charAt(0));
		parser.setValueConverter(converter);
		
		if (this.table != null)
		{
			parser.setTableName(this.table.getTableExpression());
		}
		this.inputColumns = null;
		this.producer = parser;
		this.fileParser = parser;
	}
	
	public File getSourceFile()
	{
		return this.inputFile;
	}
	
	private void createXmlFileParser()
	{
		XmlDataFileParser parser = new XmlDataFileParser(inputFile);
		parser.setEncoding(this.generalOptions.getEncoding());
		//parser.setUseVerboseFormat(this.xmlOptions.getUseVerboseXml());
		this.inputColumns = null;
		this.producer = parser;
		this.fileParser = parser;
	}
	
	/**
	 * Appends text import options to the passed sql command
	 */
	private void appendTextOptions(StringBuilder command, StringBuilder indent)
	{
		if (this.textOptions == null) return;
		appendArgument(command, WbImport.ARG_CONTAINSHEADER, textOptions.getContainsHeader(), indent);
		appendArgument(command, WbImport.ARG_DECODE, textOptions.getDecode(), indent);
		String delim = textOptions.getTextDelimiter();
		if ("\t".equals(delim)) delim = "\\t";
		appendArgument(command, CommonArgs.ARG_DELIM, "'" + delim + "'", indent);
		appendArgument(command, WbImport.ARG_QUOTE, textOptions.getTextQuoteChar(), indent);
		appendArgument(command, CommonArgs.ARG_DECCHAR, textOptions.getDecimalChar(), indent);
		appendArgument(command, WbImport.ARG_FILECOLUMNS, this.fileParser.getColumns(), indent);
	}
	
	private void appendArgument(StringBuilder result, String arg, boolean value, StringBuilder indent)
	{
		appendArgument(result, arg, Boolean.toString(value), indent);
	}
	
	private void appendArgument(StringBuilder result, String arg, String value, StringBuilder indent)
	{
		if (value != null)
		{
			result.append(indent);
			result.append('-');
			result.append(arg);
			result.append('=');
			if (value.indexOf('-') > -1) result.append('"');
			else if ("\"".equals(value)) result.append('\'');
			else if ("\'".equals(value)) result.append('\"');
			result.append(value);
			if (value.indexOf('-') > -1) result.append('"');
			else if ("\"".equals(value)) result.append('\'');
			else if ("\'".equals(value)) result.append('\"');
		}
	}
	
	/**
	 *	Generates a WB SQL command from the current import
	 *  settings
	 */
	public String getWbCommand()
	{
		StringBuilder result = new StringBuilder(150);
		StringBuilder indent = new StringBuilder();
		indent.append('\n');
		for (int i=0; i < WbImport.VERB.length(); i++) indent.append(' ');
		indent.append(' ');
		CommandTester ct = new CommandTester();
		String verb = ct.formatVerb(WbImport.VERB);
		result.append(verb + " -" + WbImport.ARG_FILE + "=");
		String filename = inputFile.getAbsolutePath();
		if (filename.indexOf('-') > -1) result.append('"');
		result.append(StringUtil.replace(filename, "\\", "/"));
		if (filename.indexOf('-') > -1) result.append('"');
		result.append(indent);
		result.append('-');
		result.append(WbImport.ARG_TYPE);
		result.append('=');
		if (this.isXmlImport())
		{
			result.append("xml");
		}
		else 
		{
			result.append("text");
		}
		
		appendArgument(result, WbImport.ARG_TARGETTABLE, this.table.getTableName(), indent);
		appendArgument(result, CommonArgs.ARG_ENCODING, this.generalOptions.getEncoding(), indent);
		appendArgument(result, WbImport.ARG_MODE, this.generalOptions.getMode(), indent);
		if (this.batchSize > 0)
		{
			appendArgument(result, CommonArgs.ARG_BATCHSIZE, Integer.toString(this.batchSize), indent);
		}
		
		appendTextOptions(result, indent);
		
		result.append("\n;");
		
		return result.toString();
	}
	
}
