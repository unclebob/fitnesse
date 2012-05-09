package fitnesse.responders.editing;

import static fitnesse.wiki.PageData.PAGE_TYPE_ATTRIBUTES;
import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.templateUtilities.HtmlPage;
import fitnesse.responders.templateUtilities.PageTitle;
import fitnesse.wiki.PageType;
import fitnesse.wiki.PathParser;

public class NewPageResponder implements Responder {

  public Response makeResponse(FitNesseContext context, Request request) {
    
    SimpleResponse response = new SimpleResponse();
    response.setContent(doMakeHtml(context, request));
    return response;
  }

  private String doMakeHtml(FitNesseContext context, Request request) {
    HtmlPage html = context.pageFactory.newPage();
    html.setTitle("New page:");

    html.setPageTitle(new PageTitle("New Page", PathParser.parse(request.getResource())));
    html.setMainTemplate("editPage");
    makeEditForm(html, context, request);
    
    return html.html();
  }

  private void makeEditForm(HtmlPage html, FitNesseContext context, Request request) {
    html.put("resource", request.getResource());

    html.put("isNewPage", true);
    html.put(EditResponder.HELP_TEXT, "");
    html.put(EditResponder.CONTENT_INPUT_NAME, context.defaultNewPageContent);
    if (request.hasInput("pageType")) {
      String pageType = (String) request.getInput("pageType");
      // Validate page type:
      PageType.fromString(pageType);
      html.put(EditResponder.PAGE_TYPE, pageType);
    } else {
      html.put("pageTypes", PAGE_TYPE_ATTRIBUTES);
    }
  }

  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }

}
