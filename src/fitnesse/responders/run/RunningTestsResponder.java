// Copyright (C) 2026 by netcare GmbH. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.testrunner.RunningTestingTracker;
import fitnesse.testrunner.RunningTestingTracker.RunningProcess;
import org.json.JSONArray;
import org.json.JSONObject;

/** GET ?responder=runningTests[&id=<X-FitNesse-Test-Id>] -> JSON */
public class RunningTestsResponder implements Responder {
  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    RunningTestingTracker tracker = SuiteResponder.runningTestingTracker;

    JSONObject json;
    String id = request.getInput("id");
    if (id != null) {
      RunningProcess process = tracker.getRunningProcess(id);
      json = process != null ? toJson(process) : new JSONObject().put("id", id);
      json.put("running", process != null);
    } else {
      JSONArray running = new JSONArray();
      for (RunningProcess process : tracker.getRunningProcesses()) {
        running.put(toJson(process));
      }
      json = new JSONObject().put("running", running);
    }

    SimpleResponse response = new SimpleResponse();
    response.setContentType("application/json");
    response.setContent(json.toString());
    return response;
  }

  private static JSONObject toJson(RunningProcess process) {
    JSONObject json = new JSONObject();
    json.put("id", process.getId());
    json.put("path", process.getPagePath());
    json.put("type", process.getType());
    return json;
  }
}
