/*
 * WbColorPicker.java
 *
 * Created on 22. Februar 2006, 20:44
 */

package workbench.gui.components;

import java.awt.Color;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import workbench.resource.ResourceMgr;

/**
 *
 * @author  thomas
 */
public class WbColorPicker extends javax.swing.JPanel
{
	
	/** Creates new form WbColorPicker */
	public WbColorPicker()
	{
		initComponents();
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
  // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
  private void initComponents()
  {
    samplePanel = new javax.swing.JPanel();
    selectColor = new javax.swing.JButton();

    setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));

    samplePanel.setBackground(new java.awt.Color(255, 255, 255));
    samplePanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    samplePanel.setMaximumSize(new java.awt.Dimension(18, 18));
    samplePanel.setMinimumSize(new java.awt.Dimension(18, 18));
    samplePanel.setPreferredSize(new java.awt.Dimension(18, 18));
    add(samplePanel);

    selectColor.setText("...");
    selectColor.setBorder(javax.swing.BorderFactory.createEtchedBorder());
    selectColor.setMargin(new java.awt.Insets(0, 0, 0, 0));
    selectColor.setMaximumSize(new java.awt.Dimension(22, 22));
    selectColor.setMinimumSize(new java.awt.Dimension(22, 22));
    selectColor.setPreferredSize(new java.awt.Dimension(22, 22));
    selectColor.addMouseListener(new java.awt.event.MouseAdapter()
    {
      public void mouseClicked(java.awt.event.MouseEvent evt)
      {
        selectColorMouseClicked(evt);
      }
    });

    add(selectColor);

  }// </editor-fold>//GEN-END:initComponents

	private void selectColorMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_selectColorMouseClicked
	{//GEN-HEADEREND:event_selectColorMouseClicked
		Color newColor = JColorChooser.showDialog(SwingUtilities.getWindowAncestor(this), ResourceMgr.getString("TxtSelectColor"), this.getSelectedColor());
		if (newColor != null)
		{
			this.setColor(newColor);
		}
	}//GEN-LAST:event_selectColorMouseClicked
	
	public void setColor(Color c)
	{
		samplePanel.setBackground(c);
	}
	
	public Color getSelectedColor()
	{
		return samplePanel.getBackground();
	}
	
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel samplePanel;
  private javax.swing.JButton selectColor;
  // End of variables declaration//GEN-END:variables
	
}
