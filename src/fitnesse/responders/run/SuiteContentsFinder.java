// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageCrawlerImpl;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.VirtualCouplingExtension;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageDummy;
import fitnesse.wiki.WikiPagePath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class SuiteContentsFinder {

  public static final String SUITE_SETUP_NAME = "SuiteSetUp";
  public static final String SUITE_TEARDOWN_NAME = "SuiteTearDown";
  
  private final WikiPage pageToRun;
  private final WikiPage wikiRootPage;
  private final SuiteFilter suiteFilter;

  public SuiteContentsFinder(final WikiPage pageToRun, final WikiPage root, final SuiteFilter suiteFilter) {
    this.pageToRun = pageToRun;
    this.wikiRootPage = root;
    this.suiteFilter = (suiteFilter != null) ? suiteFilter : SuiteFilter.MATCH_ALL;
  }
  
  public List<WikiPage> makePageListForSingleTest() throws Exception {
    LinkedList<WikiPage> pages = new LinkedList<WikiPage>();

    pages.add(pageToRun);
    addSetupAndTeardown(pages);

    return pages;
  }
  
  public List<WikiPage> makePageList() throws Exception {
    LinkedList<WikiPage> pages = getAllPagesToRunForThisSuite();

    if (pages.isEmpty()) {
      String name = new WikiPagePath(pageToRun).toString();
      WikiPageDummy dummy = new WikiPageDummy("",
        "|Comment|\n|No test found with " + suiteFilter.toString() + " in subwiki !-" + name + "-!!|\n"
      );
      dummy.setParent(wikiRootPage);
      pages.add(dummy);
    }
    return pages;
  }

  private void addSetupAndTeardown(LinkedList<WikiPage> pages) throws Exception {
    WikiPage suiteSetUp = PageCrawlerImpl.getClosestInheritedPage(SUITE_SETUP_NAME, pageToRun);
    if (suiteSetUp != null) {
      if (pages.contains(suiteSetUp))
        pages.remove(suiteSetUp);
      pages.addFirst(suiteSetUp);
    }
    WikiPage suiteTearDown = PageCrawlerImpl.getClosestInheritedPage(SUITE_TEARDOWN_NAME, pageToRun);
    if (suiteTearDown != null) {
      if (pages.contains(suiteTearDown))
        pages.remove(suiteTearDown);
      pages.addLast(suiteTearDown);
    }
  }


  public LinkedList<WikiPage> getAllPagesToRunForThisSuite() throws Exception {
    LinkedList<WikiPage> pages = getAllTestPagesUnder();
    List<WikiPage> referencedPages = gatherCrossReferencedTestPages();
    pages.addAll(referencedPages);
    addSetupAndTeardown(pages);
    return pages;
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
    LinkedList<WikiPage> pages = new LinkedList<WikiPage>();
    PageData data = pageToRun.getData();
    List<String> pageReferences = data.getXrefPages();
    PageCrawler crawler = pageToRun.getPageCrawler();
    WikiPagePath testPagePath = crawler.getFullPath(pageToRun);
    WikiPage parent = crawler.getPage(wikiRootPage, testPagePath.parentPath());
    for (String pageReference : pageReferences) {
      WikiPagePath path = PathParser.parse(pageReference);
      WikiPage referencedPage = crawler.getPage(parent, path);
      if (referencedPage != null)
        pages.add(referencedPage);
    }
    return pages;
  }

  public static boolean isSuiteSetupOrTearDown(WikiPage testPage) throws Exception {
    String name = testPage.getName();
    return (SUITE_SETUP_NAME.equals(name) || SUITE_TEARDOWN_NAME.equals(name));
  }

}
