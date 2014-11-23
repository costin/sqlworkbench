/*
 * EditorPanel.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2005, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: info@sql-workbench.net
 *
 */
package workbench.gui.sql;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileFilter;

import workbench.db.WbConnection;
import workbench.exception.ExceptionUtil;
import workbench.gui.WbSwingUtilities;
import workbench.gui.actions.ColumnSelectionAction;
import workbench.gui.actions.CommentAction;
import workbench.gui.actions.FileOpenAction;
import workbench.gui.actions.FileSaveAsAction;
import workbench.gui.actions.FindAction;
import workbench.gui.actions.FindAgainAction;
import workbench.gui.actions.FormatSqlAction;
import workbench.gui.actions.MatchBracketAction;
import workbench.gui.actions.ReplaceAction;
import workbench.gui.actions.UnCommentAction;
import workbench.gui.actions.WbAction;
import workbench.gui.components.EncodingPanel;
import workbench.gui.components.ExtensionFileFilter;
import workbench.gui.components.ReplacePanel;
import workbench.gui.components.SearchCriteriaPanel;
import workbench.gui.editor.AnsiSQLTokenMarker;
import workbench.gui.editor.JEditTextArea;
import workbench.gui.editor.SyntaxDocument;
import workbench.gui.editor.SyntaxStyle;
import workbench.gui.editor.Token;
import workbench.gui.editor.TokenMarker;
import workbench.interfaces.ClipboardSupport;
import workbench.interfaces.FilenameChangeListener;
import workbench.interfaces.FontChangedListener;
import workbench.interfaces.FormattableSql;
import workbench.interfaces.Replaceable;
import workbench.interfaces.Searchable;
import workbench.interfaces.TextContainer;
import workbench.interfaces.TextFileContainer;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.sql.ScriptParser;
import workbench.sql.formatter.SqlFormatter;
import workbench.util.StringUtil;
import workbench.util.UnicodeReader;


/**
 * @author  info@sql-workbench.net
 */
