/*
 * ExportFileDialog.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2005, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: info@sql-workbench.net
 *
 */
package workbench.gui.dialogs.dataimport;

import java.awt.Component;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import workbench.db.exporter.DataExporter;
import workbench.db.importer.DataImporter;
import workbench.db.importer.ProducerFactory;

import workbench.gui.components.ExtensionFileFilter;
import workbench.gui.dialogs.export.ExportOptions;
import workbench.gui.dialogs.export.TextOptions;
import workbench.gui.dialogs.export.XmlOptions;
import workbench.log.LogMgr;
import workbench.resource.Settings;
import workbench.storage.ResultInfo;


/**
 * @author info@sql-workbench.net
 */
public class ImportFileDialog
	implements PropertyChangeListener
{
	private int importType = -1;
	private String selectedFilename = null;
	private boolean isCancelled = false;
	private Settings settings = Settings.getInstance();
	private ImportOptionsPanel importOptions;
	private JFileChooser chooser;
	private boolean filterChange = false;
	private String lastDirConfigKey = "workbench.import.lastdir";
	private Component parentComponent;
	
	public ImportFileDialog(Component caller)
	{
		this.importOptions = new ImportOptionsPanel();
		this.parentComponent = caller;
	}

	public void saveSettings()
	{
		importOptions.saveSettings();
	}
	
	public void restoreSettings()
	{
		importOptions.restoreSettings();
	}

	public XmlImportOptions getXmlOptions()
	{
		return importOptions.getXmlOptions();
	}
	
	public TextImportOptions getTextOptions()
	{
		return importOptions.getTextOptions();
	}
	
	public ImportOptions getGeneralOptions()
	{
		return importOptions.getGeneralOptions();
	}
	
	public String getSelectedFilename()
	{
		return this.selectedFilename;
	}
	
	public int getImportType()
	{
		return this.importType;
	}

	public boolean isCancelled()
	{
		return this.isCancelled;
	}
	
	/**
	 *	Set the config key for the Settings object
	 *  where the selected directory should be stored
	 */
	public void setConfigKey(String key)
	{
		this.lastDirConfigKey = key;
	}
	
	public boolean selectInput()
	{
		return this.selectInput(null);
	}
	
	public boolean selectInput(String title)
	{
		this.importType = -1;
		this.selectedFilename = null;
		boolean result = false;
		
		String lastDir = settings.getProperty(lastDirConfigKey, null);
		this.chooser = new JFileChooser(lastDir);
		if (title != null) this.chooser.setDialogTitle(title);
		
		chooser.addChoosableFileFilter(ExtensionFileFilter.getTextFileFilter());
		chooser.addChoosableFileFilter(ExtensionFileFilter.getXmlFileFilter());
		chooser.addPropertyChangeListener("fileFilterChanged", this);
		chooser.setFileFilter(ExtensionFileFilter.getTextFileFilter());
		this.importOptions.addPropertyChangeListener("exportType", this);
		this.restoreSettings();
		this.importOptions.setTypeText();
			
		chooser.setAccessory(this.importOptions);
		
		Window parentWindow = SwingUtilities.getWindowAncestor(this.parentComponent);

		int answer = chooser.showOpenDialog(parentWindow);
		if (answer == JFileChooser.APPROVE_OPTION)
		{
			String filename = null;
			this.isCancelled = false;
			File fl = chooser.getSelectedFile();
			
			FileFilter ff = chooser.getFileFilter();
			if (ff instanceof ExtensionFileFilter)
			{
				ExtensionFileFilter eff = (ExtensionFileFilter)ff;
				filename = fl.getAbsolutePath();

				String ext = ExtensionFileFilter.getExtension(fl);
				if (ext.length() == 0)
				{
					if (!filename.endsWith(".")) filename = filename + ".";
					filename = filename + eff.getDefaultExtension();
				}
				this.importType = this.getImportType(ff);
			}
			else
			{
				filename = fl.getAbsolutePath();
				this.importType = this.importOptions.getImportType();
			}
			lastDir = chooser.getCurrentDirectory().getAbsolutePath();
				
			settings.setProperty(this.lastDirConfigKey, lastDir);
			this.saveSettings();
			this.selectedFilename = filename;
			result = true;
		}
		else
		{
			this.isCancelled = true;
			result = false;
		}
		return result;
	}

	public void setImportOptions(ProducerFactory factory)
	{
		switch (this.importType)
		{
			case ProducerFactory.IMPORT_TEXT:
				break;
			case ProducerFactory.IMPORT_XML:
				break;
		}
	}
	
	private int getImportType(FileFilter ff)
	{
		if (ff == ExtensionFileFilter.getXmlFileFilter())
		{
			return ProducerFactory.IMPORT_XML;
		}
		else if (ff == ExtensionFileFilter.getTextFileFilter())
		{
			return ProducerFactory.IMPORT_TEXT;
		}
		return -1;
	}
	
	public void propertyChange(PropertyChangeEvent evt) 
	{
		if (this.importOptions == null) return;
		
		if (evt.getSource() instanceof JFileChooser && !filterChange)
		{
			JFileChooser fc = (JFileChooser)evt.getSource();
			FileFilter ff = fc.getFileFilter();
			if (ff instanceof ExtensionFileFilter)
			{
				ExtensionFileFilter eff = (ExtensionFileFilter)ff;
				int type = this.getImportType(eff);
				this.importOptions.setImportType(type);
			}
		}
		else if (evt.getSource() == this.importOptions && this.chooser != null)
		{
			try
			{
				FileFilter ff = this.chooser.getFileFilter();
				// check for All file (*.*) filter. In that
				// case we do not change the current filter.
				if (!(ff instanceof ExtensionFileFilter)) return;
				
				Integer newvalue = (Integer)evt.getNewValue();
				int type = (newvalue == null ? -1 : newvalue.intValue());
				this.filterChange = true;
				
				switch (type)
				{
					case ProducerFactory.IMPORT_XML:
						this.chooser.setFileFilter(ExtensionFileFilter.getXmlFileFilter());
						break;
					case ProducerFactory.IMPORT_TEXT:
						this.chooser.setFileFilter(ExtensionFileFilter.getTextFileFilter());
						break;
				}
			}
			catch (Throwable th)
			{
				LogMgr.logError("ImportFileDialog.propertyChange", "Error: ", th);
			}
			finally 
			{
				this.filterChange = false;
			}
		}
	}

}