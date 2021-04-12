// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testrunner;

import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wiki.WikiPageUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
      testPageList.addAll(gatherCrossReferencedTestPages());
    }
    return testPageList;
  }

  private List<WikiPage> getAllTestPagesUnder() {
    List<WikiPage> testPages = addTestPagesToSuite(pageToRun, suiteFilter);

    Collections.sort(testPages, new Comparator<WikiPage>() {
      @Override
      public int compare(WikiPage p1, WikiPage p2) {
        try {
          WikiPagePath path1 = p1.getFullPath();
          WikiPagePath path2 = p2.getFullPath();

          return path1.compareTo(path2);
        }
        catch (Exception e) {
          LOG.log(Level.WARNING, "Unable to compare " + p1 + " and " + p2, e);
          return 0;
        }
      }
    }
    );

    return testPages;
  }

  private List<WikiPage> addTestPagesToSuite(WikiPage page, SuiteFilter suiteFilter) {
    List<WikiPage> testPages = new LinkedList<>();
    boolean includePage = isTopPage(page) || !isPruned(page);
    if (suiteFilter.isMatchingTest(page) && includePage) {
      testPages.add(page);
    }

    SuiteFilter suiteFilterForChildren = includePage ? suiteFilter.getFilterForTestsInSuite(page) : SuiteFilter.NO_MATCHING;

    for (WikiPage child : getChildren(page)) {
      testPages.addAll(addTestPagesToSuite(child, suiteFilterForChildren));
    }
    return testPages;
  }

  private boolean isPruned(WikiPage page) {
    return page.getData().hasAttribute(PageData.PropertyPRUNE);
  }

  private boolean isTopPage(WikiPage page) {
    return page == pageToRun;
  }

  private static List<WikiPage> getChildren(WikiPage page) {
	    List<WikiPage> children = new ArrayList<>();
	    children.addAll(page.getChildren());
	    return children;
	  }

  protected List<WikiPage> gatherCrossReferencedTestPages() {
    List<WikiPage> pages = new LinkedList<>();
    addAllXRefs(pages, pageToRun);
    return pages;
  }

  private void addAllXRefs(List<WikiPage> xrefPages, WikiPage page) {
    List<WikiPage> children = page.getChildren();
    addXrefPages(xrefPages, page);
    for (WikiPage child: children)
       addAllXRefs(xrefPages, child);
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
