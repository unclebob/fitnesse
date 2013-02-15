// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import junit.framework.TestCase;

public class VirtualEnabledPageCrawlerTest extends TestCase {
  private WikiPage root;
  private WikiPage target;
  private WikiPage vlink;
  private WikiPage child1;
  private PageCrawler crawler;
  private WikiPagePath child1Path = PathParser.parse("ChildOne");

  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
    crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
    target = crawler.addPage(root, PathParser.parse("TargetPage"));
    vlink = crawler.addPage(root, PathParser.parse("VirtualLink"));
    child1 = crawler.addPage(target, child1Path);
    crawler.addPage(child1, PathParser.parse("GrandChildOne"));
  }

  public void tearDown() throws Exception {
  }

  public void testCanCrossVirtualLink() throws Exception {
    WikiPage virtualChild = crawler.getPage(vlink, child1Path);
    assertNotNull(virtualChild);
    assertEquals("ChildOne", virtualChild.getName());
    assertNotNull(crawler.getPage(vlink, PathParser.parse("ChildOne.GrandChildOne")));
  }

  public void testFullyQualifiedNameUsesVirtualParent() throws Exception {
    WikiPage virtualChildPage = crawler.getPage(vlink, child1Path);
    WikiPagePath virtualChildFullPath = PathParser.parse("VirtualLink.ChildOne");
    assertEquals(virtualChildFullPath, crawler.getFullPath(virtualChildPage));
  }
}
