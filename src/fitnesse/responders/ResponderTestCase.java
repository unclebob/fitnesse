// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import util.RegexTestCase;
import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.WikiPage;

public abstract class ResponderTestCase extends RegexTestCase {
  protected WikiPage root;
  protected MockRequest request;
  protected Responder responder;
  protected PageCrawler crawler;
  protected FitNesseContext context;

  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
    request = new MockRequest();
    responder = responderInstance();
    context = new FitNesseContext(root);
  }

  // Return an instance of the Responder being tested.
  protected abstract Responder responderInstance();
}
