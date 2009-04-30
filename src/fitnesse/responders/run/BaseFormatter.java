// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.wiki.WikiPage;
import fitnesse.FitNesseContext;

public abstract class BaseFormatter implements ResultsListener {

  protected final WikiPage page;
  protected FitNesseContext context;
  public static final BaseFormatter NULL = new NullFormatter();
  public abstract void writeHead(String pageType) throws Exception;

  public abstract void allTestingComplete() throws Exception;

  protected BaseFormatter(FitNesseContext context, final WikiPage page) {
    this.page = page;
    this.context = context;
  }
  
  protected WikiPage getPage() {
    return page;
  }
  
  public void errorOccured() {
    try {
      allTestingComplete();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public void announceNumberTestsToRun(int testsToRun) {
  }
}

class NullFormatter extends BaseFormatter {
  NullFormatter() {
    super(null, null);
  }

  public void allTestingComplete() throws Exception {
  }

  protected WikiPage getPage() {
    return null;
  }

  public void errorOccured() {

  }

  public void announceNumberTestsToRun(int testsToRun) {
  }

  public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log) throws Exception {
  }

  public void announceStartTestSystem(TestSystem testSystem, String testSystemName, String testRunner) throws Exception {
  }

  public void announceStartNewTest(WikiPage test) throws Exception {
  }

  public void processTestOutput(String output) throws Exception {
  }

  public void processTestResults(WikiPage test, TestSummary testSummary) throws Exception {
  }

  public void writeHead(String pageType) throws Exception {
  }
}
