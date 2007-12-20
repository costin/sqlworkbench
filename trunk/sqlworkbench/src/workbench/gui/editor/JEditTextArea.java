/*
 * JEditTextArea.java - jEdit's text component
 * Copyright (C) 1999 Slava Pestov
 *
 * You may use and modify this package for any purpose. Redistribution is
 * permitted, in both source and binary form, provided that this notice
 * remains intact in all source distributions of this package.
 */
package workbench.gui.editor;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.Segment;
import javax.swing.text.Utilities;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import workbench.gui.actions.WbAction;
import workbench.gui.menu.TextPopup;
import workbench.interfaces.ClipboardSupport;
import workbench.interfaces.EditorStatusbar;
import workbench.interfaces.TextChangeListener;
import workbench.interfaces.TextSelectionListener;
import workbench.interfaces.Undoable;
import workbench.log.LogMgr;
import workbench.resource.Settings;
import workbench.util.NumberStringCache;
import workbench.util.StringUtil;

/**
 * jEdit's text area component. It is more suited for editing program
 * source code than JEditorPane, because it drops the unnecessary features
 * (images, variable-width lines, and so on) and adds a whole bunch of
 * useful goodies such as:
 * <ul>
 * <li>More flexible key binding scheme
 * <li>Supports macro recorders
 * <li>Rectangular selection
 * <li>Bracket highlighting
 * <li>Syntax highlighting
 * <li>Command repetition
 * <li>Block caret can be enabled
 * </ul>
 * It is also faster and doesn't have as many problems. It can be used
 * in other applications; the only other part of jEdit it depends on is
 * the syntax package.<p>
 *
 * To use it in your app, treat it like any other component, for example:
 * <pre>JEditTextArea ta = new JEditTextArea();
 * ta.setTokenMarker(new JavaTokenMarker());
 * ta.setText("public class Test {\n"
 *     + "    public static void main(String[] args) {\n"
 *     + "        System.out.println(\"Hello World\");\n"
 *     + "    }\n"
 *     + "}");</pre>
 *
 * @author Slava Pestov
 */
