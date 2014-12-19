/*
 * DwTable.java
 *
 * Created on December 1, 2001, 11:41 PM
 */
package workbench.gui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.print.PageFormat;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.swing.AbstractListModel;

import javax.swing.ActionMap;
import javax.swing.CellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.JPopupMenu.Separator;
import javax.swing.JScrollBar;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import workbench.WbManager;
import workbench.gui.WbSwingUtilities;
import workbench.gui.actions.DataToClipboardAction;
import workbench.gui.actions.FindDataAction;
import workbench.gui.actions.FindDataAgainAction;
import workbench.gui.actions.OptimizeAllColumnsAction;
import workbench.gui.actions.OptimizeColumnWidthAction;
import workbench.gui.actions.PrintAction;
import workbench.gui.actions.PrintPreviewAction;
import workbench.gui.actions.SaveDataAsAction;
import workbench.gui.actions.SetColumnWidthAction;
import workbench.gui.actions.SortAscendingAction;
import workbench.gui.actions.SortDescendingAction;
import workbench.gui.actions.WbAction;
import workbench.gui.renderer.DateColumnRenderer;
import workbench.gui.renderer.NumberColumnRenderer;
import workbench.gui.renderer.RowStatusRenderer;
import workbench.gui.renderer.StringColumnRenderer;
import workbench.gui.renderer.ToolTipRenderer;
import workbench.interfaces.Exporter;
import workbench.interfaces.FontChangedListener;
import workbench.interfaces.PrintableComponent;
import workbench.interfaces.Searchable;
import workbench.log.LogMgr;
import workbench.print.PrintPreview;
import workbench.print.TablePrinter;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.storage.DataStore;
import workbench.storage.NullValue;
import workbench.util.StringUtil;


