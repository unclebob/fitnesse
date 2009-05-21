// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.html;

import util.RegexTestCase;
import static util.RegexTestCase.*;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import org.junit.Before;
import org.junit.Test;

public class SetupTeardownIncluderTest {
  private PageData pageData;
  private PageCrawler crawler;
  private WikiPage root;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
    WikiPage page = addPage("TestPage", "the content");
    addPage("SetUp", "setup");
    addPage("TearDown", "teardown");
    addPage("SuiteSetUp", "suiteSetUp");
    addPage("SuiteTearDown", "suiteTearDown");
    pageData = page.getData();
  }

  private WikiPage addPage(String pageName, String content) throws Exception {
    return crawler.addPage(root, PathParser.parse(pageName), content);
  }

  @Test
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

  @Test
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

  @Test
  public void includeScenarioLibraryBrother() throws Exception {
    WikiPage slimTestPage = addPage("SlimTest", "!define TEST_SYSTEM {slim}\n");
    pageData = slimTestPage.getData();
    addPage("ScenarioLibrary", "scenario library");
    SetupTeardownIncluder.includeInto(pageData);
    String html = pageData.getHtml();
    assertSubString("scenario library", html);
  }

  @Test
  public void includeScenarioLibraryUnclesInOrder() throws Exception {
    addPage("TestPage.TestPageChild", "child");
    addPage("TestPage.ScenarioLibrary", "child library");
    WikiPage nephew = addPage("TestPage.TestPageChild.TestPageGrandChild", "!define TEST_SYSTEM {slim}\n");
    addPage("TestPage.TestPageChild.ScenarioLibrary", "grand child library");

    pageData = nephew.getData();
    SetupTeardownIncluder.includeInto(pageData);
    String html = pageData.getHtml();
    assertSubString("child library", html);
    assertSubString("grand child library", html);
    assertSubString("TestPage.ScenarioLibrary", html);
    assertSubString("TestPage.TestPageChild.ScenarioLibrary", html);
    assertSubString("Scenario Libraries", html);
    assertTrue(html.indexOf("grand child library") > html.indexOf("child library"));
  }

  @Test
  public void shouldNotContainScenarioLibrarySectionIfThereAreNone() throws Exception {
    WikiPage slimTestPage = addPage("SlimTest", "!define TEST_SYSTEM {slim}\n");
    pageData = slimTestPage.getData();
    SetupTeardownIncluder.includeInto(pageData);
    String html = pageData.getHtml();
    assertNotSubString("Scenario Libraries", html);
  }



  @Test
  public void shouldNotIncludeSenarioLibrariesIfNotSlimTest() throws Exception {
    addPage("ScenarioLibrary", "scenario library");
    SetupTeardownIncluder.includeInto(pageData);
    String html = pageData.getHtml();
    assertNotSubString("scenario library", html);
  }
}
