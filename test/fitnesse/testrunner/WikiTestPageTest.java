// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testrunner;

import fitnesse.testsystems.TestPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.fs.InMemoryPage;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static util.RegexTestCase.assertNotSubString;
import static util.RegexTestCase.assertSubString;

public class WikiTestPageTest {
  private WikiPage root;
  private WikiPage wikiPage;
  private Properties properties;

  @Before
  public void setUp() throws Exception {
    properties = new Properties();
    root = InMemoryPage.makeRoot("RooT", properties);
    wikiPage = addPage("TestPage", "!define TEST_SYSTEM {slim}\n"+"the content");
    addPage("SetUp", "setup");
    addPage("TearDown", "teardown");
    addPage("SuiteSetUp", "suiteSetUp");
    addPage("SuiteTearDown", "suiteTearDown");

    WikiPage subPage = WikiPageUtil.addPage(wikiPage, PathParser.parse("SubPage"), "sub page");
    WikiPageUtil.addPage(wikiPage, PathParser.parse("ScenarioLibrary"), "scenario library 2");

    WikiPageUtil.addPage(subPage, PathParser.parse("TestSubPage"), "sub test page");
    WikiPageUtil.addPage(subPage, PathParser.parse("ScenarioLibrary"), "scenario library 3");
  }

  private WikiPage addPage(String pageName, String content) throws Exception {
    return WikiPageUtil.addPage(root, PathParser.parse(pageName), content);
  }

  @Test
  public void testIncludeSetupTearDownOutsideOfSuite()
    throws Exception {
    TestPage testPage = new WikiTestPage(wikiPage);
    String html = testPage.getHtml();
    assertSubString(".SetUp", html);
    assertSubString("setup", html);
    assertSubString(".TearDown", html);
    assertSubString("teardown", html);
    assertSubString("the content", html);
    assertSubString("class=\"collapsible closed\"", html);
    assertNotSubString(".SuiteSetUp", html);
    assertNotSubString("suiteSetUp", html);
    assertNotSubString(".SuiteTearDown", html);
    assertNotSubString("suitTearDown", html);
  }

  @Test
  public void testIncludeSetupTearDownInsideOfSuite() {
    TestPage test = new TestPageWithSuiteSetUpAndTearDown(wikiPage);
    String html = test.getHtml();
    assertSubString(".SetUp", html);
    assertSubString("setup", html);
    assertSubString(".TearDown", html);
    assertSubString("teardown", html);
    assertSubString("the content", html);
    assertSubString("class=\"collapsible closed\"", html);
    assertSubString(".SuiteSetUp", html);
    assertSubString("suiteSetUp", html);
    assertSubString(".SuiteTearDown", html);
    assertSubString("suiteTearDown", html);
    assertEquals("SetUp occurs more than once", html.indexOf(" SetUp"), html.lastIndexOf(" SetUp"));
    assertEquals("TearDown occurs more than once", html.indexOf(" TearDown"), html.lastIndexOf(" TearDown"));
  }

  @Test
  public void testIncludeOneSetupTearDownInsideOfSuiteWhenIncludingAll() {
    properties.setProperty("ALL_UNCLE_SUITE_SETUPS", "true");
    testIncludeSetupTearDownInsideOfSuite();
  }

  @Test
  public void includeScenarioLibraryBrother() throws Exception {
    WikiPage slimTestPage = addPage("SlimTest", "!define TEST_SYSTEM {slim}\n");
    TestPage testPage = new WikiTestPage(slimTestPage);
    addPage("ScenarioLibrary", "scenario library");
    String html = testPage.getHtml();
    assertSubString("scenario library", html);
  }

  @Test
  public void includeScenarioLibraryUnclesInOrder() throws Exception {
    addPage("TestPage.TestPageChild", "child");
    addPage("TestPage.ScenarioLibrary", "child library");
    WikiPage nephew = addPage("TestPage.TestPageChild.TestPageGrandChild", "!define TEST_SYSTEM {slim}\n");
    addPage("TestPage.TestPageChild.ScenarioLibrary", "grand child library");

    TestPage testPage = new WikiTestPage(nephew);
    String html = testPage.getHtml();
    assertSubString("child library", html);
    assertSubString("grand child library", html);
    assertSubString("TestPage.ScenarioLibrary", html);
    assertSubString("TestPage.TestPageChild.ScenarioLibrary", html);
    assertSubString("Scenario Libraries", html);
    assertTrue(html.indexOf("grand child library") > html.indexOf("child library"));
  }

