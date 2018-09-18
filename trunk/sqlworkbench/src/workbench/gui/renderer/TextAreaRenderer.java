/*
 * TextAreaRenderer.java
 *
 * This file is part of SQL Workbench/J, https://www.sql-workbench.eu
 *
 * Copyright 2002-2018, Thomas Kellerer
 *
 * Licensed under a modified Apache License, Version 2.0
 * that restricts the use for certain governments.
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at.
 *
 *     https://www.sql-workbench.eu/manual/license.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * To contact the author please send an email to: support@sql-workbench.eu
 *
 */
package workbench.gui.renderer;

import java.awt.Component;
import java.awt.Insets;
import java.io.StringReader;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import workbench.resource.GuiSettings;
import workbench.resource.Settings;

import workbench.gui.WbSwingUtilities;

import workbench.util.StringUtil;

/**
 * A renderer to display multi-line character data.
 * <br/>
 * The renderer uses a JTextArea internally which is a lot slower than the own
 * drawing of the text implemented in ToolTipRender. But ToolTipRenderer
 * cannot cope with line breaks
 *
 * @author Thomas Kellerer
 */
public class TextAreaRenderer
	extends ToolTipRenderer
	implements TableCellRenderer, WbRenderer
{
	protected JTextArea textDisplay;
  protected boolean useStringReader;

	public TextAreaRenderer()
	{
		super();
		textDisplay = new JTextArea()
		{
			@Override
			public Insets getInsets()
			{
        return new Insets(3, 0, 0, 0);
			}

      @Override
			public Insets getMargin()
			{
				return WbSwingUtilities.getEmptyInsets();
			}

		};

		boolean wrap = GuiSettings.getWrapMultilineRenderer();
    useStringReader = GuiSettings.getUseReaderForMultilineRenderer();

		textDisplay.setWrapStyleWord(wrap);
		textDisplay.setLineWrap(wrap);
		textDisplay.setAutoscrolls(false);
		textDisplay.setTabSize(Settings.getInstance().getEditorTabWidth());
		textDisplay.setBorder(WbSwingUtilities.EMPTY_BORDER);
	}

	@Override
	public int getHorizontalAlignment()
	{
		return SwingConstants.LEFT;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,	boolean isSelected,	boolean hasFocus, int row, int col)
	{
		initDisplay(table, value, isSelected, hasFocus, row, col);

		this.textDisplay.setFont(table.getFont());

		if (hasFocus)
		{
			this.textDisplay.setBorder(WbSwingUtilities.FOCUSED_CELL_BORDER);
		}
		else
		{
			this.textDisplay.setBorder(WbSwingUtilities.EMPTY_BORDER);
		}

		prepareDisplay(value);

		this.textDisplay.setBackground(getBackgroundColor());
		this.textDisplay.setForeground(getForegroundColor());

		return textDisplay;
	}

	@Override
	public void prepareDisplay(Object value)
	{
		this.isNull = (value == null);
		if (this.isNull)
		{
			this.displayValue = rendererSetup == null ? null : rendererSetup.nullString;
			this.textDisplay.setText(displayValue);
			this.textDisplay.setToolTipText(null);
		}
		else
		{
			try
			{
				this.displayValue = (String)value;
			}
			catch (ClassCastException cce)
			{
				this.displayValue = value.toString();
			}

      if (useStringReader)
      {
        try
        {
          StringReader reader = new StringReader(this.displayValue);
          this.textDisplay.read(reader, null);
        }
        catch (Throwable th)
        {
          // cannot happen
        }
      }
      else
      {
        this.textDisplay.setText(this.displayValue);
      }

			if (showTooltip)
			{
				this.textDisplay.setToolTipText(StringUtil.getMaxSubstring(this.displayValue, maxTooltipSize));
			}
		}
	}

}
