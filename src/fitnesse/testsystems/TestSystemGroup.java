// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import fitnesse.FitNesseContext;
import fitnesse.slim.SlimCommandRunningClient;
import fitnesse.testsystems.fit.FitTestSystem;
import fitnesse.testsystems.slim.HtmlSlimTestSystem;
import fitnesse.testsystems.slim.SlimClientBuilder;
import fitnesse.testsystems.slim.results.ExceptionResult;
import fitnesse.testsystems.slim.results.TestResult;
import fitnesse.testsystems.slim.tables.Assertion;
import fitnesse.wiki.WikiPage;

public class TestSystemGroup {
  private Map<Descriptor, TestSystem> testSystems = new HashMap<Descriptor, TestSystem>();
  private FitNesseContext context;
  private WikiPage page;
  private TestSystemListener testSystemListener;
  private CompositeExecutionLog log;
  private boolean fastTest = false;
  private boolean manualStart = false;
  private boolean remoteDebug;

  public TestSystemGroup(FitNesseContext context, WikiPage page, TestSystemListener listener) {
    this.context = context;
    this.page = page;
    this.testSystemListener = listener;
    log = new CompositeExecutionLog(page);
  }

  public CompositeExecutionLog getExecutionLog() {
    return log;
  }

  public void kill() throws IOException {
    for (TestSystem testSystem : testSystems.values()) {
      testSystem.kill();
    }
  }

  public void setFastTest(boolean fastTest) {
    this.fastTest = fastTest;
  }

  public void setManualStart(boolean manualStart) {
    this.manualStart = manualStart;
  }

  public void setRemoteDebug(boolean remoteDebug) {
    this.remoteDebug = remoteDebug;
  }

  public TestSystem startTestSystem(Descriptor descriptor) throws IOException {
    TestSystem testSystem = null;
    if (!testSystems.containsKey(descriptor)) {
      testSystem = makeTestSystem(descriptor);

      testSystems.put(descriptor, testSystem);
      testSystem.start();
    }
    return testSystem;
  }

  private TestSystem makeTestSystem(Descriptor descriptor) throws IOException {
    if ("slim".equalsIgnoreCase(WikiPageDescriptor.getTestSystemType(descriptor.getTestSystemName())))
      return createHtmlSlimTestSystem(descriptor);
    else
      return createFitTestSystem(descriptor);
  }

  private HtmlSlimTestSystem createHtmlSlimTestSystem(Descriptor descriptor) throws IOException {
    SlimCommandRunningClient slimClient = new SlimClientBuilder(descriptor)
            .withFastTest(fastTest)
            .withManualStart(manualStart)
            .withRemoteDebug(remoteDebug)
            .build();

    ExecutionLogListener listener = new ExecutionLogListener(slimClient.getCommandRunner(), testSystemListener);
    log.add(descriptor.getTestSystemName(), listener.getExecutionLog());
    HtmlSlimTestSystem testSystem = new HtmlSlimTestSystem(descriptor.getTestSystem(), slimClient, listener);

    return testSystem;
  }

  private FitTestSystem createFitTestSystem(Descriptor descriptor) throws IOException {
    FitTestSystem testSystem = new FitTestSystem(context, page, descriptor, testSystemListener);
    testSystem.withFastTest(fastTest)
            .withManualStart(manualStart)
            .withRemoteDebug(remoteDebug)
            .build();


    log.add(descriptor.getTestSystemName(), testSystem.getExecutionLog());

    return testSystem;
  }

  static class ExecutionLogListener implements TestSystemListener {

    private final ExecutionLog log;
    private final TestSystemListener testSystemListener;

    ExecutionLogListener(CommandRunner commandRunner, TestSystemListener testSystemListener) {
      this.log = new ExecutionLog(commandRunner);
      this.testSystemListener = testSystemListener;
    }

    public ExecutionLog getExecutionLog() {
      return log;
    }

    @Override
    public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner) {
      testSystemListener.testSystemStarted(testSystem, testSystemName, testRunner);
    }

    @Override
    public void testOutputChunk(String output) throws IOException {
      testSystemListener.testOutputChunk(output);
    }

    @Override
    public void testComplete(TestSummary testSummary) throws IOException {
      testSystemListener.testComplete(testSummary);
    }

    @Override
    public void exceptionOccurred(Throwable e) {
      log.addException(e);
      testSystemListener.exceptionOccurred(e);
    }

    @Override
    public void testAssertionVerified(Assertion assertion, TestResult testResult) {
      testSystemListener.testAssertionVerified(assertion, testResult);
    }

    @Override
    public void testExceptionOccurred(Assertion assertion, ExceptionResult exceptionResult) {
      testSystemListener.testExceptionOccurred(assertion, exceptionResult);
    }
  }
}
