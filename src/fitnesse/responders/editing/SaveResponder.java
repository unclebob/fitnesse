// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.editing;

import fitnesse.*;
import fitnesse.responders.SecureResponder;
import fitnesse.authentication.*;
import fitnesse.components.*;
import fitnesse.http.*;
import fitnesse.wiki.*;

public class SaveResponder implements SecureResponder
{
	private String user;
	private long ticketId;
	private String savedContent;
	private PageData data;

	public Response makeResponse(FitNesseContext context, Request request) throws Exception
	{
		String resource = request.getResource();
    WikiPage page = getPage(resource, context);
    data = page.getData();
    user = request.getAuthorizationUsername();

    if(editsNeedMerge(request))
			return new MergeResponder(request).makeResponse(context, request);
		else
		{
      return saveEdits(request, page);
		}
	}

  private Response saveEdits(Request request, WikiPage page) throws Exception
  {
    Response response = new SimpleResponse();
    savedContent = (String) request.getInput(EditResponder.CONTENT_INPUT_NAME);
    setData();
    VersionInfo commitRecord = page.commit(data);
    response.addHeader("Previous-Version", commitRecord.getName());
    RecentChanges.updateRecentChanges(data);
    response.redirect(request.getResource());
    return response;
  }

  private boolean editsNeedMerge(Request request) throws Exception
  {
    String saveIdString = (String) request.getInput(EditResponder.SAVE_ID);
    long saveId = Long.parseLong(saveIdString);

    String ticketIdString = (String) request.getInput(EditResponder.TICKET_ID);
    ticketId = Long.parseLong(ticketIdString);

    boolean shouldMerge = SaveRecorder.changesShouldBeMerged(saveId, ticketId, data);
    return shouldMerge;
  }

  private WikiPage getPage(String resource, FitNesseContext context) throws Exception
  {
    WikiPagePath path = PathParser.parse(resource);
	  PageCrawler pageCrawler = context.root.getPageCrawler();
	  WikiPage page = pageCrawler.getPage(context.root, path);
    if(page == null)
      page = pageCrawler.addPage(context.root, PathParser.parse(resource));
    return page;
  }

  private void setData() throws Exception
	{
		data.setContent(savedContent);
		data.setAttribute(EditResponder.TICKET_ID, ticketId + "");
		SaveRecorder.pageSaved(data);
		if(user != null)
			data.setAttribute(WikiPage.LAST_MODIFYING_USER, user);
		else
			data.removeAttribute(WikiPage.LAST_MODIFYING_USER);
	}

	public SecureOperation getSecureOperation()
	{
		return new SecureWriteOperation();
	}
}
