/*
 * XmlDataFileParser.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2008, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db.importer;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import workbench.db.ColumnIdentifier;
import workbench.db.TableIdentifier;
import workbench.db.WbConnection;
import workbench.db.exporter.XmlRowDataConverter;
import workbench.db.importer.modifier.ImportValueModifier;
import workbench.interfaces.JobErrorHandler;
import workbench.resource.ResourceMgr;
import workbench.util.ExceptionUtil;
import workbench.interfaces.ImportFileParser;
import workbench.log.LogMgr;
import workbench.util.FileUtil;
import workbench.util.MessageBuffer;
import workbench.util.SqlUtil;
import workbench.util.StringUtil;
import workbench.util.ValueConverter;
import workbench.util.WbFile;
import workbench.util.WbStringTokenizer;

/**
 *
 * @author  support@sql-workbench.net
 */
public class XmlDataFileParser
	extends DefaultHandler
	implements RowDataProducer, ImportFileParser
{
	private String sourceDirectory;
	private File inputFile;
	private String tableName;
	private String tableNameFromFile;
	
	private int currentRowNumber = 1;
	private int colCount;
	private int realColCount;

	private List<ColumnIdentifier> columnsToImport;
	private ColumnIdentifier[] columns;
	private String encoding = "UTF-8";

	private Object[] currentRow;
	private RowDataReceiver receiver;
	private boolean ignoreCurrentRow = false;
	private boolean abortOnError = false;
	private boolean checkDependencies = false;
	private boolean[] warningAdded;
	private JobErrorHandler errorHandler;
	private boolean verboseFormat = true;
	private boolean formatKnown = false;
	private String missingColumn;
	private MessageBuffer messages;
	private String extensionToUse;
	
	private int currentColIndex = 0;
	private int realColIndex = 0;
	private long columnLongValue = 0;
	private String columnDataFile = null;
	private boolean isNull = false;
	private StringBuilder chars;
	private boolean keepRunning;
	private boolean regularStop;
	private String rowTag = XmlRowDataConverter.LONG_ROW_TAG;
	private String columnTag = XmlRowDataConverter.LONG_COLUMN_TAG;

	private boolean hasErrors = false;
	private boolean hasWarnings = false;
	
	private SAXParser saxParser;
	private ImportFileHandler fileHandler = new ImportFileHandler();
	private WbConnection dbConn;
	
	private ValueConverter converter = new ValueConverter();
	private ImportValueModifier valueModifier;
	
	public XmlDataFileParser()
	{
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(false);
		try
		{
			saxParser = factory.newSAXParser();
		}
		catch (Exception e)
		{
			// should not happen!
			LogMgr.logError("XmlDataFileParser.<init>", "Error creating XML parser", e);
		}
	}
	public XmlDataFileParser(File inputFile)
	{
		this();
		this.inputFile = inputFile;
	}

	public void setValueModifier(ImportValueModifier mod)
	{
		this.valueModifier = mod;
	}
	
	public ImportFileHandler getFileHandler()
	{
		return this.fileHandler;
	}
	
	public String getColumns()
	{
		return StringUtil.listToString(this.columnsToImport, ',', false);
	}

	public String getLastRecord()
	{
		return null;
	}
	
	public void setCheckDependencies(boolean flag)
	{
		this.checkDependencies = flag;
	}
	
	public boolean hasErrors() { return this.hasErrors; }
	public boolean hasWarnings() 
	{ 
		if (this.hasWarnings) return true;
		if (this.warningAdded == null) return false;
		for (boolean b : warningAdded)
		{
			if (b) return true;
		}
		return false;
	}
	
	public void setValueConverter(ValueConverter convert)
	{
		this.converter = convert;
	}
	
	public void setColumns(String columnList)
		throws SQLException
	{
		if (columnList != null && columnList.trim().length() > 0)
		{
			WbStringTokenizer tok = new WbStringTokenizer(columnList, ",");
			this.columnsToImport = new ArrayList<ColumnIdentifier>();
			while (tok.hasMoreTokens())
			{
				String col = tok.nextToken();
				if (col == null) continue;
				col = col.trim();
				if (col.length() == 0) continue;
				ColumnIdentifier ci = new ColumnIdentifier(col);
				this.columnsToImport.add(ci);
			}
		}
		else
		{
			this.columnsToImport = null;
		}
		checkImportColumns();
	}
	
	/**	 Define the columns to be imported
	 */
	public void setColumns(List<ColumnIdentifier> cols)
		throws SQLException
	{
		if (cols != null && cols.size() > 0)
		{
			this.columnsToImport = new ArrayList<ColumnIdentifier>(cols.size());
			Iterator<ColumnIdentifier> itr = cols.iterator();
			while (itr.hasNext())
			{
				ColumnIdentifier id = itr.next();
				if (!id.getColumnName().equals(RowDataProducer.SKIP_INDICATOR))
				{
					this.columnsToImport.add(id);
				}
			}
		}
		else
		{
			this.columnsToImport = null;
		}
		checkImportColumns();
	}

	public void setConnection(WbConnection conn)
	{
		this.dbConn = conn;
	}

	/**
	 * Check if all columns defined for the import (through the table definition
	 * as part of the XML file, or passed by the user on the command line) are
	 * actually available in the target table. 
	 * For this all columns of the target table are retrieved from the database,
	 * and each column that has been defined through setColumns() is checked 
	 * whether it exists there. Columns that are not found are dropped from
	 * the list of import columns
	 * If continueOnError == true, a warning is added to the messages. Otherwise
	 * an Exception is thrown.
	 */
	public void checkTargetColumns()
		throws SQLException
	{
		if (this.dbConn == null) return;
		if (this.columns == null) return;
		TableIdentifier tbl = new TableIdentifier(this.tableName == null ? this.tableNameFromFile : this.tableName);
		if (!this.dbConn.getMetadata().tableExists(tbl))
		{
			String msg = ResourceMgr.getFormattedString("ErrImportTableNotFound", tbl.getTableName());
			this.messages.append(msg);
			this.messages.appendNewLine();
			throw new SQLException("Table '" + tbl.getTableName() + "' not found!");
		}
		List<ColumnIdentifier> tableCols = this.dbConn.getMetadata().getTableColumns(tbl);
		List<ColumnIdentifier> validCols = new LinkedList<ColumnIdentifier>();

		for (int colIndex=0; colIndex < this.columns.length; colIndex++)
		{
			int i = tableCols.indexOf(this.columns[colIndex]);
			
			if (i != -1)
			{
				// Use the column definition retrieved from the database
				// to make sure we are using the correct data types when converting the input (String) values
				// this is also important to get quoting of column names
				// with special characters correctly (as this is handled by DbMetadata already
				// but the columns retrieved from the XML file are not quoted correctly)
				ColumnIdentifier tc = tableCols.get(i);
				this.columns[colIndex] = tc;
				validCols.add(tc);
			}
			else
			{
				String errorColumn = (this.columns[colIndex] != null ? this.columns[colIndex].getColumnName() : "n/a");
				String msg = ResourceMgr.getString("ErrImportColumnNotFound");
				msg = StringUtil.replace(msg, "%column%", errorColumn);
				msg = StringUtil.replace(msg, "%table%", tbl.getTableExpression());
				this.messages.append(msg);
				this.messages.appendNewLine();
				if (this.abortOnError)
				{
					this.hasErrors = true;
					throw new SQLException("Column " + errorColumn + " not found in target table");
				}
				else
				{
					this.hasWarnings = true;
					LogMgr.logWarning("XmlDataFileParser.checkTargetColumns()", msg);
				}
			}
		}

		// Make sure we are using the columns collected during the check
		if (validCols.size() != columns.length)
		{
			this.columnsToImport = validCols;
			this.realColCount = this.columnsToImport.size();
		}
	}
	
	private void checkImportColumns()
		throws SQLException
	{
		if (this.columnsToImport == null) 
		{
			this.realColCount = this.colCount;
			return;
		}
		
		this.missingColumn = null;
		
		try
		{
			if (this.columns == null) this.readXmlTableDefinition();
		}
		catch (Throwable e)
		{
			LogMgr.logError("XmlDataFileParser.checkImportColumns()", "Error reading table definition from XML file", e);
			this.hasErrors = true;
			throw new SQLException("Could not read table definition from XML file");
		}
		
		for (ColumnIdentifier c : columnsToImport)
		{
			if (!this.containsColumn(c)) 
			{
				this.missingColumn = c.getColumnName();
				this.hasErrors = true;
				throw new SQLException("Import column " + c.getColumnName() + " not present in input file!");
			}
		}
		this.realColCount = this.columnsToImport.size();
	}
	
	/**
	 *	Returns the first column from the import columns
	 *  that is not found in the import file
	 *	@see #setColumns(String)
	 *  @see #setColumns(List)
	 */
	public String getMissingColumn() { return this.missingColumn; }
	
	private boolean containsColumn(ColumnIdentifier col)
	{
		if (this.columns == null) return false;
		for (int i=0; i<this.columns.length; i++)
		{
			if (this.columns[i].equals(col)) return true;
		}
		return false;
	}
	
	public void setTableName(String aName)
	{
		this.tableName = aName;
	}

	public List<ColumnIdentifier> getColumnsFromFile()
	{
		try
		{
			if (this.columns == null) this.readXmlTableDefinition();
		}
		catch (IOException e)
		{
			return Collections.emptyList();
		}
		catch (SAXException e)
		{
			return Collections.emptyList();
		}
		ArrayList<ColumnIdentifier> result = new ArrayList<ColumnIdentifier>(this.columns.length);
		for (int i=0; i < this.columns.length; i++)
		{
			result.add(this.columns[i]);
		}
		return result;
	}

	private void detectTagFormat()
	{
		try
		{
			fileHandler.setMainFile(this.inputFile, this.encoding);
			XmlTableDefinitionParser tableDef = new XmlTableDefinitionParser(this.fileHandler);
			detectTagFormat(tableDef);
		}
		catch (Exception e)
		{
			LogMgr.logError("XmlDataFileParser", "Could not detect XML tag format. Assuming 'verbose'", e);
			this.setUseVerboseFormat(true);
		}
	}
	
	private void detectTagFormat(XmlTableDefinitionParser tableDef)
	{
		String format = tableDef.getTagFormat();
		if (format != null)
		{
			if (XmlRowDataConverter.KEY_FORMAT_LONG.equals(format))
			{
				this.setUseVerboseFormat(true);
			}
			else if (XmlRowDataConverter.KEY_FORMAT_SHORT.equals(format))
			{
				this.setUseVerboseFormat(false);
			}
		}
	}
	
	private void readXmlTableDefinition()
		throws IOException, SAXException
	{
		fileHandler.setMainFile(this.inputFile, this.encoding);
		
		XmlTableDefinitionParser tableDef = new XmlTableDefinitionParser(this.fileHandler);
		this.columns = tableDef.getColumns();
		this.colCount = this.columns.length;
		this.tableNameFromFile = tableDef.getTableName();
		this.warningAdded = new boolean[this.colCount];
		detectTagFormat(tableDef);
	}

	public String getSourceFilename()
	{
		if (this.inputFile == null) return null;
		return this.inputFile.getAbsolutePath();
	}
	
	public void setSourceFile(File file)
	{
		this.sourceDirectory = null;
		this.inputFile = file;
	}
	
	public void setSourceExtension(String ext)
	{
		this.extensionToUse = ext;
	}
	
	public void setSourceDirectory(String dir)
	{
		File f = new File(dir);
		if (!f.isDirectory()) throw new IllegalArgumentException(dir + " is not a directory");
		this.sourceDirectory = dir;
		this.inputFile = null;
	}
	
	public String getSourceDirectory()
	{
		return this.sourceDirectory;
	}
	
	public void setAbortOnError(boolean flag)
	{
		this.abortOnError = flag;
	}
	
	private void processOneFile()
		throws Exception
	{
		this.keepRunning = true;
		this.regularStop = false;
			
		// readTableDefinition relies on the fileHandler, so this 
		// has to be called after initializing the fileHandler
		if (this.columns == null) this.readXmlTableDefinition();
		if (!this.formatKnown)
		{
			detectTagFormat();
		}
		
		if (this.columnsToImport == null)
		{
			this.realColCount = this.colCount;
		}
		else
		{
			this.realColCount = this.columnsToImport.size();
		}
		
		// Re-initialize the reader in case we are reading from a ZIP archive
		// because readTableDefinition() can change the file handler
		this.fileHandler.setMainFile(this.inputFile, this.encoding);
		
		this.messages = new MessageBuffer();
		this.sendTableDefinition();
		Reader in = null;
		boolean finished = false;
		
		try
		{
			in = this.fileHandler.getMainFileReader();
			InputSource source = new InputSource(in);
			saxParser.parse(source, this);
		}
		catch (ParsingInterruptedException e)
		{
			if (this.regularStop)
			{
				this.receiver.importFinished();
			}
			else
			{
				this.receiver.importCancelled();
				this.hasErrors = true;
			}
			finished = true;
		}
		catch (Exception e)
		{
		  String msg = "Error during parsing of data row: " + (this.currentRowNumber) + 
				  ", column: " + this.currentColIndex + 
				  ", current data: " + (this.chars == null ? "<n/a>" : "[" + this.chars.toString() + "]" ) + 
				  ", message: " + ExceptionUtil.getDisplay(e);
			LogMgr.logWarning("XmlDataFileParser.processOneFile()", msg);
			this.messages.append(msg);
			this.messages.appendNewLine();
			this.receiver.tableImportError();
			throw e;
		}
		finally
		{
			FileUtil.closeQuitely(in);
			if (!finished)
			{
				this.receiver.importFinished();
			}
		}
	}

	private void reset()
	{
		messages = new MessageBuffer();
		tableName = null;
		tableNameFromFile = null;
		ignoreCurrentRow = false;
		currentColIndex = 0;
		realColIndex = 0;
		columnLongValue = 0;
		isNull = false;
		chars = null;
		columns = null;
		columnsToImport = null;
		keepRunning = true;
	}
	
	private void processDirectory()
		throws Exception
	{
		File dir = new File(this.sourceDirectory);
		boolean verbose = this.verboseFormat;
		if (this.extensionToUse == null) this.extensionToUse = ".xml";
		
		FileNameSorter sorter = new FileNameSorter(this.dbConn, dir, extensionToUse, new XmlTableNameResolver(encoding));
		List<WbFile> toProcess = null;
		if (this.checkDependencies) 
		{
			try
			{
				toProcess = sorter.getSortedList();
			}
			catch (CycleErrorException e)
			{
				hasErrors = true;
				LogMgr.logError("XmlDataFileParser.processDirectory()", "Error when checking dependencies", e);
				throw e;
			}
		}
		else
		{
			toProcess = sorter.getFiles();
		}
		
		for (WbFile sourceFile : toProcess)
		{
			if (!this.keepRunning) break;
			try
			{
				this.inputFile = sourceFile;
				this.reset();

				// readTableDefinition() might reset the verbose 
				// flag if a new XML structure is used
				// this ensures, that the flag specified by the 
				// user will be used for files that do not have the 
				// flag in the meta-data tag
				this.verboseFormat = verbose;
				this.processOneFile();
			}
			catch (ParsingInterruptedException e)
			{
				// cancel the import
				break;
			}
			catch (Exception e)
			{
				if (this.abortOnError) throw e;
			}
		}
	}
	
	public void start()
		throws Exception
	{
		this.hasErrors = false;
		this.hasWarnings = false;
		this.keepRunning = true;
		
		this.receiver.setTableCount(-1); // clear multi-table flag in receiver
		this.receiver.setCurrentTable(-1);		
		
		try
		{
			if (this.sourceDirectory == null)
			{
				processOneFile();
			}
			else 
			{
				processDirectory();
			}
		}
		finally
		{
			try { this.fileHandler.done(); } catch (Throwable th) {}
		}
	}

	public void stop()
	{
		this.keepRunning = false;
		this.regularStop = true;
	}
	
	public boolean isCancelled()
	{
		return !this.keepRunning && !regularStop;
	}
	
	public void cancel()
	{
		this.keepRunning = false;
		this.regularStop = false;
	}

	private void clearRowData()
	{
		for (int i=0; i < this.realColCount; i++)
		{
			this.currentRow[i] = null;
		}
		this.currentColIndex = 0;
		this.realColIndex = 0;
	}

	public String getEncoding() { return this.encoding; } 
	public void setEncoding(String enc) { this.encoding = enc; }

	public void setReceiver(RowDataReceiver aReceiver)
	{
		this.receiver = aReceiver;
	}

	public void startDocument()
		throws SAXException
	{
		Thread.yield();
		if (!this.keepRunning) throw new ParsingInterruptedException();
	}

	public void endDocument()
		throws SAXException
	{
		Thread.yield();
		if (!this.keepRunning) throw new ParsingInterruptedException();
	}

	public void startElement(String namespaceURI, String sName, String qName, Attributes attrs)
		throws SAXException
	{
		Thread.yield();
		if (!this.keepRunning) throw new ParsingInterruptedException();
		
		if (qName.equals(this.rowTag))
		{
			// row definition ended, start a new row
			this.clearRowData();
			this.chars = null;
		}
		else if (qName.equals(this.columnTag))
		{
			this.chars = new StringBuilder();
			String attrValue = attrs.getValue(XmlRowDataConverter.ATTR_LONGVALUE);
			if (attrValue != null)
			{
				try
				{
					columnLongValue = Long.parseLong(attrValue);
				}
				catch (NumberFormatException e)
				{
					LogMgr.logError("XmlDataFileParser.startElement()", "Error converting longvalue", e);
				}
			}
			attrValue = attrs.getValue(XmlRowDataConverter.ATTR_NULL);
			this.isNull = "true".equals(attrValue);
			columnDataFile = attrs.getValue(XmlRowDataConverter.ATTR_DATA_FILE);
		}
		else
		{
			this.chars = null;
		}
	}

	public void endElement(String namespaceURI, String sName, String qName)
		throws SAXException
	{
		if (!this.keepRunning) throw new ParsingInterruptedException();
		if (qName.equals(this.rowTag))
		{
			if (!this.receiver.shouldProcessNextRow())
			{
				this.receiver.nextRowSkipped();
			}
			else 
			{
				if (!this.ignoreCurrentRow)
				{
					try
					{
						this.sendRowData();
					}
					catch (Exception e)
					{
						// don't need to log the error as sendRowData() has already done that.
						if (this.abortOnError) throw new ParsingInterruptedException();
					}
				}
			}
			this.ignoreCurrentRow = false;
			this.currentRowNumber ++;
		}
		else if (qName.equals(this.columnTag))
		{
			this.buildColumnData();
			this.currentColIndex ++;
		}
		this.chars = null;
	}

	public void characters(char buf[], int offset, int len)
		throws SAXException
	{
		Thread.yield();
		if (!this.keepRunning) throw new ParsingInterruptedException();
		if (chars != null)
		{
			this.chars.append(buf, offset, len);
		}
	}

	/**	Only implemented to have even more possibilities for cancelling the import */
	public void ignorableWhitespace(char[] ch,int start,int length)
    throws SAXException
	{
		Thread.yield();
		if (!this.keepRunning) throw new ParsingInterruptedException();
	}
	
	public void processingInstruction(String target,String data)
		throws SAXException
	{
		Thread.yield();
		if (!this.keepRunning) throw new ParsingInterruptedException();
	}

	public void error(SAXParseException e)
		throws SAXParseException
	{
		String msg = "XML Parse error in line=" + e.getLineNumber() + ",data-row=" + (this.currentRowNumber);
		LogMgr.logError("XmlDataFileParser.error()", msg, e);
		this.ignoreCurrentRow = true;
	}

	public void fatalError(SAXParseException e)
		throws SAXParseException
	{
		String msg = "Fatal XML parse error in line=" + e.getLineNumber() + ",data-row=" + (this.currentRowNumber) + "\nRest of file will be ignored!";
		LogMgr.logError("XmlDataFileParser.fatalError()", msg, e);
		this.ignoreCurrentRow = true;
	}

	// dump warnings too
	public void warning(SAXParseException err)
		throws SAXParseException
	{
		this.messages.append(ExceptionUtil.getDisplay(err));
		this.messages.appendNewLine();
		if (!this.keepRunning) throw err;
	}

	/**
	 *	Creates the approriate column data object and puts it
	 *	into rowData[currentColIndex]
	 *  {@link workbench.util.ValueConverter} is not used because
	 *  for most of the datatypes we have some special processing here
	 *  Date and time can be initialized through the long value in the XML file
	 *  Numeric types contain the actual class to be used {@link #createNumericType(String, String)}
	 */
	private void buildColumnData()
		throws ParsingInterruptedException
	{
		if (this.columnsToImport != null && !this.columnsToImport.contains(this.columns[this.currentColIndex])) return;
		this.currentRow[this.realColIndex] = null;

		if (!this.receiver.shouldProcessNextRow()) return;
		
		// the isNull flag will be set by the startElement method
		// as that is an attribute of the tag
		if (this.isNull)
		{
			this.realColIndex ++;
			return;
		}

		String value = this.chars.toString();
		if (this.valueModifier != null)
		{
			value = this.valueModifier.modifyValue(this.columns[this.realColIndex], value);
		}
		
		int type = this.columns[this.realColIndex].getDataType();
		try
		{
			if (SqlUtil.isCharacterType(type))
			{
				// if clobs are exported as external files, than we'll have a filename in the
				// attribute (just like with BLOBS)
				if (this.columnDataFile == null)
				{
					this.currentRow[this.realColIndex] = value;
				}
				else
				{
					String fileDir = this.inputFile.getParent();
					this.currentRow[this.realColIndex] = new File(fileDir, columnDataFile);
				}
			}
			else if (SqlUtil.isBlobType(type))
			{
				String fileDir = this.inputFile.getParent();
				this.currentRow[this.realColIndex] = new File(fileDir, columnDataFile);
			}
			else if (SqlUtil.isDateType(type))
			{
				// For Date types we don't need the ValueConverter as already we 
				// have a suitable long value that doesn't need parsing
				java.sql.Date d = new java.sql.Date(this.columnLongValue);
				if (type == Types.TIMESTAMP)
				{
					this.currentRow[this.realColIndex] = new java.sql.Timestamp(d.getTime());
				}
				else
				{
					this.currentRow[this.realColIndex] = d;
				}
			}
			else
			{
				// for all other types we can use the ValueConverter
				this.currentRow[this.realColIndex] = converter.convertValue(value, type);
			}
		}
		catch (Exception e)
		{
			String msg = ResourceMgr.getString("ErrConvertError");
			msg = StringUtil.replace(msg, "%type%", SqlUtil.getTypeName(this.columns[realColIndex].getDataType()));
			msg = StringUtil.replace(msg, "%column%", this.columns[realColIndex].getColumnName());
			msg = StringUtil.replace(msg, "%error%", e.getMessage());
			msg = StringUtil.replace(msg, "%value%", value);
			msg = StringUtil.replace(msg, "%row%", Integer.toString(this.currentRowNumber));
			
			this.messages.append(msg);
			this.messages.appendNewLine();
			
			if (this.abortOnError)
			{
				LogMgr.logError("XmlDataFileParser.buildColumnData()", msg, e);
				this.hasErrors = true;
				throw new ParsingInterruptedException();				
			}
			else
			{
				this.messages.append(ResourceMgr.getString("ErrConvertWarning"));
				this.hasWarnings = true;
				LogMgr.logWarning("XmlDataFileParser.buildColumnData()", msg, null);
			}
		}
		
		this.realColIndex ++;
	}

	private TableIdentifier getTargetTable()
	{
		TableIdentifier tbl = null;
		if (this.tableName == null)
		{
			tbl = new TableIdentifier(this.tableNameFromFile);
		}
		else
		{
			tbl = new TableIdentifier(this.tableName);
		}
		return tbl;
	}
	
	private void sendTableDefinition()
		throws SQLException
	{
		try
		{
			TableIdentifier tbl = getTargetTable();
			
			checkTargetColumns();
			if (this.columnsToImport == null)
			{
				this.receiver.setTargetTable(tbl, this.columns);
			}
			else
			{
				ColumnIdentifier[] cols = new ColumnIdentifier[this.realColCount];
				int index = 0;
				for (int i=0; i < this.colCount; i++)
				{
					if (this.columnsToImport.contains(this.columns[i]))
					{
						cols[index] = this.columns[i];
						index ++;
					}
				}
				this.receiver.setTargetTable(tbl, cols);
			}
			this.currentRow = new Object[this.realColCount];
		}
		catch (SQLException e)
		{
			this.currentRow = null;
			this.hasErrors = true;
			throw e;
		}
	}

	private void sendRowData()
		throws SAXException, Exception
	{
		if (this.receiver != null)
		{
			try
			{
				this.receiver.processRow(this.currentRow);
			}
			catch (Exception e)
			{
				LogMgr.logError("XmlDataFileParser.sendRowData()", "Error when sending row data to receiver", e);
				if (this.abortOnError) 
				{
					this.hasErrors = true;
					throw e;
				}
				this.hasWarnings = true;
				if (this.errorHandler != null)
				{
					int choice = errorHandler.getActionOnError(this.currentRowNumber + 1, null, null, ExceptionUtil.getDisplay(e, false));
					if (choice == JobErrorHandler.JOB_ABORT) throw e;
					if (choice == JobErrorHandler.JOB_IGNORE_ALL) 
					{
						this.abortOnError = false;
					}
				}
				
			}
		}
		if (!this.keepRunning) throw new ParsingInterruptedException();
	}

	public MessageBuffer getMessages()
	{
		return this.messages;
	}

	private void setUseVerboseFormat(boolean flag)
	{
		this.formatKnown = true;
		this.verboseFormat = flag;
		if (this.verboseFormat)
		{
			rowTag = XmlRowDataConverter.LONG_ROW_TAG;
			columnTag = XmlRowDataConverter.LONG_COLUMN_TAG;
		}
		else
		{
			rowTag = XmlRowDataConverter.SHORT_ROW_TAG;
			columnTag = XmlRowDataConverter.SHORT_COLUMN_TAG;
		}
	}

    public void setErrorHandler(JobErrorHandler handler) 
		{
			this.errorHandler = handler;
    }
	
}