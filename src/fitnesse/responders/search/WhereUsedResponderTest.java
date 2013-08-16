// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.search;

import static org.junit.Assert.assertEquals;
import static util.RegexTestCase.assertHasRegexp;

import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.mem.InMemoryPage;
import org.junit.Before;
import org.junit.Test;

public class WhereUsedResponderTest {
  private WikiPage root;
  private WikiPage pageTwo;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    FitNesseContext context = FitNesseUtil.makeTestContext(root);
    WikiPageUtil.addPage(root, PathParser.parse("PageOne"), "PageOne");
    pageTwo = WikiPageUtil.addPage(root, PathParser.parse("PageTwo"), "PageOne");
    WikiPageUtil.addPage(pageTwo, PathParser.parse("ChildPage"), ".PageOne");
  }

  @Test
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

