// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.html;

import fitnesse.responders.run.SuiteResponder;
import fitnesse.wiki.*;

public class HtmlUtil
{
	public static HtmlElement BR = new RawHtml("<br/>");
	public static HtmlElement HR = new RawHtml("<hr/>");
	public static HtmlElement NBSP = new RawHtml("&nbsp;");
	public static HtmlElement P = new RawHtml("<p>");
	public static final boolean NO_NEW_WINDOW = false;
	public static final String ENDL = System.getProperty("line.separator");

	public static HtmlTag makeDivTag(String divClass)
	{
		HtmlTag div = new HtmlTag("div");
		div.addAttribute("class", divClass);
		div.add("");
		return div;
	}

	public static void addTitles(HtmlPage page, String title)
	{
		page.title.use(title);
		HtmlTag span = new HtmlTag("span");
		span.addAttribute("class", "page_title");
		span.add(title);
		page.header.use(span);
	}

	public static HtmlTag makeBold(String content)
	{
		HtmlTag bold = new HtmlTag("b");
		bold.add(content);
		return bold;
	}

	public static HtmlTag makeItalic(String content)
	{
		HtmlTag italic = new HtmlTag("i");
		italic.add(content);
		return italic;
	}

	public static HtmlTag makeSpanTag(String spanClass, String content)
	{
		HtmlTag span = new HtmlTag("span");
		span.addAttribute("class", spanClass);
		span.add(content);
		return span;
	}

	public static HtmlTag makeFormTag(String method, String action)
	{
		HtmlTag formTag = new HtmlTag("form");
		formTag.addAttribute("method", method);
		formTag.addAttribute("action", action);
		return formTag;
	}

	public static HtmlTag makeAnchorTag(String name)
	{
		HtmlTag anchorTag = new HtmlTag("a", " ");
		anchorTag.addAttribute("name", name);
		return anchorTag;
	}

	public static HtmlTag makeActionLink(String action, String name, String inputName, String accessKey, boolean newWindow)
	{
		TagGroup group = new TagGroup();
		String href = action;
		if(inputName != null)
			href = href + "?" + inputName;

		HtmlTag link = new HtmlTag("a");
		link.addAttribute("href", href);
		if(newWindow)
			link.addAttribute("target", "newWindow");
		link.addAttribute("accesskey", accessKey);
		link.add(name);

		group.add(new HtmlComment(name + " button"));
		group.add(link);
		return group;
	}

	public static HtmlTag makeInputTag(String type, String name, String value)
	{
		HtmlTag input = makeInputTag(type, name);
		input.addAttribute("value", value);
		return input;
	}

	public static HtmlTag makeInputTag(String type, String name)
	{
		HtmlTag input = new HtmlTag("input");
		input.addAttribute("type", type);
		input.addAttribute("name", name);
		return input;
	}

	public static HtmlTag makeOptionTag(String value, String text)
	{
		HtmlTag option = new HtmlTag("option");
		option.addAttribute("value", value);
		option.add(text);

		return option;
	}

	public static HtmlTag makeLink(String href, String text)
	{
		return makeLink(href, new RawHtml(text));
	}

	public static HtmlTag makeLink(String href, HtmlElement content)
	{
		HtmlTag link = new HtmlTag("a");
		link.addAttribute("href", href);
		link.add(content);
		return link;
	}

	public static TagGroup makeBreadCrumbsWithCurrentPageLinked(String path) throws Exception
	{
		return makeBreadCrumbsWithCurrentPageLinked(path, ".");
	}

	public static HtmlTag makeBreadCrumbsWithCurrentPageNotLinked(String trail) throws Exception
	{
		return makeBreadCrumbsWithCurrentPageNotLinked(trail, ".");
	}

	public static TagGroup makeBreadCrumbsWithCurrentPageLinked(String path, String separator) throws Exception
	{
		TagGroup tagGroup = new TagGroup();
		String[] crumbs = path.split("[" + separator + "]");
		String trail = makeAllButLastCrumb(crumbs, separator, tagGroup);
		tagGroup.add(getLastCrumbAsLink(crumbs, trail));
		return tagGroup;
	}

	public static HtmlTag makeBreadCrumbsWithCurrentPageNotLinked(String path, String separator) throws Exception
	{
		TagGroup tagGroup = new TagGroup();
		String[] crumbs = path.split("[" + separator + "]");
		makeAllButLastCrumb(crumbs, separator, tagGroup);
		tagGroup.add(getLastCrumbAsText(crumbs));
		return tagGroup;
	}

