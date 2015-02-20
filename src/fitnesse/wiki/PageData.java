// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import static fitnesse.wiki.PageType.*;

import java.io.Serializable;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

public class PageData implements ReadOnlyPageData, Serializable {

  private static final long serialVersionUID = 1L;

  // TODO: Find a better place for us
  public static final String PropertyLAST_MODIFIED = "LastModified";
  public static final String PropertyHELP = "Help";
  public static final String PropertyPRUNE = "Prune";
  public static final String PropertySEARCH = "Search";
  public static final String PropertyRECENT_CHANGES = "RecentChanges";
  public static final String PropertyFILES = "Files";
  public static final String PropertyWHERE_USED = "WhereUsed";
  public static final String PropertyREFACTOR = "Refactor";
  public static final String PropertyPROPERTIES = "Properties";
  public static final String PropertyVERSIONS = "Versions";
  public static final String PropertyEDIT = "Edit";
  public static final String PropertySUITES = "Suites";

  public static final String PAGE_TYPE_ATTRIBUTE = "PageType";
  public static final String[] PAGE_TYPE_ATTRIBUTES = { STATIC.toString(),
      TEST.toString(), SUITE.toString() };

  public static final String[] ACTION_ATTRIBUTES = { PropertyEDIT,
      PropertyVERSIONS, PropertyPROPERTIES,
      PropertyREFACTOR, PropertyWHERE_USED };

  public static final String[] NAVIGATION_ATTRIBUTES = {
      PropertyRECENT_CHANGES, PropertyFILES, PropertySEARCH };

  public static final String[] NON_SECURITY_ATTRIBUTES = (String[]) ArrayUtils.addAll(ACTION_ATTRIBUTES, NAVIGATION_ATTRIBUTES);

  public static final String PropertySECURE_READ = "secure-read";
  public static final String PropertySECURE_WRITE = "secure-write";
  public static final String PropertySECURE_TEST = "secure-test";
  public static final String[] SECURITY_ATTRIBUTES = { PropertySECURE_READ,
      PropertySECURE_WRITE, PropertySECURE_TEST };

  public static final String LAST_MODIFYING_USER = "LastModifyingUser";

  public static final String SUITE_SETUP_NAME = "SuiteSetUp";

  public static final String SUITE_TEARDOWN_NAME = "SuiteTearDown";

  private String content = "";
  private WikiPageProperties properties = new WikiPageProperties();

  public static final String PATH_SEPARATOR = "PATH_SEPARATOR";

  public PageData(PageData data, String content) {
    this(data);
    setContent(content);
  }

  public PageData(PageData data) {
    this.properties = new WikiPageProperties(data.properties);
    this.content = data.content;
  }

  public PageData(String content, WikiPageProperties properties) {
    setContent(content);
    setProperties(properties);
  }

  @Override
  public WikiPageProperties getProperties() {
    return properties;
  }

  @Override
  public String getAttribute(String key) {
    return properties.get(key);
  }

  public void removeAttribute(String key) {
    properties.remove(key);
  }

  public void setAttribute(String key, String value) {
    properties.set(key, value);
  }

  public void setAttribute(String key) {
    properties.set(key);
  }

  public void setOrRemoveAttribute(String property, String content) {
    if (content == null || "".equals(content)) {
      removeAttribute(property);
    } else {
      setAttribute(property, content);
    }
  }

  @Override
  public boolean hasAttribute(String attribute) {
    return properties.has(attribute);
  }

  public void setProperties(WikiPageProperties properties) {
    this.properties = properties;
  }

  @Override
  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = StringUtils.remove(content, '\r');
  }

  public boolean isEmpty() {
    return getContent() == null || getContent().isEmpty();
  }
}
