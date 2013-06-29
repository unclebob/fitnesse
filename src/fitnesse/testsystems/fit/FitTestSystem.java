// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.fit;

import fitnesse.FitNesseContext;
import fitnesse.testsystems.*;
import fitnesse.testsystems.slim.results.ExceptionResult;
import fitnesse.testsystems.slim.results.TestResult;
import fitnesse.testsystems.slim.tables.Assertion;
import fitnesse.wiki.WikiPage;

import java.io.IOException;
import java.util.Map;

public class FitTestSystem extends ClientBuilder<FitClient> implements TestSystem, TestSystemListener {
  protected static final String EMPTY_PAGE_CONTENT = "OH NO! This page is empty!";

  private final FitNesseContext context;
  private final WikiPage page;
  private final TestSystemListener testSystemListener;
  private CommandRunningFitClient client;

  public FitTestSystem(FitNesseContext context, WikiPage page, Descriptor descriptor,
                       TestSystemListener listener) {
    super(descriptor);
    this.context = context;
    this.page = page;
    this.testSystemListener = listener;
  }

  public static String defaultTestRunner() {
    return "fit.FitServer";
  }

  @Override
  public String getName() {
    return descriptor.getTestSystemName();
  }

  @Override
  public void start() {
    client.start();
    testSystemStarted(this, descriptor.getTestSystemName(), descriptor.getTestRunner());
  }

  @Override
  public void runTests(TestPage pageToTest) throws IOException, InterruptedException {
    String html = pageToTest.getDecoratedData().getHtml();
    if (html.length() == 0)
      client.send(EMPTY_PAGE_CONTENT);
    else
      client.send(html);
  }

  @Override
  public void bye() throws IOException, InterruptedException {
    client.done();
    client.join();
    testSystemStopped(this, new ExecutionLog(client.commandRunner), null);
  }

  @Override
  public void kill() {
    client.kill();
    testSystemStopped(this, new ExecutionLog(client.commandRunner), null);
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
    testSystemListener.exceptionOccurred(e);
    ExecutionLog log = new ExecutionLog(client.commandRunner);
    log.addException(e);
    client.kill();
    testSystemStopped(this, log, e);
  }

  @Override
  public void testSystemStopped(TestSystem testSystem, ExecutionLog executionLog, Throwable throwable) {
    testSystemListener.testSystemStopped(this, executionLog, throwable);
  }

  @Override
  public void testAssertionVerified(Assertion assertion, TestResult testResult) {
    testSystemListener.testAssertionVerified(assertion, testResult);
  }

  @Override
  public void testExceptionOccurred(Assertion assertion, ExceptionResult exceptionResult) {
    testSystemListener.testExceptionOccurred(assertion, exceptionResult);
  }

  // Remove from here and below: this has all to do with client creation.

  @Override
  public boolean isSuccessfullyStarted() {
    return client.isSuccessfullyStarted();
  }

  @Override
  public FitClient build() {
    String testRunner = descriptor.getTestRunner();
    String classPath = descriptor.getClassPath();
    String command = buildCommand(descriptor.getCommandPattern(), testRunner, classPath);
    Map<String, String> environmentVariables = descriptor.createClasspathEnvironment(classPath);
    CommandRunningFitClient.CommandRunningStrategy runningStrategy = fastTest ?
            new CommandRunningFitClient.InProcessCommandRunner(testRunner) :
            new CommandRunningFitClient.OutOfProcessCommandRunner(command, environmentVariables);

    client = new CommandRunningFitClient(this, context.port, context.socketDealer, runningStrategy);

    return client;
  }

}
