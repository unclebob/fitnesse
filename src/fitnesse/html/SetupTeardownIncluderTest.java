// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.html;

import util.RegexTestCase;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class SetupTeardownIncluderTest extends RegexTestCase {
  private PageData pageData;

  protected void setUp() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    PageCrawler crawler = root.getPageCrawler();
    WikiPage page = crawler.addPage(root,
        PathParser.parse("TestPage"),
        "the content"
    );
    crawler.addPage(root, PathParser.parse("SetUp"), "setup");
    crawler.addPage(root, PathParser.parse("TearDown"), "teardown");
    crawler.addPage(root, PathParser.parse("SuiteSetUp"), "suiteSetUp");
    crawler.addPage(root, PathParser.parse("SuiteTearDown"), "suiteTearDown");
    pageData = page.getData();
  }

  public void testIncludeSetupTearDownOutsideOfSuite()
  throws Exception {
    SetupTeardownIncluder.includeInto(pageData);
    String html = pageData.getHtml();
    assertSubString(".SetUp", html);
    assertSubString("setup", html);
    assertSubString(".TearDown", html);
    assertSubString("teardown", html);
    assertSubString("the content", html);
    assertSubString("class=\"hidden\"", html);
    assertNotSubString(".SuiteSetUp", html);
    assertNotSubString("suiteSetUp", html);
    assertNotSubString(".SuiteTearDown", html);
    assertNotSubString("suitTearDown", html);
  }

  public void testIncludeSetupTearDownInsideOfSuite() throws Exception {
    SetupTeardownIncluder.includeInto(pageData, true);
    String html = pageData.getHtml();
    assertSubString(".SetUp", html);
    assertSubString("setup", html);
    assertSubString(".TearDown", html);
    assertSubString("teardown", html);
    assertSubString("the content", html);
    assertSubString("class=\"hidden\"", html);
    assertSubString(".SuiteSetUp", html);
    assertSubString("suiteSetUp", html);
    assertSubString(".SuiteTearDown", html);
    assertSubString("suiteTearDown", html);
  }
}
