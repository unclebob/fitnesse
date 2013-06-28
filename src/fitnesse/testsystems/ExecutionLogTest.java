// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static util.RegexTestCase.assertNotSubString;
import static util.RegexTestCase.assertSubString;

import org.junit.Before;
import org.junit.Test;

import fitnesse.FitNesseContext;
import fitnesse.responders.run.ExecutionStatus;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.mem.InMemoryPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageDummy;

public class ExecutionLogTest {

  @Test
    public void testExecutionReport_Ok() throws Exception {
    MockCommandRunner mockCommandRunner = new MockCommandRunner();
    ExecutionLog executionLog = new ExecutionLog(mockCommandRunner);
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
    MockCommandRunner mockCommandRunner = new MockCommandRunner();
    mockCommandRunner.setOutput("I wrote something here");
    ExecutionLog executionLog = new ExecutionLog(mockCommandRunner);
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
    MockCommandRunner mockCommandRunner = new MockCommandRunner();
    ExecutionLog executionLog = new ExecutionLog(mockCommandRunner);
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

