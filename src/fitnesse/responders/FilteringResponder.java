package fitnesse.responders;

import java.util.List;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.Request;
import fitnesse.http.Response;

public class FilteringResponder implements Responder {
  private final List<Responder> filters;
  private final Responder responder;

  public FilteringResponder(List<Responder> filters, Responder responder) {
    this.filters = filters;
    this.responder = responder;
  }

  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    Response response;
    for (Responder filter : filters) {
      response = filter.makeResponse(context, request);
      if (response != null) {
        return response;
      }
    }
    return responder.makeResponse(context, request);
  }
}
