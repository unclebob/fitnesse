// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.search;

import fitnesse.components.SearchObserver;
import fitnesse.components.WhereUsedPageFinder;
import fitnesse.html.HtmlElement;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;

public class WhereUsedResponder extends ResultResponder {

  @Override
  protected void startSearching(SearchObserver observer) {
    new WhereUsedPageFinder(page, observer).search(root);
  }

  protected String getTitle() {
    return "Where Used Results";
  }

}
