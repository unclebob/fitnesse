// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.editing;


import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.authentication.SecureWriteOperation;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.*;

public class SaveResponder implements SecureResponder {

  private String user;
  private long ticketId;
  private String savedContent;
  private String helpText;
  private String suites;
  private WikiPage page;
  private PageData data;
  private long editTimeStamp;

  @Override
  public Response makeResponse(FitNesseContext context, Request request) {
    editTimeStamp = getEditTime(request);
    ticketId = getTicketId(request);
    String resource = request.getResource();
    page = getPage(resource, context);
    data = page.getData();
    user = request.getAuthorizationUsername();

    if (editsNeedMerge())
      return new MergeResponder(request).makeResponse(context, request);
    else {
      savedContent = (String) request.getInput(EditResponder.CONTENT_INPUT_NAME);
      helpText = (String) request.getInput(EditResponder.HELP_TEXT);
      suites = (String) request.getInput(EditResponder.SUITES);

      return saveEdits(context, request, page);
    }
  }

  private Response saveEdits(FitNesseContext context, Request request, WikiPage page) {
    Response response = new SimpleResponse();
    setData();
    VersionInfo commitRecord = page.commit(data);
    if (commitRecord != null) {
      response.addHeader("Current-Version", commitRecord.getName());
    }
    context.recentChanges.updateRecentChanges(page);

    if (request.hasInput("redirect"))
      response.redirect("", request.getInput("redirect"));
    else
      response.redirect(context.contextRoot, request.getResource());

    return response;
  }

  private boolean editsNeedMerge() {
    return SaveRecorder.changesShouldBeMerged(editTimeStamp, ticketId, page);
  }

  private long getTicketId(Request request) {
    if (!request.hasInput(EditResponder.TICKET_ID))
      return 0;
    String ticketIdString = (String) request.getInput(EditResponder.TICKET_ID);
    return Long.parseLong(ticketIdString);
  }

  private long getEditTime(Request request) {
    if (!request.hasInput(EditResponder.TIME_STAMP))
      return 0;
    String editTimeStampString = (String) request.getInput(EditResponder.TIME_STAMP);
    return Long.parseLong(editTimeStampString);
  }

  private WikiPage getPage(String resource, FitNesseContext context) {
    WikiPagePath path = PathParser.parse(resource);
    PageCrawler pageCrawler = context.getRootPage().getPageCrawler();
    WikiPage page = pageCrawler.getPage(path);
    if (page == null)
      page = WikiPageUtil.addPage(context.getRootPage(), PathParser.parse(resource));
    return page;
  }

  private void setData() {
    data.setContent(savedContent);
    data.setOrRemoveAttribute(PageData.PropertyHELP, helpText);
    data.setOrRemoveAttribute(PageData.PropertySUITES, suites);
    SaveRecorder.pageSaved(page, ticketId);
    
    data.setOrRemoveAttribute(PageData.LAST_MODIFYING_USER, user);
  }

  @Override
  public SecureOperation getSecureOperation() {
    return new SecureWriteOperation();
  }
}