public class EditorPanel
	extends JEditTextArea
	implements ClipboardSupport, FontChangedListener, PropertyChangeListener, DropTargetListener,
						 TextContainer, TextFileContainer, Replaceable, Searchable, FormattableSql
{
	private static final Border DEFAULT_BORDER = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
	private AnsiSQLTokenMarker sqlTokenMarker;
	private static final int SQL_EDITOR = 0;
	private static final int JAVA_EDITOR = 1;
	private static final int TEXT_EDITOR = 2;
	private int editorType;
	private String lastSearchCriteria;

	private FindAction findAction;
	private FindAgainAction findAgainAction;
	private ReplaceAction replaceAction;
	private FormatSqlAction formatSql;
	private FileOpenAction fileOpen;
	private ColumnSelectionAction columnSelection;
	private MatchBracketAction matchBracket;
	private CommentAction commentAction;
	private UnCommentAction unCommentAction;

	private List filenameChangeListeners;
	private File currentFile;
	private String fileEncoding;

  private static final SyntaxStyle[] SYNTAX_COLORS;
  static
  {
		SYNTAX_COLORS = new SyntaxStyle[Token.ID_COUNT];

		SYNTAX_COLORS[Token.COMMENT1] = new SyntaxStyle(Color.GRAY,true,false);
		SYNTAX_COLORS[Token.COMMENT2] = new SyntaxStyle(Color.GRAY,true,false);
		SYNTAX_COLORS[Token.KEYWORD1] = new SyntaxStyle(Color.BLUE,false,false);
		SYNTAX_COLORS[Token.KEYWORD2] = new SyntaxStyle(Color.MAGENTA,false,false);
		SYNTAX_COLORS[Token.KEYWORD3] = new SyntaxStyle(new Color(0x009600),false,false);
		SYNTAX_COLORS[Token.LITERAL1] = new SyntaxStyle(new Color(0x650099),false,false);
		SYNTAX_COLORS[Token.LITERAL2] = new SyntaxStyle(new Color(0x650099),false,true);
		SYNTAX_COLORS[Token.LABEL] = new SyntaxStyle(new Color(0x990033),false,true);
		SYNTAX_COLORS[Token.OPERATOR] = new SyntaxStyle(Color.BLACK,false,false);
		SYNTAX_COLORS[Token.INVALID] = new SyntaxStyle(Color.RED,false,true);
  }

	public static EditorPanel createSqlEditor()
	{
		AnsiSQLTokenMarker sql = new AnsiSQLTokenMarker();
		EditorPanel p = new EditorPanel(sql);
		p.editorType = SQL_EDITOR;
		p.sqlTokenMarker = sql;
		return p;
	}

	public static EditorPanel createTextEditor()
	{
		EditorPanel p = new EditorPanel(null);
		p.editorType = TEXT_EDITOR;
		return p;
	}

	private EditorPanel()
	{
		this(null);
	}

	public EditorPanel(TokenMarker aMarker)
	{
		super();
		this.setDoubleBuffered(true);
		this.setFont(Settings.getInstance().getEditorFont());
		this.setBorder(DEFAULT_BORDER);

		this.getPainter().setStyles(SYNTAX_COLORS);

		this.setTabSize(Settings.getInstance().getEditorTabWidth());
		this.setCaretBlinkEnabled(true);
		this.addPopupMenuItem(new FileSaveAsAction(this), true);
		this.fileOpen = new FileOpenAction(this);
		this.addPopupMenuItem(this.fileOpen, false);

		this.findAction = new FindAction(this);
		this.findAction.setEnabled(true);
		this.addKeyBinding(this.findAction);

		this.findAgainAction = new FindAgainAction(this);
		this.findAgainAction.setEnabled(false);
		this.addKeyBinding(this.findAgainAction);

		this.replaceAction = new ReplaceAction(this);
		this.addKeyBinding(this.replaceAction);

		if (aMarker != null) this.setTokenMarker(aMarker);

		this.setMaximumSize(null);
		this.setPreferredSize(null);
		this.setShowLineNumbers(Settings.getInstance().getShowLineNumbers());

		this.columnSelection = new ColumnSelectionAction(this);
		this.matchBracket = new MatchBracketAction(this);
		this.addKeyBinding(this.matchBracket);

		this.commentAction = new CommentAction(this);
		this.unCommentAction = new UnCommentAction(this);
		this.addKeyBinding(this.commentAction);
		this.addKeyBinding(this.unCommentAction);
		
		//this.setSelectionRectangular(true);
		Settings.getInstance().addFontChangedListener(this);
		Settings.getInstance().addPropertyChangeListener(this);
		this.setRightClickMovesCursor(Settings.getInstance().getRightClickMovesCursor());
		new DropTarget(this, DnDConstants.ACTION_COPY, this);
	}

	public void setFileOpenAction(FileOpenAction anAction)
	{
		this.fileOpen = anAction;
	}

	private Set dbFunctions = null;

	public void initDatabaseKeywords(WbConnection aConnection)
	{
		if (aConnection == null) return;
		AnsiSQLTokenMarker token = this.getSqlTokenMarker();
		if (token != null) token.initDatabaseKeywords(aConnection.getSqlConnection());
		this.dbFunctions = aConnection.getMetadata().getDbFunctions();
		if (aConnection.getMetadata().isMySql())
		{
			this.commentChar = "#";
		}
		else
		{
			this.commentChar = "--";
		}
	}

	public void fontChanged(String aKey, Font aFont)
	{
		if (aKey.equals(Settings.EDITOR_FONT_KEY))
		{
			this.setFont(aFont);
		}
	}
	public AnsiSQLTokenMarker getSqlTokenMarker()
	{
		return this.sqlTokenMarker;
	}

	public void showFindOnPopupMenu()
	{
		this.addPopupMenuItem(this.findAction, true);
		this.addPopupMenuItem(this.findAgainAction, false);
		this.addPopupMenuItem(this.replaceAction, false);
	}

	public MatchBracketAction getMatchBracketAction()
	{
		return this.matchBracket;
	}

	public ColumnSelectionAction getColumnSelection()
	{
		return this.columnSelection;
	}

	public FormatSqlAction getFormatSqlAction()
	{
		return this.formatSql;
	}

	public void showFormatSql()
	{
		if (this.formatSql != null) return;
		this.formatSql = new FormatSqlAction(this);
		this.addKeyBinding(this.formatSql);
		this.addPopupMenuItem(this.formatSql, true);
	}

	public void setEditable(boolean editable)
	{
		super.setEditable(editable);
		this.replaceAction.setEnabled(editable);
		this.fileOpen.setEnabled(false);
	}

	public void reformatSql()
	{
		String sql = this.getSelectedStatement();
		ScriptParser parser = new ScriptParser();
		parser.setAlternateDelimiter(Settings.getInstance().getAlternateDelimiter());
		parser.setScript(sql);
		List commands = parser.getCommands();
		String delimit = parser.getDelimiter();

		int count = commands.size();
		if (count < 1) return;
		
		StringBuffer newSql = new StringBuffer(sql.length() + 100);
		String formattedDelimit = StringUtil.EMPTY_STRING;

		if (count > 1)
		{
			// make sure add a delimiter after each statement
			// if we have more then one
			formattedDelimit = "\n" + delimit + "\n\n";
		}
		else
		{
			String s = sql.trim();
			if (s.endsWith(delimit))
			{
				formattedDelimit = delimit;
			}
		}
		
		for (int i=0; i < count; i++)
		{
			String command = (String)commands.get(i);
			SqlFormatter f = new SqlFormatter(command, Settings.getInstance().getMaxSubselectLength());
			f.setDBFunctions(this.dbFunctions);
			try
			{
				String formattedSql = f.format().trim();
				newSql.append(formattedSql);
				if (!command.trim().endsWith(delimit))
				{
					newSql.append(formattedDelimit);
				}
				else
				{
					newSql.append("\n");
				}
			}
			catch (Exception e)
			{
			}
		}

		if (newSql.length() == 0) return;
		int caret = -1;

		if (this.isTextSelected())
		{
			caret = this.getSelectionStart();
			this.setSelectedText(newSql.toString());
			this.select(caret, caret + newSql.length());
		}
		else
		{
			caret = this.getCaretPosition();
			this.setText(newSql.toString());
			if (caret > 0 && caret < this.getText().length()) this.setCaretPosition(caret);
		}

	}

	/**
	 * 	Enable column selection for the next selection.
	 */
	public void enableColumnSelection()
	{
		this.setSelectionRectangular(true);
	}


	/**
	 *	Change the currently selected so that it can be used for a SQL IN statement with
	 *	character datatype.
	 *	e.g.
	 *<pre>
	 *1234
	 *5678
	 *</pre>
	 *will be converted to
	 *<pre>
	 *('1234',
	 *'4456')
	 *</pre>
	 */
	public void makeInListForChar()
	{
		this.makeInList(true);
	}


	public void makeInListForNonChar()
	{
		this.makeInList(false);
	}

	private void makeInList(boolean quoteElements)
	{
		int startline = this.getSelectionStartLine();
		int endline = this.getSelectionEndLine();
		int count = (endline - startline + 1);
		StringBuffer newText = new StringBuffer(count * 80);
		for (int i=startline; i <= endline; i++)
		{
			String line = this.getLineText(i);

			// make sure at least one character from the line is selected
			// if the selection does not extend into the line, then
			// the line is ignored. This can happen with the last line
			int pos = this.getSelectionEnd(i) - this.getLineStartOffset(i);

			StringBuffer newline = new StringBuffer(line.length() + 10);
			if (pos > 0 && line != null && line.length() > 0)
			{
				if (i > startline)
				{
					newText.append(',');
					if ( (quoteElements && count > 5) || (!quoteElements && count > 15)) newText.append('\n');
					newline.append(' ');
				}
				else
				{
					newline.append("(");
				}
				if (quoteElements) newline.append('\'');
				newline.append(line);
				if (quoteElements) newline.append('\'');
			}
			if (i == endline)
			{
				newline.append(')');
			}
			newText.append(newline);
		}
		int pos = this.getSelectionEnd(endline) - this.getLineStartOffset(endline);
		if (pos == 0) newText.append("\n");
		this.setSelectedText(newText.toString());
	}

	public void addPopupMenuItem(WbAction anAction, boolean withSeparator)
	{
		if (withSeparator)
		{
			this.popup.addSeparator();
		}
		this.popup.add(anAction.getMenuItem());
		this.addKeyBinding(anAction);
	}

	/**
	 *	Return the contents of the EditorPanel
	 */
	public String getStatement()
	{
		return this.getText();
	}

	public void dispose()
	{
		this.clearUndoBuffer();
		this.popup.removeAll();
		this.popup = null;
		this.setDocument(new SyntaxDocument());
	}
	/**
	 *	Return the selected text of the editor
	 */
	public String getSelectedStatement()
	{
		String text = this.getSelectedText();
		if (text == null || text.length() == 0)
			return this.getText();
		else
			return text;
	}

	public boolean closeFile(boolean clearText)
	{
    if (this.currentFile == null) return false;
		this.currentFile = null;
		if (clearText)
		{
			this.setCaretPosition(0);
			this.setText("");
			this.clearUndoBuffer();
		}
		this.resetModified();
    return true;
	}

	public void fireFilenameChanged(String aNewName)
	{
		if (this.filenameChangeListeners == null) return;
		for (int i=0; i < this.filenameChangeListeners.size(); i++)
		{
			FilenameChangeListener l = (FilenameChangeListener)this.filenameChangeListeners.get(i);
			l.fileNameChanged(this, aNewName);
		}
	}

	public void addFilenameChangeListener(FilenameChangeListener aListener)
	{
		if (aListener == null) return;
		if (this.filenameChangeListeners == null) this.filenameChangeListeners = new ArrayList();
		this.filenameChangeListeners.add(aListener);
	}

	public void removeFilenameChangeListener(FilenameChangeListener aListener)
	{
		if (aListener == null) return;
		if (this.filenameChangeListeners == null) return;
		this.filenameChangeListeners.remove(aListener);
	}

	public boolean openFile()
	{
		boolean result = false;
		String oldFile = this.getCurrentFileName();
		if (!this.canCloseFile())
		{
			this.requestFocusInWindow();
			return false;
		}

		String lastDir = Settings.getInstance().getLastSqlDir();
		JFileChooser fc = new JFileChooser(lastDir);
		EncodingPanel p = new EncodingPanel();
		fc.setAccessory(p);
		fc.addChoosableFileFilter(ExtensionFileFilter.getSqlFileFilter());
		int answer = fc.showOpenDialog(SwingUtilities.getWindowAncestor(this));
		if (answer == JFileChooser.APPROVE_OPTION)
		{
			String encoding = p.getEncoding();
			result = this.readFile(fc.getSelectedFile(), encoding);
			lastDir = fc.getCurrentDirectory().getAbsolutePath();
			Settings.getInstance().setLastSqlDir(lastDir);
			Settings.getInstance().setDefaultFileEncoding(encoding);
		}
		return result;
	}

	public boolean reloadFile()
	{
		if (!this.hasFileLoaded()) return false;
		if (this.currentFile == null) return false;

		if (this.isModified())
		{
			String msg = ResourceMgr.getString("MsgConfirmUnsavedReload");
			msg = StringUtil.replace(msg, "%filename%", this.getCurrentFileName());
			boolean reload = WbSwingUtilities.getYesNo(this, msg);
			if (!reload) return false;
		}
		boolean result = false;
		int caret = this.getCaretPosition();
		result = this.readFile(currentFile);
		if (result)
		{
			this.setCaretPosition(caret);
		}
		return result;
	}


	public boolean hasFileLoaded()
	{
		String file = this.getCurrentFileName();
		return (file != null) && (file.length() > 0);
	}

	public int checkAndSaveFile()
	{
		if (!this.hasFileLoaded()) return JOptionPane.YES_OPTION;
		int result = JOptionPane.YES_OPTION;

		if (this.isModified())
		{
			String msg = ResourceMgr.getString("MsgConfirmUnsavedEditorFile");
			msg = StringUtil.replace(msg, "%filename%", this.getCurrentFileName());
			result = WbSwingUtilities.getYesNoCancel(this, msg);
			if (result == JOptionPane.YES_OPTION) 
			{
				this.saveCurrentFile();
			}
		}
		return result;
	}

	public boolean canCloseFile()
	{
		if (!this.hasFileLoaded()) return true;
		if (!this.isModified()) return true;
		int choice = this.checkAndSaveFile();
		if (choice == JOptionPane.YES_OPTION)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public boolean readFile(File aFile)
	{
		return this.readFile(aFile, null);
	}

	public boolean readFile(File aFile, String encoding)
	{
		if (aFile == null) return false;
		if (!aFile.exists()) return false;
		if (aFile.length() >= Integer.MAX_VALUE)
		{
			WbSwingUtilities.showErrorMessage(this, ResourceMgr.getString("MsgFileTooBig"));
			return false;
		}
		boolean result = false;

		if (encoding == null || encoding.trim().length() == 0)
		{
			encoding = Settings.getInstance().getDefaultFileEncoding();
		}
		try
		{
			this.setText(""); // clear memory!
			String filename = aFile.getAbsolutePath();
			Reader r = null;
			FileInputStream in = new FileInputStream(filename);
			try
			{
				if (encoding.toLowerCase().startsWith("utf"))
				{
					try
					{
						r = new UnicodeReader(in, encoding);
					}
					catch (IOException io)
					{
						LogMgr.logError("EditorPanel.readFile()", "Error creating UnicodeReader, using default reader", io);
						r = null;
					}
				}
				
				if (r == null)
				{
					r = new InputStreamReader(in, encoding);
				}
			}
			catch (UnsupportedEncodingException e)
			{
				LogMgr.logError("EditorPanel.readFile()", "Unsupported encoding: " + encoding + " requested. Using UTF-8", e);
				try
				{
					encoding = "UTF-8";
					r = new InputStreamReader(in, "UTF-8");
				}
				catch (Throwable ignore) {}
			}

			BufferedReader reader = new BufferedReader(r);
			
			// Reading the text into a StringBuffer before
			// putting it into the editor is faster then
			// then calling this.appendLine() for each line
			// in the file.
			// Using a StrBuffer would make the creation and filling
			// of the buffer faster, but would duplicate the memory usage
			// as StrBuffer.toString() effectively copies the char array
			// whereas StringBuffer.toString() re-uses the internal buffer
			// for the new String object

			StringBuffer content = new StringBuffer((int)aFile.length() + 500);
			String line = reader.readLine();
			while (line != null)
			{
				content.append(line);
				content.append('\n');
				line = reader.readLine();
			}
			this.setText(content.toString());
			reader.close();
			this.currentFile = aFile;
			this.fileEncoding = encoding;
			result = true;
			this.clearUndoBuffer();
			this.resetModified();
			content = null;
			this.fireFilenameChanged(filename);
		}
		catch (IOException e)
		{
			LogMgr.logError("EditorPanel.readFile()", "Error reading file " + aFile.getAbsolutePath(), e);
		}
		catch (OutOfMemoryError mem)
		{
			mem.printStackTrace();
			WbSwingUtilities.showErrorMessage(this, ResourceMgr.getString("MsgOutOfMemoryError"));
		}
		this.setCaretPosition(0);
		return result;
	}

	public boolean saveCurrentFile()
	{
		boolean result = false;
		try
		{
			if (this.currentFile != null)
			{
				this.saveFile(this.currentFile);
				result = true;
			}
			else
			{
				this.saveFile();
			}
		}
		catch (IOException e)
		{
			result = false;
		}
		return result;
	}

	public boolean saveFile()
	{
		boolean result = false;
		String lastDir;
		FileFilter ff = null;
		if (this.editorType == SQL_EDITOR)
		{
			lastDir = Settings.getInstance().getLastSqlDir();
			ff = ExtensionFileFilter.getSqlFileFilter();
		}
		else if (this.editorType == JAVA_EDITOR)
		{
			lastDir = Settings.getInstance().getLastJavaDir();
			ff = ExtensionFileFilter.getJavaFileFilter();
		}
		else
		{
			lastDir = Settings.getInstance().getLastEditorDir();
			ff = ExtensionFileFilter.getTextFileFilter();
		}
		JFileChooser fc = new JFileChooser(lastDir);
		fc.setSelectedFile(this.currentFile);
		fc.addChoosableFileFilter(ff);
		EncodingPanel p = new EncodingPanel(this.fileEncoding);
		fc.setAccessory(p);

		int answer = fc.showSaveDialog(SwingUtilities.getWindowAncestor(this));
		if (answer == JFileChooser.APPROVE_OPTION)
		{
			try
			{
				String encoding = p.getEncoding();
				this.saveFile(fc.getSelectedFile(), encoding);
	      this.fireFilenameChanged(this.getCurrentFileName());
				lastDir = fc.getCurrentDirectory().getAbsolutePath();
				if (this.editorType == SQL_EDITOR)
				{
					Settings.getInstance().setLastSqlDir(lastDir);
				}
				else if (this.editorType == JAVA_EDITOR)
				{
					Settings.getInstance().setLastJavaDir(lastDir);
				}
				else
				{
					Settings.getInstance().setLastEditorDir(lastDir);
				}
			}
			catch (IOException e)
			{
				WbSwingUtilities.showErrorMessage(this, ResourceMgr.getString("ErrorSavingFile") + "\n" + ExceptionUtil.getDisplay(e));
				result = false;
			}
		}
		return result;
	}

	public void saveFile(File aFile)
		throws IOException
	{
		this.saveFile(aFile, this.fileEncoding);
	}

	public void saveFile(File aFile, String encoding)
		throws IOException
	{
		if (encoding == null)
		{
			encoding = Settings.getInstance().getDefaultFileEncoding();
		}

		try
		{
			String filename = aFile.getAbsolutePath();
			int pos = filename.indexOf('.');
			if (pos < 0)
			{
				filename = filename + ".sql";
				aFile = new File(filename);
			}
			Writer w = null;
			FileOutputStream out = new FileOutputStream(filename);
			try
			{
				w = new OutputStreamWriter(out, encoding);
			}
			catch (UnsupportedEncodingException e)
			{
				LogMgr.logError("EditorPanel.readFile()", "Unsupported encoding: " + encoding + " requested. Using UTF-8", e);
				try { w = new OutputStreamWriter(out, "UTF-8"); } catch (Throwable ignore) {}
			}
			PrintWriter writer = new PrintWriter(w);
			int count = this.getLineCount();
			String line;
			int trimLen;
			for (int i=0; i < count; i++)
			{
				line = this.getLineText(i);
				if (line.endsWith("\r\n") || line.endsWith("\n\r"))
					trimLen = 2;
				else if (line.endsWith("\n") || line.endsWith("\r"))
					trimLen = 1;
				else
					trimLen = 0;

				if (trimLen > 0)
					writer.println(line.substring(0, line.length() - trimLen));
				else
					writer.println(line);
			}
			writer.close();
			this.currentFile = aFile;
			this.fileEncoding = encoding;
			this.resetModified();
		}
		catch (IOException e)
		{
			LogMgr.logError("EditorPanel.saveFile()", "Error saving file", e);
			throw e;
		}
	}

	public File getCurrentFile() { return this.currentFile; }

	public String getCurrentFileEncoding()
	{
		if (this.currentFile == null) return null;
		return this.fileEncoding;
	}
	public String getCurrentFileName()
	{
		if (this.currentFile == null) return null;
		return this.currentFile.getAbsolutePath();
	}

	public CommentAction getCommentAction() { return this.commentAction; }
	public UnCommentAction getUnCommentAction() { return this.unCommentAction; }	
	
	public FindAction getFindAction() { return this.findAction; }
	public FindAgainAction getFindAgainAction() { return this.findAgainAction; }

	public ReplaceAction getReplaceAction() { return this.replaceAction; }

	public int find()
	{
		String crit = this.lastSearchCriteria;
		if (crit == null) crit = this.getSelectedText();
		SearchCriteriaPanel p = new SearchCriteriaPanel(crit);
		boolean doFind = p.showFindDialog(this);
		if (!doFind) return -1;
		String criteria = p.getCriteria();
		boolean ignoreCase = p.getIgnoreCase();
		boolean wholeWord = p.getWholeWordOnly();
		boolean useRegex = p.getUseRegex();
		int pos = this.findText(criteria, ignoreCase, wholeWord, useRegex);
		this.lastSearchCriteria = criteria;
		this.findAgainAction.setEnabled(pos > -1);
		return pos;
	}

	public int findNext()
	{
		return this.findNextText();
	}

	public int findFirst(String aValue, boolean ignoreCase, boolean wholeWord, boolean useRegex)
	{
		int pos = this.findText(aValue, ignoreCase, wholeWord, useRegex);
		return pos;
	}

	public int find(String aValue, boolean ignoreCase, boolean wholeWord, boolean useRegex)
	{
		if (this.isCurrentSearchCriteria(aValue, ignoreCase, wholeWord, useRegex))
		{
			return this.findNext();
		}
		else
		{
			return this.findFirst(aValue, ignoreCase, wholeWord, useRegex);
		}
	}
	private ReplacePanel replacePanel = null;

	public void replace()
	{
		if (this.replacePanel == null)
		{

			this.replacePanel = new ReplacePanel(this);
		}
		this.replacePanel.showReplaceDialog(this, this.getSelectedText());
	}

	/**
	 *	Find and replace the next occurance of the current search string
	 */
	public boolean replaceNext(String aReplacement)
	{
		int pos = this.findNext();
		if (pos > -1)
		{
			this.setSelectedText(aReplacement);
		}
		return (pos > -1);
	}

	public boolean isTextSelected()
	{
		int selStart = this.getSelectionStart();
		int selEnd = this.getSelectionEnd();
		return (selStart > -1 && selEnd > selStart);
	}
	public int replaceAll(String value, String replacement, boolean selectedText, boolean ignoreCase, boolean wholeWord, boolean useRegex)
	{
		String old = null;
		if (selectedText)
		{
			old = this.getSelectedText();
		}
		else
		{
			old = this.getText();
		}
		int cursor = this.getCaretPosition();
		int selStart = this.getSelectionStart();
		int selEnd = this.getSelectionEnd();
		int newLen = -1;
		String regex = this.getSearchExpression(value, ignoreCase, wholeWord, useRegex);
		String newText = old.replaceAll(regex, replacement);
		if (selectedText)
		{
			this.setSelectedText(newText);
			newLen = this.getText().length();
		}
		else
		{
			this.setText(newText);
			newLen = this.getText().length();
			selStart = -1;
			selEnd = -1;
		}
		if (cursor < newLen)
		{
			this.setCaretPosition(cursor);
		}
		if (selStart > -1 && selEnd > selStart && selStart < newLen && selEnd < newLen)
		{
			this.select(selStart, selEnd);
		}
		return 0;
	}

	public boolean replaceCurrent(String aReplacement)
	{
		if (this.searchPatternMatchesSelectedText())
		{
			this.setSelectedText(aReplacement);
			return true;
		}
		else
		{
			if (this.findNext() > -1)
			{
				this.setSelectedText(aReplacement);
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (Settings.PROPERTY_SHOW_LINE_NUMBERS.equals(evt.getPropertyName()))
		{
			this.setShowLineNumbers(Settings.getInstance().getShowLineNumbers());
			this.repaint();
		}
		else if (Settings.PROPERTY_EDITOR_TAB_WIDTH.equals(evt.getPropertyName()))
		{
			this.setTabSize(Settings.getInstance().getEditorTabWidth());
			this.repaint();
		}
	}

	public void dragEnter(java.awt.dnd.DropTargetDragEvent dropTargetDragEvent)
	{
		if (this.isEditable())
		{
			dropTargetDragEvent.acceptDrag (DnDConstants.ACTION_COPY);
		}
		else
		{
			dropTargetDragEvent.rejectDrag();
		}
	}

	public void dragExit(java.awt.dnd.DropTargetEvent dropTargetEvent)
	{
	}

	public void dragOver(java.awt.dnd.DropTargetDragEvent dropTargetDragEvent)
	{
	}

	public void drop(java.awt.dnd.DropTargetDropEvent dropTargetDropEvent)
	{
		try
		{
			Transferable tr = dropTargetDropEvent.getTransferable();
			if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
			{
				dropTargetDropEvent.acceptDrop(DnDConstants.ACTION_COPY);
				java.util.List fileList = (java.util.List)tr.getTransferData(DataFlavor.javaFileListFlavor);
				if (fileList != null && fileList.size() == 1)
				{
					File file = (File)fileList.get(0);
					if (this.canCloseFile())
					{
						this.readFile(file);
						dropTargetDropEvent.getDropTargetContext().dropComplete(true);
					}
					else
					{
						dropTargetDropEvent.getDropTargetContext().dropComplete(false);
					}
				}
				else
				{
					dropTargetDropEvent.getDropTargetContext().dropComplete(false);
					final Window w = SwingUtilities.getWindowAncestor(this);
					EventQueue.invokeLater(new Runnable()
					{
						public void run()
						{
							w.toFront();
							w.requestFocus();
							WbSwingUtilities.showErrorMessage(w, ResourceMgr.getString("ErrorNoMultipleDrop"));
						}
					});
				}
			}
			else
			{
				dropTargetDropEvent.rejectDrop();
			}
		}
		catch (IOException io)
		{
			io.printStackTrace();
			dropTargetDropEvent.rejectDrop();
		}
		catch (UnsupportedFlavorException ufe)
		{
			ufe.printStackTrace();
			dropTargetDropEvent.rejectDrop();
		}
	}


	public void dropActionChanged(java.awt.dnd.DropTargetDragEvent dropTargetDragEvent)
	{
	}

}