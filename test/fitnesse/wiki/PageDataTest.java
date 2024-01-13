// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import static org.junit.Assert.assertEquals;

import fitnesse.wiki.fs.InMemoryPage;
import org.junit.Before;
import org.junit.Test;

public class PageDataTest {
  public WikiPage page;
  private WikiPage root;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    page = WikiPageUtil.addPage(root, PathParser.parse("PagE"), "some content");
  }

  @Test
  public void testAttributesAreTruelyCopiedInCopyConstructor() {
    PageData data = root.getData();
    data.setAttribute("myFriend", "Joe");
    PageData newData = new PageData(data);
    newData.setAttribute("myFriend", "Jane");

    assertEquals("Joe", data.getAttribute("myFriend"));
  }
}
