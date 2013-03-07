// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertSame;
import static util.RegexTestCase.assertNotSubString;
import static util.RegexTestCase.assertSubString;

import org.junit.Before;
import org.junit.Test;

import fitnesse.FitNesseContext;
import fitnesse.responders.run.ExecutionStatus;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageDummy;

public class ExecutionLogTest {
  private static String ErrorLogName = ExecutionLog.ErrorLogName;

  private WikiPage testPage;
  private MockCommandRunner runner;
  private ExecutionLog log;
  private WikiPage root;
  private FitNesseContext context;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    testPage = root.addChildPage("TestPage");
    runner = new MockCommandRunner("some command", 123);
    context = FitNesseUtil.makeTestContext(root);
    log = new ExecutionLog(testPage, runner);
  }


  @Test
  public void testNoErrrorLogPageToBeginWith() throws Exception {
    assertFalse(root.hasChildPage(ErrorLogName));
  }
  @Test
    public void testBasicContent() throws Exception {
    String content = getGeneratedContent();

    assertSubString("'''Command: '''", content);
    assertSubString("!-some command-!", content);
    assertSubString("'''Exit code: '''", content);
    assertSubString("123", content);
    assertSubString("'''Date: '''", content);
    assertSubString("'''Time elapsed: '''", content);
  }

  @Test
  public void testPageLink() throws Exception {
    String content = getGeneratedContent();
    assertSubString("|'''Test Page: '''|.TestPage|", content);
  }

  private String getGeneratedContent() throws Exception {
    return log.buildLogContent(context.pageFactory);
  }

  @Test
    public void testNoExtraLogTextWasGenerated() throws Exception {
    String content = getGeneratedContent();

    assertNotSubString("Exception", content);
    assertNotSubString("Standard Error", content);
    assertNotSubString("Standard Output", content);
  }

  @Test
    public void testStdout() throws Exception {
    runner.setOutput("standard output that got printed");
    String content = getGeneratedContent();

    assertSubString("'''Standard Output:'''", content);
    assertSubString("standard output that got printed", content);
  }

  @Test
    public void testStderr() throws Exception {
    runner.setError("standard error that got printed");
    String content = getGeneratedContent();

    assertSubString("'''Standard Error:'''", content);
    assertSubString("standard error that got printed", content);
  }

  @Test
    public void testException() throws Exception {
    log.addException(new Exception("I made this"));
    String content = getGeneratedContent();

    assertSubString("'''Internal Exception:'''", content);
    assertSubString("I made this", content);
  }

  @Test
    public void testExecutionReport_Ok() throws Exception {
    WikiPageDummy wikiPageDummy = new WikiPageDummy("This.Is.Not.A.Real.Location");
    MockCommandRunner mockCommandRunner = new MockCommandRunner();
    ExecutionLog executionLog = new ExecutionLog(wikiPageDummy, mockCommandRunner);
    ExecutionStatus result;

    if (executionLog.exceptionCount() > 0)
      result = ExecutionStatus.ERROR;
    else if (executionLog.hasCapturedOutput())
      result = ExecutionStatus.OUTPUT;
    else
      result = ExecutionStatus.OK;

    assertSame(ExecutionStatus.OK, result);
  }

  @Test
    public void testExecutionReport_Output() throws Exception {
    WikiPageDummy wikiPageDummy = new WikiPageDummy("This.Is.Not.A.Real.Location");
    MockCommandRunner mockCommandRunner = new MockCommandRunner();
    mockCommandRunner.setOutput("I wrote something here");
    ExecutionLog executionLog = new ExecutionLog(wikiPageDummy, mockCommandRunner);
    ExecutionStatus result;

    if (executionLog.exceptionCount() > 0)
      result = ExecutionStatus.ERROR;
    else if (executionLog.hasCapturedOutput())
      result = ExecutionStatus.OUTPUT;
    else
      result = ExecutionStatus.OK;

    assertSame(ExecutionStatus.OUTPUT, result);
  }

  @Test
    public void testExecutionReport_Error() throws Exception {
    WikiPageDummy wikiPageDummy = new WikiPageDummy("This.Is.Not.A.Real.Location");
    MockCommandRunner mockCommandRunner = new MockCommandRunner();
    ExecutionLog executionLog = new ExecutionLog(wikiPageDummy, mockCommandRunner);
    executionLog.addException(new RuntimeException("I messed up"));
    ExecutionStatus result;

    if (executionLog.exceptionCount() > 0)
      result = ExecutionStatus.ERROR;
    else if (executionLog.hasCapturedOutput())
      result = ExecutionStatus.OUTPUT;
    else
      result = ExecutionStatus.OK;

    assertSame(ExecutionStatus.ERROR, result);
  }
}

