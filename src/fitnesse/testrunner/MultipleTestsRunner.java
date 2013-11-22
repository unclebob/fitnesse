// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testrunner;

import fitnesse.FitNesseContext;
import fitnesse.wiki.ClassPathBuilder;
import fitnesse.testsystems.*;
import fitnesse.wiki.WikiPage;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MultipleTestsRunner implements TestSystemListener<WikiTestPage>, Stoppable {
  private static final Logger LOG = Logger.getLogger(MultipleTestsRunner.class.getName());

  private final CompositeFormatter formatters;
  private final FitNesseContext fitNesseContext;
  private final List<WikiPage> testPagesToRun;
  private boolean inProcess = false;
  private boolean remoteDebug = false;

  private final TestSystemFactory testSystemFactory;

  private volatile boolean isStopped = false;
  private String stopId = null;
  private PageListSetUpTearDownSurrounder surrounder;
  private TestSystem testSystem;

  private volatile int testsInProgressCount;

  public MultipleTestsRunner(final List<WikiPage> testPagesToRun,
                             final FitNesseContext fitNesseContext,
                             final TestSystemFactory testSystemFactory) {
    this.testPagesToRun = testPagesToRun;
    this.formatters = new CompositeFormatter();
    this.fitNesseContext = fitNesseContext;
    this.testSystemFactory = testSystemFactory;
    surrounder = new PageListSetUpTearDownSurrounder(fitNesseContext.root);
  }

  public void addTestSystemListener(TestSystemListener listener) {
    this.formatters.addTestSystemListener(listener);
  }

  public void setRemoteDebug(boolean isDebug) {
    remoteDebug = isDebug;
  }

  public void setInProcess(boolean inProcess) {
    this.inProcess = inProcess;
  }

  private TestSystem startTestSystem(Descriptor descriptor) throws IOException {
    testSystem = testSystemFactory.create(descriptor);
    testSystem.addTestSystemListener(this);
    testSystem.start();
    return testSystem;
  }

  public void executeTestPages() throws IOException, InterruptedException {
    internalExecuteTestPages();
    allTestingComplete();
  }

  void allTestingComplete() throws IOException {
    formatters.close();
  }

  private void internalExecuteTestPages() throws IOException, InterruptedException {
    stopId = fitNesseContext.runningTestingTracker.addStartedProcess(this);

    formatters.setTrackingId(stopId);
    PagesByTestSystem pagesByTestSystem = makeMapOfPagesByTestSystem();
    announceTotalTestsToRun(pagesByTestSystem);

    for (Map.Entry<WikiPageDescriptor, LinkedList<WikiTestPage>> PagesByTestSystem : pagesByTestSystem.entrySet()) {
      startTestSystemAndExecutePages(PagesByTestSystem.getKey(), PagesByTestSystem.getValue());
    }

    fitNesseContext.runningTestingTracker.removeEndedProcess(stopId);
  }

  private void startTestSystemAndExecutePages(WikiPageDescriptor descriptor, List<WikiTestPage> testSystemPages) throws IOException, InterruptedException {
    TestSystem testSystem = null;
    try {
      if (!isStopped) {
        testSystem = startTestSystem(new WikiPageDescriptor(descriptor,
                new ClassPathBuilder().buildClassPath(testPagesToRun)));
      }

      if (testSystem != null && testSystem.isSuccessfullyStarted()) {
        executeTestSystemPages(testSystemPages, testSystem);
        waitForTestSystemToSendResults();
      }
    } finally {
      if (!isStopped && testSystem != null) {
        testSystem.bye();
      }
    }
  }

  private void executeTestSystemPages(List<WikiTestPage> pagesInTestSystem, TestSystem testSystem) throws IOException, InterruptedException {
    for (TestPage testPage : pagesInTestSystem) {
      testsInProgressCount++;
      testSystem.runTests(testPage);
    }
  }

  private void waitForTestSystemToSendResults() throws InterruptedException {
    // TODO: use testSystemStopped event to wait for tests to end.
    while (testsInProgressCount > 0 && isNotStopped())
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
    WikiTestPage testPage = new WikiTestPage(wikiPage);
    WikiPageDescriptor descriptor = new WikiPageDescriptor(wikiPage.readOnlyData(), inProcess, remoteDebug, "");
    getOrMakeListWithinMap(pagesByTestSystem, descriptor).add(testPage);
  }

  private LinkedList<WikiTestPage> getOrMakeListWithinMap(PagesByTestSystem pagesByTestSystem, WikiPageDescriptor descriptor) {
    LinkedList<WikiTestPage> pagesForTestSystem;
    if (!pagesByTestSystem.containsKey(descriptor)) {
      pagesForTestSystem = new LinkedList<WikiTestPage>();
      pagesByTestSystem.put(descriptor, pagesForTestSystem);
    } else {
      pagesForTestSystem = pagesByTestSystem.get(descriptor);
    }
    return pagesForTestSystem;
  }

  private PagesByTestSystem addSuiteSetUpAndTearDownToAllTestSystems(PagesByTestSystem pagesByTestSystem) {
    if (testPagesToRun.size() == 0)
      return pagesByTestSystem;
    for (LinkedList<WikiTestPage> pagesForTestSystem : pagesByTestSystem.values())
      surrounder.surroundGroupsOfTestPagesWithRespectiveSetUpAndTearDowns(pagesForTestSystem);

    return pagesByTestSystem;
  }

  void announceTotalTestsToRun(PagesByTestSystem pagesByTestSystem) {
    int tests = 0;
    for (LinkedList<WikiTestPage> listOfPagesToRun : pagesByTestSystem.values()) {
      tests += listOfPagesToRun.size();
    }
    formatters.announceNumberTestsToRun(tests);
  }

  @Override
  public void testSystemStarted(TestSystem testSystem) {
    formatters.testSystemStarted(testSystem);
  }

  @Override
  public void testOutputChunk(String output) throws IOException {
    formatters.testOutputChunk(output);
  }

  @Override
  public void testStarted(WikiTestPage testPage) throws IOException {
    formatters.testStarted(testPage);
  }

  @Override
  public void testComplete(WikiTestPage testPage, TestSummary testSummary) throws IOException {
    formatters.testComplete(testPage, testSummary);
    testsInProgressCount--;
  }

  @Override
  public void testSystemStopped(TestSystem testSystem, ExecutionLog executionLog, Throwable cause) {
    formatters.testSystemStopped(testSystem, executionLog, cause);

    if (cause != null) {
      stop();
    }
  }

  @Override
  public void testAssertionVerified(Assertion assertion, TestResult testResult) {
    formatters.testAssertionVerified(assertion, testResult);
  }

  @Override
  public void testExceptionOccurred(Assertion assertion, ExceptionResult exceptionResult) {
    formatters.testExceptionOccurred(assertion, exceptionResult);
  }

  private boolean isNotStopped() {
    return !isStopped;
  }

  @Override
  public void stop() {
    boolean wasNotStopped = isNotStopped();
    isStopped = true;
    if (stopId != null) {
      fitNesseContext.runningTestingTracker.removeEndedProcess(stopId);
    }

    if (wasNotStopped && testSystem != null) {
      try {
        testSystem.kill();
      } catch (IOException e) {
        LOG.log(Level.WARNING, "Unable to stop test systems", e);
      }
    }
  }
}

class PagesByTestSystem extends HashMap<WikiPageDescriptor, LinkedList<WikiTestPage>> {
  private static final long serialVersionUID = 1L;
}
