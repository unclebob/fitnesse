// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.versions;

import fitnesse.FitNesseContext;
import fitnesse.authentication.*;
import fitnesse.components.RecentChanges;
import fitnesse.http.*;
import fitnesse.responders.*;
import fitnesse.wiki.*;

public class RollbackResponder implements SecureResponder
{
	public Response makeResponse(FitNesseContext context, Request request) throws Exception
	{
		SimpleResponse response = new SimpleResponse();

		String resource = request.getResource();
		String version = (String) request.getInput("version");

		WikiPagePath path = PathParser.parse(resource);
		WikiPage page = context.root.getPageCrawler().getPage(context.root, path);
		if(page == null)
			return new NotFoundResponder().makeResponse(context, request);
		PageData data = page.getDataVersion(version);

		page.commit(data);

		RecentChanges.updateRecentChanges(data);
		response.redirect(resource);

		return response;
	}

	public SecureOperation getSecureOperation()
	{
		return new SecureWriteOperation();
	}
}
