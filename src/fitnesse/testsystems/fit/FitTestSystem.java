// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.fit;

import java.io.IOException;
import java.util.Map;

import fitnesse.FitNesseContext;
import fitnesse.testsystems.*;
import fitnesse.testsystems.slim.results.ExceptionResult;
import fitnesse.testsystems.slim.results.TestResult;
import fitnesse.testsystems.slim.tables.Assertion;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

public class FitTestSystem extends ClientBuilder implements TestSystem, TestSystemListener {
  protected static final String EMPTY_PAGE_CONTENT = "OH NO! This page is empty!";

  private final WikiPage page;
  private final TestSystemListener testSystemListener;
  private ExecutionLog log;
  private final PageData data;
  private CommandRunningFitClient client;
  private FitNesseContext context;
  private final Descriptor descriptor;

  public FitTestSystem(FitNesseContext context, WikiPage page, Descriptor descriptor,
                       TestSystemListener listener) {
    super(page);
    this.descriptor = descriptor;
    this.context = context;
    this.page = page;
    this.testSystemListener = listener;
    this.data = page.getData();
    this.context = context;
  }

  protected final void setExecutionLog(final ExecutionLog log) {
    this.log = log;
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

  public boolean isSuccessfullyStarted() {
    return client.isSuccessfullyStarted();
  }

  public void start() {
    String command = buildCommand(descriptor);
    String testRunner = descriptor.getTestRunner();
    Map<String, String> environmentVariables = createClasspathEnvironment(descriptor.getClassPath());
    CommandRunningFitClient.CommandRunningStrategy runningStrategy = fastTest ?
            new CommandRunningFitClient.InProcessCommandRunner(testRunner) :
            new CommandRunningFitClient.OutOfProcessCommandRunner(command, environmentVariables);

    this.client = new CommandRunningFitClient(this, context.port, context.socketDealer, runningStrategy);
    setExecutionLog(new ExecutionLog(page, client.commandRunner));
    client.start();
  }
}
