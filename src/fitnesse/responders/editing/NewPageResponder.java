package fitnesse.responders.editing;

import static fitnesse.wiki.PageData.PAGE_TYPE_ATTRIBUTES;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageTitle;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageType;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class NewPageResponder implements Responder {
  public static final String DEFAULT_PAGE_CONTENT_PROPERTY = "newpage.default.content";
  public static final String DEFAULT_PAGE_CONTENT = "!contents -R2 -g -p -f -h";

  public static final String PAGE_TEMPLATE = "pageTemplate";
  public static final String PAGE_TYPE = "pageType";
  public static final String PAGE_TYPES = "pageTypes";

  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {

    SimpleResponse response = new SimpleResponse();
    response.setContent(doMakeHtml(context, request));
    return response;
  }

  private String doMakeHtml(FitNesseContext context, Request request) {
    HtmlPage html = context.pageFactory.newPage();
    html.setTitle("New page");

    html.setPageTitle(new PageTitle("New Page", PathParser.parse(request.getResource())));
    html.setMainTemplate("editPage");
    makeEditForm(html, context, request);

    return html.html();
  }

  private void makeEditForm(HtmlPage html, FitNesseContext context, Request request) {
    html.put("resource", request.getResource());

    html.put("isNewPage", true);
    html.put(EditResponder.HELP_TEXT, "");

    WikiPage parentWikiPage = getParentWikiPage(context, request);
    html.put(EditResponder.TEMPLATE_MAP, TemplateUtil.getTemplateMap(parentWikiPage));
    if (request.hasInput(PAGE_TEMPLATE)) {
      PageCrawler crawler = context.getRootPage().getPageCrawler();
      String pageTemplate = request.getInput(PAGE_TEMPLATE);
      WikiPage template = crawler.getPage(PathParser.parse(pageTemplate));
      html.put(EditResponder.CONTENT_INPUT_NAME, template.getData().getContent());
      html.put(EditResponder.PAGE_TYPE, PageType.fromWikiPage(template));
      html.put(PAGE_TEMPLATE, pageTemplate);
    } else if (request.hasInput(PAGE_TYPE)) {
      String pageType = request.getInput(PAGE_TYPE);
      // Validate page type:
      PageType.fromString(pageType);
      html.put(EditResponder.PAGE_TYPE, pageType);
      html.put(EditResponder.CONTENT_INPUT_NAME, getDefaultContent(parentWikiPage));
    } else {
      html.put(PAGE_TYPES, PAGE_TYPE_ATTRIBUTES);
      html.put(EditResponder.CONTENT_INPUT_NAME, getDefaultContent(parentWikiPage));
    }
  }

  public static String getDefaultContent(WikiPage page) {
    String content = page.getVariable(DEFAULT_PAGE_CONTENT_PROPERTY);
    if (content == null) {
      content = DEFAULT_PAGE_CONTENT;
    }
    return content;
  }

  private WikiPage getParentWikiPage(FitNesseContext context, Request request) {
    //the request resource is already th parent path.
    WikiPagePath parentPath = PathParser.parse(request.getResource());

    //we need a pageBuilder to get the page from the path. The root has a pageBuilder we can use.
    PageCrawler crawler = context.getRootPage().getPageCrawler();
    WikiPage page = crawler.getPage(parentPath);
    return page;
  }

  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }

}
