/*
 * DateColumnRenderer.java
 *
 * Created on 15. Juli 2002, 20:38
 */

package workbench.gui.renderer;

import java.awt.Color;
import java.awt.Component;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author  sql.workbench@freenet.de
 */
public class DateColumnRenderer
	extends DefaultTableCellRenderer
{
	private SimpleDateFormat formatter;
	private HashMap displayCache = new HashMap();
	public static final String DEFAULT_FORMAT = "yyyy-MM-dd";
	public DateColumnRenderer()
	{
		this(DEFAULT_FORMAT);
	}
	/** Creates a new instance of DateColumnRenderer */
	public DateColumnRenderer(String aDateFormat)
	{
		if (aDateFormat == null)
		{
			aDateFormat = DEFAULT_FORMAT;
		}
		this.formatter = new SimpleDateFormat(aDateFormat);
	}

  public void setValue(Object value)
	{
    this.setHorizontalAlignment(DefaultTableCellRenderer.RIGHT);
		Date aDate = null;
		String newVal = null;
		String tip = null;
		
		if (value != null )
		{
			try
			{
				aDate = (Date)value;
				tip = aDate.toString();
				newVal = (String)this.displayCache.get(aDate);
				if (newVal == null)
				{
					newVal = this.formatter.format(aDate);
					this.displayCache.put(aDate, newVal);
				}
			}
			catch (ClassCastException cc)
			{
				newVal = "";
				tip = "";
			}
		}
		else
		{
			newVal = "";
			tip = "";
		}
		this.setToolTipText(tip);
		super.setValue(newVal);
  }

}
