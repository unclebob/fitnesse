// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
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

public class MultipleTestsRunner implements TestSystemListener, Stoppable{
  
  private final ResultsListener resultsListener;
  private final FitNesseContext fitNesseContext;
  private final WikiPage page;
  private final List<WikiPage> testPagesToRun;
  private boolean isFastTest = false;

  private LinkedList<WikiPage> processingQueue = new LinkedList<WikiPage>();
  private WikiPage currentTest = null;
  
  private TestSystemGroup testSystemGroup = null;
  private TestSystem currentTestSystem = null;
  private boolean isStopped = false;
  private String stopId = null;

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
  
  public void executeTestPages() {
    try {
      internalExecuteTestPages();
    }
    catch (Exception exception) {
      exceptionOccurred(exception);
    }  
  }
  
  private void internalExecuteTestPages() throws Exception {
    synchronized (this) {
      testSystemGroup = new TestSystemGroup(fitNesseContext, page, this);
      stopId = fitNesseContext.runningTestingTracker.addStartedProcess(this);
    }
    testSystemGroup.setFastTest(isFastTest);
    
    resultsListener.setExecutionLogAndTrackingId(stopId, testSystemGroup.getExecutionLog());
    Map<TestSystem.Descriptor, LinkedList<WikiPage>> pagesByTestSystem = makeMapOfPagesByTestSystem(); 
    for (TestSystem.Descriptor descriptor : pagesByTestSystem.keySet()) {
      executePagesInTestSystem(descriptor, pagesByTestSystem);
    }
    fitNesseContext.runningTestingTracker.removeEndedProcess(stopId);
  }

  private void executePagesInTestSystem(TestSystem.Descriptor descriptor,
      Map<TestSystem.Descriptor, LinkedList<WikiPage>> pagesByTestSystem) throws Exception {
      List<WikiPage> pagesInTestSystem = pagesByTestSystem.get(descriptor);

      startTestSystemAndExecutePages(descriptor, pagesInTestSystem);
  }

  private void startTestSystemAndExecutePages(TestSystem.Descriptor descriptor, List<WikiPage> testSystemPages) throws Exception {
    TestSystem testSystem = null;
    synchronized (this) {
      if (!isStopped) {
        currentTestSystem = testSystemGroup.startTestSystem(descriptor, buildClassPath());
        testSystem = currentTestSystem;
        resultsListener.announceStartTestSystem(testSystem, descriptor.testSystemName, descriptor.testRunner);
      }
    }
    if (testSystem != null) {
      if (testSystem.isSuccessfullyStarted()) {
        executeTestSystemPages(testSystemPages, testSystem);
        waitForTestSystemToSendResults();
      } else {
        throw new Exception("Test system not started");
      }
      
      synchronized (this) {
        if (!isStopped) {
          testSystem.bye();
        }
        currentTestSystem = null;
      }
    }
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
    while ((processingQueue.size() > 0) && isNotStopped())
      Thread.sleep(50);
  }
  
  Map<TestSystem.Descriptor, LinkedList<WikiPage>> makeMapOfPagesByTestSystem() throws Exception {
    Map<TestSystem.Descriptor, LinkedList<WikiPage>> map = new HashMap<TestSystem.Descriptor, LinkedList<WikiPage>>();
    
    Map<WikiPage, TestSystem.Descriptor> pageToSystemMap = new HashMap<WikiPage, TestSystem.Descriptor>(testPagesToRun.size());
   
    for (WikiPage testPage : testPagesToRun) {
      TestSystem.Descriptor descriptor = TestSystem.getDescriptor(testPage.getData());
      pageToSystemMap.put(testPage, descriptor);
      getPagesForTestSystem(map, descriptor);
    }

    for (WikiPage testPage : testPagesToRun) {
      if (SuiteContentsFinder.isSuiteSetupOrTearDown(testPage)) {
        // add to all test systems
        for (TestSystem.Descriptor descriptor : map.keySet()) {
          List<WikiPage> pagesForTestSystem = getPagesForTestSystem(map, descriptor);
          pagesForTestSystem.add(testPage);
        }
      }
      else {
        // add only to the system for running the page
        List<WikiPage> pagesForTestSystem = getPagesForTestSystem(map, pageToSystemMap.get(testPage));
        pagesForTestSystem.add(testPage);
      }
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

  public void acceptOutputFirst(String output) throws Exception {
    WikiPage firstInQueue = processingQueue.isEmpty() ? null : processingQueue.getFirst();
    boolean isNewTest = firstInQueue != null && firstInQueue != currentTest;
    if (isNewTest) {
      currentTest = firstInQueue;
      resultsListener.announceStartNewTest(currentTest);
    }
    resultsListener.processTestOutput(output);
  }

  public void acceptResultsLast(TestSummary testSummary) throws Exception {
    WikiPage testPage = processingQueue.removeFirst();
    
    resultsListener.processTestResults(testPage, testSummary);
  }

  public synchronized void exceptionOccurred(Throwable e) {
    try {
      resultsListener.errorOccured();
      stop();
    }
    catch (Exception e1) {
      if (isNotStopped()) {
        e1.printStackTrace();
      }
    }
  }
  
  private synchronized boolean isNotStopped() {
    return !isStopped;
  }
  
  public void stop() throws Exception {
    boolean wasNotStopped = isNotStopped();
    synchronized (this) {
      isStopped = true;
      if (stopId != null) {
        fitNesseContext.runningTestingTracker.removeEndedProcess(stopId);
      }
    }
    
    if (wasNotStopped) {
      testSystemGroup.kill();
    }
  }
}
