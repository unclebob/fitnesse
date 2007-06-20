// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders;

import fitnesse.*;
import fitnesse.html.*;
import fitnesse.http.*;
import fitnesse.wikitext.widgets.WikiWordWidget;

import java.util.regex.Pattern;

public class NotFoundResponder implements Responder
{
	private String resource;

	public Response makeResponse(FitNesseContext context, Request request) throws Exception
	{
		SimpleResponse response = new SimpleResponse(404);
		resource = request.getResource();

		response.setContent(makeHtml(context));
		return response;
	}

	private String makeHtml(FitNesseContext context) throws Exception
	{
		HtmlPage page = context.htmlPageFactory.newPage();
		HtmlUtil.addTitles(page, "Not Found:" + resource);
		page.main.use(makeRightColumn(resource));
		return page.html();
	}

	private String makeRightColumn(String name) throws Exception
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("The requested resource: <i>" + name + "</i> was not found.");
		if(Pattern.matches(WikiWordWidget.REGEXP, name))
		{
			HtmlTag unorderedListTag = new HtmlTag("ul");
			HtmlTag item = new HtmlTag("li");
			item.add(HtmlUtil.makeLink(name + "?edit", "create this page"));
			unorderedListTag.add(item);
			buffer.append(unorderedListTag.html());
		}
		return buffer.toString();
	}

}
