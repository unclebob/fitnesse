// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

import util.XmlUtil;
import fitnesse.components.ClassPathBuilder;
import fitnesse.html.HtmlTag;
import fitnesse.html.SetupTeardownIncluder;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageCrawlerImpl;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.VirtualCouplingExtension;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageDummy;
import fitnesse.wiki.WikiPagePath;

public class SuiteResponder extends TestResponder implements TestSystemListener {
  public static final String SUITE_SETUP_NAME = "SuiteSetUp";
  public static final String SUITE_TEARDOWN_NAME = "SuiteTearDown";

  private LinkedList<WikiPage> processingQueue = new LinkedList<WikiPage>();
  private WikiPage currentTest = null;
  private SuiteHtmlFormatter suiteFormatter;
  private TestSummary xmlPageCounts = new TestSummary();

  protected HtmlTag addSummaryPlaceHolder() {
    HtmlTag testSummaryDiv = new HtmlTag("div", "Running Tests ...");
    testSummaryDiv.addAttribute("id", "test-summary");

    return testSummaryDiv;
  }

  protected void finishSending() throws Exception {
  }

  protected String buildClassPath() throws Exception {
    List<WikiPage> testPages = makePageList();
    return buildClassPath(testPages, page);
  }

  protected void performExecution() throws Exception {
    executeTestPages();
    if (response.isXmlFormat()) {
      addFinalCounts();
    }
    completeResponse();
  }

  private void addFinalCounts() throws Exception {
    Element finalCounts = testResultsDocument.createElement("finalCounts");
    testResultsElement.appendChild(finalCounts);
    XmlUtil.addTextNode(testResultsDocument, finalCounts, "right", Integer.toString(xmlPageCounts.right));
    XmlUtil.addTextNode(testResultsDocument, finalCounts, "wrong", Integer.toString(xmlPageCounts.wrong));
    XmlUtil.addTextNode(testResultsDocument, finalCounts, "ignores", Integer.toString(xmlPageCounts.ignores));
    XmlUtil.addTextNode(testResultsDocument, finalCounts, "exceptions", Integer.toString(xmlPageCounts.exceptions));
  }

  private void executeTestPages() throws Exception {
    Map<TestSystem.Descriptor, LinkedList<WikiPage>> pagesByTestSystem;
    String suiteQuery = getSuiteQuery();
    pagesByTestSystem = makeMapOfPagesByTestSystem(page, root, suiteQuery);
    for (TestSystem.Descriptor descriptor : pagesByTestSystem.keySet())
      executePagesInTestSystem(descriptor, pagesByTestSystem);
  }

  private void executePagesInTestSystem(TestSystem.Descriptor descriptor,
                                        Map<TestSystem.Descriptor, LinkedList<WikiPage>> pagesByTestSystem) throws Exception {
    List<WikiPage> pagesInTestSystem = pagesByTestSystem.get(descriptor);
    announceTestSystem(String.format("%s:%s", descriptor.testSystemName, descriptor.testRunner));
    startTestSystemAndExecutePages(descriptor, pagesInTestSystem);
  }

  private void announceTestSystem(String testSystemName) throws Exception {
    if (response.isHtmlFormat()) {
      suiteFormatter.announceTestSystem(testSystemName);
      addToResponse(suiteFormatter.getTestSystemHeader(testSystemName));
    }
  }

  private void startTestSystemAndExecutePages(TestSystem.Descriptor descriptor, List<WikiPage> testSystemPages) throws Exception {
    TestSystem testSystem = testSystemGroup.startTestSystem(descriptor, classPath);
    if (testSystem.isSuccessfullyStarted()) {
      executeTestSystemPages(testSystemPages, testSystem);
      waitForTestSystemToSendResults();
    } else {
      throw new Exception("Test system not started");
    }
    testSystem.bye();
  }

  private void executeTestSystemPages(List<WikiPage> pagesInTestSystem, TestSystem testSystem) throws Exception {
    for (WikiPage testPage : pagesInTestSystem) {
      processingQueue.addLast(testPage);
      PageData pageData = testPage.getData();
      SetupTeardownIncluder.includeInto(pageData);
      testSystem.runTestsAndGenerateHtml(pageData);
    }
  }