  @Test
  public void includesSetupTearDownUnclesInOrder() throws Exception {
    addPage("TestPage.SuiteSetUp", "child set up");
    addPage("TestPage.SuiteTearDown", "child tear down");
    WikiPage nephew = addPage("TestPage.TestPageChild.TestPageGrandChild", "!define TEST_SYSTEM {slim}\n");
    addPage("TestPage.TestPageChild.SuiteSetUp", "grand child set up");
    addPage("TestPage.TestPageChild.SuiteTearDown", "grand child tear down");

    properties.setProperty("ALL_UNCLE_SUITE_SETUPS", "true");

    TestPage test = new TestPageWithSuiteSetUpAndTearDown(wikiPage);
    String html = test.getHtml();
    assertSubString("Suite Set Ups", html);
    assertSubString("Suite Tear Downs", html);
    assertSubString(".SuiteSetUp", html);
    assertSubString(".TestPage.SuiteSetUp", html);
    assertSubString(".SuiteTearDown", html);
    assertSubString(".TestPage.SuiteTearDown", html);
    assertTrue("Uncle SuiteSetUp before brother", html.indexOf(">.SuiteSetUp<") < html.indexOf(">.TestPage.SuiteSetUp<"));
    assertTrue("Uncle SuiteTearDown after brother", html.indexOf(">.SuiteTearDown<") > html.indexOf(">.TestPage.SuiteTearDown<"));
    assertNotSubString(".TestPage.TestPageChild.SuiteSetUp", html);
    assertNotSubString(".TestPage.TestPageChild.SuiteTearDown", html);
  }

  @Test
  public void shouldNotContainScenarioLibrarySectionIfThereAreNone() throws Exception {
    WikiPage slimTestPage = addPage("SlimTest", "!define TEST_SYSTEM {slim}\n");
    TestPage testPage = new WikiTestPage(slimTestPage);
    String html = testPage.getHtml();
    assertNotSubString("Scenario Libraries", html);
  }

  @Test
  public void shouldNotContainSuiteSetUpTearDownSectionIfThereAreNone() throws Exception {
    properties.setProperty("ALL_UNCLE_SUITE_SETUPS", "true");
    WikiPage slimTestPage = addPage("SlimTest", "!define TEST_SYSTEM {slim}\n");
    TestPage testPage = new WikiTestPage(slimTestPage);
    String html = testPage.getHtml();
    assertNotSubString("Suite Set Ups", html);
    assertNotSubString("Suite Tear Downs", html);
    assertNotSubString(".SuiteSetUp", html);
    assertNotSubString(".SuiteTearDown", html);
  }

  @Test
  public void shouldReturnDecoratedContentForWikitextPages() throws Exception {
    WikiPage slimTestPage = addPage("SlimTest", "!define TEST_SYSTEM {slim}\n");
    TestPage testPage = new WikiTestPage(slimTestPage);
    String content = testPage.getContent();
    assertSubString("!define TEST_SYSTEM {slim}\n", content);
  }


  @Test
  public void shouldNotIncludeScenarioLibrariesIfNotSlimTest() throws Exception {
    addPage("ScenarioLibrary", "scenario library");
    WikiPage someTest = addPage("SomeTest", "some test");
    TestPage testPage = new WikiTestPage(someTest);
    String html = testPage.getHtml();
    assertNotSubString("scenario library", html);
  }

  @Test
  public void shouldIncludeScenarioLibrariesIfIncludeVariableSetAndNotSlimTest() throws Exception {
    WikiPage slimTestPage = addPage("SlimTest", "!define INCLUDE_SCENARIO_LIBRARIES {true}\n");
    TestPage testPage = new WikiTestPage(slimTestPage);
    addPage("ScenarioLibrary", "scenario library");
    String html = testPage.getHtml();
    assertSubString("scenario library", html);
  }

  @Test
  public void shouldNotIncludeScenarioLibrariesIfSlimTestAndIncludeVariableSetToFalse() throws Exception {
    WikiPage slimTestPage = addPage("SlimTest", "!define TEST_SYSTEM {slim}\n!define INCLUDE_SCENARIO_LIBRARIES {false}\n");
    TestPage testPage = new WikiTestPage(slimTestPage);
    addPage("ScenarioLibrary", "scenario library");
    String html = testPage.getHtml();
    assertNotSubString("scenario library", html);
  }


  @Test
  public void testPathSeparatorVariable() throws Exception {
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("TestPage"),
            "!define PATH_SEPARATOR {|}\n" +
                    "!path fitnesse.jar\n" +
                    "!path my.jar");
    PageData data = page.getData();
    page.commit(data);

    String expected = "fitnesse.jar" + "|" + "my.jar";
    assertEquals(expected, new WikiTestPage(page).getClassPath().toString());
  }

}
