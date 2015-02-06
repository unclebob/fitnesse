package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.html.template.HtmlPage;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.wiki.WikiPage;

public class DisabledResponder extends BasicResponder {

  @Override
  public Response makeResponse(FitNesseContext context, Request request) {
    return responseWith(contentFrom(context, request, null));
  }

  @Override
  protected String contentFrom(FitNesseContext context, Request request, WikiPage requestedPage) {
    return prepareResponseDocument(context).html();
  }

  private HtmlPage prepareResponseDocument(FitNesseContext context) {
    HtmlPage responseDocument = context.pageFactory.newPage();
    responseDocument.addTitles("Default Responder");
    responseDocument.setMainTemplate("disabledPage.vm");
    return responseDocument;
  }

}
