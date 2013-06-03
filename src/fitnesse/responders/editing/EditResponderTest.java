// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.editing;

import util.RegexTestCase;
import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.mem.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperties;

public class EditResponderTest extends RegexTestCase {
  private WikiPage root;
  private MockRequest request;
  private EditResponder responder;
  private PageCrawler crawler;

  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
    FitNesseContext context = FitNesseUtil.makeTestContext(root);
    crawler = root.getPageCrawler();
    request = new MockRequest();
    responder = new EditResponder();
  }

  public void testResponse() throws Exception {
    WikiPage page=crawler.addPage(root, PathParser.parse("ChildPage"), "child content with <html>");
    PageData data = page.getData();
    WikiPageProperties properties = data.getProperties();
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
    assertSubString(String.format("textarea", EditResponder.CONTENT_INPUT_NAME), body);
    assertSubString("<h5> Edit Page tags</h5>", body);
  }

  private SimpleResponse makeResponse() {
    request.setResource("ChildPage");
    return (SimpleResponse) responder.makeResponse(FitNesseUtil.makeTestContext(root), request);
  }

  public void testResponseWhenNonexistentPageRequestsed() throws Exception {
    request.setResource("NonExistentPage");
    request.addInput("nonExistent", true);

    FitNesseContext context = FitNesseUtil.makeTestContext(root);
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    assertEquals(200, response.getStatus());

    String body = response.getContent();
    assertSubString("<html>", body);
    assertSubString("<form", body);
    assertSubString("method=\"post\"", body);
    assertSubString(context.defaultNewPageContent, body);
    assertSubString("name=\"responder\"", body);
    assertSubString("name=\"" + EditResponder.TIME_STAMP + "\"", body);
    assertSubString("name=\"" + EditResponder.TICKET_ID + "\"", body);
    assertSubString("type=\"submit\"", body);
    assertNotSubString("<h5> </h5>", body);
  }

  public void testRedirectToRefererEffect() throws Exception {
    crawler.addPage(root, PathParser.parse("ChildPage"), "child content with <html>");
    request.setResource("ChildPage");
    request.addInput("redirectToReferer", true);
    request.addInput("redirectAction", "boom");
    request.addHeader("Referer", "http://fitnesse.org:8080/SomePage");

    SimpleResponse response = (SimpleResponse) responder.makeResponse(FitNesseUtil.makeTestContext(root), request);
    assertEquals(200, response.getStatus());

    String body = response.getContent();
    assertSubString("name=\"redirect\" value=\"http://fitnesse.org:8080/SomePage?boom\"", body);
  }
  
  public void testTemplateListPopulates() throws Exception {
    crawler.addPage(root, PathParser.parse("TemplateLibrary"), "template library");
    
    crawler.addPage(root, PathParser.parse("TemplateLibrary.TemplateOne"), "template 1");
    crawler.addPage(root, PathParser.parse("TemplateLibrary.TemplateTwo"), "template 2");
    crawler.addPage(root, PathParser.parse("ChildPage"), "child content with <html>");

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
    assertSubString(String.format("textarea", EditResponder.CONTENT_INPUT_NAME), body);
  }
  
  public void testTemplateInserterScriptsExists() throws Exception {
    SimpleResponse response = (SimpleResponse) responder.makeResponse(FitNesseUtil.makeTestContext(root), request);
    String body = response.getContent();
    assertMatches("TemplateInserter.js", body);
  }

  public void testPasteFromExcelExists() throws Exception {
    SimpleResponse response = (SimpleResponse) responder.makeResponse(FitNesseUtil.makeTestContext(root), request);
    String body = response.getContent();
    assertMatches("SpreadsheetTranslator.js", body);
  }

  public void testFormatterScriptsExist() throws Exception {
    SimpleResponse response = (SimpleResponse) responder.makeResponse(FitNesseUtil.makeTestContext(root), request);
    String body = response.getContent();
    assertMatches("WikiFormatter.js", body);
  }

  public void testMissingPageDoesNotGetCreated() throws Exception {
    request.setResource("MissingPage");
    responder.makeResponse(FitNesseUtil.makeTestContext(root), request);
    assertFalse(root.hasChildPage("MissingPage"));
  }
  
}
