/*
 * EnumReader.java
 *
 * Created on January 7, 2004, 8:10 PM
 */

package workbench.db.mysql;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import workbench.db.DbMetadata;
import workbench.db.WbConnection;
import workbench.log.LogMgr;
import workbench.storage.DataStore;

/**
 *
 * @author  workbench@kellerer.org
 */
public class EnumReader
{
	
	
	public static void updateEnumDefinition(String tableName, DataStore tableDefinition, WbConnection connection)
	{
		Statement stmt = null;
		ResultSet rs = null;
		HashMap defs = new HashMap(17);
		
		try
		{
			stmt = connection.createStatement();
			rs = stmt.executeQuery("SHOW COLUMNS FROM " + tableName);
			int colCount = 0;
			while (rs.next())
			{
				String column = rs.getString(1);
				if (column == null) continue;
				
				String type = rs.getString(2);
				if (type == null) continue;
				String ltype = type.toLowerCase();
				if (ltype.startsWith("enum") || ltype.startsWith("set"))
				{
					colCount ++;
					defs.put(column, type);
				}
			}
			int count = tableDefinition.getRowCount();
			for (int row=0; row < count; row ++)
			{
				String column = tableDefinition.getValueAsString(row, DbMetadata.COLUMN_IDX_TABLE_DEFINITION_COL_NAME);
				String type = (String)defs.get(column);
				if (type != null)
				{
					tableDefinition.setValue(row, DbMetadata.COLUMN_IDX_TABLE_DEFINITION_DATA_TYPE, type);
				}
			}
		}
		catch (Exception e)
		{
			LogMgr.logError("EnumReader.updateEnumDefinition()", "Could not read enum definition", e);
		}
		finally 
		{
			try { rs.close(); } catch (Throwable th) {}
			try { stmt.close(); } catch (Throwable th) {}
		}
	}
}