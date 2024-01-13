// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testrunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import fitnesse.wiki.*;
import fitnesse.wiki.fs.InMemoryPage;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class SuiteContentsFinderTest {

  private WikiPage root;
  private WikiPage suite;
  private WikiPage testPage;
  private WikiPage testPage2;
  private WikiPage testChildPage;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    PageData data = root.getData();
    root.commit(data);
    suite = WikiPageUtil.addPage(root, PathParser.parse("SuitePageName"), "The is the test suite\n");
    testPage = addTestPage(suite, "TestOne", "My test and has some content");
  }

  private WikiPage addTestPage(WikiPage page, String name, String content) {
    WikiPage testPage = WikiPageUtil.addPage(page, PathParser.parse(name), content);
    PageData data = testPage.getData();
    data.setAttribute("Test");
    testPage.commit(data);
    return testPage;
  }

  @Test
  public void testGatherXRefTestPages() {
    WikiPage testPage = WikiPageUtil.addPage(root, PathParser.parse("SomePage"), "!see PageA\n!see PageB");
    WikiPage pageA = WikiPageUtil.addPage(root, PathParser.parse("PageA"), "");
    WikiPage pageB = WikiPageUtil.addPage(root, PathParser.parse("PageB"), "");
    SuiteContentsFinder finder = new SuiteContentsFinder(testPage, null, root);
    List<WikiPage> testPages = finder.getAllPagesToRunForThisSuite();
    assertEquals(2, testPages.size());
    assertTrue(testPages.contains(pageA));
    assertTrue(testPages.contains(pageB));
  }

  @Test
  public void shouldTestXRefsInSubSuites() {
    WikiPageUtil.addPage(suite, PathParser.parse("SubSuite"), "!see .PageA\n!see .PageB");
    WikiPage pageA = WikiPageUtil.addPage(root, PathParser.parse("PageA"), "");
    WikiPage pageB = WikiPageUtil.addPage(root, PathParser.parse("PageB"), "");
    SuiteContentsFinder finder = new SuiteContentsFinder(suite, null, root);
    List<WikiPage> testPages = finder.getAllPagesToRunForThisSuite();
    assertEquals(3, testPages.size());
    assertTrue(testPages.contains(pageA));
    assertTrue(testPages.contains(pageB));
  }


  private void setUpForGetAllTestPages() {
    testPage2 = addTestPage(suite, "TestPageTwo", "test page two");
    testChildPage = testPage2.addChildPage("ChildPage");
    PageData data = testChildPage.getData();
    data.setAttribute("Test");
    testChildPage.commit(data);
  }

  @Test
  public void testGetAllTestPages() {
    setUpForGetAllTestPages();

    SuiteContentsFinder finder = new SuiteContentsFinder(suite, null, root);
    List<WikiPage> testPages = finder.getAllPagesToRunForThisSuite();

    assertEquals(3, testPages.size());
    assertTrue(testPages.contains(testPage));
    assertTrue(testPages.contains(testPage2));
    assertTrue(testPages.contains(testChildPage));
  }

  @Test
  public void testGetAllTestPagesSortsByQualifiedNames() {
    setUpForGetAllTestPages();

    SuiteContentsFinder finder = new SuiteContentsFinder(suite, null, root);
    List<WikiPage> testPages = finder.getAllPagesToRunForThisSuite();

    assertEquals(3, testPages.size());
    assertEquals(testPage, testPages.get(0));
    assertEquals(testPage2, testPages.get(1));
    assertEquals(testChildPage, testPages.get(2));
  }


  @Test
  public void shouldTellIfItIsASpecificationsSuite() {
    WikiPageUtil.addPage(root, PathParser.parse("SuiteSetUp"), "suite set up");
    WikiPageUtil.addPage(root, PathParser.parse("SuiteTearDown"), "suite tear down");
    setUpForGetAllTestPages();
    String content = "|Suite|\n|Title|Test|\n|Content|.|\n";
    suite.commit(new PageData(suite.getData(), content));
    SuiteContentsFinder finder = new SuiteContentsFinder(suite, null, root);
    List<WikiPage> testPages = finder.getAllPagesToRunForThisSuite();
    assertEquals(2, testPages.size());
    assertEquals(testPage, testPages.get(0));
    assertEquals(testPage2, testPages.get(1));
  }

  @Test
  public void shouldRejectNestedPrunedPages() {
    PageData data = testPage.getData();
    data.setAttribute(WikiPageProperty.PRUNE);
    testPage.commit(data);

    SuiteContentsFinder finder = new SuiteContentsFinder(suite, null, root);
    List<WikiPage> testPages = finder.getAllPagesToRunForThisSuite();

    assertEquals(0, testPages.size());

  }

  @Test
  public void shouldAddPrunedPageToTest() {
    PageData data = suite.getData();
    data.setAttribute(WikiPageProperty.PRUNE);
    suite.commit(data);

    SuiteContentsFinder finder = new SuiteContentsFinder(suite, null, root);
    List<WikiPage> testPages = finder.getAllPagesToRunForThisSuite();

    assertEquals(1, testPages.size());
  }
}
