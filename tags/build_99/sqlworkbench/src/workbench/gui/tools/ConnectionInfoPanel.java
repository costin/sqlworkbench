/*
 * ConnectionInfoPanel.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2008, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.tools;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.StringWriter;
import java.sql.DatabaseMetaData;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.text.Document;
import workbench.WbManager;
import workbench.db.DbMetadata;
import workbench.db.WbConnection;
import workbench.gui.WbSwingUtilities;
import workbench.gui.components.TextComponentMouseListener;
import workbench.gui.components.ValidatingDialog;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.util.StringUtil;

/**
 *
 * @author  support@sql-workbench.net
 */
public class ConnectionInfoPanel
	extends javax.swing.JPanel
{

	public ConnectionInfoPanel(WbConnection conn)
	{
		initComponents();
		
		try
		{
			StringBuilder content = new StringBuilder();
			content.append("<html>");
			
			DatabaseMetaData meta = conn.getSqlConnection().getMetaData();
			DbMetadata wbmeta = conn.getMetadata();

			content.append("<b>" + ResourceMgr.getString("LblDbProductName") + ":</b> " + meta.getDatabaseProductName() + "<br>\r\n");
			content.append("<b>" + ResourceMgr.getString("LblDbProductInfo") + ":</b> " + meta.getDatabaseProductVersion() + "<br>\r\n");
			content.append("<b>" + ResourceMgr.getString("LblDbProductVersion") + ":</b> " + conn.getDatabaseVersion() + "<br>\r\n");
			content.append("<b>" + ResourceMgr.getString("LblDriverInfoName") + ":</b> " + meta.getDriverName() + "<br>\r\n");
			content.append("<b>" + ResourceMgr.getString("LblDriverInfoVersion") + ":</b> " + meta.getDriverVersion() + "<br>\r\n");
			content.append("<b>" + ResourceMgr.getString("LblDbURL") + ":</b> " + conn.getUrl() + "<br>\r\n");
			content.append("<b>" + ResourceMgr.getString("LblUsername") + ":</b> " + conn.getCurrentUser() + "<br>\r\n");
			content.append("<b>" + StringUtil.capitalize(wbmeta.getSchemaTerm()) + ":</b> " + getDisplayValue(conn.getCurrentSchema()) + "<br>\r\n");
			content.append("<b>" + StringUtil.capitalize(wbmeta.getCatalogTerm()) + ":</b> " + getDisplayValue(wbmeta.getCurrentCatalog()) + "<br>\r\n");
			content.append("</html>");
			infotext.setContentType("text/html");
			infotext.setFont(Settings.getInstance().getEditorFont());
			infotext.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
			infotext.setText(content.toString());
			infotext.setCaretPosition(0);
			new TextComponentMouseListener(infotext);
		}
		catch (Exception e)
		{
			LogMgr.logError("ConnectionInfoPanel", "Could not read connection properties", e);
		}
	}

	private String getDisplayValue(String value)
	{
		if (value == null) return "";
		return value;
	}
	public static void showConnectionInfo(WbConnection con)
	{
		ConnectionInfoPanel p = new ConnectionInfoPanel(con);
		JFrame f = WbManager.getInstance().getCurrentWindow();
		ValidatingDialog d = new ValidatingDialog(f, ResourceMgr.getString("LblConnInfo"), p, false);
		d.setSize(450,350);
		WbSwingUtilities.center(d, f);
		d.setVisible(true);
		d.dispose();
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    jScrollPane1 = new javax.swing.JScrollPane();
    infotext = new InfoEditorPane();
    copyButton = new javax.swing.JButton();

    setLayout(new java.awt.GridBagLayout());

    infotext.setContentType("text/html");
    infotext.setEditable(false);
    infotext.setFont(new java.awt.Font("Dialog", 0, 11));
    jScrollPane1.setViewportView(infotext);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
    add(jScrollPane1, gridBagConstraints);

    copyButton.setText(ResourceMgr.getString("LblCopyInfo"));
    copyButton.setToolTipText(ResourceMgr.getDescription("LblCopyInfo"));
    copyButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        copyButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    add(copyButton, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents

	private void copyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyButtonActionPerformed
		String content = infotext.getText();
		copyText(content);
	}//GEN-LAST:event_copyButtonActionPerformed

	public void copyText(String content)
	{
		String clean = content.replaceAll(StringUtil.REGEX_CRLF, " ");
		clean = clean.replaceAll(" {2,}", " ");
		clean = clean.replaceAll("\\<br\\>", "\r\n");
		clean = clean.replaceAll("\\<[/a-z]*\\>", "").trim();
		Clipboard clp = Toolkit.getDefaultToolkit().getSystemClipboard();
		StringSelection sel = new StringSelection(clean);
		clp.setContents(sel, sel);
	}
	
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton copyButton;
  private javax.swing.JEditorPane infotext;
  private javax.swing.JScrollPane jScrollPane1;
  // End of variables declaration//GEN-END:variables

	private class InfoEditorPane
		extends JEditorPane
	{
		public InfoEditorPane()
		{
			super();
		}
		
		public String getSelection()
		{
			Document doc = getDocument();
			int start = Math.min(getCaret().getDot(), getCaret().getMark());
			int end = Math.max(getCaret().getDot(), getCaret().getMark());
			if (start == end) return null; 
			StringWriter out = new StringWriter();
			try
			{
				int len = getSelectionEnd() - getSelectionStart();
				getUI().getEditorKit(this).write(out, doc, start, end - start);
			}
			catch (Exception e)
			{
				return null;
			}
			return out.toString();
		}
		
		public void copy()
		{
			String content = getSelection();
			if (content == null) content = getText();
			copyText(content);
		}
	}	
}
