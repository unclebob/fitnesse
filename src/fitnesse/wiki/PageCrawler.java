// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import fitnesse.components.TraversalListener;

public interface PageCrawler {
  WikiPage getPage(WikiPage context, WikiPagePath path);

  WikiPage getPage(WikiPage context, WikiPagePath path, PageCrawlerDeadEndStrategy deadEndStrategy);

  boolean pageExists(WikiPage context, WikiPagePath path);

  WikiPagePath getFullPathOfChild(WikiPage parent, WikiPagePath childPath);

  WikiPagePath getFullPath(WikiPage page);

  String getRelativeName(WikiPage base, WikiPage page);

  boolean isRoot(WikiPage page);

  WikiPage getRoot(WikiPage page);

  void traverse(WikiPage root, TraversalListener<? super WikiPage> callback);

  WikiPage getSiblingPage(WikiPage page, WikiPagePath pathRelativeToSibling);

  WikiPage findAncestorWithName(WikiPage page, String name);

  WikiPage getClosestInheritedPage(WikiPage context, String pageName);
}