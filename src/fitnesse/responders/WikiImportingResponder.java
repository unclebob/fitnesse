// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders;

import fitnesse.wiki.*;
import fitnesse.util.*;
import fitnesse.html.*;
import fitnesse.http.*;
import fitnesse.authentication.*;
import java.net.*;
import java.io.*;
import org.w3c.dom.Document;

public class WikiImportingResponder extends ChunkingResponder implements XmlizerPageHandler, SecureResponder
{
	public XmlizerPageHandler xmlizerPageHandler = this;
	public String remoteHostname;
	public int remotePort;
	public WikiPagePath remotePath = new WikiPagePath();
	public WikiPagePath relativePath = new WikiPagePath();
	private int alternation = 0;
	private int importCount = 0;
	private boolean isUpdate;
	private boolean isNonRoot;
	private PageData data;

	public static String remoteUsername;
	public static String remotePassword;

	protected void doSending() throws Exception
	{
		data = page.getData();
		String remoteWikiUrl = establishRemoteUrlAndUpdateStyle();
		HtmlPage html = makeHtml();
		response.add(html.preDivision);

		try
		{
			setRemoteUserCredentials();
			parseUrl(remoteWikiUrl);
			Document pageTree = getPageTree();
			addHeadContent();
			if(isNonRoot)
				importRemotePageContent(page);
			new PageXmlizer().deXmlizeSkippingRootLevel(pageTree, page, xmlizerPageHandler);
			addTailContent();

			if(!isUpdate)
			{
				data.setAttribute("WikiImportRoot", remoteUrl());
				page.commit(data);
			}
		}
		catch(MalformedURLException e)
		{
			writeErrorMessage(e.getMessage());
		}
		catch(FileNotFoundException e)
		{
			writeErrorMessage("The remote resource, " + remoteUrl() + ", was not found.");
		}
		catch(AuthenticationRequiredException e)
		{
			writeAuthenticationForm(e.getMessage());
		}
		catch(Exception e)
		{
			writeErrorMessage(e.toString());
		}

		response.add(html.postDivision);
		response.closeAll();
	}

	private void setRemoteUserCredentials()
	{
		if(request.hasInput("remoteUsername"))
			remoteUsername = (String) request.getInput("remoteUsername");
		if(request.hasInput("remotePassword"))
			remotePassword = (String) request.getInput("remotePassword");
	}

	private String establishRemoteUrlAndUpdateStyle() throws Exception
	{
		String remoteWikiUrl = (String) request.getInput("remoteUrl");
		if(data.hasAttribute("WikiImportRoot"))
		{
			remoteWikiUrl = data.getAttribute("WikiImportRoot");
			isUpdate = true;
		}
		else if(data.hasAttribute("WikiImportSource"))
		{
			remoteWikiUrl = data.getAttribute("WikiImportSource");
			isUpdate = true;
			isNonRoot = true;
		}
		return remoteWikiUrl;
	}

	private void writeErrorMessage(String message) throws Exception
	{
		HtmlTag alert = HtmlUtil.makeDivTag("centered");
		alert.add(new HtmlTag("h2", "Import Failure"));
		alert.add(message);
		response.add(alert.html());
	}

	private void addHeadContent() throws Exception
	{
		TagGroup head = new TagGroup();
		if(isUpdate)
			head.add("Updating imported wiki.");
		else
			head.add("Importing wiki.");
		head.add(" This may take a few moments.");
		head.add(HtmlUtil.BR);
		head.add(HtmlUtil.BR);
		head.add("Destination wiki: ");
		String pageName = PathParser.render(path);
		head.add(HtmlUtil.makeLink(pageName, pageName));

		head.add(HtmlUtil.BR);
		head.add("Source wiki: ");
		String remoteWikiUrl = remoteUrl();
		head.add(HtmlUtil.makeLink(remoteWikiUrl, remoteWikiUrl));

		head.add(HtmlUtil.BR);
		head.add(HtmlUtil.BR);
		head.add("Imported pages:");
		head.add(HtmlUtil.HR);
		response.add(head.html());
	}

	private void addTailContent() throws Exception
	{
		TagGroup tail = new TagGroup();
		tail.add("<a name=\"end\"><hr></a>");
		tail.add("Import complete. ");
		if(importCount == 1)
			tail.add("1 page was imported.");
		else
			tail.add(importCount + " pages were imported.");
		response.add(tail.html());
	}

	private HtmlPage makeHtml() throws Exception
	{
		HtmlPage html = context.htmlPageFactory.newPage();
		html = context.htmlPageFactory.newPage();
		String title = "Wiki Import";
		if(isUpdate)
			title += " Update";
		String localPathName = PathParser.render(path);
		html.title.use(title + ": " + localPathName);
		html.header.use(HtmlUtil.makeBreadCrumbsWithPageType(localPathName, title));
		html.main.add(HtmlPage.BreakPoint);
		html.divide();
		return html;
	}

