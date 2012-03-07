package fitnesse.responders.editing;

import static fitnesse.responders.editing.EditResponder.TIME_STAMP;
import static fitnesse.responders.editing.EditResponder.TICKET_ID;
import static fitnesse.responders.editing.EditResponder.TIME_STAMP;
import static fitnesse.wiki.PageData.PAGE_TYPE_ATTRIBUTES;

import org.apache.velocity.VelocityContext;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.VelocityFactory;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.components.SaveRecorder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.templateUtilities.HtmlPage;
import fitnesse.responders.templateUtilities.PageTitle;
import fitnesse.wiki.PathParser;
import fitnesse.wikitext.Utils;

public class NewPageResponder implements Responder {

  public Response makeResponse(FitNesseContext context, Request request) {
    
    SimpleResponse response = new SimpleResponse();
    response.setContent(doMakeHtml(context, request));
    return response;
  }

  private String doMakeHtml(FitNesseContext context, Request request) {
    HtmlPage html = context.htmlPageFactory.newPage();
    html.setTitle("New page:");

    html.setPageTitle(new PageTitle("New Page", PathParser.parse(request.getResource())));
    html.setMainTemplate("editPage.vm");
    makeEditForm(html, context, request);
    
    return html.html();
  }

  private void makeEditForm(HtmlPage html, FitNesseContext context, Request request) {
    html.put("action", request.getResource());

    html.put("isNewPage", true);
    html.put("helpText", "");
    html.put("pageContent", context.defaultNewPageContent);
    html.put("pageTypes", PAGE_TYPE_ATTRIBUTES);
  }

  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }

}
