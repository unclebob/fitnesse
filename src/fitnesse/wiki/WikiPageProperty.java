// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import util.StringUtil;

public class WikiPageProperty implements Serializable {
  private static final long serialVersionUID = 1L;

  private String value;
  protected HashMap<String, WikiPageProperty> children;

  public WikiPageProperty() {
  }

  public WikiPageProperty(String value) {
    setValue(value);
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = StringUtil.trimNonNullString(value);
  }

  public void set(String name, WikiPageProperty child) {
    if (children == null)
      children = new HashMap<String, WikiPageProperty>();
    children.put(name, child);
  }

  public WikiPageProperty set(String name, String value) {
    WikiPageProperty child = new WikiPageProperty(value);
    set(name, child);
    return child;
  }

  public WikiPageProperty set(String name) {
    return set(name, (String) null);
  }

  public void remove(String name) {
    children.remove(name);
  }

  public WikiPageProperty getProperty(String name) {
    if (children == null)
      return null;
    else
      return children.get(name);
  }

  public String get(String name) {
    WikiPageProperty child = getProperty(name);
    return child == null ? null : child.getValue();
  }

  public boolean has(String name) {
    return children != null && children.containsKey(name);
  }

  public Set<String> keySet() {
    return children == null ? new HashSet<String>() : children.keySet();
  }

  public String toString() {
    return toString("WikiPageProperty root", 0);
  }

  protected String toString(String key, int depth) {
    StringBuffer buffer = new StringBuffer();

    for (int i = 0; i < depth; i++)
      buffer.append("\t");
    buffer.append(key);
    if (getValue() != null)
      buffer.append(" = ").append(getValue());
    buffer.append("\n");

    for (Iterator<?> iterator = keySet().iterator(); iterator.hasNext();) {
      String childKey = (String) iterator.next();
      WikiPageProperty value = getProperty(childKey);
      if (value != null)
        buffer.append(value.toString(childKey, depth + 1));
    }
    return buffer.toString();
  }

  public boolean hasChildren() {
    return children != null && children.size() > 0;
  }

  public static SimpleDateFormat getTimeFormat() {
    //SimpleDateFormat is not thread safe, so we need to create each instance independently.
    return new SimpleDateFormat("yyyyMMddHHmmss");
  }
}
