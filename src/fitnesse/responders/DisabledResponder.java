package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.html.template.HtmlPage;
import fitnesse.html.HtmlUtil;
import fitnesse.http.Request;
import fitnesse.http.Response;

public class DisabledResponder extends BasicResponder {
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    String content = prepareResponseDocument(context).html();
    return responseWith(content);
  }

  private HtmlPage prepareResponseDocument(FitNesseContext context) {
    HtmlPage responseDocument = context.pageFactory.newPage();
    responseDocument.addTitles("Default Responder");
    responseDocument.setMainTemplate("disabledPage.vm");
    return responseDocument;
  }

}
