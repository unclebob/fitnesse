package fitnesse.responders.run;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testrunner.Stoppable;
import fitnesse.testutil.FitNesseUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RunningTestsResponderTest {
  private final Stoppable stoppable = () -> { };

  @Before
  public void setUp() {
    SuiteResponder.runningTestingTracker.addStartedProcess("1", stoppable, "FrontPage.MySuite", "suite");
    SuiteResponder.runningTestingTracker.addStartedProcess("2", stoppable, "FrontPage.MySuite.MyTest", "test");
    SuiteResponder.runningTestingTracker.addStartedProcess("3", stoppable, "FrontPage.OtherSuite", "suite");
  }

  @After
  public void tearDown() {
    SuiteResponder.runningTestingTracker.removeEndedProcess("1");
    SuiteResponder.runningTestingTracker.removeEndedProcess("2");
    SuiteResponder.runningTestingTracker.removeEndedProcess("3");
  }

  @Test
  public void listsAllRunningTestsWithPageAndType() throws Exception {
    JSONArray running = run(new MockRequest()).getJSONArray("running");

    assertEquals(3, running.length());
    Map<String, JSONObject> entries = new HashMap<>();
    for (int i = 0; i < running.length(); i++) {
      JSONObject entry = running.getJSONObject(i);
      entries.put(entry.getString("id"), entry);
    }
    assertEntry(entries.get("1"), "FrontPage.MySuite", "suite");
    assertEntry(entries.get("2"), "FrontPage.MySuite.MyTest", "test");
    assertEntry(entries.get("3"), "FrontPage.OtherSuite", "suite");
  }

  @Test
  public void listsRemainingTestsAfterOneEnded() throws Exception {
    SuiteResponder.runningTestingTracker.removeEndedProcess("2");

    JSONArray running = run(new MockRequest()).getJSONArray("running");

    assertEquals(2, running.length());
    assertFalse(running.toString().contains("FrontPage.MySuite.MyTest"));
  }

  @Test
  public void reportsRequestedRunningTest() throws Exception {
    JSONObject json = run(requestFor("2"));

    assertEquals("2", json.getString("id"));
    assertTrue(json.getBoolean("running"));
    assertEntry(json, "FrontPage.MySuite.MyTest", "test");

    json = run(requestFor("3"));

    assertEquals("3", json.getString("id"));
    assertTrue(json.getBoolean("running"));
    assertEntry(json, "FrontPage.OtherSuite", "suite");
  }

  @Test
  public void reportsUnknownTestAsNotRunning() throws Exception {
    JSONObject json = run(requestFor("42"));

    assertEquals("42", json.getString("id"));
    assertFalse(json.getBoolean("running"));
    assertFalse(json.has("path"));
  }

  private static void assertEntry(JSONObject entry, String path, String type) {
    assertEquals(path, entry.getString("path"));
    assertEquals(type, entry.getString("type"));
  }

  private static MockRequest requestFor(String id) {
    MockRequest request = new MockRequest();
    request.addInput("id", id);
    return request;
  }

  private JSONObject run(MockRequest request) throws Exception {
    FitNesseContext context = FitNesseUtil.makeTestContext();
    SimpleResponse response = (SimpleResponse) new RunningTestsResponder().makeResponse(context, request);
    return new JSONObject(response.getContent());
  }
}
