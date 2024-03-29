/*
 * DisconnectInfo.java
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
package workbench.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import workbench.resource.ResourceMgr;

/**
 * A modal dialog displaying a message and allowing for a callback action.
 *
 * @author Thomas Kellerer
 */
public class DisconnectInfo
	extends JDialog
  implements ActionListener
{
	private ActionListener cancelAction;
	private JButton cancelButton;

	public DisconnectInfo(JFrame parent, ActionListener action, String msgKey)
	{
		super(parent, false);
		cancelAction = action;
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		JPanel p = new JPanel();
    Color c = p.getBackground().darker();
    p.setBorder(new CompoundBorder(new LineBorder(c, 1), new EmptyBorder(15, 20, 15, 20)));
		p.setLayout(new BorderLayout(0,0));
		p.setMinimumSize(new Dimension(350, 50));

		JLabel l = new JLabel(ResourceMgr.getString("MsgClosingConnections"));
		l.setMinimumSize(new Dimension(300, 50));
		l.setHorizontalAlignment(SwingConstants.CENTER);
		p.add(l, BorderLayout.CENTER);

		if (cancelAction != null)
		{
			cancelButton = new JButton(ResourceMgr.getString(msgKey));
			cancelButton.setToolTipText(ResourceMgr.getDescription(msgKey));
			cancelButton.addActionListener(this);
			JPanel p2 = new JPanel();
			p2.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			p2.setBorder(new EmptyBorder(15,10,5,10));
			p2.add(cancelButton);
			p.add(p2, BorderLayout.SOUTH);
		}
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(p, BorderLayout.CENTER);
		setUndecorated(true);
    getRootPane().setWindowDecorationStyle(JRootPane.NONE);
		pack();
		WbSwingUtilities.center(this, parent);
	}

  @Override
  public void actionPerformed(ActionEvent evt)
  {
    cancelAction.actionPerformed(evt);
  }

	@Override
	public void dispose()
	{
		if (this.cancelButton != null && this.cancelAction != null)
		{
			cancelButton.removeActionListener(cancelAction);
		}
		super.dispose();
	}
}
