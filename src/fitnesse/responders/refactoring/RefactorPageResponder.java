// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.refactoring;

import fitnesse.*;
import fitnesse.responders.SecureResponder;
import fitnesse.authentication.*;
import fitnesse.html.*;
import fitnesse.http.*;

public class RefactorPageResponder implements SecureResponder
{
	private String resource;

	public Response makeResponse(FitNesseContext context, Request request) throws Exception
	{
		resource = request.getResource();
		SimpleResponse response = new SimpleResponse();
		response.setContent(html(context));
		return response;
	}

	public String html(FitNesseContext context) throws Exception
	{
		HtmlPage html = context.htmlPageFactory.newPage();
		html.title.use("Refactor: " + resource);
		html.header.use(HtmlUtil.makeBreadCrumbsWithPageType(resource, "Refactor"));
		html.main.use(mainContent());
		return html.html();
	}

	private HtmlTag mainContent() throws Exception
	{
		TagGroup group = new TagGroup();
		group.add(deletePageForm());
		group.add(renamePageForm());
		group.add(movePageForm());
		return group;
	}

	private HtmlTag deletePageForm() throws Exception
	{
		TagGroup group = new TagGroup();
		group.add(makeHeaderTag("Delete:"));
		group.add("Delete this entire sub-wiki.");
		group.add(makeDeletePageForm());
		return group;
	}

	private HtmlTag makeHeaderTag(String content) throws Exception
	{
		return new HtmlTag("h3", content);
	}

	private HtmlTag makeDeletePageForm() throws Exception
	{
		HtmlTag form = HtmlUtil.makeFormTag("get", resource);
		form.add(HtmlUtil.makeInputTag("submit", "", "Delete Page"));
		form.add(HtmlUtil.makeInputTag("hidden", "responder", "deletePage"));
		return form;
	}

	private HtmlTag movePageForm() throws Exception
	{
		TagGroup group = new TagGroup();
		group.add(HtmlUtil.BR);
		group.add(makeHeaderTag("Move:"));
		group.add("Moving this page will find all references and change them accordingly.");
		group.add(makeMovePageForm());
		return group;
	}

	private HtmlTag makeMovePageForm() throws Exception
	{
		HtmlTag form = HtmlUtil.makeFormTag("get", resource);
		form.add(HtmlUtil.makeInputTag("hidden", "responder", "movePage"));
		form.add("New Location: ");
		form.add(HtmlUtil.makeInputTag("text", "newLocation", ""));
		form.add(HtmlUtil.BR);
		form.add(HtmlUtil.makeInputTag("submit", "", "Move Page"));
		return form;
	}

	private HtmlTag renamePageForm() throws Exception
	{
		TagGroup group = new TagGroup();
		group.add(HtmlUtil.BR);
		group.add(makeHeaderTag("Rename:"));
		group.add("Renaming this page will find all references and change them accordingly.");
		group.add(makeRenamePageForm());
		return group;
	}

	private HtmlTag makeRenamePageForm() throws Exception
	{
		HtmlTag form = HtmlUtil.makeFormTag("get", resource);
		form.add(HtmlUtil.makeInputTag("hidden", "responder", "renamePage"));
		form.add("  New Name: ");
		form.add(HtmlUtil.makeInputTag("text", "newName", ""));
		form.add(HtmlUtil.BR);
		form.add(HtmlUtil.makeInputTag("submit", "", "Rename Page"));
		return form;
	}

	public SecureOperation getSecureOperation()
	{
		return new AlwaysSecureOperation();
	}
}
