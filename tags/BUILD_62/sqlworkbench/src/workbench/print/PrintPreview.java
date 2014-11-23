/*
 * PrintPreview.java
 *
 * Created on July 23, 2003, 3:38 PM
 */

package workbench.print;

import java.awt.*;
import java.awt.Stroke;
import java.awt.event.*;
import java.awt.event.WindowListener;
import java.awt.image.*;
import java.util.*;
import java.awt.print.*;
import java.awt.print.Paper;
import javax.print.attribute.Attribute;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.Size2DSyntax;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import workbench.WbManager;
import workbench.gui.WbSwingUtilities;
import workbench.gui.components.WbToolbar;
import workbench.gui.components.WbToolbarButton;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;


public class PrintPreview 
	extends JDialog 
	implements ActionListener, WindowListener
{
	protected int pageWidth;
	protected int pageHeight;
	private int scale = 100;
	
	protected TablePrinter printTarget;
	
	protected JComboBox cbZoom;
	private JButton pageSetupButton;
	private JButton printButton;
	private JButton closeButton;
	
	private JButton pageRight;
	private JButton pageLeft;
	
	private JButton pageDown;
	private JButton pageUp;
	private boolean hasHorizontalPages;
	
	private JScrollPane scroll;
	protected PreviewContainer preview;
	private PagePreview pageDisplay;
	private int currentPage = 0;
	
	public PrintPreview(JFrame owner, TablePrinter target)
	{
		super(owner, ResourceMgr.getString("TxtPrintPreviewWindowTitle"), true);
		
		if (!WbManager.getSettings().restoreWindowSize(this))
		{
			setSize(500, 600);
		}
		if (!WbManager.getSettings().restoreWindowPosition(this))
		{
			WbSwingUtilities.center(this, owner);
		}
		getContentPane().setLayout(new BorderLayout());
		this.printTarget = target;

		WbToolbar tb = new WbToolbar();
		tb.addDefaultBorder();
		
		this.printButton = new WbToolbarButton(ResourceMgr.getString("LabelPrintButton"));
		this.printButton.addActionListener(this);
		tb.add(printButton);
		
		this.pageSetupButton = new WbToolbarButton(ResourceMgr.getString("LabelPageSetupButton"));
		this.pageSetupButton.addActionListener(this);
		tb.add(this.pageSetupButton);

		tb.addSeparator();
		
		this.pageDown = new WbToolbarButton(ResourceMgr.getImage("Down"));
		this.pageDown.addActionListener(this);
		this.pageDown.setEnabled(false);
		tb.add(this.pageDown);

		this.pageUp = new WbToolbarButton(ResourceMgr.getImage("Up"));
		this.pageUp.addActionListener(this);
		this.pageUp.setEnabled(false);
		tb.add(this.pageUp);

		if (this.printTarget.getPagesAcross() > 1)
		{
			this.hasHorizontalPages = true;
			
			this.pageLeft = new WbToolbarButton(ResourceMgr.getImage("Back"));
			this.pageLeft.addActionListener(this);
			this.pageLeft.setEnabled(false);
			tb.add(this.pageLeft);

			this.pageRight = new WbToolbarButton(ResourceMgr.getImage("Forward"));
			this.pageRight.addActionListener(this);
			this.pageRight.setEnabled(false);
			tb.add(this.pageRight);
		}
		
		tb.addSeparator();

		String[] scales = { "10%", "25%", "50%", "100%", "150%"};
		this.cbZoom = new JComboBox(scales);
		this.cbZoom.setMaximumSize(this.cbZoom.getPreferredSize());
		this.cbZoom.setEditable(true);
		this.cbZoom.setSelectedItem("100%");
		this.cbZoom.addActionListener(this);
		tb.add(this.cbZoom);
		tb.addSeparator();
		
		this.closeButton = new WbToolbarButton(ResourceMgr.getString("LabelClose"));
		this.closeButton.addActionListener(this);
		tb.add(this.closeButton);

		getContentPane().add(tb, BorderLayout.NORTH);

		this.addWindowListener(this);
		
		this.preview = new PreviewContainer();
		this.pageDisplay = new PagePreview();
		this.preview.add(this.pageDisplay);
		showCurrentPage();
		
		this.scroll = new JScrollPane(this.preview);
		adjustScrollbar();
		
		getContentPane().add(scroll, BorderLayout.CENTER);
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setVisible(true);
	}
	
	private void adjustScrollbar()
	{
		this.scroll.getVerticalScrollBar().setBlockIncrement((int)printTarget.getPageFormat().getImageableHeight());
		Font f = this.printTarget.getFont();
		FontMetrics fm = this.getFontMetrics(f);
		this.scroll.getVerticalScrollBar().setUnitIncrement((int)fm.getHeight());
	}
	
	private void showCurrentPage()
	{
		WbSwingUtilities.showWaitCursorOnWindow(this);
		try
		{
			PageFormat pageFormat = this.printTarget.getPageFormat();
			this.pageWidth = (int)(pageFormat.getWidth());
			this.pageHeight = (int)(pageFormat.getHeight());

			int w = (int)(this.pageWidth * this.scale/100);
			int h = (int)(this.pageHeight* this.scale/100);

			try
			{
				BufferedImage img = new BufferedImage(pageWidth, pageHeight, BufferedImage.TYPE_INT_RGB);
				Graphics2D g = img.createGraphics();
				g.setColor(Color.white);
				g.fillRect(0, 0, pageWidth, pageHeight);
				g.setColor(Color.LIGHT_GRAY);
				Stroke s = g.getStroke();
				g.setStroke(new BasicStroke(0.2f));
				g.drawRect((int)pageFormat.getImageableX() - 1, (int)pageFormat.getImageableY() - 1, (int)pageFormat.getImageableWidth() + 1, (int)pageFormat.getImageableHeight() + 1);
				g.setStroke(s);
				if (this.printTarget.print(g, pageFormat, this.currentPage) == Printable.PAGE_EXISTS)			
				{
					this.pageDisplay.setImage(w,h,img);
				}
			}
			catch (PrinterException e)
			{
				LogMgr.logError("PrintPreview.updateDisplay()", "Error when creating preview", e);
				WbManager.getInstance().showErrorMessage(this, ResourceMgr.getString("MsgPrintPreviewError") + "\n" + e.getMessage());
			}
		}
		catch (OutOfMemoryError e)
		{
			WbManager.getInstance().showErrorMessage(this, ResourceMgr.getString("MsgOutOfMemoryError"));
			this.pageDisplay.setImage(0,0,null);
		}
		finally
		{
			this.validate();
			this.repaint();
			WbSwingUtilities.showDefaultCursorOnWindow(this);
		}
		
		this.pageUp.setEnabled(this.printTarget.getPreviousVerticalPage(this.currentPage) != -1);
		this.pageDown.setEnabled(this.printTarget.getNextVerticalPage(this.currentPage) != -1);
		
		if (this.hasHorizontalPages)
		{
			this.pageLeft.setEnabled(this.printTarget.getPreviousHorizontalPage(this.currentPage) != -1);
			this.pageRight.setEnabled(this.printTarget.getNextHorizontalPage(this.currentPage) != -1);
		}
	}

	public void doPrint()
	{
		try
		{
			PrinterJob prnJob = PrinterJob.getPrinterJob();
			prnJob.setPrintable(this.printTarget, this.printTarget.getPageFormat());
			prnJob.setPageable(this.printTarget);

			if (!prnJob.printDialog())
				return;
			this.setCursor( Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			prnJob.print();
			this.setCursor( Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			this.dispose();
		}
		catch (PrinterException ex)
		{
			ex.printStackTrace();
			System.err.println("Printing error: "+ex.toString());
		}
	}
	
	private synchronized void showNativePageSetup()
	{
		PrinterJob prnJob = PrinterJob.getPrinterJob();
		PageFormat oldFormat = this.printTarget.getPageFormat();
		
		PageFormat newFormat = prnJob.pageDialog(oldFormat);
		
		if (!PrintUtil.pageFormatEquals(newFormat, oldFormat))
		{
			WbManager.getSettings().setPageFormat(newFormat);
			this.printTarget.setPageFormat(newFormat);
			showCurrentPage();
			this.doLayout();
		}
	}
	
	public void doPageSetup()
	{
		Thread t = new Thread()
		{
			public void run()
			{
				showNativePageSetup();
			}
		};
		t.start();
	}
	
	public void changeZoom()
	{
		WbSwingUtilities.showWaitCursor(this);
		try
		{
			String str = cbZoom.getSelectedItem().toString();
			if (str.endsWith("%")) str = str.substring(0, str.length()-1);
			str = str.trim();
			try
			{
				scale = Integer.parseInt(str);
			}
			catch (NumberFormatException ex)
			{
				return;
			}
			int w = (int)(pageWidth*scale/100);
			int h = (int)(pageHeight*scale/100);

			Component[] comps = this.preview.getComponents();
			for (int k=0; k<comps.length; k++)
			{
				if (!(comps[k] instanceof PagePreview))
					continue;
				PagePreview pp = (PagePreview)comps[k];
				pp.setScaledSize(w, h);
			}
		}
		catch (Throwable th)
		{
			LogMgr.logError("PrintPreview.changeZoom()", "Error when changing the zoom factor", th);
		}
		finally
		{
			this.preview.validate();
			this.preview.doLayout();
			WbSwingUtilities.showDefaultCursor(this);
		}
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == this.printButton)
		{
			this.doPrint();
		}
		else if (e.getSource() == this.pageSetupButton)
		{
			this.doPageSetup();
		}
		else if (e.getSource() == this.cbZoom)
		{
			Thread runner = new Thread()
			{
				public void run()
				{
					changeZoom();
				}
			};
			runner.start();
		}
		else if (e.getSource() == this.pageRight)
		{
			int newIndex = this.printTarget.getNextHorizontalPage(this.currentPage);
			if (newIndex != -1)	this.currentPage = newIndex;
			this.showCurrentPage();
		}
		else if (e.getSource() == this.pageLeft)
		{
			int newIndex = this.printTarget.getPreviousHorizontalPage(this.currentPage);
			if (newIndex != -1)	this.currentPage = newIndex;
			this.showCurrentPage();
		}
		else if (e.getSource() == this.pageUp)
		{
			int newIndex = this.printTarget.getPreviousVerticalPage(this.currentPage);
			if (newIndex != -1)
			{
				this.currentPage = newIndex;
				this.showCurrentPage();
			}
		}
		else if (e.getSource() == this.pageDown)
		{
			int newIndex = this.printTarget.getNextVerticalPage(this.currentPage);
			if (newIndex != -1)
			{
				this.currentPage = newIndex;
				this.showCurrentPage();
			}
		}
		else if (e.getSource() == this.closeButton)
		{
			this.saveSettings();
			this.dispose();
		}
	}

	public void saveSettings()
	{
		WbManager.getSettings().storeWindowSize(this);
		WbManager.getSettings().storeWindowPosition(this);
	}
	
	public void windowActivated(WindowEvent e)
	{
	}
	
	public void windowClosed(WindowEvent e)
	{
	}
	
	public void windowClosing(WindowEvent e)
	{
		this.saveSettings();
	}
	
	public void windowDeactivated(WindowEvent e)
	{
	}
	
	public void windowDeiconified(WindowEvent e)
	{
	}
	
	public void windowIconified(WindowEvent e)
	{
	}
	
	public void windowOpened(WindowEvent e)
	{
	}
	
	class PreviewContainer
		extends JPanel
	{
		protected int H_GAP = 16;
		protected int V_GAP = 10;

		public Dimension getPreferredSize()
		{
			int n = getComponentCount();
			if (n == 0)
				return new Dimension(H_GAP, V_GAP);
			Component comp = getComponent(0);
			Dimension dc = comp.getPreferredSize();
			int w = dc.width;
			int h = dc.height;
			
			Dimension dp = getParent().getSize();
			int nCol = Math.max((dp.width-H_GAP)/(w+H_GAP), 1);
			int nRow = n/nCol;
			if (nRow*nCol < n)
				nRow++;

			int ww = nCol*(w+H_GAP) + H_GAP;
			int hh = nRow*(h+V_GAP) + V_GAP;
			Insets ins = getInsets();
			return new Dimension(ww+ins.left+ins.right, hh+ins.top+ins.bottom);
		}

		public Dimension getMaximumSize()
		{
			return getPreferredSize();
		}

		public Dimension getMinimumSize()
		{
			return getPreferredSize();
		}

		public void doLayout()
		{
			Insets ins = getInsets();
			int x = ins.left + H_GAP;
			int y = ins.top + V_GAP;

			int n = getComponentCount();
			if (n == 0)
				return;
			Component comp = getComponent(0);
			Dimension dc = comp.getPreferredSize();
			int w = dc.width;
			int h = dc.height;
			
			Dimension dp = getParent().getSize();
			int nCol = Math.max((dp.width-H_GAP)/(w+H_GAP), 1);
			int nRow = n/nCol;
			if (nRow*nCol < n)
				nRow++;

			int index = 0;
			for (int k = 0; k<nRow; k++)
			{
				for (int m = 0; m<nCol; m++)
				{
					if (index >= n)
						return;
					comp = getComponent(index++);
					comp.setBounds(x, y, w, h);
					x += w+H_GAP;
				}
				y += h+V_GAP;
				x = ins.left + H_GAP;
			}
		}
	}

	class PagePreview
		extends JPanel
	{
		protected int m_w;
		protected int m_h;
		protected Image m_source;
		protected Image m_img;

		public PagePreview()
		{
		}
		
		public PagePreview(int w, int h, Image source)
		{
			this.setImage(w,h,source);
		}
		
		public void setImage(int w, int h, Image source)
		{
			m_w = w;
			m_h = h;
			m_source= source;
			m_img = m_source.getScaledInstance(m_w, m_h, Image.SCALE_SMOOTH);
			m_img.flush();
			setBackground(Color.WHITE);
			setBorder(new MatteBorder(1, 1, 2, 2, Color.BLACK));
		}

		public void setScaledSize(int w, int h)
		{
			m_w = w;
			m_h = h;
			m_img = m_source.getScaledInstance(m_w, m_h, Image.SCALE_SMOOTH);
			repaint();
		}

		public Dimension getPreferredSize()
		{
			Insets ins = getInsets();
			return new Dimension(m_w+ins.left+ins.right, m_h+ins.top+ins.bottom);
		}

		public Dimension getMaximumSize()
		{
			return getPreferredSize();
		}

		public Dimension getMinimumSize()
		{
			return getPreferredSize();
		}

		public void paint(Graphics g)
		{
			if (this.m_img != null)
			{
				g.setColor(getBackground());
				g.fillRect(0, 0, getWidth(), getHeight());
				//g.setColor(Color.LIGHT_GRAY);
				g.drawImage(m_img, 0, 0, this);
				paintBorder(g);
			}
		}
	}

	public static void main(String[] args)
	{
		int cols = 7;
		int rows = 150;
		
		DefaultTableModel data = new DefaultTableModel(rows, cols);
		for (int row = 0; row < rows; row ++)
		{
			for (int c = 0; c < cols; c++)
			{
				data.setValueAt("Test" + row + "/" + c, row, c);
			}
		}
		JTable tbl = new JTable(data);
		TableColumnModel mod = tbl.getColumnModel();
		for (int c = 0; c < cols; c++)
		{
			mod.getColumn(c).setWidth(85);
		}

		try
		{
			WbManager.getInstance().initSettings();
			PrinterJob pj=PrinterJob.getPrinterJob();
			PageFormat page = pj.defaultPage();
			TablePrinter printer = new TablePrinter(tbl, page, new Font("Courier New", Font.PLAIN, 12));
			printer.setFooterText("Page");
			PrintPreview preview = new PrintPreview((JFrame)null, printer);
			System.out.println("done.");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.exit(0);
	}
	
}