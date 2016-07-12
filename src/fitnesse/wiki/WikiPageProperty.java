// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.lang.StringUtils;

import fitnesse.util.Clock;

public class WikiPageProperty implements Serializable {
  private static final long serialVersionUID = 1L;

  public static final String LAST_MODIFIED = "LastModified";
  public static final String LAST_MODIFYING_USER = "LastModifyingUser";
  public static final String HELP = "Help";
  public static final String PRUNE = "Prune";
  public static final String SEARCH = "Search";
  public static final String RECENT_CHANGES = "RecentChanges";
  public static final String FILES = "Files";
  public static final String WHERE_USED = "WhereUsed";
  public static final String REFACTOR = "Refactor";
  public static final String PROPERTIES = "Properties";
  public static final String VERSIONS = "Versions";
  public static final String EDIT = "Edit";
  public static final String SUITES = "Suites";

  public static final String SECURE_READ = "secure-read";
  public static final String SECURE_WRITE = "secure-write";
  public static final String SECURE_TEST = "secure-test";

  private String value;
  private SortedMap<String, WikiPageProperty> children = new TreeMap<>();

  public WikiPageProperty() {
  }

  public WikiPageProperty(String value) {
    setValue(value);
  }

  public WikiPageProperty(WikiPageProperty that) {
    if (that != null && that.children != null)
      children = new TreeMap<>(that.children);
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = StringUtils.trim(value);
  }

  public void set(String name, WikiPageProperty child) {
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
    return children == null ? Collections.<String>emptySet() : children.keySet();
  }

  @Override
  public String toString() {
    return toString("WikiPageProperty root", 0);
  }

  protected String toString(String key, int depth) {
    StringBuilder buffer = new StringBuilder();

    for (int i = 0; i < depth; i++)
      buffer.append("\t");
    buffer.append(key);
    if (getValue() != null)
      buffer.append(" = ").append(getValue());
    buffer.append("\n");

    for (String childKey : keySet()) {
      WikiPageProperty value = getProperty(childKey);
      if (value != null)
        buffer.append(value.toString(childKey, depth + 1));
    }
    return buffer.toString();
  }

  public boolean hasChildren() {
    return children != null && !children.isEmpty();
  }

  public Date getLastModificationTime() {
    String dateStr = get(LAST_MODIFIED);
    if (dateStr == null)
      return Clock.currentDate();
    else
      try {
        return getTimeFormat().parse(dateStr);
      } catch (ParseException e) {
        throw new RuntimeException("Unable to parse date '" + dateStr + "'", e);
      }
  }

  private static ThreadLocal<DateFormat> timeFormat = new ThreadLocal<>();

  public static DateFormat getTimeFormat() {
    DateFormat format = timeFormat.get();
    if (format == null) {
      format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ROOT);
      timeFormat.set(format);
    }
    return format;
  }

  public void setLastModificationTime(Date date) {
    set(LAST_MODIFIED, getTimeFormat().format(date));
  }
}
