// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import static util.RegexTestCase.*;
import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class MultipleTestsRunnerTest {
  private WikiPage root;
  private WikiPage suite;
  private WikiPage testPage;
  private PageCrawler crawler;
  private String suitePageName;
  private final String simpleSlimDecisionTable = "!define TEST_SYSTEM {slim}\n" +
    "|!-DT:fitnesse.slim.test.TestSlim-!|\n" +
    "|string|get string arg?|\n" +
    "|wow|wow|\n";
  private List<WikiPage> testPages;
  
  @Before
  public void setUp() throws Exception {
    suitePageName = "SuitePage";
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
    PageData data = root.getData();
    data.setContent(classpathWidgets());
    root.commit(data);
    suite = crawler.addPage(root, PathParser.parse(suitePageName), "This is the test suite\n");
    testPages = new LinkedList<WikiPage>();
    testPage = addTestPage(suite, "TestOne", "My test");
 }
  
  @Test
  public void testBuildClassPath() throws Exception {
    MultipleTestsRunner runner = new MultipleTestsRunner(testPages, null, suite, null);
    
    String classpath = runner.buildClassPath();
    assertSubString("classes", classpath);
    assertSubString("dummy.jar", classpath);
  }
  
  @Test
  public void testGenerateSuiteMapWithMultipleTestSystems() throws Exception {
    WikiPage slimPage = addTestPage(suite, "SlimTest", simpleSlimDecisionTable);
    
    MultipleTestsRunner runner = new MultipleTestsRunner(testPages, null, suite, null);
    Map<TestSystem.Descriptor, LinkedList<WikiPage>> map = runner.makeMapOfPagesByTestSystem();

    TestSystem.Descriptor fitDescriptor = TestSystem.getDescriptor(testPage.getData());
    TestSystem.Descriptor slimDescriptor = TestSystem.getDescriptor(slimPage.getData());
    List<WikiPage> fitList = map.get(fitDescriptor);
    List<WikiPage> slimList = map.get(slimDescriptor);

    assertEquals(1, fitList.size());
    assertEquals(1, slimList.size());
    assertEquals(testPage, fitList.get(0));
    assertEquals(slimPage, slimList.get(0));
  }
  
  @Test
  public void testPagesForTestSystemAreSurroundedBySuiteSetupAndTeardown() throws Exception {
    WikiPage slimPage = addTestPage(suite, "AaSlimTest", simpleSlimDecisionTable);
    WikiPage setUp = crawler.addPage(root, PathParser.parse("SuiteSetUp"), "suite set up");
    WikiPage tearDown = crawler.addPage(root, PathParser.parse("SuiteTearDown"), "suite tear down");
    
    testPages = new LinkedList<WikiPage>();
    testPages.add(setUp);
    testPages.add(slimPage);
    testPages.add(testPage);
    testPages.add(tearDown);

    MultipleTestsRunner runner = new MultipleTestsRunner(testPages, null, suite, null);
    Map<TestSystem.Descriptor, LinkedList<WikiPage>> map = runner.makeMapOfPagesByTestSystem();
    TestSystem.Descriptor fitDescriptor = TestSystem.getDescriptor(testPage.getData());
    TestSystem.Descriptor slimDescriptor = TestSystem.getDescriptor(slimPage.getData());

    List<WikiPage> fitList = map.get(fitDescriptor);
    List<WikiPage> slimList = map.get(slimDescriptor);

    assertEquals(3, fitList.size());
    assertEquals(3, slimList.size());

    assertEquals(setUp, fitList.get(0));
    assertEquals(testPage, fitList.get(1));
    assertEquals(tearDown, fitList.get(2));

    assertEquals(setUp, slimList.get(0));
    assertEquals(slimPage, slimList.get(1));
    assertEquals(tearDown, slimList.get(2));
  }

  
  private WikiPage addTestPage(WikiPage page, String name, String content) throws Exception {
    WikiPage testPage = crawler.addPage(page, PathParser.parse(name), content);
    PageData data = testPage.getData();
    data.setAttribute("Test");
    testPage.commit(data);
    testPages.add(testPage);

    return testPage;
  }
  
  private String classpathWidgets() {
    return "!path classes\n" +
      "!path lib/dummy.jar\n";
  }
}