public class WbTable
	extends JTable
	implements ActionListener, FocusListener, MouseListener,
	           Exporter, FontChangedListener, Searchable, PrintableComponent
{
	public static final LineBorder FOCUSED_CELL_BORDER = new LineBorder(Color.YELLOW);
	private JPopupMenu popup;

	private JPopupMenu headerPopup;

	private DataStoreTableModel dwModel;
	private String lastSearchCriteria;
	private int lastFoundRow = -1;

	private WbTextCellEditor defaultEditor;
	private DefaultCellEditor defaultNumberEditor;
	private JTextField numberEditorTextField;

	private SortAscendingAction sortAscending;
	private SortDescendingAction sortDescending;
	private OptimizeColumnWidthAction optimizeCol;
	private OptimizeAllColumnsAction optimizeAllCol;
	private SetColumnWidthAction setColWidth;

	private FindDataAction findAction;
	private FindDataAgainAction findAgainAction;
	private DataToClipboardAction dataToClipboard;
	private SaveDataAsAction exportDataAction;

	private PrintAction printDataAction;
	private PrintPreviewAction printPreviewAction;

	private boolean adjustToColumnLabel = false;
	private int headerPopupY = -1;
	private int headerPopupX = -1;
	private HashMap savedColumnSizes;
	private int maxColWidth = 32768;
	private int minColWidth = 10;
	private static final TableModel EMPTY_MODEL = new EmptyTableModel();
	private SortHeaderRenderer sortRenderer = new SortHeaderRenderer();

	private ToolTipRenderer defaultTooltipRenderer = new ToolTipRenderer();
	private DateColumnRenderer defaultDateRenderer;
	private NumberColumnRenderer defaultNumberRenderer;
	private NumberColumnRenderer defaultIntegerRenderer = new NumberColumnRenderer(0);
	private StringColumnRenderer defaultStringRenderer = new StringColumnRenderer();

	private RowHeightResizer rowResizer;
	//private List changeListener;
	private TableModelListener changeListener;
	private JScrollPane scrollPane;

	private String defaultPrintHeader = null;
	private boolean showRowNumbers = false;
	private JList rowHeader = null;
	private boolean showPopup = true;
	private boolean selectOnRightButtonClick = false;

	public WbTable()
	{
		super(EMPTY_MODEL);
		this.setMinimumSize(null);
		this.setMaximumSize(null);
		this.setPreferredSize(null);

		this.sortAscending = new SortAscendingAction(this);
		this.sortAscending.setEnabled(false);
		this.sortDescending = new SortDescendingAction(this);
		this.sortDescending.setEnabled(false);
		this.optimizeCol = new OptimizeColumnWidthAction(this);
		this.optimizeAllCol = new OptimizeAllColumnsAction(this);
		this.optimizeAllCol.setEnabled(true);
		this.setColWidth = new SetColumnWidthAction(this);

		this.headerPopup = new JPopupMenu();
		this.headerPopup.add(this.sortAscending.getMenuItem());
		this.headerPopup.add(this.sortDescending.getMenuItem());
		this.headerPopup.addSeparator();
		this.headerPopup.add(this.optimizeCol.getMenuItem());
		this.headerPopup.add(this.optimizeAllCol.getMenuItem());
		this.headerPopup.add(this.setColWidth.getMenuItem());

		this.setDoubleBuffered(true);

		Font dataFont = this.getFont();
		if (dataFont == null) dataFont = (Font)UIManager.get("Table.font");

		JTextField text = new JTextField();
		text.setFont(dataFont);
		this.defaultEditor = WbTextCellEditor.createInstance(this);
		this.defaultEditor.setFont(dataFont);

		numberEditorTextField = new JTextField();
		if (dataFont != null)  numberEditorTextField.setFont(dataFont);
		numberEditorTextField.setBorder(WbSwingUtilities.EMPTY_BORDER);
		numberEditorTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		numberEditorTextField.addMouseListener(new TextComponentMouseListener());
    WbCellEditor.setDefaultCopyPasteKeys(numberEditorTextField);

		this.defaultNumberEditor = new DefaultCellEditor(numberEditorTextField);

		this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		//JTableHeader th = this.getTableHeader();
		//th.addMouseListener(this);

		this.findAction = new FindDataAction(this);
		this.findAction.setEnabled(false);
		this.findAction.setCreateMenuSeparator(true);
		this.findAgainAction = new FindDataAgainAction(this);
		this.findAgainAction.setEnabled(false);

		this.dataToClipboard = new DataToClipboardAction(this);
		this.exportDataAction = new SaveDataAsAction(this);
		this.setBorder(WbSwingUtilities.EMPTY_BORDER);
		this.addPopupAction(this.exportDataAction, false);
		this.addPopupAction(this.dataToClipboard, false);
		this.addPopupAction(this.findAction, true);
		this.addPopupAction(this.findAgainAction, false);

		this.printDataAction = new PrintAction(this);
		this.printPreviewAction = new PrintPreviewAction(this);
		this.popup.addSeparator();
		this.popup.add(this.printDataAction.getMenuItem());
		this.popup.add(this.printPreviewAction.getMenuItem());

		this.addMouseListener(this);

		InputMap im = this.getInputMap(WHEN_FOCUSED);
		ActionMap am = this.getActionMap();
		this.findAction.addToInputMap(im, am);
		this.findAgainAction.addToInputMap(im, am);
		this.dataToClipboard.addToInputMap(im, am);
		this.exportDataAction.addToInputMap(im, am);
		this.optimizeAllCol.addToInputMap(im, am);
		WbManager.getSettings().addFontChangedListener(this);
	}


	public void setShowPopupMenu(boolean aFlag)
	{
		this.showPopup = aFlag;
	}

	public void setRowResizeAllowed(boolean aFlag)
	{
		if (aFlag && this.rowResizer == null)
		{
			this.rowResizer = new RowHeightResizer(this);
		}
		else if (!aFlag)
		{
			 if (this.rowResizer != null)
			 {
				 this.rowResizer.done();
			 }
			 this.rowResizer = null;
		}
	}

	public JToolTip createToolTip()
	{
		JToolTip tip = new MultiLineToolTip();
		tip.setComponent(this);
		return tip;
	}


	public SaveDataAsAction getExportAction()
	{
		return this.exportDataAction;
	}

	public DataToClipboardAction getDataToClipboardAction()
	{
		return this.dataToClipboard;
	}

	public FindDataAction getFindAction()
	{
		return this.findAction;
	}

	public FindDataAgainAction getFindAgainAction()
	{
		return this.findAgainAction;
	}

	public void setSelectOnRightButtonClick(boolean flag) { this.selectOnRightButtonClick = flag; }
	public boolean getSelectOnRightButtonClick() { return this.selectOnRightButtonClick; }

	public void reset()
	{
		if (this.dwModel != null)
		{
			this.dwModel.removeTableModelListener(this.changeListener);
			this.dwModel.dispose();
			this.dwModel = null;
		}
		this.setModel(EMPTY_MODEL, false);
	}

	public JPopupMenu getPopupMenu()
	{
		return this.popup;
	}

	public void addPopupAction(WbAction anAction, boolean withSep)
	{
		if (this.popup == null) this.popup = new JPopupMenu();


		if (this.printDataAction != null)
		{
			int count = this.popup.getComponentCount();

			if (withSep)
			{
				this.popup.add(new Separator(), this.popup.getComponentCount() - 3);
			}
			this.popup.add(anAction.getMenuItem(), this.popup.getComponentCount() - 3);
		}
		else
		{
			if (withSep) this.popup.addSeparator();
			this.popup.add(anAction.getMenuItem());
		}
	}


	protected void configureEnclosingScrollPane()
	{
		super.configureEnclosingScrollPane();
		Container p = getParent();
		if (p instanceof JViewport)
		{
			Container gp = p.getParent();
			if (gp instanceof JScrollPane)
			{
				// Make certain we are the viewPort's view and not, for
				// example, the rowHeaderView of the scrollPane -
				// an implementor of fixed columns might do this.
				this.scrollPane = (JScrollPane)gp;
				JViewport viewport = scrollPane.getViewport();
				if (viewport == null || viewport.getView() != this)
				{
					this.scrollPane = null;
				}
			}
		}
		this.checkMouseListener();
	}

	private void checkMouseListener()
	{
		JTableHeader th = this.getTableHeader();
		MouseListener[] list = th.getMouseListeners();
		if (list == null)
		{
			th.addMouseListener(this);
			return;
		}
		for (int i=0; i < list.length; i++)
		{
			if (list[i] == this) return;
		}
		th.addMouseListener(this);
	}

	public void printPreview()
	{
		TablePrinter printer = this.getTablePrinter();

		Window w = SwingUtilities.getWindowAncestor(this);
		JFrame parent = null;
		if (w instanceof JFrame)
		{
			parent = (JFrame)w;
		}
		PrintPreview preview = new PrintPreview(parent, printer);
	}

	public void setPrintHeader(String aHeader)
	{
		this.defaultPrintHeader = aHeader;
	}

	public String getPrintHeader()
	{
		return this.defaultPrintHeader;
	}

	public void print()
	{
		this.getTablePrinter().startPrint();
	}

	private TablePrinter getTablePrinter()
	{
		PageFormat format = WbManager.getSettings().getPageFormat();
		Font printerFont = WbManager.getSettings().getPrinterFont();
		TablePrinter printer = new TablePrinter(this, format, printerFont);
		if (this.defaultPrintHeader != null)
		{
			printer.setHeaderText(this.defaultPrintHeader);
		}
		printer.setFooterText(ResourceMgr.getString("TxtPageFooter"));
		return printer;
	}

	public synchronized void setModel(TableModel aModel)
	{
		this.setModel(aModel, false);
	}

	public synchronized void setModel(TableModel aModel, boolean sortIt)
	{
		if (this.dwModel != null)
		{
			this.dwModel.removeTableModelListener(this.changeListener);
			this.dwModel.removeTableModelListener(this);
			this.dwModel.dispose();
			this.dwModel = null;
		}

		JTableHeader header = this.getTableHeader();
		if (header != null)
		{
			header.removeMouseListener(this);
		}

		if (aModel instanceof DataStoreTableModel)
		{
			this.dwModel = (DataStoreTableModel)aModel;
			if (sortIt && header != null)
			{
				header.setDefaultRenderer(this.sortRenderer);
				header.addMouseListener(this);
			}
			DataStore ds = this.dwModel.getDataStore();
			if (ds != null)
			{
				// The underlying DataStore needs to know the date format in order
				// to convert input strings to dates
				ds.setDefaultDateFormat(WbManager.getSettings().getDefaultDateFormat());
			}
			else
			{
				Exception e = new NullPointerException();
				LogMgr.logError("WbTable.setModel()", "Received a DataStoreTableModel without a DataStore", e);
			}
		}

		if (this.sortAscending != null) this.sortAscending.setEnabled(sortIt);
		if (this.sortDescending != null) this.sortDescending.setEnabled(sortIt);

		if (this.changeListener != null && this.dwModel != null)
		{
			this.dwModel.addTableModelListener(this.changeListener);
		}

		super.setModel(aModel);

		this.initDefaultRenderers();
		this.initDefaultEditors();
		if (aModel == EMPTY_MODEL)
		{
			this.createDefaultColumnsFromModel();
		}
		if (this.printDataAction != null) this.printDataAction.setEnabled(this.getRowCount() > 0);
		if (this.printPreviewAction != null) this.printPreviewAction.setEnabled(this.getRowCount() > 0);

		if (this.showRowNumbers && aModel != EMPTY_MODEL)
		{
			ListModel lm = new AbstractListModel()
			{
				public int getSize() { return getRowCount(); }
				public Object getElementAt(int index)
				{
					return "";
				}
			};

			this.rowHeader = new JList(lm);
			this.rowHeader.setCellRenderer(new RowHeaderRenderer(this));
			String max = Integer.toString(aModel.getRowCount());
			FontMetrics fm = this.getFontMetrics(this.getFont());
			int width = fm.stringWidth(max);
			this.rowHeader.setFixedCellWidth(width + 10);

			if (this.scrollPane != null)
			{
				this.scrollPane.setRowHeaderView(this.rowHeader);
			}
		}
		else if (this.scrollPane != null)
		{
			this.scrollPane.setRowHeaderView(null);
		}
	}

	public DataStoreTableModel getDataStoreTableModel()
	{
		return this.dwModel;
	}

	public DataStore getDataStore()
	{
		if (this.dwModel != null)
		{
			return this.dwModel.getDataStore();
		}
		else
		{
			return null;
		}
	}

	public PrintPreviewAction getPrintPreviewAction()
	{
		return this.printPreviewAction;
	}

	public PrintAction getPrintAction()
	{
		return this.printDataAction;
	}

	public String getValueAsString(int row, int column)
		throws IndexOutOfBoundsException
	{
		Object value = this.getValueAt(row, column);
		if (value == null) return null;
		if (value instanceof NullValue) return null;
		return value.toString();
	}

	public boolean getShowStatusColumn()
	{
		if (this.dwModel == null) return false;
		return this.dwModel.getShowStatusColumn();
	}

	public void setAdjustToColumnLabel(boolean aFlag)
	{
		this.adjustToColumnLabel = aFlag;
	}

	public int getSortColum()
	{
		if (this.dwModel == null) return -1;
		return this.dwModel.getSortColumn();
	}

	public void setShowStatusColumn(boolean aFlag)
	{
		if (this.dwModel == null) return;
		if (aFlag == this.dwModel.getShowStatusColumn()) return;

		try
		{
			int column = this.getSelectedColumn();
			int row = this.getSelectedRow();

			int sortColumn = -1;
			boolean asc = false;
			if (this.dwModel != null)
			{
				sortColumn = dwModel.getSortColumn();
				asc = this.dwModel.isSortAscending();
			}

			this.saveColumnSizes();

			this.setSuspendRepaint(true);

			this.dwModel.setShowStatusColumn(aFlag);
			if (aFlag)
			{
				TableColumn col = this.getColumnModel().getColumn(0);
				col.setCellRenderer(new RowStatusRenderer());
				col.setMaxWidth(20);
				col.setMinWidth(20);
				col.setPreferredWidth(20);
			}
			else
			{
				TableColumnModel model = this.getTableHeader().getColumnModel();
				if (model.getColumnCount() > this.dwModel.getColumnCount())
				{
					TableColumn col = model.getColumn(0);
					model.removeColumn(col);
				}
			}

			this.initDefaultEditors();
			this.restoreColumnSizes();

			this.setSuspendRepaint(false);

			if (sortColumn > -1 && this.dwModel != null)
			{
				if (aFlag)
					sortColumn ++;
				else
					sortColumn --;
				this.dwModel.sortByColumn(sortColumn, asc);
			}
			if (row >= 0)
			{
				this.getSelectionModel().setSelectionInterval(row, row);
				int newColumn = column;
				if (aFlag)
					newColumn ++;
				else
					newColumn --;

				if (newColumn >= 0) this.changeSelection(row, newColumn, true, true);
			}
		}
		finally
		{
			this.setSuspendRepaint(false);
		}
	}

	private boolean suspendRepaint = false;

	public synchronized void setSuspendRepaint(boolean aFlag)
	{
		boolean suspend = this.suspendRepaint;
		this.suspendRepaint = aFlag;

		// if repainting was re-enabled, then queue
		// a repaint event right away
		if (suspend && !aFlag)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					invalidate();
					repaint();
				}
			});
		}
	}

	public void repaint()
	{
		if (this.suspendRepaint) return;
		super.repaint();
	}

	public void paintComponents(Graphics g)
	{
		if (this.suspendRepaint) return;
		super.paintComponents(g);
	}


	public int getSortedViewColumnIndex()
	{
		if (this.dwModel == null) return -1;
		int modelIndex = this.dwModel.getSortColumn();
		int viewIndex = this.convertColumnIndexToView(modelIndex);
		return viewIndex;
	}

	public boolean isSortedColumnAscending()
	{
		if (this.dwModel == null) return true;
		return this.dwModel.isSortAscending();
	}

	public synchronized void sortingStarted()
	{
		this.setIgnoreRepaint(true);
	}

	public synchronized void sortingFinished()
	{
		this.setIgnoreRepaint(false);
	}

	public String getDataString(String aLineTerminator)
	{
		return this.getDataString(aLineTerminator, true);
	}

	public String getDataString(String aLineTerminator, boolean includeHeaders)
	{
		if (this.dwModel == null) return "";
		DataStore ds = this.dwModel.getDataStore();
		return ds.getDataString(aLineTerminator, includeHeaders);
	}

	public boolean canSearchAgain()
	{
		return this.lastFoundRow >= 0;
	}

	public int search(String aText)
	{
		return this.search(aText, false);
	}

	public int searchNext()
	{
		return this.search(this.lastSearchCriteria, true);
	}

	public int search(String aText, boolean doContinue)
	{
		if (aText == null) return -1;
		aText = aText.toLowerCase();
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		int foundRow = -1;
		int start = 0;
		if (doContinue && this.lastFoundRow >= 0)
		{
			start = this.lastFoundRow  + 1;
		}

		for (int i=start; i < this.dwModel.getRowCount(); i++)
		{
			//int row = sortModel.getRealIndex(i);
			String rowString = this.dwModel.getRowData(i).toString();
			if (rowString == null) continue;

			if (rowString.toLowerCase().indexOf(aText) > -1)
			{
				this.getSelectionModel().setSelectionInterval(i,i);
				foundRow = i;
				this.lastSearchCriteria = aText;
				break;
			}
		}
		this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		this.lastFoundRow = foundRow;
		if (foundRow >= 0)
		{
			this.scrollToRow(foundRow);
		}

		return foundRow;
	}

	public void saveColumnSizes()
	{
		TableColumnModel colMod = this.getColumnModel();
		int count = colMod.getColumnCount();
		this.savedColumnSizes = new HashMap(count);
		for (int i=0; i < count; i++)
		{
			TableColumn col = colMod.getColumn(i);
			String name = this.getColumnName(i);
			Integer width = new Integer(col.getPreferredWidth());
			this.savedColumnSizes.put(name, width);
		}
	}

	public void restoreColumnSizes()
	{
		if (this.savedColumnSizes == null || this.savedColumnSizes.size() == 0) return;
		Iterator itr = this.savedColumnSizes.entrySet().iterator();
		while (itr.hasNext())
		{
			Entry entry = (Entry)itr.next();
			try
			{
				TableColumn col = this.getColumn(entry.getKey());
				int width = ((Integer)entry.getValue()).intValue();
				col.setPreferredWidth(width);
			}
			catch (Throwable th)
			{
				// ignore errors for columns which do no longer exist
			}
		}
		this.savedColumnSizes = null;
	}

	public TableCellRenderer getCellRenderer(int row, int column)
	{
		TableCellRenderer renderer = super.getCellRenderer(row, column);
		if (renderer == null)
		{
			renderer = ToolTipRenderer.DEFAULT_TEXT_RENDERER;
		}
		return renderer;
	}

	private boolean useDefaultStringRenderer = true;
	public void setUseDefaultStringRenderer(boolean aFlag)
	{
		this.useDefaultStringRenderer = aFlag;
	}
	public boolean getUseDefaultStringRenderer() { return this.useDefaultStringRenderer; }

	public void initDefaultRenderers()
	{
		// need to let JTable do some initialization stuff
		// otherwise setDefaultRenderer() bombs out with a NullPointerException
		if (this.defaultRenderersByColumnClass == null) this.createDefaultRenderers();

		if (this.defaultDateRenderer == null)
		{
			String format = WbManager.getSettings().getDefaultDateFormat();
			defaultDateRenderer = new DateColumnRenderer(format);
		}
		this.setDefaultRenderer(Date.class, defaultDateRenderer);

		int maxDigits = WbManager.getSettings().getMaxFractionDigits();
		char sep = WbManager.getSettings().getDecimalSymbol().charAt(0);

		if (this.defaultNumberRenderer == null)
		{
			this.defaultNumberRenderer = new NumberColumnRenderer(maxDigits, sep);
		}
		else
		{
			defaultNumberRenderer.setMaxDigits(maxDigits);
			defaultNumberRenderer.setDecimalSymbol(sep);
		}

		this.setDefaultRenderer(Number.class, defaultNumberRenderer);
		this.setDefaultRenderer(Double.class, defaultNumberRenderer);
		this.setDefaultRenderer(Float.class, defaultNumberRenderer);
		this.setDefaultRenderer(BigDecimal.class, defaultNumberRenderer);

		this.setDefaultRenderer(BigInteger.class, defaultIntegerRenderer);
		this.setDefaultRenderer(Integer.class, defaultIntegerRenderer);
		if (this.useDefaultStringRenderer)
		{
			this.setDefaultRenderer(String.class, defaultStringRenderer);
		}

		ToolTipRenderer rend = new ToolTipRenderer();
		this.setDefaultRenderer(Object.class, rend);

		/*
		InputMap mymap = this.getInputMap(WHEN_FOCUSED);
		InputMap im = defaultNumberRenderer.getInputMap(WHEN_FOCUSED);

		im.setParent(mymap);
		im = rend.getInputMap(WHEN_FOCUSED);
		im.setParent(mymap);
		im = rend.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		im.setParent(mymap);
		*/
	}

	public void initDefaultEditors()
	{
		if (this.dwModel == null) return;

		TableColumnModel colMod = this.getColumnModel();

		for (int i=0; i < colMod.getColumnCount(); i++)
		{
			TableColumn col = colMod.getColumn(i);
			if (Number.class.isAssignableFrom(this.dwModel.getColumnClass(i)))
			{
				col.setCellEditor(this.defaultNumberEditor);
			}
			else
			{
				col.setCellEditor(this.defaultEditor);
			}
		}
	}

	public void adjustColumns()
	{
		if (this.getModel() == null) return;
		Font f = this.getFont();
		FontMetrics fm = this.getFontMetrics(f);
		int charWidth = fm.stringWidth("n");
		TableColumnModel colMod = this.getColumnModel();
		if (colMod == null) return;

		for (int i=0; i < colMod.getColumnCount(); i++)
		{
			TableColumn col = colMod.getColumn(i);
			int addWidth = this.getAdditionalColumnSpace(0, i);
			int addHeaderWidth = this.getAdditionalColumnSpace(-1, i);

			if (this.dwModel != null)
			{
				int lblWidth = 0;
				if (this.adjustToColumnLabel)
				{
					String s = this.dwModel.getColumnName(i);
					lblWidth = fm.stringWidth(s) + addHeaderWidth;
				}
				int width = (this.dwModel.getColumnWidth(i) * charWidth) + addWidth;
				int w = Math.max(width, lblWidth);
				w	= Math.min(w, this.maxColWidth);
				if (w < this.minColWidth) w = this.minColWidth;
				col.setPreferredWidth(w);
			}
		}
	}

	public void optimizeAllColWidth()
	{
		this.optimizeAllColWidth(0);
	}

	public void optimizeAllColWidth(int aMinWidth)
	{
		int count = this.getColumnCount();
		for (int i=0; i < count; i++)
		{
			this.optimizeColWidth(i, aMinWidth);
		}
	}

	public void optimizeColWidth(int aColumn)
	{
		this.optimizeColWidth(aColumn, 0);
	}

	public void optimizeColWidth(int aColumn, int aMinWidth)
	{
		if (this.dwModel == null) return;
		if (aColumn < 0 || aColumn > this.getColumnCount() - 1) return;

		Font f = this.getFont();
		FontMetrics fm = this.getFontMetrics(f);
		TableColumnModel colMod = this.getColumnModel();
		TableColumn col = colMod.getColumn(aColumn);
		int addWidth = this.getAdditionalColumnSpace(0, aColumn);
		String s = null;//this.dwModel.getColumnName(aColumn);
		int optWidth = aMinWidth;
		int stringWidth = 0;
		int rowCount = this.getRowCount();
		for (int row = 0; row < rowCount; row ++)
		{
			s = this.getValueAsString(row, aColumn);
			if (s == null || s.length() == 0)
				stringWidth = 0;
			else
				stringWidth = fm.stringWidth(s);

			optWidth = Math.max(optWidth, stringWidth + addWidth);
		}
		if (optWidth > 0)
		{
			col.setPreferredWidth(optWidth);
		}
	}

	private int getAdditionalColumnSpace(int aRow, int aColumn)
	{
		TableColumn col = this.getColumnModel().getColumn(aColumn);
		TableCellRenderer rend;
		if (aRow == -1)
			rend = col.getHeaderRenderer();
		else
			rend = col.getCellRenderer();

		int addWidth = this.getIntercellSpacing().width * 2;
		if (this.getShowVerticalLines()) addWidth += 4;

		if (rend == null)
		{
			rend = this.getDefaultRenderer(this.getColumnClass(aColumn));
		}
		if (rend != null)
		{
			Component c = rend.getTableCellRendererComponent(this, null, false, false, aRow, aColumn);
			if (c instanceof JComponent)
			{
				JComponent jc = (JComponent)c;
				Insets ins = jc.getInsets();
				if (ins != null)
				{
					addWidth += ins.left + ins.right;
				}
			}
		}
		return addWidth;
	}

	public void cancelEditing()
	{
		if (this.isEditing())
		{
			CellEditor editor = this.getCellEditor();
			if(editor != null)
			{
				editor.cancelCellEditing();
			}
		}
	}

	public boolean stopEditing()
	{
		if (!this.isEditing()) return false;
		CellEditor editor = this.getCellEditor();
		if(editor != null)
		{
			return editor.stopCellEditing();
		}
		return false;
	}

	public void tableChanged(TableModelEvent evt)
	{
		super.tableChanged(evt);
		if (evt.getFirstRow() == TableModelEvent.HEADER_ROW)
		{
			this.initDefaultEditors();
		}
	}

	public void openEditWindow()
	{
		if (!this.isEditing()) return;

		int col = this.getEditingColumn();
		int row = this.getEditingRow();
		String data = this.getValueAsString(row, col);
		Window owner = (Window)SwingUtilities.getWindowAncestor(this);
		Frame ownerFrame = null;
		if (owner instanceof Frame)
		{
			ownerFrame = (Frame)owner;
		}
		String title = ResourceMgr.getString("TxtEditWindowTitle");
		TableCellEditor editor = this.getCellEditor();
		EditWindow w = new EditWindow(ownerFrame, title, data);
		w.show();
		if (editor != null)
		{
			// we need to "cancel" the editor so that the data
			// in the editor component will not be written into the
			// table model!
			editor.cancelCellEditing();
		}
		if (!w.isCancelled())
		{
			this.setValueAt(w.getText(), row, col);
		}
		w.dispose();
	}

	public void addTableModelListener(TableModelListener aListener)
	{
		this.changeListener = aListener;
		if (this.dwModel != null) this.dwModel.addTableModelListener(aListener);
	}

	public void removeTableModelListener(TableModelListener aListener)
	{
		this.changeListener = null;
		if (this.dwModel != null) this.dwModel.removeTableModelListener(aListener);
	}

	public int getFirstVisibleRow()
	{
		if (this.getRowCount() == 0) return -1;
		Point p = this.scrollPane.getViewport().getViewPosition();
		int row = this.rowAtPoint(p);
		return row;
	}

	public int getLastVisibleRow()
	{
		return this.getLastVisibleRow(this.getFirstVisibleRow());
	}

	public int getLastVisibleRow(int first)
	{
		int count = this.getRowCount();
		if (count == 0) return -1;

		JScrollBar bar = this.scrollPane.getVerticalScrollBar();
		if (bar != null && bar.getValue() == bar.getMaximum()) return count;

		JViewport view = this.scrollPane.getViewport();
		Point p = view.getViewPosition();
		Dimension d = view.getExtentSize();
		int height = (int)d.getHeight();
		int rowHeight = 0;
		int spacing = this.getRowMargin();
		int lastRow = 0;
		if (this.rowResizer == null)
		{
			// if the row height cannot be resized, we can
			// calculate the number of visible rows
			rowHeight = this.getRowHeight();
			int numRows = (int) ((height / rowHeight) - 0.5);
			lastRow = numRows;
		}
		else
		{
			for (int r = first; r < count; r ++)
			{
				int h = this.getRowHeight(r) + spacing;
				if (rowHeight + h > height) break;
				rowHeight += h;
			}

			//p.move(0, (int)d.getHeight());
			p.move(0, rowHeight);

			lastRow = this.rowAtPoint(p);
		}

		// if rowAtPoint() returns a negative number, then all
		// rows fit into the current viewport
		if (lastRow < 0) lastRow = this.getRowCount() - 1;

		return first + lastRow;
	}

	/** Scroll the given row into view.
	 */
	public void scrollToRow(int aRow)
	{
		Rectangle rect = this.getCellRect(aRow, 1, true);
		this.scrollRectToVisible(rect);
	}

	/**
	 *	Start sorting if the column header has been clicked.
	 *
	 */
	public void mouseClicked(MouseEvent e)
	{
		if (e.getButton() == MouseEvent.BUTTON3)
		{
			if (e.getSource() instanceof JTableHeader)
			{
				this.headerPopupX = e.getX();
				this.headerPopupY = e.getY();
				this.headerPopup.show(this.getTableHeader(), e.getX(), e.getY());
			}
			else if (this.showPopup && this.popup != null)
			{
				int row = this.rowAtPoint(e.getPoint());
				int selected = this.getSelectedRowCount();
				if (selected <= 1 && row >= 0 && this.selectOnRightButtonClick)
				{
					this.getSelectionModel().setSelectionInterval(row, row);
				}
				this.findAction.setEnabled(this.getRowCount() > 0);
				this.findAgainAction.setEnabled(this.lastFoundRow > 0);
				this.popup.show(this, e.getX(), e.getY());
			}
		}
		else if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1
		         && this.dwModel != null && e.getSource() instanceof JTableHeader)
		{
			TableColumnModel columnModel = this.getColumnModel();
			int viewColumn = columnModel.getColumnIndexAtX(e.getX());
			int realColumn = this.convertColumnIndexToModel(viewColumn);

			if (realColumn >= 0)
			{
				//sorter.startSorting(tableView, realColumn, sortAscending);
				this.dwModel.sortInBackground(this, realColumn);
			}
		}
	}

	/** Invoked when the mouse enters a component.
	 *
	 */
	public void mouseEntered(MouseEvent e)
	{
	}

	/** Invoked when the mouse exits a component.
	 *
	 */
	public void mouseExited(MouseEvent e)
	{
	}

	/** Invoked when a mouse button has been pressed on a component.
	 *
	 */
	public void mousePressed(MouseEvent e)
	{
	}

	/** Invoked when a mouse button has been released on a component.
	 *
	 */
	public void mouseReleased(MouseEvent e)
	{
	}

	public void setColumnWidth(int column, int width)
	{
		TableColumn col = this.getColumnModel().getColumn(column);
		if (width > 0 && col != null)
		{
			col.setWidth(width);
			col.setPreferredWidth(width);
		}
	}

	public void actionPerformed(ActionEvent e)
	{
		TableColumnModel columnModel = this.getColumnModel();
		int viewColumn = columnModel.getColumnIndexAtX(this.headerPopupX);
		final int column = this.convertColumnIndexToModel(viewColumn);
		if (e.getSource() == this.sortAscending && this.dwModel != null)
		{
			this.dwModel.sortInBackground(this, column, true);
		}
		else if (e.getSource() == this.sortDescending && this.dwModel != null)
		{
			this.dwModel.sortInBackground(this, column, false);
		}
		else if (e.getSource() == this.optimizeCol)
		{
			Thread t = new Thread()
			{
				public void run()	{ optimizeColWidth(column); }
			};
			t.setName("OptimizeCol Thread");
			t.start();

		}
		else if (e.getSource() == this.optimizeAllCol)
		{
			Thread t = new Thread() { 	public void run()	{ optimizeAllColWidth(); } };
			t.setName("OptimizeAllCols Thread");
			t.start();
		}
		else if (e.getSource() == this.setColWidth)
		{
			try
			{
				TableColumn col = this.getColumnModel().getColumn(column);
				int colWidth = col.getWidth();
				String s = WbSwingUtilities.getUserInput(this, ResourceMgr.getString("MsgEnterNewColWidth"), Integer.toString(colWidth));
				if (s != null)
				{
					try { colWidth = Integer.parseInt(s); } catch (Exception ex) { colWidth = -1; }
					if (colWidth > 0)
					{
						col.setWidth(colWidth);
						col.setPreferredWidth(colWidth);
					}
				}
			}
			catch (Exception ex2)
			{
			}
		}
	}

	public void resetPopup()
	{
		if (this.popup != null) this.popup.setVisible(false);
		this.popup = null;
	}

	/**
	 *	Open the Find dialog for searching strings in the result set
	 */
	public int find()
	{
		String criteria;
		criteria = WbSwingUtilities.getUserInput(this, ResourceMgr.getString("MsgEnterSearchCriteria"), this.lastSearchCriteria);
		if (criteria == null) return -1;
		int row = this.search(criteria, false);
		this.lastSearchCriteria = criteria;
		this.findAgainAction.setEnabled(row >= 0);
		return row;
	}

	public int findNext()
	{
		return this.searchNext();
	}

	public void copyDataToClipboard()
	{
		this.copyDataToClipboard(true);
	}

	public void copyDataToClipboard(final boolean includeheaders)
	{
		if (this.getRowCount() <= 0) return;

		try
		{
			Clipboard clp = Toolkit.getDefaultToolkit().getSystemClipboard();
			WbSwingUtilities.showWaitCursorOnWindow(this);
			String data = getDataString("\n", includeheaders);
			StringSelection sel = new StringSelection(data);
			clp.setContents(sel, sel);
		}
		catch (Throwable e)
		{
			LogMgr.logError(this, "Could not copy text data to clipboard", e);
		}
		WbSwingUtilities.showDefaultCursorOnWindow(this);
	}

	public void copyAsSqlUpdate()
	{
		this.copyAsSql(true);
	}

	public void copyAsSqlInsert()
	{
		this.copyAsSql(false);
	}

	/**
	 * 	Copy the data of this table into the clipboard using SQL statements
	 *
	 */
	private void copyAsSql(boolean useUpdate)
	{
		if (this.getRowCount() <= 0) return;

		DataStore ds = this.dwModel.getDataStore();
		if (ds == null) return;
		if (!ds.canSaveAsSqlInsert()) return;

		try
		{
			Clipboard clp = Toolkit.getDefaultToolkit().getSystemClipboard();
			WbSwingUtilities.showWaitCursorOnWindow(this);
			String data;
			if (useUpdate)
			{
				data = ds.getDataAsSqlUpdate();
			}
			else
			{
				data = ds.getDataAsSqlInsert();
			}
			StringSelection sel = new StringSelection(data);
			clp.setContents(sel, sel);
		}
		catch (Throwable e)
		{
			LogMgr.logError(this, "Error when copying SQL inserts", e);
		}
		WbSwingUtilities.showDefaultCursorOnWindow(this);
	}

	public void saveAsSqlInsert(String aFilename)
	{
		this.saveAsSql(aFilename, false);
	}

	public void saveAsSqlUpdate(String aFilename)
	{
		this.saveAsSql(aFilename, true);
	}

	private void saveAsSql(String aFilename, boolean useUpdate)
	{
		if (this.getRowCount() <= 0) return;
		if (this.dwModel == null) return;
		DataStore ds = this.dwModel.getDataStore();
		if (ds == null) return;
		if (!ds.canSaveAsSqlInsert()) return;

		PrintWriter out = null;

		try
		{
			WbSwingUtilities.showWaitCursor(this);
			out = new PrintWriter(new BufferedOutputStream(new FileOutputStream(aFilename)));
			if (useUpdate)
			{
				ds.writeDataAsSqlUpdate(out, StringUtil.LINE_TERMINATOR);
			}
			else
			{
				ds.writeDataAsSqlInsert(out, StringUtil.LINE_TERMINATOR);
			}
		}
		catch (Throwable th)
		{
			LogMgr.logError(this, "Could not save SQL data", th);
		}
		finally
		{
			try { out.close(); } catch (Throwable th) {}
		}
		WbSwingUtilities.showDefaultCursor(this);
	}

	public void saveAsHtml(String aFilename)
	{
		if (this.dwModel == null) return;

		PrintWriter out = null;

		WbSwingUtilities.showWaitCursor(this.getParent());
		try
		{
			out = new PrintWriter(new BufferedOutputStream(new FileOutputStream(aFilename)));
			DataStore ds = this.getDataStore();
			ds.writeHtmlData(out);
		}
		catch (Throwable th)
		{
			LogMgr.logError(this, "Could not save data", th);
		}
		finally
		{
			try { out.close(); } catch (Throwable th) {}
		}
		WbSwingUtilities.showDefaultCursor(this.getParent());
	}

	public void saveAsXml(String aFilename)
	{
		if (this.dwModel == null) return;

		PrintWriter out = null;

		WbSwingUtilities.showWaitCursor(this.getParent());
		try
		{
			out = new PrintWriter(new BufferedOutputStream(new FileOutputStream(aFilename)));
			DataStore ds = this.getDataStore();
			ds.writeXmlData(out);
		}
		catch (Throwable th)
		{
			LogMgr.logError(this, "Could not save data", th);
		}
		finally
		{
			try { out.close(); } catch (Throwable th) {}
		}
		WbSwingUtilities.showDefaultCursor(this.getParent());
	}

	public void saveAsAscii(String aFilename)
	{
		if (this.dwModel == null) return;

		PrintWriter out = null;

		WbSwingUtilities.showWaitCursor(this.getParent());
		try
		{
			out = new PrintWriter(new BufferedOutputStream(new FileOutputStream(aFilename)));
			DataStore ds = this.getDataStore();
			ds.writeDataString(out, WbManager.getSettings().getDefaultTextDelimiter(), StringUtil.LINE_TERMINATOR, true);
		}
		catch (Throwable th)
		{
			LogMgr.logError(this, "Could not save data", th);
		}
		finally
		{
			try { out.close(); } catch (Throwable th) {}
		}
		WbSwingUtilities.showDefaultCursor(this.getParent());
	}

	public void saveAs()
	{
		try
		{
			DataStore ds = this.dwModel.getDataStore();
			boolean sql = (ds != null && ds.canSaveAsSqlInsert());
			String filename = WbManager.getInstance().getExportFilename(this, sql);
			if (filename != null)
			{
				//String ext = ExtensionFileFilter.getExtension(new File(filename));
				final String name = filename;
				int type = WbManager.getInstance().getLastSelectedFileType();

				Thread t = null;
				switch (type)
				{
					case WbManager.FILE_TYPE_SQL:
						t = new Thread() { public void run() { saveAsSqlInsert(name); } };
						t.setDaemon(true);
						t.setName("SaveAsSql Thread");
						t.start();
						break;
					case WbManager.FILE_TYPE_SQL_UPDATE:
						t = new Thread() { public void run() { saveAsSqlUpdate(name); } };
						t.setDaemon(true);
						t.setName("SaveAsSql Thread");
						t.start();
						break;
					case WbManager.FILE_TYPE_TXT:
						t = new Thread() { public void run() { saveAsAscii(name); }};
						t.setDaemon(true);
						t.setName("SaveAsAscii Thread");
						t.start();
						break;
					case WbManager.FILE_TYPE_HTML:
						t = new Thread() { public void run() { saveAsHtml(name); }};
						t.setName("saveAsHtml Thread");
						t.setDaemon(true);
						t.start();
						break;
					case WbManager.FILE_TYPE_XML:
						t = new Thread() { public void run() { saveAsXml(name); }};
						t.setDaemon(true);
						t.setName("SaveAsXml Thread");
						t.start();
						break;
					default:
						LogMgr.logError("WbTable.saveAs()", "Unknown file type selected", null);
				}
			}
		}
		catch (Exception e)
		{
			LogMgr.logError(this, "Error exporting data", e);
		}
	}

	public int getMaxColWidth()
	{
		return maxColWidth;
	}

	public void setMaxColWidth(int maxColWidth)
	{
		this.maxColWidth = maxColWidth;
	}

	public int getMinColWidth()
	{
		return minColWidth;
	}

	public void setMinColWidth(int minColWidth)
	{
		this.minColWidth = minColWidth;
	}

	public void fontChanged(String aFontId, Font newFont)
	{
		if (aFontId.equals(Settings.DATA_FONT_KEY))
		{
			this.setFont(newFont);
			this.getTableHeader().setFont(newFont);
			//this.defaultEditor.setFont(newFont);
			this.numberEditorTextField.setFont(newFont);
		}
	}

	public String toString()
	{
		return getClass().getName() + '@' + Integer.toHexString(hashCode());
	}

	public void focusGained(FocusEvent e)
	{
	}

	public void focusLost(FocusEvent e)
	{
		this.stopEditing();
	}

	public long addRow()
	{
		DataStoreTableModel ds = this.getDataStoreTableModel();
		if (ds == null) return -1;

		int selectedRow = this.getSelectedRow();
		final int newRow;

		this.stopEditing();

		if (selectedRow == -1)
		{
			newRow = ds.addRow();
		}
		else
		{
			newRow = ds.insertRow(selectedRow);
		}
		this.getSelectionModel().setSelectionInterval(newRow, newRow);
		this.scrollToRow(newRow);
		this.setEditingRow(newRow);
		if (this.dwModel.getShowStatusColumn())
		{
			this.setEditingColumn(1);
			this.editCellAt(newRow, 1);
		}
		else
		{
			this.setEditingColumn(0);
			this.editCellAt(newRow, 0);
		}

		final Component edit = this.getEditorComponent();
		if (edit != null)
		{
			EventQueue.invokeLater(new Runnable()
			{
				public void run()
				{
					edit.requestFocusInWindow();
				}
			});
		}
		return newRow;
	}

	public int duplicateRow()
	{
		DataStoreTableModel model = this.getDataStoreTableModel();
		if (model == null) return -1;
		if (this.getSelectedRowCount() != 1) return -1;
		int row = this.getSelectedRow();
		int newRow = model.duplicateRow(row);
		return newRow;
	}

	public boolean deleteRow()
	{
		DataStoreTableModel ds = this.getDataStoreTableModel();
		if (ds == null) return false;

		int[] selectedRows = this.getSelectedRows();
		int numRows = selectedRows.length;
		if (numRows > 0)
		{
			for (int i = numRows - 1; i >= 0; i--)
			{
				ds.deleteRow(selectedRows[i]);
			}
		}
//		if (selectedRow != -1)
//		{
//			ds.deleteRow(selectedRow);
//			if (selectedRow >= ds.getRowCount())
//			{
//				selectedRow --;
//			}
//			this.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
//		}
		return true;
	}

}

class RowHeaderRenderer
	extends JLabel
	implements ListCellRenderer
{
	private WbTable table;
	RowHeaderRenderer(WbTable aTable)
	{
		this.table = aTable;
		JTableHeader header = table.getTableHeader();
		setOpaque(true);
		//Border b = new CompoundBorder(new DividerBorder(DividerBorder.TOP, 1, false), new EmptyBorder(0, 0, 0, 2));
		Border b = new WbLineBorder(WbLineBorder.BOTTOM);
		setBorder(b);
		setHorizontalAlignment(RIGHT);
		setForeground(header.getForeground());
		setBackground(header.getBackground());
		setFont(header.getFont());
	}

	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
	{
		setText(Integer.toString(index + 1));
		Dimension d = this.getPreferredSize();
		d.height = this.table.getRowHeight(index);
		this.setPreferredSize(d);
		this.setMaximumSize(d);
		this.setMinimumSize(d);
		return this;
	}
}