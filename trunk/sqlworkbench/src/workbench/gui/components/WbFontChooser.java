/*
 * WbFontChooser.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2005, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: info@sql-workbench.net
 *
 */
package workbench.gui.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import workbench.resource.ResourceMgr;
import workbench.util.StringUtil;

/**
 *
 * @author  info@sql-workbench.net
 */
public class WbFontChooser extends JPanel
{
	
	/** Creates new form BeanForm */
	public WbFontChooser()
	{
		initComponents();
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        fontNameComboBox = new javax.swing.JComboBox();
        fontSizeComboBox = new javax.swing.JComboBox();
        checkBoxPanel = new javax.swing.JPanel();
        boldCheckBox = new javax.swing.JCheckBox();
        italicCheckBox = new javax.swing.JCheckBox();
        sampleLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        fontNameComboBox.setMaximumSize(new java.awt.Dimension(120, 22));
        fontNameComboBox.setMinimumSize(new java.awt.Dimension(60, 22));
        fontNameComboBox.setPreferredSize(new java.awt.Dimension(60, 22));
        fillFontDropDown();
        fontNameComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                updateFontDisplay(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.7;
        add(fontNameComboBox, gridBagConstraints);

        fontSizeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24" }));
        fontSizeComboBox.setMaximumSize(new java.awt.Dimension(200, 22));
        fontSizeComboBox.setMinimumSize(new java.awt.Dimension(30, 22));
        fontSizeComboBox.setPreferredSize(new java.awt.Dimension(40, 22));
        fontSizeComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                updateFontDisplay(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        add(fontSizeComboBox, gridBagConstraints);

        checkBoxPanel.setLayout(new java.awt.GridBagLayout());

        checkBoxPanel.setMaximumSize(new java.awt.Dimension(32767, 22));
        checkBoxPanel.setMinimumSize(new java.awt.Dimension(200, 22));
        boldCheckBox.setText(ResourceMgr.getString("LabelBold"));
        boldCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                updateFontDisplay(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        checkBoxPanel.add(boldCheckBox, gridBagConstraints);

        italicCheckBox.setText(ResourceMgr.getString("LabelItalic"));
        italicCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                updateFontDisplay(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        checkBoxPanel.add(italicCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(checkBoxPanel, gridBagConstraints);

        sampleLabel.setText("jLabel1");
        sampleLabel.setMaximumSize(new java.awt.Dimension(43, 48));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 1.0;
        add(sampleLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);

    }//GEN-END:initComponents

	private void updateFontDisplay(java.awt.event.ItemEvent evt)//GEN-FIRST:event_updateFontDisplay
	{//GEN-HEADEREND:event_updateFontDisplay
		if (!this.updateing)
		{
			Font f = this.getSelectedFont();
			this.sampleLabel.setFont(f);
			this.sampleLabel.setText((String)this.fontNameComboBox.getSelectedItem());
		}
	}//GEN-LAST:event_updateFontDisplay
	
	public Font getSelectedFont()
	{
		String fontName = (String)this.fontNameComboBox.getSelectedItem();
		int size = StringUtil.getIntValue((String)this.fontSizeComboBox.getSelectedItem());
		int style = 0;
		if (this.italicCheckBox.isSelected())
			style = style | Font.ITALIC;
		if (this.boldCheckBox.isSelected())
			style = style | Font.BOLD;
		
		Font f = new Font(fontName, style, size);
		return f;
	}
	
	public void setSelectedFont(Font aFont)
	{
		String name = aFont.getFamily();
		String size = Integer.toString(aFont.getSize());
		int style = aFont.getStyle();
		this.updateing = true;
		try
		{
			this.fontNameComboBox.setSelectedItem(name);
			this.fontSizeComboBox.setSelectedItem(size);
			this.boldCheckBox.setSelected((style & Font.BOLD) == Font.BOLD);
			this.italicCheckBox.setSelected((style & Font.ITALIC) == Font.ITALIC);
		}
		catch (Exception e)
		{
		}
		this.updateing = false;
		this.updateFontDisplay(null);
	}
	private void fillFontDropDown()
	{
		String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		ComboBoxModel model = new DefaultComboBoxModel(fonts);
		this.fontNameComboBox.setModel(model);
	}
  
  public static Font chooseFont(JDialog owner, Font defaultFont)
  {
		WbFontChooser chooser = new WbFontChooser();
		chooser.setSelectedFont(defaultFont);
		chooser.setSize(new Dimension(400, 250));

		int answer = JOptionPane.showConfirmDialog(owner, chooser, ResourceMgr.getString("TxtWindowTitleChooseFont"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		Font result = null;
		if (answer == JOptionPane.OK_OPTION)
		{
			result = chooser.getSelectedFont();
		}
		return result;
  }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox boldCheckBox;
    private javax.swing.JPanel checkBoxPanel;
    private javax.swing.JComboBox fontNameComboBox;
    private javax.swing.JComboBox fontSizeComboBox;
    private javax.swing.JCheckBox italicCheckBox;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel sampleLabel;
    // End of variables declaration//GEN-END:variables

	private boolean updateing;
	
}
