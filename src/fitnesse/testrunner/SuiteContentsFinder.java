// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testrunner;

import fitnesse.wiki.*;

import java.util.*;

public class SuiteContentsFinder {

  private final WikiPage pageToRun;
  private final WikiPage wikiRootPage;
  private final SuiteFilter suiteFilter;
  private LinkedList<WikiPage> testPageList;

  public SuiteContentsFinder(final WikiPage pageToRun, final SuiteFilter suiteFilter, WikiPage root) {
    this.pageToRun = pageToRun;
    wikiRootPage = root;
    this.suiteFilter = (suiteFilter != null) ? suiteFilter : SuiteFilter.MATCH_ALL;
    testPageList = new LinkedList<WikiPage>();
  }

  public List<WikiPage> makePageListForSingleTest() {
    testPageList = new LinkedList<WikiPage>();

    testPageList.add(pageToRun);

    return testPageList;
  }

  public List<WikiPage> makePageList() {
    getAllPagesToRunForThisSuite();

    if (testPageList.isEmpty()) {
      String name = new WikiPagePath(pageToRun).toString();
      WikiPageDummy dummy = new WikiPageDummy("",
        "|Comment|\n|No test found with " + suiteFilter.toString() + " in subwiki !-" + name + "-!!|\n"
      );
      dummy.setParent(wikiRootPage);
      testPageList.add(dummy);
    }
    return testPageList;
  }


  public LinkedList<WikiPage> getAllPagesToRunForThisSuite() {
    String content = pageToRun.getData().getHtml();
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

  private LinkedList<WikiPage> getAllTestPagesUnder() {
    LinkedList<WikiPage> testPages = new LinkedList<WikiPage>();
    addTestPagesToSuite(testPages, pageToRun, suiteFilter);

    Collections.sort(testPages, new Comparator<WikiPage>() {
      public int compare(WikiPage p1, WikiPage p2) {
        try {
          WikiPagePath path1 = p1.getPageCrawler().getFullPath();
          WikiPagePath path2 = p2.getPageCrawler().getFullPath();

          return path1.compareTo(path2);
        }
        catch (Exception e) {
          e.printStackTrace();
          return 0;
        }
      }
    }
    );

    return testPages;
  }

  private void addTestPagesToSuite(List<WikiPage> suite, WikiPage page, SuiteFilter suiteFilter) {
      if (suiteFilter.isMatchingTest(page)) {
        suite.add(page);
      }

      SuiteFilter suiteFilterForChildren = suiteFilter.getFilterForTestsInSuite(page);

	    List<WikiPage> children = getChildren(page);
	    for (WikiPage child : children) {
	      addTestPagesToSuite(suite, child, suiteFilterForChildren);
	    }
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
    ReadOnlyPageData data = thePage.readOnlyData();
    List<String> pageReferences = data.getXrefPages();
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
