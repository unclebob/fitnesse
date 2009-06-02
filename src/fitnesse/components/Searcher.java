// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.components;

import fitnesse.wiki.WikiPage;

// TODO: Check if this can be safely replaced with TitleWikiPageFinder and ContentWikiPageFinder
@Deprecated
public class Searcher implements TraversalListener {
  WikiPage root;
  SearchObserver observer;
  boolean isTitleSearch = false;
  private String searchString;

  public Searcher(String exp, WikiPage root) throws Exception {
    searchString = exp.toLowerCase();
    this.root = root;
  }

  public void searchContent(SearchObserver observer) throws Exception {
    search(observer);
  }

  private void search(SearchObserver observer) throws Exception {
    this.observer = observer;
    processPage(root);
    root.getPageCrawler().traverse(root, this);
  }

  public void processPage(WikiPage currentPage) throws Exception {
    if (isHit(currentPage))
      observer.hit(currentPage);
  }

  private boolean isHit(WikiPage page) throws Exception {
    String content = page.getName().toLowerCase();
    if (!isTitleSearch)
      content = page.getData().getContent().toLowerCase();

    boolean matches = content.indexOf(searchString) != -1;
    return matches;
  }

  public String getSearchPattern() throws Exception {
    return searchString;
  }

  public void searchTitles(SearchObserver observer) throws Exception {
    isTitleSearch = true;
    search(observer);
  }
}
