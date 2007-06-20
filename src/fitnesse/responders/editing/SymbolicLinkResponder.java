// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.editing;

import fitnesse.*;
import fitnesse.http.*;
import fitnesse.responders.*;
import fitnesse.wiki.*;

import java.io.File;

public class SymbolicLinkResponder implements Responder
{
	private Response response;
	private String resource;
	private PageCrawler crawler;
	private FitNesseContext context;

	public Response makeResponse(FitNesseContext context, Request request) throws Exception
	{
		resource = request.getResource();
		this.context = context;
		crawler = context.root.getPageCrawler();
		WikiPage page = crawler.getPage(context.root, PathParser.parse(resource));
		if(page == null)
			return new NotFoundResponder().makeResponse(context, request);

		response = new SimpleResponse();
		if(request.hasInput("removal"))
			removeSymbolicLink(request, page);
		else
			addSymbolicLink(request, page);

		return response;
	}

	private void setRedirect(String resource)
	{
		response.redirect(resource + "?properties");
	}

	private void removeSymbolicLink(Request request, WikiPage page) throws Exception
	{
		String linkToRemove = (String) request.getInput("removal");

		PageData data = page.getData();
		WikiPageProperties properties = data.getProperties();
		WikiPageProperty symLinks = getSymLinkProperty(properties);
		symLinks.remove(linkToRemove);
		if(symLinks.keySet().size() == 0)
			properties.remove(SymbolicPage.PROPERTY_NAME);
		page.commit(data);
		setRedirect(resource);
	}

	private void addSymbolicLink(Request request, WikiPage page) throws Exception
	{
		String linkName = (String) request.getInput("linkName");
		String linkPath = (String) request.getInput("linkPath");

		if(isFilePath(linkPath) && !isValidDirectoryPath(linkPath))
		{
			String message = "Cannot create link to the file system path, <b>" + linkPath + "</b>." +
				"<br> The canonical file system path used was <b>" + createFileFromPath(linkPath).getCanonicalPath() + ".</b>" +
				"<br>Either it doesn't exist or it's not a directory.";
			response = new ErrorResponder(message).makeResponse(context, null);
			response.setStatus(404);
		}
		else if(!isFilePath(linkPath) && isInternalPageThatDoesntExist(linkPath))
		{
			response = new ErrorResponder("The page to which you are attemting to link, " + linkPath + ", doesn't exist.").makeResponse(context, null);
			response.setStatus(404);
		}
		else if(page.hasChildPage(linkName))
		{
			response = new ErrorResponder(resource + " already has a child named " + linkName + ".").makeResponse(context, null);
			response.setStatus(412);
		}
		else
		{
			PageData data = page.getData();
			WikiPageProperties properties = data.getProperties();
			WikiPageProperty symLinks = getSymLinkProperty(properties);
			symLinks.set(linkName, linkPath);
			page.commit(data);
			setRedirect(resource);
		}
	}

	private boolean isValidDirectoryPath(String linkPath) throws Exception
	{
		File file = createFileFromPath(linkPath);

		if(file.exists())
			return file.isDirectory();
		else
		{
			File parentDir = file.getParentFile();
			return parentDir.exists() && parentDir.isDirectory();
		}
	}

	private File createFileFromPath(String linkPath)
	{
		String pathToFile = linkPath.substring(7);
		return new File(pathToFile);
	}

	private boolean isFilePath(String linkPath)
	{
		return linkPath.startsWith("file://");
	}

	private boolean isInternalPageThatDoesntExist(String linkPath) throws Exception
	{
		return !crawler.pageExists(context.root, PathParser.parse(linkPath));
	}

	private WikiPageProperty getSymLinkProperty(WikiPageProperties properties)
	{
		WikiPageProperty symLinks = properties.getProperty(SymbolicPage.PROPERTY_NAME);
		if(symLinks == null)
			symLinks = properties.set(SymbolicPage.PROPERTY_NAME);
		return symLinks;
	}
}
