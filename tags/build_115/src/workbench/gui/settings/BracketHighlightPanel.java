/*
 * BracketHighlightPanel.java
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
package workbench.gui.settings;

import java.awt.Color;
import javax.swing.JPanel;
import workbench.gui.components.WbColorPicker;
import workbench.interfaces.Restoreable;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;

/**
 *
 * @author Thomas Kellerer
 */
public class BracketHighlightPanel
	extends JPanel
	implements Restoreable
{

	/** Creates new form BracketHighlightPanel */
	public BracketHighlightPanel()
	{
		initComponents();
	}

	@Override
	public void restoreSettings()
	{
		Settings sett = Settings.getInstance();
		Color hilite = sett.getEditorBracketHighlightColor();
		enableHilite.setSelected(sett.isBracketHighlightEnabled());
		bracketHilite.setSelectedColor(hilite);
		matchLeft.setSelected(sett.getBracketHighlightLeft());
		matchRight.setSelected(!sett.getBracketHighlightLeft());
		hiliteRec.setSelected(sett.getBracketHighlightRectangle());
		hiliteBoth.setSelected(sett.getBracketHighlightBoth());
		hiliteMatching.setSelected(!sett.getBracketHighlightBoth());
	}

	@Override
	public void saveSettings()
	{
		Settings sett = Settings.getInstance();
		sett.setColor(Settings.PROPERTY_EDITOR_BRACKET_HILITE_COLOR, bracketHilite.getSelectedColor());
		sett.setBracketHighlight(enableHilite.isSelected());
		sett.setBracketHighlightLeft(matchLeft.isSelected());
		sett.setBracketHighlightRectangle(hiliteRec.isSelected());
		sett.setBracketHighlightBoth(hiliteBoth.isSelected());
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    matchType = new javax.swing.ButtonGroup();
    hiliteType = new javax.swing.ButtonGroup();
    bracketHiliteLabel = new javax.swing.JLabel();
    bracketHilite = new WbColorPicker(true);
    enableHilite = new javax.swing.JCheckBox();
    hiliteRec = new javax.swing.JCheckBox();
    jPanel1 = new javax.swing.JPanel();
    matchLeft = new javax.swing.JRadioButton();
    matchRight = new javax.swing.JRadioButton();
    jPanel2 = new javax.swing.JPanel();
    hiliteBoth = new javax.swing.JRadioButton();
    hiliteMatching = new javax.swing.JRadioButton();

    setLayout(new java.awt.GridBagLayout());

    bracketHiliteLabel.setText(ResourceMgr.getString("LblBracketHiliteColor")); // NOI18N
    bracketHiliteLabel.setToolTipText(ResourceMgr.getString("d_LblBracketHiliteColor")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(10, 12, 0, 0);
    add(bracketHiliteLabel, gridBagConstraints);

    bracketHilite.setToolTipText(ResourceMgr.getString("d_LblBracketHiliteColor")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
    add(bracketHilite, gridBagConstraints);

    enableHilite.setText(ResourceMgr.getString("LblBracketHilite")); // NOI18N
    enableHilite.setToolTipText(ResourceMgr.getString("d_LblBracketHilite")); // NOI18N
    enableHilite.setBorder(null);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(10, 12, 0, 0);
    add(enableHilite, gridBagConstraints);

    hiliteRec.setText(ResourceMgr.getString("LblBracketHiliteRec")); // NOI18N
    hiliteRec.setToolTipText(ResourceMgr.getString("d_LblBracketHiliteRec")); // NOI18N
    hiliteRec.setBorder(null);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(15, 12, 0, 0);
    add(hiliteRec, gridBagConstraints);

    jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    jPanel1.setLayout(new java.awt.GridBagLayout());

    matchType.add(matchLeft);
    matchLeft.setText(ResourceMgr.getString("LblBracketLeftOfCursor")); // NOI18N
    matchLeft.setToolTipText(ResourceMgr.getString("d_LblBracketLeftOfCursor")); // NOI18N
    matchLeft.setBorder(null);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
    jPanel1.add(matchLeft, gridBagConstraints);

    matchType.add(matchRight);
    matchRight.setText(ResourceMgr.getString("LblBracketRightOfCursor")); // NOI18N
    matchRight.setToolTipText(ResourceMgr.getString("d_LblBracketRightOfCursor")); // NOI18N
    matchRight.setBorder(null);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
    jPanel1.add(matchRight, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 10);
    add(jPanel1, gridBagConstraints);

    jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    jPanel2.setLayout(new java.awt.GridBagLayout());

    hiliteType.add(hiliteBoth);
    hiliteBoth.setText(ResourceMgr.getString("LblBracketHiliteBoth")); // NOI18N
    hiliteBoth.setToolTipText(ResourceMgr.getString("d_LblBracketHiliteBoth")); // NOI18N
    hiliteBoth.setBorder(null);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
    jPanel2.add(hiliteBoth, gridBagConstraints);

    hiliteType.add(hiliteMatching);
    hiliteMatching.setText(ResourceMgr.getString("LblBracketHiliteMatching")); // NOI18N
    hiliteMatching.setToolTipText(ResourceMgr.getString("d_LblBracketHiliteMatching")); // NOI18N
    hiliteMatching.setBorder(null);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
    jPanel2.add(hiliteMatching, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
    add(jPanel2, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private workbench.gui.components.WbColorPicker bracketHilite;
  private javax.swing.JLabel bracketHiliteLabel;
  private javax.swing.JCheckBox enableHilite;
  private javax.swing.JRadioButton hiliteBoth;
  private javax.swing.JRadioButton hiliteMatching;
  private javax.swing.JCheckBox hiliteRec;
  private javax.swing.ButtonGroup hiliteType;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JRadioButton matchLeft;
  private javax.swing.JRadioButton matchRight;
  private javax.swing.ButtonGroup matchType;
  // End of variables declaration//GEN-END:variables

}