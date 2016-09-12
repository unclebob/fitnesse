// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.authentication;

import static org.junit.Assert.assertEquals;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.testutil.SimpleAuthenticator;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperty;

import org.junit.Before;
import org.junit.Test;

public class AuthenticatorTest {
  SimpleAuthenticator authenticator;
  private MockRequest request;
  private Class<? extends Responder> responderType;
  private DummySecureResponder privilegedResponder;
  private FitNesseContext context;

  class DummySecureResponder implements SecureResponder {

    @Override
    public SecureOperation getSecureOperation() {
      return new AlwaysSecureOperation();
    }

    @Override
    public Response makeResponse(FitNesseContext context, Request request) {
      return null;
    }

    protected void refactorReferences(FitNesseContext context, WikiPage pageToBeMoved, String newParentName) {
    }
  }

  @Before
  public void setUp() {
    context = FitNesseUtil.makeTestContext();
    WikiPage root = context.getRootPage();
    WikiPage frontpage = root.addChildPage("FrontPage");
    makeReadSecure(frontpage);
    authenticator = new SimpleAuthenticator();
    privilegedResponder = new DummySecureResponder();

    request = new MockRequest();
    request.setResource("FrontPage");
  }

  private void makeReadSecure(WikiPage frontpage) {
    PageData data = frontpage.getData();
    data.setAttribute(WikiPageProperty.SECURE_READ);
    frontpage.commit(data);
  }

  @Test
  public void testNotAuthenticated() throws Exception {
    makeResponder();
    assertEquals(UnauthorizedResponder.class, responderType);
  }

  @Test
  public void testAuthenticated() throws Exception {
    authenticator.authenticated = true;
    makeResponder();
    assertEquals(DummySecureResponder.class, responderType);
  }

  private void makeResponder() throws Exception {
    Responder responder = authenticator.authenticate(context, request, privilegedResponder);
    responderType = responder.getClass();
  }
}
