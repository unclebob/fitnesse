// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testrunner;

import fitnesse.testsystems.Assertion;
import fitnesse.testsystems.ExceptionResult;
import fitnesse.testsystems.ExecutionLog;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import util.TimeMeasurement;

import java.io.IOException;

public interface ResultsListener {

  // runner specific

  public void allTestingComplete() throws IOException;

  public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log);

  // Only really used in SuiteHtmlFormatter
  public void announceNumberTestsToRun(int testsToRun);

  // The remaining methods are redundant with TestSystemListener:

  public void testSystemStarted(TestSystem testSystem);

  public void newTestStarted(WikiTestPage test) throws IOException;

  public void testOutputChunk(String output) throws IOException;

  public void testAssertionVerified(Assertion assertion, TestResult testResult);

  public void testExceptionOccurred(Assertion assertion, ExceptionResult exceptionResult);

  public void testComplete(WikiTestPage test, TestSummary testSummary) throws IOException;

  public void testSystemStopped(TestSystem testSystem, ExecutionLog executionLog, Throwable cause);
}
