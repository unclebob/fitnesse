// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.search;

import fitnesse.*;
import fitnesse.html.*;
import fitnesse.http.*;

public class SearchFormResponder implements Responder
{
	public Response makeResponse(FitNesseContext context, Request request) throws Exception
	{
		SimpleResponse response = new SimpleResponse();
		response.setContent(html(context));

		return response;
	}

	private String html(FitNesseContext context) throws Exception
	{
		HtmlPage page = context.htmlPageFactory.newPage();
		page.body.addAttribute("onload", "document.forms[0].searchString.focus()");
		HtmlUtil.addTitles(page, "Search Form");
		page.main.use(makeRightColumn());
		return page.html();
	}

	private HtmlTag makeRightColumn()
	{
		HtmlTag form = new HtmlTag("form");
		form.addAttribute("action", "search");
		form.addAttribute("method", "post");
		form.add(HtmlUtil.makeInputTag("hidden", "responder", "search"));

		form.add("Search String:");
		form.add(HtmlUtil.makeInputTag("text", "searchString", ""));
		form.add(HtmlUtil.BR);
		form.add(HtmlUtil.makeInputTag("submit", "searchType", "Search Titles!"));
		form.add(HtmlUtil.makeInputTag("submit", "searchType", "Search Content!"));

		form.add(HtmlUtil.BR);
		form.add(HtmlUtil.BR);
		form.add(new HtmlTag("b", "Search Titles!: "));
		form.add("Searches in page titles only.  Will run fairly quickly.");
		form.add(HtmlUtil.BR);
		form.add(new HtmlTag("b", "Search Content!: "));
		form.add("Searches in the content of every page.  Don't hold your breath.");

		return form;
	}

}
