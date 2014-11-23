/*
 * SchemaDiff.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2004, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db.diff;

import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import workbench.db.DbMetadata;
import workbench.db.TableIdentifier;
import workbench.db.WbConnection;
import workbench.db.report.ReportTable;
import workbench.db.report.TagWriter;
import workbench.resource.ResourceMgr;
import workbench.storage.RowActionMonitor;
import workbench.util.StrBuffer;
import workbench.util.StrWriter;

/**
 * Compare to Schemas for differences in the definition of the tables
 * 
 * @author  support@sql-workbench.net
 */
public class SchemaDiff
{
	public static final String TAG_ADD_TABLE = "add-table";
	public static final String TAG_DROP_TABLE = "drop-table";
	public static final String TAG_REF_CONN = "reference-connection";
	public static final String TAG_TARGET_CONN = "target-connection";
	public static final String TAG_COMPARE_INFO = "compare-settings";
	public static final String TAG_TABLE_PAIR = "table-info";
	public static final String TAG_INDEX_INFO = "include-index";
	public static final String TAG_FK_INFO = "include-foreign-key";
	public static final String TAG_PK_INFO = "include-primary-key";
	
	private WbConnection sourceDb;
	private WbConnection targetDb;
	private ReportTable[] referenceTables;
	private ReportTable[] targetTables;
	private List tablesToDelete;
	private String namespace;
	private String encoding = "UTF-8";
	private boolean diffIndex = true;
	private boolean diffForeignKeys = true;
	private boolean diffPrimaryKeys = true;
	private boolean diffConstraints;
	private RowActionMonitor monitor;
	private boolean cancel = false;
	private String referenceSchema;
	private String targetSchema;
	private List tablesToIgnore;
	private boolean diffComments;
	
	public SchemaDiff()
	{
	}

	/**
	 *	Create a new SchemaDiff for the given connections
	 */
	public SchemaDiff(WbConnection source, WbConnection target)
	{
		this(source, target, null);
	}
	
	/**
	 *	Create a new SchemaDiff for the given connections with the given 
	 *  namespace to be used when writing the XML
	 */
	public SchemaDiff(WbConnection source, WbConnection target, String space)
	{
		sourceDb = source;
		targetDb = target;
		this.namespace = space;
	}
	
	/**
	 * Control whether foreign keys should be compared as well.
	 * The default is to compare foreign keys.
	 */
	public void setIncludeForeignKeys(boolean flag) { this.diffForeignKeys = flag; }

	/**
	 *	Control whether index definitions should be compared as well.
	 *  The default is to compare index definitions
	 */
	public void setIncludeIndex(boolean flag) { this.diffIndex = flag; }
	
	/**
	 * Control whether primary keys should be compared as well.
	 * The default is to compare primary keys.
	 */
	public void setIncludePrimaryKeys(boolean flag) { this.diffPrimaryKeys = flag; }

	/**
	 * Control whether table constraints should be compared as well.
	 * The default is to not compare primary keys.
	 */
	public void setIncludeTableConstraints(boolean flag) { this.diffConstraints = flag; }
	
	public void setIncludeComments(boolean flag) { this.diffComments = flag; }
	
	/**
	 *	Set the {@link workbench.storage.RowActionMonitor} for reporting progress
	 */
	public void setMonitor(RowActionMonitor mon)
	{
		this.monitor = mon;
	}
	
	/**
	 *	Cancel the creation of the XML file
	 *  @see #isCancelled()
	 */
	public void cancel()
	{
		this.cancel = true;
	}

	/**
	 *	Return if the XML generation has been cancelled
	 * @return true if #cancel() has been called
	 */
	public boolean isCancelled()
	{
		return this.cancel;
	}
	
