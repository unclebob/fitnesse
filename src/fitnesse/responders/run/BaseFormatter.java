// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.wiki.WikiPage;
import fitnesse.FitNesseContext;

public abstract class BaseFormatter implements ResultsListener {

  protected WikiPage page = null;
  protected FitNesseContext context;
  public static final BaseFormatter NULL = new NullFormatter();
  public abstract void writeHead(String pageType) throws Exception;

  public abstract int allTestingComplete() throws Exception;

  protected BaseFormatter() {
  }

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

  public void addMessageForBlankHtml() throws Exception
  {}
}

class NullFormatter extends BaseFormatter {
  NullFormatter() {
    super(null, null);
  }

  public int allTestingComplete() throws Exception {
    return 0;
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

  public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner) throws Exception {
  }

  public void newTestStarted(WikiPage test) throws Exception {
  }

  public void testOutputChunk(String output) throws Exception {
  }

  public void testComplete(WikiPage test, TestSummary testSummary) throws Exception {
  }

  public void writeHead(String pageType) throws Exception {
  }
}
