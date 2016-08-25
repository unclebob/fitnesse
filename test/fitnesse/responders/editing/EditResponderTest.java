// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.editing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static util.RegexTestCase.assertMatches;
import static util.RegexTestCase.assertNotSubString;
import static util.RegexTestCase.assertSubString;

import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.*;

import org.junit.Before;
import org.junit.Test;

public class EditResponderTest {
  private WikiPage root;
  private MockRequest request;
  private EditResponder responder;
  private FitNesseContext context;

  @Before
  public void setUp() throws Exception {
    context = FitNesseUtil.makeTestContext();
    root = context.getRootPage();
    request = new MockRequest();
    responder = new EditResponder();
  }

  @Test
  public void testResponse() throws Exception {
    WikiPage page= WikiPageUtil.addPage(root, PathParser.parse("ChildPage"), "child content with <html>");
    PageData data = page.getData();
    WikiPageProperty properties = data.getProperties();
    properties.set(PageData.PropertySUITES, "Edit Page tags");
    page.commit(data);

    SimpleResponse response = makeResponse();
    assertEquals(200, response.getStatus());

    String body = response.getContent();
    assertSubString("<html>", body);
    assertSubString("<form", body);
    assertSubString("method=\"post\"", body);
    assertSubString("child content with &lt;html&gt;", body);
    assertSubString("name=\"responder\"", body);
    assertSubString("name=\"" + EditResponder.TIME_STAMP + "\"", body);
    assertSubString("name=\"" + EditResponder.TICKET_ID + "\"", body);
    assertSubString("name=\"" + EditResponder.HELP_TEXT + "\"", body);
    assertSubString("select id=\"" + EditResponder.TEMPLATE_MAP + "\"", body);

    assertSubString("type=\"submit\"", body);
    assertSubString("textarea", body);
    assertSubString("<label for=\"suites\">Tags:</label>", body);
  }

  private SimpleResponse makeResponse() throws Exception {
    request.setResource("ChildPage");
    return (SimpleResponse) responder.makeResponse(context, request);
  }

  @Test
  public void testResponseWhenNonexistentPageRequestsed() throws Exception {
    request.setResource("NonExistentPage");
    request.addInput("nonExistent", "true");

    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    assertEquals(200, response.getStatus());

    String body = response.getContent();
    assertSubString("<html>", body);
    assertSubString("<form", body);
    assertSubString("method=\"post\"", body);
    assertSubString(NewPageResponder.DEFAULT_PAGE_CONTENT, body);
    assertSubString("name=\"responder\"", body);
    assertSubString("name=\"" + EditResponder.TIME_STAMP + "\"", body);
    assertSubString("name=\"" + EditResponder.TICKET_ID + "\"", body);
    assertSubString("type=\"submit\"", body);
    assertNotSubString("<h5> </h5>", body);
  }

  @Test
  public void testRedirectToRefererEffect() throws Exception {
    WikiPageUtil.addPage(root, PathParser.parse("ChildPage"), "child content with <html>");
    request.setResource("ChildPage");
    request.addInput("redirectToReferer", "true");
    request.addInput("redirectAction", "boom");
    request.addHeader("Referer", "http://fitnesse.org:8080/SomePage");

    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    assertEquals(200, response.getStatus());

    String body = response.getContent();
    assertSubString("name=\"redirect\" value=\"http://fitnesse.org:8080/SomePage?boom\"", body);
  }

  @Test
  public void testTemplateListPopulates() throws Exception {
    WikiPageUtil.addPage(root, PathParser.parse("TemplateLibrary"), "template library");

    WikiPageUtil.addPage(root, PathParser.parse("TemplateLibrary.TemplateOne"), "template 1");
    WikiPageUtil.addPage(root, PathParser.parse("TemplateLibrary.TemplateTwo"), "template 2");
    WikiPageUtil.addPage(root, PathParser.parse("ChildPage"), "child content with <html>");

    SimpleResponse response = makeResponse();
    assertEquals(200, response.getStatus());

    String body = response.getContent();
    assertSubString("<html>", body);
    assertSubString("<form", body);
    assertSubString("method=\"post\"", body);
    assertSubString("child content with &lt;html&gt;", body);
    assertSubString("name=\"responder\"", body);
    assertSubString("name=\"" + EditResponder.TIME_STAMP + "\"", body);
    assertSubString("name=\"" + EditResponder.TICKET_ID + "\"", body);
    assertSubString("name=\"" + EditResponder.HELP_TEXT + "\"", body);
    assertSubString("select id=\"" + EditResponder.TEMPLATE_MAP + "\"", body);
    assertSubString("option value=\"" + ".TemplateLibrary.TemplateOne" + "\"", body);
    assertSubString("option value=\"" + ".TemplateLibrary.TemplateTwo" + "\"", body);

    assertSubString("type=\"submit\"", body);
    assertSubString("textarea", body);
  }

  @Test
  public void testTemplateInserterScriptsExists() throws Exception {
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    String body = response.getContent();
    assertMatches("TemplateInserter.js", body);
  }

  @Test
  public void testPasteFromExcelExists() throws Exception {
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    String body = response.getContent();
    assertMatches("SpreadsheetTranslator.js", body);
  }

  @Test
  public void testFormatterScriptsExist() throws Exception {
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    String body = response.getContent();
    assertMatches("WikiFormatter.js", body);
  }

  @Test
  public void testMissingPageDoesNotGetCreated() throws Exception {
    request.setResource("MissingPage");
    responder.makeResponse(context, request);
    assertFalse(root.hasChildPage("MissingPage"));
  }

}
