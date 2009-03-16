// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageCrawlerImpl;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.VirtualCouplingExtension;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageDummy;
import fitnesse.wiki.WikiPagePath;

public class SuiteContentsFinder {

  public static final String SUITE_SETUP_NAME = "SuiteSetUp";
  public static final String SUITE_TEARDOWN_NAME = "SuiteTearDown";
  
  private final WikiPage pageToRun;
  private final WikiPage wikiRootPage;
  private final Set<String> suiteTags;

  public SuiteContentsFinder(final WikiPage pageToRun, final WikiPage root, final String suiteQueryString) {
    this.pageToRun = pageToRun;
    this.wikiRootPage = root;
    suiteTags = new HashSet<String>();
    if (suiteQueryString != null)
    	suiteTags.addAll(Arrays.asList(suiteQueryString.split("\\s*,\\s*")));
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
        "|Comment|\n|No test found with suite filter '" + suiteTags + "' in subwiki !-" + name + "-!!|\n"
      );
      dummy.setParent(wikiRootPage);
      pages.add(dummy);
    }
    return pages;
  }

  private void addSetupAndTeardown(LinkedList<WikiPage> pages) throws Exception {
    WikiPage suiteSetUp = PageCrawlerImpl.getInheritedPage(SUITE_SETUP_NAME, pageToRun);
    if (suiteSetUp != null) {
      if (pages.contains(suiteSetUp))
        pages.remove(suiteSetUp);
      pages.addFirst(suiteSetUp);
    }
    WikiPage suiteTearDown = PageCrawlerImpl.getInheritedPage(SUITE_TEARDOWN_NAME, pageToRun);
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
    addTestPagesToSuite(testPages, pageToRun, suiteTags);

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
  
  private static void addTestPagesToSuite(List<WikiPage> suite, WikiPage page, Set<String> suiteQuery) throws Exception {
	    if (shouldBePartOfSuite(page, suiteQuery))
	      suite.add(page);

	    PageData pageData = page.getData();
	    if (pageData.hasAttribute("Suite") && belongsToSuite(page, suiteQuery))
	      suiteQuery = null;

	    List<WikiPage> children = getChildren(page);
	    for (WikiPage child : children) {
	      addTestPagesToSuite(suite, child, suiteQuery);
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

	  private static boolean shouldBePartOfSuite(WikiPage context, Set<String> suiteQuery) throws Exception {
	    PageData data = context.getData();
	    boolean pruned = data.hasAttribute(PageData.PropertyPRUNE);
	    boolean test = data.hasAttribute("Test");
	    return !pruned && test && (belongsToSuite(context, suiteQuery));
	  }

	  private static boolean belongsToSuite(WikiPage context, Set<String> suiteQuery) {
	    return !exists(suiteQuery) || testMatchesQuery(context, suiteQuery);
	  }

	  private static boolean testMatchesQuery(WikiPage context, Set<String> suiteQuery) {
	    String testTagString = getTestTags(context);
	    return (testTagString != null && testTagsMatchQueryTags(testTagString, suiteQuery));
	  }

	  private static boolean exists(Set<String> suiteQuery) {
	    return (suiteQuery != null) && (suiteQuery.size() != 0);
	  }

	  private static String getTestTags(WikiPage context) {
	    try {
	      return context.getData().getAttribute(PageData.PropertySUITES);
	    } catch (Exception e) {
	      e.printStackTrace();
	      return null;
	    }
	  }

	  private static boolean testTagsMatchQueryTags(String testTagString, Set<String> suiteQuery) {
	    String testTags[] = testTagString.trim().split("\\s*,\\s*");
	    for (String testTag : testTags) {
	      if (testTagMatchesQueryTags(testTag.trim(), suiteQuery)) return true;
	    }
	    return false;
	  }

	  private static boolean testTagMatchesQueryTags(String testTag, Set<String> queryTags) {
	    for (String queryTag : queryTags) {
	      if (testTag.equalsIgnoreCase(queryTag)) {
	        return true;
	      }
	    }
	    return false;
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
