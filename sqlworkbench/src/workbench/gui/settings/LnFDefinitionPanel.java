/*
 * LnFDefinitionPanel.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2008, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.settings;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import workbench.gui.WbSwingUtilities;
import workbench.gui.components.ExtensionFileFilter;
import workbench.gui.components.StringPropertyEditor;
import workbench.gui.components.TextComponentMouseListener;
import workbench.gui.components.WbButton;
import workbench.gui.lnf.LnFDefinition;
import workbench.gui.lnf.LnFLoader;
import workbench.resource.GuiSettings;
import workbench.resource.ResourceMgr;
import workbench.util.StringUtil;

/**
 *
 * @author  support@sql-workbench.net
 */
public class LnFDefinitionPanel
	extends JPanel
	implements ActionListener
{
	private LnFDefinition currentLnF;
	private PropertyChangeListener changeListener;

	public LnFDefinitionPanel()
	{
		super();
		initComponents();
		String text = ResourceMgr.getDescription("LblLnFLib");
		text = text.replace("%path_sep%", StringUtil.getPathSeparator());
		lblLibrary.setToolTipText(text);
		libraryPath.setTextFieldPropertyName("library");
		libraryPath.setTextfieldTooltip(text);
		text = ResourceMgr.getDescription("SelectLnfLib");
		libraryPath.setButtonTooltip(text);
		libraryPath.setFileFilter(ExtensionFileFilter.getJarFileFilter());
		libraryPath.setAllowMultiple(true);
		libraryPath.setLastDirProperty("workbench.lnf.lastdir");
		tfName.addFocusListener(new FocusAdapter()
		{
			public void focusLost(FocusEvent evt)
			{
				nameFieldFocusLost(evt);
			}
		});

		Font f = UIManager.getDefaults().getFont("Label.font");
		f = f.deriveFont((float)(f.getSize() * 1.1));
		infoText.setFont(f);
		String button = changeLnfButton.getText();
		String info = ResourceMgr.getString("TxtChangeLnFInfo").replace("%button%", button);
		infoText.setText(info);
		infoText.setWrapStyleWord(true);
		infoText.setLineWrap(true);
		infoText.setOpaque(true);
		infoText.setBackground(this.getBackground());
	}

	public void setPropertyListener(PropertyChangeListener l)
	{
		this.changeListener = l;
	}

	public void nameFieldFocusLost(FocusEvent evt)
	{
		if (this.changeListener != null)
		{
			PropertyChangeEvent pEvt = new PropertyChangeEvent(this.currentLnF, "name", null, tfName.getText());
			this.changeListener.propertyChange(pEvt);
		}
	}

	public void setEnabled(boolean flag)
	{
		this.tfClassName.setEnabled(flag);
		this.libraryPath.setEnabled(flag);
		this.tfName.setEnabled(flag);
	}

	private boolean testLnF(LnFDefinition lnf)
	{
		try
		{
			LnFLoader loader = new LnFLoader(lnf);
			return loader.isAvailable();
		}
		catch (Exception e)
		{
			return false;
		}
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    lblName = new javax.swing.JLabel();
    tfName = new StringPropertyEditor();
    lblClassName = new javax.swing.JLabel();
    tfClassName = new StringPropertyEditor();
    lblLibrary = new javax.swing.JLabel();
    infoText = new javax.swing.JTextArea();
    jSeparator1 = new javax.swing.JSeparator();
    changeLnfButton = new WbButton();
    currentLabel = new HtmlLabel();
    libraryPath = new workbench.gui.components.WbFilePicker();

    setLayout(new java.awt.GridBagLayout());

    lblName.setText(ResourceMgr.getString("LblLnFName"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 10, 1, 7);
    add(lblName, gridBagConstraints);

    tfName.setHorizontalAlignment(javax.swing.JTextField.LEFT);
    tfName.setMinimumSize(new java.awt.Dimension(50, 20));
    tfName.setName("name"); // NOI18N
    tfName.setPreferredSize(new java.awt.Dimension(100, 20));
    tfName.addMouseListener(new TextComponentMouseListener());
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(4, 3, 1, 3);
    add(tfName, gridBagConstraints);

    lblClassName.setText(ResourceMgr.getString("LblLnFClass"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 10, 0, 7);
    add(lblClassName, gridBagConstraints);

    tfClassName.setColumns(10);
    tfClassName.setHorizontalAlignment(javax.swing.JTextField.LEFT);
    tfClassName.setName("className"); // NOI18N
    tfClassName.addMouseListener(new TextComponentMouseListener());
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 3, 0, 3);
    add(tfClassName, gridBagConstraints);

    lblLibrary.setText(ResourceMgr.getString("LblLnFLib"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 10, 0, 7);
    add(lblLibrary, gridBagConstraints);

    infoText.setEditable(false);
    infoText.setLineWrap(true);
    infoText.setText("Please click on the \"Make current\" button to switch the current Look and Feel");
    infoText.setWrapStyleWord(true);
    infoText.setDisabledTextColor(new java.awt.Color(0, 0, 0));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(11, 10, 0, 10);
    add(infoText, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
    add(jSeparator1, gridBagConstraints);

    changeLnfButton.setText(ResourceMgr.getString("LblActivateLnf"));
    ((WbButton)changeLnfButton).setResourceKey("LblSwitchLnF");
    changeLnfButton.addActionListener(this);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(10, 8, 0, 0);
    add(changeLnfButton, gridBagConstraints);

    currentLabel.setBackground(new java.awt.Color(255, 255, 255));
    currentLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 2, 3, 2));
    currentLabel.setOpaque(true);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(13, 8, 0, 8);
    add(currentLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 3);
    add(libraryPath, gridBagConstraints);
  }

  // Code for dispatching events from components to event handlers.

  public void actionPerformed(java.awt.event.ActionEvent evt) {
    if (evt.getSource() == changeLnfButton) {
      LnFDefinitionPanel.this.changeLnfButtonActionPerformed(evt);
    }
  }// </editor-fold>//GEN-END:initComponents

	private void changeLnfButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_changeLnfButtonActionPerformed
	{//GEN-HEADEREND:event_changeLnfButtonActionPerformed
		LnFDefinition lnf = getDefinition();
		if (testLnF(lnf))
		{
			String className = lnf.getClassName();
			this.currentLabel.setText(lnf.getName());
			GuiSettings.setLookAndFeelClass(className);
			WbSwingUtilities.showMessage(SwingUtilities.getWindowAncestor(this), ResourceMgr.getString("MsgLnFChanged"));
		}
		else
		{
			WbSwingUtilities.showErrorMessageKey(this, "MsgLnFNotLoaded");
		}
	}//GEN-LAST:event_changeLnfButtonActionPerformed

	public void setCurrentLookAndFeeld(LnFDefinition lnf)
	{
		if (lnf != null) currentLabel.setText(lnf.getName());
	}

	public void setDefinition(LnFDefinition lnf)
	{
		this.currentLnF = lnf;
		WbSwingUtilities.initPropertyEditors(this.currentLnF, this);
		libraryPath.setFilename(lnf.getLibrary());
		this.setEnabled(!lnf.isBuiltInLnF());
	}

	public LnFDefinition getDefinition()
	{
		return this.currentLnF;
	}

  // Variables declaration - do not modify//GEN-BEGIN:variables
  public javax.swing.JButton changeLnfButton;
  public javax.swing.JLabel currentLabel;
  public javax.swing.JTextArea infoText;
  public javax.swing.JSeparator jSeparator1;
  public javax.swing.JLabel lblClassName;
  public javax.swing.JLabel lblLibrary;
  public javax.swing.JLabel lblName;
  public workbench.gui.components.WbFilePicker libraryPath;
  public javax.swing.JTextField tfClassName;
  public javax.swing.JTextField tfName;
  // End of variables declaration//GEN-END:variables

	static class HtmlLabel
		extends JLabel
	{
		public void setText(String name)
		{
			setBackground(Color.WHITE);
			setForeground(Color.BLACK);
			super.setText("<html>" + ResourceMgr.getString("LblCurrLnf") + " <b>" + name + "</b></html>");
		}
	}

}
