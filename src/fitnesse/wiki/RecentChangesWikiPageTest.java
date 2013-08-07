// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.util.List;

import util.RegexTestCase;
import fitnesse.wiki.mem.InMemoryPage;

public class RecentChangesWikiPageTest extends RegexTestCase {
  private WikiPage rootPage;
  private WikiPage newPage;
  private WikiPage page1;
  private WikiPage page2;
  private RecentChangesWikiPage recentChangesWikiPage;

  public void setUp() throws Exception {
    rootPage = InMemoryPage.makeRoot("RooT");
    newPage = rootPage.addChildPage("SomeNewPage");
    page1 = rootPage.addChildPage("PageOne");
    page2 = rootPage.addChildPage("PageTwo");
    recentChangesWikiPage = new RecentChangesWikiPage();
  }

  public void tearDown() throws Exception {
  }

  public void testFirstRecentChange() throws Exception {
    assertEquals(false, rootPage.hasChildPage("RecentChanges"));
    recentChangesWikiPage.updateRecentChanges(newPage.getData());
    assertEquals(true, rootPage.hasChildPage("RecentChanges"));
    WikiPage recentChanges = rootPage.getChildPage("RecentChanges");
    List<String> lines = recentChangesWikiPage.getRecentChangesLines(recentChanges.getData());
    assertEquals(1, lines.size());
    assertHasRegexp("SomeNewPage", lines.get(0));
  }

  public void testTwoChanges() throws Exception {
    recentChangesWikiPage.updateRecentChanges(page1.getData());
    recentChangesWikiPage.updateRecentChanges(page2.getData());
    WikiPage recentChanges = rootPage.getChildPage("RecentChanges");
    List<String> lines = recentChangesWikiPage.getRecentChangesLines(recentChanges.getData());
    assertEquals(2, lines.size());
    assertHasRegexp("PageTwo", lines.get(0));
    assertHasRegexp("PageOne", lines.get(1));
  }

  public void testNoDuplicates() throws Exception {
    recentChangesWikiPage.updateRecentChanges(page1.getData());
    recentChangesWikiPage.updateRecentChanges(page1.getData());
    WikiPage recentChanges = rootPage.getChildPage("RecentChanges");
    List<String> lines = recentChangesWikiPage.getRecentChangesLines(recentChanges.getData());
    assertEquals(1, lines.size());
    assertHasRegexp("PageOne", lines.get(0));
  }

  public void testMaxSize() throws Exception {
    for (int i = 0; i < 101; i++) {
      StringBuffer b = new StringBuffer("LotsOfAs");
      for (int j = 0; j < i; j++)
        b.append("a");
      WikiPage page = rootPage.addChildPage(b.toString());
      recentChangesWikiPage.updateRecentChanges(page.getData());
    }

    WikiPage recentChanges = rootPage.getChildPage("RecentChanges");
    List<String> lines = recentChangesWikiPage.getRecentChangesLines(recentChanges.getData());
    assertEquals(100, lines.size());
  }

  public void testUsernameColumnWithoutUser() throws Exception {
    recentChangesWikiPage.updateRecentChanges(page1.getData());
    WikiPage recentChanges = rootPage.getChildPage("RecentChanges");
    List<String> lines = recentChangesWikiPage.getRecentChangesLines(recentChanges.getData());
    String line = lines.get(0).toString();
    assertSubString("|PageOne||", line);
  }

  public void testUsernameColumnWithUser() throws Exception {
    PageData data = page1.getData();
    data.setAttribute(PageData.LAST_MODIFYING_USER, "Aladdin");
    page1.commit(data);

    recentChangesWikiPage.updateRecentChanges(page1.getData());
    WikiPage recentChanges = rootPage.getChildPage("RecentChanges");
    List<String> lines = recentChangesWikiPage.getRecentChangesLines(recentChanges.getData());
    String line = lines.get(0).toString();
    assertSubString("|PageOne|Aladdin|", line);
  }
}
