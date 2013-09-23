// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.search;

import fitnesse.components.TraversalListener;
import fitnesse.wiki.search.WhereUsedPageFinder;

public class WhereUsedResponder extends ResultResponder {

  @Override
  public void traverse(TraversalListener<Object> observer) {
    new WhereUsedPageFinder(page, observer).search(root);
  }

  protected String getTitle() {
    return "Where Used Results";
  }

}
