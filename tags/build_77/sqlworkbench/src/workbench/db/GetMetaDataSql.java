/*
 * GetMetaDataSql.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2004, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: info@sql-workbench.net
 *
 */
package workbench.db;

import java.util.HashMap;

import workbench.util.WbPersistence;

/**
 *
 * @author  info@sql-workbench.net
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
	private boolean argumentsNeedParanthesis;
	private int schemaArgumentPos;
	private int catalogArgumentPos;
	private int objectNameArgumentPos;
	
	public GetMetaDataSql()
	{
	}
	
	public String getSql()
	{
		if (this.isProcedureCall)
			return this.getProcedureCallSql();
		else
			return this.getSelectSql();
	}

	private String getSelectSql()
	{
		boolean needsAnd;
		StringBuffer sql = new StringBuffer(baseSql);
		if (baseSql.toLowerCase().indexOf("where") == -1)
		{
			sql.append(" WHERE ");
			needsAnd = false;
		}
		else
		{
			needsAnd = true;
		}	
		if (schema != null && schemaField != null)
		{
			if (needsAnd) sql.append(" AND ");
			sql.append(schemaField + " = '" + schema + "'");
			needsAnd = true;
		}
		if (catalog != null && catalogField != null)
		{
			if (needsAnd) sql.append(" AND ");
			sql.append(catalogField + " = '" + catalog + "'");
			needsAnd = true;
		}
		if (objectName != null && objectNameField != null)
		{
			if (needsAnd) sql.append(" AND ");
			sql.append(objectNameField + " = '" + objectName + "'");
		}
		if (this.orderBy != null)
		{
			sql.append(" " + this.orderBy);
		}
		return sql.toString();
	}

	private String getProcedureCallSql()
	{
		StringBuffer sql = new StringBuffer(this.baseSql);
		if (this.argumentsNeedParanthesis)
		{
			sql.append(" (");
		}
		sql.append(' ');
		for (int i=1; i < 4; i ++)
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
		if (this.argumentsNeedParanthesis)
		{
			sql.append(")");
		}
		return sql.toString();
	}
	/** Getter for property baseSql.
	 * @return Value of property baseSql.
	 *
	 */
	public String getBaseSql()
	{
		return baseSql;
	}
	
	/** Setter for property baseSql.
	 * @param baseSql New value of property baseSql.
	 *
	 */
	public void setBaseSql(String baseSql)
	{
		this.baseSql = baseSql;
	}
	
	/** Getter for property schema.
	 * @return Value of property schema.
	 *
	 */
	public String getSchema()
	{
		return schema;
	}
	
	/** Setter for property schema.
	 * @param schema New value of property schema.
	 *
	 */
	public void setSchema(String schema)
	{
		if (schema == null) 
		{
			this.schema = null;
		}
		else
		{
			if (this.useLowerCase)
			{
				this.schema = schema.toLowerCase();
			}
			else if (this.useUpperCase)
			{
				this.schema = schema.toUpperCase();
			}
			else
			{
				this.schema = schema;
			}
		}
	}
	
	/** Getter for property catalog.
	 * @return Value of property catalog.
	 *
	 */
	public String getCatalog()
	{
		return catalog;
	}
	
	/** Setter for property catalog.
	 * @param catalog New value of property catalog.
	 *
	 */
	public void setCatalog(String catalog)
	{
		if (catalog == null) 
		{
			this.catalog = null;
		}
		else
		{
			if (this.useLowerCase)
			{
				this.catalog = catalog.toLowerCase();
			}
			else if (this.useUpperCase)
			{
				this.catalog = catalog.toUpperCase();
			}
			else
			{
				this.catalog = catalog;
			}
		}
	}
	
	/** Getter for property objectName.
	 * @return Value of property objectName.
	 *
	 */
	public String getObjectName()
	{
		return objectName;
	}
	
	/** Setter for property objectName.
	 * @param objectName New value of property objectName.
	 *
	 */
	public void setObjectName(String objectName)
	{
		if (objectName == null) 
		{
			this.objectName = null;
		}
		else
		{
			if (this.useLowerCase)
			{
				this.objectName = objectName.toLowerCase();
			}
			else if (this.useUpperCase)
			{
				this.objectName = objectName.toUpperCase();
			}
			else
			{
				this.objectName = objectName;
			}
		}
	}
	
	/** Getter for property schemaField.
	 * @return Value of property schemaField.
	 *
	 */
	public String getSchemaField()
	{
		return schemaField;
	}
	
	/** Setter for property schemaField.
	 * @param schemaField New value of property schemaField.
	 *
	 */
	public void setSchemaField(String schemaField)
	{
		this.schemaField = schemaField;
	}
	
	/** Getter for property catalogField.
	 * @return Value of property catalogField.
	 *
	 */
	public String getCatalogField()
	{
		return catalogField;
	}
	
	/** Setter for property catalogField.
	 * @param catalogField New value of property catalogField.
	 *
	 */
	public void setCatalogField(String catalogField)
	{
		this.catalogField = catalogField;
	}
	
	/** Getter for property objectNameField.
	 * @return Value of property objectNameField.
	 *
	 */
	public String getObjectNameField()
	{
		return objectNameField;
	}
	
	/** Setter for property objectNameField.
	 * @param objectNameField New value of property objectNameField.
	 *
	 */
	public void setObjectNameField(String objectNameField)
	{
		this.objectNameField = objectNameField;
	}
	
	/** Getter for property orderBy.
	 * @return Value of property orderBy.
	 *
	 */
	public String getOrderBy()
	{
		return orderBy;
	}
	
	/** Setter for property orderBy.
	 * @param orderBy New value of property orderBy.
	 *
	 */
	public void setOrderBy(String orderBy)
	{
		this.orderBy = orderBy;
	}
	

	/** Getter for property useUpperCase.
	 * @return Value of property useUpperCase.
	 *
	 */
	public boolean isUseUpperCase()
	{
		return useUpperCase;
	}
	
	/** Setter for property useUpperCase.
	 * @param useUpperCase New value of property useUpperCase.
	 *
	 */
	public void setUseUpperCase(boolean useUpperCase)
	{
		this.useUpperCase = useUpperCase;
	}
	
	/** Getter for property useLowerCase.
	 * @return Value of property useLowerCase.
	 *
	 */
	public boolean isUseLowerCase()
	{
		return useLowerCase;
	}
	
	/** Setter for property useLowerCase.
	 * @param useLowerCase New value of property useLowerCase.
	 *
	 */
	public void setUseLowerCase(boolean useLowerCase)
	{
		this.useLowerCase = useLowerCase;
	}
	/** Getter for property isProcedureCall.
	 * @return Value of property isProcedureCall.
	 *
	 */
	public boolean isIsProcedureCall()
	{
		return isProcedureCall;
	}
	
	/** Setter for property isProcedureCall.
	 * @param isProcedureCall New value of property isProcedureCall.
	 *
	 */
	public void setIsProcedureCall(boolean isProcedureCall)
	{
		this.isProcedureCall = isProcedureCall;
	}

	/** Getter for property schemaArgumentPos.
	 * @return Value of property schemaArgumentPos.
	 *
	 */
	public int getSchemaArgumentPos()
	{
		return schemaArgumentPos;
	}
	
	/** Setter for property schemaArgumentPos.
	 * @param schemaArgumentPos New value of property schemaArgumentPos.
	 *
	 */
	public void setSchemaArgumentPos(int schemaArgumentPos)
	{
		this.schemaArgumentPos = schemaArgumentPos;
	}
	
	/** Getter for property catalogArgumentPos.
	 * @return Value of property catalogArgumentPos.
	 *
	 */
	public int getCatalogArgumentPos()
	{
		return catalogArgumentPos;
	}
	
	/** Setter for property catalogArgumentPos.
	 * @param catalogArgumentPos New value of property catalogArgumentPos.
	 *
	 */
	public void setCatalogArgumentPos(int catalogArgumentPos)
	{
		this.catalogArgumentPos = catalogArgumentPos;
	}
	
	/** Getter for property objectNameArgumentPos.
	 * @return Value of property objectNameArgumentPos.
	 *
	 */
	public int getObjectNameArgumentPos()
	{
		return objectNameArgumentPos;
	}
	
	/** Setter for property objectNameArgumentPos.
	 * @param objectNameArgumentPos New value of property objectNameArgumentPos.
	 *
	 */
	public void setObjectNameArgumentPos(int objectNameArgumentPos)
	{
		this.objectNameArgumentPos = objectNameArgumentPos;
	}
	
	/** Getter for property argumentsNeedParanthesis.
	 * @return Value of property argumentsNeedParanthesis.
	 *
	 */
	public boolean isArgumentsNeedParanthesis()
	{
		return argumentsNeedParanthesis;
	}
	
	/** Setter for property argumentsNeedParanthesis.
	 * @param argumentsNeedParanthesis New value of property argumentsNeedParanthesis.
	 *
	 */
	public void setArgumentsNeedParanthesis(boolean argumentsNeedParanthesis)
	{
		this.argumentsNeedParanthesis = argumentsNeedParanthesis;
	}

	public static void createTriggerSourceStatements()
	{
		HashMap trgSrcStatements = new HashMap();
		GetMetaDataSql asaSource = new GetMetaDataSql();
		asaSource.setBaseSql("select syscomments.text  \n" + 
           " from sysobjects, syscomments  \n" + 
           " where sysobjects.id = syscomments.id  \n" + 
           "and  sysobjects.type = 'TR'");
		asaSource.setObjectNameField("sysobjects.name");
		asaSource.setOrderBy(" order by 1");

		GetMetaDataSql oraTrigSrc = new GetMetaDataSql();
		oraTrigSrc.setUseUpperCase(true);
		oraTrigSrc.setBaseSql("SELECT 'CREATE OR REPLACE TRIGGER '|| description, trigger_body FROM all_triggers");
		oraTrigSrc.setCatalogField(null);
		oraTrigSrc.setObjectNameField("trigger_name");
		oraTrigSrc.setSchemaField("owner");
		
		trgSrcStatements.put("Adaptive Server Anywhere", asaSource);
		trgSrcStatements.put("Oracle", oraTrigSrc);
		WbPersistence.writeObject(trgSrcStatements, "d:/temp/TriggerSourceStatements.xml");
	}
	
	public static void createListTriggerStatements()
	{
		GetMetaDataSql listOraTrigs = new GetMetaDataSql();
		listOraTrigs.setUseUpperCase(true);
		listOraTrigs.setBaseSql("SELECT trigger_name, trigger_type, triggering_event as trigger_event FROM all_triggers");
		listOraTrigs.setCatalogField(null);
		listOraTrigs.setObjectNameField("table_name");
		listOraTrigs.setSchemaField("owner");
		listOraTrigs.setOrderBy("ORDER BY trigger_name");

		GetMetaDataSql listPostgresTrigs = new GetMetaDataSql();
		String sql="select trg.tgname,  \n" + 
           "       case trg.tgtype & cast(2 as int2) \n" + 
           "         when 0 then 'AFTER' \n" + 
           "         else 'BEFORE' \n" + 
           "       end as trigger_type, \n" + 
           "       case trg.tgtype & cast(28 as int2) \n" + 
           "         when 16 then 'UPDATE' \n" + 
           "         when 8 then 'DELETE' \n" + 
           "         when 4 then 'INSERT' \n" + 
           "         when 20 then 'INSERT, UPDATE' \n" + 
           "         when 28 then 'INSERT, UPDATE, DELETE' \n" + 
           "         when 24 then 'UPDATE, DELETE' \n" + 
           "         when 12 then 'INSERT, DELETE' \n" + 
           "       end as trigger_event \n" + 
           "from pg_trigger trg, pg_class tbl \n" + 
           "where trg.tgrelid = tbl.oid \n";

		listPostgresTrigs.setBaseSql(sql);
		listPostgresTrigs.setObjectNameField("tbl.relname");
		listPostgresTrigs.setOrderBy("trg.name");
		
		GetMetaDataSql listMsSqlTrigs = new GetMetaDataSql();
		sql="	select tr.name as trigger_name, \n" + 
           "	case 1 \n" + 
           "	   when ObjectProperty( tr.id, 'ExecIsAfterTrigger') then 'AFTER' \n" + 
           "	   else 'BEFORE' \n" + 
           "	end  as trigger_type, \n" + 
           "	case 1 \n" + 
           "	   when ObjectProperty( tr.id, 'ExecIsUpdateTrigger') then 'UPDATE' \n" + 
           "	   when ObjectProperty( tr.id, 'ExecIsDeleteTrigger') then 'DELETE' \n" + 
           "     when ObjectProperty( tr.id, 'ExecIsInsertTrigger') then 'INSERT' \n" + 
           "  end as trigger_event \n" + 
           "	from sysobjects tr, sysobjects tab \n" + 
           "	where tab.id = tr.parent_obj \n" + 
           "	 and  tr.xtype = 'TR' \n";		
		listMsSqlTrigs.setBaseSql(sql);
		listMsSqlTrigs.setObjectNameField("tab.name");
		listMsSqlTrigs.setOrderBy("trigger_name");
		
		GetMetaDataSql hsql = new GetMetaDataSql();
		hsql.setBaseSql("SELECT trigger_name, when_clause as trigger_type, triggering_event as trigger_event from system_triggers");
		hsql.setObjectNameField("table_name");
		hsql.setOrderBy("trigger_name");
		
		GetMetaDataSql asa = new GetMetaDataSql();
		asa.setBaseSql("select trigname as trigger_name, trigtime as trigger_type, event from systriggers");
		asa.setObjectNameField("tname");
		asa.setOrderBy("trigger_name");
		
		HashMap trgStatements = new HashMap();
		trgStatements.put("Oracle", listOraTrigs);
		trgStatements.put("Oracle8", listOraTrigs);
		trgStatements.put("PostgreSQL", listPostgresTrigs);
		trgStatements.put("Microsoft SQL Server", listMsSqlTrigs);
		trgStatements.put("HSQL Database Engine", hsql);
		trgStatements.put("Adaptive Server Anywhere", asa);
		
		WbPersistence.writeObject(trgStatements, "d:/temp/ListTriggersStatements.xml");
	}

	public static void createViewStatements()
	{
		GetMetaDataSql hsql = new GetMetaDataSql();
		hsql.setUseUpperCase(true);
		hsql.setBaseSql("SELECT view_definition FROM SYSTEM_VIEWS");
		hsql.setObjectNameField("TABLE_NAME");
		hsql.setCatalogField(null);
		hsql.setSchemaField(null);

		GetMetaDataSql ora = new GetMetaDataSql();
		ora.setUseUpperCase(true);
		ora.setBaseSql("SELECT text FROM all_views");
		ora.setObjectNameField("view_name");
		ora.setCatalogField(null);
		ora.setSchemaField("owner");

		GetMetaDataSql mss = new GetMetaDataSql();
		mss.setBaseSql("exec sp_helptext");
		mss.setArgumentsNeedParanthesis(false);
		mss.setSchemaArgumentPos(0);
		mss.setCatalogArgumentPos(0);
		mss.setObjectNameArgumentPos(1);
		mss.setIsProcedureCall(true);
		
		GetMetaDataSql asa = new GetMetaDataSql();
		asa.setBaseSql("select viewtext from sysviews");
		asa.setUseUpperCase(true);
		asa.setObjectNameField("viewname");
		asa.setCatalogField(null);
		asa.setSchemaField("vcreator");
		
		
		HashMap viewStatements = new HashMap();
		viewStatements.put("Oracle", ora);
		viewStatements.put("Microsoft SQL Server", mss);
		viewStatements.put("HSQLDB Database Engine", hsql);
		viewStatements.put("Adaptive Server Anywhere", asa);
		WbPersistence.writeObject(viewStatements, "d:/temp/ViewSourceStatements.xml");
	}
	public static void createDefaultStatements()
	{
		System.out.println("Generating default statements...");
		GetMetaDataSql oracleProc = new GetMetaDataSql();
		oracleProc.setUseUpperCase(true);
		oracleProc.setBaseSql("SELECT text FROM all_source");
		oracleProc.setObjectNameField("name");
		oracleProc.setCatalogField(null);
		oracleProc.setSchemaField("owner");
		oracleProc.setOrderBy("ORDER BY line");
		
		GetMetaDataSql mssProc = new GetMetaDataSql();
		mssProc.setBaseSql("exec sp_helptext");
		mssProc.setArgumentsNeedParanthesis(false);
		mssProc.setSchemaArgumentPos(0);
		mssProc.setCatalogArgumentPos(0);
		mssProc.setObjectNameArgumentPos(1);
		mssProc.setIsProcedureCall(true);
		
		HashMap procStatements = new HashMap();
		procStatements.put("Oracle", oracleProc);
		procStatements.put("Microsoft SQL Server", mssProc);
		WbPersistence.writeObject(procStatements, "ProcSourceStatements.xml");
	}

	public static void main(String args[])
	{
		//createDefaultStatements();
		//createListTriggerStatements();
		createViewStatements();
		createTriggerSourceStatements();
		System.out.println("Done.");
	}
	
}
