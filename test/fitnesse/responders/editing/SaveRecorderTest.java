// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.editing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fitnesse.wiki.*;
import org.junit.Before;
import org.junit.Test;

import fitnesse.wiki.fs.InMemoryPage;

public class SaveRecorderTest {
  public WikiPage somePage;
  public WikiPage root;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    somePage = WikiPageUtil.addPage(root, PathParser.parse("SomePage"), "some page");
  }

  @Test
  public void testTiming() throws Exception {
    PageData data = somePage.getData();
    long savedTicket = 0;
    long editTicket = 1;
    long time = SaveRecorder.pageSaved(somePage, savedTicket);
    somePage.commit(data);
    assertTrue(SaveRecorder.changesShouldBeMerged(time - 1, editTicket, somePage));
    assertFalse(SaveRecorder.changesShouldBeMerged(time + 1, editTicket, somePage));
  }

  @Test
  public void testDefaultValues() throws Exception {
    WikiPage neverSaved = WikiPageUtil.addPage(root, PathParser.parse("NeverSaved"), "never saved");
    assertFalse(SaveRecorder.changesShouldBeMerged(12345, 0, neverSaved));
  }

  @Test
  public void testCanSaveOutOfOrderIfFromSameEditSession() throws Exception {
    PageData data = somePage.getData();
    long ticket = 99;
    long time = SaveRecorder.pageSaved(somePage, ticket);
    somePage.commit(data);
    assertFalse(SaveRecorder.changesShouldBeMerged(time-1, ticket, somePage));
  }

}
