/*
 * ConnectionInfoPanel.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2012, Thomas Kellerer
 * No part of this code maybe reused without the permission of the author
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.gui.tools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.StringWriter;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.text.Document;
import workbench.WbManager;
import workbench.db.ConnectionInfoBuilder;
import workbench.db.DriverInfo;
import workbench.db.WbConnection;
import workbench.gui.WbSwingUtilities;
import workbench.gui.components.*;
import workbench.log.LogMgr;
import workbench.resource.ResourceMgr;
import workbench.resource.Settings;
import workbench.util.StringUtil;

/**
 *
 * @author  Thomas Kellerer
 */
public class ConnectionInfoPanel
	extends javax.swing.JPanel
{

	public ConnectionInfoPanel(WbConnection conn)
	{
		super();
		initComponents();

		try
		{
			ConnectionInfoBuilder info = new ConnectionInfoBuilder();

			infotext.setContentType("text/html");
			infotext.setFont(Settings.getInstance().getEditorFont());
			infotext.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
			infotext.setText(info.getHtmlDisplay(conn));
			infotext.setCaretPosition(0);
			new TextComponentMouseListener(infotext);
			FontMetrics fm = infotext.getFontMetrics(infotext.getFont());
			int height = fm.getHeight() * 12 + 40;
			Dimension d = new Dimension(470, height);
			jScrollPane1.setSize(d);
			jScrollPane1.setPreferredSize(d);
			jScrollPane1.setMaximumSize(d);

			DriverInfo drvInfo = new DriverInfo(conn.getSqlConnection());
			WbTable data = new WbTable(true, false, false);
			WbScrollPane scroll = new WbScrollPane(data);
			scroll.setPreferredSize(d);
			scroll.setMaximumSize(d);
			DataStoreTableModel ds = new DataStoreTableModel(drvInfo.getInfo());
			data.setModel(ds, true);
			extendedPanel.add(scroll, BorderLayout.CENTER);
			infoTabs.setTitleAt(0, ResourceMgr.getString("TxtInfoBasic"));
			infoTabs.setTitleAt(1, ResourceMgr.getString("TxtInfoExt"));
		}
		catch (Exception e)
		{
			LogMgr.logError("ConnectionInfoPanel", "Could not read connection properties", e);
		}
	}

	public static void showConnectionInfo(WbConnection con)
	{
		ConnectionInfoPanel p = new ConnectionInfoPanel(con);
		JFrame f = WbManager.getInstance().getCurrentWindow();
		ValidatingDialog d = new ValidatingDialog(f, ResourceMgr.getString("LblConnInfo"), p, false);
		d.pack();
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

        infoTabs = new WbTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        infotext = new InfoEditorPane();
        copyButton = new javax.swing.JButton();
        extendedPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        infotext.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        infotext.setContentType("text/html");
        infotext.setEditable(false);
        infotext.setFont(new java.awt.Font("Dialog", 0, 11));
        jScrollPane1.setViewportView(infotext);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jScrollPane1, gridBagConstraints);

        copyButton.setText(ResourceMgr.getString("LblCopyInfo")); // NOI18N
        copyButton.setToolTipText(ResourceMgr.getString("d_LblCopyInfo")); // NOI18N
        copyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        jPanel1.add(copyButton, gridBagConstraints);

        infoTabs.addTab("Basic", jPanel1);

        extendedPanel.setLayout(new java.awt.BorderLayout());
        infoTabs.addTab("Extended", extendedPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(infoTabs, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

	private void copyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyButtonActionPerformed
		String content = infotext.getText();
		copyText(content);
	}//GEN-LAST:event_copyButtonActionPerformed

	public void copyText(String content)
	{
		String clean = content.replaceAll(StringUtil.REGEX_CRLF, " ");
		clean = clean.replaceAll(" {2,}", "");
		clean = clean.replaceAll("<br>", "\r\n");
		clean = clean.replaceAll("<div[0-9 a-zA-Z;=\\-\":]*>", "");
		clean = clean.replaceAll("</div>", "\r\n");
		clean = clean.replaceAll("<[/a-z]*>", "").trim();
		Clipboard clp = Toolkit.getDefaultToolkit().getSystemClipboard();
		StringSelection sel = new StringSelection(clean);
		clp.setContents(sel, sel);
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton copyButton;
    private javax.swing.JPanel extendedPanel;
    private javax.swing.JTabbedPane infoTabs;
    private javax.swing.JEditorPane infotext;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

	private class InfoEditorPane
		extends JEditorPane
	{
		InfoEditorPane()
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
				getUI().getEditorKit(this).write(out, doc, start, end - start);
			}
			catch (Exception e)
			{
				return null;
			}
			return out.toString();
		}

		@Override
		public void copy()
		{
			String content = getSelection();
			if (content == null) content = getText();
			copyText(content);
		}
	}
}
