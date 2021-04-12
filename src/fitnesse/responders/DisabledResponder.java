package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.html.template.HtmlPage;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.wiki.WikiPage;

public class DisabledResponder extends BasicResponder {
  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    return responseWith(contentFrom(context, request, null));
  }

  @Override
  protected HtmlPage prepareResponseDocument(FitNesseContext context) {
    HtmlPage responseDocument = context.pageFactory.newPage();
    responseDocument.addTitles("Default Responder");
    responseDocument.setMainTemplate("disabledPage.vm");
    return responseDocument;
  }

}
