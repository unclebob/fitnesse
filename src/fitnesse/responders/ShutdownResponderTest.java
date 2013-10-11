// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.FitNesseShutdownException;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.http.MockRequest;
import fitnesse.testutil.FitNesseUtil;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ShutdownResponderTest {
  private FitNesseContext context;

  @Before
  public void setUp() throws Exception {
    context = FitNesseUtil.makeTestContext(FitNesseUtil.PORT);
  }

  @Test(expected = FitNesseShutdownException.class)
  public void testFitNesseGetsShutdown() throws Exception {
    ShutdownResponder responder = new ShutdownResponder();
    responder.makeResponse(context, new MockRequest());
  }

  @Test
  public void testIsSecure() throws Exception {
    assertTrue((new ShutdownResponder().getSecureOperation() instanceof AlwaysSecureOperation) == true);
  }
}
