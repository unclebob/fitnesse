// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import static org.junit.Assert.assertEquals;
import static util.RegexTestCase.assertHasRegexp;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import org.junit.Before;
import org.junit.Test;

public class NotFoundResponderTest {
  
  private FitNesseContext context;

  @Before
  public void setUp() {
    context = FitNesseUtil.makeTestContext();
  }

  @Test
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

  @Test
  public void testHasEditLinkForWikiWords() throws Exception {
    MockRequest request = new MockRequest();
    request.setResource("PageOne.PageTwo");

    Responder responder = new NotFoundResponder();
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);

    assertHasRegexp("\"PageOne[.]PageTwo[?]edit\"", response.getContent());
  }

}
