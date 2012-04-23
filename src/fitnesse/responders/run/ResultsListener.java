// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import java.io.IOException;

import util.TimeMeasurement;
import fitnesse.wiki.WikiPage;

public interface ResultsListener {

  public void allTestingComplete(TimeMeasurement totalTimeMeasurement) throws IOException;
  
  public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log);

  public void announceNumberTestsToRun(int testsToRun);

  public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner);

  public void newTestStarted(TestPage test, TimeMeasurement timeMeasurement) throws IOException;

  public void testOutputChunk(String output) throws IOException;

  public void testComplete(TestPage test, TestSummary testSummary, TimeMeasurement timeMeasurement) throws IOException;
  
  public void errorOccured();
}
