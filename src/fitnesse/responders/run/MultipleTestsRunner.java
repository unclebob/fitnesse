package fitnesse.responders.run;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fitnesse.FitNesseContext;
import fitnesse.components.ClassPathBuilder;
import fitnesse.html.SetupTeardownIncluder;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

/**
 * This test runs the list of tests that it is given, on slim or fit or multiple types of 
 * servers.  
 * 
 * @author Clare McLennan
 */

public class MultipleTestsRunner implements TestSystemListener{
  
  private final ResultsListener resultsListener;
  private final FitNesseContext fitNesseContext;
  private final WikiPage page;
  private final List<WikiPage> testPagesToRun;
  private boolean isFastTest = false;

  private LinkedList<WikiPage> processingQueue = new LinkedList<WikiPage>();
  private WikiPage currentTest = null;
  
  private TestSystemGroup testSystemGroup = null;

  public MultipleTestsRunner(final List<WikiPage> testPagesToRun, 
                             final FitNesseContext fitNesseContext,
                             final WikiPage page,
                             final ResultsListener resultsListener) {
    this.testPagesToRun = testPagesToRun;
    this.resultsListener = resultsListener;
    this.page = page;
    this.fitNesseContext = fitNesseContext;
  }
  
  /**
   * Fast test must be called before you run execute test pages
   * @param isFastTest
   */
  public void setFastTest(boolean isFastTest) {
    this.isFastTest = isFastTest;
  }
  
  public void executeTestPages() throws Exception {
    synchronized (this) {
      testSystemGroup = new TestSystemGroup(fitNesseContext, page, this);
    }
    testSystemGroup.setFastTest(isFastTest);
    
    resultsListener.setExecutionLog(testSystemGroup.getExecutionLog());
    Map<TestSystem.Descriptor, LinkedList<WikiPage>> pagesByTestSystem = makeMapOfPagesByTestSystem(); 
    for (TestSystem.Descriptor descriptor : pagesByTestSystem.keySet()) {
      executePagesInTestSystem(descriptor, pagesByTestSystem);
    }
  }

  private void executePagesInTestSystem(TestSystem.Descriptor descriptor,
      Map<TestSystem.Descriptor, LinkedList<WikiPage>> pagesByTestSystem) throws Exception {
      List<WikiPage> pagesInTestSystem = pagesByTestSystem.get(descriptor);

      resultsListener.announceStartTestSystem(descriptor.testSystemName, descriptor.testRunner);
      startTestSystemAndExecutePages(descriptor, pagesInTestSystem);
  }

  private void startTestSystemAndExecutePages(TestSystem.Descriptor descriptor, List<WikiPage> testSystemPages) throws Exception {
    TestSystem testSystem = testSystemGroup.startTestSystem(descriptor, buildClassPath());
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


  Map<TestSystem.Descriptor, LinkedList<WikiPage>> makeMapOfPagesByTestSystem() throws Exception {
    Map<TestSystem.Descriptor, LinkedList<WikiPage>> map = new HashMap<TestSystem.Descriptor, LinkedList<WikiPage>>();
  //  List<WikiPage> pages = getAllPagesToRunForThisSuite();
    for (WikiPage testPage : testPagesToRun) {
      TestSystem.Descriptor descriptor = TestSystem.getDescriptor(testPage.getData());
      List<WikiPage> pagesForTestSystem = getPagesForTestSystem(map, descriptor);
      pagesForTestSystem.add(testPage);
    }
    return map;
  }
  
  private List<WikiPage> getPagesForTestSystem(Map<TestSystem.Descriptor, LinkedList<WikiPage>> map, TestSystem.Descriptor descriptor) {
    LinkedList<WikiPage> listInMap;
    if (map.containsKey(descriptor))
      listInMap = map.get(descriptor);
    else {
      listInMap = new LinkedList<WikiPage>();
      map.put(descriptor, listInMap);
    }
    return listInMap;
  }
  
  public String buildClassPath() throws Exception {
    
    final ClassPathBuilder classPathBuilder = new ClassPathBuilder();
    final String pathSeparator = classPathBuilder.getPathSeparator(page);
    List<String> classPathElements = new ArrayList<String>();
    Set<WikiPage> visitedPages = new HashSet<WikiPage>();

    for (WikiPage testPage : testPagesToRun) {
      addClassPathElements(testPage, classPathElements, visitedPages);
    }
    
    return classPathBuilder.createClassPathString(classPathElements, pathSeparator);
  }

  private void addClassPathElements(WikiPage page, List<String> classPathElements, Set<WikiPage> visitedPages)
  throws Exception {
    List<String> pathElements = new ClassPathBuilder().getInheritedPathElements(page, visitedPages);
    classPathElements.addAll(pathElements);
  }

  @Override
  public void acceptOutputFirst(String output) throws Exception {
    WikiPage firstInQueue = processingQueue.isEmpty() ? null : processingQueue.getFirst();
    boolean isNewTest = firstInQueue != null && firstInQueue != currentTest;
    if (isNewTest) {
      currentTest = firstInQueue;
      resultsListener.announceStartNewTest(currentTest);
    }
    resultsListener.processTestOutput(output);
  }

  @Override
  public void acceptResultsLast(TestSummary testSummary) throws Exception {
    WikiPage testPage = processingQueue.removeFirst();
    
    resultsListener.processTestResults(testPage, testSummary);
  }

  @Override
  public synchronized void exceptionOccurred(Throwable e) {
    System.err.println("MultipleTestsRunner.exceptionOcurred:" + e.getMessage());
    try {
      testSystemGroup.kill();
      resultsListener.errorOccured();
    }
    catch (Exception e1) {
      e1.printStackTrace();
    }
  }
}
