/*
 * SqlPanel.java
 *
 * Created on November 25, 2001, 2:17 PM
 */

package workbench.gui.sql;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import workbench.WbManager;
import workbench.db.exporter.DataExporter;
import workbench.db.DeleteScriptGenerator;
import workbench.db.WbConnection;
import workbench.exception.ExceptionUtil;
import workbench.gui.WbSwingUtilities;
import workbench.gui.actions.*;

import workbench.gui.components.*;
import workbench.gui.menu.TextPopup;
import workbench.interfaces.*;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.sql.MacroManager;
import workbench.sql.ScriptParser;
import workbench.sql.commands.SingleVerbCommand;
import workbench.storage.DataStore;
import workbench.util.SqlUtil;
import workbench.util.StrBuffer;
import workbench.util.StringUtil;
import workbench.util.WbThread;
import workbench.util.WbWorkspace;


/**
 *	A panel with an SQL editor (EditorPanel), a log panel and
 *	a panel for displaying SQL results (DwPanel)
 *
 * @author  workbench@kellerer.org
 * @version 1.0
 */
public class SqlPanel
	extends JPanel
	implements FontChangedListener, ActionListener, TextChangeListener,
				    PropertyChangeListener,
						MainPanel, Spooler, TextFileContainer, DbUpdater, Interruptable, FormattableSql, Commitable,
						JobErrorHandler, FilenameChangeListener
{
	EditorPanel editor;
	private DwPanel data;
	private SqlHistory sqlHistory;

	private JTextArea log;
	private JTabbedPane resultTab;
	private JSplitPane contentPanel;
	private boolean threadBusy;
	private boolean cancelExecution = false;

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
	private CopyAsSqlUpdateAction copyAsSqlUpdate;
	private CreateDeleteScriptAction createDeleteScript;
	private ImportFileAction importFileAction;
	private PrintAction printDataAction;
	private PrintPreviewAction printPreviewAction;

	private WbMenu copySelectedMenu;
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
	private FileReloadAction fileReloadAction;
	private FindDataAction findDataAction;
	private FindDataAgainAction findDataAgainAction;
	private String lastSearchCriteria;
	private WbToolbar toolbar;
	private ConnectionInfo connectionInfo;

	private WbConnection dbConnection;
	private boolean updating;

	private boolean updateRunning;
	private boolean textModified = false;
	private String tabName = null;

	private ImageIcon loadingIcon;
	private Icon dummyIcon;

	private int lastDividerLocation = -1;

	private ArrayList execListener = null;
	private Thread executionThread = null;

	/** Creates new SqlPanel */
	public SqlPanel(int anId)
	{
		this.setId(anId);
		this.setDoubleBuffered(true);
		this.setBorder(WbSwingUtilities.EMPTY_BORDER);
		this.setLayout(new BorderLayout());
		this.setOpaque(true);
		DwStatusBar statusBar = new DwStatusBar();
		Border b = BorderFactory.createCompoundBorder(new EmptyBorder(2, 1, 0, 1), new EtchedBorder());
		statusBar.setBorder(b);

		this.data = new DwPanel(statusBar);
		this.data.setBorder(WbSwingUtilities.EMPTY_BORDER);
		this.data.setDoubleBuffered(true);

		this.log = new JTextArea();
		this.log.setDoubleBuffered(true);
		this.log.setBorder(new EmptyBorder(0,2,0,0));
		this.log.setFont(Settings.getInstance().getMsgLogFont());
		this.log.setEditable(false);
		this.log.setLineWrap(true);
		this.log.setWrapStyleWord(true);
		this.log.addMouseListener(new TextComponentMouseListener());

		this.maxHistorySize = Settings.getInstance().getMaxHistorySize();

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
		this.editor.addFilenameChangeListener(this);
		this.contentPanel = new WbSplitPane(JSplitPane.VERTICAL_SPLIT, true, this.editor, this.resultTab);
		this.contentPanel.setOneTouchExpandable(true);
		this.contentPanel.setContinuousLayout(true);
		this.contentPanel.setBorder(WbSwingUtilities.EMPTY_BORDER);

		this.add(this.contentPanel, BorderLayout.CENTER);
		this.add(statusBar, BorderLayout.SOUTH);

		this.initActions();
		this.initToolbar();
		this.setupActionMap();

		this.data.getTable().setMaxColWidth(Settings.getInstance().getMaxColumnWidth());
		this.data.getTable().setMinColWidth(Settings.getInstance().getMinColumnWidth());
		this.makeReadOnly();
		this.checkResultSetActions();
		this.initStatementHistory();

		Settings s = Settings.getInstance();
		s.addFontChangedListener(this);

		this.editor.addTextChangeListener(this);
		this.data.setUpdateDelegate(this);
		this.data.addPropertyChangeListener("updateTable", this);

		Settings.getInstance().addChangeListener(this);
	}

	public String getId()
	{
		return Integer.toString(this.internalId);
	}

	public void setId(int anId)
	{
		this.internalId = anId;
		this.historyFilename = Settings.getInstance().getConfigDir() + "WbStatements" + Integer.toString(this.internalId);
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

	public DwPanel getData()
	{
		return this.data;
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
			this.fileReloadAction.setEnabled(true);
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
				this.fileReloadAction.setEnabled(true);
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
		return this.editor.hasFileLoaded();
	}

	public boolean reloadFile()
	{
		if (this.editor.reloadFile())
		{
			this.showFileIcon();
			return true;
		}
		return false;
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
		return this.editor.canCloseFile();
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
			this.fileReloadAction.setEnabled(false);
      this.fireFilenameChanged(this.tabName);
			this.removeTabIcon();
			this.selectEditorLater();
			return true;
    }
		return false;
	}

	public void fileNameChanged(Object sender, String aNewName)
	{
		this.fireFilenameChanged(aNewName);
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
		this.executeAll = new ExecuteAllAction(this);
		this.executeSelected = new ExecuteSelAction(this);
		this.executeCurrent = new ExecuteCurrentAction(this);

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

		FileOpenAction open = new FileOpenAction(this);
		open.setCreateMenuSeparator(true);
		this.actions.add(open);
		this.actions.add(new FileSaveAction(this));
		this.actions.add(new FileSaveAsAction(this));
		this.fileDiscardAction = new FileDiscardAction(this);
		this.actions.add(this.fileDiscardAction);
		this.fileReloadAction = new FileReloadAction(this);
		this.actions.add(this.fileReloadAction);

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
		this.actions.add(this.editor.getMatchBracketAction());

		this.actions.add(this.data.getStartEditAction());
		this.actions.add(this.data.getSelectKeysAction());
		this.actions.add(this.data.getUpdateDatabaseAction());
		this.actions.add(this.data.getInsertRowAction());
		this.actions.add(this.data.getCopyRowAction());
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

		this.optimizeAllCol = new OptimizeAllColumnsAction(this.data.getTable());
		this.optimizeAllCol.setCreateMenuSeparator(true);
		this.optimizeAllCol.setEnabled(false);
		this.optimizeAllCol.putValue(Action.SMALL_ICON, null);
		this.optimizeAllCol.putValue(WbAction.MAIN_MENU_ITEM, ResourceMgr.MNU_TXT_VIEW);
		this.actions.add(this.optimizeAllCol);

		this.dataToClipboard = this.data.getTable().getDataToClipboardAction();
		this.dataToClipboard.setEnabled(false);
		this.actions.add(this.exportDataAction);
		this.actions.add(this.dataToClipboard);

		this.copyAsSqlInsert = this.data.getTable().getCopyAsInsertAction();
		this.actions.add(this.copyAsSqlInsert);

		this.copyAsSqlUpdate = this.data.getTable().getCopyAsUpdateAction();
		this.actions.add(this.copyAsSqlUpdate);

		copySelectedMenu = this.data.getTable().getCopySelectedMenu();
		this.actions.add(copySelectedMenu);

		this.importFileAction = new ImportFileAction(this);
		this.importFileAction.setCreateMenuSeparator(true);
		this.actions.add(this.importFileAction);

		this.printDataAction = this.data.getTable().getPrintAction();
		this.printPreviewAction = this.data.getTable().getPrintPreviewAction();

		this.actions.add(this.executeAll);
		this.actions.add(this.executeSelected);
		this.actions.add(this.executeCurrent);

		this.spoolData = new SpoolDataAction(this);
		this.spoolData.setCreateMenuSeparator(true);
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

		this.actions.add(new AutoJumpNextStatement());
		WbAction hi = new HighlightCurrentStatement();
		hi.setCreateMenuSeparator(false);
		this.actions.add(hi);

		this.executeAll.setEnabled(false);
		this.executeSelected.setEnabled(false);

		this.toolbarActions.add(this.executeSelected);
		this.toolbarActions.add(this.stopAction);
		this.toolbarActions.add(this.firstStmtAction);
		this.toolbarActions.add(this.prevStmtAction);
		this.toolbarActions.add(this.nextStmtAction);
		this.toolbarActions.add(this.lastStmtAction);

		this.toolbarActions.add(this.data.getUpdateDatabaseAction());
		this.toolbarActions.add(this.data.getStartEditAction());
		this.toolbarActions.add(this.data.getInsertRowAction());
		this.toolbarActions.add(this.data.getCopyRowAction());
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
			Object entry = this.actions.get(i);
			if (entry instanceof WbAction)
			{
				WbAction wb = (WbAction)entry;
				wb.addToInputMap(im, am);
			}
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
		if (this.isVisible() && this.isCurrentTab() && editor != null)
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
		// Make sure we have real PK columns.
		boolean needPk = this.data.getTable().getDataStore().needPkForUpdate();
		boolean hasPk = this.data.getTable().checkPkColumns(true);
		if (needPk && !hasPk) return;

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
		boolean wasUpdate = this.updateRunning;

		try
		{
			this.log.setText(ResourceMgr.getString("MsgUpdatingDatabase"));
			this.log.append("\n");
			int rows = this.data.saveChanges(this.dbConnection, this);
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
			WbSwingUtilities.showDefaultCursor(this);
		}

		this.enableExecuteActions();

		if (success)
		{
			this.clearStatusMessage();
			this.checkResultSetActions();
		}
		else if (!wasUpdate)
		{
			final String msg = errorMessage;
			// Make sure the error dialog is displayed on the AWT
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					WbSwingUtilities.showErrorMessage(getParentWindow(), msg);
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
		if (this.data == null) return;
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
		if (this.data == null) return;
		this.data.setStatusMessage(aMsg);
	}

	/**
	 *	Clear the message in the status bar of the DwPanel
	 */
	public void clearStatusMessage()
	{
		this.data.clearStatusMessage();
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
		this.editor.setText("");
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
		this.setId(index + 1);

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
			this.editor.initDatabaseKeywords(aConnection);
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
		//this.doLayout();
	}

	public boolean isRequestFocusEnabled() { return true; }
	//public boolean isFocusTraversable() { return true; }

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
							ds.resetStatusForSentRows();
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
				ds.resetStatusForSentRows();
			}
		}
		this.setCancelState(false);
	}

	public void forceAbort()
	{
		if (!this.isBusy()) return;
		if (this.executionThread == null) return;
		try
		{
			this.cancelExecution = true;
			this.executionThread.interrupt();
			this.executionThread = null;
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
		if (this.executionThread == null) return true;

		boolean success = false;
		int wait = Settings.getInstance().getIntProperty(this.getClass().getName(), "abortwait", 1);
		try
		{
			LogMgr.logDebug("SqlPanel.abortExecution()", "Interrupting SQL Thread...");
			this.executionThread.interrupt();
			this.executionThread.join(wait * 1000);
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

	public boolean confirmCancel()
	{
		return true;
	}

	/**
	 *	Implementation of the Interruptable Interface.
	 */
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
						cancelRetrieve();
					}
				};
				t.setDaemon(true);
				t.setName("Cancel Thread");
				t.start();
			}
		}
		catch (Throwable th)
		{
			LogMgr.logError("SqlPanel.cancelExecution()", "Error cancelling execution", th);
		}
	}

	private void cancelRetrieve()
	{
		this.showCancelIcon();
		this.cancelExecution = true;
		this.setCancelState(false);
		this.data.cancelExecution();
	}

	public void setCancelState(final boolean aFlag)
	{
		this.stopAction.setEnabled(aFlag);
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

	public void runCurrentStatement()
	{
		String sql = this.editor.getText();
		int caret = this.editor.getCaretPosition();
		startExecution(sql, 0, caret, true);
	}

	public void runSelectedStatement()
	{
		String sql = this.editor.getSelectedStatement();
		int offset = 0;
		if (this.editor.isTextSelected())
		{
			offset = this.editor.getSelectionStart();
		}
		this.startExecution(sql, offset, -1, true);
	}

	public void commit()
	{
		if (this.isBusy()) return;
		this.startExecution(SingleVerbCommand.COMMIT.getVerb(), 0, -1, false);
	}

	public void rollback()
	{
		if (this.isBusy()) return;
		this.startExecution(SingleVerbCommand.ROLLBACK.getVerb(), 0, -1, false);
	}

	public void runAll()
	{
		String sql = this.editor.getText();
		this.startExecution(sql, 0, -1, true);
	}

	private void startExecution(final String sql, final int offset, final int commandAtIndex, final boolean highlight)
	{
		if (this.isBusy()) return;

		this.executionThread = new Thread(new Runnable()
		{
			public void run()
			{
				runStatement(sql, offset, commandAtIndex, highlight);
			}
		});
		this.executionThread.setDaemon(true);
		this.executionThread.setName("SQL Execution Thread " + this.getId());
		this.executionThread.start();
	}

	/*
	 * 	Execute the given SQL string. This is invoked from the the run() and other
	 *  methods in order to execute the SQL command. It takes care of updating the
	 *  actions and the menu.
	 *  The actual execution and display of the result is handled by displayResult()
	 */
	private void runStatement(String sql, int selectionOffset, int commandAtIndex, boolean highlightOnError)
	{
		this.showStatusMessage(ResourceMgr.getString(ResourceMgr.MSG_EXEC_SQL));
		this.data.getStartEditAction().setSwitchedOn(false);

		this.storeStatementInHistory();

		// the dbStart should be fired *after* updating the
		// history, as the history might be saved ("AutoSaveHistory") if the MainWindow
		// receives the execStart event
		this.fireDbExecStart();

		this.cancelExecution = false;
		this.setBusy(true);
		this.setCancelState(true);
		this.makeReadOnly();
		this.data.setBatchUpdate(true);

		this.displayResult(sql, selectionOffset, commandAtIndex, highlightOnError);

		this.data.setBatchUpdate(false);

		this.setBusy(false);

		this.clearStatusMessage();
		this.setCancelState(false);
		this.checkResultSetActions();

		this.fireDbExecEnd();
		this.selectEditorLater();
		this.executionThread = null;
	}

	public void executeMacro(final String macroName, final boolean replaceText)
	{
		if (isBusy()) return;

		final String sql = MacroManager.getInstance().getMacroText(macroName);
		if (sql == null || sql.trim().length() == 0) return;

		if (replaceText)
		{
			this.storeStatementInHistory();
			this.editor.setText(sql);
			this.storeStatementInHistory();
		}
		this.startExecution(sql, 0, -1, replaceText);
	}

	public void spoolData()
	{
		String sql = SqlUtil.makeCleanSql(this.editor.getSelectedStatement(),false);

		DataExporter spooler = new DataExporter();
		spooler.executeStatement(this.getParentWindow(), this.dbConnection, sql);
	}

	private boolean importRunning = false;

	public int getActionOnError(int errorRow, String errorColumn, String data, String errorMessage)
	{
		if (this.importRunning)
		{
			return this.getImportErrorAction(errorRow, errorColumn, data, errorMessage);
		}
		else if (this.updateRunning)
		{
			return this.getUpdateErrorAction(errorRow, errorColumn, data, errorMessage);
		}
		return JobErrorHandler.JOB_ABORT;
	}

	/**
	 * 	We are implementing our own getUpdateErrorAction() because it's necessary to
	 *  turn off the loading indicator before displaying a message box.
	 * 	DwPanel's getUpdateErrorAction is called from here after turning off the loading indicator.
	 */
	public int getUpdateErrorAction(int errorRow, String errorColumn, String data, String errorMessage)
	{
		this.showBusyIcon(false);
		int choice = this.data.getActionOnError(errorRow, errorColumn, data, errorMessage);
		this.showBusyIcon(true);
		return choice;
	}

	public int getImportErrorAction(int errorRow, String errorColumn, String data, String errorMessage)
	{
		String msg = null;
		if (errorColumn != null)
		{
			msg = ResourceMgr.getString("ErrorColumnImportError");
			msg = msg.replaceAll("%row%", Integer.toString(errorRow));
			msg = msg.replaceAll("%column%", errorColumn);
			msg = msg.replaceAll("%data%", data);
		}
		else
		{
			msg = ResourceMgr.getString("ErrorRowImportError");
			msg = msg.replaceAll("%row%", Integer.toString(errorRow));
			msg = msg.replaceAll("%data%", data == null ? "(null)" : data.substring(0,40) + " ...");
		}

		this.showBusyIcon(false);
		int choice = WbSwingUtilities.getYesNoIgnoreAll(this, msg);
		int result = JobErrorHandler.JOB_ABORT;
		this.showBusyIcon(true);
		if (choice == JOptionPane.YES_OPTION)
		{
			result = JobErrorHandler.JOB_CONTINUE;
		}
		else if (choice == WbSwingUtilities.IGNORE_ALL)
		{
			result = JobErrorHandler.JOB_IGNORE_ALL;
		}
		return result;
	}

	public synchronized void importFile()
	{
		if (!this.data.startEdit()) return;

		WbTable table = this.data.getTable();
		if (table == null) return;
		final DataStoreTableModel model = (DataStoreTableModel)table.getModel();
		final DataStore ds = table.getDataStore();
		final String currentFormat = ds.getDefaultDateFormat();
		if (ds == null) return;
		String lastDir = Settings.getInstance().getLastImportDir();
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
			Settings.getInstance().setLastImportDir(lastDir);
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
				Thread importThread = new WbThread("DataImport")
				{
					public void run()
					{
						try
						{
							importRunning = true;
							ds.setProgressMonitor(data);
							model.importFile(fname, header, delimit, quote, SqlPanel.this);
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
		log.append(aString);
		log.setCaretPosition(log.getDocument().getLength());
	}

	private void displayResult(String script, int selectionOffset, int commandAtIndex, boolean highlightOnError)
	{
		if (script == null) return;

		boolean compressLog = Settings.getInstance().getConsolidateLogMsg();
		boolean jumpToNext = (commandAtIndex > -1 && Settings.getInstance().getAutoJumpNextStatement());
		boolean highlightCurrent = false;
		boolean restoreSelection = false;
		
		ScriptParser scriptParser = new ScriptParser();
		scriptParser.setAlternateDelimiter(Settings.getInstance().getAlternateDelimiter());
		int oldSelectionStart = -1;
		int oldSelectionEnd = -1;
		
		try
		{
			this.log.setText("");

			String cleanSql = SqlUtil.makeCleanSql(script, false);
			String macro = MacroManager.getInstance().getMacroText(cleanSql);
			if (macro != null)
			{
				appendToLog(ResourceMgr.getString("MsgExecutingMacro") + ": " + cleanSql + "\n");
				script = macro;
			}

			scriptParser.setScript(script);
			List sqls = scriptParser.getCommands();

			int commandWithError = -1;
			int startIndex = 0;
			int endIndex = sqls.size();
			int count = sqls.size();

			if (commandAtIndex > -1)
			{
				int pos = this.editor.getCaretPosition();
				count = 1;
				startIndex = scriptParser.getCommandIndexAtCursorPos(pos);
				endIndex = startIndex + 1;
			}

			String msg = ResourceMgr.getString("TxtScriptStatementFinished");
			msg = StringUtil.replace(msg, "%total%", Integer.toString(count));
			String currentMsg = null;

			boolean onErrorAsk = true;
			if (count == 1) compressLog = false;

			this.data.scriptStarting();
			this.showResultPanel();

			highlightCurrent = (count > 1 && Settings.getInstance().getHighlightCurrentStatement());
			
			if (highlightCurrent)
			{
				oldSelectionStart = this.editor.getSelectionStart();
				oldSelectionEnd = this.editor.getSelectionEnd();
				restoreSelection = true;
			}
			
			long startTime = System.currentTimeMillis();

			for (int i=startIndex; i < endIndex; i++)
			{
				StrBuffer logmsg = new StrBuffer();
				String sql = scriptParser.getCommand(i);

				// in case of a batch execution we need to make sure that
				// this thread can actually be interrupted!
				Thread.yield();
				if (cancelExecution) break;

				VariablePrompter prompter = new VariablePrompter(sql);
				boolean goOn = true;
				if (prompter.needsInput())
				{
					this.showBusyIcon(false);
					goOn = prompter.getPromptValues();
				}

				if (!goOn)
				{
					String cancelMsg = ResourceMgr.getString("MsgSqlCancelledDuringPrompt");
					cancelMsg = cancelMsg.replaceAll("%nr%", Integer.toString(i+1));
					this.appendToLog(cancelMsg);
					this.showLogPanel();
					continue;
				}
				this.showBusyIcon(true);

				StrBuffer header = new StrBuffer(80);
				header.append(ResourceMgr.getString("TxtPrintHeaderResultFrom"));
				header.append(sql);

				this.data.setPrintHeader(header.toString());
				if (highlightCurrent)
				{
					highlightStatement(scriptParser, i, selectionOffset);
				}
				this.data.runStatement(sql);

				if (!compressLog)
				{
					logmsg.append(this.data.getLastMessage());
					if (count > 1)
					{
						currentMsg = StringUtil.replace(msg, "%nr%", Integer.toString(i + 1));
						logmsg.append("\n(");
						logmsg.append(currentMsg);
						logmsg.append(")\n\n");
					}
					this.appendToLog(logmsg.toString());
				}
				if (count > 1 && currentMsg != null) this.showStatusMessage(currentMsg);

				if (i == 0 && !this.data.hasResultSet())
				{
					this.showLogPanel();
				}

				if (!this.data.wasSuccessful())
				{
					commandWithError = i;
				}

				if (count > 1 && !this.data.wasSuccessful() && onErrorAsk && (i < (count - 1)))
				{
					// the animated gif needs to be turned off when a
					// dialog is displayed, otherwise Swing uses too much CPU
					this.showBusyIcon(false);

					this.highlightError(scriptParser, commandWithError, selectionOffset);

					// force a refresh in order to display the selection
					this.repaint();
					Thread.yield();

					String question = ResourceMgr.getString("MsgScriptStatementError");
					question = StringUtil.replace(question, "%nr%", Integer.toString(i+1));
					question = StringUtil.replace(question, "%count%", Integer.toString(count));
					int choice = WbSwingUtilities.getYesNoIgnoreAll(this, question);
					this.showBusyIcon(true);

					if (choice == JOptionPane.NO_OPTION)
					{
						break;
					}
					if (choice == WbSwingUtilities.IGNORE_ALL)
					{
						onErrorAsk = false;
					}
				}
			} // end for loop

			if (commandWithError > -1 && highlightOnError)
			{
				restoreSelection = false;
				final ScriptParser p = scriptParser;
				final int command = commandWithError;
				final int offset = selectionOffset;

				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						highlightError(p, command, offset);
					}
				});
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
			if (restoreSelection && oldSelectionStart > -1 && oldSelectionEnd > -1)
			{
				final int selstart = oldSelectionStart;
				final int selend = oldSelectionEnd;
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						editor.select(selstart, selend);
					}
				});
			}
			if (commandWithError == -1 && jumpToNext)
			{
				int nextCommand = startIndex + 1;
				int startPos = scriptParser.getStartPosForCommand(nextCommand);
				startPos = scriptParser.findNextLineStart(startPos);
				if (startPos > -1)
				{
					this.editor.setCaretPosition(startPos);
				}
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

	private void highlightStatement(ScriptParser scriptParser, int command, int startOffset)
	{
		if (this.editor == null) return;
		int startPos = scriptParser.getStartPosForCommand(command) + startOffset;
		int endPos = scriptParser.getEndPosForCommand(command) + startOffset;
		int line = this.editor.getLineOfOffset(startPos);
		this.editor.scrollTo(line, 0);
		this.editor.selectStatement(startPos, endPos);
	}
	private void highlightError(ScriptParser scriptParser, int commandWithError, int startOffset)
	{
		if (this.editor == null) return;
		int startPos = scriptParser.getStartPosForCommand(commandWithError) + startOffset;
		int endPos = scriptParser.getEndPosForCommand(commandWithError) + startOffset;
		int line = this.editor.getLineOfOffset(startPos);
		this.editor.scrollTo(line, 0);
		this.editor.selectError(startPos, endPos);
	}

	public void generateDeleteScript()
	{
		WbTable table = this.data.getTable();
		if (table == null) return;
		try
		{
			DeleteScriptGenerator gen = new DeleteScriptGenerator(this.dbConnection);
			gen.setSource(table);
			gen.startGenerate();
		}
		catch (Exception e)
		{
			LogMgr.logError("SqlPanel.generateDeleteScript()", "Error initializing DeleteScriptGenerator", e);
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
		if (this.data == null) return;

		boolean hasResult = this.data.hasResultSet();
		this.setActionState(new Action[] {this.findDataAction, this.dataToClipboard, this.exportDataAction, this.optimizeAllCol}, hasResult);

		boolean mayEdit = hasResult && this.data.hasUpdateableColumns();
		this.data.getStartEditAction().setEnabled(mayEdit);
		int rows = this.data.getTable().getSelectedRowCount();
		this.createDeleteScript.setEnabled(mayEdit && rows == 1);

		this.data.getCopyRowAction().setEnabled(mayEdit && (rows == 1));
		this.data.getInsertRowAction().setEnabled(this.data.isUpdateable());

		boolean findNext = hasResult && (this.data.getTable() != null && this.data.getTable().canSearchAgain());
		this.findDataAgainAction.setEnabled(findNext);

		boolean canUpdate = this.data.isUpdateable();
		this.copyAsSqlInsert.setEnabled(canUpdate);
		this.copyAsSqlUpdate.setEnabled(canUpdate);
		this.importFileAction.setEnabled(canUpdate);
	}

	private ImageIcon getLoadingIndicator()
	{
		if (this.loadingIcon == null)
		{
			if (Settings.getInstance().getUseAnimatedIcon())
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
			if (Settings.getInstance().getUseAnimatedIcon())
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
		if (this.loadingIcon != null) this.loadingIcon.getImage().flush();
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

	private void showBusyIcon(boolean show)
	{
		Container parent = this.getParent();
		if (parent instanceof JTabbedPane)
		{
			final JTabbedPane tab = (JTabbedPane)parent;
			int index = tab.indexOfComponent(this);
			if (index >= 0 && index < tab.getTabCount())
			{
				try
				{
					if (show)
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
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						tab.repaint();
					}
				});
			}
		}
	}

	private synchronized void setBusy(final boolean busy)
	{
		if (busy == this.threadBusy) return;
		this.threadBusy = busy;
		this.setExecuteActionStates(!busy);
		this.showBusyIcon(busy);
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
		// Make sure the icon for the file is updated to reflect
		// the modidified status
		if (this.hasFileLoaded()) this.showFileIcon();
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
		Settings.getInstance().removeChangeLister(this);
		this.data.clearContent();
		this.data = null;
		this.editor.dispose();
		this.editor = null;
		this.abortExecution();
		this.executionThread = null;
	}

	public void propertyChange(PropertyChangeEvent evt)
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
		if (evt.getSource() == this.data && evt.getPropertyName().equals("updateTable"))
		{
			this.checkResultSetActions();
		}
	}

}