	/** 
	 * Define the tables to be compared. They will be compared based 
	 * on the position in the arrays (i.e. reference at index 0 will be
	 * compared to target at index 0...)
	 * No name matching will take place. Thus it's possible to compare
	 * tables that might have different names but are supposed to be identical
	 * otherwise. The entries in the list are expected to be Strings or {@link workbench.db.TableIdentifier}
	 *
	 * @see #setTables(List)
	 * @see #compareAll()
	 */
	public void setTables(List reference, List target)
		throws SQLException
	{
		if (reference == null) throw new NullPointerException("Source tables may not be null");
		if (target == null) throw new NullPointerException("Target tables may not be null");
		if (reference.size() != target.size()) throw new IllegalArgumentException("Number of source and target tables have to match");
		int count = reference.size();
		this.referenceTables = new ReportTable[count];
		this.targetTables = new ReportTable[count];
		
		if (this.monitor != null)
		{
			this.monitor.setMonitorType(RowActionMonitor.MONITOR_PROCESS_TABLE);
			this.monitor.setCurrentObject(ResourceMgr.getString("MsgDiffRetrieveDbInfo"), -1, -1);
		}
		
		for (int i=0; i < count; i++)
		{
			if (this.cancel) 
			{
				this.targetTables = null;
				this.referenceTables = null;
				break;
			}
			
			Object o = reference.get(i);
			TableIdentifier ref = null;
			
			if (o instanceof TableIdentifier)
			{
				ref = (TableIdentifier)o;
			}
			else if (o != null)
			{
				ref = new TableIdentifier(o.toString());
			}
			if (ref == null) continue;
			
			TableIdentifier tar = null;
			o = target.get(i);
			if (o instanceof TableIdentifier)
			{
				tar = (TableIdentifier)o;
			}
			else if (o != null)
			{
				tar = new TableIdentifier(o.toString());
			}
			this.referenceTables[i] = createReportTableInstance(ref, this.sourceDb);
			if (tar != null)
			{
				this.targetTables[i] = createReportTableInstance(tar, this.targetDb);
			}
		}
	}

	private ReportTable createReportTableInstance(TableIdentifier tbl, WbConnection con)
		throws SQLException
	{
		tbl.adjustCase(con);
		return new ReportTable(tbl, con, this.namespace, diffIndex, diffForeignKeys, diffPrimaryKeys, diffConstraints);
	}
	
	public void setExcludeTables(List tables)
	{
		if (tables == null || tables.size() == 0)
		{
			this.tablesToIgnore = null;
			return;
		}
		int count = tables.size();
		this.tablesToIgnore = new ArrayList(count);
		for (int i=0; i < count; i++)
		{
			String s = (String)tables.get(i);
			this.tablesToIgnore.add(this.sourceDb.getMetadata().adjustObjectnameCase(s));
		}
	}
	
	/**
	 *	Setup this SchemaDiff object to compare all tables that the user
	 *  can access in the reference connection with all matching (=same name)
	 *  tables in the target connection.
	 *  This will retrieve all user tables from the reference (=source)
	 *  connection and will match them to the tables in the target connection.
	 *  
	 *  When using compareAll() drop statements will be created for tables 
	 *  present in the target connection but not existing in the reference
	 *  connection.
	 *
	 * @see #setTables(List, List)
	 * @see #setTables(List)
	 */
	public void compareAll()
		throws SQLException
	{
		if (this.monitor != null)
		{
			this.monitor.setMonitorType(RowActionMonitor.MONITOR_PLAIN);
			this.monitor.setCurrentObject(ResourceMgr.getString("MsgDiffRetrieveDbInfo"), -1, -1);
		}
		
		List refTables = this.sourceDb.getMetadata().getTableList();
		int count = refTables.size();
		List refTableNames = new ArrayList(count);
		
		this.referenceTables = new ReportTable[count];
		this.targetTables = new ReportTable[count];
		DbMetadata meta = this.targetDb.getMetadata();
		
		this.monitor.setMonitorType(RowActionMonitor.MONITOR_PLAIN);
		String msg = ResourceMgr.getString("MsgLoadTableInfo") + " ";
		
		for (int i=0; i < count; i++)
		{
			if (this.cancel) 
			{
				this.targetTables = null;
				this.referenceTables = null;
				break;
			}
			
			TableIdentifier t = (TableIdentifier)refTables.get(i);
			if (this.tablesToIgnore != null && this.tablesToIgnore.contains(t.getTableName())) continue;
			
			if (this.monitor != null)
			{
				this.monitor.setCurrentObject(msg + t.getTableName(), -1, -1);
			}
			this.referenceTables[i] = createReportTableInstance(t, this.sourceDb);
			TableIdentifier tid = new TableIdentifier(t.getTableName());
			if (meta.tableExists(tid))
			{
				this.targetTables[i] = createReportTableInstance(tid, this.targetDb);
			}
			refTableNames.add(t.getTableName());
		}

		if (cancel) return;
		
		this.tablesToDelete = new ArrayList();
		List target= meta.getTableList();
		count = target.size();
		for (int i=0; i < count; i++)
		{
			TableIdentifier t = (TableIdentifier)target.get(i);
			if (!refTableNames.contains(t.getTableName()))
			{
				this.tablesToDelete.add(t);
			}
		}
	}

