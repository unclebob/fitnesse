package fitnesse.responders;

import fitnesse.*;
import fitnesse.http.*;

public abstract class BasicResponder implements Responder
{
  protected Response pageNotFoundResponse(FitNesseContext context, Request request) throws Exception
  {
    return new NotFoundResponder().makeResponse(context, request);
  }

  protected Response responseWith(String content) throws Exception
  {
    SimpleResponse response = new SimpleResponse();
    response.setContentType(getContentType());
    response.setContent(content);
    return response;
  }

  protected String getContentType()
  {
    return Response.DEFAULT_CONTENT_TYPE;
  }
}
