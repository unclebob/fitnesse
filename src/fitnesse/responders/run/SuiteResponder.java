// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.run;

import fitnesse.components.ClassPathBuilder;
import fitnesse.html.HtmlTag;
import fitnesse.html.SetupTeardownIncluder;
import fitnesse.util.XmlUtil;
import fitnesse.wiki.*;
import org.w3c.dom.Element;

import java.util.*;

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
    pagesByTestSystem = makeMapOfPagesByTestSystem(page, root, getSuiteFilter());
    for (TestSystem.Descriptor descriptor : pagesByTestSystem.keySet())
      executePagesInTestSystem(descriptor, pagesByTestSystem);
  }

  private void executePagesInTestSystem(TestSystem.Descriptor descriptor,
                                        Map<TestSystem.Descriptor, LinkedList<WikiPage>> pagesByTestSystem) throws Exception {
    List<WikiPage> pagesInTestSystem = pagesByTestSystem.get(descriptor);
    announceTestSystem(String.format("%s:%s",descriptor.testSystemName, descriptor.testRunner));
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

  private String extractRunnerFromTestSystemName(String testSystemName) {
    String testSystemComponents[] = testSystemName.split(":");
    String testRunner = testSystemComponents[testSystemComponents.length - 1];
    return testRunner;
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
    return makePageList(page, root, getSuiteFilter());
  }

  private String getSuiteFilter() {
    return request != null ? (String) request.getInput("suiteFilter") : null;
  }

  public static List<WikiPage> makePageList(WikiPage suitePage, WikiPage root, String suite) throws Exception {
    LinkedList<WikiPage> pages = getAllPagesToRunForThisSuite(suitePage, root, suite);
    if (suitePage.getData().hasAttribute("Test"))
      pages.add(suitePage);

    addSetupAndTeardown(suitePage, pages);

    if (pages.isEmpty()) {
      String name = new WikiPagePath(suitePage).toString();
      WikiPageDummy dummy = new WikiPageDummy("",
        "|Comment|\n|No test found with suite filter '" + suite + "' in subwiki !-" + name + "-!!|\n"
      );
      dummy.setParent(root);
      pages.add(dummy);
    }
    return pages;
  }

  private static LinkedList<WikiPage> getAllPagesToRunForThisSuite(WikiPage suitePage, WikiPage root, String suite)
    throws Exception {
    LinkedList<WikiPage> pages = getAllTestPagesUnder(suitePage, suite);
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

  public static LinkedList<WikiPage> getAllTestPagesUnder(WikiPage suiteRoot, String suite) throws Exception {
    LinkedList<WikiPage> testPages = new LinkedList<WikiPage>();
    addTestPagesToList(testPages, suiteRoot, suite);

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

  private static void addTestPagesToList(List<WikiPage> testPages, WikiPage context, String suite) throws Exception {
    PageData data = context.getData();
    if (!data.hasAttribute(PageData.PropertyPRUNE)) {
      if (data.hasAttribute("Test")) {
        if (belongsToSuite(context, suite)) {
          testPages.add(context);
        }
      }

      ArrayList<WikiPage> children = new ArrayList<WikiPage>();
      children.addAll(context.getChildren());
      if (context.hasExtension(VirtualCouplingExtension.NAME)) {
        VirtualCouplingExtension extension = (VirtualCouplingExtension) context.getExtension(
          VirtualCouplingExtension.NAME
        );
        children.addAll(extension.getVirtualCoupling().getChildren());
      }
      for (WikiPage page : children) {
        addTestPagesToList(testPages, page, suite);
      }
    }
  }

  private static boolean belongsToSuite(WikiPage context, String suite) {
    if ((suite == null) || (suite.trim().length() == 0)) {
      return true;
    }
    try {
      String suitesStr = context.getData().getAttribute(PageData.PropertySUITES);
      if (suitesStr != null) {
        StringTokenizer t = new StringTokenizer(suitesStr, ",");
        while (t.hasMoreTokens()) {
          if (t.nextToken().trim().equalsIgnoreCase(suite)) {
            return true;
          }
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
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
  makeMapOfPagesByTestSystem(WikiPage suitePage, WikiPage root, String suiteFilter) throws Exception {
    Map<TestSystem.Descriptor, LinkedList<WikiPage>> map = new HashMap<TestSystem.Descriptor, LinkedList<WikiPage>>();
    List<WikiPage> pages = getAllPagesToRunForThisSuite(suitePage, root, suiteFilter);
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
