/*
 * WbFile.java
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
package workbench.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

import workbench.log.LogMgr;

/**
 * A wrapper around Java's File object to allow of automatic "expansion" of
 * system properties and other utility functions such as getFullPath() which
 * does not throw an exception
 *
 * @author Thomas Kellerer
 */
public class WbFile
  extends File
{

  /**
   * Create a new file object.
   *
   * Variables in the names are replaced with the value of the corresponding
   * system property (e.g. ${user.home})
   *
   * @param parent     the directory name
   * @param filename   the filename
   *
   * @see workbench.util.StringUtil#replaceProperties(java.lang.String)
   */
  public WbFile(String parent, String filename)
  {
    super(StringUtil.replaceProperties(parent), StringUtil.replaceProperties(filename));
  }

  /**
   * Create a new file object.
   *
   * Variables in the filename are replaced with the value of the corresponding
   * system property (e.g. ${user.home})
   *
   * @param parent     the directory
   * @param filename   the filename
   *
   * @see workbench.util.StringUtil#replaceProperties(java.lang.String)
   */
  public WbFile(File parent, String filename)
  {
    super(parent, StringUtil.replaceProperties(filename));
  }

  public WbFile(File f)
  {
    super(f.getAbsolutePath());
  }

  /**
   * Create a new file object.
   *
   * Variables in the filename are replaced with the value of the corresponding
   * system property (e.g. ${user.home})
   *
   * @param filename   the filename
   *
   * @see workbench.util.StringUtil#replaceProperties(java.lang.String)
   */
  public WbFile(String filename)
  {
    super(StringUtil.replaceProperties(filename));
  }

  /**
   * Creates a backup copy of this file.
   *
   * This file is copied to a new file while adding the current timestamp to the filename.
   */
  public WbFile makeBackup()
  {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
    String newname = this.getName() + "." + sdf.format(new java.util.Date());
    WbFile newfile = new WbFile(this.getParent(), newname);
    try
    {
      FileUtil.copy(this, newfile);
    }
    catch (Exception io)
    {
      LogMgr.logWarning("WbFile.makeBackup()", "Could not copy " + this.getFullPath() + " to " + newfile.getFullPath());
      return null;
    }
    return newfile;
  }

  /**
   * Returns the filename without an extension
   */
  public String getFileName()
  {
    String name = getName();
    int pos = name.lastIndexOf('.');
    if (pos == -1) return name;
    return name.substring(0, pos);
  }

  /**
   * Returns the extension of this file.
   * The extension is defined as the the characters after the last dot, but
   * excluding the dot.
   */
  public String getExtension()
  {
    String name = getName();
    int pos = name.lastIndexOf('.');
    if (pos == -1) return null;
    return name.substring(pos + 1);
  }

  /**
   * Tests if this file is writeable for the current user.
   * If it exists the result of this call is super.canWrite().
   *
   * If it does not exist, an attempt will be made to create
   * the file to ensure that it's writeabl.
   *
   * @see #canCreate()
   * @see #tryCreate()
   */
  public boolean isWriteable()
  {
    if (exists()) return canWrite();
    return canCreate();
  }

  /**
   * Checks if this file can be created
   * Note that canCreate() does <b>not</b> check if the file already
   * exists. <br/>
   *
   * <b>If the file already exists, it will be deleted!</b>
   *
   * This method calls tryCreate() and swallows any IOException
   *
   * @return true if the file can be created
   */
  public boolean canCreate()
  {
    try
    {
      tryCreate();
      return true;
    }
    catch (IOException e)
    {
      return false;
    }
  }

  /**
   * Tries to create this file.
   *
   * @throws java.io.IOException
   */
  public void tryCreate()
    throws IOException
  {
    FileOutputStream out = null;
    try
    {
      out = new FileOutputStream(this);
    }
    finally
    {
      FileUtil.closeQuietely(out);
      this.delete();
    }
  }

  /**
   * Returns the canoncial name for this file
   * @return the canonical filename or the absolute filename if getCanonicalPath threw an Exception
   */
  public String getFullPath()
  {
    try
    {
      return this.getCanonicalPath();
    }
    catch (Throwable th)
    {
      return this.getAbsolutePath();
    }
  }

  @Override
  public String toString()
  {
    return getFullPath();
  }

}
