// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.io.File;
import java.util.List;
import util.EnvironmentVariableTool;
import util.FileUtil;

public abstract class BaseWikiPage implements WikiPage {
  private static final long serialVersionUID = 1L;

  protected String name;
  protected WikiPage parent;

  protected BaseWikiPage(String name, WikiPage parent) {
    this.name = name;
    this.parent = parent;
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

  // TODO: Use Factory for this.
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

  @Deprecated
  // Move to factory
  private WikiPage createSymbolicPage(WikiPageProperty symLinkProperty, String linkName) {
    if (symLinkProperty == null)
      return null;
    String linkPath = symLinkProperty.get(linkName);
    if (linkPath == null)
      return null;
    if (linkPath.startsWith("file://"))
      return createExternalSymbolicLink(linkPath, linkName);
    else
      return createInternalSymbolicPage(linkPath, linkName);
  }

  @Deprecated
  // Move to factory
  private WikiPage createExternalSymbolicLink(String linkPath, String linkName) {
    String fullPagePath = EnvironmentVariableTool.replace(linkPath.substring(7));
    File file = new File(fullPagePath);
    File parentDirectory = file.getParentFile();
    if (parentDirectory.exists()) {
      if (!file.exists())
        FileUtil.makeDir(file.getPath());
      if (file.isDirectory()) {
        //WikiPageFactory.makeRootPage
        WikiPage externalRoot = new FileSystemPage(parentDirectory.getPath(), file.getName());
        return new SymbolicPage(linkName, externalRoot, this);
      }
    }
    return null;
  }

  @Deprecated
  // Move to factory
  protected WikiPage createInternalSymbolicPage(String linkPath, String linkName) {
    WikiPagePath path = PathParser.parse(linkPath);
    WikiPage start = (path.isRelativePath()) ? getParent() : this;  //TODO -AcD- a better way?
    WikiPage page = getPageCrawler().getPage(start, path);
    if (page != null)
      page = new SymbolicPage(linkName, page, this);
    return page;
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
