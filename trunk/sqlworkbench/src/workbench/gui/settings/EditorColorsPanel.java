/*
 * EditorColorsPanel.java
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
package workbench.gui.settings;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import workbench.interfaces.Restoreable;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;

import workbench.gui.components.WbColorPicker;
import workbench.gui.editor.SyntaxStyle;
import workbench.gui.editor.SyntaxUtilities;
import workbench.gui.editor.Token;

/**
 *
 * @author Thomas Kellerer
 */
public class EditorColorsPanel
	extends JPanel
	implements Restoreable
{

	public EditorColorsPanel()
	{
		initComponents();
	}

	@Override
	public void restoreSettings()
	{
		Settings sett = Settings.getInstance();

		SyntaxStyle[] defaultStyles = SyntaxUtilities.getDefaultSyntaxStyles();

		textColor.setDefaultLabelKey("LblDefaultIndicator");
		bgColor.setDefaultLabelKey("LblDefaultIndicator");

		Color fg = sett.getColor(Settings.PROPERTY_EDITOR_FG_COLOR, null);
		textColor.setSelectedColor(fg);

		Color bg = sett.getColor(Settings.PROPERTY_EDITOR_BG_COLOR, null);
		bgColor.setSelectedColor(bg);

		Color c1 = sett.getColor("workbench.editor.color.comment1", defaultStyles[Token.COMMENT1].getColor());
		blockComments.setSelectedColor(c1);

		Color c2 = sett.getColor("workbench.editor.color.comment2", defaultStyles[Token.COMMENT2].getColor());
		lineComments.setSelectedColor(c2);

		Color k1 = sett.getColor("workbench.editor.color.keyword1", defaultStyles[Token.KEYWORD1].getColor());
		keyword1.setSelectedColor(k1);

		Color k2 = sett.getColor("workbench.editor.color.keyword2", defaultStyles[Token.KEYWORD2].getColor());
		keyword2.setSelectedColor(k2);

		Color k3 = sett.getColor("workbench.editor.color.keyword3", defaultStyles[Token.KEYWORD3].getColor());
		keyword3.setSelectedColor(k3);

		Color l1 = sett.getColor("workbench.editor.color.literal1", defaultStyles[Token.LITERAL1].getColor());
		literals.setSelectedColor(l1);

		Color l2 = sett.getColor("workbench.editor.color.literal2", defaultStyles[Token.LITERAL1].getColor());
		quotedIds.setSelectedColor(l2);

		Color op = sett.getColor("workbench.editor.color.operator", defaultStyles[Token.OPERATOR].getColor());
		operators.setSelectedColor(op);

		Color dt = sett.getEditorDatatypeColor();
		datatypes.setSelectedColor(dt);

		errorColor.setSelectedColor(Settings.getInstance().getEditorErrorColor());
		selectionColor.setSelectedColor(Settings.getInstance().getEditorSelectionColor());
		currLineColor.setSelectedColor(Settings.getInstance().getEditorCurrentLineColor());
		cursorColor.setSelectedColor(Settings.getInstance().getEditorCursorColor());
	}

	@Override
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
		sett.setEditorBackgroundColor(bgColor.getSelectedColor());
		sett.setEditorTextColor(textColor.getSelectedColor());
		sett.setEditorCursorColor(cursorColor.getSelectedColor());
		sett.setEditorDatatypeColor(datatypes.getSelectedColor());
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {
    GridBagConstraints gridBagConstraints;

    syntaxColors = new JPanel();
    lineCommentsLabel = new JLabel();
    wbCommandsLabel = new JLabel();
    keyword2 = new WbColorPicker();
    lineComments = new WbColorPicker();
    literals = new WbColorPicker();
    literalsLabel = new JLabel();
    functionsLabel = new JLabel();
    keyword3 = new WbColorPicker();
    blockCommentsLabel = new JLabel();
    blockComments = new WbColorPicker();
    operatorsLabel = new JLabel();
    operators = new WbColorPicker();
    keywordsLabel = new JLabel();
    keyword1 = new WbColorPicker();
    dataTypesLabel = new JLabel();
    datatypes = new WbColorPicker();
    quoteIdLabel = new JLabel();
    quotedIds = new WbColorPicker();
    editorColors = new JPanel();
    currLineLabel = new JLabel();
    currLineColor = new WbColorPicker(true);
    selectionColorLabel = new JLabel();
    selectionColor = new WbColorPicker();
    errorColorLabel = new JLabel();
    errorColor = new WbColorPicker();
    textColor = new WbColorPicker(true);
    textColorLabel = new JLabel();
    bgColorLabel = new JLabel();
    bgColor = new WbColorPicker(true);
    cursorLabel = new JLabel();
    cursorColor = new WbColorPicker(true);

    setLayout(new GridBagLayout());

    syntaxColors.setBorder(BorderFactory.createTitledBorder(ResourceMgr.getString("LblSyntaxColors"))); // NOI18N
    syntaxColors.setLayout(new GridBagLayout());

    lineCommentsLabel.setText(ResourceMgr.getString("LblColorComment2")); // NOI18N
    lineCommentsLabel.setToolTipText(ResourceMgr.getString("d_LblColorComment2")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(10, 10, 0, 0);
    syntaxColors.add(lineCommentsLabel, gridBagConstraints);

    wbCommandsLabel.setText(ResourceMgr.getString("LblColorKeyword2")); // NOI18N
    wbCommandsLabel.setToolTipText(ResourceMgr.getString("d_LblColorKeyword2")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(10, 5, 5, 0);
    syntaxColors.add(wbCommandsLabel, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new Insets(7, 6, 5, 0);
    syntaxColors.add(keyword2, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(7, 6, 0, 5);
    syntaxColors.add(lineComments, gridBagConstraints);

    literals.setToolTipText(ResourceMgr.getString("d_LblColorLiteral")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new Insets(7, 6, 0, 5);
    syntaxColors.add(literals, gridBagConstraints);

    literalsLabel.setText(ResourceMgr.getString("LblColorLiteral")); // NOI18N
    literalsLabel.setToolTipText(ResourceMgr.getString("d_LblColorLiteral")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(10, 10, 0, 0);
    syntaxColors.add(literalsLabel, gridBagConstraints);

    functionsLabel.setText(ResourceMgr.getString("LblColorKeyword3")); // NOI18N
    functionsLabel.setToolTipText(ResourceMgr.getString("d_LblColorKeyword3")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(10, 5, 0, 0);
    syntaxColors.add(functionsLabel, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(7, 6, 0, 5);
    syntaxColors.add(keyword3, gridBagConstraints);

    blockCommentsLabel.setText(ResourceMgr.getString("LblColorComment1")); // NOI18N
    blockCommentsLabel.setToolTipText(ResourceMgr.getString("d_LblColorComment1")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(10, 10, 0, 0);
    syntaxColors.add(blockCommentsLabel, gridBagConstraints);

    blockComments.setToolTipText(ResourceMgr.getString("d_LblColorComment1")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(7, 6, 0, 5);
    syntaxColors.add(blockComments, gridBagConstraints);

    operatorsLabel.setText(ResourceMgr.getString("LblColorOperator")); // NOI18N
    operatorsLabel.setToolTipText(ResourceMgr.getString("d_LblColorOperator")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(10, 5, 0, 0);
    syntaxColors.add(operatorsLabel, gridBagConstraints);

    operators.setToolTipText(ResourceMgr.getString("d_LblColorOperator")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(7, 6, 0, 0);
    syntaxColors.add(operators, gridBagConstraints);

    keywordsLabel.setText(ResourceMgr.getString("LblColorKeyword1")); // NOI18N
    keywordsLabel.setToolTipText(ResourceMgr.getString("d_LblColorKeyword1")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(10, 5, 0, 0);
    syntaxColors.add(keywordsLabel, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(7, 6, 0, 0);
    syntaxColors.add(keyword1, gridBagConstraints);

    dataTypesLabel.setText(ResourceMgr.getString("LblColorDatatype")); // NOI18N
    dataTypesLabel.setToolTipText(ResourceMgr.getString("d_LblColorDatatype")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(10, 5, 0, 0);
    syntaxColors.add(dataTypesLabel, gridBagConstraints);

    datatypes.setToolTipText(ResourceMgr.getString("d_LblColorOperator")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(7, 6, 0, 0);
    syntaxColors.add(datatypes, gridBagConstraints);

    quoteIdLabel.setText(ResourceMgr.getString("LblColorQuotedIds")); // NOI18N
    quoteIdLabel.setToolTipText(ResourceMgr.getString("d_LblColorQuotedIds")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(10, 10, 0, 0);
    syntaxColors.add(quoteIdLabel, gridBagConstraints);

    quotedIds.setToolTipText(ResourceMgr.getString("d_LblColorQuotedIds")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new Insets(7, 6, 0, 5);
    syntaxColors.add(quotedIds, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new Insets(0, 9, 0, 9);
    add(syntaxColors, gridBagConstraints);

    editorColors.setBorder(BorderFactory.createTitledBorder(ResourceMgr.getString("LblEditorColors"))); // NOI18N
    editorColors.setLayout(new GridBagLayout());

    currLineLabel.setText(ResourceMgr.getString("LblCurrLineColor")); // NOI18N
    currLineLabel.setToolTipText(ResourceMgr.getString("d_LblCurrLineColor")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(9, 10, 0, 0);
    editorColors.add(currLineLabel, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(9, 0, 0, 0);
    editorColors.add(currLineColor, gridBagConstraints);

    selectionColorLabel.setText(ResourceMgr.getString("LblSelectionColor")); // NOI18N
    selectionColorLabel.setToolTipText(ResourceMgr.getString("d_LblSelectionColor")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(9, 5, 0, 0);
    editorColors.add(selectionColorLabel, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(9, 3, 0, 0);
    editorColors.add(selectionColor, gridBagConstraints);

    errorColorLabel.setText(ResourceMgr.getString("LblSelectErrorColor")); // NOI18N
    errorColorLabel.setToolTipText(ResourceMgr.getString("d_LblSelectErrorColor")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(9, 5, 0, 0);
    editorColors.add(errorColorLabel, gridBagConstraints);

    errorColor.setToolTipText(ResourceMgr.getString("d_LblSelectErrorColor")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new Insets(9, 3, 0, 0);
    editorColors.add(errorColor, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(1, 3, 0, 0);
    editorColors.add(textColor, gridBagConstraints);

    textColorLabel.setText(ResourceMgr.getString("LblEditorFgColor")); // NOI18N
    textColorLabel.setToolTipText(ResourceMgr.getString("d_LblEditorFgColor")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(5, 5, 0, 0);
    editorColors.add(textColorLabel, gridBagConstraints);

    bgColorLabel.setText(ResourceMgr.getString("LblEditorBgColor")); // NOI18N
    bgColorLabel.setToolTipText(ResourceMgr.getString("d_LblEditorBgColor")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(5, 10, 0, 0);
    editorColors.add(bgColorLabel, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(1, 0, 0, 0);
    editorColors.add(bgColor, gridBagConstraints);

    cursorLabel.setText(ResourceMgr.getString("LblEditorCursorColor")); // NOI18N
    cursorLabel.setToolTipText(ResourceMgr.getString("d_LblEditorCursorColor")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(9, 10, 0, 0);
    editorColors.add(cursorLabel, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(9, 0, 0, 0);
    editorColors.add(cursorColor, gridBagConstraints);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new Insets(7, 9, 7, 9);
    add(editorColors, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private WbColorPicker bgColor;
  private JLabel bgColorLabel;
  private WbColorPicker blockComments;
  private JLabel blockCommentsLabel;
  private WbColorPicker currLineColor;
  private JLabel currLineLabel;
  private WbColorPicker cursorColor;
  private JLabel cursorLabel;
  private JLabel dataTypesLabel;
  private WbColorPicker datatypes;
  private JPanel editorColors;
  private WbColorPicker errorColor;
  private JLabel errorColorLabel;
  private JLabel functionsLabel;
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
  private JLabel quoteIdLabel;
  private WbColorPicker quotedIds;
  private WbColorPicker selectionColor;
  private JLabel selectionColorLabel;
  private JPanel syntaxColors;
  private WbColorPicker textColor;
  private JLabel textColorLabel;
  private JLabel wbCommandsLabel;
  // End of variables declaration//GEN-END:variables

}
