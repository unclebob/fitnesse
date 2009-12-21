// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.wiki.*;

import java.util.*;

public class SuiteContentsFinder {

  public static final String SUITE_SETUP_NAME = "SuiteSetUp";
  public static final String SUITE_TEARDOWN_NAME = "SuiteTearDown";

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

  public List<WikiPage> makePageListForSingleTest() throws Exception {
    testPageList = new LinkedList<WikiPage>();

    testPageList.add(pageToRun);

    return testPageList;
  }

  public List<WikiPage> makePageList() throws Exception {
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


  public LinkedList<WikiPage> getAllPagesToRunForThisSuite() throws Exception {
    String content = pageToRun.getData().getHtml();
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

  private LinkedList<WikiPage> getAllTestPagesUnder() throws Exception {
    LinkedList<WikiPage> testPages = new LinkedList<WikiPage>();
    addTestPagesToSuite(testPages, pageToRun, suiteFilter);

    Collections.sort(testPages, new Comparator<WikiPage>() {
      public int compare(WikiPage p1, WikiPage p2) {
        try {
          PageCrawler crawler = p1.getPageCrawler();
          WikiPagePath path1 = crawler.getFullPath(p1);
          WikiPagePath path2 = crawler.getFullPath(p2);

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

  private void addTestPagesToSuite(List<WikiPage> suite, WikiPage page, SuiteFilter suiteFilter) throws Exception {
      if (suiteFilter.isMatchingTest(page)) {
        suite.add(page);
      }

      SuiteFilter suiteFilterForChildren = suiteFilter.getFilterForTestsInSuite(page);

	    List<WikiPage> children = getChildren(page);
	    for (WikiPage child : children) {
	      addTestPagesToSuite(suite, child, suiteFilterForChildren);
	    }
	  }

	  private static List<WikiPage> getChildren(WikiPage page) throws Exception {
	    List<WikiPage> children = new ArrayList<WikiPage>();
	    children.addAll(page.getChildren());
	    addVirtualChildrenIfAny(page, children);
	    return children;
	  }

	  private static void addVirtualChildrenIfAny(WikiPage context, List<WikiPage> children) throws Exception {
	    if (context.hasExtension(VirtualCouplingExtension.NAME)) {
	      VirtualCouplingExtension extension = (VirtualCouplingExtension) context.getExtension(
	        VirtualCouplingExtension.NAME
	      );
	      children.addAll(extension.getVirtualCoupling().getChildren());
	    }
	  }

  protected List<WikiPage> gatherCrossReferencedTestPages() throws Exception {
    List<WikiPage> pages = new LinkedList<WikiPage>();
    addAllXRefs(pages, pageToRun);
    return pages;
  }

  private void addAllXRefs(List<WikiPage> xrefPages, WikiPage page) throws Exception {
    List<WikiPage> children = page.getChildren();
    addXrefPages(xrefPages, page);
    for (WikiPage child: children)
       addAllXRefs(xrefPages, child);
  }

  private void addXrefPages(List<WikiPage> pages, WikiPage thePage) throws Exception {
    PageData data = thePage.getData();
    List<String> pageReferences = data.getXrefPages();
    PageCrawler crawler = thePage.getPageCrawler();
    WikiPagePath testPagePath = crawler.getFullPath(thePage);
    WikiPage parent = crawler.getPage(wikiRootPage, testPagePath.parentPath());
    for (String pageReference : pageReferences) {
      WikiPagePath path = PathParser.parse(pageReference);
      WikiPage referencedPage = crawler.getPage(parent, path);
      if (referencedPage != null)
        pages.add(referencedPage);
    }
  }

  public static boolean isSuiteSetupOrTearDown(WikiPage testPage) throws Exception {
    String name = testPage.getName();
    return (SUITE_SETUP_NAME.equals(name) || SUITE_TEARDOWN_NAME.equals(name));
  }

}
