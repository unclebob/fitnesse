// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fitnesse.FitNesseContext;
import fitnesse.components.ClassPathBuilder;
import fitnesse.testsystems.PageListSetUpTearDownSurrounder;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystem.Descriptor;
import fitnesse.testsystems.TestSystemGroup;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.testsystems.slim.results.ExceptionResult;
import fitnesse.testsystems.slim.results.TestResult;
import fitnesse.testsystems.slim.tables.Assertion;
import fitnesse.wiki.WikiPage;
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
  private volatile boolean isStopped = false;
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
    } catch (Exception exception) {
      //hoped to write exceptions to log file but will take some work.
      exception.printStackTrace(System.out);
      exceptionOccurred(exception);
    }
  }

  void allTestingComplete() throws IOException {
    TimeMeasurement completionTimeMeasurement = new TimeMeasurement().start();
    resultsListener.allTestingComplete(totalTestTime.stop());
    completionTimeMeasurement.stop(); // a non-trivial amount of time elapses here
  }

  private void internalExecuteTestPages() throws IOException, InterruptedException {
    testSystemGroup = new TestSystemGroup(fitNesseContext, page, this);
    stopId = fitNesseContext.runningTestingTracker.addStartedProcess(this);

    testSystemGroup.setFastTest(isFastTest);
    testSystemGroup.setManualStart(useManualStartForTestSystem());

    resultsListener.setExecutionLogAndTrackingId(stopId, testSystemGroup.getExecutionLog());
    PagesByTestSystem pagesByTestSystem = makeMapOfPagesByTestSystem();
    announceTotalTestsToRun(pagesByTestSystem);

    for (Map.Entry<TestSystem.Descriptor, LinkedList<TestPage>> PagesByTestSystem : pagesByTestSystem.entrySet()) {
      startTestSystemAndExecutePages(PagesByTestSystem.getKey(), PagesByTestSystem.getValue());
    }

    fitNesseContext.runningTestingTracker.removeEndedProcess(stopId);
  }

  private boolean useManualStartForTestSystem() {
    if (isRemoteDebug) {
      String useManualStart = page.readOnlyData().getVariable("MANUALLY_START_TEST_RUNNER_ON_DEBUG");
      return (useManualStart != null && useManualStart.toLowerCase().equals("true"));
    }
    return false;
  }

  private void startTestSystemAndExecutePages(TestSystem.Descriptor descriptor, List<TestPage> testSystemPages) throws IOException, InterruptedException {
    TestSystem testSystem = null;
    try {
      if (!isStopped) {
        testSystem = testSystemGroup.startTestSystem(descriptor,
                new ClassPathBuilder().buildClassPath(testPagesToRun));
        resultsListener.testSystemStarted(testSystem, descriptor.getTestSystem(), descriptor.getTestRunner());
      }

      if (testSystem != null) {
        if (testSystem.isSuccessfullyStarted()) {
          executeTestSystemPages(testSystemPages, testSystem);
          waitForTestSystemToSendResults();
        }
      }
    } finally {
      if (!isStopped && testSystem != null) {
        testSystem.bye();
      }
    }
  }

  private void executeTestSystemPages(List<TestPage> pagesInTestSystem, TestSystem testSystem) throws IOException, InterruptedException {
    for (TestPage testPage : pagesInTestSystem) {
      addToProcessingQueue(testPage);
      testSystem.runTests(testPage);
    }
  }

  void addToProcessingQueue(TestPage testPage) {
    processingQueue.addLast(testPage);
  }

  private void waitForTestSystemToSendResults() throws InterruptedException {
    while ((processingQueue.size() > 0) && isNotStopped())
      Thread.sleep(50);
  }

  PagesByTestSystem makeMapOfPagesByTestSystem() {
    return addSuiteSetUpAndTearDownToAllTestSystems(mapWithAllPagesButSuiteSetUpAndTearDown());
  }

  private PagesByTestSystem mapWithAllPagesButSuiteSetUpAndTearDown() {
    PagesByTestSystem pagesByTestSystem = new PagesByTestSystem();

    for (WikiPage testPage : testPagesToRun) {
      if (!SuiteContentsFinder.isSuiteSetupOrTearDown(testPage)) {
        addPageToListWithinMap(pagesByTestSystem, testPage);
      }
    }
    return pagesByTestSystem;
  }

  private void addPageToListWithinMap(PagesByTestSystem pagesByTestSystem, WikiPage wikiPage) {
    TestPage testPage = new TestPage(wikiPage);
    Descriptor descriptor = TestSystem.getDescriptor(wikiPage, fitNesseContext.pageFactory, isRemoteDebug);
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

  private PagesByTestSystem addSuiteSetUpAndTearDownToAllTestSystems(PagesByTestSystem pagesByTestSystem) {
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

  public void testOutputChunk(String output) throws IOException {
    TestPage firstInQueue = processingQueue.isEmpty() ? null : processingQueue.getFirst();
    boolean isNewTest = firstInQueue != null && firstInQueue != currentTest;
    if (isNewTest) {
      startingNewTest(firstInQueue);
    }
    resultsListener.testOutputChunk(output);
  }

  void startingNewTest(TestPage test) throws IOException {
    currentTest = test;
    currentTestTime = new TimeMeasurement().start();
    resultsListener.newTestStarted(currentTest, currentTestTime);
  }

  public void testComplete(TestSummary testSummary) throws IOException {
    TestPage testPage = processingQueue.removeFirst();
    resultsListener.testComplete(testPage, testSummary, currentTestTime.stop());
  }

  public void exceptionOccurred(Throwable e) {
    try {
      resultsListener.errorOccured();
      stop();
    } catch (Exception e1) {
      if (isNotStopped()) {
        e1.printStackTrace();
      }
    }
  }

  @Override
  public void testAssertionVerified(Assertion assertion, TestResult testResult) {
    resultsListener.testAssertionVerified(assertion, testResult);
  }

  @Override
  public void testExceptionOccurred(Assertion assertion, ExceptionResult exceptionResult) {
    resultsListener.testExceptionOccurred(assertion, exceptionResult);
  }

  private boolean isNotStopped() {
    return !isStopped;
  }

  public void stop() throws IOException {
    boolean wasNotStopped = isNotStopped();
    isStopped = true;
    if (stopId != null) {
      fitNesseContext.runningTestingTracker.removeEndedProcess(stopId);
    }

    if (wasNotStopped) {
      testSystemGroup.kill();
    }
  }
}

class PagesByTestSystem extends HashMap<TestSystem.Descriptor, LinkedList<TestPage>> {
  private static final long serialVersionUID = 1L;
}
