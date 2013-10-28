// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki.mem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;

import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import org.junit.Before;
import org.junit.Test;

public class InMemoryPageTest {
  private WikiPage page1;
  private WikiPage page2;

  @Before
  public void setUp() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    page1 = WikiPageUtil.addPage(root, PathParser.parse("PageOne"), "page one");
    page2 = WikiPageUtil.addPage(root, PathParser.parse("PageTwo"), "page two");
  }

  @Test
  public void testCommitUsesProperPageWhenCommitingFromOtherPage() throws Exception {
    PageData data = page1.getData();
    page2.commit(data);
    data = page2.getData();

    assertSame(page2, data.getWikiPage());
  }

  @Test
  public void testVersions() throws Exception {
    PageData data = page1.getData();
    data.setContent("version 1");
    page1.commit(data);
    data.setContent("version 2");
    page1.commit(data);

    data = page1.getData();
    Collection<VersionInfo> versions = page1.getVersions();

    assertEquals(4, versions.size());
  }

  @Test
  public void testVersionAuthor() throws Exception {
    PageData data = page1.getData();
    Collection<VersionInfo> versions = page1.getVersions();
    for (Iterator<VersionInfo> iterator = versions.iterator(); iterator.hasNext();) {
      VersionInfo versionInfo = iterator.next();
      assertEquals("", versionInfo.getAuthor());
    }

    data.setAttribute(PageData.LAST_MODIFYING_USER, "Joe");
    page1.commit(data);
    page1.commit(data);

    data = page1.getData();
    versions = page1.getVersions();
    boolean joeFound = false;
    for (Iterator<VersionInfo> iterator = versions.iterator(); iterator.hasNext();) {
      VersionInfo versionInfo = iterator.next();
      if ("Joe".equals(versionInfo.getAuthor()))
        joeFound = true;
    }

    assertTrue(joeFound);
  }
}
