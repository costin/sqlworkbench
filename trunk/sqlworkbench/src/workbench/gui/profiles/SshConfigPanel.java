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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import workbench.resource.ResourceMgr;
import workbench.ssh.SshConfig;

import workbench.util.StringUtil;

/**
 *
 * @author Thomas Kellerer
 */
public class SshConfigPanel
  extends javax.swing.JPanel
{

  public SshConfigPanel()
  {
    initComponents();
  }

  public void setConfig(SshConfig config, boolean allowURLRewrite)
  {
    clear();
    if (config != null)
    {
      hostname.setText(StringUtil.coalesce(config.getHostname(), ""));
      username.setText(StringUtil.coalesce(config.getUsername(), ""));
      password.setText(StringUtil.coalesce(config.getPassword(), ""));
      int localPortNr = config.getLocalPort();
      if (localPortNr > 0)
      {
        localPort.setText(Integer.toString(localPortNr));
      }
      if (allowURLRewrite)
      {
        rewriteUrl.setSelected(config.getRewriteURL());
      }
      else
      {
        rewriteUrl.setEnabled(false);
        rewriteUrl.setSelected(false);
      }
    }
  }

  private void clear()
  {
    hostname.setText("");
    username.setText("");
    password.setText("");
    localPort.setText("");
    rewriteUrl.setSelected(false);
  }

  public SshConfig getConfig()
  {
    String host = hostname.getText();
    String user = username.getText();
    String port = localPort.getText();
    String pwd = password.getText();

    if (StringUtil.isBlank(host) && StringUtil.isBlank(user) && StringUtil.isBlank(port) && StringUtil.isBlank(pwd))
    {
      return null;
    }

    SshConfig config = new SshConfig();
    config.setHostname(host);
    config.setUsername(user);
    config.setPassword(pwd);
    config.setRewriteURL(rewriteUrl.isSelected());
    config.setLocalPort(StringUtil.getIntValue(port, 0));
    return config;
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
    GridBagConstraints gridBagConstraints;

    labelHost = new JLabel();
    hostname = new JTextField();
    labelUsername = new JLabel();
    username = new JTextField();
    labelPassword = new JLabel();
    password = new JPasswordField();
    rewriteUrl = new JCheckBox();
    labelLocalPort = new JLabel();
    localPort = new JTextField();

    setLayout(new GridBagLayout());

    labelHost.setText(ResourceMgr.getString("LblSshHost")); // NOI18N
    labelHost.setToolTipText(ResourceMgr.getString("d_LblSshHost")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.anchor = GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new Insets(5, 5, 0, 0);
    add(labelHost, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new Insets(5, 5, 0, 11);
    add(hostname, gridBagConstraints);

    labelUsername.setText(ResourceMgr.getString("LblSshUser")); // NOI18N
    labelUsername.setToolTipText(ResourceMgr.getString("d_LblSshUser")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new Insets(5, 5, 0, 0);
    add(labelUsername, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new Insets(5, 5, 0, 11);
    add(username, gridBagConstraints);

    labelPassword.setText(ResourceMgr.getString("LblSshPwd")); // NOI18N
    labelPassword.setToolTipText(ResourceMgr.getString("d_LblSshPwd")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new Insets(5, 5, 0, 0);
    add(labelPassword, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new Insets(5, 5, 0, 11);
    add(password, gridBagConstraints);

    rewriteUrl.setText(ResourceMgr.getString("LblSshRewriteUrl")); // NOI18N
    rewriteUrl.setToolTipText(ResourceMgr.getString("d_LblSshRewriteUrl")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new Insets(4, 1, 0, 0);
    add(rewriteUrl, gridBagConstraints);

    labelLocalPort.setText(ResourceMgr.getString("LblSshPort")); // NOI18N
    labelLocalPort.setToolTipText(ResourceMgr.getString("d_LblSshPort")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new Insets(5, 5, 0, 0);
    add(labelLocalPort, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new Insets(5, 5, 0, 11);
    add(localPort, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private JTextField hostname;
  private JLabel labelHost;
  private JLabel labelLocalPort;
  private JLabel labelPassword;
  private JLabel labelUsername;
  private JTextField localPort;
  private JPasswordField password;
  private JCheckBox rewriteUrl;
  private JTextField username;
  // End of variables declaration//GEN-END:variables
}