	protected PageCrawler getPageCrawler()
	{
		return root.getPageCrawler();
	}

	public void pageAdded(WikiPage newPage) throws Exception
	{
		remotePath.addName(newPage.getName());
		relativePath.addName(newPage.getName());
		path.addName(newPage.getName());
		importRemotePageContent(newPage);
	}

	private void importRemotePageContent(WikiPage localPage) throws Exception
	{
		try
		{
			Document doc = getXmlDocument("data");
			PageData data = new PageXmlizer().deXmlizeData(doc);
			data.setAttribute("WikiImportSource", remoteUrl());
			localPage.commit(data);
			addRowToResponse("");
		}
		catch(AuthenticationRequiredException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			addRowToResponse(e.getMessage());
		}
		importCount++;
	}

	private String remoteUrl()
	{
		String remotePathName = PathParser.render(remotePath);
		return "http://" + remoteHostname + ":" + remotePort + "/" + remotePathName;
	}

	public void exitPage()
	{
		remotePath.pop();
		relativePath.pop();
		path.pop();
	}

	public Document getPageTree() throws Exception
	{
		return getXmlDocument("pages");
	}

	private Document getXmlDocument(String documentType) throws Exception
	{

		String remotePathName = PathParser.render(remotePath);
		RequestBuilder builder = new RequestBuilder("/" + remotePathName);
		builder.addInput("responder", "proxy");
		builder.addInput("type", documentType);
		builder.setHostAndPort(remoteHostname, remotePort);
		if(remoteUsername != null)
			builder.addCredentials(remoteUsername, remotePassword);

		ResponseParser parser = ResponseParser.performHttpRequest(remoteHostname, remotePort, builder);

		if(parser.getStatus() == 404)
			throw new Exception("The remote resource, " + remoteUrl() + ", was not found.");
		if(parser.getStatus() == 401)
			throw new AuthenticationRequiredException(remoteUrl());

		String body = parser.getBody();
		Document doc = XmlUtil.newDocument(body);
		return doc;
	}

	private void addRowToResponse(String status) throws Exception
	{
		HtmlTag tag = HtmlUtil.makeDivTag("alternating_row_" + alternate());
		String relativePathName = PathParser.render(relativePath);
		String localPathName = PathParser.render(path);
		tag.add(HtmlUtil.makeLink(localPathName, relativePathName));
		tag.add(" " + status);
		response.add(tag.html());
	}

	private int alternate()
	{
		alternation = alternation % 2 + 1;
		return alternation;
	}

	public void setResponse(ChunkedResponse response)
	{
		this.response = response;
	}

	public void parseUrl(String urlString) throws Exception
	{
		URL url = null;
		try
		{
			url = new URL(urlString);
		}
		catch(MalformedURLException e)
		{
			throw new MalformedURLException(urlString + " is not a valid URL.");
		}

		remoteHostname = url.getHost();
		remotePort = url.getPort();
		if(remotePort == -1)
			remotePort = 80;

		String path = url.getPath();
		if(path.startsWith("/"))
			path = path.substring(1);
		remotePath = PathParser.parse(path);

		if(remotePath == null)
			throw new MalformedURLException("The URL's resource path, " + path + ", is not a valid WikiWord.");
	}

	public SecureOperation getSecureOperation()
	{
		return new SecureWriteOperation();
	}

	private void writeAuthenticationForm(String resource) throws Exception
	{
		HtmlTag html = HtmlUtil.makeDivTag("centered");
		html.add(new HtmlTag("h3", "The wiki at " + resource + " requires authentication."));
		html.add(HtmlUtil.BR);

		HtmlTag form = new HtmlTag("form");
		form.addAttribute("action", request.getResource());
		form.addAttribute("method", "post");
		form.add(HtmlUtil.makeInputTag("hidden", "responder", "import"));
		if(request.hasInput("remoteUrl"))
			form.add(HtmlUtil.makeInputTag("hidden", "remoteUrl", (String) request.getInput("remoteUrl")));

		form.add("remote username: ");
		form.add(HtmlUtil.makeInputTag("text", "remoteUsername"));
		form.add(HtmlUtil.BR);
		form.add("remote password: ");
		form.add(HtmlUtil.makeInputTag("password", "remotePassword"));
		form.add(HtmlUtil.BR);
		form.add(HtmlUtil.makeInputTag("submit", "submit", "Authenticate and Continue Import"));

		html.add(form);
		response.add(html.html());
	}

	private static class AuthenticationRequiredException extends Exception
	{
		public AuthenticationRequiredException(String message)
		{
			super(message);
		}
	}
}
