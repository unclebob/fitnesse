// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PageCreatorTest {

  @Before
  public void setUp() throws Exception {
    new SetUp();
  }

  @After
  public void tearDown() throws Exception {
    new TearDown();
  }

  @Test
  public void testCreatePage() throws Exception {
    WikiPage testPage = makePage("TestPage", "contents", "attr=val");
    assertNotNull(testPage);
    PageData data = testPage.getData();
    assertEquals("contents", data.getContent());
    assertEquals("val", data.getAttribute("attr"));
  }

  private WikiPage makePage(String pageName, String pageContent, String pageAttributes) throws Exception {
    PageCreator creator = new PageCreator();
    creator.setPageName(pageName);
    creator.setPageContents(pageContent);
    creator.setPageAttributes(pageAttributes);
    assertTrue(creator.valid());
    WikiPage testPage = FitnesseFixtureContext.context.getRootPage().getChildPage("TestPage");
    return testPage;
  }

  @Test
  public void testMultipleAttributes() throws Exception {
    WikiPage testPage = makePage("TestPage", "Contents", "att1=one,att2=two");
    PageData data = testPage.getData();
    assertEquals("one", data.getAttribute("att1"));
    assertEquals("two", data.getAttribute("att2"));
  }
}
