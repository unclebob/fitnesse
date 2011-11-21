// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.html;

import fitnesse.responders.run.TestPage;
import fitnesse.wiki.*;
import org.junit.Before;
import org.junit.Test;

import static util.RegexTestCase.*;

public class SetupTeardownAndLibraryIncluderTest {
  private PageData pageData;
  private PageCrawler crawler;
  private WikiPage root;
  private WikiPage testPage;
  private WikiPage subPage;
  private WikiPage subTestPage;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
    testPage = addPage("TestPage", "!define TEST_SYSTEM {slim}\n"+"the content");
    addPage("SetUp", "setup");
    addPage("TearDown", "teardown");
    addPage("SuiteSetUp", "suiteSetUp");
    addPage("SuiteTearDown", "suiteTearDown");

    subPage = crawler.addPage(testPage, PathParser.parse("SubPage"), "sub page");
    crawler.addPage(testPage, PathParser.parse("ScenarioLibrary"), "scenario library 2");

    subTestPage = crawler.addPage(subPage, PathParser.parse("TestSubPage"), "sub test page");
    crawler.addPage(subPage, PathParser.parse("ScenarioLibrary"), "scenario library 3");
    pageData = testPage.getData();
  }

  private WikiPage addPage(String pageName, String content) throws Exception {
    return crawler.addPage(root, PathParser.parse(pageName), content);
  }

  @Test
  public void testIncludeSetupTearDownOutsideOfSuite()
    throws Exception {
    SetupTeardownAndLibraryIncluder.includeInto(pageData);
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
  public void includeSetupsAndTeardownsAndLibrariesBelowASuite()
    throws Exception {
    WikiPage suitePage = testPage;
    TestPage testPage = new TestPage(subTestPage);
    SetupTeardownAndLibraryIncluder.includeSetupsTeardownsAndLibrariesBelowTheSuite(testPage, suitePage);
    String html = testPage.getDecoratedData().getHtml();
    assertSubString(".SetUp", html);
    assertSubString("setup", html);
    assertSubString(".TearDown", html);
    assertSubString("teardown", html);
    assertSubString("sub test page", html);
    assertSubString("class=\"hidden\"", html);
    assertSubString("scenario library 3", html);
    assertNotSubString(".SuiteSetUp", html);
    assertNotSubString("suiteSetUp", html);
    assertNotSubString(".SuiteTearDown", html);
    assertNotSubString("suitTearDown", html);
    assertNotSubString("scenario library 1", html);
    assertNotSubString("scenario library 2", html);
  }

  @Test
  public void testIncludeSetupTearDownInsideOfSuite() throws Exception {
    SetupTeardownAndLibraryIncluder.includeInto(pageData, true);
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
    assertEquals("SetUp occurs more than once", html.indexOf(" SetUp"), html.lastIndexOf(" SetUp"));
    assertEquals("TearDown occurs more than once", html.indexOf(" TearDown"), html.lastIndexOf(" TearDown"));
  }
  
  @Test
  public void includeScenarioLibraryBrother() throws Exception {
    WikiPage slimTestPage = addPage("SlimTest", "!define TEST_SYSTEM {slim}\n");
    pageData = slimTestPage.getData();
    addPage("ScenarioLibrary", "scenario library");
    SetupTeardownAndLibraryIncluder.includeInto(pageData);
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
    SetupTeardownAndLibraryIncluder.includeInto(pageData);
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
    SetupTeardownAndLibraryIncluder.includeInto(pageData);
    String html = pageData.getHtml();
    assertNotSubString("Scenario Libraries", html);
  }


  @Test
  public void shouldNotIncludeSenarioLibrariesIfNotSlimTest() throws Exception {
    addPage("ScenarioLibrary", "scenario library");
    WikiPage someTest = addPage("SomeTest", "some test");
    PageData somePageData = someTest.getData();
    SetupTeardownAndLibraryIncluder.includeInto(somePageData);
    String html = somePageData.getHtml();
    assertNotSubString("scenario library", html);
  }
}
