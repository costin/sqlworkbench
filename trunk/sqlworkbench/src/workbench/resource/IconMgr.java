/*
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2015 Thomas Kellerer.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * To contact the author please send an email to: support@sql-workbench.net
 */
package workbench.resource;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import workbench.log.LogMgr;

import workbench.gui.WbSwingUtilities;
import workbench.gui.lnf.LnFHelper;

/**
 *
 * @author Thomas Kellerer
 */
public class IconMgr
{
	public static final String IMG_SAVE = "save";

	private final RenderingHints scaleHint = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

	private final Map<String, ImageIcon> iconCache = new HashMap<>();
	private final String filepath;

 	protected static class LazyInstanceHolder
	{
		protected static final IconMgr instance = new IconMgr();
	}

	public static IconMgr getInstance()
	{
		return LazyInstanceHolder.instance;
	}

	private IconMgr()
	{
		filepath = ResourcePath.ICONS.getPath() + "/";
	}

	public ImageIcon getGifIcon(String baseName)
	{
		return getIcon(baseName, Settings.getInstance().getToolbarIconSize(), false);
	}

	public ImageIcon getPngIcon(String baseName)
	{
		return getIcon(baseName.toLowerCase(), Settings.getInstance().getToolbarIconSize(), true);
	}

	public ImageIcon getPicture(String filename)
	{
		return retrieveImage(filename);
	}

	public ImageIcon getPngIcon(String basename, int size)
	{
		return retrieveImage(basename + Integer.toString(size) + ".png");
	}

	public ImageIcon getToolbarIcon(String basename)
	{
		int size = Settings.getInstance().getToolbarIconSize();
		return getPngIcon(basename, size);
	}

	public int getSizeForMenuFont()
	{
		int fontHeight = LnFHelper.getMenuFontHeight();
		return getSizeForFont(fontHeight);
	}

	public int getSizeForLabel()
	{
		int fontHeight = LnFHelper.getLabelFontHeight();
		return getSizeForFont(fontHeight);
	}

	public int getSizeForFont(int fontHeight)
	{
		if (fontHeight < 24)
		{
			return 16;
		}
		else if (fontHeight < 32)
		{
			return 24;
		}
		return 32;
	}

	public int getSizeForComponentFont(JComponent comp)
	{
		int fontHeight = WbSwingUtilities.getFontHeight(comp);
		return getSizeForFont(fontHeight);
	}

	public ImageIcon getLabelIcon(String basename)
	{
		int imgSize = getSizeForLabel();
		return getIcon(basename.toLowerCase(), imgSize, true);
	}

	public ImageIcon getMenuGifIcon(String basename)
	{
		int imgSize = getSizeForMenuFont();
		return getIcon(basename, imgSize, false);
	}

	public ImageIcon getLabelGifIcon(String basename)
	{
		int imgSize = getSizeForLabel();
		return getIcon(basename, imgSize, false);
	}

	public ImageIcon getIcon(String baseName, int imageSize, boolean isPng)
	{
		String fname = makeFilename(baseName, imageSize, isPng);

		ImageIcon result = null;
		synchronized (this)
		{
			result = iconCache.get(fname);
			if (result == null)
			{
				result = retrieveImage(fname);
				if (result == null)
				{
					if (imageSize > 16)
					{
						LogMgr.logDebug("IconMgr.getIcon()", "Icon " + fname + " not found. Scaling existing 16px image");
						ImageIcon small = retrieveImage(makeBaseFilename(baseName, isPng));
						result = scale(small, imageSize);
					}
					else
					{
						result = retrieveImage("empty.gif");
					}
				}
				iconCache.put(fname, result);
			}
		}
		return result;
	}

	private String makeBaseFilename(String basename, boolean isPng)
	{
		return basename + (isPng ? "16.png" : "16.gif");
	}

	private String makeFilename(String basename, int size, boolean isPng)
	{
		String sz = Integer.toString(size);
		if (isPng) basename = basename.toLowerCase();
		return basename + (isPng ? sz + ".png" : sz + ".gif");
	}

	private ImageIcon retrieveImage(String fname)
	{
		ImageIcon result = null;
		URL imageIconUrl = getClass().getClassLoader().getResource(filepath + fname);
		if (imageIconUrl != null)
		{
			result = new ImageIcon(imageIconUrl);
		}
		return result;
	}

	private ImageIcon scale(ImageIcon original, int imageSize)
	{
		BufferedImage bi = new BufferedImage(imageSize, imageSize, BufferedImage.TRANSLUCENT);
		Graphics2D g2d = null;
		try
		{
			g2d = (Graphics2D) bi.createGraphics();
			g2d.addRenderingHints(scaleHint);
			g2d.drawImage(original.getImage(), 0, 0, imageSize, imageSize, null);
		}
		catch (Throwable th)
		{
			return new ImageIcon(original.getImage().getScaledInstance(imageSize, imageSize, Image.SCALE_SMOOTH));
		}
		finally
		{
			if (g2d != null)
			{
				g2d.dispose();
			}
		}
		return new ImageIcon(bi);
	}

}
