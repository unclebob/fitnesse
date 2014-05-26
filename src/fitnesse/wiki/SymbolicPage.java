// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import fitnesse.wiki.fs.SymbolicPageFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class SymbolicPage extends BaseWikiPage {
  private static final long serialVersionUID = 1L;

  public static final String PROPERTY_NAME = "SymbolicLinks";

  private WikiPage realPage;

  public SymbolicPage(String name, WikiPage realPage, WikiPage parent) {
    super(name, (BaseWikiPage) parent);
    this.realPage = realPage;
  }

  public WikiPage getRealPage() {
    return realPage;
  }

  @Override
  public WikiPage addChildPage(String name) {
    return realPage.addChildPage(name);
  }

  @Override
  public boolean hasChildPage(String name) {
    return realPage.hasChildPage(name);
  }

  @Override
  protected WikiPage getNormalChildPage(String name) {
    WikiPage childPage = realPage.getChildPage(name);
    if (childPage != null) {
      childPage = new SymbolicPage(name, childPage, this);
    }

    return childPage;
  }

  @Override
  public void removeChildPage(String name) {
    realPage.removeChildPage(name);
  }

  @Override
  public List<WikiPage> getNormalChildren() {
    List<?> children = realPage.getChildren();
    List<WikiPage> symChildren = new LinkedList<WikiPage>();
    //...Intentionally exclude symbolic links on symbolic pages
    //   to prevent infinite cyclic symbolic references.
    //TODO: -AcD- we need a better cyclic infinite recursion algorithm here.
    for (Iterator<?> iterator = children.iterator(); iterator.hasNext();) {
      WikiPage child = (WikiPage) iterator.next();
      symChildren.add(new SymbolicPage(child.getName(), child, this));
    }
    return symChildren;
  }

  @Override
  public PageData getData() {
    PageData data = realPage.getData();
    data.setWikiPage(this);
    return data;
  }

  @Override
  public ReadOnlyPageData readOnlyData() { return getData(); }

  @Override
  public Collection<VersionInfo> getVersions() {
    return realPage.getVersions();
  }

  public PageData getDataVersion(String versionName) {
    PageData data = realPage.getDataVersion(versionName);
    data.setWikiPage(this);
    return data;
  }

  public VersionInfo commit(PageData data) {
    return realPage.commit(data);
  }
}
