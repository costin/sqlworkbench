/*
 * BlobHandler.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2007, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.components;

import java.awt.Dialog;
import java.awt.Frame;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.Blob;
import java.sql.SQLException;
import workbench.WbManager;
import workbench.gui.dialogs.BlobInfoDialog;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.storage.NullValue;
import workbench.util.EncodingUtil;
import workbench.util.FileUtil;

/**
 *
 * @author support@sql-workbench.net
 */
public class BlobHandler
{
	private File uploadFile;
	private boolean setToNull = false;
	
	public BlobHandler()
	{
	}
	
	public StringBuilder getByteDisplay(Object value)
	{
		long l = getBlobSize(value);
		return getByteDisplay(l);
	}

	public boolean setToNull() { return this.setToNull; }
	public File getUploadFile() 
	{
		return uploadFile;
	}
	
	public StringBuilder getByteDisplay(long l)
	{
		StringBuilder result = new StringBuilder(32);
		
		if (l < 1024) 
		{
			result.append(Long.toString(l));
			result.append(' ');
		}
		else if (l < 1024*1024)
		{
			result.append(Long.toString((long)l/1024));
			result.append(" K");
		}
		else 
		{
			result.append(Long.toString((long)l/(1024*1024)));
			result.append(" M");
		}
		result.append('B');
		return result;
	}
	
	public byte[] getBlobAsArray(Object value)
	{
		if (value == null) return null;
		if (value instanceof NullValue) return null;
		
		if (value instanceof Blob)
		{
			Blob blob = (Blob)value;
			try
			{
				byte[] buffer = blob.getBytes(1, (int)blob.length());
				return buffer;
			}
			catch (Exception e)
			{
				LogMgr.logError("BlobHandler.getBlobAsArray()", "Error retrieving blob value", e);
				return null;
			}
		}
		else if (value instanceof byte[])
		{
			return (byte[])value;
		}
		else if (value instanceof File)
		{
			InputStream in = null;
			try
			{
				File f = (File)value;
				in = new BufferedInputStream(new FileInputStream(f));
				byte[] buff = FileUtil.readBytes(in);
				return buff;
			}
			catch (Exception e)
			{
				LogMgr.logError("BlobHandler.getBlobAsArray()", "Error retrieving blob value", e);
				return null;
			}
			finally
			{
				try { in.close(); } catch (Throwable th) {}
			}
			
		}
		return null;
	}
	
	public long getBlobSize(Object value)
	{
		if (value == null) return 0;
		if (value instanceof NullValue)
		{
			return 0;
		}
		else if (value instanceof Blob)
		{
			Blob blob = (Blob)value;
			try
			{
				return blob.length();
			}
			catch (Exception e)
			{
				LogMgr.logError("BlobHandler.getBlobSize()", "Could not retrieve blob size",e);
			}
		}
		else if (value instanceof File)
		{
			// can happen if a file has been uploaded through the BlobInfoDialog
			return ((File)value).length();
		}
		else if (value instanceof byte[])
		{
			byte[] b = (byte[])value;
			return b.length;
		}
		return -1;
	}


	private String convertArray(byte[] value, String encoding)
	{
		String data = null;
		try
		{
			data = new String((byte[])value, encoding);
		}
		catch (UnsupportedEncodingException e)
		{
			LogMgr.logError("BlobHandler.convertArray()", "Could not convert binary to string using encoding: " + encoding, e);
			data = new String((byte[])value);
		}
		return data;
	}
	
	public String getBlobAsString(Object value, String encoding)
	{
		if (value == null) return null;
		if (value instanceof NullValue) return null;
		if (encoding == null) encoding = Settings.getInstance().getDefaultBlobTextEncoding();
		
		if (value instanceof Blob)
		{
			Blob blob = (Blob)value;
			try
			{
				byte[] buffer = blob.getBytes(1, (int)blob.length());
				return convertArray(buffer, encoding);
			}
			catch (Exception e)
			{
				LogMgr.logError("BlobHandler.getBlobAsString()", "Error retrieving blob value", e);
				return "";
			}
		}
		else if (value instanceof byte[])
		{
			return convertArray((byte[])value, encoding);
		}
		else if (value instanceof File)
		{
			Reader in = null;
			try
			{
				File f = (File)value;
				in = EncodingUtil.createReader(f, encoding);
				String s = FileUtil.readCharacters(in);
				return s;
			}
			catch (Exception e)
			{
				LogMgr.logError("BlobHandler.getBlobAsString()", "Error retrieving blob value", e);
				return "";
			}
			finally
			{
				try { in.close(); } catch (Throwable th) {}
			}
			
		}
		return value.toString();
	}
	
	public static long saveBlobToFile(Object data, String file)
		throws IOException, SQLException
	{
		OutputStream out = new FileOutputStream(file);
		return saveBlobToFile(data, out);
	}
	
	public static long saveBlobToFile(Object data, OutputStream out)
		throws IOException, SQLException
	{
		InputStream in = null;
		if (data instanceof java.sql.Blob)
		{
			java.sql.Blob blob = (java.sql.Blob)data;
			in = blob.getBinaryStream();
		}
		else if (data instanceof byte[])
		{
			in = new ByteArrayInputStream((byte[])data);
		}
		else if (data instanceof File)
		{
			in = new FileInputStream((File)data);
		}
		
		if (in == null) 
		{
			LogMgr.logError("WbTable.saveBlobContent", "No valid BLOB data found, got " + data.getClass().getName() + " instead", null);
			//WbSwingUtilities.showMessageKey(caller, "ErrBlobNoAvail");
			throw new IOException("No BLOB data object found");
		}
		return FileUtil.copy(in, out);
	}

	public void showBlobAsText(Object value)
	{
		showBlobAsText(null, value, null);
	}
	
	public void showBlobAsText(Dialog parent, Object value, String encoding)
	{
		String data = getBlobAsString(value, encoding);
		String title = ResourceMgr.getString("TxtBlobData");
		EditWindow w;
		if (parent != null)
		{
			w = new EditWindow(parent, title, data, false, true);
		}
		else
		{
			w = new EditWindow(WbManager.getInstance().getCurrentWindow(), title, data, false, true);
		}
		w.setReadOnly();
		w.setVisible(true);
		w.dispose();
	}

	public void showBlobInfoDialog(Frame parent, Object blobValue)
	{
		BlobInfoDialog d = new BlobInfoDialog(parent, true);
		d.setBlobValue(blobValue);
		d.setVisible(true);
		this.uploadFile = d.getUploadedFile();
		this.setToNull = d.setToNull();
		d.dispose();
	}
}