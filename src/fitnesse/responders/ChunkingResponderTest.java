// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import util.RegexTestCase;
import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageDummy;

public class ChunkingResponderTest extends RegexTestCase {

  private Exception exception;
  private Response response;
  private FitNesseContext context;
  private WikiPage root = new WikiPageDummy();
  private ChunkingResponder responder = new ChunkingResponder() {
    protected void doSending() throws Exception {
      throw exception;
    }
  };

  protected void setUp() throws Exception {
    context = new FitNesseContext();
    context.root = root;
  }

  public void testException() throws Exception {
    exception = new Exception("test exception");
    response = responder.makeResponse(context, new MockRequest());
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    String responseSender = sender.sentData();
    assertSubString("test exception", responseSender);
  }
}
