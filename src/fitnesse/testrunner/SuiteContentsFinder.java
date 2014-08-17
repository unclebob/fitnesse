// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testrunner;

import fitnesse.wiki.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SuiteContentsFinder {
  public static final Logger LOG = Logger.getLogger(SuiteContentsFinder.class.getName());

  private final WikiPage pageToRun;
  private final WikiPage wikiRootPage;
  private final SuiteFilter suiteFilter;
  private List<WikiPage> testPageList;

  public SuiteContentsFinder(final WikiPage pageToRun, final SuiteFilter suiteFilter, WikiPage root) {
    this.pageToRun = pageToRun;
    wikiRootPage = root;
    this.suiteFilter = (suiteFilter != null) ? suiteFilter : SuiteFilter.MATCH_ALL;
    testPageList = new LinkedList<WikiPage>();
  }

  public List<WikiPage> getAllPagesToRunForThisSuite() {
    String content = pageToRun.getHtml();
    //todo perf: all pages html parsed here?
    if (SuiteSpecificationRunner.isASuiteSpecificationsPage(content)) {
      SuiteSpecificationRunner runner = new SuiteSpecificationRunner(wikiRootPage);
      if (runner.getPageListFromPageContent(content))
        testPageList = runner.testPageList;
    } else {
      testPageList = getAllTestPagesUnder();
      List<WikiPage> referencedPages = gatherCrossReferencedTestPages();
      testPageList.addAll(referencedPages);
    }
    return testPageList;
  }

  private List<WikiPage> getAllTestPagesUnder() {
    List<WikiPage> testPages = addTestPagesToSuite(pageToRun, suiteFilter);

    Collections.sort(testPages, new Comparator<WikiPage>() {
      public int compare(WikiPage p1, WikiPage p2) {
        try {
          WikiPagePath path1 = p1.getPageCrawler().getFullPath();
          WikiPagePath path2 = p2.getPageCrawler().getFullPath();

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
    List<WikiPage> testPages = new LinkedList<WikiPage>();
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
	    List<WikiPage> children = new ArrayList<WikiPage>();
	    children.addAll(page.getChildren());
	    return children;
	  }

  protected List<WikiPage> gatherCrossReferencedTestPages() {
    List<WikiPage> pages = new LinkedList<WikiPage>();
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
    WikiPagePath testPagePath = thePage.getPageCrawler().getFullPath();
    WikiPage parent = wikiRootPage.getPageCrawler().getPage(testPagePath.parentPath());
    for (String pageReference : pageReferences) {
      WikiPagePath path = PathParser.parse(pageReference);
      WikiPage referencedPage = parent.getPageCrawler().getPage(path);
      if (referencedPage != null)
        pages.add(referencedPage);
    }
  }

  public static boolean isSuiteSetupOrTearDown(WikiPage testPage) {
    String name = testPage.getName();
    return (PageData.SUITE_SETUP_NAME.equals(name) || PageData.SUITE_TEARDOWN_NAME.equals(name));
  }

}
