// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import fitnesse.components.TraversalListener;

//TODO after extracting the WikiPageModel... rethink this class.  Lots of these methods might be able to go back into WikiPAge.
public interface PageCrawler {
  WikiPage getPage(WikiPage context, WikiPagePath path);

  void setDeadEndStrategy(PageCrawlerDeadEndStrategy strategy);

  boolean pageExists(WikiPage context, WikiPagePath path);

  WikiPagePath getFullPathOfChild(WikiPage parent, WikiPagePath childPath);

  WikiPagePath getFullPath(WikiPage page);

  WikiPage addPage(WikiPage context, WikiPagePath path, String content);

  WikiPage addPage(WikiPage context, WikiPagePath path);

  String getRelativeName(WikiPage base, WikiPage page);

  boolean isRoot(WikiPage page);

  WikiPage getRoot(WikiPage page);

  void traverse(WikiPage root, TraversalListener pageCrawlerTest);

  WikiPage getSiblingPage(WikiPage page, WikiPagePath pathRelativeToSibling);

  WikiPage findAncestorWithName(WikiPage page, String name);
}