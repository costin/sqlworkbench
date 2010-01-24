/*
 * FontOptionsPanel.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2010, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.settings;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JPanel;
import workbench.gui.components.WbFontPicker;
import workbench.interfaces.Restoreable;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;

/**
 *
 * @author Thomas Kellerer
 */
public class FontOptionsPanel
	extends JPanel
	implements Restoreable
{

	public FontOptionsPanel()
	{
		initComponents();
		standardFont.setAllowFontReset(true);
		editorFont.setListMonospacedOnly(true);
		editorFont.setAllowFontReset(true);
		dataFont.setAllowFontReset(true);
		msgLogFont.setAllowFontReset(true);
	}

	public void restoreSettings()
	{
		editorFont.setSelectedFont(Settings.getInstance().getEditorFont(false));
		dataFont.setSelectedFont(Settings.getInstance().getDataFont());
		msgLogFont.setSelectedFont(Settings.getInstance().getMsgLogFont());
		standardFont.setSelectedFont(Settings.getInstance().getStandardFont());
	}

	public void saveSettings()
	{
		Settings.getInstance().setEditorFont(editorFont.getSelectedFont());
		Settings.getInstance().setDataFont(dataFont.getSelectedFont());
		Settings.getInstance().setStandardFont(standardFont.getSelectedFont());
		Settings.getInstance().setMsgLogFont(msgLogFont.getSelectedFont());
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
		GridBagConstraints gridBagConstraints;

    dataFontLabel = new JLabel();
    dataFont = new WbFontPicker();
    standardFont = new WbFontPicker();
    standardFontLabel = new JLabel();
    msgFontLabel = new JLabel();
    msgLogFont = new WbFontPicker();
    editorFont = new WbFontPicker();
    editorFontLabel = new JLabel();
    jPanel1 = new JPanel();

    setLayout(new GridBagLayout());

    dataFontLabel.setText(ResourceMgr.getString("LblDataFont")); // NOI18N
    dataFontLabel.setToolTipText(ResourceMgr.getString("d_LblDataFont")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(0, 12, 5, 0);
    add(dataFontLabel, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new Insets(0, 8, 5, 15);
    add(dataFont, gridBagConstraints);

    standardFont.setFont(standardFont.getFont());
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new Insets(0, 8, 5, 15);
    add(standardFont, gridBagConstraints);

    standardFontLabel.setText(ResourceMgr.getString("LblStandardFont")); // NOI18N
    standardFontLabel.setToolTipText(ResourceMgr.getString("d_LblStandardFont")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(0, 12, 5, 0);
    add(standardFontLabel, gridBagConstraints);

    msgFontLabel.setText(ResourceMgr.getString("LblMsgLogFont")); // NOI18N
    msgFontLabel.setToolTipText(ResourceMgr.getString("d_LblMsgLogFont")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(0, 12, 5, 0);
    add(msgFontLabel, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new Insets(0, 8, 5, 15);
    add(msgLogFont, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new Insets(10, 8, 5, 15);
    add(editorFont, gridBagConstraints);

    editorFontLabel.setText(ResourceMgr.getString("LblEditorFont")); // NOI18N
    editorFontLabel.setToolTipText(ResourceMgr.getString("d_LblEditorFont")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(10, 12, 5, 0);
    add(editorFontLabel, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.weighty = 1.0;
    add(jPanel1, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private WbFontPicker dataFont;
  private JLabel dataFontLabel;
  private WbFontPicker editorFont;
  private JLabel editorFontLabel;
  private JPanel jPanel1;
  private JLabel msgFontLabel;
  private WbFontPicker msgLogFont;
  private WbFontPicker standardFont;
  private JLabel standardFontLabel;
  // End of variables declaration//GEN-END:variables
}
