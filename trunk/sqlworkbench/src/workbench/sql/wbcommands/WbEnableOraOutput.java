package workbench.sql.wbcommands;

import java.sql.SQLException;
import workbench.db.WbConnection;
import workbench.exception.WbException;
import workbench.sql.SqlCommand;
import workbench.sql.StatementRunnerResult;
import workbench.storage.DataStore;
import workbench.util.LineTokenizer;

/**
 *
 * @author  workbench@kellerer.org
 */
public class WbEnableOraOutput extends SqlCommand
{
	public static final String VERB = "ENABLEOUT";
	
	public WbEnableOraOutput()
	{
	}
	
	public String getVerb() { return VERB; }
	
	public StatementRunnerResult execute(WbConnection aConnection, String aSql) 
		throws SQLException, WbException
	{
		this.checkVerb(aSql);
		
		LineTokenizer tok = new LineTokenizer(aSql, " ");
		long limit = -1;
		String verb = tok.nextToken(); // skip the verb
		
		// second token is the buffer size
		if (tok.hasMoreTokens())
		{
			String value = tok.nextToken();
			try
			{
				limit = Long.parseLong(value);
			}
			catch (NumberFormatException nfe)
			{
				limit = -1;
			}
		}
		aConnection.getMetadata().enableOutput(limit);
		StatementRunnerResult result = new StatementRunnerResult();
		return result;
	}	
	
}
