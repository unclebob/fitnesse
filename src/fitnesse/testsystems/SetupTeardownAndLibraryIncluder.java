// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageCrawlerImpl;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

// TODO: refactor this into a TestPageBuilder.
public class SetupTeardownAndLibraryIncluder {
  public static final String TEAR_DOWN = "TearDown";
  public static final String SET_UP = "SetUp";
  private TestPage testPage;
  private boolean isSuite;
  private PageCrawler pageCrawler;


  public static void includeInto(TestPage testPage) {
    includeInto(testPage, false);
  }

  public static void includeInto(TestPage testPage, boolean isSuite) {
    new SetupTeardownAndLibraryIncluder(testPage).includeInto(isSuite);
  }

  private SetupTeardownAndLibraryIncluder(TestPage testPage) {
    this.testPage = testPage;
    pageCrawler = testPage.getSourcePage().getPageCrawler();
  }

  private void includeInto(boolean isSuite) {
    this.isSuite = isSuite;
    if (testPage.isTestPage())
      includeSetupTeardownAndLibraryPages();
  }


  private void includeSetupTeardownAndLibraryPages() {
    String pageName = testPage.getName();
    includeScenarioLibraries();
    if (!isSuiteSetUpOrTearDownPage(pageName))
      includeSetupPages();
    if (!isSuiteSetUpOrTearDownPage(pageName))
      includeTeardownPages();
  }

  private boolean isSuiteSetUpOrTearDownPage(String pageName) {
    return PageData.SUITE_SETUP_NAME.equals(pageName) || PageData.SUITE_TEARDOWN_NAME.equals(pageName);
  }

  private void includeScenarioLibraries() {
    includeScenarioLibrariesIfAppropriate(AllLibrariesFilter.instance);

  }

  private void includeSetupPages() {
    if (isSuite) {
      testPage.setSuiteSetUp(findInheritedPage(PageData.SUITE_SETUP_NAME));
    }
    testPage.setSetUp(findInheritedPage(SET_UP));
  }

  private void includeTeardownPages() {
    testPage.setTearDown(findInheritedPage(TEAR_DOWN));
    if (isSuite) {
      testPage.setSuiteTearDown(findInheritedPage(PageData.SUITE_TEARDOWN_NAME));
    }
  }

  private void includeScenarioLibrariesIfAppropriate(LibraryFilter libraryFilter) {
    if (testPage.isSlim())
      includeScenarioLibrariesIfAny(libraryFilter);
  }

  private void includeScenarioLibrariesIfAny(LibraryFilter libraryFilter) {
    List<WikiPage> uncles = PageCrawlerImpl.getAllUncles("ScenarioLibrary", testPage.getSourcePage());

    List<WikiPage> filteredUncles = filter(uncles, libraryFilter);
    if (filteredUncles.size() > 0)
      includeScenarioLibraries(filteredUncles);
  }

  private List<WikiPage> filter(List<WikiPage> widgets, LibraryFilter filter) {
    List<WikiPage> filteredList = new LinkedList<WikiPage>();
    for (WikiPage widget : widgets) {
      if (filter.canUse(widget))
        filteredList.add(widget);
    }
    return filteredList;
  }

  private void includeScenarioLibraries(List<WikiPage> uncles) {
    Collections.reverse(uncles);
    testPage.setScenarioLibraries(uncles);
  }

  private WikiPage findInheritedPage(String pageName) {
    return PageCrawlerImpl.getClosestInheritedPage(pageName, testPage.getSourcePage());
  }


  private static interface LibraryFilter {
    boolean canUse(WikiPage libraryPage);
  }

  private static class AllLibrariesFilter implements LibraryFilter {
    public static AllLibrariesFilter instance = new AllLibrariesFilter();

    @Override
    public boolean canUse(WikiPage libraryPage) {
      return true;
    }
  }

}
