// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems;

public interface TestSystemListener {
  void testSystemStarted(TestSystem testSystem);

  void testOutputChunk(String output);

  void testStarted(TestPage testPage);

  void testComplete(TestPage testPage, TestSummary testSummary);

  void testSystemStopped(TestSystem testSystem, Throwable cause /* may be null */);

  void testAssertionVerified(Assertion assertion, TestResult testResult);

  void testExceptionOccurred(Assertion assertion, ExceptionResult exceptionResult);
}
