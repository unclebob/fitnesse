// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run.slimResponder;

import java.io.IOException;

import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureTestOperation;
import fitnesse.components.ClassPathBuilder;
import fitnesse.responders.WikiPageResponder;
import fitnesse.responders.run.ExecutionLog;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.TestSystem;
import fitnesse.responders.run.TestSystemListener;
import fitnesse.wiki.PageData;

/*
This responder is a test rig for SlimTestSystemTest, which makes sure that the SlimTestSystem works nicely with
responders in general.
*/
public abstract class SlimResponder extends WikiPageResponder implements TestSystemListener {
  private boolean slimOpen = false;
  ExecutionLog log;
  private boolean fastTest = false;
  SlimTestSystem testSystem;


  protected String generateHtml(PageData pageData) {
    testSystem = getTestSystem(pageData);
    String html = null;

    String classPath = new ClassPathBuilder().getClasspath(page);
    TestSystem.Descriptor descriptor = TestSystem.getDescriptor(page.getData(), false);
    descriptor.testRunner = "fitnesse.slim.SlimService";
    log = testSystem.getExecutionLog(classPath, descriptor);
    try {
      testSystem.start();
      testSystem.setFastTest(fastTest);
      html = testSystem.runTestsAndGenerateHtml(pageData);
      testSystem.bye();
    } catch (IOException e) {
      html = "Could not execute tests: " + e.getMessage();
      e.printStackTrace();
    }
    
    return html;
  }

  protected abstract SlimTestSystem getTestSystem(PageData pageData);

  public SecureOperation getSecureOperation() {
    return new SecureTestOperation();
  }

  boolean slimOpen() {
    return slimOpen;
  }

  public PageData getTestResults() {
    return testSystem.getTestResults();
  }

  public TestSummary getTestSummary() {
    return testSystem.getTestSummary();
  }

  protected void setFastTest(boolean fastTest) {
    this.fastTest = fastTest;
  }

  public void acceptOutputFirst(String output) {
  }

  public void testComplete(TestSummary testSummary)  {
  }

  public void exceptionOccurred(Throwable e) {
    //todo remove sout
    System.err.println("SlimResponder.exceptionOcurred:" + e.getMessage());
  }

  public String getCommandLine() {
    return testSystem.getCommandLine();
  }
}

