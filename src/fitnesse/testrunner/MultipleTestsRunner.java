// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testrunner;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import fitnesse.testsystems.Assertion;
import fitnesse.testsystems.CompositeExecutionLogListener;
import fitnesse.testsystems.Descriptor;
import fitnesse.testsystems.ExceptionResult;
import fitnesse.testsystems.ExecutionLogListener;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystemFactory;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.wiki.ClassPathBuilder;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.VariableSource;

public class MultipleTestsRunner implements Stoppable {
  private static final Logger LOG = Logger.getLogger(MultipleTestsRunner.class.getName());

  private final CompositeFormatter formatters;
  private final PagesByTestSystem pagesByTestSystem;

  private final TestSystemFactory testSystemFactory;
  private final TestingTracker testingTracker;
  private final CompositeExecutionLogListener executionLogListener;
  private final VariableSource variableSource;

  private volatile boolean isStopped = false;
  private String stopId = null;

  private boolean runInProcess;
  private boolean enableRemoteDebug;

  private TestSystem testSystem;
  private volatile int testsInProgressCount;

  public MultipleTestsRunner(final PagesByTestSystem pagesByTestSystem,
                             final TestingTracker testingTracker,
                             final TestSystemFactory testSystemFactory,
                             final VariableSource variableSource) {
    this.pagesByTestSystem = pagesByTestSystem;
    this.testingTracker = testingTracker;
    this.testSystemFactory = testSystemFactory;
    this.variableSource = variableSource;
    this.formatters = new CompositeFormatter();
    this.executionLogListener = new CompositeExecutionLogListener();
  }

  public void setRunInProcess(boolean runInProcess) {
    this.runInProcess = runInProcess;
  }

  public void setEnableRemoteDebug(boolean enableRemoteDebug) {
    this.enableRemoteDebug = enableRemoteDebug;
  }

  public void addTestSystemListener(TestSystemListener listener) {
    this.formatters.addTestSystemListener(listener);
  }

  public void executeTestPages() throws IOException, InterruptedException {
    try {
      internalExecuteTestPages();
    } finally {
      allTestingComplete();
    }
  }

  private void allTestingComplete() throws IOException {
    formatters.close();
  }

  private void internalExecuteTestPages() throws IOException, InterruptedException {
    stopId = testingTracker.addStartedProcess(this);

    formatters.setTrackingId(stopId);
    announceTotalTestsToRun(pagesByTestSystem);

    try {
      for (WikiPageIdentity identity : pagesByTestSystem.identities()) {
        startTestSystemAndExecutePages(identity, pagesByTestSystem.testPagesForIdentity(identity));
      }
    } finally {
      testingTracker.removeEndedProcess(stopId);
    }
  }

  private void startTestSystemAndExecutePages(WikiPageIdentity identity, List<WikiPage> testSystemPages) throws IOException, InterruptedException {
    TestSystem testSystem = null;
    try {
      if (!isStopped) {
        testSystem = startTestSystem(identity, testSystemPages);
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

  private TestSystem startTestSystem(final WikiPageIdentity identity, final List<WikiPage> testPages) throws IOException {
    Descriptor descriptor = new Descriptor() {
      private String classPath;

      @Override
      public String getTestSystem() {
        String testSystemName = getVariable(WikiPageIdentity.TEST_SYSTEM);
        if (testSystemName == null)
          return "fit";
        return testSystemName;
      }

      @Override
      public String getTestSystemType() {
        return getTestSystem().split(":")[0];
      }

      @Override
      public String getClassPath() {
        if (classPath == null) {
          classPath = new ClassPathBuilder().buildClassPath(testPages);
        }
        return classPath;
      }

      @Override
      public boolean runInProcess() {
        return runInProcess;
      }

      @Override
      public boolean isDebug() {
        return enableRemoteDebug;
      }

      @Override
      public String getVariable(String name) {
        return identity.getVariable(name);
      }

      @Override
      public ExecutionLogListener getExecutionLogListener() {
        return executionLogListener;
      }
    };

    InternalTestSystemListener internalTestSystemListener = new InternalTestSystemListener();
    try {
      testSystem = testSystemFactory.create(descriptor);

      testSystem.addTestSystemListener(internalTestSystemListener);
      testSystem.start();
    } catch (Exception e) {
      internalTestSystemListener.testOutputChunk(String.format("<span class=\"error\">Unable to start test system '%s': %s</span>", descriptor.getTestSystem(), e.toString()));
      return null;
    }
    return testSystem;
  }

  private void executeTestSystemPages(List<WikiPage> pagesInTestSystem, TestSystem testSystem) throws IOException, InterruptedException {
    for (WikiPage testPage : pagesInTestSystem) {
      testsInProgressCount++;
      testSystem.runTests(new WikiTestPage(testPage, variableSource));
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

  public void addExecutionLogListener(ExecutionLogListener listener) {
    executionLogListener.addExecutionLogListener(listener);
  }

  private class InternalTestSystemListener implements TestSystemListener<WikiTestPage> {
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
    public void testSystemStopped(TestSystem testSystem, Throwable cause) {
      formatters.testSystemStopped(testSystem, cause);

      if (cause != null) {
        executionLogListener.exceptionOccurred(cause);
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
