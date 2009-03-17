// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.runner;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import util.RegexTestCase;
import fitnesse.responders.run.TestSummary;

public class StandardResultHandlerTest extends RegexTestCase {
  private StandardResultHandler handler;
  private ByteArrayOutputStream bytes;

  public void setUp() throws Exception {
    bytes = new ByteArrayOutputStream();
    handler = new StandardResultHandler(new PrintStream(bytes));
  }

  public void testHandleResultPassing() throws Exception {
    String output = getOutputForResultWithCount(new TestSummary(5, 0, 0, 0));
    assertSubString(".....", output);
  }

  public void testHandleResultFailing() throws Exception {
    String output = getOutputForResultWithCount(new TestSummary(0, 1, 0, 0));
    assertSubString("SomePage has failures", output);
  }

  public void testHandleResultWithErrors() throws Exception {
    String output = getOutputForResultWithCount(new TestSummary(0, 0, 0, 1));
    assertSubString("SomePage has errors", output);
  }

  public void testHandleErrorWithBlankTitle() throws Exception {
    String output = getOutputForResultWithCount("", new TestSummary(0, 0, 0, 1));
    assertSubString("The test has errors", output);
  }

  public void testFinalCount() throws Exception {
    TestSummary testSummary = new TestSummary(5, 4, 3, 2);
    handler.acceptFinalCount(testSummary);

    assertSubString(testSummary.toString(), bytes.toString());
  }

  private String getOutputForResultWithCount(TestSummary testSummary) throws Exception {
    return getOutputForResultWithCount("SomePage", testSummary);
  }

  private String getOutputForResultWithCount(String title, TestSummary testSummary) throws Exception {
    PageResult result = new PageResult(title);
    result.setTestSummary(testSummary);
    handler.acceptResult(result);
    String output = bytes.toString();
    return output;
  }

}
