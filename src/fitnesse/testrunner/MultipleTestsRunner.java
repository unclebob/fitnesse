// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testrunner;

import fitnesse.wiki.ClassPathBuilder;
import fitnesse.testsystems.*;
import fitnesse.wiki.WikiPage;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MultipleTestsRunner implements TestSystemListener<WikiTestPage>, Stoppable {
  private static final Logger LOG = Logger.getLogger(MultipleTestsRunner.class.getName());

  private final CompositeFormatter formatters;
  private final PagesByTestSystem pagesByTestSystem;

  private final TestSystemFactory testSystemFactory;
  private final TestingTracker testingTracker;

  private volatile boolean isStopped = false;
  private String stopId = null;

  private TestSystem testSystem;
  private volatile int testsInProgressCount;

  public MultipleTestsRunner(final PagesByTestSystem pagesByTestSystem,
                             final TestingTracker testingTracker,
                             final TestSystemFactory testSystemFactory) {
    this.pagesByTestSystem = pagesByTestSystem;
    this.testingTracker = testingTracker;
    this.testSystemFactory = testSystemFactory;
    this.formatters = new CompositeFormatter();
  }

  public void addTestSystemListener(TestSystemListener listener) {
    this.formatters.addTestSystemListener(listener);
  }

  public void executeTestPages() throws IOException, InterruptedException {
    internalExecuteTestPages();
    allTestingComplete();
  }

  void allTestingComplete() throws IOException {
    formatters.close();
  }

  private void internalExecuteTestPages() throws IOException, InterruptedException {
    stopId = testingTracker.addStartedProcess(this);

    formatters.setTrackingId(stopId);
    announceTotalTestsToRun(pagesByTestSystem);

    for (Descriptor descriptor : pagesByTestSystem.descriptors()) {
      startTestSystemAndExecutePages(descriptor, pagesByTestSystem.testPageForDescriptor(descriptor));
    }

    testingTracker.removeEndedProcess(stopId);
  }

  private void startTestSystemAndExecutePages(Descriptor descriptor, List<WikiPage> testSystemPages) throws IOException, InterruptedException {
    TestSystem testSystem = null;
    try {
      if (!isStopped) {
        testSystem = startTestSystem(descriptor);
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

  private TestSystem startTestSystem(Descriptor descriptor) throws IOException {
    testSystem = testSystemFactory.create(descriptor);
    testSystem.addTestSystemListener(this);
    testSystem.start();
    return testSystem;
  }

  private void executeTestSystemPages(List<WikiPage> pagesInTestSystem, TestSystem testSystem) throws IOException, InterruptedException {
    for (WikiPage testPage : pagesInTestSystem) {
      testsInProgressCount++;
      testSystem.runTests(new WikiTestPage(testPage));
    }
  }

  private void waitForTestSystemToSendResults() throws InterruptedException {
    // TODO: use testSystemStopped event to wait for tests to end.
    while (testsInProgressCount > 0 && isNotStopped())
      Thread.sleep(50);
  }

  void announceTotalTestsToRun(PagesByTestSystem pagesByTestSystem) {
    formatters.announceNumberTestsToRun(pagesByTestSystem.totalTestsToRun());
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
      testingTracker.removeEndedProcess(stopId);
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
