/*
 * HtmlRowDataConverter.java
 *
 * Created on August 26, 2004, 10:54 PM
 */

package workbench.db.exporter;

import java.sql.Types;
import java.text.SimpleDateFormat;
import workbench.db.report.ReportColumn;
import workbench.db.report.ReportTable;
import workbench.util.SqlUtil;
import workbench.util.StrBuffer;
import workbench.util.StringUtil;
import workbench.storage.*;

/**
 *
 * @author  workbench@kellerer.org
 */
public class HtmlRowDataConverter
	extends RowDataConverter
{
	private String pageTitle;
	private boolean createFullPage = true;
	private boolean escapeHtml = false;
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat timestampFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SS");

	public HtmlRowDataConverter(ResultInfo info)
	{
		super(info);
	}
	
	public StrBuffer convertData()
	{
		return null;
	}

	public StrBuffer getEnd()
	{
		StrBuffer html = new StrBuffer("</table>\n");
		if (createFullPage) html.append("</body>\n</html>\n");
		return html;
	}

	public void setCreateFullPage(boolean flag)
	{
		this.createFullPage = flag;
	}
	public void setPageTitle(String title)
	{
		this.pageTitle = title;
	}
	
	public void setEscapeHtml(boolean flag)
	{
		this.escapeHtml = flag;
	}
	
	public String getFormatName()
	{
		return "HTML";
	}

	public StrBuffer convertRowData(RowData row, int rowIndex)
	{
		int count = this.metaData.getColumnCount();
		StrBuffer result = new StrBuffer(count * 30);
		result.append("  <tr>\n      ");
		for (int c=0; c < count; c ++)
		{
			String value = this.getValueAsFormattedString(row, c);
			int type = this.metaData.getColumnType(c);
			if (SqlUtil.isDateType(type))
			{
				result.append("<td class=\"date-cell\">");
			}
			else if (SqlUtil.isNumberType(type) || SqlUtil.isDateType(type))
			{
				result.append("<td class=\"number-cell\">");
			}
			else
			{
				result.append("<td class=\"text-cell\">");
			}

			if (value == null)
			{
				result.append("&nbsp;");
			}
			else
			{
				if (this.escapeHtml)
				{
					value = StringUtil.escapeHTML(value);
				}
				result.append(value);
			}
			result.append("</td>");
		}
		result.append("\n  </tr>\n");
		return result;
	}

	public StrBuffer getStart()
	{
		StrBuffer result = new StrBuffer(250);
		
		if (createFullPage)
		{
			result.append("<html>\n");
			
			if (pageTitle != null && pageTitle.length() > 0)
			{
				result.append("<head>\n<title>");
				result.append(pageTitle);
				result.append("</title>\n");
			}			
			result.append("<style type=\"text/css\">\n");
			result.append("<!--\n");
			result.append("  table { border-spacing:0; border-collapse:collapse}\n");
			result.append("  td { padding:2; border-style:solid;border-width:1px; vertical-align:top;}\n");
			result.append("  .number-cell { text-align:right; white-space:nowrap; } \n");
			result.append("  .text-cell { text-align:left; } \n");
			result.append("  .date-cell { text-align:left; white-space:nowrap;} \n");
			result.append("-->\n</style>\n");

			result.append("</head>\n<body>\n");
		}
		result.append("<table>\n");

		// table header with column names
		result.append("  <tr>\n      ");
		for (int c=0; c < this.metaData.getColumnCount(); c ++)
		{
			result.append("<td><b>");
			result.append(this.metaData.getColumnName(c));
			result.append("</b></td>");
		}
		result.append("\n  </tr>\n");

		return result;
	}

}