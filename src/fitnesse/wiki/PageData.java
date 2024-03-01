// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.stream.Stream;

import static fitnesse.wiki.PageType.*;

public class PageData implements ReadOnlyPageData, Serializable {

  private static final long serialVersionUID = 1L;

  @Deprecated
  public static final String PropertyLAST_MODIFIED = WikiPageProperty.LAST_MODIFIED;
  @Deprecated
  public static final String PropertyHELP = WikiPageProperty.HELP;
  @Deprecated
  public static final String PropertyPRUNE = WikiPageProperty.PRUNE;
  @Deprecated
  public static final String PropertySEARCH = WikiPageProperty.SEARCH;
  @Deprecated
  public static final String PropertyRECENT_CHANGES = WikiPageProperty.RECENT_CHANGES;
  @Deprecated
  public static final String PropertyFILES = WikiPageProperty.FILES;
  @Deprecated
  public static final String PropertyWHERE_USED = WikiPageProperty.WHERE_USED;
  @Deprecated
  public static final String PropertyREFACTOR = WikiPageProperty.REFACTOR;
  @Deprecated
  public static final String PropertyPROPERTIES = WikiPageProperty.PROPERTIES;
  @Deprecated
  public static final String PropertyVERSIONS = WikiPageProperty.VERSIONS;
  @Deprecated
  public static final String PropertyEDIT = WikiPageProperty.EDIT;
  @Deprecated
  public static final String PropertySUITES = WikiPageProperty.SUITES;

  /* Wiki page content is saved always with LineFeed as separor even on Windows
   * see function set Content
   * To simplify writing test cases the below is defined
   */
  public static final String PAGE_LINE_SEPARATOR ="\n";
  
  public static final String PAGE_TYPE_ATTRIBUTE = "PageType";
  public static final String[] PAGE_TYPE_ATTRIBUTES = { STATIC.toString(),
      TEST.toString(), SUITE.toString() };

  public static final String[] ACTION_ATTRIBUTES = { WikiPageProperty.EDIT,
      WikiPageProperty.VERSIONS, WikiPageProperty.PROPERTIES,
      WikiPageProperty.REFACTOR, WikiPageProperty.WHERE_USED };

  public static final String[] NAVIGATION_ATTRIBUTES = {
      WikiPageProperty.RECENT_CHANGES, WikiPageProperty.FILES, WikiPageProperty.SEARCH };

  public static final String[] DISABLE_ATTRIBUTES = { WikiPageProperty.DISABLE_TESTHISTORY };

  public static final String[] NON_SECURITY_ATTRIBUTES = Stream
      .of(ACTION_ATTRIBUTES, NAVIGATION_ATTRIBUTES, DISABLE_ATTRIBUTES)
      .flatMap(Stream::of).toArray(String[]::new);

  @Deprecated
  public static final String PropertySECURE_READ = WikiPageProperty.SECURE_READ;
  @Deprecated
  public static final String PropertySECURE_WRITE = WikiPageProperty.SECURE_WRITE;
  @Deprecated
  public static final String PropertySECURE_TEST = WikiPageProperty.SECURE_TEST;
  public static final String[] SECURITY_ATTRIBUTES = { WikiPageProperty.SECURE_READ,
      WikiPageProperty.SECURE_WRITE, WikiPageProperty.SECURE_TEST };

  @Deprecated
  public static final String LAST_MODIFYING_USER = WikiPageProperty.LAST_MODIFYING_USER;

  public static final String SUITE_SETUP_NAME = "SuiteSetUp";

  public static final String SUITE_TEARDOWN_NAME = "SuiteTearDown";

  private String content = "";
  private WikiPageProperty properties = new WikiPageProperty();

  public static final String PATH_SEPARATOR = "PATH_SEPARATOR";

  public PageData(PageData data, String content) {
    this(data);
    setContent(content);
  }

  public PageData(PageData data) {
    this.properties = new WikiPageProperty(data.properties);
    this.content = data.content;
  }

  public PageData(String content, WikiPageProperty properties) {
    setContent(content);
    setProperties(properties);
  }

  @Override
  public WikiPageProperty getProperties() {
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

  public void setProperties(WikiPageProperty properties) {
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