	/**
	 *	Setup this SchemaDiff object to compare all tables that the user
	 *  can access in the reference connection with all matching (=same name)
	 *  tables in the target connection.
	 *  This will retrieve all user tables from the reference (=source)
	 *  connection and will match them to the tables in the target connection.
	 *  
	 *  When using compareAll() drop statements will be created for tables 
	 *  present in the target connection but not existing in the reference
	 *  connection.
	 *
	 * @see #setTables(List, List)
	 * @see #setTables(List)
	 */
	public void setSchemas(String refSchema, String targetSchema)
		throws SQLException
	{
		if (this.monitor != null)
		{
			this.monitor.setMonitorType(RowActionMonitor.MONITOR_PLAIN);
			this.monitor.setCurrentObject(ResourceMgr.getString("MsgDiffRetrieveDbInfo"), -1, -1);
		}
		this.referenceSchema = refSchema;
		this.targetSchema = targetSchema;
		
		List refTables = this.sourceDb.getMetadata().getTableList(refSchema);
		int count = refTables.size();
		List refTableNames = new ArrayList(count);
		
		this.referenceTables = new ReportTable[count];
		this.targetTables = new ReportTable[count];
		DbMetadata meta = this.targetDb.getMetadata();
		
		this.monitor.setMonitorType(RowActionMonitor.MONITOR_PLAIN);
		String msg = ResourceMgr.getString("MsgLoadTableInfo") + " ";
		
		for (int i=0; i < count; i++)
		{
			if (this.cancel) 
			{
				this.targetTables = null;
				this.referenceTables = null;
				break;
			}
			
			TableIdentifier t = (TableIdentifier)refTables.get(i);
			if (this.tablesToIgnore != null && this.tablesToIgnore.contains(t.getTableName())) continue;
			
			if (this.monitor != null)
			{
				this.monitor.setCurrentObject(msg + t.getTableName(), -1, -1);
			}
			this.referenceTables[i] = createReportTableInstance(t, this.sourceDb);
			TableIdentifier tid = new TableIdentifier(targetSchema, t.getTableName());
			tid.adjustCase(targetDb);
			if (meta.tableExists(tid))
			{
				this.targetTables[i] = createReportTableInstance(tid, this.targetDb);
			}
			refTableNames.add(t.getTableName());
		}

		if (cancel) return;
		
		this.tablesToDelete = new ArrayList();
		List target= meta.getTableList();
		count = target.size();
		for (int i=0; i < count; i++)
		{
			TableIdentifier t = (TableIdentifier)target.get(i);
			if (tablesToIgnore != null && tablesToIgnore.contains(t.getTableName())) continue;
			if (!refTableNames.contains(t.getTableName()))
			{
				this.tablesToDelete.add(t);
			}
		}
	}
	
	/**
	 * Define the reference tables to be compared with the matching 
	 * tables (based on the name) in the target connection. The list 
	 * has to contain objects of type {@link workbench.db.TableIdentifier}
	 *
	 * @see #setTables(List, List)
	 * @see #compareAll()
	 */
	public void setTables(List reference)
		throws SQLException
	{
		if (reference == null) throw new NullPointerException("Source tables may not be null");
		int count = reference.size();
		this.referenceTables = new ReportTable[count];
		this.targetTables = new ReportTable[count];
		this.tablesToDelete = null;
		DbMetadata meta = this.targetDb.getMetadata();
		
		this.monitor.setMonitorType(RowActionMonitor.MONITOR_PLAIN);
		String msg = ResourceMgr.getString("MsgLoadTableInfo") + " ";
		
		for (int i=0; i < count; i++)
		{
			Object o = reference.get(i);
			TableIdentifier ref = null;

			if (o instanceof TableIdentifier)
			{
				ref = (TableIdentifier)o;
			}
			else if (o != null)
			{
				ref = new TableIdentifier(o.toString());
			}
			if (ref == null) continue;
		
			if (this.monitor != null)
			{
				this.monitor.setCurrentObject(msg + ref.getTableName(), -1, -1);
			}
			
			this.referenceTables[i] = createReportTableInstance(ref, this.sourceDb);

			TableIdentifier tid = new TableIdentifier(ref.getTableName());
			if (meta.tableExists(tid))
			{
				this.targetTables[i] = createReportTableInstance(tid, this.targetDb);
			}
		}
	}
	
