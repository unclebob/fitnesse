// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.search;

import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.components.TraversalListener;
import fitnesse.components.Traverser;
import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageTitle;
import fitnesse.responders.ChunkingResponder;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageType;
import fitnesse.wiki.SymbolicLinkPruningStrategy;
import fitnesse.wiki.SymbolicPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.search.PageFinder;
import fitnesse.wiki.search.WikiPageFinder;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static fitnesse.wiki.PageData.PropertyEDIT;
import static fitnesse.wiki.PageData.PropertyFILES;
import static fitnesse.wiki.PageData.PropertyPROPERTIES;
import static fitnesse.wiki.PageData.PropertyPRUNE;
import static fitnesse.wiki.PageData.PropertyRECENT_CHANGES;
import static fitnesse.wiki.PageData.PropertyREFACTOR;
import static fitnesse.wiki.PageData.PropertySEARCH;
import static fitnesse.wiki.PageData.PropertyVERSIONS;
import static fitnesse.wiki.PageData.PropertyWHERE_USED;
import static fitnesse.wiki.PageData.SECURITY_ATTRIBUTES;


public abstract class ResultResponder extends ChunkingResponder implements
  SecureResponder, Traverser<WikiPage> {

  static final String[] SEARCH_ACTION_ATTRIBUTES = { PropertyEDIT, PropertyVERSIONS,
    PropertyPROPERTIES, PropertyREFACTOR, PropertyWHERE_USED };
  static final String[] SEARCH_NAVIGATION_ATTRIBUTES = { PropertyRECENT_CHANGES, PropertyFILES, PropertySEARCH };
  static final String SEARCH_ATTRIBUTE_SKIP = PropertyPRUNE;
  static final String[] SPECIAL_ATTRIBUTES = { "SetUp", "TearDown" };
  static final String INCLUDE_SYMBOLIC_LINKS = "includeSymLinks";

  private long searchStartTime;
  private long searchEndTime;

  @Override
  protected PageCrawler getPageCrawler() {
    return root.getPageCrawler();
  }

  protected WikiPage getSearchScope() {
    String searchScope = request.getInput("searchScope");

    if (searchScope == null || searchScope.isEmpty())
      return page;
    else if(searchScope.equalsIgnoreCase("root"))
      return root;
    else{
      WikiPage scope = page.getPageCrawler().findAncestorWithName(searchScope);
      return scope;
    }
  }


  @Override
  protected void doSending() throws IOException {
    if (page == null)
      page = root;
    String queryString = request.getQueryString() == null ? "" : request.getQueryString();

    PageTitle pageTitle = new PageTitle(page.getFullPath() );

    HtmlPage htmlPage = context.pageFactory.newPage();
    htmlPage.setTitle(getTitle());
    htmlPage.setPageTitle(pageTitle);
    htmlPage.setMainTemplate(getTemplate());

    htmlPage.put("queryString", queryString);
    htmlPage.put("page", page);
    htmlPage.put("viewLocation", request.getResource());
    htmlPage.setNavTemplate("viewNav");
    htmlPage.put("resultResponder", this);

    htmlPage.put("pageTypeAttributes", PageType.valuesAsString());
    htmlPage.put("actionAttributes", SEARCH_ACTION_ATTRIBUTES);
    htmlPage.put("navigationAttributes", SEARCH_NAVIGATION_ATTRIBUTES);
    htmlPage.put("securityAttributes", SECURITY_ATTRIBUTES);
    htmlPage.put("specialAttributes", SPECIAL_ATTRIBUTES);
    htmlPage.put("request", request);

    htmlPage.render(response.getWriter(), request);

    response.close();
  }

  @Override
  public final void traverse(TraversalListener<WikiPage> observer) {
    searchStartTime = System.nanoTime();
    try {
      search(observer);
    } finally {
      searchEndTime = System.nanoTime();
    }
  }

  /**
   * @return the time the search took, in seconds with two decimals.
   */
  public String getSearchTime() {
    return String.format(Locale.ROOT, "%.2f", (searchEndTime - searchStartTime) / 1_000_000_000.0);
  }

  private void search(TraversalListener<WikiPage> observer) {
    PageFinder pageFinder = getPageFinder(observer);
    if (pageFinder instanceof WikiPageFinder && !includeSymbolicLinks()) {
      ((WikiPageFinder) pageFinder).search(getSearchScope(), new SymbolicLinkPruningStrategy());
    } else if (pageFinder != null) {
      pageFinder.search(getSearchScope());
    }
  }

  /**
   * @return true to also search the pages behind symbolic links, as requested; false by default.
   */
  protected boolean includeSymbolicLinks() {
    return request.hasInput(INCLUDE_SYMBOLIC_LINKS);
  }

  /**
   * @param page a search result
   * @return the pages making up the full path of the page, from the top level page down to the page itself
   */
  public List<WikiPage> getPathPages(WikiPage page) {
    LinkedList<WikiPage> pathPages = new LinkedList<>();
    for (WikiPage current = page; !current.isRoot(); current = current.getParent()) {
      pathPages.addFirst(current);
    }
    return pathPages;
  }

  public boolean isSymbolicLink(WikiPage page) {
    return SymbolicPage.isSymbolicLink(page);
  }

  protected abstract String getTemplate();

  protected abstract String getTitle() ;

  protected abstract PageFinder getPageFinder(TraversalListener<WikiPage> observer);

  @Override
  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }

}