	private static HtmlTag getLastCrumbAsLink(String[] crumbs, String trail) throws Exception
	{
		String crumb = getLastCrumb(crumbs);
		HtmlTag link = makeLink("/" + trail + crumb, crumb);
		link.head = HtmlUtil.BR.html();
		link.addAttribute("class", "page_title");
		return link;
	}

	private static String getLastCrumb(String[] crumbs)
	{
		String crumb = "";
		if(crumbs.length > 0)
			crumb = crumbs[crumbs.length - 1];
		return crumb;
	}

	private static HtmlTag getLastCrumbAsText(String[] crumbs) throws Exception
	{
		String crumb = getLastCrumb(crumbs);
		HtmlTag thisPage = new HtmlTag("span", crumb);
		thisPage.addAttribute("class", "page_title");
		thisPage.head = HtmlUtil.BR.html();
		return thisPage;
	}

	public static HtmlTag makeBreadCrumbsWithPageType(String trail, String type) throws Exception
	{
		return makeBreadCrumbsWithPageType(trail, ".", type);
	}

	public static HtmlTag makeBreadCrumbsWithPageType(String trail, String separator, String type) throws Exception
	{
		TagGroup group = makeBreadCrumbsWithCurrentPageLinked(trail, separator);
		group.add(HtmlUtil.BR);
		group.add(HtmlUtil.makeSpanTag("page_type", type));
		return group;
	}

	private static String makeAllButLastCrumb(String[] crumbs, String separator, TagGroup group)
	{
		String trail = "";
		for(int i = 0; i < crumbs.length - 1; i++)
		{
			String crumb = crumbs[i];
			HtmlTag link = makeLink("/" + trail + crumb, crumb);
			link.tail = separator;
			trail = trail + crumb + separator;
			group.add(link);
		}
		return trail;
	}

	public static HtmlTag makeActions(PageData pageData) throws Exception
	{
		WikiPage page = pageData.getWikiPage();

		WikiPagePath localPagePath = page.getPageCrawler().getFullPath(page);
		String localPageName = PathParser.render(localPagePath);
		String localOrRemotePageName = localPageName;
		boolean newWindowIfRemote = NO_NEW_WINDOW;
		if(page instanceof ProxyPage)
		{
			ProxyPage proxyPage = (ProxyPage) page;
			localOrRemotePageName = proxyPage.getThisPageUrl();
			newWindowIfRemote = true;
		}
		return makeActions(pageData, localPageName, localOrRemotePageName, newWindowIfRemote);

	}

	public static HtmlTag makeActions(PageData pageData, String localPageName, String localOrRemotePageName, boolean newWindowIfRemote) throws Exception
	{
		TagGroup actions = new TagGroup();
		if(pageData.hasAttribute("Test"))
			actions.add(makeActionLink(localPageName, "Test", "test", "t", NO_NEW_WINDOW));
		if(pageData.hasAttribute("Suite"))
			actions.add(makeActionLink(localPageName, "Suite", "suite", "", NO_NEW_WINDOW));
		if(pageData.hasAttribute("Test") || pageData.hasAttribute("Suite"))
			actions.add(makeNavBreak());
		if(pageData.hasAttribute("Edit"))
			actions.add(makeActionLink(localOrRemotePageName, "Edit", "edit", "e", newWindowIfRemote));
		if(pageData.hasAttribute("Versions"))
			actions.add(makeActionLink(localOrRemotePageName, "Versions", "versions", "v", newWindowIfRemote));
		if(pageData.hasAttribute("Properties"))
			actions.add(makeActionLink(localOrRemotePageName, "Properties", "properties", "p", newWindowIfRemote));
		if(pageData.hasAttribute("Refactor"))
			actions.add(makeActionLink(localOrRemotePageName, "Refactor", "refactor", "r", newWindowIfRemote));
		if(pageData.hasAttribute("WhereUsed"))
			actions.add(makeActionLink(localOrRemotePageName, "Where Used", "whereUsed", "w", NO_NEW_WINDOW));
		actions.add(makeNavBreak());
		if(pageData.hasAttribute("RecentChanges"))
			actions.add(makeActionLink("/RecentChanges", "RecentChanges", null, "", NO_NEW_WINDOW));
		if(pageData.hasAttribute("Files"))
			actions.add(makeActionLink("/files", "Files", null, "f", NO_NEW_WINDOW));
		if(pageData.hasAttribute("Search"))
			actions.add(makeActionLink("?searchForm", "Search", null, "s", NO_NEW_WINDOW));

		return actions;
	}

