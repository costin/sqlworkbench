/*
 * SpreadSheetOptionsPanel.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2010, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.dialogs.export;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;

/**
 *
 * @author  Thomas Kellerer
 */
public class SpreadSheetOptionsPanel
	extends JPanel
	implements SpreadSheetOptions
{
	private String exportType;

	public SpreadSheetOptionsPanel(String type)
	{
		super();
		exportType = type;
		initComponents();

		if (isAutoFilterAvailable())
		{
			createAutoFilter.setEnabled(true);
		}
		else
		{
			createAutoFilter.setSelected(false);
			createAutoFilter.setEnabled(false);
		}
	}

	private boolean isAutoFilterAvailable()
	{
		return (exportType.equalsIgnoreCase("ods") || exportType.equalsIgnoreCase("xlsm"));
	}
	
	public void saveSettings()
	{
		Settings s = Settings.getInstance();
		s.setProperty("workbench.export." + exportType + ".pagetitle", this.getPageTitle());
		s.setProperty("workbench.export." + exportType + ".header", getExportHeaders());
		s.setProperty("workbench.export." + exportType + ".fixedheader", getCreateFixedHeaders());
		s.setProperty("workbench.export." + exportType + ".autofilter", getCreateAutoFilter());
		s.setProperty("workbench.export." + exportType + ".infosheet", getCreateInfoSheet());
	}

	public void restoreSettings()
	{
		Settings s = Settings.getInstance();
		this.setPageTitle(s.getProperty("workbench.export." + exportType + ".pagetitle", ""));
		boolean headerDefault = s.getBoolProperty("workbench.export." + exportType + ".default.header", false);
		boolean header = s.getBoolProperty("workbench.export." + exportType + ".header", headerDefault);
		this.setExportHeaders(header);
		if (createAutoFilter.isEnabled())
		{
			setCreateAutoFilter(s.getBoolProperty("workbench.export." + exportType + ".autofilter", true));
		}
		setCreateInfoSheet(s.getBoolProperty("workbench.export." + exportType + ".infosheet", false));
		setCreateFixedHeaders(s.getBoolProperty("workbench.export." + exportType + ".fixedheader", true));
	}

	@Override
	public boolean getCreateInfoSheet()
	{
		return createInfosheet.isSelected();
	}

	@Override
	public void setCreateInfoSheet(boolean flag)
	{
		createInfosheet.setSelected(flag);
	}

	@Override
	public boolean getCreateFixedHeaders()
	{
		return freezeHeaders.isSelected();
	}

	@Override
	public void setCreateFixedHeaders(boolean flag)
	{
		freezeHeaders.setSelected(flag);
	}

	@Override
	public boolean getCreateAutoFilter()
	{
		return createAutoFilter.isSelected();
	}

	@Override
	public void setCreateAutoFilter(boolean flag)
	{
		createAutoFilter.setSelected(flag);
	}

	public boolean getExportHeaders()
	{
		return exportHeaders.isSelected();
	}

	public void setExportHeaders(boolean flag)
	{
		exportHeaders.setSelected(flag);
	}

	public String getPageTitle()
	{
		return pageTitle.getText();
	}

	public void setPageTitle(String title)
	{
		pageTitle.setText(title);
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
		GridBagConstraints gridBagConstraints;

    pageTitleLabel = new JLabel();
    pageTitle = new JTextField();
    jPanel1 = new JPanel();
    exportHeaders = new JCheckBox();
    createInfosheet = new JCheckBox();
    freezeHeaders = new JCheckBox();
    createAutoFilter = new JCheckBox();
		FormListener formListener = new FormListener();

    setLayout(new GridBagLayout());

    pageTitleLabel.setText(ResourceMgr.getString("LblSheetName")); // NOI18N
    pageTitleLabel.setHorizontalTextPosition(SwingConstants.LEADING);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(13, 6, 3, 6);
    add(pageTitleLabel, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new Insets(0, 6, 0, 6);
    add(pageTitle, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    add(jPanel1, gridBagConstraints);

    exportHeaders.setText(ResourceMgr.getString("LblExportIncludeHeaders")); // NOI18N
    exportHeaders.setBorder(null);
    exportHeaders.addActionListener(formListener);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new Insets(7, 6, 0, 6);
    add(exportHeaders, gridBagConstraints);

    createInfosheet.setText(ResourceMgr.getString("LblExportInfoSheet")); // NOI18N
    createInfosheet.setToolTipText(ResourceMgr.getString("d_LblExportInfoSheet")); // NOI18N
    createInfosheet.setBorder(null);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(9, 6, 0, 0);
    add(createInfosheet, gridBagConstraints);

    freezeHeaders.setText(ResourceMgr.getString("LblExportFreezeHeader")); // NOI18N
    freezeHeaders.setToolTipText(ResourceMgr.getString("d_LblExportFreezeHeader")); // NOI18N
    freezeHeaders.setBorder(null);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(9, 6, 0, 0);
    add(freezeHeaders, gridBagConstraints);

    createAutoFilter.setText(ResourceMgr.getString("LblExportAutoFilter")); // NOI18N
    createAutoFilter.setToolTipText(ResourceMgr.getString("d_LblExportAutoFilter")); // NOI18N
    createAutoFilter.setBorder(null);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    gridBagConstraints.insets = new Insets(9, 6, 0, 0);
    add(createAutoFilter, gridBagConstraints);
  }

  // Code for dispatching events from components to event handlers.

  private class FormListener implements ActionListener {
    FormListener() {}
    public void actionPerformed(ActionEvent evt) {
      if (evt.getSource() == exportHeaders) {
        SpreadSheetOptionsPanel.this.exportHeadersActionPerformed(evt);
      }
    }
  }// </editor-fold>//GEN-END:initComponents

	private void exportHeadersActionPerformed(ActionEvent evt)//GEN-FIRST:event_exportHeadersActionPerformed
	{//GEN-HEADEREND:event_exportHeadersActionPerformed
		freezeHeaders.setEnabled(exportHeaders.isSelected());
		if (isAutoFilterAvailable())
		{
			createAutoFilter.setEnabled(exportHeaders.isSelected());
		}
	}//GEN-LAST:event_exportHeadersActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private JCheckBox createAutoFilter;
  private JCheckBox createInfosheet;
  private JCheckBox exportHeaders;
  private JCheckBox freezeHeaders;
  private JPanel jPanel1;
  private JTextField pageTitle;
  private JLabel pageTitleLabel;
  // End of variables declaration//GEN-END:variables

}
