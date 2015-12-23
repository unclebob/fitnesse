// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.search;

import fitnesse.components.TraversalListener;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.search.PageFinder;
import fitnesse.wiki.search.WhereUsedPageFinder;

public class WhereUsedResponder extends ResultResponder {

  @Override
  public PageFinder getPageFinder(TraversalListener<WikiPage> observer) {
    return new WhereUsedPageFinder(page, observer);
  }

  @Override
  protected WikiPage getSearchScope() {
    return root;
  }

  @Override
  protected String getTemplate() {
    return "searchResults";
  }

  @Override
  protected String getTitle() {
    return "Where Used Results";
  }

}
