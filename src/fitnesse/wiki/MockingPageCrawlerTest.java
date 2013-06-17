// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import fitnesse.wiki.mem.InMemoryPage;
import junit.framework.TestCase;

public class MockingPageCrawlerTest extends TestCase {
  private WikiPage root;
  private PageCrawler crawler;

  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
  }

  public void tearDown() throws Exception {
  }

  public void testGetMockPageSimple() throws Exception {
    WikiPagePath pageOnePath = PathParser.parse("PageOne");
    WikiPage mockPage = crawler.getPage(pageOnePath, new MockingPageCrawler());
    assertNotNull(mockPage);
    assertTrue(mockPage instanceof WikiPageDummy);
    assertEquals("PageOne", mockPage.getName());
  }

  public void testGetMockPageMoreComplex() throws Exception {
    WikiPagePath otherPagePath = PathParser.parse("PageOne.SomePage.OtherPage");
    WikiPage mockPage = crawler.getPage(otherPagePath, new MockingPageCrawler());
    assertNotNull(mockPage);
    assertTrue(mockPage instanceof WikiPageDummy);
    assertEquals("OtherPage", mockPage.getName());
  }
}
