// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.fit;

import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;

import fitnesse.testsystems.*;

public class FitTestSystem implements TestSystem, FitClientListener {
  private static final String EMPTY_PAGE_CONTENT = "OH NO! This page is empty!";

  private final CompositeTestSystemListener testSystemListener;
  private final String testSystemName;
  private final CommandRunningFitClient client;
  private Deque<TestPage> processingQueue = new LinkedList<>();
  private TestPage currentTestPage;
  private boolean testSystemIsStopped;

  public FitTestSystem(String testSystemName, CommandRunningFitClient fitClient) {
    this.testSystemListener = new CompositeTestSystemListener();
    this.testSystemName = testSystemName;
    this.client = fitClient;
    client.addFitClientListener(this);
  }

  @Override
  public String getName() {
    return testSystemName;
  }

  @Override
  public void start() throws UnableToStartException {
    try {
      client.start();
    } catch (IOException e) {
      throw new UnableToStartException("Can not start Fit client", e);
    }
    testSystemStarted(this);
  }

  @Override
  public void runTests(TestPage pageToTest) throws TestExecutionException {
    processingQueue.addLast(pageToTest);
    String html = pageToTest.getHtml();
    try {
      if (html.isEmpty())
        client.send(EMPTY_PAGE_CONTENT);
      else
        client.send(html);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      exceptionOccurred(e);
      throw new TestExecutionException("Testing has been interrupted", e);
    } catch (IOException e) {
      exceptionOccurred(e);
      throw new TestExecutionException("Communication error during testing", e);
    }
  }

  @Override
  public void bye() throws UnableToStopException {
    try {
      client.done();
      client.join();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new UnableToStopException("Unable to stop Fit client", e);
    } catch (IOException e) {
      throw new UnableToStopException("Unable to stop Fit client", e);
    } finally {
      testSystemStopped(null);
    }
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
  public void testOutputChunk(String output) {
    if (currentTestPage == null) {
      currentTestPage = processingQueue.removeFirst();
      testSystemListener.testStarted(currentTestPage);
    }
    testSystemListener.testOutputChunk(output);
  }

  @Override
  public void testComplete(TestSummary testSummary) {
    assert currentTestPage != null;
    try {
      testSystemListener.testComplete(currentTestPage, testSummary);
    } finally {
      currentTestPage = null;
    }
  }

  @Override
  public void exceptionOccurred(Throwable t) {
    try {
      client.kill();
    } finally {
      testSystemStopped(t);
    }
  }

  private void testSystemStarted(TestSystem testSystem) {
    testSystemListener.testSystemStarted(testSystem);
  }

  private void testSystemStopped(Throwable throwable) {
    if (testSystemIsStopped) return;
    testSystemIsStopped = true;
    testSystemListener.testSystemStopped(this, throwable);
  }

  // Remove from here and below: this has all to do with client creation.

  @Override
  public boolean isSuccessfullyStarted() {
    return client.isSuccessfullyStarted();
  }
}
