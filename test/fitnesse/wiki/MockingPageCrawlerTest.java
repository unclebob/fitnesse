// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import fitnesse.wiki.fs.InMemoryPage;
import org.junit.Before;
import org.junit.Test;

public class MockingPageCrawlerTest {
  private PageCrawler crawler;

  @Before
  public void setUp() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
  }

  @Test
  public void testGetMockPageSimple() throws Exception {
    WikiPagePath pageOnePath = PathParser.parse("PageOne");
    WikiPage mockPage = crawler.getPage(pageOnePath, new MockingPageCrawler());
    assertNotNull(mockPage);
    assertTrue(mockPage instanceof WikiPageDummy);
    assertEquals("PageOne", mockPage.getName());
  }

  @Test
  public void testGetMockPageMoreComplex() throws Exception {
    WikiPagePath otherPagePath = PathParser.parse("PageOne.SomePage.OtherPage");
    WikiPage mockPage = crawler.getPage(otherPagePath, new MockingPageCrawler());
    assertNotNull(mockPage);
    assertTrue(mockPage instanceof WikiPageDummy);
    assertEquals("OtherPage", mockPage.getName());
  }
}
