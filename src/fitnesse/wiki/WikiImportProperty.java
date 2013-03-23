// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.text.ParseException;
import java.util.Date;

import fitnesse.responders.templateUtilities.HtmlPage;

public class WikiImportProperty extends WikiPageProperty {
  private static final long serialVersionUID = 1L;

  public static final String PROPERTY_NAME = "WikiImport";

  private WikiImportProperty() {
  }

  public WikiImportProperty(String source) {
    set("Source", source);
  }

  public String getSourceUrl() {
    return get("Source");
  }

  public boolean isRoot() {
    return has("IsRoot");
  }

  public void setRoot(boolean value) {
    if (value)
      set("IsRoot");
    else
      remove("IsRoot");
  }

  public boolean isAutoUpdate() {
    return has("AutoUpdate");
  }

  public void setAutoUpdate(boolean value) {
    if (value)
      set("AutoUpdate");
    else
      remove("AutoUpdate");
  }

  public static boolean isImported(PageData pageData) {
    try {
      WikiImportProperty importProperty = WikiImportProperty.createFrom(pageData.getProperties());
      return importProperty != null && !importProperty.isRoot();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static WikiImportProperty createFrom(WikiPageProperty property) {
    if (property.has(PROPERTY_NAME)) {
      WikiImportProperty importProperty = new WikiImportProperty();
      WikiPageProperty rawImportProperty = property.getProperty(PROPERTY_NAME);
      importProperty.set("Source", rawImportProperty.getProperty("Source"));
      importProperty.set("LastRemoteModification", rawImportProperty.getProperty("LastRemoteModification"));
      if (rawImportProperty.has("IsRoot"))
        importProperty.set("IsRoot", rawImportProperty.getProperty("IsRoot"));
      if (rawImportProperty.has("AutoUpdate"))
        importProperty.set("AutoUpdate", rawImportProperty.getProperty("AutoUpdate"));

      return importProperty;
    } else
      return null;
  }

  public void addTo(WikiPageProperty rootProperty) {
    rootProperty.set(PROPERTY_NAME, this);
  }

  public void setLastRemoteModificationTime(Date date) {
    set("LastRemoteModification", getTimeFormat().format(date));
  }

  public Date getLastRemoteModificationTime() {
    Date date = new Date(0);
    String strValue = get("LastRemoteModification");
    if (strValue != null) {
      try {
        date = getTimeFormat().parse(strValue);
      } catch (ParseException e) {
        date = new Date(0);
      }
    }
    return date;
  }

  public static void handleImportProperties(HtmlPage html, WikiPage page) {
    PageData pageData = page.getData();
    if (isImported(pageData)) {
      html.setBodyClass("imported");
      WikiImportProperty importProperty = WikiImportProperty.createFrom(pageData.getProperties());
      html.put("sourceUrl", importProperty.getSourceUrl());
    } else if (page instanceof ProxyPage)
      html.setBodyClass("virtual");
  }

  public static String makeRemoteEditQueryParameters() {
    return "responder=edit&amp;redirectToReferer=true&amp;redirectAction=importAndView";
  }
}
