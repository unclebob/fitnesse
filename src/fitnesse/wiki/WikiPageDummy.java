// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.

package fitnesse.wiki;

import fitnesse.util.Clock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class WikiPageDummy extends BaseWikiPage {

  private PageData pageData;

  public WikiPageDummy(String name, String content, WikiPage parent) {
    super(name, parent);
    pageData = new PageData(content, new WikiPageProperty());
  }

  public WikiPageDummy() {
    super("Default", null);
    pageData = new PageData("", new WikiPageProperty());
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
    return new ArrayList<>();
  }

  @Override
  public WikiPage getVersion(String versionName) {
    return this;
  }

  @Override
  public String getHtml() {
    return String.format("<em>%s</em>", pageData.getContent());
  }

  @Override
  public void removeChildPage(String name) {
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
  public WikiPage getChildPage(String name) {
    return null;
  }

}
