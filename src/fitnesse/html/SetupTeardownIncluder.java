// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.html;

import fitnesse.responders.run.SuiteContentsFinder;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageCrawlerImpl;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

import java.util.List;
import java.util.Collections;

public class SetupTeardownIncluder {
  private PageData pageData;
  private boolean isSuite;
  private WikiPage testPage;
  private StringBuffer newPageContent;
  private PageCrawler pageCrawler;


  public static void includeInto(PageData pageData) throws Exception {
    includeInto(pageData, false);
  }

  public static void includeInto(PageData pageData, boolean isSuite)
    throws Exception {
    new SetupTeardownIncluder(pageData).includeInto(isSuite);
  }

  private SetupTeardownIncluder(PageData pageData) {
    this.pageData = pageData;
    testPage = pageData.getWikiPage();
    pageCrawler = testPage.getPageCrawler();
    newPageContent = new StringBuffer();
  }

  private void includeInto(boolean isSuite) throws Exception {
    this.isSuite = isSuite;
    if (isTestPage())
      includeSetupAndTeardownPages();
  }

  private boolean isTestPage() throws Exception {
    return pageData.hasAttribute("Test");
  }

  private void includeSetupAndTeardownPages() throws Exception {
    includeSetupPages();
    includeScenarioLibrary();
    includePageContent();
    includeTeardownPages();
    updatePageContent();
  }

  private void includeScenarioLibrary() throws Exception {
    includeScenarioLibrariesIfAppropriate();

  }

  private void includeSetupPages() throws Exception {
    if (isSuite)
      includeSuiteSetupPage();
    includeSetupPage();
  }

  private void includeSuiteSetupPage() throws Exception {
    include(SuiteContentsFinder.SUITE_SETUP_NAME, "-setup");
  }

  private void includeSetupPage() throws Exception {
    include("SetUp", "-setup");
  }

  private void includePageContent() throws Exception {
    newPageContent.append(pageData.getContent());
  }

  private void includeTeardownPages() throws Exception {
    includeTeardownPage();
    if (isSuite)
      includeSuiteTeardownPage();
  }

  private void includeTeardownPage() throws Exception {
    include("TearDown", "-teardown");
  }

  private void includeSuiteTeardownPage() throws Exception {
    include(SuiteContentsFinder.SUITE_TEARDOWN_NAME, "-teardown");
  }

  private void updatePageContent() throws Exception {
    pageData.setContent(newPageContent.toString());
  }

  private void include(String pageName, String arg) throws Exception {
    WikiPage inheritedPage = findInheritedPage(pageName);
    if (inheritedPage != null) {
      String pagePathName = getPathNameForPage(inheritedPage);
      includePage(pagePathName, arg);
    }
  }

  private void includeScenarioLibrariesIfAppropriate() throws Exception {
    if (isSlim(testPage))
      includeScenaiorLibrariesIfAny();
  }

  private void includeScenaiorLibrariesIfAny() throws Exception {
    List<WikiPage> uncles = PageCrawlerImpl.getAllUncles("ScenarioLibrary", testPage);
    if (uncles.size() > 0)
      includeScenarioLibraries(uncles);
  }

  private boolean isSlim(WikiPage page) throws Exception {
    String testSystem = page.getData().getVariable("TEST_SYSTEM");
    boolean isSlim = "slim".equalsIgnoreCase(testSystem);
    return isSlim;
  }

  private void includeScenarioLibraries(List<WikiPage> uncles) throws Exception {
    Collections.reverse(uncles);
    newPageContent.append("!*> Scenario Libraries\n");
    for (WikiPage uncle : uncles)
      includeScenarioLibrary(uncle);
    newPageContent.append("*!\n");
  }

  private void includeScenarioLibrary(WikiPage uncle) throws Exception {
    newPageContent.append("!include -c .");
    newPageContent.append(PathParser.render(pageCrawler.getFullPath(uncle)));
    newPageContent.append("\n");
  }

  private WikiPage findInheritedPage(String pageName) throws Exception {
    return PageCrawlerImpl.getClosestInheritedPage(pageName, testPage);
  }

  private String getPathNameForPage(WikiPage page) throws Exception {
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
}
