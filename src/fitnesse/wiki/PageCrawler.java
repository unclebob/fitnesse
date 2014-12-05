// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import fitnesse.components.TraversalListener;

public interface PageCrawler {
  WikiPage getPage(WikiPagePath path);

  WikiPage getPage(WikiPagePath path, PageCrawlerDeadEndStrategy deadEndStrategy);

  boolean pageExists(WikiPagePath path);

  WikiPagePath getFullPathOfChild(WikiPagePath childPath);

  WikiPagePath getFullPath();

  String getRelativeName(WikiPage page);

  WikiPage getRoot();

  void traverse(TraversalListener<? super WikiPage> callback);

  void traversePageAndAncestors(TraversalListener<? super WikiPage> callback);

  void traverseUncles(String uncleName, TraversalListener<? super WikiPage> callback);

  WikiPage getSiblingPage(WikiPagePath pathRelativeToSibling);

  WikiPage findAncestorWithName(String name);

  WikiPage getClosestInheritedPage(String pageName);
}