	/**
	 *	Return the XML that describes how the target schema needs to be 
	 *  modified in order to get the same structure as the reference schema.
	 *
	 *	For this, each defined table in the reference schema will be compared
	 *  to the corresponding table in the target schema. 
	 *
	 *  @see TableDiff#getMigrateTargetXml()
	 */
	public String getMigrateTargetXml()
	{
		StrWriter writer = new StrWriter(5000);
		try
		{
			this.writeXml(writer);
		}
		catch (Exception e)
		{
			// cannot happen
		}
		return writer.toString();
	}

	/**
	 *	Return the encoding that is used in the encoding attribute of the XML tag
	 */
	public String getEncoding()
	{
		return encoding;
	}
	
	/**
	 *	Set the encoding that is used for writing the XML. This will
	 *  be put into the <?xml tag at the beginning of the generated XML
	 */
	public void setEncoding(String encoding)
	{
		this.encoding = encoding;
	}

	/**
	 *	Write the XML of the schema differences to the supplied writer.
	 *  This writes some meta information about the compare, and then 
	 *  creates a {@link TableDiff} object for each pair of tables that
	 *  needs to be compared. The output of {@link TableDiff#getMigrateTargetXml()}
	 *  will then be written into the writer.
	 */
	public void writeXml(Writer out)
		throws IOException
	{
		if (referenceTables == null) throw new NullPointerException("Source tables may not be null");
		if (targetTables == null) throw new NullPointerException("Target tables may not be null");
		if (referenceTables.length != targetTables.length) throw new IllegalArgumentException("Number of source and target tables have to match");
		
		StrBuffer indent = new StrBuffer("  ");
		StrBuffer tblIndent = new StrBuffer("    ");
		TagWriter tw = new TagWriter(this.namespace);
		out.write("<?xml version=\"1.0\" encoding=\"");
		out.write(this.encoding);
		out.write("\"?>\n");
		
		if (this.monitor != null)
		{
			this.monitor.setMonitorType(RowActionMonitor.MONITOR_PROCESS_TABLE);
		}
		
		writeTag(out, null, "schema-diff", true);
		writeDiffInfo(out);
		int count = this.referenceTables.length;
		for (int i=0; i < count; i++)
		{
			if (this.cancel) 
			{
				break;
			}
			
			if (this.referenceTables[i] == null) continue;
			
			if (this.monitor != null)
			{
				this.monitor.setCurrentObject(this.referenceTables[i].getTable().getTableExpression(), i+1, count);
			}
			
			if (this.targetTables[i] == null)
			{
				out.write("\n");
				writeTag(out, indent, TAG_ADD_TABLE, true, "name", referenceTables[i].getTable().getTableName());
				StrBuffer s = referenceTables[i].getXml(tblIndent);
				s.writeTo(out);
				writeTag(out, indent, TAG_ADD_TABLE, false);
			}
			else
			{
				TableDiff d = new TableDiff(this.referenceTables[i], this.targetTables[i]);
				d.setCompareComments(this.diffComments);
				d.setIndent(indent);
				d.setTagWriter(tw);
				StrBuffer s = d.getMigrateTargetXml();
				if (s.length() > 0)
				{
					out.write("\n");
					s.writeTo(out);
				}
			}
		}
		if (this.cancel) return;
		
		this.appendDropTables(out, indent, tw);
		out.write("\n");
		writeTag(out, null, "schema-diff", false);
	}
	
