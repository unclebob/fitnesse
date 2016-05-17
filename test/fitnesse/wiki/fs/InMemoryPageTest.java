// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki.fs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.fs.InMemoryPage;
import org.junit.Before;
import org.junit.Test;

public class InMemoryPageTest {
  private WikiPage page1;

  @Before
  public void setUp() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    page1 = WikiPageUtil.addPage(root, PathParser.parse("PageOne"), "page one");
  }

  @Test
  public void testVersions() throws Exception {
    PageData data = page1.getData();
    data.setContent("version 1");
    page1.commit(data);
    data.setContent("version 2");
    page1.commit(data);

    Collection<VersionInfo> versions = page1.getVersions();

    assertEquals(3, versions.size());
  }

  @Test
  public void testVersionAuthor() throws Exception {
    PageData data = page1.getData();
    Collection<VersionInfo> versions = page1.getVersions();
    for (VersionInfo versionInfo : versions) {
      assertEquals("", versionInfo.getAuthor());
    }

    data.setAttribute(PageData.LAST_MODIFYING_USER, "Joe");
    page1.commit(data);
    page1.commit(data);

    versions = page1.getVersions();
    boolean joeFound = false;
    for (VersionInfo versionInfo : versions) {
      if ("Joe".equals(versionInfo.getAuthor()))
        joeFound = true;
    }

    assertTrue(joeFound);
  }
}
