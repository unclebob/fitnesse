// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

//TODO rename me
public class MockingPageCrawler implements PageCrawlerDeadEndStrategy
{
	public WikiPage getPageAfterDeadEnd(WikiPage context, WikiPagePath restOfPath, PageCrawler crawler) throws Exception
	{
		return createMockPage(restOfPath.last(), context);
	}

  public static WikiPage createMockPage(String pageName, WikiPage context) throws Exception
  {
    MockWikiPage mockPage = new MockWikiPage(pageName, "");
    mockPage.setParent(context);
    return mockPage;
  }
}