/*
 * SqlPanel.java
 *
 * Created on November 25, 2001, 2:17 PM
 */

package workbench.gui.sql;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import workbench.WbManager;
import workbench.db.DataSpooler;
import workbench.db.DeleteScriptGenerator;
import workbench.db.WbConnection;
import workbench.gui.MainWindow;
import workbench.gui.WbSwingUtilities;
import workbench.gui.actions.*;
import workbench.gui.components.ConnectionInfo;
import workbench.gui.components.DataStoreTableModel;
import workbench.gui.components.ExtensionFileFilter;
import workbench.gui.components.ImportFileOptionsPanel;
import workbench.gui.components.TabbedPaneUIFactory;
import workbench.gui.components.TextComponentMouseListener;
import workbench.gui.components.WbScrollPane;
import workbench.gui.components.WbSplitPane;
import workbench.gui.components.WbTable;
import workbench.gui.components.WbToolbar;
import workbench.gui.components.WbToolbarSeparator;
import workbench.gui.components.WbTraversalPolicy;
import workbench.gui.editor.AnsiSQLTokenMarker;
import workbench.gui.menu.TextPopup;
import workbench.interfaces.Commitable;
import workbench.interfaces.DbExecutionListener;
import workbench.interfaces.DbUpdater;
import workbench.interfaces.FilenameChangeListener;
import workbench.interfaces.FontChangedListener;
import workbench.interfaces.FormattableSql;
import workbench.interfaces.Interruptable;
import workbench.interfaces.MainPanel;
import workbench.interfaces.Spooler;
import workbench.interfaces.TextChangeListener;
import workbench.interfaces.TextFileContainer;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.sql.MacroManager;
import workbench.sql.commands.SingleVerbCommand;
import workbench.storage.DataStore;
import workbench.util.SqlUtil;
import workbench.util.StringUtil;
import workbench.util.WbWorkspace;
import workbench.exception.ExceptionUtil;
import workbench.sql.ScriptParser;



/**
 *	A panel with an SQL editor (EditorPanel), a log panel and
 *	a panel for displaying SQL results (DwPanel)
 *
 * @author  workbench@kellerer.org
 * @version 1.0
 */
