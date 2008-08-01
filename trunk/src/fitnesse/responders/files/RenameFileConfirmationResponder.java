// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.files;

import fitnesse.FitNesseContext;
import fitnesse.authentication.*;
import fitnesse.html.*;
import fitnesse.http.*;
import fitnesse.responders.SecureResponder;

public class RenameFileConfirmationResponder implements SecureResponder
{
	private String resource;

	public Response makeResponse(FitNesseContext context, Request request) throws Exception
	{
		SimpleResponse response = new SimpleResponse();
		resource = request.getResource();
		String filename = (String) request.getInput("filename");
		response.setContent(makePageContent(filename, context));
		return response;
	}

	private String makePageContent(String filename, FitNesseContext context) throws Exception
	{
		HtmlPage page = context.htmlPageFactory.newPage();
		page.title.use("Rename " + filename);
		page.header.use(HtmlUtil.makeBreadCrumbsWithPageType(resource + filename, "/", "Rename File"));
		page.main.use(makeRenameFormHTML(filename));

		return page.html();
	}

	private HtmlTag makeRenameFormHTML(String filename) throws Exception
	{
		HtmlTag form = HtmlUtil.makeFormTag("get", "/" + resource);
		form.add(HtmlUtil.makeInputTag("hidden", "responder", "renameFile"));

		form.add("Rename " + HtmlUtil.makeBold(filename).html() + " to ");
		form.add(HtmlUtil.BR);
		form.add(HtmlUtil.BR);
		form.add(HtmlUtil.BR);
		form.add(HtmlUtil.makeInputTag("text", "newName", filename));
		form.add(HtmlUtil.makeInputTag("submit", "renameFile", "Rename"));
		form.add(HtmlUtil.makeInputTag("hidden", "filename", filename));

		return form;
	}

	public SecureOperation getSecureOperation()
	{
		return new AlwaysSecureOperation();
	}
}
