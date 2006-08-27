/*
 * LnFDefinitionPanel.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2006, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.settings;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import workbench.gui.WbSwingUtilities;
import workbench.gui.components.ExtensionFileFilter;
import workbench.gui.components.FlatButton;
import workbench.gui.components.StringPropertyEditor;
import workbench.gui.components.TextComponentMouseListener;
import workbench.gui.components.WbButton;
import workbench.gui.lnf.LnFDefinition;
import workbench.gui.lnf.LnFLoader;
import workbench.interfaces.SimplePropertyEditor;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
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
	private JLabel currentLabel;
	
	/** Creates new form BeanForm */
	public LnFDefinitionPanel()
	{
		initComponents();
		String text = ResourceMgr.getDescription("LblLnFLib");
		text = text.replaceAll("%path_sep%", StringUtil.PATH_SEPARATOR);
		lblLibrary.setToolTipText(text);
		tfLibrary.setToolTipText(text);
		text = ResourceMgr.getDescription("SelectLnfLib");
		selectLibButton.setToolTipText(text);
		tfName.addFocusListener(new FocusAdapter()
		{
			public void focusLost(FocusEvent evt)
			{
				nameFieldFocusLost(evt);
			}
		});

		String button = changeLnfButton.getText();
		String info = ResourceMgr.getString("TxtChangeLnFInfo").replaceAll("%button%", button);
		infoText.setText(info);
		infoText.setWrapStyleWord(true);
		infoText.setLineWrap(true);
		infoText.setOpaque(true);
		infoText.setBackground(this.getBackground());
	}

	public void setCurrentInfoDisplay(JLabel label)
	{
		this.currentLabel = label;
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
		this.tfLibrary.setEnabled(flag);
		this.tfName.setEnabled(flag);
		this.selectLibButton.setEnabled(flag);
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
  // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
  private void initComponents()
  {
    java.awt.GridBagConstraints gridBagConstraints;

    lblName = new javax.swing.JLabel();
    tfName = new StringPropertyEditor();
    lblClassName = new javax.swing.JLabel();
    tfClassName = new StringPropertyEditor();
    lblLibrary = new javax.swing.JLabel();
    tfLibrary = new StringPropertyEditor();
    selectLibButton = new FlatButton();
    infoText = new javax.swing.JTextArea();
    jSeparator1 = new javax.swing.JSeparator();
    changeLnfButton = new WbButton();

    setLayout(new java.awt.GridBagLayout());

    lblName.setText(ResourceMgr.getString("LblLnFName"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(15, 10, 1, 7);
    add(lblName, gridBagConstraints);

    tfName.setHorizontalAlignment(javax.swing.JTextField.LEFT);
    tfName.setMinimumSize(new java.awt.Dimension(50, 20));
    tfName.setName("name");
    tfName.setPreferredSize(new java.awt.Dimension(100, 20));
    tfName.addMouseListener(new TextComponentMouseListener());
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(11, 3, 1, 3);
    add(tfName, gridBagConstraints);

    lblClassName.setText(ResourceMgr.getString("LblLnFClass"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(1, 10, 0, 7);
    add(lblClassName, gridBagConstraints);

    tfClassName.setColumns(10);
    tfClassName.setHorizontalAlignment(javax.swing.JTextField.LEFT);
    tfClassName.setName("className");
    tfClassName.addMouseListener(new TextComponentMouseListener());
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(1, 3, 0, 3);
    add(tfClassName, gridBagConstraints);

    lblLibrary.setText(ResourceMgr.getString("LblLnFLib"));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 10, 0, 7);
    add(lblLibrary, gridBagConstraints);

    tfLibrary.setColumns(10);
    tfLibrary.setHorizontalAlignment(javax.swing.JTextField.LEFT);
    tfLibrary.setName("library");
    tfLibrary.addMouseListener(new TextComponentMouseListener());
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(2, 3, 0, 3);
    add(tfLibrary, gridBagConstraints);

    selectLibButton.setText("...");
    selectLibButton.setMaximumSize(new java.awt.Dimension(20, 20));
    selectLibButton.setMinimumSize(new java.awt.Dimension(20, 20));
    selectLibButton.setPreferredSize(new java.awt.Dimension(20, 20));
    selectLibButton.addActionListener(this);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 3);
    add(selectLibButton, gridBagConstraints);

    infoText.setEditable(false);
    infoText.setLineWrap(true);
    infoText.setText("Please click on the \"Make current\" button to switch the current Look and Feel");
    infoText.setWrapStyleWord(true);
    infoText.setDisabledTextColor(new java.awt.Color(0, 0, 0));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(11, 10, 0, 10);
    add(infoText, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
    add(jSeparator1, gridBagConstraints);

    changeLnfButton.setText("Make Current");
    ((WbButton)changeLnfButton).setResourceKey("LblSwitchLnF");
    changeLnfButton.addActionListener(this);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(10, 8, 0, 0);
    add(changeLnfButton, gridBagConstraints);

  }

  // Code for dispatching events from components to event handlers.

  public void actionPerformed(java.awt.event.ActionEvent evt)
  {
    if (evt.getSource() == selectLibButton)
    {
      LnFDefinitionPanel.this.selectLibrary(evt);
    }
    else if (evt.getSource() == changeLnfButton)
    {
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
			Settings.getInstance().setLookAndFeelClass(className);
			WbSwingUtilities.showMessage(this, ResourceMgr.getString("MsgLnFChanged"));
		}
		else
		{
			WbSwingUtilities.showErrorMessage(this, ResourceMgr.getString("MsgLnFNotLoaded"));
		}
	}//GEN-LAST:event_changeLnfButtonActionPerformed

	private void selectLibrary(java.awt.event.ActionEvent evt)//GEN-FIRST:event_selectLibrary
	{//GEN-HEADEREND:event_selectLibrary
		JFileChooser jf = new JFileChooser();
		jf.setMultiSelectionEnabled(true);
		jf.setFileFilter(ExtensionFileFilter.getJarFileFilter());
		int answer = jf.showOpenDialog(SwingUtilities.getWindowAncestor(this));
		if (answer == JFileChooser.APPROVE_OPTION)
		{
			File[] f = jf.getSelectedFiles();
			StringBuffer path = new StringBuffer(f.length * 100);
			for (int i=0; i < f.length; i++)
			{
				if (i>0) path.append(StringUtil.PATH_SEPARATOR);
				path.append(f[i].getAbsolutePath().trim());
			}
			this.tfLibrary.setText(path.toString());
		}
	}//GEN-LAST:event_selectLibrary

	private void initPropertyEditors()
	{
		for (int i=0; i < this.getComponentCount(); i++)
		{
			Component c = this.getComponent(i);
			if (c instanceof SimplePropertyEditor)
			{
				SimplePropertyEditor editor = (SimplePropertyEditor)c;
				String property = c.getName();
				if (!StringUtil.isEmptyString(property))
				{
					editor.setSourceObject(this.currentLnF, property);
					editor.setImmediateUpdate(true);
				}
			}
		}
	}


	public void setDefinition(LnFDefinition lnf)
	{
		this.currentLnF = lnf;
		initPropertyEditors();
		this.setEnabled(!lnf.isBuiltInLnF());
	}

	public LnFDefinition getDefinition()
	{
		return this.currentLnF;
	}

  // Variables declaration - do not modify//GEN-BEGIN:variables
  public javax.swing.JButton changeLnfButton;
  public javax.swing.JTextArea infoText;
  public javax.swing.JSeparator jSeparator1;
  public javax.swing.JLabel lblClassName;
  public javax.swing.JLabel lblLibrary;
  public javax.swing.JLabel lblName;
  public javax.swing.JButton selectLibButton;
  public javax.swing.JTextField tfClassName;
  public javax.swing.JTextField tfLibrary;
  public javax.swing.JTextField tfName;
  // End of variables declaration//GEN-END:variables


}
