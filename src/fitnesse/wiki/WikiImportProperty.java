// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.text.ParseException;
import java.util.Date;

public class WikiImportProperty extends WikiPageProperty {
  private static final long serialVersionUID = 1L;

  public static final String PROPERTY_NAME = "WikiImport";
  
  private static final String SOURCE = "Source";
  private static final String IS_ROOT = "IsRoot";
  private static final String LAST_REMOTE_MODIFICATION = "LastRemoteModification";
  private static final String AUTO_UPDATE = "AutoUpdate";

  private WikiImportProperty() {
  }

  public WikiImportProperty(String source) {
    set(SOURCE, source);
  }

  public String getSourceUrl() {
    return get(SOURCE);
  }

  public boolean isRoot() {
    return has(IS_ROOT);
  }

  public void setRoot(boolean value) {
    if (value)
      set(IS_ROOT);
    else
      remove(IS_ROOT);
  }

  public boolean isAutoUpdate() {
    return has(AUTO_UPDATE);
  }

  public void setAutoUpdate(boolean value) {
    if (value)
      set(AUTO_UPDATE);
    else
      remove(AUTO_UPDATE);
  }

  public static boolean isImported(PageData pageData) {
    return pageData.hasAttribute(PROPERTY_NAME);
  }

  public static boolean isImportedSubWiki(PageData pageData) {
    WikiImportProperty importProperty = WikiImportProperty.createFrom(pageData.getProperties());
    return importProperty != null && !importProperty.isRoot();
  }

  public static WikiImportProperty createFrom(WikiPageProperty property) {
    if (property.has(PROPERTY_NAME)) {
      WikiImportProperty importProperty = new WikiImportProperty();
      WikiPageProperty rawImportProperty = property.getProperty(PROPERTY_NAME);
      importProperty.set(SOURCE, rawImportProperty.getProperty(SOURCE));
      importProperty.set(LAST_REMOTE_MODIFICATION, rawImportProperty.getProperty(LAST_REMOTE_MODIFICATION));
      if (rawImportProperty.has(IS_ROOT))
        importProperty.set(IS_ROOT, rawImportProperty.getProperty(IS_ROOT));
      if (rawImportProperty.has(AUTO_UPDATE))
        importProperty.set(AUTO_UPDATE, rawImportProperty.getProperty(AUTO_UPDATE));

      return importProperty;
    } else
      return null;
  }

  public static boolean isAutoUpdated(PageData pageData) {
    WikiImportProperty importProperty = createFrom(pageData.getProperties());
    return importProperty != null && importProperty.isAutoUpdate();
  }

  public void addTo(WikiPageProperty rootProperty) {
    rootProperty.set(PROPERTY_NAME, this);
  }

  public void setLastRemoteModificationTime(Date date) {
    set(LAST_REMOTE_MODIFICATION, getTimeFormat().format(date));
  }

  public Date getLastRemoteModificationTime() {
    Date date = new Date(0);
    String strValue = get(LAST_REMOTE_MODIFICATION);
    if (strValue != null) {
      try {
        date = getTimeFormat().parse(strValue);
      } catch (ParseException e) {
        date = new Date(0);
      }
    }
    return date;
  }

}
