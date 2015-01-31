/*
 * LnFDefinitionPanel.java
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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import workbench.gui.WbSwingUtilities;
import workbench.gui.components.ExtensionFileFilter;
import workbench.gui.components.StringPropertyEditor;
import workbench.gui.components.TextComponentMouseListener;
import workbench.gui.components.WbButton;
import workbench.gui.components.WbFilePicker;
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
		f = f.deriveFont(Font.BOLD, (float)(f.getSize() * 1.2));
		infoText.setFont(f);
		String button = changeLnfButton.getText();
		String info = ResourceMgr.getString("TxtChangeLnFInfo").replace("%button%", button);
		infoText.setText(info);
		infoText.setWrapStyleWord(true);
		infoText.setLineWrap(true);
//		infoText.setOpaque(true);
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
		GridBagConstraints gridBagConstraints;

    lblName = new JLabel();
    tfName = new StringPropertyEditor();
    lblClassName = new JLabel();
    tfClassName = new StringPropertyEditor();
    lblLibrary = new JLabel();
    infoText = new JTextArea();
    jSeparator1 = new JSeparator();
    changeLnfButton = new WbButton();
    currentLabel = new HtmlLabel();
    libraryPath = new WbFilePicker();

    setLayout(new GridBagLayout());

    lblName.setLabelFor(tfName);
    lblName.setText(ResourceMgr.getString("LblLnFClass")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(4, 10, 1, 7);
    add(lblName, gridBagConstraints);

    tfName.setHorizontalAlignment(JTextField.LEFT);
    tfName.setName("name"); // NOI18N
    tfName.addMouseListener(new TextComponentMouseListener());
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.EAST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new Insets(4, 3, 1, 3);
    add(tfName, gridBagConstraints);

    lblClassName.setLabelFor(tfClassName);
    lblClassName.setText(ResourceMgr.getString("LblLnFClass")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(2, 10, 0, 7);
    add(lblClassName, gridBagConstraints);

    tfClassName.setColumns(10);
    tfClassName.setHorizontalAlignment(JTextField.LEFT);
    tfClassName.setName("className"); // NOI18N
    tfClassName.addMouseListener(new TextComponentMouseListener());
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(2, 3, 0, 3);
    add(tfClassName, gridBagConstraints);

    lblLibrary.setLabelFor(lblLibrary);
    lblLibrary.setText(ResourceMgr.getString("LblLnFLib")); // NOI18N
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(2, 10, 0, 7);
    add(lblLibrary, gridBagConstraints);

    infoText.setLineWrap(true);
    infoText.setText("Please click on the \"Make current\" button to switch the current Look and Feel");
    infoText.setWrapStyleWord(true);
    infoText.setDisabledTextColor(new Color(0, 0, 0));
    infoText.setOpaque(false);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(11, 10, 0, 10);
    add(infoText, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new Insets(10, 0, 0, 0);
    add(jSeparator1, gridBagConstraints);

    changeLnfButton.setText(ResourceMgr.getString("LblActivateLnf")); // NOI18N
    changeLnfButton.setMaximumSize(new Dimension(200, 50));
    changeLnfButton.setMinimumSize(new Dimension(140, 30));
    changeLnfButton.setPreferredSize(new Dimension(140, 30));
    ((WbButton)changeLnfButton).setResourceKey("LblSwitchLnF");
    changeLnfButton.addActionListener(this);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(10, 8, 0, 0);
    add(changeLnfButton, gridBagConstraints);

    currentLabel.setBackground(new Color(255, 255, 255));
    currentLabel.setBorder(BorderFactory.createEmptyBorder(3, 2, 3, 2));
    currentLabel.setOpaque(true);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.SOUTH;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new Insets(13, 8, 5, 8);
    add(currentLabel, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(3, 3, 0, 3);
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
  public JButton changeLnfButton;
  public JLabel currentLabel;
  public JTextArea infoText;
  public JSeparator jSeparator1;
  public JLabel lblClassName;
  public JLabel lblLibrary;
  public JLabel lblName;
  public WbFilePicker libraryPath;
  public JTextField tfClassName;
  public JTextField tfName;
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