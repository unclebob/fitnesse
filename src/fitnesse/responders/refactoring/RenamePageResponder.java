// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.refactoring;

import fitnesse.*;
import fitnesse.authentication.*;
import fitnesse.components.PageReferenceRenamer;
import fitnesse.html.HtmlUtil;
import fitnesse.http.*;
import fitnesse.responders.*;
import fitnesse.wiki.*;
import fitnesse.wikitext.widgets.WikiWordWidget;

import java.util.*;

public class RenamePageResponder implements SecureResponder
{
	private String qualifiedName;
	private String newName;
	private boolean refactorReferences;

	public Response makeResponse(FitNesseContext context, Request request) throws Exception
	{
		qualifiedName = request.getResource();
		newName = (String) request.getInput("newName");
		refactorReferences = request.hasInput("refactorReferences");

		Response response;

		if(newName != null && !qualifiedName.equals("FrontPage") && WikiWordWidget.isSingleWikiWord(newName))
		{
			PageCrawler pageCrawler = context.root.getPageCrawler();

			WikiPagePath subjectPath = PathParser.parse(qualifiedName);
			WikiPage subjectPage = pageCrawler.getPage(context.root, subjectPath);
			if(subjectPage == null)
				response = new NotFoundResponder().makeResponse(context, request);
			else
			{
				WikiPagePath parentPath = subjectPath.parentPath();
				WikiPage parent = pageCrawler.getPage(context.root, parentPath);
				final boolean pageExists = pageCrawler.pageExists(parent, PathParser.parse(newName));
				if(!pageExists)
				{
					qualifiedName = doRename(context.root, subjectPage, parent, newName, subjectPath);
					response = new SimpleResponse();
					response.redirect(qualifiedName);
				}
				else // already exists
				{
					response = makeErrorMessageResponder(makeLink(newName) + " already exists").makeResponse(context, request);
				}
			}
		}
		else
		{
			response = makeErrorMessageResponder(newName + " is not a valid simple page name.").makeResponse(context, request);
		}

		return response;
	}

	private Responder makeErrorMessageResponder(String message) throws Exception
	{
		return new ErrorResponder("Cannot rename " + makeLink(qualifiedName) + " to " + newName + "<br>" + message);
	}

	private String makeLink(String page) throws Exception
	{
		return HtmlUtil.makeLink(page, page).html();
	}

	private String doRename(WikiPage root, WikiPage pageToRename, WikiPage parent, String newName, WikiPagePath subjectPath) throws Exception
	{
		if(refactorReferences)
			renameReferences(root, pageToRename, newName);
		rename(parent, pageToRename.getName(), newName, root);

		subjectPath.pop();
		subjectPath.addName(newName);
		return PathParser.render(subjectPath);
	}

	private void renameReferences(WikiPage root, WikiPage pageToRename, String newName) throws Exception
	{
		PageReferenceRenamer renamer = new PageReferenceRenamer(root);
		renamer.renameReferences(pageToRename, newName);
	}

	private static boolean rename(WikiPage context, String oldName, String newName, WikiPage root) throws Exception
	{
		if(context.hasChildPage(oldName) && !context.hasChildPage(newName))
		{
			WikiPage originalPage = context.getChildPage(oldName);
			PageCrawler crawler = originalPage.getPageCrawler();
			PageData data = originalPage.getData();

			WikiPage renamedPage = context.addChildPage(newName);
			renamedPage.commit(data);

			List children = originalPage.getChildren();
			for(Iterator iterator = children.iterator(); iterator.hasNext();)
			{
				WikiPage child = (WikiPage) iterator.next();
				MovePageResponder.movePage(root, crawler.getFullPath(child), crawler.getFullPath(renamedPage));
			}

			context.removeChildPage(oldName);
			return true;
		}
		return false;
	}

	public SecureOperation getSecureOperation()
	{
		return new AlwaysSecureOperation();
	}
}
