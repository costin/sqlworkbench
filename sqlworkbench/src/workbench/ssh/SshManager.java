/*
 * This file is part of SQL Workbench/J, https://www.sql-workbench.eu
 *
 * Copyright 2002-2016 Thomas Kellerer.
 *
 * Licensed under a modified Apache License, Version 2.0 (the "License")
 * that restricts the use for certain governments.
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.sql-workbench.eu/manual/license.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * To contact the author please send an email to: support@sql-workbench.eu
 */
package workbench.ssh;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import workbench.log.CallerInfo;
import workbench.log.LogMgr;
import workbench.resource.Settings;

import workbench.db.ConnectionProfile;

import workbench.gui.profiles.ProfileKey;

import workbench.util.WbFile;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.KeyPair;
import com.jcraft.jsch.agentproxy.Connector;
import com.jcraft.jsch.agentproxy.ConnectorFactory;

/**
 *
 * @author Thomas Kellerer
 */
public class SshManager
{
  private final Object lock = new Object();
  private Map<SshConfig, Entry> activeSessions = new HashMap<>();
  private final Map<String, String> passphrases = new ConcurrentHashMap<>(2);

  public String initializeSSHSession(ConnectionProfile profile)
    throws SshException
  {
    return initializeSSHSession(profile.getSshConfig(), profile.getUrl(), profile.getKey());
  }
  
  public String initializeSSHSession(SshConfig config, String profileUrl, ProfileKey key)
    throws SshException
  {

    if (config == null) return profileUrl;

    if (Settings.getInstance().enableJSchLoggin())
    {
      JSch.setLogger(new JschLogger());
    }
    else
    {
      JSch.setLogger(null);
    }

    SshHostConfig hostConfig = config.getSshHostConfig();
    String li = new CallerInfo(){}.toString();

    try
    {
      LogMgr.logDebug(li, "SSH session required for profile: " + key);

      int localPort = config.getLocalPort();
      String urlToUse = profileUrl;
      UrlParser parser = new UrlParser(urlToUse);

      PortForwarder forwarder = getForwarder(config);
      if (forwarder.isConnected() == false)
      {
        localPort = forwarder.startForwarding(config.getDbHostname(), config.getDbPort(), localPort, hostConfig.getSshPort());

        // If the connection was successfull remember the passphrase for a private key file
        // so the user is not asked multiple times for the same keystore.
        if (hostConfig.getPrivateKeyFile() != null && hostConfig.hasTemporaryPassword())
        {
          passphrases.put(hostConfig.getPrivateKeyFile(), hostConfig.getPassword());
        }
      }
      else
      {
        LogMgr.logDebug(li, "Re-Using existing SSH session for " + forwarder);
        localPort = forwarder.getLocalPort();
      }

      if (config.getLocalPort() == 0)
      {
        urlToUse = parser.getLocalUrl(localPort);
      }
      return urlToUse;
    }
    catch (Throwable ex)
    {
      LogMgr.logError(li, "Could not initialize SSH tunnel", ex);
      throw new SshException("Could not initialize SSH tunnel: " + ex.getMessage(), ex);
    }
  }

  public boolean needsPassphrase(SshHostConfig config)
  {
    if (config == null) return false;

    String privateKeyFile = config.getPrivateKeyFile();
    if (privateKeyFile == null) return false;

    boolean doCheck = Settings.getInstance().getBoolProperty("workbench.ssh.check.encrypted.keyfile", true);
    if (doCheck == false) return true;

    WbFile keyFile = new WbFile(privateKeyFile);
    if (!keyFile.exists()) return false;

    String filePath = keyFile.getFullPath();

    KeyPair kpair = null;
    String li = new CallerInfo(){}.toString();

    try
    {
      long start = System.currentTimeMillis();
      JSch jsch = new JSch();
      kpair = KeyPair.load(jsch, filePath);
      boolean encrypted = kpair.isEncrypted();
      long duration = System.currentTimeMillis() - start;

      LogMgr.logDebug(li, "Checking for encrypted key file took: " + duration + "ms");

      if (!encrypted)
      {
        LogMgr.logInfo(li, "Key file " + filePath + " is not encrypted. Assuming no passphrase is required");
      }

      return encrypted;
    }
    catch (Throwable th)
    {
      return true;
    }
    finally
    {
      if (kpair != null)
      {
        kpair.dispose();
      }
    }
  }

  public int getLocalPort(SshConfig config)
  {
    if (config == null) return -1;
    if (config.getSshHostConfig() == null) return -1;
    PortForwarder forwarder = findForwarder(config);
    if (forwarder != null)
    {
      return forwarder.getLocalPort();
    }
    return -1;
  }

  public String getPassphrase(SshHostConfig config)
  {
    if (config.getPrivateKeyFile() == null) return null;
    return passphrases.get(config.getPrivateKeyFile());
  }

  private PortForwarder findForwarder(SshConfig config)
  {
    PortForwarder forwarder = null;
    synchronized (lock)
    {
      Entry e = activeSessions.get(config);
      if (e != null)
      {
        forwarder = e.fwd;
      }
    }
    return forwarder;
  }

  public PortForwarder getForwarder(SshConfig config)
  {
    PortForwarder forwarder = null;
    synchronized (lock)
    {
      Entry e = activeSessions.get(config);
      if (e == null)
      {
        e = new Entry(new PortForwarder(config.getSshHostConfig()));
        forwarder = e.fwd;
        e.usageCount = 1;
        activeSessions.put(config, e);
      }
      else
      {
        forwarder = e.fwd;
        e.usageCount ++;
      }
    }
    return forwarder;
  }

  public void decrementUsage(SshConfig config)
  {
    if (config == null) return;

    synchronized (lock)
    {
      Entry e = activeSessions.get(config);
      if (e != null)
      {
        e.usageCount --;
        if (e.usageCount == 0)
        {
          e.fwd.close();
          activeSessions.remove(config);
        }
      }
    }
  }

  public void disconnect(SshConfig config)
  {
    if (config == null) return;

    synchronized (lock)
    {
      Entry e = activeSessions.get(config);
      if (e != null)
      {
        e.fwd.close();
        activeSessions.remove(config);
      }
    }
  }

  public void disconnectAll()
  {
    synchronized (lock)
    {
      for (Entry e : activeSessions.values())
      {
        e.fwd.close();
      }
      activeSessions.clear();
    }
  }

  private static class Entry
  {
    final PortForwarder fwd;
    int usageCount;

    Entry(PortForwarder fwd)
    {
      this.fwd = fwd;
    }
  }

  public static boolean canUseAgent()
  {
    String li = new CallerInfo(){}.toString();
    long start = System.currentTimeMillis();

    boolean available = false;
    try
    {
      Connector connector = ConnectorFactory.getDefault().createConnector();
      if (connector != null)
      {
        LogMgr.logInfo(li, "SSH agent connector " + connector.getName() + " available: " + connector.isAvailable());
      }
      else
      {
        LogMgr.logInfo(li, "No agent connector available");
      }
      available = connector != null;
    }
    catch (Throwable th)
    {
      LogMgr.logWarning(li, "Can not create agent connector (" + th.getClass().getName() + " " + th.getMessage() + ")");
    }

    long duration = System.currentTimeMillis() - start;
    LogMgr.logDebug(li, "Checking for SSH agent connector took: " + duration + "ms");

    return available;
  }


}

