// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.editing;

import util.RegexTestCase;
import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class MergeResponderTest extends RegexTestCase {
  private WikiPage source;
  private MockRequest request;

  public void setUp() throws Exception {
    source = InMemoryPage.makeRoot("RooT");
    source.getPageCrawler().addPage(source, PathParser.parse("SimplePage"), "this is SimplePage");
    request = new MockRequest();
    request.setResource("SimplePage");
    request.addInput(EditResponder.TIME_STAMP, "");
    request.addInput(EditResponder.CONTENT_INPUT_NAME, "some new content");
  }

  public void tearDown() throws Exception {
  }

  public void testHtml() throws Exception {
    Responder responder = new MergeResponder(request);
    SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(source), new MockRequest());
    assertHasRegexp("name=\\\"" + EditResponder.CONTENT_INPUT_NAME + "\\\"", response.getContent());
    assertHasRegexp("this is SimplePage", response.getContent());
    assertHasRegexp("name=\\\"oldContent\\\"", response.getContent());
    assertHasRegexp("some new content", response.getContent());
  }

  public void testAttributeValues() throws Exception {
    request.addInput("Edit", "On");
    request.addInput("PageType", "Test");
    request.addInput("Search", "On");
    Responder responder = new MergeResponder(request);
    SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(source), new MockRequest());

    assertHasRegexp("type=\"hidden\"", response.getContent());
    assertHasRegexp("name=\"Edit\"", response.getContent());
    assertHasRegexp("name=\"PageType\" value=\"Test\" checked", response.getContent());
    assertHasRegexp("name=\"Search\"", response.getContent());
  }
}
