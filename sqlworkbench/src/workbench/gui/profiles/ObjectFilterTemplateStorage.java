/*
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2015 Thomas Kellerer.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * To contact the author please send an email to: support@sql-workbench.net
 */
package workbench.gui.profiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import workbench.resource.Settings;

import workbench.db.ObjectNameFilter;

/**
 *
 * @author Thomas Kellerer
 */
public class ObjectFilterTemplateStorage
  implements ComboBoxModel<ObjectFilterTemplate>
{
  private final String prefix;
  private final List<ObjectFilterTemplate> templates;
  private ObjectFilterTemplate selectedItem;
  private List<ListDataListener> listener = new ArrayList<>(1);

  public ObjectFilterTemplateStorage(TemplateType type)
  {
    prefix = "workbench.gui.object.filter.template."  + type.name().toLowerCase() + ".";
    List<String> keys = Settings.getInstance().getKeysLike(prefix + "name");
    templates = new ArrayList<>(keys.size());
    for (String key : keys)
    {
      String name = Settings.getInstance().getProperty(key, null);
      String idx = getTemplateIndex(key);
      if (idx != null && name != null)
      {
        String defKey = prefix + idx + ".definition";
        String inclKey = prefix + idx + ".include";
        String def = Settings.getInstance().getProperty(defKey, null);
        boolean include = Settings.getInstance().getBoolProperty(inclKey, true);
        ObjectNameFilter filter = new ObjectNameFilter();
        filter.setExpressionList(def);
        filter.setInclusionFilter(include);
        ObjectFilterTemplate template = new ObjectFilterTemplate(name, filter);
        templates.add(template);
      }
    }
  }

  public void setTemplates(List<ObjectFilterTemplate> templateList)
  {
    templates.clear();
    templates.addAll(templateList);
    fireItemAdded(0, templates.size() - 1);
  }

  public synchronized void saveTemplates()
  {
    if (templates == null) return;

    for (int i=0; i < templates.size(); i++)
    {
      String nameKey = prefix + "name." + Integer.toString(i);
      String defKey = prefix + Integer.toString(i) + ".definition";
      String inclKey = prefix + Integer.toString(i) + ".include";
      ObjectFilterTemplate template = templates.get(i);
      Settings.getInstance().setProperty(nameKey, template.getName());
      Settings.getInstance().setProperty(defKey, template.getFilter().getFilterString());
      Settings.getInstance().setProperty(inclKey, template.getFilter().isInclusionFilter());
    }
  }

  public void addTemplate(String name, ObjectNameFilter filter)
  {
    ObjectFilterTemplate template = new ObjectFilterTemplate(name, filter);
    templates.add(template);
    fireItemAdded();
  }

  public List<ObjectFilterTemplate> getTemplates()
  {
    return Collections.unmodifiableList(templates);
  }

  private String getTemplateIndex(String key)
  {
    if (key == null) return null;
    int pos = key.lastIndexOf('.');
    return key.substring(pos + 1);
  }

  @Override
  public void setSelectedItem(Object anItem)
  {
    selectedItem = (ObjectFilterTemplate)anItem;
  }

  @Override
  public ObjectFilterTemplate getSelectedItem()
  {
    return selectedItem;
  }

  @Override
  public int getSize()
  {
    return templates.size();
  }

  @Override
  public ObjectFilterTemplate getElementAt(int index)
  {
    return templates.get(index);
  }

  @Override
  public void addListDataListener(ListDataListener l)
  {
    listener.add(l);
  }

  @Override
  public void removeListDataListener(ListDataListener l)
  {
    listener.remove(l);
  }

  private void fireItemAdded()
  {
    fireItemAdded(templates.size() - 1, templates.size() - 1);
  }

  private void fireItemAdded(int start, int end)
  {
    if (listener.isEmpty()) return;
    ListDataEvent evt = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, start, end);
    for (ListDataListener l : listener)
    {
      l.contentsChanged(evt);
    }
  }
}
