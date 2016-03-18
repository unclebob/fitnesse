// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.editing;

import static util.RegexTestCase.assertHasRegexp;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import org.junit.Before;
import org.junit.Test;

public class MergeResponderTest {
  private FitNesseContext context;
  private MockRequest request;

  @Before
  public void setUp() throws Exception {
    context = FitNesseUtil.makeTestContext();
    WikiPage source = context.getRootPage();
    WikiPageUtil.addPage(source, PathParser.parse("SimplePage"), "this is SimplePage");
    request = new MockRequest();
    request.setResource("SimplePage");
    request.addInput(EditResponder.TIME_STAMP, "");
    request.addInput(EditResponder.CONTENT_INPUT_NAME, "some new content");
  }

  @Test
  public void testHtml() throws Exception {
    Responder responder = new MergeResponder(request);
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, new MockRequest());
    assertHasRegexp("name=\\\"" + EditResponder.CONTENT_INPUT_NAME + "\\\"", response.getContent());
    assertHasRegexp("this is SimplePage", response.getContent());
    assertHasRegexp("name=\\\"oldContent\\\"", response.getContent());
    assertHasRegexp("some new content", response.getContent());
  }

  @Test
  public void testAttributeValues() throws Exception {
    request.addInput("Edit", "On");
    request.addInput("PageType", "Test");
    request.addInput("Search", "On");
    Responder responder = new MergeResponder(request);
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, new MockRequest());

    assertHasRegexp("type=\"hidden\"", response.getContent());
    assertHasRegexp("name=\"Edit\"", response.getContent());
    assertHasRegexp("name=\"PageType\" value=\"Test\" checked", response.getContent());
    assertHasRegexp("name=\"Search\"", response.getContent());
  }
}
