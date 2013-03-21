// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.util.List;

public abstract class BaseWikiPage implements WikiPage {
  private static final long serialVersionUID = 1L;

  protected final String name;
  protected WikiPage parent;
  private final SymbolicPageFactory symbolicPageFactory;

  protected BaseWikiPage(String name, WikiPage parent, SymbolicPageFactory symbolicPageFactory) {
    this.name = name;
    this.parent = parent;
    this.symbolicPageFactory = symbolicPageFactory;
  }

  public String getName() {
    return name;
  }

  public PageCrawler getPageCrawler() {
    return new PageCrawlerImpl();
  }

  public WikiPage getParent() {
    return parent == null ? this : parent;
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
    return PageCrawlerImpl.getClosestInheritedPage("PageHeader", this);
  }

  public WikiPage getFooterPage() {
    return PageCrawlerImpl.getClosestInheritedPage("PageFooter", this);
  }

  public boolean isOpenInNewWindow() {
    return false;
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
      PageCrawler crawler = getPageCrawler();
      return crawler.getFullPath(this).equals(crawler.getFullPath(((WikiPage) o)));
    }
    catch (Exception e) {
      return false;
    }
  }

  public int hashCode() {
    try {
      return getPageCrawler().getFullPath(this).hashCode();
    }
    catch (Exception e) {
      return 0;
    }
  }
}
