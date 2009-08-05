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
    this.wikiRootPage = root;
    this.suiteFilter = (suiteFilter != null) ? suiteFilter : SuiteFilter.MATCH_ALL;
    testPageList = new LinkedList();
  }

  public List<WikiPage> makePageListForSingleTest() throws Exception {
    testPageList = new LinkedList<WikiPage>();

    testPageList.add(pageToRun);
    surroundGroupsOfTestPagesWithRespectiveSetUpAndTearDowns();

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

  private void surroundGroupsOfTestPagesWithRespectiveSetUpAndTearDowns() throws Exception {
    Map<String, LinkedList<WikiPage>> pageSetUpTearDownGroups = new HashMap<String, LinkedList<WikiPage>>();
    createPageSetUpTearDownGroups(pageSetUpTearDownGroups);
    testPageList.clear();
    reinsertPagesViaSetUpTearDownGroups(pageSetUpTearDownGroups);
  }

  private void createPageSetUpTearDownGroups(Map<String, LinkedList<WikiPage>> pageSetUpTearDownGroups) throws Exception {
    for (WikiPage page : testPageList) {
      makeSetUpTearDownPageGroupForPage(page, pageSetUpTearDownGroups);
    }
  }

  private void makeSetUpTearDownPageGroupForPage(WikiPage page, Map<String, LinkedList<WikiPage>> pageSetUpTearDownGroups) throws Exception {
    String group = getSetUpTearDownGroup(page);
    LinkedList<WikiPage> pageGroup;
    if (pageSetUpTearDownGroups.get(group) != null) {
      pageGroup = pageSetUpTearDownGroups.get(group);
      pageGroup.add(page);
    } else {
      pageGroup = new LinkedList<WikiPage>();
      pageGroup.add(page);
      pageSetUpTearDownGroups.put(group, pageGroup);
    }
  }

  private String getSetUpTearDownGroup(WikiPage page) throws Exception {
    String setUpPath = getPathForSetUpTearDown(page, SUITE_SETUP_NAME);
    String tearDownPath = getPathForSetUpTearDown(page, SUITE_TEARDOWN_NAME);
    return setUpPath + "," + tearDownPath;
  }

  private String getPathForSetUpTearDown(WikiPage page, String setUpTearDownName) throws Exception {
    String path = null;
    WikiPage suiteSetUpTearDown = PageCrawlerImpl.getClosestInheritedPage(setUpTearDownName, page);
    if (suiteSetUpTearDown != null)
      path = suiteSetUpTearDown.getPageCrawler().getFullPath(suiteSetUpTearDown).toString();
    return path;
  }

   private void reinsertPagesViaSetUpTearDownGroups(Map<String, LinkedList<WikiPage>> pageSetUpTearDownGroups) throws Exception {
    Set groups = pageSetUpTearDownGroups.keySet();
     for (Object group : groups) {
      String setUpAndTearDownGroupKey = group.toString();
      LinkedList<WikiPage> pageGroup = pageSetUpTearDownGroups.get(group);
      insertSetUpTearDownPageGroup(setUpAndTearDownGroupKey, pageGroup);
    }
  }

  private void insertSetUpTearDownPageGroup(String setUpAndTearDownGroupKey, LinkedList<WikiPage> pageGroup) throws Exception {
    insertSetUpForThisGroup(setUpAndTearDownGroupKey);
    insertPagesOfThisGroup(pageGroup);
    insertTearDownForThisGroup(setUpAndTearDownGroupKey);
  }

  private void insertSetUpForThisGroup(String setUpAndTearDown) throws Exception {
    String setUpPath = setUpAndTearDown.split(",")[0];
    WikiPage setUpPage = wikiRootPage.getPageCrawler().getPage(wikiRootPage, PathParser.parse(setUpPath));
    if (setUpPage != null)
      testPageList.add(setUpPage);
  }
  private void insertPagesOfThisGroup(LinkedList<WikiPage> pageGroup) {
    for (WikiPage page : pageGroup)
      testPageList.add(page);
  }

  private void insertTearDownForThisGroup(String setUpAndTearDownGroupKey) throws Exception {
    String tearDownPath = setUpAndTearDownGroupKey.split(",")[1];
    WikiPage tearDownPage = wikiRootPage.getPageCrawler().getPage(wikiRootPage, PathParser.parse(tearDownPath));
    if (tearDownPage != null)
    testPageList.add(tearDownPage);
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
    surroundGroupsOfTestPagesWithRespectiveSetUpAndTearDowns();
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
