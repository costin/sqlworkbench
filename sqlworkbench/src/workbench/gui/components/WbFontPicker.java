/*
 * WbFontPicker.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2008, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.components;

import java.awt.Dimension;
import java.awt.Font;
import java.io.Serializable;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import workbench.gui.WbSwingUtilities;
import workbench.resource.ResourceMgr;

/**
 *
 * @author support@sql-workbench.net
 */
public class WbFontPicker
	extends JPanel
	implements Serializable
{
	private Font selectedFont;
	private boolean monospacedOnly = false;
	private boolean allowFontReset = false;

	public WbFontPicker()
	{
		super();
		initComponents();
		this.setAllowFontReset(false);
	}

	public void setAllowFontReset(boolean flag)
	{
		this.allowFontReset = flag;
		this.resetButton.setVisible(flag);
		this.resetButton.setEnabled(flag);
		if (flag)
		{
			this.resetButton.setIcon(ResourceMgr.getImage("Delete"));
		}
	}

	public void setListMonospacedOnly(boolean flag)
	{
		this.monospacedOnly = flag;
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    fontName = new javax.swing.JLabel();
    selectFontButton = new FlatButton();
    resetButton = new FlatButton();

    setLayout(new java.awt.GridBagLayout());

    fontName.setText("jLabel1");
    fontName.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), javax.swing.BorderFactory.createEmptyBorder(1, 5, 1, 5)));
    fontName.setMaximumSize(new java.awt.Dimension(45, 22));
    fontName.setMinimumSize(new java.awt.Dimension(45, 22));
    fontName.setPreferredSize(new java.awt.Dimension(45, 22));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
    add(fontName, gridBagConstraints);

    selectFontButton.setText("...");
    selectFontButton.setMaximumSize(new java.awt.Dimension(22, 22));
    selectFontButton.setMinimumSize(new java.awt.Dimension(22, 22));
    selectFontButton.setPreferredSize(new java.awt.Dimension(22, 22));
    selectFontButton.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        selectFontButtonMouseClicked(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    add(selectFontButton, gridBagConstraints);

    resetButton.setToolTipText(ResourceMgr.getDescription("LblResetFont"));
    resetButton.setMaximumSize(new java.awt.Dimension(22, 22));
    resetButton.setMinimumSize(new java.awt.Dimension(22, 22));
    resetButton.setPreferredSize(new java.awt.Dimension(22, 22));
    resetButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        resetButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 3);
    add(resetButton, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents

private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
	this.setSelectedFont(null);
}//GEN-LAST:event_resetButtonActionPerformed

	private void selectFontButtonMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_selectFontButtonMouseClicked
	{//GEN-HEADEREND:event_selectFontButtonMouseClicked
		WbFontChooser chooser = new WbFontChooser(monospacedOnly, allowFontReset);
		chooser.setSelectedFont(getSelectedFont());
		Dimension d = new Dimension(320, 240);
		chooser.setSize(d);
		chooser.setPreferredSize(d);

		JOptionPane option = new JOptionPane(chooser, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		JDialog dialog = option.createDialog(this, ResourceMgr.getString("TxtWindowTitleChooseFont"));
		dialog.pack();
		WbSwingUtilities.center(dialog, SwingUtilities.getWindowAncestor(this));
		dialog.setVisible(true);
		dialog.dispose();

		if (chooser.isFontReset())
		{
			this.setSelectedFont(null);
			return;
		}

		Object value= option.getValue();
		if (value == null) return;
		Font result = null;
		if (value instanceof Integer)
		{
			int answer = ((Integer)value).intValue();
			if (answer == JOptionPane.OK_OPTION)
			{
				result = chooser.getSelectedFont();
			}
		}
		if (result != null)
		{
			this.setSelectedFont(result);
		}

	}//GEN-LAST:event_selectFontButtonMouseClicked

	public Font getSelectedFont()
	{
		return this.selectedFont;
	}

	public void setSelectedFont(Font f)
	{
		this.selectedFont = f;
		if (f == null)
		{
			Font df = UIManager.getDefaults().getFont("Label.font");
			this.fontName.setFont(df);
			this.fontName.setText(ResourceMgr.getString("LblDefaultIndicator"));
		}
		else
		{
			this.fontName.setFont(f);
			this.fontName.setText(f.getFontName() + ", " + f.getSize());
		}
	}

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel fontName;
  private javax.swing.JButton resetButton;
  private javax.swing.JButton selectFontButton;
  // End of variables declaration//GEN-END:variables

}
