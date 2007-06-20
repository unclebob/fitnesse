// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.refactoring;

import fitnesse.FitNesseContext;
import fitnesse.authentication.*;
import fitnesse.html.*;
import fitnesse.http.*;
import fitnesse.responders.SecureResponder;
import fitnesse.wiki.*;

import java.util.List;

public class DeletePageResponder implements SecureResponder
{
	public Response makeResponse(FitNesseContext context, Request request) throws Exception
	{
		SimpleResponse response = new SimpleResponse();
		String qualifiedPageName = request.getResource();
		WikiPagePath path = PathParser.parse(qualifiedPageName);

		if(qualifiedPageName.equals("FrontPage"))
			response.redirect("FrontPage");
		else
		{
			String confirmedString = (String) request.getInput("confirmed");
			if("yes".equals(confirmedString))
			{
				String nameOfPageToBeDeleted = path.last();
				path.pop();
				WikiPage parentOfPageToBeDeleted = context.root.getPageCrawler().getPage(context.root, path);
				if(parentOfPageToBeDeleted != null)
					parentOfPageToBeDeleted.removeChildPage(nameOfPageToBeDeleted);
				redirect(path, response);
			}
			else
				response.setContent(buildConfirmationHtml(context.root, qualifiedPageName, context));
		}

		return response;
	}

	private void redirect(WikiPagePath path, SimpleResponse response)
	{
		String location = PathParser.render(path);
		if(location == null || location.length() == 0)
			response.redirect("root");
		else
			response.redirect(location);
	}

	private String buildConfirmationHtml(WikiPage root, String qualifiedPageName, FitNesseContext context) throws Exception
	{
		HtmlPage html = context.htmlPageFactory.newPage();
		html.title.use("Delete Confirmation");
		html.header.use(HtmlUtil.makeBreadCrumbsWithPageType(qualifiedPageName, "/", "Confirm Deletion"));
		html.main.use(makeMainContent(root, qualifiedPageName));
		return html.html();
	}

	private String makeMainContent(WikiPage root, String qualifiedPageName) throws Exception
	{
		WikiPagePath path = PathParser.parse(qualifiedPageName);
		WikiPage pageToDelete = root.getPageCrawler().getPage(root, path);
		List children = pageToDelete.getChildren();
		boolean addSubPageWarning = true;
		if(children == null || children.size() == 0)
			addSubPageWarning = false;

		HtmlTag divTag = HtmlUtil.makeDivTag("centered");
		divTag.add(makeHeadingTag(addSubPageWarning, qualifiedPageName));
		divTag.add(HtmlUtil.BR);
		divTag.add(HtmlUtil.makeLink(qualifiedPageName + "?responder=deletePage&confirmed=yes", "Yes"));
		divTag.add("&nbsp;&nbsp;&nbsp;&nbsp;");
		divTag.add(HtmlUtil.makeLink(qualifiedPageName, "No"));

		return divTag.html();
	}

	private HtmlTag makeHeadingTag(boolean addSubPageWarning, String qualifiedPageName)
	{
		HtmlTag h3Tag = new HtmlTag("H3");
		if(addSubPageWarning)
		{
			h3Tag.add("Warning, this page contains one or more subpages.");
			h3Tag.add(HtmlUtil.BR);
		}
		h3Tag.add("Are you sure you want to delete " + qualifiedPageName + "?");
		return h3Tag;
	}

	public SecureOperation getSecureOperation()
	{
		return new AlwaysSecureOperation();
	}
}
