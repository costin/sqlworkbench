/*
 * MainWindow.java
 *
 * Created on November 25, 2001, 3:10 PM
 */

package workbench.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.event.*;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import workbench.WbManager;
import workbench.db.ConnectionMgr;
import workbench.db.ConnectionProfile;
import workbench.db.WbConnection;
import workbench.gui.actions.FileConnectAction;
import workbench.gui.actions.FileExitAction;
import workbench.gui.actions.SelectTabAction;
import workbench.gui.actions.ShowDbExplorerAction;
import workbench.gui.actions.WbAction;
import workbench.gui.components.WbMenu;
import workbench.gui.components.WbMenuItem;
import workbench.gui.components.WbToolbar;
import workbench.gui.dbobjects.DbExplorerPanel;
import workbench.gui.dbobjects.DbExplorerWindow;
import workbench.gui.profiles.ProfileSelectionDialog;
import workbench.gui.settings.SettingsPanel;
import workbench.gui.sql.SqlPanel;
import workbench.interfaces.FilenameChangeListener;
import workbench.interfaces.MainPanel;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;


/**
 *
 * @author  workbench@kellerer.org
 * @version
 */
public class MainWindow 
	extends JFrame 
	implements ActionListener, WindowListener, ChangeListener, FilenameChangeListener
{
	private String windowId;
	private String currentProfileName;
	private WbConnection currentConnection;

	private DbExplorerPanel dbExplorerPanel;

	private JMenuBar currentMenu;
	private FileConnectAction connectAction;
	private ShowDbExplorerAction dbExplorerAction;

	private ProfileSelectionDialog profileDialog;
	private JTabbedPane sqlTab = new JTabbedPane();
	private WbToolbar currentToolbar;
	private ArrayList panelMenus = new ArrayList(5);
	private int tabCount = 0;

	/** Creates new MainWindow */
	public MainWindow()
	{
		super(ResourceMgr.TXT_PRODUCT_NAME);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.addWindowListener(this);
		this.sqlTab.setBorder(WbSwingUtilities.EMPTY_BORDER);

		this.tabCount = WbManager.getSettings().getDefaultTabCount();
		if (tabCount <= 0) tabCount = 1;

		for (int i=0; i < tabCount; i++)
		{
			SqlPanel sql = new SqlPanel(i + 1);
			sql.addFilenameChangeListener(this);
			this.sqlTab.addTab(ResourceMgr.getString("LabelTabStatement") + " " + Integer.toString(i+1), sql);
		}
		this.initMenu();

		this.getContentPane().add(this.sqlTab, BorderLayout.CENTER);
		this.setTitle(ResourceMgr.getString("MsgNotConnected"));
		this.sqlTab.setBorder(WbSwingUtilities.EMPTY_BORDER);
		this.restorePosition();
		this.setIconImage(ResourceMgr.getPicture("workbench16").getImage());

		if (WbManager.getSettings().getShowDbExplorerInMainWindow())
		{
			this.addDbExplorerTab();
		}

		int lastIndex = WbManager.getSettings().getLastSqlTab();
		if (lastIndex < 0 || lastIndex > this.sqlTab.getTabCount() - 1)
		{
			lastIndex = 0;
		}

		this.sqlTab.setSelectedIndex(lastIndex);
		this.tabSelected(lastIndex);

		// now that we have setup the SplitPane we can add the
		// change listener
		this.sqlTab.addChangeListener(this);
	}

	private void initMenu()
	{
		this.dbExplorerAction = new ShowDbExplorerAction(this);
		this.dbExplorerAction.setEnabled(false);

		for (int tab=0; tab < this.tabCount; tab ++)
		{
			MainPanel sql = (MainPanel)this.sqlTab.getComponentAt(tab);
			JMenuBar menuBar = this.getMenuForPanel(sql);
			this.panelMenus.add(menuBar);
		}
	}

	private JMenuBar getMenuForPanel(MainPanel aPanel)
	{
		HashMap menus = new HashMap(10);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.setBorderPainted(false);

		// Create the file menu for all tabs
		JMenu menu = new WbMenu(ResourceMgr.getString(ResourceMgr.MNU_TXT_FILE));
		menu.setName(ResourceMgr.MNU_TXT_FILE);
		menuBar.add(menu);
		menus.put(ResourceMgr.MNU_TXT_FILE, menu);
		
		WbAction action;
		JMenuItem item;

		this.connectAction = new FileConnectAction(this);
		item = this.connectAction.getMenuItem();
		menu.add(item);
		menu.addSeparator();

		// now create the menus for the current tab
		List actions = aPanel.getActions();

		// Create the menus in the correct order
		menu = new WbMenu(ResourceMgr.getString(ResourceMgr.MNU_TXT_EDIT));
		menu.setName(ResourceMgr.MNU_TXT_EDIT);
		menu.setVisible(false);
		menuBar.add(menu);
		menus.put(ResourceMgr.MNU_TXT_EDIT, menu);

		menu = new WbMenu(ResourceMgr.getString(ResourceMgr.MNU_TXT_VIEW));
		menu.setName(ResourceMgr.MNU_TXT_VIEW);
		menu.setVisible(false);
		menuBar.add(menu);
		menus.put(ResourceMgr.MNU_TXT_VIEW, menu);

		menu = new WbMenu(ResourceMgr.getString(ResourceMgr.MNU_TXT_DATA));
		menu.setName(ResourceMgr.MNU_TXT_DATA);
		menu.setVisible(false);
		menuBar.add(menu);
		menus.put(ResourceMgr.MNU_TXT_DATA, menu);

		menu = new WbMenu(ResourceMgr.getString(ResourceMgr.MNU_TXT_SQL));
		menu.setName(ResourceMgr.MNU_TXT_SQL);
		menu.setVisible(false);
		menuBar.add(menu);
		menus.put(ResourceMgr.MNU_TXT_SQL, menu);

		for (int i=0; i < actions.size(); i++)
		{
			action = (WbAction)actions.get(i);
			String menuName = (String)action.getValue(WbAction.MAIN_MENU_ITEM);
			if (menuName == null)
			{
				LogMgr.logWarning(this, "Action " + action.getClass() + " does not define a main menu entry!");
				continue;
			}
			menu = (JMenu)menus.get(menuName);
			if (menu == null)
			{
				menu = new WbMenu(ResourceMgr.getString(menuName));
				menuBar.add(menu);
				menus.put(menuName, menu);
			}
			boolean menuSep = "true".equals((String)action.getValue(WbAction.MENU_SEPARATOR));

			if (menuSep)
			{
				menu.addSeparator();
			}
			action.addToMenu(menu);
			menu.setVisible(true);
		}
		
		
		action = new FileExitAction();
		menu = (JMenu)menus.get(ResourceMgr.MNU_TXT_FILE);
		menu.addSeparator();
		menu.add(action.getMenuItem());

		// now put the tabs into the view menu
		menu = (JMenu)menus.get(ResourceMgr.MNU_TXT_VIEW);
		menu.setVisible(true);

		for (int i=0; i < this.tabCount; i ++)
		{
			action = new SelectTabAction(this.sqlTab, i);
			menu.add(action.getMenuItem());
		}

		menuBar.add(this.buildToolsMenu());
		menuBar.add(this.buildHelpMenu());

		if (!WbManager.getSettings().getShowDbExplorerInMainWindow())
		{
			WbToolbar tool = aPanel.getToolbar();
			aPanel.addToToolbar(this.dbExplorerAction, true);
		}
		return menuBar;
	}

	private MainPanel getCurrentPanel()
	{
		int index = this.sqlTab.getSelectedIndex();
		return this.getSqlPanel(index);
	}

	private MainPanel getSqlPanel(int anIndex)
	{
		return (MainPanel)this.sqlTab.getComponentAt(anIndex);
	}

	private void tabSelected(int anIndex)
	{
		Container content = this.getContentPane();
		MainPanel current = this.getCurrentPanel();

		JMenuBar menu = (JMenuBar)this.panelMenus.get(anIndex);
		this.setJMenuBar(menu);

		if (this.currentToolbar != null) content.remove(this.currentToolbar);
		if (current != null)
		{
			this.currentToolbar = current.getToolbar();
			content.add(this.currentToolbar, BorderLayout.NORTH);
		}
		this.doLayout();
	}

	public void restorePosition()
	{
		Settings s = WbManager.getSettings();

		if (!s.restoreWindowSize(this))
		{
			this.setSize(500,500);
		}

		if (!s.restoreWindowPosition(this))
		{
			WbSwingUtilities.center(this, null);
		}
	}

	public void saveSettings()
	{
		int index = this.sqlTab.getSelectedIndex();
		WbManager.getSettings().setLastSqlTab(index);
		for (int i=0; i < this.tabCount; i++)
		{
			MainPanel sql = (MainPanel)this.sqlTab.getComponentAt(i);
			sql.saveSettings();
		}
		WbManager.getSettings().storeWindowPosition(this);
		WbManager.getSettings().storeWindowSize(this);
		if (dbExplorerPanel != null && !WbManager.getSettings().getShowDbExplorerInMainWindow())
		{
			this.dbExplorerPanel.saveSettings();
		}
	}

	public void fileNameChanged(Object sender, String newFilename)
	{
		String fname;
		int index = -1;
		String tooltip = null;
		for (int i=0; i < this.sqlTab.getTabCount(); i++)
		{
			if (this.sqlTab.getComponentAt(i) == sender)
			{
				index = i;
				break;
			}
		}
		if (index == -1) return;
		
		if (newFilename == null) 
		{
			fname = ResourceMgr.getString("LabelTabStatement") + " " + (index + 1);
		}
		else
		{
			File f = new File(newFilename);
			fname = f.getName();
			tooltip = f.getAbsolutePath();
		}
		this.sqlTab.setTitleAt(index, fname);
		this.sqlTab.setToolTipTextAt(index, tooltip);
	}
	public void windowOpened(WindowEvent windowEvent)
	{
	}

	public void windowClosed(WindowEvent e)
	{
		WbManager.getInstance().exitWorkbench();
	}

	public void windowDeiconified(WindowEvent windowEvent)
	{
		if (this.dbExplorerPanel != null)
		{
			this.dbExplorerPanel.mainWindowDeiconified();
		}
	}

	public void windowClosing(WindowEvent windowEvent)
	{
		this.saveSettings();
	}

	public void windowDeactivated(WindowEvent windowEvent)
	{
	}

	public void windowActivated(WindowEvent windowEvent)
	{
	}

	public void windowIconified(WindowEvent windowEvent)
	{
		if (this.dbExplorerPanel != null)
		{
			this.dbExplorerPanel.mainWindowIconified();
		}
	}

	public void showStatusMessage(String aMsg)
	{
		MainPanel current = this.getCurrentPanel();
		if (current != null) current.showStatusMessage(aMsg);
	}

	public void showLogMessage(String aMsg)
	{
		MainPanel current = this.getCurrentPanel();
		if (current != null) current.showLogMessage(aMsg);
	}

	public void setConnection(WbConnection con)
	{
		for (int i=0; i < this.tabCount; i++)
		{
			MainPanel sql = (MainPanel)this.sqlTab.getComponentAt(i);
			sql.setConnection(con);
		}
		this.currentConnection = con;
		this.dbExplorerAction.setEnabled(true);

		if (this.dbExplorerPanel != null)
		{
			try
			{
				this.dbExplorerPanel.setConnection(con, this.currentProfileName);
			}
			catch (Exception e)
			{
				LogMgr.logError(this, "Could not set connection for DbExplorerWindow", e);
				this.dbExplorerPanel.disconnect();
				this.dbExplorerPanel = null;
			}
		}
	}

	public void selectConnection()
	{
		if (this.profileDialog == null)
		{
			this.profileDialog = new ProfileSelectionDialog(this, true);
		}
		WbSwingUtilities.center(this.profileDialog, this);
		this.profileDialog.setVisible(true);
		if (!this.profileDialog.isCancelled())
		{
			ConnectionProfile prof = this.profileDialog.getSelectedProfile();
			if (prof != null)
			{
				this.currentProfileName = prof.getName();
				this.connectTo(prof);
				WbManager.getSettings().setLastConnection(this.currentProfileName);
			}
		}
	}

	public JMenu getViewMenu(int anIndex)
	{
		return this.getMenu(ResourceMgr.MNU_TXT_VIEW, anIndex);
	}

	public JMenu getMenu(String aName, int anIndex)
	{
		JMenuBar menubar = (JMenuBar)this.panelMenus.get(anIndex);
		for (int k=0; k < menubar.getMenuCount(); k++)
		{
			JMenu item = menubar.getMenu(k);
			if (aName.equals(item.getName())) return item;
		}
		return null;
	}

	public void addToViewMenu(WbAction anAction)
	{
		for (int i=0; i < this.panelMenus.size(); i++)
		{
			JMenu view = this.getViewMenu(i);
			view.add(anAction.getMenuItem());
		}
	}

	public void addDbExplorerTab()
	{
		if (this.dbExplorerPanel == null)
		{
			this.dbExplorerPanel = new DbExplorerPanel();
		}
		JMenuBar dbmenu = this.getMenuForPanel(this.dbExplorerPanel);

		this.sqlTab.addTab(ResourceMgr.getString("LabelDbExplorer"), this.dbExplorerPanel);
		this.tabCount = this.sqlTab.getTabCount();

		WbAction action = new SelectTabAction(this.sqlTab, this.tabCount - 1, ResourceMgr.getString("MnuTxtShowDbExplorer"));

		// Add a second keystroke for the Db Explorer panel
		KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK);
		InputMap im = this.sqlTab.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap am = this.sqlTab.getActionMap();
		im.put(key, action.getActionName());
		am.put(action.getActionName(), action);

		this.panelMenus.add(dbmenu);
		this.addToViewMenu(action);
	}

	public void showDbExplorer()
	{
		if (this.dbExplorerPanel == null)
		{
			this.dbExplorerPanel = new DbExplorerPanel();
			this.dbExplorerPanel.setConnection(this.currentConnection);
		}
		if (WbManager.getSettings().getShowDbExplorerInMainWindow())
		{
			Component c = this.sqlTab.getComponentAt(this.tabCount - 1);
			if (!(c instanceof DbExplorerPanel))
			{
				this.addDbExplorerTab();
			}
			this.sqlTab.setSelectedIndex(this.tabCount - 1);
		}
		else
		{
			this.dbExplorerPanel.openWindow(this.currentProfileName);
		}
	}

	public String getCurrentProfileName()
	{
		return this.currentProfileName;
	}

	public void connectTo(ConnectionProfile aProfile)
	{
		try
		{
			this.getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			this.showStatusMessage(ResourceMgr.getString("MsgConnecting"));
			try
			{
				ConnectionMgr mgr = WbManager.getInstance().getConnectionMgr();
				WbConnection conn = mgr.getConnection(aProfile);
				this.setConnection(conn);
				this.setTitle(ResourceMgr.TXT_PRODUCT_NAME + " [" + aProfile.getName() + "]");
			}
			catch (ClassNotFoundException cnf)
			{
				this.showLogMessage(ResourceMgr.getString(ResourceMgr.ERR_DRIVER_NOT_FOUND));
			}
			catch (SQLException se)
			{
				this.showLogMessage(ResourceMgr.getString(ResourceMgr.ERR_CONNECTION_ERROR) + "\r\n\n" + se.toString());
			}
			this.showStatusMessage(null);
		}
		catch (Exception e)
		{
			this.showLogMessage("Could not connect\r\n" + e.getMessage());
		}
		this.getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	public JMenu buildHelpMenu()
	{
		JMenu result = new WbMenu(ResourceMgr.getString(ResourceMgr.MNU_TXT_HELP));
		result.setName(ResourceMgr.MNU_TXT_HELP);
		JMenuItem item = new WbMenuItem(ResourceMgr.getString("MnuTxtHelpContents"));
		item.putClientProperty("command", "helpContents");
		item.addActionListener(this);
		result.add(item);

		item = new WbMenuItem(ResourceMgr.getString("MnuTxtAbout"));
		item.putClientProperty("command", "helpAbout");
		item.addActionListener(this);
		result.add(item);
		return result;
	}

	public JMenu buildToolsMenu()
	{
		JMenu result = new WbMenu(ResourceMgr.getString(ResourceMgr.MNU_TXT_TOOLS));
		result.setName(ResourceMgr.MNU_TXT_TOOLS);

		if (!WbManager.getSettings().getShowDbExplorerInMainWindow())
		{
			result.add(this.dbExplorerAction);
			result.addSeparator();
		}

		JMenuItem options = new WbMenuItem(ResourceMgr.getString(ResourceMgr.MNU_TXT_OPTIONS));
		options.setName(ResourceMgr.MNU_TXT_OPTIONS);
		options.putClientProperty("command", "optionsDialog");
		options.addActionListener(this);
		result.add(options);
		JMenu lnf = new WbMenu(ResourceMgr.getString("MnuTxtLookAndFeel"));
		LookAndFeelInfo[] info = UIManager.getInstalledLookAndFeels();
		for (int i=0; i < info.length; i++)
		{
			JMenuItem item = new WbMenuItem(info[i].getName());
			item.putClientProperty("command", "lnf");
			item.putClientProperty("class", info[i].getClassName());
			item.addActionListener(this);
			lnf.add(item);
		}
		result.add(lnf);
		return result;
	}

	/**
	 *	Invoked when the a different SQL panel has been selected
	 *
	 * @param e  a ChangeEvent object
	 *
	 */
	public void stateChanged(ChangeEvent e)
	{
		if (e.getSource() == this.sqlTab)
		{
			int index = this.sqlTab.getSelectedIndex();
			this.tabSelected(index);
		}
	}


	public void listMenus()
	{
		for (int i=0; i < this.panelMenus.size(); i ++)
		{
			JMenuBar bar = (JMenuBar)this.panelMenus.get(i);
			System.out.print("Menu " + i  + "=");
			for (int k=0; k < bar.getComponentCount(); k++)
			{
				JMenuItem item = (JMenuItem)bar.getComponent(k);
				System.out.print("/" + item.getText());
			}
			System.out.println("");
		}
	}
	
	/**
	 *	Invoked when any of the main window menu commands are
	 *
	 */
	public void actionPerformed(ActionEvent e)
	{
		Object sender = e.getSource();
		if (sender instanceof JMenuItem)
		{
			JMenuItem item = (JMenuItem)sender;
			String command = (String)item.getClientProperty("command");
			if ("lnf".equals(command))
			{
				String className = (String)item.getClientProperty("class");
				try
				{
					WbManager.getSettings().setLookAndFeelClass(className);
					UIManager.setLookAndFeel(className);
					SwingUtilities.updateComponentTreeUI(this);
					if (this.dbExplorerPanel != null)
					{
						DbExplorerWindow win = this.dbExplorerPanel.getWindow();
						if (win != null)
						{
							SwingUtilities.updateComponentTreeUI(win);
						}
						else
						{
							//SwingUtilities.updateComponentTreeUI(this.dbExplorerPanel);
						}
					}
					for (int i=0; i < this.tabCount; i ++)
					{
						JMenuBar menu = (JMenuBar)this.panelMenus.get(i);
						SwingUtilities.updateComponentTreeUI(menu);
					}
				}
				catch (Exception ex)
				{
					LogMgr.logError(this, "Could not change look and feel", ex);
				}
			}
			else if ("optionsDialog".equals(command))
			{
				//JOptionPane.showMessageDialog(this, "Options are not yet implemented\r\n Please edit the file 'workbench.settings'");
				SettingsPanel panel = new SettingsPanel();
				panel.showSettingsDialog(this);
			}
			else if ("helpContents".equals(command))
			{
				JOptionPane.showMessageDialog(this, "Sorry! Help is not yet available");
			}
			else if ("helpAbout".equals(command))
			{
				WbAboutDialog about = new WbAboutDialog(this, true);
				WbSwingUtilities.center(about, this);
				about.show();
			}

		}
	}

}
