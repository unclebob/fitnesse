// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testrunner;

import fitnesse.FitNesseContext;
import fitnesse.wiki.ClassPathBuilder;
import fitnesse.testsystems.*;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.*;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import fitnesse.wiki.mem.InMemoryPage;
import org.junit.Before;
import org.junit.Test;

import static util.RegexTestCase.assertSubString;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class MultipleTestsRunnerTest {
  private WikiPage root;
  private WikiPage suite;
  private WikiPage testPage;
  private String suitePageName;
  private final String simpleSlimDecisionTable = "!define TEST_SYSTEM {slim}\n" +
    "|!-DT:fitnesse.slim.test.TestSlim-!|\n" +
    "|string|get string arg?|\n" +
    "|wow|wow|\n";
  private List<WikiPage> testPages;
  private FitNesseContext context;

  @Before
  public void setUp() throws Exception {
    suitePageName = "SuitePage";
    root = InMemoryPage.makeRoot("RooT");
    context = FitNesseUtil.makeTestContext(root);
    PageData data = root.getData();
    data.setContent(classpathWidgets());
    root.commit(data);
    suite = WikiPageUtil.addPage(root, PathParser.parse(suitePageName), "This is the test suite\n");
    testPages = new LinkedList<WikiPage>();
    testPage = addTestPage(suite, "TestOne", "My test");
 }

  @Test
  public void testBuildClassPath() throws Exception {
    String classpath = new ClassPathBuilder().buildClassPath(testPages);
    assertSubString("classes", classpath);
    assertSubString("dummy.jar", classpath);
  }

  @Test
  public void testGenerateSuiteMapWithMultipleTestSystems() throws Exception {
    WikiPage slimPage = addTestPage(suite, "SlimTest", simpleSlimDecisionTable);
    
    MultipleTestsRunner runner = new MultipleTestsRunner(testPages, context);
    Map<WikiPageDescriptor, LinkedList<WikiTestPage>> map = runner.makeMapOfPagesByTestSystem();

    Descriptor fitDescriptor = new WikiPageDescriptor(testPage.readOnlyData(), false, new ClassPathBuilder().getClasspath(testPage));
    Descriptor slimDescriptor = new WikiPageDescriptor(slimPage.readOnlyData(), false, new ClassPathBuilder().getClasspath(slimPage));
    List<WikiTestPage> fitList = map.get(fitDescriptor);
    List<WikiTestPage> slimList = map.get(slimDescriptor);

    assertEquals(1, fitList.size());
    assertEquals(1, slimList.size());
    assertEquals(testPage, fitList.get(0).getSourcePage());
    assertEquals(slimPage, slimList.get(0).getSourcePage());
  }
  
  @Test
  public void testPagesForTestSystemAreSurroundedBySuiteSetupAndTeardown() throws Exception {
    WikiPage slimPage = addTestPage(suite, "AaSlimTest", simpleSlimDecisionTable);
    WikiPage setUp = WikiPageUtil.addPage(root, PathParser.parse("SuiteSetUp"), "suite set up");
    WikiPage tearDown = WikiPageUtil.addPage(root, PathParser.parse("SuiteTearDown"), "suite tear down");
    
    testPages = new LinkedList<WikiPage>();
    testPages.add(setUp);
    testPages.add(slimPage);
    testPages.add(testPage);
    testPages.add(tearDown);

    MultipleTestsRunner runner = new MultipleTestsRunner(testPages, context);
    Map<WikiPageDescriptor, LinkedList<WikiTestPage>> map = runner.makeMapOfPagesByTestSystem();
    Descriptor fitDescriptor = new WikiPageDescriptor(testPage.readOnlyData(), false, new ClassPathBuilder().getClasspath(testPage));
    Descriptor slimDescriptor = new WikiPageDescriptor(slimPage.readOnlyData(), false, new ClassPathBuilder().getClasspath(slimPage));

    List<WikiTestPage> fitList = map.get(fitDescriptor);
    List<WikiTestPage> slimList = map.get(slimDescriptor);

    assertEquals(3, fitList.size());
    assertEquals(3, slimList.size());

    assertEquals(setUp, fitList.get(0).getSourcePage());
    assertEquals(testPage, fitList.get(1).getSourcePage());
    assertEquals(tearDown, fitList.get(2).getSourcePage());

    assertEquals(setUp, slimList.get(0).getSourcePage());
    assertEquals(slimPage, slimList.get(1).getSourcePage());
    assertEquals(tearDown, slimList.get(2).getSourcePage());
  }

  
  private WikiPage addTestPage(WikiPage page, String name, String content) throws Exception {
    WikiPage testPage = WikiPageUtil.addPage(page, PathParser.parse(name), content);
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
  
  @Test
  public void startingNewTestShouldStartTimeMeasurementAndNotifyListener() throws Exception {
    List<WikiPage> testPagesToRun = mock(List.class);
    WikiPage slimPage = addTestPage(suite, "AaSlimTest", simpleSlimDecisionTable);
    WikiTestPage page = new WikiTestPage(slimPage);
    CompositeFormatter resultsListener = new CompositeFormatter();
    TestSystemListener listener = mock(TestSystemListener.class);
    resultsListener.addTestSystemListener(listener);

    MultipleTestsRunner runner = new MultipleTestsRunner(testPagesToRun, context);
    runner.addTestSystemListener(resultsListener);

    runner.testStarted(page);
    verify(listener).testStarted(same(page));
  }

  @Test
  public void testCompleteShouldRemoveHeadOfQueueAndNotifyListener() throws Exception {
    List<WikiPage> testPagesToRun = mock(List.class);
    WikiPage slimPage = addTestPage(suite, "AaSlimTest", simpleSlimDecisionTable);
    WikiTestPage page = new WikiTestPage(slimPage);
    CompositeFormatter resultsListener = new CompositeFormatter();
    TestSystemListener listener = mock(TestSystemListener.class);
    resultsListener.addTestSystemListener(listener);
    
    MultipleTestsRunner runner = new MultipleTestsRunner(testPagesToRun, context);
    runner.addTestSystemListener(resultsListener);

    TestSummary testSummary = mock(TestSummary.class);

    runner.testStarted(page);
    runner.testComplete(page, testSummary);
    verify(listener).testComplete(same(page), same(testSummary));
  }
}
