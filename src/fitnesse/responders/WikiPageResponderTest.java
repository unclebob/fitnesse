// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static util.RegexTestCase.assertDoesntHaveRegexp;
import static util.RegexTestCase.assertHasRegexp;
import static util.RegexTestCase.assertNotSubString;
import static util.RegexTestCase.assertSubString;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiImportProperty;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperties;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.mem.InMemoryPage;
import org.junit.Before;
import org.junit.Test;

public class WikiPageResponderTest {
  private WikiPage root;
  private FitNesseContext context;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
    context = FitNesseUtil.makeTestContext(root);
  }

  @Test
  public void testResponse() throws Exception {
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("ChildPage"), "child content");
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

  @Test
  public void testAttributeButtons() throws Exception {
    WikiPageUtil.addPage(root, PathParser.parse("NormalPage"));
    final WikiPage noButtonsPage = WikiPageUtil.addPage(root, PathParser.parse("NoButtonPage"));
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

  @Test
  public void testHeadersAndFooters() throws Exception {
    WikiPageUtil.addPage(root, PathParser.parse("NormalPage"), "normal");
    WikiPageUtil.addPage(root, PathParser.parse("TestPage"), "test page");
    WikiPageUtil.addPage(root, PathParser.parse("PageHeader"), "header");
    WikiPageUtil.addPage(root, PathParser.parse("PageFooter"), "footer");
    WikiPageUtil.addPage(root, PathParser.parse("SetUp"), "setup");
    WikiPageUtil.addPage(root, PathParser.parse("TearDown"), "teardown");
    WikiPageUtil.addPage(root, PathParser.parse("SuiteSetUp"), "suite setup");
    WikiPageUtil.addPage(root, PathParser.parse("SuiteTearDown"), "suite teardown");

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

  @Test
  public void testImportedPageIndication() throws Exception {
    final WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("SamplePage"));
    final PageData data = page.getData();
    final WikiImportProperty importProperty = new WikiImportProperty("blah");
    importProperty.addTo(data.getProperties());
    page.commit(data);

    final String content = requestPage("SamplePage").getContent();

    assertSubString("<body class=\"imported\">", content);
  }

  @Test
  public void testImportedPageIndicationNotOnRoot() throws Exception {
    final WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("SamplePage"));
    final PageData data = page.getData();
    final WikiImportProperty importProperty = new WikiImportProperty("blah");
    importProperty.setRoot(true);
    importProperty.addTo(data.getProperties());
    page.commit(data);

    final String content = requestPage("SamplePage").getContent();

    assertNotSubString("<body class=\"imported\">", content);
  }

  @Test
  public void testResponderIsSecureReadOperation() throws Exception {
    final Responder responder = new WikiPageResponder();
    assertTrue(responder instanceof SecureResponder);
    final SecureOperation operation = ((SecureResponder) responder).getSecureOperation();
    assertEquals(SecureReadOperation.class, operation.getClass());
  }
}
