// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import fitnesse.components.TraversalListener;

import java.util.List;

public interface PageCrawler {
  WikiPage getPage(WikiPage context, WikiPagePath path);

  WikiPage getPage(WikiPage context, WikiPagePath path, PageCrawlerDeadEndStrategy deadEndStrategy);

  boolean pageExists(WikiPage context, WikiPagePath path);

  WikiPagePath getFullPathOfChild(WikiPage parent, WikiPagePath childPath);

  WikiPagePath getFullPath(WikiPage page);

  String getRelativeName(WikiPage base, WikiPage page);

  // Should become a property of WIkiPage
  boolean isRoot(WikiPage page);

  WikiPage getRoot(WikiPage page);

  void traverse(WikiPage root, TraversalListener<? super WikiPage> callback);

  WikiPage getSiblingPage(WikiPage page, WikiPagePath pathRelativeToSibling);

  WikiPage findAncestorWithName(WikiPage page, String name);

  WikiPage getClosestInheritedPage(WikiPage context, String pageName);

  // TODO: make these use TraversalListener
  List<WikiPage> getAllUncles(WikiPage context, String uncleName);

  List<WikiPage> getAncestorsOf(WikiPage page);

  List<WikiPage> getAncestorsStartingWith(WikiPage context);
}