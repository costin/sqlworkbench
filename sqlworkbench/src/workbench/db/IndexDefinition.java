/*
 * IndexDefinition.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2012, Thomas Kellerer
 * No part of this code may be reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import workbench.log.LogMgr;
import workbench.util.SqlUtil;

/**
 * A class to store the defintion of a database index.
 * @author  Thomas Kellerer
 */
public class IndexDefinition
	implements DbObject
{
	private boolean isPK;
	private boolean isUnique;
	private String indexName;
	private String indexType;
	private TableIdentifier baseTable;
	private List<IndexColumn> columns = new ArrayList<IndexColumn>();
	private String comment;
	private ConstraintDefinition uniqueConstraint;
	private String indexExpression;

	private boolean autoGenerated;

	// for Firebird which only supports a "global" index direction, not per column
	private String direction;

	public IndexDefinition(TableIdentifier table, String name)
	{
		this.indexName = name;
		this.baseTable = table;
	}

	public boolean isAutoGenerated()
	{
		return autoGenerated;
	}

	public void setAutoGenerated(boolean flag)
	{
		this.autoGenerated = flag;
	}

	public String getDirection()
	{
		return direction;
	}

	public void setDirection(String dir)
	{
		this.direction = dir;
	}

	@Override
	public String getComment()
	{
		return comment;
	}

	@Override
	public void setComment(String c)
	{
		comment = c;
	}

	@Override
	public String getSchema()
	{
		return baseTable.getSchema();
	}

	@Override
	public String getCatalog()
	{
		return null;
	}

	public boolean isNonStandardExpression()
	{
		return indexExpression != null;
	}

	public String getIndexExpression()
	{
		return indexExpression;
	}

	public void setIndexExpression(String expression)
	{
		this.indexExpression = expression;
	}


	public boolean isUniqueConstraint()
	{
		return uniqueConstraint != null;
	}

	public void setUniqueConstraint(ConstraintDefinition constraint)
	{
		if (constraint != null && constraint.getConstraintType() != ConstraintType.Unique)
		{
			LogMgr.logError("IndexDefinition.setUniqueConstraint()", "setUniqueConstraint() called with a different constraint type", new IllegalArgumentException("Invalid type: " + constraint.getConstraintType()));
		}
		this.uniqueConstraint = constraint;
	}

	public String getUniqueConstraintName()
	{
		return uniqueConstraint == null ? null : uniqueConstraint.getConstraintName();
	}

	public ConstraintDefinition getUniqueConstraint()
	{
		return uniqueConstraint;
	}

	public void addColumn(String column, String direction)
	{
		this.columns.add(new IndexColumn(column, direction));
	}

	public void setIndexType(String type)
	{
		if (type == null)
		{
			this.indexType = "NORMAL";
		}
		else
		{
			this.indexType = type;
		}
	}

	@Override
	public String getFullyQualifiedName(WbConnection conn)
	{
		return SqlUtil.buildExpression(null, getCatalog(), getSchema(), indexName);
	}

	@Override
	public String getObjectExpression(WbConnection conn)
	{
		return SqlUtil.buildExpression(conn, null, getSchema(), indexName);
	}

	@Override
	public String getDropStatement(WbConnection con, boolean cascade)
	{
		return null;
	}

	@Override
	public String getObjectNameForDrop(WbConnection con)
	{
		return getFullyQualifiedName(con);
	}

	@Override
	public String getObjectName(WbConnection conn)
	{
		return conn.getMetadata().quoteObjectname(indexName);
	}

	@Override
	public String getObjectType()
	{
		return "INDEX";
	}

	@Override
	public String getObjectName()
	{
		return getName();
	}

	public List<IndexColumn> getColumns()
	{
		if (columns == null) return Collections.emptyList();
		return columns;
	}

	public String getIndexType()
	{
		return this.indexType;
	}

	@Override
	public String toString()
	{
		return getExpression();
	}

	public String getColumnList()
	{
		StringBuilder result = new StringBuilder(this.columns.size() * 10);
		for (int i=0; i < this.columns.size(); i++)
		{
			if (i > 0) result.append(", ");
			result.append(columns.get(i).getColumn());
		}
		return result.toString();
	}

	public String getExpression()
	{
		if (indexExpression != null)
		{
			return indexExpression;
		}

		StringBuilder result = new StringBuilder(this.columns.size() * 10);
		for (int i=0; i < this.columns.size(); i++)
		{
			if (i > 0) result.append(", ");
			result.append(columns.get(i).getExpression());
		}
		return result.toString();
	}

	public String getName()
	{
		return this.indexName;
	}

	public void setPrimaryKeyIndex(boolean flag)
	{
		this.isPK = flag;
	}

	public boolean isPrimaryKeyIndex()
	{
		return this.isPK;
	}

	public void setUnique(boolean flag) { this.isUnique = flag; }
	public boolean isUnique() { return this.isUnique; }

	@Override
  public int hashCode()
  {
    int hash = 71 * 7 + (this.indexName != null ? this.indexName.hashCode() : 0);
    return hash;
  }

	@Override
	public boolean equals(Object o)
	{
		if (o instanceof IndexDefinition)
		{
			IndexDefinition other = (IndexDefinition)o;
			boolean equals = false;
			if (this.isPK && other.isPK || this.isUnique && other.isUnique)
			{
				equals = true;
				// for PK indexes the order of the columns in the index does not matter
				// so we consider the same list of columns equal even if they have a different order
				for (IndexColumn col : columns)
				{
					if (!other.columns.contains(col))
					{
						equals = false;
						break;
					}
				}
			}
			else
			{
				equals = this.columns.equals(other.columns);
			}

			if (equals)
			{
				equals = (this.isPK == other.isPK) && (this.isUnique == other.isUnique);
			}
			return equals;
		}
		else if (o instanceof String)
		{
			return this.getExpression().equals((String)o);
		}
		return false;
	}

	@Override
	public CharSequence getSource(WbConnection con)
	{
		if (con == null) return null;
		IndexReader reader = con.getMetadata().getIndexReader();
		return reader.getIndexSource(baseTable, this);
	}

	public static IndexDefinition findIndex(List<IndexDefinition> indexList, String indexName, String indexSchema)
	{
		for (IndexDefinition idx : indexList)
		{
			if (idx.getObjectName().equals(indexName) && (indexSchema == null || indexSchema.equals(idx.getSchema())))
			{
				return idx;
			}
		}
		return null;
	}
}