public class JEditTextArea
	extends JComponent
	implements MouseWheelListener, Undoable, ClipboardSupport, FocusListener
{
	protected boolean rightClickMovesCursor = false;
	
	private Color alternateSelectionColor;
	private static final Color ERROR_COLOR = Color.RED.brighter();
	private static final Color TEMP_COLOR = Color.GREEN.brighter();
	private boolean currentSelectionIsTemporary;
	protected String commentChar;
	private TokenMarker currentTokenMarker;
	
	private KeyListener keyEventInterceptor;
	private EditorStatusbar statusBar;
	
	protected static final String CENTER = "center";
	protected static final String RIGHT = "right";
	protected static final String BOTTOM = "bottom";

	protected Timer caretTimer;

	protected TextAreaPainter painter;

	protected TextPopup popup;

	protected EventListenerList listeners;
	protected MutableCaretEvent caretEvent;

	protected boolean caretBlinks;
	protected boolean caretVisible;
	protected boolean blink;

	protected boolean editable;
	protected boolean autoIndent;

	protected int firstLine;
	protected int visibleLines;
	protected int electricScroll;

	protected int horizontalOffset;

	protected JScrollBar vertical;
	protected JScrollBar horizontal;
	protected boolean scrollBarsInitialized;

	protected InputHandler inputHandler;
	protected SyntaxDocument document;
	protected DocumentHandler documentHandler;

	protected Segment lineSegment;

	protected int selectionStart;
	protected int selectionStartLine;
	protected int selectionEnd;
	protected int selectionEndLine;
	protected boolean biasLeft;
	protected Color currentColor = null;

	protected int bracketPosition;
	protected int bracketLine;

	protected int magicCaret;
	protected boolean overwrite;
	protected boolean rectSelect;
	protected boolean modified;
	
	private int invalidationInterval = 10;
	
	
	/**
	 * Creates a new JEditTextArea with the default settings.
	 */
	public JEditTextArea()
	{
		// Enable the necessary events
		enableEvents(AWTEvent.KEY_EVENT_MASK);

		painter = new TextAreaPainter(this);
		setBackground(Color.WHITE);
		
		documentHandler = new DocumentHandler();
		listeners = new EventListenerList();
		caretEvent = new MutableCaretEvent();
		lineSegment = new Segment();
		bracketLine = bracketPosition = -1;
		blink = true;

		// Initialize the GUI
		setLayout(new ScrollLayout());
		add(CENTER,painter);
		add(RIGHT,vertical = new JScrollBar(JScrollBar.VERTICAL));
		add(BOTTOM,horizontal = new JScrollBar(JScrollBar.HORIZONTAL));

		// Add some event listeners
		vertical.addAdjustmentListener(new AdjustHandler());
		horizontal.addAdjustmentListener(new AdjustHandler());
		painter.addComponentListener(new ComponentHandler());
		painter.addMouseListener(new MouseHandler());
		painter.addMouseMotionListener(new DragHandler());
		this.addMouseWheelListener(this);
		addFocusListener(this);

		// Load the defaults
		setInputHandler(new DefaultInputHandler());
		this.inputHandler.addDefaultKeyBindings();
		setDocument(new SyntaxDocument());
		editable = true;
		
		// Let the focusGained() event display the caret
		caretVisible = false;
		caretBlinks = true;
		
		electricScroll = Settings.getInstance().getElectricScroll();
		autoIndent = true;
		this.setTabSize(Settings.getInstance().getEditorTabWidth());
		this.popup = new TextPopup(this);

		this.addKeyBinding("C+C", this.popup.getCopyAction());
		this.addKeyBinding("C+INSERT", this.popup.getCopyAction());

		this.addKeyBinding("C+V", this.popup.getPasteAction());
		this.addKeyBinding("SHIFT+INSERT", this.popup.getPasteAction());

		this.addKeyBinding("C+X", this.popup.getCutAction());
		this.addKeyBinding("SHIFT+DELETE", this.popup.getCutAction());

		this.addKeyBinding("C+a", this.popup.getSelectAllAction());
		this.invalidationInterval = Settings.getInstance().getIntProperty("workbench.editor.update.lineinterval", 10);
	}
	
	public int getHScrollBarHeight()
	{
		if (horizontal != null && horizontal.isVisible())
			return (int)horizontal.getPreferredSize().getHeight();
		else
			return 0;
	}
	
	public Point getCursorLocation()
	{
		int line = getCaretLine();
		int pos = getCaretPosition() - getLineStartOffset(line);
		FontMetrics fm = painter.getFontMetrics();
		int y = (line - firstLine + 1) * fm.getHeight();
		if (y <= 0) y = 0;
		y += 4;
		int x = offsetToX(line, pos);
		if (x < 0) x = 0;
		x += this.getPainter().getGutterWidth();
		return new Point(x,y);
	}
	
	public void setShowLineNumbers(boolean aFlag)
	{
		this.painter.setShowLineNumbers(aFlag);
	}

	public boolean getShowLineNumbers()
	{
		return this.painter.getShowLineNumbers();
	}

	private String fixLinefeed(String input)
	{
		return StringUtil.makePlainLinefeed(input);
	}
	
	private void changeCase(boolean toLower)
	{
		String sel = this.getSelectedText();
		if (sel == null || sel.length() == 0) return;
		int start = this.getSelectionStart();
		int end = this.getSelectionEnd();
		if (toLower)
			sel = sel.toLowerCase();
		else
			sel = sel.toUpperCase();
		this.setSelectedText(sel);
		this.select(start, end);
	}

	public String getCommentChar()
	{
		return this.commentChar;
	}
	
	public void toLowerCase()
	{
		this.changeCase(true);
	}

	public void toUpperCase()
	{
		this.changeCase(false);
	}
		
	public void matchBracket()
	{
		try
		{
			int bracket = getBracketPosition();
			int line = getBracketLine();
			int caret = getLineStartOffset(line) + bracket;
			if (caret > -1)
			{
				setCaretPosition(caret);
			}
		}
		catch (Exception e)
		{
			// ignore
		}
	}

	public void addKeyBinding(String aBinding, ActionListener aListener)
	{
		this.getInputHandler().addKeyBinding(aBinding, aListener);
	}

	public void addKeyBinding(WbAction anAction)
	{
		KeyStroke key = anAction.getAccelerator();
		if (key != null)
		{
			this.getInputHandler().addKeyBinding(key, anAction);
		}
	}

	public void removeKeyBinding(KeyStroke key)
	{
		this.getInputHandler().removeKeyBinding(key);
	}
	
	@SuppressWarnings("deprecation")
	public final boolean isManagingFocus()
	{
		return true;
	}

	/**
	 * Returns the object responsible for painting this text area.
	 */
	public final TextAreaPainter getPainter()
	{
		return painter;
	}

	/**
	 * Returns the input handler.
	 */
	public final InputHandler getInputHandler()
	{
		return inputHandler;
	}

	/**
	 * Sets the input handler.
	 * @param inputHandler The new input handler
	 */
	public void setInputHandler(InputHandler inputHandler)
	{
		this.inputHandler = inputHandler;
	}

	/**
	 * Returns true if the caret is blinking, false otherwise.
	 */
	public final boolean isCaretBlinkEnabled()
	{
		return caretBlinks;
	}

	protected void stopBlinkTimer()
	{
		if (this.caretTimer != null)
		{
			caretTimer.stop();
		}
		caretTimer = null;
	}
	
	private void startBlinkTimer()
	{
		if (caretTimer != null) return;
		
		final int blinkInterval = 750;
		caretTimer = new Timer(blinkInterval,
		 new ActionListener()
			{
				public void actionPerformed(ActionEvent evt)
				{
					blinkCaret();
				}
			});
		caretTimer.setInitialDelay(blinkInterval);
		caretTimer.start();
	}
	
	/**
	 * Toggles caret blinking.
	 * @param caretBlinks True if the caret should blink, false otherwise
	 */
	public void setCaretBlinkEnabled(boolean caretBlinks)
	{
		this.caretBlinks = caretBlinks;
		if(!caretBlinks)
		{
			blink = false;
		}
		if (caretBlinks)
		{
			startBlinkTimer();
		}
		else
		{
			stopBlinkTimer();
		}
		painter.invalidateSelectedLines();
	}

	/**
	 * Returns true if the caret is visible, false otherwise.
	 */
	public final boolean isCaretVisible()
	{
		return (!caretBlinks || blink) && caretVisible;
	}

	public boolean isTextSelected()
	{
		int start = this.getSelectionStart();
		int end = this.getSelectionEnd();
		return (start < end);
	}
	
	/**
	 * Sets if the caret should be visible.
	 * @param caretVisible True if the caret should be visible, false
	 * otherwise
	 */
	public void setCaretVisible(boolean caretVisible)
	{
		this.caretVisible = caretVisible;
		blink = true;
		painter.invalidateSelectedLines();
	}

	public void focusGained(FocusEvent e)
	{
		setCaretVisible(true);
	}

	public void focusLost(FocusEvent e)
	{
		setCaretVisible(false);
	}
	
	/**
	 * Blinks the caret.
	 */
	public final void blinkCaret()
	{
		if (!caretVisible) return;
		
		if (caretBlinks)
		{
			blink = !blink;
			painter.invalidateSelectedLines();
		}
		else
		{
			blink = true;
		}
	}

	/**
	 * Returns the number of lines from the top and button of the
	 * text area that are always visible.
	 */
	public final int getElectricScroll()
	{
		return electricScroll;
	}

	/**
	 * Sets the number of lines from the top and bottom of the text
	 * area that are always visible
	 * @param electricScroll The number of lines always visible from
	 * the top or bottom
	 */
	public final void setElectricScroll(int electricScroll)
	{
		this.electricScroll = electricScroll;
	}

	/**
	 * Updates the state of the scroll bars. This should be called
	 * if the number of lines in the document changes, or when the
	 * size of the text are changes.
	 */
	public void updateScrollBars()
	{
		if (vertical != null && visibleLines != 0)
		{
			vertical.setValues(firstLine, visibleLines, 0, getLineCount());
			vertical.setUnitIncrement(2);
			vertical.setBlockIncrement(visibleLines);
			if (visibleLines > getLineCount())
			{
				setFirstLine(0);
			}
		}

		int charWidth = painter.getFontMetrics().charWidth('w');
		int maxLineLength = getDocument().getMaxLineLength();
		int maxLineWidth = charWidth * (maxLineLength + 2);
		int width = painter.getWidth();
		if (horizontal != null && width != 0)
		{
			horizontal.setValues(-horizontalOffset, width, 0, maxLineWidth);
			horizontal.setUnitIncrement(charWidth);
			horizontal.setBlockIncrement(width / 3);
		}
	}

	/**
	 * Returns the line displayed at the text area's origin.
	 */
	public final int getFirstLine()
	{
		return (firstLine < 0 ? 0 : firstLine);
	}

	/**
	 * Sets the line displayed at the text area's origin without
	 * updating the scroll bars.
	 */
	public void setFirstLine(int firstLine)
	{
		if (firstLine == this.firstLine) return;
		this.firstLine = firstLine;
		
		if (firstLine != vertical.getValue())
		{
			updateScrollBars();
		}
		
		painter.repaint();
	}

	/**
	 * Returns the number of lines visible in this text area.
	 */
	public final int getVisibleLines()
	{
		return visibleLines;
	}

	/**
	 * Recalculates the number of visible lines. This should not
	 * be called directly.
	 */
	final void recalculateVisibleLines()
	{
		if (painter == null)
		{
			return;
		}
		int height = painter.getHeight();
		int lineHeight = painter.getFontMetrics().getHeight();
		visibleLines = height / lineHeight;
		updateScrollBars();
	}

	/**
	 * Returns the horizontal offset of drawn lines.
	 */
	public final int getHorizontalOffset()
	{
		return horizontalOffset;
	}

	/**
	 * Sets the horizontal offset of drawn lines. This can be used to
	 * implement horizontal scrolling.
	 * @param horizontalOffset offset The new horizontal offset
	 */
	public void setHorizontalOffset(int horizontalOffset)
	{
		if (horizontalOffset == this.horizontalOffset) return;
		this.horizontalOffset = horizontalOffset;
		if (horizontal != null && horizontalOffset != horizontal.getValue())	updateScrollBars();
		painter.repaint();
	}

	/**
	 * A fast way of changing both the first line and horizontal
	 * offset.
	 * @param firstLine The new first line
	 * @param horizontalOffset The new horizontal offset
	 * @return True if any of the values were changed, false otherwise
	 */
	public boolean setOrigin(int firstLine, int horizontalOffset)
	{
		boolean changed = false;

		if(horizontalOffset != this.horizontalOffset)
		{
			this.horizontalOffset = horizontalOffset;
			changed = true;
		}

		if(firstLine != this.firstLine)
		{
			this.firstLine = firstLine;
			changed = true;
		}

		if(changed)
		{
			updateScrollBars();
			painter.repaint();
		}

		return changed;
	}

	/**
	 * Ensures that the caret is visible by scrolling the text area if
	 * necessary.
	 * @return True if scrolling was actually performed, false if the
	 * caret was already visible
	 */
	public boolean scrollToCaret()
	{
		int line = getCaretLine();
		int lineStart = getLineStartOffset(line);
		int offset = Math.max(0,Math.min(getLineLength(line) - 1, getCaretPosition() - lineStart));

		return scrollTo(line, offset);
	}

	/**
	 * Ensures that the specified line and offset is visible by scrolling
	 * the text area if necessary.
	 * @param line The line to scroll to
	 * @param offset The offset in the line to scroll to
	 * @return True if scrolling was actually performed, false if the
	 * line and offset was already visible
	 */
	public boolean scrollTo(final int line, final int offset)
	{
		if (visibleLines == 0)
		{
			// visibleLines == 0 before the component is realized
			// we can't do any proper scrolling, so we'll try again later
			EventQueue.invokeLater(new Runnable()
			{
				public void run()
				{
					scrollTo(line, offset);
				}
			});
			return false;
		}
		
		int newFirstLine = firstLine;
		int newHorizontalOffset = horizontalOffset;
		int lineCount = getLineCount();
		
		if (line < firstLine + electricScroll)
		{
			newFirstLine = Math.max(0, line - electricScroll);
		}
		else if (line + electricScroll >= firstLine + visibleLines)
		{
			newFirstLine = (line - visibleLines) + electricScroll + 1;

			if (newFirstLine + visibleLines >= lineCount)
			{
				newFirstLine = lineCount - visibleLines;
			}
			if (newFirstLine < 0)
			{
				newFirstLine = 0;
			}
		}
		
		int x = _offsetToX(line, offset);
		int width = painter.getFontMetrics().charWidth('w');
		int pwidth = painter.getWidth();
		
		if (x < 0)
		{
			newHorizontalOffset = Math.min(0, horizontalOffset - x + width + 5);
		}
		else if (x + width >= pwidth)
		{
			newHorizontalOffset = horizontalOffset + (pwidth - x) - width - 5;
			if (this.painter.getShowLineNumbers())
			{
				newHorizontalOffset -= painter.getGutterWidth();
			}
		}

		return setOrigin(newFirstLine, newHorizontalOffset);
	}

	/**
	 * Converts a line index to a y co-ordinate.
	 * @param line The line
	 */
	public int lineToY(int line)
	{
		FontMetrics fm = painter.getFontMetrics();
		return (line - firstLine) * fm.getHeight() - (fm.getLeading() + fm.getMaxDescent());
	}

	/**
	 * Converts a y co-ordinate to a line index.
	 * @param y The y co-ordinate
	 */
	public int yToLine(int y)
	{
		FontMetrics fm = painter.getFontMetrics();
		int height = fm.getHeight();
		return Math.max(0,Math.min(getLineCount() - 1, y / height + firstLine));
	}

	/**
	 * Converts an offset in a line into an x co-ordinate. This is a
	 * slow version that can be used any time.
	 * @param line The line
	 * @param offset The offset, from the start of the line
	 */
	public final int offsetToX(int line, int offset)
	{
		// don't use cached tokens
		painter.currentLineTokens = null;
		return _offsetToX(line,offset);
	}

	/**
	 * Converts an offset in a line into an x co-ordinate. This is a
	 * fast version that should only be used if no changes were made
	 * to the text since the last repaint.
	 * @param line The line
	 * @param offset The offset, from the start of the line
	 */
	public int _offsetToX(int line, int offset)
	{
		TokenMarker tokenMarker = getTokenMarker();

		/* Use painter's cached info for speed */
		FontMetrics fm = painter.getFontMetrics();

		getLineText(line, lineSegment);

		int segmentOffset = lineSegment.offset;
		int x = horizontalOffset;

		/* If syntax coloring is disabled, do simple translation */
		if (tokenMarker == null)
		{
			lineSegment.count = offset;
			return x + Utilities.getTabbedTextWidth(lineSegment, fm, x, painter, 0);
		}
		else
		{
			// If syntax coloring is enabled, we have to do this because
			// tokens can vary in width
			Token tokens = tokenMarker.markTokens(lineSegment, line);
			
			Font defaultFont = painter.getFont();
			SyntaxStyle[] styles = painter.getStyles();

			while (tokens != null)
			{
				byte id = tokens.id;

				if (id == Token.NULL)
				{
					fm = painter.getFontMetrics();
				}
				else
				{
					fm = styles[id].getFontMetrics(defaultFont);
				}
				int length = tokens.length;

				if (offset + segmentOffset < lineSegment.offset + length)
				{
					lineSegment.count = offset - (lineSegment.offset - segmentOffset);
					return x + Utilities.getTabbedTextWidth(lineSegment, fm, x, painter, 0);
				}
				else
				{
					lineSegment.count = length;
					x += Utilities.getTabbedTextWidth(lineSegment, fm, x, painter, 0);
					lineSegment.offset += length;
				}
				tokens = tokens.next;
			}
		}
		return x;
	}

	/**
	 * Converts an x co-ordinate to an offset within a line.
	 * @param line The line
	 * @param x The x co-ordinate
	 */
	public int xToOffset(int line, int x)
	{
		TokenMarker tokenMarker = getTokenMarker();

		/* Use painter's cached info for speed */
		FontMetrics fm = painter.getFontMetrics();

		getLineText(line,lineSegment);

		char[] segmentArray = lineSegment.array;
		int segmentOffset = lineSegment.offset;
		int segmentCount = lineSegment.count;

		int width = horizontalOffset;

		if(tokenMarker == null)
		{
			for (int i = 0; i < segmentCount; i++)
			{
				char c = segmentArray[i + segmentOffset];
				int charWidth;

				if (c == '\t')
				{
					charWidth = (int) painter.nextTabStop(width,i) - width;
				}
				else
				{
					charWidth = fm.charWidth(c);
				}
				if (x - charWidth / 2 <= width)
				{
					return i;
				}
				width += charWidth;
			}

			return segmentCount;
		}
		else
		{
			Token tokens = tokenMarker.markTokens(lineSegment, line);
			
			int offset = 0;
			Font defaultFont = painter.getFont();
			SyntaxStyle[] styles = painter.getStyles();

			while (tokens != null)
			{
				byte id = tokens.id;
				
				if (id == Token.NULL)
				{
					fm = painter.getFontMetrics();
				}
				else
				{
					fm = styles[id].getFontMetrics(defaultFont);
				}

				int length = tokens.length;

				for (int i = 0; i < length; i++)
				{
					char c = segmentArray[segmentOffset + offset + i];
					int charWidth = fm.charWidth(c);
					
					if (c == '\t')
					{
						charWidth = (int)painter.nextTabStop(width, offset + i) - width;
					}

					if (x - charWidth / 2 <= width) return offset + i;

					width += charWidth;
				}

				offset += length;
				tokens = tokens.next;
			}
			return offset;
		}
		
	}

	/**
	 * Converts a point to an offset, from the start of the text.
	 * @param x The x co-ordinate of the point
	 * @param y The y co-ordinate of the point
	 */
	public int xyToOffset(int x, int y)
	{
		int line = yToLine(y);
		int start = getLineStartOffset(line);
		return start + xToOffset(line,x);
	}

	/**
	 * Returns the document this text area is editing.
	 */
	public final SyntaxDocument getDocument()
	{
		return document;
	}

	/**
	 * Sets the document this text area is editing.
	 * @param document The document
	 */
	public void setDocument(SyntaxDocument document)
	{
		if(this.document == document)	return;

		if(this.document != null)
		{
			this.document.removeDocumentListener(documentHandler);
			this.document.dispose();
		}
		
		this.document = document;

		if(this.document != null)
		{
			painter.calculateTabSize();
			
			if (this.currentTokenMarker != null)
			{
				this.document.setTokenMarker(this.currentTokenMarker);
			}
			
			this.document.addDocumentListener(documentHandler);
		
			EventQueue.invokeLater(new Runnable()
			{
				public void run()
				{
					painter.invalidateLineRange(0,getLineCount());
					//setCaretPosition(0);
					painter.repaint();
					painter.validate();
					repaint();
					validate();
					updateScrollBars();
				}
			});
		}
	}

	public void setFont(Font aNewFont)
	{
		super.setFont(aNewFont);
		this.painter.setFont(aNewFont);
	}

	/**
	 * Returns the document's token marker. Equivalent to calling
	 * <code>getDocument().getTokenMarker()</code>.
	 */
	public final TokenMarker getTokenMarker()
	{
		return document.getTokenMarker();
	}

	/**
	 * Sets the document's token marker. Equivalent to caling
	 * <code>getDocument().setTokenMarker()</code>.
	 * @param tokenMarker The token marker
	 */
	public final void setTokenMarker(TokenMarker tokenMarker)
	{
		this.currentTokenMarker = tokenMarker;
		document.setTokenMarker(tokenMarker);
	}

	/**
	 * Returns the length of the document. Equivalent to calling
	 * <code>getDocument().getLength()</code>.
	 */
	public final int getDocumentLength()
	{
		return document.getLength();
	}

	/**
	 * Returns the number of lines in the document.
	 */
	public final int getLineCount()
	{
		return document.getDefaultRootElement().getElementCount();
	}

	/**
	 * Returns the line containing the specified offset.
	 * @param offset The offset
	 */
	public final int getLineOfOffset(int offset)
	{
		return document.getDefaultRootElement().getElementIndex(offset);
	}

	public int getCaretPositionInLine(int line)
	{
		int pos = getCaretPosition();
		int start = getLineStartOffset(line);
		return (pos - start);
		
	}
	
	/**
	 * Returns the start offset of the specified line.
	 * @param line The line
	 * @return The start offset of the specified line, or -1 if the line is
	 * invalid
	 */
	public int getLineStartOffset(int line)
	{
		Element lineElement = document.getDefaultRootElement().getElement(line);
		if (lineElement == null)
			return -1;
		else
			return lineElement.getStartOffset();
	}

	/**
	 * Returns the end offset of the specified line.
	 * @param line The line
	 * @return The end offset of the specified line, or -1 if the line is
	 * invalid.
	 */
	public int getLineEndOffset(int line)
	{
		Element lineElement = document.getDefaultRootElement().getElement(line);
		if (lineElement == null)
			return -1;
		else
			return lineElement.getEndOffset();
	}

	/**
	 * Returns the length of the specified line, without the line end terminator 
	 * @param line The line
	 */
	public int getLineLength(int line)
	{
		Element lineElement = document.getDefaultRootElement().getElement(line);
		
		if(lineElement == null)
			return -1;
		else
			return lineElement.getEndOffset() - lineElement.getStartOffset() - 1;
	}

	/**
	 * Returns the entire text of this text area.
	 */
	public String getText()
	{
		int len = document.getLength();
		if (len < 0) return null;
		try
		{
			return document.getText(0,document.getLength());
		}
		catch (BadLocationException bl)
		{
			LogMgr.logError("JEditTextArea.getText()", "Error setting text", bl);
			return null;
		}
	}

	/**
	 *	Set the tab size to be used in characters.
	 */
	public void setTabSize(int aSize)
	{
		document.putProperty(PlainDocument.tabSizeAttribute, new Integer(aSize));
	}


	/**
	 *	Return the current Tab size in characters
	 */
	public int getTabSize()
	{
		Integer tab = (Integer)document.getProperty(PlainDocument.tabSizeAttribute);

		if (tab != null)
		{
			return tab.intValue();
		}
		else
		{
			return 4;
		}

	}

	public void appendLine(String aLine)
	{
		try
		{
			document.beginCompoundEdit();
			document.insertString(document.getLength(),fixLinefeed(aLine),null);
		}
		catch (BadLocationException bl)
		{
			LogMgr.logError("JEditTextArea.appendLine()", "Error setting text", bl);
		}
		finally
		{
			document.endCompoundEdit();
		}
	}
	
	public void reset()
	{
		setText("");
		resetModified();
	}
	
	/**
	 * Sets the entire text of this text area.
	 */
	public void setText(String text)
	{
		try
		{
			document.beginCompoundEdit();

			if (document.getLength() > 0)
			{
				document.remove(0,document.getLength());
			}
			if (text != null && text.length() > 0)
			{
				String realtext = fixLinefeed(text);
				document.insertString(0,realtext,null);
			}
		}
		catch (BadLocationException bl)
		{
			LogMgr.logError("JEditTextArea.setText()", "Error setting text", bl);
		}
		finally
		{
			document.endCompoundEdit();
			document.tokenizeLines();
		}
		
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				updateScrollBars();
				painter.invalidateLineRange(0, getLineCount());
				repaint();
			}
		});
	}

	/**
	 * Returns the specified substring of the document.
	 * @param start The start offset
	 * @param len The length of the substring
	 * @return The substring, or null if the offsets are invalid
	 */
	public final String getText(int start, int len)
	{
		try
		{
			return document.getText(start,len);
		}
		catch (BadLocationException bl)
		{
			LogMgr.logError("JEditTextArea.getText()", "Error setting text", bl);
			return null;
		}
	}

	/**
	 * Copies the specified substring of the document into a segment.
	 * If the offsets are invalid, the segment will contain a null string.
	 * @param start The start offset
	 * @param len The length of the substring
	 * @param segment The segment
	 */
	public final void getText(int start, int len, Segment segment)
	{
		if (len < 0) return;
		try
		{
			document.getText(start, len ,segment);
		}
		catch(BadLocationException bl)
		{
			LogMgr.logError("JEditTextArea.getText()", "Error setting text", bl);
			segment.offset = segment.count = 0;
		}
	}
	
	/**
	 * Returns the word that is left of the cursor.
	 * If the character left of the cursor is a whitespace
	 * this method returns null.
	 * @param wordBoundaries additional word boundary characters (whitespace is always a word boundary)
	 */
	public String getWordAtCursor(String wordBoundaries)
	{
		int currentLine = getCaretLine();
		String line = this.getLineText(currentLine);
		int pos = this.getCaretPositionInLine(currentLine);
		return StringUtil.getWordLeftOfCursor(line, pos, wordBoundaries);
	}

	public void selectWordAtCursor(String wordBoundaries)
	{
		int currentLine = getCaretLine();
		String line = this.getLineText(currentLine);
		int caret = this.getCaretPosition();
		int lineStart = this.getLineStartOffset(currentLine);
		int pos = (caret - lineStart);
		//this.getCaretPositionInLine(currentLine);
		if (pos <= 0) return;
		if (Character.isWhitespace(line.charAt(pos - 1))) return;
		int start = StringUtil.findWordBoundary(line, pos - 1, wordBoundaries);
		if (start > -1) 
		{
			this.select(lineStart + start + 1, caret);
		}
	}
	/**
	 * Returns the text on the specified line without the line terminator
	 * @param lineIndex The line
	 * @return The text, or null if the line is invalid
	 */
	public final String getLineText(int lineIndex)
	{
		Element lineElement = document.getDefaultRootElement().getElement(lineIndex);
		int start = (lineElement != null ? lineElement.getStartOffset() : -1);
		int end = (lineElement != null ? lineElement.getEndOffset() : -1);
		return getText(start, end - start - 1);
	}

	/**
	 * Copies the text on the specified line into a segment. If the line
	 * is invalid, the segment will contain a null string.
	 * @param lineIndex The line
	 */
	public final void getLineText(int lineIndex, Segment segment)
	{
		Element lineElement = document.getDefaultRootElement().getElement(lineIndex);
		int start = (lineElement != null ? lineElement.getStartOffset() : -1);
		int end = (lineElement != null ? lineElement.getEndOffset() : -1);
		getText(start, end - start - 1,segment);
	}

	/**
	 * Returns the selection start offset.
	 */
	public final int getSelectionStart()
	{
		return selectionStart;
	}

	/**
	 * Returns the offset where the selection starts on the specified
	 * line.
	 */
	public int getSelectionStart(int line)
	{
		if (line == selectionStartLine)
		{
			return selectionStart;
		}
		else if(rectSelect)
		{
			Element map = document.getDefaultRootElement();
			int start = selectionStart - map.getElement(selectionStartLine).getStartOffset();

			Element lineElement = map.getElement(line);
			int lineStart = lineElement.getStartOffset();
			int lineEnd = lineElement.getEndOffset() - 1;
			return Math.min(lineEnd,lineStart + start);
		}
		else
		{
			return getLineStartOffset(line);
		}
	}

	/**
	 * Returns the selection start line.
	 */
	public final int getSelectionStartLine()
	{
		return selectionStartLine;
	}

	/**
	 * Sets the selection start. The new selection will be the new
	 * selection start and the old selection end.
	 * @param selectionStart The selection start
	 * @see #select(int,int)
	 */
	public final void setSelectionStart(int selectionStart)
	{
		select(selectionStart,selectionEnd);
	}

	/**
	 * Returns the selection end offset.
	 */
	public final int getSelectionEnd()
	{
		return selectionEnd;
	}

	/**
	 * Returns the offset where the selection ends on the specified
	 * line.
	 */
	public int getSelectionEnd(int line)
	{
		if (line == selectionEndLine)
		{
			return selectionEnd;
		}
		else if (rectSelect)
		{
			Element map = document.getDefaultRootElement();
			int end = selectionEnd - map.getElement(selectionEndLine).getStartOffset();

			Element lineElement = map.getElement(line);
			int lineStart = lineElement.getStartOffset();
			int lineEnd = lineElement.getEndOffset() - 1;
			return Math.min(lineEnd,lineStart + end);
		}
		else
		{
			return getLineEndOffset(line) - 1;
		}
	}

	/**
	 * Returns the selection end line.
	 */
	public final int getSelectionEndLine()
	{
		return selectionEndLine;
	}

	/**
	 * Sets the selection end. The new selection will be the old
	 * selection start and the bew selection end.
	 * @param selectionEnd The selection end
	 * @see #select(int,int)
	 */
	public final void setSelectionEnd(int selectionEnd)
	{
		select(selectionStart,selectionEnd);
	}

	/**
	 * Returns the caret position. This will either be the selection
	 * start or the selection end, depending on which direction the
	 * selection was made in.
	 */
	public final int getCaretPosition()
	{
		return (biasLeft ? selectionStart : selectionEnd);
	}

	/**
	 * Returns the caret line.
	 */
	public final int getCaretLine()
	{
		return (biasLeft ? selectionStartLine : selectionEndLine);
	}

	/**
	 * Returns the mark position. This will be the opposite selection
	 * bound to the caret position.
	 * @see #getCaretPosition()
	 */
	public final int getMarkPosition()
	{
		return (biasLeft ? selectionEnd : selectionStart);
	}

	/**
	 * Returns the mark line.
	 */
	public final int getMarkLine()
	{
		return (biasLeft ? selectionEndLine : selectionStartLine);
	}

	/**
	 * Sets the caret position. The new selection will consist of the
	 * caret position only (hence no text will be selected)
	 * @param caret The caret position
	 * @see #select(int,int)
	 */
	public final void setCaretPosition(int caret)
	{
		select(caret,caret);
	}

	/**
	 * Selects all text in the document.
	 */
	public final void selectAll()
	{
		select(0,getDocumentLength());
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				requestFocusInWindow();
			}
		});
	}

	/**
	 * Moves the mark to the caret position.
	 */
	public final void selectNone()
	{
		select(getCaretPosition(),getCaretPosition());
	}

	public void clearUndoBuffer()
	{
		this.document.clearUndoBuffer();
	}

	public void undo()
	{
		this.document.undo();
		int pos = this.document.getPositionOfLastChange();
		if (pos > -1)
		{
			this.setCaretPosition(pos);
			this.scrollToCaret();
		}
	}

	public void redo()
	{
		this.document.redo();
		int pos = this.document.getPositionOfLastChange();
		if (pos > -1)
		{
			this.setCaretPosition(pos);
			this.scrollToCaret();
		}
	}

	public boolean currentSelectionIsTemporary()
	{
		return currentSelectionIsTemporary;
	}

	public void selectError(int start, int end)
	{
		this.selectCommand(start, end, ERROR_COLOR);
	}

	public void selectStatementTemporary(int start, int end)
	{
		this.selectCommand(start, end, TEMP_COLOR);
	}

	private void selectCommand(int start, int end, Color alternateColor)
	{
		if (start >= end) return;

		int len = (end - start);
		String text = this.getText(start, len);
		if (text == null || text.length() == 0) return;

		len = text.length();
		int pos = 0;
		char c = text.charAt(pos);
		while (Character.isWhitespace(c) && pos < len)
		{
			pos ++;
			c = text.charAt(pos);
		}

		int newStart = start + pos;
		int maxIndex = len - 1;

		pos = 0;
		c = text.charAt(maxIndex - pos);
		while (Character.isWhitespace(c) && pos < maxIndex)
		{
			pos ++;
			c = text.charAt(maxIndex - pos);
		}
		int newEnd = end - pos;
		this.select(newStart, newEnd, alternateColor);
	}

	public void select(int start, int end)
	{
		this.select(start, end, null);
	}

	/**
	 * Selects from the start offset to the end offset. This is the
	 * general selection method used by all other selecting methods.
	 * The caret position will be start if start &lt; end, and end
	 * if end &gt; start.
	 * @param start The start offset
	 * @param end The end offset
	 */
	private void select(int start, int end, Color alternateColor)
	{
		int newStart, newEnd;
		boolean newBias;

		if (start <= end)
		{
			newStart = start;
			newEnd = end;
			newBias = false;
		}
		else
		{
			newStart = end;
			newEnd = start;
			newBias = true;
		}

		if(newStart < 0 || newEnd > getDocumentLength())
		{
			throw new IllegalArgumentException("Bounds out of"+ " range: " + newStart + "," +	newEnd);
		}


		// If the new position is the same as the old, we don't
		// do all this crap, however we still do the stuff at
		// the end (clearing magic position, scrolling)
		if (newStart != selectionStart || newEnd != selectionEnd || newBias != biasLeft)
		{
			this.alternateSelectionColor = alternateColor;
			this.currentSelectionIsTemporary = (alternateColor != null);

			int newStartLine = getLineOfOffset(newStart);
			int newEndLine = getLineOfOffset(newEnd);

			if(painter.isBracketHighlightEnabled())
			{
				if(bracketLine != -1)	painter.invalidateLine(bracketLine);
				updateBracketHighlight(end);
				if(bracketLine != -1) painter.invalidateLine(bracketLine);
			}

			painter.invalidateLineRange(selectionStartLine,selectionEndLine);
			painter.invalidateLineRange(newStartLine,newEndLine);

			document.addUndoableEdit(new CaretUndo(selectionStart,selectionEnd));

			selectionStart = newStart;
			selectionEnd = newEnd;
			selectionStartLine = newStartLine;
			selectionEndLine = newEndLine;
			biasLeft = newBias;

			fireCaretEvent();
		}

		// When the user is typing, etc, we don't want the caret
		// to blink
		blink = true;
		if (caretTimer != null) caretTimer.restart();

		// Disable rectangle select if selection start = selection end
		if(selectionStart == selectionEnd) rectSelect = false;

		// Clear the `magic' caret position used by up/down
		magicCaret = -1;

		scrollToCaret();
		fireSelectionEvent();
	}

	public Color getAlternateSelectionColor()
	{
		return this.alternateSelectionColor;
	}

	/**
	 * Returns the selected text, or null if no selection is active.
	 */
	public final String getSelectedText()
	{
		if (selectionStart == selectionEnd) return null;

		if (rectSelect)
		{
			// Return each row of the selection on a new line
			Element map = document.getDefaultRootElement();

			int start = selectionStart - map.getElement(selectionStartLine).getStartOffset();
			int end = selectionEnd - map.getElement(selectionEndLine).getStartOffset();

			// Certain rectangles satisfy this condition...
			if(end < start)
			{
				int tmp = end;
				end = start;
				start = tmp;
			}

			StringBuilder buf = new StringBuilder();
			Segment seg = new Segment();

			for(int i = selectionStartLine; i <= selectionEndLine; i++)
			{
				Element lineElement = map.getElement(i);
				int lineStart = lineElement.getStartOffset();
				int lineEnd = lineElement.getEndOffset() - 1;
				int lineLen = lineEnd - lineStart;

				lineStart = Math.min(lineStart + start,lineEnd);
				lineLen = Math.min(end - start,lineEnd - lineStart);

				getText(lineStart,lineLen,seg);
				buf.append(seg.array,seg.offset,seg.count);

				if (i != selectionEndLine) buf.append('\n');
			}
			return buf.toString();
		}
		else
		{
			return getText(selectionStart,selectionEnd - selectionStart);
		}

	}

	/**
	 * Replaces the selection with the specified text.
	 * @param selectedText The replacement text for the selection
	 */
	public void setSelectedText(String selectedText)
	{
		if(!editable) return;

		try
		{
			selectedText = fixLinefeed(selectedText);
			document.beginCompoundEdit();

			if (rectSelect)
			{
				Element map = document.getDefaultRootElement();

				int start = selectionStart - map.getElement(selectionStartLine).getStartOffset();
				int end = selectionEnd - map.getElement(selectionEndLine).getStartOffset();

				// Certain rectangles satisfy this condition...
				if (end < start)
				{
					int tmp = end;
					end = start;
					start = tmp;
				}

				int lastNewline = 0;
				int currNewline = 0;

				for (int i = selectionStartLine; i <= selectionEndLine; i++)
				{
					Element lineElement = map.getElement(i);
					int lineStart = lineElement.getStartOffset();
					int lineEnd = lineElement.getEndOffset() - 1;
					int rectStart = Math.min(lineEnd,lineStart + start);

					document.remove(rectStart,Math.min(lineEnd - rectStart,end - start));

					if (selectedText == null) continue;

					currNewline = selectedText.indexOf("\n",lastNewline);
					if (currNewline == -1)
					{
						currNewline = selectedText.length();
					}
					document.insertString(rectStart,selectedText.substring(lastNewline,currNewline),null);

					lastNewline = Math.min(selectedText.length(),currNewline + 1);
				}

				if (selectedText != null && currNewline != selectedText.length())
				{
					int offset = map.getElement(selectionEndLine).getEndOffset() - 1;
					document.insertString(offset,"\n",null);
					document.insertString(offset + 1,selectedText.substring(currNewline + 1),null);
				}
			}
			else
			{
				document.remove(selectionStart,selectionEnd - selectionStart);

				if(selectedText != null)
				{
					document.insertString(selectionStart,selectedText,null);
				}
				if (this.autoIndent)
				{
					int c = this.getCaretLine();
					if (c > 0 && selectedText.equals("\n"))
					{
						String s = this.getLineText(c - 1);
						String p = StringUtil.getStartingWhiteSpace(s);
						if (p!= null && p.length() > 0)
						{
							document.insertString(selectionEnd, p, null);
						}
					}
				}
			}
		}
		catch (BadLocationException bl)
		{
			LogMgr.logError("JEditTextArea.setSelectedText()", "Error setting text", bl);
			throw new InternalError("Cannot replace selection");
		}
		finally
		{
			document.endCompoundEdit();
		}
		
		updateScrollBars();
		setCaretPosition(selectionEnd);
//		this.invalidate();
		this.repaint();
	}

	public void insertText(String text)
	{
		insertText(getCaretPosition(), text);
	}
	
	public void insertText(int position, String text)
	{
		try
		{
			document.beginCompoundEdit();
			document.insertString(position, fixLinefeed(text), null);
		}
		catch (Exception e)
		{
			LogMgr.logError("JEditTextArea.insertText()", "Error setting text", e);
		}
		finally
		{
			document.endCompoundEdit();
		}
	}
	
	public void setAutoIndent(boolean aFlag)  { this.autoIndent = aFlag; }
	public boolean getAutoIndent() 	{ return this.autoIndent; }

	/**
	 * Returns true if this text area is editable, false otherwise.
	 */
	public final boolean isEditable()
	{
		return editable;
	}

	/**
	 * Sets if this component is editable.
	 * @param editable True if this text area should be editable,
	 * false otherwise
	 */
	public void setEditable(boolean editable)
	{
		this.editable = editable;
		if (this.popup != null)
		{
			this.popup.getCutAction().setEnabled(editable);
			this.popup.getClearAction().setEnabled(editable);
			this.popup.getPasteAction().setEnabled(editable);
		}
	}

	/**
	 * Returns the right click popup menu.
	 */
	public final JPopupMenu getRightClickPopup()
	{
		return popup;
	}

	/**
	 * Sets the right click popup menu.
	 * @param popup The popup
	 */
	public final void setRightClickPopup(TextPopup popup)
	{
		this.popup = popup;
	}

	/**
	 * Returns the `magic' caret position. This can be used to preserve
	 * the column position when moving up and down lines.
	 */
	public final int getMagicCaretPosition()
	{
		return magicCaret;
	}

	/**
	 * Sets the `magic' caret position. This can be used to preserve
	 * the column position when moving up and down lines.
	 * @param magicCaret The magic caret position
	 */
	public final void setMagicCaretPosition(int magicCaret)
	{
		this.magicCaret = magicCaret;
	}

	/**
	 * Similar to <code>setSelectedText()</code>, but overstrikes the
	 * appropriate number of characters if overwrite mode is enabled.
	 * @param str The string
	 * @see #setSelectedText(String)
	 * @see #isOverwriteEnabled()
	 */
	public void overwriteSetSelectedText(String str)
	{
		// Don't overstrike if there is a selection
		if(!overwrite || selectionStart != selectionEnd)
		{
			setSelectedText(str);
			return;
		}

		// Don't overstrike if we're on the end of the line
		int caret = getCaretPosition();
		int caretLineEnd = getLineEndOffset(getCaretLine());
		if(caretLineEnd - caret <= str.length())
		{
			setSelectedText(str);
			return;
		}

		document.beginCompoundEdit();

		try
		{
			str = fixLinefeed(str);
			document.remove(caret,str.length());
			document.insertString(caret,str,null);
		}
		catch (BadLocationException bl)
		{
			LogMgr.logError("JEditTextArea.overwriteSelectedText()", "Error setting text", bl);
		}
		finally
		{
			document.endCompoundEdit();
		}
		updateScrollBars();
	}

	/**
	 * Returns true if overwrite mode is enabled, false otherwise.
	 */
	public final boolean isOverwriteEnabled()
	{
		return overwrite;
	}

	/**
	 * Sets if overwrite mode should be enabled.
	 * @param overwrite True if overwrite mode should be enabled,
	 * false otherwise.
	 */
	public final void setOverwriteEnabled(boolean overwrite)
	{
		this.overwrite = overwrite;
		painter.invalidateSelectedLines();
	}

	/**
	 * Returns true if the selection is rectangular, false otherwise.
	 */
	public final boolean isSelectionRectangular()
	{
		return rectSelect;
	}

	/**
	 * Sets if the selection should be rectangular.
	 * @param rectSelect True if the selection should be rectangular,
	 * false otherwise.
	 */
	public final void setSelectionRectangular(boolean rectSelect)
	{
		this.rectSelect = rectSelect;
		painter.invalidateSelectedLines();
	}

	/**
	 * Returns the position of the highlighted bracket (the bracket
	 * matching the one before the caret)
	 */
	public final int getBracketPosition()
	{
		return bracketPosition;
	}

	/**
	 * Returns the line of the highlighted bracket (the bracket
	 * matching the one before the caret)
	 */
	public final int getBracketLine()
	{
		return bracketLine;
	}

	public final void addSelectionListener(TextSelectionListener l)
	{
		listeners.add(TextSelectionListener.class, l);
	}

	public final void removeSelectionListener(TextSelectionListener l)
	{
		listeners.remove(TextSelectionListener.class, l);
	}

	public final void addTextChangeListener(TextChangeListener l)
	{
		listeners.add(TextChangeListener.class, l);
	}

	public final void removeTextChangeListener(TextChangeListener l)
	{
		listeners.remove(TextChangeListener.class, l);
	}

	/**
	 * Adds a caret change listener to this text area.
	 * @param listener The listener
	 */
	public final void addCaretListener(CaretListener listener)
	{
		listeners.add(CaretListener.class,listener);
	}

	/**
	 * Removes a caret change listener from this text area.
	 * @param listener The listener
	 */
	public final void removeCaretListener(CaretListener listener)
	{
		listeners.remove(CaretListener.class,listener);
	}

	/**
	 * Deletes the selected text from the text area and places it
	 * into the clipboard.
	 */
	public void cut()
	{
		if(editable)
		{
			copy();
			setSelectedText("");
		}
	}

	/**
	 *	Deletes the selected text from the text area
	 **/
	public void clear()
	{
		if (editable)
		{
			setSelectedText("");
		}
	}

	/**
	 * Places the selected text into the clipboard.
	 */
	public void copy()
	{
		if(selectionStart != selectionEnd)
		{
			Clipboard clipboard = getToolkit().getSystemClipboard();

			String selection = getSelectedText();

			clipboard.setContents(new StringSelection(selection),null);
		}
	}

	/**
	 * Inserts the clipboard contents into the text.
	 */
	public void paste()
	{
		if(editable)
		{
			Clipboard clipboard = getToolkit().getSystemClipboard();
			try
			{
				String selection = ((String)clipboard.getContents(this).getTransferData(DataFlavor.stringFlavor));
				setSelectedText(selection);
			}
			catch(Exception e)
			{
				getToolkit().beep();
				System.err.println("Clipboard does not contain a string");
			}
		}
	}

	public void setKeyEventInterceptor(KeyListener c)
	{
		this.keyEventInterceptor = c;
	}
	
	public void removeKeyEventInterceptor()
	{
		this.keyEventInterceptor = null;
	}
	
	private void forwardKeyEvent(KeyEvent evt)
	{
		switch(evt.getID())
		{
			case KeyEvent.KEY_TYPED:
				keyEventInterceptor.keyTyped(evt);
				break;
			case KeyEvent.KEY_PRESSED:
				keyEventInterceptor.keyPressed(evt);
				break;
			case KeyEvent.KEY_RELEASED:
				keyEventInterceptor.keyReleased(evt);
			break;
		}
	}
	
	/**
	 * Forwards key events directly to the input handler.
	 * This is slightly faster than using a KeyListener
	 * because some Swing overhead is avoided.
	 */
	public void processKeyEvent(KeyEvent evt)
	{
		if(inputHandler == null)
			return;
		if (keyEventInterceptor != null)
		{
			forwardKeyEvent(evt);
			return;
		}
		
		int oldcount = NumberStringCache.getNumberString(this.getLineCount()).length();
		switch(evt.getID())
		{
			case KeyEvent.KEY_TYPED:
				inputHandler.keyTyped(evt);
				break;
			case KeyEvent.KEY_PRESSED:
				inputHandler.keyPressed(evt);
				break;
			case KeyEvent.KEY_RELEASED:
				inputHandler.keyReleased(evt);
				break;
		}
		if (!evt.isConsumed())
		{
			super.processKeyEvent(evt);
		}
		int newcount = NumberStringCache.getNumberString(this.getLineCount()).length();
		boolean changed = false;

		if(this.getFirstLine() < 0)
		{
			updateScrollBars();
			changed = true;
		}

		changed = changed || (oldcount != newcount);

		if (changed)
		{
			this.invalidate();
			this.repaint();
		}
	}

	protected void fireTextStatusChanged(boolean isModified)
	{
		Object[] list = listeners.getListenerList();
		for(int i = list.length - 2; i >= 0; i--)
		{
			if(list[i] == TextChangeListener.class)
			{
				((TextChangeListener)list[i+1]).textStatusChanged(isModified);
			}
		}
	}

	protected void fireSelectionEvent()
	{
		Object[] list = listeners.getListenerList();
		for(int i = list.length - 2; i >= 0; i--)
		{
			if(list[i] == TextSelectionListener.class)
			{
				((TextSelectionListener)list[i+1]).selectionChanged(this.getSelectionStart(), this.getSelectionEnd());
			}
		}
	}

	public void setStatusBar(EditorStatusbar bar)
	{
		this.statusBar = bar;
		updateStatusBar();
	}
	
	private void updateStatusBar()
	{
		if (this.statusBar != null)
		{
			int line = this.getCaretLine();
			this.statusBar.setEditorLocation(line + 1, this.getCaretPositionInLine(line) + 1);
		}
	}
	
	protected void fireCaretEvent()
	{
		Object[] list = listeners.getListenerList();
		for(int i = list.length - 2; i >= 0; i--)
		{
			if(list[i] == CaretListener.class)
			{
				((CaretListener)list[i+1]).caretUpdate(caretEvent);
			}
		}
		updateStatusBar();
	}
	
	protected void updateBracketHighlight(int newCaretPosition)
	{
		if(newCaretPosition == 0)
		{
			bracketPosition = bracketLine = -1;
			return;
		}

		try
		{
			int offset = TextUtilities.findMatchingBracket(document,newCaretPosition - 1);
			if(offset != -1)
			{
				bracketLine = getLineOfOffset(offset);
				bracketPosition = offset - getLineStartOffset(bracketLine);
				return;
			}
		}
		catch (BadLocationException bl)
		{
			LogMgr.logError("JEditTextArea.updateBracketHighlight()", "Error setting text", bl);
		}

		bracketLine = bracketPosition = -1;
	}

	protected void documentChanged(DocumentEvent evt)
	{
		DocumentEvent.ElementChange ch = evt.getChange(document.getDefaultRootElement());

		int count;
		if (ch == null)
		{
			count = 0;
		}
		else
		{
			count = ch.getChildrenAdded().length - ch.getChildrenRemoved().length;
		}
		
		int line = getLineOfOffset(evt.getOffset());
		invalidateLines(line);
		
		if (count == 0)
		{
			painter.invalidateLine(line);
		}
		else
		{
			painter.invalidateLineRange(line,(firstLine < 0 ? 0 : firstLine) + visibleLines);
		}
		
		boolean wasModified = this.modified;
		this.modified = true;
		
		// only fire event if modified status is changed
		if (!wasModified)
		{
			this.fireTextStatusChanged(true);
		}
		updateScrollBars();
	}
	
	private void invalidateLines(int changedLine)
	{
		TokenMarker marker = getTokenMarker();
		if (marker == null) return;
		
		// In order to display multi-line literals correctly
		// I simply invalidate some line above and below the
		// currently changed line. This still can leave
		// incorrect tokens with regards to multiline literals
		// but my assumptioin is, that literals spanning more than 
		// 'delta' number lines are used very rarely in SQL scripts. 
		
		// Testing for possible literals in those lines and then only 
		// invalidating the lines that need it, is probably 
		// not much faster then simply invalidating a constant range of lines
		int startInvalid = changedLine - this.invalidationInterval;
		int endInvalid = changedLine + this.invalidationInterval;
		
		if (startInvalid < 0) startInvalid = 0;
		if (endInvalid > marker.getLineCount()) endInvalid = marker.getLineCount() - 1;

		// re-tokenize all lines
		document.tokenizeLines(startInvalid, endInvalid);
			
		// re-paint the lines that need it
		int repaintStart = (startInvalid < getFirstLine() ? getFirstLine() : startInvalid);
		int repaintEnd = (endInvalid > (repaintStart + getVisibleLines()) ? repaintStart + getVisibleLines() : endInvalid);
		painter.invalidateLineRange(repaintStart, repaintEnd);
	}
	
	
	public boolean isModified() { return this.modified; }
	public void resetModified()
	{
		boolean wasModified = this.modified;
		this.modified = false;
		if (wasModified)
		{
			this.fireTextStatusChanged(false);
		}
	}

	/** Invoked when the mouse wheel is rotated.
	 * @see MouseWheelEvent
	 *
	 */
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL)
		{
			int totalScrollAmount = e.getUnitsToScroll() * vertical.getUnitIncrement();
			vertical.setValue(vertical.getValue() + totalScrollAmount);
		}
	}

	class ScrollLayout implements LayoutManager
	{
		public void addLayoutComponent(String name, Component comp)
		{
			if(name.equals(CENTER))
				center = comp;
			else if(name.equals(RIGHT))
				right = comp;
			else if(name.equals(BOTTOM))
				bottom = comp;
		}

		public void removeLayoutComponent(Component comp)
		{
			if(center == comp)
				center = null;
			if(right == comp)
				right = null;
			if(bottom == comp)
				bottom = null;
			else
				leftOfScrollBar.removeElement(comp);
		}

		public Dimension preferredLayoutSize(Container parent)
		{
			Dimension dim = new Dimension();
			Insets insets = getInsets();
			dim.width = insets.left + insets.right;
			dim.height = insets.top + insets.bottom;

			Dimension centerPref = center.getPreferredSize();
			dim.width += centerPref.width;
			dim.height += centerPref.height;
			Dimension rightPref = right.getPreferredSize();
			dim.width += rightPref.width;
			Dimension bottomPref = bottom.getPreferredSize();
			dim.height += bottomPref.height;

			return dim;
		}

		public Dimension minimumLayoutSize(Container parent)
		{
			Dimension dim = new Dimension();
			Insets insets = getInsets();
			dim.width = insets.left + insets.right;
			dim.height = insets.top + insets.bottom;

			Dimension centerPref = center.getMinimumSize();
			dim.width += centerPref.width;
			dim.height += centerPref.height;
			Dimension rightPref = right.getMinimumSize();
			dim.width += rightPref.width;
			Dimension bottomPref = bottom.getMinimumSize();
			dim.height += bottomPref.height;

			return dim;
		}

		public void layoutContainer(Container parent)
		{
			Dimension size = parent.getSize();
			Insets insets = parent.getInsets();
			int itop = insets.top;
			int ileft = insets.left;
			int ibottom = insets.bottom;
			int iright = insets.right;

			int rightWidth = right.getPreferredSize().width;
			int bottomHeight = bottom.getPreferredSize().height;
			int centerWidth = size.width - rightWidth - ileft - iright;
			int centerHeight = size.height - bottomHeight - itop - ibottom;

			center.setBounds(
				ileft,
				itop,
				centerWidth,
				centerHeight);

			right.setBounds(
				ileft + centerWidth,
				itop,
				rightWidth,
				centerHeight);

			// Lay out all status components, in order
			Enumeration status = leftOfScrollBar.elements();
			while(status.hasMoreElements())
			{
				Component comp = (Component)status.nextElement();
				Dimension dim = comp.getPreferredSize();
				comp.setBounds(ileft,
					itop + centerHeight,
					dim.width,
					bottomHeight);
				ileft += dim.width;
			}

			bottom.setBounds(
				ileft,
				itop + centerHeight,
				size.width - rightWidth - ileft - iright,
				bottomHeight);
		}

		// private members
		private Component center;
		private Component right;
		private Component bottom;
		private Vector leftOfScrollBar = new Vector();
	}
		
	class MutableCaretEvent extends CaretEvent
	{
		MutableCaretEvent()
		{
			super(JEditTextArea.this);
		}

		public int getDot()
		{
			return getCaretPosition();
		}

		public int getMark()
		{
			return getMarkPosition();
		}
	}

	class AdjustHandler implements AdjustmentListener
	{
		public void adjustmentValueChanged(final AdjustmentEvent evt)
		{
			if(!scrollBarsInitialized)
				return;

			// If this is not done, mousePressed events accumilate
			// and the result is that scrolling doesn't stop after
			// the mouse is released
			EventQueue.invokeLater(new Runnable() 
			{
				public void run()
				{
					if(evt.getAdjustable() == vertical)
						setFirstLine(vertical.getValue());
					else
						setHorizontalOffset(horizontal != null ? -horizontal.getValue() : 0);
				}
			});
		}
	}

	class ComponentHandler extends ComponentAdapter
	{
		public void componentResized(ComponentEvent evt)
		{
			recalculateVisibleLines();
			scrollBarsInitialized = true;
		}
	}

	class DocumentHandler implements DocumentListener
	{
		public void insertUpdate(DocumentEvent evt)
		{
			documentChanged(evt);

			int offset = evt.getOffset();
			int length = evt.getLength();

			int newStart;
			int newEnd;

			if(selectionStart > offset || (selectionStart == selectionEnd && selectionStart == offset))
				newStart = selectionStart + length;
			else
				newStart = selectionStart;

			if(selectionEnd >= offset)
				newEnd = selectionEnd + length;
			else
				newEnd = selectionEnd;

			select(newStart,newEnd);
		}

		public void removeUpdate(DocumentEvent evt)
		{
			documentChanged(evt);

			int offset = evt.getOffset();
			int length = evt.getLength();

			int newStart;
			int newEnd;

			if(selectionStart > offset)
			{
				if(selectionStart > offset + length)
					newStart = selectionStart - length;
				else
					newStart = offset;
			}
			else
				newStart = selectionStart;

			if(selectionEnd > offset)
			{
				if(selectionEnd > offset + length)
					newEnd = selectionEnd - length;
				else
					newEnd = offset;
			}
			else
				newEnd = selectionEnd;

			select(newStart,newEnd);
		}

		public void changedUpdate(DocumentEvent evt)
		{
		}
	}

	class DragHandler implements MouseMotionListener
	{
		public void mouseDragged(MouseEvent evt)
		{
			if(popup != null && popup.isVisible()) return;

			setSelectionRectangular((evt.getModifiers()	& Settings.getInstance().getRectSelectionModifier()) != 0);

			int x = evt.getX() - painter.getGutterWidth();
			int y = evt.getY();
			select(getMarkPosition(),xyToOffset(x,y));
		}

		public void mouseMoved(MouseEvent evt) {}
	}

	class MouseHandler extends MouseAdapter
	{
		public void mousePressed(MouseEvent evt)
		{
			requestFocus();

			// Focus events not fired sometimes?
			setCaretVisible(true);

			int x = evt.getX() - painter.getGutterWidth();
			int line = yToLine(evt.getY());
			int offset = xToOffset(line,x);
			int dot = getLineStartOffset(line) + offset;


			if((evt.getModifiers() & InputEvent.BUTTON3_MASK) != 0 && popup != null)
			{
				if (rightClickMovesCursor && !isTextSelected())
				{
					setCaretPosition(dot);
				}
				popup.show(painter,x,evt.getY());
				return;
			}

			switch(evt.getClickCount())
			{
				case 1:
					doSingleClick(evt,line,offset,dot);
					break;
				case 2:
					// It uses the bracket matching stuff, so
					// it can throw a BLE
					try
					{
						doDoubleClick(evt,line,offset,dot);
					}
					catch(BadLocationException bl)
					{
						LogMgr.logError("MouseHandler.mousePressed()", "Error setting text", bl);
					}
					break;
				case 3:
					doTripleClick(evt,line,offset,dot);
					break;
			}
		}

		protected void doSingleClick(MouseEvent evt, int line,int offset, int dot)
		{
			if((evt.getModifiers() & InputEvent.SHIFT_MASK) != 0)
			{
				rectSelect = (evt.getModifiers() & Settings.getInstance().getRectSelectionModifier()) != 0;
				select(getMarkPosition(),dot);
			}
			else
			{
				setCaretPosition(dot);
			}
		}

		protected void doDoubleClick(MouseEvent evt, int line, int offset, int dot)
			throws BadLocationException
		{
			// Ignore empty lines
			if(getLineLength(line) == 0) return;

			try
			{
				int bracket = TextUtilities.findMatchingBracket(document,Math.max(0,dot - 1));
				if(bracket != -1)
				{
					int mark = getMarkPosition();
					// Hack
					if(bracket > mark)
					{
						bracket++;
						mark--;
					}
					select(mark,bracket);
					return;
				}
			}
			catch(BadLocationException bl)
			{
				bl.printStackTrace();
			}

			// Ok, it's not a bracket... select the word
			String lineText = getLineText(line);
			char ch = lineText.charAt(Math.max(0,offset - 1));

			String noWordSep = Settings.getInstance().getEditorNoWordSep();
			if (noWordSep == null)	noWordSep = "";

			// If the user clicked on a non-letter char,
			// we select the surrounding non-letters
			boolean selectNoLetter = (!Character.isLetterOrDigit(ch) && noWordSep.indexOf(ch) == -1);

			int wordStart = 0;

			for(int i = offset - 1; i >= 0; i--)
			{
				ch = lineText.charAt(i);
				if(selectNoLetter ^ (!Character.isLetterOrDigit(ch) && noWordSep.indexOf(ch) == -1))
				{
					wordStart = i + 1;
					break;
				}
			}

			int wordEnd = lineText.length();
			for(int i = offset; i < lineText.length(); i++)
			{
				ch = lineText.charAt(i);
				if(selectNoLetter ^ (!Character.isLetterOrDigit(ch) && noWordSep.indexOf(ch) == -1))
				{
					wordEnd = i;
					break;
				}
			}

			int lineStart = getLineStartOffset(line);
			select(lineStart + wordStart,lineStart + wordEnd);
		}

		protected void doTripleClick(MouseEvent evt, int line,int offset, int dot)
		{
			select(getLineStartOffset(line),getLineEndOffset(line)-1);
		}
	}

	class CaretUndo extends AbstractUndoableEdit
	{
		private int start;
		private int end;

		CaretUndo(int start, int end)
		{
			this.start = start;
			this.end = end;
		}

		public boolean isSignificant()
		{
			return false;
		}

		public String getPresentationName()
		{
			return "caret move";
		}

		public void undo() throws CannotUndoException
		{
			super.undo();

			select(start,end);
		}

		public void redo() throws CannotRedoException
		{
			super.redo();

			select(start,end);
		}

		public boolean addEdit(UndoableEdit edit)
		{
			if(edit instanceof CaretUndo)
			{
				CaretUndo cedit = (CaretUndo)edit;
				start = cedit.start;
				end = cedit.end;
				cedit.die();

				return true;
			}
			else
				return false;
		}
	}

	public boolean isRightClickMovesCursor()
	{
		return rightClickMovesCursor;
	}

	public void setRightClickMovesCursor(boolean rightClickMovesCursor)
	{
		this.rightClickMovesCursor = rightClickMovesCursor;
	}
}