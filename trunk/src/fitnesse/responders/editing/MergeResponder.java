// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.editing;

import fitnesse.*;
import fitnesse.components.SaveRecorder;
import fitnesse.html.*;
import fitnesse.http.*;
import fitnesse.wiki.*;
import fitnesse.wikitext.Utils;

public class MergeResponder implements Responder
{
	private Request request;
	private String newContent;
	private String existingContent;
	private String resource;

	public MergeResponder(Request request)
	{
		this.request = request;
	}

	public Response makeResponse(FitNesseContext context, Request request) throws Exception
	{
		SimpleResponse response = new SimpleResponse();
		resource = this.request.getResource();
		WikiPagePath path = PathParser.parse(resource);
		WikiPage page = context.root.getPageCrawler().getPage(context.root, path);
		existingContent = page.getData().getContent();
		newContent = (String) this.request.getInput(EditResponder.CONTENT_INPUT_NAME);

		response.setContent(makePageHtml(context));

		return response;
	}

	private String makePageHtml(FitNesseContext context) throws Exception
	{
		HtmlPage page = context.htmlPageFactory.newPage();
		page.title.use("Merge " + resource);
		page.header.use(HtmlUtil.makeBreadCrumbsWithPageType(resource, "Merge Changes"));
		page.main.use(makeRightColumn());
		return page.html();
	}

	private String makeRightColumn() throws Exception
	{
		HtmlTag form = HtmlUtil.makeFormTag("post", resource);
		form.add(HtmlUtil.makeInputTag("hidden", "responder", "saveData"));
		form.add(HtmlUtil.makeInputTag("hidden", EditResponder.SAVE_ID, String.valueOf(SaveRecorder.newIdNumber())));
		form.add(HtmlUtil.makeInputTag("hidden", EditResponder.TICKET_ID, String.valueOf(SaveRecorder.newTicket())));
		HtmlTag title = HtmlUtil.makeDivTag("centered");
		title.use("This page has been recently modified.  You may want to merge existing page content into your changes.");
		form.add(title);
		form.add(makeMergeNewDivTag());
		form.add(makeMergeOldDivTag());
		form.add(addHiddenAttributes());
		return form.html();
	}

	private HtmlTag makeMergeOldDivTag()
	{
		HtmlTag mergeOld = HtmlUtil.makeDivTag("merge_old");
		mergeOld.add("Existing Content (read only)");
		mergeOld.add(HtmlUtil.BR);
		mergeOld.add(makeOldContentTextArea());

		return mergeOld;
	}

	private HtmlTag makeOldContentTextArea()
	{
		HtmlTag oldContentTextArea = new HtmlTag("textarea");
		oldContentTextArea.addAttribute("name", "oldContent");
		oldContentTextArea.addAttribute("rows", "25");
		oldContentTextArea.addAttribute("cols", "50");
		oldContentTextArea.addAttribute("readonly", "");
		oldContentTextArea.add(Utils.escapeText(existingContent));
		return oldContentTextArea;
	}

	private HtmlTag makeMergeNewDivTag()
	{
		HtmlTag mergeNew = HtmlUtil.makeDivTag("merge_new");
		mergeNew.add("Your Changes");
		mergeNew.add(HtmlUtil.BR);
		mergeNew.add(makeContentTextArea());
		mergeNew.add(makeInputTagWithAccessKey());
		return mergeNew;
	}

	private HtmlTag makeInputTagWithAccessKey()
	{
		HtmlTag input = HtmlUtil.makeInputTag("submit", "submit", "Save");
		input.addAttribute("accesskey", "s");
		return input;
	}

	private HtmlTag makeContentTextArea()
	{
		HtmlTag contentTextArea = new HtmlTag("textarea");
		contentTextArea.addAttribute("name", EditResponder.CONTENT_INPUT_NAME);
		contentTextArea.addAttribute("rows", "25");
		contentTextArea.addAttribute("cols", "50");
		contentTextArea.add(newContent);
		return contentTextArea;
	}

	private String addHiddenAttributes()
	{
		StringBuffer buffer = new StringBuffer();
		for(int i = 0; i < WikiPage.NON_SECURITY_ATTRIBUTES.length; i++)
		{
			String attribute = WikiPage.NON_SECURITY_ATTRIBUTES[i];
			if(request.hasInput(attribute))
				buffer.append("<input type=\"hidden\" name=\"" + attribute + "\" value=\"On\">");
		}
		return buffer.toString();
	}
}
