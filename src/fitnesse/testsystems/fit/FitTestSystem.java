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
  private final String classPath;
  private final TestSystemListener testSystemListener;
  private final String testSystemName;
  private ExecutionLog log;
  private CommandRunningFitClient client;

  public FitTestSystem(String testSystemName, FitNesseContext context, WikiPage page, String classPath,
                       TestSystemListener listener) {
    super(page.getData());
    this.testSystemName = testSystemName;
    this.context = context;
    this.page = page;
    this.classPath = classPath;
    this.testSystemListener = listener;
  }

  protected final void setExecutionLog(final ExecutionLog log) {
    this.log = log;
  }

  @Override
  protected String defaultTestRunner() {
    return "fit.FitServer";
  }

  @Override
  public void start() {
    client.start();
    testSystemStarted(this, testSystemName, client.getTestRunner());
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
  public ExecutionLog getExecutionLog() {
    return log;
  }

  @Override
  public void bye() throws IOException, InterruptedException {
    client.done();
    client.join();
  }

  @Override
  public void kill() {
    client.kill();
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

  // Remove from here and below: this has all to do with client creation.

  @Override
  public boolean isSuccessfullyStarted() {
    return client.isSuccessfullyStarted();
  }

  @Override
  public FitClient build() {
    String testRunner = getTestRunner();
    String command = buildCommand(getCommandPattern(), testRunner, classPath);
    Map<String, String> environmentVariables = createClasspathEnvironment(classPath);
    CommandRunningFitClient.CommandRunningStrategy runningStrategy = fastTest ?
            new CommandRunningFitClient.InProcessCommandRunner(testRunner) :
            new CommandRunningFitClient.OutOfProcessCommandRunner(command, environmentVariables);

    client = new CommandRunningFitClient(testRunner, this, context.port, context.socketDealer, runningStrategy);
    setExecutionLog(new ExecutionLog(page, client.commandRunner));

    return client;
  }

}
