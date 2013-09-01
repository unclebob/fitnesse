// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testrunner;

import java.io.IOException;

import fitnesse.testsystems.TestSystemListener;

public interface ResultsListener extends TestSystemListener<WikiTestPage> {

  // runner specific

  // TODO: replace by Closeable.close().
  public void allTestingComplete() throws IOException;

  public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log);

  // Only really used in SuiteHtmlFormatter
  public void announceNumberTestsToRun(int testsToRun);
}
