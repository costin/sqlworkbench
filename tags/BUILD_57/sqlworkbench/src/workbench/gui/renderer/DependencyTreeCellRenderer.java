/*
 * DependencyTreeCellRenderer.java
 *
 * Created on October 24, 2002, 11:30 AM
 */

package workbench.gui.renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

import workbench.db.DependencyNode;
import workbench.gui.WbSwingUtilities;
import workbench.resource.ResourceMgr;

/**
 *
 * @author  workbench@kellerer.org
 */
public class DependencyTreeCellRenderer
	extends JLabel
	implements TreeCellRenderer
{
	private Color selectedForeground;
	private Color selectedBackground;
	private Color unselectedForeground;
	private Color unselectedBackground;
	private ImageIcon fk;
	private ImageIcon table;
	private boolean isSelected;
	
	public DependencyTreeCellRenderer()
	{
		this.setBorder(WbSwingUtilities.EMPTY_BORDER);
		this.setVerticalAlignment(SwingConstants.TOP);
		this.setHorizontalAlignment(SwingConstants.LEFT);
		this.fk = ResourceMgr.getPicture("foreignKey");
		this.table = ResourceMgr.getPicture("table");
		this.selectedForeground = UIManager.getColor("Tree.selectionForeground");
		this.selectedBackground = UIManager.getColor("Tree.selectionBackground");
		this.unselectedForeground = UIManager.getColor("Tree.textForeground");
		this.unselectedBackground = UIManager.getColor("Tree.textBackground");
	}
	
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
	{
		this.isSelected = selected;
		if (selected)
		{
			this.setForeground(this.selectedForeground);
		}
		else
		{
			this.setForeground(this.unselectedForeground);
			//this.setBackground(this.unselectedBackground);
		}
		
		if (value instanceof DefaultMutableTreeNode)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
			Object o = node.getUserObject();
			if (o instanceof DependencyNode)
			{
				this.setIcon(table);
				DependencyNode depnode = (DependencyNode)o;
				String uaction = depnode.getUpdateAction();
				String daction = depnode.getDeleteAction();
				if (uaction.length() > 0 || daction.length() > 0)
				{
					StringBuffer tooltip = new StringBuffer(50);
					tooltip.append("<html><body>");
					boolean needBreak = false;
					if (uaction.length() > 0) 
					{
						tooltip.append("ON UPDATE ");
						tooltip.append(uaction);
						needBreak = true;
					}
					if (daction.length() > 0) 
					{
						if (needBreak) tooltip.append("<br>");
						tooltip.append("ON DELETE ");
						tooltip.append(daction);
					}
					tooltip.append("</body></html>");
					setToolTipText(tooltip.toString());
				}
				else
				{
					setToolTipText(null);
				}
			}
			else
			{
				this.setIcon(fk);
				this.setToolTipText(null);
			}
		}
		else
		{
			this.setIcon(null);
		}
		this.setText(value.toString());
		
		return this;
	}

	public void paint(Graphics g)
	{
		Color bColor;
		
		if(this.isSelected)
		{
			bColor = this.selectedBackground;
		} 
		else
		{
			bColor = this.unselectedBackground;
			if(bColor == null)	bColor = getBackground();
		}
		int imageOffset = -1;
		if(bColor != null)
		{
			Icon currentI = getIcon();
			
			imageOffset = getLabelStart();
			Color oldColor = g.getColor();
			g.setColor(bColor);
			if(getComponentOrientation().isLeftToRight())
			{
				g.fillRect(imageOffset, 0, getWidth() - 1 - imageOffset,
				getHeight());
			} 
			else
			{
				g.fillRect(0, 0, getWidth() - 1 - imageOffset,
				getHeight());
			}
			g.setColor(oldColor);
		}
		super.paint(g);
	}

	private int getLabelStart()
	{
		Icon currentI = getIcon();
		if(currentI != null && getText() != null)
		{
			return currentI.getIconWidth() + Math.max(0, getIconTextGap() - 1);
		}
		return 0;
	}
	
}