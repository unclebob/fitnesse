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
import fitnesse.wiki.*;
import util.RegexTestCase;

public class WikiPageResponderTest extends RegexTestCase {
  private WikiPage root;
  private PageCrawler crawler;
  private FitNesseContext context;

  @Override
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
    crawler = root.getPageCrawler();
    context = FitNesseUtil.makeTestContext(root);
  }

  public void testResponse() throws Exception {
    WikiPage page = crawler.addPage(root, PathParser.parse("ChildPage"), "child content");
    PageData data = page.getData();
    WikiPageProperties properties = data.getProperties();
    properties.set(PageData.PropertySUITES, "Wiki Page tags");
    page.commit(data);

    final MockRequest request = new MockRequest();
    request.setResource("ChildPage");

    final Responder responder = new WikiPageResponder();
    final SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);

    assertEquals(200, response.getStatus());

    final String body = response.getContent();

    assertSubString("<html>", body);
    assertSubString("<body", body);
    assertSubString("child content", body);
    assertSubString("href=\"ChildPage?whereUsed\"", body);
    assertSubString("Cache-Control: max-age=0", response.makeHttpHeaders());
    assertSubString("<h5> Wiki Page tags</h5>", body);
  }

  public void testAttributeButtons() throws Exception {
    crawler.addPage(root, PathParser.parse("NormalPage"));
    final WikiPage noButtonsPage = crawler.addPage(root, PathParser.parse("NoButtonPage"));
    for (final String attribute : PageData.NON_SECURITY_ATTRIBUTES) {
      final PageData data = noButtonsPage.getData();
      data.removeAttribute(attribute);
      noButtonsPage.commit(data);
    }

    SimpleResponse response = requestPage("NormalPage");
    assertSubString(">Edit</a>", response.getContent());
    assertSubString(">Search</a>", response.getContent());
    assertSubString(">Versions</a>", response.getContent());
    assertNotSubString(">Suite</a>", response.getContent());
    assertNotSubString(">Test</a>", response.getContent());

    response = requestPage("NoButtonPage");
    assertNotSubString(">Edit</a>", response.getContent());
    assertNotSubString(">Search</a>", response.getContent());
    assertNotSubString(">Versions</a>", response.getContent());
    assertNotSubString(">Suite</a>", response.getContent());
    assertNotSubString(">Test</a>", response.getContent());
  }

  public void testHeadersAndFooters() throws Exception {
    crawler.addPage(root, PathParser.parse("NormalPage"), "normal");
    crawler.addPage(root, PathParser.parse("TestPage"), "test page");
    crawler.addPage(root, PathParser.parse("PageHeader"), "header");
    crawler.addPage(root, PathParser.parse("PageFooter"), "footer");
    crawler.addPage(root, PathParser.parse("SetUp"), "setup");
    crawler.addPage(root, PathParser.parse("TearDown"), "teardown");
    crawler.addPage(root, PathParser.parse("SuiteSetUp"), "suite setup");
    crawler.addPage(root, PathParser.parse("SuiteTearDown"), "suite teardown");

    SimpleResponse response = requestPage("NormalPage");
    String content = response.getContent();
    assertHasRegexp("header", content);
    assertHasRegexp("normal", content);
    assertHasRegexp("footer", content);
    assertDoesntHaveRegexp("setup", content);
    assertDoesntHaveRegexp("teardown", content);
    assertDoesntHaveRegexp("suite setup", content);
    assertDoesntHaveRegexp("suite teardown", content);

    response = requestPage("TestPage");
    content = response.getContent();
    assertHasRegexp("header", content);
    assertHasRegexp("test page", content);
    assertHasRegexp("footer", content);
    assertHasRegexp("setup", content);
    assertHasRegexp("teardown", content);
    assertHasRegexp("suite setup", content);
    assertHasRegexp("suite teardown", content);
  }

  private SimpleResponse requestPage(String name) throws Exception {
    final MockRequest request = new MockRequest();
    request.setResource(name);
    final Responder responder = new WikiPageResponder();
    return (SimpleResponse) responder.makeResponse(context, request);
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
