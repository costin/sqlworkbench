/*
 * EditorColorsPanel.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2009, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.settings;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import workbench.gui.components.WbColorPicker;
import workbench.interfaces.Restoreable;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;

/**
 *
 * @author support@sql-workbench.net
 */
public class EditorColorsPanel
	extends JPanel
	implements Restoreable
{

	public EditorColorsPanel()
	{
		initComponents();
	}

	public void restoreSettings()
	{
		Settings sett = Settings.getInstance();
		
		Color c1 = sett.getColor("workbench.editor.color.comment1", Color.GRAY);
		blockComments.setSelectedColor(c1);

		Color c2 = sett.getColor("workbench.editor.color.comment2", Color.GRAY);
		lineComments.setSelectedColor(c2);

		Color k1 = sett.getColor("workbench.editor.color.keyword1", Color.BLUE);
		keyword1.setSelectedColor(k1);

		Color k2 = sett.getColor("workbench.editor.color.keyword2", Color.MAGENTA);
		keyword2.setSelectedColor(k2);

		Color k3 = sett.getColor("workbench.editor.color.keyword3", new Color(0x009600));
		keyword3.setSelectedColor(k3);

		Color l1 = sett.getColor("workbench.editor.color.literal1", new Color(0x650099));
		literals.setSelectedColor(l1);

		Color op = sett.getColor("workbench.editor.color.operator", Color.BLACK);
		operators.setSelectedColor(op);

		errorColor.setSelectedColor(Settings.getInstance().getEditorErrorColor());
		selectionColor.setSelectedColor(Settings.getInstance().getEditorSelectionColor());
		currLineColor.setSelectedColor(Settings.getInstance().getEditorCurrentLineColor());

	}

	public void saveSettings()
	{
		Settings sett = Settings.getInstance();
		sett.setColor("workbench.editor.color.comment1", blockComments.getSelectedColor());
		sett.setColor("workbench.editor.color.comment2", lineComments.getSelectedColor());
		sett.setColor("workbench.editor.color.keyword1", keyword1.getSelectedColor());
		sett.setColor("workbench.editor.color.keyword2", keyword2.getSelectedColor());
		sett.setColor("workbench.editor.color.keyword3", keyword3.getSelectedColor());
		sett.setColor("workbench.editor.color.literal1", literals.getSelectedColor());
		sett.setColor("workbench.editor.color.operator", operators.getSelectedColor());

		sett.setEditorErrorColor(errorColor.getSelectedColor());
		sett.setEditorCurrentLineColor(currLineColor.getSelectedColor());
		sett.setEditorSelectionColor(selectionColor.getSelectedColor());

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

    blockCommentsLabel = new JLabel();
    blockComments = new WbColorPicker();
    lineCommentsLabel = new JLabel();
    lineComments = new WbColorPicker();
    keywordsLabel = new JLabel();
    keyword1 = new WbColorPicker();
    functionsLabel = new JLabel();
    keyword3 = new WbColorPicker();
    operatorsLabel = new JLabel();
    operators = new WbColorPicker();
    literalsLabel = new JLabel();
    literals = new WbColorPicker();
    wbCommandsLabel = new JLabel();
    keyword2 = new WbColorPicker();
    currLineLabel = new JLabel();
    currLineColor = new WbColorPicker(true);
    selectionColorLabel = new JLabel();
    selectionColor = new WbColorPicker();
    errorColorLabel = new JLabel();
    errorColor = new WbColorPicker();
    jSeparator1 = new JSeparator();

    setLayout(new GridBagLayout());

    blockCommentsLabel.setText(ResourceMgr.getString("LblColorComment1")); // NOI18N
    blockCommentsLabel.setToolTipText(ResourceMgr.getString("d_LblColorComment1")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(10, 10, 0, 0);
    add(blockCommentsLabel, gridBagConstraints);

    blockComments.setToolTipText(ResourceMgr.getString("d_LblColorComment1")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(7, 6, 0, 0);
    add(blockComments, gridBagConstraints);

    lineCommentsLabel.setText(ResourceMgr.getString("LblColorComment2")); // NOI18N
    lineCommentsLabel.setToolTipText(ResourceMgr.getString("d_LblColorComment2")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(10, 10, 0, 0);
    add(lineCommentsLabel, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(7, 6, 0, 0);
    add(lineComments, gridBagConstraints);

    keywordsLabel.setText(ResourceMgr.getString("LblColorKeyword1")); // NOI18N
    keywordsLabel.setToolTipText(ResourceMgr.getString("d_LblColorKeyword1")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(10, 10, 0, 0);
    add(keywordsLabel, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(7, 6, 0, 0);
    add(keyword1, gridBagConstraints);

    functionsLabel.setText(ResourceMgr.getString("LblColorKeyword3")); // NOI18N
    functionsLabel.setToolTipText(ResourceMgr.getString("d_LblColorKeyword3")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(10, 10, 0, 0);
    add(functionsLabel, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(7, 6, 0, 0);
    add(keyword3, gridBagConstraints);

    operatorsLabel.setText(ResourceMgr.getString("LblColorOperator")); // NOI18N
    operatorsLabel.setToolTipText(ResourceMgr.getString("d_LblColorOperator")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(10, 10, 0, 0);
    add(operatorsLabel, gridBagConstraints);

    operators.setToolTipText(ResourceMgr.getString("d_LblColorOperator")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(7, 6, 0, 0);
    add(operators, gridBagConstraints);

    literalsLabel.setText(ResourceMgr.getString("LblColorLiteral")); // NOI18N
    literalsLabel.setToolTipText(ResourceMgr.getString("d_LblColorLiteral")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(10, 10, 0, 0);
    add(literalsLabel, gridBagConstraints);

    literals.setToolTipText(ResourceMgr.getString("d_LblColorLiteral")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new Insets(7, 6, 0, 0);
    add(literals, gridBagConstraints);

    wbCommandsLabel.setText(ResourceMgr.getString("LblColorKeyword2")); // NOI18N
    wbCommandsLabel.setToolTipText(ResourceMgr.getString("d_LblColorKeyword2")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(10, 10, 0, 0);
    add(wbCommandsLabel, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new Insets(7, 6, 0, 0);
    add(keyword2, gridBagConstraints);

    currLineLabel.setText(ResourceMgr.getString("LblCurrLineColor")); // NOI18N
    currLineLabel.setToolTipText(ResourceMgr.getString("d_LblCurrLineColor")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(18, 10, 0, 0);
    add(currLineLabel, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(14, 6, 0, 0);
    add(currLineColor, gridBagConstraints);

    selectionColorLabel.setText(ResourceMgr.getString("LblSelectionColor")); // NOI18N
    selectionColorLabel.setToolTipText(ResourceMgr.getString("d_LblSelectionColor")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(10, 10, 0, 0);
    add(selectionColorLabel, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(7, 6, 0, 0);
    add(selectionColor, gridBagConstraints);

    errorColorLabel.setText(ResourceMgr.getString("LblSelectErrorColor")); // NOI18N
    errorColorLabel.setToolTipText(ResourceMgr.getString("d_LblSelectErrorColor")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(10, 10, 0, 0);
    add(errorColorLabel, gridBagConstraints);

    errorColor.setToolTipText(ResourceMgr.getString("d_LblSelectErrorColor")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(7, 6, 0, 0);
    add(errorColor, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(14, 0, 2, 0);
    add(jSeparator1, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private WbColorPicker blockComments;
  private JLabel blockCommentsLabel;
  private WbColorPicker currLineColor;
  private JLabel currLineLabel;
  private WbColorPicker errorColor;
  private JLabel errorColorLabel;
  private JLabel functionsLabel;
  private JSeparator jSeparator1;
  private WbColorPicker keyword1;
  private WbColorPicker keyword2;
  private WbColorPicker keyword3;
  private JLabel keywordsLabel;
  private WbColorPicker lineComments;
  private JLabel lineCommentsLabel;
  private WbColorPicker literals;
  private JLabel literalsLabel;
  private WbColorPicker operators;
  private JLabel operatorsLabel;
  private WbColorPicker selectionColor;
  private JLabel selectionColorLabel;
  private JLabel wbCommandsLabel;
  // End of variables declaration//GEN-END:variables

}
