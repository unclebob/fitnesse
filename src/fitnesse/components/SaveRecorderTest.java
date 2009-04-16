// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.components;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class SaveRecorderTest {
  public WikiPage somePage;
  public WikiPage root;
  private PageCrawler crawler;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
    somePage = crawler.addPage(root, PathParser.parse("SomePage"), "some page");
  }

  @Test
  public void testTiming() throws Exception {
    PageData data = somePage.getData();
    long savedTicket = 0;
    long editTicket = 1;
    long time = SaveRecorder.pageSaved(data, savedTicket);
    somePage.commit(data);
    assertTrue(SaveRecorder.changesShouldBeMerged(time - 1, editTicket, somePage.getData()));
    assertFalse(SaveRecorder.changesShouldBeMerged(time + 1, editTicket, somePage.getData()));
  }

  @Test
  public void testDefaultValues() throws Exception {
    WikiPage neverSaved = crawler.addPage(root, PathParser.parse("NeverSaved"), "never saved");
    assertFalse(SaveRecorder.changesShouldBeMerged(12345, 0, neverSaved.getData()));
  }

  @Test
  public void testCanSaveOutOfOrderIfFromSameEditSession() throws Exception {
    PageData data = somePage.getData();
    long ticket = 99;
    long time = SaveRecorder.pageSaved(data, ticket);
    somePage.commit(data);
    assertFalse(SaveRecorder.changesShouldBeMerged(time-1, ticket, data));
  }

}
