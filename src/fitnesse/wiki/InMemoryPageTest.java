// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

public class InMemoryPageTest extends TestCase {
  private WikiPage root;
  private PageCrawler crawler;
  private WikiPage page1;
  private WikiPage page2;

  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
    page1 = crawler.addPage(root, PathParser.parse("PageOne"), "page one");
    page2 = crawler.addPage(root, PathParser.parse("PageTwo"), "page two");
  }

  public void tearDown() throws Exception {
  }

  public void testCommitUsesProperPageWhenCommitingFromOtherPage() throws Exception {
    PageData data = page1.getData();
    page2.commit(data);
    data = page2.getData();

    assertSame(page2, data.getWikiPage());
  }

  public void testVersions() throws Exception {
    PageData data = page1.getData();
    data.setContent("version 1");
    page1.commit(data);
    data.setContent("version 2");
    page1.commit(data);

    data = page1.getData();
    Set<VersionInfo> versions = data.getVersions();

    assertEquals(3, versions.size());
  }

  public void testVersionAuthor() throws Exception {
    PageData data = page1.getData();
    Set<VersionInfo> versions = data.getVersions();
    for (Iterator<VersionInfo> iterator = versions.iterator(); iterator.hasNext();) {
      VersionInfo versionInfo = iterator.next();
      assertEquals("", versionInfo.getAuthor());
    }

    data.setAttribute(WikiPage.LAST_MODIFYING_USER, "Joe");
    page1.commit(data);
    page1.commit(data);

    data = page1.getData();
    versions = data.getVersions();
    boolean joeFound = false;
    for (Iterator<VersionInfo> iterator = versions.iterator(); iterator.hasNext();) {
      VersionInfo versionInfo = iterator.next();
      if ("Joe".equals(versionInfo.getAuthor()))
        joeFound = true;
    }

    assertTrue(joeFound);
  }
}
