// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import fitnesse.wiki.fs.SymbolicPageFactory;
import fitnesse.wikitext.parser.VariableSource;

import java.util.List;

public abstract class BaseWikiPage implements WikiPage {
  private static final long serialVersionUID = 1L;

  protected final String name;
  private VariableSource variableSource;
  protected final BaseWikiPage parent;
  protected final SymbolicPageFactory symbolicPageFactory;

  protected BaseWikiPage(String name, SymbolicPageFactory symbolicPageFactory, VariableSource variableSource) {
    this.name = name;
    this.parent = null;
    this.symbolicPageFactory = symbolicPageFactory;
    this.variableSource = variableSource;
  }

  protected BaseWikiPage(String name, BaseWikiPage parent) {
    this.name = name;
    this.parent = parent;
    this.symbolicPageFactory = parent.symbolicPageFactory;
    this.variableSource = parent.variableSource;
  }

  public String getName() {
    return name;
  }

  public PageCrawler getPageCrawler() {
    return new PageCrawlerImpl(this);
  }

  public WikiPage getParent() {
    return parent == null ? this : parent;
  }


  public boolean isRoot() {
    WikiPage parent = getParent();
    return parent == null || parent == this;
  }

  protected abstract List<WikiPage> getNormalChildren();

  public List<WikiPage> getChildren() {
    List<WikiPage> children = getNormalChildren();
    WikiPageProperties props = getData().getProperties();
    WikiPageProperty symLinksProperty = props.getProperty(SymbolicPage.PROPERTY_NAME);
    if (symLinksProperty != null) {
      for (String linkName : symLinksProperty.keySet()) {
        WikiPage page = createSymbolicPage(symLinksProperty, linkName);
        if (page != null && !children.contains(page))
          children.add(page);
      }
    }
    return children;
  }

  protected VariableSource getVariableSource() {
    return variableSource;
  }

  private WikiPage createSymbolicPage(WikiPageProperty symLinkProperty, String linkName) {
    if (symLinkProperty == null)
      return null;
    String linkPath = symLinkProperty.get(linkName);
    if (linkPath == null)
      return null;
    return symbolicPageFactory.makePage(linkPath, linkName, this);
  }

  protected abstract WikiPage getNormalChildPage(String name);

  public WikiPage getChildPage(String name) {
    WikiPage page = getNormalChildPage(name);
    if (page == null) {
      page = createSymbolicPage(readOnlyData().getProperties().getProperty(SymbolicPage.PROPERTY_NAME), name);
    }
    return page;
  }

  public WikiPage getHeaderPage() {
    return getPageCrawler().getClosestInheritedPage("PageHeader");
  }

  public WikiPage getFooterPage() {
    return getPageCrawler().getClosestInheritedPage("PageFooter");
  }

  public String toString() {
    return this.getClass().getName() + ": " + name;
  }

  public int compareTo(Object o) {
    try {
      return getName().compareTo(((WikiPage) o).getName());
    }
    catch (Exception e) {
      return 0;
    }
  }

  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof WikiPage))
      return false;
    try {
      return getPageCrawler().getFullPath().equals(((WikiPage) o).getPageCrawler().getFullPath());
    }
    catch (Exception e) {
      return false;
    }
  }

  public int hashCode() {
    try {
      return getPageCrawler().getFullPath().hashCode();
    }
    catch (Exception e) {
      return 0;
    }
  }
}
