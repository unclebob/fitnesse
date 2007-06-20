// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.refactoring;

import fitnesse.*;
import fitnesse.authentication.*;
import fitnesse.components.MovedPageReferenceRenamer;
import fitnesse.html.HtmlUtil;
import fitnesse.http.*;
import fitnesse.responders.*;
import fitnesse.wiki.*;

import java.util.*;

public class MovePageResponder implements SecureResponder
{
	private String nameOfPageToBeMoved;
	private String newParentName;
	private boolean refactorReferences;

	public Response makeResponse(FitNesseContext context, Request request) throws Exception
	{
		newParentName = getNameofNewParent(request);
		refactorReferences = request.hasInput("refactorReferences");

		nameOfPageToBeMoved = request.getResource();
		WikiPagePath path = PathParser.parse(nameOfPageToBeMoved);
		PageCrawler crawler = context.root.getPageCrawler();
		WikiPage pageToBeMoved = crawler.getPage(context.root, path);
		if(pageToBeMoved == null)
			return new NotFoundResponder().makeResponse(context, request);

		WikiPagePath newParentPath = PathParser.parse(newParentName);
		WikiPage newParent = crawler.getPage(context.root, newParentPath);
		if(newParent == null)
			return makeErrorMessageResponder(newParentName + " does not exist.").makeResponse(context, request);

		if(pageCanBeMoved(pageToBeMoved, newParent, path, newParentPath))
		{
			if(refactorReferences)
				refactorReferences(context, pageToBeMoved);
			movePage(context.root, crawler.getFullPath(pageToBeMoved), crawler.getFullPath(newParent));

			SimpleResponse response = new SimpleResponse();
			response.redirect(createRedirectionUrl(newParent, pageToBeMoved));
			return response;
		}
		else
		{
			return makeErrorMessageResponder("").makeResponse(context, request);
		}
	}

	private void refactorReferences(FitNesseContext context, WikiPage pageToBeMoved)
		throws Exception
	{
		MovedPageReferenceRenamer renamer = new MovedPageReferenceRenamer(context.root);
		renamer.renameReferences(pageToBeMoved, newParentName);
	}

	private static String getNameofNewParent(Request request)
	{
		String newParentName = (String) request.getInput("newLocation");
		if(".".equals(newParentName))
			newParentName = "";
		return newParentName;
	}

	private boolean pageCanBeMoved(WikiPage pageToBeMoved, WikiPage newParent, WikiPagePath pageToBeMovedPath, WikiPagePath newParentPath) throws Exception
	{
		return !pageToBeMovedPath.equals(newParentPath) &&
			!selfPage(pageToBeMovedPath, newParentPath) &&
			targetPageDoesntExist(pageToBeMoved.getName(), newParent) &&
			!pageIsAncestorOfNewParent(pageToBeMovedPath, newParentPath);
	}

	public boolean pageIsAncestorOfNewParent(WikiPagePath pageToBeMovedPath, WikiPagePath newParentPath) throws Exception
	{
		return newParentPath.startsWith(pageToBeMovedPath);
	}

	public String createRedirectionUrl(WikiPage newParent, WikiPage pageToBeMoved) throws Exception
	{
		PageCrawler crawler = pageToBeMoved.getPageCrawler();
		if(crawler.isRoot(newParent))
			return pageToBeMoved.getName();
		else
			return PathParser.render(crawler.getFullPath(newParent).addName(pageToBeMoved.getName()));
	}

	public boolean selfPage(WikiPagePath pageToBeMovedPath, WikiPagePath newParentPath) throws Exception
	{
		WikiPagePath originalParentPath = pageToBeMovedPath.parentPath();
		return originalParentPath.equals(newParentPath);
	}

	private Responder makeErrorMessageResponder(String message) throws Exception
	{
		return new ErrorResponder("Cannot move " + makeLink(nameOfPageToBeMoved) + " below " + newParentName + "<br>" + message);
	}

	private String makeLink(String page) throws Exception
	{
		return HtmlUtil.makeLink(page, page).html();
	}

	public static void movePage(WikiPage root, WikiPagePath pageToBeMovedPath, WikiPagePath newParentPath) throws Exception
	{
		PageCrawler crawler = root.getPageCrawler();
		WikiPage movee = crawler.getPage(root, pageToBeMovedPath);
		WikiPagePath movedPagePath = newParentPath.withNameAdded(movee.getName());
		WikiPage movedPage = crawler.addPage(root, movedPagePath, movee.getData().getContent());
		PageData movedData = movedPage.getData();
		PageData oldData = movee.getData();
		movedData.setProperties(oldData.getProperties());
		movedData.getProperties().setLastModificationTime(oldData.getProperties().getLastModificationTime());
		List children = movee.getChildren();
		if(children.size() > 0)
			moveChildren(children, root, movedPagePath);
		movedPage.commit(movedData);
		WikiPagePath originalParentPath = pageToBeMovedPath.parentPath();
		WikiPage parentOfMovedPage = crawler.getPage(root, originalParentPath);
		parentOfMovedPage.removeChildPage(movee.getName());
	}

	private boolean targetPageDoesntExist(String oldWikiPageName, WikiPage newParent) throws Exception
	{
		return !newParent.hasChildPage(oldWikiPageName);
	}

	public static void moveChildren(List children, WikiPage root, WikiPagePath newParentPath) throws Exception
	{
		for(Iterator iterator = children.iterator(); iterator.hasNext();)
		{
			WikiPage page = (WikiPage) iterator.next();
			movePage(root, page.getPageCrawler().getFullPath(page), newParentPath);
		}
	}

	public SecureOperation getSecureOperation()
	{
		return new AlwaysSecureOperation();
	}
}
