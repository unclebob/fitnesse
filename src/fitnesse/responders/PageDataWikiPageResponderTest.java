// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import util.RegexTestCase;
import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class PageDataWikiPageResponderTest extends RegexTestCase {
  WikiPage root;
  WikiPage pageOne;

  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    pageOne = root.getPageCrawler().addPage(root, PathParser.parse("PageOne"), "Line one\nLine two");
  }

  public void testGetPageData() throws Exception {
    Responder responder = new PageDataWikiPageResponder();
    MockRequest request = new MockRequest();
    request.setResource("PageOne");
    request.addInput("pageData", "");
    SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
    assertEquals(pageOne.getData().getContent(), response.getContent());
  }
}
