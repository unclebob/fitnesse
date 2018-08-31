// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

// Extracted Test class to be implemented by all Java based Slim ports

public abstract class SlimGetSymbolTestBase {
  protected StatementExecutorInterface caller;

  @Before
  public abstract void setUp() throws Exception;

  @Test
  public void getUnsetSymbol() throws Exception {
    Object symbol = caller.getSymbol("vavavar");
    assertNull(symbol);
  }

  @Test
  public void getSetSymbol() throws Exception {
    caller.assign("vavavar", "thisisassigned");
    Object symbol = caller.getSymbol("vavavar");
    assertEquals("thisisassigned", symbol);
  }

  @Test
  public void getSetSymbolInteger() throws Exception {
    caller.assign("vavavar", 12);
    Object symbol = caller.getSymbol("vavavar");
    assertEquals("12", symbol);
  }

  @Test
  public void getSetSymbolObjectInteger() throws Exception {
    caller.assign("vavavar", 12);
    Object symbol = caller.getSymbolObject("vavavar");
    assertEquals(12, symbol);
  }

}
