// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.

package fitnesse.wiki;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import fitnesse.util.Clock;

public class WikiPageDummy implements WikiPage {
  private static final long serialVersionUID = 1L;

  public String name;
  private PageData pageData;
  private final WikiPage parent;

  public WikiPageDummy(String name, String content, WikiPage parent) {
    this.name = name;
    pageData = new PageData(content, new WikiPageProperties());
    this.parent = parent;
  }

  public WikiPageDummy() {
    name = "Default";
    pageData = new PageData("", new WikiPageProperties());
    this.parent = null;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public WikiPage getParent() {
    return parent;
  }

  @Override
  public boolean isRoot() {
    return parent == null;
  }

  @Override
  public PageData getData() {
    return pageData;
  }

  @Override
  public Collection<VersionInfo> getVersions() {
    return Collections.emptySet();
  }

  @Override
  public VersionInfo commit(PageData data) {
    pageData = data;
    return new VersionInfo("mockVersionName", "mockAuthor", Clock.currentDate());
  }

  @Override
  public List<WikiPage> getChildren() {
    return new ArrayList<WikiPage>();
  }

  @Override
  public int compareTo(Object o) {
    return 0;
  }

  @Override
  public WikiPage getVersion(String versionName) {
    return this;
  }

  @Override
  public String getHtml() {
    return "";
  }

  @Override
  public void removeChildPage(String name) {
  }

  @Override
  public PageCrawler getPageCrawler() {
    return new PageCrawlerImpl(this);
  }

  @Override
  public String getVariable(String name) {
    return null;
  }

  @Override
  public WikiPage addChildPage(String name) {
    return null;
  }

  @Override
  public boolean hasChildPage(String name) {
    return false;
  }

  @Override
  public WikiPage getChildPage(String name) {
    return null;
  }

}
