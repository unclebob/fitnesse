// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testrunner;

import fitnesse.testrunner.run.RunCoordinator;
import fitnesse.testrunner.run.TestRun;
import fitnesse.testsystems.Assertion;
import fitnesse.testsystems.ClassPath;
import fitnesse.testsystems.CompositeExecutionLogListener;
import fitnesse.testsystems.Descriptor;
import fitnesse.testsystems.ExceptionResult;
import fitnesse.testsystems.ExecutionLogListener;
import fitnesse.testsystems.TestExecutionException;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystemFactory;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.testsystems.slim.TestingInterruptedException;
import util.FileUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MultipleTestsRunner implements Stoppable {
  private final TestRun run;
  private final CompositeFormatter formatters;

  private final TestSystemFactory testSystemFactory;
  private final CompositeExecutionLogListener executionLogListener;

  private volatile boolean isStopped = false;

  private boolean runInProcess;
  private boolean enableRemoteDebug;

  private final AtomicInteger testsInProgressCount = new AtomicInteger();

  public MultipleTestsRunner(TestRun run, TestSystemFactory testSystemFactory) {
    this.testSystemFactory = testSystemFactory;
    this.formatters = new CompositeFormatter();
    this.executionLogListener = new CompositeExecutionLogListener();
    this.run = run;
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

  public void addExecutionLogListener(ExecutionLogListener listener) {
    executionLogListener.addExecutionLogListener(listener);
  }

  public void executeTestPages() throws TestExecutionException {
    MultipleTestsCoordinator coordinator = new MultipleTestsCoordinator();
    try {
      run.executeTestPages(coordinator);
    } catch (Exception e) {
      executionLogListener.exceptionOccurred(e);
      throw new TestingInterruptedException(e);
    } finally {
      allTestingComplete();
    }
  }

  @Override
  public void stop() {
    boolean wasNotStopped = isNotStopped();
    isStopped = true;

    if (wasNotStopped) {
      run.stop();
    }
  }

  private void allTestingComplete() {
    FileUtil.close(formatters);
  }

  private class MultipleTestsCoordinator implements RunCoordinator {

    @Override
    public boolean isNotStopped() {
      return MultipleTestsRunner.this.isNotStopped();
    }

    @Override
    public int announceTestStarted() {
      return testsInProgressCount.getAndIncrement();
    }

    @Override
    public void reportException(Exception e) {
      executionLogListener.exceptionOccurred(e);
    }

    @Override
    public TestSystem startTestSystem(final WikiPageIdentity identity, final List<TestPage> testPages) {
      Descriptor descriptor = new Descriptor() {
        private ClassPath classPath;

        @Override
        public String getTestSystem() {
          return identity.testSystem();
        }

        @Override
        public String getTestSystemType() {
          return getTestSystem().split(":")[0];
        }

        @Override
        public ClassPath getClassPath() {
          if (classPath == null) {
            List<ClassPath> paths = new ArrayList<>();
            for (TestPage testPage: testPages) {
              paths.add(testPage.getClassPath());
            }
            classPath = new ClassPath(paths);
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
        TestSystem testSystem = testSystemFactory.create(descriptor);

        testSystem.addTestSystemListener(internalTestSystemListener);
        testSystem.start();
        return testSystem;
      } catch (Exception e) {
        formatters.unableToStartTestSystem(descriptor.getTestSystem(), e);
        return null;
      }
    }

    @Override
    public void waitForNoTestsInProgress() throws TestingInterruptedException {
      // TODO: use testSystemStopped event to wait for tests to end.
      while (testsInProgressCount.get() > 0 && isNotStopped())
        try {
          Thread.sleep(50);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new TestingInterruptedException("Interrupted while waiting for test results", e);
        }
    }

    @Override
    public void announceTotalTestsToRun(int toRun) {
      formatters.announceNumberTestsToRun(toRun);
    }
  }

  private class InternalTestSystemListener implements TestSystemListener {
    @Override
    public void testSystemStarted(TestSystem testSystem) {
      formatters.testSystemStarted(testSystem);
    }

    @Override
    public void testOutputChunk(TestPage testPage, String output) {
      formatters.testOutputChunk(testPage, output);
    }

    @Override
    public void testStarted(TestPage testPage) {
      formatters.testStarted(testPage);
    }

    @Override
    public void testComplete(TestPage testPage, TestSummary testSummary) {
      formatters.testComplete(testPage, testSummary);
      testsInProgressCount.getAndDecrement();
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
}
