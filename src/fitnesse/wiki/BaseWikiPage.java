// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import fitnesse.wikitext.parser.VariableSource;

public abstract class BaseWikiPage implements WikiPage {
  private static final long serialVersionUID = 1L;

  protected final String name;
  protected final BaseWikiPage parent;
  private final VariableSource variableSource;

  protected BaseWikiPage(String name, VariableSource variableSource) {
    this.name = name;
    this.parent = null;
    this.variableSource = variableSource;
  }

  protected BaseWikiPage(String name, BaseWikiPage parent) {
    this.name = name;
    this.parent = parent;
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

  protected VariableSource getVariableSource() {
    return variableSource;
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
