/*
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2016 Thomas Kellerer.
 *
 * Licensed under a modified Apache License, Version 2.0 (the "License")
 * that restricts the use for certain governments.
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.sql-workbench.net/manual/license.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * To contact the author please send an email to: support@sql-workbench.net
 */
package workbench.gui.profiles;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import workbench.interfaces.ValidatingComponent;
import workbench.resource.IconMgr;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;

import workbench.db.ConnectionMgr;
import workbench.db.ProfileManager;

import workbench.gui.WbSwingUtilities;
import workbench.gui.components.ExtensionFileFilter;

import workbench.util.CollectionUtil;
import workbench.util.WbFile;


/**
 *
 * @author Thomas Kellerer
 */
public class ProfileImporterPanel
  extends JPanel
  implements ActionListener, ValidatingComponent
{

  public ProfileImporterPanel()
  {
    initComponents();
    sourceFilename.setText(ResourceMgr.getString("LblNone"));
    Border b = new EmptyBorder(0,0,0,0);
    sourceFilename.setBorder(b);
    currentFilename.setBorder(b);

    sourceProfiles.setModel(ProfileListModel.emptyModel());
    openButton.setIcon(IconMgr.getInstance().getToolbarIcon("open"));
    openButton.addActionListener(this);
    currentInfopanel.setPreferredSize(sourceInfoPanel.getPreferredSize());
  }

  public void loadCurrentProfiles()
  {
    ProfileListModel model = new ProfileListModel(ConnectionMgr.getInstance().getProfiles());
    WbSwingUtilities.invoke(() ->
    {
      currentProfiles.setModel(model);
      currentFilename.setText(ConnectionMgr.getInstance().getProfilesPath());
    });
  }

  public void applyProfiles()
  {
    ProfileListModel list = (ProfileListModel)currentProfiles.getModel();
    list.applyProfiles();
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    String settingsKey = "workbench.gui.profileimporter.lastsourcedir";
    String dir = Settings.getInstance().getProperty(settingsKey, Settings.getInstance().getConfigDir().getAbsolutePath());

    JFileChooser chooser = new  JFileChooser(dir);
    FileFilter ff = chooser.getFileFilter();
    chooser.removeChoosableFileFilter(ff);

    chooser.addChoosableFileFilter(new ExtensionFileFilter("Profiles (*.xml, *.properties)", CollectionUtil.arrayList("xml", "properties"), true));
    chooser.setMultiSelectionEnabled(false);

    int choice = chooser.showOpenDialog(this);

    WbFile currentDir = new WbFile(chooser.getCurrentDirectory());
    Settings.getInstance().setProperty(settingsKey, currentDir.getFullPath());

    if (choice == JFileChooser.APPROVE_OPTION)
    {
      File selectedFile = chooser.getSelectedFile();
      if (selectedFile != null)
      {
        ProfileManager mgr = new ProfileManager(selectedFile.getAbsolutePath());
        mgr.load();
        ProfileListModel model = new ProfileListModel(mgr.getProfiles());
        sourceProfiles.setModel(model);
        sourceFilename.setText(mgr.getProfilesPath());
      }
    }
    currentInfopanel.setPreferredSize(sourceInfoPanel.getPreferredSize());
  }

  private void saveCurrent()
  {
    ProfileListModel model = currentProfiles.getModel();
    if (model.profilesAreModified() || model.groupsChanged())
    {
      currentProfiles.getModel().applyProfiles();
    }
  }

  private boolean saveSource()
  {
    ProfileListModel model = currentProfiles.getModel();
    try
    {
      if (model.profilesAreModified() || model.groupsChanged())
      {
        ProfileManager mgr = new ProfileManager(sourceFilename.getText());
        mgr.applyProfiles(sourceProfiles.getModel().getAllProfiles());
        mgr.save();
      }
      return true;
    }
    catch (Exception ex)
    {
      return false;
    }
  }
  
  @Override
  public boolean validateInput()
  {
    saveCurrent();
    saveSource();
    return true;
  }

  @Override
  public void componentDisplayed()
  {
    loadCurrentProfiles();
    sourceProfiles.setPopupEnabled(false);
    currentProfiles.setPopupEnabled(false);
  }

  @Override
  public void componentWillBeClosed()
  {
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {
    java.awt.GridBagConstraints gridBagConstraints;

    sourcePanel = new javax.swing.JPanel();
    sourceInfoPanel = new javax.swing.JPanel();
    openButton = new javax.swing.JButton();
    sourceFilename = new workbench.gui.components.WbLabelField();
    jScrollPane3 = new javax.swing.JScrollPane();
    sourceProfiles = new workbench.gui.profiles.ProfileTree();
    currentPanel = new javax.swing.JPanel();
    currentInfopanel = new javax.swing.JPanel();
    currentFilename = new workbench.gui.components.WbLabelField();
    jScrollPane4 = new javax.swing.JScrollPane();
    currentProfiles = new workbench.gui.profiles.ProfileTree();

    setLayout(new java.awt.GridBagLayout());

    sourcePanel.setLayout(new java.awt.GridBagLayout());

    sourceInfoPanel.setLayout(new java.awt.GridBagLayout());

    openButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/workbench/resource/images/open16.png"))); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
    sourceInfoPanel.add(openButton, gridBagConstraints);

    sourceFilename.setText("wbLabelField2");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
    sourceInfoPanel.add(sourceFilename, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
    sourcePanel.add(sourceInfoPanel, gridBagConstraints);

    sourceProfiles.setName("sourceProfiles"); // NOI18N
    jScrollPane3.setViewportView(sourceProfiles);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    sourcePanel.add(jScrollPane3, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 5);
    add(sourcePanel, gridBagConstraints);

    currentPanel.setLayout(new java.awt.GridBagLayout());

    currentInfopanel.setLayout(new java.awt.GridBagLayout());

    currentFilename.setText("wbLabelField1");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
    currentInfopanel.add(currentFilename, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
    currentPanel.add(currentInfopanel, gridBagConstraints);

    currentProfiles.setName("currentProfiles"); // NOI18N
    jScrollPane4.setViewportView(currentProfiles);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    currentPanel.add(jScrollPane4, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 5);
    add(currentPanel, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private workbench.gui.components.WbLabelField currentFilename;
  private javax.swing.JPanel currentInfopanel;
  private javax.swing.JPanel currentPanel;
  private workbench.gui.profiles.ProfileTree currentProfiles;
  private javax.swing.JScrollPane jScrollPane3;
  private javax.swing.JScrollPane jScrollPane4;
  private javax.swing.JButton openButton;
  private workbench.gui.components.WbLabelField sourceFilename;
  private javax.swing.JPanel sourceInfoPanel;
  private javax.swing.JPanel sourcePanel;
  private workbench.gui.profiles.ProfileTree sourceProfiles;
  // End of variables declaration//GEN-END:variables
}
