// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testrunner;

import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wiki.WikiPageProperty;
import fitnesse.wiki.WikiPageUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SuiteContentsFinder {
  private static final Logger LOG = Logger.getLogger(SuiteContentsFinder.class.getName());

  private final WikiPage pageToRun;
  private final WikiPage wikiRootPage;
  private final SuiteFilter suiteFilter;

  public SuiteContentsFinder(final WikiPage pageToRun, final SuiteFilter suiteFilter, WikiPage root) {
    this.pageToRun = pageToRun;
    wikiRootPage = root;
    this.suiteFilter = (suiteFilter != null) ? suiteFilter : SuiteFilter.MATCH_ALL;
  }

  public List<WikiPage> getAllPagesToRunForThisSuite() {
    List<WikiPage> testPageList = new LinkedList<>();
    String content = pageToRun.getHtml();
    if (SuiteSpecificationRunner.isASuiteSpecificationsPage(content)) {
      SuiteSpecificationRunner runner = new SuiteSpecificationRunner(wikiRootPage);
      if (runner.getPageListFromPageContent(content))
        testPageList = runner.testPages();
    } else {
      testPageList = getAllTestPagesUnder();
    }
    return testPageList;
  }

  private List<WikiPage> getAllTestPagesUnder() {
    List<WikiPage> testPages = addTestPagesToSuite(pageToRun, suiteFilter);

    testPages.sort((p1, p2) -> {
      try {
        WikiPagePath path1 = p1.getFullPath();
        WikiPagePath path2 = p2.getFullPath();

        return path1.compareTo(path2);
      } catch (Exception e) {
        LOG.log(Level.WARNING, "Unable to compare " + p1 + " and " + p2, e);
        return 0;
      }
    });

    return testPages;
  }

  private List<WikiPage> addTestPagesToSuite(WikiPage page, SuiteFilter suiteFilter) {
    List<WikiPage> testPages = new LinkedList<>();
    boolean includePage = isTopPage(page) || !isPruned(page);
    if (suiteFilter.isMatchingTest(page) && includePage) {
      testPages.add(page);
    }
    addXrefPages(testPages, page);

    SuiteFilter suiteFilterForChildren = includePage ? suiteFilter.getFilterForTestsInSuite(page) : SuiteFilter.NO_MATCHING;

    for (WikiPage child : page.getChildren()) {
      testPages.addAll(addTestPagesToSuite(child, suiteFilterForChildren));
    }
    return testPages;
  }

  private boolean isPruned(WikiPage page) {
    return page.getData().hasAttribute(WikiPageProperty.PRUNE);
  }

  private boolean isTopPage(WikiPage page) {
    return page == pageToRun;
  }

  private void addXrefPages(List<WikiPage> pages, WikiPage thePage) {
    List<String> pageReferences = WikiPageUtil.getXrefPages(thePage);
    if (pageReferences.isEmpty()) {
      return;
    }
    PageCrawler pageCrawler = thePage.getPageCrawler();
    for (String pageReference : pageReferences) {
      WikiPagePath path = PathParser.parse(pageReference);
      WikiPage referencedPage = pageCrawler.getSiblingPage(path);
      if (referencedPage != null)
        pages.add(referencedPage);
    }
  }
}
