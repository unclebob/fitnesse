// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.testsystems.CompositeExecutionLog;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.slim.results.ExceptionResult;
import fitnesse.testsystems.slim.results.TestResult;
import fitnesse.testsystems.slim.tables.Assertion;
import util.TimeMeasurement;

import java.io.IOException;

public interface ResultsListener {

  public void allTestingComplete(TimeMeasurement totalTimeMeasurement) throws IOException;
  
  public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log);

  public void announceNumberTestsToRun(int testsToRun);

  public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner);

  public void newTestStarted(TestPage test, TimeMeasurement timeMeasurement) throws IOException;

  public void testOutputChunk(String output) throws IOException;

  public void testAssertionVerified(Assertion assertion, TestResult testResult);

  public void testExceptionOccurred(Assertion assertion, ExceptionResult exceptionResult);

  public void testComplete(TestPage test, TestSummary testSummary, TimeMeasurement timeMeasurement) throws IOException;
  
  public void errorOccured();
}
