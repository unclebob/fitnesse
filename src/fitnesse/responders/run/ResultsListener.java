// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.wiki.WikiPage;

public interface ResultsListener {
  
  public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log) throws Exception;

  public void announceNumberTestsToRun(int testsToRun);

  public void announceStartTestSystem(TestSystem testSystem, String testSystemName, String testRunner) throws Exception;

  public void announceStartNewTest(WikiPage test) throws Exception;

  public void processTestOutput(String output) throws Exception;

  public void processTestResults(WikiPage test, TestSummary testSummary) throws Exception;
  
  public void errorOccured();
}
