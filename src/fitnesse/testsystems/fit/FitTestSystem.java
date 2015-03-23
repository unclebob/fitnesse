// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.fit;

import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;

import fitnesse.testsystems.CompositeTestSystemListener;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystemListener;

public class FitTestSystem implements TestSystem, FitClientListener {
  protected static final String EMPTY_PAGE_CONTENT = "OH NO! This page is empty!";

  private final CompositeTestSystemListener testSystemListener;
  private final String testSystemName;
  private final CommandRunningFitClient client;
  private Deque<TestPage> processingQueue = new LinkedList<TestPage>();
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
  public void start() throws IOException {
    // TODO: start a server socket (thread) here
    client.start();
    testSystemStarted(this);
  }

  @Override
  public void runTests(TestPage pageToTest) throws IOException, InterruptedException {
    processingQueue.addLast(pageToTest);
    String html = pageToTest.getHtml();
    try {
      if (html.isEmpty())
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
    try {
      client.done();
      client.join();
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
  public void exceptionOccurred(Throwable t) {
    try {
      client.kill();
    } finally {
      testSystemStopped(t);
    }
  }

  private void testSystemStarted(TestSystem testSystem) throws IOException {
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
