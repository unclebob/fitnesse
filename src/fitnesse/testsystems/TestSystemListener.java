// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems;

import java.io.IOException;

import fitnesse.testsystems.slim.results.ExceptionResult;
import fitnesse.testsystems.slim.results.TestResult;
import fitnesse.testsystems.slim.tables.Assertion;

public interface TestSystemListener {
  public void testOutputChunk(String output) throws IOException;

  public void testComplete(TestSummary testSummary) throws IOException;

  public void exceptionOccurred(Throwable e);

  public void testAssertionVerified(Assertion assertion, TestResult testResult);

  public void testExceptionOccurred(Assertion assertion, ExceptionResult exceptionResult);

}
