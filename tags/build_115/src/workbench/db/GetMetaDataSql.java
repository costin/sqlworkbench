/*
 * GetMetaDataSql.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2013, Thomas Kellerer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at.
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db;

import workbench.sql.formatter.SQLLexer;
import workbench.sql.formatter.SQLToken;
import workbench.sql.formatter.SqlFormatter;

/**
 *
 * @author Thomas Kellerer
 */
public class GetMetaDataSql
{
	private String baseSql;
	private String schema;
	private String schemaField;
	private String catalog;
	private String catalogField;
	private String objectName;
	private String objectNameField;
	private String orderBy;
	private boolean useUpperCase;
	private boolean useLowerCase;
	private boolean isProcedureCall;
	private int schemaArgumentPos;
	private int catalogArgumentPos;
	private int objectNameArgumentPos;

	private String baseObjectName;
	private String baseObjectCatalog;
	private String baseObjectSchema;

	private String baseObjectNameField;
	private String baseObjectCatalogField;
	private String baseObjectSchemaField;

	public String getSql()
	{
		if (this.isProcedureCall) return this.getProcedureCallSql();
		else return this.getSelectSql();
	}

	private String getSelectSql()
	{
		boolean containsWhere = containsWhere(baseSql);
		boolean needsAnd = containsWhere;
		boolean needsWhere = !containsWhere;
		StringBuilder sql = new StringBuilder(baseSql);

		if (schema != null && schemaField != null)
		{
			if (needsWhere)
			{
				sql.append(" WHERE ");
				needsWhere = false;
			}

			if (needsAnd) sql.append(" AND ");
			sql.append(schemaField + getOperator(schema) + "'" + getNameValue(schema) + "'");
			needsAnd = true;
		}

		if (catalog != null && catalogField != null)
		{
			if (needsWhere)
			{
				sql.append(" WHERE ");
				needsWhere = false;
			}
			if (needsAnd) sql.append(" AND ");
			sql.append(catalogField + getOperator(catalog) + "'" + getNameValue(catalog) + "'");
			needsAnd = true;
		}

		if (objectName != null && objectNameField != null)
		{
			if (needsWhere)
			{
				sql.append(" WHERE ");
				needsWhere = false;
			}
			if (needsAnd) sql.append(" AND ");
			sql.append(objectNameField + " = '" + getNameValue(objectName) + "'");
			needsAnd = true;
		}

		if (baseObjectName != null && baseObjectNameField != null)
		{
			sql.append(" AND ");
			sql.append(baseObjectNameField + " = '" + getNameValue(baseObjectName ) + "'");
		}

		if (baseObjectCatalog != null && baseObjectCatalogField != null)
		{
			sql.append(" AND ");
			sql.append(baseObjectCatalogField + getOperator(baseObjectCatalog) + "'" + getNameValue(baseObjectCatalog) + "'");
		}

		if (baseObjectSchema != null && baseObjectSchemaField != null)
		{
			sql.append(" AND ");
			sql.append(baseObjectSchemaField + getOperator(baseObjectSchema) + "'" + getNameValue(baseObjectSchema) + "'");
		}

		if (this.orderBy != null)
		{
			sql.append(" " + this.orderBy);
		}
		return sql.toString();
	}

	private String getOperator(String inputValue)
	{
		if (inputValue == null) return "";
		if (inputValue.indexOf('%') > -1)
		{
			return " LIKE ";
		}
		return " = ";
	}


	private String getNameValue(String value)
	{
		if (value == null) return null;
		if (useLowerCase) return value.toLowerCase();
		if (useUpperCase) return value.toUpperCase();
		return value;
	}

	private String getProcedureCallSql()
	{
		StringBuilder sql = new StringBuilder(this.baseSql);
		sql.append(' ');
		for (int i = 1; i < 4; i++)
		{
			if (schemaArgumentPos == i && this.schema != null)
			{
				if (i > 1) sql.append(',');
				sql.append(this.schema);
			}
			else if (catalogArgumentPos == i && this.catalog != null)
			{
				if (i > 1) sql.append(',');
				sql.append(this.catalog);
			}
			else if (this.objectNameArgumentPos == i && this.objectName != null)
			{
				if (i > 1) sql.append(',');
				sql.append(this.objectName);
			}
		}
		return sql.toString();
	}

	public String getBaseSql()
	{
		return baseSql;
	}

	public void setBaseSql(String sql)
	{
		this.baseSql = sql;
	}

