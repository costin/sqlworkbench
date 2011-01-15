/*
 * WbFontChooser.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.components;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import workbench.gui.WbSwingUtilities;
import workbench.resource.ResourceMgr;
import workbench.util.StringUtil;

/**
 *
 * @author  Thomas Kellerer
 */
public class WbFontChooser
	extends javax.swing.JPanel
{
	private boolean updateing;
	private boolean fontReset;

	public WbFontChooser(boolean monospacedOnly)
	{
		this(monospacedOnly, false);
	}

	public WbFontChooser(boolean monospacedOnly, boolean allowReset)
	{
		super();
		initComponents();
		resetButton.setVisible(allowReset);
		resetButton.setEnabled(allowReset);
		fillFontList(monospacedOnly);
	}

	public void setSelectedFont(Font aFont)
	{
		this.updateing = true;
		try
		{
			if (aFont != null)
			{
				String name = aFont.getFamily();
				String size = Integer.toString(aFont.getSize());
				int style = aFont.getStyle();

				this.fontNameList.setSelectedValue(name, true);
				this.fontSizeComboBox.setSelectedItem(size);
				this.boldCheckBox.setSelected((style & Font.BOLD) == Font.BOLD);
				this.italicCheckBox.setSelected((style & Font.ITALIC) == Font.ITALIC);
				fontReset = false;
			}
			else
			{
				this.fontNameList.clearSelection();
				this.boldCheckBox.setSelected(false);
				this.italicCheckBox.setSelected(false);
			}
		}
		catch (Exception e)
		{
		}
		this.updateing = false;
		this.updateFontDisplay();
	}

	public boolean isFontReset()
	{
		return this.fontReset;
	}

	public Font getSelectedFont()
	{
		String fontName = (String)this.fontNameList.getSelectedValue();
		if (fontName == null) return null;
		int size = StringUtil.getIntValue((String)this.fontSizeComboBox.getSelectedItem());
		int style = Font.PLAIN;
		if (this.italicCheckBox.isSelected())
			style = style | Font.ITALIC;
		if (this.boldCheckBox.isSelected())
			style = style | Font.BOLD;

		Font f = new Font(fontName, style, size);
		return f;
	}

	public static Font chooseFont(JComponent owner, Font defaultFont, boolean monospacedOnly, boolean allowReset)
	{
		WbFontChooser chooser = new WbFontChooser(monospacedOnly, allowReset);
		if (defaultFont != null) chooser.setSelectedFont(defaultFont);
		Dimension d = new Dimension(320, 240);
		chooser.setSize(d);
		chooser.setPreferredSize(d);

		JOptionPane option = new JOptionPane(chooser, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		//int answer = JOptionPane.showConfirmDialog(owner, chooser, ResourceMgr.getString("TxtWindowTitleChooseFont"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		JDialog dialog = option.createDialog(owner, ResourceMgr.getString("TxtWindowTitleChooseFont"));
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		dialog.pack();//setSize(320,240);
		WbSwingUtilities.center(dialog, SwingUtilities.getWindowAncestor(owner));
		dialog.setVisible(true);
		Object value= option.getValue();
		if (value == null) return null;
		Font result = null;
		if (value instanceof Integer)
		{
			int answer = ((Integer)value).intValue();
			if (answer == JOptionPane.OK_OPTION)
			{
				result = chooser.getSelectedFont();
			}
		}
		return result;
	}


	private void fillFontList(boolean monospacedOnly)
	{
		String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		DefaultListModel model = new DefaultListModel();

		for (int i = 0; i < fonts.length; i++)
		{
			if (monospacedOnly)
			{
				Font f = new Font(fonts[i], Font.PLAIN, 10);
				FontMetrics fm = getFontMetrics(f);
				int iWidth = fm.charWidth('i');
				int mWidth = fm.charWidth('M');
				if (iWidth != mWidth) continue;
			}
			model.addElement(fonts[i]);
		}
		this.fontNameList.setModel(model);
	}

	private void updateFontDisplay()
	{
		if (!this.updateing)
		{
			synchronized (this)
			{
				this.updateing = true;
				try
				{
					Font f = this.getSelectedFont();
					if (f != null)
					{
						this.sampleLabel.setFont(f);
						this.sampleLabel.setText((String)this.fontNameList.getSelectedValue());
					}
					else
					{
						this.sampleLabel.setText("");
					}
				}
				finally
				{
					this.updateing = false;
				}
			}
		}
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
  // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
  private void initComponents()
  {
    java.awt.GridBagConstraints gridBagConstraints;

    fontSizeComboBox = new javax.swing.JComboBox();
    jScrollPane1 = new javax.swing.JScrollPane();
    fontNameList = new javax.swing.JList();
    boldCheckBox = new javax.swing.JCheckBox();
    italicCheckBox = new javax.swing.JCheckBox();
    sampleLabel = new javax.swing.JLabel();
    resetButton = new javax.swing.JButton();

    setMinimumSize(new java.awt.Dimension(320, 240));
    setPreferredSize(new java.awt.Dimension(320, 240));
    setLayout(new java.awt.GridBagLayout());

    fontSizeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24" }));
    fontSizeComboBox.setMaximumSize(new java.awt.Dimension(200, 22));
    fontSizeComboBox.setMinimumSize(new java.awt.Dimension(30, 22));
    fontSizeComboBox.setPreferredSize(new java.awt.Dimension(40, 22));
    fontSizeComboBox.addItemListener(new java.awt.event.ItemListener()
    {
      public void itemStateChanged(java.awt.event.ItemEvent evt)
      {
        fontSizeComboBoxupdateFontDisplay(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 1);
    add(fontSizeComboBox, gridBagConstraints);

    fontNameList.setModel(new javax.swing.AbstractListModel()
    {
      String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
      public int getSize() { return strings.length; }
      public Object getElementAt(int i) { return strings[i]; }
    });
    fontNameList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    fontNameList.addListSelectionListener(new javax.swing.event.ListSelectionListener()
    {
      public void valueChanged(javax.swing.event.ListSelectionEvent evt)
      {
        fontNameListValueChanged(evt);
      }
    });
    jScrollPane1.setViewportView(fontNameList);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridheight = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.weighty = 0.5;
    add(jScrollPane1, gridBagConstraints);

    boldCheckBox.setText(ResourceMgr.getString("LblBold"));
    boldCheckBox.addItemListener(new java.awt.event.ItemListener()
    {
      public void itemStateChanged(java.awt.event.ItemEvent evt)
      {
        boldCheckBoxupdateFontDisplay(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
    add(boldCheckBox, gridBagConstraints);

    italicCheckBox.setText(ResourceMgr.getString("LblItalic"));
    italicCheckBox.addItemListener(new java.awt.event.ItemListener()
    {
      public void itemStateChanged(java.awt.event.ItemEvent evt)
      {
        italicCheckBoxupdateFontDisplay(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
    add(italicCheckBox, gridBagConstraints);

    sampleLabel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createTitledBorder("Preview"), javax.swing.BorderFactory.createEmptyBorder(1, 1, 5, 1)));
    sampleLabel.setMaximumSize(new java.awt.Dimension(43, 100));
    sampleLabel.setMinimumSize(new java.awt.Dimension(48, 60));
    sampleLabel.setPreferredSize(new java.awt.Dimension(48, 60));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    add(sampleLabel, gridBagConstraints);

    resetButton.setText(ResourceMgr.getString("LblResetFont"));
    resetButton.setToolTipText(ResourceMgr.getDescription("LblResetFont"));
    resetButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        resetButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(10, 7, 0, 0);
    add(resetButton, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents

private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
	setSelectedFont(null);
	fontReset = true;
}//GEN-LAST:event_resetButtonActionPerformed

	private void fontNameListValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_fontNameListValueChanged
	{//GEN-HEADEREND:event_fontNameListValueChanged
		updateFontDisplay();
	}//GEN-LAST:event_fontNameListValueChanged

	private void italicCheckBoxupdateFontDisplay(java.awt.event.ItemEvent evt)//GEN-FIRST:event_italicCheckBoxupdateFontDisplay
	{//GEN-HEADEREND:event_italicCheckBoxupdateFontDisplay
		updateFontDisplay();
	}//GEN-LAST:event_italicCheckBoxupdateFontDisplay

	private void boldCheckBoxupdateFontDisplay(java.awt.event.ItemEvent evt)//GEN-FIRST:event_boldCheckBoxupdateFontDisplay
	{//GEN-HEADEREND:event_boldCheckBoxupdateFontDisplay
		updateFontDisplay();
	}//GEN-LAST:event_boldCheckBoxupdateFontDisplay

	private void fontSizeComboBoxupdateFontDisplay(java.awt.event.ItemEvent evt)//GEN-FIRST:event_fontSizeComboBoxupdateFontDisplay
	{//GEN-HEADEREND:event_fontSizeComboBoxupdateFontDisplay
		updateFontDisplay();
	}//GEN-LAST:event_fontSizeComboBoxupdateFontDisplay

  // Variables declaration - do not modify//GEN-BEGIN:variables
  public javax.swing.JCheckBox boldCheckBox;
  public javax.swing.JList fontNameList;
  public javax.swing.JComboBox fontSizeComboBox;
  public javax.swing.JCheckBox italicCheckBox;
  public javax.swing.JScrollPane jScrollPane1;
  public javax.swing.JButton resetButton;
  public javax.swing.JLabel sampleLabel;
  // End of variables declaration//GEN-END:variables

}
