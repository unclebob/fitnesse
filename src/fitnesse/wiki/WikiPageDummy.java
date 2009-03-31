// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.

package fitnesse.wiki;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WikiPageDummy implements WikiPage {
  private static final long serialVersionUID = 1L;

  public String name;
  protected String location;
  private PageData pageData;
  private WikiPage parent;
  protected WikiPage parentForVariables;

  public static final int daysTillVersionsExpire = 14;

  public WikiPageDummy(String name, String content) throws Exception {
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

  public void setParentForVariables(WikiPage parent) {
    parentForVariables = parent;
  }

  public WikiPage getParentForVariables() throws Exception {
    return parentForVariables == null ? this : parentForVariables;
  }

  public void setParent(WikiPage parent) {
    this.parent = this.parentForVariables = parent;
  }

  public PageData getData() throws Exception {
    return pageData;
  }

  public VersionInfo commit(PageData data) throws Exception {
    pageData = data;
    return new VersionInfo("mockVersionName", "mockAuthor", new Date());
  }

  public List<WikiPage> getChildren() {
    return new ArrayList<WikiPage>();
  }

  public int compareTo(Object o) {
    return 0;
  }

  public PageData getDataVersion(String versionName) throws Exception {
    return null;
  }

  public void removeChildPage(String name) throws Exception {
  }

  public PageCrawler getPageCrawler() {
    return new PageCrawlerImpl();
  }

  public WikiPage getHeaderPage() throws Exception {
    return null;
  }

  public WikiPage getFooterPage() throws Exception {
    return null;
  }

  public WikiPage addChildPage(String name) throws Exception {
    return null;
  }

  public boolean hasChildPage(String name) throws Exception {
    return false;
  }

  public WikiPage getChildPage(String name) throws Exception {
    return null;
  }

  public boolean hasExtension(String extensionName) {
    return false;
  }

  public Extension getExtension(String extensionName) {
    return null;
  }

  public String getHelpText() throws Exception {
    return "Dummy help text";
  }

  public List<WikiPageAction> getActions() throws Exception {
    return null;
  }
}
