// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

// TODO rename me
public class VirtualMockingPageCrawler extends VirtualEnabledPageCrawler {
  public WikiPage getPageAfterDeadEnd(WikiPage context, WikiPagePath restOfPath, PageCrawler crawler) throws Exception {
    WikiPage page = super.getPageAfterDeadEnd(context, restOfPath, crawler);
    if (page == null)
      page = MockingPageCrawler.createMockPage(restOfPath.last(), context);

    return page;
  }
}
