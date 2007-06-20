// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.authentication.*;
import fitnesse.http.*;
import fitnesse.wiki.*;

public class RawContentResponder implements SecureResponder
{
	public Response makeResponse(FitNesseContext context, Request request) throws Exception
	{
		String resource = request.getResource();
		WikiPagePath path = PathParser.parse(resource);
		WikiPage page = context.root.getPageCrawler().getPage(context.root, path);
		if(page == null)
			return new NotFoundResponder().makeResponse(context, request);
		PageData pageData = page.getData();

		SimpleResponse response = new SimpleResponse();
		response.setMaxAge(0);
		response.setContent(pageData.getContent());

		return response;
	}

	public SecureOperation getSecureOperation()
	{
		return new SecureReadOperation();
	}
}
