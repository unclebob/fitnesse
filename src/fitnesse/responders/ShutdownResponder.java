// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders;

import fitnesse.*;
import fitnesse.authentication.*;
import fitnesse.html.*;
import fitnesse.http.*;

public class ShutdownResponder implements SecureResponder
{
	public Response makeResponse(FitNesseContext context, Request request) throws Exception
	{
		SimpleResponse response = new SimpleResponse();

		HtmlPage html = context.htmlPageFactory.newPage();
		html.title.use("Shutdown");
		html.header.use(HtmlUtil.makeSpanTag("page_title", "Shutdown"));

		HtmlTag content = HtmlUtil.makeDivTag("centered");
		content.add(new HtmlTag("h3", "FitNesse is shutting down..."));

		html.main.use(content);
		response.setContent(html.html());

		final FitNesse fitnesseInstance = context.fitnesse;

		Thread shutdownThread = new Thread()
		{
			public void run()
			{
				try
				{
					fitnesseInstance.stop();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		};
		shutdownThread.start();

		return response;
	}

	public SecureOperation getSecureOperation()
	{
		return new AlwaysSecureOperation();
	}
}
