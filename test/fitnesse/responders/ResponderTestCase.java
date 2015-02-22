// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.WikiPage;
import org.junit.Before;

public abstract class ResponderTestCase {
  protected WikiPage root;
  protected MockRequest request;
  protected Responder responder;
  protected FitNesseContext context;

  @Before
  public void setUp() throws Exception {
    request = new MockRequest();
    responder = responderInstance();
    context = FitNesseUtil.makeTestContext();
    root = context.getRootPage();
  }

  // Return an instance of the Responder being tested.
  protected abstract Responder responderInstance();
}
