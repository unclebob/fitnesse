// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;

import fitnesse.testsystems.slim.results.ExceptionResult;
import fitnesse.testsystems.slim.results.TestResult;
import fitnesse.testsystems.slim.tables.Assertion;
import fitnesse.wiki.WikiPage;

public abstract class TestSystem extends ClientBuilder implements TestSystemListener {

  protected final TestSystemListener testSystemListener;

  public TestSystem(WikiPage page, TestSystemListener testSystemListener) {
    super(page);
    this.testSystemListener = testSystemListener;
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

  public abstract void bye() throws IOException, InterruptedException;

  public abstract boolean isSuccessfullyStarted();

  public abstract void kill() throws IOException;

  public abstract void runTests(TestPage pageToTest) throws IOException, InterruptedException;

  public static Descriptor getDescriptor(WikiPage page, boolean isRemoteDebug) {
    return new Descriptor(page, isRemoteDebug);
  }

}
