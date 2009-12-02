package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;

public class CodeCompletionResponder implements Responder {

  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    System.out.println(request.getQueryString());
    SimpleResponse response = new SimpleResponse();
    response.setContent("my fixture one\nmy fixture two\nmy fixture three");
    return response;
  }

}
