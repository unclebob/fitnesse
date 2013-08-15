// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testrunner;

import fitnesse.testsystems.Assertion;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.slim.results.ExceptionResult;
import util.TimeMeasurement;

import java.io.IOException;

public interface ResultsListener {

  public void allTestingComplete(TimeMeasurement totalTimeMeasurement) throws IOException;
  
  public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log);

  public void announceNumberTestsToRun(int testsToRun);

  public void testSystemStarted(TestSystem testSystem);

  public void newTestStarted(WikiTestPage test, TimeMeasurement timeMeasurement) throws IOException;

  public void testOutputChunk(String output) throws IOException;

  public void testAssertionVerified(Assertion assertion, TestResult testResult);

  public void testExceptionOccurred(Assertion assertion, ExceptionResult exceptionResult);

  public void testComplete(WikiTestPage test, TestSummary testSummary, TimeMeasurement timeMeasurement) throws IOException;
  
  public void errorOccured();
}