  private void waitForTestSystemToSendResults() throws InterruptedException {
    while (processingQueue.size() > 0)
      Thread.sleep(50);
  }

  protected void close() throws Exception {
    response.add(suiteFormatter.testOutput());
    response.add(suiteFormatter.tail());
    response.closeChunks();
    response.addTrailingHeader("Exit-Code", String.valueOf(exitCode()));
    response.closeTrailer();
    response.close();
  }

  public void acceptOutputFirst(String output) throws Exception {
    WikiPage firstInLine = processingQueue.isEmpty() ? null : processingQueue.getFirst();
    boolean isNewTest = firstInLine != null && firstInLine != currentTest;
    if (isNewTest) {
      currentTest = firstInLine;
      outputHeader(firstInLine);
    }
    if (response.isXmlFormat()) {
      super.acceptOutputFirst(output);
    } else if (response.isHtmlFormat()) {
      suiteFormatter.acceptOutput(output);
    }
  }

  private void outputHeader(WikiPage firstInLine) throws Exception {
    if (response.isHtmlFormat()) {
      PageCrawler pageCrawler = page.getPageCrawler();
      String relativeName = pageCrawler.getRelativeName(page, firstInLine);
      WikiPagePath fullPath = pageCrawler.getFullPath(firstInLine);
      String fullPathName = PathParser.render(fullPath);
      suiteFormatter.startOutputForNewTest(relativeName, fullPathName);
    }
  }

  public void acceptResultsLast(TestSummary testSummary) throws Exception {
    PageCrawler pageCrawler = page.getPageCrawler();
    WikiPage testPage = processingQueue.removeFirst();
    String relativeName = pageCrawler.getRelativeName(page, testPage);
    if ("".equals(relativeName))
      relativeName = String.format("(%s)", testPage.getName());
    if (response.isXmlFormat()) {
      addTestResultsToXmlDocument(testSummary, relativeName);
      xmlPageCounts.tallyPageCounts(testSummary);
    } else {
      assertionCounts.tally(testSummary);
      addToResponse(suiteFormatter.acceptResults(relativeName, testSummary));
    }
  }

  protected void makeFormatter() throws Exception {
    suiteFormatter = new SuiteHtmlFormatter(html);
    formatter = suiteFormatter;
  }

  protected String pageType() {
    return "Suite Results";
  }

  public static String buildClassPath(List<WikiPage> testPages, WikiPage page) throws Exception {
    final ClassPathBuilder classPathBuilder = new ClassPathBuilder();
    final String pathSeparator = classPathBuilder.getPathSeparator(page);
    List<String> classPathElements = new ArrayList<String>();
    Set<WikiPage> visitedPages = new HashSet<WikiPage>();

    for (WikiPage testPage : testPages)
      addClassPathElements(testPage, classPathElements, visitedPages);

    return classPathBuilder.createClassPathString(classPathElements, pathSeparator);
  }

  private static void addClassPathElements(WikiPage page, List<String> classPathElements, Set<WikiPage> visitedPages)
    throws Exception {
    List<String> pathElements = new ClassPathBuilder().getInheritedPathElements(page, visitedPages);
    classPathElements.addAll(pathElements);
  }

  public List<WikiPage> makePageList() throws Exception {
    return makePageList(page, root, getSuiteQuery());
  }

  private String getSuiteQuery() {
    if (request != null) {
      String queryString = (String) request.getInput("suiteFilter");
      return "".equals(queryString) ? null : queryString;
    }
    return null;
  }

  public static List<WikiPage> makePageList(WikiPage suitePage, WikiPage root, String suiteQueryString) throws Exception {
    LinkedList<WikiPage> pages = getAllPagesToRunForThisSuite(suitePage, root, suiteQueryString);

    if (suitePage.getData().hasAttribute("Test"))
      pages.add(suitePage);

    addSetupAndTeardown(suitePage, pages);

    if (pages.isEmpty()) {
      String name = new WikiPagePath(suitePage).toString();
      WikiPageDummy dummy = new WikiPageDummy("",
        "|Comment|\n|No test found with suite query '" + suiteQueryString + "' in subwiki !-" + name + "-!!|\n"
      );
      dummy.setParent(root);
      pages.add(dummy);
    }
    return pages;
  }

