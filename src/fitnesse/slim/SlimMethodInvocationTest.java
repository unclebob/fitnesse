// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import fitnesse.slim.test.TestSlim;
import fitnesse.slim.test.Zork;


public class SlimMethodInvocationTest extends SlimMethodInvocationTestBase {
  @Override
  protected String getTestClassName() {
    return "fitnesse.slim.test.TestSlim";
  }

  @Before
  @Override
  public void setUp() {
    caller = new StatementExecutor();
    caller.create("testSlim", getTestClassName(), new Object[0]);
    testSlim = (TestSlim) caller.getInstance("testSlim");
  }

  @Test
  public void passAndReturnOneZorkWithPropertyEditor() throws Exception {
    Object retval = caller.call("testSlim", "oneZork", "zork_42");
    assertEquals(new Zork(42), testSlim.getZork());
    assertEquals("zork_42", retval);
  }
}
