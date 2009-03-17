// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import util.RegexTestCase;
import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class FitClientResponderTest extends RegexTestCase {
  private WikiPage root;
  private FitClientResponder responder;
  private MockRequest request;
  private FitNesseContext context;
  private Response response;
  private MockResponseSender sender;
  private static PageCrawler crawler;
  private static WikiPage suite;

  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    responder = new FitClientResponder();
    request = new MockRequest();
    context = new FitNesseContext(root);

    buildSuite(root);
  }

  public static void buildSuite(WikiPage root) throws Exception {
    crawler = root.getPageCrawler();
    suite = crawler.addPage(root, PathParser.parse("SuitePage"), "!path classes\n");
    WikiPage page1 = crawler.addPage(suite, PathParser.parse("TestPassing"), "!|fitnesse.testutil.PassFixture|\n");
    WikiPage page2 = crawler.addPage(suite, PathParser.parse("TestFailing"), "!|fitnesse.testutil.FailFixture|\n");
    crawler.addPage(suite, PathParser.parse("TestError"), "!|fitnesse.testutil.ErrorFixture|\n");
    crawler.addPage(suite, PathParser.parse("TestIgnore"), "!|fitnesse.testutil.IgnoreFixture|\n");
    crawler.addPage(suite, PathParser.parse("SomePage"), "This is just some page.");

    PageData data1 = page1.getData();
    PageData data2 = page2.getData();
    data1.setAttribute(PageData.PropertySUITES, "foo");
    data2.setAttribute(PageData.PropertySUITES, "bar, smoke");
    page1.commit(data1);
    page2.commit(data2);
  }

  public void tearDown() throws Exception {
  }

  public void testPageNotFound() throws Exception {
    String result = getResultFor("MissingPage");
    assertSubString("MissingPage was not found", result);
  }

  public void testOneTest() throws Exception {
    String result = getResultFor("SuitePage.TestPassing");
    assertEquals("0000000000", result.substring(0, 10));
    assertSubString("PassFixture", result);
  }

  public void testSuite() throws Exception {
    String result = getResultFor("SuitePage");
    assertEquals("0000000000", result.substring(0, 10));
    assertSubString("PassFixture", result);
    assertSubString("FailFixture", result);
    assertSubString("ErrorFixture", result);
    assertSubString("IgnoreFixture", result);
    assertNotSubString("some page", result);
  }

  public void testRelativePageNamesIncluded() throws Exception {
    String result = getResultFor("SuitePage");
    assertNotSubString("SuitePage", result);
    assertSubString("TestPassing", result);
    assertSubString("TestFailing", result);
    assertSubString("TestError", result);
    assertSubString("TestIgnore", result);
  }

  public void testPageThatIsNoATest() throws Exception {
    String result = getResultFor("SuitePage.SomePage");
    assertSubString("SomePage is neither a Test page nor a Suite page.", result);
  }

  private String getResultFor(String name) throws Exception {
    return getResultFor(name, false);
  }

  private String getResultFor(String name, boolean addPaths) throws Exception {
    request.setResource(name);
    if (addPaths)
      request.addInput("includePaths", "blah");
    response = responder.makeResponse(context, request);
    sender = new MockResponseSender();
    sender.doSending(response);
    String result = sender.sentData();
    return result;
  }

  public void testWithClasspathOnSuite() throws Exception {
    String result = getResultFor("SuitePage", true);
    assertTrue(result.startsWith("00000000000000000007classes"));
  }

  public void testWithClasspathOnTestInSuite() throws Exception {
    crawler.addPage(suite, PathParser.parse("TestPage"), "!path jar.jar\n!path /some/dir/with/.class/files\n!|fitnesse.testutil.IgnoreFixture|\n");
    String result = getResultFor("SuitePage.TestPage", true);

    assertSubString("classes", result);
    assertSubString("jar.jar", result);
    assertSubString("/some/dir/with/.class/files", result);
  }
}