	public static HtmlTag makeNavBreak()
	{
		HtmlTag navBreak = new HtmlTag("div");
		navBreak.addAttribute("class", "nav_break");
		navBreak.add("&nbsp;");
		return navBreak;
	}

	public static String makeNormalWikiPageContent(PageData pageData) throws Exception
	{
		String content = testableHtml(pageData);
		return addHeaderAndFooter(pageData.getWikiPage(), content);
	}

	public static String addHeaderAndFooter(WikiPage page, String content) throws Exception
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append(getHtmlOfInheritedPage("PageHeader", page));
		buffer.append(content);
		buffer.append(getHtmlOfInheritedPage("PageFooter", page));
		return buffer.toString();
	}

	public static String testableHtml(PageData pageData) throws Exception
	{
		return testableHtml(pageData, false);
	}

	public static String testableHtml(PageData pageData, boolean includeSuiteSetup) throws Exception
	{
		WikiPage wikiPage = pageData.getWikiPage();
		StringBuffer buffer = new StringBuffer();
		if(pageData.hasAttribute("Test"))
		{
			if(includeSuiteSetup)
			{
				WikiPage suiteSetup = PageCrawlerImpl.getInheritedPage(SuiteResponder.SUITE_SETUP_NAME, wikiPage);
				if(suiteSetup != null)
				{
					WikiPagePath pagePath = suiteSetup.getPageCrawler().getFullPath(suiteSetup);
					String pagePathName = PathParser.render(pagePath);
					buffer.append("!include -setup .").append(pagePathName).append("\n");
				}
			}
			WikiPage setup = PageCrawlerImpl.getInheritedPage("SetUp", wikiPage);
			if(setup != null)
			{
				WikiPagePath setupPath = wikiPage.getPageCrawler().getFullPath(setup);
				String setupPathName = PathParser.render(setupPath);
				buffer.append("!include -setup .").append(setupPathName).append("\n");
			}
		}
		buffer.append(pageData.getContent());
		if(pageData.hasAttribute("Test"))
		{
			WikiPage teardown = PageCrawlerImpl.getInheritedPage("TearDown", wikiPage);
			if(teardown != null)
			{
				WikiPagePath tearDownPath = wikiPage.getPageCrawler().getFullPath(teardown);
				String tearDownPathName = PathParser.render(tearDownPath);
				buffer.append("\n").append("!include -teardown .").append(tearDownPathName).append("\n");
			}
			if(includeSuiteSetup)
			{
				WikiPage suiteTeardown = PageCrawlerImpl.getInheritedPage(SuiteResponder.SUITE_TEARDOWN_NAME, wikiPage);
				if(suiteTeardown != null)
				{
					WikiPagePath pagePath = suiteTeardown.getPageCrawler().getFullPath(suiteTeardown);
					String pagePathName = PathParser.render(pagePath);
					buffer.append("!include -teardown .").append(pagePathName).append("\n");
				}
			}
		}
		pageData.setContent(buffer.toString());
		return pageData.getHtml();
	}

	public static String getHtmlOfInheritedPage(String pageName, WikiPage context) throws Exception
	{
		return getLabeledHtmlOfInheritedPage(pageName, context, "");
	}

	public static String getLabeledHtmlOfInheritedPage(String pageName, WikiPage context, String label) throws Exception
	{
		WikiPage inheritedPage = PageCrawlerImpl.getInheritedPage(pageName, context);
		if(inheritedPage != null)
		{
			PageData data = inheritedPage.getData();
			if(label != null && label.length() > 1)
			{
				WikiPagePath inheritedPagePath = context.getPageCrawler().getFullPath(inheritedPage);
				String inheritedPagePathName = PathParser.render(inheritedPagePath);
				String fullLabel = "!meta " + label + ": ." + inheritedPagePathName + "\n";
				String newContent = fullLabel + data.getContent();
				data.setContent(newContent);
			}
			return data.getHtml(context);
		}
		else
			return "";
	}

	public static String metaText(String text)
	{
		return "<span class=\"meta\">" + text + "</span>";
	}

	public static HtmlTag makeJavascriptLink(String jsFile)
	{
		HtmlTag scriptTag = new HtmlTag("script");
		scriptTag.addAttribute("src", jsFile);
		scriptTag.addAttribute("type", "text/javascript");
		scriptTag.use("");
		return scriptTag;
	}
}
