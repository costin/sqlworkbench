/*
 * SqlRowDataConverter.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2004, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: info@sql-workbench.net
 *
 */
package workbench.db.exporter;

import java.sql.SQLException;
import java.util.List;

import workbench.db.ColumnIdentifier;
import workbench.db.DbMetadata;
import workbench.db.TableIdentifier;
import workbench.log.LogMgr;
import workbench.storage.DmlStatement;
import workbench.storage.ResultInfo;
import workbench.storage.RowData;
import workbench.storage.StatementFactory;
import workbench.util.StrBuffer;

/**
 *
 * @author  info@sql-workbench.net
 */
public class SqlRowDataConverter
	extends RowDataConverter
{
	// if this is false, we will generate update statements
	private boolean createInsert = true;
	private boolean createTable = false;
	private String alternateUpdateTable;
	private int commitEvery;
	private String concatString;
	private String chrFunction;
	private String concatFunction;
	private String sql;
	private StatementFactory factory;
	private List keyColumnsToUse;

	public SqlRowDataConverter(ResultInfo info)
	{
		super(info);
		this.factory = new StatementFactory(info);
	}

	public StrBuffer convertData()
	{
		return null;
	}

	public StrBuffer getEnd(long totalRows)
	{
		boolean writeCommit = true;
		if (commitEvery > 0 && (totalRows % commitEvery == 0))
		{
			writeCommit = false;
		}

		StrBuffer end = null;
		if (writeCommit)
		{
			end = new StrBuffer();
			end.append("\nCOMMIT;\n");
		}
		return end;
	}

	public String getFormatName()
	{
		if (createInsert)
			return "SQL INSERT";
		else
			return "SQL UPDATE";
	}

	public StrBuffer convertRowData(RowData row, long rowIndex)
	{
		StrBuffer result = new StrBuffer();
		DmlStatement dml = null;
		if (this.createInsert)
		{
			dml = this.factory.createInsertStatement(row, true);
		}
		else
		{
			dml = this.factory.createUpdateStatement(row, true);
		}
		dml.setChrFunction(this.chrFunction);
		dml.setConcatString(this.concatString);
		dml.setConcatFunction(this.concatFunction);
		result.append(dml.getExecutableStatement());
		result.append(";\n\n");

		if (this.commitEvery > 0 && ((rowIndex + 1) % commitEvery) == 0)
		{
			result.append("COMMIT;\n\n");
		}
		return result;
	}

	public StrBuffer getStart()
	{
		if (!this.createTable) return null;
		TableIdentifier updatetable = this.metaData.getUpdateTable();
		ColumnIdentifier[] cols = this.metaData.getColumns();
		DbMetadata db = this.originalConnection.getMetadata();
		String source = db.getTableSource(updatetable, cols, alternateUpdateTable);
		StrBuffer createSql = new StrBuffer(source);
		createSql.append("\n\n");
		return createSql;
	}

	public boolean isCreateInsert()
	{
		return createInsert;
	}

	public void setCreateInsert(boolean createInsert)
	{
		this.createInsert = createInsert;
		if (!createInsert)
		{
			boolean keysPresent = false;
			if (this.keyColumnsToUse != null && this.keyColumnsToUse.size() > 0)
			{
				// first check if all defined columns are actually present
				int keyCount = this.keyColumnsToUse.size();
				for (int i=0; i < keyCount; i++)
				{
					int col = this.metaData.findColumn((String)this.keyColumnsToUse.get(i));
					if (col == -1)
					{
						keysPresent = false;
						break;
					}
				}
				if (keysPresent)
				{
					// make sure the default key columns are not used
					this.metaData.resetPkColumns();

					for (int i=0; i < keyCount; i++)
					{
						this.metaData.setIsPkColumn((String)this.keyColumnsToUse.get(i), true);
					}
				}
			}

			if (!keysPresent)
			{
				try
				{
					this.metaData.readPkDefinition(this.originalConnection);
				}
				catch (SQLException e)
				{
					LogMgr.logError("SqlRowDataConverter.setCreateInsert", "Could not read PK columns for update table", e);
				}
			}
		}
	}

	public int getCommitEvery()
	{
		return commitEvery;
	}

	public void setCommitEvery(int commitEvery)
	{
		this.commitEvery = commitEvery;
	}

	public String getConcatString()
	{
		return concatString;
	}

	public void setConcatString(String concat)
	{
		if (concat == null) return;
		this.concatString = concat;
		this.concatFunction = null;
	}

	public String getConcatFunction()
	{
		return concatFunction;
	}

	public void setConcatFunction(String func)
	{
		if (func == null) return;
		this.concatFunction = func;
		this.concatString = null;
	}

	public String getChrFunction()
	{
		return chrFunction;
	}

	public void setChrFunction(String chrFunction)
	{
		this.chrFunction = chrFunction;
	}

	public String getSql()
	{
		return sql;
	}

	public void setSql(String sql)
	{
		this.sql = sql;
	}

	/**
	 * Getter for property createTable.
	 * @return Value of property createTable.
	 */
	public boolean isCreateTable()
	{
		return createTable;
	}

	/**
	 * Setter for property createTable.
	 * @param createTable New value of property createTable.
	 */
	public void setCreateTable(boolean flag)
	{
		this.createTable = flag;
	}

	/**
	 * Getter for property alternateUpdateTable.
	 * @return Value of property alternateUpdateTable.
	 */
	public String getAlternateUpdateTable()
	{
		return alternateUpdateTable;
	}

	/**
	 * Setter for property alternateUpdateTable.
	 * @param alternateUpdateTable New value of property alternateUpdateTable.
	 */
	public void setAlternateUpdateTable(String table)
	{
		this.alternateUpdateTable = table;
		this.factory.setTableToUse(this.alternateUpdateTable);
	}

	/**
	 * Getter for property keyColumnsToUse.
	 * @return Value of property keyColumnsToUse.
	 */
	public java.util.List getKeyColumnsToUse()
	{
		return keyColumnsToUse;
	}

	/**
	 * Setter for property keyColumnsToUse.
	 * @param keyColumnsToUse New value of property keyColumnsToUse.
	 */
	public void setKeyColumnsToUse(java.util.List keyColumnsToUse)
	{
		this.keyColumnsToUse = keyColumnsToUse;
	}

}
