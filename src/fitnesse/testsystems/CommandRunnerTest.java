// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static util.RegexTestCase.assertHasRegexp;

import org.junit.Test;

public class CommandRunnerTest {

  @Test
  public void testBasics() throws Exception {
    CommandRunner runner = new CommandRunner("java -cp ./classes fitnesse.testutil.Echo", "echo this!", null);
    runner.run();
    assertHasRegexp("echo this!", runner.getOutput());
    assertEquals("", runner.getError());
    assertEquals(false, runner.hasExceptions());
    assertEquals(0, runner.getExitCode());
  }

  @Test
  public void testClassNotFound() throws Exception {
    CommandRunner runner = new CommandRunner("java BadClass", "", null);
    runner.run();
    assertHasRegexp("Error", runner.getError());
    assertEquals("", runner.getOutput());
    assertTrue(0 != runner.getExitCode());
  }
}
