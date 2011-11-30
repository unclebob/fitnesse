// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.FitNesseContext;
import fitnesse.components.ClassPathBuilder;
import fitnesse.html.SetupTeardownAndLibraryIncluder;
import fitnesse.responders.run.TestSystem.Descriptor;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

import java.util.*;

import util.TimeMeasurement;

public class MultipleTestsRunner implements TestSystemListener, Stoppable {

  private final ResultsListener resultsListener;
  private final FitNesseContext fitNesseContext;
  private final WikiPage page;
  private final List<WikiPage> testPagesToRun;
  private boolean isFastTest = false;
  private boolean isRemoteDebug = false;

  private LinkedList<TestPage> processingQueue = new LinkedList<TestPage>();
  private TestPage currentTest = null;

  private TestSystemGroup testSystemGroup = null;
  private TestSystem currentTestSystem = null;
  private boolean isStopped = false;
  private String stopId = null;
  private PageListSetUpTearDownSurrounder surrounder;
  TimeMeasurement currentTestTime, totalTestTime;
  
  public MultipleTestsRunner(final List<WikiPage> testPagesToRun,
                             final FitNesseContext fitNesseContext,
                             final WikiPage page,
                             final ResultsListener resultsListener) {
    this.testPagesToRun = testPagesToRun;
    this.resultsListener = resultsListener;
    this.page = page;
    this.fitNesseContext = fitNesseContext;
    surrounder = new PageListSetUpTearDownSurrounder(fitNesseContext.root);
  }

  public void setDebug(boolean isDebug) {
    isRemoteDebug = isDebug;
  }

  public void setFastTest(boolean isFastTest) {
    this.isFastTest = isFastTest;
  }

  public void executeTestPages() {
    try {
      internalExecuteTestPages();
      allTestingComplete();
    }
    catch (Exception exception) {
      //hoped to write exceptions to log file but will take some work.
      exception.printStackTrace(System.out);
      exceptionOccurred(exception);
    }
  }

  void allTestingComplete() throws Exception {
    TimeMeasurement completionTimeMeasurement = new TimeMeasurement().start();
    resultsListener.allTestingComplete(totalTestTime.stop());
    completionTimeMeasurement.stop(); // a non-trivial amount of time elapses here
  }

  private void internalExecuteTestPages() throws Exception {
    synchronized (this) {
      testSystemGroup = new TestSystemGroup(fitNesseContext, page, this);
      stopId = fitNesseContext.runningTestingTracker.addStartedProcess(this);
    }
    testSystemGroup.setFastTest(isFastTest);
    testSystemGroup.setManualStart(useManualStartForTestSystem());

    resultsListener.setExecutionLogAndTrackingId(stopId, testSystemGroup.getExecutionLog());
    PagesByTestSystem pagesByTestSystem = makeMapOfPagesByTestSystem();
    announceTotalTestsToRun(pagesByTestSystem);
    for (TestSystem.Descriptor descriptor : pagesByTestSystem.keySet()) {
      executePagesInTestSystem(descriptor, pagesByTestSystem);
    }
    fitNesseContext.runningTestingTracker.removeEndedProcess(stopId);
  }
  
  private boolean useManualStartForTestSystem() {
    if (isRemoteDebug) {
      try {
        String useManualStart = page.getData().getVariable("MANUALLY_START_TEST_RUNNER_ON_DEBUG");
        return (useManualStart != null && useManualStart.toLowerCase().equals("true"));
      } 
      catch (Exception e) {
          throw new RuntimeException(e);
      }
    }
    return false;
  }

  private void executePagesInTestSystem(TestSystem.Descriptor descriptor,
                                        PagesByTestSystem pagesByTestSystem) throws Exception {
    List<TestPage> pagesInTestSystem = pagesByTestSystem.get(descriptor);

    startTestSystemAndExecutePages(descriptor, pagesInTestSystem);
  }

  private void startTestSystemAndExecutePages(TestSystem.Descriptor descriptor, List<TestPage> testSystemPages) throws Exception {
    TestSystem testSystem = null;
    synchronized (this) {
      if (!isStopped) {
        currentTestSystem = testSystemGroup.startTestSystem(descriptor, buildClassPath());
        testSystem = currentTestSystem;
        resultsListener.testSystemStarted(testSystem, descriptor.testSystemName, descriptor.testRunner);
      } else {
      }
    }
    if (testSystem != null) {
      if (testSystem.isSuccessfullyStarted()) {
        executeTestSystemPages(testSystemPages, testSystem);
        waitForTestSystemToSendResults();
      } else {
      }

      synchronized (this) {
        if (!isStopped) {
          testSystem.bye();
        }
        currentTestSystem = null;
      }
    } else {
    }
  }

