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
import fitnesse.html.HtmlPage;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
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
    html.setMainContent(makeEditForm(context, request));

    return html.html();
  }

  private String makeEditForm(FitNesseContext context, Request request) {
    VelocityContext velocityContext = new VelocityContext();
    velocityContext.put("action", request.getResource());

    velocityContext.put("isNewPage", true);
    velocityContext.put("helpText", "");
    velocityContext.put("pageContent", context.defaultNewPageContent);
    velocityContext.put("pageTypes", PAGE_TYPE_ATTRIBUTES);

    return VelocityFactory.translateTemplate(velocityContext, "editPage.vm");
  }

  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }

}
