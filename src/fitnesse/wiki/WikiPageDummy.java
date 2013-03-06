// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.

package fitnesse.wiki;

import java.util.ArrayList;
import java.util.List;

import util.Clock;

public class WikiPageDummy implements WikiPage {
  private static final long serialVersionUID = 1L;

  public String name;
  protected String location;
  private PageData pageData;
  private WikiPage parent;

  public WikiPageDummy(String name, String content) {
    this.name = name;
    pageData = new PageData(this, content);
  }

  public WikiPageDummy(String location) {
    this.location = location;
    name = "Default";
  }

  public WikiPageDummy() {
    location = null;
    name = "Default";
  }

  public String getName() {
    return name;
  }

  public WikiPage getParent() {
    return parent;
  }

  public void setParent(WikiPage parent) {
    this.parent = parent;
  }

  public PageData getData() {
    return pageData;
  }

  public ReadOnlyPageData readOnlyData() { return getData(); }

  public VersionInfo commit(PageData data) {
    pageData = data;
    return new VersionInfo("mockVersionName", "mockAuthor", Clock.currentDate());
  }

  public List<WikiPage> getChildren() {
    return new ArrayList<WikiPage>();
  }

  public int compareTo(Object o) {
    return 0;
  }

  public PageData getDataVersion(String versionName) {
    return null;
  }

  public void removeChildPage(String name) {
  }

  public PageCrawler getPageCrawler() {
    return new PageCrawlerImpl();
  }

  public WikiPage getHeaderPage() {
    return null;
  }

  public WikiPage getFooterPage() {
    return null;
  }

  public WikiPage addChildPage(String name) {
    return null;
  }

  public boolean hasChildPage(String name) {
    return false;
  }

  public WikiPage getChildPage(String name) {
    return null;
  }

  public boolean isOpenInNewWindow() {
    return false;
  }
}
