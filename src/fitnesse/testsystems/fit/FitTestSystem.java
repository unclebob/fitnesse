// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.fit;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;

import fitnesse.FitNesseContext;
import fitnesse.testsystems.ClientBuilder;
import fitnesse.testsystems.CompositeTestSystemListener;
import fitnesse.testsystems.Descriptor;
import fitnesse.testsystems.ExecutionLog;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystemListener;

public class FitTestSystem extends ClientBuilder<FitClient> implements TestSystem, FitClientListener {
  protected static final String EMPTY_PAGE_CONTENT = "OH NO! This page is empty!";

  private static SocketDealer socketDealer = new SocketDealer();

  private final FitNesseContext context;
  private final CompositeTestSystemListener testSystemListener;
  private CommandRunningFitClient client;
  private LinkedList<TestPage> processingQueue = new LinkedList<TestPage>();
  private TestPage currentTestPage;

  public FitTestSystem(FitNesseContext context, Descriptor descriptor,
                       TestSystemListener listener) {
    super(descriptor);
    this.context = context;
    this.testSystemListener = new CompositeTestSystemListener();
    this.testSystemListener.addTestSystemListener(listener);
  }

  public static SocketDealer socketDealer() {
    return socketDealer;
  }

  @Override
  public String getName() {
    return descriptor.getTestSystemName();
  }

  @Override
  public void start() {
    client.start();
    testSystemStarted(this);
  }

  @Override
  public void runTests(TestPage pageToTest) throws IOException, InterruptedException {
    processingQueue.addLast(pageToTest);
    String html = pageToTest.getDecoratedData().getHtml();
    try {
      if (html.length() == 0)
        client.send(EMPTY_PAGE_CONTENT);
      else
        client.send(html);
    } catch (InterruptedException e) {
      exceptionOccurred(e);
      throw e;
    } catch (IOException e) {
      exceptionOccurred(e);
      throw e;
    }
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
  }

  @Override
  public void addTestSystemListener(TestSystemListener listener) {
    testSystemListener.addTestSystemListener(listener);
  }

  @Override
  public void testOutputChunk(String output) throws IOException {
    if (currentTestPage == null) {
      currentTestPage = processingQueue.removeFirst();
      testSystemListener.testStarted(currentTestPage);
    }
    testSystemListener.testOutputChunk(output);
  }

  @Override
  public void testComplete(TestSummary testSummary) throws IOException {
    assert currentTestPage != null;
    testSystemListener.testComplete(currentTestPage, testSummary);
    currentTestPage = null;
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

  private void testSystemStarted(TestSystem testSystem) {
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
    client = new CommandRunningFitClient(this, context.port, socketDealer, runningStrategy);

    return client;
  }

}
