package fitnesse.responders.editing;

import java.io.IOException;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageTitle;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.PathParser;

public class ContentFilterResponder implements Responder {

  private final ContentFilter contentFilter;

  public ContentFilterResponder(ContentFilter contentFilter) {
    this.contentFilter = contentFilter;
  }

  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    String resource = request.getResource();
    String content = request.getInput(EditResponder.CONTENT_INPUT_NAME);

    if (!contentFilter.isContentAcceptable(content, resource))
      return makeBannedContentResponse(context, resource);
    return null;
  }

  private Response makeBannedContentResponse(FitNesseContext context, String resource) throws IOException {
    SimpleResponse response = new SimpleResponse();
    HtmlPage html = context.pageFactory.newPage();
    html.setTitle("Edit " + resource);
    html.setPageTitle(new PageTitle("Banned Content", PathParser.parse(resource)));
    html.setMainTemplate("bannedPage.vm");
    response.setContent(html.html());
    return response;
  }

}