	private void appendDropTables(Writer out, StrBuffer indent, TagWriter tw)
		throws IOException
	{
		if (this.tablesToDelete == null || this.tablesToDelete.size() == 0) return;
		out.write("\n");
		writeTag(out, indent, TAG_DROP_TABLE, true);
		Iterator itr = this.tablesToDelete.iterator();
		StrBuffer myindent = new StrBuffer(indent);
		myindent.append("  ");
		while (itr.hasNext())
		{
			TableIdentifier t = (TableIdentifier)itr.next();
			writeTagValue(out, myindent, ReportTable.TAG_TABLE_NAME, t.getTableName());
		}
		writeTag(out, indent, TAG_DROP_TABLE, false);
	}
	
	private void writeDiffInfo(Writer out)
		throws IOException
	{
		StrBuffer indent = new StrBuffer("  ");
		StrBuffer indent2 = new StrBuffer("    ");
		writeTag(out, indent, TAG_REF_CONN, true);
		StrBuffer info = this.sourceDb.getDatabaseInfoAsXml(indent2, this.namespace);
		info.writeTo(out);
		writeTag(out, indent, TAG_REF_CONN, false);
		out.write("\n");
		out.write("  <!-- If the target connection is modified according to the  -->\n");
		out.write("  <!-- defintions in this file, then its structure will be    -->\n");
		out.write("  <!-- the same as the reference connection -->\n");
		writeTag(out, indent, TAG_TARGET_CONN, true);
		info = this.targetDb.getDatabaseInfoAsXml(indent2, this.namespace);
		info.writeTo(out);
		writeTag(out, indent, TAG_TARGET_CONN, false);
		out.write("\n");
		
		info = new StrBuffer();
		TagWriter tw = new TagWriter(this.namespace);
		
		tw.appendOpenTag(info, indent, TAG_COMPARE_INFO);
		info.append('\n');
		tw.appendTag(info, indent2, TAG_INDEX_INFO, this.diffIndex);
		tw.appendTag(info, indent2, TAG_FK_INFO, this.diffForeignKeys);
		tw.appendTag(info, indent2, TAG_PK_INFO, this.diffPrimaryKeys);
		info.append('\n');
		
		if (this.referenceSchema != null && this.targetSchema != null)
		{
			tw.appendTag(info, indent2, "reference-schema", this.referenceSchema);
			tw.appendTag(info, indent2, "target-schema", this.targetSchema);
		}
		int count = this.referenceTables.length;
		String attr[] = new String[] { "referenceTable", "compareTo" };
		String tbls[] = new String[2];
		for (int i=0; i < count; i++)
		{
			// check for ignored tables
			if (this.referenceTables[i] == null) continue;
			
			tbls[0] = this.referenceTables[i].getTable().getTableName();
			tbls[1] = this.targetTables[i] == null ? "" : this.targetTables[i].getTable().getTableName();
			tw.appendOpenTag(info, indent2, TAG_TABLE_PAIR, attr, tbls, false);
			info.append("/>\n");
		}
		tw.appendCloseTag(info, indent, TAG_COMPARE_INFO);

		info.writeTo(out);
	}
	
	private void writeTag(Writer out, StrBuffer indent, String tag, boolean isOpeningTag)
		throws IOException
	{
		writeTag(out, indent, tag, isOpeningTag, null, null);
	}
	private void writeTag(Writer out, StrBuffer indent, String tag, boolean isOpeningTag, String attr, String attrValue)
		throws IOException
	{
		if (indent != null) indent.writeTo(out);;
		if (isOpeningTag)
		{
			out.write("<");
		}
		else
		{
			out.write("</");
		}
		if (this.namespace != null)
		{
			out.write(namespace);
			out.write(":");
		}
		out.write(tag);
		if (isOpeningTag && attr != null)
		{
			out.write(' ');
			out.write(attr);
			out.write("=\"");
			out.write(attrValue);
			out.write('"');
		}
		out.write(">\n");
	}
	
	private void writeTagValue(Writer out, StrBuffer indent, String tag, String value)
		throws IOException
	{
		if (indent != null) indent.writeTo(out);;
		out.write("<");
		if (this.namespace != null)
		{
			out.write(namespace);
			out.write(":");
		}
		out.write(tag);
		out.write(">");
		out.write(value);
		out.write("</");
		if (this.namespace != null)
		{
			out.write(namespace);
			out.write(":");
		}
		out.write(tag);
		out.write(">\n");
	}

}