// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import fitnesse.components.TraversalListener;

//TODO after extracting the WikiPageModel... rethink this class.  Lots of these methods might be able to go back into WikiPAge.
public interface PageCrawler {
  WikiPage getPage(WikiPage context, WikiPagePath path) throws Exception;

  void setDeadEndStrategy(PageCrawlerDeadEndStrategy strategy);

  boolean pageExists(WikiPage context, WikiPagePath path) throws Exception;

  WikiPagePath getFullPathOfChild(WikiPage parent, WikiPagePath childPath) throws Exception;

  WikiPagePath getFullPath(WikiPage page) throws Exception;

  WikiPage addPage(WikiPage context, WikiPagePath path, String content) throws Exception;

  WikiPage addPage(WikiPage context, WikiPagePath path) throws Exception;

  String getRelativeName(WikiPage base, WikiPage page) throws Exception;

  boolean isRoot(WikiPage page) throws Exception;

  WikiPage getRoot(WikiPage page) throws Exception;

  void traverse(WikiPage root, TraversalListener pageCrawlerTest) throws Exception;

  WikiPage getSiblingPage(WikiPage page, WikiPagePath pathRelativeToSibling) throws Exception;
}
