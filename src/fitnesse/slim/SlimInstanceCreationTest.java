// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import static org.junit.Assert.assertTrue;

import org.junit.Before;

import fitnesse.slim.test.TestSlim;

public class SlimInstanceCreationTest extends SlimInstanceCreationTestBase {

  @Before
  @Override
  public void setUp() throws Exception {
    caller = new StatementExecutor();
  }

  @Override
  protected void assertInstanceOfTestSlim(Object x) {
    assertTrue(x instanceof TestSlim);
  }

  @Override
  protected String getTestClassPath() {
    return "fitnesse.slim.test";
  }

}