  private static LinkedList<WikiPage> getAllPagesToRunForThisSuite(WikiPage suitePage, WikiPage root, String suiteQueryString)
    throws Exception {
    Set<String> suiteQuery = new HashSet<String>();
    if (suiteQueryString != null)
      suiteQuery.addAll(Arrays.asList(suiteQueryString.split("\\s*,\\s*")));
    LinkedList<WikiPage> pages = getAllTestPagesUnder(suitePage, suiteQuery);
    List<WikiPage> referencedPages = gatherCrossReferencedTestPages(suitePage, root);
    pages.addAll(referencedPages);
    return pages;
  }

  private static void addSetupAndTeardown(WikiPage suitePage, LinkedList<WikiPage> pages) throws Exception {
    WikiPage suiteSetUp = PageCrawlerImpl.getInheritedPage(SUITE_SETUP_NAME, suitePage);
    if (suiteSetUp != null) {
      if (pages.contains(suiteSetUp))
        pages.remove(suiteSetUp);
      pages.addFirst(suiteSetUp);
    }
    WikiPage suiteTearDown = PageCrawlerImpl.getInheritedPage(SUITE_TEARDOWN_NAME, suitePage);
    if (suiteTearDown != null) {
      if (pages.contains(suiteTearDown))
        pages.remove(suiteTearDown);
      pages.addLast(suiteTearDown);
    }
  }

  public static LinkedList<WikiPage> getAllTestPagesUnder(WikiPage suiteRoot) throws Exception {
    return getAllTestPagesUnder(suiteRoot, null);
  }

  public static LinkedList<WikiPage> getAllTestPagesUnder(WikiPage suiteRoot, Set<String> suiteQuery) throws Exception {
    LinkedList<WikiPage> testPages = new LinkedList<WikiPage>();
    addTestPagesToSuite(testPages, suiteRoot, suiteQuery);

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
      suiteQuery = new HashSet<String>();

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

  public static List<WikiPage> gatherCrossReferencedTestPages(WikiPage testPage, WikiPage root) throws Exception {
    LinkedList<WikiPage> pages = new LinkedList<WikiPage>();
    PageData data = testPage.getData();
    List<String> pageReferences = data.getXrefPages();
    PageCrawler crawler = testPage.getPageCrawler();
    WikiPagePath testPagePath = crawler.getFullPath(testPage);
    WikiPage parent = crawler.getPage(root, testPagePath.parentPath());
    for (String pageReference : pageReferences) {
      WikiPagePath path = PathParser.parse(pageReference);
      WikiPage referencedPage = crawler.getPage(parent, path);
      if (referencedPage != null)
        pages.add(referencedPage);
    }
    return pages;
  }

  public static Map<TestSystem.Descriptor, LinkedList<WikiPage>>
  makeMapOfPagesByTestSystem(WikiPage suitePage, WikiPage root, String suiteQuery) throws Exception {
    Map<TestSystem.Descriptor, LinkedList<WikiPage>> map = new HashMap<TestSystem.Descriptor, LinkedList<WikiPage>>();
    List<WikiPage> pages = getAllPagesToRunForThisSuite(suitePage, root, suiteQuery);
    for (WikiPage page : pages) {
      TestSystem.Descriptor descriptor = TestSystem.getDescriptor(page.getData());
      List<WikiPage> pagesForTestSystem = getPagesForTestSystem(map, descriptor);
      pagesForTestSystem.add(page);
    }

    for (LinkedList<WikiPage> pagesForTestSystem : map.values()) {
      addSetupAndTeardown(suitePage, pagesForTestSystem);
    }
    return map;
  }

  private static List<WikiPage> getPagesForTestSystem(Map<TestSystem.Descriptor, LinkedList<WikiPage>> map, TestSystem.Descriptor descriptor) {
    LinkedList<WikiPage> listInMap;
    if (map.containsKey(descriptor))
      listInMap = map.get(descriptor);
    else {
      listInMap = new LinkedList<WikiPage>();
      map.put(descriptor, listInMap);
    }
    return listInMap;
  }
}
