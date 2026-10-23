// Copyright (C) 2026 by netcare GmbH. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testrunner.Stoppable;
import fitnesse.testutil.FitNesseUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Test;

public class RunningTestsResponderTest {
  private final Stoppable stoppable = () -> { };

  @After
  public void tearDown() {
    SuiteResponder.runningTestingTracker.removeEndedProcess("1");
  }

  @Test
  public void listsRunningTestsWithPageAndType() throws Exception {
    SuiteResponder.runningTestingTracker.addStartedProcess("1", stoppable, "FrontPage.MySuite", "suite");

    JSONArray running = run(new MockRequest()).getJSONArray("running");

    assertEquals(1, running.length());
    JSONObject entry = running.getJSONObject(0);
    assertEquals("1", entry.getString("id"));
    assertEquals("FrontPage.MySuite", entry.getString("path"));
    assertEquals("suite", entry.getString("type"));
  }

  @Test
  public void reportsSingleRunningTest() throws Exception {
    SuiteResponder.runningTestingTracker.addStartedProcess("1", stoppable, "FrontPage.MyTest", "test");
    MockRequest request = new MockRequest();
    request.addInput("id", "1");

    JSONObject json = run(request);

    assertTrue(json.getBoolean("running"));
    assertEquals("FrontPage.MyTest", json.getString("path"));
    assertEquals("test", json.getString("type"));
  }

  @Test
  public void reportsUnknownTestAsNotRunning() throws Exception {
    MockRequest request = new MockRequest();
    request.addInput("id", "42");

    JSONObject json = run(request);

    assertEquals("42", json.getString("id"));
    assertFalse(json.getBoolean("running"));
  }

  private JSONObject run(MockRequest request) throws Exception {
    FitNesseContext context = FitNesseUtil.makeTestContext();
    SimpleResponse response = (SimpleResponse) new RunningTestsResponder().makeResponse(context, request);
    return new JSONObject(response.getContent());
  }
}
