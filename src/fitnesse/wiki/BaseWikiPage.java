// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import util.FileUtil;

public abstract class BaseWikiPage implements WikiPage {
  private static final long serialVersionUID = 1L;

  protected String name;
  protected WikiPage parent;
  protected WikiPage parentForVariables;

  protected BaseWikiPage(String name, WikiPage parent) {
    this.name = name;
    this.parent = this.parentForVariables = parent;
  }

  public String getName() throws Exception {
    return name;
  }

  public PageCrawler getPageCrawler() {
    return new PageCrawlerImpl();
  }

  public WikiPage getParent() throws Exception {
    return parent == null ? this : parent;
  }

  public void setParentForVariables(WikiPage parent) {
    parentForVariables = parent;
  }

  public WikiPage getParentForVariables() throws Exception {
    return parentForVariables == null ? this : parentForVariables;
  }

  protected abstract List<WikiPage> getNormalChildren() throws Exception;

  public List<WikiPage> getChildren() throws Exception {
    List<WikiPage> children = getNormalChildren();
    WikiPageProperties props = getData().getProperties();
    WikiPageProperty symLinksProperty = props.getProperty(SymbolicPage.PROPERTY_NAME);
    if (symLinksProperty != null) {
      for (Iterator<?> iterator = symLinksProperty.keySet().iterator(); iterator.hasNext();) {
        String linkName = (String) iterator.next();
        WikiPage page = createSymbolicPage(symLinksProperty, linkName);
        if (page != null && !children.contains(page))
          children.add(page);
      }
    }
    return children;
  }

  private WikiPage createSymbolicPage(WikiPageProperty symLinkProperty, String linkName) throws Exception {
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

  private WikiPage createExternalSymbolicLink(String linkPath, String linkName) throws Exception {
    String fullPagePath = linkPath.substring(7);
    File file = new File(fullPagePath);
    File parentDirectory = file.getParentFile();
    if (parentDirectory.exists()) {
      if (!file.exists())
        FileUtil.makeDir(file.getPath());
      if (file.isDirectory()) {
        WikiPage externalRoot = FileSystemPage.makeRoot(parentDirectory.getPath(), file.getName());
        return new SymbolicPage(linkName, externalRoot, this);
      }
    }
    return null;
  }

  protected WikiPage createInternalSymbolicPage(String linkPath, String linkName) throws Exception {
    WikiPagePath path = PathParser.parse(linkPath);
    WikiPage start = (path.isRelativePath()) ? getParent() : this;  //TODO -AcD- a better way?
    WikiPage page = getPageCrawler().getPage(start, path);
    if (page != null)
      page = new SymbolicPage(linkName, page, this);
    return page;
  }

  protected abstract WikiPage getNormalChildPage(String name) throws Exception;

  public WikiPage getChildPage(String name) throws Exception {
    WikiPage page = getNormalChildPage(name);
    if (page == null)
      page = createSymbolicPage(getData().getProperties().getProperty(SymbolicPage.PROPERTY_NAME), name);
    return page;
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

  public String getHelpText() throws Exception {
    String helpText = getData().getAttribute(PageData.PropertyHELP);
    return ((helpText == null) || (helpText.length() == 0)) ? null : helpText;
  }
}
