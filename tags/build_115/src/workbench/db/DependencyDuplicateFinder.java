/*
 * DependencyDuplicateFinder.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2013, Thomas Kellerer
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
package workbench.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Thomas Kellerer
 */
class DependencyDuplicateFinder
{
	private DependencyNode root;
	private Map<TableIdentifier, Integer> tableLevels = new HashMap<TableIdentifier, Integer>();

	DependencyDuplicateFinder(DependencyNode rootNode)
	{
		this.root = rootNode;
	}

	List<DependencyNode> getDuplicates()
	{
		List<DependencyNode> result = new ArrayList<DependencyNode>();
		List<NodeInformation> tree = buildTree(root, 0);
		for (NodeInformation info : tree)
		{
			if (info.level > getHighestLevel(info.node.getTable()))
			{
				result.add(info.node);
			}
		}
		return result;
	}

	private int getHighestLevel(TableIdentifier table)
	{
		Integer lvl = tableLevels.get(table);
		if (lvl == null) return 0;
		return lvl.intValue();
	}

	private List<NodeInformation> buildTree(DependencyNode root, int level)
	{
		List<NodeInformation> result = new ArrayList<NodeInformation>();
		List<DependencyNode> children = root.getChildren();
		if (children.isEmpty()) return result;

		for (DependencyNode child : children)
		{
			NodeInformation info = new NodeInformation();
			info.node = child;
			info.level = level;
			if (!tableLevels.containsKey(child.getTable()))
			{
				tableLevels.put(child.getTable(), Integer.valueOf(level));
			}
			result.add(info);
			result.addAll(buildTree(child, level + 1));
		}
		return result;
	}

	private class NodeInformation
	{
		DependencyNode node;
		int level;
	}

}