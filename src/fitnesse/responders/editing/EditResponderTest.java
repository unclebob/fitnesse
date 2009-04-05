// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.editing;

import util.RegexTestCase;
import fitnesse.FitNesseContext;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class EditResponderTest extends RegexTestCase {
  private WikiPage root;
  private MockRequest request;
  private EditResponder responder;
  private PageCrawler crawler;

  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
    crawler = root.getPageCrawler();
    request = new MockRequest();
    responder = new EditResponder();
  }

  public void testResponse() throws Exception {
    crawler.addPage(root, PathParser.parse("ChildPage"), "child content with <html>");
    request.setResource("ChildPage");

    SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
    assertEquals(200, response.getStatus());

    String body = response.getContent();
    assertSubString("<html>", body);
    assertSubString("<form", body);
    assertSubString("method=\"post\"", body);
    assertSubString("child content with &lt;html&gt;", body);
    assertSubString("name=\"responder\"", body);
    assertSubString("name=\"" + EditResponder.TIME_STAMP + "\"", body);
    assertSubString("name=\"" + EditResponder.TICKET_ID + "\"", body);
    assertSubString("type=\"submit\"", body);
  }

  public void testResponseWhenNonexistentPageRequestsed() throws Exception {
    request.setResource("NonExistentPage");
    request.addInput("nonExistent", true);

    FitNesseContext context = new FitNesseContext(root);
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
  }

  public void testRedirectToRefererEffect() throws Exception {
    crawler.addPage(root, PathParser.parse("ChildPage"), "child content with <html>");
    request.setResource("ChildPage");
    request.addInput("redirectToReferer", true);
    request.addInput("redirectAction", "boom");
    request.addHeader("Referer", "http://fitnesse.org:8080/SomePage");

    SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
    assertEquals(200, response.getStatus());

    String body = response.getContent();
    HtmlTag redirectInputTag = HtmlUtil.makeInputTag("hidden", "redirect", "http://fitnesse.org:8080/SomePage?boom");
    assertSubString(redirectInputTag.html(), body);
  }

  public void testPasteFromExcelExists() throws Exception {
    SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
    String body = response.getContent();
    assertMatches("SpreadsheetTranslator.js", body);
    assertMatches("spreadsheetSupport.js", body);
  }

  public void testFormatterScriptsExist() throws Exception {
    SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
    String body = response.getContent();
    assertMatches("WikiFormatter.js", body);
    assertMatches("wikiFormatterSupport.js", body);
  }

  public void testWrapScriptExists() throws Exception {
    SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
    String body = response.getContent();
    assertMatches("textareaWrapSupport.js", body);
  }

  public void testMissingPageDoesNotGetCreated() throws Exception {
    request.setResource("MissingPage");
    responder.makeResponse(new FitNesseContext(root), request);
    assertFalse(root.hasChildPage("MissingPage"));
  }
}
