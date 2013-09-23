// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CommandRunnerExecutionLogTest {

  @Test
    public void testExecutionReport_Ok() throws Exception {
    MockCommandRunner mockCommandRunner = new MockCommandRunner();
    CommandRunnerExecutionLog executionLog = new CommandRunnerExecutionLog(mockCommandRunner);

    assertTrue(executionLog.getExceptions().isEmpty());
    assertFalse(executionLog.hasCapturedOutput());
  }

  @Test
    public void testExecutionReport_Output() throws Exception {
    MockCommandRunner mockCommandRunner = new MockCommandRunner();
    mockCommandRunner.setOutput("I wrote something here");
    CommandRunnerExecutionLog executionLog = new CommandRunnerExecutionLog(mockCommandRunner);

    assertTrue(executionLog.getExceptions().isEmpty());
    assertTrue(executionLog.hasCapturedOutput());
  }

  @Test
    public void testExecutionReport_Error() throws Exception {
    MockCommandRunner mockCommandRunner = new MockCommandRunner();
    CommandRunnerExecutionLog executionLog = new CommandRunnerExecutionLog(mockCommandRunner);
    executionLog.addException(new RuntimeException("I messed up"));

    assertFalse(executionLog.getExceptions().isEmpty());
    assertFalse(executionLog.hasCapturedOutput());
  }
}

