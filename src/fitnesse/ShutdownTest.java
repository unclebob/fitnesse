// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static util.RegexTestCase.assertNotSubString;
import static util.RegexTestCase.assertSubString;

import fitnesse.http.ResponseParser;
import org.junit.Before;
import org.junit.Test;

public class ShutdownTest {
  private Shutdown shutdown;

  @Before
  public void setUp() throws Exception {
    shutdown = new Shutdown();
  }

  @Test
  public void testArgs() throws Exception {
    assertTrue(shutdown.parseArgs(new String[]{}));
    assertEquals("localhost", shutdown.hostname);
    assertEquals(80, shutdown.port);
    assertEquals(null, shutdown.username);
    assertEquals(null, shutdown.password);

    assertTrue(shutdown.parseArgs(new String[]{"-h", "host.com", "-p", "1234", "-c", "user", "pass"}));
    assertEquals("host.com", shutdown.hostname);
    assertEquals(1234, shutdown.port);
    assertEquals("user", shutdown.username);
    assertEquals("pass", shutdown.password);
  }

  @Test
  public void testBuildRequest() throws Exception {
    String request = shutdown.buildRequest().getText();
    assertSubString("GET /?responder=shutdown", request);
    assertNotSubString("Authorization: ", request);

    shutdown.username = "user";
    shutdown.password = "pass";
    request = shutdown.buildRequest().getText();
    assertSubString("Authorization: ", request);
  }

  @Test
  public void testBadServer() throws Exception {
    try {
      shutdown.hostname = "http://google.com";
      ResponseParser response = shutdown.buildAndSendRequest();
      String status = shutdown.checkResponse(response);
      assertEquals("Not a FitNesse server", status);
    }
    catch (Exception e) {
    }
  }
}
