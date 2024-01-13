// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import fitnesse.wikitext.SyntaxTree;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class SymbolicPage extends BaseWikitextPage {

  public static final String PROPERTY_NAME = "SymbolicLinks";
  public static final String SHORT_CIRCUIT_BREAK_MESSAGE = "Short circuit! This page references %s, which is already one of the parent pages of this page.";

  private final WikiPage realPage;

  public SymbolicPage(String name, WikiPage realPage, WikiPage parent) {
    super(name, parent);
    this.realPage = realPage;
  }

  @Override
  public WikiPage getRealPage() {
    return realPage;
  }

  private boolean containsWikitext() {
    return containsWikitext(realPage);
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
  public WikiPage getChildPage(String name) {
    WikiPage childPage = realPage.getChildPage(name);
    if (childPage != null) {
      childPage = createChildPage(childPage);
    }
    return childPage;
  }

  @Override
  public void removeChildPage(String name) {
    realPage.removeChildPage(name);
  }

  @Override
  public void remove() {
    realPage.remove();
  }

  @Override
  public List<WikiPage> getChildren() {
    List<WikiPage> children = realPage.getChildren();
    List<WikiPage> symChildren = new LinkedList<>();
    for (WikiPage child : children) {
      symChildren.add(createChildPage(child));
    }
    return symChildren;
  }

  private WikiPage createChildPage(WikiPage child) {
    WikiPage cyclicReference = findCyclicReference(child);
    if (cyclicReference != null) {
      return new WikiPageDummy(child.getName(), String.format(SHORT_CIRCUIT_BREAK_MESSAGE, cyclicReference.getFullPath().toString()), this);
    } else {
      return new SymbolicPage(child.getName(), child, this);
    }
  }

  private WikiPage findCyclicReference(WikiPage childPage) {
    for (WikiPage parentPage = getParent(); !parentPage.isRoot(); parentPage = parentPage.getParent()) {
      if (childPage.equals(parentPage)) {
        return parentPage;
      }
    }
    return null;
  }

  @Override
  public PageData getData() {
    return realPage.getData();
  }

  @Override
  public Collection<VersionInfo> getVersions() {
    return realPage.getVersions();
  }

  @Override
  public WikiPage getVersion(String versionName) {
    return new SymbolicPage(this.getName(), realPage.getVersion(versionName), this.getParent());
  }

  @Override
  public VersionInfo commit(PageData data) {
    return realPage.commit(data);
  }

  @Override
  public String getVariable(String name) {
    if (containsWikitext()) {
      return super.getVariable(name);
    }
    String value = realPage.getVariable(name);
    return (value == null && !isRoot()) ? getParent().getVariable(name) : value;
  }

  @Override
  public String getHtml() {
    if (containsWikitext()) {
      return super.getHtml();
    }
    return realPage.getHtml();
  }

  @Override
  public SyntaxTree getSyntaxTree() {
    if (containsWikitext()) {
      return super.getSyntaxTree();
    }
    return null;
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof SymbolicPage) {
      SymbolicPage symbolicOther = (SymbolicPage) other;
      return getName().equals(symbolicOther.getName()) && getRealPage().equals(symbolicOther.getRealPage());
    } else {
      return getRealPage().equals(other);
    }
  }

  @Override
  public int hashCode() {
    return realPage.hashCode();
  }

  public static boolean containsWikitext(WikiPage wikiPage) {
    if (wikiPage instanceof SymbolicPage) {
      return containsWikitext(((SymbolicPage) wikiPage).realPage);
    } else {
      return wikiPage instanceof WikitextPage;
    }
  }

}
