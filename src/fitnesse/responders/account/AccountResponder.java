package fitnesse.responders.account;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageTitle;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.NotFoundResponder;
import fitnesse.wiki.MockingPageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import static fitnesse.wiki.WikiPageProperty.*;

public class AccountResponder implements Responder {
  private WikiPage page;
  public PageData pageData;
  private String resource;
  private WikiPagePath path;
  private SimpleResponse response;
  private HtmlPage html;

  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {

    response = new SimpleResponse();
    resource = request.getResource();
    path = PathParser.parse(resource);
    PageCrawler crawler = context.getRootPage().getPageCrawler();
    page = crawler.getPage(path, new MockingPageCrawler());
    if (page == null)
      return new NotFoundResponder().makeResponse(context, request);

    pageData = page.getData();
    makeContent(context, request);
    response.setMaxAge(0);
    return response;
  }

  private void makeContent(FitNesseContext context, Request request) throws UnsupportedEncodingException {
    if ("json".equals(request.getInput("format"))) {
      JSONObject jsonObject = makeJson();
      response.setContent(jsonObject.toString(1));
    } else {
      String html = makeHtml(context, request);
      response.setContent(html);
    }
  }

  private JSONObject makeJson() {
    response.setContentType(Response.Format.JSON);
    JSONObject jsonObject = new JSONObject();
    if (pageData.hasAttribute(HELP)) {
      jsonObject.put(HELP, pageData.getAttribute(HELP));
    }
    if (pageData.hasAttribute(SUITES)) {
      JSONArray tags = new JSONArray();
      for (String tag : pageData.getAttribute(SUITES).split(",")) {
        if (StringUtils.isNotBlank(tag)) {
          tags.put(tag.trim());
        }
      }
      jsonObject.put(SUITES, tags);
    }
    return jsonObject;
  }

  private String makeHtml(FitNesseContext context, Request request) {
    html = context.pageFactory.newPage();
    html.setNavTemplate("viewNav");
    html.put("viewLocation", request.getResource());
    html.setTitle("Account: " + resource);

    String tags = "";
    if (pageData != null) {
      tags = pageData.getAttribute(SUITES);
    }

    html.setPageTitle(new PageTitle("Account", path, tags));
    html.put("pageData", pageData);
    html.setMainTemplate("accountPage");

    return html.html(request);

  }

}
