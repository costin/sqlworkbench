/*
 * ProfileTree.java
 *
 * This file is part of SQL Workbench/J, https://www.sql-workbench.eu
 *
 * Copyright 2002-2019, Thomas Kellerer
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
package workbench.gui.profiles;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import workbench.interfaces.ClipboardSupport;
import workbench.interfaces.ExpandableTree;
import workbench.interfaces.FileActions;
import workbench.interfaces.GroupTree;
import workbench.log.CallerInfo;
import workbench.log.LogMgr;
import workbench.resource.IconMgr;
import workbench.resource.ResourceMgr;

import workbench.db.ConnectionProfile;

import workbench.gui.WbSwingUtilities;
import workbench.gui.actions.DeleteListEntryAction;
import workbench.gui.actions.WbAction;
import workbench.gui.components.ValidatingDialog;
import workbench.gui.menu.CutCopyPastePopup;

import workbench.util.CollectionUtil;
import workbench.util.StringUtil;

/**
 * A tree to display connection profiles and profile groups.
 *
 * It supports drag & drop from profiles into different groups.
 *
 * @author Thomas Kellerer
 */
public class ProfileTree
	extends JTree
	implements TreeModelListener, MouseListener, ClipboardSupport, ActionListener, TreeSelectionListener,
						 GroupTree, ExpandableTree
{
	private ProfileListModel profileModel;
	private CutCopyPastePopup popup;
	private WbAction pasteToFolderAction;
  private WbAction renameGroup;
	private Insets autoscrollInsets = new Insets(20, 20, 20, 20);
  private boolean allowDirectChange = true;
  private ProfileTreeTransferHandler transferHandler = new ProfileTreeTransferHandler(this);
  private NewGroupAction newGroupAction;
  private DeleteListEntryAction deleteAction;

	public ProfileTree()
	{
		super(ProfileListModel.getDummyModel());
		setRootVisible(false);
		putClientProperty("JTree.lineStyle", "Angled");
		setShowsRootHandles(true);
		setEditable(true);
		setExpandsSelectedPaths(true);
		addMouseListener(this);
		getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		addTreeSelectionListener(this);

		InputMap im = this.getInputMap(WHEN_FOCUSED);
		ActionMap am = this.getActionMap();

    popup = new CutCopyPastePopup(this);

    newGroupAction = new NewGroupAction(this, "LblNewProfileGroup");
    deleteAction = new DeleteListEntryAction(new FileActions()
    {
      @Override
      public void saveItem()
        throws Exception
      {
      }

      @Override
      public void deleteItem()
        throws Exception
      {
        deleteSelectedItem();
      }

      @Override
      public void newItem(boolean copyCurrent)
        throws Exception
      {
      }
    });

    WbAction a = popup.getPasteAction();
    a.addToInputMap(im, am);

    a = popup.getCopyAction();
    a.addToInputMap(im, am);

    a = popup.getCutAction();
    a.addToInputMap(im, am);

    pasteToFolderAction = new WbAction(this, "pasteToFolder");
    pasteToFolderAction.removeIcon();
    pasteToFolderAction.initMenuDefinition("MnuTxtPasteNewFolder");

    popup.addAction(newGroupAction, true);
    popup.addAction(pasteToFolderAction, false);
    renameGroup = new RenameGroupAction(this);
    popup.addAction(renameGroup, false);

    popup.addAction(deleteAction, true);
    deleteAction.addToInputMap(im, am);

    setupIcons();
		setAutoscrolls(true);
    setDragEnabled(true);
    setDropMode(DropMode.ON);
    setTransferHandler(transferHandler);

		// setting the row height to 0 makes it dynamic
		// so it will adjust properly to the font of the renderer
		setRowHeight(0);
    setBorder(WbSwingUtilities.EMPTY_BORDER);
	}

  public DeleteListEntryAction getDeleteAction()
  {
    return deleteAction;
  }

  private void setupIcons()
  {
    // Use reflection to call the various setXXXIcon() methods.
    // not all look and feels use a DefaultTreeCellRenderer
    // but many non-standard one do still provided these methods
    TreeCellRenderer tcr = getCellRenderer();
    setIcon(tcr, "setLeafIcon", IconMgr.getInstance().getLabelIcon("profile"));
    setIcon(tcr, "setOpenIcon", IconMgr.getInstance().getLabelIcon("folder-open"));
    setIcon(tcr, "setClosedIcon", IconMgr.getInstance().getLabelIcon("folder"));
  }

  private void setIcon(TreeCellRenderer renderer, String setter, Icon toSet)
  {
    try
    {
      Method setIcon = renderer.getClass().getMethod(setter, Icon.class);
      if (setIcon != null)
      {
        setIcon.invoke(renderer, toSet);
      }
    }
    catch (Throwable th)
    {
    }
  }

	public void deleteSelectedItem()
		throws Exception
	{
		TreePath[] path = getSelectionPaths();
		if (path == null) return;
		if (path.length == 0)	return;

		if (onlyProfilesSelected())
		{
			DefaultMutableTreeNode group = (DefaultMutableTreeNode)path[0].getPathComponent(1);
			DefaultMutableTreeNode firstNode = (DefaultMutableTreeNode)path[0].getLastPathComponent();
			int newIndex = getModel().getIndexOfChild(group, firstNode);
			if (newIndex > 0)
			{
				newIndex--;
			}

			for (TreePath element : path)
			{
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) element.getLastPathComponent();
				ConnectionProfile prof = (ConnectionProfile)node.getUserObject();
				getModel().deleteProfile(prof);
			}
			if (group.getChildCount() > 0)
			{
				Object newChild = getModel().getChild(group, newIndex);
				TreePath newPath = new TreePath(new Object[]{getModel().getRoot(), group, newChild});
				selectPath(newPath);
			}
		}
		else // delete a group
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)path[0].getLastPathComponent();
			if (node.getChildCount() > 0)
			{
				if (!checkGroupWithProfiles(node))
				{
					return;
				}
			}
			getModel().removeGroupNode(node);
		}
	}

	private boolean checkGroupWithProfiles(DefaultMutableTreeNode groupNode)
	{
		List<String> groups = getModel().getGroups();
		JPanel p = new JPanel();
		DefaultComboBoxModel m = new DefaultComboBoxModel(groups.toArray());
		JComboBox groupBox = new JComboBox(m);
		groupBox.setSelectedIndex(0);
		p.setLayout(new BorderLayout(0, 5));
		String groupName = (String)groupNode.getUserObject();
		String lbl = ResourceMgr.getFormattedString("LblDeleteNonEmptyGroup", groupName);
		p.add(new JLabel(lbl), BorderLayout.NORTH);
		p.add(groupBox, BorderLayout.SOUTH);
		String[] options = new String[]{ResourceMgr.getString("LblMoveProfiles"), ResourceMgr.getString("LblDeleteProfiles")};

		Dialog parent = (Dialog)SwingUtilities.getWindowAncestor(this);

		ValidatingDialog dialog = new ValidatingDialog(parent, ResourceMgr.TXT_PRODUCT_NAME, p, options);
		WbSwingUtilities.center(dialog, parent);
		dialog.setVisible(true);
		if (dialog.isCancelled())
		{
			return false;
		}

		int result = dialog.getSelectedOption();
		if (result == 0)
		{
			// move profiles
			String group = (String)groupBox.getSelectedItem();
			if (group == null)
			{
				return false;
			}

			getModel().moveProfilesToGroup(groupNode, group);
			return true;
		}
		else if (result == 1)
		{
			return true;
		}

		return false;
	}

	@Override
	public void setModel(TreeModel model)
	{
		super.setModel(model);
		if (model instanceof ProfileListModel)
		{
			this.profileModel = (ProfileListModel)model;
      model.addTreeModelListener(this);
		}
	}

  @Override
  public ProfileListModel getModel()
  {
    return profileModel;
  }

	@Override
	public boolean isPathEditable(TreePath path)
	{
		if (path == null) return false;
		// Only allow editing of groups
		if (path.getPathCount() != 2) return false;

		DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();

		return node.getAllowsChildren();
	}

	@Override
	public void treeNodesChanged(TreeModelEvent e)
	{
		Object[] changed = e.getChildren();
		DefaultMutableTreeNode group = (DefaultMutableTreeNode)changed[0];
		Object data = group.getUserObject();

		if (group.getAllowsChildren())
		{
			String newGroupName = (String)data;
			renameGroup(group, newGroupName);
		}
		else if (data instanceof ConnectionProfile)
		{
			// If the connection profile has changed, the title
			// of the profile possibly changed as well, so we need to
			// trigger a repaint to display the correct title
			// in the tree
			WbSwingUtilities.repaintLater(this);
		}
	}

	@Override
	public void expandAll()
	{
		TreePath[] groups = this.profileModel.getGroupNodes();
		for (TreePath group : groups)
		{
			if (group != null)
			{
				expandPath(group);
			}
		}
	}

	@Override
	public void collapseAll()
	{
		TreePath[] groups = this.profileModel.getGroupNodes();
		for (TreePath group : groups)
		{
			if (group != null)
			{
				collapsePath(group);
			}
		}
	}

	/**
	 * Expand the groups that are contained in th list.
	 * The list is expected to contain Sting objects that identify
	 * the names of the groups.
	 */
	public void expandGroups(List groupList)
	{
		if (groupList == null) return;
		TreePath[] groupNodes = this.profileModel.getGroupNodes();
		if (groupNodes == null) return;
		for (TreePath groupNode : groupNodes)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) groupNode.getLastPathComponent();
			String g = (String)node.getUserObject();
			if (groupList.contains(g))
			{
				if (!isExpanded(groupNode))
				{
					expandPath(groupNode);
				}
			}
		}
	}

	/**
	 * Return the names of the expaned groups.
	 */
	public List<String> getExpandedGroupNames()
	{
		ArrayList<String> result = new ArrayList<>();
		TreePath[] groupNodes = this.profileModel.getGroupNodes();
		for (TreePath groupNode : groupNodes)
		{
			if (isExpanded(groupNode))
			{
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) groupNode.getLastPathComponent();
				String g = (String)node.getUserObject();
				result.add(g);
			}
		}
		return result;
	}

	@Override
	public void treeNodesInserted(TreeModelEvent e)
	{
	}

	@Override
	public void treeNodesRemoved(TreeModelEvent e)
	{
	}

	@Override
	public void treeStructureChanged(TreeModelEvent e)
	{
	}

	public boolean isGroup(TreePath p)
	{
		if (p == null) return false;
		TreeNode n = (TreeNode)p.getLastPathComponent();
		return n.getAllowsChildren();
	}

  private boolean canPaste()
  {
    // On some Linux distributions isDataFlavorAvailable() throws an exception
    // ignoring that exception is a workaround for that.
    try
    {
      return getToolkit().getSystemClipboard().isDataFlavorAvailable(ProfileFlavor.FLAVOR);
    }
    catch (Throwable th)
    {
      LogMgr.logDebug(new CallerInfo(){}, "Could not check clipboard", th);
      return false;
    }
  }

	/**
	 * Enable/disable the cut/copy/paste actions
	 * according to the current selection and the content
	 * of the "clipboard"
	 */
	private void checkActions()
	{
		boolean groupSelected = onlyGroupSelected();
    boolean canPaste = canPaste();
		boolean canCopy = onlyProfilesSelected();

		pasteToFolderAction.setEnabled(canPaste);

		WbAction a = popup.getPasteAction();
		a.setEnabled(allowDirectChange && canPaste);

		a = popup.getCopyAction();
		a.setEnabled(allowDirectChange && canCopy);

		a = popup.getCutAction();
		a.setEnabled(allowDirectChange && canCopy);

    renameGroup.setEnabled(allowDirectChange && groupSelected);
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
    if (!allowDirectChange) return;

		if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() == 1)
		{
			TreePath p = this.getClosestPathForLocation(e.getX(), e.getY());
			if (p == null) return;

			if (this.getSelectionCount() == 1 || isGroup(p))
			{
				setSelectionPath(p);
			}
			checkActions();
			popup.show(this, e.getX(), e.getY());
		}
	}

	/**
	 * Finds and selects the connection profile with the given name.
	 *
	 * If the profile is not found, the first profile
	 * will be selected and its group expanded
	 */
	public void selectProfile(ProfileKey key)
	{
		selectProfile(key, true);
	}

	public void selectProfile(ProfileKey key, boolean selectFirst)
	{
		if (profileModel == null) return;
		TreePath path = this.profileModel.getPath(key);
		if (path == null && selectFirst)
		{
			path = this.profileModel.getFirstProfile();
		}
		selectPath(path); // selectPath can handle a null value
	}

  public void selectFirstProfile()
  {
    if (profileModel == null) return;
    selectPath(profileModel.getFirstProfile());
  }

	/**
	 * Checks if the current selection contains only profiles
	 */
	public boolean onlyProfilesSelected()
	{
		TreePath[] selection = getSelectionPaths();
		if (selection == null) return false;

		for (TreePath element : selection)
		{
			TreeNode n = (TreeNode)element.getLastPathComponent();
			if (n.getAllowsChildren()) return false;
		}
		return true;
	}

	/**
	 * Checks if the current selection contains only groups
	 */
	public boolean onlyGroupSelected()
	{
		if (getSelectionCount() > 1) return false;
		TreePath[] selection = getSelectionPaths();
		if (selection == null) return false;
		for (TreePath element : selection)
		{
			TreeNode n = (TreeNode) element.getLastPathComponent();
			if (!n.getAllowsChildren()) return false;
		}
		return true;
	}

	protected DefaultMutableTreeNode getSelectedGroupNode()
	{
		TreePath[] selection = getSelectionPaths();
		if (selection == null) return null;
		if (selection.length != 1) return null;

		DefaultMutableTreeNode node = (DefaultMutableTreeNode)getLastSelectedPathComponent();
		if (node != null && node.getAllowsChildren()) return node;
		return null;
	}

	/**
	 * Checks if the current selection contains only profiles
	 */
	public List<ConnectionProfile> getSelectedProfiles()
	{
		TreePath[] selection = getSelectionPaths();
		if (selection == null) return Collections.emptyList();

    List<ConnectionProfile> result = new ArrayList<>(selection.length);

		for (TreePath element : selection)
		{
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)element.getLastPathComponent();
      if (!node.getAllowsChildren())
      {
        result.add((ConnectionProfile)node.getUserObject());
      }
		}
		return result;
	}

	/**
	 * Returns the currently selected Profile. If either more then one
	 * entry is selected or a group is selected, null is returned
	 *
	 * @return the selected profile if any
	 */
	public ConnectionProfile getSelectedProfile()
	{
		TreePath[] selection = getSelectionPaths();
		if (selection == null) return null;
		if (selection.length != 1) return null;

		DefaultMutableTreeNode node = (DefaultMutableTreeNode)getLastSelectedPathComponent();
		if (node == null) return null;

		Object o = node.getUserObject();
		if (o instanceof ConnectionProfile)
		{
			ConnectionProfile prof = (ConnectionProfile)o;
			return prof;
		}
		return null;
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
	}

	@Override
	public void copy()
	{
    try
    {
      transferHandler.exportToClipboard(this, getToolkit().getSystemClipboard(), DnDConstants.ACTION_COPY);
    }
    catch (Throwable th)
    {
      LogMgr.logError(new CallerInfo(){}, "Could not put Profile to clipboard", th);
    }
	}

	@Override
	public void selectAll()
	{
	}

	@Override
	public void clear()
	{
	}

	@Override
	public void cut()
	{
    try
    {
      transferHandler.exportToClipboard(this, getToolkit().getSystemClipboard(), DnDConstants.ACTION_MOVE);
    }
    catch (Throwable th)
    {
      LogMgr.logError(new CallerInfo(){}, "Could not put Profile to clipboard", th);
    }
	}

	@Override
	public void paste()
	{
    try
    {
      Clipboard clipboard = getToolkit().getSystemClipboard();
      Transferable contents = clipboard.getContents(this);
      transferHandler.importData(new TransferHandler.TransferSupport(this, contents));
    }
    catch (Throwable ex)
    {
      LogMgr.logError(new CallerInfo(){}, "Could not access clipboard", ex);
    }
	}

	public void handleDroppedNodes(List<ConnectionProfile> profiles, DefaultMutableTreeNode newParent, int action)
	{
    if (CollectionUtil.isEmpty(profiles)) return;
		if (newParent == null) return;

    DefaultMutableTreeNode firstNode = null;
		if (action == DnDConstants.ACTION_MOVE)
		{
			firstNode = profileModel.moveProfilesToGroup(profiles, newParent);
		}
		else if (action == DnDConstants.ACTION_COPY)
		{
			firstNode = profileModel.copyProfilesToGroup(profiles, newParent);
		}
		selectNode(firstNode);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		// invoked from the "paste into new folder" action
		String group = addGroup();
		if (group != null)
		{
			paste();
		}
	}

	/**
	 * Prompts the user for a new group name and renames the currently selected group
	 * to the supplied name.
	 */
	public void renameGroup()
	{
		DefaultMutableTreeNode group = this.getSelectedGroupNode();
		if (group == null) return;
		String oldName = (String)group.getUserObject();
		String newName = WbSwingUtilities.getUserInput(SwingUtilities.getWindowAncestor(this), ResourceMgr.getString("LblRenameProfileGroup"), oldName);
		if (StringUtil.isEmptyString(newName)) return;
		group.setUserObject(newName);
		renameGroup(group, newName);
	}

	private void renameGroup(DefaultMutableTreeNode group, String newGroupName)
	{
		if (StringUtil.isEmptyString(newGroupName)) return;
		int count = profileModel.getChildCount(group);
		for (int i = 0; i < count; i++)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)profileModel.getChild(group,i);
			ConnectionProfile prof = (ConnectionProfile)node.getUserObject();
			prof.setGroup(newGroupName);
		}
	}

	/**
	 * Prompts the user for a group name and creates a new group
	 * with the provided name. The new group node is automatically
	 * after creation.
	 * @return the name of the new group or null if the user cancelled the name input
	 */
	@Override
	public String addGroup()
	{
		String group = WbSwingUtilities.getUserInput(SwingUtilities.getWindowAncestor(this), ResourceMgr.getString("LblNewProfileGroup"), "");
		if (StringUtil.isEmptyString(group)) return null;
		List groups = this.profileModel.getGroups();
		if (groups.contains(group))
		{
			WbSwingUtilities.showErrorMessageKey(SwingUtilities.getWindowAncestor(this), "ErrGroupNotUnique");
			return null;
		}
		TreePath path = this.profileModel.addGroup(group);
		selectPath(path);
		return group;
	}

	public void selectPath(TreePath path)
	{
		if (path == null) return;
		expandPath(path);
		setSelectionPath(path);
		scrollPathToVisible(path);
	}

	private void selectNode(DefaultMutableTreeNode node)
	{
    if (node == null) return;
		TreeNode[] nodes = this.profileModel.getPathToRoot(node);
		TreePath path = new TreePath(nodes);
		this.selectPath(path);
	}

	@Override
	public void valueChanged(TreeSelectionEvent e)
	{
		checkActions();
	}

	public void autoscroll(Point cursorLocation)
	{
		Rectangle outer = getVisibleRect();
		Rectangle inner = new Rectangle(
						outer.x + autoscrollInsets.left,
						outer.y + autoscrollInsets.top,
						outer.width - (autoscrollInsets.left + autoscrollInsets.right),
						outer.height - (autoscrollInsets.top+autoscrollInsets.bottom)
					);

		if (!inner.contains(cursorLocation))
		{
			Rectangle scrollRect = new Rectangle(
							cursorLocation.x - autoscrollInsets.left,
							cursorLocation.y - autoscrollInsets.top,
							autoscrollInsets.left + autoscrollInsets.right,
							autoscrollInsets.top + autoscrollInsets.bottom
						);
			scrollRectToVisible(scrollRect);
		}
	}

}
