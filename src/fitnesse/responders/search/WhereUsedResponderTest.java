// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.search;

import fitnesse.wiki.*;
import util.RegexTestCase;
import fitnesse.FitNesseContext;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.wiki.mem.InMemoryPage;

public class WhereUsedResponderTest extends RegexTestCase {
  private WikiPage root;
  private WikiPage pageTwo;

  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    FitNesseContext context = FitNesseUtil.makeTestContext(root);
    WikiPageUtil.addPage(root, PathParser.parse("PageOne"), "PageOne");
    pageTwo = WikiPageUtil.addPage(root, PathParser.parse("PageTwo"), "PageOne");
    WikiPageUtil.addPage(pageTwo, PathParser.parse("ChildPage"), ".PageOne");
  }

  public void testResponse() throws Exception {
    MockRequest request = new MockRequest();
    request.setResource("PageOne");
    WhereUsedResponder responder = new WhereUsedResponder();

    Response response = responder.makeResponse(FitNesseUtil.makeTestContext(root), request);
    MockResponseSender sender = new MockResponseSender();
    response.sendTo(sender);

    String content = sender.sentData();
    assertEquals(200, response.getStatus());
    assertHasRegexp("Where Used", content);
    assertHasRegexp("PageOne", content);
    assertHasRegexp("PageTwo", content);
    assertHasRegexp("PageTwo\\.ChildPage", content);
  }
}

