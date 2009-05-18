// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import static junit.framework.Assert.assertSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class SuiteContentsFinderTest {

  private WikiPage root;
  private WikiPage suite;
  private WikiPage testPage;
  private PageCrawler crawler;
  private WikiPage testPage2;
  private WikiPage testChildPage;
  private final String simpleSlimDecisionTable = "!define TEST_SYSTEM {slim}\n" +
  "|!-DT:fitnesse.slim.test.TestSlim-!|\n" +
  "|string|get string arg?|\n" +
  "|wow|wow|\n";

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
    PageData data = root.getData();
    root.commit(data);
    suite = crawler.addPage(root, PathParser.parse("SuitePageName"), "The is the test suite\n");
    testPage = addTestPage(suite, "TestOne", "My test");
  }
  
  private WikiPage addTestPage(WikiPage page, String name, String content) throws Exception {
    WikiPage testPage = crawler.addPage(page, PathParser.parse(name), content);
    PageData data = testPage.getData();
    data.setAttribute("Test");
    testPage.commit(data);
    return testPage;
  }

  @Test
  public void testGatherXRefTestPages() throws Exception {
    WikiPage testPage = crawler.addPage(root, PathParser.parse("SomePage"), "!see PageA\n!see PageB");
    WikiPage pageA = crawler.addPage(root, PathParser.parse("PageA"));
    WikiPage pageB = crawler.addPage(root, PathParser.parse("PageB"));
    SuiteContentsFinder finder = new SuiteContentsFinder(testPage, root, null);
    List<WikiPage> xrefTestPages = finder.gatherCrossReferencedTestPages();
    assertEquals(2, xrefTestPages.size());
    assertTrue(xrefTestPages.contains(pageA));
    assertTrue(xrefTestPages.contains(pageB));
  }
  
  
  private void setUpForGetAllTestPages() throws Exception {
    testPage2 = addTestPage(suite, "TestPageTwo", "test page two");
    testChildPage = testPage2.addChildPage("TestChildPage");
    PageData data = testChildPage.getData();
    data.setAttribute("Test");
    testChildPage.commit(data);
  }
  
  @Test
  public void testGetAllTestPages() throws Exception {
    setUpForGetAllTestPages();

    SuiteContentsFinder finder = new SuiteContentsFinder(suite, root, null);
    List<WikiPage> testPages = finder.makePageList();

    assertEquals(3, testPages.size());
    assertEquals(true, testPages.contains(testPage));
    assertEquals(true, testPages.contains(testPage2));
    assertEquals(true, testPages.contains(testChildPage));
  }
  
  @Test
  public void testGetAllTestPagesSortsByQulifiedNames() throws Exception {
    setUpForGetAllTestPages();
    
    SuiteContentsFinder finder = new SuiteContentsFinder(suite, root, null);
    List<WikiPage> testPages = finder.makePageList();

    assertEquals(3, testPages.size());
    assertEquals(testPage, testPages.get(0));
    assertEquals(testPage2, testPages.get(1));
    assertEquals(testChildPage, testPages.get(2));
  }
  
  @Test
  public void testPagesForTestSystemAreSurroundedBySuiteSetupAndTeardown() throws Exception {
    WikiPage slimPage = addTestPage(suite, "AaSlimTest", simpleSlimDecisionTable);
    WikiPage setUp = crawler.addPage(root, PathParser.parse("SuiteSetUp"), "suite set up");
    WikiPage tearDown = crawler.addPage(root, PathParser.parse("SuiteTearDown"), "suite tear down");

    SuiteContentsFinder finder = new SuiteContentsFinder(suite, root, null);
    List<WikiPage> testPages = finder.makePageList();

    assertEquals(4, testPages.size());
    assertEquals(setUp, testPages.get(0));
    assertEquals(slimPage, testPages.get(1));
    assertEquals(testPage, testPages.get(2));
    assertEquals(tearDown, testPages.get(3));
  }

  @Test
  public void testSetUpAndTearDown() throws Exception {
    WikiPage setUp = crawler.addPage(root, PathParser.parse("SuiteSetUp"), "suite set up");
    WikiPage tearDown = crawler.addPage(root, PathParser.parse("SuiteTearDown"), "suite tear down");

    SuiteContentsFinder finder = new SuiteContentsFinder(suite, root, null);
    List<WikiPage> testPages = finder.makePageList();
    
    assertEquals(3, testPages.size());
    assertSame(setUp, testPages.get(0));
    assertSame(tearDown, testPages.get(2));
  }
}
