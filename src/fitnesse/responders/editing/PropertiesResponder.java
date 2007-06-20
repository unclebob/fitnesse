// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.editing;

import fitnesse.FitNesseContext;
import fitnesse.authentication.*;
import fitnesse.html.*;
import fitnesse.http.*;
import fitnesse.responders.*;
import fitnesse.wiki.*;

import java.util.*;

public class PropertiesResponder implements SecureResponder
{
	public static final String SUITES = "Suites";

	private WikiPage page;
	public PageData pageData;
	private String resource;

	public Response makeResponse(FitNesseContext context, Request request) throws Exception
	{
		SimpleResponse response = new SimpleResponse();
		resource = request.getResource();
		WikiPagePath path = PathParser.parse(resource);
		PageCrawler crawler = context.root.getPageCrawler();
		if(!crawler.pageExists(context.root, path))
		{
			crawler.setDeadEndStrategy(new MockingPageCrawler());
			page = crawler.getPage(context.root, path);
		}
		else
			page = crawler.getPage(context.root, path);
		if(page == null)
			return new NotFoundResponder().makeResponse(context, request);

		pageData = page.getData();
		String html = makeHtml(context);

		response.setContent(html);
		response.setMaxAge(0);

		return response;
	}

	private String makeHtml(FitNesseContext context) throws Exception
	{
		HtmlPage page = context.htmlPageFactory.newPage();
		page.title.use("Properties: " + resource);
		page.header.use(HtmlUtil.makeBreadCrumbsWithPageType(resource, "Page Properties"));
		page.main.use(makeLastModifiedTag());
		page.main.add(makeFormSections());

		return page.html();
	}

	private HtmlTag makeAttributeCheckbox(String attribute, PageData pageData) throws Exception
	{
		HtmlTag checkbox = makeCheckbox(attribute);
		if(pageData.hasAttribute(attribute))
			checkbox.addAttribute("checked", "true");
		return checkbox;
	}

	private HtmlTag makeCheckbox(String attribute)
	{
		HtmlTag checkbox = HtmlUtil.makeInputTag("checkbox", attribute);
		checkbox.tail = " - " + attribute;
		return checkbox;
	}

	private HtmlTag makeLastModifiedTag() throws Exception
	{
		HtmlTag tag = HtmlUtil.makeDivTag("right");
		String username = pageData.getAttribute(WikiPage.LAST_MODIFYING_USER);
		if(username == null || "".equals(username))
			tag.use("Last modified anonymously");
		else
			tag.use("Last modified by " + username);
		return tag;
	}

	private HtmlTag makeFormSections() throws Exception
	{
		TagGroup html = new TagGroup();
		html.add(makePropertiesForm());

		WikiImportProperty importProperty = WikiImportProperty.createFrom(pageData.getProperties());
		if(importProperty != null)
			html.add(makeImportUpdateForm(importProperty));
		else
			html.add(makeImportForm());

		html.add(makeSymbolicLinkSection());

		return html;
	}

	private HtmlTag makePropertiesForm() throws Exception
	{
		HtmlTag form = HtmlUtil.makeFormTag("post", resource);
		form.add(HtmlUtil.makeInputTag("hidden", "responder", "saveProperties"));

		HtmlTag trisection = new HtmlTag("div");
		trisection.addAttribute("style", "height: 200px");
		trisection.add(makeTestActionCheckboxesHtml(pageData));
		trisection.add(makeNavigationCheckboxesHtml(pageData));
		trisection.add(makeSecurityCheckboxesHtml(pageData));
		trisection.add(makeVirtualWikiHtml());
		trisection.add(makeSuitesHtml(pageData));
		form.add(trisection);

		HtmlTag saveButton = HtmlUtil.makeInputTag("submit", "Save", "Save Properties");
		saveButton.addAttribute("accesskey", "s");
		form.add(HtmlUtil.BR);
		form.add(saveButton);
		return form;
	}

	private HtmlTag makeVirtualWikiHtml() throws Exception
	{
		HtmlTag virtualWiki = new HtmlTag("div");
		virtualWiki.addAttribute("style", "float: left;");
		virtualWiki.add("VirtualWiki URL: ");
		HtmlTag deprecated = new HtmlTag("span", "(DEPRECATED)");
		deprecated.addAttribute("style", "color: #FF0000;");
		virtualWiki.add(deprecated);
		virtualWiki.add(HtmlUtil.BR);
		HtmlTag vwInput = HtmlUtil.makeInputTag("text", "VirtualWiki", getVirtualWikiValue(pageData));
		vwInput.addAttribute("size", "40");
		virtualWiki.add(vwInput);
		return virtualWiki;
	}

	private HtmlTag makeImportForm()
	{
		HtmlTag form = HtmlUtil.makeFormTag("post", resource + "#end");
		form.add(HtmlUtil.HR);
		form.add("Wiki Import.  Supply the URL for the wiki you'd like to import.");
		form.add(HtmlUtil.BR);
		form.add("Remote Wiki URL:");
		HtmlTag remoteUrlField = HtmlUtil.makeInputTag("text", "remoteUrl");
		remoteUrlField.addAttribute("size", "40");
		form.add(remoteUrlField);
		form.add(HtmlUtil.BR);
		form.add(HtmlUtil.makeInputTag("checkbox", "autoUpdate", "0"));
		form.add("- Automatically update imported content when executing tests");
		form.add(HtmlUtil.BR);
		form.add(HtmlUtil.makeInputTag("hidden", "responder", "import"));
		form.add(HtmlUtil.makeInputTag("submit", "save", "Import"));
		return form;
	}

