// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.io.File;
import java.util.ArrayList;
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

  public String getName() {
    return name;
  }

  public PageCrawler getPageCrawler() {
    return new PageCrawlerImpl();
  }

  public WikiPage getParent() {
    return parent == null ? this : parent;
  }

  public void setParentForVariables(WikiPage parent) {
    parentForVariables = parent;
  }

  public WikiPage getParentForVariables() {
    return parentForVariables == null ? this : parentForVariables;
  }

  protected abstract List<WikiPage> getNormalChildren() throws Exception;

  public List<WikiPage> getChildren() throws Exception {
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
        WikiPage externalRoot = new FileSystemPage(parentDirectory.getPath(), file.getName());
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

  public WikiPage getHeaderPage() throws Exception {
    return PageCrawlerImpl.getInheritedPage("PageHeader", this);
  }

  public WikiPage getFooterPage() throws Exception {
    return PageCrawlerImpl.getInheritedPage("PageFooter", this);
  }

  public List<WikiPageAction> getActions() throws Exception {
    WikiPagePath localPagePath = getPageCrawler().getFullPath(this);
    String localPageName = PathParser.render(localPagePath);
    String localOrRemotePageName = localPageName;
    boolean newWindowIfRemote = false;
    if (this instanceof ProxyPage) {
      ProxyPage proxyPage = (ProxyPage) this;
      localOrRemotePageName = proxyPage.getThisPageUrl();
      newWindowIfRemote = true;
    }
    return makeActions(localPageName, localOrRemotePageName, newWindowIfRemote);
  }

  private List<WikiPageAction> makeActions(String localPageName, String localOrRemotePageName, boolean newWindowIfRemote) throws Exception {
    PageData pageData = getData();
    List<WikiPageAction> actions = new ArrayList<WikiPageAction>();
    addActionForAttribute("Test", pageData, localPageName, newWindowIfRemote, null, null, actions);
    addActionForAttribute("Suite", pageData, localPageName, newWindowIfRemote, "", null, actions);
    addActionForAttribute("Edit", pageData, localOrRemotePageName, newWindowIfRemote, null, null, actions);
    addActionForAttribute("Properties", pageData, localOrRemotePageName, newWindowIfRemote, null, null, actions);
    addActionForAttribute("Refactor", pageData, localOrRemotePageName, newWindowIfRemote, null, null, actions);
    addActionForAttribute("Where Used", pageData, localOrRemotePageName, newWindowIfRemote, null, "whereUsed", actions);
    addActionForAttribute("Search", pageData, "", newWindowIfRemote, null, "searchForm", actions);
    addActionForAttribute("Files", pageData, "/files", newWindowIfRemote, null, "", actions);
    addActionForAttribute("Versions", pageData, localOrRemotePageName, newWindowIfRemote, null, null, actions);
    addActionForAttribute("Recent Changes", pageData, "/RecentChanges", newWindowIfRemote, "", "", actions);
    addAction("User Guide", ".FitNesse.UserGuide", newWindowIfRemote, "", "", actions);
    return actions;
  }

  private void addActionForAttribute(String attribute, PageData pageData, String pageName, boolean newWindowIfRemote,
                                     String shortcutKey, String query, List<WikiPageAction> actions) {
    if (pageData.hasAttribute(attribute.replaceAll("\\s", ""))) {
      addAction(attribute, pageName, newWindowIfRemote, shortcutKey, query, actions);
    }
  }

  private void addAction(String linkName, String pageName, boolean newWindowIfRemote, String shortcutKey, String query, List<WikiPageAction> actions) {
    WikiPageAction link = new WikiPageAction(pageName, linkName);
    link.setNewWindow(newWindowIfRemote);
    if (shortcutKey != null)
      link.setShortcutKey(shortcutKey);
    if (query != null)
      link.setQuery(query);
    actions.add(link);
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
