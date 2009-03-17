// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.

package fitnesse.wiki;

import junit.framework.TestCase;
import fitnesse.testutil.SimpleCachinePage;

public class CachingPageTest extends TestCase {
  private CachingPage root;
  private PageCrawler crawler;
  private WikiPagePath pageOnePath;
  private WikiPagePath childOnePath;
  private WikiPagePath rootPath;

  public void setUp() throws Exception {
    root = new SimpleCachinePage("RooT", null);
    crawler = root.getPageCrawler();
    pageOnePath = PathParser.parse(".PageOne");
    childOnePath = PathParser.parse(".PageOne.ChildOne");
    rootPath = PathParser.parse("root");
  }

  public void testCreate() throws Exception {
    String alpha = "AlphaAlpha";
    WikiPage root = InMemoryPage.makeRoot("root");
    assertFalse(root.hasChildPage(alpha));

    crawler.addPage(root, PathParser.parse(alpha), "content");
    assertTrue(root.hasChildPage(alpha));
  }

  public void testTwoLevel() throws Exception {
    String alpha = "AlphaAlpha";
    String beta = "BetaBeta";
    WikiPage subPage1 = crawler.addPage(root, PathParser.parse(alpha));
    crawler.addPage(subPage1, PathParser.parse(beta));
    assertTrue(crawler.pageExists(root, PathParser.parse(alpha + "." + beta)));

  }

  public void testDoubleDot() throws Exception {
    String alpha = "AlphaAlpha";
    String beta = "BetaBeta";
    WikiPage subPage1 = crawler.addPage(root, PathParser.parse(alpha));
    crawler.addPage(subPage1, PathParser.parse(beta));
    assertFalse(crawler.pageExists(root, PathParser.parse(alpha + ".." + beta)));

  }

  public void testClearPage() throws Exception {
    String child = "ChildPage";
    crawler.addPage(root, PathParser.parse(child), "content");
    assertTrue(root.hasCachedSubpage(child));
    root.removeChildPage(child);
    assertFalse(root.hasCachedSubpage(child));
  }

  public void testGetName() throws Exception {
    WikiPage frontPage = crawler.addPage(root, PathParser.parse("FrontPage"), "FrontPage");
    WikiPage c1 = crawler.addPage(frontPage, PathParser.parse("ChildOne"), "ChildOne");
    assertEquals("ChildOne", c1.getName());
    assertEquals(PathParser.parse("FrontPage.ChildOne"), crawler.getFullPath(c1));
  }

  public void testDefaultAttributes() throws Exception {
    WikiPage page = crawler.addPage(root, PathParser.parse("SomePage"));
    assertTrue(page.getData().hasAttribute("Edit"));
    assertTrue(page.getData().hasAttribute("Search"));
    assertFalse(page.getData().hasAttribute("Test"));
    assertFalse(page.getData().hasAttribute("TestSuite"));
  }

  public void testPageDataIsCached() throws Exception {
    CachingPage.cacheTime = 100;
    CachingPage page = (CachingPage) crawler.addPage(root, PathParser.parse("PageOne"), "some content");

    PageData data1 = page.getCachedData();
    PageData data2 = page.getCachedData();
    Thread.sleep(200);

    PageData data3 = page.getData();

    assertSame(data1, data2);
    assertNotSame(data1, data3);
  }

  public void testDumpCachedExpiredData() throws Exception {
    CachingPage.cacheTime = 100;
    CachingPage page = (CachingPage) crawler.addPage(root, PathParser.parse("PageOne"), "some content");
    PageData data = page.getData();
    assertNotNull(data);
    Thread.sleep(200);
    ((CachingPage) page).dumpExpiredCachedData();
    assertNull(page.getCachedData());
  }

  public void testGetPageThatStartsWithDot() throws Exception {
    WikiPage page1 = crawler.addPage(root, PathParser.parse("PageOne"), "page one");
    WikiPage child1 = crawler.addPage(root, PathParser.parse("PageOne.ChildOne"), "child one");
    assertSame(page1, crawler.getPage(page1, pageOnePath));
    assertSame(child1, crawler.getPage(page1, childOnePath));
    assertSame(page1, crawler.getPage(child1, pageOnePath));
  }

  public void testGetPageUsingRootKeyWord() throws Exception {
    WikiPage page1 = crawler.addPage(root, PathParser.parse("PageOne"), "page one");
    assertSame(root, crawler.getPage(page1, rootPath));
    assertSame(root, crawler.getPage(root, rootPath));
  }

  public void testEquals() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    WikiPage pageOne = crawler.addPage(root, PathParser.parse("PageOne"), "content");
    assertEquals(pageOne, pageOne);

    root.removeChildPage("PageOne");
    WikiPage pageOneOne = crawler.addPage(root, PathParser.parse("PageOne"));
    assertEquals(pageOne, pageOneOne);
  }

  public void testCachedDataIsTrashedBeforeOutOfMemoryError() throws Exception {
    CachingPage page = (CachingPage) crawler.addPage(root, PathParser.parse("SomePage"), "some content");
    page.getData();
    assertTrue(page.getCachedData() != null);
    boolean exceptionThrown = false;
    try {
      new MemoryEater();
    }
    catch (OutOfMemoryError e) {
      assertTrue(page.getCachedData() == null);
      exceptionThrown = true;
    }
    assertTrue(exceptionThrown);
  }

  class MemoryEater {
    long[] array = new long[1000000];
    MemoryEater eater = new MemoryEater();
  }
}
