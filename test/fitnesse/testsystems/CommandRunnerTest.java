// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static util.RegexTestCase.assertHasRegexp;

import org.junit.Test;

import fitnesse.testutil.Echo;
import util.GradleSupport;

public class CommandRunnerTest {

  private TestExecutionLogListener executionLogListener = new TestExecutionLogListener();

  @Test
  public void testBasics() throws Exception {
    CommandRunner runner = new CommandRunner(new String[] { "java", "-cp", GradleSupport.CLASSES_DIR, "fitnesse.testutil.Echo" }, null, executionLogListener);
    runner.asynchronousStart();
    runner.join();
    assertHasRegexp(Echo.ECHO_THIS, executionLogListener.stdOut.toString());
    assertEquals("", executionLogListener.stdErr.toString());
    assertEquals(true, executionLogListener.exceptions.isEmpty());
    assertEquals(0, executionLogListener.exitCode);
  }

  @Test
  public void testClassNotFound() throws Exception {
    CommandRunner runner = new CommandRunner(new String[] {  "java", "-Duser.country=US", "-Duser.language=en", "BadClass" }, null, executionLogListener);
    runner.asynchronousStart();
    runner.join();
    assertHasRegexp("Error", executionLogListener.stdErr.toString());
    assertEquals("", executionLogListener.stdOut.toString());
    assertTrue(0 != executionLogListener.exitCode);
  }

  private class TestExecutionLogListener implements ExecutionLogListener {

    private StringBuilder stdOut = new StringBuilder();
    private StringBuilder stdErr = new StringBuilder();
    private int exitCode;
    private List<Throwable> exceptions = new LinkedList<>();

    @Override
    public void commandStarted(ExecutionContext context) {

    }

    @Override
    public void stdOut(String output) {
      stdOut.append(output);
    }

    @Override
    public void stdErr(String output) {
      stdErr.append(output);
    }

    @Override
    public void exitCode(int exitCode) {
      this.exitCode = exitCode;
    }

    @Override
    public void exceptionOccurred(Throwable e) {
      exceptions.add(e);
    }
  }
}
