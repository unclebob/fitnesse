// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import util.RegexTestCase;
import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.templateUtilities.HtmlPageFactory;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;

public class NotFoundResponderTest extends RegexTestCase {
  
  private FitNesseContext context;

  public void setUp() {
    context = FitNesseUtil.makeTestContext();
    context.htmlPageFactory = new HtmlPageFactory();
  }
  
  public void testResponse() throws Exception {
    MockRequest request = new MockRequest();
    request.setResource("some page");

    Responder responder = new NotFoundResponder();
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);

    assertEquals(404, response.getStatus());

    String body = response.getContent();

    assertHasRegexp("<html>", body);
    assertHasRegexp("<body", body);
    assertHasRegexp("some page", body);
    assertHasRegexp("Not Found", body);
  }

  public void testHasEditLinkForWikiWords() throws Exception {
    MockRequest request = new MockRequest();
    request.setResource("PageOne.PageTwo");
    WikiPage root = InMemoryPage.makeRoot("RooT");

    Responder responder = new NotFoundResponder();
    SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);

    assertHasRegexp("\"PageOne[.]PageTwo[?]edit\"", response.getContent());
  }

}
