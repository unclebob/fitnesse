// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fitnesse.FitNesseContext;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.http.MockRequest;
import fitnesse.http.RequestBuilder;
import fitnesse.http.ResponseParser;
import fitnesse.testutil.FitNesseUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ShutdownResponderTest {
  private FitNesseContext context;
  private boolean doneShuttingDown;

  @Before
  public void setUp() throws Exception {
    context = FitNesseUtil.makeTestContext();
    context.fitNesse.start();
  }

  @After
  public void tearDown() throws Exception {
    context.fitNesse.stop();
  }

  @Test
  public void testFitNesseGetsShutdown() throws Exception {
    ShutdownResponder responder = new ShutdownResponder();
    responder.makeResponse(context, new MockRequest());
    Thread.sleep(200);
    assertFalse(context.fitNesse.isRunning());
  }

  @Test
  public void testShutdownCalledFromServer() throws Exception {
    Thread thread = new Thread() {
      public void run() {
        try {
          RequestBuilder request = new RequestBuilder("/?responder=shutdown");
          ResponseParser.performHttpRequest("localhost", FitNesseUtil.PORT, request);
          doneShuttingDown = true;
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    };
    thread.start();

    Thread.sleep(500);

    assertTrue(doneShuttingDown);
    assertFalse(context.fitNesse.isRunning());
  }

  @Test
  public void testIsSecure() throws Exception {
    assertTrue((new ShutdownResponder().getSecureOperation() instanceof AlwaysSecureOperation));
  }
}