public class SqlPanel
	extends JPanel
	implements Runnable, FontChangedListener, ActionListener, TextChangeListener,
				    PropertyChangeListener,
						MainPanel, Spooler, TextFileContainer, DbUpdater, Interruptable, FormattableSql, Commitable
{
	private boolean runSelectedCommand;
	private boolean runCurrentCommand;

	EditorPanel editor;
	private DwPanel data;
	private SqlHistory sqlHistory;

	private JTextArea log;
	private JTabbedPane resultTab;
	private JSplitPane contentPanel;
	private boolean threadBusy;
	private boolean suspended = true;

	private Thread background;
	private int currentHistoryEntry = -1;
	private int maxHistorySize = 10;

	private List actions = new ArrayList();
	private List toolbarActions = new ArrayList();

	private List filenameChangeListeners;

	private NextStatementAction nextStmtAction;
	private PrevStatementAction prevStmtAction;
	private FirstStatementAction firstStmtAction;
	private LastStatementAction lastStmtAction;
	private StopAction stopAction;
	private ExecuteAllAction executeAll;
	private ExecuteCurrentAction executeCurrent;
	private ExecuteSelAction executeSelected;

	private DataToClipboardAction dataToClipboard;
	private SaveDataAsAction exportDataAction;
	private CopyAsSqlInsertAction copyAsSqlInsert;
	private CreateDeleteScriptAction createDeleteScript;
	private ImportFileAction importFileAction;
	private PrintAction printDataAction;
	private PrintPreviewAction printPreviewAction;

	private CommitAction commitAction;
	private RollbackAction rollbackAction;

	private OptimizeAllColumnsAction optimizeAllCol;
	private FormatSqlAction formatSql;

	private SpoolDataAction spoolData;
	private UndoAction undo;
	private RedoAction redo;

	private int internalId;
	private String historyFilename;

	private FileDiscardAction fileDiscardAction;
	private FindDataAction findDataAction;
	private FindDataAgainAction findDataAgainAction;
	private String lastSearchCriteria;
	private WbToolbar toolbar;
	private ConnectionInfo connectionInfo;

	private WbConnection dbConnection;
	private boolean updating;
	private boolean cancelExecution;
	private boolean updateRunning;

	private boolean textModified = false;
	private String tabName = null;

	private ImageIcon loadingIcon;
	private Icon dummyIcon;
	//private boolean dummyIconFetched = false;
	private int lastDividerLocation = -1;

	private ArrayList execListener = null;
	
	/** Creates new SqlPanel */
	public SqlPanel(int anId)
	{
		this.setId(anId);
		this.setDoubleBuffered(true);
		this.setBorder(WbSwingUtilities.EMPTY_BORDER);
		this.setLayout(new BorderLayout());
		this.setOpaque(true);
		this.data = new DwPanel();
		this.data.setBorder(WbSwingUtilities.EMPTY_BORDER);
		this.log = new JTextArea();
		this.log.setDoubleBuffered(true);
		this.log.setBorder(new EmptyBorder(0,2,0,0));
		this.log.setFont(WbManager.getSettings().getMsgLogFont());
		this.log.setEditable(false);
		this.log.setLineWrap(true);
		this.log.setWrapStyleWord(true);
		this.log.addMouseListener(new TextComponentMouseListener());

		this.maxHistorySize = WbManager.getSettings().getMaxHistorySize();

		this.resultTab = new JTabbedPane();
		this.resultTab.setTabPlacement(JTabbedPane.TOP);
		this.resultTab.setUI(TabbedPaneUIFactory.getBorderLessUI());
		this.resultTab.setDoubleBuffered(true);
		this.resultTab.setBorder(WbSwingUtilities.EMPTY_BORDER);

		this.resultTab.addTab(ResourceMgr.getString(ResourceMgr.TAB_LABEL_RESULT), this.data);
		JScrollPane scroll = new WbScrollPane(log);
		this.resultTab.addTab(ResourceMgr.getString(ResourceMgr.TAB_LABEL_MSG), scroll);

		WbTraversalPolicy pol = new WbTraversalPolicy();
		pol.setDefaultComponent(data.getTable());
		pol.addComponent(data.getTable());
		this.resultTab.setFocusTraversalPolicy(pol);

		this.editor = EditorPanel.createSqlEditor();
		this.contentPanel = new WbSplitPane(JSplitPane.VERTICAL_SPLIT, true, this.editor, this.resultTab);
		this.contentPanel.setOneTouchExpandable(true);
		this.contentPanel.setContinuousLayout(true);
		this.contentPanel.setBorder(WbSwingUtilities.EMPTY_BORDER);

		this.add(this.contentPanel, BorderLayout.CENTER);

		this.initActions();
		this.initToolbar();
		this.setupActionMap();

		this.data.getTable().setMaxColWidth(WbManager.getSettings().getMaxColumnWidth());
		this.data.getTable().setMinColWidth(WbManager.getSettings().getMinColumnWidth());
		this.makeReadOnly();
		this.checkResultSetActions();
		this.initStatementHistory();

		Settings s = WbManager.getSettings();
		s.addFontChangedListener(this);

		this.editor.addTextChangeListener(this);
		this.data.setUpdateDelegate(this);

		WbManager.getSettings().addChangeListener(this);
	}

	public String getId()
	{
		return Integer.toString(this.internalId);
	}

	public void setId(int anId)
	{
		this.internalId = anId;
		this.historyFilename = WbManager.getSettings().getConfigDir() + "WbStatements" + Integer.toString(this.internalId);
	}

	public void initDefaults()
	{
		int loc = this.getHeight() / 2;
		if (loc <= 5) loc = 200;
		this.contentPanel.setDividerLocation(loc);
	}

	public void saveSettings(Properties props)
	{
		int location = this.contentPanel.getDividerLocation();
		int last = this.contentPanel.getLastDividerLocation();
		props.setProperty("tab" + (this.internalId - 1) + ".divider.location", Integer.toString(location));
		props.setProperty("tab" + (this.internalId - 1) + ".divider.lastlocation", Integer.toString(last));
	}

	public void restoreSettings(Properties props)
	{
		try
		{
			int loc = Integer.parseInt(props.getProperty("tab" + (this.internalId - 1) + ".divider.location", "0"));
			if (loc <= 0) loc = 200;
			this.contentPanel.setDividerLocation(loc);
			loc = Integer.parseInt(props.getProperty("tab" + (this.internalId - 1) + ".divider.lastlocation", "0"));
			if (loc > 0) this.contentPanel.setLastDividerLocation(loc);
		}
		catch (Exception e)
		{
			LogMgr.logWarning("SqlPanel.restoreSettings()", "Error when restore settings", e);
		}
	}

	public WbToolbar getToolbar()
	{
		return this.toolbar;
	}

	private void initToolbar()
	{
		this.toolbar = new WbToolbar();
		this.toolbar.addDefaultBorder();
		for (int i=0; i < this.toolbarActions.size(); i++)
		{
			WbAction a = (WbAction)toolbarActions.get(i);
			boolean toolbarSep = "true".equals((String)a.getValue(WbAction.TBAR_SEPARATOR));
			{
				if (toolbarSep)
				{
					toolbar.addSeparator();
				}
				a.addToToolbar(toolbar);
			}
		}
		toolbar.addSeparator();
		this.connectionInfo = new ConnectionInfo(this.toolbar.getBackground());
		toolbar.add(this.connectionInfo);
	}

	public void addToToolbar(WbAction anAction, boolean withSeperator)
	{
		this.toolbar.add(anAction.getToolbarButton(true), this.toolbar.getComponentCount() - 1);
		if (withSeperator) this.toolbar.add(new WbToolbarSeparator(), this.toolbar.getComponentCount() - 1);
	}

	public void updateUI()
	{
		super.updateUI();
		if (this.toolbar != null)
		{
			this.toolbar.updateUI();
			this.toolbar.repaint();
		}
	}

	public boolean readFile(String aFilename)
	{
		if (aFilename == null) return false;
		boolean result = false;
		File f = new File(aFilename);
		if (!f.exists()) return false;

		if (this.editor.readFile(f))
		{
			this.fileDiscardAction.setEnabled(true);
      this.fireFilenameChanged(this.editor.getCurrentFileName());
			this.selectEditor();
			result = true;
			this.showFileIcon();
		}
		else
		{
			this.removeTabIcon();
		}
		return result;
	}

	public boolean openFile()
	{
		String oldFile = this.editor.getCurrentFileName();
		if (!this.canCloseFile())
		{
			this.selectEditorLater();
			return false;
		}
		if (this.editor.openFile())
		{
			String newFile = this.editor.getCurrentFileName();
			if (newFile != null && !newFile.equals(oldFile))
			{
				this.fileDiscardAction.setEnabled(true);
	      this.fireFilenameChanged(this.editor.getCurrentFileName());
				this.selectEditorLater();
			}
			this.showFileIcon();
			return true;
		}
		return false;
	}

	public boolean hasFileLoaded()
	{
		String file = this.editor.getCurrentFileName();
		return (file != null) && (file.length() > 0);
	}

	public void checkAndSaveFile()
	{
		if (!this.hasFileLoaded()) return;
		if (this.editor.isModified())
		{
			String filename = this.editor.getCurrentFileName().replaceAll("\\\\", "\\\\\\\\");
			String msg = ResourceMgr.getString("MsgConfirmUnsavedEditorFile").replaceAll("%filename%", filename);
			if (WbSwingUtilities.getYesNo(this, msg))
			{
				this.editor.saveCurrentFile();
			}
		}
	}

	public EditorPanel getEditor()
	{
		return this.editor;
	}
	/**
	 *	Check if the current file has modifications.
	 *	@return true Modifications saved or user doesn't care
	 *          false do not close the current
	 */
	public boolean canCloseFile()
	{
		if (!this.hasFileLoaded()) return true;
		boolean result = true;
		if (this.editor.isModified())
		{
			String filename = this.editor.getCurrentFileName().replaceAll("\\\\", "\\\\\\\\");
			String msg = ResourceMgr.getString("MsgConfirmUnsavedEditorFile").replaceAll("%filename%", filename);
			int choice = WbSwingUtilities.getYesNoCancel(this, msg);
			if (choice == JOptionPane.YES_OPTION)
			{
				this.editor.saveCurrentFile();
			}
			result = (choice != JOptionPane.CANCEL_OPTION);
		}
		return result;
	}

	public boolean saveCurrentFile()
	{
		String oldFile = this.editor.getCurrentFileName();
		if (this.editor.saveCurrentFile())
		{
			String newFile = this.editor.getCurrentFileName();
			if (newFile != null && !newFile.equals(oldFile))
			{
	      this.fireFilenameChanged(this.editor.getCurrentFileName());
				return true;
			}
		}
		return false;
	}

	public boolean saveFile()
	{
		String oldFile = this.editor.getCurrentFileName();
		if (this.editor.saveFile())
		{
			String newFile = this.editor.getCurrentFileName();
			if (newFile != null && !newFile.equals(oldFile))
			{
	      this.fireFilenameChanged(this.editor.getCurrentFileName());
				return true;
			}
		}
		return false;
	}

	public void clearSqlStatements()
	{
		if (this.sqlHistory != null) this.sqlHistory.clear();
		this.editor.setText("");
		this.checkStatementActions();
	}

	public boolean closeFile(boolean emptyEditor)
	{
		this.checkAndSaveFile();
		if (this.editor.closeFile(emptyEditor))
    {
			this.fileDiscardAction.setEnabled(false);
      this.fireFilenameChanged(this.tabName);
			this.removeTabIcon();
			this.selectEditorLater();
			return true;
    }
		return false;
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

	public void undoExpand()
	{
		int newLoc = (int)(this.getHeight() / 2);
		this.contentPanel.setDividerLocation(newLoc);
	}

	public void expandEditor()
	{
		this.contentPanel.setDividerLocation(this.getHeight());
	}

	public void expandResultTable()
	{
		this.contentPanel.setDividerLocation(0);
	}

	private void initActions()
	{
		WbAction a;
		ExecuteSql e = new ExecuteSql();
		this.executeAll = new ExecuteAllAction(e);

		ExecuteSelectedSql se = new ExecuteSelectedSql();
		this.executeSelected = new ExecuteSelAction(se);

		ExecuteCurrentSql c = new ExecuteCurrentSql();
		this.executeCurrent = new ExecuteCurrentAction(c);

		MakeLowerCaseAction makeLower = new MakeLowerCaseAction(this.editor);
		MakeUpperCaseAction makeUpper = new MakeUpperCaseAction(this.editor);

		this.editor.showFindOnPopupMenu();
		this.editor.showFormatSql();

		//this.editor.addPopupMenuItem(makeLower, true);
		//this.editor.addPopupMenuItem(makeUpper, false);

		this.editor.addPopupMenuItem(this.executeSelected, true);
		this.editor.addPopupMenuItem(this.executeAll, false);
		this.editor.addPopupMenuItem(this.executeCurrent, false);

		TextPopup pop = (TextPopup)this.editor.getRightClickPopup();

		a = new FileOpenAction(this);
		a.setCreateMenuSeparator(true);
		this.actions.add(a);
		this.actions.add(new FileSaveAction(this));
		this.actions.add(new FileSaveAsAction(this));
		this.fileDiscardAction = new FileDiscardAction(this);
		this.actions.add(this.fileDiscardAction);

		this.undo = new UndoAction(this.editor);
		this.actions.add(undo);
		this.redo = new RedoAction(this.editor);
		this.actions.add(redo);

		a = pop.getCutAction();
		a.setCreateMenuSeparator(true);
		this.actions.add(a);
		this.actions.add(pop.getCopyAction());
		this.actions.add(pop.getPasteAction());

		a = pop.getClearAction();
		a.setCreateMenuSeparator(true);
		this.actions.add(a);
		this.actions.add(pop.getSelectAllAction());
		this.actions.add(editor.getColumnSelection());

		FindAction f = this.editor.getFindAction();
		f.setCreateMenuSeparator(true);
		this.actions.add(f);
		this.actions.add(this.editor.getFindAgainAction());
		this.actions.add(this.editor.getReplaceAction());

		makeLower.setCreateMenuSeparator(true);
		this.actions.add(makeLower);
		this.actions.add(makeUpper);

		this.actions.add(this.data.getStartEditAction());
		this.actions.add(this.data.getUpdateDatabaseAction());
		this.actions.add(this.data.getInsertRowAction());
		this.actions.add(this.data.getDeleteRowAction());

		this.createDeleteScript = new CreateDeleteScriptAction(this);
		this.actions.add(this.createDeleteScript);

		this.exportDataAction = this.data.getTable().getExportAction();
		this.exportDataAction.setCreateMenuSeparator(true);
		this.exportDataAction.setEnabled(false);

		SelectEditorAction sea = new SelectEditorAction(this);
		sea.setCreateMenuSeparator(true);
		this.actions.add(sea);
		SelectResultAction r = new SelectResultAction(this);
		this.actions.add(r);
    this.actions.add(new SelectMaxRowsAction(this));

		a = new ExpandEditorAction(this);
		a.setCreateMenuSeparator(true);
		this.actions.add(a);
		this.actions.add(new ExpandResultAction(this));
		this.actions.add(new UndoExpandAction(this));

		this.optimizeAllCol = new OptimizeAllColumnsAction(this);
		this.optimizeAllCol.setCreateMenuSeparator(true);
		this.optimizeAllCol.setEnabled(false);
		this.optimizeAllCol.putValue(Action.SMALL_ICON, null);
		this.optimizeAllCol.putValue(WbAction.MAIN_MENU_ITEM, ResourceMgr.MNU_TXT_VIEW);
		this.actions.add(this.optimizeAllCol);

		this.dataToClipboard = this.data.getTable().getDataToClipboardAction();
		this.dataToClipboard.setEnabled(false);
		this.actions.add(this.exportDataAction);
		this.actions.add(this.dataToClipboard);

		this.copyAsSqlInsert = new CopyAsSqlInsertAction(this.data.getTable());
		this.actions.add(this.copyAsSqlInsert);

		this.importFileAction = new ImportFileAction(this);
		this.actions.add(this.importFileAction);

		this.printDataAction = this.data.getTable().getPrintAction();
		this.printPreviewAction = this.data.getTable().getPrintPreviewAction();

		this.actions.add(this.executeAll);
		this.actions.add(this.executeSelected);
		this.actions.add(this.executeCurrent);

		this.spoolData = new SpoolDataAction(this);
		this.actions.add(this.spoolData);

		this.stopAction = new StopAction(this);
		this.stopAction.setEnabled(false);
		this.actions.add(this.stopAction);

		this.commitAction = new CommitAction(this);
		this.commitAction.setCreateMenuSeparator(true);
		this.commitAction.setEnabled(false);
		this.actions.add(this.commitAction);
		this.rollbackAction = new RollbackAction(this);
		this.rollbackAction.setEnabled(false);
		this.actions.add(this.rollbackAction);

		this.firstStmtAction = new FirstStatementAction(this);
		this.firstStmtAction.setEnabled(false);
		this.actions.add(this.firstStmtAction);
		
		this.prevStmtAction = new PrevStatementAction(this);
		this.prevStmtAction.setEnabled(false);
		this.actions.add(this.prevStmtAction);

		this.nextStmtAction = new NextStatementAction(this);
		this.nextStmtAction.setEnabled(false);
		this.actions.add(this.nextStmtAction);

		this.lastStmtAction = new LastStatementAction(this);
		this.lastStmtAction.setEnabled(false);
		this.actions.add(this.lastStmtAction);
		
		this.executeAll.setEnabled(false);
		this.executeSelected.setEnabled(false);
		this.initBackgroundThread();

		this.toolbarActions.add(this.executeSelected);
		this.toolbarActions.add(this.stopAction);
		this.toolbarActions.add(this.firstStmtAction);
		this.toolbarActions.add(this.prevStmtAction);
		this.toolbarActions.add(this.nextStmtAction);
		this.toolbarActions.add(this.lastStmtAction);

		this.toolbarActions.add(this.data.getUpdateDatabaseAction());
		this.toolbarActions.add(this.data.getStartEditAction());
		this.toolbarActions.add(this.data.getInsertRowAction());
		this.toolbarActions.add(this.data.getDeleteRowAction());

		this.commitAction.setCreateToolbarSeparator(true);
		this.toolbarActions.add(this.commitAction);
		this.toolbarActions.add(this.rollbackAction);

		this.findDataAction = this.data.getTable().getFindAction();
		this.findDataAction.setEnabled(false);
		this.findDataAction.setCreateMenuSeparator(true);
		this.findDataAgainAction = this.data.getTable().getFindAgainAction();
		this.findDataAgainAction.setEnabled(false);

		this.formatSql = this.editor.getFormatSqlAction();
		this.formatSql.setCreateMenuSeparator(true);
		this.actions.add(this.formatSql);

		a = new CreateSnippetAction(this.editor);
		this.actions.add(a);
		a = new CleanJavaCodeAction(this.editor);
		this.actions.add(a);

		a = new MakeInListAction(this.editor);
		a.setCreateMenuSeparator(true);
		this.actions.add(a);
		this.actions.add(new MakeNonCharInListAction(this.editor));

		this.findDataAction.setCreateMenuSeparator(true);
		this.actions.add(this.findDataAction);
		this.actions.add(this.findDataAgainAction);
		this.printDataAction.setCreateMenuSeparator(true);
		this.actions.add(this.printDataAction);
		this.actions.add(this.printPreviewAction);

		this.disableExecuteActions();
	}

	private void setupActionMap()
	{
		InputMap im = new ComponentInputMap(this);
		ActionMap am = new ActionMap();
		this.setInputMap(WHEN_IN_FOCUSED_WINDOW, im);
		this.setActionMap(am);

		for (int i=0; i < this.actions.size(); i++)
		{
			WbAction wb = (WbAction)this.actions.get(i);
			wb.addToInputMap(im, am);
		}
		editor.getInputMap().setParent(im);
		editor.getActionMap().setParent(am);
	}

	public void addToActionMap(WbAction anAction)
	{
		InputMap im = this.getInputMap(WHEN_IN_FOCUSED_WINDOW);
		ActionMap am = this.getActionMap();
		anAction.addToInputMap(im, am);
	}

	public void selectEditorLater()
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				selectEditor();
			}
		});
	}

	public void reformatSql()
	{
		this.storeStatementInHistory();
		this.editor.reformatSql();
		this.selectEditorLater();
	}

	private JTabbedPane parentTab;

	private boolean isCurrentTab()
	{
		if (this.parentTab == null)
		{
			Component p = this.getParent();
			if (p instanceof JTabbedPane)
			{
				this.parentTab = (JTabbedPane)p;
			}
		}

		if (this.parentTab == null) return false;

		return (this.parentTab.getSelectedComponent() == this);
	}

	public void selectEditor()
	{
		// make sure the editor is really visible!
		//boolean visible = this.isVisible();
		//boolean current = this.isCurrentTab();
		//System.out.println("current =" + current +",visible=" + visible);
		if (this.isVisible() && this.isCurrentTab())
		{
			editor.requestFocusInWindow();
		}
	}

	public void selectResult()
	{
		if (this.isVisible() && this.isCurrentTab())
		{
			showResultPanel();
			data.getTable().requestFocusInWindow();
		}
	}

	public void saveChangesToDatabase()
	{
		// check if we really want to save the data
		// it fhe "DbDebugger" is not enabled this will
		// always return true, otherwise it depends on the user's
		// selection after the SQL preview has been displayed
		if (!this.data.shouldSaveChanges(this.dbConnection)) return;
		this.setBusy(true);

		this.updateRunning = true;
		this.showStatusMessage(ResourceMgr.getString("MsgUpdatingDatabase"));
		this.setCancelState(true);
		this.disableExecuteActions();

		Thread t = new Thread()
		{
			public void run()
			{
				updateDb();
			}
		};
		t.setName("Workbench DB Update Thread");
		t.setDaemon(true);
		t.start();
	}

	private void updateDb()
	{
		String errorMessage = null;
		boolean success = false;

		try
		{
			this.log.setText(ResourceMgr.getString("MsgUpdatingDatabase"));
			this.log.append("\n");
			int rows = this.data.saveChanges(this.dbConnection);
			this.log.append(this.data.getLastMessage());
			success = true;
		}
		catch (OutOfMemoryError mem)
		{
			// do not show the error message right away
			// the message dialog should only be shown if the
			// animated icon is not running! Otherwise 
			// the system might lock
			this.log.setText(ExceptionUtil.getDisplay(mem));
			errorMessage = ResourceMgr.getString("MsgOutOfMemoryError");
			success = false;
		}
		catch (Exception e)
		{
			errorMessage = this.data.getLastMessage();
			this.log.setText(errorMessage);
			success = false;
		}
		finally
		{
			this.updateRunning = false;
			this.setCancelState(false);
			this.setBusy(false);
		}

		this.enableExecuteActions();

		if (success)
		{
			this.clearStatusMessage();
			this.checkResultSetActions();
		}
		else
		{
			final String msg = errorMessage;
			// Make sure the error dialog is displayed on the AWT
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					WbManager.getInstance().showErrorMessage(getParentWindow(), msg);
				}
			});
		}
	}

	/**
	 *	When the SqlPanel becomse visible (i.e. the tab is
	 *	selected in the main window) we set the focus to
	 *	the editor component.
	 */
	public void setVisible(boolean aFlag)
	{
		super.setVisible(aFlag);
		if (aFlag)
		{
			selectEditorLater();
		}
	}

	public List getToolbarActions()
	{
		return this.toolbarActions;
	}

	public List getActions()
	{
		return this.actions;
	}

	public long addRow()
	{
		return this.data.addRow();
	}

	public void deleteRow()
	{
		this.data.deleteRow();
	}

	public void makeReadOnly()
	{
		this.data.endEdit();
	}

	/**
	 *	Show a message in the log panel. This will also switch 
	 *	the display to the log panel (away from the result panel
	 */
	public synchronized void showLogMessage(String aMsg)
	{
		this.showLogPanel();
		this.log.setText(aMsg);
	}

	/**
	 *	Clear the message log, but do not switch the panel display to it.
	 */
	public void clearLog()
	{
		this.log.setText("");
	}

	/**
	 *	Show the panel with the log messages.
	 */
	public void showLogPanel()
	{
		this.resultTab.setSelectedIndex(1);
	}

	/**
	 *	Show the panel with the result set
	 */
	public void showResultPanel()
	{
		this.resultTab.setSelectedIndex(0);
	}

	/**	
	 *	Display a message in the status bar of the DwPanel.
	 */
	public void showStatusMessage(String aMsg)
	{
		this.data.setStatusMessage(aMsg);
	}

	/**
	 *	Clear the message in the status bar of the DwPanel
	 */
	public void clearStatusMessage()
	{
		this.data.clearStatusMessage();
	}

	/**
	 *	Create and initialize the SQL Thread which carries
	 *	out the actual SQL execution.
	 *	The Thread's runnable method is this.run();
	 */
	private void initBackgroundThread()
	{
		this.suspendThread();
		this.background = new Thread(this);
		this.background.setDaemon(true);
		this.background.setName("SQL execution thread - " + this.internalId);
		this.background.start();
	}

	public void initStatementHistory()
	{
		this.sqlHistory = new SqlHistory(this.maxHistorySize);
		this.checkStatementActions();
	}

	private void checkStatementActions()
	{
		if (this.sqlHistory == null)
		{
			this.nextStmtAction.setEnabled(false);
			this.prevStmtAction.setEnabled(false);
			this.lastStmtAction.setEnabled(false);
			this.firstStmtAction.setEnabled(false);
		}
		else
		{
			this.nextStmtAction.setEnabled(this.sqlHistory.hasNext());
			this.lastStmtAction.setEnabled(this.sqlHistory.hasNext());
			this.prevStmtAction.setEnabled(this.sqlHistory.hasPrevious());
			this.firstStmtAction.setEnabled(this.sqlHistory.hasPrevious());
		}
	}

	public void showNextStatement()
	{
		this.sqlHistory.showNext(this.editor);
		this.checkStatementActions();
		this.selectEditor();
	}

	public void showPrevStatement()
	{
		this.sqlHistory.showPrevious(editor);
		this.checkStatementActions();
		this.selectEditor();
	}

	public void showFirstStatement()
	{
		this.sqlHistory.showFirst(editor);
		this.checkStatementActions();
		this.selectEditor();
	}
	
	public void showLastStatement()
	{
		this.sqlHistory.showLast(editor);
		this.checkStatementActions();
		this.selectEditor();
	}
	
	public void showCurrentHistoryStatement()
	{
		this.sqlHistory.showCurrent(editor);
		this.checkStatementActions();
		this.selectEditor();
	}

	public void readFromWorkspace(WbWorkspace w)
		throws IOException
	{
		SqlHistory history = this.getSqlHistory();

		try
		{
			w.readHistoryData(this.internalId - 1, history);
		}
		catch (Exception e)
		{
			LogMgr.logWarning("SqlPanel.readFromWorkspace()", "Could not read history data for index " + (this.internalId - 1));
			this.clearSqlStatements();
		}

		String filename = w.getExternalFileName(this.internalId - 1);
		this.tabName = w.getTabTitle(this.internalId - 1);
		if (this.tabName != null && this.tabName.length() == 0)
		{
			this.tabName = null;
		}

		int maxRows = w.getMaxRows(this.internalId - 1);
		this.data.setMaxRows(maxRows);

		boolean fileLoaded = false;
		if (filename != null)
		{
			fileLoaded = this.readFile(filename);
		}

		if (!fileLoaded)
		{
			try
			{
				this.showCurrentHistoryStatement();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			int cursorPos = w.getExternalFileCursorPos(this.internalId - 1);
			if (cursorPos > -1 && cursorPos < this.editor.getText().length()) this.editor.setCaretPosition(cursorPos);
		}

		Properties props = w.getSettings();
		this.restoreSettings(props);
	}

	/** Do any work which should be done during the process of saving the
	 *  current workspace, but before the workspace file is actually opened!
	 *  This is to ensure a corrupted workspace due to interrupting the saving
	 *  because of the check for unsaved changes in the current editor file
	 */
	public void prepareWorkspaceSaving()
	{
		this.checkAndSaveFile();
	}

	public void saveToWorkspace(WbWorkspace w)
		throws IOException
	{
		this.saveHistory(w);
		Properties props = w.getSettings();
		this.saveSettings(props);
		w.setMaxRows(this.internalId - 1, this.data.getMaxRows());
		if (this.hasFileLoaded())
		{
			w.setExternalFileName(this.internalId - 1, this.getCurrentFileName());
			w.setExternalFileCursorPos(this.internalId - 1, this.editor.getCaretPosition());
		}
	}

	public void saveHistory(WbWorkspace w)
		throws IOException
	{
		this.storeStatementInHistory();
		w.addHistoryEntry(this.getHistoryFilename(), this.sqlHistory);
	}

	public SqlHistory getSqlHistory()
	{
		return this.sqlHistory;
	}


	public String getHistoryFilename()
	{
		return this.historyFilename + ".txt";
	}

	private final String DEFAULT_TAB_NAME = ResourceMgr.getString("LabelTabStatement");

	public void setTabTitle(JTabbedPane tab, int index)
	{
		String fname = null;
		String tooltip = null;

		fname = this.getCurrentFileName();
		if (fname != null)
		{
			File f = new File(fname);
			fname = f.getName();
			tooltip = f.getAbsolutePath();
		}
		else
		{
			fname = this.getTabName();
		}
		if (fname == null) fname = DEFAULT_TAB_NAME;
		tab.setTitleAt(index, fname+ " " + Integer.toString(index+1));
		if (index < 9)
		{
			char c = Integer.toString(index+1).charAt(0);
			tab.setMnemonicAt(index, c);
		}
		tab.setToolTipTextAt(index, tooltip);
	}

	public String getTabName()
	{
		return this.tabName;
	}

	public void setTabName(String aName)
	{
		this.tabName = aName;
		this.fireFilenameChanged(aName);
	}

	public String getCurrentFileName()
	{
		return this.editor.getCurrentFileName();
	}

	public void setStatementText(String aStatement)
	{
		this.storeStatementInHistory();
		if (this.editor.getCurrentFile() != null) this.editor.saveCurrentFile();
		this.editor.closeFile(true);
		this.editor.setText(aStatement);
	}

	public void disconnect()
	{
		this.setConnection(null);
		this.makeReadOnly();
		this.log.setText("");
	}

	public WbConnection getConnection()
	{
		return this.dbConnection;
	}

	public boolean isConnected()
	{
		// I'm only checking if the connection is defined, because
		// MainWindow will make sure a valid connection is set
		// for the panel. When using only one connection for all
		// panels, isClosed() will block the entire AWT thread!

		return (this.dbConnection != null);

		/*
		// check isBusy() first! This is very important, because
		// Connection.isClosed() will be blocked until the current
		// statement is finished!
		if (this.isBusy()) return true;

		if (this.dbConnection == null) return false;
		return true;
		try
		{
			return !this.dbConnection.isClosed();
		}
		catch (Throwable e)
		{
			return false;
		}
		*/
	}

	public void setConnection(WbConnection aConnection)
	{
		this.dbConnection = aConnection;
		try
		{
			this.data.setConnection(aConnection);
		}
		catch (Exception e)
		{
		}
		boolean enable = (aConnection != null);
		if (this.connectionInfo != null) this.connectionInfo.setConnection(aConnection);
		this.setExecuteActionStates(enable);

		if (aConnection != null)
		{
			AnsiSQLTokenMarker token = this.editor.getSqlTokenMarker();
			token.initDatabaseKeywords(aConnection.getSqlConnection());
		}

		this.checkResultSetActions();

		if (aConnection != null)
		{
			this.commitAction.setEnabled(!aConnection.getAutoCommit());
			this.rollbackAction.setEnabled(!aConnection.getAutoCommit());
		}
		else
		{
			this.commitAction.setEnabled(false);
			this.rollbackAction.setEnabled(false);
		}
		this.doLayout();
	}

	public boolean isRequestFocusEnabled() { return true; }
	//public boolean isFocusTraversable() { return true; }

	public void suspendThread()
	{
		this.suspended = true;
	}

	public synchronized void resumeThread()
	{
		this.suspended = false;
		notify();
	}

	public synchronized void storeStatementInHistory()
	{
		this.sqlHistory.addContent(editor);
		this.checkStatementActions();
	}

	public void cancelUpdate()
	{
		WbTable table = this.data.getTable();
		if (table != null)
		{
			DataStoreTableModel model = (DataStoreTableModel)table.getModel();
			if (model == null) return;
			DataStore ds = table.getDataStore();
			if (ds == null) return;
			ds.cancelUpdate();

			if (!this.dbConnection.getAutoCommit())
			{
				String msg = ResourceMgr.getString("MsgCommitPartialUpdate");
				int commit = WbSwingUtilities.getCommitRollbackQuestion(this, msg);
				{
					try
					{
						if (commit == WbSwingUtilities.DO_COMMIT)
						{
							this.dbConnection.commit();
							ds.resetStatusForSentRow();
						}
						else
						{
							this.dbConnection.rollback();
							ds.resetDmlSentStatus();
						}

					}
					catch (SQLException e)
					{
						LogMgr.logError("SqlPanel.cancelExecution()", "Commit failed!", e);
						msg = e.getMessage();
						WbSwingUtilities.showErrorMessage(this, msg);
					}
				}
				this.data.rowCountChanged();
				this.repaint();
			}
			else
			{
				ds.resetStatusForSentRow();
			}
		}
		this.setCancelState(false);
	}

	public void forceAbort()
	{
		if (!this.isBusy()) return;
		try
		{
			this.background.interrupt();
			this.background = null;
		}
		catch (Exception e)
		{
			LogMgr.logWarning("SqlPanel.forceAbort()", "Error when trying to kill background thread",e);
		}
		finally
		{
		}
	}

	public boolean abortExecution()
	{
		if (!this.isBusy()) return true;
		boolean success = false;
		int wait = WbManager.getSettings().getIntProperty(this.getClass().getName(), "abortwait", 1);
		try
		{
			LogMgr.logDebug("SqlPanel.abortExecution()", "Interrupting SQL Thread...");
			this.background.interrupt();
			this.background.join(wait * 1000);
			if (this.isBusy())
			{
				LogMgr.logDebug("SqlPanel.abortExecution()", "SQL Thread still running after " + wait +"s!");
			}
			else
			{
				success = true;
			}
		}
		catch (Exception e)
		{
			LogMgr.logError("SqlPanel.abortExecution()", "Error when interrupting SQL thread", e);
		}
		return success;
	}

	public void cancelExecution()
	{
		if (!this.isBusy()) return;

		this.showStatusMessage(ResourceMgr.getString("MsgCancellingStmt") + "\n");
		try
		{
			if (this.importRunning)
			{
				WbTable table = this.data.getTable();
				if (table != null)
				{
					DataStoreTableModel model = (DataStoreTableModel)table.getModel();
					DataStore ds = table.getDataStore();
					ds.cancelImport();
				}
				this.setCancelState(false);
			}
			else if (this.updateRunning)
			{
				this.cancelUpdate();
			}
			else
			{
				Thread t = new Thread()
				{
					public void run()
					{
						try
						{
							showCancelIcon();
							data.cancelExecution();
							suspendThread();
						}
						finally
						{
							setCancelState(false);
						}
					}
				};
				t.setName("SQL Cancel Thread");
				t.setDaemon(true);
				t.setPriority(Thread.MAX_PRIORITY);
				t.start();
			}
		}
		catch (Throwable th)
		{
			LogMgr.logError("SqlPanel.cancelExecution()", "Error cancelling execution", th);
		}
	}

	public void setCancelState(final boolean aFlag)
	{
		this.setActionState(this.stopAction, aFlag);
	}

	/**
	 *	Modify the enabled state of the given action.
	 */
	public void setActionState(final Action anAction, final boolean aFlag)
	{
		anAction.setEnabled(aFlag);
	}

	public void setActionState(final Action[] anActionList, final boolean aFlag)
	{
		for (int i=0; i < anActionList.length; i++)
		{
			anActionList[i].setEnabled(aFlag);
		}
	}

	public String getCurrentStatement()
	{
		return this.editor.getSelectedStatement();
	}

	/*
	 * 	Execute the given SQL string. This is invoked from the the run() and other
	 *  methods in order to execute the SQL command. It takes care of updating the
	 *  actions and the menu.
	 *  The actual execution and display of the result is handled by displayResult()
	 */
	private void runStatement(String sql)
	{
		this.showStatusMessage(ResourceMgr.getString(ResourceMgr.MSG_EXEC_SQL));
		this.data.getStartEditAction().setSwitchedOn(false);

		this.storeStatementInHistory();

		// the dbStart should be fired *after* updating the
		// history, as the history might be saved ("AutoSaveHistory") if the MainWindow
		// receives the execStart event
		this.fireDbExecStart();

		this.setBusy(true);
		this.setCancelState(true);
		this.makeReadOnly();
		this.data.setBatchUpdate(true);

		this.displayResult(sql);

		this.data.setBatchUpdate(false);

		this.setBusy(false);

		this.clearStatusMessage();
		this.setCancelState(false);
		this.checkResultSetActions();

		this.fireDbExecEnd();
		this.selectEditorLater();


		/*
		if (sql.trim().toLowerCase().startsWith("shutdown") && WbManager.getSettings().getProcessHsqlShutdown())
		{
			String url = this.dbConnection.getUrl();
			if (url != null)
			{
				if (url.startsWith("jdbc:hsqldb"))
				{
					final MainWindow win = (MainWindow)SwingUtilities.getWindowAncestor(this);
					String msg = ResourceMgr.getString("MsgShutdownHsqlDb");
					this.showLogMessage(msg);
					WbManager.getInstance().showErrorMessage(this.getParentWindow(), msg);
					SwingUtilities.invokeLater(new Runnable()
					{
						public void run()
						{
							win.disconnect(true, true);
						}
					});

				}
			}
		}
		*/

	}

	public void executeMacro(final String macroName, final boolean replaceText)
	{
		final String sql = MacroManager.getInstance().getMacroText(macroName);
		if (sql == null || sql.trim().length() == 0) return;
		if (replaceText)
		{
			this.storeStatementInHistory();
			this.editor.setText(sql);
			this.storeStatementInHistory();
		}
		Thread t = new Thread()
		{
			public void run()
			{
				runStatement(sql);
			}
		};
		t.setName("SqlPanel - Macro execution thread");
		t.setDaemon(true);
		t.start();
	}

	public void spoolData()
	{
		String sql = SqlUtil.makeCleanSql(this.editor.getSelectedStatement(),false);

		DataSpooler spooler = new DataSpooler();
		spooler.executeStatement(this.getParentWindow(), this.dbConnection, sql);
	}

	private boolean importRunning = false;

	public synchronized void importFile()
	{
		if (!this.data.startEdit()) return;

		WbTable table = this.data.getTable();
		if (table == null) return;
		final DataStoreTableModel model = (DataStoreTableModel)table.getModel();
		final DataStore ds = table.getDataStore();
		final String currentFormat = ds.getDefaultDateFormat();
		if (ds == null) return;
		String lastDir = WbManager.getSettings().getLastImportDir();
		JFileChooser fc = new JFileChooser(lastDir);
		ImportFileOptionsPanel optionPanel = new ImportFileOptionsPanel();
		optionPanel.restoreSettings();
		fc.setAccessory(optionPanel);
		fc.addChoosableFileFilter(ExtensionFileFilter.getTextFileFilter());
		int answer = fc.showOpenDialog(SwingUtilities.getWindowAncestor(this));
		if (answer == JFileChooser.APPROVE_OPTION)
		{
			this.setActionState(this.importFileAction, false);
			String filename = fc.getSelectedFile().getAbsolutePath();
			lastDir = fc.getCurrentDirectory().getAbsolutePath();
			WbManager.getSettings().setLastImportDir(lastDir);
			optionPanel.saveSettings();
			try
			{
				ds.setDefaultDateFormat(optionPanel.getDateFormat());
				ds.setDefaultNumberFormat(optionPanel.getNumberFormat());
				final boolean header = optionPanel.getContainsHeader();
				final String delimit = optionPanel.getColumnDelimiter();
				final String quote = optionPanel.getQuoteChar();
				final String fname = filename;
				this.setBusy(true);
				this.setCancelState(true);
				Thread importThread = new Thread()
				{
					public void run()
					{
						try
						{
							importRunning = true;
							ds.setProgressMonitor(data);
							model.importFile(fname, header, delimit, quote);
							data.rowCountChanged();
							importRunning = false;
						}
						catch (Exception e)
						{
							LogMgr.logError("SqlPanel.importFile() - worker thread", "Error when importing " + fname, e);
						}
						finally
						{
							setBusy(false);
							ds.setDefaultDateFormat(currentFormat);
							ds.setProgressMonitor(null);
							data.clearStatusMessage();
							setCancelState(false);
							checkResultSetActions();
						}
					}
				};
				Thread.yield();
				importThread.setName("Data Import Thread");
				importThread.setDaemon(true);
				importThread.start();
			}
			catch (Exception e)
			{
				LogMgr.logError("SqlPanel.importFile()", "Error importing " + filename, e);
			}
		}
		this.selectEditor();
	}

	private void appendToLog(final String aString)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				log.append(aString);
				log.setCaretPosition(log.getDocument().getLength());
			}
		});
	}

	private void displayResult(String aSql)
	{
		boolean compressLog = WbManager.getSettings().getConsolidateLogMsg();

		ScriptParser scriptParser = new ScriptParser();
		scriptParser.setAlternateDelimiter(WbManager.getSettings().getAlternateDelimiter());
		
		String script = null;
		boolean editorCommand = true;
		int startOffset = 0;
		if (aSql == null)
		{
			// the flags to run the current command, the selected text, or everything
			// are set in the nested classes ExecuteSql, ExcecuteCurrentSql, ExecuteSelectedSql
			if (this.runSelectedCommand)
			{
				script = this.editor.getSelectedStatement();
				startOffset = this.editor.getSelectionStart();
			}
			else
			{
				script = this.editor.getStatement();
			}
		}
		else
		{
			script = aSql;
			editorCommand = false;
		}
		
		try
		{
			this.log.setText(ResourceMgr.getString(ResourceMgr.MSG_EXEC_SQL));
			
			String cleanSql = SqlUtil.makeCleanSql(script, false);
			String macro = MacroManager.getInstance().getMacroText(cleanSql);
			if (macro != null)
			{
				script = macro;
				editorCommand = false;
			}

			scriptParser.setScript(script);
			List sqls = scriptParser.getCommands();
			
			int commandWithError = -1;
			int startIndex = 0;
			int endIndex = sqls.size();
			int count = sqls.size();
			
			if (this.runCurrentCommand) 
			{
				int pos = this.editor.getCaretPosition();
				count = 1;
				startIndex = scriptParser.getCommandIndexAtCursorPos(pos);
				endIndex = startIndex + 1;
			}

			String msg = ResourceMgr.getString("TxtScriptStatementFinished");
			msg = StringUtil.replace(msg, "%total%", Integer.toString(count));
			this.log.setText("");

			boolean onErrorAsk = true;
			if (count == 1) compressLog = false;

			this.data.scriptStarting();
			long startTime = System.currentTimeMillis();

			for (int i=startIndex; i < endIndex; i++)
			{
				StringBuffer logmsg = new StringBuffer(200);
				String sql = scriptParser.getCommand(i);//(String)sqls.get(i);
				this.data.runStatement(sql);
				if (!compressLog || !this.data.wasSuccessful())
				{
					logmsg.append(this.data.getLastMessage());
					if (count > 1)
					{
						logmsg.append("\n");
						logmsg.append(StringUtil.replace(msg, "%nr%", Integer.toString(i + 1)));
						logmsg.append("\n\n");
					}
					this.appendToLog(logmsg.toString());
				}

				if (i == 0 && !this.data.hasResultSet())
				{
					this.showLogPanel();
				}
				// in case of a batch execution we need to make sure that
				// this thread can actually be interrupted!
				Thread.yield();
				if (suspended) break;
				//sqls.set(i, null);
				if (!this.data.wasSuccessful())
				{
					commandWithError = i;
				}
				if (count > 1 && !this.data.wasSuccessful() && onErrorAsk && (i < (count - 1)))
				{
					String question = ResourceMgr.getString("MsgScriptStatementError");
					question = StringUtil.replace(question, "%nr%", Integer.toString(i+1));
					question = StringUtil.replace(question, "%count%", Integer.toString(count));
					int choice = WbSwingUtilities.getYesNoIgnoreAll(this, question);

					if (choice == JOptionPane.NO_OPTION)
					{
						break;
					}
					if (choice == WbSwingUtilities.IGNORE_ALL)
					{
						onErrorAsk = false;
					}
				}
			}
			
			if (commandWithError > -1 && editorCommand)
			{
				int startPos = scriptParser.getStartPosForCommand(commandWithError) + startOffset;
				int endPos = scriptParser.getEndPosForCommand(commandWithError) + startOffset;
				int line = this.editor.getLineOfOffset(startPos);
				this.editor.scrollTo(line, 0);
				//this.editor.setCaretPosition(startPos + 1);
				this.editor.selectError(startPos, endPos);
			}
			
			final long end = System.currentTimeMillis();

			if (compressLog)
			{
				msg = count + " " + ResourceMgr.getString("MsgTotalStatementsExecuted") + "\n";
				this.appendToLog(msg);
				long rows = this.data.getRowsAffectedByScript();
				msg = rows + " " + ResourceMgr.getString("MsgTotalRowsAffected") + "\n";
				this.appendToLog(msg);
			}

			if (this.data.hasResultSet())
			{
				this.showResultPanel();
				this.data.checkUpdateTable();
			}
			else
			{
				this.showLogPanel();
			}

			if (count > 1)
			{
				this.appendToLog("\n");
				this.appendToLog(ResourceMgr.getString("TxtScriptFinished"));
				long execTime = (end - startTime);
				String s = ResourceMgr.getString("MsgScriptExecTime") + " " + (((double)execTime) / 1000.0) + "s";
				this.appendToLog("\n");
				this.appendToLog(s);
			}
		}
		catch (SQLException e)
		{
			this.showLogMessage(this.data.getLastMessage());
		}
		catch (Exception e)
		{
			this.showLogMessage(this.data.getLastMessage());
			LogMgr.logError("SqlPanel.displayResult()", "Error executing statement", e);
		}
		finally
		{
			this.data.scriptFinished();
		}
	}

	public void generateDeleteScript()
	{
		WbTable table = this.data.getTable();
		if (table == null) return;

		DataStore ds = table.getDataStore();
		if (ds == null) return;

		int row = table.getSelectedRow();
		if (row < 0)
		{
			WbManager.getInstance().showErrorMessage(this, ResourceMgr.getString("MsgSelectRow"));
			return;
		}

		String updatetable = ds.getUpdateTable();
		String schema = ds.getUpdateTableSchema();

		Map pkvalues = ds.getPkValues(row);
		try
		{
			DeleteScriptGenerator gen = new DeleteScriptGenerator(this.dbConnection);
			gen.setTable(null, schema, updatetable);
			gen.setValues(pkvalues);
			String script = gen.createScript();
			MainWindow parent = (MainWindow)SwingUtilities.getWindowAncestor(this);
			String title = ResourceMgr.getString("TxtDeleteScriptWindowTitle") + " " + schema + "." + ds.getRealUpdateTable();
			final String id = this.getClass().getName() + ".ScriptDialog";
			JFrame f = new JFrame(title);
			f.setIconImage(ResourceMgr.getPicture("workbench16").getImage());
			final JFrame fd = f;
			f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			f.addWindowListener(new WindowAdapter()
			{
				public void windowClosing(WindowEvent e)
				{
					WbManager.getSettings().storeWindowSize(fd, id);
				}
			});
			EditorPanel editor = EditorPanel.createSqlEditor();
			editor.setText(script);
			editor.setCaretPosition(0);
			//editor.addPopupMenuItem(new FileSaveAsAction(editor), true);

			f.getContentPane().add(editor);
			if (!WbManager.getSettings().restoreWindowSize(f, id))
			{
				f.setSize(400,400);
			}
			WbSwingUtilities.center(f, parent);
			f.show();
		}
		catch (Exception e)
		{
			LogMgr.logError("SqlPanel.generateDeleteScript", "Error generating delete script", e);
		}
	}

  public void selectMaxRowsField()
  {
		this.showResultPanel();
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				data.selectMaxRowsField();
			}
		});

  }

	private void checkResultSetActions()
	{
		boolean hasResult = this.data.hasResultSet();
		this.setActionState(new Action[] {this.findDataAction, this.dataToClipboard, this.exportDataAction, this.optimizeAllCol}, hasResult);

		boolean mayEdit = hasResult && this.data.hasUpdateableColumns();
		this.setActionState(this.data.getStartEditAction(), mayEdit);
		this.setActionState(this.createDeleteScript, mayEdit);

		boolean findNext = hasResult && (this.data.getTable() != null && this.data.getTable().canSearchAgain());
		this.setActionState(this.findDataAgainAction, findNext);

		boolean canUpdate = this.data.isUpdateable();
		this.setActionState(this.copyAsSqlInsert, canUpdate);
		this.setActionState(this.importFileAction, canUpdate);
	}

	public void run()
	{
		while (true)
		{
			try
			{
				if (suspended)
				{
					synchronized(this)
					{
						while(suspended)
							wait();
					}
				}
			}
			catch(InterruptedException e)
			{
				LogMgr.logDebug("SqlPanel.run()", "Thread " + this.internalId + " has been interrupted");
				return;
			}

			this.runStatement(null);

			suspendThread();
		}
	}


	private ImageIcon getLoadingIndicator()
	{
		if (this.loadingIcon == null)
		{
			if (WbManager.getSettings().getUseAnimatedIcon())
			{
				this.loadingIcon = ResourceMgr.getPicture("loading");
			}
			else
			{
				this.loadingIcon = ResourceMgr.getPicture("loading-static");
			}
		}
		return this.loadingIcon;
	}

	private ImageIcon cancelIcon = null;

	private ImageIcon getCancelIndicator()
	{
		if (this.cancelIcon == null)
		{
			if (WbManager.getSettings().getUseAnimatedIcon())
			{
				this.cancelIcon = ResourceMgr.getPicture("cancelling");
			}
			else
			{
				this.cancelIcon = ResourceMgr.getPicture("cancelling-static");
			}
		}
		return this.cancelIcon;
	}

	private void enableExecuteActions()
	{
		this.setExecuteActionStates(true);
	}
	private void disableExecuteActions()
	{
		this.setExecuteActionStates(false);
	}
	private void setExecuteActionStates(boolean aFlag)
	{
		this.executeAll.setEnabled(aFlag);
		this.executeSelected.setEnabled(aFlag);
		this.executeCurrent.setEnabled(aFlag);
		this.importFileAction.setEnabled(aFlag);
		this.spoolData.setEnabled(aFlag);
	}

	private synchronized void showCancelIcon()
	{
		this.showTabIcon(this.getCancelIndicator());
	}

	private ImageIcon fileIcon = null;
	private ImageIcon fileModifiedIcon = null;

	private ImageIcon getFileIcon()
	{
		ImageIcon icon = null;
		if (this.textModified)
		{
			if (this.fileModifiedIcon == null)
			{
				this.fileModifiedIcon = ResourceMgr.getPicture("file-modified-icon");
			}
			icon = this.fileModifiedIcon;
		}
		else
		{
			if (this.fileIcon == null)
			{
				this.fileIcon = ResourceMgr.getPicture("file-icon");
			}
			icon = this.fileIcon;
		}

		return icon;
	}

	private void removeTabIcon()
	{
		if (this.isBusy()) return;
		this.showTabIcon(null);
	}

	private void showFileIcon()
	{
		if (this.isBusy()) return;
		this.showTabIcon(this.getFileIcon());
	}

	private void showTabIcon(ImageIcon icon)
	{
		Container parent = this.getParent();
		if (parent instanceof JTabbedPane)
		{
			JTabbedPane tab = (JTabbedPane)parent;
			int index = tab.indexOfComponent(this);
			tab.setIconAt(index, icon);
		}
	}

	private synchronized void setBusy(boolean busy)
	{
		if (busy == this.threadBusy) return;
		Container parent = this.getParent();
		if (parent instanceof JTabbedPane)
		{
			JTabbedPane tab = (JTabbedPane)parent;
			int index = tab.indexOfComponent(this);
			if (index >= 0 && index < tab.getTabCount())
			{
				try
				{
					if (busy)
					{
						tab.setIconAt(index, getLoadingIndicator());
					}
					else
					{
						if (this.hasFileLoaded())
						{
							tab.setIconAt(index, this.getFileIcon());
						}
						else
						{
							tab.setIconAt(index, null);
						}
						if (this.loadingIcon != null) this.loadingIcon.getImage().flush();
						if (this.cancelIcon != null) this.cancelIcon.getImage().flush();
					}
				}
				catch (Throwable th)
				{
					LogMgr.logWarning("SqlPanel.setBusy()", "Error when setting busy icon!", th);
				}
			}
		}
		this.threadBusy = busy;
		this.setExecuteActionStates(!busy);
	}

	public synchronized boolean isBusy() { return this.threadBusy; }


	public void fontChanged(String aFontId, Font newFont)
	{
		if (aFontId.equals(Settings.MSGLOG_FONT_KEY))
		{
			this.log.setFont(newFont);
		}
	}

	public Window getParentWindow()
	{
		return SwingUtilities.getWindowAncestor(this);
	}

	/** Invoked when an action occurs.
	 *
	 */
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == this.optimizeAllCol)
		{
			this.data.getTable().optimizeAllColWidth();
		}
	}

	public void textStatusChanged(boolean modified)
	{
		this.textModified = modified;
		if (this.hasFileLoaded())
		{
			this.showFileIcon();
		}
	}

	public void addDbExecutionListener(DbExecutionListener l)
	{
		if (this.execListener == null) this.execListener = new ArrayList();
		this.execListener.add(l);
	}

	public void removeDbExecutionListener(DbExecutionListener l)
	{
		if (this.execListener == null) return;
		this.execListener.remove(l);
	}

	private void fireDbExecStart()
	{
		if (this.execListener == null) return;
		int count = this.execListener.size();
		for (int i=0; i < count; i++)
		{
			((DbExecutionListener)this.execListener.get(i)).executionStart(this.dbConnection, this);
		}
	}
	private void fireDbExecEnd()
	{
		if (this.execListener == null) return;
		int count = this.execListener.size();
		for (int i=0; i < count; i++)
		{
			((DbExecutionListener)this.execListener.get(i)).executionEnd(this.dbConnection, this);
		}
	}

	public void dispose()
	{
		this.data.clearContent();
		WbManager.getSettings().removeChangeLister(this);
		this.abortExecution();
		this.background = null;
	}

	public void propertyChange(java.beans.PropertyChangeEvent evt)
	{
		if (evt.getPropertyName().equals(Settings.ANIMATED_ICONS_KEY))
		{
			if (this.cancelIcon != null)
			{
				this.cancelIcon.getImage().flush();
				this.cancelIcon = null;
			}
			if (this.loadingIcon != null)
			{
				this.loadingIcon.getImage().flush();
				this.loadingIcon = null;
			}
		}
	}

	public void commit()
	{
		if (this.isBusy()) return;
		this.runStatement(SingleVerbCommand.COMMIT.getVerb());
	}

	public void rollback()
	{
		if (this.isBusy()) return;
		this.runStatement(SingleVerbCommand.ROLLBACK.getVerb());
	}

	class ExecuteCurrentSql implements ActionListener
	{
		public void actionPerformed(ActionEvent actionEvent)
		{
			if (!isBusy())
			{
				runSelectedCommand = false;
				runCurrentCommand = true;
				resumeThread();
			}
			else
			{
				Toolkit.getDefaultToolkit().beep();
				LogMgr.logWarning("ExecuteCurrentSql", "actionPerformed called while thread is busy!");
			}
		}
	}

	class ExecuteSql implements ActionListener
	{
		public void actionPerformed(ActionEvent actionEvent)
		{
			if (!isBusy())
			{
				runSelectedCommand = false;
				runCurrentCommand = false;
				resumeThread();
			}
			else
			{
				Toolkit.getDefaultToolkit().beep();
				LogMgr.logWarning("ExecuteSql", "actionPerformed called while thread is busy!");
			}
		}

	}

	class ExecuteSelectedSql implements ActionListener
	{
		public void actionPerformed(ActionEvent actionEvent)
		{
			if (!isBusy())
			{
				runSelectedCommand = true;
				runCurrentCommand = false;
				resumeThread();
			}
			else
			{
				Toolkit.getDefaultToolkit().beep();
				LogMgr.logWarning("ExecuteSelectedSql", "actionPerformed called while thread is busy!");
			}
		}

	}

}