	public String getSchema()
	{
		return schema;
	}

	public void setSchema(String schem)
	{
		this.schema = schem;
	}

	public String getCatalog()
	{
		return catalog;
	}

	public void setCatalog(String cat)
	{
		this.catalog = cat;
	}

	public String getObjectName()
	{
		return objectName;
	}

	public void setObjectName(String name)
	{
		this.objectName = name;
	}

	@Override
	public String toString()
	{
		return getSql();
	}

	public String getSchemaField()
	{
		return schemaField;
	}

	public void setSchemaField(String field)
	{
		this.schemaField = field;
	}

	public String getCatalogField()
	{
		return catalogField;
	}

	public void setCatalogField(String field)
	{
		this.catalogField = field;
	}

	public String getObjectNameField()
	{
		return objectNameField;
	}

	public void setObjectNameField(String field)
	{
		this.objectNameField = field;
	}

	public String getOrderBy()
	{
		return orderBy;
	}

	public void setOrderBy(String order)
	{
		this.orderBy = order;
	}

	public boolean getUseUpperCase()
	{
		return useUpperCase;
	}

	public void setUseUpperCase(boolean upperCase)
	{
		this.useUpperCase = upperCase;
	}

	public boolean getUseLowerCase()
	{
		return useLowerCase;
	}

	public void setUseLowerCase(boolean lowerCase)
	{
		this.useLowerCase = lowerCase;
	}

	public boolean isIsProcedureCall()
	{
		return isProcedureCall;
	}

	public void setIsProcedureCall(boolean isCall)
	{
		this.isProcedureCall = isCall;
	}

	public int getSchemaArgumentPos()
	{
		return schemaArgumentPos;
	}

	public void setSchemaArgumentPos(int pos)
	{
		this.schemaArgumentPos = pos;
	}

	public int getCatalogArgumentPos()
	{
		return catalogArgumentPos;
	}

	public void setCatalogArgumentPos(int pos)
	{
		this.catalogArgumentPos = pos;
	}

	public int getObjectNameArgumentPos()
	{
		return objectNameArgumentPos;
	}

	public void setObjectNameArgumentPos(int pos)
	{
		this.objectNameArgumentPos = pos;
	}

	public String getBaseObjectCatalog()
	{
		return baseObjectCatalog;
	}

	public void setBaseObjectCatalog(String baseObjectCatalog)
	{
		this.baseObjectCatalog = baseObjectCatalog;
	}

	public String getBaseObjectCatalogField()
	{
		return baseObjectCatalogField;
	}

	public void setBaseObjectCatalogField(String baseObjectCatalogField)
	{
		this.baseObjectCatalogField = baseObjectCatalogField;
	}

	public String getBaseObjectName()
	{
		return baseObjectName;
	}

	public void setBaseObjectName(String baseObjectName)
	{
		this.baseObjectName = baseObjectName;
	}

	public String getBaseObjectNameField()
	{
		return baseObjectNameField;
	}

	public void setBaseObjectNameField(String baseObjectNameField)
	{
		this.baseObjectNameField = baseObjectNameField;
	}

	public String getBaseObjectSchema()
	{
		return baseObjectSchema;
	}

	public void setBaseObjectSchema(String baseObjectSchema)
	{
		this.baseObjectSchema = baseObjectSchema;
	}

	public String getBaseObjectSchemaField()
	{
		return baseObjectSchemaField;
	}

	public void setBaseObjectSchemaField(String baseObjectSchemaField)
	{
		this.baseObjectSchemaField = baseObjectSchemaField;
	}

	boolean containsWhere(String sql)
	{
		if (sql == null) return false;
		sql = sql.toLowerCase();
		if (!sql.contains("where")) return false;
		SQLLexer lexer = new SQLLexer(sql);
		SQLToken token = lexer.getNextToken(false, false);
		int bracketCount = 0;
		boolean inFrom = false;
		while (token != null)
		{
			String text = token.getText();
			if (text.equals(")"))
			{
				bracketCount --;
			}
			else if (text.equals("("))
			{
				bracketCount ++;
			}
			else if (text.equals("from") && bracketCount == 0)
			{
				inFrom = true;
			}
			else if (inFrom && text.equals("where") && bracketCount == 0)
			{
				return true;
			}
			else if (SqlFormatter.FROM_TERMINAL.contains(text) && bracketCount == 0)
			{
				inFrom = false;
			}
			token = lexer.getNextToken(false, false);
		}
		return false;
	}
}