/*
 * DriverEditorDialog.java
 *
 * Created on 9. Juli 2002, 13:10
 */

package workbench.gui.macros;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionListener;
import workbench.WbManager;
import workbench.exception.WbException;
import workbench.gui.WbSwingUtilities;
import workbench.gui.actions.EscAction;
import workbench.gui.components.WbButton;
import workbench.gui.sql.SqlPanel;
import workbench.resource.ResourceMgr;

public class MacroManagerDialog 
	extends JDialog 
	implements ActionListener, ListSelectionListener
{
	private JPanel dummyPanel;
	private JPanel buttonPanel;
	private JButton okButton;
	private MacroManagerGui macroPanel;
	private JButton cancelButton;
  private boolean cancelled = true;
	private EscAction escAction;
	private boolean runMacro;
	private SqlPanel client;

	public MacroManagerDialog(Frame parent, boolean modal)
	{
		super(parent, modal);
		this.initComponents(false);
		this.initKeys();
		this.initWindow(parent);
	}

	public MacroManagerDialog(Frame parent, SqlPanel aTarget)
	{
		super(parent, true);
		this.initComponents(true);
		this.runMacro = true;
		this.initKeys();
		this.initWindow(parent);
		this.client = aTarget;
	}

	private void initWindow(Frame parent)
	{
		if (!WbManager.getSettings().restoreWindowSize(this))
		{
			this.setSize(600,400);
		}
		WbSwingUtilities.center(this, parent);
		macroPanel.restoreSettings();
	}
	
	private void initKeys()
	{
		this.getRootPane().setDefaultButton(this.okButton);
		InputMap im = this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap am = this.getRootPane().getActionMap();
		escAction = new EscAction(this);
		im.put(escAction.getAccelerator(), escAction.getActionName());
		am.put(escAction.getActionName(), escAction);
	}
	
	private void initComponents(boolean withRunButton)
	{
		macroPanel = new MacroManagerGui();
		buttonPanel = new JPanel();
		if (withRunButton)
		{
			okButton = new WbButton(ResourceMgr.getString("LabelRunMacro"));
		}
		else
		{
			okButton = new WbButton(ResourceMgr.getString(ResourceMgr.TXT_OK));
		}
		cancelButton = new WbButton(ResourceMgr.getString(ResourceMgr.TXT_CANCEL));
		dummyPanel = new JPanel();

		setTitle(ResourceMgr.getString("TxtMacroManagerWindowTitle"));
		setModal(true);
		setName("MacroManagerDialog");
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent evt)
			{
				closeDialog(evt);
			}
		});

		macroPanel.setBorder(new CompoundBorder(new EmptyBorder(1,1,1,1), new EtchedBorder()));
		getContentPane().add(macroPanel, BorderLayout.CENTER);

		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

		okButton.addActionListener(this);
		buttonPanel.add(okButton);

		cancelButton.addActionListener(this);
		buttonPanel.add(cancelButton);

		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		dummyPanel.setMaximumSize(new Dimension(2, 2));
		dummyPanel.setMinimumSize(new Dimension(1, 1));
		dummyPanel.setPreferredSize(new Dimension(2, 2));
		getContentPane().add(dummyPanel, BorderLayout.NORTH);

	}

	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == okButton)
		{
			okButtonActionPerformed(e);
		}
		else if (e.getSource() == cancelButton || e.getActionCommand().equals(escAction.getActionName()))
		{
			cancelButtonActionPerformed(e);
		}
	}
	private void cancelButtonActionPerformed(ActionEvent evt)
	{
    this.cancelled = true;
		this.closeDialog();
	}

	private void okButtonActionPerformed(ActionEvent evt)
	{
		try
		{
			this.macroPanel.saveItem();
			this.cancelled = false;
			this.closeDialog();
			if (this.runMacro && this.client != null)
			{
				String name = this.macroPanel.getSelectedMacroName();
				this.client.executeMacro(name);
			}
		}
		catch (WbException e)
		{
			e.printStackTrace();
		}
	}

  public boolean isCancelled() { return this.cancelled; }
  
	/** Closes the dialog */
	private void closeDialog(WindowEvent evt)
	{
		this.closeDialog();
	}

	public void closeDialog()
	{
		WbManager.getSettings().storeWindowSize(this);
		macroPanel.saveSettings();
		setVisible(false);
	}
	
	public void valueChanged(javax.swing.event.ListSelectionEvent e)
	{
		if (!this.runMacro) return;
		if (e.getValueIsAdjusting()) return;
		this.okButton.setEnabled(e.getFirstIndex() > -1);
	}
	
}