	private HtmlTag makeImportUpdateForm(WikiImportProperty importProps) throws Exception
	{
		HtmlTag form = HtmlUtil.makeFormTag("post", resource + "#end");

		form.add(HtmlUtil.HR);
		form.add(new HtmlTag("strong", "Wiki Import Update"));
		form.add(HtmlUtil.BR);
		String buttonMessage = "";
		form.add(HtmlUtil.makeLink(page.getName(), page.getName()));
		if(importProps.isRoot())
		{
			form.add(" imports its subpages from ");
			buttonMessage = "Update Subpages";
		}
		else
		{
			form.add(" imports its content and subpages from ");
			buttonMessage = "Update Content and Subpages";
		}
		form.add(HtmlUtil.makeLink(importProps.getSourceUrl(), importProps.getSourceUrl()));
		form.add(".");
		form.add(HtmlUtil.BR);
		HtmlTag autoUpdateCheckBox = HtmlUtil.makeInputTag("checkbox", "autoUpdate");
		if(importProps.isAutoUpdate())
			autoUpdateCheckBox.addAttribute("checked", "true");
		form.add(autoUpdateCheckBox);

		form.add("- Automatically update imported content when executing tests");
		form.add(HtmlUtil.BR);
		form.add(HtmlUtil.makeInputTag("hidden", "responder", "import"));
		form.add(HtmlUtil.makeInputTag("submit", "save", buttonMessage));

		return form;
	}

	private HtmlTag makeSymbolicLinkSection() throws Exception
	{
		HtmlTag form = HtmlUtil.makeFormTag("get", resource);
		form.add(HtmlUtil.HR);
		form.add(HtmlUtil.makeInputTag("hidden", "responder", "symlink"));
		form.add(new HtmlTag("strong", "Symbolic Links"));

		HtmlTableListingBuilder table = new HtmlTableListingBuilder();
		table.addRow(new HtmlElement[]{new HtmlTag("strong", "Name"), new HtmlTag("strong", "Path to Page"), new HtmlTag("strong", "Action")});
		addSymbolicLinkRows(table);
		addFormRow(table);
		form.add(table.getTable());

		return form;
	}

	private void addFormRow(HtmlTableListingBuilder table) throws Exception
	{
		HtmlTag nameInput = HtmlUtil.makeInputTag("text", "linkName");
		HtmlTag pathInput = HtmlUtil.makeInputTag("text", "linkPath");
		pathInput.addAttribute("size", "40");
		HtmlTag submitButton = HtmlUtil.makeInputTag("submit", "submit", "Create Symbolic Link");
		table.addRow(new HtmlElement[]{nameInput, pathInput, submitButton});
	}

	private void addSymbolicLinkRows(HtmlTableListingBuilder table) throws Exception
	{
		WikiPageProperty symLinksProperty = pageData.getProperties().getProperty(SymbolicPage.PROPERTY_NAME);
		if(symLinksProperty == null)
			return;
		Set symbolicLinkNames = symLinksProperty.keySet();
		for(Iterator iterator = symbolicLinkNames.iterator(); iterator.hasNext();)
		{
			String linkName = (String) iterator.next();
			HtmlElement nameItem = new RawHtml(linkName);
			HtmlElement pathItem = makeHtmlForSymbolicPath(symLinksProperty, linkName);
			HtmlTag actionItem = HtmlUtil.makeLink(resource + "?responder=symlink&removal=" + linkName, "remove");
			table.addRow(new HtmlElement[]{nameItem, pathItem, actionItem});
		}
	}

	private HtmlElement makeHtmlForSymbolicPath(WikiPageProperty symLinksProperty, String linkName)
	{
		String linkPath = symLinksProperty.get(linkName);
		WikiPagePath wikiPagePath = PathParser.parse(linkPath);
		if(wikiPagePath != null)
			return HtmlUtil.makeLink(PathParser.render(wikiPagePath), PathParser.render(wikiPagePath));
		else
			return new RawHtml(linkPath);
	}

	public static String getVirtualWikiValue(PageData data) throws Exception
	{
		String value = data.getAttribute(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE);
		if(value == null)
			return "";
		else
			return value;
	}

	public SecureOperation getSecureOperation()
	{
		return new SecureReadOperation();
	}

	public HtmlTag makeTestActionCheckboxesHtml(PageData pageData) throws Exception
	{
		return makeAttributeCheckboxesHtml("Actions:", WikiPage.ACTION_ATTRIBUTES, pageData);
	}

	public HtmlElement makeNavigationCheckboxesHtml(PageData pageData) throws Exception
	{
		return makeAttributeCheckboxesHtml("Navigation:", WikiPage.NAVIGATION_ATTRIBUTES, pageData);
	}

	public HtmlTag makeSecurityCheckboxesHtml(PageData pageData) throws Exception
	{
		return makeAttributeCheckboxesHtml("Security:", WikiPage.SECURITY_ATTRIBUTES, pageData);
	}

	public HtmlTag makeSuitesHtml(PageData pageData) throws Exception
	{
		HtmlTag div = new HtmlTag("div");
		div.addAttribute("style", "float: left;");
		div.add("Suites:");

		String suites = "";
		WikiPageProperty suitesProp = pageData.getProperties().getProperty(SUITES);
		if(suitesProp != null)
		{
			suites = suitesProp.getValue();
		}

		div.add(HtmlUtil.BR);

		div.add(HtmlUtil.makeInputTag("text", "Suites", suites));
		return div;
	}

	private HtmlTag makeAttributeCheckboxesHtml(String label, String[] attributes, PageData pageData)
		throws Exception
	{
		HtmlTag div = new HtmlTag("div");
		div.addAttribute("style", "float: left; width: 150px;");

		div.add(label);
		for(int i = 0; i < attributes.length; i++)
		{
			String attribute = attributes[i];
			div.add(HtmlUtil.BR);
			div.add(makeAttributeCheckbox(attribute, pageData));
		}
		return div;
	}

}
