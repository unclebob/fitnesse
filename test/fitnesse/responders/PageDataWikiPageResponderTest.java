// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import static org.junit.Assert.assertEquals;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import org.junit.Before;
import org.junit.Test;

public class PageDataWikiPageResponderTest {
  WikiPage root;
  WikiPage pageOne;
  private FitNesseContext context;

  @Before
  public void setUp() throws Exception {
    context = FitNesseUtil.makeTestContext();
    root = context.getRootPage();
    pageOne = WikiPageUtil.addPage(root, PathParser.parse("PageOne"), "Line one\nLine two");
  }

  @Test
  public void testGetPageData() throws Exception {
    Responder responder = new PageDataWikiPageResponder();
    MockRequest request = new MockRequest();
    request.setResource("PageOne");
    request.addInput("pageData", "");
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    assertEquals(pageOne.getData().getContent(), response.getContent());
  }
}
