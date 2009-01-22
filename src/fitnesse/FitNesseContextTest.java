// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import junit.framework.TestCase;

public class FitNesseContextTest extends TestCase {
  public void testShouldReportPortOfMinusOneIfNotInitialized() {
    FitNesseContext.globalContext = null;
    assertEquals(-1, FitNesseContext.getPort());
  }

  public void testShouldHavePortSetAfterFitNesseObjectConstructed() throws Exception {
    FitNesseContext context = new FitNesseContext();
    context.port = 9988;
    new FitNesse(context, false);
    assertEquals(9988, FitNesseContext.getPort());
  }
}
