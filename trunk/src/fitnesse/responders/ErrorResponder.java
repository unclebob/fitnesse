// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders;

import fitnesse.*;
import fitnesse.html.*;
import fitnesse.http.*;

public class ErrorResponder implements Responder
{
	Exception exception;
	private String message;

	public ErrorResponder(Exception e)
	{
		exception = e;
	}

	public ErrorResponder(String message)
	{
		this.message = message;
	}

	public Response makeResponse(FitNesseContext context, Request request) throws Exception
	{
		SimpleResponse response = new SimpleResponse(400);
		HtmlPage html = context.htmlPageFactory.newPage();
		HtmlUtil.addTitles(html, "Error Occured");
		if(exception != null)
			html.main.add("<pre>" + makeExceptionString(exception) + "</pre>");
		if(message != null)
			html.main.add(makeErrorMessage());
		response.setContent(html.html());

		return response;
	}

	public static String makeExceptionString(Exception e)
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append(e.toString()).append("\n");
		StackTraceElement[] stackTreace = e.getStackTrace();
		for(int i = 0; i < stackTreace.length; i++)
			buffer.append("\t" + stackTreace[i]).append("\n");

		return buffer.toString();
	}

	public HtmlTag makeErrorMessage()
	{
		HtmlTag tag = HtmlUtil.makeDivTag("centered");
		tag.add(message);
		return tag;
	}
}
