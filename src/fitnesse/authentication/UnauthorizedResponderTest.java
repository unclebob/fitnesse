// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.authentication;

import util.RegexTestCase;
import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;

public class UnauthorizedResponderTest extends RegexTestCase {
  MockRequest request;
  private FitNesseContext context;

  public void setUp() throws Exception {
    request = new MockRequest();
    context = FitNesseUtil.makeTestContext(null);
  }

  public void testContentOfPage() throws Exception {
    request.setResource("Blah");
    Responder responder = new UnauthorizedResponder();
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    String content = response.getContent();

    assertSubString("Blah", content);
  }

}
