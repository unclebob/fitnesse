// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.editing;

import org.apache.velocity.VelocityContext;

import fitnesse.FitNesseContext;
import fitnesse.VelocityFactory;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.components.SaveRecorder;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.html.TagGroup;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.templateUtilities.HtmlPage;
import fitnesse.responders.templateUtilities.PageTitle;
import fitnesse.wiki.*;
import fitnesse.wikitext.Utils;

public class EditResponder implements SecureResponder {
  public static final String CONTENT_INPUT_NAME = "pageContent";
  public static final String TIME_STAMP = "editTime";
  public static final String TICKET_ID = "ticketId";
  public static final String HELP_TEXT = "helpText";

  protected String content;
  protected WikiPage page;
  protected WikiPage root;
  protected PageData pageData;
  protected Request request;
  
  public EditResponder() {
  }

  public Response makeResponse(FitNesseContext context, Request request) {
    boolean nonExistent = request.hasInput("nonExistent");
    return doMakeResponse(context, request, nonExistent);
  }

  public Response makeResponseForNonExistentPage(FitNesseContext context, Request request) {
    return doMakeResponse(context, request, true);
  }

  protected Response doMakeResponse(FitNesseContext context, Request request, boolean firstTimeForNewPage) {
    initializeResponder(context.root, request);

    SimpleResponse response = new SimpleResponse();
    String resource = request.getResource();
    WikiPagePath path = PathParser.parse(resource);
    PageCrawler crawler = context.root.getPageCrawler();
    if (!crawler.pageExists(root, path)) {
      crawler.setDeadEndStrategy(new MockingPageCrawler());
      page = crawler.getPage(root, path);
    } else
      page = crawler.getPage(root, path);

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
    HtmlPage html = context.htmlPageFactory.newPage();
    String title = firstTimeForNewPage ? "Page doesn't exist. Edit " : "Edit ";
    html.setTitle(title + resource + ":");
    
    html.setPageTitle(new PageTitle(title + " Page:", PathParser.parse(resource)));
    html.setMainTemplate("editPage.vm");
    makeEditForm(html, resource, firstTimeForNewPage, context.defaultNewPageContent);
    
    return html.html();
  }

  private void makeEditForm(HtmlPage html, String resource, boolean firstTimeForNewPage, String defaultNewPageContent) {
    html.put("action", resource);
    html.put(TIME_STAMP, String.valueOf(SaveRecorder.timeStamp()));
    html.put(TICKET_ID, String.valueOf(SaveRecorder.newTicket()));
    
    if (request.hasInput("redirectToReferer") && request.hasHeader("Referer")) {
      String redirectUrl = request.getHeader("Referer").toString();
      int questionMarkIndex = redirectUrl.indexOf("?");
      if (questionMarkIndex > 0)
        redirectUrl = redirectUrl.substring(0, questionMarkIndex);
      redirectUrl += "?" + request.getInput("redirectAction").toString();
      html.put("redirect", redirectUrl);
    }

    html.put("helpText", pageData.getAttribute("Help"));
    html.put("pageContent", Utils.escapeHTML(firstTimeForNewPage ? defaultNewPageContent : content));
  }

  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }
}
