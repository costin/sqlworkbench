/*
 * ImportFileOptionsPanel.java
 *
 * Created on October 30, 2002, 1:41 PM
 */

package workbench.gui.components;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import workbench.resource.ResourceMgr;



/**
 *
 * @author  workbench@kellerer.org
 */
public class ExportOptionsPanel
	extends JPanel
	implements ActionListener
{

	private JRadioButton typeSql;
	private JRadioButton typeText;
	private JCheckBox createTableOption;
	private JCheckBox includeHeadersOption;
	private JTextField commitEvery;
	private JLabel commitLabel;
	
	public ExportOptionsPanel()
	{
		this.setLayout(new GridBagLayout());
		ButtonGroup type = new ButtonGroup();
		
		JLabel label = new JLabel(ResourceMgr.getString("LabelExportTypeDesc"));
		this.typeSql = new JRadioButton(ResourceMgr.getString("LabelExportTypeSql"));
		
		this.typeText = new JRadioButton(ResourceMgr.getString("LabelExportTypeText"));
		
		this.typeSql.addActionListener(this);
		this.typeText.addActionListener(this);

		type.add(typeSql);
		type.add(typeText);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.WEST;
		Insets leftMarginInsets = new Insets(0, 5, 0, 0);
		gbc.insets = leftMarginInsets;
		this.add(label, gbc);

		Insets emptyInsets = new Insets(0, 0, 0, 0);
		gbc.gridy++;
		gbc.insets = emptyInsets;
		this.add(typeSql, gbc);
		
		this.createTableOption = new JCheckBox(ResourceMgr.getString("LabelExportIncludeCreateTable"));
		gbc.gridy++;
		this.add(this.createTableOption, gbc);
		
		this.commitEvery = new JTextField(5);
		this.commitLabel = new JLabel(ResourceMgr.getString("LabelDPCommitEvery"));
		gbc.gridy++;
		gbc.gridwidth = 1;
		this.add(this.commitLabel, gbc);
		
		gbc.gridx ++;
		gbc.gridwidth = 1;
		gbc.insets = leftMarginInsets;
		gbc.anchor = GridBagConstraints.WEST;
		this.add(this.commitEvery, gbc);

		gbc.gridx--;
		gbc.gridwidth = 2;
		gbc.gridy++;
		gbc.insets = emptyInsets;
		this.add(this.typeText, gbc);
		
		this.includeHeadersOption = new JCheckBox(ResourceMgr.getString("LabelExportIncludeHeaders"));
		gbc.gridy++;
		this.add(this.includeHeadersOption, gbc);

		gbc.gridx = 0;
		gbc.gridy ++;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		JPanel dummy = new JPanel();
		this.add(dummy, gbc);

		this.typeSql.setSelected(true);
		this.includeHeadersOption.setSelected(false);
		this.includeHeadersOption.setEnabled(false);
		
	}
	
	public boolean isTypeSql()
	{
		return this.typeSql.isSelected();
	}
	
	public boolean isTypeText()
	{
		return this.typeText.isSelected();
	}
	
	public boolean getIncludeTextHeader()
	{
		return this.includeHeadersOption.isSelected();
	}
	
	public void setCommitEvery(int aNumber)
	{
		this.commitEvery.setText(Integer.toString(aNumber));
	}
	
	public boolean getCreateTable()
	{
		return this.createTableOption.isSelected();
	}

	public int getCommitEvery()
	{
		String every = this.commitEvery.getText();
		int result = -1;
		try
		{
			result = Integer.parseInt(every);
		}
		catch (Exception e)
		{
			result = -1;
		}
		return result;
	}
	
	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		this.includeHeadersOption.setEnabled(this.typeText.isSelected());
		this.createTableOption.setEnabled(this.typeSql.isSelected());
		this.commitEvery.setEnabled(this.typeSql.isSelected());
		this.commitLabel.setForeground(this.includeHeadersOption.getForeground());
	}	
	
	
	public static void main(String args[])
	{
		try
		{
			ExportOptionsPanel p = new ExportOptionsPanel();
			JFrame f = new JFrame("Test");
			f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			f.getContentPane().add(p);
			f.pack();
			f.show();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("*** Done.");
	}
	

}