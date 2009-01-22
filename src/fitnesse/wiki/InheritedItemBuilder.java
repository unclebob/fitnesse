// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class InheritedItemBuilder {
  protected List<String> getInheritedItems(WikiPage page, Set<WikiPage> visitedPages) throws Exception {
    List<String> items = new ArrayList<String>();
    addItemsFromPage(page, items);

    List<WikiPage> ancestors = WikiPageUtil.getAncestorsOf(page);
    for (WikiPage ancestor : ancestors) {
      if (!visitedPages.contains(ancestor)) {
        visitedPages.add(ancestor);
        addItemsFromPage(ancestor, items);
      }

    }
    return items;
  }

  private void addItemsFromPage(WikiPage itemPage, List<String> items) throws Exception {
    List<String> itemsOnThisPage = getItemsFromPage(itemPage);
    items.addAll(itemsOnThisPage);
  }

  protected abstract List<String> getItemsFromPage(WikiPage page) throws Exception;
}