  private void executeTestSystemPages(List<TestPage> pagesInTestSystem, TestSystem testSystem) throws Exception {
    for (TestPage testPage : pagesInTestSystem) {
      addToProcessingQueue(testPage);
      SetupTeardownAndLibraryIncluder.includeSetupsTeardownsAndLibrariesBelowTheSuite(testPage, page);
      testSystem.runTestsAndGenerateHtml(testPage.getDecoratedData());
    }
  }

  void addToProcessingQueue(TestPage testPage) {
    processingQueue.addLast(testPage);
  }

  private void waitForTestSystemToSendResults() throws InterruptedException {
    while ((processingQueue.size() > 0) && isNotStopped())
      Thread.sleep(50);
  }

  PagesByTestSystem makeMapOfPagesByTestSystem() throws Exception {
    return addSuiteSetUpAndTearDownToAllTestSystems(mapWithAllPagesButSuiteSetUpAndTearDown());
  }

  private PagesByTestSystem mapWithAllPagesButSuiteSetUpAndTearDown() throws Exception {
    PagesByTestSystem pagesByTestSystem = new PagesByTestSystem();

    for (WikiPage testPage : testPagesToRun) {
      if (!SuiteContentsFinder.isSuiteSetupOrTearDown(testPage)) {
        addPageToListWithinMap(pagesByTestSystem, testPage);
      }
    }
    return pagesByTestSystem;
  }

  private void addPageToListWithinMap(PagesByTestSystem pagesByTestSystem, WikiPage wikiPage) throws Exception {
    TestPage testPage = new TestPage(wikiPage);
    Descriptor descriptor = TestSystem.getDescriptor(testPage.getData(), isRemoteDebug);
    getOrMakeListWithinMap(pagesByTestSystem, descriptor).add(testPage);
  }

  private LinkedList<TestPage> getOrMakeListWithinMap(PagesByTestSystem pagesByTestSystem, Descriptor descriptor) {
    LinkedList<TestPage> pagesForTestSystem;
    if (!pagesByTestSystem.containsKey(descriptor)) {
      pagesForTestSystem = new LinkedList<TestPage>();
      pagesByTestSystem.put(descriptor, pagesForTestSystem);
    } else {
      pagesForTestSystem = pagesByTestSystem.get(descriptor);
    }
    return pagesForTestSystem;
  }

  private PagesByTestSystem addSuiteSetUpAndTearDownToAllTestSystems(PagesByTestSystem pagesByTestSystem) throws Exception {
    if (testPagesToRun.size() == 0)
      return pagesByTestSystem;
    for (LinkedList<TestPage> pagesForTestSystem : pagesByTestSystem.values())
      surrounder.surroundGroupsOfTestPagesWithRespectiveSetUpAndTearDowns(pagesForTestSystem);

    return pagesByTestSystem;
  }

  void announceTotalTestsToRun(PagesByTestSystem pagesByTestSystem) {
    int tests = 0;
    for (LinkedList<TestPage> listOfPagesToRun : pagesByTestSystem.values()) {
      tests += listOfPagesToRun.size();
    }
    resultsListener.announceNumberTestsToRun(tests);
    totalTestTime = new TimeMeasurement().start();
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
    TestPage firstInQueue = processingQueue.isEmpty() ? null : processingQueue.getFirst();
    boolean isNewTest = firstInQueue != null && firstInQueue != currentTest;
    if (isNewTest) {
      startingNewTest(firstInQueue);
    }
    resultsListener.testOutputChunk(output);
  }

  void startingNewTest(TestPage test) throws Exception {
    currentTest = test;
    currentTestTime = new TimeMeasurement().start();
    resultsListener.newTestStarted(currentTest, currentTestTime);
  }
  
  public void testComplete(TestSummary testSummary) throws Exception {
    TestPage testPage = processingQueue.removeFirst();
    resultsListener.testComplete(testPage, testSummary, currentTestTime.stop());
  }

  public void exceptionOccurred(Throwable e) {
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

class PagesByTestSystem extends HashMap<TestSystem.Descriptor, LinkedList<TestPage>> {
  private static final long serialVersionUID = 1L;
}
