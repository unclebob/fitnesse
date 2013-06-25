// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems;

import fitnesse.testsystems.slim.results.ExceptionResult;
import fitnesse.testsystems.slim.results.TestResult;
import fitnesse.testsystems.slim.tables.Assertion;

import java.io.IOException;

public interface TestSystemListener {
  void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner);

  void testOutputChunk(String output) throws IOException;

  void testComplete(TestSummary testSummary) throws IOException;

  void exceptionOccurred(Throwable e);

  void testAssertionVerified(Assertion assertion, TestResult testResult);

  void testExceptionOccurred(Assertion assertion, ExceptionResult exceptionResult);
}
