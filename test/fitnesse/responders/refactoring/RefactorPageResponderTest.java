// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.refactoring;

import static org.junit.Assert.assertEquals;
import static util.RegexTestCase.assertHasRegexp;
import static util.RegexTestCase.assertSubString;

import fitnesse.Responder;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.ResponderTestCase;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPageUtil;
import org.junit.Before;
import org.junit.Test;

public class RefactorPageResponderTest extends ResponderTestCase {

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    String childPage = "ChildPage";
    WikiPageUtil.addPage(root, PathParser.parse(childPage), "content");
    request.setResource(childPage);
  }

  @Override
  protected Responder responderInstance() {
    return new RefactorPageResponder();
  }

  @Test
  public void testHtml() throws Exception {
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    assertEquals(200, response.getStatus());

    String content = response.getContent();
    assertSubString("Replace", content);
    assertSubString("Delete Page", content);
    assertSubString("Rename Page", content);
    assertSubString("Move Page", content);
  }

  @Test
  public void replaceFormsOfferScopeWithPageSelected() throws Exception {
    request.setResource("ChildPage.GrandChild");
    WikiPageUtil.addPage(root, PathParser.parse("ChildPage.GrandChild"), "content");
    String content = ((SimpleResponse) responder.makeResponse(context, request)).getContent();

    assertHasRegexp("id=\"replaceScopeRoot\" name=\"searchScope\" value=\"root\"/>All", content);
    assertHasRegexp("id=\"replaceScopeChildPage\" name=\"searchScope\" value=\"ChildPage\"/>ChildPage", content);
    assertHasRegexp("id=\"replaceScopePage\" name=\"searchScope\" value=\"\" checked=\"checked\"/>GrandChild", content);
    assertHasRegexp("id=\"replaceMethodScopePage\" name=\"searchScope\" value=\"\" checked=\"checked\"/>GrandChild", content);
    assertHasRegexp("<button type=\"submit\" name=\"dryRun\"", content);
  }

  @Test
  public void testMoveWithoutStartPath() throws Exception {
    request.addInput("type", "move");
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);

    String content = response.getContent();
    assertSubString("<div id=\"configPath\" data-path=\"ROOT\"></div>", content);
  }

  @Test
  public void testMoveWithStartPath() throws Exception {
    context.getProperties().put("Refactor.StartPath", "SomeStartPath");

    request.addInput("type", "move");
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);

    String content = response.getContent();
    assertSubString("<div id=\"configPath\" data-path=\"SomeStartPath\"></div>", content);
  }
}
