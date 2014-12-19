package workbench.sql.wbcommands;

import java.sql.SQLException;
import workbench.db.WbConnection;
import workbench.exception.WbException;
import workbench.resource.ResourceMgr;
import workbench.sql.SqlCommand;
import workbench.sql.StatementRunnerResult;
import workbench.storage.DataStore;
import workbench.util.LineTokenizer;

/**
 *
 * @author  workbench@kellerer.org
 */
public class WbDescribeTable extends SqlCommand
{
	public static final String VERB = "DESC";
  public static final String VERB_LONG = "DESCRIBE";
	
	public WbDescribeTable()
	{
	}
	
	public String getVerb() { return VERB; }
	
	public StatementRunnerResult execute(WbConnection aConnection, String aSql) 
		throws SQLException, WbException
	{
		StatementRunnerResult result = new StatementRunnerResult(aSql);
		LineTokenizer tok = new LineTokenizer(aSql, " ");
		String verb = tok.nextToken(); 
		if (!VERB.equalsIgnoreCase(verb) && 
        !VERB_LONG.equalsIgnoreCase(verb)) throw new WbException("Wrong syntax. " + VERB + " expected!");
		String table = tok.nextToken();
		String schema = null;
		int pos = table.indexOf('.');
		if (pos > -1)
		{
			schema = table.substring(0, pos);
			table = table.substring(pos + 1);
		}
		
    if (schema == null && aConnection.getMetadata().isOracle())
    {
      schema = aConnection.getMetadata().getUserName();
    }
		DataStore ds = aConnection.getMetadata().getTableDefinition(null, schema, table);
    if (ds == null || ds.getRowCount() == 0)
    {
      result.setFailure();
      String msg = ResourceMgr.getString("ErrorTableOrViewNotFound");
      msg = msg.replaceAll("%name%", table);
      result.addMessage(msg);
    }
    else
    {
      result.setSuccess();
  		result.addDataStore(ds);
    }
		return result;
	}	
	
}