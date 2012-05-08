// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.html;

import fitnesse.responders.run.TestPage;
import fitnesse.wiki.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SetupTeardownAndLibraryIncluder {
  private TestPage testPage;
  private boolean isSuite;
  private StringBuffer newPageContent;
  private PageCrawler pageCrawler;


  public static void includeInto(PageData pageData) {
    includeInto(pageData, false);
  }

  public static void includeInto(PageData pageData, boolean isSuite) {
    TestPage testPage = new TestPage(pageData);
    new SetupTeardownAndLibraryIncluder(testPage).includeInto(isSuite);
    pageData.setContent(testPage.getDecoratedData().getContent());
  }

  public static void includeSetupsTeardownsAndLibrariesBelowTheSuite(TestPage testPage, WikiPage suitePage) {
    new SetupTeardownAndLibraryIncluder(testPage).includeSetupsTeardownsAndLibrariesBelowTheSuite(suitePage);
  }


  private SetupTeardownAndLibraryIncluder(TestPage testPage) {
    this.testPage = testPage;
    pageCrawler = testPage.getSourcePage().getPageCrawler();
    newPageContent = new StringBuffer();
  }

  private void includeInto(boolean isSuite) {
    this.isSuite = isSuite;
    if (testPage.isTestPage())
      includeSetupTeardownAndLibraryPages();
  }


  private void includeSetupTeardownAndLibraryPages() {
    includeScenarioLibraries();
    includeSetupPages();
    includePageContent();
    includeTeardownPages();
    updatePageContent();
  }

  private void includeSetupsTeardownsAndLibrariesBelowTheSuite(WikiPage suitePage) {
    String pageName = testPage.getName();
    includeScenarioLibraryBelow(suitePage);
    if (!isSuiteSetUpOrTearDownPage(pageName))
      includeSetupPages();
    includePageContent();
    if (!isSuiteSetUpOrTearDownPage(pageName))
      includeTeardownPages();
    updatePageContent();
  }

  private boolean isSuiteSetUpOrTearDownPage(String pageName) {
    return PageData.SUITE_SETUP_NAME.equals(pageName) || PageData.SUITE_TEARDOWN_NAME.equals(pageName);
  }

  private void includeScenarioLibraryBelow(WikiPage suitePage) {
    includeScenarioLibrariesIfAppropriate(new BelowSuiteLibraryFilter(suitePage));
  }

  private void includeScenarioLibraries() {
    includeScenarioLibrariesIfAppropriate(AllLibrariesFilter.instance);

  }

  private void includeSetupPages() {
    if (isSuite)
      includeSuiteSetupPage();
    includeSetupPage();
  }

  private void includeSuiteSetupPage() {
    include(PageData.SUITE_SETUP_NAME, "-setup");
  }

  private void includeSetupPage() {
    include("SetUp", "-setup");
  }

  private void includePageContent() {
    newPageContent.append(testPage.parsedData().getContent());
  }

  private void includeTeardownPages() {
    includeTeardownPage();
    if (isSuite)
      includeSuiteTeardownPage();
  }

  private void includeTeardownPage() {
    include("TearDown", "-teardown");
  }

  private void includeSuiteTeardownPage() {
    include(PageData.SUITE_TEARDOWN_NAME, "-teardown");
  }

  private void updatePageContent() {
    testPage.decorate(newPageContent.toString());
  }

  private void include(String pageName, String arg) {
    WikiPage inheritedPage = findInheritedPage(pageName);
    if (inheritedPage != null) {
      String pagePathName = getPathNameForPage(inheritedPage);
      includePage(pagePathName, arg);
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
    newPageContent.append("!*> Scenario Libraries\n");
    for (WikiPage uncle : uncles)
      includeScenarioLibrary(uncle);
    newPageContent.append("*!\n");
  }

  private void includeScenarioLibrary(WikiPage uncle) {
    newPageContent.append("!include -c .");
    newPageContent.append(PathParser.render(pageCrawler.getFullPath(uncle)));
    newPageContent.append("\n");
  }

  private WikiPage findInheritedPage(String pageName) {
    return PageCrawlerImpl.getClosestInheritedPage(pageName, testPage.getSourcePage());
  }

  private String getPathNameForPage(WikiPage page) {
    WikiPagePath pagePath = pageCrawler.getFullPath(page);
    return PathParser.render(pagePath);
  }

  private void includePage(String pagePathName, String arg) {
    newPageContent
      .append("\n!include ")
      .append(arg)
      .append(" .")
      .append(pagePathName)
      .append("\n");
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

  private class BelowSuiteLibraryFilter implements LibraryFilter {
    private int minimumPathLength;

    public BelowSuiteLibraryFilter(WikiPage suitePage) {
      minimumPathLength = suitePage.getPageCrawler().getFullPath(suitePage).addNameToEnd("ScenarioLibrary").toString().length();
    }

    @Override
    public boolean canUse(WikiPage libraryPage) {
      return libraryPage.getPageCrawler().getFullPath(libraryPage).toString().length() > minimumPathLength;
    }
  }


}
