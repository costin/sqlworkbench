package workbench.interfaces;

import java.beans.PropertyChangeListener;

/**
 *
 * @author  workbench@kellerer.org
 */
public interface SimplePropertyEditor
{
	void setSourceObject(Object aSource, String aProperty);
	void applyChanges();
	boolean isChanged();
	void addPropertyChangeListener(PropertyChangeListener aListener);
	void removePropertyChangeListener(PropertyChangeListener aListener);
  void setImmediateUpdate(boolean aFlag);
  boolean getImmediateUpdate();
}