// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.fit;

import java.io.IOException;
import java.util.Map;

import fitnesse.FitNesseContext;
import fitnesse.testsystems.ClientBuilder;
import fitnesse.testsystems.Descriptor;
import fitnesse.testsystems.ExecutionLog;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystemListener;

public class FitTestSystem extends ClientBuilder<FitClient> implements TestSystem, FitClientListener {
  protected static final String EMPTY_PAGE_CONTENT = "OH NO! This page is empty!";

  private final FitNesseContext context;
  private final TestSystemListener testSystemListener;
  private CommandRunningFitClient client;

  public FitTestSystem(FitNesseContext context, Descriptor descriptor,
                       TestSystemListener listener) {
    super(descriptor);
    this.context = context;
    this.testSystemListener = listener;
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
    testSystemStopped(client.getExecutionLog(), null);
  }

  @Override
  public void kill() {
    client.kill();
    testSystemStopped(client.getExecutionLog(), null);
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
  public void exceptionOccurred(Exception e) {
    ExecutionLog log = client.getExecutionLog();
    log.addException(e);
    try {
      client.kill();
    } finally {
      testSystemStopped(log, e);
    }
  }

  private void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner) {
    testSystemListener.testSystemStarted(testSystem);
  }

  private void testSystemStopped(ExecutionLog executionLog, Throwable throwable) {
    testSystemListener.testSystemStopped(this, executionLog, throwable);
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
    CommandRunningFitClient.CommandRunningStrategy runningStrategy =
            new CommandRunningFitClient.OutOfProcessCommandRunner(command, environmentVariables);

    return buildFitClient(runningStrategy);
  }

  protected FitClient buildFitClient(CommandRunningFitClient.CommandRunningStrategy runningStrategy) {
    client = new CommandRunningFitClient(this, context.port, context.socketDealer, runningStrategy);

    return client;
  }

}
