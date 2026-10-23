package fitnesse.responders.run;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.testrunner.RunningTestingTracker;
import org.json.JSONArray;
import org.json.JSONObject;

/** GET ?responder=runningTests[&id=<X-FitNesse-Test-Id>] -> JSON */
public class RunningTestsResponder implements Responder {
  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    RunningTestingTracker tracker = SuiteResponder.runningTestingTracker;

    JSONObject json = new JSONObject();
    String id = request.getInput("id");
    if (id != null) {
      json.put("id", id);
      json.put("running", tracker.isRunning(id));
    } else {
      json.put("running", new JSONArray(tracker.getRunningIds()));
    }

    SimpleResponse response = new SimpleResponse();
    response.setContentType("application/json");
    response.setContent(json.toString());
    return response;
  }
}
