// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.components;

import junit.framework.TestCase;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class SaveRecorderTest extends TestCase {
  public WikiPage somePage;
  public WikiPage root;
  private PageCrawler crawler;

  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
    somePage = crawler.addPage(root, PathParser.parse("SomePage"), "some page");
  }

  public void tearDown() throws Exception {
  }

  public void testTiming() throws Exception {
    PageData data = somePage.getData();
    long time = SaveRecorder.pageSaved(data);
    somePage.commit(data);
    assertEquals(true, SaveRecorder.changesShouldBeMerged(time - 1, 0, somePage.getData()));
    assertEquals(false, SaveRecorder.changesShouldBeMerged(time + 1, 0, somePage.getData()));
  }

  public void testDefaultValues() throws Exception {
    WikiPage neverSaved = crawler.addPage(root, PathParser.parse("NeverSaved"), "never saved");
    assertEquals(false, SaveRecorder.changesShouldBeMerged(12345, 0, neverSaved.getData()));
  }

}
