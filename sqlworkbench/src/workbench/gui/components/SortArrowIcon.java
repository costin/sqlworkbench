/*
 * SortArrowIcon.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2014, Thomas Kellerer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at.
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.HashMap;

import javax.swing.Icon;

import workbench.gui.renderer.ColorUtils;

/**
 *
 * @author Thomas Kellerer
 */
public class SortArrowIcon
	implements Icon
{
	public enum Direction
	{
		UP,
		DOWN;
	}

	private final Direction direction;
	private final int width;
	private final int height;

	private static final HashMap<Integer, SortArrowIcon> sharedUpArrows = new HashMap<Integer, SortArrowIcon>();
	private static final HashMap<Integer, SortArrowIcon> sharedDownArrows = new HashMap<Integer, SortArrowIcon>();

	public static synchronized SortArrowIcon getIcon(Direction dir, int size)
	{
		HashMap<Integer, SortArrowIcon> cache = (dir == Direction.UP ? sharedUpArrows : sharedDownArrows);

		Integer key = Integer.valueOf(size);
		SortArrowIcon icon = cache.get(key);
		if (icon == null)
		{
			icon = new SortArrowIcon(dir, size);
			cache.put(key, icon);
		}
		return icon;
	}

	private SortArrowIcon(Direction dir, int size)
	{
		this.direction = dir;
		this.width = size + (int)(size * 0.1);
		this.height = size;
	}

	@Override
	public int getIconWidth()
	{
		return width;
	}

	@Override
	public int getIconHeight()
	{
		return height;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y)
	{
		Color bg = c.getBackground();
		Color fg = c.getForeground();

		Color arrowColor = ColorUtils.blend(bg, Color.BLACK, 175);
		int w = width;
		int h = height;
		int top = y + h;
		int bottom = y;

		int[] xPoints = new int[] {x, x + w/2, x + w}; // left, middle, right
		int[] yPoints;

		if (direction == Direction.UP)
		{
			yPoints = new int[] {top, bottom, top};
		}
		else
		{
			yPoints = new int[] {bottom, top, bottom};
		}

		g.setColor(arrowColor);
		g.fillPolygon(xPoints, yPoints, 3);
		g.setColor(fg);
	}
}

