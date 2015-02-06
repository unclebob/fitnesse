// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

//TODO rename me
public class MockingPageCrawler implements PageCrawlerDeadEndStrategy {
  public WikiPage getPageAfterDeadEnd(WikiPage context, WikiPagePath restOfPath, PageCrawler crawler) {
    return createMockPage(restOfPath.last(), context);
  }

  public static WikiPage createMockPage(String pageName, WikiPage context) {
    WikiPageDummy pageDummy = new WikiPageDummy(pageName, "", context);
    return pageDummy;
  }
}
