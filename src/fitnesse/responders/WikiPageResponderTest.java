// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.VirtualCouplingExtensionTest;
import fitnesse.wiki.WikiPage;
import util.RegexTestCase;

public class WikiPageResponderTest extends RegexTestCase {
  private WikiPage root;
  private PageCrawler crawler;

  @Override
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
    crawler = root.getPageCrawler();
  }

  public void testResponse() throws Exception {
    crawler.addPage(root, PathParser.parse("ChildPage"), "child content");
    final MockRequest request = new MockRequest();
    request.setResource("ChildPage");

    final Responder responder = new WikiPageResponder();
    final SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);

    assertEquals(200, response.getStatus());

    final String body = response.getContent();

    assertSubString("<html>", body);
    assertSubString("<body", body);
    assertSubString("child content", body);
    assertSubString("href=\"ChildPage?whereUsed\"", body);
    assertSubString("ChildPage</span>", body);
    assertSubString("Cache-Control: max-age=0", response.makeHttpHeaders());
    assertSubString("<div id=\"addChildPopup\" class=\"popup_window\"", body);
  }

  public void testAttributeButtons() throws Exception {
    crawler.addPage(root, PathParser.parse("NormalPage"));
    final WikiPage noButtonsPage = crawler.addPage(root, PathParser.parse("NoButtonPage"));
    for (final String attribute : WikiPage.NON_SECURITY_ATTRIBUTES) {
      final PageData data = noButtonsPage.getData();
      data.removeAttribute(attribute);
      noButtonsPage.commit(data);
    }

    SimpleResponse response = requestPage("NormalPage");
    assertSubString("<!--Edit button-->", response.getContent());
    assertSubString("<!--Search button-->", response.getContent());
    assertSubString("<!--Versions button-->", response.getContent());
    assertNotSubString("<!--Suite button-->", response.getContent());
    assertNotSubString("<!--Test button-->", response.getContent());

    response = requestPage("NoButtonPage");
    assertNotSubString("<!--Edit button-->", response.getContent());
    assertNotSubString("<!--Search button-->", response.getContent());
    assertNotSubString("<!--Versions button-->", response.getContent());
    assertNotSubString("<!--Suite button-->", response.getContent());
    assertNotSubString("<!--Test button-->", response.getContent());
  }

  public void testHeadersAndFooters() throws Exception {
    crawler.addPage(root, PathParser.parse("NormalPage"), "normal");
    crawler.addPage(root, PathParser.parse("TestPage"), "test page");
    crawler.addPage(root, PathParser.parse("PageHeader"), "header");
    crawler.addPage(root, PathParser.parse("PageFooter"), "footer");
    crawler.addPage(root, PathParser.parse("SetUp"), "setup");
    crawler.addPage(root, PathParser.parse("TearDown"), "teardown");

    SimpleResponse response = requestPage("NormalPage");
    String content = response.getContent();
    assertHasRegexp("header", content);
    assertHasRegexp("normal", content);
    assertHasRegexp("footer", content);
    assertDoesntHaveRegexp("setup", content);
    assertDoesntHaveRegexp("teardown", content);

    response = requestPage("TestPage");
    content = response.getContent();
    assertHasRegexp("header", content);
    assertHasRegexp("test page", content);
    assertHasRegexp("footer", content);
    assertHasRegexp("setup", content);
    assertHasRegexp("teardown", content);
  }

  private SimpleResponse requestPage(String name) throws Exception {
    final MockRequest request = new MockRequest();
    request.setResource(name);
    final Responder responder = new WikiPageResponder();
    return (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
  }

  public void testShouldGetVirtualPage() throws Exception {
    final WikiPage pageOne = crawler.addPage(root, PathParser.parse("TargetPage"), "some content");
    crawler.addPage(pageOne, PathParser.parse("ChildPage"), "child content");
    final WikiPage linkerPage = crawler.addPage(root, PathParser.parse("LinkerPage"), "linker content");
    FitNesseUtil.bindVirtualLinkToPage(linkerPage, pageOne);
    final SimpleResponse response = requestPage("LinkerPage.ChildPage");

    assertSubString("child content", response.getContent());
  }

  public void testVirtualPageIndication() throws Exception {
    final WikiPage targetPage = crawler.addPage(root, PathParser.parse("TargetPage"));
    crawler.addPage(targetPage, PathParser.parse("ChildPage"));
    final WikiPage linkPage = crawler.addPage(root, PathParser.parse("LinkPage"));
    VirtualCouplingExtensionTest.setVirtualWiki(linkPage, "http://localhost:" + FitNesseUtil.port + "/TargetPage");

    FitNesseUtil.startFitnesse(root);
    SimpleResponse response = null;
    try {
      response = requestPage("LinkPage.ChildPage");
    } finally {
      FitNesseUtil.stopFitnesse();
    }

    assertSubString("<body class=\"virtual\">", response.getContent());
  }

  public void testImportedPageIndication() throws Exception {
    final WikiPage page = crawler.addPage(root, PathParser.parse("SamplePage"));
    final PageData data = page.getData();
    final WikiImportProperty importProperty = new WikiImportProperty("blah");
    importProperty.addTo(data.getProperties());
    page.commit(data);

    final String content = requestPage("SamplePage").getContent();

    assertSubString("<body class=\"imported\">", content);
  }
  
  public void testImportedPageIndicationNotOnRoot() throws Exception {
    final WikiPage page = crawler.addPage(root, PathParser.parse("SamplePage"));
    final PageData data = page.getData();
    final WikiImportProperty importProperty = new WikiImportProperty("blah");
    importProperty.setRoot(true);
    importProperty.addTo(data.getProperties());
    page.commit(data);

    final String content = requestPage("SamplePage").getContent();

    assertNotSubString("<body class=\"imported\">", content);
  }

  public void testResponderIsSecureReadOperation() throws Exception {
    final Responder responder = new WikiPageResponder();
    assertTrue(responder instanceof SecureResponder);
    final SecureOperation operation = ((SecureResponder) responder).getSecureOperation();
    assertEquals(SecureReadOperation.class, operation.getClass());
  }
}
