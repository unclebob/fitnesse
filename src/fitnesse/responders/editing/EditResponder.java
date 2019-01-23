// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.editing;

import java.io.UnsupportedEncodingException;

import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.html.HtmlUtil;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.html.template.HtmlPage;
import fitnesse.html.template.PageTitle;
import fitnesse.wiki.MockingPageCrawler;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class EditResponder implements SecureResponder {
  public static final String CONTENT_INPUT_NAME = "pageContent";
  public static final String TIME_STAMP = "editTime";
  public static final String TICKET_ID = "ticketId";
  public static final String HELP_TEXT = "helpText";
  public static final String SUITES = "suites";
  public static final String PAGE_TYPE = "pageType";
  public static final String PAGE_NAME = "pageName";
  public static final String TEMPLATE_MAP = "templateMap";

  protected String content;
  protected WikiPage page;
  protected WikiPage root;
  protected PageData pageData;
  protected Request request;

  public EditResponder() {
  }

  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    boolean nonExistent = request.hasInput("nonExistent");
    return doMakeResponse(context, request, nonExistent);
  }

  public Response makeResponseForNonExistentPage(FitNesseContext context, Request request) throws UnsupportedEncodingException {
    return doMakeResponse(context, request, true);
  }

  protected Response doMakeResponse(FitNesseContext context, Request request, boolean firstTimeForNewPage) throws UnsupportedEncodingException {
    initializeResponder(context.getRootPage(), request);

    SimpleResponse response = new SimpleResponse();
    String resource = request.getResource();
    WikiPagePath path = PathParser.parse(resource);
    PageCrawler crawler = root.getPageCrawler();

    page = crawler.getPage(path, new MockingPageCrawler());
    pageData = page.getData();
    content = createPageContent();

    String html = doMakeHtml(resource, context, firstTimeForNewPage);

    response.setContent(html);
    response.setMaxAge(0);

    return response;
  }


  protected void initializeResponder(WikiPage root, Request request) {
    this.root = root;
    this.request = request;
  }

  protected String createPageContent() {
    return pageData.getContent();
  }

  private String doMakeHtml(String resource, FitNesseContext context, boolean firstTimeForNewPage) {
    HtmlPage html = context.pageFactory.newPage();
    String title = firstTimeForNewPage ? "Page doesn't exist. Edit: " : "Edit: ";
    html.setTitle(title + resource);

    html.setPageTitle(new PageTitle(title + " Page:", PathParser.parse(resource), pageData.getAttribute(PageData.PropertySUITES)));
    html.setMainTemplate("editPage");
    makeEditForm(html, resource, firstTimeForNewPage, NewPageResponder.getDefaultContent(page));

    return html.html(request);
  }

  private void makeEditForm(HtmlPage html, String resource, boolean firstTimeForNewPage, String defaultNewPageContent) {
    html.put("resource", resource);
    html.put(TIME_STAMP, String.valueOf(SaveRecorder.timeStamp()));
    html.put(TICKET_ID, String.valueOf(SaveRecorder.newTicket()));

    if (request.hasInput("redirectToReferer") && request.hasHeader("Referer")) {
      String redirectUrl = request.getHeader("Referer");
      int questionMarkIndex = redirectUrl.indexOf("?");
      if (questionMarkIndex > 0)
        redirectUrl = redirectUrl.substring(0, questionMarkIndex);
      redirectUrl += "?" + request.getInput("redirectAction");
      html.put("redirect", redirectUrl);
    }

    html.put(HELP_TEXT, pageData.getAttribute(PageData.PropertyHELP));
    html.put(TEMPLATE_MAP, TemplateUtil.getTemplateMap(page));
    html.put("suites", pageData.getAttribute(PageData.PropertySUITES));
    html.put(CONTENT_INPUT_NAME, HtmlUtil.escapeHTML(firstTimeForNewPage ? defaultNewPageContent : content));
  }

  @Override
  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }
}
