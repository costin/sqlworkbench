/*
 * ReportTableGrants.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2007, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db.report;

import java.util.Collection;
import java.util.Collections;
import workbench.db.TableGrant;
import workbench.db.TableIdentifier;
import workbench.db.WbConnection;
import workbench.util.StrBuffer;

/**
 * Generate XML report information about table grants.
 * 
 * @see workbench.db.DbMetadata#getTableGrants(workbench.db.TableIdentifier)
 * @author support@sql-workbench.net
 */
public class ReportTableGrants
{
	public static final String TAG_GRANT = "grant";
	public static final String TAG_GRANT_GRANTEE = "grantee";
	public static final String TAG_GRANT_PRIV = "privilege";
	public static final String TAG_GRANT_GRANTABLE = "grantable";
	private Collection<TableGrant> grants;
	private String namespace; 
	
	public ReportTableGrants(WbConnection con, TableIdentifier tbl)
	{
		grants = con.getMetadata().getTableGrants(tbl);
	}
	
	public ReportTableGrants(Collection<TableGrant> tableGrants)
	{
		this.grants = tableGrants;
	}
	
	public void appendXml(StrBuffer result, StrBuffer indent)
	{
		if (grants.size() == 0) return;
		
		TagWriter tagWriter = new TagWriter();
		tagWriter.setNamespace(this.namespace);
		
		StrBuffer indent1 = new StrBuffer(indent);
		indent1.append("  ");

		for (TableGrant grant : grants)
		{
			tagWriter.appendOpenTag(result, indent, TAG_GRANT);
			result.append('\n');
			tagWriter.appendTag(result, indent1, TAG_GRANT_PRIV, grant.getPrivilege());
			tagWriter.appendTag(result, indent1, TAG_GRANT_GRANTEE, grant.getGrantee());
			tagWriter.appendTag(result, indent1, TAG_GRANT_GRANTABLE, grant.isGrantable());
			tagWriter.appendCloseTag(result, indent, TAG_GRANT);
		}
		
		return;
	}

	public Collection<TableGrant> getGrants()
	{
		return Collections.unmodifiableCollection(grants);
	}
	
	public void setNamespace(String namespace)
	{
		this.namespace = namespace;
	}	
}
